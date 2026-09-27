package com.levin.commons.dao.codegen;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ModuleInstructionTemplateTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldGenerateInstructionsInSourceModule() throws Exception {
        Path sourceModule = tempDir.resolve("entities");

        ServiceModelCodeGenerator.genModuleInstructionFiles(Map.of(), sourceModule.toFile());

        assertTrue(Files.isRegularFile(sourceModule.resolve("模块开发说明.md")));
        assertTrue(Files.isRegularFile(sourceModule.resolve("代码生成说明.md")));
        assertTrue(Files.isRegularFile(sourceModule.resolve("后端项目开发规则.md")));
        String scopeRules = Files.readString(sourceModule.resolve("请求对象数据范围规则.md"));
        assertTrue(scopeRules.contains("为什么这样划分"));
        assertTrue(scopeRules.contains("普通用户的共享标志不能扩大更新、删除范围"));
    }
}
