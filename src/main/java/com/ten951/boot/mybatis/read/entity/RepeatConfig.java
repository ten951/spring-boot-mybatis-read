package com.ten951.boot.mybatis.read.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.Instant;

/**
 * b2c检测单配置中心 b2c_repeat_config
 *
 * @author eudora
 * 2018-10-08
 */
@Data
public class RepeatConfig implements Serializable {
    /**
     *
     */
    private Long configId;

    /**
     * 省
     */
    private String province;

    /**
     * 省码
     */
    private String provinceCode;

    /**
     * 城市名称
     */
    private String city;

    /**
     * 城市码
     */
    private String cityCode;

    /**
     * 检测单类型(code)
     */
    private Integer repeatType;

    /**
     * 创建时间
     */
    private Instant createDate;

    /**
     * 更新时间
     */
    private Instant updateDate;

    /**
     * 创建人ID
     */
    private String createUserId;

    /**
     * 创建人姓名
     */
    private String createUserName;

    /**
     * 更新人ID
     */
    private String updateUserId;

    /**
     * 更新人姓名
     */
    private String updateUserName;

    /**
     * 检测单类型(value)
     */
    private String repeatTypeName;
}