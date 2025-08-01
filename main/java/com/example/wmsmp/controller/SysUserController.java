package com.example.wmsmp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.wmsmp.entity.system.InterReturn;
import com.example.wmsmp.entity.system.ResReturn;
import com.example.wmsmp.entity.system.SysDictionary;
import com.example.wmsmp.entity.system.SysUser;
import com.example.wmsmp.mapper.SysUserMapper;
import com.example.wmsmp.service.SysDictionaryService;
import com.example.wmsmp.service.SysUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.ibatis.session.RowBounds;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Api(value = "123", tags = "用户")
@RestController
@CrossOrigin
public class SysUserController {

    @Autowired
    SysUserService sysUserService;

    @Resource
    SysUserMapper sysUserMapper;

    @Autowired
    SysDictionaryService sysDictionaryService;

    @Autowired
    private Environment environment;//用于读取配置文件

    private static Logger logger = Logger.getLogger(SysUserController.class);

    @ApiOperation(value = "当前后端程序版本（无参）", notes = "用于测试接口可用")
    @PostMapping("/User/AAATestHello")
    @CrossOrigin
    public String AAATestHello() {
        Date date = new Date();


        String URL = environment.getProperty("version.version");
        String URLdatasource = environment.getProperty("spring.datasource.url");
        String str = "后端程序版本：" + URL +
                "\n数据库地址：" + URLdatasource +
                "\n服务器时间：" + new SimpleDateFormat("yyyy年MM月dd日 hh:mm:ss EE").format(date);
        logger.info(str);
        return str;
    }

    @ApiOperation(value = "条件查询用户", notes = "sysUser类 pageNo pageSize")
    @PostMapping("/User/GetAllSysUserBy")
    @CrossOrigin
    public InterReturn GetAllSysUserBy(@RequestBody SysUser sysUser, int pageNo, int pageSize) {
        logger.info("进入 GetAllSysUserBy");
        logger.info("接收到参数：" + sysUser);
        InterReturn interReturn = new InterReturn();
        ResReturn resReturn = new ResReturn();
        try {
            resReturn.setPageNo(pageNo);//页码赋值
            resReturn.setPageSize(pageSize);//页大小赋值
            List<SysUser> usersAll = sysUserMapper.searchBy(sysUser);
            List<SysUser> users = sysUserMapper.searchBy(sysUser, new RowBounds((pageNo - 1) * pageSize, pageSize));
            if (users == null || users.size() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("未找到此用户！");
            } else {
                interReturn.setStatus(true);
                interReturn.setMessage("OK");
            }

            int totalCount = usersAll.size();
            resReturn.setTotalCount(totalCount);//总条数赋值
            int totalPageInt = totalCount / pageSize;//总页数的整数部分
            int totalPage = totalCount % pageSize == 0 ? totalPageInt : totalPageInt + 1;//总页数

            resReturn.setTotalPage(totalPage);//总页数赋值
            resReturn.setAnything(users);
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("查询出错：" + e.getCause());
            System.out.println("查询出错：" + e.getMessage());
            logger.error("查询出错：" + e.getMessage());
        }
        interReturn.setResult(resReturn);//装入
        logger.info("GetAllSysUserBy 返回：" + interReturn);
        return interReturn;
    }

