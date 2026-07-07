package com.dev24.client.pdetail.dao;

import java.util.List;

import com.dev24.client.pdetail.vo.PdetailVO;

public interface PdetailDAO {
	public int pdetailInsert(List<PdetailVO> pdvoList);
}
