package ${packageName};



import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.*;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.*;
import java.util.stream.*;
import java.util.function.*;
import jakarta.validation.*;
import jakarta.validation.constraints.*;

import com.levin.commons.dao.support.*;
import com.levin.commons.service.domain.*;
import com.levin.commons.dao.*;

import static ${modulePackageName}.ModuleOption.*;

import ${entityClassPackage}.*;

import ${modulePackageName}.*;
import ${modulePackageName}.entities.*;

import ${entityClassPackage}.${entityName}.*;

import static ${modulePackageName}.entities.EntityConst.*;

/**
 * ${entityTitle}-BizMapper接口
 *
 * @author Auto gen by simple-dao-codegen, @time: ${.now}, 代码生成哈希校验码：[]，请不要修改和删除此行内容。
 *
 */
@Mapper
public interface Biz${entityName}Mapper {

    Biz${entityName}Mapper INSTANCE = Mappers.getMapper(Biz${entityName}Mapper.class);


}
