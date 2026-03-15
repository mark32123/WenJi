package com.example;

import com.example.DTO.LoginFormDTO;
import com.example.DTO.UserUpdateDTO;
import com.example.Service.UserService;
import com.example.VO.UserInfoVO;
import com.example.VO.UserLoginVO;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户体验流程测试类
 * 目标：模拟用户从注册到使用各项功能的完整体验流程
 */
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserExperienceFlowTest {

    @Autowired
    private UserService userService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static String testUsername = "test" + (System.currentTimeMillis() % 1000000);
    private static String testPhone = "13800138011";
    private static String testPassword = "Test123456";
    private static String testToken;
    private static Long testUserId;

    @BeforeEach
    void setUp() {
        // 每个测试前的准备
        System.out.println("\n=== 准备执行测试 ===");
        // 检查Redis连接
        try {
            stringRedisTemplate.getConnectionFactory().getConnection().ping();
        } catch (Exception e) {
            System.err.println("警告：Redis连接失败，测试可能无法正常运行");
            System.err.println("错误信息：" + e.getMessage());
        }
    }

    @AfterEach
    void tearDown() {
        // 每个测试后的清理
        System.out.println("\n=== 测试执行完毕 ===");
    }

    /**
     * 测试1：用户注册流程
     * 流程：输入用户名、手机号、密码 → 系统验证 → 创建账户
     */
    @Test
    @Order(1)
    @DisplayName("用户注册流程测试")
    void test_01_用户注册流程() {
        System.out.println("=== 步骤1: 准备注册信息 ===");
        LoginFormDTO registerForm = new LoginFormDTO();
        registerForm.setUsername(testUsername);
        registerForm.setPhone(testPhone);
        registerForm.setPassword(testPassword);
        registerForm.setRePassword(testPassword);

        System.out.println("用户名: " + testUsername);
        System.out.println("手机号: " + testPhone);

        System.out.println("\n=== 步骤2: 执行注册 ===");
        var result = userService.login(registerForm);

        System.out.println("\n=== 步骤3: 验证注册结果 ===");
        assertNotNull(result, "注册结果不能为null");
        assertEquals(1, result.getCode(), "注册应该成功");
        System.out.println("✅ 注册成功");

        // 注册后立即登录获取用户ID
        System.out.println("\n=== 步骤4: 注册后登录获取用户ID ===");
        LoginFormDTO loginForm = new LoginFormDTO();
        loginForm.setUsername(testUsername);
        loginForm.setPhone(testPhone);
        loginForm.setPassword(testPassword);

        String captchaKey = "testCaptchaKey_" + System.currentTimeMillis();
        String captchaValue = "1234";
        try {
            stringRedisTemplate.opsForValue().set(
                "captcha:" + captchaKey,
                captchaValue.toLowerCase(),
                5,
                java.util.concurrent.TimeUnit.MINUTES
            );
            loginForm.setCaptcha(captchaValue);
            loginForm.setCaptchaKey(captchaKey);

            var loginResult = userService.login(loginForm);
            if (loginResult.getCode() == 200 && loginResult.getData() != null) {
                UserLoginVO loginVO = (UserLoginVO) loginResult.getData();
                testToken = loginVO.getAccessToken();
                testUserId = loginVO.getUserId();
                System.out.println("✅ 获取用户ID成功: " + testUserId);
            }
        } catch (Exception e) {
            System.err.println("警告：注册后登录失败，可能影响后续测试");
            System.err.println("错误信息：" + e.getMessage());
        }
    }

    /**
     * 测试2：用户登录流程
     * 流程：输入用户名、密码、验证码 → 系统验证 → 返回token
     */
    @Test
    @Order(2)
    @DisplayName("用户登录流程测试")
    void test_02_用户登录流程() {
        System.out.println("=== 步骤1: 准备登录信息 ===");
        LoginFormDTO loginForm = new LoginFormDTO();
        loginForm.setUsername(testUsername);
        loginForm.setPhone(testPhone);
        loginForm.setPassword(testPassword);

        // 模拟验证码（实际项目中需要先获取验证码）
        String captchaKey = "testCaptchaKey_" + System.currentTimeMillis();
        String captchaValue = "1234";
        stringRedisTemplate.opsForValue().set(
            "captcha:" + captchaKey,
            captchaValue.toLowerCase(),
            5,
            java.util.concurrent.TimeUnit.MINUTES
        );
        loginForm.setCaptcha(captchaValue);
        loginForm.setCaptchaKey(captchaKey);

        System.out.println("用户名: " + testUsername);
        System.out.println("验证码: " + captchaValue);

        System.out.println("\n=== 步骤2: 执行登录 ===");
        var result = userService.login(loginForm);

        System.out.println("\n=== 步骤3: 验证登录结果 ===");
        assertNotNull(result, "登录结果不能为null");
        assertEquals(1, result.getCode(), "登录应该成功");

        UserLoginVO loginVO = (UserLoginVO) result.getData();
        assertNotNull(loginVO, "登录数据不能为 null");
        assertNotNull(loginVO.getAccessToken(), "登录 token 不能为 null");
        testToken = loginVO.getAccessToken();
        testUserId = loginVO.getUserId();
                
        // 确保 testUserId 被正确赋值
        assertNotNull(testUserId, "testUserId 不能为 null，登录应该返回用户 ID");
        
        System.out.println("✅ 登录成功");
        System.out.println("Token: " + testToken);
        System.out.println("用户 ID: " + testUserId);
    }

    /**
     * 测试3：获取用户信息流程
     * 流程：使用token → 请求用户信息 → 返回用户详情
     */
    @Test
    @Order(3)
    @DisplayName("获取用户信息流程测试")
    void test_03_获取用户信息流程() {
        System.out.println("=== 步骤1: 使用token获取用户信息 ===");
        var result = userService.getUserProfile(testUserId, testToken);

        System.out.println("\n=== 步骤2: 验证用户信息 ===");
        assertNotNull(result, "用户信息结果不能为null");
        assertEquals(1, result.getCode(), "获取用户信息应该成功");

        UserInfoVO userInfo = result.getData();
        assertNotNull(userInfo, "用户信息数据不能为null");
        assertEquals(testUsername, userInfo.getUsername(), "用户名应该匹配");

        System.out.println("✅ 获取用户信息成功");
        System.out.println("用户名: " + userInfo.getUsername());
        System.out.println("手机号: " + userInfo.getPhone());
    }

    /**
     * 测试4：更新用户信息流程
     * 流程：准备更新数据 → 提交更新 → 验证更新结果
     */
    @Test
    @Order(4)
    @DisplayName("更新用户信息流程测试")
    void test_04_更新用户信息流程() {
        System.out.println("=== 步骤1: 准备更新数据 ===");
        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setUsername(testUsername);
        updateDTO.setRealName("测试用户");
//        updateDTO.setEmail("test@example.com");
        updateDTO.setGender("1");

        System.out.println("真实姓名: 测试用户");
        System.out.println("邮箱: test@example.com");

        System.out.println("\n=== 步骤2: 执行更新 ===");
        userService.updateById(testUserId, updateDTO);

        System.out.println("\n=== 步骤3: 验证更新结果 ===");
        UserInfoVO updatedInfo = userService.getByUserId(testUserId);
        assertNotNull(updatedInfo, "更新后的用户信息不能为null");
        assertEquals("测试用户", updatedInfo.getRealName(), "真实姓名应该更新");
        //assertEquals("test@example.com", updatedInfo.getEmail(), "邮箱应该更新");

        System.out.println("✅ 更新用户信息成功");
        System.out.println("更新后的真实姓名: " + updatedInfo.getRealName());
    }

    /**
     * 测试5：修改密码流程
     * 流程：输入旧密码和新密码 → 验证旧密码 → 更新密码
     */
    @Test
    @Order(5)
    @DisplayName("修改密码流程测试")
    void test_05_修改密码流程() {
        System.out.println("=== 步骤1: 准备密码更新数据 ===");
        String newPassword = "NewTest123456";
        UserUpdateDTO passwordUpdateDTO = new UserUpdateDTO();
        passwordUpdateDTO.setUsername(testUsername);
        passwordUpdateDTO.setPassword(testPassword);  // 旧密码
        passwordUpdateDTO.setNewPassword(newPassword);
        passwordUpdateDTO.setRePassword(newPassword);

        System.out.println("旧密码: " + testPassword);
        System.out.println("新密码: " + newPassword);

        System.out.println("\n=== 步骤2: 执行密码更新 ===");
        userService.updateById(testUserId, passwordUpdateDTO);

        System.out.println("\n=== 步骤3: 验证密码更新 ===");
        // 使用新密码登录验证
        LoginFormDTO loginForm = new LoginFormDTO();
        loginForm.setUsername(testUsername);
        loginForm.setPhone(testPhone);
        loginForm.setPassword(newPassword);

        String captchaKey = "testCaptchaKey_" + System.currentTimeMillis();
        String captchaValue = "1234";
        stringRedisTemplate.opsForValue().set(
            "captcha:" + captchaKey,
            captchaValue.toLowerCase(),
            5,
            java.util.concurrent.TimeUnit.MINUTES
        );
        loginForm.setCaptcha(captchaValue);
        loginForm.setCaptchaKey(captchaKey);

        var result = userService.login(loginForm);
        assertEquals(1, result.getCode(), "使用新密码登录应该成功");

        // 更新测试密码供后续测试使用
        testPassword = newPassword;

        System.out.println("✅ 密码修改成功");
    }

    /**
     * 测试6：用户登出流程
     * 流程：使用token → 执行登出 → 验证token失效
     */
    @Test
    @Order(6)
    @DisplayName("用户登出流程测试")
    void test_06_用户登出流程() {
        System.out.println("=== 步骤1: 执行登出 ===");
        var result = userService.logout(testToken);

        System.out.println("\n=== 步骤2: 验证登出结果 ===");
        assertNotNull(result, "登出结果不能为null");
        assertEquals(1, result.getCode(), "登出应该成功");

        // 验证Redis中的token是否被删除
        String redisKey = "login:token:" + testToken;
        Boolean exists = stringRedisTemplate.hasKey(redisKey);
        assertFalse(exists, "登出后token应该从Redis中删除");

        System.out.println("✅ 用户登出成功");
    }

    /**
     * 测试7：删除用户流程
     * 流程：使用用户ID → 执行删除 → 验证用户不存在
     */
    @Test
    @Order(7)
    @DisplayName("删除用户流程测试")
    void test_07_删除用户流程() {
        System.out.println("=== 步骤 1: 验证用户 ID 是否存在 ===");
        assertNotNull(testUserId, "用户 ID 不能为 null，请确保前面的登录测试成功执行");
            
        System.out.println("用户 ID: " + testUserId);
            
        System.out.println("\n=== 步骤 2: 执行用户删除 ===");
        userService.removeUserById(testUserId);

        System.out.println("\n=== 步骤2: 验证用户删除 ===");
        UserInfoVO deletedUser = userService.getByUserId(testUserId);
        assertNull(deletedUser, "删除后用户应该不存在");

        System.out.println("✅ 用户删除成功");
    }
}
