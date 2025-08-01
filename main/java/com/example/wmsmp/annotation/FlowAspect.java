package com.example.wmsmp.annotation;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.wmsmp.entity.Flow;
import com.example.wmsmp.service.FlowService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.Map;


/**
 * 流水注解记录数据
 */
@Aspect
@Component
@Slf4j
public class FlowAspect {

    @Resource
    private FlowService flowService;

    /**
     * 配置织入点
     */
    @Pointcut("@annotation(com.example.wmsmp.annotation.FlowAnnotation)")
    public void flowPointCut() {
    }

    /**
     * 处理完请求后执行
     *
     * @param joinPoint 切点
     */
    @AfterReturning(pointcut = "flowPointCut()", returning = "jsonResult")
    public void doAfterReturning(JoinPoint joinPoint, Object jsonResult) {
        handleLog(joinPoint, null, jsonResult);
    }

    /**
     * 拦截异常操作
     *
     * @param joinPoint 切点
     * @param e         异常
     */
    @AfterThrowing(value = "flowPointCut()", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Exception e) {
        handleLog(joinPoint, e, null);
    }

    protected void handleLog(final JoinPoint joinPoint, final Exception e, Object jsonResult) {
        //如果返回值为false则不记录
        JSON parse = JSONUtil.parse(jsonResult);
        Boolean b = Boolean.valueOf(parse.getByPath("status").toString());
        if (!b) return;
        try {
            // 获得注解
            FlowAnnotation controllerLog = getAnnotationLog(joinPoint);
            if (controllerLog == null) {
                return;
            }
            /*记录流水*/
            Flow flow = new Flow();
            //获取用户
            String user = ServletUtils.getRequest().getHeader("User-Info");
            String decodeUser = "设备回传";
            if (StrUtil.isNotBlank(user)) {
                decodeUser = URLDecoder.decode(user, "UTF-8");
            }
            flow.setCreator(decodeUser);
            flow.setCreate_time(DateUtil.date());
            flow.setUrl(ServletUtils.getRequest().getRequestURI());
            // 处理设置注解上的参数
            getControllerMethodDescription(joinPoint, controllerLog, flow);
            if (controllerLog.businessType().getCode() == BusinessType.ADD_AND_LOSS.getCode()) {
                Flow flowId = BeanUtil.copyProperties(flow, Flow.class, "flow_id");
                //移动需要查询原载具库存数量
                flow.setPallet_code(flow.getLocation_code());
                Integer inventoryCount = flowService.queryInventoryCount(flow);
                if (inventoryCount != null) flow.setAfter_count(inventoryCount);
                else flow.setAfter_count(0);
                flow.setFlow_type(BusinessType.LOSS.getInfo());
                recordOper(flow);
                flowId.setFlow_type(BusinessType.ADD.getInfo());
                recordOper(flowId);
            } else if (controllerLog.businessType().getCode() == BusinessType.ADD.getCode()) {
                // 保存数据库
                recordOper(flow);
            } else if (controllerLog.businessType().getCode() == BusinessType.LOSS.getCode()) {
                // 保存数据库
                recordOper(flow);
            }
        } catch (Exception exp) {
            // 记录本地异常日志
            log.error("==前置通知异常==");
            log.error("异常信息:{}", exp.getMessage(), e);
        }
    }


    /**
     * 获取注解中对方法的描述信息 用于Controller层注解
     *
     * @param flowAnnotation
     * @param flow
     */
    public void getControllerMethodDescription(JoinPoint joinPoint, FlowAnnotation flowAnnotation, Flow flow) throws IOException {
        // 设置流水类型
        flow.setFlow_title(flowAnnotation.title());
        flow.setFlow_type(flowAnnotation.businessType().getInfo());
        // 是否需要保存request，参数和值
        if (flowAnnotation.isSaveRequestData()) {
            // 获取参数的信息，传入到数据库中。
            setRequestValue(joinPoint, flow);
        }
    }

