package ${packageName};

import static ${modulePackageName}.ModuleOption.*;

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

import ${entityClassPackage}.*;
import ${packageName}.req.*;
import ${packageName}.info.*;

import ${modulePackageName}.*;
import ${modulePackageName}.entities.*;

import ${entityClassPackage}.${entityName}.*;

import static ${modulePackageName}.entities.EntityConst.*;


/**
 * ${entityTitle}-Mapper接口
 *
 * @author Auto gen by simple-dao-codegen, @time: ${.now}, 代码生成哈希校验码：[]，请不要修改和删除此行内容。
 *
 */
@Mapper
public interface ${entityName}Mapper {

    ${entityName}Mapper INSTANCE = Mappers.getMapper(${entityName}Mapper.class);

    ${entityName}Info toInfo(${entityName} entity);

    ${entityName}Info toInfo(${entityName}Info info);
    
    ${entityName}Info toInfo(Create${entityName}Req req);
    
    ${entityName}Info toInfo(Update${entityName}Req req);

    ${entityName} toEntity(Create${entityName}Req req);

    ${entityName} toEntity(Update${entityName}Req req);

    Create${entityName}Req toCreateReq(${entityName} entity);
    
    Create${entityName}Req toCreateReq(${entityName}Info info);
    
    Create${entityName}Req toCreateReq(Update${entityName}Req req);
    
    Update${entityName}Req toUpdateReq(${entityName}Info info);
    
    Update${entityName}Req toUpdateReq(Create${entityName}Req req);
    
    ${entityName}IdReq toIdReq(Update${entityName}Req req);
    
    ${entityName}IdReq toIdReq(Query${entityName}Req req);

}
