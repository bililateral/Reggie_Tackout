package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.AddressBook;
import com.itheima.reggie.service.AddressBookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "地址簿接口", description = "用户收货地址管理，支持增删改查操作及默认地址设置")
@Slf4j
@RestController
@RequestMapping("/addressBook")
public class AddressBookController {
    @Autowired
    private AddressBookService addressBookService;

    //新增地址
    @Operation(summary = "添加用户地址列表", description = "为当前登录用户添加地址信息")
    @PostMapping
    public R<AddressBook> save(@Parameter(description = "地址信息，包含收货人、电话、详细地址等",
            required = true,
            schema = @Schema(example = "{\"consignee\":\"张三\",\"phone\":\"13800138000\",\"detail\":\"北京市海淀区\"}"))  @RequestBody AddressBook addressBook){
        addressBook.setUserId(BaseContext.getCurrentId());
        log.info("正在新增地址簿:{}", addressBook);
        addressBookService.save(addressBook);
        return R.success(addressBook);
    }

    //设置默认地址
    @Operation(summary = "设置用户默认地址", description = "为当前登录用户设置默认地址信息")
    @PutMapping("default")
    public R<AddressBook> setDefault(@Parameter(description = "地址簿对象，包含用户ID", required = true,
            schema = @Schema(example = "{\"id\":1}")) @RequestBody AddressBook addressBook){
        log.info("正在创建用户默认地址：{}", addressBook);
        //注意这里生成的是LambdaUpdateWrapper,不是LambdaQueryWrapper
        LambdaUpdateWrapper<AddressBook> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AddressBook::getUserId,BaseContext.getCurrentId());
        updateWrapper.set(AddressBook::getIsDefault,0);
        //SQL:update address_book set is_default = 0 where user_id = ?
        addressBookService.update(updateWrapper);

        addressBook.setIsDefault(1);
        //SQL:update address_book set is_default = 1 where id = ?
        addressBookService.updateById(addressBook);
        return R.success(addressBook);
    }

    /**
     * 根据id查询地址
     */
    @Operation(summary = "根据ID查询地址详情", description = "通过地址ID查询指定地址的详细信息，包含收货人、电话、地址等")
    @GetMapping("/{id}")
    public R get(
            @Parameter(description = "地址ID，唯一标识一个地址", required = true, example = "1001")
            @PathVariable Long id) {
        log.info("正在根据id查询地址... id:{}", id);
        AddressBook addressBook = addressBookService.getById(id);
        if (addressBook != null)
            return R.success(addressBook);
        else
            return R.error("没有找到该对象");
    }

    //查询默认地址
    @Operation(summary = "查询当前用户默认地址", description = "获取当前登录用户设置的默认收货地址，无默认地址时返回错误")
    @GetMapping("/default")
    public R<AddressBook> getDefault() {
        log.info("正在查询默认地址...");
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressBook::getUserId, BaseContext.getCurrentId());
        queryWrapper.eq(AddressBook::getIsDefault, 1);
        //SQL select * from address_book where user_id = ? and is_default = 1
        AddressBook addressBook = addressBookService.getOne(queryWrapper);
        if (addressBook == null)
            return R.error("没有找到该对象");
        return R.success(addressBook);
    }

    //查询指定用户的全部地址
    @Operation(summary = "查询当前用户所有地址", description = "获取当前登录用户的所有收货地址，按更新时间倒序排列")
    @GetMapping("/list")
    public R<List<AddressBook>> list(
            @Parameter(description = "地址查询条件（可空，系统会自动填充当前用户ID）", hidden = true)
            AddressBook addressBook) {
        addressBook.setUserId(BaseContext.getCurrentId());
        log.info("地址簿(addressBook)对象:{}", addressBook);
        //条件构造器
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(addressBook.getUserId() != null, AddressBook::getUserId, addressBook.getUserId());
        queryWrapper.orderByDesc(AddressBook::getUpdateTime);

        //SQL:select * from address_book where user_id = ? order by update_time desc
        return R.success(addressBookService.list(queryWrapper));
    }
}
