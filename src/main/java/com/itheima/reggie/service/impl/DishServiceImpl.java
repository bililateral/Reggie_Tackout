package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealDishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private SetmealDishService setmealDishService;

    //新增菜品，同时插入菜品对应的口味数据,需要操作两张表- dish、dish_flavor
    @Transactional//涉及多张表操作，需要加入该注解保证数据同步
    public void saveWithFlavor(DishDto dto) {
        //保存菜品的基本信息到菜品表dish
        this.save(dto);
        //获取菜品ID。因为dishId是dish表主键，前端传数据给dto时，dto
        //里的flavors(每个元素都是表dish_falvor的一行数据)中的dishId没有赋值
        Long dishId = dto.getId();
        List<DishFlavor> flavors = dto.getFlavors();
        //把菜品ID赋值给flavors中的每个DishFlavor实体
        flavors = flavors.stream().map(item ->{
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());
        //保存菜品口味数据到菜品口味表dish_flavor，批量保存一个集合的数据用saveBatch()
        dishFlavorService.saveBatch(flavors);
    }

    //根据菜品Id查询菜品信息和对应的口味信息
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        DishDto dto = new DishDto();
        //查询菜品基本信息，从dish表查询
        Dish dish = this.getById(id);
        BeanUtils.copyProperties(dish, dto); //BeanUtils.copyProperties(源，目的）
        //查询菜品对应的口味信息，从dish_flavor表中查询
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dish.getId());
        List<DishFlavor> list = dishFlavorService.list(queryWrapper);
        dto.setFlavors(list); //给DishDto中的flavors属性赋值
        return dto;
    }

    //更新菜品信息,同时更新口味信息
    @Transactional
    @Override
    public void updateWithFlavor(DishDto dto) {
        //更新dish表基本信息
        this.updateById(dto);
        //先清理当前菜品对应的口味数据--dish_flavor表的delete操作
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dto.getId());
        dishFlavorService.remove(queryWrapper);   //Bug日记：不是用removeById啊，用的是remove
        //添加前端当前提交过来的口味数据--dish_flavor表的insert操作
        List<DishFlavor> flavors = dto.getFlavors();
        //把菜品ID赋值给flavors中的每个DishFlavor实体(和上面saveWithFlavor的逻辑一样）
        flavors = flavors.stream().map(item ->{
            item.setDishId(dto.getId());
            return item;
        }).collect(Collectors.toList());
        dishFlavorService.saveBatch(flavors);
    }

    @Override
    public void removeWithFlavor(List<Long> ids) {
        //查询菜品状态，确定是否可以删除
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Dish::getId, ids);
        queryWrapper.eq(Dish::getStatus,1);
        //如果不能删除，抛出一个业务异常
        if(this.count(queryWrapper) > 0)
            throw new CustomException("菜品正在售卖中，不能删除！");

        //检查菜品是否被套餐关联
        LambdaQueryWrapper<SetmealDish> setmealDishQueryWrapper = new LambdaQueryWrapper<>();
        setmealDishQueryWrapper.in(SetmealDish::getDishId, ids);
        if (setmealDishService.count(setmealDishQueryWrapper) > 0) {
            throw new CustomException("菜品已关联套餐，不能删除！");
        }

        // 如果能删，先删除菜品表中的数据
        this.removeByIds(ids);
        //接着删除关系表中的数据--dish_flavor表
        LambdaQueryWrapper<DishFlavor> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.in(DishFlavor::getDishId, ids);
        dishFlavorService.remove(queryWrapper1);
    }
}
