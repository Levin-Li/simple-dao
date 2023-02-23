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
     * module name
     */
    String NAME = "请填写模块名称";

    /**
     * module desc
     */
    String DESC = "请填写模块描述";

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
     * 缓存分隔符
     */
    String CACHE_DELIM = ":";

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
     * websocket
     * 注意路径必须以 / 结尾
     */
    String WS_PATH = BASE_PATH + "ws/";


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
     * 注意路径必须以 / 结尾
     */
    String HTTP_REQUEST_INFO_RESOLVER = PLUGIN_PREFIX + "HttpRequestInfoResolver";

}
