package com.example.wmsmp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.wmsmp.entity.system.*;
import com.example.wmsmp.mapper.SysRoleMapper;
import com.example.wmsmp.service.SysDictionaryService;
import com.example.wmsmp.service.SysRoleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Api(value = "123", tags = "权限")
@RestController
@CrossOrigin
public class SysRoleController {

    @Autowired
    SysRoleService sysRoleService;

    @Resource
    SysRoleMapper sysRoleMapper;
    @Autowired
    SysDictionaryService sysDictionaryService;

    @Autowired
    protected SqlSessionFactory sqlSessionFactory;

    private static Logger logger = Logger.getLogger(SysRoleController.class);
    String superRoleName = "超级管理员";


    @ApiOperation(value = "获取角色名称（去重）", notes = "无参数")
    @PostMapping("/Role/GetAllRoleName")
    @CrossOrigin
    public InterReturn GetAllRoleName() {
        logger.info("进入 GetAllRoleName");
        logger.info("接收到参数：无参");
        InterReturn interReturn = new InterReturn();
        ResReturn resReturn = new ResReturn();
        try {
            LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByAsc(SysRole::getRole_id);//按id递增
            List<SysRole> roles = sysRoleService.list(wrapper);
            roles = roles.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(SysRole::getRole_name))), ArrayList::new));
            interReturn.setStatus(true);
            interReturn.setMessage("获取到角色名称！");
            resReturn.setAnything(roles);
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("查询出错：" + e.getCause());
            System.out.println("查询出错：" + e.getMessage());
            logger.error("查询出错：" + e.getMessage());
        }
        interReturn.setResult(resReturn);
        logger.info("GetAllRoleName 返回:" + interReturn);
        return interReturn;
    }

    @ApiOperation(value = "条件查询权限", notes = "sysRole类")
    @PostMapping("/Role/GetAllSysRoleBy")
    @CrossOrigin
    public InterReturn GetAllSysRoleBy(@RequestBody SysRole sysRole) {
//        logger.info("进入 GetAllSysRoleBy");
//        logger.info("接收到参数：" + sysRole);
        InterReturn interReturn = new InterReturn();
        ResReturn resReturn = new ResReturn();
        try {
            List<SysRole> roles = sysRoleMapper.searchBy(sysRole);//查询


            //表→树
            for (SysRole sysRoleOldA : roles) {
                List<SysRole> rolesChildren = new ArrayList<>();
                for (SysRole sysRoleOldB : roles) {
                    if (sysRoleOldA.getRole_id().equals(sysRoleOldB.getParent_id()))//父亲id由int改为Integer 判等==改为equals
                        rolesChildren.add(sysRoleOldB);
                }
                sysRoleOldA.setChildren(rolesChildren);
            }
            {
                //迭代器
//                Iterator<SysRole> iteratorSysRole = roles.iterator();
//                while (iteratorSysRole.hasNext()) {
//                    SysRole sysRoleOldMap = iteratorSysRole.next();
//                    int parentId = sysRoleOldMap.getParent_id();
//                    if (parentId != 0)
//                        iteratorSysRole.remove();//删除父节点不为空的节点（删子节点）
//                }

                //删除父节点不为空的节点（删子节点）
                for (int i = roles.size() - 1; i >= 0; i--) {
                    int parentId = roles.get(i).getParent_id();
                    if (parentId != 0) roles.remove(i);//删除父节点不为空的节点（删子节点）
                }
            }

            //resReturn赋值
            resReturn.setAnything(roles);
            if (roles.size() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("未找到此角色权限！");
            } else {
                interReturn.setStatus(true);
                interReturn.setMessage("OK");
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("查询出错：" + e.getCause());
            System.out.println("查询出错：" + e.getMessage());
            logger.error("查询出错：" + e.getMessage());
        }
        interReturn.setResult(resReturn);//装入
//        logger.info("GetAllSysRoleBy 返回：" + interReturn);
        return interReturn;
    }

    @ApiOperation(value = "登录成功后根据role_name条件查询菜单", notes = "sysRole类和storeClassifyValue库型值")
    @PostMapping("/Role/GetSysRoleByRoleName")
    @CrossOrigin
    public InterReturn GetSysRoleByRoleName(@RequestBody SysRole sysRole, int storeClassifyValue) {
//        logger.info("进入 GetSysRoleByRoleName");
//        logger.info("接收到参数：" + sysRole);
        InterReturn interReturn = new InterReturn();
        ResReturn resReturn = new ResReturn();
        String roleName = "";
        try {
            if (storeClassifyValue == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("未传递库型值！");
                return interReturn;
            } else {
                if (sysRole.getRole_name().equals(superRoleName)) {
                    roleName = sysRole.getRole_name();
                } else {
                    //用库类型值查找显示值
                    LambdaQueryWrapper<SysDictionary> wrapperStore = new LambdaQueryWrapper<>();
                    wrapperStore.eq(SysDictionary::getDic_value, storeClassifyValue);
                    wrapperStore.eq(SysDictionary::getDic_name, "store_classify");
                    List<SysDictionary> dics = sysDictionaryService.list(wrapperStore);
                    String storeClassify = dics.get(0).getDic_display_value();
                    //查询出字典里库型值对应的库型
                    String intRoleName = sysRole.getRole_name();
                    //查找出库型对应的role_name
                    String[] resRoleName = intRoleName.split(",");
                    for (String s : resRoleName) {
                        if (storeClassify.equals(s.substring(0, 2))) {
                            roleName = s;
                        }
                    }
                }
                LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
                wrapper.orderByAsc(SysRole::getRole_id);//按id递增
                wrapper.eq(SysRole::getRole_name, roleName);
                List<SysRole> roles = sysRoleService.list(wrapper);

                //表→树
                for (SysRole sysRoleOldA : roles) {
                    List<SysRole> rolesChildren = new ArrayList<>();
                    for (SysRole sysRoleOldB : roles) {
                        if (sysRoleOldA.getRole_id().equals(sysRoleOldB.getParent_id()))//父亲id由int改为Integer 判等==改为equals
                            rolesChildren.add(sysRoleOldB);
                    }
                    sysRoleOldA.setChildren(rolesChildren);
                }
                {
                    //迭代器
//                Iterator<SysRole> iteratorSysRole = roles.iterator();
//                while (iteratorSysRole.hasNext()) {
//                    SysRole sysRoleOldMap = iteratorSysRole.next();
//                    int parentId = sysRoleOldMap.getParent_id();
//                    if (parentId != 0)
//                        iteratorSysRole.remove();//删除父节点不为空的节点（删子节点）
//                }

                    //删除父节点不为空的节点（删子节点）
                    for (int i = roles.size() - 1; i >= 0; i--) {
                        int parentId = roles.get(i).getParent_id();
                        if (parentId != 0) roles.remove(i);//删除父节点不为空的节点（删子节点）
                    }
                }

                resReturn.setAnything(roles);
                if (roles.size() == 0) {
                    interReturn.setStatus(false);
                    interReturn.setMessage("未找到此角色权限！");
                } else {
                    interReturn.setStatus(true);
                    interReturn.setMessage("OK");
                }
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("查询出错：" + e.getCause());
            System.out.println("查询出错：" + e.getMessage());
            logger.error("查询出错：" + e.getMessage());
        }
        interReturn.setResult(resReturn);//装入
//        logger.info("GetSysRoleByRoleName 返回：" + interReturn);
        return interReturn;
    }

    @ApiOperation(value = "获取库型名称", notes = "sysRole类")
    @PostMapping("/Role/GetStoreName")
    @CrossOrigin
    public InterReturn GetStoreName(@RequestBody SysRole sysRole) {
//        logger.info("进入 GetStoreName");
//        logger.info("接收到参数：" + sysRole);
        InterReturn interReturn = new InterReturn();
        ResReturn resReturn = new ResReturn();
        try {
            List<SysDictionary> storeName = new ArrayList<>();
            //查询出字典里库型值对应的库型
            String intRoleName = sysRole.getRole_name();
            String[] tempRoleName = intRoleName.split(",");
            List<String> resRoleName = Arrays.asList(tempRoleName);
            if (intRoleName.equals(superRoleName)) {
                LambdaQueryWrapper<SysDictionary> tempwrapperStore = new LambdaQueryWrapper<>();
                tempwrapperStore.eq(SysDictionary::getDic_name, "store_classify");
                storeName = sysDictionaryService.list(tempwrapperStore);
            } else {
                for (int i = 0; i < resRoleName.size(); i++) {
                    tempRoleName[i] = tempRoleName[i].substring(0, 2);
                    LambdaQueryWrapper<SysDictionary> wrapperStore = new LambdaQueryWrapper<>();
                    wrapperStore.clear();
                    wrapperStore.eq(SysDictionary::getDic_display_value, tempRoleName[i]);
                    wrapperStore.eq(SysDictionary::getDic_name, "store_classify");
                    List<SysDictionary> tempStoreName = sysDictionaryService.list(wrapperStore);
                    storeName.add(tempStoreName.get(0));
                }
            }
            resReturn.setAnything(storeName);
            interReturn.setStatus(true);
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("查询出错：" + e.getCause());
            System.out.println("查询出错：" + e.getMessage());
            logger.error("查询出错：" + e.getMessage());
        }
        interReturn.setResult(resReturn);//装入
        //logger.info("GetStoreName 返回：" + interReturn);
        return interReturn;
    }


    @ApiOperation(value = "更新权限", notes = "sysRole类")
    @PostMapping("/Role/UpdateSysRole")
    @CrossOrigin
    public InterReturn UpdateSysRole(@RequestBody SysRole sysRole) {
        logger.info("进入 UpdateSysRole");
        logger.info("接收到参数：" + sysRole);
        InterReturn interReturn = new InterReturn();
        try {
            if (sysRole.getRole_id() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("参数异常或role_id为空");
                System.out.println("参数异常或role_id为空");
                return interReturn;
            } else {
                // int count = sysRoleMapper.updateByPrimaryKeySelective(sysRole);
                //mybatisplus
                LambdaUpdateWrapper<SysRole> wrapper = new LambdaUpdateWrapper<>();
                wrapper.eq(SysRole::getRole_id, sysRole.getRole_id());
                wrapper.set(sysRole.getRole_name() != null && !sysRole.getRole_name().isEmpty(), SysRole::getRole_name, sysRole.getRole_name());
                wrapper.set(sysRole.getTitle() != null && !sysRole.getTitle().isEmpty(), SysRole::getTitle, sysRole.getTitle());
                wrapper.set(sysRole.getComponent_id() != null && !sysRole.getComponent_id().isEmpty(), SysRole::getComponent_id, sysRole.getComponent_id());
                wrapper.set(sysRole.getComponent() != null && !sysRole.getComponent().isEmpty(), SysRole::getComponent, sysRole.getComponent());
                wrapper.set(sysRole.getComponent_name() != null && !sysRole.getComponent_name().isEmpty(), SysRole::getComponent_name, sysRole.getComponent_name());
                wrapper.set(sysRole.getIcon() != null && !sysRole.getIcon().isEmpty(), SysRole::getIcon, sysRole.getIcon());
                wrapper.set(sysRole.getParent_id() != 0, SysRole::getParent_id, sysRole.getParent_id());
                wrapper.set(sysRole.getStatus() != 0, SysRole::getStatus, sysRole.getStatus());
                wrapper.set(sysRole.getRemark() != null && !sysRole.getRemark().isEmpty(), SysRole::getRemark, sysRole.getRemark());
                wrapper.set(SysRole::getUpdater, sysRole.getUpdater());
                wrapper.set(SysRole::getUpdate_time, LocalDateTime.now());
                boolean bool = sysRoleService.update(null, wrapper);
                if (bool) {
                    interReturn.setStatus(true);
                    interReturn.setMessage("更新成功");
                } else {
                    interReturn.setStatus(false);
                    interReturn.setMessage("无更新");
                }
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("更新出错：" + e.getCause());
            System.out.println("更新出错：" + e.getMessage());
            logger.error("更新出错：" + e.getMessage());
        }
        logger.info("UpdateSysRole 返回：" + interReturn);
        return interReturn;
    }

    @ApiOperation(value = "根据界面选择添加权限菜单", notes = "checkedKeysAdd[]为component_id集合," + "role_name角色名称,storage_name[]库型值集合")
    @PostMapping("/Role/AddSysRoleInfo")
    @CrossOrigin
    public InterReturn AddSysRoleInfo(@RequestBody String[] checkedKeysAdd, String role_name, String[] storage_name) {
//        logger.info("进入 AddSysRoleInfo");
//        logger.info("接收到参数" + checkedKeysAdd + role_name + storage_name);
        InterReturn interReturn = new InterReturn();
        String roleName;

        try {
            //storage_name[]为库型值，通过库型值查找字典找出库型名称，循环storage_name数组，进行多个库型角色生成权限菜单
            //先循环库型值数组，取出库型与角色组合，判断用户输入的库型角色名时否在数据库中存在，只要有一个在里面存在则跳出，不执行，返回给前端重新输入
            if (existRoleName(role_name, storage_name)) {
                for (String s : storage_name) {
                    //查找字典，找出库型名称
                    LambdaQueryWrapper<SysDictionary> wrapperdic = new LambdaQueryWrapper<>();
                    wrapperdic.eq(SysDictionary::getDic_value, s);
                    wrapperdic.eq(SysDictionary::getDic_name, "store_classify");
                    List<SysDictionary> dics = sysDictionaryService.list(wrapperdic);
                    String storageName = dics.get(0).getDic_display_value();
                    //取出角色名称和库型名字段组成最终角色名称
                    roleName = storageName + role_name;

                    LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
                    wrapper.orderByAsc(SysRole::getRole_id);
                    //数据库表里所有的菜单记录
                    List<SysRole> roles = sysRoleService.list(wrapper);
                    //查询出超级管理员的菜单，超级管理员菜单基础全部菜单
                    List<SysRole> superRoles = roles.stream().filter(w -> w.getRole_name().equals(superRoleName)).collect(Collectors.toList());
                    List<SysRole> componentIdAll = new ArrayList<>();

                    //循环取出超级管理员中包含checkedKeysAdd[]的元素的列表
                    for (int i = 0; i < checkedKeysAdd.length; i++) {
                        int tempNum = i;
                        //取出角色名为超级管理员中给，component_id等于所给参数的记录，依次添加
                        List<SysRole> cmponentId = superRoles.stream().filter(w -> w.getComponent_id().equals(checkedKeysAdd[tempNum])).collect(Collectors.toList());
                        componentIdAll.add(cmponentId.get(0));
                    }
                    //取出除了超级管理员的所有记录
                    List<SysRole> roleList = roles.stream().filter(w -> !superRoleName.equals(w.getRole_name())).collect(Collectors.toList());
                    int temproleId;
                    SysRole roleId = null;
                    //如果只是管理员的话默认ID是从200000开始，不然找不到ID报错
                    if (roleList.size() == 0) {
                        temproleId = 200000;
                    } else {
                        //取出除了超级管理员的所有记录的最大的的roleId
                        roleId = roleList.stream().max(Comparator.comparing(SysRole::getRole_id)).get();
                        temproleId = roleId.getRole_id();
                        //取出最大的id
                    }
                    int oneBit = temproleId / 100000 % 10; //最大id的第一位
                    //9为超级管理员，9之前的才可以分配
                    if (oneBit < 9) {
                        //新的role_id为第二位加上10000，role_id序号的自增时通过六位数的前两位来做运算
                        temproleId = temproleId + 10000;
                        //提出一个方法，后面编辑权限需要复用
                        componentIdAll = getModifyroleId(componentIdAll, temproleId, roleName);
                    } else {
                        interReturn.setStatus(false);
                        interReturn.setMessage("超出最大权限配置值!");
                    }
                    //批量插入提出一个方法，后面编辑权限需要复用
                    if (BatchInsert(componentIdAll)) {
                        interReturn.setStatus(true);
                        interReturn.setMessage("生成成功！");
                    } else {
                        interReturn.setStatus(false);
                        interReturn.setMessage("批量插入异常！");
                    }
                }
            } else {
                interReturn.setStatus(false);
                interReturn.setMessage("数据库里已经存在该角色名,请重新填写!");
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("添加出错：" + e.getCause());
            System.out.println("添加出错：" + e.getMessage());
            logger.error("添加出错：" + e.getMessage());
        }
//        logger.info("AddSysRoleInfo 返回:" + interReturn);
        return interReturn;
    }

    @ApiOperation(value = "根据角色名称查询权限菜单", notes = "无参数")
    @PostMapping("/Role/GetMenuBysysRoleName")
    @CrossOrigin
    public InterReturn GetMenuBysysRoleName(String role_name) {
//        logger.info("进入 GetMenuBysysRoleName");
//        logger.info("接收到参数：" + role_name);
        InterReturn interReturn = new InterReturn();
        ResReturn resReturn = new ResReturn();
        try {
            LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByAsc(SysRole::getRole_id);//按id递增
            wrapper.eq(SysRole::getRole_name, role_name);
            List<SysRole> roles = sysRoleService.list(wrapper);
            interReturn.setStatus(true);
            interReturn.setMessage("获取角色权限菜单！");
            resReturn.setAnything(roles);
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("查询出错：" + e.getCause());
            System.out.println("查询出错：" + e.getMessage());
            logger.error("查询出错：" + e.getMessage());
        }
        interReturn.setResult(resReturn);
//        logger.info("GetMenuBysysRoleName 返回:" + interReturn);
        return interReturn;
    }

    /**
     * @Author:lcy
     * @Description:单独方法用于用户输入的角色名称是否已经存在
     * @DateTime: 2023/5/11 11:47
     * @Params:
     */
    public boolean existRoleName(String roleName, String[] storage_name) {
        logger.info("进入 existRoleName");
        logger.info("接收到参数：" + roleName + storage_name);
        for (String s : storage_name) {

            String temproleName;
            //查找字典，找出库型名称
            LambdaQueryWrapper<SysDictionary> wrapperdic = new LambdaQueryWrapper<>();
            wrapperdic.eq(SysDictionary::getDic_value, s);
            wrapperdic.eq(SysDictionary::getDic_name, "store_classify");
            List<SysDictionary> dics = sysDictionaryService.list(wrapperdic);
            String storageName = dics.get(0).getDic_display_value();
            //取出角色名称和库型名字段组成最终角色名称
            temproleName = storageName + roleName;

            //查询出权限表的所有菜单
            LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByAsc(SysRole::getRole_id);//按id递增
            wrapper.eq(SysRole::getRole_name, temproleName);//按id递增
            List<SysRole> roles = sysRoleService.list(wrapper);
            if (roles.size() != 0) {
                return false;
            }
        }
        return true;
    }

    @ApiOperation(value = "编辑权限信息", notes = "checkedKeysAdd[][]为component_id旧、新集合," + "roleName角色名称")
    @PostMapping("/Role/ModifySysRoleInfo")
    @CrossOrigin
    public InterReturn ModifySysRoleInfo(@RequestBody String[][] checkedKeys, String role_name) {
//        logger.info("进入 ModifySysRoleInfo");
//        logger.info("接收到参数：" + checkedKeys + role_name);
        InterReturn interReturn = new InterReturn();
        List<SysRole> addComponentId = new ArrayList<>();
        List<SysRole> delcomponentId = new ArrayList<>();
        try {
            LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByAsc(SysRole::getRole_id);//按id递增
            // 数据库表里所有的菜单记录
            List<SysRole> roles = sysRoleService.list(wrapper);
            //获取旧的组件id集合
            List<String> oldList = Stream.of(checkedKeys[0]).collect(Collectors.toList());
            //获取新的组件id集合
            List<String> newList = Stream.of(checkedKeys[1]).collect(Collectors.toList());

            List<String> removeList = new ArrayList<>();
            for (String s : oldList) {
                if (newList.contains(s)) {
                    //操作完newList实际中要增加的元素
                    newList.remove(s);
                } else {
                    //操作完removeList为实际中要删除的元素
                    removeList.add(s);
                }
            }
            if (newList.equals(removeList)) {
                interReturn.setStatus(false);
                interReturn.setMessage("未更改权限请重新选择！");
            } else {
                //查询出超级管理员的菜单，超级管理员菜单为基础全部菜单
                List<SysRole> superRoles = roles.stream().filter(w -> w.getRole_name().equals(superRoleName)).collect(Collectors.toList());
                //循环取出超级管理员中包含newList的元素的列表
                for (String id : newList) {
                    List<SysRole> cmponentId = superRoles.stream().filter(w -> w.getComponent_id().equals(id)).collect(Collectors.toList());
                    addComponentId.add(cmponentId.get(0));
                }
                //查询出用户名为role_name记录,取出ID
                List<SysRole> roleList = roles.stream().filter(w -> w.getRole_name().equals(role_name)).collect(Collectors.toList());
                int temproleId = roleList.get(0).getRole_id();
                //调用替换方法，增加的菜单全部替换掉role_id和parent_id
                addComponentId = getModifyroleId(addComponentId, temproleId, role_name);
                //批量插入
                //删除的菜单，获取到记录
                for (String id : removeList) {
                    //取出角色名为该用户角色，component_id为removeList中元素的记录
                    List<SysRole> cmponentId = roles.stream().filter(w -> w.getComponent_id().equals(id) && w.getRole_name().equals(role_name)).collect(Collectors.toList());
                    delcomponentId.add(cmponentId.get(0));
                }
                //批量添加和批量删除同时成功时，才说明编辑权限成功
                if ((BatchDelete(delcomponentId)) && (BatchInsert(addComponentId))) {
                    interReturn.setStatus(true);
                    interReturn.setMessage("编辑成功！");
                } else {
                    interReturn.setStatus(false);
                    interReturn.setMessage("编辑失败！");
                }
            }

        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("编辑出错：" + e.getCause());
            System.out.println("编辑出错：" + e.getMessage());
            logger.error("编辑出错：" + e.getMessage());
        }
//        logger.info("ModifySysRoleInfo 返回:" + interReturn);
        return interReturn;
    }

    @ApiOperation(value = "通过角色名称编辑角色状态", notes = "无参数")
    @PostMapping("/Role/ModifySysRoleStatus")
    @CrossOrigin
    public InterReturn ModifySysRoleStatus(String role_name, int status) {
        logger.info("进入 ModifySysRoleStatus");
        logger.info("接收到参数：" + role_name + status);
        InterReturn interReturn = new InterReturn();
        try {
            LambdaUpdateWrapper<SysRole> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(SysRole::getRole_name, role_name);
            wrapper.set(SysRole::getStatus, status);
            wrapper.set(SysRole::getUpdate_time, LocalDateTime.now());
            boolean bool = sysRoleService.update(null, wrapper);
            if (bool) {
                interReturn.setStatus(true);
                interReturn.setMessage("编辑角色状态成功！");
            } else {
                interReturn.setStatus(false);
                interReturn.setMessage("编辑角色状态失败！");
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("编辑角色状态出错：" + e.getCause());
            System.out.println("编辑角色状态出错：" + e.getMessage());
            logger.error("编辑角色状态出错：" + e.getMessage());
        }
        logger.info("ModifySysRoleStatus 返回:" + interReturn);
        return interReturn;
    }

    public boolean BatchInsert(List<SysRole> componentIdAll) {
//        logger.info("进入BatchInsert");
//        logger.info("接收到参数：" + componentIdAll);
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);//获取sqlsession
        SysRoleMapper mapMapper = sqlSession.getMapper(SysRoleMapper.class);//获取对应Mapper
        Date time = Calendar.getInstance().getTime();//创建时间赋值

        for (SysRole sysRole : componentIdAll) {
            sysRole.setCreate_time(time);
            mapMapper.insert(sysRole);//准备执行sql
        }
        try {
            sqlSession.commit();//执行sql
            logger.info("批量添加成功！");
            return true;
        } catch (Exception e) {
            logger.error("批量插入异常，事务回滚", e);
            sqlSession.rollback();
        } finally {
            sqlSession.close();
        }
        return false;
    }

    /**
     * @Author:lcy
     * @Description: 批量删除记录
     * @DateTime: 2023/5/11 17:18
     * @Params:
     */
    public boolean BatchDelete(List<SysRole> componentIdAll) {
//        logger.info("进入BatchDelete");
//        logger.info("接收到参数：" + componentIdAll);
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);//获取sqlsession
        SysRoleMapper mapMapper = sqlSession.getMapper(SysRoleMapper.class);//获取对应Mapper
        //   Date time = Calendar.getInstance().getTime();//创建时间赋值

        for (SysRole sysRole : componentIdAll) {
            mapMapper.deleteById(sysRole);//准备执行sql
        }
        try {
            sqlSession.commit();//执行sql
            logger.info("批量删除成功！");
            return true;
        } catch (Exception e) {
            logger.error("批量删除异常，事务回滚", e);
            sqlSession.rollback();
        } finally {
            sqlSession.close();
        }
        return false;
    }

    /**
     * @Author:lcy
     * @Description: 算出替换role_id前两位后的权限菜单列
     * @DateTime: 2023/5/11 16:38
     * @Params:
     */
    public List<SysRole> getModifyroleId(List<SysRole> componentIdAll, int id, String roleName) {
        //取出算出的roleId前两位
        int roleIdTwoBit = Integer.parseInt(Integer.toString(id).substring(0, 2));
        //依次替换role_id和parent_id里的前两位
        for (SysRole sysRole : componentIdAll) {
            sysRole.setRole_id((roleIdTwoBit * 10000) + (sysRole.getRole_id() / 1000 % 10 * 1000) + ((sysRole.getRole_id() / 100 % 10) * 100) + ((sysRole.getRole_id() / 10 % 10) * 10) + (sysRole.getRole_id() % 10));
            //处理首页的parent_id
            if (sysRole.getParent_id() == 0) {
                sysRole.setParent_id(0);
            } else {
                sysRole.setParent_id((roleIdTwoBit * 10000) + (sysRole.getParent_id() / 1000 % 10 * 1000) + ((sysRole.getParent_id() / 100 % 10) * 100) + ((sysRole.getParent_id() / 10 % 10) * 10) + (sysRole.getParent_id() % 10));
            }
            sysRole.setRole_name(roleName);
        }
        return componentIdAll;
    }
}