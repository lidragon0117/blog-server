package com.lilong.blog.respone.agent;

import com.alibaba.nacos.shaded.com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 * @Author lilong
 * @webSite com.lilong
 * @Date 2024/3/21 01:34
 * @description
 */
@Data
public class Model {

    private String id;
    private String object;

    @SerializedName("owner_by")
    private String ownedBy;
    private String root;
    private String parent;

    public Model(String id, String object, String ownedBy, String root, String parent) {
        this.id = id;
        this.object = object;
        this.ownedBy = ownedBy;
        this.root = root;
        this.parent = parent;
    }
}
