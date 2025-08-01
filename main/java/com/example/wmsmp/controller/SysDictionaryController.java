package com.example.wmsmp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.wmsmp.entity.system.InterReturn;
import com.example.wmsmp.entity.system.ResReturn;
import com.example.wmsmp.entity.system.SysDictionary;
import com.example.wmsmp.service.SysDictionaryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

@Api(value = "123", tags = "字典")
@RestController
@CrossOrigin
public class SysDictionaryController {

    @Autowired
    SysDictionaryService sysDictionaryService;

    private static Logger logger = Logger.getLogger(SysDictionaryController.class);


    @ApiOperation(value = "查询全部字典", notes = "pageNo和pageSize 是否去重isDedup")
    @PostMapping("/Dictionary/GetAllSysDictionary")
    @CrossOrigin
    public InterReturn GetAllSysDictionary(@RequestBody ResReturn resReturnIn, boolean isDedup) {
        logger.info("进入 GetAllSysDictionary");
        logger.info("接收到参数：" + resReturnIn);
        InterReturn interReturn = new InterReturn();
        ResReturn resReturn = new ResReturn();
        try {
            int pageNo = resReturnIn.getPageNo();
            int pageSize = resReturnIn.getPageSize();
            resReturn.setPageNo(pageNo);//页码赋值
            resReturn.setPageSize(pageSize);//页大小赋值

            //mybatisplus
            Page<SysDictionary> page = new Page<>(pageNo, pageSize);
            LambdaQueryWrapper<SysDictionary> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByAsc(SysDictionary::getDic_id);//按id递增
            List<SysDictionary> list = sysDictionaryService.page(page, wrapper).getRecords();
            if (isDedup) {
                //需要去重
                for (int i = 0; i < list.size() - 1; i++) {
                    for (int j = list.size() - 1; j > i; j--) {
                        if (list.get(j).getDic_type().equals(list.get(i).getDic_type())) {
                            list.remove(j);
                        }
                    }
                }
                int totalCount = list.size();
                resReturn.setTotalCount(totalCount);//总条数赋值
                int totalPageInt = totalCount / pageSize;//总页数的整数部分
                int totalPage = totalCount % pageSize == 0 ? totalPageInt : totalPageInt + 1;//总页数
                resReturn.setTotalPage(totalPage);//总页数赋值

            } else {
                //不需要去重
                resReturn.setTotalCount((int) page.getTotal());//总条数赋值
                resReturn.setTotalPage((int) page.getPages());//总页数赋值
            }
            interReturn.setStatus(true);
            interReturn.setMessage("OK");
            resReturn.setAnything(list);
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("查询出错：" + e.getCause());
            System.out.println("查询出错：" + e.getMessage());
            logger.error("查询出错：" + e.getMessage());
        }
        interReturn.setResult(resReturn);//装入
    //    logger.info("GetAllSysDictionary 返回：" + interReturn);
        return interReturn; //+ environment.getProperty("url");
    }



