package com.lilong.blog.respone.agent;

import java.util.List;

/**
 * @Author lilong
 * @webSite com.lilong
 * @Date 2024/3/21 01:34
 * @description
 */
public class ModelsList {

    private List<Model> data;

    public List<Model> getData() {
        return data;
    }

    public void setData(List<Model> data) {
        this.data = data;
    }

    public ModelsList(List<Model> data) {
        this.data = data;
    }
}
