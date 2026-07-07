package com.dev24.admin.faq.service;

import java.util.List;

import com.dev24.client.faq.vo.FaqVO;


public interface AdminFaqSerivce {
	
	public List<FaqVO> faqList(FaqVO fvo); 
	public int faqInsert(FaqVO fvo);
	public FaqVO faqDetail(FaqVO fvo);
	public FaqVO faqupdateForm(FaqVO fvo);
	
	public int faqCount(int faq_num);
	public int faqDelete(int faq_num);
	public int faqUpdate(FaqVO fvo);
}	
