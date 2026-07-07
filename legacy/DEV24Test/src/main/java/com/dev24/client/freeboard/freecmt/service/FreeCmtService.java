package com.dev24.client.freeboard.freecmt.service;

import java.util.List;

import com.dev24.client.freeboard.freecmt.vo.FreeCmtVO;


public interface FreeCmtService {
	public List<FreeCmtVO> freeCmtList(Integer fb_num);
	public int freeCmtInsert(FreeCmtVO fcmtvo);
	
}
