package com.example.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import validation.Valida;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "邮箱登录实体")
public class LoginEmail implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "电话号码", required = true)
    @NotBlank(message = "电话号码不能为空", groups = {Valida.Create.class})
    private String phone;

    @ApiModelProperty(value = "验证码", required = true)
    @NotBlank(message = "验证码不能为空", groups = {Valida.Create.class})
    private String code;
}
