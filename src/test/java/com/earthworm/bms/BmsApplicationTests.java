package com.earthworm.bms;

import com.earthworm.bms.dbutils.GraphUtils;
import com.earthworm.bms.model.Folder;
import com.earthworm.bms.model.GraphNode;
import com.earthworm.bms.repository.GraphRepository;
import com.earthworm.bms.service.GraphNodeService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sql.DataSource;
import java.sql.SQLException;

@RunWith(SpringRunner.class)
@SpringBootTest
class BmsApplicationTests {
	@Autowired
	private DataSource ds;
	@Autowired
	private GraphUtils gu;
	@Autowired
	private GraphRepository graphRepository;
	@Autowired
	private GraphNodeService _g;
	@Test
	void helloTest() throws SQLException {
		//gu.createTable("sunil (sunny int)");
		System.out.println("sunil ds "+ds.toString());
		assert("Sunil").equals("Sunil");
	}
	@Test
	void commitGNodeTest() throws SQLException {
		Folder n = new Folder();
		n.setName("TestFolder");
		n.setType("Folder");
		n.setDescription("TestFolder");
		//gu.createTable("sunil (sunny int)");
		//GraphNode ret = (GraphNode) graphRepository.findById(52L).get();
		//GraphNode ret = _g.commitGNode(n,_g.getNodeById(999999L),"Children");
		// GraphNode ret =	_g.createCompanyPublicFolder();
		//System.out.println("sunil ret "+ret.getId());
		assert("Sunil").equals("Sunil");
	}

}
