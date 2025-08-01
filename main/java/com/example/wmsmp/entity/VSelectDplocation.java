package com.example.wmsmp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 
 * @TableName v_select_dplocation
 */
@TableName(value ="v_select_dplocation")
@Data
public class VSelectDplocation implements Serializable {
    /**
     * 
     */
    @TableField(value = "location_code")
    private String location_code;

    /**
     * 
     */
    @TableField(value = "material_code")
    private String material_code;

    /**
     * 
     */
    @TableField(value = "batch")
    private String batch;

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
        VSelectDplocation other = (VSelectDplocation) that;
        return (this.getLocation_code() == null ? other.getLocation_code() == null : this.getLocation_code().equals(other.getLocation_code()))
            && (this.getMaterial_code() == null ? other.getMaterial_code() == null : this.getMaterial_code().equals(other.getMaterial_code()))
            && (this.getBatch() == null ? other.getBatch() == null : this.getBatch().equals(other.getBatch()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getLocation_code() == null) ? 0 : getLocation_code().hashCode());
        result = prime * result + ((getMaterial_code() == null) ? 0 : getMaterial_code().hashCode());
        result = prime * result + ((getBatch() == null) ? 0 : getBatch().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", location_code=").append(location_code);
        sb.append(", material_code=").append(material_code);
        sb.append(", batch=").append(batch);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}