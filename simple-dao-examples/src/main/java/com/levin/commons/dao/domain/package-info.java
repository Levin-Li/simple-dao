/**
 * 实体包定义
 *
 * <p>定义ID生成器
 *
 * @author Auto gen by simple-dao-codegen, @time: 2023年8月6日 下午5:34:40, 代码生成哈希校验码：[ce540a5d8e4c0499559ed69e15ebd164]，请不要修改和删除此行内容。
 */
@GenericGenerator(name = "guid", strategy = "org.hibernate.id.GUIDGenerator")

// 版本1的uuid生成器
@GenericGenerator(
        name = "default_uuid",
        strategy = "org.hibernate.id.UUIDGenerator",
        parameters = {
            @Parameter(
                    name = "uuid_gen_strategy_class",
                    value = "org.hibernate.id.uuid.CustomVersionOneStrategy"),
        })
// 默认的id生成器
@GenericGenerator(
        name = "default_id",
        strategy = "com.levin.commons.dao.support.DelegateIdGenerator",
        parameters = {
            @Parameter(
                    name = "uuid_gen_strategy_class",
                    value = "org.hibernate.id.uuid.CustomVersionOneStrategy"),
        })
@GenericGenerator(name = "hex_uuid", strategy = "org.hibernate.id.UUIDHexGenerator")
@GenericGenerator(name = "table_gid", strategy = "org.hibernate.id.TableGenerator")

// Hibernate 字段类型映射器
@TypeDefs({
    @TypeDef(name = "EnumDesc", defaultForType = EnumDesc.class, typeClass = EnumDescType.class)
})
package com.levin.commons.dao.domain;

import com.levin.commons.dao.support.EnumDescType;
import com.levin.commons.service.domain.EnumDesc;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
