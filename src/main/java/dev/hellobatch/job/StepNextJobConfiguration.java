package dev.hellobatch.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class StepNextJobConfiguration {

    @Bean
    public Job stepNextJob(PlatformTransactionManager transactionManager, JobRepository jobRepository) {
        return new JobBuilder("stepNextJob", jobRepository)
                .start(step1(transactionManager, jobRepository))
                .next(step2(transactionManager, jobRepository))
                .next(step3(transactionManager, jobRepository))
                .build();
    }

    @Bean
    @JobScope
    public Step step1(PlatformTransactionManager transactionManager,
                      JobRepository jobRepository) {
        return new StepBuilder("step1", jobRepository)
                .tasklet(tasklet1(), transactionManager)
                .build();
    }

    public Tasklet tasklet1() {
        return ((contribution, chunkContext) -> {
            log.info(">>>>> This is Step1");
            return RepeatStatus.FINISHED;
        });
    }

    @Bean
    public Step step2(PlatformTransactionManager transactionManager,
                      JobRepository jobRepository) {
        return new StepBuilder("step2", jobRepository)
                .tasklet(tasklet2(), transactionManager)
                .build();
    }

    public Tasklet tasklet2() {
        return ((contribution, chunkContext) -> {
            log.info(">>>>> This is Step2");
            return RepeatStatus.FINISHED;
        });
    }

    @Bean
    public Step step3(PlatformTransactionManager transactionManager,
                      JobRepository jobRepository) {
        return new StepBuilder("step3", jobRepository)
                .tasklet(tasklet3(), transactionManager)
                .build();
    }

    public Tasklet tasklet3() {
        return ((contribution, chunkContext) -> {
            log.info(">>>>> This is Step3");
            return RepeatStatus.FINISHED;
        });
    }
}
