package com.earthworm.bms.dbutils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

@Component
public class GraphUtils {
    // JDBC driver name and database URL

    Connection conn = null;
    Statement stmt = null;
    JdbcTemplate jdbcTemplate;
    @Autowired
    public void GraphUtils(DataSource ds){
        jdbcTemplate = new JdbcTemplate(ds);
    }
    public void createTable(String type) throws SQLException {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS "+type;
        jdbcTemplate.execute(createTableSQL);
        //statement.executeUpdate(createTableSQL);
        System.out.println("Table created successfully.");
    }

    private void defineItem(String type, String subType)
    {

    }
}
