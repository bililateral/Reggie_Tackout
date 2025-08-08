package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.OrdersDto;
import com.itheima.reggie.entity.OrderDetail;
import com.itheima.reggie.entity.Orders;
import com.itheima.reggie.service.OrderDetailService;
import com.itheima.reggie.service.OrdersService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrdersController {
    @Autowired
    private OrdersService ordersService;
    @Autowired
    private OrderDetailService orderDetailService;

    //处理用户下单
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders) {
        log.info("移动端用户下单... 订单数据：{}", orders);
        ordersService.submit(orders);
        return R.success("移动端用户下单成功");
    }

    //管理界面分页展示订单
    @GetMapping("/page") public R<Page> page(int page, int pageSize, String number, String beginTime, String endTime){
        log.info("管理界面分页展示订单...");
        //分页构造器对象
        Page<Orders> pageInfo = new Page<>(page, pageSize);
        //构造条件查询对象
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        //添加查询条件
        queryWrapper.like(StringUtils.isNotEmpty(number), Orders::getNumber, number).gt(StringUtils.isNotEmpty(beginTime), Orders::getOrderTime, beginTime).lt(StringUtils.isNotEmpty(endTime), Orders::getOrderTime, endTime);

        ordersService.page(pageInfo, queryWrapper);
        return R.success(pageInfo);
    }

    //移动端订单分页展示
    @GetMapping("/userPage")
    public R<Page> userPage(int page, int pageSize){
        log.info("移动端客户订单分页展示...");
        Page<Orders> userPage =new Page<>(page,pageSize);
        Page<OrdersDto> pageInfo = new Page<>(page,pageSize);
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        //获取当前的userId
        Long currentId = BaseContext.getCurrentId();
        queryWrapper.eq(currentId != null,Orders::getUserId,currentId);
        queryWrapper.orderByDesc(Orders::getOrderTime);
        ordersService.page(userPage,queryWrapper);
        BeanUtils.copyProperties(userPage, pageInfo ,"records");
        //修改records数组
        List<Orders> records = userPage.getRecords();
        List<OrdersDto> list = records.stream().map(item->{
            OrdersDto dto = new OrdersDto();
            BeanUtils.copyProperties(item,dto);
            LambdaQueryWrapper<OrderDetail> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.eq(item.getId() != null,OrderDetail::getOrderId,item.getId());
            List<OrderDetail> orderDetails =orderDetailService.list(queryWrapper1);
            dto.setOrderDetails(orderDetails);
            return dto;
        }).collect(Collectors.toList());
        pageInfo.setRecords(list);
        return R.success(pageInfo);
    }

}
