package com.dev24.common.vo;

import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class PageDTO {
	
	private int startPage;
	private int endPage;
	private boolean prev, next;
	
	private int total; 
	private CommonVO cvo;
	
	public PageDTO(CommonVO cvo, int total) {
		this.cvo=cvo;
		this.total=total;
		
		/*페이징의 끝번호(endPage) 구하기
		 * this.endPAge = (int) (Math.ceil(페이지번호/10.0)) * 10;
		*/
		this.endPage= (int)(Math.ceil(cvo.getPageNum()/10.0))*10;
		
		// 페이징의 시작번호(startPage) 구하기 
		this.startPage = this.endPage-9;
		
		//끝페이지 구하기
		
		int realEnd = (int)(Math.ceil((total*1.0)/cvo.getAmount()));
		
		if(realEnd <= this.endPage) {
			this.endPage = realEnd;
		}
		
		// 이전(prev) 구하기 
		this.prev = this.startPage > 1;
		
		// 다음 (next) 구하기
		this.next = this.endPage < realEnd;
	}
	
	
}
