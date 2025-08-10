package com.itheima.reggie.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.io.Serial;
import java.io.Serializable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "用户信息实体") // 替换旧版Swagger注解@ApiModel
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键") // 替换@ApiModelProperty
    @TableId(value = "id", type = IdType.ASSIGN_ID) // 关键修改：ID_WORKER → ASSIGN_ID（适配MyBatis-Plus新版本）
    private Long id;

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "性别")
    private String sex;

    @Schema(description = "身份证号")
    private String idNumber;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "状态 0:禁用，1:正常")
    private Integer status;

}