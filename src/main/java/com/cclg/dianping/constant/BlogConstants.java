package com.cclg.dianping.constant;

public final class BlogConstants {

    private BlogConstants() {
        throw new AssertionError("常量类不能被实例化");
    }

    public static final Integer DEFAULT_PAGE_CURRENT = 1;
    public static final Integer DEFAULT_PAGE_SIZE = 10;
    public static final Integer MAX_PAGE_SIZE = 100;

    public static final Integer MAX_IMAGES_COUNT = 9;
    public static final String IMAGES_SEPARATOR = ",";

    public static final Integer DEFAULT_LIKED = 0;
    public static final Integer DEFAULT_COMMENTS = 0;
    public static final Long DEFAULT_PARENT_ID = 0L;
    public static final Boolean COMMENT_STATUS_NORMAL = true;
    public static final Boolean COMMENT_STATUS_BLOCKED = false;

    public static final Integer DELTA_ONE = 1;

    public static final String BLOG_INFO_NOT_NULL = "博客信息不能为空";
    public static final String BLOG_TITLE_NOT_NULL = "博客标题不能为空";
    public static final String BLOG_ID_NOT_NULL = "博客ID不能为空";
    public static final String BLOG_NOT_EXIST = "博客不存在";
    public static final String BLOG_ID_LIST_NOT_NULL = "博客ID列表不能为空";
    public static final String BLOG_CONTENT_NOT_NULL = "博客内容不能为空";
    public static final String USER_ID_NOT_NULL = "用户ID不能为空";
    public static final String USER_NOT_EXIST = "用户不存在";
    public static final String IMAGES_COUNT_EXCEED = "图片数量不能超过" + MAX_IMAGES_COUNT + "张";

    public static final String COMMENT_INFO_NOT_NULL = "评论信息不能为空";
    public static final String COMMENT_CONTENT_NOT_NULL = "评论内容不能为空";
    public static final String COMMENT_ID_NOT_NULL = "评论ID不能为空";
    public static final String COMMENT_NOT_EXIST = "评论不存在";
    public static final String COMMENT_ID_LIST_NOT_NULL = "评论ID列表不能为空";
    public static final String COMMENT_BLOG_ID_NOT_NULL = "评论关联的博客ID不能为空";
    public static final String COMMENT_PARENT_NOT_EXIST = "父评论不存在";
    public static final String COMMENT_ANSWER_NOT_EXIST = "回复的评论不存在";
    public static final String COMMENT_PARENT_NOT_IN_BLOG = "父评论不属于当前博客";
    public static final String COMMENT_ANSWER_NOT_IN_BLOG = "回复的评论不属于当前博客";
    public static final String COMMENT_ANSWER_NOT_IN_PARENT = "回复的评论不属于当前父评论";
    public static final String COMMENT_NOT_OWNER = "无权操作该评论";
    public static final String USER_NOT_LOGIN = "用户未登录";

    public static final String BLOG_CREATE_SUCCESS = "博客创建成功，博客ID：{}";
    public static final String BLOG_CREATE_FAIL = "博客创建失败";
    public static final String BLOG_UPDATE_SUCCESS = "博客更新成功，博客ID：{}";
    public static final String BLOG_UPDATE_FAIL = "博客更新失败";
    public static final String BLOG_DELETE_SUCCESS = "博客删除成功，博客ID：{}";
    public static final String BLOG_DELETE_FAIL = "博客删除失败";
    public static final String BLOG_BATCH_DELETE_SUCCESS = "批量删除博客成功，博客数量：{}";
    public static final String BLOG_BATCH_DELETE_FAIL = "批量删除博客失败";

    public static final String COMMENT_CREATE_SUCCESS = "评论创建成功，评论ID：{}";
    public static final String COMMENT_CREATE_FAIL = "评论创建失败";
    public static final String COMMENT_UPDATE_SUCCESS = "评论更新成功，评论ID：{}";
    public static final String COMMENT_UPDATE_FAIL = "评论更新失败";
    public static final String COMMENT_DELETE_SUCCESS = "评论删除成功，评论ID：{}";
    public static final String COMMENT_DELETE_FAIL = "评论删除失败";
    public static final String COMMENT_BATCH_DELETE_SUCCESS = "批量删除评论成功，评论数量：{}";
    public static final String COMMENT_BATCH_DELETE_FAIL = "批量删除评论失败";
    public static final String COMMENT_CASCADE_DELETE = "级联删除子评论成功，共删除 {} 条评论";
    public static final String COMMENT_COUNT_UPDATE_FAIL = "更新博客评论数失败，博客ID：{}";
    public static final String COMMENT_COUNT_UPDATE_FAIL_WITH_DELTA = "更新博客评论数失败，博客ID：{}，数量变化：{}";

    public static final String LOG_BLOG_PAGE_QUERY = "分页查询博客，当前页：{}，每页大小：{}，博客标题：{}";
    public static final String LOG_BLOG_SAVE = "新增博客，博客标题：{}";
    public static final String LOG_BLOG_UPDATE = "更新博客，博客ID：{}";
    public static final String LOG_BLOG_GET = "根据ID查询博客，博客ID：{}";
    public static final String LOG_BLOG_DELETE = "删除博客，博客ID：{}";
    public static final String LOG_BLOG_BATCH_DELETE = "批量删除博客，博客数量：{}";

    public static final String LOG_COMMENT_PAGE_QUERY = "分页查询评论，当前页：{}，每页大小：{}，博客ID：{}";
    public static final String LOG_COMMENT_SAVE = "新增评论，博客ID：{}";
    public static final String LOG_COMMENT_UPDATE = "更新评论，评论ID：{}";
    public static final String LOG_COMMENT_GET = "根据ID查询评论，评论ID：{}";
    public static final String LOG_COMMENT_DELETE = "删除评论，评论ID：{}";
    public static final String LOG_COMMENT_BATCH_DELETE = "批量删除评论，评论数量：{}";
    public static final String LOG_COMMENT_BY_BLOG = "根据博客ID查询评论列表，博客ID：{}，当前页：{}，每页大小：{}";
}
