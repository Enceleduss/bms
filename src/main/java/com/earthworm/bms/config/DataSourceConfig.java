package com.earthworm.bms.config;

import com.zaxxer.hikari.HikariDataSource;
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
    public DataSource getDataSourceFromApplicationPropertiesPGSQL() {
        PGSimpleDataSource ds = new PGSimpleDataSource() ;
        String [] serverNames = {"localhost"};
        int[] serverPortNumbers = { 5432 };
        ds.setPortNumbers( serverPortNumbers );
        ds.setServerNames(serverNames);
        ds.setDatabaseName( "bms" );
        ds.setUser( USER );
        ds.setPassword( PASS );
        return ds;
    }
    @Bean
    public DataSource getDataSourceFromApplicationPropertiesPGSQLAWS() {
        PGSimpleDataSource ds = new PGSimpleDataSource() ;
        String [] serverNames = {"ec2-13-49-46-209.eu-north-1.compute.amazonaws.com"};
        int[] serverPortNumbers = { 5432 };
        ds.setPortNumbers( serverPortNumbers );
        ds.setServerNames(serverNames);
        ds.setDatabaseName( "bms" );
        ds.setUser( USER );
        ds.setPassword( PASS );
        return ds;
    }

    @Bean
    @Primary
    public DataSource getDataSourceFromApplicationPropertiesAWS2() {
        HikariDataSource ds = DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .driverClassName(JDBC_DRIVER)
                .url(DB_URL)
                .username(USER)
                .password(PASS)
                .build();

        // 1. Initial SQL for Apache AGE
        ds.setConnectionInitSql("LOAD 'age'; SET search_path = ag_catalog, \"$user\", public;");

        // 2. Fix for "This connection has been closed"
        // Shorten maxLifetime to 10 minutes. This ensures Hikari retires connections
        // before the AWS/External server kills them due to idleness.
        ds.setMaxLifetime(600000);

        // 3. Keep-alive to prevent the firewall/server from timing out the connection
        ds.setKeepaliveTime(60000); // 1 minute

        return ds;
    }
}
