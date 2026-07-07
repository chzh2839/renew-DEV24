package com.dev24.data.book.vo;

public class BooksVO {
	// 자바 안에서 구분하고자 만든 코드
	private String URL;

	// yes24books
	private int b_num;
	private String b_name;
	private String b_date;
	private String b_list;
	private String b_author;
	private String b_pub;
	private String b_authorinfo;
	private String b_info;
	private int b_price;
	private int catetwo_num;
	private String b_disct;
	private int cateone_num;

	// yes24booksimg
	private String listcover_imgurl;
	private String detailcover_imgurl;
	private String detail_imgurl;

	public BooksVO() {
	}

	public BooksVO(String URL, int cateone_num, int catetwo_num, String listcover_imgurl) {
		this.URL = URL;
		this.cateone_num = cateone_num;
		this.catetwo_num = catetwo_num;
		this.listcover_imgurl = listcover_imgurl;
	}

	public String getURL() {
		return URL;
	}

	public void setURL(String uRL) {
		URL = uRL;
	}

	public int getB_num() {
		return b_num;
	}

	public void setB_num(int b_num) {
		this.b_num = b_num;
	}

	public String getB_name() {
		return b_name;
	}

	public void setB_name(String b_name) {
		this.b_name = b_name;
	}

	public String getB_date() {
		return b_date;
	}

	public void setB_date(String b_date) {
		this.b_date = b_date;
	}

	public String getB_list() {
		return b_list;
	}

	public void setB_list(String b_list) {
		this.b_list = b_list;
	}

	public String getB_author() {
		return b_author;
	}

	public void setB_author(String b_author) {
		this.b_author = b_author;
	}

	public String getB_pub() {
		return b_pub;
	}

	public void setB_pub(String b_pub) {
		this.b_pub = b_pub;
	}

	public String getB_authorinfo() {
		return b_authorinfo;
	}

	public void setB_authorinfo(String b_authorinfo) {
		this.b_authorinfo = b_authorinfo;
	}

	public String getB_info() {
		return b_info;
	}

	public void setB_info(String b_info) {
		this.b_info = b_info;
	}

	public int getB_price() {
		return b_price;
	}

	public void setB_price(int b_price) {
		this.b_price = b_price;
	}

	public int getCatetwo_num() {
		return catetwo_num;
	}

	public void setCatetwo_num(int catetwo_num) {
		this.catetwo_num = catetwo_num;
	}

	public String getB_disct() {
		return b_disct;
	}

	public void setB_disct(String b_disct) {
		this.b_disct = b_disct;
	}

	public int getCateone_num() {
		return cateone_num;
	}

	public void setCateone_num(int cateone_num) {
		this.cateone_num = cateone_num;
	}

	public String getListcover_imgurl() {
		return listcover_imgurl;
	}

	public void setListcover_imgurl(String listcover_imgurl) {
		this.listcover_imgurl = listcover_imgurl;
	}

	public String getDetailcover_imgurl() {
		return detailcover_imgurl;
	}

	public void setDetailcover_imgurl(String detailcover_imgurl) {
		this.detailcover_imgurl = detailcover_imgurl;
	}

	public String getDetail_imgurl() {
		return detail_imgurl;
	}

	public void setDetail_imgurl(String detail_imgurl) {
		this.detail_imgurl = detail_imgurl;
	}

	@Override
	public String toString() {
		return "BooksVO [URL=" + URL + ", b_num=" + b_num + ", b_name=" + b_name + ", b_date="
				+ b_date + ", b_list=(생략)" + ", b_author=" + b_author + ", b_pub=" + b_pub + ", b_authorinfo==(생략)"
				+ ", b_info==(생략)" + ", b_price=" + b_price + ", catetwo_num=" + catetwo_num
				+ ", b_disct=" + b_disct + ", cateone_num=" + cateone_num + ", listcover_imgurl=" + listcover_imgurl
				+ ", detailcover_imgurl=" + detailcover_imgurl + ", detail_imgurl=" + detail_imgurl + "]";
	}

}
