package ${CLASS_PACKAGE_NAME};

import com.levin.commons.dao.domain.StatefulObject;
import com.levin.commons.dao.domain.support.AbstractNamedEntityObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import javax.persistence.*;

/**
 * 示例代码
 * <p>
 * Created by simple-dao-code-gen on ${now}.
 */
@Entity(name =EntityConst.PREFIX +  "exam_users")
@Data
@Accessors(chain = true)
@ToString(exclude = "group")
@FieldNameConstants
@Schema(description = "用户")
public class User
        extends AbstractNamedEntityObject<Long>
        implements StatefulObject<String> {

    @Id
    @GeneratedValue
    private Long id;

    @Schema(description = "状态")
    @Column
    String state;

    @Schema(description = "区域")
    @Column
    String area;

    @Schema(description = "组织")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    Group group;

    @Schema(description = "职业")
    @Column
    String job;

    @Schema(description = "分数")
    Integer score;


}
