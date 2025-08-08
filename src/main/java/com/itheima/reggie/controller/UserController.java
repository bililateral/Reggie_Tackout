package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.service.UserService;
import com.itheima.reggie.utils.SMSUtil;
import com.itheima.reggie.utils.ValidateCodeUtil;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    //发送手机验证码
    @GetMapping("/code")
    public R<String> sendMsg( String phone){
        log.info("正在发送验证码...");
        //获取手机号
        if(StringUtils.isNotEmpty(phone)){
            //生成随机的4位验证码
            String validCode = ValidateCodeUtil.generateValidateCode(6).toString();
            //调用阿里云的短信服务API完成发送短信(没钱用不了,只能让它显示在控制台了)
            log.info("验证码为：{}",validCode);
            //SMSUtil.sendMessage("","", phone, validCode);
            //保存并校验验证码
            //session.setAttribute(phone,validCode);

            //使用redis缓存验证码，设置过期时间5分钟
            redisTemplate.opsForValue().set(phone,validCode,300, TimeUnit.SECONDS);
            return R.success("手机验证码短信发送成功");
        }
        return R.error("手机验证码短信发送失败");
    }

    //移动端用户登录
    @PostMapping("/login")
    public R<User> login(@RequestBody Map<String,Object> map, HttpSession session){//map也可以换成DTO的方式，主要是要包含前面发送的验证码
        log.info("移动端用户登录...{}",map.toString());
        //获取手机号
        String phone = map.get("phone").toString();
        //获取验证码
        String code = map.get("code").toString();
        //从redis中获取缓存的验证码,进行验证码比对
        Object codeInSession =redisTemplate.opsForValue().get(phone);
        //Object codeInSession =session.getAttribute(phone);
        if(codeInSession != null &&codeInSession.toString().equals(code)){
            //验证成功后判断当前手机号是否为新用户，若是新用户就自动完成注册
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone,phone);
            User user = userService.getOne(queryWrapper);
            if(user == null){
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }
            //登陆成功后保存用户信息到session中
            session.setAttribute("user",user.getId());

            //登陆成功从后redis删除验证码
            redisTemplate.delete(phone);
            return R.success(user);
        }
        return R.error("登录失败,验证码错误！");
    }

    @PostMapping("/loginout")
    public R<String> loginout(HttpSession session){
        log.info("移动端用户退出...");
        session.removeAttribute("user");
        return R.success("移动端用户退出成功");
    }
}
