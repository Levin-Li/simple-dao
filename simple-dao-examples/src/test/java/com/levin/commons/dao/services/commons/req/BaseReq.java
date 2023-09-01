package com.levin.commons.dao.services.commons.req;

import com.levin.commons.service.domain.ServiceReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.springframework.util.StringUtils;

/**
 * 基本查询对象
 *
 * @author Auto gen by simple-dao-codegen, @time: 2023年8月6日 下午6:02:03, 代码生成哈希校验码：[e5e2c019fc9c0b3adaf8aa627cbb483b]，请不要修改和删除此行内容。
 */
@Schema(title = "基本查询对象")
@Data
@Accessors(chain = true)
@FieldNameConstants
public abstract class BaseReq implements ServiceReq {

    /**
     * 是否非空
     *
     * @param value
     * @return
     */
    protected boolean isNotBlank(Object value) {
        return value != null
                && (!(value instanceof CharSequence) || StringUtils.hasText((CharSequence) value));
    }
}
