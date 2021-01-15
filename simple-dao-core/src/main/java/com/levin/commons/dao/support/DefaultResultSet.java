package com.levin.commons.dao.support;

import com.levin.commons.dao.PageOption;
import com.levin.commons.dao.Paging;
import com.levin.commons.dao.annotation.Ignore;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;
import java.util.List;

/**
 *
 */

@Data
@Accessors(chain = true)
@Builder
@FieldNameConstants
public class DefaultResultSet<T>
        implements Serializable {

    @Ignore
    @PageOption(value = PageOption.Type.RecordTotals, condition = "totals < 0")
    int totals = -1;

    @Ignore
    @PageOption(value = PageOption.Type.ResultList, condition = "totals < 0")
    List<T> data;

}
