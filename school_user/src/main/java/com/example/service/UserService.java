package com.example.service;

import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.User;

import java.util.Map;

public interface UserService extends IService<User> {

    /** 分页条件查询 */
    SaResult selectPage(Integer page, Integer size, Map<String, Object> params);

    /** 保存用户：手机号查重 + 密码加密 */
    SaResult saveUser(User user);

    /** 修改用户：存在校验 + 密码变更检测 + 加密 */
    SaResult updateUser(User user);

    /** 删除用户：存在校验 */
    SaResult deleteUser(Long id);

    /** 初始化密码 */
    SaResult initPassword(Long id);

    /** 登录：返回 token */
    SaResult login(String phone, String password);
}
