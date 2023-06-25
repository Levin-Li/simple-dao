package ${CLASS_PACKAGE_NAME};

/**
 * module table prefix
 * <p>
 * eg.
 * <p>
 * //@Entity(name = EntityConst.PREFIX + "exam_tasks")
 * //@Table(name = EntityConst.PREFIX + "exam_tasks")
 * //Auto gen by simple-dao-codegen ${now}
 * 代码生成哈希校验码：[]
 */
public interface EntityConst
        extends com.levin.commons.dao.EntityOpConst //继承默认的定义
{

    /**
     * JPA/Hibernate table name prefix
     */
    String PREFIX = "${modulePackageName}-";

 }
