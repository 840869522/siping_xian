package com.example.wmsmp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.wmsmp.entity.base.BaseETag;
import com.example.wmsmp.entity.system.InterReturn;
import com.example.wmsmp.entity.system.ResReturn;
import com.example.wmsmp.service.BaseETagService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(value = "123", tags = "电子标签绑定关系")
@RestController
@CrossOrigin
public class BaseETagController {
    @Autowired
    BaseETagService baseETagService;
    private static Logger logger = Logger.getLogger(BaseETagController.class);

    /**
     * @Author:xhc
     * @Description 条件查询电子标签绑定关系
     * @DateTime 2024/6/28 16:27
     * @Params
     * @Return
     */
    @ApiOperation(value = "条件查询电子标签绑定关系")
    @PostMapping("/BaseETag/GetBaseETagBy")
    @CrossOrigin
    public InterReturn GetBaseETagByPage(@RequestBody BaseETag baseETag, int pageNo, int pageSize) {
       // logger.info("进入到GetBaseETagByPage方法");
     //   logger.info("接收到参数：" + baseETag);
        InterReturn interReturn = new InterReturn();
        ResReturn resReturn = new ResReturn();
        try {
            resReturn.setPageNo(pageNo);//页码赋值
            resReturn.setPageSize(pageSize);//页大小赋值
            Page<BaseETag> page = new Page<>(pageNo, pageSize);
            LambdaQueryWrapper<BaseETag> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(baseETag.getTag_code() != null && !baseETag.getTag_code().isEmpty(),
                    BaseETag::getTag_code, baseETag.getTag_code());
            wrapper.eq(baseETag.getTag_location() != null && !baseETag.getTag_location().isEmpty(),
                    BaseETag::getTag_location, baseETag.getTag_location());
            wrapper.orderByAsc(BaseETag::getId);
            List<BaseETag> equipment = baseETagService.page(page, wrapper).getRecords();

            if (equipment == null || equipment.size() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("未找到电子标签绑定关系");
            } else {
                interReturn.setStatus(true);
                interReturn.setMessage("查询到电子标签绑定关系成功！");
            }
            resReturn.setTotalCount((int) page.getTotal());//总条数赋值
            resReturn.setTotalPage((int) page.getPages());//总页数赋值
            resReturn.setAnything(equipment);
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("查询出错：" + e.getCause());
            System.out.println("查询出错：" + e.getMessage());
            logger.error("查询出错：" + e.getMessage());
        }
        interReturn.setResult(resReturn);//装入
      //  logger.info("GetBaseETagByPage 返回：" + interReturn);
        return interReturn;
    }

    /**
     * @Author:xhc
     * @Description 按位置号更新任务状态
     * @DateTime 2024/6/28 17:37
     * @Params
     * @Return
     */
    @ApiOperation(value = "按任务号更新任务状态", notes = "传电子标签绑定类 位置号与新托盘必填")
    @PostMapping("/BaseETag/UpdateETagPalletByLocation")
    @CrossOrigin
    public InterReturn UpdateETagPalletByLocation(@RequestBody BaseETag baseETag) {
        logger.info("进入 UpdateETagPalletByLocation");
        logger.info("接收到参数：" + baseETag);
        InterReturn interReturn = new InterReturn();
        try {
            if (baseETag.getTag_location() == null || baseETag.getTag_location().length() == 0) {
                interReturn.setStatus(false);
                interReturn.setMessage("位置号为空");
                System.out.println("位置号为空");
                logger.info("UpdateTaskPStatusByTaskNo返回：" + interReturn);
                return interReturn;
            }


            LambdaUpdateWrapper<BaseETag> wrapperUpdate = new LambdaUpdateWrapper<>();
            wrapperUpdate.eq(BaseETag::getTag_location, baseETag.getTag_location());//位置号
            BaseETag baseETag1 = new BaseETag();
            baseETag1.setPallet_code(baseETag.getPallet_code());//托盘号
            boolean boolUpdate = baseETagService.update(baseETag1, wrapperUpdate);
            if (boolUpdate) {
                interReturn.setStatus(true);
                interReturn.setMessage("电子标签绑定更新成功");
            } else {
                interReturn.setStatus(false);
                interReturn.setMessage("电子标签绑定更新失败");
                logger.info("UpdateETagPalletByLocation 返回：" + interReturn);
                return interReturn;
            }
        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("电子标签绑定更新出错：" + e.getCause());
            System.out.println("电子标签绑定更新出错：" + e.getMessage());
            logger.error("电子标签绑定更新出错：" + e.getMessage());
        }
        logger.info("UpdateETagPalletByLocation 返回：" + interReturn);
        return interReturn;
    }

    @ApiOperation(value = "判断隔壁是否有料箱", notes = "传电子标签位置号")
    @PostMapping("/BaseETag/IsNextExist")
    @CrossOrigin
    public String GetNextPallet(@RequestBody String tag_location) {
        String nextLocation = "";
        switch (tag_location) {
            case "3016":
                nextLocation = "3019";
                break;
            case "3019":
                nextLocation = "3016";
                break;
            case "3020":
                nextLocation = "3042";
                break;
            case "3042":
                nextLocation = "3020";
                break;
            case "3043":
                nextLocation = "3059";
                break;
            case "3059":
                nextLocation = "3043";
                break;
            default:
                break;
        }

        //查询有无托盘
        LambdaQueryWrapper<BaseETag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BaseETag::getTag_location, nextLocation);
        BaseETag baseETag = baseETagService.getOne(wrapper);


        return baseETag.getPallet_code();


    }


}

