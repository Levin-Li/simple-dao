package com.levin.commons.dao.support;

import com.levin.commons.dao.PageOption;
import com.levin.commons.dao.Paging;
import com.levin.commons.dao.annotation.Ignore;
import com.levin.commons.service.domain.ServiceReq;
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
public class PagingQueryReq extends SimplePaging
        implements Paging, ServiceReq {

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
