package ${CLASS_PACKAGE_NAME};

/**
 * module table prefix
 * <p>
 * eg.
 * <p>
 * @Entity(name = EntityConst.PREFIX + "exam_tasks")
 * @Table(name = EntityConst.PREFIX + "exam_tasks")
 * @author Auto gen by simple-dao-codegen, @time: ${.now}, 请不要修改和删除此行内容。
 * 代码生成哈希校验码：[], 请不要修改和删除此行内容。
 */
public interface EntityConst
        extends com.levin.commons.dao.EntityOpConst //继承默认的定义
{

    /**
     * JPA/Hibernate table name prefix
     */
    String PREFIX = "${modulePackageName}-";

 }
