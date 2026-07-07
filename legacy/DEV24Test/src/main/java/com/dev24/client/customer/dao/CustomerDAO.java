package com.dev24.client.customer.dao;

import com.dev24.client.customer.vo.CustomerVO;

public interface CustomerDAO {
	// 구매 화면에서 주문자 정보 출력
	public CustomerVO getSenderInfo(int c_num);
	
	
	public CustomerVO customerSelect(String c_id);
	public CustomerVO customerNickSelect(String c_nickname);
	public CustomerVO customerEmailSelect(String c_email);
	public int customerInsert(CustomerVO cvo);
	public int customerUpdate(CustomerVO cvo);
	public int customerDelete(String c_id);
}
