package com.levin.commons.dao.domain;

import com.levin.commons.dao.annotation.Contains;
import com.levin.commons.dao.domain.support.AbstractBaseEntityObject;
import com.levin.commons.dao.domain.support.AbstractMultiTenantObject;
import com.levin.commons.dao.domain.support.AbstractNamedMultiTenantObject;
import com.levin.commons.dao.domain.support.E_AbstractNamedEntityObject;
import com.levin.commons.service.domain.EnumDesc;
import com.levin.commons.service.domain.InjectVar;
import com.levin.commons.service.support.PrimitiveArrayJsonConverter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import javax.persistence.*;
import java.util.List;

/**
 * 示例代码
 *
 * @author Auto gen by simple-dao-codegen, @time: 2023年8月6日 下午5:17:54, 代码生成哈希校验码：[34410848f9b6119067f1ced2359d804a]，请不要修改和删除此行内容。
 */
@Data
@EqualsAndHashCode(of = {"id"})
@Accessors(chain = true)
@FieldNameConstants
@Schema(title = "测试角色", description = "示例代码")
@Entity(name = EntityConst.PREFIX + "TestRole")


// 属性的字段定义可覆盖
// @AssociationOverride
// 默认情况下，JPA 持续性提供程序自动假设子类继承超类中定义的持久属性及其关联映射。
// 如果继承的列定义对实体不正确（例如，如果继承的列名与已经存在的数据模型不兼容或作为数据库中的列名无效），请使用 @AssociationOverride 批注自定义从 
// @MappedSuperclass 或 @Embeddable 继承的  @OneToOne 或  @ManyToOne 映射，以更改与字段或属性关联的 @JoinColumn。
// 如果有多个要进行的@AssociationOverride更改，则必须使用@AssociationOverrides。
// 要自定义基本映射以更改它的@Column，请使用@AttributeOverride。

// 关于 JPA 继承模型
// @DiscriminatorColumn
// @DiscriminatorValue
// @PrimaryKeyJoinColumn(name="aId", referencedColumnName="id")
// @Inheritance( strategy = InheritanceType.TABLE_PER_CLASS )
// @MappedSuperclass告诉JPA提供者包含基类的持久性属性，就好像它们是由扩展用@MappedSuperclass注解的超类的子类所声明的@MappedSuperclass 。
// 但是，inheritance仅在OOP世界中是可见的，因为从数据库的angular度来看，没有任何基类的迹象。 只有子类实体将有一个关联的映射表。
// @Inheritance注释是为了实现数据库表结构中的OOPinheritance模型。
// 更多的，你可以查询用@Inheritance注解的基类，但是你不能用@MappedSuperclass注解的基类。
// 现在，您要使用@Inheritance JPA注释的原因是要实施像“战略模式”这样的行为驱动模式 。另一方面，
// @MappedSuperclass只是一种重用基本属性，关联，甚至是使用公共基类的实体@Id方法。
// 不过，使用@Embeddabletypes可以达到几乎相同的目标。 唯一的区别是你不能重复@Embeddable的@Id定义，但你可以用@MappedSuperclass 。

public class TestRole extends AbstractNamedMultiTenantObject {

    @Schema(title = "数据范围")
    public enum OrgDataScope implements EnumDesc {
        @Schema(title = "所有部门")
        All,
        @Schema(title = "指定部门")
        Assigned,
        @Schema(title = "仅本部门（不含子部门）")
        MyDept,
        @Schema(title = "本部门及子部门")
        MyDeptAndChildren,
        @Schema(title = "仅本人数据")
        MySelf,
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
    @InjectVar(
            domain = "dao",
            expectBaseType = List.class,
            expectGenericTypes = {String.class},
            converter = PrimitiveArrayJsonConverter.class,
            isRequired = "false")
    protected String assignedOrgIdList;

    @Schema(title = "资源权限列表", description = "Json数组")
    @Lob
    @Basic(fetch = FetchType.LAZY) // 延迟抓取
    @InjectVar(
            domain = "dao",
            expectBaseType = List.class,
            expectGenericTypes = {String.class},
            converter = PrimitiveArrayJsonConverter.class,
            isRequired = "false")
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
