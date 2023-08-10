package com.levin.commons.dao.codegen.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@EqualsAndHashCode(of = "name")
@ToString()
@Accessors(chain = true)
public class FieldModel implements Cloneable {

    //识别中文
    static final Pattern zhCn = Pattern.compile("[\u4e00-\u9fa5]");

    public enum CRUD {
        CREATE,
        RETRIEVE,
        UPDATE,
        DELETE,
        DEFAULT
    }

    public CRUD crud = CRUD.DEFAULT;

    final Class entityType;

    Field field;

    public String name;

    public String title = "";
    private String desc = "";


    String prefix;

    //类的短名称
    public String typeName;

    //字段类型
    private Class type;

    //对于集合类型的字段，元素的类型
    private Class eleType;

    private Integer length = -1;


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

    private boolean autoGenValue = false; //是否自动生成值

    private boolean notUpdate = false;//是否不需要更新

    private boolean hasDefValue = false;//是否有默认值

    private boolean lazy = false;//是否lazy

    private boolean contains; //是否生成模糊查询

    private boolean hidden = false;//是否有默认值

    private String infoClassName;

    private String testValue;

    /**
     * 使用常量应用
     */
    private boolean isSchemaDescUseConstRef = true;

    public FieldModel(Class entityType) {
        Assert.notNull(entityType, "实体类型为空");
        this.entityType = entityType;
    }

    public String getModifiersPrefix() {
        return modifiers.stream().map(StringUtils::trimAllWhitespace).collect(Collectors.joining(" ")) + " ";
    }

    public static String anToStr(Annotation an) {
        Class<? extends Annotation> annotationType = an.annotationType();
        String prefix = "@" + annotationType.getPackage().getName();
        return "@" + an.toString().substring(prefix.length() + 1);
    }

    public void addAnnotation(Class<? extends Annotation> type, String... attrs) {

        imports.add(type.getName());

        String attr = String.join(",", attrs);
        if (StringUtils.hasText(attr)) {
            attr = "(" + attr + ")";
        }

        this.annotations.add("@" + type.getSimpleName() + attr);
    }

    public void addAnnotations(Predicate<Annotation> includePredicate, Annotation[] annotations) {

        Stream.of(annotations)
                .filter(Objects::nonNull)
                .filter(includePredicate)
                .forEach(this::addAnnotation);
    }

    public void addAnnotation(Annotation... annotations) {
        Stream.of(annotations)
                .filter(Objects::nonNull).forEach(an -> {
                    imports.add(an.getClass().getName());
                    this.annotations.add(anToStr(an));
                });
    }

    /**
     * 返回swagger 描述
     * <p>
     * 通过参数可以生成 引用或是字符串
     *
     * @return
     */
    public String getSchemaDesc() {
        return isSchemaDescUseConstRef ? "D_" + name : ("\"" + getDesc() + "\"");
    }

    public String getSchemaTitle() {
        return isSchemaDescUseConstRef ? "L_" + name : ("\"" + getTitle() + "\"");
    }

    /**
     * 是否是基本实体的字段
     *
     * @return
     */
    public boolean isBaseEntityField() {
        return isClassField("com.levin.commons.dao.domain.support.AbstractBaseEntityObject");
    }

    public boolean isClassField(String className) {
        return field.getDeclaringClass().getName().equals(className);
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
