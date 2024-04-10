package ${CLASS_PACKAGE_NAME};

import com.levin.commons.dao.*;
import com.levin.commons.dao.domain.*;
import com.levin.commons.dao.annotation.*;
import com.levin.commons.service.domain.*;
import com.levin.commons.dao.domain.support.*;

import com.levin.commons.service.support.*;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import javax.persistence.*;
import java.util.List;
import org.hibernate.annotations.Type;

/**
 * 示例代码
 * @author Auto gen by simple-dao-codegen, @time: ${.now}, 代码生成哈希校验码：[]，请不要修改和删除此行内容。
 *
 */

@Data
@EqualsAndHashCode(of = {"id"})
@Accessors(chain = true)
@FieldNameConstants

@Schema(title = "测试角色", description = "示例代码")
@Entity(name = EntityConst.PREFIX + "TestRole")
@Cacheable
@Table(
        indexes = {
                @Index(columnList = E_TestRole.orderCode),
                @Index(columnList = E_TestRole.enable),
                @Index(columnList = E_TestRole.createTime),
                @Index(columnList = E_TestRole.tenantId),
                @Index(columnList = E_TestRole.name),
                @Index(columnList = E_TestRole.code),

                //基于租户的复合索引
                @Index(columnList = E_TestRole.tenantId +"," + E_TestRole.id),
        },

        uniqueConstraints = {
                @UniqueConstraint(columnNames = {E_TestRole.tenantId, E_TestRole.code}),
                @UniqueConstraint(columnNames = {E_TestRole.tenantId, E_TestRole.name}),
        }
)

//逻辑删除的例子
//@EntityOption(disableActions = EntityOption.Action.Delete,logicalDeleteFieldName = "deleted",logicalDeleteValue = "true")

//@EntityListener(${entityName}Listener.class)

//@Inheritance(strategy = InheritanceType.JOINED)//定义实体类的继承策略，这里表示使用Joined Table的继承策略，子类和父类分别映射到不同的数据库表
//@DiscriminatorColumn(name = E_User.type)//DiscriminatorColumn注解指定了区分列的名称
// 子类中需要配置注解 @DiscriminatorValue("Legal")

//DiscriminatorColumn 字段例子
//@Column(length = 64, nullable = false, insertable = false, updatable = false)
//String type;

//默认
//@DiscriminatorValue("Default")

//关于 JPA 继承模型
//@DiscriminatorColumn
//@DiscriminatorValue
//@PrimaryKeyJoinColumn(name="aId", referencedColumnName="id")
//@Inheritance( strategy = InheritanceType.TABLE_PER_CLASS )
//@MappedSuperclass告诉JPA提供者包含基类的持久性属性，就好像它们是由扩展用@MappedSuperclass注解的超类的子类所声明的@MappedSuperclass 。
//但是，inheritance仅在OOP世界中是可见的，因为从数据库的angular度来看，没有任何基类的迹象。 只有子类实体将有一个关联的映射表。
//@Inheritance注释是为了实现数据库表结构中的OOPinheritance模型。 更多的，你可以查询用@Inheritance注解的基类，但是你不能用@MappedSuperclass注解的基类。
//现在，您要使用@Inheritance JPA注释的原因是要实施像“战略模式”这样的行为驱动模式 。另一方面， @MappedSuperclass只是一种重用基本属性，关联，甚至是使用公共基类的实体@Id方法。
//不过，使用@Embeddabletypes可以达到几乎相同的目标。 唯一的区别是你不能重复@Embeddable的@Id定义，但你可以用@MappedSuperclass 。

//实体模型的类别
@EntityCategory(EntityOpConst.BIZ_TYPE_NAME)

//@Cacheable(false) //JPA缓存

public class TestRole
        extends AbstractNamedMultiTenantObject {

    @Schema(title = "数据范围")
    public enum OrgDataScope implements EnumDesc {
        @Schema(title = "所有部门") All,
        @Schema(title = "指定部门") Assigned,
        @Schema(title = "仅本部门（不含子部门）") MyDept,
        @Schema(title = "本部门及子部门") MyDeptAndChildren,
        @Schema(title = "仅本人数据") MySelf,

        ;
        @Override
        public String toString() {
            return nameAndDesc();
        }
    }

    @Id
//    @GeneratedValue
    @GeneratedValue(generator = "default_uuid")
    @Column(length = 64)
    protected String id;

    @Schema(title = "编码")
    @Column(nullable = false, length = 128)
    @Contains
    protected String code;

    @Schema(title = "图标")
    protected String icon;

    @Schema(title = "部门数据权限")
    @Column(nullable = false, length = 64)
    @Enumerated(EnumType.STRING)
    protected OrgDataScope orgDataScope;

    @Schema(title = "指定的部门列表", description = "Json数组")
    @Lob
    @InjectVar(domain = "dao", expectBaseType = List.class, expectGenericTypes = {String.class}, converter = PrimitiveArrayJsonConverter.class, isRequired = "false")
    protected String assignedOrgIdList;

    @Schema(title = "资源权限列表", description = "Json数组")
    @Lob
    @Basic(fetch = FetchType.LAZY) //延迟抓取
    @InjectVar(domain = "dao", expectBaseType = List.class, expectGenericTypes = {String.class}, converter = PrimitiveArrayJsonConverter.class, isRequired = "false")
    protected String permissionList;

    @Override
    @PrePersist
    public void prePersist() {

        super.prePersist();

        if (orgDataScope == null) {
            orgDataScope = OrgDataScope.MySelf;
        }
    }

}

