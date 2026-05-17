package com.skynet.auth.qo;

import lombok.Data;

@Data
public class UserQO {
    private String username;
    private String nickname;
    private String email;
    private Integer status;
    private Integer page = 1;
    private Integer size = 10;
}
