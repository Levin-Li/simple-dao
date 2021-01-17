package com.levin.commons.dao.support;

import com.levin.commons.dao.PageOption;
import com.levin.commons.dao.Paging;
import com.levin.commons.dao.annotation.Ignore;
import com.levin.commons.service.domain.ServiceRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;



/**
 * @author llw
 */

@Data
@Accessors(chain = true)
//@Builder
@FieldNameConstants
public class PagingQueryReq
        implements Paging, ServiceRequest {

    @Ignore
    @Schema(description = "是否查询总记录数")
    @PageOption(value = PageOption.Type.RequireTotals, remark = "通过注解设置是否查询总记录数，被标注字段值为 true 或是非空对象")
    boolean isRequireTotals = false;

    @Ignore
    @Schema(description = "是否查询结果集")
    @PageOption(value = PageOption.Type.RequireResultList, remark = "通过注解设置是否返回结果集列表，被标注字段值为 true 或是非空对象")
    boolean isRequireResultList = true;

    @Ignore
    @Schema(description = "页面索引")
    @PageOption(value = PageOption.Type.PageIndex, remark = "通过注解设置页面索引")
    int pageIndex = 1;

    @Ignore
    @Schema(description = "页面大小")
    @PageOption(value = PageOption.Type.PageSize, remark = "通过注解设置分页大小")
    int pageSize = 20;

    @Schema(description = "是否使用缓存")
    @Ignore
    Boolean fromCache;

    public PagingQueryReq() {
    }

    public PagingQueryReq(int pageIndex, int pageSize) {
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
    }

}
