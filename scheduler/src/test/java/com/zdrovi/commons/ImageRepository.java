package com.zdrovi.commons;

import org.testcontainers.containers.PostgreSQLContainer;

public interface ImageRepository {

    static PostgreSQLContainer getPostgresImage() {
        return new PostgreSQLContainer<>("postgres:14-alpine")
                .withDatabaseName("testdb")
                .withUsername("test")
                .withPassword("test");
    }
}
