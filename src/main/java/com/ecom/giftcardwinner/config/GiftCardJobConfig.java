package com.ecom.giftcardwinner.config;

import com.ecom.giftcardwinner.model.Order;
import com.ecom.giftcardwinner.model.User;
import com.ecom.giftcardwinner.processor.DataProcessor;
import com.ecom.giftcardwinner.reader.OrderCsvReader;
import com.ecom.giftcardwinner.reader.UserApiReader;
import com.ecom.giftcardwinner.tasklet.WinnerSelectionTasklet;
import com.ecom.giftcardwinner.writer.OrderWriter;
import com.ecom.giftcardwinner.writer.UserWriter;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import com.ecom.giftcardwinner.tasklet.TruncateOrdersTasklet;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class GiftCardJobConfig {

    @Value("${batch.chunk-size:10}")
    private int chunkSize;

    @Value("${batch.skip-limit:10}")
    private int skipLimit;

    private final EntityManagerFactory entityManagerFactory;
    private final PlatformTransactionManager transactionManager;
    private final TruncateOrdersTasklet truncateOrdersTasklet;
    private final WinnerSelectionTasklet winnerSelectionTasklet;
    private final UserApiReader userApiReader;
    private final OrderCsvReader orderCsvReader;


    @Autowired
    public GiftCardJobConfig(EntityManagerFactory entityManagerFactory, PlatformTransactionManager transactionManager,
                             TruncateOrdersTasklet truncateOrdersTasklet, WinnerSelectionTasklet winnerSelectionTasklet,
                             UserApiReader userApiReader, OrderCsvReader orderCsvReader) {
        this.entityManagerFactory = entityManagerFactory;
        this.transactionManager = transactionManager;
        this.truncateOrdersTasklet = truncateOrdersTasklet;
        this.winnerSelectionTasklet = winnerSelectionTasklet;
        this.userApiReader = userApiReader;
        this.orderCsvReader = orderCsvReader;
    }

    @Bean
    public Job winnerSelectionJob(org.springframework.batch.core.repository.JobRepository jobRepository) {
        return new JobBuilder("winnerSelectionJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(userImportStep(jobRepository))
                .next(truncateOrdersStep(jobRepository))
                .next(orderImportStep(jobRepository))
                .next(winnerSelectionStep(jobRepository)).build();
    }

    @Bean
    public Step userImportStep(org.springframework.batch.core.repository.JobRepository jobRepository) {
        return new StepBuilder("userImportStep", jobRepository)
                .<User, User>chunk(chunkSize, transactionManager).reader(userApiReader).
                processor(new DataProcessor<>()).writer(new UserWriter(entityManagerFactory))
                .faultTolerant().skip(Exception.class).skipLimit(skipLimit).build();
    }

    @Bean
    public Step truncateOrdersStep(org.springframework.batch.core.repository.JobRepository jobRepository) {
        return new StepBuilder("truncateOrdersStep", jobRepository)
                .tasklet(truncateOrdersTasklet, transactionManager).build();
    }

    @Bean
    public Step orderImportStep(org.springframework.batch.core.repository.JobRepository jobRepository) {
        return new StepBuilder("orderImportStep", jobRepository)
                .<Order, Order>chunk(chunkSize, transactionManager).reader(orderCsvReader)
                .processor(new DataProcessor<>()).writer(new OrderWriter(entityManagerFactory))
                .faultTolerant().skip(Exception.class).skipLimit(skipLimit).build();
    }

    @Bean
    public Step winnerSelectionStep(JobRepository jobRepository) {
        return new StepBuilder("winnerSelectionStep", jobRepository)
                .tasklet(winnerSelectionTasklet, transactionManager).build();
    }

}