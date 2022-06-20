package ${CLASS_PACKAGE_NAME};

import com.levin.commons.dao.domain.StatefulObject;
import com.levin.commons.dao.domain.support.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import javax.persistence.*;

/**
 * 示例代码
 * <p>
 * Created by simple-dao-code-gen on ${now}.
 */

@Data
@EqualsAndHashCode(of = {"id"})
@Accessors(chain = true)
@FieldNameConstants
@Schema(description = "部门")

@Entity(name = EntityConst.PREFIX + "exam_groups")

@Table(
        indexes = {
                @Index(columnList = AbstractBaseEntityObject.Fields.orderCode),
                @Index(columnList = AbstractBaseEntityObject.Fields.enable),
                @Index(columnList = AbstractBaseEntityObject.Fields.createTime),
                @Index(columnList = AbstractBaseEntityObject.Fields.creator),
                @Index(columnList = AbstractNamedEntityObject.Fields.name),
        }
)

public class Group
        extends AbstractTreeObject<Long, Group> {

    @Id
    @GeneratedValue
    Long id;

    @Schema(description = "父ID")
    Long parentId;

    @Schema(description = "类别")
    String category;

}
