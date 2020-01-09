package com.levin.commons.dao.processor;


import com.levin.commons.service.domain.Desc;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@SupportedAnnotationTypes({"javax.persistence.MappedSuperclass", "javax.persistence.Entity"})
@SupportedSourceVersion(SourceVersion.RELEASE_6)
//@com.google.auto.service.AutoService(Processor.class)
@Deprecated
public class EntityClassProcessor extends AbstractProcessor {

    final Map<String, Object> processedFiles = new ConcurrentHashMap<>();


    public static final String CLASS_NAME_PREFIX = "N";

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        for (String typeName : this.getSupportedAnnotationTypes()) {


            Class<? extends Annotation> annoType = null;

            try {
                annoType = (Class<? extends Annotation>) Class.forName(typeName);
            } catch (ClassNotFoundException e) {
                this.processingEnv.getMessager().printMessage(Diagnostic.Kind.MANDATORY_WARNING, "Annotation processor " + getClass().getSimpleName() + "  can't found class " + typeName);
            }


            if (annoType != null) {
                process(roundEnv, roundEnv.getElementsAnnotatedWith(annoType));
            }

        }


        return false;
    }


    private static String newClassName(String className) {
        return newClassName(className, CLASS_NAME_PREFIX);
    }

    private static String newClassName(String className, String prefix) {

        if (className == null || className.trim().length() < 1)
            return "";

        int lastIndexOf = className.lastIndexOf('.');

        if (lastIndexOf == -1)
            return prefix + className;

        return className.substring(0, lastIndexOf + 1) + prefix + className.substring(lastIndexOf + 1);
    }


    private void process(RoundEnvironment roundEnv, Set<? extends Element> elementList) {

        Elements elementUtils = this.processingEnv.getElementUtils();

        Types typeUtils = this.processingEnv.getTypeUtils();


        for (Element element : elementList) {

            if (!element.getKind().isClass()) {
                continue;
            }


            TypeElement typeElement = (TypeElement) element;


            final String packageName = elementUtils.getPackageOf(typeElement).getQualifiedName().toString();

            final String fullClassName = typeElement.getQualifiedName().toString();

            final String newSimpleClassName = CLASS_NAME_PREFIX + typeElement.getSimpleName().toString();

            final String newFullClassName = packageName + "." + newSimpleClassName;

            final String superFullClassName = typeElement.getSuperclass().toString();

            final String newSuperFullClassName = newClassName(superFullClassName);


            if (processedFiles.containsKey(newFullClassName)) {

                this.processingEnv.getMessager().printMessage(Diagnostic.Kind.MANDATORY_WARNING, getClass().getSimpleName() + " <<<" + newFullClassName + ">>> already processed.");

                continue;
            } else {

                this.processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, getClass().getSimpleName() + " Processing class " + fullClassName + " --> " + newFullClassName);

            }

            boolean hasParent = processedFiles.containsKey(newSuperFullClassName);

            if (!hasParent) {
                try {
                    hasParent = Class.forName(newSuperFullClassName) != null;
                } catch (ClassNotFoundException e) {

                }
            }


            final StringBuilder codeBlock = new StringBuilder();


            codeBlock
                    .append("\n")
                    .append("package ").append(packageName).append(";\n")
                    .append("\n\n")

                    .append("@javax.persistence.metamodel.StaticMetamodel(").append(typeElement.getSimpleName()).append(".class)\n")

                    .append("public interface ").append(newSimpleClassName)

                    .append(" extends java.io.Serializable ")
                    .append(hasParent ? (" , " + newSuperFullClassName) : "")

                    .append(" {\n");


            for (Element subEle : hasParent ? typeElement.getEnclosedElements() : elementUtils.getAllMembers(typeElement)) {

                Set<Modifier> modifiers = subEle.getModifiers();

                if (subEle.getKind() != ElementKind.FIELD
                        || modifiers.contains(Modifier.STATIC)
                        || modifiers.contains(Modifier.TRANSIENT)
                        || modifiers.contains(Modifier.FINAL)) {
                    continue;
                }

                String fieldName = subEle.getSimpleName().toString();

                this.processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, getClass().getSimpleName() + " Processing field " + fieldName);

                Desc desc = subEle.getAnnotation(Desc.class);

                String name = "";

                if (desc != null) {

                    name = desc.name().trim();

                    if (name.length() < 1) {
                        name = desc.value().trim();
                    }

                    if (name.length() > 0) {
                        //public static final

                        name = name
                                .replace(' ', '_')
                                .replace('(', '_')
                                .replace(')', '_')
                                .replace('（', '_')
                                .replace('）', '_')
                                .replace('-', '_')
                                .replace('，', '_')
                                .replace(',', '_')
                                .replace('{', '_')
                                .replace('}', '_')
                                .replace('[', '_')
                                .replace(']', '_')
                                .replace('<', '_')
                                .replace('>', '_')
                                .replace('.', '_');

                        codeBlock.append("\n    String ").append(name).append(" = \"").append(fieldName).append("\";\n");
                    } else {
                        // this.processingEnv.getMessager().printMessage(Diagnostic.Kind.MANDATORY_WARNING, getClass().getSimpleName() + " process " + typeElement + " field " + fieldName + "  Annotation @Desc value or name contains invalid char " + desc);
                    }

                }


                if (!fieldName.equals(name)) {
                    //public static final
                    codeBlock.append("\n    String ").append(fieldName).append(" = \"").append(fieldName).append("\";\n");
                }

            }


            codeBlock.append("\n}\n");

            try {

                JavaFileObject sourceFile = this.processingEnv.getFiler().createSourceFile(newFullClassName);

                Writer writer = sourceFile.openWriter();

                writer.write(codeBlock.toString());

                writer.flush();

                writer.close();

                processedFiles.put(newFullClassName, sourceFile);

            } catch (IOException e) {

                this.processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, getClass().getSimpleName() + " Processing  " + fullClassName + " error, " + e.getLocalizedMessage());

                throw new RuntimeException(e);
            }

        }
    }

    public static void main(String[] args) {

        System.out.println(newClassName(String.class.getName()));

    }

}
