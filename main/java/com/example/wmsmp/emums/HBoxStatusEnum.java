package com.example.wmsmp.emums;

public enum HBoxStatusEnum implements BaseEnum  {
    /**
     * 1-空箱
     */
    EMPTY(1, "空箱"),

    /**
     * 2-半箱
     */
    HALF_CHEST(2, "半箱"),

    /**
     * 3-满箱
     */
    TRUNKFUL(3, "满箱");

    HBoxStatusEnum(int number, String description) {
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
