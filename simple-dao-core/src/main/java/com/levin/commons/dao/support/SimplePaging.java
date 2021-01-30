package com.levin.commons.dao.support;

import com.levin.commons.dao.PageOption;
import com.levin.commons.dao.Paging;
import com.levin.commons.dao.annotation.Ignore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;

@Data
@Accessors(chain = true)
//@Builder
@FieldNameConstants
@NoArgsConstructor
@AllArgsConstructor
public class SimplePaging
        implements Paging, Serializable {

    @Ignore
    @Schema(description = "是否查询总记录数")
    boolean requireTotals = false;

    @Ignore
    @Schema(description = "是否查询结果集")
    boolean requireResultList = true;

    @Ignore
    @Schema(description = "页面索引")
    int pageIndex = 1;

    @Ignore
    @Schema(description = "页面大小")
    int pageSize = 20;


}
