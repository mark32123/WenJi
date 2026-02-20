package com.example.Service.Impl;

import com.example.Common.Result;
import com.example.DTO.LoginFormDTO;
import com.example.DTO.UserUpdateDTO;
import com.example.Mapper.UserMapper;
import com.example.Pojo.User;
import com.example.Service.UserService;
import com.example.Utils.CaptchaUtils;
import com.example.Utils.Md5Util;
import com.example.Utils.RegexUtils;
import com.example.VO.UserInfoVO;
import com.example.VO.UserLoginVO;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static com.example.Common.Result.error;
import static com.example.Config.LevelConfig.getLevelByExp;
import static com.example.Utils.JwtUtils.generateToken;
import static com.example.Utils.RedisConstants.CAPTCHA_PREFIX;

@Service
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    /**
     * 用户注册/登录
     *
     * @param loginFormDTO 登录/注册信息
     * @return 登录结果
     */
    @Override
    public Result<?> login(LoginFormDTO loginFormDTO) {
        // 1. 判断用户名是否存在
        User user = null;
        try {
            user = userMapper.selectByUserName(loginFormDTO.getUsername());
            if(user!=null){
                return handlerLogin(user,loginFormDTO);
            }else{
                return handlerRegister(loginFormDTO);
            }
        } catch (Exception e) {
            log.error("认证处理异常",e);
            throw new RuntimeException("认证失败,请稍后重试");
        }
    }


    private Result<UserLoginVO>handlerLogin(User user, LoginFormDTO loginFormDTO){
        // 2.1 用户存在，验证密码和验证码
        String encryptedPassword = Md5Util.getMD5String(loginFormDTO.getPassword());
        // 验证验证码（需要从前端传入captchaKey）
        if (isCaptchaValid(loginFormDTO.getCaptchaKey(), loginFormDTO.getCaptcha())
                && encryptedPassword.equals(user.getPassword())) {

            // 生成JWT token
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", user.getUserId());
            claims.put("username", user.getUsername());
            String token = generateToken(claims);

            UserLoginVO userLoginVO = new UserLoginVO();
            userLoginVO.setUserId(user.getUserId());
            userLoginVO.setAccessToken(token);
            userLoginVO.setLastLoginTime(LocalDateTime.now());

            log.info("用户登录成功：{}", user.getUsername());
            return Result.success(userLoginVO);
        } else {
            return error("密码错误或验证码错误，请重试", null);
        }
    }

    private Result<Void> handlerRegister(LoginFormDTO loginFormDTO){
        // 2.2 用户不存在，执行注册
        // 验证密码一致性
        if (!loginFormDTO.getPassword().equals(loginFormDTO.getRePassword())) {
            return error("密码不一致", null);
        }

        // 验证手机号格式
        if (RegexUtils.isPhoneInvalid(loginFormDTO.getPhone())) {
            return error("手机号格式错误", null);
        }

        // 执行注册
        String encryptedPassword = Md5Util.getMD5String(loginFormDTO.getPassword());
        userMapper.register(loginFormDTO.getUsername(), loginFormDTO.getPhone(), encryptedPassword);

        log.info("用户注册成功：{}", loginFormDTO.getUsername());
        return Result.success(); // 注册成功返回空结果
    }

    /**
     * 验证验证码
     * @param captchaKey    图形验证码
     * @param userInput     用户输入的验证码
     * @return              验证结果
     */
    private boolean isCaptchaValid(String captchaKey, String userInput) {
        if(captchaKey==null||userInput==null){
            return false;
        }
        // 获取Redis中的验证码
        String redisCaptcha = stringRedisTemplate.opsForValue().get(CAPTCHA_PREFIX + captchaKey);
        // 验证码不存在或过期
        if (redisCaptcha == null) {
            return false;
        }

        boolean isValid = redisCaptcha.equalsIgnoreCase(userInput);

        // 验证成功后删除验证码（一次性使用）
        if (isValid) {
            stringRedisTemplate.delete(CAPTCHA_PREFIX + captchaKey);
        }

        return isValid;
    }

    /**
     * 获取用户信息
     * @param userId 用户ID
     * @return 用户信息
     */
    @Override
    public UserInfoVO getByUserId(Integer userId) {
        return userMapper.getByUserId(userId);
    }

    /**
     * 修改用户信息
     *
     * @param userId 用户ID
     */
    @Override
    public void updateById(Integer userId, UserUpdateDTO userUpdateDTO) {
        // 检查用户是否存在
        User existingUser = userMapper.selectByUserId(userId);
        if(existingUser == null) {
            throw new RuntimeException("用户不存在");
        }
        // 如果提供了新密码，则需要验证旧密码
        if (userUpdateDTO.getNewPassword() != null && !userUpdateDTO.getNewPassword().isEmpty()) {
            // 检查旧密码是否正确
            if (userUpdateDTO.getPassword() == null || userUpdateDTO.getPassword().isEmpty()) {
                throw new RuntimeException("修改密码需要提供旧密码");
            }
            if (!Md5Util.getMD5String(userUpdateDTO.getPassword()).equals(existingUser.getPassword())) {
                throw new RuntimeException("旧密码错误");
            }
            // 验证新密码和确认密码是否一致
            if (!userUpdateDTO.getNewPassword().equals(userUpdateDTO.getRePassword())) {
                throw new RuntimeException("两次输入的新密码不一致");
            }
            // 将加密后的密码放入DTO中用于更新
            userUpdateDTO.setPassword(Md5Util.getMD5String(userUpdateDTO.getNewPassword()));
        }
        // 处理experience为null的情况，如果为null则使用0作为默认值
        Integer experience = userUpdateDTO.getExperience();
        if (experience != null) {
            String userLevel = getLevelByExp(experience);
            userUpdateDTO.setLevel(userLevel);
        } else {
            // 如果experience为null，保持原等级不变或设置为默认等级
            userUpdateDTO.setLevel(getLevelByExp(0)); // 设置为默认等级"见习学徒"
        }
        // 调用mapper更新用户信息
        userMapper.updateById(userId, userUpdateDTO);
    }
    /**
     * 删除用户
     * @param userId 用户ID
     */
    @Override
    public void removeUserById(Integer userId) {
        userMapper.removeUserById(userId);
    }
}
