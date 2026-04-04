package com.mindgarden.customer.controller;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public abstract class AbstractIntegrationTest {

    // ① static → ein Container für alle Tests in der Klasse, nicht neu starten pro Test
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine").withDatabaseName(
                                                                                                    "customers_test")
                                                                                            .withUsername("mindgarden")
                                                                                            .withPassword("password");

    // ② Spring überschreibt die Datasource-Properties dynamisch mit den Container-Werten
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        // Config-Server im Test deaktivieren
        registry.add("spring.cloud.config.enabled", () -> "false");
        registry.add("spring.cloud.config.import-check.enabled", () -> "false");
    }
}
