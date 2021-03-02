package ${modulePackageName};

/**
 * module option
 * <p>
 * eg.
 * <p>
 * //Auto gen by simple-dao-codegen ${.now}
 */
public interface ModuleOption {

    /**
     * module package name
     */
    String PACKAGE_NAME = "${modulePackageName}";

    /**
     * module id
     */
    String ID = PACKAGE_NAME;

    /**
     * api path
     * 注意路径必须以 / 结尾
     */
    String API_PATH = ID + "/api/";

    /**
     * admin path
     * 注意路径必须以 / 结尾
     */
    String ADMIN_PATH = ID + "/admin/";

    /**
     * h5 path
     * 注意路径必须以 / 结尾
     */
    String H5_PATH = ID + "/h5/";
}
