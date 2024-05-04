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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@RequiredArgsConstructor
@Configuration //모든 Job은 @Configuration 으로 등록해서 사용
public class SimpleJobConfig {

    // simpleJob이란 이름의 Batch Job 생성
    @Bean
    public Job simpleJob(PlatformTransactionManager transactionManager, JobRepository jobRepository) {
        return new JobBuilder("simpleJob", jobRepository)
                .start(simpleStep1(null, transactionManager, jobRepository))
                .next(simpleStep2(null, transactionManager, jobRepository))
                .build();
    }

    // simpleStep1이란 이름의 Batch Step 생성
    @Bean
    @JobScope
    public Step simpleStep1(@Value("#{jobParameters[requestData]}") String requestDate,
                            PlatformTransactionManager transactionManager,
                            JobRepository jobRepository) {
        return new StepBuilder("simpleStep1", jobRepository)
                .tasklet(tasklet1(requestDate), transactionManager)
                .build();
    }

    @Bean
    @JobScope
    public Step simpleStep2(@Value("#{jobParameters[requestData]}") String requestDate,
                            PlatformTransactionManager transactionManager,
                            JobRepository jobRepository) {
        return new StepBuilder("simpleStep2", jobRepository)
                .tasklet(tasklet2(requestDate), transactionManager)
                .build();
    }

    // Step안에서 수행될 기능 명시
    public Tasklet tasklet1(String requestDate) {
        return ((contribution, chunkContext) -> {
            log.info(">>>>> This is Step1");
            log.info(">>>>> requestData={}", requestDate);
            return RepeatStatus.FINISHED;
        });
    }

    public Tasklet tasklet2(String requestDate) {
        return ((contribution, chunkContext) -> {
            log.info(">>>>> This is Step2");
            log.info(">>>>> requestData={}", requestDate);
            return RepeatStatus.FINISHED;
        });
    }
}
