package com.lilong.blogclient.controller.friend;

import com.lilong.blog.base.Result;
import com.lilong.blog.vo.friend.SysFriendVo;
import com.lilong.blogclient.service.service.FriendService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/client/friend")
@RequiredArgsConstructor
@Api(tags = "门户-友情链接管理")
public class FriendController {

    private final FriendService friendService;

    @GetMapping("/list")
    @ApiOperation(value = "友情链接列表")
    public Result<List<SysFriendVo>> getFriendList() {
        return Result.success(friendService.getFriendList());
    }

    @PostMapping("/apply")
    @ApiOperation(value = "申请友链")
    public Result<Boolean> apply(@RequestBody SysFriendVo sysFriend) {
        return Result.success(friendService.apply(sysFriend));
    }
}
