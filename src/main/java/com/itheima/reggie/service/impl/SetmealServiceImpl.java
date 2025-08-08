package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {
    @Autowired
    private SetmealDishService setmealDishService;

    //修改套餐，同时修改关联的菜品信息
    @Transactional
    @Override
    public void updateWithDish(SetmealDto setmealdto) {
        //更新setmeal表基本信息
        this.updateById(setmealdto);
        //先清理当前套餐关联的菜品数据--setmeal_dish表的delete操作
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, setmealdto.getId());
        setmealDishService.remove(queryWrapper);   //Bug日记：不是用removeById啊，用的是remove
        //添加前端当前提交过来的口味数据--setmeal_dish表的insert操作
        List<SetmealDish> dishes = setmealdto.getSetmealDishes();
        //把套餐ID赋值给dishes中的每个SetmealDish实体(和上面saveWithDish的逻辑一样）
        dishes = dishes.stream().map(item ->{
            item.setSetmealId(setmealdto.getId());
            return item;
        }).collect(Collectors.toList());
        setmealDishService.saveBatch(dishes);
    }

    //新增套餐，同时需要保持套餐和菜品的关联关系
    @Override
    @Transactional()
    public void saveWithDish(SetmealDto setmealdto) {
        //保存套餐基本信息，操作setmeal表，执行insert操作
        this.save(setmealdto);
        //保存套餐和菜品的关联信息，操作setmealdish表，执行insert操作
        List<SetmealDish> setmealDishes = setmealdto.getSetmealDishes();
        //同样的外键还是没保存(setmealId)
        setmealDishes = setmealDishes.stream().map(item -> {
            item.setSetmealId(setmealdto.getId());
            return item;
        }).collect(Collectors.toList());
        setmealDishService.saveBatch(setmealDishes);
    }

    //批量删除套餐
    @Override
    @Transactional()
    public void removeWithDish(List<Long> ids){
        //查询套餐状态，确定是否可以删除
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId, ids);
        queryWrapper.eq(Setmeal::getStatus,1);
        //如果不能删除，抛出一个业务异常
        if(this.count(queryWrapper) > 0)
            throw new CustomException("套餐正在售卖中，不能删除！");
        // 如果能删，先删除套餐表中的数据
        this.removeByIds(ids);
        //接着删除关系表中的数据--setmeal_dish表
        LambdaQueryWrapper<SetmealDish> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.in(SetmealDish::getSetmealId, ids);
        setmealDishService.remove(queryWrapper1);
    }

    @Override
    public SetmealDto getByIdWithDish(Long id) {
        Setmeal setmeal = this.getById(id);
        if (setmeal == null)
            throw new CustomException("该套餐不存在");

        SetmealDto setmealDto = new SetmealDto();

        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(id != null,SetmealDish::getSetmealId,id);

        BeanUtils.copyProperties(setmeal,setmealDto);
        List<SetmealDish> dishes = setmealDishService.list(queryWrapper);
        setmealDto.setSetmealDishes(dishes);
        return setmealDto;
    }
}
