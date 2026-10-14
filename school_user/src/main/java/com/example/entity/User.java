package com.example.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import validation.Valida;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("sys_user")
@ApiModel(description = "用户实体")
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "用户ID")
    @NotNull(message = "用户ID不能为空", groups = Valida.Update.class)
    private Long id;

    @ApiModelProperty(value = "用户名")
    @NotNull(message = "名称不能为空", groups = Valida.Create.class)
    private String name;

    @ApiModelProperty(value = "电话号码")
    @NotNull(message = "电话号码不能为空", groups = Valida.Create.class)
    @Pattern(regexp = "^1(3\\d|4[5-9]|5[0-35-9]|6[567]|7[0-8]|8\\d|9[0-35-9])\\d{8}$",
            message = "手机号格式错误", groups = Valida.Create.class)
    private String phone;

    @ApiModelProperty(value = "密码")
    @NotBlank(message = "密码不能为空", groups = {Valida.Create.class})
    @Size(min = 6, max = 10, message = "密码长度需要6-10位", groups = {Valida.Create.class})
    private String password;

    @ApiModelProperty(value = "邮箱")
    private String email;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "状态")
    private String status;

    @ApiModelProperty(value = "提交时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submitTime;
}
