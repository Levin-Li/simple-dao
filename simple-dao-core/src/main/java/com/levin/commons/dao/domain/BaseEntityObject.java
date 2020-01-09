package com.levin.commons.dao.domain;

import java.io.Serializable;
import java.util.Date;

public interface BaseEntityObject<ID extends Serializable>
        extends
        EntityObject<ID>,
        BaseObject<ID> {

    /**
     * 对象创建时间
     *
     * @return createTime
     */
    Date getCreateTime();


    /**
     * 最后更新时间
     *
     * @return
     */

    Date getLastUpdateTime();

    /**
     * 获取对象描述
     *
     * @return desc
     */
    String getRemark();

}
