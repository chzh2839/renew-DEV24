package com.dev24.client.customer.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dev24.client.customer.dao.CustomerDAO;
import com.dev24.client.customer.vo.CustomerVO;

import lombok.Setter;

@Service
public class CustomerServiceImpl implements CustomerService {
	
	@Setter(onMethod_ =@Autowired)
	private CustomerDAO customerDAO;
	
	@Override
	public int userIdConfirm(String c_id) {
		int result;
		if(customerDAO.customerSelect(c_id) != null) {
			result = 1;
		} else {
			result = 2;
		}
		return result;
	}
	
	@Override
	public int userNickConfirm(String c_nickname) {
		int result;
		if(customerDAO.customerNickSelect(c_nickname) != null) {
			result = 1;
		} else {
			result = 2;
		}
		return result;
	}
	
	@Override
	public int userEmailConfirm(String c_email) {
		int result;
		if(customerDAO.customerEmailSelect(c_email) != null) {
			result = 1;
		} else {
			result = 2;
		}
		return result;
	}
	
	

	@Override
	public CustomerVO customerSelect(String c_id) {
		
		CustomerVO vo = customerDAO.customerSelect(c_id);
		
		return vo;
	}
	

	@Override
	public int customerInsert(CustomerVO cvo) {
		
		int sCode = 2;
		if (customerDAO.customerSelect(cvo.getC_id()) != null ) {
			return 1;
		} else {
			try {
				sCode = customerDAO.customerInsert(cvo);
				if(sCode == 1) {
					return 3;
				}else{
					return 2;
				}
			}catch(RuntimeException e ){
				e.printStackTrace();
				return 2;
			}
		}
		
	}
	

	@Override
	public int customerUpdate(CustomerVO cvo) {
		int result = customerDAO.customerUpdate(cvo);
		return result;
	}
	
	@Transactional
	@Override
	public int customerDelete(String c_id) {
		int mCode, isSuccessCode=3;
		try {
			mCode = customerDAO.customerDelete(c_id);
			if(mCode==1) {
				isSuccessCode = 2;
			}
		}catch(Exception e) {
			e.printStackTrace();
			isSuccessCode = 3;
		}
		return isSuccessCode;
	}

	

}
