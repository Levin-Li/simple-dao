package com.levin.commons.dao.util;


import com.levin.commons.service.domain.Desc;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

/**
 * 类型解释器
 * <p/>
 * 非线程安全类
 * <p/>
 * <p/>
 * <p/>
 */
public class SimpleTypeParser {

    /**
     * 类型堆栈用来检测是否存在递归
     */
    protected Stack<Type> typeStack = new Stack();

    /**
     * 泛型堆栈，用来获取子具体的类型
     */
    protected Stack<Map<String, Type>> genericStack = new Stack();

    /**
     * 解析泛型中存在的具体类型
     *
     * @param type
     * @return
     */
    protected Map<String, Type> parseSubType(ParameterizedType type) {

        Map<String, Type> genricTypeMap = new HashMap<>();

        if (!(type.getRawType() instanceof Class))
            return genricTypeMap;

        Class rawType = (Class) type.getRawType();

        String className = rawType.getName();

        TypeVariable[] typeParameters = rawType.getTypeParameters();

        if (typeParameters == null || typeParameters.length < 1)
            return genricTypeMap;


        Type[] types = type.getActualTypeArguments();

        int i = -1;

        if (types != null) {
            for (Type subType : types) {
                i++;
                String key = className + "." + typeParameters[i].getName();

                //如果还是类型变量
                if (subType instanceof TypeVariable) {
                    TypeVariable subTypeV = (TypeVariable) subType;
                    Type typeFromParent = findTypeFromParent(subTypeV);

                    genricTypeMap.put(key, typeFromParent);
//                    genricTypeMap.put(((Class) subTypeV.getGenericDeclaration()).getName() + "." + subTypeV.getName(), typeFromParent);
                } else
                    genricTypeMap.put(key, subType);
            }
        }

        return genricTypeMap;
    }

    /**
     * 获取当前上下文中存在的类型
     *
     * @param typeVariable
     * @return
     */
    protected Type findTypeFromParent(TypeVariable typeVariable) {

        int n = genericStack.size();

        String key = getKey(typeVariable);

        while (n-- > 0) {

            Type type = genericStack.get(n).get(key);

            if (type == null || type instanceof TypeVariable)
                continue;
            else
                return type;
        }

        return typeVariable;
    }

    /**
     * 具体类型变量标识
     *
     * @param typeVariable
     * @return
     */
    protected String getKey(TypeVariable typeVariable) {
        return ((Class) typeVariable.getGenericDeclaration()).getName() + "." + typeVariable.getName();
    }

    /**
     * 获取当前上下文处理类型
     *
     * @return
     */
    protected Object peek() {
        return typeStack.isEmpty() ? "" : typeStack.peek();
    }


    /**
     * 解析指定类型
     *
     * @param method
     * @return
     */
    public FieldInfo parseParams(Method method) {

        Type[] parameterTypes = method.getGenericParameterTypes();

        Annotation[][] methodParameterAnnotations = method.getParameterAnnotations();

        if (parameterTypes.length < 1)
            return new FieldInfo(null, Void.class);
        else if (parameterTypes.length == 1)
            return parse(parameterTypes[0]);

        FieldInfo fieldInfo = new FieldInfo(null);

        fieldInfo.isList = true;

        for (int i = 0; i < parameterTypes.length; i++) {
            parse(parameterTypes[i]).setParent(fieldInfo).annotations = methodParameterAnnotations[i];
        }

        return fieldInfo;
    }

    /**
     * 解析方法的返回值类型
     *
     * @param method
     * @return
     */
    public FieldInfo parseReturnType(Method method) {
        return parse(method.getGenericReturnType());
    }

    /**
     * 解析指定类型
     *
     * @param type
     * @return
     */
    public FieldInfo parse(Type type) {
        return parse(null, null, type);
    }