    /**
     * 获取请求的参数，放到流水中
     *
     * @param flow 流水数据
     * @throws Exception 异常
     */
    private void setRequestValue(JoinPoint joinPoint, Flow flow) throws IOException {
        //解析body
        String params = argsArrayToString(joinPoint.getArgs());
        JSONObject paramJson = JSONUtil.parseObj(params);
        if (paramJson.containsKey("material_code")) {
            flow.setMaterial_code(paramJson.get("material_code").toString());
        }
        if (paramJson.containsKey("material_name")) {
            flow.setMaterial_name(paramJson.get("material_name").toString());
        }
        if (paramJson.containsKey("batch")) {
            flow.setBatch(paramJson.get("batch").toString());
        }
        if (paramJson.containsKey("pallet_code")) {
//            flow.setPallet_code(paramJson.get("pallet_code").toString());
            flow.setPallet_code(getOne(paramJson.get("pallet_code")));
        }
        if (paramJson.containsKey("palletCode")) {
            flow.setPallet_code(getOne(paramJson.get("palletCode")));
//            flow.setPallet_code(paramJson.get("palletCode").toString());
        }
        if (paramJson.containsKey("location_code")) {
//            flow.setLocation_code(paramJson.get("location_code").toString());
            flow.setLocation_code(getOne(paramJson.get("location_code")));
        }
        if (paramJson.containsKey("palletCodeOld")) {
//            flow.setLocation_code(paramJson.get("palletCodeOld").toString());
            String palletCodeOld = getOne(paramJson.get("palletCodeOld"));
            flow.setLocation_code(palletCodeOld);
        }
        if (paramJson.containsKey("inventory_count")) {
            flow.setCmd_count(Integer.valueOf(paramJson.get("inventory_count").toString()));
        }
        if (paramJson.containsKey("pick_qty")) {
            flow.setCmd_count(Integer.valueOf(paramJson.get("pick_qty").toString()));
        }
        if (paramJson.containsKey("in_order_id")) {
            flow.setOrder_id(paramJson.get("in_order_id").toString());
        }
        if (paramJson.containsKey("out_order_id")) {
            flow.setOrder_id(paramJson.get("out_order_id").toString());
        }
        if (paramJson.containsKey("inOrderId")) {
            flow.setOrder_id(paramJson.get("inOrderId").toString());
        }
        if (paramJson.containsKey("cell_id")) {
            flow.setCell_id(paramJson.get("cell_id").toString());
        }
        if (paramJson.containsKey("creator")) {
            flow.setCreator(paramJson.get("creator").toString());
        }
        if (paramJson.containsKey("updater")) {
            flow.setCreator(paramJson.get("updater").toString());
        }
        //解析param
        HttpServletRequest request = ServletUtils.getRequest();
        Map<String, String[]> parameterMap = request.getParameterMap();
        if (parameterMap.containsKey("material_code")) {
            flow.setMaterial_code(request.getParameter("material_code"));
        }
        if (parameterMap.containsKey("material_name")) {
            flow.setMaterial_name(request.getParameter("material_name"));
        }
        if (parameterMap.containsKey("batch")) {
            flow.setBatch(request.getParameter("batch"));
        }
        if (parameterMap.containsKey("pallet_code")) {
//            flow.setPallet_code(request.getParameter("pallet_code"));
            flow.setPallet_code(getOne(parameterMap.get("pallet_code")));
        }
        if (parameterMap.containsKey("palletCode")) {
//            flow.setPallet_code(request.getParameter("palletCode"));
            flow.setPallet_code(getOne(parameterMap.get("palletCode")));
        }
        if (parameterMap.containsKey("location_code")) {
//            flow.setLocation_code(parameterMap.get("location_code").toString());
            flow.setLocation_code(getOne(parameterMap.get("location_code")));
        }
        if (parameterMap.containsKey("palletCodeOld")) {
//            flow.setLocation_code(parameterMap.get("palletCodeOld").toString());
            String palletCodeOld = getOne(parameterMap.get("palletCodeOld"));
            flow.setLocation_code(palletCodeOld);
        }
        if (parameterMap.containsKey("inventory_count")) {
            flow.setCmd_count(Integer.valueOf(request.getParameter("inventory_count")));
        }
        if (parameterMap.containsKey("pick_qty")) {
            flow.setCmd_count(Integer.valueOf(parameterMap.get("pick_qty").toString()));
        }
        if (parameterMap.containsKey("in_order_id")) {
            flow.setOrder_id(request.getParameter("in_order_id"));
        }
        if (parameterMap.containsKey("out_order_id")) {
            flow.setOrder_id(request.getParameter("out_order_id"));
        }
        if (parameterMap.containsKey("inOrderId")) {
            flow.setOrder_id(request.getParameter("inOrderId"));
        }
        if (parameterMap.containsKey("cell_id")) {
            flow.setCell_id(request.getParameter("cell_id"));
        }
        if (parameterMap.containsKey("creator")) {
            flow.setCreator(parameterMap.get("creator").toString());
        }
        if (parameterMap.containsKey("updater")) {
            flow.setCreator(parameterMap.get("updater").toString());
        }
        //针对需特殊解析的接口进行处理
        if ("/OutPick/PickPallet".equals(ServletUtils.getRequest().getRequestURI())) {
            if (paramJson.containsKey("pallet_code")) {
                flow.setLocation_code(getOne(paramJson.get("pallet_code")));
            }
            if (parameterMap.containsKey("palletCode")) {
                flow.setPallet_code(getOne(parameterMap.get("palletCode")));
            }
        }
        Integer inventoryCount = flowService.queryInventoryCount(flow);
        if (inventoryCount != null) flow.setAfter_count(inventoryCount);
    }

    private String getOne(Object palletCode) throws IOException {
        if (ArrayUtil.isArray(palletCode)) {
            Object[] objects = (Object[]) palletCode;
            return objects[0].toString();
        } else {
            return palletCode.toString();
        }
    }


    /**
     * 是否存在注解，如果存在就获取
     */
    private FlowAnnotation getAnnotationLog(JoinPoint joinPoint) {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();
        if (method != null) {
            return method.getAnnotation(FlowAnnotation.class);
        }
        return null;
    }

    /**
     * 参数拼装
     */
    private String argsArrayToString(Object[] paramsArray) {
        String params = "";
        if (paramsArray != null && paramsArray.length > 0) {
            for (int i = 0; i < paramsArray.length; i++) {
                if (!isFilterObject(paramsArray[i])) {
                    params += JSONUtil.toJsonStr(paramsArray[i]) + " ";
                }
            }
        }
        return params.trim();
    }

    /**
     * 判断是否需要过滤的对象。
     *
     * @param o 对象信息。
     * @return 如果是需要过滤的对象，则返回true；否则返回false。
     */
    public boolean isFilterObject(final Object o) {
        return o instanceof MultipartFile || o instanceof HttpServletRequest || o instanceof HttpServletResponse;
    }


    /**
     * 操作日志记录 通过环形队列
     */
    private void recordOper(final Flow flow) {
        TimerUtils.instance().addTask((timeout) -> {
            flowService.save(flow);
        });
    }

}
