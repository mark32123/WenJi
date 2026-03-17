package com.example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 这是一个纯实验性质的测试类
 * 目标：在隔离环境中验证 Spring Security 密码加密器的行为
 */
@SpringBootTest // 1. 启动 Spring 容器，这样我们才能自动注入 Bean
public class PasswordEncoderIntegrationTest {

    // 2. 【核心】直接注入 PasswordEncoder
    // 在 Spring Boot 2.x/3.x 中，这默认就是 DelegatingPasswordEncoder
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void test_01_注册流程_自动生成前缀() {
        System.out.println("=== 实验 1: 模拟新用户注册 ===");

        String rawPassword = "mySecretPassword123";

        // 调用 encode 方法
        String encodedPassword = passwordEncoder.encode(rawPassword);

        System.out.println("原始密码: " + rawPassword);
        System.out.println("加密后存入DB: " + encodedPassword);

        // 【验证点 1】检查是否有 {bcrypt} 前缀
        assertTrue(encodedPassword.startsWith("{bcrypt}"),
                "失败！新生成的密码必须带有 {bcrypt} 前缀，否则旧系统无法识别");

        // 【验证点 2】检查每次加密结果是否不同 (因为加了盐 Salt)
//        String encodedPassword2 = passwordEncoder.encode(rawPassword);
//        System.out.println("再次加密:   " + encodedPassword2);
//        assertNotEquals(encodedPassword, encodedPassword2,
//                "失败！BCrypt 每次加密结果应该不同（因为随机盐），但都能匹配成功");
    }

    @Test
    void test_02_登录流程_自动匹配() {
        System.out.println("\n=== 实验 2: 模拟用户登录 ===");

        String rawPassword = "mySecretPassword123";
        // 模拟从数据库查出来的带前缀的密码 (假设是刚才生成的)
        String dbPassword = passwordEncoder.encode(rawPassword);

        System.out.println("用户输入: " + rawPassword);
        System.out.println("数据库值: " + dbPassword);

        // 调用 matches 方法
        boolean isMatch = passwordEncoder.matches(rawPassword, dbPassword);

        System.out.println("匹配结果: " + isMatch);
        assertTrue(isMatch, "登录应该成功！");
    }

    @Test
    void test_03_错误密码_匹配失败() {
        System.out.println("\n=== 实验 3: 模拟输错密码 ===");

        String wrongPassword = "wrongPassword";
        String dbPassword = passwordEncoder.encode("correctPassword");

        boolean isMatch = passwordEncoder.matches(wrongPassword, dbPassword);

        assertFalse(isMatch, "密码错误时应该返回 false");
        System.out.println("匹配结果: " + isMatch + " (预期为 false)");
    }

    @Test
    void test_04_手动模拟旧数据_如果没有前缀会怎样() {
        System.out.println("\n=== 实验 4: 模拟旧系统坑 (无前缀) ===");

        String rawPassword = "oldSystemPassword";
        // 模拟旧数据库里的数据：只有哈希值，没有 {bcrypt} 前缀
        String dirtyDbPassword = passwordEncoder.encode(rawPassword).replace("{bcrypt}", "");

        System.out.println("脏数据示例: " + dirtyDbPassword);

        try {
            boolean isMatch = passwordEncoder.matches(rawPassword, dirtyDbPassword);
            fail("预期会抛出异常，但没有抛出"); // 如果没报错，说明测试逻辑有问题
        } catch (IllegalArgumentException e) {
            System.out.println("❌ 捕获到经典错误: " + e.getMessage());

            // ✅ 修改这里：检查新版本的错误提示关键词
            // 新版本通常包含 "no PasswordEncoder" 或 "{noop}"
            assertTrue(
                    e.getMessage().contains("no PasswordEncoder") || e.getMessage().contains("{noop}"),
                    "错误信息应该提示缺少编码器或建议添加 {noop} 前缀"
            );

            System.out.println("✅ 验证通过：系统正确识别了无前缀的脏数据并给出了友好提示！");
        }
    }
}