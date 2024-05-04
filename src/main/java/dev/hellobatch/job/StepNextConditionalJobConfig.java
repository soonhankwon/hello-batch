package dev.hellobatch.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
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
public class StepNextConditionalJobConfig {

    @Bean
    public Job stepNextConditionalJob(PlatformTransactionManager transactionManager,
                                      JobRepository jobRepository) {
        return new JobBuilder("stepNextConditionalJob", jobRepository)
                .start(conditionalJobStep1(transactionManager, jobRepository))
                .on("FAILED") // FAILED일 경우
                .to(conditionalJobStep3(transactionManager, jobRepository)) // step3 이동
                .on("*") // step3의 결과와 관계없이
                .end() // step3로 이동하면 Flow 종료
                .from(conditionalJobStep1(transactionManager, jobRepository)) // step1으로부터
                .on("*") // FAILED 외 모든 경우
                .to(conditionalJobStep2(transactionManager, jobRepository)) // step2로 이동
                .next(conditionalJobStep3(transactionManager, jobRepository))
                .on("*") // step3의 결과 관계 없이
                .end() // step3로 이동하면 Flow 종료
                .end() // Job 종료
                .build();
    }

    @Bean
    public Step conditionalJobStep1(PlatformTransactionManager transactionManager,
                                    JobRepository jobRepository) {
        return new StepBuilder("step1", jobRepository)
                .tasklet(tasklet1(), transactionManager)
                .build();
    }

    public Tasklet tasklet1() {
        return ((contribution, chunkContext) -> {
            log.info(">>>>>> This is stepNextConditionalJob Step1");
            /*
             * ExistStatus FAILED 지정
             * 해당 Status를 보고
             */
//            contribution.setExitStatus(ExitStatus.FAILED);
            return RepeatStatus.FINISHED;
        });
    }

    @Bean
    public Step conditionalJobStep2(PlatformTransactionManager transactionManager,
                                    JobRepository jobRepository) {
        return new StepBuilder("step2", jobRepository)
                .tasklet(tasklet2(), transactionManager)
                .build();
    }

    public Tasklet tasklet2() {
        return ((contribution, chunkContext) -> {
            log.info(">>>>>> This is stepNextConditionalJob Step2");
            return RepeatStatus.FINISHED;
        });
    }

    @Bean
    public Step conditionalJobStep3(PlatformTransactionManager transactionManager,
                                    JobRepository jobRepository) {
        return new StepBuilder("step3", jobRepository)
                .tasklet(tasklet3(), transactionManager)
                .build();
    }

    public Tasklet tasklet3() {
        return ((contribution, chunkContext) -> {
            log.info(">>>>>> This is stepNextConditionalJob Step3");
            return RepeatStatus.FINISHED;
        });
    }
}
