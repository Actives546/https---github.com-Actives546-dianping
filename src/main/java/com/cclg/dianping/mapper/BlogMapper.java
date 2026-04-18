package com.cclg.dianping.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cclg.dianping.domain.Blog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface BlogMapper extends BaseMapper<Blog> {

    @Update("UPDATE tb_blog SET comments = comments + #{delta}, update_time = NOW() WHERE id = #{blogId} AND comments + #{delta} >= 0")
    int incrementComments(@Param("blogId") Long blogId, @Param("delta") Integer delta);

    @Update("UPDATE tb_blog SET comments = comments - #{delta}, update_time = NOW() WHERE id = #{blogId} AND comments - #{delta} >= 0")
    int decrementComments(@Param("blogId") Long blogId, @Param("delta") Integer delta);
}
