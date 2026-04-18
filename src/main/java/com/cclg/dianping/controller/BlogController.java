package com.cclg.dianping.controller;

import com.cclg.dianping.constant.BlogConstants;
import com.cclg.dianping.domain.Blog;
import com.cclg.dianping.domain.BlogComments;
import com.cclg.dianping.dto.Result;
import com.cclg.dianping.service.IBlogCommentsService;
import com.cclg.dianping.service.IBlogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 博客管理控制器
 * 提供博客和评论的增删改查等接口
 *
 * @author system
 */
@Slf4j
@RestController
@RequestMapping("/blog")
public class BlogController {

    @Resource
    private IBlogService blogService;

    @Resource
    private IBlogCommentsService blogCommentsService;

    // ==================== 博客相关接口 ====================

    /**
     * 新增博客
     *
     * @param blog 博客信息
     * @return 操作结果，成功返回博客ID
     */
    @PostMapping
    public Result saveBlog(@RequestBody Blog blog) {
        log.info(BlogConstants.LOG_BLOG_SAVE, blog.getTitle());
        return blogService.saveBlog(blog);
    }

    /**
     * 更新博客信息
     *
     * @param blog 博客信息
     * @return 操作结果
     */
    @PutMapping
    public Result updateBlog(@RequestBody Blog blog) {
        log.info(BlogConstants.LOG_BLOG_UPDATE, blog.getId());
        return blogService.updateBlog(blog);
    }

    /**
     * 根据ID查询博客信息
     *
     * @param id 博客ID
     * @return 博客信息
     */
    @GetMapping("/{id}")
    public Result getBlogById(@PathVariable("id") Long id) {
        log.info(BlogConstants.LOG_BLOG_GET, id);
        return blogService.getBlogById(id);
    }

    /**
     * 分页查询博客信息
     * 支持按博客标题、用户ID、商铺ID筛选
     *
     * @param current 当前页码，默认1
     * @param size    每页大小，默认10，最大100
     * @param title   博客标题（可选，模糊匹配）
     * @param userId  用户ID（可选，按用户筛选）
     * @param shopId  商铺ID（可选，按商铺筛选）
     * @return 分页结果，包含数据列表和总记录数
     */
    @GetMapping("/page")
    public Result queryBlogPage(
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "shopId", required = false) Long shopId) {
        log.info(BlogConstants.LOG_BLOG_PAGE_QUERY, current, size, title);
        return blogService.queryBlogPage(current, size, title, userId, shopId);
    }

    /**
     * 根据ID删除博客
     *
     * @param id 博客ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result deleteBlogById(@PathVariable("id") Long id) {
        log.info(BlogConstants.LOG_BLOG_DELETE, id);
        return blogService.deleteBlogById(id);
    }

    /**
     * 批量删除博客
     *
     * @param ids 博客ID列表
     * @return 操作结果
     */
    @DeleteMapping("/batch")
    public Result deleteBlogByIds(@RequestBody List<Long> ids) {
        log.info(BlogConstants.LOG_BLOG_BATCH_DELETE, ids.size());
        return blogService.deleteBlogByIds(ids);
    }

    // ==================== 评论相关接口 ====================

    /**
     * 新增评论
     * 支持一级评论和回复评论
     *
     * @param comment 评论信息
     * @return 操作结果，成功返回评论ID
     */
    @PostMapping("/comment")
    public Result saveComment(@RequestBody BlogComments comment) {
        log.info(BlogConstants.LOG_COMMENT_SAVE, comment.getBlogId());
        return blogCommentsService.saveComment(comment);
    }

    /**
     * 更新评论信息
     *
     * @param comment 评论信息
     * @return 操作结果
     */
    @PutMapping("/comment")
    public Result updateComment(@RequestBody BlogComments comment) {
        log.info(BlogConstants.LOG_COMMENT_UPDATE, comment.getId());
        return blogCommentsService.updateComment(comment);
    }

    /**
     * 根据ID查询评论信息
     *
     * @param id 评论ID
     * @return 评论信息
     */
    @GetMapping("/comment/{id}")
    public Result getCommentById(@PathVariable("id") Long id) {
        log.info(BlogConstants.LOG_COMMENT_GET, id);
        return blogCommentsService.getCommentById(id);
    }

    /**
     * 分页查询评论信息
     * 支持按博客ID、用户ID筛选
     *
     * @param current 当前页码，默认1
     * @param size    每页大小，默认10，最大100
     * @param blogId  博客ID（可选，按博客筛选）
     * @param userId  用户ID（可选，按用户筛选）
     * @return 分页结果，包含数据列表和总记录数
     */
    @GetMapping("/comment/page")
    public Result queryCommentPage(
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "blogId", required = false) Long blogId,
            @RequestParam(value = "userId", required = false) Long userId) {
        log.info(BlogConstants.LOG_COMMENT_PAGE_QUERY, current, size, blogId);
        return blogCommentsService.queryCommentPage(current, size, blogId, userId);
    }

    /**
     * 根据博客ID查询评论列表
     *
     * @param blogId 博客ID
     * @return 评论列表
     */
    @GetMapping("/comment/blog/{blogId}")
    public Result queryCommentsByBlogId(@PathVariable("blogId") Long blogId) {
        log.info("根据博客ID查询评论列表，博客ID：{}", blogId);
        return blogCommentsService.queryCommentsByBlogId(blogId);
    }

    /**
     * 根据ID删除评论
     *
     * @param id 评论ID
     * @return 操作结果
     */
    @DeleteMapping("/comment/{id}")
    public Result deleteCommentById(@PathVariable("id") Long id) {
        log.info(BlogConstants.LOG_COMMENT_DELETE, id);
        return blogCommentsService.deleteCommentById(id);
    }

    /**
     * 批量删除评论
     *
     * @param ids 评论ID列表
     * @return 操作结果
     */
    @DeleteMapping("/comment/batch")
    public Result deleteCommentByIds(@RequestBody List<Long> ids) {
        log.info(BlogConstants.LOG_COMMENT_BATCH_DELETE, ids.size());
        return blogCommentsService.deleteCommentByIds(ids);
    }
}
