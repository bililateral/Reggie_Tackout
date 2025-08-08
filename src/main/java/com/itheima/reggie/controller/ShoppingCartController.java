package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;

    //将菜品或套餐添加到购物车
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
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

    @PostMapping("/sub")
    public R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart) {
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
    @DeleteMapping("/clean")
    public R<String> clean(){
        log.info("清空购物车...");
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        shoppingCartService.remove(queryWrapper);
        return R.success("购物车清空成功");
    }
}
