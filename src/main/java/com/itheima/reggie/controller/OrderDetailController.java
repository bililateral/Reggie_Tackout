package com.itheima.reggie.controller;

import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Orders;
import com.itheima.reggie.service.OrderDetailService;
import com.itheima.reggie.service.OrdersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "订单详情接口", description = "订单详情的补充操作接口")
@Slf4j
@RestController
@RequestMapping("orderDetail")
public class OrderDetailController {
    @Autowired
    private OrdersService ordersService;

    //移动用户下单
    @Operation(summary = "用户下单（备用接口）", description = "移动端用户提交订单信息的备用接口，与/order/submit功能一致")
    @PostMapping("/submit")
    public R<String> submit(@Parameter(description = "订单信息，包含地址、商品等信息", required = true) @RequestBody Orders orders) {
        ordersService.submit(orders);
        return R.success("移动用户下单成功");
    }

}
