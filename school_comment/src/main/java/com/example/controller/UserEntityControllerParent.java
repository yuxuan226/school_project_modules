package com.example.controller;

/**
 * 用户实体Controller父接口
 * 用于解决 school_book 与 school_user 之间的循环依赖问题
 * school_book 依赖 school_comment（本接口所在模块），school_user 实现本接口
 * 通过 Spring 注入接口类型，运行时调用 school_user 的实现
 */
public interface UserEntityControllerParent {

    /**
     * 根据手机号查询用户ID
     * @param phone 手机号
     * @return 用户ID，未找到返回 null
     */
    Long selectByPhone(String phone);
}
