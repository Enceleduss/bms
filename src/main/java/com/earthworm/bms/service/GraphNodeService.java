package com.earthworm.bms.service;

import com.earthworm.bms.dbutils.GraphUtils;
import com.earthworm.bms.model.GraphNode;
import com.earthworm.bms.repository.GraphRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class GraphNodeService {
    @Autowired
    GraphUtils gUtils;
    @Autowired
    private GraphRepository<GraphNode> graphRepository;
    @Transactional
    public GraphNode commitGNode (GraphNode node, GraphNode parentNode, String edgeName){
        try {
            long node_id = gUtils.getNextSequenceValue("NODE_ID").orElseThrow();
            node.setId(node_id);
            long parentNodeId = parentNode.getId();
            node.setParentid(parentNodeId);
            // node.setCreationTime
            String stmt = "SELECT * from cypher('main_graph',$$MATCH(p:GraphNode) WHERE p.id = "+parentNodeId+" CREATE (p) -[:Children]-> (g:GraphNode{id:"+node_id+"}) RETURN NULL$$) AS (a agtype)";
            System.out.println("stmt: "+stmt);
            gUtils.executeQuery(stmt);
            graphRepository.save(node);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return node;
    }
    public GraphNode getNodeById (long id){
        return graphRepository.findById(id).orElseThrow();
    }
    public List<GraphNode> getChildSet(GraphNode node, String edgeName){
        String stmt = "SELECT * from cypher('main_graph',$$MATCH(n:GraphNode -[:"+edgeName+"]-> c:GraphNode) where n.id = ? return c.id$$ as (id agtype))";
        System.out.println("stmt: "+stmt);
        PreparedStatementSetter pss = ps -> ps.setLong(1, node.getId());
        RowMapper<Long> rm = (rs, rowNum) -> rs.getLong(0);
        List<Long> nodeIds = gUtils.executeQueryForResults(stmt, pss, rm);
        return nodeIds.stream().map(this::getNodeById).collect(Collectors.toList());
    }
}


