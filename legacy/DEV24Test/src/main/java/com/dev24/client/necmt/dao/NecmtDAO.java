package com.dev24.client.necmt.dao;

import java.util.List;

import com.dev24.client.necmt.vo.NecmtVO;

public interface NecmtDAO {
	
	//select
	public List<NecmtVO> necmtList(int ne_num);
	public int replyCheck(int ne_num);
	
	//insert
	public int necmtInsert(NecmtVO nvo);
	
	//delete
	public int necmtDeleteNeNum(int ne_num);
	public int necmtDeleteNecmtNum(int necmt_num);
}
