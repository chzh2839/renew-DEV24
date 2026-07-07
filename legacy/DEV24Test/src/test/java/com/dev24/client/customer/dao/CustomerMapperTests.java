package com.dev24.client.customer.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.dev24.client.customer.vo.CustomerVO;

import lombok.Setter;
import lombok.extern.log4j.Log4j;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/main/webapp/WEB-INF/spring/root-context.xml")
@Log4j
public class CustomerMapperTests {
	
	@Setter(onMethod_ = @Autowired)
	private CustomerDAO customerDAO;
	
	@Test
	public void testGetSenderInfo() {
		log.info("getSenderInfo »£√‚");
		CustomerVO cvo = customerDAO.getSenderInfo(2);
		log.info(cvo);
	}

}
