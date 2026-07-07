package com.dev24.client.cart.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.dev24.client.cart.service.CartService;
import com.dev24.client.cart.vo.CartVO;
import com.dev24.client.login.vo.LoginVO;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j;

@Controller
@RequestMapping("/cart/*")
@Log4j
@AllArgsConstructor
public class CartController {
	private CartService cartService;

	/************************************************
	 * cart_view를 이용해 장바구니 리스트 출력 일반도서, ebook (대분류) 나눠 출력하기
	 **************/
	@GetMapping("/cartList")
	public String CartList(@ModelAttribute("data") CartVO cvo, Model model, HttpSession session) {
		log.info("cartList() 메서드 호출");

		LoginVO lvo = (LoginVO) session.getAttribute("login");
		int c_num = lvo.getC_num();
		log.info(lvo);
		log.info("c_num : "+c_num);
		
		cvo.setC_num(c_num);
		
		List<CartVO> list = cartService.cartList(cvo);
		List<CartVO> list1 = new ArrayList<CartVO>();
		List<CartVO> list2 = new ArrayList<CartVO>();

		for (int i = 0; i < list.size(); i++) {
			int cate = list.get(i).getCateone_num();
			if (cate == 1) { // 일반도서일때
				CartVO vo1 = list.get(i);
				list1.add(vo1);
			} else if (cate == 2) { // ebook일때
				CartVO vo2 = list.get(i);
				list2.add(vo2);
			}
		}

		model.addAttribute("cartList1", list1);
		model.addAttribute("cartList2", list2);

		return "cart/cartList";
	}

	/************************************************
	 * 장바구니 수량 변경 REST방식에서 UPDATE작업은 PUT, PATCH방식을 이용해서 처리.
	 **************/
	@RequestMapping(value = "/{crt_num}", method = { RequestMethod.PUT,
			RequestMethod.PATCH }, consumes = "application/json", produces = { MediaType.TEXT_PLAIN_VALUE })
	public ResponseEntity<String> cartUpdate(@PathVariable("crt_num") int crt_num, @RequestBody CartVO cvo) {
		log.info("cartUpdate() 호출 성공");
		cvo.setCrt_num(crt_num);
		int result = cartService.cartUpdate(cvo);
		return result == 1 ? new ResponseEntity<String>("SUCCESS", HttpStatus.OK)
				: new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
	}

	/************************************************
	 * 장바구니 상품 삭제 REST방식에서 DELETE작업은 DELETE방식을 이용해서 처리.
	 **************/
	@DeleteMapping(value = "/{crt_num}", produces = { MediaType.TEXT_PLAIN_VALUE })
	public ResponseEntity<String> cartDelete(@PathVariable("crt_num") int crt_num) {
		log.info("cartDelete() 호출 성공");
		log.info("crt_num : " + crt_num);
		int result = cartService.cartDelete(crt_num);
		return result == 1 ? new ResponseEntity<String>("SUCCESS", HttpStatus.OK)
				: new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@PostMapping(value = "/addToCart", produces = "text/plain; charset=utf8")
	@ResponseBody
	public String addToCart(@RequestBody List<Map<String, Object>> cartList, HttpSession session) {

		log.info("addToCart 호출 성공");

		String returnStr = "";
		LoginVO lvo = (LoginVO) session.getAttribute("login");
		int c_num = lvo.getC_num();
		log.info(lvo);
		log.info("c_num : "+c_num);
		
		CartVO cvo;
		List<CartVO> cvoList = new ArrayList<CartVO>();

		for (Map<String, Object> map : cartList) {
			cvo = new CartVO();

			int b_num = Integer.parseInt(map.get("b_num") + "");
			int crt_qty = Integer.parseInt(map.get("crt_qty") + "");
			int crt_price = Integer.parseInt(map.get("crt_price") + "");

			cvo.setB_num(b_num);
			cvo.setCrt_qty(crt_qty);
			cvo.setCrt_price(crt_price);
			cvo.setC_num(c_num);

			cvoList.add(cvo);
		}

		int result = cartService.addToCart(cvoList);

		if (result == 1) {
			returnStr = "SUCCESS";
		} else {
			returnStr = "FAIL";
		}
		return returnStr;
	}

	/***
	 * 단일항목 구매를 위해 cart_num_seq.nextval 값을 얻기위한 메소드 
	 * @param cvo
	 * @param session
	 * @return
	 */
	@RequestMapping(value = "/buySingleItem", produces = "text/plain; charset=utf8", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<String> buySingleItem(@RequestBody CartVO cvo, HttpSession session) {

		log.info("buySingleItem 호출 성공");

		int crt_num = cartService.getCrtNum();
		log.info("crt_num: " + crt_num);
		int returnVal = -1;
		int result = -1;
		ResponseEntity<String> entity;

		log.info(cvo.toString());

		LoginVO lvo = (LoginVO) session.getAttribute("login");
		int c_num = lvo.getC_num();
		log.info(lvo);
		log.info("c_num : "+c_num);

		cvo.setC_num(c_num);
		cvo.setCrt_num(crt_num);
		log.info("cvo 에 c_num, crt_num 추가");
		log.info("cvoList 에 cvo추가 \n\t" + cvo.toString());
		result = cartService.buySingleItem(cvo);
		log.info("result: " + result);
		
		if (result == 1) {
			log.info("단일 항목 추가 성공");
			returnVal = crt_num;
			entity = new ResponseEntity<String>(returnVal+"", HttpStatus.OK);
		} else {
			log.info("단일 항목 추가 실패");
			returnVal = -1;
			entity = new ResponseEntity<String>(returnVal+"", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		return entity;
	}

}
