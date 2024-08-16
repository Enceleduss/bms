package com.earthworm.bms;

import com.earthworm.bms.dbutils.GraphUtils;
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
	@Test
	void helloTest() throws SQLException {
		gu.createTable("sunil (sunny int)");
		System.out.println("sunil ds "+ds.toString());
		assert("Sunil").equals("Sunil");
	}

}
