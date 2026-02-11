package com.lilong.file.controller;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lilong.blog.base.Constants;
import com.lilong.blog.base.Result;
import com.lilong.blog.exception.ServiceException;
import com.lilong.blog.utils.DateUtil;
import com.lilong.file.entity.FileDetail;
import com.lilong.file.entity.SysFileOss;
import com.lilong.file.service.FileDetailService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.FileStorageService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/file")
@Api(tags = "文件管理")
@RequiredArgsConstructor
public class FileController {

    private final FileDetailService fileDetailService;

    private final FileStorageService fileStorageService;


    @GetMapping("/list")
    @ApiOperation(value = "获取文件记录表列表")
    public Result<IPage<FileDetail>> list(FileDetail fileDetail) {
        return Result.success(fileDetailService.selectPage(fileDetail));
    }

    @GetMapping("/getOssConfig")
    @ApiOperation(value = "获取存储平台配置")
    public Result<List<SysFileOss>> getOssConfig() {
        return Result.success(fileDetailService.getOssConfig());
    }


    @PostMapping("/addOss")
    @ApiOperation(value = "添加存储平台配置")
    public Result<Void> addOss(@RequestBody SysFileOss sysFileOss) {
        fileDetailService.addOss(sysFileOss);
        if (sysFileOss.getIsEnable() == Constants.YES) {
            fileStorageService.getProperties().setDefaultPlatform(sysFileOss.getPlatform());
        }
        return Result.success();
    }


    @PutMapping("/updateOss")

    @ApiOperation(value = "修改存储平台配置")
    public Result<Void> updateOss(@RequestBody SysFileOss sysFileOss) {
        fileDetailService.updateOss(sysFileOss);
        if (sysFileOss.getIsEnable() == Constants.YES) {
            fileStorageService.getProperties().setDefaultPlatform(sysFileOss.getPlatform());
        }
        return Result.success();
    }


    @PostMapping("/upload")
    @ApiOperation(value = "上传文件")
    public Result<String> upload(MultipartFile file, String source) {
        String path = DateUtil.parseDateToStr(DateUtil.YYYYMMDD, DateUtil.getNowDate()) + "/";
        //这个source可在前端上传文件时提供，可用来区分是头像还是文章图片等
        if (StringUtils.isNotBlank(source)) {
            path = path + source + "/";
        }
        //获取文件名和后缀
        FileInfo fileInfo = fileStorageService.of(file)
                .setPath(path)
                .setSaveFilename(RandomUtil.randomNumbers(2) + "_" + file.getOriginalFilename()) //随机俩个数字，避免相同文件名时文件名冲突
                .putAttr("source", source)
                .upload();

        if (fileInfo == null) {
            throw new ServiceException("上传文件失败");
        }
        return Result.success(fileInfo.getUrl());
    }

    @GetMapping("/delete")
    @ApiOperation(value = "删除文件")
    public Result<Boolean> delete(String url) {
        boolean flag = fileStorageService.delete(url);
        if (flag) {
            fileDetailService.delete(url);
        }
        return Result.success();
    }
}
