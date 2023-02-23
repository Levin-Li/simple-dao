package ${modulePackageName}.config;

import static ${modulePackageName}.ModuleOption.*;
import ${modulePackageName}.*;

import com.levin.commons.service.domain.ApiResp;
import com.levin.commons.service.domain.ServiceResp;
import com.levin.commons.service.exception.AccessDeniedException;
import com.levin.commons.service.exception.ServiceException;
import com.levin.commons.utils.ExceptionUtils;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.beans.factory.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.*;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.persistence.PersistenceException;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.net.ConnectException;
import java.net.SocketException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

import org.springframework.web.servlet.config.annotation.*;
import org.springframework.boot.autoconfigure.condition.*;
import lombok.extern.slf4j.Slf4j;
import java.text.SimpleDateFormat;

/**
 *
 * 在Spring 3.2中
 * 新增了@ControllerAdvice、@RestControllerAdvice 注解，
 * 可以用于定义@ExceptionHandler、@InitBinder、@ModelAttribute，并应用到所有@RequestMapping、@PostMapping， @GetMapping注解中。
 *
 * 注意：默认不启用，启用请取消注释
 *
 */
@Slf4j
//@Component(PLUGIN_PREFIX + "${className}")
@ConditionalOnProperty(prefix = PLUGIN_PREFIX, name = "${className}", matchIfMissing = true)
//@RestControllerAdvice(PACKAGE_NAME)
public class ModuleWebControllerAdvice {

    @Autowired
    HttpServletRequest request;

    @Autowired
    HttpServletResponse response;

    @PostConstruct
    void init() {
        log.info("init...");
    }

    /**
     * // @InitBinder标注的initBinder()方法表示注册一个Date类型的类型转换器，用于将类似这样的2019-06-10
     * // 日期格式的字符串转换成Date对象
     * @param binder
     */
    @InitBinder
    protected void initBinder(WebDataBinder binder) {
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//        dateFormat.setLenient(false);
//        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, false));
//        binder.registerCustomEditor(Date.class,new CustomDateEditor(new SimpleDateFormat("MM-dd-yyyy"),false));
    }


//    // 这里@ModelAttribute("loginUserInfo")标注的modelAttribute()方法表示会在Controller方法之前
//    // 执行，返回当前登录用户的UserDetails对象
//    @ModelAttribute("loginUserInfo")
//    public UserDetails modelAttribute() {
//        return (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//    }


//    // 这里表示Controller抛出的MethodArgumentNotValidException异常由这个方法处理
//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public Result exceptionHandler(MethodArgumentNotValidException e) {
//        Result result = new Result(BizExceptionEnum.INVALID_REQ_PARAM.getErrorCode(),
//                BizExceptionEnum.INVALID_REQ_PARAM.getErrorMsg());
//        logger.error("req params error", e);
//        return result;
//    }
//    // 这里表示Controller抛出的BizException异常由这个方法处理
//    @ExceptionHandler(BizException.class)
//    public Result exceptionHandler(BizException e) {
//        BizExceptionEnum exceptionEnum = e.getBizExceptionEnum();
//        Result result = new Result(exceptionEnum.getErrorCode(), exceptionEnum.getErrorMsg());
//        logger.error("business error", e);
//        return result;
//    }
//    // 这里就是通用的异常处理器了,所有预料之外的Exception异常都由这里处理
//    @ExceptionHandler(Exception.class)
//    public Result exceptionHandler(Exception e) {
//        Result result = new Result(1000, "网络繁忙,请稍后再试");
//        logger.error("application error", e);
//        return result;
//    }


//    @ExceptionHandler({NotLoginException.class,})
//    public ApiResp onNotLoginException(Exception e) {
//
//        response.setStatus(HttpStatus.UNAUTHORIZED.value());
//
//        return ApiResp.error(ServiceResp.ErrorType.AuthenticationError.getBaseErrorCode()
//                , "未登录：" + e.getMessage());
//    }
//
//    @ExceptionHandler({SaTokenException.class,})
//    public ApiResp onSaTokenException(Exception e) {
//
//        response.setStatus(HttpStatus.UNAUTHORIZED.value());
//
//        return ApiResp.error(ServiceResp.ErrorType.AuthenticationError.getBaseErrorCode()
//                , "认证异常：" + e.getMessage());
//    }

