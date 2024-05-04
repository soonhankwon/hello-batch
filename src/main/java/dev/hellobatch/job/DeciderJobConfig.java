package dev.hellobatch.job;

import java.util.Random;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
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
public class DeciderJobConfig {
    @Bean
    public Job deciderJob(PlatformTransactionManager transactionManager, JobRepository jobRepository) {
        return new JobBuilder("deciderJob", jobRepository)
                .start(startStep(transactionManager, jobRepository))
                .next(decider()) // 홀수, 짝수 구분
                .from(decider()) // decider 상태가
                .on("ODD") // ODD라면
                .to(oddStep(transactionManager, jobRepository)) // oddStep으로 간다.
                .from(decider()) // decider 상태가
                .on("EVEN") // EVEN라면
                .to(evenStep(transactionManager, jobRepository)) // evenStep으로 간다.
                .end() // 종료
                .build();
    }

    @Bean
    public Step startStep(PlatformTransactionManager transactionManager,
                          JobRepository jobRepository) {
        return new StepBuilder("startStep", jobRepository)
                .tasklet(tasklet1(), transactionManager)
                .build();
    }

    public Tasklet tasklet1() {
        return ((contribution, chunkContext) -> {
            log.info(">>>>> Start!");
            return RepeatStatus.FINISHED;
        });
    }

    @Bean
    public Step evenStep(PlatformTransactionManager transactionManager,
                         JobRepository jobRepository) {
        return new StepBuilder("startStep", jobRepository)
                .tasklet(tasklet2(), transactionManager)
                .build();
    }

    public Tasklet tasklet2() {
        return ((contribution, chunkContext) -> {
            log.info(">>>>> Even!!!!!");
            return RepeatStatus.FINISHED;
        });
    }

    @Bean
    public Step oddStep(PlatformTransactionManager transactionManager,
                        JobRepository jobRepository) {
        return new StepBuilder("oddStep", jobRepository)
                .tasklet(tasklet3(), transactionManager)
                .build();
    }

    public Tasklet tasklet3() {
        return ((contribution, chunkContext) -> {
            log.info(">>>>> ODD!!!!!");
            return RepeatStatus.FINISHED;
        });
    }

    @Bean
    public JobExecutionDecider decider() {
        return new OddDecider();
    }

    public static class OddDecider implements JobExecutionDecider {

        @Override
        public FlowExecutionStatus decide(@NonNull JobExecution jobExecution, StepExecution stepExecution) {
            Random rand = new Random();
            int randomNum = rand.nextInt(50) + 1;
            log.info("Random Number={}", randomNum);

            if (randomNum % 2 == 0) {
                return new FlowExecutionStatus("EVEN");
            }
            return new FlowExecutionStatus("ODD");
        }
    }
}
