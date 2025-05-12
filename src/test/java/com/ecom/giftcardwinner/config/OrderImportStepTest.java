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

@SpringBatchTest
@SpringBootTest(properties = "order.csv.path=src/test/resources/test-order.csv")
class OrderImportStepTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private DataSource dataSource;

    @Test
    void testOrderImport() throws Exception {
        JobParametersBuilder jobParametersBuilder = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis());
        JobExecution jobExecution1 = jobLauncherTestUtils.launchStep("orderImportStep", jobParametersBuilder.toJobParameters());
        assertEquals("COMPLETED", jobExecution1.getStatus().toString());
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        Long orderCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM orders", Long.class);
        assertEquals(7, orderCount);
    }
}