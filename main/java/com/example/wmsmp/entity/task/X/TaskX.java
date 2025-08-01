package com.example.wmsmp.entity.task.X;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 箱组任务表
 * @TableName t_task_x
 */
@TableName(value ="t_task_x")
@Data
public class TaskX implements Serializable {
    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 任务号
     */
    @TableField(value = "task_no")
    private String task_no;

    /**
     * 库名
     */
    @TableField(value = "warehouse")
    private String warehouse;

    /**
     * 货位号
     */
    @TableField(value = "location_code")
    private String location_code;

    /**
     * 任务类型
     */
    @TableField(value = "task_type")
    private String task_type;

    /**
     * 载具编号
     */
    @TableField(value = "pallet_code")
    private String pallet_code;

    /**
     * 站台编号
     */
    @TableField(value = "prot_no")
    private String prot_no;

    /**
     * 
     */
    @TableField(value = "status")
    private String status;

    /**
     * 
     */
    @TableField(value = "udf01")
    private String udf01;

    /**
     * 
     */
    @TableField(value = "udf02")
    private String udf02;

    /**
     * 
     */
    @TableField(value = "udf03")
    private String udf03;

    /**
     * 
     */
    @TableField(value = "udf04")
    private String udf04;

    /**
     * 
     */
    @TableField(value = "udf05")
    private String udf05;

    /**
     * 
     */
    @TableField(value = "creator")
    private String creator;

    /**
     * 
     */
    @TableField(value = "create_time",fill = FieldFill.INSERT)
    private Date create_time;

    /**
     * 
     */
    @TableField(value = "updater")
    private String updater;

    /**
     * 
     */
    @TableField(value = "update_time",fill = FieldFill.INSERT_UPDATE)
    private Date update_time;

    /**
     * 
     */
    @TableField(value = "car_no")
    private String car_no;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        TaskX other = (TaskX) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getTask_no() == null ? other.getTask_no() == null : this.getTask_no().equals(other.getTask_no()))
            && (this.getLocation_code() == null ? other.getLocation_code() == null : this.getLocation_code().equals(other.getLocation_code()))
            && (this.getTask_type() == null ? other.getTask_type() == null : this.getTask_type().equals(other.getTask_type()))
            && (this.getPallet_code() == null ? other.getPallet_code() == null : this.getPallet_code().equals(other.getPallet_code()))
            && (this.getProt_no() == null ? other.getProt_no() == null : this.getProt_no().equals(other.getProt_no()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getUdf01() == null ? other.getUdf01() == null : this.getUdf01().equals(other.getUdf01()))
            && (this.getUdf02() == null ? other.getUdf02() == null : this.getUdf02().equals(other.getUdf02()))
            && (this.getUdf03() == null ? other.getUdf03() == null : this.getUdf03().equals(other.getUdf03()))
            && (this.getUdf04() == null ? other.getUdf04() == null : this.getUdf04().equals(other.getUdf04()))
            && (this.getUdf05() == null ? other.getUdf05() == null : this.getUdf05().equals(other.getUdf05()))
            && (this.getCreator() == null ? other.getCreator() == null : this.getCreator().equals(other.getCreator()))
            && (this.getCreate_time() == null ? other.getCreate_time() == null : this.getCreate_time().equals(other.getCreate_time()))
            && (this.getUpdater() == null ? other.getUpdater() == null : this.getUpdater().equals(other.getUpdater()))
            && (this.getUpdate_time() == null ? other.getUpdate_time() == null : this.getUpdate_time().equals(other.getUpdate_time()))
            && (this.getCar_no() == null ? other.getCar_no() == null : this.getCar_no().equals(other.getCar_no()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getTask_no() == null) ? 0 : getTask_no().hashCode());
        result = prime * result + ((getLocation_code() == null) ? 0 : getLocation_code().hashCode());
        result = prime * result + ((getTask_type() == null) ? 0 : getTask_type().hashCode());
        result = prime * result + ((getPallet_code() == null) ? 0 : getPallet_code().hashCode());
        result = prime * result + ((getProt_no() == null) ? 0 : getProt_no().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getUdf01() == null) ? 0 : getUdf01().hashCode());
        result = prime * result + ((getUdf02() == null) ? 0 : getUdf02().hashCode());
        result = prime * result + ((getUdf03() == null) ? 0 : getUdf03().hashCode());
        result = prime * result + ((getUdf04() == null) ? 0 : getUdf04().hashCode());
        result = prime * result + ((getUdf05() == null) ? 0 : getUdf05().hashCode());
        result = prime * result + ((getCreator() == null) ? 0 : getCreator().hashCode());
        result = prime * result + ((getCreate_time() == null) ? 0 : getCreate_time().hashCode());
        result = prime * result + ((getUpdater() == null) ? 0 : getUpdater().hashCode());
        result = prime * result + ((getUpdate_time() == null) ? 0 : getUpdate_time().hashCode());
        result = prime * result + ((getCar_no() == null) ? 0 : getCar_no().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", task_no=").append(task_no);
        sb.append(", location_code=").append(location_code);
        sb.append(", task_type=").append(task_type);
        sb.append(", pallet_code=").append(pallet_code);
        sb.append(", prot_no=").append(prot_no);
        sb.append(", status=").append(status);
        sb.append(", udf01=").append(udf01);
        sb.append(", udf02=").append(udf02);
        sb.append(", udf03=").append(udf03);
        sb.append(", udf04=").append(udf04);
        sb.append(", udf05=").append(udf05);
        sb.append(", creator=").append(creator);
        sb.append(", create_time=").append(create_time);
        sb.append(", updater=").append(updater);
        sb.append(", update_time=").append(update_time);
        sb.append(", car_no=").append(car_no);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}