package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "分类管理接口", description = "菜品分类和套餐分类的CRUD操作，支持排序和分页")
@Slf4j
@RestController
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    //新增菜品或套餐分类
    @Operation(summary = "新增分类", description = "新增分类类型")
    @PostMapping
    public R<String> save(@Parameter(description = "分类信息，type=1为菜品分类，type=2为套餐分类", required = true,
            schema = @Schema(example = "{\"name\":\"热菜\",\"type\":1,\"sort\":1}"))@RequestBody Category category){
        log.info("新增分类 category:{}",category);
        categoryService.save(category);
        return R.success("新增分类成功");
    }

    //分页信息查询
    @GetMapping("/page")
    public R<Page> page(@Parameter(description = "页码", required = true, example = "1") int page,
                        @Parameter(description = "每页条数", required = true, example = "10") int pageSize){
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

    // 根据id删除分类
    @Operation(summary = "删除分类", description = "根据ID删除指定的菜品分类或套餐分类，删除前需确保该分类下无关联数据")
    @DeleteMapping
    public R<String> delete(
            @Parameter(description = "分类ID，唯一标识要删除的分类", required = true, example = "1001")
            Long ids) {
        log.info("删除分类，id为:{}", ids);
        categoryService.remove(ids);
        return R.success("分类信息删除成功");
    }

    // 根据id修改分类信息
    @Operation(summary = "修改分类信息", description = "更新分类的基本信息，如名称、排序值、类型等")
    @PutMapping
    public R<String> update(
            @Parameter(description = "包含更新信息的分类对象，需包含ID字段", required = true,
                    schema = @Schema(example = "{\"id\":1001,\"name\":\"热菜\",\"sort\":2}"))
            @RequestBody Category category) {
        log.info("修改分类信息,category:{}", category);
        categoryService.updateById(category);
        return R.success("修改分类信息成功");
    }

    // 根据条件查询分类数据
    @Operation(summary = "查询分类列表", description = "根据分类类型查询分类列表，支持按排序值升序和更新时间降序排列")
    @GetMapping("/list")
    public R<List<Category>> list(
            @Parameter(description = "查询条件，主要用于指定分类类型（1=菜品分类，2=套餐分类）",
                    example = "1", schema = @Schema(allowableValues = {"1", "2"}))
            Category category) {
        log.info("分类(Category)list...");
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        // 添加查询条件
        queryWrapper.eq(category.getType() != null, Category::getType, category.getType());
        // 添加排序条件
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
        List<Category> list = categoryService.list(queryWrapper);
        return R.success(list);
    }

}
