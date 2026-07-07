package com.dev24.client.necmt.service;

import java.util.List;

import com.dev24.client.necmt.vo.NecmtVO;

public interface NecmtService {
	public List<NecmtVO> necmtList(int ne_num);
	public int necmtInsert(NecmtVO nvo);
	public int replyCheck(int ne_num);
	public int necmtDeleteNecmtNum(int necmt_num);
}
