package com.lilong.blogclient.service.service.impl;


import cn.hutool.crypto.digest.BCrypt;
import com.lilong.blog.exception.ServiceException;
import com.lilong.blog.vo.album.SysAlbumVo;
import com.lilong.blog.vo.photo.SysPhotoVo;
import com.lilong.blogclient.service.service.AlbumService;
import com.lilong.blogrpc.act.AlbumServiceRpc;
import com.lilong.blogrpc.act.PhotosServiceRpc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author: lilong
 * @date: 2025/2/7
 * @description:
 */
@Service
public class AlbumServiceImpl implements AlbumService {
    @Autowired
    private AlbumServiceRpc albumServiceRpc;

    @Autowired
    private PhotosServiceRpc photosServiceRpc;

    @Override
    public List<SysAlbumVo> getAlbumList() {
        return albumServiceRpc.getAlbumList(new SysAlbumVo()).getData();
    }

    @Override
    public List<SysPhotoVo> getPhotos(Long albumId) {
        return photosServiceRpc.getPhotos(albumId).getData();
    }

    @Override
    public Boolean verify(Long id, String password) {
        SysAlbumVo sysAlbumVo = albumServiceRpc.selectById(id).getData();
        if (sysAlbumVo == null) {
            throw new ServiceException("相册不存在!");
        }
        return BCrypt.checkpw(password, sysAlbumVo.getPassword());
    }

    @Override
    public SysAlbumVo detail(Long id) {
        SysAlbumVo sysAlbumVo = albumServiceRpc.selectById(id).getData();
        if (sysAlbumVo == null) {
            throw new ServiceException("相册不存在!");
        }
        sysAlbumVo.setPassword(null);
        return sysAlbumVo;
    }
}
