package com.example.wmsmp.entity.task.AGV;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName t_agv_location_map
 */
@TableName(value ="t_agv_location_map")
@Data
public class AgvLocationMap implements Serializable {
    /**
     * 
     */
    @TableId(value = "location_code")
    private String location_code;

    /**
     * 
     */
    @TableField(value = "id")
    private Integer id;

    /**
     * 
     */
    @TableField(value = "location_code_x")
    private Integer location_code_x;

    /**
     * 
     */
    @TableField(value = "location_code_y")
    private Integer location_code_y;

    /**
     * 
     */
    @TableField(value = "location_code_z")
    private Object location_code_z;

    /**
     * 
     */
    @TableField(value = "location_code_d")
    private Integer location_code_d;

    /**
     * 
     */
    @TableField(value = "location_area")
    private Integer location_area;

    /**
     * 
     */
    @TableField(value = "pallet_code")
    private String pallet_code;

    /**
     * 
     */
    @TableField(value = "warehouse")
    private String warehouse;

    /**
     * 
     */
    @TableField(value = "warehouse_type")
    private String warehouse_type;

    /**
     * 
     */
    @TableField(value = "height")
    private String height;

    /**
     * 
     */
    @TableField(value = "volume")
    private Integer volume;

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
        AgvLocationMap other = (AgvLocationMap) that;
        return (this.getLocation_code() == null ? other.getLocation_code() == null : this.getLocation_code().equals(other.getLocation_code()))
            && (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getLocation_code_x() == null ? other.getLocation_code_x() == null : this.getLocation_code_x().equals(other.getLocation_code_x()))
            && (this.getLocation_code_y() == null ? other.getLocation_code_y() == null : this.getLocation_code_y().equals(other.getLocation_code_y()))
            && (this.getLocation_code_z() == null ? other.getLocation_code_z() == null : this.getLocation_code_z().equals(other.getLocation_code_z()))
            && (this.getLocation_code_d() == null ? other.getLocation_code_d() == null : this.getLocation_code_d().equals(other.getLocation_code_d()))
            && (this.getLocation_area() == null ? other.getLocation_area() == null : this.getLocation_area().equals(other.getLocation_area()))
            && (this.getPallet_code() == null ? other.getPallet_code() == null : this.getPallet_code().equals(other.getPallet_code()))
            && (this.getWarehouse() == null ? other.getWarehouse() == null : this.getWarehouse().equals(other.getWarehouse()))
            && (this.getWarehouse_type() == null ? other.getWarehouse_type() == null : this.getWarehouse_type().equals(other.getWarehouse_type()))
            && (this.getHeight() == null ? other.getHeight() == null : this.getHeight().equals(other.getHeight()))
            && (this.getVolume() == null ? other.getVolume() == null : this.getVolume().equals(other.getVolume()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getUdf01() == null ? other.getUdf01() == null : this.getUdf01().equals(other.getUdf01()))
            && (this.getUdf02() == null ? other.getUdf02() == null : this.getUdf02().equals(other.getUdf02()))
            && (this.getUdf03() == null ? other.getUdf03() == null : this.getUdf03().equals(other.getUdf03()))
            && (this.getUdf04() == null ? other.getUdf04() == null : this.getUdf04().equals(other.getUdf04()))
            && (this.getUdf05() == null ? other.getUdf05() == null : this.getUdf05().equals(other.getUdf05()))
            && (this.getCreator() == null ? other.getCreator() == null : this.getCreator().equals(other.getCreator()))
            && (this.getCreate_time() == null ? other.getCreate_time() == null : this.getCreate_time().equals(other.getCreate_time()))
            && (this.getUpdater() == null ? other.getUpdater() == null : this.getUpdater().equals(other.getUpdater()))
            && (this.getUpdate_time() == null ? other.getUpdate_time() == null : this.getUpdate_time().equals(other.getUpdate_time()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getLocation_code() == null) ? 0 : getLocation_code().hashCode());
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getLocation_code_x() == null) ? 0 : getLocation_code_x().hashCode());
        result = prime * result + ((getLocation_code_y() == null) ? 0 : getLocation_code_y().hashCode());
        result = prime * result + ((getLocation_code_z() == null) ? 0 : getLocation_code_z().hashCode());
        result = prime * result + ((getLocation_code_d() == null) ? 0 : getLocation_code_d().hashCode());
        result = prime * result + ((getLocation_area() == null) ? 0 : getLocation_area().hashCode());
        result = prime * result + ((getPallet_code() == null) ? 0 : getPallet_code().hashCode());
        result = prime * result + ((getWarehouse() == null) ? 0 : getWarehouse().hashCode());
        result = prime * result + ((getWarehouse_type() == null) ? 0 : getWarehouse_type().hashCode());
        result = prime * result + ((getHeight() == null) ? 0 : getHeight().hashCode());
        result = prime * result + ((getVolume() == null) ? 0 : getVolume().hashCode());
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
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", location_code=").append(location_code);
        sb.append(", id=").append(id);
        sb.append(", location_code_x=").append(location_code_x);
        sb.append(", location_code_y=").append(location_code_y);
        sb.append(", location_code_z=").append(location_code_z);
        sb.append(", location_code_d=").append(location_code_d);
        sb.append(", location_area=").append(location_area);
        sb.append(", pallet_code=").append(pallet_code);
        sb.append(", warehouse=").append(warehouse);
        sb.append(", warehouse_type=").append(warehouse_type);
        sb.append(", height=").append(height);
        sb.append(", volume=").append(volume);
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
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}