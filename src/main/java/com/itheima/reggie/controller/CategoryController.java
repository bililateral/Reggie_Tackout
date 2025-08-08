package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    //新增菜品或套餐分类
    @PostMapping
    public R<String> save(@RequestBody Category category){
        log.info("新增分类 category:{}",category);
        categoryService.save(category);
        return R.success("新增分类成功");
    }

    //分页信息查询
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize){
        log.info("page:{}, pageSize{}",page,pageSize);

        Page<Category> categoryPage = new Page<>(page,pageSize);
        //设置条件构造器对象
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        //添加生序排序条件，根据sort字段进行排序
        queryWrapper.orderByAsc(Category::getSort);
        //进行分页查询
        categoryPage = categoryService.page(categoryPage,queryWrapper);
        return R.success(categoryPage);
    }

    //根据id删除分类
    @DeleteMapping
    public R<String>  delete(Long ids){
        log.info("删除分类，id为:{}",ids);
        categoryService.remove(ids);
        return R.success("分类信息删除成功");
    }

    //根据id修改分类信息
    @PutMapping
    public R<String>  update(@RequestBody Category category){
        log.info("修改分类信息,category:{}",category);
        categoryService.updateById(category);
        return R.success("修改分类信息成功");
    }

    //根据条件查询分类数据，由于是Get请求，请求参数在请求行中，所以不需要加@RequestBody
    @GetMapping("/list")
    public R<List<Category>> list(Category category){
        log.info("分类(Category)list...");
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        //添加查询条件
        queryWrapper.eq(category.getType()!=null,Category::getType,category.getType());
        //添加排序条件
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
        List<Category> list = categoryService.list(queryWrapper);
        return R.success(list);
    }

}
