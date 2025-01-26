package com.zdrovi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.Body;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import com.zdrovi.commons.*;
import com.zdrovi.commons.DatabaseVerifier.Repositories;
import com.zdrovi.commons.EntityRepository.TestCourseSetup;
import com.zdrovi.domain.entity.*;
import com.zdrovi.domain.repository.*;
import com.zdrovi.google.model.Form;
import com.zdrovi.google.model.FormResponse;
import com.zdrovi.google.model.ListFormResponsesResponse;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.PropertyAccessor.FIELD;
import static com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.zdrovi.commons.TestConstants.*;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
        classes = SchedulerApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("test")
@Testcontainers
class SchedulerApplicationTest {

    public static final String FORM_ID = "JUST_FORM_ID";

    @Container
    private static final PostgreSQLContainer<?> postgres = ImageRepository.getPostgresImage();

    static WireMockServer wireMockServer = new WireMockServer(2137);

    private static GreenMail greenMail;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ContentRepository contentRepository;

    @Autowired
    private UserCourseRepository userCourseRepository;

    @Autowired
    private CourseContentRepository courseContentRepository;

    @Autowired
    private EntityRepository entityRepository;

    @Autowired
    private DatabaseVerifier databaseVerifier;

    @Autowired
    private UserLabelRepository userLabelRepository;

    @Autowired
    private ContentLabelRepository contentLabelRepository;

    @Autowired
    private LabelRepository labelRepository;

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeAll
    @SneakyThrows
    static void beforeAll() {
        greenMail = new GreenMail(new ServerSetup(3025, null, "smtp"))
                .withConfiguration(GreenMailConfiguration.aConfig().withDisabledAuthentication());
        greenMail.start();

        wireMockServer.start();

        var mapper = new ObjectMapper();
        mapper.setVisibility(FIELD, ANY);
        mapper.configure(FAIL_ON_EMPTY_BEANS, false);

        Form form = Form.builder()
                .formId(FORM_ID)
                .items(ApiMockRepository.createItems())
                .build();

        WireMock.configureFor(2137);
        WireMock.stubFor(WireMock.get(urlEqualTo("/v1/forms/" + FORM_ID + "?access_token=token"))
                .atPriority(1)
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withResponseBody(Body.fromJsonBytes(mapper.writeValueAsBytes(form))))
        );

        ListFormResponsesResponse responses = ListFormResponsesResponse.builder()
                .responses(
                        List.of(FormResponse.builder()
                                .formId(FORM_ID)
                                .answers(ApiMockRepository.getAnswers())
                                .build())
                )
                .build();

