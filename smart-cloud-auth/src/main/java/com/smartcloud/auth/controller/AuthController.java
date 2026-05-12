package com.smartcloud.auth.controller;

import com.smartcloud.auth.dto.LoginDTO;
import com.smartcloud.auth.dto.RegisterDTO;
import com.smartcloud.auth.qo.UserQO;
import com.smartcloud.auth.service.AuthService;
import com.smartcloud.auth.vo.LoginVO;
import com.smartcloud.auth.vo.UserVO;
import com.smartcloud.common.result.PageResult;
import com.smartcloud.common.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.ok(authService.login(dto));
    }

    @PostMapping("/register")
    public Result<UserVO> register(@Valid @RequestBody RegisterDTO dto) {
        return Result.ok(authService.register(dto));
    }

    @GetMapping("/user/{id}")
    public Result<UserVO> getUser(@PathVariable Long id) {
        return Result.ok(authService.getById(id));
    }

    @GetMapping("/user/current")
    public Result<UserVO> currentUser(@RequestHeader("X-User-Id") Long userId) {
        return Result.ok(authService.getById(userId));
    }

    @GetMapping("/user/page")
    public Result<PageResult<UserVO>> page(UserQO qo) {
        return Result.ok(authService.page(qo));
    }
}
