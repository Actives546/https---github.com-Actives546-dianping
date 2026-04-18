package com.cclg.dianping.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cclg.dianping.domain.Blog;
import com.cclg.dianping.dto.Result;

import java.util.List;

/**
 * 博客服务接口
 * 定义博客相关的业务操作方法
 *
 * @author system
 */
public interface IBlogService extends IService<Blog> {

    /**
     * 新增博客
     *
     * @param blog 博客信息
     * @return 操作结果
     */
    Result saveBlog(Blog blog);

    /**
     * 更新博客信息
     *
     * @param blog 博客信息
     * @return 操作结果
     */
    Result updateBlog(Blog blog);

    /**
     * 根据ID查询博客信息
     *
     * @param id 博客ID
     * @return 博客信息
     */
    Result getBlogById(Long id);

    /**
     * 分页查询博客信息
     *
     * @param current 当前页码
     * @param size    每页大小
     * @param title   博客标题（可选，模糊匹配）
     * @param userId  用户ID（可选，按用户筛选）
     * @param shopId  商铺ID（可选，按商铺筛选）
     * @return 分页结果
     */
    Result queryBlogPage(Integer current, Integer size, String title, Long userId, Long shopId);

    /**
     * 根据ID删除博客
     *
     * @param id 博客ID
     * @return 操作结果
     */
    Result deleteBlogById(Long id);

    /**
     * 批量删除博客
     * 注意：有一个删除失败则整体失败
     *
     * @param ids 博客ID列表
     * @return 操作结果
     */
    Result deleteBlogByIds(List<Long> ids);
}
