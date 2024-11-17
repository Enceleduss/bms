package com.earthworm.bms.dbutils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class GraphUtils {
    // JDBC driver name and database URL

    Connection conn = null;
    Statement stmt = null;

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    JdbcTemplate jdbcTemplate;
    @Autowired
    public GraphUtils(DataSource ds){
        jdbcTemplate = new JdbcTemplate(ds);
        String stmt = "SET search_path = ag_catalog, \"$user\", public";
        jdbcTemplate.execute(stmt);
    }
    public Optional<Long> getNextSequenceValue(String seqName) throws SQLException {
            return Optional.ofNullable(jdbcTemplate.queryForObject("SELECT nextval('\"NODE_ID\"')", Long.class));
    }
    public void createTable(String type) throws SQLException {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS "+type;
        jdbcTemplate.execute(createTableSQL);
        //statement.executeUpdate(createTableSQL);
        System.out.println("Table created successfully.");
    }
    public void createTable(String type, String intTypePK) throws SQLException {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS "+type+" ("+intTypePK+" INT PRIMARY KEY)";
        jdbcTemplate.execute(createTableSQL);
        //statement.executeUpdate(createTableSQL);
        System.out.println("Table created successfully with pk.");
    }
    public void defineItem(String type, String subType) throws SQLException {
        this.createTable(type,"NODE_ID");
    }
    public void defineAttr(String nodeType, String attrName, String dataType)
    {
        String createTableSQL = "ALTER TABLE "+nodeType+" ADD COLUMN "+attrName+" "+dataType;
        jdbcTemplate.execute(createTableSQL);
        //statement.executeUpdate(createTableSQL);
        System.out.println("define attr to Table created successfully");
    }
    public void executeQuery(String query) {
        jdbcTemplate.execute(query);
    }
    public <T> List<T> executeQueryForResults(String query, PreparedStatementSetter pss, RowMapper<T> rowMapper) {
        return jdbcTemplate.query(query, pss,rowMapper);
    }
}