    @ExceptionHandler({AccessDeniedException.class,})
    public ApiResp onAccessDeniedException(Exception e) {

        response.setStatus(HttpStatus.FORBIDDEN.value());

        return ApiResp.error(ServiceResp.ErrorType.AuthenticationError.getBaseErrorCode()
                , e.getMessage());
    }

    @ExceptionHandler({IllegalArgumentException.class,
            IllegalStateException.class,
            MethodArgumentNotValidException.class,
            MissingServletRequestParameterException.class})
    public ApiResp onParameterException(Exception e) {

        log.error("请求参数异常," + request.getRequestURL(), e);

        return (ApiResp) ApiResp.error(ServiceResp.ErrorType.BizError.getBaseErrorCode()
                , e.getMessage())
                .setDetailMsg(ExceptionUtils.getAllCauseInfo(e, " -> "));
    }

    @ExceptionHandler({ConstraintViolationException.class, SQLIntegrityConstraintViolationException.class})
    public ApiResp onConstraintViolationException(Exception e) {

        log.error("发生数据约束异常," + request.getRequestURL(), e);

        boolean used = e.getMessage().contains(" delete ")
                || e.getMessage().contains(" update ");

        return (ApiResp) ApiResp.error(ServiceResp.ErrorType.BizError.getBaseErrorCode()
                , used ? "操作失败，数据已经被使用" : "名称、编码或其它唯一值已经存在")
                .setDetailMsg(ExceptionUtils.getRootCauseInfo(e));
    }

    @ExceptionHandler(ServiceException.class)
    public ApiResp onServiceException(Exception e) {

        response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());

        return (ApiResp) ApiResp.error(ServiceResp.ErrorType.SystemInnerError.getBaseErrorCode()
                , e.getMessage())
                .setDetailMsg(ExceptionUtils.getAllCauseInfo(e, " -> "));
    }

    @ExceptionHandler({PersistenceException.class, SQLException.class})
    public ApiResp onPersistenceException(Exception e) {

        Throwable rootCause = ExceptionUtils.getRootCause(e);
        if (rootCause instanceof ConstraintViolationException
                || rootCause instanceof SQLIntegrityConstraintViolationException) {
            return onConstraintViolationException((Exception) rootCause);
        }

        log.error("发生数据库操作异常," + request.getRequestURL(), e);

        return (ApiResp) ApiResp.error(ServiceResp.ErrorType.SystemInnerError.getBaseErrorCode(),
                "数据异常，请稍后重试")
                .setDetailMsg(ExceptionUtils.getRootCauseInfo(e));
    }

    //    // 这里就是通用的异常处理器了,所有预料之外的Exception异常都由这里处理
    @ExceptionHandler(ServletException.class)
    public ApiResp onServletException(ServletException e) {

        log.error("发生 Web异常:" + request.getRequestURL(), e);

        response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());

        return (ApiResp) ApiResp.error(ServiceResp.ErrorType.SystemInnerError.getBaseErrorCode()
                , e.getMessage())
                .setDetailMsg(ExceptionUtils.getPrintInfo(e));
    }

    //    // 这里就是通用的异常处理器了,所有预料之外的Exception异常都由这里处理
    @ExceptionHandler(Exception.class)
    public ApiResp exceptionHandler(Exception e) {

        log.error("发生异常:" + request.getRequestURL(), e);

        //网络异常
        if (ExceptionUtils.getCauseByTypes(e, SocketException.class) != null) {

            response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());

            return (ApiResp) ApiResp.error(ServiceResp.ErrorType.ResourceError.getBaseErrorCode()
                    , e.getMessage())
                    .setDetailMsg(ExceptionUtils.getPrintInfo(e));
        }

        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());

        return (ApiResp) ApiResp.error(ServiceResp.ErrorType.UnknownError.getBaseErrorCode()
                , e.getMessage())
                .setDetailMsg(ExceptionUtils.getPrintInfo(e));
    }
}
