package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/setmeal")
//套餐管理
public class SetmealController {
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private CategoryService categoryService;

    //新增套餐，同时需要保持套餐和菜品的关联关系
    @PostMapping
    //删除setmealCache下的所有缓存数据
    @CacheEvict(value = "setmealCache",allEntries = true)
    public R<String> save(@RequestBody SetmealDto setmealdto){
        log.info("save SetmealDish");
        setmealService.saveWithDish(setmealdto);
        return R.success("套餐添加成功");
    }

    @GetMapping("/page")
    public R<Page<SetmealDto>> page(int page, int pageSize, String name){
        log.info("setmeal page...");
        Page<Setmeal> setmealPage = new Page<>(page, pageSize);
        Page<SetmealDto> setmealDtoPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(name != null, Setmeal::getName, name);
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        setmealService.page(setmealPage, queryWrapper);
        //对象拷贝
        BeanUtils.copyProperties(setmealPage, setmealDtoPage,"records");
        List<Setmeal> records = setmealPage.getRecords();
        List<SetmealDto> list = records.stream().map(item->{
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item,setmealDto);
            //根据分类id获取Category对象
            Category category = categoryService.getById(item.getCategoryId());
            if(category != null)
                setmealDto.setCategoryName(category.getName());
            return setmealDto;
        }).toList();
        setmealDtoPage.setRecords(list);
        return R.success(setmealDtoPage);
    }

    //批量删除套餐
    @DeleteMapping
    //删除setmealCache下的所有缓存数据
    @CacheEvict(value = "setmealCache",allEntries = true)
    public R<String> delete(@RequestParam List<Long> ids){
        log.info("批量删除套餐 ids:{}",ids.toString());
        setmealService.removeWithDish(ids);
        return R.success("套餐数据删除成功");
    }

    //管理端展示套餐信息
    @GetMapping("{id}")
    public R<SetmealDto> getSetmal(@PathVariable("id") Long id){
        log.info("管理端展示套餐信息 Id:"+id);
        SetmealDto setmealDto=setmealService.getByIdWithDish(id);
        return R.success(setmealDto);
    }

    //向移动端用户展示套餐信息
    @GetMapping("/dish/{id}")
    public R<SetmealDto> getSetmalDetail(@PathVariable("id") Long id){
        log.info("移动端展示套餐信息 Id:"+id);
        SetmealDto setmealDto=setmealService.getByIdWithDish(id);
        return R.success(setmealDto);
    }

    //修改套餐
    @PutMapping
    //删除setmealCache下的所有缓存数据
    @CacheEvict(value = "setmealCache",allEntries = true)
    public R<String> update(@RequestBody SetmealDto setmealdto) {
        log.info("更新套餐 stemealdto :{}", setmealdto);
        setmealService.updateWithDish(setmealdto);
        return R.success("修改套餐成功");
    }

    //停售套餐
    @PostMapping("/status/0")
    //删除setmealCache下的所有缓存数据
    @CacheEvict(value = "setmealCache",allEntries = true)
    public R<String> stopSale(Long ids){
        log.info("停售套餐 id:{}",ids);
        Setmeal setmeal=setmealService.getById(ids);
        setmeal.setStatus(0);
        LambdaQueryWrapper<Setmeal> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Setmeal::getId, ids);
        setmealService.update(setmeal, lambdaQueryWrapper);
        return R.success("更新套餐在售状态成功");
    }

    //启售套餐
    @PostMapping("/status/1")
    //删除setmealCache下的所有缓存数据
    @CacheEvict(value = "setmealCache",allEntries = true)
    public R<String> startSale(Long ids){
        log.info("启售套餐 id:{}",ids);
        Setmeal setmeal=setmealService.getById(ids);
        setmeal.setStatus(1);
        LambdaQueryWrapper<Setmeal> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Setmeal::getId, ids);
        setmealService.update(setmeal, lambdaQueryWrapper);
        return R.success("更新套餐在售状态成功");
    }

    //根据categoryId展示套餐
    @GetMapping("/list")
    //缓存对象需要实现序列化(类R需要实现序列化)
    @Cacheable(value = "setmealCache",key = "#setmeal.getCategoryId() + '_' + #setmeal.status")
    public R<List<Setmeal>> list(Setmeal setmeal){
        log.info("根据categoryId展示套餐 id:{}",setmeal.getCategoryId());
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId()!=null,Setmeal::getCategoryId,setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus()!=null,Setmeal::getStatus,setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> list = setmealService.list(queryWrapper);
        return R.success(list);
    }
}
