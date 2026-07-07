package com.dev24.client.freeboard.dao;

import java.util.List;

import com.dev24.client.freeboard.vo.FreeBoardVO;

public interface FreeBoardDAO {
	public List<FreeBoardVO> freeboardList(FreeBoardVO fbvo);
	public FreeBoardVO freeboardDetail(FreeBoardVO fbvo);
	
	public int freeboardListCnt (FreeBoardVO fbvo);
	
	public int freeboardInsert(FreeBoardVO fbvo);
	public int freeboardDelete(FreeBoardVO fbvo);
	public int freeboardUpdate(FreeBoardVO fbvo);
	public int updateFBReadCount(int fb_readcnt);
	
}
