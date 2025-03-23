package com.shop.shoppingevent.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class EventDataSourceConfig {

    /**
     * 이벤트 데이터베이스를 위한 DataSource 빈을 생성합니다.
     * <p>
     * 이 메서드는 HikariDataSource를 사용하여 이벤트 데이터베이스(`eventdb`)에 대한
     * 커넥션 풀을 설정합니다.
     * </p>
     *
     * <h3>Database Configuration</h3>
     * <ul>
     *     <li><b>driver-class-name:</b> {@code com.mysql.cj.jdbc.Driver}</li>
     *     <li><b>jdbc-url:</b> {@code jdbc:mysql://hostname:port/database-name}</li>
     *     <li><b>username:</b> {@code username}</li>
     *     <li><b>password:</b> {@code password}</li>
     * </ul>
     *
     * @return 이벤트 데이터베이스(`eventdb`)를 위한 {@link DataSource} 빈
     */
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
