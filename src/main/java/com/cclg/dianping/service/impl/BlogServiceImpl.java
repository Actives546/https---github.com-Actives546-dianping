package com.cclg.dianping.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cclg.dianping.constant.BlogConstants;
import com.cclg.dianping.domain.Blog;
import com.cclg.dianping.domain.User;
import com.cclg.dianping.dto.Result;
import com.cclg.dianping.mapper.BlogMapper;
import com.cclg.dianping.service.IBlogService;
import com.cclg.dianping.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 博客服务实现类
 * 实现博客相关的业务逻辑
 *
 * @author system
 */
@Slf4j
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements IBlogService {

    @Resource
    private IUserService userService;

    /**
     * 新增博客
     * 业务逻辑：
     * 1. 校验博客信息不能为空
     * 2. 校验博客标题不能为空
     * 3. 校验博客内容不能为空
     * 4. 校验用户ID是否存在
     * 5. 校验图片数量不超过9张
     * 6. 设置默认值（点赞数、评论数）
     * 7. 设置创建时间和更新时间
     * 8. 保存博客信息
     *
     * @param blog 博客信息
     * @return 操作结果，成功返回博客ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result saveBlog(Blog blog) {
        if (blog == null) {
            return Result.fail(BlogConstants.BLOG_INFO_NOT_NULL);
        }

        if (StrUtil.isBlank(blog.getTitle())) {
            return Result.fail(BlogConstants.BLOG_TITLE_NOT_NULL);
        }

        if (StrUtil.isBlank(blog.getContent())) {
            return Result.fail(BlogConstants.BLOG_CONTENT_NOT_NULL);
        }

        if (blog.getUserId() == null) {
            return Result.fail("用户ID不能为空");
        }

        User user = userService.getById(blog.getUserId());
        if (user == null) {
            return Result.fail("用户不存在");
        }

        if (StrUtil.isNotBlank(blog.getImages())) {
            String[] images = blog.getImages().split(BlogConstants.IMAGES_SEPARATOR);
            if (images.length > BlogConstants.MAX_IMAGES_COUNT) {
                return Result.fail("图片数量不能超过" + BlogConstants.MAX_IMAGES_COUNT + "张");
            }
        }

        if (blog.getLiked() == null) {
            blog.setLiked(0);
        }
        if (blog.getComments() == null) {
            blog.setComments(0);
        }

        LocalDateTime now = LocalDateTime.now();
        blog.setCreateTime(now);
        blog.setUpdateTime(now);

        boolean success = save(blog);
        if (success) {
            log.info(BlogConstants.BLOG_CREATE_SUCCESS, blog.getId());
            return Result.ok(blog.getId());
        }

        return Result.fail(BlogConstants.BLOG_CREATE_FAIL);
    }

    /**
     * 更新博客信息
     * 业务逻辑：
     * 1. 校验博客ID不能为空
     * 2. 校验博客是否存在
     * 3. 校验博客标题不能是空串（如果传入了标题）
     * 4. 校验博客内容不能是空串（如果传入了内容）
     * 5. 校验图片数量不超过9张
     * 6. 设置更新时间
     * 7. 更新博客信息
     *
     * @param blog 博客信息
     * @return 操作结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result updateBlog(Blog blog) {
        if (blog == null || blog.getId() == null) {
            return Result.fail(BlogConstants.BLOG_ID_NOT_NULL);
        }

        Blog existBlog = getById(blog.getId());
        if (existBlog == null) {
            return Result.fail(BlogConstants.BLOG_NOT_EXIST);
        }

        if (blog.getTitle() != null && StrUtil.isBlank(blog.getTitle())) {
            return Result.fail(BlogConstants.BLOG_TITLE_NOT_NULL);
        }

        if (blog.getContent() != null && StrUtil.isBlank(blog.getContent())) {
            return Result.fail(BlogConstants.BLOG_CONTENT_NOT_NULL);
        }

        if (StrUtil.isNotBlank(blog.getImages())) {
            String[] images = blog.getImages().split(BlogConstants.IMAGES_SEPARATOR);
            if (images.length > BlogConstants.MAX_IMAGES_COUNT) {
                return Result.fail("图片数量不能超过" + BlogConstants.MAX_IMAGES_COUNT + "张");
            }
        }

        blog.setUpdateTime(LocalDateTime.now());

        boolean success = updateById(blog);
        if (success) {
            log.info(BlogConstants.BLOG_UPDATE_SUCCESS, blog.getId());
            return Result.ok();
        }

        return Result.fail(BlogConstants.BLOG_UPDATE_FAIL);
    }

    /**
     * 根据ID查询博客信息
     * 业务逻辑：
     * 1. 校验博客ID不能为空
     * 2. 查询博客信息
     * 3. 关联查询用户信息（头像、昵称）
     * 4. 返回博客信息
     *
     * @param id 博客ID
     * @return 博客信息
     */
    @Override
    public Result getBlogById(Long id) {
        if (id == null) {
            return Result.fail(BlogConstants.BLOG_ID_NOT_NULL);
        }

        Blog blog = getById(id);
        if (blog == null) {
            return Result.fail(BlogConstants.BLOG_NOT_EXIST);
        }

        setBlogUserInfo(blog);

        return Result.ok(blog);
    }

    /**
     * 分页查询博客信息
     * 支持按博客标题、用户ID、商铺ID筛选
     * 业务逻辑：
     * 1. 处理默认分页参数
     * 2. 限制最大分页数
     * 3. 构建查询条件（支持标题模糊搜索、用户ID、商铺ID筛选）
     * 4. 执行分页查询
     * 5. 批量关联查询用户信息
     *
     * @param current 当前页码
     * @param size    每页大小
     * @param title   博客标题（可选，模糊匹配）
     * @param userId  用户ID（可选，按用户筛选）
     * @param shopId  商铺ID（可选，按商铺筛选）
     * @return 分页结果，包含数据列表和总记录数
     */
    @Override
    public Result queryBlogPage(Integer current, Integer size, String title, Long userId, Long shopId) {
        if (current == null || current <= 0) {
            current = BlogConstants.DEFAULT_PAGE_CURRENT;
        }

        if (size == null || size <= 0) {
            size = BlogConstants.DEFAULT_PAGE_SIZE;
        } else if (size > BlogConstants.MAX_PAGE_SIZE) {
            size = BlogConstants.MAX_PAGE_SIZE;
        }

        Page<Blog> page = new Page<>(current, size);
        LambdaQueryWrapper<Blog> queryWrapper = new LambdaQueryWrapper<>();

        if (StrUtil.isNotBlank(title)) {
            queryWrapper.like(Blog::getTitle, title);
        }

        if (userId != null) {
            queryWrapper.eq(Blog::getUserId, userId);
        }

        if (shopId != null) {
            queryWrapper.eq(Blog::getShopId, shopId);
        }

        queryWrapper.orderByDesc(Blog::getUpdateTime);

        page(page, queryWrapper);

        setBlogUserInfoBatch(page.getRecords());

        return Result.ok(page.getRecords(), page.getTotal());
    }

    /**
     * 根据ID删除博客
     * 业务逻辑：
     * 1. 校验博客ID不能为空
     * 2. 查询博客是否存在
     * 3. 执行删除操作
     *
     * @param id 博客ID
     * @return 操作结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result deleteBlogById(Long id) {
        if (id == null) {
            return Result.fail(BlogConstants.BLOG_ID_NOT_NULL);
        }

        Blog blog = getById(id);
        if (blog == null) {
            return Result.fail(BlogConstants.BLOG_NOT_EXIST);
        }

        boolean success = removeById(id);
        if (success) {
            log.info(BlogConstants.BLOG_DELETE_SUCCESS, id);
            return Result.ok();
        }

        return Result.fail(BlogConstants.BLOG_DELETE_FAIL);
    }

    /**
     * 批量删除博客
     * 业务逻辑：
     * 1. 校验博客ID列表不能为空
     * 2. 对ID列表进行去重，避免重复操作
     * 3. 批量查询所有博客，校验是否存在
     * 4. 执行批量删除操作
     *
     * @param ids 博客ID列表
     * @return 操作结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result deleteBlogByIds(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Result.fail(BlogConstants.BLOG_ID_LIST_NOT_NULL);
        }

        Set<Long> distinctIds = ids.stream()
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        if (distinctIds.isEmpty()) {
            return Result.fail(BlogConstants.BLOG_ID_LIST_NOT_NULL);
        }

        List<Blog> existBlogs = listByIds(distinctIds);
        Set<Long> existIds = existBlogs.stream()
                .map(Blog::getId)
                .collect(Collectors.toSet());

        Optional<Long> nonExistId = distinctIds.stream()
                .filter(id -> !existIds.contains(id))
                .findFirst();
        if (nonExistId.isPresent()) {
            return Result.fail(BlogConstants.BLOG_NOT_EXIST + "，博客ID：" + nonExistId.get());
        }

        boolean success = removeByIds(distinctIds);
        if (success) {
            log.info(BlogConstants.BLOG_BATCH_DELETE_SUCCESS, distinctIds.size());
            return Result.ok();
        }

        return Result.fail(BlogConstants.BLOG_BATCH_DELETE_FAIL);
    }

    /**
     * 为单个博客设置关联的用户信息
     * 适用于单条查询的场景
     *
     * @param blog 博客对象
     */
    private void setBlogUserInfo(Blog blog) {
        if (blog.getUserId() != null) {
            User user = userService.getById(blog.getUserId());
            if (user != null) {
                blog.setIcon(user.getIcon());
                blog.setName(user.getNickName());
            }
        }
    }

    /**
     * 批量为博客列表设置关联的用户信息
     * 使用批量查询替代循环单条查询，减少数据库访问次数
     *
     * @param blogs 博客列表
     */
    private void setBlogUserInfoBatch(List<Blog> blogs) {
        if (CollUtil.isEmpty(blogs)) {
            return;
        }

        Set<Long> userIds = blogs.stream()
                .map(Blog::getUserId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        if (userIds.isEmpty()) {
            return;
        }

        List<User> users = userService.listByIds(userIds);
        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        for (Blog blog : blogs) {
            if (blog.getUserId() != null) {
                User user = userMap.get(blog.getUserId());
                if (user != null) {
                    blog.setIcon(user.getIcon());
                    blog.setName(user.getNickName());
                }
            }
        }
    }
}
