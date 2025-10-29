package tests;

import io.restassured.RestAssured;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

//@Testcontainers
public class UserControllerTest {

//    @Container
//    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
//            .withDatabaseName("demo")
//            .withUsername("postgres")
//            .withPassword("password");
//
//    private static Connection connection;

//    @BeforeEach
//    void setupFlyway() {
//        // Подключаем Flyway к Testcontainer
//        Flyway flyway = Flyway.configure()
//                .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
//                .locations("classpath:db/migration") // путь к миграциям
//                .load();
//
//        flyway.migrate(); // применяем все миграции
//    }

    @BeforeAll
    static void setup() throws SQLException {
//        connection = DriverManager.getConnection(
//                postgres.getJdbcUrl(),
//                postgres.getUsername(),
//                postgres.getPassword()
//        );
        RestAssured.baseURI = "http://localhost:8080";
    }

    @Test
    void testCreateAndGetUser() {
        String name = "Alice";
        String email = "alice@example.com";

        // Создание
        given()
                .contentType("application/json")
                .body("{\"name\":\"" + name + "\", \"email\":\"" + email + "\"}")
                .when()
                .post("/users")
                .then()
                .statusCode(200)
                .body("name", equalTo(name))
                .body("email", equalTo(email));

        // Проверка списка
        when()
                .get("/users")
                .then()
                .statusCode(200)
                .body("size()", equalTo(4));

        given()
                .contentType("application/json")
                .body("{\"name\":\"" + name + "\"}")
                .when()
                .delete("/users")
                .then()
                .statusCode(200)
                .body("message", equalTo("User deleted"));
    }
//
//    @AfterAll
//    static void teardown() throws SQLException {
//        if (connection != null && !connection.isClosed()) {
//            connection.close();
//        }
//    }
}
