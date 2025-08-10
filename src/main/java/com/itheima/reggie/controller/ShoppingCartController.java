package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.service.ShoppingCartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "购物车接口", description = "用户购物车的添加、减少、查询、清空等操作")
@Slf4j
@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;

    //将菜品或套餐添加到购物车
    @Operation(summary = "添加商品到购物车", description = "将菜品或套餐添加到当前用户的购物车，支持相同商品数量累加")
    @PostMapping("/add")
    public R<ShoppingCart> add(@Parameter(description = "购物车对象，包含菜品ID/套餐ID及口味信息", required = true) @RequestBody ShoppingCart shoppingCart){
        log.info("正在添加购物车... 购物车数据：{}",shoppingCart);
        //设置用户id,指定当前是哪个用户的购物车数据
        shoppingCart.setUserId(BaseContext.getCurrentId());
        //查询当前的菜品或套餐是否已经在购物车中
        LambdaQueryWrapper <ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,shoppingCart.getUserId());
        if(shoppingCart.getDishId()!=null){//添加到购物车的是菜品
            queryWrapper.eq(ShoppingCart::getDishId,shoppingCart.getDishId());
            // 添加口味判断，不同口味作为不同记录
            queryWrapper.eq(ShoppingCart::getDishFlavor, shoppingCart.getDishFlavor());
        }


        if(shoppingCart.getSetmealId()!=null)//添加到购物车的是套餐
            queryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());

        //SQL: select * from shopping_cart where user_id = ? and dish_id/setmeal_id = ?
        ShoppingCart cartServiceOne = shoppingCartService.getOne(queryWrapper);

        if(cartServiceOne!=null){
            //已经存在，在原来基础上+1即可
            Integer number = cartServiceOne.getNumber();
            cartServiceOne.setNumber(number+1);
            shoppingCartService.updateById(cartServiceOne);
        }
        else{//不存在，则添加到购物车，数量默认是1
            shoppingCart.setNumber(1);
            //设置加入的时间
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            cartServiceOne = shoppingCart;
        }
        return R.success(cartServiceOne);
    }

    @Operation(summary = "减少购物车商品数量", description = "减少购物车中商品的数量，数量为1时直接删除",
            responses = {
                    @ApiResponse(responseCode = "200", description = "操作成功"),
                    @ApiResponse(responseCode = "500", description = "商品不存在或操作失败")
            })
    @PostMapping("/sub")
    public R<ShoppingCart> sub(@Parameter(description = "购物车对象，包含菜品ID/套餐ID及口味信息", required = true) @RequestBody ShoppingCart shoppingCart) {
        log.info("正在减少购物车商品... 购物车数据：{}", shoppingCart);

        // 设置用户ID，指定当前用户的购物车数据
        shoppingCart.setUserId(BaseContext.getCurrentId());

        // 查询当前的菜品或套餐在购物车中的记录
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, shoppingCart.getUserId());

        if (shoppingCart.getDishId() != null) {
            // 减少的是菜品
            queryWrapper.eq(ShoppingCart::getDishId, shoppingCart.getDishId());
            // 考虑口味，相同菜品不同口味是不同记录
            queryWrapper.eq(ShoppingCart::getDishFlavor, shoppingCart.getDishFlavor());
        }

        if (shoppingCart.getSetmealId() != null)
            // 减少的是套餐
            queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());

        ShoppingCart cartServiceOne = shoppingCartService.getOne(queryWrapper);

        if (cartServiceOne != null) {
            // 存在该商品，数量减1
            Integer number = cartServiceOne.getNumber();
            if (number > 1) {
                // 数量大于1时，只减少数量
                cartServiceOne.setNumber(number - 1);
                shoppingCartService.updateById(cartServiceOne);
            } else {
                // 数量为1时，直接删除该记录
                shoppingCartService.removeById(cartServiceOne);
                cartServiceOne.setNumber(0);
            }
            return R.success(cartServiceOne);
        } else
            // 不存在该商品，返回错误信息
            return R.error("购物车中不存在该商品");
    }

    //查看购物车
    @Operation(summary = "查询购物车列表", description = "获取当前用户购物车中的所有商品")
    @GetMapping("list")
    public R<List<ShoppingCart>> getShoppingCart(){
        log.info("查看购物车...");
        LambdaQueryWrapper <ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        queryWrapper.orderByAsc(ShoppingCart::getCreateTime);

        List<ShoppingCart> shoppingCartList = shoppingCartService.list(queryWrapper);
        return R.success(shoppingCartList);
    }

    //清空购物车
    @Operation(summary = "清空购物车", description = "删除当前用户购物车中的所有商品")
    @DeleteMapping("/clean")
    public R<String> clean(){
        log.info("清空购物车...");
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        shoppingCartService.remove(queryWrapper);
        return R.success("购物车清空成功");
    }
}
