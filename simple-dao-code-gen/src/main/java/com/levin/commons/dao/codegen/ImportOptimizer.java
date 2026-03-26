package com.levin.commons.dao.codegen;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.ClassExpr;
import com.github.javaparser.ast.expr.EnclosedExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.MethodReferenceExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.TypeExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.TypeParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ImportOptimizer {
    private static final Logger log = LoggerFactory.getLogger(ImportOptimizer.class);
    private static final Map<String, Optional<Class<?>>> CLASS_CACHE = new ConcurrentHashMap<>();

    public static String optimizeImports(String fileName, String sourceCode) {
        try {
            return optimizeImports(fileName, StaticJavaParser.parse(sourceCode)).toString();
        } catch (Exception e) {
            log.warn("[{}] javaparser-解析源码失败,{}", fileName, e.getMessage(), e);
            return sourceCode;
        }
    }

    /**
     * 优化 Java 源码的 import
     */
    public static CompilationUnit optimizeImports(String fileName, CompilationUnit cu) {
        try {
            // 1. 基于 JavaParser 移除未使用导入
            removeUnusedImports(fileName, cu);

            // 2. 去重
            removeDuplicatedImports(cu);

            // 3. 排序（java.* -> jakarta.* -> javax.* -> 其他 -> static）
            sortImports(cu);

        } catch (Exception e) {
            log.warn("[{}] javaparser-优化导入失败,{}", fileName, e.getMessage(), e);
        }

        return cu;
    }

    /**
     * 移除未使用的导入
     */
    private static void removeUnusedImports(String fileName, CompilationUnit cu) {
        removeUnusedImportsByAst(fileName, cu);
    }

    /**
     * 纯 JavaParser 的未使用 import 清理。
     */
    private static void removeUnusedImportsByAst(String fileName, CompilationUnit cu) {
        List<Path> sourceRoots = resolveSourceRoots(fileName);
        String currentPackageName = cu.getPackageDeclaration()
                .map(packageDeclaration -> packageDeclaration.getNameAsString())
                .orElse("");
        String currentPackagePrefix = getPackagePrefix(cu.getPackageDeclaration()
                .map(packageDeclaration -> packageDeclaration.getNameAsString())
                .orElse(""));

        Set<String> usedTypeSimpleNames = new HashSet<>();
        Set<String> usedPotentialTypeQualifierNames = new HashSet<>();
        Set<String> packageQualifiedTypeSimpleNames = new HashSet<>();
        Set<String> usedLikelyStaticMemberNames = new HashSet<>();
        Set<String> usedUnscopedMethodNames = new HashSet<>();

        cu.findAll(AnnotationExpr.class)
                .forEach(annotationExpr -> {
                    String simpleName = annotationExpr.getName().getIdentifier();
                    usedTypeSimpleNames.add(simpleName);

                    if (isPackageQualifiedName(annotationExpr.getNameAsString())) {
                        packageQualifiedTypeSimpleNames.add(simpleName);
                    }
                });
        cu.findAll(ClassOrInterfaceType.class)
                .forEach(type -> {
                    String simpleName = type.getName().getIdentifier();
                    usedTypeSimpleNames.add(simpleName);

                    type.getScope().ifPresent(scope -> {
                        String rootScopeName = getRootScopeIdentifier(scope);
                        if (isLikelyPackageSegment(rootScopeName)) {
                            packageQualifiedTypeSimpleNames.add(simpleName);
                        } else {
                            usedPotentialTypeQualifierNames.add(rootScopeName);
                        }
                    });
                });
        cu.findAll(ClassExpr.class)
                .forEach(classExpr -> {
                    if (classExpr.getType().isClassOrInterfaceType()) {
                        usedTypeSimpleNames.add(classExpr.getType().asClassOrInterfaceType().getName().getIdentifier());
                    }
                });
        cu.findAll(TypeExpr.class)
                .forEach(typeExpr -> {
                    if (typeExpr.getType().isClassOrInterfaceType()) {
                        usedTypeSimpleNames.add(typeExpr.getType().asClassOrInterfaceType().getName().getIdentifier());
                    }
                });

        cu.findAll(NameExpr.class)
                .forEach(nameExpr -> {
                    String identifier = nameExpr.getName().getIdentifier();
                    if (isLikelyStaticMemberName(identifier)) {
                        usedLikelyStaticMemberNames.add(identifier);
                    }
                });
        cu.findAll(MethodCallExpr.class).stream()
                .filter(methodCallExpr -> !methodCallExpr.getScope().isPresent())
                .forEach(methodCallExpr -> usedUnscopedMethodNames.add(methodCallExpr.getName().getIdentifier()));
        cu.findAll(MethodCallExpr.class).stream()
                .filter(methodCallExpr -> methodCallExpr.getScope().isPresent())
                .forEach(methodCallExpr -> collectPotentialTypeNamesFromScope(
                        methodCallExpr.getScope().get(),
                        usedPotentialTypeQualifierNames));
        cu.findAll(FieldAccessExpr.class)
                .forEach(fieldAccessExpr -> collectPotentialTypeNamesFromScope(
                        fieldAccessExpr.getScope(),
                        usedPotentialTypeQualifierNames));
        cu.findAll(MethodReferenceExpr.class)
                .forEach(methodReferenceExpr -> collectPotentialTypeNamesFromScope(
                        methodReferenceExpr.getScope(),
                        usedPotentialTypeQualifierNames));

        Set<String> declaredTypeSimpleNames = cu.findAll(TypeDeclaration.class).stream()
                .map(TypeDeclaration::getNameAsString)
                .collect(Collectors.toSet());
        Set<String> typeParameterNames = cu.findAll(TypeParameter.class).stream()
                .map(TypeParameter::getNameAsString)
                .collect(Collectors.toSet());
        Set<String> declaredValueNames = cu.findAll(VariableDeclarator.class).stream()
                .map(variableDeclarator -> variableDeclarator.getName().getIdentifier())
                .collect(Collectors.toSet());
        Set<String> declaredMethodNames = cu.findAll(MethodDeclaration.class).stream()
                .map(MethodDeclaration::getNameAsString)
                .collect(Collectors.toSet());
        Set<String> explicitTypeImportSimpleNames = cu.getImports().stream()
                .filter(imp -> !imp.isStatic() && !imp.isAsterisk())
                .map(imp -> getImportSimpleName(imp.getNameAsString()))
                .collect(Collectors.toSet());
        Set<String> explicitStaticImportSimpleNames = cu.getImports().stream()
                .filter(imp -> imp.isStatic() && !imp.isAsterisk())
                .map(imp -> getImportSimpleName(imp.getNameAsString()))
                .collect(Collectors.toSet());

        Set<String> unresolvedTypeSimpleNames = new HashSet<>(usedTypeSimpleNames);
        unresolvedTypeSimpleNames.removeAll(packageQualifiedTypeSimpleNames);
        unresolvedTypeSimpleNames.removeAll(declaredTypeSimpleNames);
        unresolvedTypeSimpleNames.removeAll(typeParameterNames);
        unresolvedTypeSimpleNames.removeAll(explicitTypeImportSimpleNames);
        unresolvedTypeSimpleNames.removeIf(typeName -> classExists("java.lang." + typeName, sourceRoots));

        Set<String> effectiveTypeQualifierNames = new HashSet<>(usedPotentialTypeQualifierNames);
        effectiveTypeQualifierNames.removeAll(declaredValueNames);
        effectiveTypeQualifierNames.removeAll(declaredTypeSimpleNames);

        Set<String> staticReferenceNames = new HashSet<>();
        usedUnscopedMethodNames.stream()
                .filter(methodName -> !declaredMethodNames.contains(methodName))
                .forEach(staticReferenceNames::add);
        usedLikelyStaticMemberNames.stream()
                .filter(name -> !declaredValueNames.contains(name))
                .filter(name -> !declaredTypeSimpleNames.contains(name))
                .forEach(staticReferenceNames::add);
        Set<String> staticReferenceNamesForAsterisk = new HashSet<>(staticReferenceNames);
        staticReferenceNamesForAsterisk.removeAll(explicitStaticImportSimpleNames);

        Map<ImportDeclaration, Boolean> nonStaticAsteriskUsage =
                evaluateNonStaticAsteriskImports(cu.getImports(), unresolvedTypeSimpleNames, sourceRoots, currentPackagePrefix);
        Map<ImportDeclaration, Boolean> staticAsteriskUsage =
                evaluateStaticAsteriskImports(cu.getImports(), staticReferenceNamesForAsterisk, sourceRoots, currentPackagePrefix);

        List<ImportDeclaration> filtered = cu.getImports().stream()
                .filter(imp -> {
                    if (imp.isAsterisk()) {
                        return imp.isStatic()
                                ? staticAsteriskUsage.getOrDefault(imp, true)
                                : nonStaticAsteriskUsage.getOrDefault(imp, true);
                    }

                    String importName = imp.getNameAsString();
                    String simpleName = getImportSimpleName(importName);

                    if (imp.isStatic()) {
                        return staticReferenceNames.contains(simpleName)
                                || usedTypeSimpleNames.contains(simpleName);
                    }

                    if (importName.startsWith("java.lang.")) {
                        return false;
                    }

                    return usedTypeSimpleNames.contains(simpleName)
                            || effectiveTypeQualifierNames.contains(simpleName);
                })
                .collect(Collectors.toList());

        ensureStaticOwnerTypeImports(filtered, effectiveTypeQualifierNames, currentPackageName);

        cu.setImports(new NodeList<>(filtered));
    }

    private static Map<ImportDeclaration, Boolean> evaluateNonStaticAsteriskImports(
            List<ImportDeclaration> imports,
            Set<String> unresolvedTypeSimpleNames,
            List<Path> sourceRoots,
            String currentPackagePrefix) {
        Map<ImportDeclaration, Boolean> usage = new IdentityHashMap<>();

        List<ImportDeclaration> asteriskImports = imports.stream()
                .filter(imp -> imp.isAsterisk() && !imp.isStatic())
                .collect(Collectors.toList());

        if (asteriskImports.isEmpty()) {
            return usage;
        }

        if (unresolvedTypeSimpleNames.isEmpty()) {
            asteriskImports.forEach(imp -> usage.put(imp, false));
            return usage;
        }

        Map<ImportDeclaration, Set<String>> matchedByImport = new IdentityHashMap<>();
        Set<String> coveredTypeNames = new HashSet<>();

        for (ImportDeclaration imp : asteriskImports) {
            String packageName = imp.getNameAsString();
            if ("java.lang".equals(packageName)) {
                usage.put(imp, false);
                continue;
            }

            Set<String> matchedTypes = unresolvedTypeSimpleNames.stream()
                    .filter(typeName -> classExists(packageName + "." + typeName, sourceRoots))
                    .collect(Collectors.toSet());

            if (!matchedTypes.isEmpty()) {
                matchedByImport.put(imp, matchedTypes);
                coveredTypeNames.addAll(matchedTypes);
            }
        }

        Set<String> uncoveredTypeNames = new HashSet<>(unresolvedTypeSimpleNames);
        uncoveredTypeNames.removeAll(coveredTypeNames);
        Set<String> unresolvedTypeSuffixTokens = uncoveredTypeNames.stream()
                .map(ImportOptimizer::getTypeSuffixToken)
                .filter(token -> !token.isEmpty())
                .collect(Collectors.toSet());

        for (ImportDeclaration imp : asteriskImports) {
            if (usage.containsKey(imp)) {
                continue;
            }

            boolean hasMatchedTypes = matchedByImport.containsKey(imp);
            if (hasMatchedTypes) {
                usage.put(imp, true);
                continue;
            }

            // 如果仍有无法映射的类型名，保守保留其它 * 导入，避免误删。
            usage.put(imp, !uncoveredTypeNames.isEmpty()
                    && isSameModulePrefix(imp.getNameAsString(), currentPackagePrefix)
                    && matchesUnresolvedTypeSuffix(imp.getNameAsString(), unresolvedTypeSuffixTokens));
        }

        return usage;
    }

    private static Map<ImportDeclaration, Boolean> evaluateStaticAsteriskImports(
            List<ImportDeclaration> imports,
            Set<String> staticReferenceNames,
            List<Path> sourceRoots,
            String currentPackagePrefix) {
        Map<ImportDeclaration, Boolean> usage = new IdentityHashMap<>();

        List<ImportDeclaration> asteriskImports = imports.stream()
                .filter(imp -> imp.isAsterisk() && imp.isStatic())
                .collect(Collectors.toList());

        if (asteriskImports.isEmpty()) {
            return usage;
        }

        if (staticReferenceNames.isEmpty()) {
            asteriskImports.forEach(imp -> usage.put(imp, false));
            return usage;
        }

        Map<ImportDeclaration, Set<String>> matchedByImport = new IdentityHashMap<>();
        Set<ImportDeclaration> unresolvedImports = new HashSet<>();
        Set<String> coveredReferenceNames = new HashSet<>();

        for (ImportDeclaration imp : asteriskImports) {
            String className = imp.getNameAsString();
            Optional<Set<String>> memberNamesOpt = getPublicStaticMemberNames(className, sourceRoots);

            if (!memberNamesOpt.isPresent()) {
                unresolvedImports.add(imp);
                continue;
            }

            Set<String> matchedNames = memberNamesOpt.get().stream()
                    .filter(staticReferenceNames::contains)
                    .collect(Collectors.toSet());

            if (!matchedNames.isEmpty()) {
                matchedByImport.put(imp, matchedNames);
                coveredReferenceNames.addAll(matchedNames);
            }
        }

        Set<String> uncoveredReferenceNames = new HashSet<>(staticReferenceNames);
        uncoveredReferenceNames.removeAll(coveredReferenceNames);

        for (ImportDeclaration imp : asteriskImports) {
            if (matchedByImport.containsKey(imp)) {
                usage.put(imp, true);
                continue;
            }

            if (unresolvedImports.contains(imp)) {
                usage.put(imp, !uncoveredReferenceNames.isEmpty()
                        && isSameModulePrefix(imp.getNameAsString(), currentPackagePrefix));
                continue;
            }

            usage.put(imp, false);
        }

        return usage;
    }

    private static void collectPotentialTypeNamesFromScope(Expression expression, Set<String> out) {
        Expression scope = expression;

        while (scope instanceof EnclosedExpr) {
            scope = ((EnclosedExpr) scope).getInner();
        }

        if (scope.isNameExpr()) {
            String name = scope.asNameExpr().getName().getIdentifier();
            if (isLikelyTypeName(name)) {
                out.add(name);
            }
            return;
        }

        if (scope.isTypeExpr()) {
            if (scope.asTypeExpr().getType().isClassOrInterfaceType()) {
                out.add(scope.asTypeExpr().getType().asClassOrInterfaceType().getName().getIdentifier());
            }
            return;
        }

        if (scope.isFieldAccessExpr()) {
            FieldAccessExpr fieldAccessExpr = scope.asFieldAccessExpr();
            String name = fieldAccessExpr.getName().getIdentifier();
            if (isLikelyTypeName(name)) {
                out.add(name);
            }
            collectPotentialTypeNamesFromScope(fieldAccessExpr.getScope(), out);
        }
    }

    private static boolean isLikelyTypeName(String name) {
        return !name.isEmpty() && Character.isUpperCase(name.charAt(0));
    }

    private static boolean isLikelyStaticMemberName(String name) {
        if (name.isEmpty()) {
            return false;
        }
        if (isLikelyTypeName(name)) {
            return true;
        }
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (Character.isLetter(c) && Character.isLowerCase(c)) {
                return false;
            }
        }
        return true;
    }

    private static String getImportSimpleName(String importName) {
        int dotIdx = importName.lastIndexOf('.');
        return dotIdx < 0 || dotIdx + 1 >= importName.length()
                ? importName
                : importName.substring(dotIdx + 1);
    }

    private static boolean isPackageQualifiedName(String name) {
        int dotIdx = name.indexOf('.');
        return dotIdx > 0 && isLikelyPackageSegment(name.substring(0, dotIdx));
    }

    private static String getRootScopeIdentifier(ClassOrInterfaceType scopeType) {
        ClassOrInterfaceType current = scopeType;
        while (current.getScope().isPresent()) {
            current = current.getScope().get();
        }
        return current.getName().getIdentifier();
    }

    private static boolean isLikelyPackageSegment(String segment) {
        return !segment.isEmpty() && Character.isLowerCase(segment.charAt(0));
    }

    private static void ensureStaticOwnerTypeImports(
            List<ImportDeclaration> imports,
            Set<String> usedTypeQualifierNames,
            String currentPackageName) {
        Set<String> explicitTypeImports = imports.stream()
                .filter(imp -> !imp.isStatic() && !imp.isAsterisk())
                .map(ImportDeclaration::getNameAsString)
                .collect(Collectors.toSet());
        Set<String> wildcardTypeImportPackages = imports.stream()
                .filter(imp -> !imp.isStatic() && imp.isAsterisk())
                .map(ImportDeclaration::getNameAsString)
                .collect(Collectors.toSet());

        List<ImportDeclaration> staticImports = imports.stream()
                .filter(ImportDeclaration::isStatic)
                .collect(Collectors.toList());

        for (ImportDeclaration staticImport : staticImports) {
            String ownerFqn = getStaticImportOwnerFqn(staticImport);
            if (ownerFqn == null || ownerFqn.isEmpty()) {
                continue;
            }

            String ownerSimpleName = getImportSimpleName(ownerFqn);
            if (!usedTypeQualifierNames.contains(ownerSimpleName)) {
                continue;
            }

            String ownerPackage = getPackageName(ownerFqn);
            if ("java.lang".equals(ownerPackage) || ownerPackage.equals(currentPackageName)) {
                continue;
            }
            if (explicitTypeImports.contains(ownerFqn) || wildcardTypeImportPackages.contains(ownerPackage)) {
                continue;
            }

            imports.add(new ImportDeclaration(ownerFqn, false, false));
            explicitTypeImports.add(ownerFqn);
        }
    }

    private static String getStaticImportOwnerFqn(ImportDeclaration importDeclaration) {
        String name = importDeclaration.getNameAsString();
        if (!importDeclaration.isStatic()) {
            return name;
        }
        if (importDeclaration.isAsterisk()) {
            return name;
        }
        int dotIdx = name.lastIndexOf('.');
        return dotIdx > 0 ? name.substring(0, dotIdx) : name;
    }

    private static String getPackagePrefix(String packageName) {
        if (packageName == null || packageName.isEmpty()) {
            return "";
        }
        String[] segments = packageName.split("\\.");
        if (segments.length <= 3) {
            return packageName;
        }
        return String.join(".", Arrays.copyOf(segments, 3));
    }

    private static boolean isSameModulePrefix(String importPackage, String currentPackagePrefix) {
        if (currentPackagePrefix == null || currentPackagePrefix.isEmpty()) {
            return false;
        }
        return importPackage.equals(currentPackagePrefix)
                || importPackage.startsWith(currentPackagePrefix + ".");
    }

    private static String getPackageName(String className) {
        int dotIdx = className.lastIndexOf('.');
        return dotIdx > 0 ? className.substring(0, dotIdx) : "";
    }

    private static String getTypeSuffixToken(String typeName) {
        if (typeName == null || typeName.isEmpty()) {
            return "";
        }
        List<String> tokens = splitCamelCase(typeName);
        if (tokens.isEmpty()) {
            return typeName.toLowerCase(Locale.ROOT);
        }
        return tokens.get(tokens.size() - 1);
    }

    private static boolean matchesUnresolvedTypeSuffix(String importPackage, Set<String> unresolvedTypeSuffixTokens) {
        if (unresolvedTypeSuffixTokens == null || unresolvedTypeSuffixTokens.isEmpty()) {
            return false;
        }
        int dotIdx = importPackage.lastIndexOf('.');
        String lastSegment = dotIdx >= 0
                ? importPackage.substring(dotIdx + 1)
                : importPackage;
        return unresolvedTypeSuffixTokens.contains(lastSegment.toLowerCase(Locale.ROOT));
    }

    private static List<String> splitCamelCase(String value) {
        List<String> tokens = new ArrayList<>();
        if (value == null || value.isEmpty()) {
            return tokens;
        }

        StringBuilder current = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (i > 0 && Character.isUpperCase(ch) && current.length() > 0) {
                tokens.add(current.toString().toLowerCase(Locale.ROOT));
                current.setLength(0);
            }
            current.append(ch);
        }

        if (current.length() > 0) {
            tokens.add(current.toString().toLowerCase(Locale.ROOT));
        }

        return tokens;
    }

    private static List<Path> resolveSourceRoots(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return Collections.emptyList();
        }

        LinkedHashSet<Path> roots = new LinkedHashSet<>();

        try {
            Path path = Paths.get(fileName);
            if (!path.isAbsolute()) {
                path = path.toAbsolutePath();
            }
            path = path.normalize();

            String normalized = path.toString().replace('\\', '/');
            addSourceRootByMarker(roots, normalized, "/src/main/java");
            addSourceRootByMarker(roots, normalized, "/src/test/java");

            Path cursor = Files.isDirectory(path) ? path : path.getParent();
            int depth = 8;
            while (cursor != null && depth-- > 0) {
                roots.add(cursor.resolve("src/main/java").normalize());
                roots.add(cursor.resolve("src/test/java").normalize());
                cursor = cursor.getParent();
            }
        } catch (Exception ignore) {
        }

        return roots.stream()
                .filter(Files::isDirectory)
                .distinct()
                .collect(Collectors.toList());
    }

    private static void addSourceRootByMarker(Set<Path> roots, String normalizedPath, String marker) {
        int idx = normalizedPath.indexOf(marker + "/");
        if (idx < 0 && normalizedPath.endsWith(marker)) {
            idx = normalizedPath.length() - marker.length();
        }
        if (idx < 0) {
            return;
        }
        String rootStr = normalizedPath.substring(0, idx + marker.length());
        try {
            roots.add(Paths.get(rootStr).normalize());
        } catch (Exception ignore) {
        }
    }

    private static boolean classExists(String className, List<Path> sourceRoots) {
        return findClass(className).isPresent() || sourceTypeExists(className, sourceRoots);
    }

    private static boolean sourceTypeExists(String className, List<Path> sourceRoots) {
        return findSourceTypeFile(className, sourceRoots).isPresent();
    }

    private static Optional<Path> findSourceTypeFile(String className, List<Path> sourceRoots) {
        if (sourceRoots == null || sourceRoots.isEmpty()) {
            return Optional.empty();
        }

        String[] parts = className.split("\\.");
        if (parts.length < 2) {
            return Optional.empty();
        }

        for (int splitIdx = parts.length - 1; splitIdx > 0; splitIdx--) {
            String packagePath = String.join("/", Arrays.copyOfRange(parts, 0, splitIdx));
            String topLevelType = parts[splitIdx];
            String relative = packagePath + "/" + topLevelType + ".java";

            for (Path root : sourceRoots) {
                Path candidate = root.resolve(relative);
                if (Files.exists(candidate)) {
                    return Optional.of(candidate);
                }
            }
        }

        return Optional.empty();
    }

    private static Optional<Class<?>> findClass(String className) {
        return CLASS_CACHE.computeIfAbsent(className, ImportOptimizer::resolveClass);
    }

    private static Optional<Class<?>> resolveClass(String className) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = ImportOptimizer.class.getClassLoader();
        }

        Class<?> clazz = tryLoadClass(className, classLoader);
        if (clazz != null) {
            return Optional.of(clazz);
        }

        for (int idx = className.lastIndexOf('.'); idx > 0; idx = className.lastIndexOf('.', idx - 1)) {
            String nestedCandidate = className.substring(0, idx) + "$" + className.substring(idx + 1);
            clazz = tryLoadClass(nestedCandidate, classLoader);
            if (clazz != null) {
                return Optional.of(clazz);
            }
        }

        return Optional.empty();
    }

    private static Class<?> tryLoadClass(String className, ClassLoader classLoader) {
        try {
            return Class.forName(className, false, classLoader);
        } catch (Throwable ignore) {
            return null;
        }
    }

    private static Optional<Set<String>> getPublicStaticMemberNames(String className, List<Path> sourceRoots) {
        Optional<Class<?>> classOpt = findClass(className);
        if (classOpt.isPresent()) {
            return Optional.of(collectPublicStaticMemberNames(classOpt.get()));
        }

        Optional<Path> sourceFileOpt = findSourceTypeFile(className, sourceRoots);
        if (!sourceFileOpt.isPresent()) {
            return Optional.empty();
        }

        return resolvePublicStaticMemberNamesFromSource(sourceFileOpt.get());
    }

    private static Set<String> collectPublicStaticMemberNames(Class<?> clazz) {
        Set<String> memberNames = new HashSet<>();

        for (Field field : clazz.getFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                memberNames.add(field.getName());
            }
        }

        for (Method method : clazz.getMethods()) {
            if (Modifier.isStatic(method.getModifiers())) {
                memberNames.add(method.getName());
            }
        }

        for (Class<?> nestedClass : clazz.getClasses()) {
            if (Modifier.isStatic(nestedClass.getModifiers())) {
                memberNames.add(nestedClass.getSimpleName());
            }
        }

        return memberNames;
    }

    private static Optional<Set<String>> resolvePublicStaticMemberNamesFromSource(Path sourceFile) {
        try {
            CompilationUnit sourceCu = StaticJavaParser.parse(sourceFile);
            String topLevelName = sourceFile.getFileName().toString().replaceFirst("\\.java$", "");

            TypeDeclaration<?> typeDeclaration = sourceCu.getTypes().stream()
                    .filter(type -> topLevelName.equals(type.getNameAsString()))
                    .findFirst()
                    .orElseGet(() -> sourceCu.getTypes().isEmpty() ? null : sourceCu.getType(0));

            if (typeDeclaration == null) {
                return Optional.empty();
            }

            Set<String> memberNames = new HashSet<>();

            typeDeclaration.getMembers().forEach(member -> {
                if (member.isFieldDeclaration()) {
                    FieldDeclaration fieldDeclaration = member.asFieldDeclaration();
                    if (fieldDeclaration.isStatic()) {
                        fieldDeclaration.getVariables()
                                .forEach(variable -> memberNames.add(variable.getNameAsString()));
                    }
                    return;
                }

                if (member.isMethodDeclaration()) {
                    MethodDeclaration methodDeclaration = member.asMethodDeclaration();
                    if (methodDeclaration.isStatic()) {
                        memberNames.add(methodDeclaration.getNameAsString());
                    }
                    return;
                }

                if (member.isTypeDeclaration()) {
                    TypeDeclaration<?> nestedType = member.asTypeDeclaration();
                    if (nestedType.isStatic()) {
                        memberNames.add(nestedType.getNameAsString());
                    }
                }
            });

            return Optional.of(memberNames);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * 去重导入
     */
    private static void removeDuplicatedImports(CompilationUnit cu) {
        Map<String, ImportDeclaration> map = new LinkedHashMap<>();
        cu.getImports().forEach(imp -> {
            String key = (imp.isStatic() ? "S" : "N")
                    + "|"
                    + (imp.isAsterisk() ? "*" : "-")
                    + "|"
                    + imp.getNameAsString();
            map.putIfAbsent(key, imp);
        });
        cu.setImports(new NodeList<>(new ArrayList<>(map.values())));
    }

    /**
     * 导入排序（标准 IDEA 风格）
     * 1. java.*
     * 2. jakarta.* (Spring Boot 3.x)
     * 3. javax.* (遗留)
     * 4. 其他包
     * 5. 静态导入
     */
    private static void sortImports(CompilationUnit cu) {

        List<ImportDeclaration> imports = new ArrayList<>(cu.getImports());

        List<ImportDeclaration> javaImports = new ArrayList<>();
        List<ImportDeclaration> jakartaImports = new ArrayList<>();
        List<ImportDeclaration> javaxImports = new ArrayList<>();
        List<ImportDeclaration> otherImports = new ArrayList<>();
        List<ImportDeclaration> staticImports = new ArrayList<>();

        for (ImportDeclaration imp : imports) {
            String impStr = imp.getNameAsString();

            if (imp.isStatic()) {
                staticImports.add(imp);
            } else if (impStr.startsWith("java.")) {
                javaImports.add(imp);
            } else if (impStr.startsWith("jakarta.")) {
                jakartaImports.add(imp);
            } else if (impStr.startsWith("javax.")) {
                javaxImports.add(imp);
            } else {
                otherImports.add(imp);
            }
        }

        // 排序
        Comparator<ImportDeclaration> comparator = Comparator.comparing(ImportDeclaration::getNameAsString);
        javaImports.sort(comparator);
        jakartaImports.sort(comparator);
        javaxImports.sort(comparator);
        otherImports.sort(comparator);
        staticImports.sort(comparator);

        // 组合
        List<ImportDeclaration> sorted = new ArrayList<>();
        sorted.addAll(javaImports);
        sorted.addAll(jakartaImports);
        sorted.addAll(javaxImports);
        sorted.addAll(otherImports);
        sorted.addAll(staticImports);

        cu.setImports(new NodeList<>(sorted));
    }

}
