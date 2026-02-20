package com.example.Controller;

import com.example.Common.Result;
import com.example.DTO.LoginFormDTO;
import com.example.DTO.UserUpdateDTO;
import com.example.Mapper.UserMapper;
import com.example.Service.UserService;
import com.example.VO.UserInfoVO;
import com.example.VO.UserLoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;
    /**
     * 用户登录/注册
     * @param loginFormDTO 登录信息
     * @return 登录结果
     */
    @PostMapping("/login")
    public Result<?> login(@Validated @RequestBody LoginFormDTO loginFormDTO){
        return userService.login(loginFormDTO);
    }


    /**
     * 获取用户信息
     * @param userId 用户ID
     * @return UserInfoVO
     */
    @GetMapping("/info/{userId}")
    public Result<UserInfoVO> getById(@PathVariable Integer userId){
        UserInfoVO userInfo = userService.getByUserId(userId);
        log.info("获取用户信息：{}",userInfo);
        return Result.success(userInfo);
    }

    /**
     * 修改用户信息
     * @Param userId
     * @Return
     */
    @PutMapping("/updateUserInfo/{userId}")
    public Result<UserInfoVO> updateUserInfoById(@PathVariable Integer userId, @Validated @RequestBody UserUpdateDTO userUpdateDTO){
        log.info("修改用户信息：{}",userUpdateDTO);
        //执行更新
        userService.updateById(userId,userUpdateDTO);
        //重新查询最新数据
        UserInfoVO userInfoVO = userService.getByUserId(userId);
        return Result.success(userInfoVO);
    }

    /**
     * 注销用户信息
     * @Param userId
     * @Return
     */
    @DeleteMapping("/deleteUserInfo/{userId}")
    public Result<Void> removeUserById(@PathVariable Integer userId){
        if(userService.getByUserId(userId)==null){
            return Result.error("用户不存在");
        }
        log.info("注销用户：{}",userId);
        userService.removeUserById(userId);
        return Result.success();
    }
}
