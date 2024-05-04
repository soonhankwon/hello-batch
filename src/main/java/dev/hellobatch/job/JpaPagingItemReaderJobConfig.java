package dev.hellobatch.job;

import dev.hellobatch.domain.Pay;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class JpaPagingItemReaderJobConfig {

    private final EntityManagerFactory entityManagerFactory;
    private final int CHUNK_SIZE = 10;

    @Bean
    public Job jpaPagingItemReaderJob(PlatformTransactionManager transactionManager, JobRepository jobRepository) {
        return new JobBuilder("jpaPagingItemReaderJob", jobRepository)
                .start(jpaPagingItemReaderStep(transactionManager, jobRepository))
                .build();
    }

    @Bean
    public Step jpaPagingItemReaderStep(PlatformTransactionManager transactionManager, JobRepository jobRepository) {
        return new StepBuilder("jpaPagingItemReaderStep", jobRepository)
                .chunk(CHUNK_SIZE, transactionManager)
                .reader(jpaPagingReader())
                .writer(jpaPagingWriter())
                .build();
    }

    @Bean
    public JpaPagingItemReader<Pay> jpaPagingReader() {
        return new JpaPagingItemReaderBuilder<Pay>()
                .name("jpaPagingItemReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(CHUNK_SIZE)
                .queryString("SELECT p FROM Pay p WHERE amount >= 2000")
                .build();
    }

    @Bean
    public ItemWriter<Object> jpaPagingWriter() {
        return this::loggingAll;
    }

    private void loggingAll(final Chunk<?> objects) {
        objects.getItems().forEach(i -> log.info(i.toString()));
    }
}