    /**
     * 解析类型
     *
     * @param parent
     * @param name
     * @param type
     * @param actualTypeArguments
     * @return
     */
    public FieldInfo parse(FieldInfo parent, String name, final Type type, Type... actualTypeArguments) {

        //这个类型的判断位置必须在第一位
        if (type instanceof TypeVariable) {//如果发现是没有型别的泛型

            Type findType = findTypeFromParent((TypeVariable) type);

            if ((findType instanceof TypeVariable)) {

                findType = Object.class;

                System.err.println(peek() + " " + name + " 没有具体的化的泛型[" + type + "]定义，自动转换成 " + findType);

                return new FieldInfo(name, findType).setParent(parent);

            } else {

                System.out.println(peek() + " " + name + ":" + type + " 找到子类型泛型子类型：" + findType);

                return parse(parent, name, findType);

            }
        } else if (type instanceof ParameterizedType) {

            //子类型入栈
            ParameterizedType pType = (ParameterizedType) type;

            Map<String, Type> subType = parseSubType(pType);
            genericStack.push(subType);

            System.out.println(peek() + " " + name + ":" + type + " 发现泛型并解析子类型：" + subType);

            FieldInfo info = (parse(parent, name, pType.getRawType(), pType.getActualTypeArguments()));

            genericStack.pop();

            return info;

        } else if (isPrimitive(type)
                || isBaseObject(type)) {

            System.out.println(peek() + " 发现简单字段：" + name + ", 类型:" + type);

            return new FieldInfo(name, type).setParent(parent);

            //如果是原子类型
        } else if (isArray(type)) {//如果是数组

            Type eleType = null;

            if (type instanceof GenericArrayType)
                eleType = ((GenericArrayType) type).getGenericComponentType();
            else
                eleType = ((Class) type).getComponentType();

            System.out.println(peek() + " " + " 发现数组：" + name + " , 数组类型：" + eleType);

            FieldInfo info = new FieldInfo(name, null, type);
            info.isList = true;
            info.setParent(parent);

            //设置类型
            parse(info, null, eleType);

            return info;

        } else if (isMap(type)) {

            boolean hasSubType = actualTypeArguments != null
                    && actualTypeArguments.length > 0;

            Type eleType = hasSubType ? actualTypeArguments[1] : Object.class;

            System.out.println(peek() + " " + " 发现Map对象：" + name + ", 元素类型：" + eleType);

            FieldInfo info = new FieldInfo(name, null, type);
            info.isMap = true;
            info.setParent(parent);

            parse(info, null, eleType);

            return info;

        } else if (isIterable(type)) {

            boolean hasSubType = actualTypeArguments != null
                    && actualTypeArguments.length > 0;

            Type eleType = hasSubType ? actualTypeArguments[0] : Object.class;

            System.out.println(peek() + " " + " 发现可迭代对象：" + name + " ,元素类型：" + eleType);

            FieldInfo info = new FieldInfo(name, null, type);
            info.isList = true;
            info.setParent(parent);

            //设置类型
            parse(info, null, eleType);

            return info;

        } else if (typeStack.contains(type)) {

            System.err.println(peek() + " " + " 出现递归：" + getStackInfo((Class) type));

            FieldInfo info = new FieldInfo(name, null, type);
            info.isPrimitive = true;
            info.isRecursion = true;

            return info.setParent(parent);

        } else if ((type instanceof Class)) {//如果是普通对象

            System.out.println(peek() + " " + " 发现复合对象：" + name + ", 类型：" + type);

            FieldInfo info = new FieldInfo(name, null, type);
            info.isObject = true;

            info.annotations = ((Class) type).getDeclaredAnnotations();

            info.setParent(parent);

            typeStack.push(type);

            Type superclass = ((Class) type).getGenericSuperclass();

            if (!isBaseObject(superclass)) {

                FieldInfo fieldInfo = parse(null, null, superclass);

                //因为是同个类，所以要把孩子节点取出
                if (fieldInfo != null)
                    info.addSub(fieldInfo.subList);
            }

            final Class typeClass = (Class) type;

            for (Field f : typeClass.getDeclaredFields()) {
                //如果不是静态字段，或是常量字段
                if (!Modifier.isStatic(f.getModifiers())
                        && !Modifier.isFinal(f.getModifiers())) {

                    final Type genericType = f.getGenericType();

                    if (isPrimitive(genericType)
                            || isBaseObject(genericType)) {
                        FieldInfo e = new FieldInfo(f.getName(), f, genericType);
                        e.isPrimitive = true;
                        e.annotations = f.getDeclaredAnnotations();
                        e.setParent(info);
                    } else {
                        FieldInfo e = parse(info, f.getName(), genericType);
                        e.name = f.getName();
                        e.annotations = f.getDeclaredAnnotations();
                        e.field = f;
                    }
                }
            }

            typeStack.pop();

            return info;

        } else {
            System.err.println("*** Parse Error *** ,未知的类型：" + type);
            return null;
        }
    }

