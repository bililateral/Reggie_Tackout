package com.itheima.reggie.entity;

import java.io.Serial;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;

import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "购物车实体") // 替换旧版Swagger注解
public class ShoppingCart implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.ASSIGN_ID) // 主键策略适配新版本
    private Long id;

    @Schema(description = "商品名称")
    private String name;

    @Schema(description = "商品图片")
    private String image;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "菜品ID")
    private Long dishId;

    @Schema(description = "套餐ID")
    private Long setmealId;

    @Schema(description = "菜品口味")
    private String dishFlavor;

    @Schema(description = "商品数量")
    private Integer number;

    @Schema(description = "商品金额")
    private BigDecimal amount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8") // 补充时间格式和时区
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8") // 补充时间格式和时区
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建人")
    private Long createUser;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "修改人")
    private Long updateUser;
}