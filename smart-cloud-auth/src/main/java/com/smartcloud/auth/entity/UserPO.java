package com.smartcloud.auth.entity;

import com.smartcloud.common.base.BasePO;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "t_user")
public class UserPO extends BasePO {

    @Column(nullable = false, unique = true, length = 64)
    private String username;

    @Column(nullable = false, length = 256)
    private String password;

    @Column(length = 64)
    private String nickname;

    @Column(length = 128)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(length = 512)
    private String avatar;

    @Column(nullable = false)
    private Integer status = 1;

    @Column(name = "last_login_time")
    private java.time.LocalDateTime lastLoginTime;
}
