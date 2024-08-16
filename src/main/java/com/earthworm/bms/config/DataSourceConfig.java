package com.earthworm.bms.config;

import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {
    @Value("${spring.datasource.driver-class-name}")
    String JDBC_DRIVER;
    @Value("${spring.datasource.url}")
    String DB_URL;
    @Value("${spring.datasource.username}")
    String USER;
    @Value("${spring.datasource.password}")
    String PASS;
    @Bean
    public DataSource getDataSourceFromApplicationProperties() {
        DataSourceBuilder<?> dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.driverClassName(JDBC_DRIVER);
        dataSourceBuilder.url(DB_URL);
        dataSourceBuilder.username(USER);
        dataSourceBuilder.password(PASS);
        return dataSourceBuilder.build();
    }
    @Bean
    @Primary
    public DataSource getDataSourceFromApplicationPropertiesPGSQL() {
        PGSimpleDataSource ds = new PGSimpleDataSource() ;
        String [] serverNames = {"localhost"};
        ds.setServerNames(serverNames);
        ds.setDatabaseName( DB_URL );
        ds.setUser( USER );
        ds.setPassword( PASS );
        return ds;
    }
}
