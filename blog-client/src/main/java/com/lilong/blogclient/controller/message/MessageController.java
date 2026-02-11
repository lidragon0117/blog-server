package com.lilong.blogclient.controller.message;

import com.lilong.blog.annotation.AccessLimit;
import com.lilong.blog.base.Result;
import com.lilong.blog.vo.message.SysMessageVo;
import com.lilong.blogclient.service.service.MessageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/client/message")
@RequiredArgsConstructor
@Api(tags = "门户-留言管理")
public class MessageController {

    private final MessageService messageService;

    @AccessLimit
    @GetMapping("/list")
    @ApiOperation(value = "留言列表")
    public Result<List<SysMessageVo>> getMessageList() {
        return Result.success(messageService.getMessageList());
    }

    @PostMapping("/add")
    @ApiOperation(value = "发表留言")
    public Result<Boolean> add(@RequestBody SysMessageVo sysMessage) {
        return Result.success(messageService.add(sysMessage));
    }
}
