package com.lilong.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lilong.system.entity.SysWebConfig;

public interface SysWebConfigService extends IService<SysWebConfig> {

    void update(SysWebConfig sysWebConfig);
}
