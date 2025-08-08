package com.itheima.reggie.entity;

import java.io.Serial;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;

import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "套餐菜品关系实体") // 替换旧版Swagger注解
public class SetmealDish implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.ASSIGN_ID) // 主键策略适配新版本
    private Long id;

    @Schema(description = "套餐id") //外键
    private Long setmealId;

    @Schema(description = "菜品id")
    private Long dishId;

    @Schema(description = "菜品名称（冗余字段）")
    private String name;

    @Schema(description = "菜品原价（冗余字段）")
    private BigDecimal price;

    @Schema(description = "份数")
    private Integer copies;

    @Schema(description = "排序")
    private Integer sort;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8") // 补充时分秒和时区
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8") // 补充时分秒和时区
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建人")
    private Long createUser;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "修改人")
    private Long updateUser;

    @Schema(description = "是否删除")
    private Integer isDeleted;

}