package com.example.Service;

import com.example.Common.Result;
import com.example.DTO.LoginFormDTO;
import com.example.DTO.UserUpdateDTO;
import com.example.VO.UserInfoVO;
import com.example.VO.UserLoginVO;

public interface UserService {
    /* *
     * 用户登录
     * @Param loginDTO
     * @Return
     */
    Result<?> login(LoginFormDTO loginDTO);

    /* *
     * 获取用户信息
     * @Param userId
     * @Return
     */
    UserInfoVO getByUserId(Integer userId);

    /* *
     * 修改用户信息
     * @Param userId
     * @Return
     */
    void updateById(Integer userId, UserUpdateDTO userUpdateDTO);

    /* *
     * 删除用户
     * @Param userId
     * @Return
     */
    void removeUserById(Integer userId);
}
