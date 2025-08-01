package com.example.wmsmp.emums;


public enum ChescEnum implements BaseEnum {

    /**
     * 1-入库任务
     */
    STORAGE1(1, "192.168.3.19"),

    /**
     * 2-出库任务
     */
    STORAGE2(2, "192.168.3.16");

    ChescEnum(int number, String description) {
        this.code = number;
        this.description = description;
    }

    private int code;
    private String description;

    @Override
    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static ChescEnum getByCode(int code) {
        for (ChescEnum item : ChescEnum.values()) {
            if (item.getCode() == code)
                return item;
        }
        return null;
    }
}
