
1、通过 javaagent 加载 ClassFileTransformer

2、ClassFileTransformer
       @Override
       public native byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain domain, byte[] classBuffer) throws IllegalClassFormatException;
  通过原生方法实现 类定义数据解密
  
  代码关键逻辑 判定 spring 类加载器
  对 spring 类加载器进行 ASM 增强，对方法进行增强，调用native原生 进行自己数组的转换
         protected final Class<?> defineClass(String name, byte[] b, int off, int len,
                                              ProtectionDomain protectionDomain)
  
  原生 native 方法，需要对 ClassFileTransformer 所在的jar 文件进行 sha-256 哈希校验，
  对参数  "-XX:+DisableAttachMechanism" 进行校验
  
  不允许 多个 javaagent
  不允许  agentlib
  
  对 spring 应用的限制，不支持代码织入，只支持代码增强
  
  静态代理分为：编译时织入（特殊编译器实现）、类加载时织入（特殊的类加载器实现）。
         动态代理有  ：   jdk动态代理（基于接口来实现）、CGlib（基于类实现）。
  
  对 spring 类加载器，强行设置 parent
  
  parent = new ClassFileTransformer(springLoader); 
  
  
  