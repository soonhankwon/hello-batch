package dev.hellobatch.job;

import dev.hellobatch.domain.Pay;
import dev.hellobatch.domain.Pay2;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
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
public class CustomItemWriterJobConfig {

    private static final int CHUNK_SIZE = 10;
    private final EntityManagerFactory entityManagerFactory;

    @Bean
    public Job customItemWriterJob(PlatformTransactionManager transactionManager, JobRepository jobRepository)
            throws Exception {
        return new JobBuilder("customItemWriterJob", jobRepository)
                .start(customItemWriterStep(transactionManager, jobRepository))
                .build();
    }

    @Bean
    public Step customItemWriterStep(PlatformTransactionManager transactionManager, JobRepository jobRepository)
            throws Exception {
        return new StepBuilder("customItemWriterStep", jobRepository)
                .<Pay, Pay2>chunk(CHUNK_SIZE, transactionManager)
                .reader(customItemWriterReader())
                .processor(customItemWriterProcessor())
                .writer(customItemWriter())
                .build();
    }

    @Bean
    public JpaPagingItemReader<Pay> customItemWriterReader() {
        return new JpaPagingItemReaderBuilder<Pay>()
                .name("customItemWriterReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(CHUNK_SIZE)
                .queryString("SELECT p FROM Pay p")
                .build();
    }

    @Bean
    public ItemProcessor<Pay, Pay2> customItemWriterProcessor() {
        return pay -> new Pay2(pay.getAmount(), pay.getTxName(), pay.getTxDateTime());
    }

    @Bean
    public ItemWriter<Pay2> customItemWriter() {
        return chunk -> chunk.getItems()
                .forEach(System.out::println);
    }
}
