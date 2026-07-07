package com.dev24.client.rating.dao;

import java.util.List;

import com.dev24.client.pdetail.vo.PdetailVO;

public interface RatingDAO {
	public int updateSalesCnt(List<PdetailVO> pdvoList);
}
