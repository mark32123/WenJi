package com.example.Controller.User;

import com.example.Common.Result;
import com.example.DTO.UserLoginDTO;
import com.example.DTO.UserRegistDTO;
import com.example.DTO.UserUpdateDTO;
import com.example.Mapper.UserMapper;
import com.example.Pojo.User;
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
     * 用户注册
     * @Param RegisterDTO
     * @Return
     */
    @PostMapping("/register")
    public Result register(@Validated @RequestBody UserRegistDTO userRegistDTO){
        log.info("用户注册：用户名：{}",userRegistDTO.getUsername());
        //判断是否已注册
        User user = userMapper.selectByUserName(userRegistDTO.getUsername());
        if(user != null){
            return Result.error("用户名已被注册");
        }
        userService.register(userRegistDTO);
        return Result.success();
    }
    /**
     * 用户登录
     * @Param LoginDTO
     * @Return
     */
    @PostMapping("/login")
    public Result<UserLoginVO> login(@Validated @RequestBody UserLoginDTO userLoginDTO){
        log.info("用户登录：{}",userLoginDTO);
        Result<UserLoginVO> result = userService.login(userLoginDTO);
        return result;
    }

    /**
     * 获取用户信息
     * @Param userId
     * @Return
     */
    @GetMapping("/info/{userId}")
    public Result getById(@PathVariable Integer userId){
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
    public Result removeUserById(@PathVariable Integer userId){
        if(userService.getByUserId(userId)==null){
            return Result.error("用户不存在");
        }
        log.info("注销用户：{}",userId);
        userService.removeUserById(userId);
        return Result.success();
    }
}
