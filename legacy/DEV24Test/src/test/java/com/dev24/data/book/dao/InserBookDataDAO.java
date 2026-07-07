package com.dev24.data.book.dao;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.dev24.client.book.vo.BookVO;

public class InserBookDataDAO {
	// 데이터베이스 연결 관련 상수 선언
	private static final String JDBC_URL = "jdbc:oracle:thin:@127.0.0.1:1521:orcl";// 서버 url
	private static final String USER = "devuser";// 사용자
	private static final String PASSWD = "dev1234";// 비밀번호

	private static InserBookDataDAO instance = null;

	public static InserBookDataDAO getInstance() {
		if (instance == null)
			instance = new InserBookDataDAO();
		return instance;
	}

	public InserBookDataDAO() {
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");// jdbc 드라이버
		} catch (ClassNotFoundException cnfe) {
			cnfe.printStackTrace();
		}

	}

	private Connection getConnection() throws Exception {
		Connection con = DriverManager.getConnection(JDBC_URL, USER, PASSWD);// 오라클 서버 연결
		return con;
	}

	/*****************************************************
	 * 책 데이터 insert 메서드
	 * 
	 * @param BooksVO를 담은 List타입
	 * @return (boolean)처리결과
	 *****************************************************/
	public boolean booksInsert(List<BookVO> voList) {

		boolean isSuccess = false;
		int resultNum = 0;

		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		StringBuilder insertQuery = new StringBuilder();
		BookVO vo = null;

		try {

			con = getConnection();
			con.setAutoCommit(false);

			for (int i = 0; i < voList.size(); i++) {
				

				vo = voList.get(i);

				/*insertQuery.append("INSERT INTO book ");
				insertQuery.append("	(b_num, b_name, b_date, b_list, b_author, b_pub, b_authorinfo, ");
				insertQuery.append("	b_info, b_price, cateone_num, catetwo_num");
				insertQuery.append(") VALUES  (");
				insertQuery.append("	?, ?, ?, ?, ?, ");
				insertQuery.append("	?, ?, ?, ?, ?, ?)");*/
				
				insertQuery.append("MERGE INTO book b ");
				insertQuery.append("USING (");
				insertQuery.append("	SELECT");
				insertQuery.append("		  ? as b_num");
				insertQuery.append("		, ? as b_name");
				insertQuery.append("		, ? as b_date");
				insertQuery.append("		, ? as b_list");
				insertQuery.append("		, ? as b_author");
				insertQuery.append("		, ? as b_pub");
				insertQuery.append("		, ? as b_authorinfo");
				insertQuery.append("		, ? as b_info");
				insertQuery.append("		, ? as b_price");
				insertQuery.append("		, ? as cateOne_num");
				insertQuery.append("		, ? as cateTwo_num");
				insertQuery.append("	FROM");
				insertQuery.append("		dual ) d ");
				insertQuery.append("ON (b.b_num = d.b_num) ");
				insertQuery.append("WHEN MATCHED THEN ");
				insertQuery.append("	UPDATE SET");
				insertQuery.append("		  b.b_name = d.b_name");
				insertQuery.append("		, b.b_date = d.b_date");
				insertQuery.append("		, b.b_list = d.b_list");
				insertQuery.append("		, b.b_author = d.b_author");
				insertQuery.append("		, b.b_pub = d.b_pub");
				insertQuery.append("		, b.b_authorinfo = d.b_authorinfo");
				insertQuery.append("		, b.b_info = d.b_info");
				insertQuery.append("		, b.b_price = d.b_price");
				insertQuery.append("		, b.cateOne_num = d.cateOne_num");
				insertQuery.append("		, b.cateTwo_num = d.cateTwo_num ");
				insertQuery.append("WHEN NOT MATCHED THEN");
				insertQuery.append("	INSERT (");
				insertQuery.append("		  b.b_num");
				insertQuery.append("		, b.b_name");
				insertQuery.append("		, b.b_date");
				insertQuery.append("		, b.b_list");
				insertQuery.append("		, b.b_author");
				insertQuery.append("		, b.b_pub");
				insertQuery.append("		, b.b_authorinfo");
				insertQuery.append("		, b.b_info");
				insertQuery.append("		, b.b_price");
				insertQuery.append("		, b.cateOne_num");
				insertQuery.append("		, b.cateTwo_num");
				insertQuery.append(" ) VALUES ( ");
				insertQuery.append("		  d.b_num");
				insertQuery.append("		, d.b_name");
				insertQuery.append("		, d.b_date");
				insertQuery.append("		, d.b_list");
				insertQuery.append("		, d.b_author");
				insertQuery.append("		, d.b_pub");
				insertQuery.append("		, d.b_authorinfo");
				insertQuery.append("		, d.b_info");
				insertQuery.append("		, d.b_price");
				insertQuery.append("		, d.cateOne_num");
				insertQuery.append("		, d.cateTwo_num");
				insertQuery.append(" )");

				pstmt = con.prepareStatement(insertQuery.toString());

				Clob b_list = con.createClob();
				b_list.setString(1, vo.getB_list());

				Clob b_authorinfo = con.createClob();
				b_authorinfo.setString(1, vo.getB_authorinfo());

				Clob b_info = con.createClob();
				b_info.setString(1, vo.getB_info());

				String b_date = vo.getB_date().replace("년 ", "-").replace("월 ", "-").replace("일", "").substring(0, 10);

				System.out.println("[book:b_num]" + vo.getB_num());

				pstmt.setInt(1, vo.getB_num());
				pstmt.setString(2, vo.getB_name());
				pstmt.setDate(3, Date.valueOf(b_date));
				pstmt.setClob(4, b_list);
				if (vo.getB_author().isEmpty())
					vo.setB_author("편집부");
				pstmt.setString(5, vo.getB_author());
				pstmt.setString(6, vo.getB_pub());
				pstmt.setClob(7, b_authorinfo);
				pstmt.setClob(8, b_info);
				pstmt.setInt(9, vo.getB_price());
				pstmt.setInt(10, vo.getCateOne_num());
				pstmt.setInt(11, vo.getCateTwo_num());

				resultNum = pstmt.executeUpdate();
				con.commit();
				insertQuery.setLength(0);// StringBuilder 값 초기화
				pstmt.clearParameters();
				pstmt.close();

				/*insertQuery.append("INSERT INTO bookimg ");
				insertQuery.append("	(b_num, ");
				insertQuery.append("	listcover_imgurl, detailcover_imgurl, detail_imgurl");
				insertQuery.append(") VALUES  (");
				insertQuery.append("	?, ?, ?, ?)");*/
//				System.out.println(insertQuery.toString());
				
				insertQuery.append("MERGE INTO bookimg i ");
				insertQuery.append("USING (");
				insertQuery.append("	SELECT");
				insertQuery.append("		  ? as b_num");
				insertQuery.append("		, ? as listcover_imgurl");
				insertQuery.append("		, ? as detailcover_imgurl");
				insertQuery.append("		, ? as detail_imgurl");
				insertQuery.append("	FROM");
				insertQuery.append("		dual ) d ");
				insertQuery.append("ON (i.b_num = d.b_num) ");
				insertQuery.append("WHEN MATCHED THEN ");
				insertQuery.append("	UPDATE SET");
				insertQuery.append("		  i.listcover_imgurl = d.listcover_imgurl");
				insertQuery.append("		, i.detailcover_imgurl = d.detailcover_imgurl");
				insertQuery.append("		, i.detail_imgurl = d.detail_imgurl ");
				insertQuery.append("WHEN NOT MATCHED THEN");
				insertQuery.append("	INSERT (");
				insertQuery.append("		  i.b_num");
				insertQuery.append("		, i.listcover_imgurl");
				insertQuery.append("		, i.detailcover_imgurl");
				insertQuery.append("		, i.detail_imgurl");
				insertQuery.append(" ) VALUES ( ");
				insertQuery.append("		  d.b_num");
				insertQuery.append("		, d.listcover_imgurl");
				insertQuery.append("		, d.detailcover_imgurl");
				insertQuery.append("		, d.detail_imgurl");
				insertQuery.append(" ) ");

				pstmt = con.prepareStatement(insertQuery.toString());

				String listCover = "/resources/bookimg/" + vo.getCateOne_num() + "/" + vo.getCateTwo_num() + "/"
						+ vo.getB_num() + "-listcover.jpg";
				String detailCover = "/resources/bookimg/" + vo.getCateOne_num() + "/" + vo.getCateTwo_num() + "/"
						+ vo.getB_num() + "-detailcover.jpg";
				String detail = "/resources/bookimg/" + vo.getCateOne_num() + "/" + vo.getCateTwo_num() + "/"
						+ vo.getB_num() + "-detail.jpg";

				pstmt.setInt(1, vo.getB_num());

				if (vo.getListcover_imgurl() != null) {
					pstmt.setString(2, listCover);
				} else {
					pstmt.setString(2, null);
				}
				if (vo.getDetailcover_imgurl() != null) {
					pstmt.setString(3, detailCover);
				} else {
					pstmt.setString(3, null);
				}
				if (vo.getDetail_imgurl() != null) {
					pstmt.setString(4, detail);
				} else {
					pstmt.setString(4, null);
				}

//				System.out.println("URL: " + vo.getURL());
//				System.out.println("b_name: " + vo.getB_name());
//				System.out.println("vo.toString(): " + vo.toString());
//				System.out.println(insertQuery.toString());

				System.out.println("b_name: " + vo.getB_name());
				System.out.println("vo.toString(): " + vo.toString());
				System.out.println(insertQuery.toString());
				resultNum = pstmt.executeUpdate();
				System.out.println("MERGE INTO 성공");

				con.commit();	
				insertQuery.setLength(0);// StringBuilder 값 초기화
				pstmt.clearParameters();
				pstmt.close();
				System.out.println("[1]pstmt.isClose=" + pstmt.isClosed() + ", con.isClosed()=" + con.isClosed());

				System.out.println(vo.getB_name() + ", 도서정보 DB저장 완료(voList.size:" + voList.size() + ")");
			}
				
			if (resultNum == 1)
				isSuccess = true;
				
			System.out.println("========================DB저장 완료 ====================");

		} catch (SQLException se) {
			System.out.println("[booksInsert() 쿼리ERROR]" + se);
			se.printStackTrace();
		} catch (Exception e) {
			System.out.println("[booksInsert() ERROR]" + e);
			e.printStackTrace();
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (pstmt != null)
					pstmt.close();
				if (con != null)
					con.close();
			} catch (Exception e) {
				System.out.println("[booksInsert() DB연동 해제 ERROR]" + e);
				e.printStackTrace();
			}
		}
		return isSuccess;
	}
}
