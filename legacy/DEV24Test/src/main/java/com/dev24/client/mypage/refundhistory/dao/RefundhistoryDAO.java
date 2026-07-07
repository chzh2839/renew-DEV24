package com.dev24.client.mypage.refundhistory.dao;

import java.util.List;

import com.dev24.client.mypage.refundhistory.vo.RefundhistoryVO;

public interface RefundhistoryDAO {
	public List<RefundhistoryVO> refundhistoryList(RefundhistoryVO rfhvo);
}
