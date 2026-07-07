package com.dev24.client.purchase.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseVO {
	private int p_num = 0;
	private String p_sender = ""; // person ordered
	private int p_price = 0; // purchase price(total)
	private String p_zipcode = ""; //
	private String p_pmethod = ""; // purchase method
	private String p_receiver = ""; // person who will receive
	private String p_address = ""; // delivery address
	private String p_senderphone = "";
	private String p_receivephone = "";
	private int c_num = 0; // customer number
	private String p_buydate = ""; // buy date

}
