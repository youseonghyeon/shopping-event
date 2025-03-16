package com.shop.shoppingevent.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class JdbcConfig {

    @Bean("shopDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.shop")
    public DataSource shopDataSource() {
        return new HikariDataSource();
    }

    @Bean("businessJdbcTemplate")
    public JdbcTemplate businessJdbcTemplate(@Qualifier("shopDataSource") DataSource shopDataSource) {
        return new JdbcTemplate(shopDataSource);
    }

    @Bean("eventDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.event")
    public DataSource eventDataSource() {
        return new HikariDataSource();
    }

    @Bean("eventJdbcTemplate")
    public JdbcTemplate eventJdbcTemplate(@Qualifier("eventDataSource") DataSource eventDataSource) {
        return new JdbcTemplate(eventDataSource);
    }
}
