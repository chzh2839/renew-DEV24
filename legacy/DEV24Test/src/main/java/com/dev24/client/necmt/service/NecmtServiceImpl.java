package com.dev24.client.necmt.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dev24.client.necmt.dao.NecmtDAO;
import com.dev24.client.necmt.vo.NecmtVO;

import lombok.Setter;

@Service
public class NecmtServiceImpl implements NecmtService{
	
	@Setter(onMethod_ = @Autowired)
	private NecmtDAO necmtDAO;
	
	@Override
	public List<NecmtVO> necmtList(int ne_num) {
		List<NecmtVO> necmtList = necmtDAO.necmtList(ne_num);
		return necmtList;
	}
	
	@Override
	public int necmtInsert(NecmtVO nvo) {
		int result = necmtDAO.necmtInsert(nvo);
		return result;
	}
	
	@Override
	public int replyCheck(int ne_num) {
		int result = necmtDAO.replyCheck(ne_num);
		return result;
	}
	
	@Override
	public int necmtDeleteNecmtNum(int necmt_num) {
		int result = necmtDAO.necmtDeleteNecmtNum(necmt_num);
		return result;
	}
}
