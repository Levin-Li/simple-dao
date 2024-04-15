package ${modulePackageName};

/**
 * module option
 * <p>
 * eg.
 * <p>
 * @author Auto gen by simple-dao-codegen, @time: ${.now}, 代码生成哈希校验码：[]，请不要修改和删除此行内容。
 * 
 */
public interface ModuleOption {

    /**
     * 主版本号
     */
    String MAJOR = "1";

    /**
     * 子版本号
     */
    String MINOR = "0";

    /**
     * 修订版本号
     */
    String REVISION = "0";

    /**
     * 版本号
     */
    String VERSION_NAME = MAJOR + "." + MINOR + "." + REVISION;

    /**
     * module name
     */
    String NAME = "${projectName";

    /**
     * module desc
     */
    String DESC = "${projectDesc}";

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
     * api version
     */
    String API_VERSION = "V1";

    /**
     * base path
     * 注意路径必须以 / 开头和结尾
     */
    String BASE_PATH = "/" + ID + "/" + API_VERSION + "/";

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
