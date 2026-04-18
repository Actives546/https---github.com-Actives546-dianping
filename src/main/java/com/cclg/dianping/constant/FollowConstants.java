package com.cclg.dianping.constant;

public final class FollowConstants {

    private FollowConstants() {
        throw new AssertionError("常量类不能被实例化");
    }

    public static final Integer DEFAULT_PAGE_CURRENT = 1;
    public static final Integer DEFAULT_PAGE_SIZE = 10;
    public static final Integer MAX_PAGE_SIZE = 100;

    public static final String FOLLOW_USER_ID_NOT_NULL = "被关注用户ID不能为空";
    public static final String CANNOT_FOLLOW_SELF = "不能关注自己";
    public static final String USER_NOT_EXIST = "用户不存在";
    public static final String ALREADY_FOLLOWED = "已经关注了该用户";
    public static final String NOT_FOLLOWED = "未关注该用户";

    public static final String FOLLOW_SUCCESS = "关注成功";
    public static final String FOLLOW_FAIL = "关注失败";
    public static final String UNFOLLOW_SUCCESS = "取消关注成功";
    public static final String UNFOLLOW_FAIL = "取消关注失败";

    public static final String FOLLOW_CREATE_SUCCESS = "关注关系创建成功，关注者ID：{}，被关注者ID：{}";
    public static final String FOLLOW_DELETE_SUCCESS = "关注关系删除成功，关注者ID：{}，被关注者ID：{}";

    public static final String LOG_FOLLOW = "关注用户，被关注用户ID：{}";
    public static final String LOG_UNFOLLOW = "取消关注，被关注用户ID：{}";
    public static final String LOG_IS_FOLLOW = "查询是否关注用户，被关注用户ID：{}";
    public static final String LOG_FOLLOW_COUNT = "查询关注数量，用户ID：{}";
    public static final String LOG_FANS_COUNT = "查询粉丝数量，用户ID：{}";
    public static final String LOG_COMMON_FOLLOW = "查询共同关注，目标用户ID：{}";
    public static final String LOG_FOLLOW_LIST = "查询关注列表，用户ID：{}，当前页：{}，每页大小：{}";
    public static final String LOG_FANS_LIST = "查询粉丝列表，用户ID：{}，当前页：{}，每页大小：{}";
}
