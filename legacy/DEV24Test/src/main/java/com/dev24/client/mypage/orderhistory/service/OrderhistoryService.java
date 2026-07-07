package com.dev24.client.mypage.orderhistory.service;

import java.util.List;

import com.dev24.client.mypage.orderhistory.vo.OrderhistoryVO;

public interface OrderhistoryService {
	// 주문내역 조회
	public List<OrderhistoryVO> orderhistoryList(OrderhistoryVO ohvo);
	public int orderstateUpdate(OrderhistoryVO ohvo);
}
