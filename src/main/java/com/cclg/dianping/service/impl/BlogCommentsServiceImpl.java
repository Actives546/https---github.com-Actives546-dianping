package com.cclg.dianping.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cclg.dianping.constant.BlogConstants;
import com.cclg.dianping.domain.Blog;
import com.cclg.dianping.domain.BlogComments;
import com.cclg.dianping.dto.Result;
import com.cclg.dianping.mapper.BlogCommentsMapper;
import com.cclg.dianping.service.IBlogCommentsService;
import com.cclg.dianping.service.IBlogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 博客评论服务实现类
 * 实现博客评论相关的业务逻辑
 *
 * @author system
 */
@Slf4j
@Service
public class BlogCommentsServiceImpl extends ServiceImpl<BlogCommentsMapper, BlogComments> implements IBlogCommentsService {

    @Resource
    private IBlogService blogService;

    /**
     * 新增评论
     * 支持一级评论和回复评论
     * 业务逻辑：
     * 1. 校验评论信息不能为空
     * 2. 校验评论内容不能为空
     * 3. 校验博客ID不能为空
     * 4. 校验博客是否存在
     * 5. 校验父评论是否存在（如果有parentId）
     * 6. 校验回复的评论是否存在（如果有answerId）
     * 7. 设置默认值（点赞数、状态）
     * 8. 设置创建时间和更新时间
     * 9. 保存评论信息
     * 10. 更新博客的评论数量
     *
     * @param comment 评论信息
     * @return 操作结果，成功返回评论ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result saveComment(BlogComments comment) {
        if (comment == null) {
            return Result.fail(BlogConstants.COMMENT_INFO_NOT_NULL);
        }

        if (StrUtil.isBlank(comment.getContent())) {
            return Result.fail(BlogConstants.COMMENT_CONTENT_NOT_NULL);
        }

        if (comment.getBlogId() == null) {
            return Result.fail(BlogConstants.COMMENT_BLOG_ID_NOT_NULL);
        }

        Blog blog = blogService.getById(comment.getBlogId());
        if (blog == null) {
            return Result.fail(BlogConstants.BLOG_NOT_EXIST);
        }

        if (comment.getParentId() != null && comment.getParentId() > 0) {
            BlogComments parentComment = getById(comment.getParentId());
            if (parentComment == null) {
                return Result.fail(BlogConstants.COMMENT_PARENT_NOT_EXIST);
            }
        } else {
            comment.setParentId(0L);
        }

        if (comment.getAnswerId() != null && comment.getAnswerId() > 0) {
            BlogComments answerComment = getById(comment.getAnswerId());
            if (answerComment == null) {
                return Result.fail(BlogConstants.COMMENT_ANSWER_NOT_EXIST);
            }
        }

        if (comment.getLiked() == null) {
            comment.setLiked(0);
        }
        if (comment.getStatus() == null) {
            comment.setStatus(false);
        }

        LocalDateTime now = LocalDateTime.now();
        comment.setCreateTime(now);
        comment.setUpdateTime(now);

        boolean success = save(comment);
        if (success) {
            log.info(BlogConstants.COMMENT_CREATE_SUCCESS, comment.getId());

            blog.setComments(blog.getComments() + 1);
            blog.setUpdateTime(LocalDateTime.now());
            blogService.updateById(blog);

            return Result.ok(comment.getId());
        }

        return Result.fail(BlogConstants.COMMENT_CREATE_FAIL);
    }

    /**
     * 更新评论信息
     * 业务逻辑：
     * 1. 校验评论ID不能为空
     * 2. 校验评论是否存在
     * 3. 校验评论内容不能是空串（如果传入了内容）
     * 4. 设置更新时间
     * 5. 更新评论信息
     *
     * @param comment 评论信息
     * @return 操作结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result updateComment(BlogComments comment) {
        if (comment == null || comment.getId() == null) {
            return Result.fail(BlogConstants.COMMENT_ID_NOT_NULL);
        }

        BlogComments existComment = getById(comment.getId());
        if (existComment == null) {
            return Result.fail(BlogConstants.COMMENT_NOT_EXIST);
        }

        if (comment.getContent() != null && StrUtil.isBlank(comment.getContent())) {
            return Result.fail(BlogConstants.COMMENT_CONTENT_NOT_NULL);
        }

        comment.setUpdateTime(LocalDateTime.now());

        boolean success = updateById(comment);
        if (success) {
            log.info(BlogConstants.COMMENT_UPDATE_SUCCESS, comment.getId());
            return Result.ok();
        }

        return Result.fail(BlogConstants.COMMENT_UPDATE_FAIL);
    }

    /**
     * 根据ID查询评论信息
     * 业务逻辑：
     * 1. 校验评论ID不能为空
     * 2. 查询评论信息
     * 3. 返回评论信息
     *
     * @param id 评论ID
     * @return 评论信息
     */
    @Override
    public Result getCommentById(Long id) {
        if (id == null) {
            return Result.fail(BlogConstants.COMMENT_ID_NOT_NULL);
        }

        BlogComments comment = getById(id);
        if (comment == null) {
            return Result.fail(BlogConstants.COMMENT_NOT_EXIST);
        }

        return Result.ok(comment);
    }

