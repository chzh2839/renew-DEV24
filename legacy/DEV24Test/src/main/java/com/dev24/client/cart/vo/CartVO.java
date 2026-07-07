package com.dev24.client.cart.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartVO {
	private int crt_num = 0;
	private int crt_price = 0;
	private int crt_qty = 0;
	private int b_num = 0;
	private int c_num = 0;

	// cart_view
	private String b_name = "";
	private int b_price = 0;
	private String listcover_imgurl = "";
	private int cateone_num = 0;
	
}
