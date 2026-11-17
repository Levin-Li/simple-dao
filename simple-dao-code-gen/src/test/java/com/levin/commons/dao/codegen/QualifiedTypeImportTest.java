package com.levin.commons.dao.codegen;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QualifiedTypeImportTest {
    @Test
    void removesNestedImportsWhenTypesAreQualified() {
        // 同名内部类均通过外层类限定时，不需要导入内部类。
        String source = "package sample;\n"
                + "import a.Customer;\nimport b.Supplier;\n"
                + "import a.Customer.Type;\nimport b.Supplier.Type;\n"
                + "class Example { Customer.Type customer; Supplier.Type supplier; }";
        String result = ImportOptimizer.optimizeImports("Example.java", source);

        assertTrue(result.contains("import a.Customer;"));
        assertTrue(result.contains("import b.Supplier;"));
        assertFalse(result.contains("import a.Customer.Type;"));
        assertFalse(result.contains("import b.Supplier.Type;"));
    }

    @Test
    void retainsImportForUnqualifiedTypeAlongsideQualifiedType() {
        // 限定类型与简单类型共存时，简单类型仍依赖自身导入。
        String source = "package sample;\nimport a.Customer;\nimport b.Type;\n"
                + "class Example { Customer.Type customer; Type other; }";
        String result = ImportOptimizer.optimizeImports("Example.java", source);

        assertTrue(result.contains("import a.Customer;"));
        assertTrue(result.contains("import b.Type;"));
    }
    @Test
    void generationPipelineRemovesConflictingUnusedImports() {
        // 模拟模板输出，验证正式生成路径也执行冲突导入清理。
        String source = "package sample;\nimport a.Customer;\nimport b.Supplier;\n"
                + "import a.Customer.Type;\nimport b.Supplier.Type;\n"
                + "class Example { Customer.Type customer; Supplier.Type supplier; }";
        String result = ServiceModelCodeGenerator.removeConflictingImports("Example.java", source);

        assertFalse(result.contains("import a.Customer.Type;"));
        assertFalse(result.contains("import b.Supplier.Type;"));
        assertTrue(result.contains("Customer.Type customer"));
    }
    @Test
    void qualifiedEnumConstantDoesNotUseNestedTypeImports() {
        // 嵌套枚举常量的中间名称不依赖同名内部类型导入。
        String source = "package sample;\nimport a.Customer.Type;\nimport b.Supplier.Type;\n"
                + "import c.OrderBy;\nclass Example { OrderBy.Type dir = OrderBy.Type.Desc; }";
        String result = ServiceModelCodeGenerator.removeConflictingImports("Example.java", source);

        assertFalse(result.contains("import a.Customer.Type;"));
        assertFalse(result.contains("import b.Supplier.Type;"));
        assertTrue(result.contains("import c.OrderBy;"));
    }
}
