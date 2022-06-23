/**
 * 实体包定义
 * <p>
 * 定义ID生成器
 */
@GenericGenerator(name = "guid", strategy = "org.hibernate.id.GUIDGenerator")

//版本1的uuid生成器
@GenericGenerator(name = "default_uuid", strategy = "org.hibernate.id.UUIDGenerator",
        parameters = {
                @Parameter(name = "uuid_gen_strategy_class", value = "org.hibernate.id.uuid.CustomVersionOneStrategy"),
        }
)
//默认的uid生成器
@GenericGenerator(name = "default_id", strategy = "com.levin.oak.base.entities.DelegateIdGenerator",
        parameters = {
                @Parameter(name = "uuid_gen_strategy_class", value = "org.hibernate.id.uuid.CustomVersionOneStrategy"),
        }
)
@GenericGenerator(name = "hex_uuid", strategy = "org.hibernate.id.UUIDHexGenerator")
@GenericGenerator(name = "table_gid", strategy = "org.hibernate.id.TableGenerator")
package ${CLASS_PACKAGE_NAME};

