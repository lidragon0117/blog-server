package com.lilong.blogclient.service.service;



import com.lilong.blog.vo.album.SysAlbumVo;
import com.lilong.blog.vo.photo.SysPhotoVo;

import java.util.List;

/**
 * @author: lilong
 * @date: 2025/2/7
 * @description:
 */
public interface AlbumService {

    /**
     * 获取相册列表
     * @return
     */
    List<SysAlbumVo> getAlbumList();

    /**
     * 获取照片列表
     * @param albumId
     * @return
     */
    List<SysPhotoVo> getPhotos(Long albumId);

    /**
     * 验证相册密码
     * @param id
     * @param password
     * @return
     */
    Boolean verify(Long id, String password);

    /**
     * 获取相册详情
     * @param id
     * @return
     */
    SysAlbumVo detail(Long id);
}
