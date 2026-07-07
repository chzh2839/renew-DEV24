package com.dev24.admin.qna.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dev24.admin.qna.dao.AdminQnaDAO;
import com.dev24.client.qna.vo.QnaVO;

import lombok.Setter;

@Service
public class AdminQnaServiceImpl implements AdminQnaService {

	@Setter(onMethod_=@Autowired) //Setter Injection; 
	private AdminQnaDAO adminQnaDAO;
	
	
	//QNA 
	@Override
	public List<QnaVO> qnaList(QnaVO qvo) {
		
		List<QnaVO> list = null;
		list = adminQnaDAO.qnaList(qvo);
		
		return list;
	}

	//QNA 글쓰기
	@Override
	public int qnaInsert(QnaVO qvo) {
		int result = 0;
		//qvo.setC_num(10); //외래키로 잡혀 있는 c_num을 임의로 준 뒤 글쓰기 테스트
		result = adminQnaDAO.qnaInsert(qvo);
		return result;
	}
	
	//QNA 상세글 확인하기
	@Override
	public QnaVO qnaDetail(QnaVO qvo) {
		
		 QnaVO detail = null;
		 detail = adminQnaDAO.qnaDetail(qvo);
		 
		 if(detail!=null) {
				detail.setQ_content(detail.getQ_content().toString().replaceAll("\n", "<br>"));
				//이 로직을 처리하지 않을 경우, 게시판 내용이 한줄로만 출력됨
		 }
		return detail;
	}
	
	//QNA 게시글 조회수 증가
	@Override
	public int qnaCount(int q_num) {
		
		int result = 0;
		result = adminQnaDAO.qnaCount(q_num);
		
		return result;
	}
	
	//QNA 글 삭제하기
	@Override
	public int qnaDelete(int q_num) {
		int result = 0;
		result = adminQnaDAO.qnaDelete(q_num);
		
		return result;
	}

	@Override//수정하고자 하는 레코드를 가져온다.
	public QnaVO qupdateForm(QnaVO qvo) {
		QnaVO detail = null;
		detail = adminQnaDAO.qnaDetail(qvo);
		return detail;
	}
	
	
	/* 글 수정 구현하기 */
	@Override
	public int qnaUpdate(QnaVO qvo) {
		int result = 0;
		result = adminQnaDAO.qnaUpdate(qvo);
		return result;
	}
	
	@Override
	public int replyInsert(QnaVO qvo) {
		int result =0;
		adminQnaDAO.makeReply(qvo);
		qvo.setC_num(7);
		result = adminQnaDAO.replyInsert(qvo);
		
		return result;
	}
	
	
	
}
