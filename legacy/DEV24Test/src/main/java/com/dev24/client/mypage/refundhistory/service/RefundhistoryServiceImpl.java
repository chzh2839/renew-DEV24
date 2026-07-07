package com.dev24.client.mypage.refundhistory.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.dev24.client.mypage.refundhistory.dao.RefundhistoryDAO;
import com.dev24.client.mypage.refundhistory.vo.RefundhistoryVO;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class RefundhistoryServiceImpl implements RefundhistoryService {

	private RefundhistoryDAO refundhistoryDAO;
	
	@Override
	public List<RefundhistoryVO> refundhistoryList(RefundhistoryVO rfhvo) {
		List<RefundhistoryVO> list = null;
		list = refundhistoryDAO.refundhistoryList(rfhvo);
		return list;
	}

}
