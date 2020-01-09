package com.levin.commons.dao.support;

import com.levin.commons.dao.util.AsmUtils;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Created by echo on 2017/4/28.
 */
public class MethodParameterNameDiscoverer
        extends DefaultParameterNameDiscoverer {

    private static final Map<Method, String[]> methodParamNameCache = new ConcurrentReferenceHashMap<>();

    @Override
    public String[] getParameterNames(Method method) {

        Class<?>[] parameterTypes = method.getParameterTypes();

        //如果没有参数
        if (parameterTypes == null || parameterTypes.length == 0)
            return new String[]{};

        //尝试从缓存获取
        String[] parameterNames = methodParamNameCache.get(method);

        if (parameterNames != null)
            return parameterNames;

        if (isValid(parameterNames = super.getParameterNames(method))) {
            methodParamNameCache.put(method, parameterNames);
        } else if (isValid(parameterNames = AsmUtils.getMethodParamNames(method))) {
            methodParamNameCache.put(method, parameterNames);
        } else {
            return null;
        }

        return parameterNames;
    }


    private static boolean isValid(String[] parameterNames) {

        if (parameterNames == null
                || parameterNames.length == 0)
            return false;

        for (String parameterName : parameterNames) {
            if (parameterName == null
                    || !StringUtils.hasText(parameterName))
                return false;
        }

        return true;
    }

}
