package ${CLASS_PACKAGE_NAME};

/**
 * module table prefix
 *
 * eg.
 *
 * //@Entity(name = ModuleTableOption.PREFIX + "exam_tasks")
 * //@Table(name = ModuleTableOption.PREFIX + "exam_tasks")
 * //Auto gen by simple-dao-codegen ${.now}
 *
 */
public interface TableOption {

    /**
     * JPA/Hibernate table name prefix
     */
    String PREFIX = "${modulePackageName}-";

}
