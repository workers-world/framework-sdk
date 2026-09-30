package com.books.framework.id_generator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class IdGeneratorAutoConfigurationTest {

    private static final AutoConfigurations JDBC_AUTO_CONFIG = AutoConfigurations.of(
            DataSourceAutoConfiguration.class,
            JdbcTemplateAutoConfiguration.class,
            DataSourceTransactionManagerAutoConfiguration.class,
            IdGeneratorAutoConfiguration.class);

    @Test
    void registersDbIdSequenceStoreWhenJdbcConfigured() {
        new ApplicationContextRunner()
                .withConfiguration(JDBC_AUTO_CONFIG)
                .withPropertyValues(
                        "spring.datasource.url=jdbc:h2:mem:id-gen-test;MODE=MySQL;DB_CLOSE_DELAY=-1",
                        "spring.datasource.driver-class-name=org.h2.Driver",
                        "spring.datasource.username=sa",
                        "spring.datasource.password=",
                        "books.framework.id-generator.store-type=jdbc")
                .run(context -> {
                    assertThat(context).hasSingleBean(DbIdSequenceStore.class);
                    assertThat(context).doesNotHaveBean(InMemoryIdSequenceStore.class);
                    IDFactory factory = context.getBean(IDFactory.class);
                    String id1 = factory.getIDGenerator("EV").genId();
                    String id2 = factory.getIDGenerator("EV").genId();
                    assertThat(id1).isNotEqualTo(id2);
                    long seq1 = Long.parseLong(id1.substring(10));
                    long seq2 = Long.parseLong(id2.substring(10));
                    assertThat(seq2).isGreaterThan(seq1);
                });
    }

    @Test
    void persistsSequenceAcrossContextRestart(@TempDir Path tempDir) {
        String dbPath = tempDir.resolve("id-seq").toAbsolutePath().toString();
        String datasourceUrl = "jdbc:h2:file:" + dbPath + ";MODE=MySQL;DB_CLOSE_ON_EXIT=FALSE";

        ApplicationContextRunner runner = new ApplicationContextRunner()
                .withConfiguration(JDBC_AUTO_CONFIG)
                .withPropertyValues(
                        "spring.datasource.url=" + datasourceUrl,
                        "spring.datasource.driver-class-name=org.h2.Driver",
                        "spring.datasource.username=sa",
                        "spring.datasource.password=",
                        "books.framework.id-generator.store-type=jdbc");

        AtomicReference<String> firstId = new AtomicReference<>();
        runner.run(context ->
                firstId.set(context.getBean(IDFactory.class).getIDGenerator("EV").genId()));

        runner.run(context -> {
            String secondId = context.getBean(IDFactory.class).getIDGenerator("EV").genId();
            long firstSeq = Long.parseLong(firstId.get().substring(10));
            long secondSeq = Long.parseLong(secondId.substring(10));
            assertThat(secondSeq).isGreaterThan(firstSeq);
        });
    }

    @Test
    void registersInMemoryStoreWhenConfigured() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(IdGeneratorAutoConfiguration.class))
                .withPropertyValues("books.framework.id-generator.store-type=memory")
                .run(context -> {
                    assertThat(context).hasSingleBean(InMemoryIdSequenceStore.class);
                    assertThat(context).doesNotHaveBean(DbIdSequenceStore.class);
                });
    }
}
