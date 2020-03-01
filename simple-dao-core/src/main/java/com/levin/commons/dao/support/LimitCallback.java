package com.levin.commons.dao.support;

/**
 * 返回记录数限制语句
 */
public interface LimitCallback {

    /**
     * 根据参数值返回语句
     *
     * @param rowStart
     * @param rowCount
     * @return
     */
    String limit(int rowStart, int rowCount,
                 StringBuilder selectOrUpdateStatement, StringBuffer fromStatement, StringBuffer whereStatement, StringBuffer havingStatement);
}
