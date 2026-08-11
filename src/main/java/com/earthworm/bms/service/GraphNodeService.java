package com.earthworm.bms.service;

import com.earthworm.bms.dbutils.GraphUtils;
import com.earthworm.bms.model.Folder;
import com.earthworm.bms.model.GraphNode;
import com.earthworm.bms.repository.FolderRepository;
import com.earthworm.bms.repository.GraphRepository;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Table;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.Metamodel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GraphNodeService {
    @Autowired
    private GraphUtils gUtils;
    
    @Autowired
    private GraphRepository<GraphNode> graphRepository;
    
    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private EntityManager entityManager;

    // Cache to store Type -> Table Name mapping
    private final Map<String, String> typeToTableMap = new HashMap<>();

    /**
     * Automatically detect all GraphNode subclasses and their table names at startup.
     */
    /**
     * Automatically detect all GraphNode subclasses and their table names at startup.
     */
    @PostConstruct
    public void initializeTableMapping() {
        Metamodel metamodel = entityManager.getMetamodel();
        for (EntityType<?> entityType : metamodel.getEntities()) {
            Class<?> javaType = entityType.getJavaType();

            // Only map classes that inherit from GraphNode (excluding GraphNode itself)
            if (GraphNode.class.isAssignableFrom(javaType) && !GraphNode.class.equals(javaType)) {
                String typeName = javaType.getSimpleName();

                // Default table name is the class name (PostgreSQL usually prefers lowercase)
                String tableName = typeName.toLowerCase();

                // If @Table annotation is present, use the name defined there
                if (javaType.isAnnotationPresent(Table.class)) {
                    Table table = javaType.getAnnotation(Table.class);
                    if (!table.name().isEmpty()) {
                        tableName = table.name();
                    }
                }

                typeToTableMap.put(typeName, tableName);
                System.out.println("Mapped GraphNode type '" + typeName + "' to table '" + tableName + "'");
            }
        }
    }
    public Map<String, Object> getNodeAsMap(long id) {
        // 1. Get base graph properties
        String baseSql = "SELECT id, type, parentid FROM graph_node WHERE id = ?";
        Map<String, Object> nodeMap = gUtils.getJdbcTemplate().queryForMap(baseSql, id);

        String type = (String) nodeMap.get("type");

        // 2. Get subclass-specific properties (the actual data)
        // We assume the table name matches the 'type' (e.g. 'users', 'folder')
        // In your case, CustomerRecord -> 'users', Folder -> 'folder'
        String tableName = typeToTableMap.get(type);

        if (tableName != null) {
            String attrSql = "SELECT * FROM " + tableName + " WHERE id = ?";
            Map<String, Object> attrs = gUtils.getJdbcTemplate().queryForMap(attrSql, id);
            nodeMap.putAll(attrs);
        }

        return nodeMap;
    }

    /**
     * Generic method to save any GraphNode subclass.
     * Handles setting the ID and ensuring the type is set.
     * This is the primary way to persist GraphNodes.
     * @param node The GraphNode (or subclass) to save.
     * @return The saved GraphNode.
     */
    @Transactional
    public GraphNode addGNode(GraphNode node, GraphNode parentNode, String edgeName) {
        if (node.getId() == 0) { // Assuming 0 means new entity, adjust if using different strategy
            try {
                long node_id = gUtils.getNextSequenceValue("NODE_ID").orElseThrow(() -> new RuntimeException("Could not get next sequence value for NODE_ID"));
                node.setId(node_id);
                node.setParentid(parentNode.getId());
            } catch (Exception e) {
                throw new RuntimeException("Failed to get next sequence ID for GraphNode", e);
            }
        }
        long parentNodeId = parentNode.getId();
        node.setParentid(parentNodeId); // Set parent ID in the relational table

        // Create edge in Apache AGE
        String stmt = "SELECT * from cypher('main_graph',$$MATCH(p:GraphNode) WHERE p.id = "+parentNodeId+" CREATE (p) -[:"+edgeName+"]-> (g:GraphNode{id:"+node.getId()+", type:'"+node.getType()+"'}) RETURN NULL$$) AS (a agtype)";
        System.out.println("stmt: "+stmt);
        gUtils.executeQuery(stmt);
        // Ensure the type is set based on the actual class name if not already set
        if (node.getType() == null || node.getType().isEmpty()) {
            node.setType(node.getClass().getSimpleName());
        }
        return updateGNode(node);
    }

    @Transactional
    public GraphNode updateGNode (GraphNode node){
        // Ensure ID and Type are set via the generic save method
        return graphRepository.save(node);
    }

    @Transactional
    public Folder createCompanyPublicFolder (){
        // Ensure ID and Type are set via the generic save method
        Folder folder = new Folder("CompanyPublicFolder", "Top Node in heirarchy");
        folder.setType("Folder"); // Explicitly set type for Folder
        Folder companyFolder = graphRepository.save(folder);
        String stmt ="SELECT * FROM cypher('main_graph', $$ CREATE (r:RootNode {id: ?, name: System Root, status: active}) RETURN r $$)) as (r agtype);";
        PreparedStatementSetter pss = ps -> {
            ps.setLong(1, companyFolder.getId());
        };
        RowMapper<Long> rm = (rs, rowNum) -> rs.getLong(0);
        gUtils.executeQueryForResults(stmt, pss, rm);
        return companyFolder;
    }


    @Transactional
    public Folder createFolder(GraphNode parent, String name, String description){
        // Ensure ID and Type are set via the generic save method
        Folder folder = new Folder(name, description);
        folder.setType("Folder"); // Explicitly set type for Folder
        GraphNode gNode = addGNode(folder,parent,"Children");
        return folder.setGraph(gNode);
    }

    /**
     * Retrieves any GraphNode by its ID.
     * @param id The ID of the GraphNode.
     * @return The GraphNode.
     */
    public GraphNode getNodeById (long id){
        return graphRepository.findById(id).orElseThrow(() -> new RuntimeException("GraphNode with ID " + id + " not found."));
    }

    /**
     * Retrieves a Folder by its name.
     * @param name The name of the folder.
     * @return An Optional containing the Folder, or empty if not found.
     */
    public List<Folder> getFoldersByName (String name, GraphNode parent){
        // Using specific repository for specific query
        String stmt ="SELECT * FROM cypher('main_graph', $$ MATCH (p:GraphNode)-[:Children]->(c:GraphNode) WHERE p.id = ? AND c.type = 'Folder' AND c.name = ? RETURN c.id $$) AS (id agtype);";
        PreparedStatementSetter pss = ps -> {
            ps.setLong(1, parent.getId());
            ps.setString(2, name);
        };
        RowMapper<Long> rm = (rs, rowNum) -> rs.getLong(0);
        List<Long> nodeIds = gUtils.executeQueryForResults(stmt, pss, rm);
        return nodeIds.stream().map(id -> (Folder) graphRepository.findById(id).orElseThrow()).collect(Collectors.toList());
    }

   /**
    * Retrieves child GraphNodes connected by a specific edge.
    *
    * @param node     The parent GraphNode.
    * @param edgeName The name of the edge.
    * @return A list of child GraphNodes.
    */
    public List<? extends GraphNode> getChildSet(GraphNode node, String edgeName) {
        // Note: We use %d to inject the ID directly because Apache AGE's
        // cypher function inside the $$ block doesn't support standard JDBC '?' placeholders.
        String stmt = String.format(
                "SELECT * from cypher('main_graph', $$ " +
                        "MATCH (n:GraphNode)-[:%s]->(c:GraphNode) " +
                        "WHERE n.id = %d " +
                        "RETURN c.id " +
                        "$$) as (id agtype)",
                edgeName, node.getId()
        );

        System.out.println("Executing Cypher: " + stmt);

        // RowMapper index is 1 for the first column
        RowMapper<Long> rm = (rs, rowNum) -> rs.getLong(1);

        List<Long> childIds = gUtils.executeQueryForResults(stmt, null, rm);

        return childIds.stream()
                .map(this::getNodeById)
                .map(GraphNode.class::cast)
                .collect(Collectors.toList());
    }

    @Cacheable("companyPublicFolder")
    public Optional<Folder> getCompanyPublicFolder() {
        return folderRepository.findByName("CompanyPublicFolder");
    }

    public Optional<Folder> getFolder(GraphNode parent, String name) {
        String stmt ="SELECT * FROM cypher('main_graph', $$ MATCH (p:GraphNode)-[:Children]->(c:GraphNode) WHERE p.id = ? AND c.type = 'Folder' AND c.name = ? RETURN c.id $$) AS (id agtype);";
        PreparedStatementSetter pss = ps -> {
            ps.setLong(1, parent.getId());
            ps.setString(2, name);
        };
        RowMapper<Long> rm = (rs, rowNum) -> rs.getLong(0);
        List<Long> nodeIds = gUtils.executeQueryForResults(stmt, pss, rm);
        return graphRepository.findById(nodeIds.get(0)).map(node -> (Folder) node);
    }
}