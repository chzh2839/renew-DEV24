package com.dev24.admin.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class AdminMainController {
	
	@RequestMapping(value="/admin")
	public String adminLoginForm() {
		return "/admin/adminLoginForm";
	}
	
}
