package dev.hellobatch.job;

import dev.hellobatch.domain.Teacher;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class ProcessorNullJobConfig {

    public static final String JOB_NAME = "processorNullBatch";
    public static final String BEAN_PREFIX = JOB_NAME + "_";
    private final EntityManagerFactory entityManagerFactory;

    @Value("${chunkSize:1000}")
    private int chunkSize;

    @Bean(JOB_NAME)
    public Job job(PlatformTransactionManager transactionManager, JobRepository jobRepository) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .preventRestart()
                .start(step(transactionManager, jobRepository))
                .build();
    }

    @Bean
    @JobScope
    public Step step(PlatformTransactionManager transactionManager, JobRepository jobRepository) {
        return new StepBuilder(BEAN_PREFIX + "step", jobRepository)
                .<Teacher, Teacher>chunk(chunkSize, transactionManager)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .build();
    }

    @Bean
    public JpaPagingItemReader<Teacher> reader() {
        return new JpaPagingItemReaderBuilder<Teacher>()
                .name(BEAN_PREFIX + "reader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(chunkSize)
                .queryString("SELECT t FROM Teacher t")
                .build();
    }

    @Bean
    public ItemProcessor<Teacher, Teacher> processor() {
        return item -> {
            boolean isIgnoreTarget = item.getId() % 2 == 0L;
            if (isIgnoreTarget) {
                log.info(">>>>>>>>>> Teacher's name={}, isIgnoreTarget={}",
                        item.getName(), true);
                return null;
            }
            return item;
        };
    }

    private ItemWriter<Teacher> writer() {
        return items -> items.getItems().forEach(item -> log.info("Teacher's Name={}", item));
    }

}
