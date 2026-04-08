package com.levin.commons.dao.support;

import com.levin.commons.dao.annotation.Ignore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import jakarta.persistence.Id;
import java.io.Serializable;

@Data
@Accessors(chain = true)
//@Builder
@FieldNameConstants
@NoArgsConstructor
@AllArgsConstructor
public class CacheKey
        implements Serializable {

    @Ignore
    @Schema(title = "缓存后缀")
    @Id
    String keySuffix;

    @Ignore
    @Schema(title = "缓存key", description = "完整的缓存key")
    String cacheKey;
}