    @ApiOperation(value = "条件查询字典", notes = "sysDictionary类 pageNo pageSize")
    @PostMapping("/Dictionary/GetAllSysDictionaryBy")
    @CrossOrigin
    public InterReturn GetAllSysDictionaryBy(@RequestBody SysDictionary sysDictionary, int pageNo, int pageSize) {
//        logger.info("进入 GetAllSysDictionaryBy");
//        logger.info("接收到参数：" + sysDictionary);
        InterReturn interReturn = new InterReturn();
        ResReturn resReturn = new ResReturn();
        try {
            resReturn.setPageNo(pageNo);//页码赋值
            resReturn.setPageSize(pageSize);//页大小赋值

            //mybatisplus
            Page<SysDictionary> page = new Page<>(pageNo, pageSize);
            LambdaQueryWrapper<SysDictionary> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByAsc(SysDictionary::getDic_id);//按id递增
            wrapper.eq(sysDictionary.getDic_id() != 0,
                    SysDictionary::getDic_id, sysDictionary.getDic_id());
            wrapper.eq(sysDictionary.getDic_name() != null && !sysDictionary.getDic_name().isEmpty(),
                    SysDictionary::getDic_name, sysDictionary.getDic_name());
            wrapper.eq(sysDictionary.getDic_type() != null && !sysDictionary.getDic_type().isEmpty(),
                    SysDictionary::getDic_type, sysDictionary.getDic_type());
            wrapper.eq(sysDictionary.getDic_display_value() != null && !sysDictionary.getDic_display_value().isEmpty(),
                    SysDictionary::getDic_display_value, sysDictionary.getDic_display_value());
            wrapper.eq(sysDictionary.getDic_value() != 0,
                    SysDictionary::getDic_value, sysDictionary.getDic_value());
            wrapper.eq(sysDictionary.getStatus() != 0,
                    SysDictionary::getStatus, sysDictionary.getStatus());
            wrapper.eq(sysDictionary.getDic_display_name() != null && !sysDictionary.getDic_display_name().isEmpty(),
                    SysDictionary::getDic_display_name, sysDictionary.getDic_display_name());
            List<SysDictionary> dics = sysDictionaryService.page(page, wrapper).getRecords();

            if (dics == null || dics.size() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("未找到此字典！");
            } else {
                interReturn.setStatus(true);
                interReturn.setMessage("OK");
            }
            resReturn.setTotalCount((int) page.getTotal());//总条数赋值
            resReturn.setTotalPage((int) page.getPages());//总页数赋值
            resReturn.setAnything(dics);
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("查询出错：" + e.getCause());
            System.out.println("查询出错：" + e.getMessage());
            logger.error("查询出错：" + e.getMessage());
        }
        interReturn.setResult(resReturn);//装入
        //logger.info("GetAllSysDictionaryBy 返回：" + interReturn);
        return interReturn;
    }


