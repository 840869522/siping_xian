package com.example.wmsmp.mapper;

import com.example.wmsmp.entity.OutOrder;
import com.example.wmsmp.entity.system.SysUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.session.RowBounds;

import java.util.List;

/**
* @author xhc
* @description 针对表【sys_users】的数据库操作Mapper
* @createDate 2023-04-20 17:15:37
* @Entity com.example.wmsmp.entity.system.SysUser
*/
public interface SysUserMapper extends BaseMapper<SysUser> {
    List<SysUser> search();

    List<SysUser> search(RowBounds rowBounds);

    List<SysUser> searchBy(SysUser sysUser);

    List<SysUser> searchBy(SysUser sysUser,RowBounds rowBounds);


    int insert(SysUser sysUser);

}




