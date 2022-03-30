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
     * module name
     */
    String NAME = "插件模块-" + ID;

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
     * admin api path
     * 注意路径必须以 / 结尾
     */
    String ADMIN_API_PATH = API_PATH + "admin/";

    /**
     * app/h5 api path
     * 注意路径必须以 / 结尾
     */
    String APP_API_PATH = API_PATH + "app/";

    /**
     * admin ui path
     * 注意路径必须以 / 结尾
     */
    String ADMIN_UI_PATH = BASE_PATH + "admin/";

    /**
     * h5 ui path
     * 注意路径必须以 / 结尾
     */
    String H5_UI_PATH = BASE_PATH + "h5/";

    /**
     *
     * 注意路径必须以 / 结尾
     */
    String HTTP_REQUEST_INFO_RESOLVER = PLUGIN_PREFIX + "HttpRequestInfoResolver";


}
