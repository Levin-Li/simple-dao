package ${CLASS_PACKAGE_NAME};

import com.levin.commons.dao.annotation.Contains;
import com.levin.commons.dao.domain.*;
import com.levin.commons.dao.domain.support.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import com.levin.commons.service.domain.InjectVar;
import com.levin.commons.service.support.*;

import javax.persistence.*;

/**
 * 示例代码
 * <p>
 * Created by simple-dao-code-gen on ${now}.
 */

@Data
@EqualsAndHashCode(of = {"id"})
@Accessors(chain = true)
@ToString(exclude = "group")
@FieldNameConstants

@Schema(description = "用户")

@Entity(name =EntityConst.PREFIX +  "exam_users")

@Table(
        indexes = {
                @Index(columnList = AbstractBaseEntityObject.Fields.orderCode),
                @Index(columnList = AbstractBaseEntityObject.Fields.enable),
                @Index(columnList = AbstractBaseEntityObject.Fields.createTime),
                @Index(columnList = AbstractBaseEntityObject.Fields.creator),
                @Index(columnList = AbstractNamedEntityObject.Fields.name),
        }
)
public class User
        extends AbstractNamedEntityObject {

    @Id
    @GeneratedValue
    Long id;

    @Schema(description = "区域")
    String area;

    @Schema(description = "组织ID")
    Long groupId;

    @Schema(description = "组织")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "groupId" , insertable = false, updatable = false)
    Group group;

    @Schema(description = "归属的虚拟组织")
    @Contains
    @InjectVar(expectTypeDesc = "List<String>", converter = PrimitiveArrayJsonConverter.class)
    String belongOrgs;

    @Schema(description = "职业")
    String job;

    @Schema(description = "分数")
    Integer score;

}
