package com.cclg.dianping.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cclg.dianping.domain.Follow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FollowMapper extends BaseMapper<Follow> {

    @Select("SELECT follow_user_id FROM tb_follow WHERE user_id = #{userId}")
    List<Long> selectFollowUserIds(@Param("userId") Long userId);

    @Select("SELECT user_id FROM tb_follow WHERE follow_user_id = #{followUserId}")
    List<Long> selectFansUserIds(@Param("followUserId") Long followUserId);

    @Select("SELECT f1.follow_user_id FROM tb_follow f1 " +
            "INNER JOIN tb_follow f2 ON f1.follow_user_id = f2.follow_user_id " +
            "WHERE f1.user_id = #{userId} AND f2.user_id = #{targetUserId}")
    List<Long> selectCommonFollowUserIds(@Param("userId") Long userId, @Param("targetUserId") Long targetUserId);
}
