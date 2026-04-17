package com.cclg.dianping.constant;

public final class UserConstants {

    private UserConstants() {
        throw new AssertionError("常量类不能被实例化");
    }

    public static final String USER_INFO_NOT_NULL = "用户信息不能为空";
    public static final String USER_PHONE_NOT_NULL = "手机号不能为空";
    public static final String USER_PHONE_INVALID = "手机号格式错误";
    public static final String USER_PASSWORD_NOT_NULL = "密码不能为空";
    public static final String USER_ID_NOT_NULL = "用户ID不能为空";
    public static final String USER_NOT_EXIST = "用户不存在";
    public static final String USER_ID_LIST_NOT_NULL = "用户ID列表不能为空";
    public static final String USER_PHONE_EXIST = "手机号已存在";
    public static final String USER_NOT_LOGIN = "用户未登录";

    public static final String LOGIN_SUCCESS = "登录成功";
    public static final String LOGIN_FAIL = "登录失败";
    public static final String LOGOUT_SUCCESS = "退出登录成功";
    public static final String CODE_SEND_SUCCESS = "验证码发送成功";
    public static final String CODE_SEND_FAIL = "验证码发送失败";
    public static final String CODE_INVALID = "验证码错误或已过期";

    public static final String USER_CREATE_SUCCESS = "用户创建成功，用户ID：{}";
    public static final String USER_CREATE_FAIL = "用户创建失败";
    public static final String USER_UPDATE_SUCCESS = "用户更新成功，用户ID：{}";
    public static final String USER_UPDATE_FAIL = "用户更新失败";
    public static final String USER_DELETE_SUCCESS = "用户删除成功，用户ID：{}";
    public static final String USER_DELETE_FAIL = "用户删除失败";
    public static final String USER_BATCH_DELETE_SUCCESS = "批量删除用户成功，用户数量：{}";
    public static final String USER_BATCH_DELETE_FAIL = "批量删除用户失败";

    public static final String LOG_PAGE_QUERY = "分页查询用户，当前页：{}，每页大小：{}，手机号：{}，昵称：{}";
    public static final String LOG_SAVE_USER = "新增用户，手机号：{}";
    public static final String LOG_UPDATE_USER = "更新用户，用户ID：{}";
    public static final String LOG_GET_USER = "根据ID查询用户，用户ID：{}";
    public static final String LOG_GET_CURRENT_USER = "获取当前登录用户信息";
    public static final String LOG_DELETE_USER = "删除用户，用户ID：{}";
    public static final String LOG_BATCH_DELETE_USER = "批量删除用户，用户数量：{}";
    public static final String LOG_LOGIN = "用户登录，手机号：{}";
    public static final String LOG_LOGOUT = "用户退出登录，用户ID：{}";

    public static final Integer DEFAULT_PAGE_CURRENT = 1;
    public static final Integer DEFAULT_PAGE_SIZE = 10;
    public static final Integer MAX_PAGE_SIZE = 100;
}
