package com.levin.commons.dao.util;

import com.levin.commons.dao.MiniDao;
import com.levin.commons.dao.StatementBuildException;
import com.levin.commons.dao.annotation.C;
import com.levin.commons.dao.annotation.Func;
import com.levin.commons.dao.annotation.Op;
import com.levin.commons.dao.support.SelectDaoImpl;
import com.levin.commons.dao.support.ValueHolder;
import com.levin.commons.utils.MapUtils;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.StringUtils;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.util.StringUtils.hasText;

public abstract class ExprUtils {

    //匹配样式：${:paramName}
    public static final Pattern jpaNamedParamStylePattern = Pattern.compile("(\\$\\{\\s*:\\s*([\\w._]+)\\s*\\})");

    //匹配样式：${paramName}
    public static final Pattern groovyVarStylePattern = Pattern.compile("(\\$\\{\\s*\\s*([\\w._]+)\\s*\\})");

    public static String genExpr(C c, String fieldExpr, boolean complexType, Class<?> expectType
            , ValueHolder holder, String paramPlaceholder, Function<ValueHolder, String> subQueryBuilder,
                                 List<Map<String, ? extends Object>> contexts) {

        Op op = c.op();

        if (op == null) {
            op = Op.Eq;
        }

        boolean isNotOp = Op.Not.name().equals(op.name());

        if (op.isAllowFieldFunc()) {
            fieldExpr = funcExpr(fieldExpr, c.fieldFuncs());
        }

        String paramExpr = "";

        boolean hasDynamicExpr = true;

        //优先使用子查询
        if (hasText(c.subQuery())) {

            paramExpr = c.subQuery();

            hasDynamicExpr = false;

        } else if (complexType) {

            paramExpr = subQueryBuilder.apply(holder);

        } else if (!isNotOp) {

            int eleCount = 1;

            //是否可以展开参数
            if (op.isExpandParamValue()) {

                //如果数据库的目标字段类型检测到，并且不是字符串类型，并且参数值是字符串
                //尝试自动解析成数组
                if (expectType != null
                        && !String.class.equals(expectType)
                        && holder.value instanceof CharSequence) {
                    holder.value = ObjectUtil.convert(holder.value, Array.newInstance(expectType, 0).getClass());
                }

                if (c.filterNullValue()) {
                    holder.value = QueryAnnotationUtil.filterNullValue(holder.value, true);
                }

                eleCount = QueryAnnotationUtil.eleCount(holder.value);

                if (eleCount <= 0) {
                    return "";
                }

            } else {
                try {
                    holder.value = ObjectUtil.convert(holder.value, expectType);
                } catch (Exception e) {
                }
            }

            // 替换成 参数占位符
            // 只有这部分是

            paramExpr = genParamExpr(op.getParamDelimiter(), paramPlaceholder, eleCount);

            hasDynamicExpr = true;

        }

        if (isNotOp) {
            hasDynamicExpr = false;
            paramExpr = (String) holder.value;
        }

        if ((hasText(c.subQuery()) || complexType)
                && !op.name().toLowerCase().contains("exists")) {
            //尝试自动加挂号
            paramExpr = autoAroundParentheses("", paramExpr, "");
        }

        final String paramKey = "P_" + Math.abs(paramExpr.hashCode()) + "_" + System.currentTimeMillis();

        final String oldParamExpr = paramExpr;

        if (hasDynamicExpr) {
            //动态参数
            paramExpr = "${:" + paramKey + "}";
        }

        //
        paramExpr = funcExpr(paramExpr, c.paramFuncs());


        final List<Object> paramValues = new ArrayList(7);

        /// Function<String, String> genExpr = ql -> processParamPlaceholder(ql, paramPlaceholder, paramValues, contexts);

        //    String desc() default "语句表达式生成规则： surroundPrefix + op.gen( func(fieldExpr), func([subQuery or fieldValue])) +  surroundSuffix ";

        String ql = c.surroundPrefix() + " " + op.gen(fieldExpr, paramExpr) + " " + c.surroundSuffix();

        ql = processParamPlaceholder(ql, (key, ctxs) -> {

            if (key.equals(paramKey)) {
                paramValues.add(holder.value);
                return oldParamExpr;
            } else {
                paramValues.add(ObjectUtil.findValue(key, true, ctxs));
                return paramPlaceholder;
            }

        }, contexts).trim();


        if (paramValues.isEmpty()) {
            holder.value = Collections.EMPTY_LIST;
        } else if (paramValues.size() == 1) {
            holder.value = paramValues.get(0);
        } else {
            holder.value = paramValues;
        }

        return surroundNotExpr(c, replace(ql, contexts).trim());
    }


    public static String surroundNotExpr(C c, String expr) {

        return c.not() && hasText(expr) ? autoAroundParentheses(" NOT", expr, "") : expr;

    }


