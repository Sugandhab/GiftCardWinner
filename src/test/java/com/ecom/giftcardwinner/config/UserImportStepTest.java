package com.ecom.giftcardwinner.config;

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
@SpringBootTest(properties = {"spring.batch.job.enabled=false",
        "user.api.url=https://jsonplaceholder.typicode.com/users"})
class UserImportStepTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private DataSource dataSource;

    @Test
    void testImportsUsers() throws Exception {
        JobParametersBuilder jobParametersBuilder = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis());
        JobExecution jobExecution = jobLauncherTestUtils.launchStep("userImportStep", jobParametersBuilder.toJobParameters());
        assertEquals("COMPLETED", jobExecution.getStatus().toString());
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        Long userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Long.class);
        assertTrue(userCount > 0);
    }
}