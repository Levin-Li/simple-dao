package com.levin.commons.dao.codegen.db;

import com.levin.commons.dao.codegen.db.converter.ColumnTypeConverter;
import com.levin.commons.dao.codegen.db.util.FieldUtil;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.util.StringUtils;

/**
 * 表字段信息
 */
public class ColumnDefinition {

    /**
     * 数据库字段名
     */
    private String columnName;
    /**
     * 数据库类型
     */
    private String type;
    /**
     * 是否自增
     */
    private Boolean isIdentity;
    /**
     * 是否主键
     */
    private Boolean isPk;
    /**
     * 字段注释
     */
    private String comment;
    /**
     * 字段长度
     */
    private Integer maxLength;
    /**
     * 小数位长度
     */
    private Integer scale;

    /**
     * 字段是否允许为null
     */
    private boolean isNullable = false;

    /**
     * 数据库字段名首字母小写
     * @return
     */
    public String getColumnNameLF() {
        return FieldUtil.lowerFirstLetter(this.columnName);
    }

    public String getLabel() {
        return StringUtils.hasLength(comment) ? comment : columnName;
    }

    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }

    public void setScale(Integer scale) {
        this.scale = scale;
    }

    public Integer getMaxLength() {
        return maxLength;
    }

    public Integer getScale() {
        return scale;
    }

    /**
     * 获得基本类型,int,float
     *
     * @return 返回基本类型
     */

    public String getFieldType() {
        return getColumnTypeConverter().convertType(type);
    }

    /**
     * 获得装箱类型,Integer,Float
     *
     * @return 返回装箱类型
     */

    public String getFieldTypeBox() {
        return getColumnTypeConverter().convertTypeBox(getType());
    }

    /**
     * 是否是自增主键
     *
     * @return true, 是自增主键
     */
    public boolean getIsIdentityPk() {
        return getIsPk() && getIsIdentity();
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getIsIdentity() {
        return isIdentity;
    }

    public void setIsIdentity(Boolean isIdentity) {
        this.isIdentity = isIdentity;
    }

    public Boolean getIsPk() {
        return isPk;
    }

    public void setIsPk(Boolean isPk) {
        this.isPk = isPk;
    }


    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        if (comment == null) {
            comment = "";
        }
        this.comment = comment;
    }

    public ColumnTypeConverter getColumnTypeConverter() {
        throw new UnsupportedOperationException("未覆盖com.levin.commons.dao.codegen.db.ColumnDefinition.getColumnTypeConverter方法");
    }

    public Boolean getIsNullable() {
        return isNullable;
    }

    public void setIsNullable(Boolean isNullable) {
        this.isNullable = isNullable;
    }
}
