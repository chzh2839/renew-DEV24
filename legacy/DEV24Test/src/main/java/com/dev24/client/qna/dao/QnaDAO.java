package com.dev24.client.qna.dao;

import java.util.List;


import com.dev24.client.qna.vo.QnaVO;

public interface QnaDAO {
	public List<QnaVO> qnaList(QnaVO qvo);
	public int qnaInsert(QnaVO qvo);
	public QnaVO qnaDetail(QnaVO qvo);
	public int qnaCount(int q_num);
	public int replyInsert(QnaVO qvo);
	
	public int qnaUpdate(QnaVO qvo);
	public int qnaDelete(int q_num);
	public void makeReply(QnaVO qvo);

	public List<QnaVO> myQnaList(QnaVO qvo); // mypage
}