    /**
     * 分页查询评论信息
     * 支持按博客ID、用户ID筛选
     * 业务逻辑：
     * 1. 处理默认分页参数
     * 2. 限制最大分页数
     * 3. 构建查询条件（支持博客ID、用户ID筛选）
     * 4. 执行分页查询
     *
     * @param current 当前页码
     * @param size    每页大小
     * @param blogId  博客ID（可选，按博客筛选）
     * @param userId  用户ID（可选，按用户筛选）
     * @return 分页结果，包含数据列表和总记录数
     */
    @Override
    public Result queryCommentPage(Integer current, Integer size, Long blogId, Long userId) {
        if (current == null || current <= 0) {
            current = BlogConstants.DEFAULT_PAGE_CURRENT;
        }

        if (size == null || size <= 0) {
            size = BlogConstants.DEFAULT_PAGE_SIZE;
        } else if (size > BlogConstants.MAX_PAGE_SIZE) {
            size = BlogConstants.MAX_PAGE_SIZE;
        }

        Page<BlogComments> page = new Page<>(current, size);
        LambdaQueryWrapper<BlogComments> queryWrapper = new LambdaQueryWrapper<>();

        if (blogId != null) {
            queryWrapper.eq(BlogComments::getBlogId, blogId);
        }

        if (userId != null) {
            queryWrapper.eq(BlogComments::getUserId, userId);
        }

        queryWrapper.orderByDesc(BlogComments::getUpdateTime);

        page(page, queryWrapper);

        return Result.ok(page.getRecords(), page.getTotal());
    }

    /**
     * 根据博客ID查询评论列表
     * 业务逻辑：
     * 1. 校验博客ID不能为空
     * 2. 查询该博客下的所有评论
     * 3. 按创建时间降序排列
     *
     * @param blogId 博客ID
     * @return 评论列表
     */
    @Override
    public Result queryCommentsByBlogId(Long blogId) {
        if (blogId == null) {
            return Result.fail(BlogConstants.COMMENT_BLOG_ID_NOT_NULL);
        }

        Blog blog = blogService.getById(blogId);
        if (blog == null) {
            return Result.fail(BlogConstants.BLOG_NOT_EXIST);
        }

        LambdaQueryWrapper<BlogComments> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BlogComments::getBlogId, blogId);
        queryWrapper.orderByDesc(BlogComments::getCreateTime);

        List<BlogComments> comments = list(queryWrapper);

        return Result.ok(comments);
    }

    /**
     * 根据ID删除评论
     * 业务逻辑：
     * 1. 校验评论ID不能为空
     * 2. 查询评论是否存在
     * 3. 执行删除操作
     * 4. 更新博客的评论数量
     *
     * @param id 评论ID
     * @return 操作结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result deleteCommentById(Long id) {
        if (id == null) {
            return Result.fail(BlogConstants.COMMENT_ID_NOT_NULL);
        }

        BlogComments comment = getById(id);
        if (comment == null) {
            return Result.fail(BlogConstants.COMMENT_NOT_EXIST);
        }

        boolean success = removeById(id);
        if (success) {
            log.info(BlogConstants.COMMENT_DELETE_SUCCESS, id);

            Blog blog = blogService.getById(comment.getBlogId());
            if (blog != null && blog.getComments() > 0) {
                blog.setComments(blog.getComments() - 1);
                blog.setUpdateTime(LocalDateTime.now());
                blogService.updateById(blog);
            }

            return Result.ok();
        }

        return Result.fail(BlogConstants.COMMENT_DELETE_FAIL);
    }

    /**
     * 批量删除评论
     * 业务逻辑：
     * 1. 校验评论ID列表不能为空
     * 2. 对ID列表进行去重，避免重复操作
     * 3. 批量查询所有评论，校验是否存在
     * 4. 执行批量删除操作
     * 5. 更新相关博客的评论数量
     *
     * @param ids 评论ID列表
     * @return 操作结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result deleteCommentByIds(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Result.fail(BlogConstants.COMMENT_ID_LIST_NOT_NULL);
        }

        Set<Long> distinctIds = ids.stream()
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        if (distinctIds.isEmpty()) {
            return Result.fail(BlogConstants.COMMENT_ID_LIST_NOT_NULL);
        }

        List<BlogComments> existComments = listByIds(distinctIds);
        Set<Long> existIds = existComments.stream()
                .map(BlogComments::getId)
                .collect(Collectors.toSet());

        Optional<Long> nonExistId = distinctIds.stream()
                .filter(id -> !existIds.contains(id))
                .findFirst();
        if (nonExistId.isPresent()) {
            return Result.fail(BlogConstants.COMMENT_NOT_EXIST + "，评论ID：" + nonExistId.get());
        }

        Map<Long, Long> blogCommentCount = existComments.stream()
                .collect(Collectors.groupingBy(
                        BlogComments::getBlogId,
                        Collectors.counting()
                ));

        boolean success = removeByIds(distinctIds);
        if (success) {
            log.info(BlogConstants.COMMENT_BATCH_DELETE_SUCCESS, distinctIds.size());

            for (Map.Entry<Long, Long> entry : blogCommentCount.entrySet()) {
                Long blogId = entry.getKey();
                Long count = entry.getValue();
                Blog blog = blogService.getById(blogId);
                if (blog != null) {
                    int newComments = Math.max(0, blog.getComments() - count.intValue());
                    blog.setComments(newComments);
                    blog.setUpdateTime(LocalDateTime.now());
                    blogService.updateById(blog);
                }
            }

            return Result.ok();
        }

        return Result.fail(BlogConstants.COMMENT_BATCH_DELETE_FAIL);
    }
}
