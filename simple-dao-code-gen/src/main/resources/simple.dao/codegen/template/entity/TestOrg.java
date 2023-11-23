package ${CLASS_PACKAGE_NAME};

import com.levin.commons.dao.annotation.Contains;
import com.levin.commons.dao.domain.MultiTenantObject;
import com.levin.commons.dao.domain.StatefulObject;
import com.levin.commons.dao.domain.support.*;
import com.levin.commons.service.domain.EnumDesc;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import javax.persistence.*;
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

@Schema(title = "测试机构", description = "示例代码")
@Cacheable
@Entity(name = EntityConst.PREFIX + "TestOrg")
@Table(
        indexes = {
                @Index(columnList = E_TestOrg.orderCode),
                @Index(columnList = E_TestOrg.enable),
                @Index(columnList = E_TestOrg.createTime),
                @Index(columnList = E_TestOrg.creator),

                @Index(columnList = E_TestOrg.name),

                @Index(columnList = E_TestOrg.parentId),
                @Index(columnList = E_TestOrg.code),
                @Index(columnList = E_TestOrg.areaCode),
                @Index(columnList = E_TestOrg.tenantId),
                @Index(columnList = E_TestOrg.type),
                @Index(columnList = E_TestOrg.category),
                @Index(columnList = E_TestOrg.state),
                @Index(columnList = E_TestOrg.level),

                //基于租户的复合索引
                @Index(columnList = E_TestOrg.tenantId +"," + E_TestOrg.id),
                @Index(columnList = E_TestOrg.tenantId +"," + E_TestOrg.parentId),
        }
        ,
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {E_TestOrg.tenantId, E_TestOrg.parentId, E_TestOrg.name}),
                @UniqueConstraint(columnNames = {E_TestOrg.tenantId, E_TestOrg.parentId, E_TestOrg.code}),
        }
)

//@EntityOption(disableActions = EntityOption.Action.Delete,logicalDeleteFieldName = "deleted",logicalDeleteValue = "true")

//@AssociationOverride
//默认情况下，JPA 持续性提供程序自动假设子类继承超类中定义的持久属性及其关联映射。
// 如果继承的列定义对实体不正确（例如，如果继承的列名与已经存在的数据模型不兼容或作为数据库中的列名无效），请使用@AssociationOverride批注自定义从@MappedSuperclass或@Embeddable继承的@OneToOne或@ManyToOne映射，以更改与字段或属性关联的@JoinColumn。
// 如果有多个要进行的@AssociationOverride更改，则必须使用@AssociationOverrides。
//要自定义基本映射以更改它的@Column，请使用@AttributeOverride。
//

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

//@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class TestOrg
        extends AbstractTreeObject<String, TestOrg>
        implements MultiTenantObject, StatefulObject {

    @Schema(title = "机构状态")
    public enum State implements EnumDesc {
        @Schema(title = "正常")
        Normal,
        @Schema(title = "冻结")
        Freeze,
        @Schema(title = "注销")
        Cancellation,
    }

    @Schema(title = "机构类型")
    public enum OrgType implements EnumDesc {
        @Schema(title = "公司/独立法人")
        LegalPerson,
        @Schema(title = "分公司/分支机构")
        Branch,
        @Schema(title = "部门")
        Department,
        @Schema(title = "小组")
        Group,
        @Schema(title = "其它")
        Other,
    }

    @Id
//    @GeneratedValue
    @GeneratedValue(generator = "default_id")
    @Column(length = 64)
    protected String id;

    @Schema(title = "父ID")
    @Column(length = 64)
    protected String parentId;

    @Schema(title = "租户ID")
    @Column(length = 64)
    protected String tenantId;

    @Schema(title = "编码", description = "对于公司是统一信用码")
    @Column(length = 64)
    @Contains
    protected String code;

    @Transient
    @Schema(title = "未使用")
    protected String unuseField;

    @Schema(title = "图标")
    protected String icon;

    @Schema(title = "状态")
    @Column(nullable = false, length = 32)
    @Enumerated(EnumType.STRING)
    protected State state;

    @Schema(title = "类型")
    @Column(nullable = false, length = 64)
    @Type(type = "EnumDesc")
    protected OrgType type;

    @Schema(title = "所属行业")
    @Column(length = 64)
    protected String industries;

    @Schema(title = "区域编码")
    @Column(nullable = false, length = 64)
    @Contains
    protected String areaCode;

//    @Schema(title = "所属区域")
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "areaCode", insertable = false, updatable = false)
//    protected Area area;

    @Schema(title = "机构级别", description = "使用字典值配置")
    @Column(length = 128)
    protected String level;

    @Column(nullable = false, length = 128)
    @Schema(title = "机构类别", description = "使用字典值配置")
    protected String category;

    @Column(nullable = false)
    @Schema(title = "是否外部机构")
    protected Boolean isExternal;

    @Lob
    @Basic(fetch = FetchType.LAZY) //默认延迟加载
    @Schema(title = "机构扩展信息")
    protected String extInfo;
    //////////////////////////////////////////////////////////////////////

    @Schema(title = "联系人")
    @Column(length = 64)
    @Contains
    protected String contacts;

    @Schema(title = "联系电话")
    @Column(length = 20)
    @Contains
    protected String phones;

    @Schema(title = "联系邮箱")
    @Column(length = 32)
    protected String emails;

    @Schema(title = "联系地址")
    @Contains
    protected String address;

    @Schema(title = "邮政编码")
    @Column(length = 32)
    protected String zipCode;

    @Override
    @PrePersist
    public void prePersist() {

        super.prePersist();

        if (state == null) {
            state = State.Normal;
        }
    }

}
