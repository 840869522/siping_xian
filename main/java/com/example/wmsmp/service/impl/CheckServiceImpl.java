package com.example.wmsmp.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.wmsmp.entity.CheckBean;
import com.example.wmsmp.entity.CheckDetailBean;
import com.example.wmsmp.entity.system.InterReturn;
import com.example.wmsmp.entity.system.ResReturn;
import com.example.wmsmp.mapper.CheckDetailMapper;
import com.example.wmsmp.mapper.CheckMapper;
import com.example.wmsmp.service.CheckService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author 王力勋
 * @description 针对表【 】的数据库操作Service实现
 * @createDate
 */
@Service
public class CheckServiceImpl extends ServiceImpl<CheckMapper, CheckBean> implements CheckService {
    @Resource
    private CheckDetailMapper checkDetailMapper;


    @Override
    public InterReturn queryPage(CheckBean check, int pageNo, int pageSize) {
        //创建回调
        InterReturn interReturn = new InterReturn();
        LambdaQueryWrapper<CheckBean> wrapper = new LambdaQueryWrapper<>();
        // 模糊查询
        wrapper.like(StrUtil.isNotEmpty(check.getCheckCode()), CheckBean::getCheckCode, check.getCheckCode());
        wrapper.eq(StrUtil.isNotEmpty(check.getWarehouseName()), CheckBean::getWarehouseName, check.getWarehouseName());
        Page<CheckBean> page = this.page(new Page<>(pageNo, pageSize), wrapper);
        ResReturn resReturn = new ResReturn();
        //查询列表
        List<CheckBean> queryList = page.getRecords();
//        for (CheckBean checkBean : queryList) {
//            switch (checkBean.getStatus()) {
//                case 0:
//                    checkBean.setStatusDescribe("已创建");
//                    break;
//                case 1:
//                    checkBean.setStatusDescribe("盘点中");
//                    break;
//                case 2:
//                    checkBean.setStatusDescribe("已完成");
//                    break;
//            }
//        }
        if (!queryList.isEmpty()) {
            interReturn.setStatus(true);
            interReturn.setMessage("查询成功！");
        } else {
            interReturn.setStatus(false);
            interReturn.setMessage("未查询到数据！");
        }
        resReturn.setTotalCount((int) page.getTotal());//总条数赋值
        resReturn.setTotalPage((int) page.getPages());//总页数赋值
        resReturn.setAnything(queryList);
        interReturn.setResult(resReturn);
        return interReturn;
    }

    @Override
    @Transactional
    public InterReturn checkUpdate(CheckBean check) {
        InterReturn interReturn = new InterReturn();
        check.setUpdateTime(DateUtil.now());
        //盘点单完成需确认明细表中所有数据是否都完成
        if (check.getStatus().equals("已完成")) {
            Long num = checkDetailMapper.queryCount(check);
            if (num > 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("盘点明细未全部完成,请完成后再结束盘底！");
                return interReturn;
            }
        }
        //盘点开始后修改盘点明细状态
        if (check.getStatus().equals("已完成")) {
            CheckDetailBean detailBean = new CheckDetailBean();
            detailBean.setCheckCode(check.getCheckCode());
            detailBean.setStatus(check.getStatus());
            Long num = checkDetailMapper.update4Check(detailBean);
        }
        boolean update = this.updateById(check);
        if (update) {
            interReturn.setStatus(true);
            interReturn.setMessage("流程变更成功！");
        } else {
            interReturn.setStatus(false);
            interReturn.setMessage("流程变更失败！");
        }
        return interReturn;
    }

}




