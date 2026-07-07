package com.dev24.admin.faq.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dev24.admin.faq.dao.AdminFaqDAO;
import com.dev24.client.faq.vo.FaqVO;

import lombok.Setter;

@Service
public class AdminFaqSerivceImpl implements AdminFaqSerivce {
	
	@Setter(onMethod_ =@Autowired)
	private AdminFaqDAO adminFaqDAO;
	
	
	@Override
	public List<FaqVO> faqList(FaqVO fvo) {
		
		List<FaqVO> list = null;
		list = adminFaqDAO.faqList(fvo);
		return list;
	}

	@Override
	public int faqInsert(FaqVO fvo) {
		
		int result = 0;
		result = adminFaqDAO.faqInsert(fvo);
		return result;
	}

	@Override
	public FaqVO faqDetail(FaqVO fvo) {
		
		FaqVO detail = null;
		detail = adminFaqDAO.faqDetail(fvo);
		
		if(detail!=null) {
			detail.setFaq_content(detail.getFaq_content().toString().replaceAll("\n", "<br>"));
		}
		
		return detail;
	}

	@Override
	public FaqVO faqupdateForm(FaqVO fvo) {
		FaqVO detail = null;
		detail = adminFaqDAO.faqDetail(fvo);
		
		return detail;
	}

	@Override
	public int faqCount(int faq_num) {
		
		int result = 0;
		result = adminFaqDAO.faqCount(faq_num);
		
		return result;
	}

	@Override
	public int faqDelete(int faq_num) {
		int result = 0;
		result = adminFaqDAO.faqDelete(faq_num);
		
		return result;
	}

	@Override
	public int faqUpdate(FaqVO fvo) {
		int result = 0;
		result = adminFaqDAO.faqUpdate(fvo);
		
		return result;
	}
	
}
