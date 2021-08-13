package ${CLASS_PACKAGE_NAME};

import com.levin.commons.dao.domain.StatefulObject;
import com.levin.commons.dao.domain.support.AbstractNamedEntityObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import javax.persistence.*;

/**
 * 示例代码
 * <p>
 * Created by simple-dao-code-gen on ${now}.
 */
@Entity(name =TableOption.PREFIX +  "exam_tasks")
@Data
@Accessors(chain = true)
@FieldNameConstants
@Schema(description = "任务")
public class Task
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

    @Schema(description = "用户")
    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;

    @Schema(description = "分数")
    Integer score;

}
