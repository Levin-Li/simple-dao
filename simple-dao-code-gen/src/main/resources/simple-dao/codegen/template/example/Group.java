package ${CLASS_PACKAGE_NAME};

import com.levin.commons.dao.domain.StatefulObject;
import com.levin.commons.dao.domain.support.AbstractTreeObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * 示例代码
 * <p>
 * Created by simple-dao-code-gen on ${now}.
 */
@Entity(name = EntityOption.PREFIX + "exam_groups")
@Data
@Accessors(chain = true)
@FieldNameConstants
@Schema(description = "部门")
public class Group
        extends AbstractTreeObject<Long, Group>
        implements StatefulObject<String> {

    @Id
    @GeneratedValue
    private Long id;

    @Schema(description = "状态")
    String state;

    @Schema(description = "类别")
    String category;

}
