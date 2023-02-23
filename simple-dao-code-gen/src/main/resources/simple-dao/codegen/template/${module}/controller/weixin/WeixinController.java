package ${modulePackageName}.aspect;

import static  ${modulePackageName}.ModuleOption.*;
import ${modulePackageName}.*;


import cn.hutool.json.JSONUtil;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.levin.commons.dao.SimpleDao;
import com.levin.commons.service.domain.ApiResp;
import com.levin.commons.utils.JwtUtils;
import com.levin.commons.utils.MapUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.bean.WxJsapiSignature;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import me.chanjar.weixin.common.bean.oauth2.WxOAuth2AccessToken;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.common.service.WxOAuth2Service;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.config.WxMpConfigStorage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Optional;

import static org.springframework.util.StringUtils.hasText;


@Controller(PLUGIN_PREFIX + "WeixinController")
@RequestMapping(PLUGIN_PREFIX + "/weixin")
@ConditionalOnProperty(value = PLUGIN_PREFIX + "WeixinController", havingValue = "false", matchIfMissing = true)
@Slf4j
@Tag(name = "微信相关", description = "微信相关")
public class WeixinController {

    @Autowired
    HttpServletRequest httpServletRequest;

    @Autowired
    HttpServletResponse httpServletResponse;

    @Autowired
    WxMpService wxMpService;

    @Autowired
    SimpleDao simpleDao;


    public static final String OPEN_ID = "openId";

    public static final String UNION_ID = "unionId";

    public static final String ACCESS_TOKEN = "accessToken";

