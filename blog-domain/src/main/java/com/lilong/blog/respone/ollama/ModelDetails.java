package com.lilong.blog.respone.ollama;

import lombok.Data;

/**
 * @ClassName ModelDetails
 * @Description
 * @Author wangjinfei
 * @Date 2025/2/14 16:41
 */
@Data
public class ModelDetails {
//    @ApiModelProperty(value = "模型格式")
    private String format;

//    @ApiModelProperty(value = "模型家族")
    private String family;

//    @ApiModelProperty(value = "模型家族列表")
    private String families;

//    @ApiModelProperty(value = "模型的参数大小")
    private String parameterSize;

//    @ApiModelProperty(value = "量化级别")
    private String quantizationLevel;
}
