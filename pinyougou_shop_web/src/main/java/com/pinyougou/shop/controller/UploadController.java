package com.pinyougou.shop.controller;

import com.pinyougou.utils.FastDFSClient;
import entity.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Steven
 * @version 1.0
 * @description com.pinyougou.shop.controller
 * @date 2018-11-2
 */
@RestController
public class UploadController {

    @Value("${FAST_DFS_SERVER_URL}")
    private String FAST_DFS_SERVER_URL;

    @RequestMapping("upload")
    public Result upload(MultipartFile file){

        try {
            //1、初始化上传工具类
            FastDFSClient dfsClient = new FastDFSClient("classpath:fdfs_client.conf");
            //2、上传文件
            //获取原来的文件名
            String oldName = file.getOriginalFilename();
            //获取文件后缀名
            String extName = oldName.substring(oldName.indexOf(".") + 1);
            //执行上传文件，得到file_id
            String uploadFile = dfsClient.uploadFile(file.getBytes(), extName);
            //3、拼接文件名返回
            String url = FAST_DFS_SERVER_URL + uploadFile;
            //上传成功，返回路径
            return new Result(true, url);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "上传文件失败");
        }
    }
}
