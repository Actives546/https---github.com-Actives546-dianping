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
import com.cclg.dianping.mapper.BlogMapper;
import com.cclg.dianping.service.IBlogCommentsService;
import com.cclg.dianping.service.IBlogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BlogCommentsServiceImpl extends ServiceImpl<BlogCommentsMapper, BlogComments> implements IBlogCommentsService {

    @Resource
    private IBlogService blogService;

    @Resource
    private BlogMapper blogMapper;

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
            if (!comment.getBlogId().equals(parentComment.getBlogId())) {
                return Result.fail("父评论不属于当前博客");
            }
        } else {
            comment.setParentId(0L);
        }

        if (comment.getAnswerId() != null && comment.getAnswerId() > 0) {
            BlogComments answerComment = getById(comment.getAnswerId());
            if (answerComment == null) {
                return Result.fail(BlogConstants.COMMENT_ANSWER_NOT_EXIST);
            }
            if (!comment.getBlogId().equals(answerComment.getBlogId())) {
                return Result.fail("回复的评论不属于当前博客");
            }
            if (!comment.getParentId().equals(answerComment.getParentId())) {
                return Result.fail("回复的评论不属于当前父评论");
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

            int updateCount = blogMapper.incrementComments(comment.getBlogId(), 1);
            if (updateCount <= 0) {
                log.warn("更新博客评论数失败，博客ID：{}", comment.getBlogId());
            }

            return Result.ok(comment.getId());
        }

        return Result.fail(BlogConstants.COMMENT_CREATE_FAIL);
    }

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

    @Override
    public Result queryCommentsByBlogId(Long blogId, Integer current, Integer size) {
        if (blogId == null) {
            return Result.fail(BlogConstants.COMMENT_BLOG_ID_NOT_NULL);
        }

        Blog blog = blogService.getById(blogId);
        if (blog == null) {
            return Result.fail(BlogConstants.BLOG_NOT_EXIST);
        }

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
        queryWrapper.eq(BlogComments::getBlogId, blogId);
        queryWrapper.orderByDesc(BlogComments::getCreateTime);

        page(page, queryWrapper);

        return Result.ok(page.getRecords(), page.getTotal());
    }

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

        Set<Long> allIdsToDelete = collectAllChildComments(id);
        allIdsToDelete.add(id);

        int deleteCount = allIdsToDelete.size();
        boolean success = removeByIds(allIdsToDelete);
        if (success) {
            log.info(BlogConstants.COMMENT_DELETE_SUCCESS, id);
            log.info("级联删除子评论成功，共删除 {} 条评论", deleteCount);

            int updateCount = blogMapper.decrementComments(comment.getBlogId(), deleteCount);
            if (updateCount <= 0) {
                log.warn("更新博客评论数失败，博客ID：{}，减少数量：{}", comment.getBlogId(), deleteCount);
            }

            return Result.ok();
        }

        return Result.fail(BlogConstants.COMMENT_DELETE_FAIL);
    }

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

        Set<Long> allIdsToDelete = new HashSet<>();
        Map<Long, Long> blogCommentCount = new java.util.HashMap<>();

        for (Long id : distinctIds) {
            BlogComments comment = getById(id);
            if (comment == null) {
                return Result.fail(BlogConstants.COMMENT_NOT_EXIST + "，评论ID：" + id);
            }

            Set<Long> childIds = collectAllChildComments(id);
            allIdsToDelete.add(id);
            allIdsToDelete.addAll(childIds);

            int totalCount = 1 + childIds.size();
            blogCommentCount.merge(comment.getBlogId(), (long) totalCount, Long::sum);
        }

        boolean success = removeByIds(allIdsToDelete);
        if (success) {
            log.info(BlogConstants.COMMENT_BATCH_DELETE_SUCCESS, allIdsToDelete.size());

            for (Map.Entry<Long, Long> entry : blogCommentCount.entrySet()) {
                Long blogId = entry.getKey();
                Long count = entry.getValue();

                int updateCount = blogMapper.decrementComments(blogId, count.intValue());
                if (updateCount <= 0) {
                    log.warn("更新博客评论数失败，博客ID：{}，减少数量：{}", blogId, count);
                }
            }

            return Result.ok();
        }

        return Result.fail(BlogConstants.COMMENT_BATCH_DELETE_FAIL);
    }

    private Set<Long> collectAllChildComments(Long parentId) {
        Set<Long> allChildIds = new HashSet<>();
        Queue<Long> queue = new LinkedList<>();
        queue.offer(parentId);

        while (!queue.isEmpty()) {
            Long currentParentId = queue.poll();

            LambdaQueryWrapper<BlogComments> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(BlogComments::getParentId, currentParentId);
            queryWrapper.select(BlogComments::getId);

            List<BlogComments> childComments = list(queryWrapper);
            for (BlogComments child : childComments) {
                if (allChildIds.add(child.getId())) {
                    queue.offer(child.getId());
                }
            }
        }

        return allChildIds;
    }
}
