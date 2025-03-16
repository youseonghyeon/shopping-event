package com.shop.shoppingevent.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class PointDataSourceConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.point")
    public DataSource pointDataSource() {
        return new HikariDataSource();
    }

    @Bean
    public JdbcTemplate pointJdbcTemplate(DataSource pointDataSource) {
        return new JdbcTemplate(pointDataSource);
    }
}
