package com.uniin.ioc.dameng.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfig {

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        JdbcTemplate template = new JdbcTemplate(dataSource);
        template.setQueryTimeout(30); // 30 seconds timeout
        return template;
    }
}
