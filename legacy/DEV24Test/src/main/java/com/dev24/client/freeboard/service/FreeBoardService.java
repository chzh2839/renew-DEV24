package com.dev24.client.freeboard.service;

import java.util.List;

import com.dev24.client.freeboard.vo.FreeBoardVO;

public interface FreeBoardService {
	List<FreeBoardVO> freeboardList(FreeBoardVO fbvo);
	public FreeBoardVO freeboardDetail(FreeBoardVO fbvo);
	
	public int freeboardListCnt (FreeBoardVO fbvo);
	
	public int freeboardInsert(FreeBoardVO fbvo);
	public int freeboardDelete(FreeBoardVO fbvo);
	public FreeBoardVO freeboardUpdateForm(FreeBoardVO fbvo);
	public int freeboardUpdate(FreeBoardVO fbvo);
	public int updateFBReadCount(int fb_readcnt);
}
