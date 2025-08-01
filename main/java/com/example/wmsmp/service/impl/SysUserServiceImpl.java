package com.example.wmsmp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.wmsmp.entity.system.SysUser;
import com.example.wmsmp.service.SysUserService;
import com.example.wmsmp.mapper.SysUserMapper;
import org.springframework.stereotype.Service;

/**
* @author xhc
* @description 针对表【sys_users】的数据库操作Service实现
* @createDate 2023-04-20 17:15:37
*/
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser>
    implements SysUserService{

}