    /**
     * 构建子查询语句及参数
     *
     * @param holder
     * @return
     */
    public static String buildSubQuery(ValueHolder holder, MiniDao dao, boolean isNative, Map<String, Object> context) {

        SelectDaoImpl selectDao = new SelectDaoImpl(dao, isNative);

        if (context != null)
            selectDao.setContext(context);

        // selectDao.localParamPlaceholder = this.localParamPlaceholder;

        Object queryObj = holder.value;

        if (queryObj != null) {
            if (queryObj.getClass().isArray())
                selectDao.appendByQueryObj((Object[]) queryObj);
            else
                selectDao.appendByQueryObj(queryObj);
        }

        String subStatement = selectDao.genFinalStatement();

        if (hasText(subStatement)) {
            holder.value = selectDao.genFinalParamList();
        } else {
            holder.value = Collections.EMPTY_LIST;
        }

        return subStatement;
    }


    public static String funcExpr(String fieldName, Func... funcs) {
        return Arrays.stream(funcs)
                //  .filter(func -> StringUtils.hasText(func.value()))
                .reduce(fieldName, (expr, func) -> func.value() + func.prefix() + expr + func.suffix(), (r1, r2) -> r1 + r2);
    }

    /**
     * 尝试自动加上小括号，并自动处理重复小括号的问题
     *
     * @param expr
     * @return
     */
    public static String autoAroundParentheses(String prefix, String expr, String suffix) {

        expr = StringUtils.trimWhitespace(expr);

        if (expr.length() > 0) {

            prefix = StringUtils.trimAllWhitespace(prefix);
            suffix = StringUtils.trimAllWhitespace(suffix);

            //如果
            if (!prefix.endsWith("(")
                    && !suffix.startsWith(")")
                    && !expr.startsWith("(")
                    && !expr.endsWith(")")) {

                expr = "( " + expr + " )";

            }

            expr = prefix + " " + expr + " " + suffix;
        }

        return expr;
    }

    /**
     * 根据参数个数生成问号
     *
     * @param n
     * @return
     */
    static String genParamExpr(String delimiter, String txt, int n) {

        if (n < 1)
            return "";

        if (n == 1)
            return txt;

        StringBuilder buf = new StringBuilder();

        for (int i = 0; i < n; i++) {

            if (i > 0)
                buf.append(delimiter);

            buf.append(txt);
        }

        return buf.toString();
    }

    /**
     * 参数替换辅助类
     *
     * @param qlSection
     * @param contexts
     * @return
     */
    public static String processParamPlaceholder(String qlSection, String paramPlaceholder,
                                                 List<? extends Object> paramValues, List<Map<String, ? extends Object>> contexts) {

        return processParamPlaceholder(qlSection, (paramName, ctxs) -> {
            paramValues.add(ObjectUtil.findValue(paramName, true, ctxs));
            return paramPlaceholder;
        }, contexts);
    }

    /**
     * 参数替换辅助类
     *
     * @param qlSection
     * @param contexts
     * @return
     */
    public static String processParamPlaceholder(String qlSection, BiFunction<String, List<Map<String, ? extends Object>>, String> biFunction,
                                                 List<Map<String, ? extends Object>> contexts) {

        if (!hasText(qlSection)) {
            return qlSection;
        }

        qlSection = replace(qlSection, contexts);

        return replace(jpaNamedParamStylePattern, qlSection, key -> biFunction.apply(key, contexts));

    }

    public static String replace(String txt, List<Map<String, ? extends Object>> contexts) {
        return replace(txt, true, contexts);
    }


    public static String replace(String txt, boolean isThrowExWhenKeyNotFound, List<Map<String, ? extends Object>> contexts) {

        if (!hasText(txt)) {
            return txt;
        }

        return replace(groovyVarStylePattern, txt, key -> {

            Object v = ObjectUtil.findValue(key, isThrowExWhenKeyNotFound, contexts);

            if (v == null) {
                throw new StatementBuildException(String.format("[{%s}] var {%s} not found on context", txt, key));
            }

            return v.toString();
        });

    }

    /**
     * spring el 求值
     *
     * @param rootObject
     * @param expression
     * @param contexts
     * @param <T>
     * @return
     */
    public static <T> T evalSpEL(Object rootObject, String expression, List<Map<String, ? extends Object>> contexts) {

        final EvaluationContext ctx = new StandardEvaluationContext(rootObject);

        Optional.ofNullable(contexts).ifPresent(
                maps -> {
                    maps.stream().filter(Objects::nonNull)
                            .forEach(map -> ((StandardEvaluationContext) ctx).setVariables((Map<String, Object>) map));
                }
        );

        ExpressionParser parser = new SpelExpressionParser();

        return (T) parser.parseExpression(expression).getValue(ctx);

    }

    /**
     * @param pattern
     * @param txt
     * @param function
     * @return
     */
    private static String replace(Pattern pattern, String txt, Function<String, String> function) {

        if (txt == null || txt.length() == 0) {
            return txt;
        }

        Matcher matcher = pattern.matcher(txt);

        StringBuffer sb = new StringBuffer();

        boolean found = false;

        while (matcher.find()) {

            found = true;

            String key = matcher.group(2);

            matcher.appendReplacement(sb, function.apply(key));
        }

        if (found) {
            matcher.appendTail(sb);
        }


        return found ? sb.toString() : txt;

    }


    public static void main(String[] args) {


        String txt = "select * from ${table} t where t.name = :?  and t.age > :age  and t.sex = ${:sex} and t.desc like ${likeDesc}";


        txt = replace(txt, Arrays.asList(MapUtils.put("table", "person").put("likeDesc", "'%it'").build()));


    }


}
