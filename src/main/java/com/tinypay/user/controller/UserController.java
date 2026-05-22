package com.tinypay.user.controller;

import com.tinypay.global.response.ApiResponse;
import com.tinypay.global.response.SuccessType;
import com.tinypay.user.dto.request.UpdateMyInfoRequest;
import com.tinypay.user.dto.response.GetMyInfoResponse;
import com.tinypay.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ApiResponse<GetMyInfoResponse> getMyInfo(@RequestAttribute("userId") Long userId) {
        return ApiResponse.success(SuccessType.GET_MY_INFO_SUCCESS, userService.getMyInfo(userId));
    }

    @PatchMapping("/me")
    public ApiResponse<?> updateMyInfo(
            @RequestAttribute("userId") Long userId,
            @RequestBody UpdateMyInfoRequest request) {
        userService.updateMyInfo(userId, request);
        return ApiResponse.success(SuccessType.UPDATE_MY_INFO_SUCCESS);
    }
}