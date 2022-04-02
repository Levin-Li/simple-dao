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
@Schema(description = "任务")

@Entity(name =EntityConst.PREFIX +  "exam_tasks")

@Table(
        indexes = {
                @Index(columnList = AbstractBaseEntityObject.Fields.orderCode),
                @Index(columnList = AbstractBaseEntityObject.Fields.enable),
                @Index(columnList = AbstractBaseEntityObject.Fields.createTime),
                @Index(columnList = AbstractBaseEntityObject.Fields.creator),
                @Index(columnList = AbstractNamedEntityObject.Fields.name),
        }
)

public class Task
        extends AbstractNamedMultiTenantObject {

    @Id
    @GeneratedValue
    Long id;

    @Schema(description = "区域")
    String area;

    @Schema(description = "用户ID")
    @Column(nullable = false)
    Long userId;

    @Schema(description = "用户")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", insertable = false, updatable = false)
    User user;

    @Schema(description = "分数")
    Integer score;

}
