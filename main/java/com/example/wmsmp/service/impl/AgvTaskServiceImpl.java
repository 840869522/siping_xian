package com.example.wmsmp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.wmsmp.entity.task.AGV.AgvTask;
import com.example.wmsmp.service.AgvTaskService;
import com.example.wmsmp.mapper.AgvTaskMapper;
import org.springframework.stereotype.Service;

/**
* @author win
* @description 针对表【t_task_agv】的数据库操作Service实现
* @createDate 2023-09-08 10:26:41
*/
@Service
public class AgvTaskServiceImpl extends ServiceImpl<AgvTaskMapper, AgvTask>
    implements AgvTaskService{

}




