package com.lilong.blogclient.service.service.impl;

import com.lilong.blog.vo.tag.TagListVo;
import com.lilong.blogclient.service.service.TagService;
import com.lilong.blogrpc.act.TagServiceRpc;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagServiceRpc tagServiceRpc;

    @Override
    public List<TagListVo> getTagsApi() {
        return tagServiceRpc.selectTagList().getData();
    }
}
