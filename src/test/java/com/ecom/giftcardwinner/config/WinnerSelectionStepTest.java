package com.ecom.giftcardwinner.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBatchTest
@SpringBootTest(properties = "spring.batch.job.enabled=false")
class WinnerSelectionStepTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private DataSource dataSource;

    @BeforeEach
    void setUp() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        jdbcTemplate.update("DELETE FROM winner");
        jdbcTemplate.update("DELETE FROM orders");
        jdbcTemplate.update("DELETE FROM users");

        jdbcTemplate.update("INSERT INTO users (id, name, email) VALUES (1, 'Leanne Graham', 'leanne@example.com')");
        jdbcTemplate.update("INSERT INTO users (id, name, email) VALUES (2, 'Ervin Howell', 'ervin@example.com')");
        jdbcTemplate.update("INSERT INTO orders (user_id, amount) VALUES (1, 45.23)");
        jdbcTemplate.update("INSERT INTO orders (user_id, amount) VALUES (1, 10.50)");
        jdbcTemplate.update("INSERT INTO orders (user_id, amount) VALUES (2, 31.13)");
    }

    @Test
    void testWinnerStep() throws Exception {
        JobParametersBuilder jobParametersBuilder = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis());
        JobExecution jobExecution = jobLauncherTestUtils.launchStep("winnerSelectionStep", jobParametersBuilder.toJobParameters());
        assertEquals("COMPLETED", jobExecution.getStatus().toString());
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        Long winnerCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM winner", Long.class);
        assertEquals(1, winnerCount);
        Double winnerAmount = jdbcTemplate.queryForObject("SELECT amount FROM winner", Double.class);
        assertTrue(winnerAmount == 55.73 || winnerAmount == 31.13);
    }
}