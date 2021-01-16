package com.levin.commons.dao.support;

import com.levin.commons.dao.PageOption;
import com.levin.commons.dao.annotation.Ignore;
import com.levin.commons.service.domain.Desc;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import java.beans.Transient;
import java.io.Serializable;
import java.util.List;

/**
 * 查询响应结果
 */

@Data
@Accessors(chain = true)
//@Builder
@FieldNameConstants
public class PagingQueryResp<T> implements Serializable {

    @Ignore
    @Desc("总记录数-用于支持分页查询")
    @PageOption(value = PageOption.Type.RequireTotals, remark = "查询结果会自动注入这个字段")
    long totals = -1;

    @Ignore
    @Desc("数据结果")
    @PageOption(value = PageOption.Type.RequireResultList, remark = "查询结果会自动注入这个字段")
    List<T> records;

    @Transient
    public T getFirst() {
        return isEmpty() ? null : records.get(0);
    }

    @Transient
    public boolean isEmpty() {
        return records == null || records.isEmpty();
    }

    public PagingQueryResp() {
    }
}
