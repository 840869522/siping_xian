package com.example.wmsmp.entity.task.CTU;

import lombok.Data;

import java.util.List;

public class CtuStatusRes {
    String code;
    String message;

    List<CtuStatus> data;
}
