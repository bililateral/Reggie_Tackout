package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;


import static org.springframework.util.DigestUtils.md5DigestAsHex;

@Tag(name = "后台员工管理接口", description = "员工登录、增删改查等操作")
@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    //登录
    @Operation(summary = "员工登录", description = "员工通过用户名和密码登录系统")
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @Parameter(description = "登录信息，包含username和password字段",
            required = true,
            schema = @Schema(example = "{\"username\":\"admin\",\"password\":\"123456\"}"))  @RequestBody Employee employee) {
        log.info("用户正在登陆...");
        String password = employee.getPassword();
        password = md5DigestAsHex(password.getBytes());

        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);

        if(emp==null)
            return R.error("用户不存在");

        if(!password.equals(emp.getPassword()))
            return R.error("密码错误");

        if(emp.getStatus() == 0)
            return R.error("该账号已禁用");

        //登录成功，将员工Id存入Session并返回登录成功结果
        request.getSession().setAttribute("employee",emp.getId());
        return R.success(emp);
    }

    // 退出登录
    @Operation(summary = "员工退出登录", description = "员工退出当前登录状态")
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        log.info("用户已退出登录");
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    //添加员工
    @Operation(summary = "新增员工", description = "添加新员工信息到系统")
    @PostMapping//如果新增员工时该账号已存在，数据库中会抛出异常
    public R<String> save(@Parameter(description = "包含员工信息的对象", required = true) @RequestBody Employee employee){
        log.info("新增员工，员工信息：{}",employee.toString());
        //设置员工初始密码123456，需要进行md5加密处理
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        //设置创建时间和更新时间，有了公共字段更新后就没用了
//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());
        //设置创建人和更新人，有了公共字段更新后就没用了
//        long empid = (long)request.getSession().getAttribute("employee");
//        employee.setCreateUser(empid);
//        employee.setUpdateUser(empid);
        employeeService.save(employee);
        return R.success("新增员工成功");
    }

    /**
     * 分页查询方法
     * @param page  页数
     * @param pageSize 页大小
     * @param name 要查询员工的名字，可为空
     * @return R<Page>
     */
    @GetMapping("/page")
    @Operation(summary = "员工分页查询", description = "分页查询员工列表，支持按姓名搜索")
    public R<Page> page(@Parameter(description = "页码，从1开始", required = true, example = "1") int page,
                        @Parameter(description = "每页显示条数", required = true, example = "10")int pageSize,
                        @Parameter(description = "员工姓名，可选，支持模糊查询，为空时查询全部", example = "张") String name){
        log.info("正在分页显示... page={}, pageSize={}, name={}", page, pageSize, name);

        //构造分页构造器
        Page pageInfo = new Page(page, pageSize);

        //构造条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件
        queryWrapper.like(StringUtils.isNotEmpty(name),Employee::getName,name);
        //添加排序条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);

        //执行查询
        employeeService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }

    //根据id启用、禁用、编辑员工账号
    @Operation(summary = "更新员工信息", description = "修改员工信息，包括启用/禁用账号")
    @PutMapping
    public R<String> update(@Parameter(description = "员工信息，包含id和需要更新的字段（如status、name等）", required = true,
            schema = @Schema(example = "{\"id\":1,\"status\":1,\"name\":\"张三\"}")) @RequestBody Employee employee){
        log.info("更新员工信息 :{}",employee.toString());
//        employee.setUpdateTime(LocalDateTime.now());
//        //js只能处理long型的前16位数据，会丢失精度，导致提交的id和数据库中的id不一致
//        long empid = (long)request.getSession().getAttribute("employee");
//        employee.setUpdateUser(empid);
        employeeService.updateById(employee);
        return R.success("员工信息更新成功");
    }

    //编辑员工信息前的查询
    @Operation(summary = "根据ID查询员工", description = "用于编辑员工前获取员工详情")
    @GetMapping("/{id}")
    public R<Employee> getById(@Parameter(description = "员工ID", required = true) @PathVariable("id") Long id){
        log.info("根据id查询员工信息, getById={}", id);
        Employee employee = employeeService.getById(id);
        return (employee != null) ? R.success(employee):R.error("没有查询到该员工信息");
    }
}
