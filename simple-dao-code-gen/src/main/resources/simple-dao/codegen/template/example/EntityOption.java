package ${CLASS_PACKAGE_NAME};

/**
 * module table prefix
 * <p>
 * eg.
 * <p>
 * //@Entity(name = EntityOption.PREFIX + "exam_tasks")
 * //@Table(name = EntityOption.PREFIX + "exam_tasks")
 * //Auto gen by simple-dao-codegen ${now}
 */
public interface EntityOption {

    /**
     * JPA/Hibernate table name prefix
     */
    String PREFIX = "${modulePackageName}-";


    String MAINTAIN_ACTION = "管理";

    /**
     *
     */
    String CREATE_ACTION = "新增";
    String BATCH_CREATE_ACTION = "批量新增";

    /**
     *
     */
    String QUERY_ACTION = "查询";

    /**
     *
     */
    String VIEW_DETAIL_ACTION = "查看详情";

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
