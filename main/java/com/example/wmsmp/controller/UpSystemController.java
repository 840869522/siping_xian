package com.example.wmsmp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.wmsmp.entity.InOrder;
import com.example.wmsmp.entity.InOrderDetail;
import com.example.wmsmp.entity.OutOrder;
import com.example.wmsmp.entity.OutOrderDetail;
import com.example.wmsmp.entity.UpSystem.DataObject;
import com.example.wmsmp.entity.UpSystem.ReturnBack;
import com.example.wmsmp.entity.UpSystem.ReturnMark;
import com.example.wmsmp.entity.system.InterReturn;
import com.example.wmsmp.service.InOrderDetailService;
import com.example.wmsmp.service.InOrderService;
import com.example.wmsmp.service.OutOrderDetailService;
import com.example.wmsmp.service.OutOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Api(value = "123", tags = "上游系统对接")
@RestController
@CrossOrigin
public class UpSystemController {
    @Autowired
    InOrderService inOrderService;
    @Autowired
    OutOrderService outOrderService;
    @Autowired
    OutOrderDetailService outOrderDetailService;
    @Autowired
    InOrderDetailService inOrderDetailService;

    @Autowired
    protected SqlSessionFactory sqlSessionFactory;

    private static Logger logger = Logger.getLogger(UpSystemController.class);


    @Autowired
    private RestTemplate restTemplate;//用于调用外部接口

    @Autowired
    private Environment environment;//用于读取配置文件


    /**
     * @Author:xhc
     * @Description 回传上游系统（入库、出库）
     * @DateTime 2024/8/22 15:12
     * @Params
     * @Return
     */
    @ApiOperation(value = "回传上游系统（入库、出库）", notes = "单据号 类型（入库、出库）")
    @PostMapping("/InOrder/Back2UpSystem")
    @CrossOrigin
    public InterReturn Back2UpSystem(@RequestBody String danjuhao, String type) {
//        logger.info("Back2UpSystem");
//        logger.info("接收到参数：" + danjuhao + "和" + type);
        InterReturn interReturn = new InterReturn();
        try {
            if (danjuhao == null || "".equals(danjuhao.trim())) {
                interReturn.setStatus(false);
                interReturn.setMessage("单据号为空！");
                System.out.println("参单据号为空！");
                return interReturn;
            }

            ReturnBack returnBack = new ReturnBack();
            String URL = "";
            switch (type) {
                case "入库": {
                    URL = environment.getProperty("apiUp.In");
                    LambdaQueryWrapper<InOrderDetail> wrapper = new LambdaQueryWrapper<>();
                    wrapper.eq(InOrderDetail::getIn_order_id, danjuhao);
                    List<InOrderDetail> inOrderDetails = inOrderDetailService.list(wrapper);

                    returnBack.setRuKuDanHao(danjuhao);//入库
                    List<DataObject> list = new ArrayList<>();
                    for (InOrderDetail inOrderDetail : inOrderDetails) {
                        DataObject dataObject = new DataObject();
                        dataObject.setQiCaiDaiMa(inOrderDetail.getMaterial_code());//器材代码
                        dataObject.setQiCaiMingCheng(inOrderDetail.getMaterial_name());//器材名称
                        dataObject.setZhiLiangDengJi(inOrderDetail.getZhiliangdengji());//质量等级
                        dataObject.setPiCiHao(inOrderDetail.getBatch());//批次号
                        dataObject.setShiJiShuLiang(inOrderDetail.getActual_count());//实际数量
                        list.add(dataObject);
                    }
                    returnBack.setData(list);
                    break;
                }
                case "出库": {
                    URL = environment.getProperty("apiUp.Out");
                    LambdaQueryWrapper<OutOrderDetail> wrapper = new LambdaQueryWrapper<>();
                    wrapper.eq(OutOrderDetail::getOut_order_id, danjuhao);
                    List<OutOrderDetail> OutOrderDetails = outOrderDetailService.list(wrapper);

                    returnBack.setChuKuDanHao(danjuhao);//出库
                    List<DataObject> list = new ArrayList<>();
                    for (OutOrderDetail outOrderDetail : OutOrderDetails) {
                        DataObject dataObject = new DataObject();
                        dataObject.setQiCaiDaiMa(outOrderDetail.getMaterial_code());//器材代码
                        dataObject.setQiCaiMingCheng(outOrderDetail.getMaterial_name());//器材名称
                        dataObject.setZhiLiangDengJi(outOrderDetail.getZhiliangdengji());//质量等级
                        dataObject.setPiCiHao(outOrderDetail.getBatch());//批次号
                        dataObject.setShiJiShuLiang(outOrderDetail.getActual_count());//实际数量
                        list.add(dataObject);
                    }
                    returnBack.setData(list);
                    break;
                }

                default:

                    //type不对
                    break;
            }


            //1.回传上游
            List<ReturnBack> list = new ArrayList<>();
            list.add(returnBack);
            logger.info("回传上游，拼接入参：" + list);
            HttpEntity<List<ReturnBack>> httpEntitysnew = new HttpEntity<>(list);
            //参数为 url http实体 返回类型
            ReturnMark response = restTemplate.postForObject(URL, httpEntitysnew, ReturnMark.class);
            logger.info("回传上游，接收出参：" + response);
            if (response.getReturnMark().equals("success")) {
                interReturn.setStatus(true);
                interReturn.setMessage(response.getErrorMsg());
                System.out.println(response.getErrorMsg());
            } else if ("fail".equals(response.getReturnMark())) {
                interReturn.setStatus(false);
                interReturn.setMessage("上游接口报错：" + response.getErrorMsg());
                System.out.println(response.getErrorMsg());
                return interReturn;
            }

            //2.更新单据状态
            switch (type) {
                case "入库": {
                    LambdaUpdateWrapper<InOrder> wrapper = new LambdaUpdateWrapper<>();
                    wrapper.eq(InOrder::getIn_order_id, danjuhao);
                    wrapper.set(InOrder::getStatus, "已回传");
                    boolean bl = inOrderService.update(wrapper);
                    if (bl) {
                        interReturn.setStatus(true);
                        interReturn.setMessage("回传完成，更改单据状态完成");
                        System.out.println(response.getErrorMsg());
                        return interReturn;
                    } else {
                        interReturn.setStatus(false);
                        interReturn.setMessage("回传完成，更改单据状态出错");
                        System.out.println("回传完成，更改单据状态出错");
                        return interReturn;
                    }
                }
                case "出库": {
                    LambdaUpdateWrapper<OutOrder> wrapper = new LambdaUpdateWrapper<>();
                    wrapper.eq(OutOrder::getOut_order_id, danjuhao);
                    wrapper.set(OutOrder::getStatus, "已回传");
                    boolean bl = outOrderService.update(wrapper);
                    if (bl) {
                        interReturn.setStatus(true);
                        interReturn.setMessage("回传完成，更改单据状态完成");
                        System.out.println(response.getErrorMsg());
                        return interReturn;
                    } else {
                        interReturn.setStatus(false);
                        interReturn.setMessage("回传完成，更改单据状态出错");
                        System.out.println("回传完成，更改单据状态出错");
                        return interReturn;
                    }
                }

                default:

                    //type不对
                    break;
            }


        } catch (Exception e) {
            interReturn.setStatus(false);
            interReturn.setMessage("回传出错：" + e.getCause());
            System.out.println("回传出错：" + e.getMessage());
            logger.error("回传出错：" + e.getMessage());
            return interReturn;
        }
        logger.info("Back2UpSystem 返回：" + interReturn);
        return interReturn;
    }


}
