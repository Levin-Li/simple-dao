package com.levin.commons.dao.support;

import com.levin.commons.dao.PageOption;
import com.levin.commons.dao.annotation.Ignore;
import com.levin.commons.service.domain.Desc;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;

/**
 * 查询响应结果
 */

@Data
@Accessors(chain = true)
//@Builder
@FieldNameConstants
public class QueryResponse<T> implements Serializable {

    @Ignore
    @Desc("返回码，0 为正确，其它为异常情况")
    int code;

    @Ignore
    @Desc("提示消息-通常用于展示给客户看")
    String msg;

    @Ignore
    @Desc("详细信息-通常用于辅助调试")
    String detailMsg;

    @Ignore
    @Desc("总记录数-用于支持分页查询")
    @PageOption(value = PageOption.Type.RequireTotals, remark = "查询结果会自动注入这个字段")
    long totals = -1;

    @Ignore
    @Desc("数据")
    @PageOption(value = PageOption.Type.RequireResultList, remark = "查询结果会自动注入这个字段")
    T data;

    public QueryResponse() {
    }
}