        WireMock.stubFor(WireMock.get(urlEqualTo("/v1/forms/" + FORM_ID +
                                                 "/responses?access_token=token&filter=timestamp%20%3E%3D%201999-12-31T23%3A00%3A00Z"))
                .atPriority(1)
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withResponseBody(Body.fromJsonBytes(mapper.writeValueAsBytes(responses))))
        );

        ListFormResponsesResponse emptyResponses = ListFormResponsesResponse.builder()
                .responses(null)
                .build();

        WireMock.stubFor(WireMock.get(urlMatching("/v1/forms/" + FORM_ID + "/responses.*"))
                .atPriority(2)
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withResponseBody(Body.fromJsonBytes(mapper.writeValueAsBytes(emptyResponses))))
        );
    }

    @BeforeEach
    void beforeEach() {
        ensureGreenMailRunning();
        cleanupDatabases();
        greenMail.reset();
    }

    @AfterAll
    static void afterAll() {
        greenMail.stop();
        wireMockServer.stop();
    }

    @Nested
    class WhenTestingSendingMails {

        @Test
        void shouldSendEmailAndIncrementStageForUserWithUnfinishedCourse() {
            // Given
            User user = entityRepository.setupUserWithCourseAndContent(
                    TITLE,
                    NAME,
                    EMAIL,
                    CONTENT
            );
            UserCourse userCourse = user.getUserCourses().iterator().next();
            Content content = userCourse.getCourse().getCourseContents().iterator().next().getContent();

            databaseVerifier.captureInitialState();

            // When/Then
            await()
                    .atMost(AWAIT_TIMEOUT, SECONDS)
                    .untilAsserted(() -> {
                        MimeMessage message = assertEmailReceived();
                        assertEmailContent(message, user, content);
                        assertStageIncremented(userCourse.getId(), 2);
                    });

            databaseVerifier.verifyDatabaseIntegrity();
        }

        @Test
        @SneakyThrows
        void shouldCompleteEntireCoursePathForUser() {
            // Given
            TestCourseSetup courseSetup = entityRepository.setupCompleteCoursePath(NAME, EMAIL);
            databaseVerifier.captureInitialState();

            verifyStageCompletion(courseSetup.user(), courseSetup.content1(), 1);

            greenMail.reset();
            verifyStageCompletion(courseSetup.user(), courseSetup.content2(), 2);

            greenMail.reset();
            verifyNoMoreEmails();

            databaseVerifier.verifyDatabaseIntegrity();
        }

        @Test
        void shouldHandleNoCoursesForUser() {
            // Given
            entityRepository.createBasicUser(NAME, EMAIL);
            databaseVerifier.captureInitialState();

            // When/Then
            await()
                    .atMost(AWAIT_TIMEOUT, SECONDS)
                    .untilAsserted(() -> {
                        assertThat(greenMail.getReceivedMessages()).isEmpty();
                        assertThat(userCourseRepository.findAll()).isEmpty();
                    });

            databaseVerifier.verifyDatabaseIntegrity();
        }

        @Test
        void shouldHandleEmailAuthenticationFailure() {
            // Given
            greenMail.stop();
            User user = entityRepository.setupUserWithCourseAndContent(
                    TITLE,
                    NAME,
                    EMAIL,
                    CONTENT
            );
            UUID userCourseId = user.getUserCourses().iterator().next().getId();
            databaseVerifier.captureInitialState();

            // When/Then
            verifyStageUnchanged(userCourseId, 1);

            greenMail.start();

            databaseVerifier.verifyDatabaseIntegrity();
        }

        @Test
        void shouldHandleEmailNetworkIssue() {
            // Given
            greenMail.stop();
            User user = entityRepository.setupUserWithCourseAndContent(
                    TITLE,
                    NAME,
                    EMAIL,
                    CONTENT
            );
            UUID userCourseId = user.getUserCourses().iterator().next().getId();
            databaseVerifier.captureInitialState();

            // When/Then
            verifyStageUnchanged(userCourseId, 1);

            databaseVerifier.verifyDatabaseIntegrity();
        }
    }

    @Nested
    class WhenTestingEvaluators {

        @Test
        void shouldCreateCourseForUser() {
            User user = entityRepository.setupUserWithLabelAndContent(
                    TITLE,
                    NAME,
                    EMAIL,
                    CONTENT,
                    MAX_MATCHING_VALUES
            );

            databaseVerifier.captureInitialState();

            verifyCourseCreated(user.getId());

            databaseVerifier.verifyDatabaseIntegrity(List.of(
                    Repositories.User
            ));
        }

        @Test
        void shouldNotCreateCourseIfUsersNotExist() {
            databaseVerifier.captureInitialState();
            await()
                    .pollDelay(3, SECONDS)
                    .untilAsserted(() -> assertThat(true).isTrue());
            databaseVerifier.verifyDatabaseIntegrity();
        }

        @Test
        void shouldNotCreateCourseIfUserHasActiveCourse() {
            User user = entityRepository.setupUserWithLabelAndContent(
                    TITLE,
                    NAME,
                    EMAIL,
                    CONTENT,
                    MAX_MATCHING_VALUES
            );
            entityRepository.setupActiveCourseForUser(user);

            databaseVerifier.captureInitialState();
            await()
                    .pollDelay(3, SECONDS)
                    .untilAsserted(() -> assertThat(true).isTrue());
            databaseVerifier.verifyDatabaseIntegrity();
        }

        @Test
        void shouldNotCreateCourseIfContentPurelyMatch() {
            User user = entityRepository.setupUserWithLabelAndContent(
                    TITLE,
                    NAME,
                    EMAIL,
                    CONTENT,
                    ZEROED_MATCHING_VALUES
            );

            databaseVerifier.captureInitialState();
            await()
                    .pollDelay(3, SECONDS)
                    .untilAsserted(() -> assertThat(true).isTrue());
            databaseVerifier.verifyDatabaseIntegrity();
        }
    }

    @Nested
    class FullTest {

        @Test
        @SneakyThrows
        void shouldAddUserToListAndPerformCourse() {
            List<Content> contents = entityRepository.setupContentWithLabels(TITLE, CONTENT, 4, (short) 100);
            databaseVerifier.captureInitialState();

            AtomicReference<User> user = new AtomicReference<>();

            // should add user to mailing list only once
            // should create only one course for user
            await()
                    .atMost(AWAIT_TIMEOUT, SECONDS)
                    .untilAsserted(() -> {
                        List<User> users = userRepository.findAll();
                        assertThat(users).hasSize(1);
                        user.set(users.stream().findFirst().get());
                        assertThat(user.get().getEmail()).isEqualTo(EMAIL);
                        assertThat(user.get().getName()).isEqualTo(NAME);

                        List<UserLabel> userLabels = userLabelRepository.findAllByUser(user.get());
                        assertThat(userLabels).hasSize(8);
                    });

            // should create 2 courses with size 3 and 1 because we have 4 contents
            // should send emails and increment stage for each created content
            // should finish 2 courses
            await()
                    .pollDelay(AWAIT_TIMEOUT, SECONDS)
                    .atMost(2 * AWAIT_TIMEOUT, SECONDS)
                    .untilAsserted(() -> {
                        List<Course> courses = courseRepository.findAll();
                        assertThat(courses).hasSize(2);
                        assertThat(courses).anyMatch(c -> c.getStages() == 1);
                        assertThat(courses).anyMatch(c -> c.getStages() == 3);

                        List<CourseContent> courseContents = courseContentRepository.findAll();
                        assertThat(courseContents).hasSize(4);

                        List<UserCourse> userCourses = userCourseRepository.findAll();
                        assertThat(userCourses).hasSize(2);
                        assertThat(userCourses).anyMatch(uc -> uc.getStage() == 1);
                        assertThat(userCourses).anyMatch(uc -> uc.getStage() == 3);
                    });


            Thread.sleep(3000);
            MimeMessage[] emailMessages = greenMail.getReceivedMessages();

            assertThat(emailMessages).hasSize(5);

            assertThat(emailMessages[0].getSubject()).isEqualTo("Welcome to Zdrovi");
            assertEmailContent(emailMessages[1], user.get(), contents.get(0));
            assertEmailContent(emailMessages[2], user.get(), contents.get(1));
            assertEmailContent(emailMessages[3], user.get(), contents.get(2));
            assertEmailContent(emailMessages[4], user.get(), contents.get(3));

            greenMail.reset();
            verifyNoMoreEmails();
        }
    }

    private void verifyCourseCreated(UUID user_id) {
        await()
                .atMost(AWAIT_TIMEOUT, SECONDS)
                .untilAsserted(() -> {
                    var user_opt = userRepository.findById(user_id);
                    assertThat(user_opt.isPresent()).isTrue();

                    var user = user_opt.get();
                    var user_courses = userCourseRepository.findAllByUser(user);
                    assertThat(user_courses).hasSize(1);
                    var user_course = user_courses.getFirst();
                    var course = courseRepository.findById(user_course.getCourse().getId()).orElseThrow();
                    assertThat(course.getStages()).isEqualTo(3);

                    var cc = courseContentRepository.findAllByCourse(course);

                    var contents = cc
                            .stream()
                            .map(c -> contentRepository.findById(c.getContent().getId()).orElseThrow())
                            .toList();
                    assertThat(contents).hasSize(3);
                });
    }

    // Helper Methods
    private void verifyStageCompletion(User user, Content content, int expectedStage) {
        await()
                .atMost(AWAIT_TIMEOUT, SECONDS)
                .untilAsserted(() -> {
                    MimeMessage message = assertEmailReceived();
                    assertEmailContent(message, user, content);
                    UserCourse updatedUserCourse = userCourseRepository.findById(
                            user.getUserCourses().iterator().next().getId()
                    ).orElseThrow();
                    assertThat(updatedUserCourse.getStage()).isEqualTo(expectedStage);
                });
    }

    private void verifyStageUnchanged(UUID userCourseId, int expectedStage) {
        await()
                .atMost(AWAIT_TIMEOUT, SECONDS)
                .untilAsserted(() -> {
                    UserCourse userCourse = userCourseRepository.findById(userCourseId).orElseThrow();
                    assertThat(userCourse.getStage()).isEqualTo(expectedStage);
                });
    }

    private void verifyNoMoreEmails() {
        await()
                .during(AWAIT_TIMEOUT, SECONDS)
                .atMost(AWAIT_TIMEOUT + 1, SECONDS)
                .until(() -> greenMail.getReceivedMessages().length == 0);
    }

    private void assertEmailContent(MimeMessage message, User user, Content content) throws MessagingException {
        assertThat(message.getAllRecipients()[0].toString()).isEqualTo(user.getEmail());
        assertThat(message.getSubject()).isEqualTo(content.getTitle());

        String emailContent = GreenMailMessageDecoder.decodeContent(message);

        assertTrue(HtmlUtils.verifyHtmlEqual(
                HtmlRepository.getExpectedMessage(
                        content.getTitle(),
                        GREETING,
                        content.getMailContent(),
                        SIGNATURE,
                        UNSUBSCRIBE_BASE_URL
                ),
                emailContent
        ));
    }

    private MimeMessage assertEmailReceived() {
        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        assertThat(receivedMessages).hasSize(1);
        return receivedMessages[0];
    }

    private void assertStageIncremented(UUID userCourseId, int expectedStage) {
        UserCourse updatedUserCourse = userCourseRepository.findById(userCourseId).orElseThrow();
        assertThat(updatedUserCourse.getStage()).isEqualTo(expectedStage);
    }

    private void ensureGreenMailRunning() {
        if (!greenMail.isRunning()) {
            greenMail.start();
        }
    }

    private void cleanupDatabases() {
        userLabelRepository.deleteAll();
        contentLabelRepository.deleteAll();
        labelRepository.deleteAll();
        courseContentRepository.deleteAll();
        userCourseRepository.deleteAll();
        contentRepository.deleteAll();
        courseRepository.deleteAll();
        userRepository.deleteAll();
    }


}