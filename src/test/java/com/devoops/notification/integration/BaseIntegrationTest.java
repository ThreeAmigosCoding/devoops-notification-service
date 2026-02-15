package com.devoops.notification.integration;

import com.devoops.notification.config.TestContainersConfig;
import com.devoops.notification.repository.NotificationPreferencesRepository;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.ServerSocket;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestContainersConfig.class)
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    private static volatile GreenMail greenMail;
    private static volatile int smtpPort;

    static {
        smtpPort = findAvailablePort();
        ServerSetup serverSetup = new ServerSetup(smtpPort, "localhost", ServerSetup.PROTOCOL_SMTP);
        greenMail = new GreenMail(serverSetup);
        greenMail.withConfiguration(GreenMailConfiguration.aConfig().withDisabledAuthentication());
        greenMail.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (greenMail != null) {
                greenMail.stop();
            }
        }));
    }

    @LocalServerPort
    protected int port;

    protected RestTemplate restTemplate = createRestTemplate();

    @Autowired
    protected RabbitTemplate rabbitTemplate;

    @Autowired
    protected NotificationPreferencesRepository preferencesRepository;

    private static RestTemplate createRestTemplate() {
        RestTemplate template = new RestTemplate();
        template.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) throws IOException {
                return false;
            }
        });
        return template;
    }

    @DynamicPropertySource
    static void configureMailProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.mail.host", () -> "localhost");
        registry.add("spring.mail.port", () -> smtpPort);
        System.out.println("Configured mail port: " + smtpPort);
    }

    @BeforeEach
    void setUp() {
        preferencesRepository.deleteAll();
        greenMail.reset();
    }

    protected GreenMail getGreenMail() {
        return greenMail;
    }

    protected String getBaseUrl() {
        return "http://localhost:" + port;
    }

    protected <T> ResponseEntity<T> get(String path, HttpHeaders headers, Class<T> responseType) {
        HttpEntity<?> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(getBaseUrl() + path, HttpMethod.GET, entity, responseType);
    }

    protected <T> ResponseEntity<T> put(String path, Object body, HttpHeaders headers, Class<T> responseType) {
        HttpEntity<?> entity = new HttpEntity<>(body, headers);
        return restTemplate.exchange(getBaseUrl() + path, HttpMethod.PUT, entity, responseType);
    }

    protected HttpHeaders createHeaders(String userId, String role) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Id", userId);
        headers.set("X-User-Role", role);
        headers.set("Content-Type", "application/json");
        return headers;
    }

    private static int findAvailablePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            socket.setReuseAddress(true);
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new RuntimeException("Could not find available port", e);
        }
    }
}
