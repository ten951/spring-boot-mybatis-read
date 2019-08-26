package com.ten951.boot.mybatis.read.mapper;

import com.ten951.boot.mybatis.read.entity.RepeatConfig;
import com.ten951.boot.mybatis.read.entity.example.RepeatConfigCondition;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * b2c检测单配置中心
 *
 * @author eudora
 * 2018-10-08
 */
@Repository
public interface RepeatConfigMapper {
    /**
     * 根据指定的条件获取数据库记录数
     *
     * @param condition RepeatConfigCondition
     * @return long 记录数
     */
    long countByExample(RepeatConfigCondition condition);

    /**
     * 根据指定的条件删除数据库符合条件的记录
     *
     * @param condition RepeatConfigCondition
     * @return int 影响行数
     */
    int deleteByExample(RepeatConfigCondition condition);

    /**
     * 根据主键删除数据库的记录
     *
     * @param configId Long
     * @return int 影响行数
     */
    int deleteByPrimaryKey(Long configId);

    /**
     * 新写入数据库记录
     *
     * @param record RepeatConfig
     * @return int 影响行数
     */
    int insert(RepeatConfig record);

    /**
     * 动态字段,写入数据库记录
     *
     * @param record RepeatConfig
     * @return int 影响行数
     */
    int insertSelective(RepeatConfig record);

    /**
     * 根据指定的条件查询符合条件的数据库记录
     *
     * @param condition RepeatConfigCondition
     * @return List<RepeatConfig>
     */
    List<RepeatConfig> selectByExample(RepeatConfigCondition condition);

    /**
     * 根据指定主键获取一条数据库记录
     *
     * @param configId Long
     * @return RepeatConfig
     */
    RepeatConfig selectByPrimaryKey(Long configId);

    /**
     * 动态根据指定的条件来更新符合条件的数据库记录
     *
     * @param record    RepeatConfig
     * @param condition RepeatConfigCondition
     * @return int 影响行数
     */
    int updateByExampleSelective(@Param("record") RepeatConfig record, @Param("example") RepeatConfigCondition condition);

    /**
     * 根据指定的条件来更新符合条件的数据库记录
     *
     * @param record    RepeatConfig
     * @param condition RepeatConfigCondition
     * @return int 影响行数
     */
    int updateByExample(@Param("record") RepeatConfig record, @Param("example") RepeatConfigCondition condition);

    /**
     * 动态字段,根据主键来更新符合条件的数据库记录
     *
     * @param record RepeatConfig
     * @return int 影响行数
     */
    int updateByPrimaryKeySelective(RepeatConfig record);

    /**
     * 根据主键来更新符合条件的数据库记录
     *
     * @param record RepeatConfig
     * @return int 影响行数
     */
    int updateByPrimaryKey(RepeatConfig record);
}