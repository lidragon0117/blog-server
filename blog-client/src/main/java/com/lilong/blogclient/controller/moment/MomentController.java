package com.lilong.blogclient.controller.moment;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lilong.blog.base.Result;
import com.lilong.blog.vo.moment.MomentPageVo;
import com.lilong.blogclient.service.service.MomentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: lilong
 * @date: 2025/2/5
 * @description:
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/client/moment")
@Api(tags = "门户-说说管理")
public class MomentController {

    private final MomentService momentService;

    @GetMapping("/list")
    @ApiOperation("说说列表")
    public Result<IPage<MomentPageVo>> getMomentList() {
        return Result.success(momentService.getMomentList());
    }

}
