package ${CLASS_PACKAGE_NAME};

/**
 * module table prefix
 * <p>
 * eg.
 * <p>
 * //@Entity(name = EntityConst.PREFIX + "exam_tasks")
 * //@Table(name = EntityConst.PREFIX + "exam_tasks")
 * //Auto gen by simple-dao-codegen ${now}
 */
public interface EntityConst {

    /**
     * JPA/Hibernate table name prefix
     */
    String PREFIX = "${modulePackageName}-";

    /**
     *
     */
    String TYPE_NAME = "实体数据";

    /**
     *
     */
    String MAINTAIN_ACTION = "管理";

    /**
     *
     */
    String QUERY_ACTION = "查询";

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