    /**
     * @Author:lcy
     * @Description: 添加库类型字段，进行库类型判断，用户登录时选择库类型不对，给出提示信息
     * @DateTime: 2023/4/28 9:33
     */
    @ApiOperation(value = "登录", notes = "login_name和password,storeClassifyValue库类型值")
    @PostMapping("/User/Login")
    @CrossOrigin
    public InterReturn Login(@RequestBody SysUser sysUser, int storeClassifyValue) {
        logger.info("进入 Login");
        logger.info("接收到参数：" + sysUser);
        InterReturn interReturn = new InterReturn();
        ResReturn resReturn = new ResReturn();
        try {
            //mybatisplus
            if (storeClassifyValue == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("请选择所属库！");
                return interReturn;
            } else {
//                LambdaQueryWrapper<SysUser> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
//                userLambdaQueryWrapper.eq(SysUser::getLogin_name, sysUser.getLogin_name());
//                userLambdaQueryWrapper.eq(SysUser::getPassword, sysUser.getPassword());
//                List<SysUser> users = sysUserService.list(userLambdaQueryWrapper);

                List<SysUser> users = sysUserMapper.searchBy(sysUser);

                if (users == null) {
                    interReturn.setStatus(false);
                    interReturn.setMessage("登录查询用户出错结果为null");
                } else if (users.size() == 0) {
                    interReturn.setStatus(false);
                    interReturn.setMessage("登录失败！请检查账号和密码");
                } else if (users.get(0).getStatus() == 2) {//2为禁用
                    interReturn.setStatus(false);
                    interReturn.setMessage("登录失败！账号已禁用");
                } else {
                    //特殊处理超级管理员
                    if (users.get(0).getRole_name().equals("超级管理员")) {
                        interReturn.setStatus(true);
                        interReturn.setMessage("登录成功！");
                        resReturn.setAnything(users);
                    } else {
                        //用库类型值查找显示值
                        LambdaQueryWrapper<SysDictionary> wrapper = new LambdaQueryWrapper<>();
                        wrapper.eq(SysDictionary::getDic_value, storeClassifyValue);
                        wrapper.eq(SysDictionary::getDic_name, "store_classify");
                        List<SysDictionary> dics = sysDictionaryService.list(wrapper);
                        String storeClassify = dics.get(0).getDic_display_value();
                        if (users.get(0).getRole_name().contains(storeClassify)) {
                            interReturn.setStatus(true);
                            interReturn.setMessage("登录成功！");
                            resReturn.setAnything(users);
                        } else {
                            interReturn.setStatus(false);
                            interReturn.setMessage("该用户不属于该库型，请重新选择库型");
                        }
                    }
                }
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("登录出错：" + e.getCause());
            logger.error("登录出错：" + e.getMessage());
        }
        interReturn.setResult(resReturn);//装入
//        logger.info("Login 返回：" + interReturn);
        return interReturn;
    }


    @ApiOperation(value = "添加用户", notes = "sysUser类")
    @PostMapping("/User/AddSysUser")
    @CrossOrigin
    public InterReturn AddSysUser(@RequestBody SysUser sysUser) {
        logger.info("进入 AddSysUser");
        logger.info("接收到参数：" + sysUser);
        InterReturn interReturn = new InterReturn();
        try {
            if (sysUser.getLogin_name() == null || sysUser.getLogin_name().length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("参数异常或登录名为空");
                System.out.println("参数异常或登录名为空");
                return interReturn;
            } else if (sysUser.getCreator() == null || sysUser.getCreator().length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("参数异常或创建人为空");
                System.out.println("参数异常或创建人为空");
                return interReturn;
            } else {
//                sdf.format(Calendar.getInstance().getTime())


                //sysUser.setCreate_time(new Date());//创建时间赋值
                //mybatisplus
                //    boolean boolSave = sysUserService.save(sysUser);

                //mybatis
                int count = sysUserMapper.insert(sysUser);


                if (count > 0) {
                    interReturn.setStatus(true);
                    interReturn.setMessage("OK");
                } else {
                    interReturn.setStatus(false);
                    interReturn.setMessage("添加数据条数为0");
                }
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("新增出错：" + e.getCause());
            System.out.println("新增出错：" + e.getMessage());
            logger.error("新增出错：" + e.getMessage());
        }
        logger.info("AddSysUser 返回：" + interReturn);
        return interReturn;
    }

    @ApiOperation(value = "删除用户", notes = "登录名login_name")
    @PostMapping("/User/DelSysUserByLoginName")
    @CrossOrigin
    public InterReturn DelSysUserByLoginName(@RequestBody SysUser sysUser) {
        logger.info("进入 DelSysUserByLoginName");
        logger.info("接收到参数：" + sysUser);
        InterReturn interReturn = new InterReturn();
        try {
            if (sysUser.getLogin_name() == null || sysUser.getLogin_name().length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("参数异常或登录名为空");
                System.out.println("参数异常或登录名为空");
                return interReturn;
            } else {
                //mybatisplus
                boolean bool = sysUserService.removeById(sysUser);
                if (bool) {
                    interReturn.setStatus(true);
                    interReturn.setMessage("删除成功！");
                } else {
                    interReturn.setStatus(false);
                    interReturn.setMessage("删除数据条数为0");
                }
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("删除出错：" + e.getCause());
            System.out.println("删除出错：" + e.getMessage());
            logger.error("删除出错：" + e.getMessage());
        }
        logger.info("DelSysUserByLoginName 返回：" + interReturn);
        return interReturn;
    }

    @ApiOperation(value = "更新用户", notes = "根据login_name更新用户 传sysUser类")
    @PostMapping("/User/UpdateSysUserByLoginName")
    @CrossOrigin
    public InterReturn UpdateSysUserByLoginName(@RequestBody SysUser sysUser) {
        logger.info("进入 UpdateSysUserByLoginName");
        logger.info("接收到参数：" + sysUser);
        InterReturn interReturn = new InterReturn();
        try {
            if (sysUser.getLogin_name() == null || sysUser.getLogin_name().length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("参数异常或登录名为空");
                System.out.println("参数异常或登录名为空");
                return interReturn;
            }
            if (sysUser.getUpdater() == null || sysUser.getUpdater().length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("参数异常或更新人为空");
                System.out.println("参数异常或更新人为空");
                return interReturn;
            }
            //mybatisplus
            LambdaUpdateWrapper<SysUser> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(SysUser::getLogin_name, sysUser.getLogin_name());
            wrapper.set(sysUser.getUser_name() != null && !sysUser.getUser_name().isEmpty(), SysUser::getUser_name, sysUser.getUser_name());
            wrapper.set(sysUser.getPassword() != null && !sysUser.getPassword().isEmpty(), SysUser::getPassword, sysUser.getPassword());
            wrapper.set(sysUser.getRole_name() != null && !sysUser.getRole_name().isEmpty(), SysUser::getRole_name, sysUser.getRole_name());
            wrapper.set(sysUser.getStatus() != null, SysUser::getStatus, sysUser.getStatus());
            wrapper.set(sysUser.getRemark() != null && !sysUser.getRemark().isEmpty(), SysUser::getRemark, sysUser.getRemark());
            wrapper.set(SysUser::getUpdater, sysUser.getUpdater());
            wrapper.set(SysUser::getUpdate_time, LocalDateTime.now());
            boolean bool = sysUserService.update(null, wrapper);

            if (bool) {
                interReturn.setStatus(true);
                interReturn.setMessage("OK");
            } else {
                interReturn.setStatus(false);
                interReturn.setMessage("无更新");
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("更新出错：" + e.getCause());
            System.out.println("更新出错：" + e.getCause());
            logger.error("更新出错：" + e.getMessage());
        }
        logger.info("UpdateSysUserByLoginName 返回：" + interReturn);
        return interReturn;
    }
}
