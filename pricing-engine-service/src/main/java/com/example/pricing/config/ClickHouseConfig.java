package com.example.pricing.config;

import com.clickhouse.jdbc.ClickHouseDriver;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;

@Configuration
public class ClickHouseConfig {

    @Value("${spring.clickhouse.url}")
    private String clickhouseUrl;

    @Bean
    public DataSource clickhouseDataSource() {
        return new SimpleDriverDataSource(new ClickHouseDriver(), clickhouseUrl);
    }

    @Bean
    public JdbcTemplate clickhouseJdbcTemplate(DataSource clickhouseDataSource) {
        return new JdbcTemplate(clickhouseDataSource);
    }

    @Bean(initMethod = "migrate")
    public Flyway clickhouseFlyway(DataSource clickhouseDataSource) {
        return Flyway.configure()
                .dataSource(clickhouseDataSource)
                .locations("classpath:db/migration")
                .load();
    }
}