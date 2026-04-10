package com.levin.commons.dao.codegen.model;

import com.levin.commons.dao.annotation.*;
import com.levin.commons.service.domain.RefInject;
import com.levin.commons.service.support.InjectConst;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ResolvableType;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import jakarta.persistence.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.temporal.Temporal;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
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

    public Field field;

    public ResolvableType resolvableType;

    //时间字段类型
    private Class type;

    public String name;

    public String title = "";
    private String desc = "";


    String prefix;

    //类的短名称
    public String typeName;


    //对于集合类型的字段，元素的类型
    private Class eleType;

    private Class injectBaseType;

    //空表示，不是字符串
    private Integer textLength = null;


    public final Set<String> imports = new LinkedHashSet<>();

    private final Set<String> annotations = new LinkedHashSet<>();

    //字段修饰前缀
    private final Set<String> modifiers = new LinkedHashSet<>();

    private final Map<String, Object> extras = new LinkedHashMap<>();

    private boolean pk = false;//是否主键字段

    private boolean uk = false;//是否唯一键

    private boolean baseType = true;//基础封装类型


    private Annotation primitiveAttrAnnotation;


    private boolean enumerable = false;//是否enum

    private boolean jpaEntity = false;//是否 jpa 对象

    private boolean required = false;//是否必填

    private boolean autoGenValue = false; //是否自动生成值

    private boolean notCreate = false;//是否不需要更新

    private boolean notUpdate = false;//是否不需要更新

    private boolean hasDefaultValue = false;//是否有默认值

    private String defaultValue = "";

    private String exampleValue;

    private boolean lazy = false;//是否lazy

    private boolean contains; //是否生成模糊查询

    private boolean hidden = false;//是否有默认值

    private boolean readOnly = false;

    //是否是乐观锁字段
    private boolean optimisticLock;

    private String infoClassName;

    private String testValue;

    //可选项关联的目标类型,比如关联枚举类, 关联实体类
    private Class<?> optionsRefTargetType;


    public static String getSimpleGenericString(ResolvableType type, Function<ResolvableType, String> classConsumer) {

        if (type.getGenerics().length == 0) {
            return classConsumer.apply(type);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(classConsumer.apply(type));
        sb.append("<");

        ResolvableType[] generics = type.getGenerics();

        for (int i = 0; i < generics.length; i++) {
            if (i > 0)
                sb.append(",");
            sb.append(getSimpleGenericString(generics[i], classConsumer));
        }

        sb.append(">");

        return sb.toString();
    }

    /**
     * 使用常量应用
     */
    private boolean isSchemaDescUseConstRef = true;

    public FieldModel(Class entityType) {
        Assert.notNull(entityType, "实体类型为空");
        this.entityType = entityType;
    }


    public String getModifiersPrefix() {
        return modifiers.stream().map(StringUtils::trimWhitespace).collect(Collectors.joining(" ")) + " ";
    }

    public boolean isTransient() {
        return field.isAnnotationPresent(Transient.class) || Modifier.isTransient(field.getModifiers());
    }

    public static String anToStr(Annotation an) {
        Class<? extends Annotation> annotationType = an.annotationType();
        String prefix = "@" + annotationType.getPackage().getName();
        return "@" + an.toString().substring(prefix.length() + 1);
    }

    public boolean isIterable() {
        return field.getType().isArray() || Iterable.class.isAssignableFrom(field.getType());
    }

    public boolean hasIgnoreAnnotation() {
        return annotations.stream().anyMatch(an -> an.trim().startsWith("@" + Ignore.class.getName()) || an.trim().startsWith("@" + Ignore.class.getSimpleName()));
    }

    /**
     * 是否原则类型
     *
     * @return
     */
    public boolean isPrimitiveAttr() {
        return getPrimitiveAttrAnnotation() != null;
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
                    imports.add(an.annotationType().getName());
                    this.annotations.add(anToStr(an));
                });
    }

    public boolean hasJpaJoinColumn() {
        return field.isAnnotationPresent(JoinColumn.class)
                || field.isAnnotationPresent(ManyToMany.class)
                || field.isAnnotationPresent(ManyToOne.class)
                || field.isAnnotationPresent(OneToMany.class)
                || field.isAnnotationPresent(JoinColumns.class);
    }

    public boolean isDateTimeType() {
        return Date.class.isAssignableFrom(type) || Temporal.class.isAssignableFrom(type);
    }

    public boolean hasBetweenAnnotation() {
        return field.isAnnotationPresent(Between.class);
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

    @SneakyThrows
    public boolean isType(String className) {
        return Class.forName(className).isAssignableFrom(entityType);
    }


    public String getTypeClsName() {
        return type.getName();
    }

    public boolean isTypeEndsWith(String className) {
        return type.getName().endsWith(className);
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
