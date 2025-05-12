package com.ecom.giftcardwinner.scheduler;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class JobScheduler {

    private static final Logger logger = LoggerFactory.getLogger(JobScheduler.class);

    private final JobLauncher jobLauncher;
    private final Job winnerSelectionJob;

    @Value("${scheduler.run-on-startup:false}")
    private boolean runOnStartup;

    @Value("${scheduler.fixed-rate-ms}")
    private long fixedRate;

    @Value("${scheduler.initial-delay-ms:10000}")
    private long initialDelay;

    public JobScheduler(JobLauncher jobLauncher, Job winnerSelectionJob) {
        this.jobLauncher = jobLauncher;
        this.winnerSelectionJob = winnerSelectionJob;
    }

    @PostConstruct
    public void onStartup() {
        if (runOnStartup) {
            logger.info("Job configured to run at startup. Executing...");
            try {
                runJob();
            } catch (Exception e) {
                logger.error("Error executing job at startup: {}", e.getMessage(), e);
            }
        }
    }

    @Scheduled(fixedRateString = "${scheduler.fixed-rate-ms}", initialDelayString = "${scheduler.initial-delay-ms:10000}")
    public void scheduledRun() {
        logger.info("Scheduled job triggered (fixedRate={} ms)...", fixedRate);
        try {
            runJob();
        } catch (Exception e) {
            logger.error("Error during scheduled job run: {}", e.getMessage(), e);
        }
    }

    private void runJob() throws Exception {
        JobParameters params = new JobParametersBuilder().addLong("timestamp", System.currentTimeMillis()).toJobParameters();

        logger.info("Launching winner selection job with parameters: {}", params);
        jobLauncher.run(winnerSelectionJob, params);
    }
}