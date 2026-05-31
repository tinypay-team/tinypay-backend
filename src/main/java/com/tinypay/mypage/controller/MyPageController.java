package com.tinypay.mypage.controller;

import com.tinypay.global.response.ApiResponse;
import com.tinypay.global.response.SuccessType;
import com.tinypay.mypage.dto.response.MyPageResponse;
import com.tinypay.mypage.service.MyPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageService myPageService;

    @GetMapping
    public ApiResponse<MyPageResponse> getMyPage(@RequestAttribute("userId") Long userId) {
        return ApiResponse.success(SuccessType.GET_MY_PAGE_SUCCESS, myPageService.getMyPage(userId));
    }
}