package com.dev24.admin.refund.dao;

import java.util.List;

import com.dev24.admin.refund.vo.AdminRefundViewVO;

public interface RefundDaoAdmin {
	public List<AdminRefundViewVO> refundList(AdminRefundViewVO rfvo);
	public int pdetailStateUpdate(AdminRefundViewVO rfvo);
	public int refundStateUpdate(AdminRefundViewVO rfvo);
}
