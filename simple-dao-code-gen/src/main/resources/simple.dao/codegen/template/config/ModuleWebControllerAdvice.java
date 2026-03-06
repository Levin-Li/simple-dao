package ${modulePackageName}.config;

import static ${modulePackageName}.ModuleOption.*;

import ${modulePackageName}.*;

import static com.levin.commons.service.domain.ServiceResp.ErrorType.*;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.ClassUtil;
import com.levin.commons.service.domain.ApiResp;
import com.levin.commons.service.domain.ServiceResp;
import com.levin.commons.service.exception.*;
import com.levin.commons.utils.ExceptionUtils;

import com.levin.commons.dao.exception.*;


import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.beans.factory.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.*;
import org.springframework.web.multipart.*;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.http.converter.HttpMessageConversionException;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.persistence.NonUniqueResultException;
import jakarta.persistence.PersistenceException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.lang.reflect.Field;
import java.net.ConnectException;
import java.net.SocketException;
import java.sql.SQLException;
import jakarta.validation.ValidationException;
import java.sql.SQLIntegrityConstraintViolationException;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.spi.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.levin.commons.service.domain.ServiceResp.ErrorType.*;

/**
 *
 * 在Spring 3.2中
 * 新增了@ControllerAdvice、@RestControllerAdvice 注解，
 * 可以用于定义@ExceptionHandler、@InitBinder、@ModelAttribute，并应用到所有@RequestMapping、@PostMapping， @GetMapping注解中。
 *
 * 注意：默认不启用，启用请取消注释
 *
 * @author Auto gen by simple-dao-codegen, @time: ${.now}, 代码生成哈希校验码：[]，请不要修改和删除此行内容。
 *
 */
@Slf4j
@Component(PLUGIN_PREFIX + "${className}")
@ConditionalOnProperty(prefix = PLUGIN_PREFIX, name = "${className}", havingValue = "true", matchIfMissing = true)
@RestControllerAdvice(PACKAGE_NAME)
public class ModuleWebControllerAdvice {


    @Autowired
    HttpServletRequest request;

    @Autowired
    HttpServletResponse response;

    @Autowired
    MultipartProperties multipartProperties;

    @Autowired
    Environment env;

    boolean isDev = false;

    /**
     * 项目启动时，初始化日志
     */
    @PostConstruct
    void init() {
        isDev = Arrays.stream(env.getActiveProfiles()).anyMatch(profile -> profile.equals("dev") || profile.equals("test") || profile.equals("local"));
        log.info("init...");
    }

    /**
     * // @InitBinder标注的initBinder()方法表示注册一个Date类型的类型转换器，用于将类似这样的2019-06-10
     * // 日期格式的字符串转换成Date对象
     *
     * @param binder
     */
    @InitBinder
    protected void initBinder(WebDataBinder binder) {
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//        dateFormat.setLenient(false);
//        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, false));
//        binder.registerCustomEditor(Date.class,new CustomDateEditor(new SimpleDateFormat("MM-dd-yyyy"),false));
    }


    /**
     * 获取异常的message
     *
     * @param exception
     * @return
     */
    String getExMsg(Throwable exception) {

        if (exception == null) {
            return null;
        }

        String failover = exception.getClass().getSimpleName();

        //循环获取异常的message,返回第一个有message的异常
        while (exception != null) {
            if (exception.getMessage() != null || exception instanceof BindException) {

                if (exception instanceof BindException) {

                    BindException ex = (BindException) exception;

                    BindingResult br = ex.getBindingResult();

                    Object target = br.getTarget();

                    FieldError fieldError = br.getFieldError();

                    if (fieldError != null && target != null) {

                        String errorMessage = fieldError.getField();

                        Field field = ReflectionUtils.findField(target.getClass(), fieldError.getField());

                        if (field != null) {
                            Schema schema = field.getAnnotation(Schema.class);
                            if (schema != null) {
                                errorMessage = Stream.of(schema.title(), schema.description())
                                        .filter(StringUtils::hasText).findFirst().orElse(fieldError.getField());
                            }
                        }

                        return errorMessage + "-" + fieldError.getDefaultMessage();

                    }


                } else if (exception instanceof HttpMessageConversionException) {
                    return "数据转换异常";
                }

                return exception.getMessage();
            }
            //防止循环引用
            if (exception.getCause() == exception) {
                break;
            }
            exception = exception.getCause();
        }

        return failover;
    }

    /**
     * 获取异常的详细信息
     *
     * @param exception
     * @return
     */
    String getExDetailMsg(Throwable exception) {

        if (exception == null) {
            return null;
        }

        //开发模式下，返回异常的堆栈信息
        if (isDev) {
            return ExceptionUtils.getPrintInfo(exception);
        }

        //循环获取cause,保存到列表
        List<Throwable> causeList = new ArrayList<>();

        while (exception != null) {
            causeList.add(exception);
            //防止循环引用
            if (exception.getCause() == exception) {
                break;
            }
            exception = exception.getCause();
        }

        return causeList.stream().map(ex -> ex.getClass().getSimpleName() + (StringUtils.hasText(ex.getMessage()) ? ":" + ex.getMessage() : "")).collect(Collectors.joining(" -> "));
    }

    //自定义错误处理
    //@ExceptionHandler({XXException.class,})
    public ApiResp onXXException(Exception e) {

        response.setStatus(HttpStatus.UNAUTHORIZED.value());

        return ApiResp.error(AuthenticationError.getBaseErrorCode(), "未登录：" + getExMsg(e));
    }



}
