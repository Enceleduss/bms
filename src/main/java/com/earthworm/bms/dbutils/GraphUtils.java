package com.earthworm.bms.dbutils;


import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Component
public class GraphUtils {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public GraphUtils(DataSource ds) {
        this.jdbcTemplate = new JdbcTemplate(ds);
    }


    private void initAge() {
        // These commands must be run for Apache AGE to work on a connection
        jdbcTemplate.execute("LOAD 'age'");
        jdbcTemplate.execute("SET search_path = ag_catalog, \"$user\", public");
    }

    public Optional<Long> getNextSequenceValue(String seqName) throws SQLException {
        return Optional.ofNullable(jdbcTemplate.queryForObject("SELECT nextval('\"NODE_ID\"')", Long.class));
    }

    public void executeQuery(String query) {
        initAge(); // Ensure AGE is loaded for this execution
        jdbcTemplate.execute(query);
    }

    public <T> List<T> executeQueryForResults(String query, PreparedStatementSetter pss, RowMapper<T> rowMapper) {
        initAge(); // Ensure AGE is loaded for this execution
        return jdbcTemplate.query(query, pss, rowMapper);
    }

    /**
     * Creates a table that inherits from a parent table using the JOINED strategy.
     * The child table's ID will be both its PK and a FK to the parent.
     */
    public void createInheritedTable(String tableName, String parentTable) {
        String sql = String.format(
            "CREATE TABLE IF NOT EXISTS %s (" +
            "id BIGINT PRIMARY KEY REFERENCES %s(id) ON DELETE CASCADE" +
            ")", tableName.toLowerCase(), parentTable.toLowerCase());
        jdbcTemplate.execute(sql);
        System.out.println("Created inherited table: " + tableName + " -> " + parentTable);
    }

    public void addColumnIfMissing(String tableName, String columnName, String dataType) {
        String sql = String.format(
            "DO $$ BEGIN " +
            "IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='%s' AND column_name='%s') THEN " +
            "ALTER TABLE %s ADD COLUMN %s %s; " +
            "END IF; END $$;", 
            tableName.toLowerCase(), columnName.toLowerCase(), tableName.toLowerCase(), columnName.toLowerCase(), dataType);
        jdbcTemplate.execute(sql);
    }

    public void defineItem(String[] args) throws SQLException {
        this.createInheritedTable(args[0], args.length > 1 ? args[1] : "GraphNode");
    }

    public void defineAttr(String nodeType, String attrName, String dataType) {
        String createTableSQL = "ALTER TABLE " + nodeType + " ADD COLUMN " + attrName + " " + dataType;
        jdbcTemplate.execute(createTableSQL);
        System.out.println("define attr to Table created successfully");
    }

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }
}
