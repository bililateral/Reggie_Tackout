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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Tag(name = "菜品管理接口", description = "菜品信息及口味管理，支持分页、条件查询和状态变更")
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

    @Operation(summary = "新增菜品", description = "添加新菜品及关联的口味信息，支持多口味设置",
            responses = {
                    @ApiResponse(responseCode = "200", description = "新增成功",
                            content = @Content(schema = @Schema(implementation = R.class))),
                    @ApiResponse(responseCode = "500", description = "新增失败",
                            content = @Content(schema = @Schema(implementation = R.class)))
            })
    @PostMapping
    public R<String> save(@Parameter(description = "包含菜品基本信息（名称、分类、价格等）和口味列表的DTO对象", required = true)
                          @RequestBody DishDto dishdto) {
        log.info("save dishdto :{}", dishdto);
        dishService.saveWithFlavor(dishdto);
        //删除旧Redis缓存
        String key ="dish_" + dishdto.getCategoryId() + "_" + dishdto.getStatus();
        redisTemplate.delete(key);
        return R.success("新增菜品成功");
    }

    @Operation(summary = "菜品分页查询", description = "分页查询菜品列表，支持按名称模糊搜索，返回结果包含分类名称",
            responses = {
                    @ApiResponse(responseCode = "200", description = "查询成功",
                            content = @Content(schema = @Schema(implementation = R.class))),
                    @ApiResponse(responseCode = "500", description = "查询失败",
                            content = @Content(schema = @Schema(implementation = R.class)))
            })
    @GetMapping("/page")
    public R<Page<DishDto>> page(
            @Parameter(description = "页码，从1开始", required = true, example = "1") int page,
            @Parameter(description = "每页显示条数", required = true, example = "10") int pageSize,
            @Parameter(description = "菜品名称，用于模糊查询，非必填", example = "宫保鸡丁") String name){
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
        //对象拷贝(忽略records这个属性)
        BeanUtils.copyProperties(pageInfo, dtoPage,"records");
        //修改records数组
        List<Dish> records = pageInfo.getRecords();
        List<DishDto> list = records.stream().map(item->{
            DishDto dto = new DishDto();
            BeanUtils.copyProperties(item, dto);
            //设置分类名称
            dto.setCategoryName(categoryService.getById(item.getCategoryId()).getName());
            return dto;
        }).collect(Collectors.toList());
        dtoPage.setRecords(list);
        return R.success(dtoPage);
    }

    @Operation(summary = "根据ID查询菜品详情", description = "查询指定ID的菜品信息及关联的口味列表，用于编辑菜品",
            responses = {
                    @ApiResponse(responseCode = "200", description = "查询成功",
                            content = @Content(schema = @Schema(implementation = R.class))),
                    @ApiResponse(responseCode = "404", description = "菜品不存在",
                            content = @Content(schema = @Schema(implementation = R.class)))
            })
    @GetMapping("/{id}")
    public R<DishDto> get(
            @Parameter(description = "菜品ID", required = true, example = "1397844313464427522")
            @PathVariable Long id) {
        log.info("根据id查询菜品信息和对应的口味信息 id: {}", id);
        DishDto byIdWithFlavor = dishService.getByIdWithFlavor(id);
        return R.success(byIdWithFlavor);
    }

    @Operation(summary = "修改菜品", description = "更新菜品基本信息及关联的口味信息，支持全量更新口味列表",
            responses = {
                    @ApiResponse(responseCode = "200", description = "修改成功",
                            content = @Content(schema = @Schema(implementation = R.class))),
                    @ApiResponse(responseCode = "500", description = "修改失败",
                            content = @Content(schema = @Schema(implementation = R.class)))
            })
    @PutMapping
    public R<String> update(
            @Parameter(description = "包含更新后菜品信息和口味列表的DTO对象", required = true)
            @RequestBody DishDto dto) {
        log.info("update dish dto :{}", dto);
        dishService.updateWithFlavor(dto);
        //删除旧Redis缓存
        String key ="dish_" + dto.getCategoryId() + "_" + dto.getStatus();
        redisTemplate.delete(key);
        return R.success("修改菜品成功");
    }

    @Operation(summary = "停售菜品", description = "将指定ID的菜品状态设置为停售（0），停售后不在前端展示",
            responses = {
                    @ApiResponse(responseCode = "200", description = "状态更新成功",
                            content = @Content(schema = @Schema(implementation = R.class))),
                    @ApiResponse(responseCode = "404", description = "菜品不存在",
                            content = @Content(schema = @Schema(implementation = R.class)))
            })
    @PostMapping("/status/0")
    public R<String> updateStatusStop(
            @Parameter(description = "菜品ID", required = true, example = "1397844313464427522")
            Long ids){
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

    @Operation(summary = "起售菜品", description = "将指定ID的菜品状态设置为起售（1），起售后可在前端展示和下单",
            responses = {
                    @ApiResponse(responseCode = "200", description = "状态更新成功",
                            content = @Content(schema = @Schema(implementation = R.class))),
                    @ApiResponse(responseCode = "404", description = "菜品不存在",
                            content = @Content(schema = @Schema(implementation = R.class)))
            })
    @PostMapping("/status/1")
    public R<String> updateStatusStart(
            @Parameter(description = "菜品ID", required = true, example = "1397844313464427522")
            Long ids){
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

    @Operation(summary = "批量删除菜品", description = "删除指定ID列表的菜品及其关联的口味信息，支持批量操作",
            responses = {
                    @ApiResponse(responseCode = "200", description = "删除成功",
                            content = @Content(schema = @Schema(implementation = R.class))),
                    @ApiResponse(responseCode = "500", description = "删除失败",
                            content = @Content(schema = @Schema(implementation = R.class)))
            })
    @DeleteMapping
    public R<String> delete(
            @Parameter(description = "菜品ID列表，多个ID用逗号分隔", required = true, example = "1,2,3")
            @RequestParam List<Long> ids) {
        log.info("批量删除菜品 ids:{}", ids);
        dishService.removeWithFlavor(ids);
        //删除旧Redis缓存
        String key ="dish_*" ;
        redisTemplate.delete(key);
        return R.success("菜品数据删除成功");
    }

    @Operation(summary = "查询菜品列表", description = "根据分类ID和状态查询菜品列表，返回包含口味信息的结果，支持Redis缓存",
            responses = {
                    @ApiResponse(responseCode = "200", description = "查询成功",
                            content = @Content(schema = @Schema(implementation = R.class))),
                    @ApiResponse(responseCode = "500", description = "查询失败",
                            content = @Content(schema = @Schema(implementation = R.class)))
            })
    @GetMapping("/list")
    public R<List<DishDto>> list(
            @Parameter(description = "查询条件，包含分类ID和状态（1为起售）", required = true)
            Dish dish){
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