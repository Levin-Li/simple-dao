package com.levin.commons.dao.support;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 表名和字段名命名策略
 */
public class EntityNamingStrategy extends SpringPhysicalNamingStrategy {

    private final static Map<String, String> prefixMapping = new ConcurrentHashMap<>();

    /**
     * 设置前缀映射
     *
     * @param mappings
     */
    public static void setPrefixMapping(Map<String, String>... mappings) {

        if (!prefixMapping.isEmpty()) {
            throw new IllegalStateException("prefixMapping already set");
        }

        if (mappings != null) {
            for (Map<String, String> mapping : mappings) {
                if (mapping != null) {
                    prefixMapping.putAll(mapping);
                }
            }
        }
    }

    @Override
    public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment jdbcEnvironment) {

        final String text = name.getText();

        int index = text.indexOf("-");

        if (index > 1) {

            //com.xxx.commons.agent-exam_tasks

            //@Entity(name = ModuleTableOption.PREFIX + "exam_tasks")

            //  本段代码获取全路径包名的最后一个包名，做为表的前缀

            String prefix = text.substring(0, index);

            final String tabName = text.substring(index + 1);

            //去除尾部的.
            while (prefix.endsWith(".")) {
                prefix = prefix.substring(0, prefix.length() - 1);
            }

            String tempPrefix = prefixMapping.get(prefix);

            if (StringUtils.hasText(tempPrefix)) {

                prefix = StringUtils.trimAllWhitespace(tempPrefix);

            } else if (prefix.lastIndexOf(".") != -1) {

                prefix = prefix.substring(prefix.lastIndexOf(".") + 1).trim();

            }

            name = new Identifier(prefix + "_" + tabName, name.isQuoted());

        }

        return super.toPhysicalTableName(name, jdbcEnvironment);
    }

}
