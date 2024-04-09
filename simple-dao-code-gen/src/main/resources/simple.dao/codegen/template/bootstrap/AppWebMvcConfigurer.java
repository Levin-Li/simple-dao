package ${modulePackageName};


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.SecureUtil;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

import com.levin.commons.dao.Paging;
import com.levin.commons.dao.support.SimplePaging;
import com.levin.commons.format.DefaultDateFormat;
import com.levin.commons.service.domain.ApiResp;
import com.levin.commons.service.domain.BaseResp;
import com.levin.commons.service.domain.PageableData;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;


import static ${modulePackageName}.ModuleOption.*;

/**
 * 应用MVC配置
 *
 * @author Auto gen by simple-dao-codegen, @time: ${.now}, 代码生成哈希校验码：[]，请不要修改和删除此行内容。
 * 
 */
@Slf4j
//默认不启用，需要时在配置文件中开启
//@Configuration(PLUGIN_PREFIX + "AppWebMvcConfigurer")
@ConditionalOnProperty(prefix = PLUGIN_PREFIX, name = "AppWebMvcConfigurer", havingValue = "true", matchIfMissing = true)
public class AppWebMvcConfigurer implements WebMvcConfigurer {

    @Autowired
    WebProperties webProperties;

    @PostConstruct
    void init() {
        log.info("init...");
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {

        //移除默认的转换器
        registry.removeConvertible(String.class, Date.class);
        registry.removeConvertible(Date.class, String.class);

        registry.addFormatter(new DefaultDateFormat());

    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        //{ "classpath:/META-INF/resources/", "classpath:/resources/", "classpath:/static/", "classpath:/public/" };

        //注意每个资源路径后面的路径加 / !!! 重要的事情说三遍
        //注意每个资源路径后面的路径加 / !!! 重要的事情说三遍
        //注意每个资源路径后面的路径加 / !!! 重要的事情说三遍

//        registry.addResourceHandler(ADMIN_UI_PATH + "**")
//                .addResourceLocations("classpath:public" + ADMIN_UI_PATH);
//
//        registry.addResourceHandler(H5_UI_PATH + "**")
//                .addResourceLocations("classpath:public" + H5_UI_PATH);
//
//        registry.addResourceHandler(H5_UI_PATH + "**")
//                .addResourceLocations("classpath:public" + H5_UI_PATH);

        //映射资源目录
//        registry.addResourceHandler(("/api-docs/**"))
//                .addResourceLocations(webProperties.getResources().getStaticLocations());

    }

    /**
     * 添加参数预处理
     * @param resolvers
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {

        //分页处理
        resolvers.add(0, new HandlerMethodArgumentResolver() {

            //private CopyOptions copyOptions = CopyOptions.create().ignoreNullValue();

            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return Paging.class.isAssignableFrom(parameter.getParameterType());
            }

            @Override
            public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {

                String page = webRequest.getParameter("page");
                String limit = webRequest.getParameter("limit");
                String requireTotals = webRequest.getParameter("count");
                String requireResultList = webRequest.getParameter("list");

                Map<String, Object> pData = new HashMap<>();

                if (StringUtils.hasText(page)) {
                    pData.put(SimplePaging.Fields.pageIndex, Integer.parseInt(page.trim()));
                }

                if (StringUtils.hasText(limit)) {
                    pData.put(SimplePaging.Fields.pageSize, Integer.parseInt(limit.trim()));
                }

                if (StringUtils.hasText(requireTotals)) {
                    pData.put(SimplePaging.Fields.requireTotals, "true".equalsIgnoreCase(requireTotals.trim()));
                }

                if (StringUtils.hasText(requireResultList)) {
                    pData.put(SimplePaging.Fields.requireResultList, "true".equalsIgnoreCase(requireResultList.trim()));
                }

                return BeanUtil.copyProperties(pData, parameter.getParameterType());

            }
        });
    }



//     const aesEncrypt = path => {
//
//       const now = new Date()
//
//       const n = now.getDay() + now.getDate() + now.getMonth() + 1
//
//        // 11 位随机前缀
//        let prefix = Math.round(Math.random() * 100000 + 100000).toString() + Math.round(Math.random() * 100000 + 100000).toString()
//
//        prefix = prefix.substring(0, 11)
//        // 24位密码
//        const pwd = prefix + '%Z%6&5*7!sX' + ((n < 10 ? '0' : '') + n + '')
//
//        // 加密前处理
//        path = CryptoJS.enc.Utf8.parse(path)
//
//        // CryptoJS.enc.Hex.parse(word);
//
//        path = CryptoJS.enc.Hex.stringify(CryptoJS.AES.encrypt(path, CryptoJS.enc.Utf8.parse(pwd), { mode: CryptoJS.mode.ECB, padding: CryptoJS.pad.Pkcs7 }).ciphertext)
//
//        return prefix + path.substring(path.length - 6) + path
//    }
//
//const aesDecrypt = resp => {
//
//        if (!resp.sign) {
//            return resp.data
//        }
//
//            // 32 位 uuid
//      const dataTxt = resp.data
//      const signStr = resp.sign
//
//      const now = new Date()
//
//      const n = now.getDay() + now.getDate() + now.getMonth() + 1
//
//      const pwd = dataTxt.substring(4, 12) + 'W0o%@)' + ((n < 10 ? '0' : '') + n + '') + signStr.substring(8, 16)
//
//            // base64密文， AES解密算法, 必须为base64格式才能解密，如果为16进制，需要先转为base64
//      const ciphertext = dataTxt.substring(32)
//
//            // 关键步骤，转换Key
//      const key = CryptoJS.enc.Utf8.parse(pwd)
//
//      const originalText = CryptoJS.enc.Utf8.stringify(CryptoJS.AES.decrypt(ciphertext, key, { mode: CryptoJS.mode.ECB, padding: CryptoJS.pad.Pkcs7 }))
//
//        if (signStr !== CryptoJS.SHA1(originalText).toString()) {
//            return Promise.reject('数据校验异常')
//        }
//
//        resp.data = JSON.parse(originalText)
//
//        return resp.data
//    }
//
//    window.__ad = aesDecrypt

    /**
     * 数据加密
     * @param json
     * @param sign
     * @return
     */
    public static String aesEncrypt(String json, String sign) {

        //故意加入的混淆的前缀
        final String prefix = RandomUtil.randomString(32).toUpperCase();

        Calendar calendar = Calendar.getInstance();

        //n 必须小于 100, 不能超过 99
        String n = "" + (calendar.get(Calendar.MONTH) + calendar.get(Calendar.DAY_OF_MONTH) + calendar.get(Calendar.DAY_OF_WEEK));

        //24位长度，192位 D2CEF6ECW0o%@)3680c8328b

        //密码长度24个字符，192 Bit, 前缀 8个，中间固定 8个，后面取签名参数的 8个
        final String pwd = prefix.substring(4, 12) + ("4vX8$o" + n) + sign.substring(8 + (n.length() - 2), 16);

        //加密后的密文
        return prefix + SecureUtil.aes(pwd.getBytes(StandardCharsets.UTF_8)).encryptBase64(json, StandardCharsets.UTF_8);

    }

    /**
     * 数据转换和加密
     * @param converters
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {

        for (HttpMessageConverter<?> converter : converters) {
            if (converter instanceof MappingJackson2HttpMessageConverter) {

                final ObjectMapper objectMapper = ((MappingJackson2HttpMessageConverter) converter).getObjectMapper();

                SimpleModule module = new SimpleModule().addSerializer(ApiResp.class, new JsonSerializer<ApiResp>() {
                    @Override
                    public void serialize(ApiResp apiResp, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

                        jsonGenerator.writeStartObject();

                        jsonGenerator.writeNumberField(BaseResp.Fields.code, (apiResp.getCode() > 100) ? (apiResp.getCode() / 100) : apiResp.getCode());

                        jsonGenerator.writeBooleanField("successful", apiResp.isSuccessful());
                        jsonGenerator.writeBooleanField("bizError", apiResp.isBizError());

                        //jsonGenerator.writeBooleanField("errType", apiResp.getErrorType().name());

                        if (StringUtils.hasText(apiResp.getMsg())) {
                            jsonGenerator.writeStringField(BaseResp.Fields.msg, apiResp.getMsg());
                        }

                        Object data = apiResp.getData();

                        //分页对象单独处理
                        if (data instanceof PageableData) {

                            PageableData pd = (PageableData) data;

                            HashMap<Object, Object> pdMap = new HashMap<>();

                            pdMap.put("total", pd.getTotals());
                            pdMap.put("hasNext", pd.hasMore());

                            if (pd.getItems() != null) {
                                pdMap.put("items", pd.getItems());
                            }

                            data = pdMap;
                        }

                        if (data != null) {

                            //如果是对象类型
                            if (!BeanUtils.isSimpleValueType(data.getClass())) {

                                final String json = objectMapper.writeValueAsString(data);

                                final String sign = SecureUtil.sha1(json);
                                //加密

                                data = aesEncrypt(json, sign);

                                jsonGenerator.writeStringField(ApiResp.Fields.sign, sign);
                            }

                            jsonGenerator.writeObjectField(BaseResp.Fields.data, data);

                        }

                        jsonGenerator.writeEndObject();

                    }
                });

                objectMapper.registerModule(module);

//
//                SimpleModule pdModule = new SimpleModule().addSerializer(PageableData.class, new JsonSerializer<PageableData>() {
//                    @Override
//                    public void serialize(PageableData pd, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
//
//                        jsonGenerator.writeStartObject();
//
//                        jsonGenerator.writeNumberField("total", pd.getTotals());
//
//                        if (pd.getItems() != null) {
//                            jsonGenerator.writeObjectField(PagingData.Fields.items, pd.getItems());
//                        }
//
//                        jsonGenerator.writeBooleanField("hasNext", pd.hasMore());
//
//                        jsonGenerator.writeEndObject();
//
//                    }
//                });
//
//                objectMapper.registerModule(pdModule);

            }
        }

    }
}