    protected String getStackInfo(Class typeClass) {

        StringBuilder b = new StringBuilder();

        for (Type type : typeStack) {
            b.append(((Class) type).getName()).append(" --> ");
        }

        b.append(typeClass.getName());

        return b.toString();
    }


    /////////////////////////////////////////////////////////////////////////////////

    public static boolean isArray(Type type) {
        return (type instanceof GenericArrayType)
                || ((type instanceof Class) && ((Class) type).isArray());
    }

    public static boolean isIterable(Type type) {
        return (type instanceof Class)
                && Iterable.class.isAssignableFrom(((Class) type));
    }

    public static boolean isMap(Type type) {
        return (type instanceof Class)
                && Map.class.isAssignableFrom(((Class) type));
    }

    public static boolean isBaseObject(Type type) {
        return (type instanceof Class)
                && (Object.class == type || Object.class.getName().equals(((Class) type).getName()));
    }

    public static boolean isPrimitive(Type type) {

        if (!(type instanceof Class))
            return false;

        Class clazz = (Class) type;

        return clazz.isPrimitive()
                || String.class.isAssignableFrom(clazz)
                || Boolean.class.isAssignableFrom(clazz)
                || Date.class.isAssignableFrom(clazz)
                || Double.class.isAssignableFrom(clazz)
                || Float.class.isAssignableFrom(clazz)
                || Enum.class.isAssignableFrom(clazz)
                || Long.class.isAssignableFrom(clazz)
                || Integer.class.isAssignableFrom(clazz)
                || Short.class.isAssignableFrom(clazz)
                || Byte.class.isAssignableFrom(clazz)
                || Character.class.isAssignableFrom(clazz)
                || Void.class.isAssignableFrom(clazz);

    }


    public static class FieldInfo implements Serializable {

        private FieldInfo parent;

        private String name;

        private Field field;

        private Annotation[] annotations;

        private transient Type valueType;

        private List<FieldInfo> subList = new ArrayList<>();

        //是否是原子类型
        private boolean isPrimitive;

        //是否是对象
        private boolean isObject;

        //是否是map
        private boolean isMap;

        //是否是列表
        private boolean isList;

        //是否出现递归
        private boolean isRecursion;

        //以下3个字段为toString时使用
        //缩进
        private int indent = 0;

        private boolean isShowType = false;

        private boolean isShowDesc = true;

        public FieldInfo(FieldInfo parent) {
            this.parent = parent;
        }

        public FieldInfo(String name, Field field, Type valueType) {
            this.name = name;
            this.field = field;
            this.valueType = valueType;
        }


        public FieldInfo(String name, Type valueType) {
            this.name = name;
            this.valueType = valueType;
            this.isPrimitive = true;
        }

        public FieldInfo addSub(FieldInfo... fieldInfos) {

            for (FieldInfo fieldInfo : fieldInfos) {

                if (fieldInfo == null) continue;

                fieldInfo.parent = this;

                subList.add(fieldInfo);
            }

            return this;
        }

        public FieldInfo addSub(List<FieldInfo> fieldInfos) {

            for (FieldInfo fieldInfo : fieldInfos) {

                if (fieldInfo == null) continue;

                fieldInfo.parent = this;

                subList.add(fieldInfo);
            }

            return this;
        }

