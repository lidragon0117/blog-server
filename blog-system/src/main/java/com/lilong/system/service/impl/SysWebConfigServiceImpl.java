package com.lilong.system.service.impl;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lilong.blog.constants.RedisConstants;
import com.lilong.blog.utils.RedisUtil;
import com.lilong.system.entity.SysWebConfig;
import com.lilong.system.mapper.SysWebConfigMapper;
import com.lilong.system.service.SysWebConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SysWebConfigServiceImpl extends ServiceImpl<SysWebConfigMapper, SysWebConfig> implements SysWebConfigService {

    private final RedisUtil redisUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SysWebConfig sysWebConfig) {
        baseMapper.updateById(sysWebConfig);
        redisUtil.set(RedisConstants.WEB_CONFIG_KEY, JSONObject.toJSONString(sysWebConfig));
    }
}
