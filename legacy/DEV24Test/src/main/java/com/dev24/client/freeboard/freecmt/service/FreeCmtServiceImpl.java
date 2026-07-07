package com.dev24.client.freeboard.freecmt.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dev24.client.freeboard.freecmt.dao.FreeCmtDAO;
import com.dev24.client.freeboard.freecmt.vo.FreeCmtVO;

import lombok.AllArgsConstructor;
import lombok.Setter;

@Service
@AllArgsConstructor
public class FreeCmtServiceImpl implements FreeCmtService{
	
	@Setter(onMethod_=@Autowired)
	private FreeCmtDAO freecmtDAO;
	
	@Override
	public List<FreeCmtVO> freeCmtList(Integer fb_num) {
		List<FreeCmtVO> list = null;
		list = freecmtDAO.freeCmtList(fb_num);
		return list;
	}

	@Override
	public int freeCmtInsert(FreeCmtVO fcmtvo) {
		int result =0;
		try {
			result = freecmtDAO.freeCmtInsert(fcmtvo);
		}catch(Exception e) {
			e.printStackTrace();
			result =0;
		}
		return result;
	}

}
