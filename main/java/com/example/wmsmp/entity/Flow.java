package com.example.wmsmp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 流水表 王力勋
 *
 * @TableName t_flow
 */
@TableName(value = "t_flow")
@Data
public class Flow implements Serializable {
    /**
     * 流水主键
     */
    @TableId(value = "flow_id", type = IdType.AUTO)
    private Integer flow_id;

    /**
     * 流水描述
     */
    @TableField(value = "flow_title")
    private String flow_title;

    /**
     * 流水类型
     */
    @TableField(value = "flow_type")
    private String flow_type;

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
     * 批次
     */
    @TableField(value = "batch")
    private String batch;

    /**
     * 载具编号
     */
    @TableField(value = "pallet_code")
    private String pallet_code;

    /**
     * 原托盘编号
     */
    @TableField(exist = false)
    private String location_code;

    /**
     * 操作数量
     */
    @TableField(value = "cmd_count")
    private int cmd_count;

    /**
     * 操作后数量
     */
    @TableField(value = "after_count")
    private int after_count;

    /**
     * 订单编号
     */
    @TableField(value = "order_id")
    private String order_id;

    /**
     * 格子号
     */
    @TableField(value = "cell_id")
    private String cell_id;

    /**
     * 接口
     */
    @TableField(value = "url")
    private String url;

    /**
     * 创建人
     */
    @TableField(value = "creator")
    private String creator;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date create_time;

    @TableField(exist = false)
    private String startTime;

    @TableField(exist = false)
    private String endTime;

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
        Flow other = (Flow) that;
        return (this.getFlow_id() == null ? other.getFlow_id() == null : this.getFlow_id().equals(other.getFlow_id())) && (this.getFlow_title() == null ? other.getFlow_title() == null : this.getFlow_title().equals(other.getFlow_title())) && (this.getFlow_type() == null ? other.getFlow_type() == null : this.getFlow_type().equals(other.getFlow_type())) && (this.getMaterial_code() == null ? other.getMaterial_code() == null : this.getMaterial_code().equals(other.getMaterial_code())) && (this.getMaterial_name() == null ? other.getMaterial_name() == null : this.getMaterial_name().equals(other.getMaterial_name())) && (this.getBatch() == null ? other.getBatch() == null : this.getBatch().equals(other.getBatch())) && (this.getPallet_code() == null ? other.getPallet_code() == null : this.getPallet_code().equals(other.getPallet_code()))  && ( (this.getOrder_id() == null ? other.getOrder_id() == null : this.getOrder_id().equals(other.getOrder_id())) && (this.getUrl() == null ? other.getUrl() == null : this.getUrl().equals(other.getUrl())) && (this.getCreator() == null ? other.getCreator() == null : this.getCreator().equals(other.getCreator())) && (this.getCreate_time() == null ? other.getCreate_time() == null : this.getCreate_time().equals(other.getCreate_time())));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getFlow_id() == null) ? 0 : getFlow_id().hashCode());
        result = prime * result + ((getFlow_title() == null) ? 0 : getFlow_title().hashCode());
        result = prime * result + ((getFlow_type() == null) ? 0 : getFlow_type().hashCode());
        result = prime * result + ((getMaterial_code() == null) ? 0 : getMaterial_code().hashCode());
        result = prime * result + ((getMaterial_name() == null) ? 0 : getMaterial_name().hashCode());
        result = prime * result + ((getBatch() == null) ? 0 : getBatch().hashCode());
        result = prime * result + ((getPallet_code() == null) ? 0 : getPallet_code().hashCode());
        result = prime * result + ((getOrder_id() == null) ? 0 : getOrder_id().hashCode());
        result = prime * result + ((getUrl() == null) ? 0 : getUrl().hashCode());
        result = prime * result + ((getCreator() == null) ? 0 : getCreator().hashCode());
        result = prime * result + ((getCreate_time() == null) ? 0 : getCreate_time().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", flow_id=").append(flow_id);
        sb.append(", flow_title=").append(flow_title);
        sb.append(", flow_type=").append(flow_type);
        sb.append(", material_code=").append(material_code);
        sb.append(", material_name=").append(material_name);
        sb.append(", batch=").append(batch);
        sb.append(", pallet_code=").append(pallet_code);
        sb.append(", cmd_count=").append(cmd_count);
        sb.append(", after_count=").append(after_count);
        sb.append(", order_id=").append(order_id);
        sb.append(", url=").append(url);
        sb.append(", creator=").append(creator);
        sb.append(", create_time=").append(create_time);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}
