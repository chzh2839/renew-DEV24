package com.dev24.client.mypage.refundhistory.service;

import java.util.List;

import com.dev24.client.mypage.refundhistory.vo.RefundhistoryVO;

public interface RefundhistoryService {
	public List<RefundhistoryVO> refundhistoryList(RefundhistoryVO rfhvo);
}
