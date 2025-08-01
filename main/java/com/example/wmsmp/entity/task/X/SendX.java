package com.example.wmsmp.entity.task.X;

import lombok.Data;

@Data
public class SendX {
    String wmsTaskId;
    Integer shuttleId;
    String direction;
    String palletId;
    String location;
    Integer palletLength;
}
