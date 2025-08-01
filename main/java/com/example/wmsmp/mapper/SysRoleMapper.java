package com.example.wmsmp.mapper;

import com.example.wmsmp.entity.system.SysRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * @author xhc
 * @description 针对表【sys_role】的数据库操作Mapper
 * @createDate 2023-04-23 15:37:06
 * @Entity com.example.wmsmp.entity.system.SysRole
 */
public interface SysRoleMapper extends BaseMapper<SysRole> {

    List<SysRole> search();

    List<SysRole> searchBy(SysRole sysRole);
}




