package com.lilong.blogclient.service.service.impl;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lilong.blog.constants.RedisConstants;
import com.lilong.blog.enums.ResourceStatusEnum;
import com.lilong.blog.exception.ServiceException;
import com.lilong.blog.helper.helper.CurrentUserHelper;
import com.lilong.blog.remote.act.QueryPageRequest;
import com.lilong.blog.utils.PageUtil;
import com.lilong.blog.utils.RedisUtil;
import com.lilong.blog.vo.resource.SysResourceVo;
import com.lilong.blogclient.service.service.ResourceService;
import com.lilong.blogrpc.act.ResourceServiceRpc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author: lilong
 * @date: 2025/3/12
 * @description:
 */
@Service
public class ResourceServiceImpl implements ResourceService {
    @Autowired
    private ResourceServiceRpc resourceServiceRpc;
    
    @Autowired
    private  RedisUtil redisUtil;

    @Override
    public Page<SysResourceVo> getResourceList(SysResourceVo sysResource) {

        return resourceServiceRpc.getResourceList( QueryPageRequest.<SysResourceVo>builder()
                .userId(CurrentUserHelper.getUserId())
                .dto(sysResource)
                .page(PageUtil.getPage()).build()).getData();
    }

    @Override
    public void add(SysResourceVo sysResource) {
        sysResource.setUserId(CurrentUserHelper.getUserId());
        sysResource.setStatus(ResourceStatusEnum.AUDIT.getCode());
        resourceServiceRpc.save(sysResource);
    }

    @Override
    public SysResourceVo verify(String code, Long id) {
        String key = RedisConstants.CAPTCHA_CODE_KEY + code;
        if (!redisUtil.hasKey(key)) {
            throw new ServiceException("验证码错误");
        }
        redisUtil.delete(key);
        SysResourceVo sysResource = resourceServiceRpc.selectById(id).getData();
        sysResource.setDownloads(sysResource.getDownloads() + 1);
        resourceServiceRpc.updateById(sysResource);
        return sysResource;
    }
}
