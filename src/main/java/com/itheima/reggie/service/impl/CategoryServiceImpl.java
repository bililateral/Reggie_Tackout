package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.mapper.CategoryMapper;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private DishService dishService;
    @Autowired
    private SetmealService setmealService;

    //根据id删除分类，删除之前需要进行关联判断
    @Override
    public void remove(Long id) {
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //添加查询条件
        queryWrapper.eq(Dish::getCategoryId,id);
        //查询当前分类是否已关联了菜品，若已关联，抛出一个业务异常
        if(dishService.count(queryWrapper) > 0)
            throw new CustomException("当前分类下关联了菜品，无法删除");

        // 查询当前分类是否已关联了套餐，若已关联，抛出一个业务异常
        LambdaQueryWrapper<Setmeal> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(Setmeal::getCategoryId,id);
        if(setmealService.count(queryWrapper1) > 0)
            throw new CustomException("当前分类下关联了套餐，无法删除");

        //正常删除分类
        super.removeById(id);
    }
}
