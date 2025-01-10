package com.zdrovi;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import com.zdrovi.commons.*;
import com.zdrovi.commons.DatabaseVerifier.Repositories;
import com.zdrovi.commons.EntityRepository.TestCourseSetup;
import com.zdrovi.domain.entity.Content;
import com.zdrovi.domain.entity.Course;
import com.zdrovi.domain.entity.User;
import com.zdrovi.domain.entity.UserCourse;
import com.zdrovi.domain.repository.*;
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

    @Container
    private static final PostgreSQLContainer<?> postgres = ImageRepository.getPostgresImage();

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
    static void beforeAll() {
        greenMail = new GreenMail(new ServerSetup(3025, null, "smtp"))
                .withConfiguration(GreenMailConfiguration.aConfig().withDisabledAuthentication());
        greenMail.start();
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
        void shouldNotCreateCourseIfUsersNotExist()
        {
            databaseVerifier.captureInitialState();
            await()
                    .pollDelay(3, SECONDS)
                    .untilAsserted(() -> assertThat(true).isTrue());
            databaseVerifier.verifyDatabaseIntegrity();
        }

        @Test
        void shouldNotCreateCourseIfUserHasActiveCourse()
        {
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
        void shouldNotCreateCourseIfContentPurelyMatch()
        {
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