    @ApiOperation(value = "添加字典", notes = "sysDictionary类")
    @PostMapping("/Dictionary/AddSysDictionary")
    @CrossOrigin
    public InterReturn AddSysDictionary(@RequestBody SysDictionary sysDictionary) {
        logger.info("进入 AddSysDictionary");
        logger.info("接收到参数：" + sysDictionary);
        InterReturn interReturn = new InterReturn();
        try {
            if (sysDictionary.getDic_id() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("参数异常或dic_id为空");
                System.out.println("参数异常或dic_id为空");
                return interReturn;
            } else {
                sysDictionary.setCreate_time(Calendar.getInstance().getTime());//创建时间赋值
                //mybatisplus
                boolean boolSave = sysDictionaryService.save(sysDictionary);
                if (boolSave) {
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
        logger.info("AddSysDictionary 返回：" + interReturn);
        return interReturn;
    }

    @ApiOperation(value = "删除字典", notes = "dic_id")
    @PostMapping("/Dictionary/DelSysDictionaryByDicId")
    @CrossOrigin
    public InterReturn DelSysDictionaryByDicId(@RequestBody SysDictionary sysDictionary) {
        logger.info("进入 DelSysDictionaryByDicId");
        logger.info("接收到参数：" + sysDictionary);
        InterReturn interReturn = new InterReturn();
        try {
            if (sysDictionary.getDic_id() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("参数异常或dic_id为空");
                System.out.println("参数异常或dic_id为空");
                return interReturn;
            } else {
                //mybatisplus
                boolean bool = sysDictionaryService.removeById(sysDictionary);
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
        logger.info("DelSysDictionaryByLoginName 返回：" + interReturn);
        return interReturn;
    }


    @ApiOperation(value = "更新字典", notes = "根据dic_id更新字典 传sysDictionary类")
    @PostMapping("/Dictionary/UpdateSysDictionaryByDicId")
    @CrossOrigin
    public InterReturn UpdateSysDictionaryByLoginName(@RequestBody SysDictionary sysDictionary) {
        logger.info("进入 UpdateSysDictionaryByLoginName");
        logger.info("接收到参数：" + sysDictionary);
        InterReturn interReturn = new InterReturn();
        try {
            if (sysDictionary.getDic_id() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("参数异常或dic_id为空");
                System.out.println("参数异常或dic_id为空");
                return interReturn;
            } else {
                //mybatisplus
                LambdaUpdateWrapper<SysDictionary> wrapper = new LambdaUpdateWrapper<>();
                wrapper.eq(SysDictionary::getDic_id, sysDictionary.getDic_id());
                wrapper.set(sysDictionary.getDic_name() != null && !sysDictionary.getDic_name().isEmpty(), SysDictionary::getDic_name, sysDictionary.getDic_name());
                wrapper.set(sysDictionary.getDic_type() != null && !sysDictionary.getDic_type().isEmpty(), SysDictionary::getDic_type, sysDictionary.getDic_type());
                wrapper.set(sysDictionary.getDic_display_value() != null && !sysDictionary.getDic_display_value().isEmpty(), SysDictionary::getDic_display_value, sysDictionary.getDic_display_value());
                wrapper.set(sysDictionary.getDic_value() != 0, SysDictionary::getDic_value, sysDictionary.getDic_value());
                wrapper.set(sysDictionary.getStatus() != 0, SysDictionary::getStatus, sysDictionary.getStatus());
                wrapper.set(sysDictionary.getRemark() != null && !sysDictionary.getRemark().isEmpty(), SysDictionary::getRemark, sysDictionary.getRemark());
                wrapper.set(SysDictionary::getUpdater, sysDictionary.getUpdater());
                wrapper.set(SysDictionary::getUpdate_time, LocalDateTime.now());
                boolean bool = sysDictionaryService.update(null, wrapper);

                if (bool) {
                    interReturn.setStatus(true);
                    interReturn.setMessage("OK");
                } else {
                    interReturn.setStatus(false);
                    interReturn.setMessage("无更新");
                }
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("更新出错：" + e.getCause());
            System.out.println("更新出错：" + e.getCause());
            logger.error("删除出错：" + e.getMessage());
        }
        logger.info("UpdateSysDictionaryByLoginName 返回：" + interReturn);
        return interReturn;
    }


    /**
     * @Author:lcy
     * @Description :所有的前端页面下拉列表接口
     * @DateTime 2023/6/29 15:23
     * @Params
     * @Return
     */
    @ApiOperation(value = "前端页面下拉列表接口")
    @PostMapping("/Dictionary/GetDropDownList")
    @CrossOrigin
    public InterReturn GetDropDownList(String dicDisplayName) {
//        logger.info("进入GetDropDownList");
        InterReturn interReturn = new InterReturn();
        ResReturn resReturn = new ResReturn();
        List<String> inOrderDetailType;
        try {
            LambdaQueryWrapper<SysDictionary> dicLambdaQueryWrapper = new LambdaQueryWrapper<>();
            dicLambdaQueryWrapper.eq(SysDictionary::getDic_display_name, dicDisplayName);
            List<SysDictionary> dics = sysDictionaryService.list(dicLambdaQueryWrapper);
            if (dics.size() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("数据库里无记录！");
            } else {
                inOrderDetailType = dics.stream().map(SysDictionary::getDic_display_value).collect(Collectors.toList());
                interReturn.setStatus(true);
                interReturn.setMessage("查询下拉列表数据成功！");
                resReturn.setAnything(inOrderDetailType);
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("查询出错" + e.getCause());
            System.out.println("查询出错：" + e.getCause());
            logger.error("查询出错：" + e.getMessage());
            return interReturn;
        }
        interReturn.setResult(resReturn);
      //  logger.info("GetDropDownList 返回：" + interReturn);
        return interReturn;
    }
}
