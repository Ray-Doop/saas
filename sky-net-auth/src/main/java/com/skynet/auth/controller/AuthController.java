package com.skynet.auth.controller;

import com.skynet.auth.dto.LoginDTO;
import com.skynet.auth.dto.RegisterDTO;
import com.skynet.auth.qo.UserQO;
import com.skynet.auth.service.AuthService;
import com.skynet.auth.vo.LoginVO;
import com.skynet.auth.vo.UserVO;
import com.skynet.common.core.result.PageResult;
import com.skynet.common.core.result.Result;
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
