package com.example.wmsmp.util;

import com.example.wmsmp.entity.LocationMap;
import com.example.wmsmp.entity.task.D.TaskD;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

//废弃
public class MapHelper {


    //获取空的LocationMap中的远伸位
    public LocationMap getEmptyLocation(List<LocationMap> locationMaps) {
        //过滤出Pallet_code为空的LocationMap
        List<LocationMap> myList = locationMaps.stream()
                .filter(item -> item.getPallet_code() == null)
                .collect(Collectors.toList());

        //对myList进行降序排序
        myList.sort(Comparator.comparing(LocationMap::getLocation_code_d).reversed());//reversed降序 按getLocation_code_d排序

        //返回排序后的第一个元素
        return myList.get(0);
    }


    //筛选map货位号某列或某层
    public List<LocationMap> getLocationAuto(List<LocationMap> locationMaps, int aaa, int bbb, int ccc) {
        List<LocationMap> myList = locationMaps.stream()
                .filter(item -> Integer.parseInt(item.getLocation_code().substring(aaa, bbb)) == ccc)
                .collect(Collectors.toList());
        return myList;
    }




}
