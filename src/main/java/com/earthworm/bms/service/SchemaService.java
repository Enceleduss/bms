package com.earthworm.bms.service;

import com.earthworm.bms.dbutils.GraphUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SchemaService {

    @Autowired
    private GraphUtils gUtils;

    @Autowired
    private GraphNodeService graphNodeService;

    /**
     * Defines a new Node Type in the system.
     * @param typeName The name of the new type.
     * @param parentType The parent type to inherit from (e.g., "GraphNode").
     */
    @Transactional
    public void defineItem(String typeName, String parentType) {
        String parentTable = "graph_node";
        
        if (!"GraphNode".equalsIgnoreCase(parentType)) {
            parentTable = parentType.toLowerCase();
        }
        
        gUtils.createInheritedTable(typeName, parentTable);
        
        // Register the new type in the mapping registry
        //graphNodeService.registerDynamicType(typeName, parentType);
    }

    /**
     * Adds an attribute (column) to an existing Node Type.
     */
    @Transactional
    public void defineAttribute(String typeName, String attrName, String dataType) {
        String sqlDataType = mapJsToSqlType(dataType);
        gUtils.addColumnIfMissing(typeName, attrName, sqlDataType);
    }

    private String mapJsToSqlType(String jsType) {
        switch (jsType.toLowerCase()) {
            case "string": return "VARCHAR(255)";
            case "text": return "TEXT";
            case "number": return "DOUBLE PRECISION";
            case "int": return "BIGINT";
            case "boolean": return "BOOLEAN";
            case "date": return "TIMESTAMP";
            default: return "VARCHAR(255)";
        }
    }
}
