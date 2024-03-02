package com.levin.commons.dao;

/**
 * 实体操作常量
 */
public interface EntityOpConst {

    /**
     * 普通实体数据
     */
    String BIZ_TYPE_NAME = "业务数据";

    /**
     * 普通实体数据
     */
    @Deprecated
    String ENTITY_TYPE_NAME = BIZ_TYPE_NAME;

    /**
     * 通用功能，通常登录就可使用
     */
    String COMMON_TYPE_NAME = "通用数据";

    /**
     * 需要有管理员角色
     */
    String SYS_TYPE_NAME = "系统数据";

    /**
     * 需要有管理员角色
     */
    String PLATFORM_TYPE_NAME = "平台数据";

    /**
     * 需要有工程实施专家角色
     */
    String EXPERT_TYPE_NAME = "专家数据";

    /**
     *
     */
    String MAINTAIN_ACTION = "管理";

    /**
     *
     */
    String QUERY_ACTION = "查询";
    String QUERY_LIST_ACTION = "查询列表";

    /**
     *
     */
    String STAT_ACTION = "统计";

    /**
     *
     */
    String CLEAR_CACHE_ACTION = "清楚缓存";

    /**
     *
     */
    String VIEW_DETAIL_ACTION = "查看详情";

    /**
     *
     */
    String CREATE_ACTION = "新增";
    String BATCH_CREATE_ACTION = "批量新增";

    /**
     *
     */
    String UPDATE_ACTION = "更新";
    String BATCH_UPDATE_ACTION = "批量更新";

    /**
     *
     */
    String DELETE_ACTION = "删除";
    String BATCH_DELETE_ACTION = "批量删除";

}
