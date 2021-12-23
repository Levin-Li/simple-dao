package ${modulePackageName}.config;

import static ${modulePackageName}.ModuleOption.*;
import ${modulePackageName}.*;

import com.levin.commons.service.domain.ApiResp;
import com.levin.commons.service.exception.AccessDeniedException;
import com.levin.commons.service.exception.ServiceException;
import com.levin.commons.utils.ExceptionUtils;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.persistence.PersistenceException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

import org.springframework.web.servlet.config.annotation.*;
import org.springframework.boot.autoconfigure.condition.*;
import lombok.extern.slf4j.Slf4j;
import java.text.SimpleDateFormat;

/**
 * 在Spring 3.2中
 * 新增了@ControllerAdvice、@RestControllerAdvice 注解，
 * 可以用于定义@ExceptionHandler、@InitBinder、@ModelAttribute，并应用到所有@RequestMapping、@PostMapping， @GetMapping注解中。
 */
@Slf4j
@Component(PLUGIN_PREFIX + "ModuleWebControllerAdvice")
@ConditionalOnMissingBean(name = {PLUGIN_PREFIX + "ModuleWebControllerAdvice"})
@RestControllerAdvice(PACKAGE_NAME)
@ConditionalOnProperty(value = PLUGIN_PREFIX + "ModuleWebControllerAdvice", havingValue = "false", matchIfMissing = true)
public class ModuleWebControllerAdvice {

    @Resource
    HttpServletRequest request;

    @PostConstruct
    void init() {
        log.info("init...");
    }

//    // 这里@ModelAttribute("loginUserInfo")标注的modelAttribute()方法表示会在Controller方法之前
//    // 执行，返回当前登录用户的UserDetails对象
//    @ModelAttribute("loginUserInfo")
//    public UserDetails modelAttribute() {
//        return (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//    }
//    // @InitBinder标注的initBinder()方法表示注册一个Date类型的类型转换器，用于将类似这样的2019-06-10
//    // 日期格式的字符串转换成Date对象
//    @InitBinder
//    protected void initBinder(WebDataBinder binder) {
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//        dateFormat.setLenient(false);
//        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, false));
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


    @ExceptionHandler({AccessDeniedException.class,})
    public ApiResp onAccessDeniedException(Exception e) {
        return ApiResp.error(2, "访问异常：" + e.getMessage())
                .setHttpStatusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class,
            MissingServletRequestParameterException.class})
    public ApiResp onParameterException(Exception e) {

        log.error("发生数据约束异常," + request.getRequestURL(), e);

        return (ApiResp) ApiResp.error(9, "请求参数异常：" + e.getMessage())
                .setHttpStatusCode(HttpStatus.BAD_REQUEST.value())
                .setDetailMsg(ExceptionUtils.getAllCauseInfo(e, " -> "));
    }

    @ExceptionHandler(ServiceException.class)
    public ApiResp onServiceException(Exception e) {
        return (ApiResp) ApiResp.error(10, e.getMessage())
                .setHttpStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .setDetailMsg(ExceptionUtils.getAllCauseInfo(e, " -> "));
    }

    @ExceptionHandler({ConstraintViolationException.class, SQLIntegrityConstraintViolationException.class})
    public ApiResp onConstraintViolationException(Exception e) {

        log.error("发生数据约束异常," + request.getRequestURL(), e);

        boolean used = e.getMessage().contains(" delete ")
                || e.getMessage().contains(" update ");

        return (ApiResp) ApiResp.error(20, used ? "操作失败，数据已经被使用" : "名称、编码或其它唯一值已经存在")
                .setHttpStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .setDetailMsg(ExceptionUtils.getRootCauseInfo(e));
    }


    @ExceptionHandler({PersistenceException.class, SQLException.class})
    public ApiResp onPersistenceException(Exception e) {

        Throwable rootCause = ExceptionUtils.getRootCause(e);
        if (rootCause instanceof ConstraintViolationException || rootCause instanceof SQLIntegrityConstraintViolationException) {
            return onConstraintViolationException((Exception) rootCause);
        }

        log.error("发生数据库操作异常," + request.getRequestURL(), e);

        return (ApiResp) ApiResp.error(20, "数据异常，请稍后重试")
                .setHttpStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .setDetailMsg(ExceptionUtils.getRootCauseInfo(e));
    }


    //    // 这里就是通用的异常处理器了,所有预料之外的Exception异常都由这里处理
    @ExceptionHandler(ServletException.class)
    public ApiResp onServletException(ServletException e) {

        log.error("发生 Web异常:" + request.getRequestURL(), e);

        return (ApiResp) ApiResp.error(1, e.getMessage())
                .setHttpStatusCode(500)
                .setDetailMsg(ExceptionUtils.getPrintInfo(e));
    }

    //    // 这里就是通用的异常处理器了,所有预料之外的Exception异常都由这里处理
    @ExceptionHandler(Exception.class)
    public ApiResp exceptionHandler(Exception e) {
        log.error("发生异常:" + request.getRequestURL(), e);
        return (ApiResp) ApiResp.error(1, e.getMessage())
                .setHttpStatusCode(500)
                .setDetailMsg(ExceptionUtils.getPrintInfo(e));
    }
}