package com.ecom.giftcardwinner.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobSchedulerTest {

    @Mock
    private JobLauncher jobLauncher;

    @Mock
    private Job winnerSelectionJob;

    @Mock
    private JobExecution jobExecution;

    @InjectMocks
    private JobScheduler jobScheduler;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jobScheduler, "runOnStartup", false);
        ReflectionTestUtils.setField(jobScheduler, "fixedRate", 60000L);
        ReflectionTestUtils.setField(jobScheduler, "initialDelay", 10000L);
    }

    @Test
    void testRunOnStartupTrue() throws Exception {
        ReflectionTestUtils.setField(jobScheduler, "runOnStartup", true);
        when(jobLauncher.run(eq(winnerSelectionJob), any(JobParameters.class))).thenReturn(jobExecution);
        jobScheduler.onStartup();
        verify(jobLauncher, times(1)).run(eq(winnerSelectionJob), any(JobParameters.class));
    }

    @Test
    void testJobExecutionThrowsException() throws Exception {
        ReflectionTestUtils.setField(jobScheduler, "runOnStartup", true);
        when(jobLauncher.run(eq(winnerSelectionJob), any(JobParameters.class)))
                .thenThrow(new RuntimeException("Job execution failed"));
        assertDoesNotThrow(() -> jobScheduler.onStartup());
        verify(jobLauncher, times(1)).run(eq(winnerSelectionJob), any(JobParameters.class));
    }

    @Test
    void testScheduledRun_JobSuccessfully() throws Exception {
        when(jobLauncher.run(eq(winnerSelectionJob), any(JobParameters.class))).thenReturn(jobExecution);
        jobScheduler.scheduledRun();
        verify(jobLauncher, times(1)).run(eq(winnerSelectionJob), any(JobParameters.class));
    }

    @Test
    void testJobExecution_ThrowsException() throws Exception {
        when(jobLauncher.run(eq(winnerSelectionJob), any(JobParameters.class)))
                .thenThrow(new RuntimeException("Scheduled job failed"));
        assertDoesNotThrow(() -> jobScheduler.scheduledRun());
        verify(jobLauncher, times(1)).run(eq(winnerSelectionJob), any(JobParameters.class));
    }

    @Test
    void testUniqueJobParameters() throws Exception {
        when(jobLauncher.run(eq(winnerSelectionJob), any(JobParameters.class))).thenReturn(jobExecution);
        jobScheduler.scheduledRun();
        verify(jobLauncher, times(1)).run(eq(winnerSelectionJob), argThat(params -> {
            Long timestamp = params.getLong("timestamp");
            return timestamp != null && timestamp <= System.currentTimeMillis();
        }));
    }

    @Test
    void tesUsesDifferentParameters() throws Exception {
        when(jobLauncher.run(eq(winnerSelectionJob), any(JobParameters.class))).thenReturn(jobExecution);
        jobScheduler.scheduledRun();
        jobScheduler.scheduledRun();
        verify(jobLauncher, times(2)).run(eq(winnerSelectionJob), any(JobParameters.class));
    }
}