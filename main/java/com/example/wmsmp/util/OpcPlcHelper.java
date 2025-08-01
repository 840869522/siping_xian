package com.example.wmsmp.util;


import com.example.wmsmp.emums.ChescEnum;
import com.example.wmsmp.entity.system.InterReturn;
import com.github.xingshuangs.iot.protocol.s7.enums.EPlcType;
import com.github.xingshuangs.iot.protocol.s7.service.S7PLC;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OpcPlcHelper {
    // 创建PLC对象
    static S7PLC s7PLC;

    //初始化Ip地址
    public static void instantiation(int ip) {
        ChescEnum str = ChescEnum.getByCode(ip);
        s7PLC = new S7PLC(EPlcType.S1200, str.getDescription());
    }

    //判断是否可以允许下发任务
    public static InterReturn getplc() {
        InterReturn interReturn = new InterReturn();
        try {
            //输送小车是否有托盘 有不能下发任务  只能人工干预
            boolean i34 = s7PLC.readBoolean("I3.4");
            boolean i35 = s7PLC.readBoolean("I3.5");
            if (i34 || i35) {
                log.info("智能货柜：输送小车有托盘！");
                interReturn.setStatus(false);
                interReturn.setMessage("智能货柜：故障报警-输送小车有托盘！");
                return interReturn;
            }
            //总故障汇总
            short m96 = s7PLC.readInt16("M96");
            short m98 = s7PLC.readInt16("M98");
            short m2000 = s7PLC.readInt16("M2000");
            short m2002 = s7PLC.readInt16("M2002");
            if (m96 != 0 || m98 != 0 || m2000 != 0 || m2002 != 0) {
                log.info("智能货柜：故障报警！");
                interReturn.setStatus(false);
                interReturn.setMessage("智能货柜：故障报警！");
                return interReturn;
            }
            //是否自动需要3
            short boolData = s7PLC.readInt16("M100");
            if (boolData != 3) {
                log.info("智能货柜:不是远程！");
                interReturn.setStatus(false);
                interReturn.setMessage("智能货柜:不是远程！");
                return interReturn;
            }
            //设备是否运行 0未运行
            short m58 = s7PLC.readInt16("M58");
            if (m58 != 0) {
                log.info("智能货柜:正在运行中！");
                interReturn.setStatus(false);
                interReturn.setMessage("智能货柜:正在运行中！");
                return interReturn;
            }
            interReturn.setStatus(true);
        } catch (Exception x) {
            interReturn.setStatus(false);
            log.error("智能货柜：与智能货柜连接失败" + x + "!");
            interReturn.setMessage("智能货柜：与智能货柜连接失败!");
            return interReturn;
        }
        return interReturn;
    }

    //下发取
    public static InterReturn setOutplc(int storageNo, int layer) {
        instantiation(storageNo);
        InterReturn interReturn = new InterReturn();
        try {
            interReturn = getplc();
            boolean i15 = s7PLC.readBoolean("I1.5");
            if (i15) {
                interReturn.setStatus(false);
                interReturn.setMessage("智能货柜：出库口有货，不可出库");
                return interReturn;
            }
            if (interReturn.isStatus()) {
                s7PLC.writeInt16("M208", Short.parseShort(String.valueOf(layer)));
                s7PLC.writeBoolean("M30.3", true);
                interReturn.setMessage("智能货柜：取货下发成功");
            }
        } catch (Exception x) {
            interReturn.setStatus(false);
            log.error("智能货柜：与智能货柜连接失败" + x + "!");
            interReturn.setMessage("智能货柜：与智能货柜连接失败!");
            return interReturn;
        } finally {
            interReturn.setStatus(false);
            log.error("智能货柜：与智能货柜连接失败 !");
            interReturn.setMessage("智能货柜：与智能货柜连接失败!");
            return interReturn;
        }
    }

    //下发存
    public static InterReturn setInPlc(int storageNo) {
        instantiation(storageNo);
        InterReturn interReturn = new InterReturn();
        try {
            interReturn = getplc();
            boolean i15 = s7PLC.readBoolean("I1.5");
            if (!i15) {
                interReturn.setStatus(false);
                interReturn.setMessage("智能货柜：出库口无货，不可入库");
                return interReturn;
            }
            if (interReturn.isStatus()) {
                s7PLC.writeBoolean("M30.2", true);
                interReturn.setMessage("智能货柜：存货下发成功");
            }
        } catch (Exception x) {
            interReturn.setStatus(false);
            log.error("智能货柜：与智能货柜连接失败" + x + "!");
            interReturn.setMessage("智能货柜：与智能货柜连接失败!");
            return interReturn;
        }
        return interReturn;
    }

    //下发终止任务
    public static InterReturn setTaskStop(int storageNo) {
        instantiation(storageNo);
        InterReturn interReturn = new InterReturn();
        //设备是否运行 0未运行
        short m58 = s7PLC.readInt16("M58");
        if (m58 == 0) {
            interReturn.setStatus(false);
            interReturn.setMessage("智能货柜:当前无任务！");
            log.info("智能货柜:当前无任务！");
            return interReturn;
        }
        s7PLC.writeBoolean("M8.6", true);
        interReturn.setStatus(true);
        interReturn.setMessage("智能货柜：终止下发成功");
        return interReturn;
    }
}

