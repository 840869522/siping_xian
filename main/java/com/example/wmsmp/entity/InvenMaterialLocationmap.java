package com.example.wmsmp.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName v_inven_material_locationmap
 */
@TableName(value ="v_inven_material_locationmap")
@Data
public class InvenMaterialLocationmap implements Serializable {
    /**
     * 物料编号
     */
    @TableField(value = "material_code")
    private String material_code;

    /**
     * 物料名称
     */
    @TableField(value = "material_name")
    private String material_name;

    /**
     * 载具编号
     */
    @TableField(value = "pallet_code")
    private String pallet_code;

    /**
     * 载具编号
     */
    @TableField(value = "location_code")
    private String location_code;

    /**
     * 批次
     */
    @TableField(value = "batch")
    private String batch;

    /**
     * 库存数量
     */
    @TableField(value = "inventory_count")
    private Object inventory_count;

    /**
     * 冻结数量
     */
    @TableField(value = "frozen_count")
    private Object frozen_count;

    /**
     * 规格
     */
    @TableField(value = "material_spec")
    private String material_spec;

    /**
     * 单位
     */
    @TableField(value = "material_unit")
    private String material_unit;

    /**
     * 保质期（天）
     */
    @TableField(value = "quality")
    private Integer quality;

    /**
     * 最低库存
     */
    @TableField(value = "min_stock")
    private Object min_stock;

    /**
     * 状态
     */
    @TableField(value = "status")
    private String status;

    /**
     * 所属区域
     */
    @TableField(value = "warehouse_type")
    private String warehouse_type;

    /**
     * 创建时间
     */
    @TableField(value = "create_time",fill = FieldFill.INSERT)
    private Date create_time;

    /**
     * 创建者
     */
    @TableField(value = "creator")
    private String creator;

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
        InvenMaterialLocationmap other = (InvenMaterialLocationmap) that;
        return (this.getMaterial_code() == null ? other.getMaterial_code() == null : this.getMaterial_code().equals(other.getMaterial_code()))
            && (this.getMaterial_name() == null ? other.getMaterial_name() == null : this.getMaterial_name().equals(other.getMaterial_name()))
            && (this.getPallet_code() == null ? other.getPallet_code() == null : this.getPallet_code().equals(other.getPallet_code()))
            && (this.getLocation_code() == null ? other.getLocation_code() == null : this.getLocation_code().equals(other.getLocation_code()))
            && (this.getBatch() == null ? other.getBatch() == null : this.getBatch().equals(other.getBatch()))
            && (this.getInventory_count() == null ? other.getInventory_count() == null : this.getInventory_count().equals(other.getInventory_count()))
            && (this.getFrozen_count() == null ? other.getFrozen_count() == null : this.getFrozen_count().equals(other.getFrozen_count()))
            && (this.getMaterial_spec() == null ? other.getMaterial_spec() == null : this.getMaterial_spec().equals(other.getMaterial_spec()))
            && (this.getMaterial_unit() == null ? other.getMaterial_unit() == null : this.getMaterial_unit().equals(other.getMaterial_unit()))
            && (this.getQuality() == null ? other.getQuality() == null : this.getQuality().equals(other.getQuality()))
            && (this.getMin_stock() == null ? other.getMin_stock() == null : this.getMin_stock().equals(other.getMin_stock()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getWarehouse_type() == null ? other.getWarehouse_type() == null : this.getWarehouse_type().equals(other.getWarehouse_type()))
            && (this.getCreate_time() == null ? other.getCreate_time() == null : this.getCreate_time().equals(other.getCreate_time()))
            && (this.getCreator() == null ? other.getCreator() == null : this.getCreator().equals(other.getCreator()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getMaterial_code() == null) ? 0 : getMaterial_code().hashCode());
        result = prime * result + ((getMaterial_name() == null) ? 0 : getMaterial_name().hashCode());
        result = prime * result + ((getPallet_code() == null) ? 0 : getPallet_code().hashCode());
        result = prime * result + ((getLocation_code() == null) ? 0 : getLocation_code().hashCode());
        result = prime * result + ((getBatch() == null) ? 0 : getBatch().hashCode());
        result = prime * result + ((getInventory_count() == null) ? 0 : getInventory_count().hashCode());
        result = prime * result + ((getFrozen_count() == null) ? 0 : getFrozen_count().hashCode());
        result = prime * result + ((getMaterial_spec() == null) ? 0 : getMaterial_spec().hashCode());
        result = prime * result + ((getMaterial_unit() == null) ? 0 : getMaterial_unit().hashCode());
        result = prime * result + ((getQuality() == null) ? 0 : getQuality().hashCode());
        result = prime * result + ((getMin_stock() == null) ? 0 : getMin_stock().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getWarehouse_type() == null) ? 0 : getWarehouse_type().hashCode());
        result = prime * result + ((getCreate_time() == null) ? 0 : getCreate_time().hashCode());
        result = prime * result + ((getCreator() == null) ? 0 : getCreator().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", material_code=").append(material_code);
        sb.append(", material_name=").append(material_name);
        sb.append(", pallet_no=").append(pallet_code);
        sb.append(", location_code=").append(location_code);
        sb.append(", batch=").append(batch);
        sb.append(", inventory_count=").append(inventory_count);
        sb.append(", frozen_count=").append(frozen_count);
        sb.append(", material_spec=").append(material_spec);
        sb.append(", material_unit=").append(material_unit);
        sb.append(", quality=").append(quality);
        sb.append(", min_stock=").append(min_stock);
        sb.append(", status=").append(status);
        sb.append(", warehouse_type=").append(warehouse_type);
        sb.append(", create_time=").append(create_time);
        sb.append(", creator=").append(creator);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}