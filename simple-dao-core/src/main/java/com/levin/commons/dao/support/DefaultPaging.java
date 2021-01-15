package com.levin.commons.dao.support;

import com.levin.commons.dao.Paging;
import com.levin.commons.dao.annotation.Ignore;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;

/**
 *
 */

@Data
@Accessors(chain = true)
@Builder
@FieldNameConstants
public class DefaultPaging
        implements Paging, Serializable {

//    @Ignore
//    boolean isRequireTotals;
//
//    @Ignore
//    long totals;
//
//    @Ignore
//    int pageCount;

    @Ignore
    int pageIndex = 1;

    @Ignore
    int pageSize = 20;

    public DefaultPaging() {
    }

    public DefaultPaging(int pageIndex, int pageSize) {
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
    }

}
