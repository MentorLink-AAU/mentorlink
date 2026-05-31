package com.mentorlink.modules.test;

import com.mentorlink.common.dto.ApiResponse;
import com.mentorlink.common.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/ping")
    public ApiResponse<String> ping() {
        return ApiResponse.success("pong 🏓");
    }

    @GetMapping("/error")
    public ApiResponse<String> forceError() {
        throw new ApiException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "This is a forced error for testing.");
    }
}
