package com.levin.commons.plugins.simple;

import com.levin.commons.plugins.BaseMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Mojo(name = "run-method", requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class RunMethodMojo extends BaseMojo {

    @Parameter
    private String className;

    @Parameter
    private String methodName;

    @Parameter
    private Object[] methodParams;

    @Override
    public void executeMojo() throws MojoExecutionException, MojoFailureException, InterruptedException {

        if (!StringUtils.hasText(className)
                || !StringUtils.hasText(methodName)) {
            getLog().error("*** nothing to do, no className and methodName config.");
        }

        getLog().info("*** ready to run " + className + " " + methodName + " ...");

        final CountDownLatch countDownLatch = new CountDownLatch(1);

        new Thread(() -> {
            try {
                invokeMethod();
            } finally {
                countDownLatch.countDown();
            }

        }).start();

        countDownLatch.await();
    }

    private void invokeMethod() {

        try {
            //先设置当前线程的类加载器
            Thread.currentThread().setContextClassLoader(getClassLoader());

            Class<?> loadClass = getClassLoader().loadClass(className);

            Method method = null;

            try {
                //尝试获取方法
                Class[] paramTypes = Stream.of(methodParams).map(Object::getClass).collect(Collectors.toList()).toArray(new Class[0]);
                method = loadClass.getMethod(methodName, paramTypes);
            } catch (Exception e) {
            }

            if (method == null) {
                method = Arrays.stream(loadClass.getDeclaredMethods())
                        .filter(m -> m.getName().equals(methodName)).findFirst().orElse(null);
            }

            if (method != null) {
                method.invoke(Modifier.isStatic(method.getModifiers()) ? null : loadClass.newInstance(), methodParams);
            } else {
                throw new NoSuchMethodException(className + "." + methodName);
            }

            logger.info("*** " + className + "." + methodName + " invoke finished.");

        } catch (Exception e) {
            getLog().error("*** invoke " + className + "." + methodName + " fail , error：" + e.getMessage(), e);
        }

    }

}
