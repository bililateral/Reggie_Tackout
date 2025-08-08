package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {

    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping
    public R<String> save(@RequestBody DishDto dishdto) {
        log.info("save dishdto :{}", dishdto);
        dishService.saveWithFlavor(dishdto);
        //删除旧Redis缓存
        String key ="dish_" + dishdto.getCategoryId() + "_" + dishdto.getStatus();
        redisTemplate.delete(key);
        return R.success("新增菜品成功");
    }

    /**
     * 菜品信息分页功能实现
     * @param page 有少页
     * @param pageSize 每页多大
     * @param name 传过来的菜品名称
     * @return 分页结果
     */
    @GetMapping("/page")
    public R<Page<DishDto>> page(int page, int pageSize, String name){
        log.info("正在进行菜品分页...");
        //构造分页构造器对象
        Page<Dish> pageInfo = new Page<>(page, pageSize);
        Page<DishDto> dtoPage = new Page<>();
        //构造条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件，采用模糊查询
        queryWrapper.like(name != null, Dish::getName, name);
        //添加排序条件
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        //执行分页查询
        dishService.page(pageInfo, queryWrapper);
        //对象拷贝(忽略records这个属性。records是分页查询展示的结果数组，就是页面上看到的一行行数据)
        BeanUtils.copyProperties(pageInfo, dtoPage,"records");//BeanUtils.copyProperties(源，目的),这里最好别强制类型转换
        //修改records数组
        List<Dish> records = pageInfo.getRecords();
        List<DishDto> list = records.stream().map(item->{
            DishDto dto = new DishDto();
            BeanUtils.copyProperties(item, dto);
            //给DishDto实体中的categoryName属性赋值(先根据item(Dish实体)中的CategoryId在
            //category表里查询对应的Category对象，获取该对象的categoryName属性赋值给DishDto实体对应属性)
            dto.setCategoryName(categoryService.getById(item.getCategoryId()).getName());
            return dto;
        }).collect(Collectors.toList());
        dtoPage.setRecords(list);
        return R.success(dtoPage);
    }

    /**
     * 根据id查询菜品信息和对应的口味信息
     * @param id  前端发起的Get请求中的菜品id参数
     * @return  菜品信息和对应的口味信息
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id) {
        log.info("根据id查询菜品信息和对应的口味信息 id: {}", id);
        DishDto byIdWithFlavor = dishService.getByIdWithFlavor(id);
        return R.success(byIdWithFlavor);
    }

    //修改菜品
    @PutMapping
    public R<String> update(@RequestBody DishDto dto) {
        log.info("update dish dto :{}", dto);
        dishService.updateWithFlavor(dto);
        //删除旧Redis缓存
        String key ="dish_" + dto.getCategoryId() + "_" + dto.getStatus();
        redisTemplate.delete(key);
        return R.success("修改菜品成功");
    }

    /**
     * 更新菜品为停售
     * @param ids Dish的id
     * @return
     */
    @PostMapping("/status/0")
    public R<String> updateStatusStop(Long ids){
        log.info("停售菜品 id:{}",ids);
        Dish dish=dishService.getById(ids);
        dish.setStatus(0);
        LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Dish::getId, ids);
        dishService.update(dish, lambdaQueryWrapper);
        //删除旧Redis缓存
        String key ="dish_*" ;
        redisTemplate.delete(key);
        return R.success("更新菜品在售状态成功");
    }

    /**
     * 更新菜品状态为起售
     * @param ids Dish的id
     * @return
     */
    @PostMapping("/status/1")
    public R<String> updateStatusStart(Long ids){
        log.info("启售菜品 id:{}",ids);
        Dish dish=dishService.getById(ids);
        dish.setStatus(1);
        LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Dish::getId, ids);
        dishService.update(dish, lambdaQueryWrapper);
        //删除旧Redis缓存
        String key ="dish_*" ;
        redisTemplate.delete(key);
        return R.success("更新菜品在售状态成功");
    }

    //批量删除菜品
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids) {
        log.info("批量删除菜品 ids:{}", ids);
        dishService.removeWithFlavor(ids);
        //删除旧Redis缓存
        String key ="dish_*" ;
        redisTemplate.delete(key);
        return R.success("菜品数据删除成功");
    }

    //根据条件查询菜品数据(起售状态为 1且CategoryId等于请求的CategoryId)
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){
        log.info("list dish :{}", dish);
        List<DishDto> dishDtolist;
        //从Redis缓存中获取缓存数据,以菜品分类为键
        String key ="dish_" + dish.getCategoryId() + "_" + dish.getStatus();
        dishDtolist = (List<DishDto>) redisTemplate.opsForValue().get(key);
        //如果存在，直接返回，无需查询数据库
        if(dishDtolist != null)
            return R.success(dishDtolist);

        //构造查询条件对象
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        //添加条件，起售状态为1
        queryWrapper.eq(Dish::getStatus,1);
        //添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list = dishService.list(queryWrapper);

        dishDtolist = list.stream().map(item->{
            DishDto dto = new DishDto();
            BeanUtils.copyProperties(item, dto);
            if(categoryService.getById(item.getCategoryId()) != null)
                dto.setCategoryName(categoryService.getById(item.getCategoryId()).getName());

            Long dishId = item.getId(); //关联当前菜品id
            LambdaQueryWrapper<DishFlavor> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.eq(DishFlavor::getDishId, dishId);
            List<DishFlavor> dishFlavorList = dishFlavorService.list(queryWrapper1);//查出菜品ID为dishid的口味集合
            dto.setFlavors(dishFlavorList);
            return dto;
        }).collect(Collectors.toList());

        //如果不存在，需要查询数据库，将查询到的菜品数据缓存在Redis中,设置过期时间60分钟
        redisTemplate.opsForValue().set(key, dishDtolist,60, TimeUnit.MINUTES);
        return R.success(dishDtolist);
    }
}
