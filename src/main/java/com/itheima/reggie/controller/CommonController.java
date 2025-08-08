package com.itheima.reggie.controller;

import com.itheima.reggie.common.R;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

//负责文件上传和下载
@Slf4j
@RestController
@RequestMapping("/common")
public class CommonController {

    @Value("${reggie.path}")
    private String basePath;

    /**
     * 方法参数名必须和upload.html提交的form data中的name一致
     * @RequestPart("file") 是 Spring MVC 中的一个注解，用于从 multipart/form-data 类型的请求中获取指定名称的部分数据。
     * 具体来说，@RequestPart("file") 表示：
     * 从请求中获取名称为 "file" 的 multipart 部分
     * 通常用于接收客户端上传的文件
     * 与 @RequestParam 的区别：
     * @RequestParam主要用于获取简单类型的表单数据
     * @RequestPart更适合处理复杂的 multipart 部分，如文件或 JSON 对象
     * 当需要 Content-Type 信息时，应该使用@RequestPart
     */
    @PostMapping("/upload")
    public R<String> upload(@RequestPart("file") MultipartFile file) throws IOException {
        log.info("文件上传中... {}",file.toString());
        //获取文件后缀名
        String suffix = Objects.requireNonNull(file.getOriginalFilename()).substring(file.getOriginalFilename().lastIndexOf("."));
        String name = UUID.randomUUID() + suffix;
        //创建目录对象
        File dir = new File(basePath);
        //判断当前目录是否存在
        if (!dir.exists())
            dir.mkdirs();
        //使用UUID重新生成文件名，防止文件名重复造成的文件覆盖
        file.transferTo(new File(basePath + name));
        return R.success(name);
    }

    //文件下载
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response) throws IOException {
        log.info("文件下载中... {}",name);
        try {
            //输入流，通过输入流读取文件内容
            FileInputStream fis = new FileInputStream(new File(basePath + name));
            //输出流，通过输出流读到的文件写回浏览器，在浏览器展示图片
            ServletOutputStream outputStream = response.getOutputStream();
            //设置响应回去的文件类型
            response.setContentType("image/jpeg");
            //边读边写
            int len;
            byte[] buffer = new byte[1024];
            while ((len = fis.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
                outputStream.flush();
            }
            outputStream.close();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
