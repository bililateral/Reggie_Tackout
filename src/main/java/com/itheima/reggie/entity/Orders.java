package com.itheima.reggie.entity;

import java.io.Serial;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;

import java.time.LocalDateTime;
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
@Schema(description = "订单表实体") // 替换Swagger v2的@ApiModel
public class Orders implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键-订单号") // 替换@ApiModelProperty
    @TableId(value = "id", type = IdType.ASSIGN_ID) // ASSIGN_ID（适配MyBatis-Plus新版本）
    private Long id;

    @Schema(description = "订单号")
    private String number;

    @Schema(description = "订单状态 1待付款，2待派送，3已派送，4已完成，5已取消")
    private Integer status;

    @Schema(description = "下单用户")
    private Long userId;

    @Schema(description = "地址id")
    private Long addressBookId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "下单时间")
    private LocalDateTime orderTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "结账时间")
    private LocalDateTime checkoutTime;

    @Schema(description = "支付方式 1微信,2支付宝")
    private Integer payMethod;

    @Schema(description = "实收金额")
    private BigDecimal amount;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "联系电话")
    private String phone;

    @Schema(description = "详细地址")
    private String address;

    @Schema(description = "用户名")
    private String userName;

    @Schema(description = "收货人")
    private String consignee;
}