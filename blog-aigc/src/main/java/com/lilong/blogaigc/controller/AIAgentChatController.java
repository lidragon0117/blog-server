package com.lilong.blogaigc.controller;

import com.lilong.blog.base.Result;
import com.lilong.blog.constants.agent.AgentConstants;
import com.lilong.blog.exception.ServiceException;
import com.lilong.blog.helper.CurrentUserHelper;
import com.lilong.blog.helper.SseEmitterHelper;
import com.lilong.blog.utils.ThreadPoolExecutorUtil;
import com.lilong.blog.vo.agent.AiAgentChatVo;
import com.lilong.blogaigc.llm.AbstractAgentChatService;
import com.lilong.blogaigc.llm.AgentChatFactory;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * @author : lilong
 * @date : 2026-02-11 19:30
 * @description :
 */
@RestController
@RequestMapping("/aigc/chat")
public class AIAgentChatController {

    /**
     * 获取SSE长连接
     *
     * @return SseEmitter 长连接
     */
    @GetMapping(path = "/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ApiOperation("获取SSE长连接")
    public SseEmitter stream() {
        String userId = CurrentUserHelper.getUserId().toString();
        if (StringUtils.isBlank(userId)) {
            throw new ServiceException("用户未登录");
        }
        return SseEmitterHelper.get(AgentConstants.AiBotConstants.AGENT_BOT_CODE, userId);
    }

    /**
     * AI智能助手
     *
     * @param aiAgentChatVo
     * @return
     */
    @PostMapping
    @ApiOperation("AI智能助手")
    public Result chat(@RequestBody AiAgentChatVo aiAgentChatVo) {
        // 设置当前用户id和业务编码
        aiAgentChatVo.setUserId(CurrentUserHelper.getUserId().toString());
        aiAgentChatVo.setBizCode(AgentConstants.AiBotConstants.AGENT_BOT_CODE);
        // 异步返回防止超时
        ThreadPoolExecutorUtil.execute(() ->
                AgentChatFactory.getService(AgentConstants.AiServiceConstants.AI_AGENT_SERVICE).chat(aiAgentChatVo)
        );
        return Result.success();
    }


}
