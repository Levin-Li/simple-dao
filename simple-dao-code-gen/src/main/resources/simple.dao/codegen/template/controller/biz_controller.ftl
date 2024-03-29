package ${packageName};

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.validation.annotation.*;
import org.springframework.util.*;
import javax.validation.*;
import java.util.*;
import javax.annotation.*;

import javax.servlet.http.*;

import org.apache.dubbo.config.annotation.*;

import com.levin.commons.dao.*;
import com.levin.commons.service.domain.*;
import com.levin.commons.dao.support.*;
import com.levin.commons.ui.annotation.*;
import javax.validation.constraints.*;

import ${modulePackageName}.controller.*;
import ${controllerPackageName}.*;

import ${modulePackageName}.*;
import ${entityClassPackage}.*;

import ${bizServicePackageName}.*;

import ${servicePackageName}.*;
import ${servicePackageName}.req.*;
import ${servicePackageName}.info.*;

import static ${modulePackageName}.ModuleOption.*;
import static ${modulePackageName}.entities.EntityConst.*;


// POST: 创建一个新的资源，如用户资源，部门资源
// PATCH: 修改资源的某个属性
// PUT: 更新资源当中包含的全部属性
// DELETE: 删除某项资源
// GET: 获取某个资源的详情

// 在数学计算或者计算机科学中，幂等性（idempotence）是指相同操作或资源在一次或多次请求中具有同样效果的作用。幂等性是在分布式系统设计中具有十分重要的地位。
// http协议明确规定，put、get、delete请求都是具有幂等性的，而post为非幂等性的。
// 所以一般插入新数据的时候使用post方法，更新数据库时用put方法

// Spring mvc 参数验证说明
// @Valid 只能用在controller
// @Validated 可以用在其他被spring管理的类上
// 注意 只有 @Valid 才支持对象嵌套验证，示例如下：
// @Valid
// @NotNull(groups = AdvanceInfo.class)
// private UserAddress useraddress;

//生成的控制器
@RestController(PLUGIN_PREFIX + "${className}")
@RequestMapping(API_PATH + "${entityName}") //${entityName?lower_case}

@Slf4j
@ConditionalOnProperty(prefix = PLUGIN_PREFIX, name = "${className}", matchIfMissing = true)

//默认需要权限访问
//@ResAuthorize(domain = ID, type = ENTITY_TYPE_NAME)

//类注解，@Tag的name属性关联权限的资源标识
@Tag(name = E_${entityName}.BIZ_NAME, description = E_${entityName}.BIZ_NAME + MAINTAIN_ACTION)
@Validated //@Valid
@CRUD
/**
 * ${entityTitle}业务控制器
 *
 * @author Auto gen by simple-dao-codegen, @time: ${.now}, 代码生成哈希校验码：[]，请不要修改和删除此行内容。
 *
 */
public class ${className} extends ${entityName}Controller{

}
