package com.skynet.auth.service;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.skynet.auth.dto.LoginDTO;
import com.skynet.auth.dto.RegisterDTO;
import com.skynet.auth.entity.QUserPO;
import com.skynet.auth.entity.UserPO;
import com.skynet.auth.qo.UserQO;
import com.skynet.auth.repository.UserRepository;
import com.skynet.auth.vo.LoginVO;
import com.skynet.auth.vo.UserVO;
import com.skynet.common.core.exception.BizException;
import com.skynet.common.core.result.PageResult;
import com.skynet.common.core.result.ResultCode;
import com.skynet.common.security.utils.JwtUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JPAQueryFactory queryFactory;
    private final PasswordEncoder passwordEncoder;

    public LoginVO login(LoginDTO dto) {
        UserPO user = userRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new BizException(ResultCode.USER_NOT_EXIST));

        if (user.getStatus() != 1) {
            throw new BizException(ResultCode.USER_DISABLED);
        }

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BizException(ResultCode.PASSWORD_ERROR);
        }

        user.setLastLoginTime(LocalDateTime.now());
        userRepository.save(user);

        String token = JwtUtils.createToken(user.getId(), user.getUsername());

        UserVO userVO = toVO(user);
        return LoginVO.builder()
                .token(token)
                .user(userVO)
                .build();
    }

    @Transactional
    public UserVO register(RegisterDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new BizException("用户名已存在");
        }

        UserPO user = new UserPO();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname() != null ? dto.getNickname() : dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setStatus(1);

        userRepository.save(user);
        return toVO(user);
    }

    public UserVO getById(Long id) {
        UserPO user = userRepository.findById(id)
                .orElseThrow(() -> new BizException(ResultCode.USER_NOT_EXIST));
        return toVO(user);
    }

    public PageResult<UserVO> page(UserQO qo) {
        QUserPO q = QUserPO.userPO;
        JPAQuery<UserVO> query = queryFactory
                .select(Projections.bean(UserVO.class,
                        q.id, q.username, q.nickname, q.email,
                        q.phone, q.avatar, q.status,
                        q.lastLoginTime, q.createTime))
                .from(q)
                .where(q.isDeleted.eq(0));

        if (qo.getUsername() != null) {
            query.where(q.username.like("%" + qo.getUsername() + "%"));
        }
        if (qo.getNickname() != null) {
            query.where(q.nickname.like("%" + qo.getNickname() + "%"));
        }
        if (qo.getStatus() != null) {
            query.where(q.status.eq(qo.getStatus()));
        }

        long total = query.fetchCount();
        long offset = (long) (qo.getPage() - 1) * qo.getSize();
        List<UserVO> records = query.offset(offset).limit(qo.getSize()).fetch();

        return PageResult.of(total, qo.getPage(), qo.getSize(), records);
    }

    private UserVO toVO(UserPO po) {
        UserVO vo = new UserVO();
        vo.setId(po.getId());
        vo.setUsername(po.getUsername());
        vo.setNickname(po.getNickname());
        vo.setEmail(po.getEmail());
        vo.setPhone(po.getPhone());
        vo.setAvatar(po.getAvatar());
        vo.setStatus(po.getStatus());
        vo.setLastLoginTime(po.getLastLoginTime());
        vo.setCreateTime(po.getCreateTime());
        return vo;
    }
}