    @SneakyThrows
    @RequestMapping(value = "authorize", method = {RequestMethod.POST, RequestMethod.GET})
    @Operation(tags = "微信相关", summary = "用户认证", description = "用户认证")
    public void authorize(String redirectUri, String code, String state) {

        log.info("微信认证请求地址：" + httpServletRequest.getRequestURL() + "?" + httpServletRequest.getQueryString());

        if (hasText(getValue(OPEN_ID))) {

            //直接重定向
            sendRedirect(redirectUri);

        } else if (hasText(state) && hasText(code)) {

            //如果是微信回调
            try {
                DecodedJWT decodedJWT = JwtUtils.decodeHMAC256(redirectUri, getJwtSecureKey());

                if (decodedJWT.getExpiresAt().getTime() < System.currentTimeMillis()) {
                    throw new IllegalArgumentException("数据已经过期");
                }

                redirectUri = decodedJWT.getClaim("redirectUri").asString();

                if (!state.trim().equals(getState(redirectUri))) {
                    throw new IllegalArgumentException("state 校验失败");
                }

            } catch (Exception e) {

                log.debug("Url param redirectUri jwt decode fail, " + e.getMessage(), e);

                httpServletResponse.getWriter().println("无效的请求");

                httpServletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);

                return;
            }

            WxOAuth2AccessToken accessToken = wxMpService.getOAuth2Service().getAccessToken(code);

            setValue(OPEN_ID, accessToken.getOpenId());
            setValue(UNION_ID, accessToken.getUnionId());
            setValue(ACCESS_TOKEN, accessToken.getAccessToken());

            //重定向
            sendRedirect(redirectUri);

        } else {

            //state 微信限制最多为128个字符
            state = getState(redirectUri);

            //url 加密
            redirectUri = JwtUtils.encodeHMAC256(getJwtSecureKey(), 60, null, MapUtils.put("redirectUri", redirectUri).build());

            //微信认证的 URL
            redirectUri = httpServletRequest.getRequestURL().toString() + "?redirectUri=" + redirectUri;

            String authorizeUrl = wxMpService.getOAuth2Service().buildAuthorizationUrl(redirectUri, WxConsts.OAuth2Scope.SNSAPI_USERINFO, state);

            log.info("原重定向地址：" + redirectUri);

            log.info("微信认证重定向地址：" + authorizeUrl);

            httpServletResponse.sendRedirect(authorizeUrl);
        }

    }


    @SneakyThrows
    private void sendRedirect(String redirectUri) {

        httpServletResponse.sendRedirect(redirectUri + (redirectUri.contains("?") ? "&" : "?")
                + OPEN_ID + "=" + getValue(OPEN_ID)
                + "&" + UNION_ID + "=" + getValue(UNION_ID)
                + "&" + ACCESS_TOKEN + "=" + getValue(ACCESS_TOKEN));

    }


    protected String getState(String redirectUri) {

        WxMpConfigStorage configStorage = wxMpService.getWxMpConfigStorage();

        return "" + Math.abs((configStorage.getAppId() + redirectUri).hashCode());
    }


    protected String getJwtSecureKey() {
        WxMpConfigStorage configStorage = wxMpService.getWxMpConfigStorage();
        return configStorage.getAppId() + configStorage.getSecret();
    }

    protected void setValue(String key, String value) {
        httpServletRequest.setAttribute(key, value);
        httpServletRequest.getSession(true).setAttribute(key, value);
        httpServletResponse.addCookie(new Cookie(key, value));
    }

    public String getValue(String name) {
        return getValue(httpServletRequest, name, true);
    }


    public static String getOpenId(HttpServletRequest httpServletRequest) {
        return getValue(httpServletRequest, OPEN_ID, true);
    }


    /**
     * 获取变量
     *
     * @param name
     * @return
     */
    public static String getValue(HttpServletRequest httpServletRequest, String name, boolean containsCookie) {

        String value = null;

        try {
            value = (String) httpServletRequest.getAttribute(name);
        } catch (Exception e) {
        }

        if (hasText(value)) {
            return value;
        }


        value = httpServletRequest.getParameter(name);

        if (hasText(value)) {
            return value;
        }


        value = (String) httpServletRequest.getSession(true).getAttribute(name);

        if (hasText(value)) {
            return value;
        }

        value = httpServletRequest.getHeader(name);

        if (hasText(value)) {
            return value;
        }

        if (containsCookie) {
            value = Arrays.stream(Optional.ofNullable(httpServletRequest.getCookies())
                    .orElse(new Cookie[0]))
                    .filter(cookie -> name.equalsIgnoreCase(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }

        return value;

    }

    @SneakyThrows
    @RequestMapping(value = "getUserInfo", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    @Operation(tags = "微信相关", summary = "获取用户信息", description = "获取用户信息- code or refreshToken 参数二选一")
    public ApiResp<WxOAuth2UserInfo> getUserInfo(String code, String refreshToken) {

        WxOAuth2Service oAuth2Service = wxMpService.getOAuth2Service();

        WxOAuth2AccessToken accessToken = null;

        if (!hasText(refreshToken)) {
            refreshToken = getValue("refreshToken");
        }

        if (!hasText(code)) {
            code = getValue("code");
        }

        if (hasText(code)) {
            accessToken = oAuth2Service.getAccessToken(code);
        } else if (hasText(refreshToken)) {
            accessToken = oAuth2Service.refreshAccessToken(refreshToken);
        } else {
            throw new IllegalArgumentException("code or refreshToken is required");
        }

        return ApiResp.ok(oAuth2Service.getUserInfo(accessToken, "zh_CN"));

    }


    /**
     * 生成的签名对象
     *
     * @param
     * @return
     * @throws WxErrorException
     */
    @ResponseBody
    @RequestMapping(value = {"signature", "jsapiSignature"}, method = {RequestMethod.POST, RequestMethod.GET})
    @Operation(tags = "微信相关", summary = "生成的签名对象", description = "生成的签名对象")
    public ApiResp<WxJsapiSignature> signature(@RequestParam(name = "url") String url) throws WxErrorException {

        WxJsapiSignature jsapiSignature = wxMpService.createJsapiSignature(url);

        log.info("生成微信签名：" + url + " --> " + JSONUtil.toJsonStr(jsapiSignature));

        return ApiResp.ok(jsapiSignature);
    }


    /**
     * checkSignature
     *
     * @param signature
     * @param timestamp
     * @param nonce
     * @param echostr
     * @return
     */
    @ResponseBody
    @RequestMapping(value = {"checkSignature"}, method = {RequestMethod.POST, RequestMethod.GET})
    @Operation(tags = "微信相关", summary = "检查签名", description = "检查签名")
    public String checkSignature(@RequestParam(name = "signature", required = false) String signature,
                                 @RequestParam(name = "timestamp", required = false) String timestamp,
                                 @RequestParam(name = "nonce", required = false) String nonce,
                                 @RequestParam(name = "echostr", required = false) String echostr) {

        log.info("\n接收到来自微信服务器的认证消息：[{}, {}, {}, {}]", signature, timestamp, nonce, echostr);

        if (StringUtils.isAnyBlank(signature, timestamp, nonce)) {
            throw new IllegalArgumentException("请求参数非法，请核实!");
        }

        if (wxMpService.checkSignature(timestamp, nonce, signature)) {
            return echostr;
        }

        return "认证失败";
    }
}
