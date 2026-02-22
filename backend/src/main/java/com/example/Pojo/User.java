package com.example.Pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private Long userId;// 用户id
    @NotBlank
    private String username;// 用户名
    private String password;// 密码
    private String gender;// 性别
    private String phone;// 手机号
    private String realName;// 真实姓名
    @Email
    private String email;// 邮箱
    private String avatarUrl;// 头像
    private String level;// 等级 见习学徒
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;// 创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime; // 更新时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginTime;// 最后登录时间
    private String status;// 状态 0:禁用 1:正常
    private Integer isRealNameVerified;// 是否实名认证 0:未认证 1:已认证
    private Integer experience;// 经验 默认0
}
