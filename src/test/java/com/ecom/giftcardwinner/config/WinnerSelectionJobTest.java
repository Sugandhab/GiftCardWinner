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
@SpringBootTest(properties = {"spring.batch.job.enabled=false",
        "order.csv.path=src/test/resources/test-order.csv"})
class WinnerSelectionJobTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private DataSource dataSource;

    @Test
    void testWinnerJob() throws Exception {
        JobParametersBuilder jobParametersBuilder = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis());
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParametersBuilder.toJobParameters());
        assertEquals("COMPLETED", jobExecution.getStatus().toString());
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        Long orderCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM orders", Long.class);
        Long winnerCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM winner", Long.class);
        assertEquals(7, orderCount);
        assertEquals(1, winnerCount);
    }
}