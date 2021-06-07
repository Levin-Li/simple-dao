package com.levin.commons.dao.support;

import com.levin.commons.dao.PageOption;
import com.levin.commons.dao.annotation.Ignore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import java.beans.Transient;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 分页数据持有
 */

@Data
@Accessors(chain = true)
//@Builder
@FieldNameConstants
public class PagingData<T> implements Serializable {

    @Ignore
    @Schema(description = "总记录数")
    @PageOption(value = PageOption.Type.RequireTotals, remark = "查询结果会自动注入这个字段")
    long totals = -1;

    @Ignore
    @Schema(description = "页面编号")
    @PageOption(value = PageOption.Type.PageIndex, remark = "通过注解设置分页索引")
    int pageIndex = -1;

    @Ignore
    @Schema(description = "分页大小")
    @PageOption(value = PageOption.Type.PageSize, remark = "通过注解设置分页大小")
    int pageSize = -1;

    @Ignore
    @Schema(description = "数据集")
    @PageOption(value = PageOption.Type.RequireResultList, remark = "查询结果会自动注入这个字段")
    List<T> records;

    @Ignore
    @Schema(description = "扩展数据")
    Map<String, Object> extra;

    @Transient
    public T getFirst() {
        return isEmpty() ? null : records.get(0);
    }

    @Transient
    public boolean isEmpty() {
        return records == null || records.isEmpty();
    }

    public PagingData() {
    }

}
