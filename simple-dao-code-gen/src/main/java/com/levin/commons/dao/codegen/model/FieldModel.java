package com.levin.commons.dao.codegen.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(of = "name")
@ToString()
@Accessors(chain = true)
public class FieldModel implements Cloneable {

    //识别中文
    static final Pattern zhCn = Pattern.compile("[\u4e00-\u9fa5]");

    public enum CRUD {
        CREATE,
        RETRIVE,
        UPDATE,
        DELETE,
        DEFAULT
    }

    public CRUD crud = CRUD.DEFAULT;

    final Class entityType;

    Field field;

    public String name;

    public String title;

    String prefix;

    //类的短名称
    public String typeName;

    //字段类型
    private Class type;

    //对于集合类型的字段，元素的类型
    private Class eleType;

    private Integer length = -1;

    private String desc;
    private String finalDesc;

    private String descDetail;

    public final Set<String> imports = new LinkedHashSet<>();

    private final Set<String> annotations = new HashSet<>();

    //字段修饰前缀
    private final Set<String> modifiers = new HashSet<>();

    private final Map<String, Object> extras = new HashMap<>();

    private boolean pk = false;//是否主键字段

    private boolean uk = false;//是否唯一键

    private boolean baseType = true;//基础封装类型

    private boolean enumType = false;//是否enum

    private boolean jpaEntity = false;//是否 jpa 对象

    private boolean required = false;//是否必填

    private boolean autoIdentity; //是否自动增长主键

    private boolean notUpdate = false;//是否不需要更新

    private boolean hasDefValue = false;//是否有默认值

    private boolean lazy = false;//是否lazy

    private boolean contains; //是否生成模糊查询

    private boolean hidden = false;//是否有默认值

    private String infoClassName;

    private String testValue;

    public FieldModel(Class entityType) {
        Assert.notNull(entityType, "实体类型为空");
        this.entityType = entityType;
    }

    public String getModifiersPrefix() {
        return modifiers.stream().map(StringUtils::trimAllWhitespace).collect(Collectors.joining(" ")) + " ";
    }

    public String getRealDesc() {
        return desc;
    }

    /**
     * 替换换行符
     *
     * @return
     */
    public String getDesc() {

        if (StringUtils.hasText(finalDesc)) {
            return finalDesc;
        }

        if (!StringUtils.hasText(desc))
            return "";

        String tempDesc = desc.replace("\n", " ")
                .replace("\r", " ");

        //尝试识别中文注释
        int idx = tempDesc.indexOf(':');

        if (idx < 0) {
            idx = tempDesc.indexOf('：');
        }

        if (idx < 0) {
            return finalDesc = tempDesc;
        }

        //分割出冒号前的内容
        String result = tempDesc.substring(0, idx);

        //如果不包含中文
        if (!result.matches(zhCn.pattern())) {

            //分割出冒号后的内容
            tempDesc = tempDesc.substring(idx + 1);

            //如果包含中文
            if (tempDesc.matches(zhCn.pattern())) {
                result = tempDesc;
            }
        }

        return finalDesc = StringUtils.trimAllWhitespace(result);
    }

    /**
     * 是否是基本实体的字段
     *
     * @return
     */
    public boolean isBaseEntityField() {
        return field.getDeclaringClass().getName().equals("com.levin.commons.dao.domain.support.AbstractBaseEntityObject");
    }

    public FieldModel addImport(Class type) {

        if (type == null) {
            return this;
        }

        while (type.isArray()) {
            type = type.getComponentType();
        }

        if (!type.isPrimitive() && !type.getName().startsWith("java.lang.")) {
            //如果是类中类
            Class declaringClass = type.getDeclaringClass();
            if (declaringClass != null) {
                // ServiceModelCodeGenerator.logger.info("增加导入类： " + type + ",DeclaringClass :" + declaringClass);
                imports.add(declaringClass.getName() + ".*");
            } else {
                imports.add(type.getName());
            }

        }

        return this;
    }

    @SneakyThrows
    @Override
    protected Object clone() {
        return super.clone();
    }
}
