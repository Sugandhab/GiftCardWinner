// Tasklet that truncates the orders table and resets ID before importing fresh data.
package com.ecom.giftcardwinner.tasklet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class TruncateOrdersTasklet implements Tasklet {

    private static final Logger logger = LoggerFactory.getLogger(TruncateOrdersTasklet.class);
    private final JdbcTemplate jdbcTemplate;
    @Value("${batch.sql.truncate-orders}")
    private String truncateSql;

    public TruncateOrdersTasklet(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        try {
            logger.info("Truncating 'orders' table...");
            jdbcTemplate.execute(truncateSql);
            logger.info("'orders' table truncated and identity reset.");
        } catch (Exception ex) {
            logger.error("Failed to truncate 'orders' table: {}", ex.getMessage(), ex);
            throw ex;
        }
        return RepeatStatus.FINISHED;
    }
}
