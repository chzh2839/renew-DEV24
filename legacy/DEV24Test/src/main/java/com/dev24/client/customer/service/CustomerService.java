package com.dev24.client.customer.service;

import com.dev24.client.customer.vo.CustomerVO;

public interface CustomerService {
	public int userIdConfirm(String c_id);
	public int userNickConfirm(String c_nickname);
	public int userEmailConfirm(String c_email);
	public CustomerVO customerSelect(String c_id);
	public int customerInsert(CustomerVO cvo);
	public int customerUpdate(CustomerVO cvo);
	public int customerDelete(String c_id);
}
