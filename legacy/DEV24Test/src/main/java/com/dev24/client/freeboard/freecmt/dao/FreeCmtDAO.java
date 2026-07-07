package com.dev24.client.freeboard.freecmt.dao;

import java.util.List;

import com.dev24.client.freeboard.freecmt.vo.FreeCmtVO;

public interface FreeCmtDAO {
	public List<FreeCmtVO> freeCmtList(Integer fb_num);
	public int freeCmtInsert(FreeCmtVO fcmtvo);
}
