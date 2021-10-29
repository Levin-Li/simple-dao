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
     * module prefix
     */
    String ID_PREFIX = ID + ".";

    /**
     * module prefix
     */
    String PLUGIN_PREFIX = "plugin." + ID_PREFIX;


    /**
     * version
     */
    String VERSION = "V1";

    /**
     * base path
     * 注意路径必须以 / 开头和结尾
     */
    String BASE_PATH = "/" + ID + "/" + VERSION + "/";

    /**
     * api path
     * 注意路径必须以 / 结尾
     */
    String API_PATH = BASE_PATH + "api/";

    /**
     * admin path
     * 注意路径必须以 / 结尾
     */
    String ADMIN_PATH = BASE_PATH + "admin/";

    /**
     * h5 path
     * 注意路径必须以 / 结尾
     */
    String H5_PATH = BASE_PATH + "h5/";

}
