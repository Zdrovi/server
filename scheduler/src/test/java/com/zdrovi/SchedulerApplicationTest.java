package com.zdrovi;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import com.zdrovi.domain.entity.*;
import com.zdrovi.domain.repository.*;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.concurrent.TimeUnit;

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

    static final String EMAIL = "test@example.com";
    static final String NAME = "Test User";
    static final String TITLE = "Test Title";
    static final String CONTENT = "Test Content";


    static GreenMail greenMail;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @BeforeAll
    static void beforeAll() {
        greenMail = new GreenMail(new ServerSetup(3025, null, "smtp"))
                .withConfiguration(GreenMailConfiguration.aConfig()
                        .withDisabledAuthentication());
        greenMail.start();
    }

    @BeforeEach
    void beforeEach() {
        if (!greenMail.isRunning()) {
            greenMail.start();
        }

        courseContentRepository.deleteAll();
        userCourseRepository.deleteAll();
        contentRepository.deleteAll();
        courseRepository.deleteAll();
        userRepository.deleteAll();

        greenMail.reset();
    }

    @AfterAll
    static void afterAll() {
        greenMail.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

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


    @Test
    void shouldSendEmailAndIncrementStageForUserWithUnfinishedCourse() {
        // Given
        User user = setupUserWithCourseAndContent();

        UserCourse userCourse = user.getUserCourses().stream().findFirst().get();

        Content content = userCourse
                .getCourse()
                .getCourseContents().stream().findFirst().get()
                .getContent();

        // When
        await()
                .atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    // Then
                    MimeMessage message = verifyEmailWasSent();

                    verifyRecipient(message, user);
                    verifySubject(message, content);

                    String emailContent = GreenMailMessageDecoder.decodeContent(message);
                    assertTrue(HtmlUtils.verifyHtmlEqual(HtmlRepository.getExpectedMessage(
                            content.getTitle(),
                            "Cześć",
                            content.getMailContent(),
                            "Pozdrawiamy",
                            "https://zdrovi.com/wypisz-sie" + userCourse.getId()
                    ), emailContent));

                    UserCourse updatedUserCourse = userCourseRepository.findById(userCourse.getId()).orElseThrow();
                    assertThat(updatedUserCourse.getStage()).isEqualTo(2);
                });
    }

    private static void verifySubject(MimeMessage message, Content content) throws MessagingException {
        assertThat(message.getSubject()).isEqualTo(content.getTitle());
    }

    private static void verifyRecipient(MimeMessage message, User user) throws MessagingException {
        assertThat(message.getAllRecipients()[0].toString()).isEqualTo(user.getEmail());
    }

    private static MimeMessage verifyEmailWasSent() {
        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        assertThat(receivedMessages).hasSize(1);
        return receivedMessages[0];
    }

    @Test
    void shouldHandleNoCoursesForUser() {
        // Given
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        userRepository.save(user);

        // When
        await()
                .atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    // Then
                    assertThat(greenMail.getReceivedMessages()).isEmpty();
                    // Verify no state changes occurred
                    assertThat(userCourseRepository.findAll()).isEmpty();
                });
    }

    @Test
    void shouldHandleEmailAuthenticationFailure() {
        // Given
        greenMail.stop(); // Stop the mail server to simulate auth failure

        User user = setupUserWithCourseAndContent();

        // When
        await()
                .atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    // Then
                    // Verify stage was not incremented
                    UserCourse updatedUserCourse = userCourseRepository.findById(user.getUserCourses()
                            .iterator()
                            .next()
                            .getId()).orElseThrow();
                    assertThat(updatedUserCourse.getStage()).isEqualTo(1);
                });

        // Cleanup
        greenMail.start();
    }

    @Test
    @SneakyThrows
    void shouldHandleEmailNetworkIssue() {
        greenMail.stop();

        // Given
        User user = setupUserWithCourseAndContent();

        // When
        await()
                .atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    // Then
                    // Verify stage was not incremented
                    UserCourse updatedUserCourse = userCourseRepository.findById(user.getUserCourses()
                                    .iterator()
                                    .next()
                                    .getId())
                            .orElseThrow();
                    assertThat(updatedUserCourse.getStage()).isEqualTo(1);
                });
    }

    // Helper method to reduce code duplication
    private User setupUserWithCourseAndContent() {
        User user = new User();
        user.setName(NAME);
        user.setEmail(EMAIL);
        userRepository.save(user);

        Course course = new Course();
        course.setStages(3);
        courseRepository.save(course);

        Content content = new Content();
        content.setPath("/test/path");
        content.setTitle(TITLE);
        content.setMailContent(CONTENT);
        contentRepository.save(content);

        UserCourse userCourse = new UserCourse();
        userCourse.setUser(user);
        userCourse.setCourse(course);
        userCourse.setStage(1);
        userCourseRepository.save(userCourse);

        CourseContent courseContent = new CourseContent();
        courseContent.setCourse(course);
        courseContent.setContent(content);
        courseContent.setStage(2);
        courseContentRepository.save(courseContent);

        user.getUserCourses().add(userCourse);
        userRepository.save(user);

        course.getCourseContents().add(courseContent);
        courseRepository.save(course);

        return user;
    }

}