        public FieldInfo setParent(FieldInfo parent) {

            if (parent != null)
                parent.addSub(this);

            return this;
        }

        public FieldInfo getParent() {
            return parent;
        }

        public String getName() {
            return name;
        }

        public Field getField() {
            return field;
        }

        public Type getValueType() {
            return valueType;
        }

        public List<FieldInfo> getSubList() {
            return subList;
        }

        public boolean isPrimitive() {
            return isPrimitive;
        }

        public boolean isObject() {
            return isObject;
        }

        public boolean isMap() {
            return isMap;
        }

        public boolean isList() {
            return isList;
        }

        public boolean isRecursion() {
            return isRecursion;
        }

        public int getIndent() {
            return indent;
        }

        public boolean isShowType() {
            return isShowType;
        }

        public void setShowType(boolean showType) {
            isShowType = showType;
        }

        public boolean isShowDesc() {
            return isShowDesc;
        }

        public void setShowDesc(boolean showDesc) {
            isShowDesc = showDesc;
        }

        public boolean isAttr() {
            return name != null && name.trim().length() > 0;
        }

        StringBuilder buf = new StringBuilder();

        StringBuilder indentTxt = new StringBuilder();

        private FieldInfo appendIndent() {

            if (indentTxt.length() < 1) {

                int n = indent;

                while (n-- > 0)
                    indentTxt.append("  ");
            }

            buf.append(indentTxt);

            return this;
        }


        private FieldInfo append(Object value) {
            if (value != null)
                buf.append(value);
            return this;
        }

        @Override
        public String toString() {

            //如果已经生成，则不在生成
            if (buf.length() > 0)
                return buf.toString();

            //缩进增加
            if (parent != null) {
                this.indent = parent.indent + 1;
                this.isShowDesc = parent.isShowDesc;
                this.isShowType = parent.isShowType;
            }

            //首先进行缩进
            appendIndent();

            //是否是属性
            if (isAttr()) {
                append(name).append(" : ");
            }

            if (isPrimitive) {
                //如果是递归对象则打印全类名
                if (valueType != Void.class)
                    append(isRecursion ? ((Class) valueType).getName() : ((Class) valueType).getSimpleName());
            } else if (isMap) {

                //Map开始
                append("{\n");

                int k = 1;
                for (FieldInfo fieldInfo : subList) {

                    //增加map_key的表达方式
                    if (!fieldInfo.isAttr())
                        fieldInfo.name = "map_key_" + (k++);

                    append(fieldInfo);
                }

                //增加缩进
                appendIndent().append("}");

            } else if (isList) {

                append("[\n");

                for (FieldInfo fieldInfo : subList) {
                    //数组元素名称
                    //fieldInfo.name = null;
                    append(fieldInfo);
                }

                //增加缩进
                appendIndent().append("]");

            } else if (isObject) {

                append("{\n");

                if (isShowType()) {
                    FieldInfo ele = new FieldInfo("\"@Type\":\"", valueType);
                    ele.parent = this;
                    subList.add(0, ele);
                }

                for (FieldInfo fieldInfo : subList) {
                    append(fieldInfo);
                }

                //增加缩进
                appendIndent().append("}");

            } else {
                System.out.println(" ***** 未知的类型 *****" + name + valueType);
            }

            //如果有兄弟结点，且自己不是最后一个
            if (parent != null
                    && parent.subList.indexOf(this) < (parent.subList.size() - 1))
                append(" , ");

            if (isShowDesc()) {
                Desc annotation = field == null ? null : field.getAnnotation(Desc.class);
                if (annotation != null && annotation.value() != null)
                    buf.append("    /* " + annotation.value().replace("\r", "\\r").replace("\n", "\\n") + " */");
            }

            append("\n");

            return buf.toString();
        }
    }


    public static void main(String[] args) {

        System.out.println(isIterable(List.class));
        System.out.println(isIterable(Map.class));
        System.out.println(isIterable((new int[]{}).getClass()));
        System.out.println(isArray((new int[]{}).getClass()));

    }

}
