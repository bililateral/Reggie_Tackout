package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetmealService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "套餐管理接口", description = "套餐的新增、修改、删除、查询及状态管理，包含套餐与菜品关联关系维护")
@Slf4j
@RestController
@RequestMapping("/setmeal")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private CategoryService categoryService;

    @Operation(summary = "新增套餐", description = "添加新套餐及关联的菜品信息，支持多菜品组合",
            responses = {
                    @ApiResponse(responseCode = "200", description = "新增成功",
                            content = @Content(schema = @Schema(implementation = R.class))),
                    @ApiResponse(responseCode = "500", description = "新增失败",
                            content = @Content(schema = @Schema(implementation = R.class)))
            })
    @PostMapping
    @CacheEvict(value = "setmealCache",allEntries = true)
    public R<String> save(
            @Parameter(description = "包含套餐基本信息和关联菜品列表的DTO对象", required = true)
            @RequestBody SetmealDto setmealdto){
        log.info("save SetmealDish");
        setmealService.saveWithDish(setmealdto);
        return R.success("套餐添加成功");
    }

    @Operation(summary = "套餐分页查询", description = "分页查询套餐列表，支持按名称模糊搜索，返回结果包含分类名称",
            responses = {
                    @ApiResponse(responseCode = "200", description = "查询成功",
                            content = @Content(schema = @Schema(implementation = R.class))),
                    @ApiResponse(responseCode = "500", description = "查询失败",
                            content = @Content(schema = @Schema(implementation = R.class)))
            })
    @GetMapping("/page")
    public R<Page<SetmealDto>> page(
            @Parameter(description = "页码，从1开始", required = true, example = "1") int page,
            @Parameter(description = "每页显示条数", required = true, example = "10") int pageSize,
            @Parameter(description = "套餐名称，用于模糊查询，非必填") String name){
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

    @Operation(summary = "批量删除套餐", description = "删除指定ID列表的套餐及其关联的菜品关系，支持批量操作",
            responses = {
                    @ApiResponse(responseCode = "200", description = "删除成功",
                            content = @Content(schema = @Schema(implementation = R.class))),
                    @ApiResponse(responseCode = "500", description = "删除失败",
                            content = @Content(schema = @Schema(implementation = R.class)))
            })
    @DeleteMapping
    @CacheEvict(value = "setmealCache",allEntries = true)
    public R<String> delete(
            @Parameter(description = "套餐ID列表，多个ID用逗号分隔", required = true)
            @RequestParam List<Long> ids){
        log.info("批量删除套餐 ids:{}",ids.toString());
        setmealService.removeWithDish(ids);
        return R.success("套餐数据删除成功");
    }

    @Operation(summary = "查询套餐详情（管理端）", description = "根据ID查询套餐基本信息及关联的菜品列表，用于编辑套餐",
            responses = {
                    @ApiResponse(responseCode = "200", description = "查询成功",
                            content = @Content(schema = @Schema(implementation = R.class))),
                    @ApiResponse(responseCode = "404", description = "套餐不存在",
                            content = @Content(schema = @Schema(implementation = R.class)))
            })
    @GetMapping("{id}")
    public R<SetmealDto> getSetmal(
            @Parameter(description = "套餐ID", required = true, example = "1397844313464427522")
            @PathVariable("id") Long id){
        log.info("管理端展示套餐信息 Id:"+id);
        SetmealDto setmealDto=setmealService.getByIdWithDish(id);
        return R.success(setmealDto);
    }

    @Operation(summary = "查询套餐详情（移动端）", description = "根据ID查询套餐基本信息及关联的菜品列表，用于用户查看套餐内容",
            responses = {
                    @ApiResponse(responseCode = "200", description = "查询成功",
                            content = @Content(schema = @Schema(implementation = R.class))),
                    @ApiResponse(responseCode = "404", description = "套餐不存在",
                            content = @Content(schema = @Schema(implementation = R.class)))
            })
    @GetMapping("/dish/{id}")
    public R<SetmealDto> getSetmalDetail(
            @Parameter(description = "套餐ID", required = true, example = "1397844313464427522")
            @PathVariable("id") Long id){
        log.info("移动端展示套餐信息 Id:"+id);
        SetmealDto setmealDto=setmealService.getByIdWithDish(id);
        return R.success(setmealDto);
    }

    @Operation(summary = "修改套餐", description = "更新套餐基本信息及关联的菜品列表，支持全量更新菜品关联关系",
            responses = {
                    @ApiResponse(responseCode = "200", description = "修改成功",
                            content = @Content(schema = @Schema(implementation = R.class))),
                    @ApiResponse(responseCode = "500", description = "修改失败",
                            content = @Content(schema = @Schema(implementation = R.class)))
            })
    @PutMapping
    @CacheEvict(value = "setmealCache",allEntries = true)
    public R<String> update(
            @Parameter(description = "包含更新后套餐信息和菜品列表的DTO对象", required = true)
            @RequestBody SetmealDto setmealdto) {
        log.info("更新套餐 stemealdto :{}", setmealdto);
        setmealService.updateWithDish(setmealdto);
        return R.success("修改套餐成功");
    }

    @Operation(summary = "停售套餐", description = "将指定ID的套餐状态设置为停售（0），停售后不在前端展示",
            responses = {
                    @ApiResponse(responseCode = "200", description = "状态更新成功",
                            content = @Content(schema = @Schema(implementation = R.class))),
                    @ApiResponse(responseCode = "404", description = "套餐不存在",
                            content = @Content(schema = @Schema(implementation = R.class)))
            })
    @PostMapping("/status/0")
    @CacheEvict(value = "setmealCache",allEntries = true)
    public R<String> stopSale(
            @Parameter(description = "套餐ID", required = true, example = "1397844313464427522")
            Long ids){
        log.info("停售套餐 id:{}",ids);
        Setmeal setmeal=setmealService.getById(ids);
        setmeal.setStatus(0);
        LambdaQueryWrapper<Setmeal> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Setmeal::getId, ids);
        setmealService.update(setmeal, lambdaQueryWrapper);
        return R.success("更新套餐在售状态成功");
    }

    @Operation(summary = "起售套餐", description = "将指定ID的套餐状态设置为起售（1），起售后可在前端展示和下单",
            responses = {
                    @ApiResponse(responseCode = "200", description = "状态更新成功",
                            content = @Content(schema = @Schema(implementation = R.class))),
                    @ApiResponse(responseCode = "404", description = "套餐不存在",
                            content = @Content(schema = @Schema(implementation = R.class)))
            })
    @PostMapping("/status/1")
    @CacheEvict(value = "setmealCache",allEntries = true)
    public R<String> startSale(
            @Parameter(description = "套餐ID", required = true, example = "1397844313464427522")
            Long ids){
        log.info("启售套餐 id:{}",ids);
        Setmeal setmeal=setmealService.getById(ids);
        setmeal.setStatus(1);
        LambdaQueryWrapper<Setmeal> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Setmeal::getId, ids);
        setmealService.update(setmeal, lambdaQueryWrapper);
        return R.success("更新套餐在售状态成功");
    }

    @Operation(summary = "查询套餐列表", description = "根据分类ID和状态查询套餐列表，支持Redis缓存，用于前端展示",
            responses = {
                    @ApiResponse(responseCode = "200", description = "查询成功",
                            content = @Content(schema = @Schema(implementation = R.class))),
                    @ApiResponse(responseCode = "500", description = "查询失败",
                            content = @Content(schema = @Schema(implementation = R.class)))
            })
    @GetMapping("/list")
    @Cacheable(value = "setmealCache",key = "#setmeal.getCategoryId() + '_' + #setmeal.status")
    public R<List<Setmeal>> list(
            @Parameter(description = "查询条件，包含分类ID和状态（1为起售）", required = true)
            Setmeal setmeal){
        log.info("根据categoryId展示套餐 id:{}",setmeal.getCategoryId());
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId()!=null,Setmeal::getCategoryId,setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus()!=null,Setmeal::getStatus,setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> list = setmealService.list(queryWrapper);
        return R.success(list);
    }
}