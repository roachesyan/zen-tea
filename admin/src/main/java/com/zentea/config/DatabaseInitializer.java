package com.zentea.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class DatabaseInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Value("${zentea.db.path:./zentea.db}")
    private String dbPath;

    public DatabaseInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        boolean tablesExist = checkTablesExist();
        if (!tablesExist) {
            log.info("Initializing database schema...");
            String schema = loadResource("schema.sql");
            executeSql(schema);
            log.info("Database schema initialized.");

            String data = loadResource("data.sql");
            executeSql(data);
            log.info("Seed data loaded.");
        } else {
            log.info("Database already initialized, skipping.");
        }

        jdbcTemplate.execute("PRAGMA journal_mode=WAL");
    }

    private boolean checkTablesExist() {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM sqlite_master WHERE type='table' AND name='user'", Integer.class);
            return count != null && count > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private String loadResource(String filename) throws Exception {
        ClassPathResource resource = new ClassPathResource(filename);
        return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
    }

    private void executeSql(String sql) {
        String[] statements = sql.split(";");
        for (String statement : statements) {
            String trimmed = statement.trim();
            if (!trimmed.isEmpty()) {
                jdbcTemplate.execute(trimmed);
            }
        }
    }
}
