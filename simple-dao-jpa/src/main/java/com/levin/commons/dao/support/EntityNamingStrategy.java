package com.levin.commons.dao.support;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy;

/**
 * 表名和字段名命名策略
 */
public class EntityNamingStrategy extends SpringPhysicalNamingStrategy {

    @Override
    public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment jdbcEnvironment) {

        String text = name.getText();

        int index = text.indexOf("-");

        if (index > 1) {

            //com.xxx.commons.agent-exam_tasks

            //@Entity(name = ModuleTableOption.PREFIX + "exam_tasks")

            //  本段代码获取全路径包名的最后一个包名，做为表的前缀

            text = text.substring(0, index);

            while (text.endsWith(".")) {
                text = text.substring(0, text.length() - 1);
            }

            if (text.lastIndexOf(".") != -1) {

                text = text.substring(text.lastIndexOf(".") + 1).trim();

                name = new Identifier(text + "_" + name.getText().substring(index + 1), name.isQuoted());

            }
        }

        return super.toPhysicalTableName(name, jdbcEnvironment);
    }

}
