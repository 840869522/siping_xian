package com.example.wmsmp.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class OutHelper {


    //料箱1楼货到人&二楼机械手&二楼人工分拣   位置编号
    public Set<String> getSetB() {
        Set<String> protNos = new HashSet<>();
        protNos.add("3016");
        protNos.add("3019");
        protNos.add("3020");
        protNos.add("3042");
        protNos.add("3043");
        protNos.add("3059");

        protNos.add("4088");
        protNos.add("4089");
        protNos.add("4090");
        protNos.add("4091");

        protNos.add("4084");
        protNos.add("4085");
        protNos.add("4086");
        protNos.add("4087");

        protNos.add("4080");
        protNos.add("4081");
        protNos.add("4082");
        protNos.add("4083");

        protNos.add("6001");
        protNos.add("6002");
        protNos.add("6003");
        protNos.add("6004");
        protNos.add("6005");

        protNos.add("6006");
        protNos.add("6007");
        protNos.add("6008");
        protNos.add("6009");
        protNos.add("6010");

        protNos.add("6011");
        protNos.add("6012");
        protNos.add("6013");

        protNos.add("6014");
        protNos.add("6015");
        protNos.add("6016");

        return protNos;
    }

    //料箱 1楼货到人   位置编号
    public Set<String> getSetFirstFloor() {
        Set<String> protNos = new HashSet<>();
        protNos.add("3016");
        protNos.add("3019");
        protNos.add("3020");
        protNos.add("3042");
        protNos.add("3043");
        protNos.add("3059");

        protNos.add("4088");
        protNos.add("4089");
        protNos.add("4090");
        protNos.add("4091");

        protNos.add("4084");
        protNos.add("4085");
        protNos.add("4086");
        protNos.add("4087");

        protNos.add("4080");
        protNos.add("4081");
        protNos.add("4082");
        protNos.add("4083");
        return protNos;
    }

    //料箱 2楼机械手  位置编号
    public Set<String> getSetRobot() {
        Set<String> protNos = new HashSet<>();

        protNos.add("6001");
        protNos.add("6002");
        protNos.add("6003");
        protNos.add("6004");
        protNos.add("6005");

        protNos.add("6006");
        protNos.add("6007");
        protNos.add("6008");
        protNos.add("6009");
        protNos.add("6010");



        return protNos;
    }

    //料箱 2楼人工分拣   位置编号
    public Set<String> getSetMan() {
        Set<String> protNos = new HashSet<>();


        protNos.add("6011");
        protNos.add("6012");
        protNos.add("6013");

        protNos.add("6014");
        protNos.add("6015");
        protNos.add("6016");

        return protNos;
    }

    //根据上端口获取下端口
    public Map getDownPortB(String upPort) {
        Map<Integer, String> map = new HashMap<>();
        switch (upPort) {
            case "3016":
                map.put(1, "4088");
                map.put(2, "4089");
                break;
            case "3019":
                map.put(1, "4090");
                map.put(2, "4090");
                break;
            case "3020":
                map.put(1, "4084");
                map.put(2, "4086");
                break;
            case "3042":
                map.put(1, "4085");
                map.put(2, "4087");
                break;
            case "3043":
                map.put(1, "4080");
                map.put(2, "4082");
                break;
            case "3059":
                map.put(1, "4081");
                map.put(2, "4083");
                break;

            default:
                map.put(1, "无");
                map.put(2, "无");
                break;

        }
        return map;
    }


    //托盘 3个出库口   位置编号
    public Set<String> getSetOutP() {
        Set<String> protNos = new HashSet<>();

        protNos.add("1041");
        protNos.add("1026");
        protNos.add("1009");

        return protNos;
    }
}
