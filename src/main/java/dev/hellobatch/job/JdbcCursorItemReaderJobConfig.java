package dev.hellobatch.job;

import dev.hellobatch.domain.Pay;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class JdbcCursorItemReaderJobConfig {

    private static final int CHUNK_SIZE = 10;
    private final DataSource dataSource;

    @Bean
    public Job jdbcCursorItemReaderJob(PlatformTransactionManager transactionManager, JobRepository jobRepository) {
        return new JobBuilder("jdbcCursorItemReaderJob", jobRepository)
                .start(jdbcCursorItemReaderStep(transactionManager, jobRepository))
                .build();
    }

    @Bean
    public Step jdbcCursorItemReaderStep(PlatformTransactionManager transactionManager, JobRepository jobRepository) {
        return new StepBuilder("jdbcCursorItemReaderStep", jobRepository)
                .chunk(CHUNK_SIZE, transactionManager)
                .reader(jdbcCursorItemReader())
                .writer(jdbcCursorItemWriter())
                .build();
    }

    @Bean
    public JdbcCursorItemReader<Pay> jdbcCursorItemReader() {
        return new JdbcCursorItemReaderBuilder<Pay>()
                .fetchSize(CHUNK_SIZE)
                .dataSource(dataSource)
                .rowMapper(new BeanPropertyRowMapper<>(Pay.class))
                .sql("SELECT id, amount, tx_name, tx_date_time FROM pay")
                .name("jdbcCursorItemReader")
                .build();
    }

    @Bean
    public ItemWriter<Object> jdbcCursorItemWriter() {
        return this::loggingAll;
    }

    private void loggingAll(final Chunk<?> objects) {
        objects.getItems().forEach(i -> log.info(i.toString()));
    }
}
