package tests;


import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openqa.selenium.devtools.v137.fetch.model.AuthChallengeResponse;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;


@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserControllerIntegrationTest {

    // Общая Docker-сеть для приложения и БД
    private static final Network network = Network.newNetwork();

    // Контейнер PostgreSQL
    @Container
    private final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("demo")
            .withUsername("postgres")
            .withPassword("password")
            .withNetwork(network)
            .withNetworkAliases("postgres")
            .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(30)));

    // Контейнер приложения
    private GenericContainer<?> appContainer;

    @BeforeAll
    void setup() throws IOException, InterruptedException {
        // Старт PostgreSQL
        postgres.start();

        // Старт контейнера приложения с подключением к PostgreSQL через alias
        appContainer = new GenericContainer<>("my-app:latest")
                .withNetwork(network)
                .withExposedPorts(8080)
                .withEnv("SPRING_DATASOURCE_URL", "jdbc:postgresql://postgres:5432/demo")
                .withEnv("SPRING_DATASOURCE_USERNAME", "postgres")
                .withEnv("SPRING_DATASOURCE_PASSWORD", "password")
                .waitingFor(Wait.forHttp("/actuator/health")
                        .forStatusCode(200)
                        .withStartupTimeout(Duration.ofMinutes(2)));

        appContainer.start();

        // Настройка RestAssured на порт контейнера приложения
        RestAssured.baseURI = "http://" + appContainer.getHost();
        RestAssured.port = appContainer.getMappedPort(8080);

        System.out.println("App started and available at: " +
                RestAssured.baseURI + ":" + RestAssured.port);
    }

    @Test
    void testCreateAndGetUser() {
        String name = "Alice";
        String email = "alice@example.com";

        // Создание пользователя
        given()
                .contentType("application/json")
                .body("{\"name\":\"" + name + "\", \"email\":\"" + email + "\"}")
                .when()
                .post("/users")
                .then()
                .statusCode(200)
                .body("name", Matchers.equalTo(name))
                .body("email", Matchers.equalTo(email));

        // Проверка списка пользователей
        when()
                .get("/users")
                .then()
                .statusCode(200)
                .body("size()", Matchers.equalTo(1));

        // Удаление пользователя
        given()
                .contentType("application/json")
                .body("{\"name\":\"" + name + "\"}")
                .when()
                .delete("/users")
                .then()
                .statusCode(200)
                .body("message", Matchers.equalTo("User deleted"));
    }
}