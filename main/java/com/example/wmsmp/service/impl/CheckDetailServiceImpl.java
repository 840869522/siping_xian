package com.example.wmsmp.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.wmsmp.entity.CheckBean;
import com.example.wmsmp.entity.CheckDetailBean;
import com.example.wmsmp.entity.base.BaseMaterial;
import com.example.wmsmp.entity.system.InterReturn;
import com.example.wmsmp.entity.system.ResReturn;
import com.example.wmsmp.mapper.BaseMaterialMapper;
import com.example.wmsmp.mapper.CheckDetailMapper;
import com.example.wmsmp.service.CheckDetailService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author 王力勋
 * @description 针对表【 】的数据库操作Service实现
 * @createDate
 */
@Service
public class CheckDetailServiceImpl extends ServiceImpl<CheckDetailMapper, CheckDetailBean> implements CheckDetailService {

    @Resource
    private CheckDetailMapper checkDetailMapper;

    @Resource
    private BaseMaterialMapper materialMapper;

    @Override
    public InterReturn queryPage(CheckDetailBean check, int pageNo, int pageSize) {
        //创建回调
        InterReturn interReturn = new InterReturn();
        Page<CheckDetailBean> page = new Page<>(pageNo, pageSize);
        ;
        if (StrUtil.isNotEmpty(check.getCheckCode())) {
            LambdaQueryWrapper<CheckDetailBean> wrapper = new LambdaQueryWrapper<>();
            // 模糊查询
            wrapper.like(StrUtil.isNotEmpty(check.getCheckCode()), CheckDetailBean::getCheckCode, check.getCheckCode());
            page = this.page(new Page<>(pageNo, pageSize), wrapper);
        }
        ResReturn resReturn = new ResReturn();
        //查询列表
        List<CheckDetailBean> queryList = page.getRecords();
//        for (CheckDetailBean checkBean : queryList) {
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
    public Long queryCount(CheckBean check) {
        return checkDetailMapper.queryCount(check);
    }

    @Override
    public InterReturn queryMaterial(CheckDetailBean checkDetail) {
        InterReturn interReturn = new InterReturn();
        List<CheckDetailBean> list = checkDetailMapper.queryMaterial(checkDetail);
        for (CheckDetailBean detailBean : list) {
            LambdaQueryWrapper<BaseMaterial> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(BaseMaterial::getMaterial_code, detailBean.getMaterialCode());
            List<BaseMaterial> baseMaterials = materialMapper.selectList(wrapper);
            detailBean.setMaterialPrice(baseMaterials.get(0).getMaterial_price().toString());
        }
        interReturn.setStatus(true);
        interReturn.setMessage("查询成功！");
        interReturn.setResult(list);
        return interReturn;
    }

    @Override
    public Long update2checkCount(CheckDetailBean checkDetailBean) {
        return checkDetailMapper.update2checkCount(checkDetailBean);
    }


}




