package com.dev24.admin.faq.dao;

import java.util.List;

import com.dev24.client.faq.vo.FaqVO;

public interface AdminFaqDAO {
	
	public List<FaqVO> faqList(FaqVO fvo);
	public int faqInsert(FaqVO fvo);
	public FaqVO faqDetail(FaqVO fvo);
	public int faqCount(int faq_num);
	
	public int faqUpdate(FaqVO fvo);
	public int faqDelete(int faq_num);
}
