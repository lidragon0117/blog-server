package com.lilong.blog.respone.ollama;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName Model
 * @Description
 * @Author wangjinfei
 * @Date 2025/2/14 16:38
 */
@Data
public class Model {
    @ApiModelProperty(value = "模型名称")
    private String name;

    @ApiModelProperty(value = "最后修改时间")
    private String modifiedAt;

    @ApiModelProperty(value = "模型大小")
    private Long size;

    @ApiModelProperty(value = "模型的哈希摘要")
    private String digest;

    @ApiModelProperty(value = "模型的详细信息")
    private ModelDetails details;
}
