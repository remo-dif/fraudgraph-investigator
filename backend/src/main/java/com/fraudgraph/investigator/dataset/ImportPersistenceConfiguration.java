package com.fraudgraph.investigator.dataset;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration(proxyBeanMethods = false)
@Profile("import")
class ImportPersistenceConfiguration {
    @Bean
    Driver importDriver(@Value("${IMPORT_NEO4J_URI:}") String uri,
                        @Value("${IMPORT_NEO4J_USERNAME:}") String username,
                        @Value("${IMPORT_NEO4J_PASSWORD:}") String password) {
        if (uri.isBlank() || username.isBlank() || password.isBlank()
                || password.equals("fraudgraph-local-password")) {
            throw new IllegalStateException("Dedicated Neo4j import configuration is required");
        }
        return GraphDatabase.driver(uri, AuthTokens.basic(username, password));
    }

    @Bean
    ApplicationRunner importSchema(DatasetFoundation foundation) {
        return args -> foundation.initializeSchema();
    }
}
