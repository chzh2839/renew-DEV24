package com.dev24.bookstore.auth.service;

import com.dev24.bookstore.auth.domain.Admin;

public interface AdminService {

    Admin authenticate(String loginId, String rawPassword);
}
