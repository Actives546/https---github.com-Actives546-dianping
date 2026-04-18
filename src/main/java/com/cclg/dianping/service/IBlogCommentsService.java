package com.cclg.dianping.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cclg.dianping.domain.BlogComments;
import com.cclg.dianping.dto.Result;

import java.util.List;

/**
 * 博客评论服务接口
 * 定义博客评论相关的业务操作方法
 *
 * @author system
 */
public interface IBlogCommentsService extends IService<BlogComments> {

    /**
     * 新增评论
     * 支持一级评论和回复评论
     *
     * @param comment 评论信息
     * @return 操作结果
     */
    Result saveComment(BlogComments comment);

    /**
     * 更新评论信息
     *
     * @param comment 评论信息
     * @return 操作结果
     */
    Result updateComment(BlogComments comment);

    /**
     * 根据ID查询评论信息
     *
     * @param id 评论ID
     * @return 评论信息
     */
    Result getCommentById(Long id);

    /**
     * 分页查询评论信息
     *
     * @param current 当前页码
     * @param size    每页大小
     * @param blogId  博客ID（可选，按博客筛选）
     * @param userId  用户ID（可选，按用户筛选）
     * @return 分页结果
     */
    Result queryCommentPage(Integer current, Integer size, Long blogId, Long userId);

    /**
     * 根据博客ID查询评论列表（分页）
     *
     * @param blogId  博客ID
     * @param current 当前页码
     * @param size    每页大小
     * @return 评论列表（分页）
     */
    Result queryCommentsByBlogId(Long blogId, Integer current, Integer size);

    /**
     * 根据ID删除评论
     *
     * @param id 评论ID
     * @return 操作结果
     */
    Result deleteCommentById(Long id);

    /**
     * 批量删除评论
     * 注意：有一个删除失败则整体失败
     *
     * @param ids 评论ID列表
     * @return 操作结果
     */
    Result deleteCommentByIds(List<Long> ids);
}
