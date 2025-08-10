# 瑞吉外卖
JavaWeb入门练手项目——瑞吉外卖管理系统是一款专为餐饮企业打造的全功能外卖平台解决方案，采用现代化前后端分离架构，支持餐厅员工管理、菜品管理、订单处理等核心功能，同时为用户提供便捷的在线点餐体验。

## ✨项目功能

### 🔧后端管理端

- 员工管理：员工信息增删改查、状态管理
- 分类管理：菜品 / 套餐分类维护
- 菜品管理：菜品信息维护、图片上传、状态管理
- 套餐管理：套餐组合与维护
- 订单管理：订单明细查询与处理

### 🍽️前端移动用户端

- 餐厅菜品浏览：按分类查看菜品
- 菜品详情：查看菜品描述、价格、图片
- 购物车：添加 / 删除菜品、修改数量
- 订单结算：提交订单、查看订单状态

## 🛠️技术栈

### 前端

- Vue.js：构建用户界面的渐进式框架
- Element UI：后端管理端 UI 组件库
- Vant：移动端 UI 组件库
- Axios：HTTP 请求库
- 原生 JavaScript/CSS：基础开发语言

### 后端

- Spring Boot：Java 后端开发框架
- MyBatis-Plus：ORM 框架
- MySQL：关系型数据库
- Redis：缓存数据库

## 📁项目结构

```plaintext
main/resources/
├── backend/                 # 后端管理端
│   ├── api/                 # 接口请求封装
│   ├── page/                # 页面文件
│   ├── plugins/             # 第三方插件
│   ├── styles/              # 样式文件
│   ├── js/                  # 请求处理工具脚本
│   └── index.html           # 后台管理端入口
├── front/                   # 前端用户端
│   ├── api/                 # 接口请求封装
│   ├── images/              # 图片资源
│   ├── js/                  # 请求处理工具脚本
│   ├── styles/              # 样式文件
│   └── index.html           # 移动端用户入口
└── application.yml          # 项目配置文件
```

## 📝安装与配置

1.克隆仓库到本地

bash

```bash
git clone https://github.com/bililateral/Reggie_Tackout.git
cd reggie_take_out\src\main\java\com\itheima\reggie
```

2.配置数据库

- 安装`mysql`数据库，创建新的数据库并修改数据库名为`reggie`
- 执行 `sql/ruiji.sql`脚本创建表结构

- 修改`application.yml`中的数据库连接信息

yaml

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/reggie?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull&useSSL=false&allowPublicKeyRetrieval=true
    username: 你的数据库用户名
    password: 你的数据库密码
```

3.运行`ReggieApplication`，启动后端服务

4.启动前端

- 可通过 `nginx` 部署或直接使用浏览器打开`front`目录下的`index.html`文件

**相关开发环境搭建也可以参考以下链接：**

[业务开发Day1-04-开发环境搭建_数据库环境搭建_哔哩哔哩_bilibili](https://www.bilibili.com/video/BV13a411q753?spm_id_from=333.788.videopod.episodes&vd_source=eab2f745842c13681dc22ade210ce78d&p=5)

## 📄使用说明

1. 管理端访问
   - 打开浏览器访问 http://localhost:8080/backend/index.html
   - 使用管理员账号登录(默认账号：admin，密码：123456)
   - 通过左侧菜单导航到各功能模块
2. 用户端访问
   - 打开浏览器访问 http://localhost:8080/front/index.html
   - 输入手机号和在控制输出的验证码完成登录
   - 浏览菜品并添加到购物车
   - 提交订单完成点餐
3. 方法接口访问
   - 打开浏览器访问 http://localhost:8080/swagger-ui/index.html

## 📞 联系我们

如有任何问题或建议，请联系：

- 邮箱：2190900472@qq.com
- 项目地址：https://github.com/bililateral/Reggie_Takeout

------

⭐️ 如果你觉得这个项目有帮助，请给我们一个星标！
