package com.dev24.client.refund.dao;

import com.dev24.client.mypage.orderhistory.vo.OrderhistoryVO;
import com.dev24.client.purchase.vo.PurchaseVO;
import com.dev24.client.refund.vo.RefundVO;

public interface RefundDAO {
	public PurchaseVO getPurchaseInfo(PurchaseVO pvo);
	public OrderhistoryVO getRefundItems(OrderhistoryVO ohvo);
	public int refundInsert(RefundVO rfvo);
}
