package dev.hellobatch.job;

import dev.hellobatch.domain.ClassInformation;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class TransactionProcessorJobConfig {

    private static final int CHUNK_SIZE = 1000;
    private final EntityManagerFactory entityManagerFactory;

    @Bean
    public Job transactionProcessorBatchJob(PlatformTransactionManager transactionManager,
                                            JobRepository jobRepository) {
        return new JobBuilder("transactionProcessorBatchJob", jobRepository)
                .preventRestart()
                .start(transactionProcessorBatchStep(transactionManager, jobRepository))
                .build();
    }

    @Bean
    @JobScope
    public Step transactionProcessorBatchStep(PlatformTransactionManager transactionManager,
                                              JobRepository jobRepository) {
        return new StepBuilder("transactionProcessorBatchStep", jobRepository)
                .<Teacher, ClassInformation>chunk(CHUNK_SIZE, transactionManager)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .build();
    }

    @Bean
    public JpaPagingItemReader<Teacher> reader() {
        return new JpaPagingItemReaderBuilder<Teacher>()
                .name("transactionProcessorBatchReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(CHUNK_SIZE)
                .queryString("SELECT t FROM Teacher t")
                .build();
    }

    public ItemProcessor<Teacher, ClassInformation> processor() {
        return item -> new ClassInformation(item.getName(), 1);
    }

    private ItemWriter<ClassInformation> writer() {
        return items -> items.getItems().forEach(i -> {
            log.info("반 정보={}", i);
        });
    }
}
