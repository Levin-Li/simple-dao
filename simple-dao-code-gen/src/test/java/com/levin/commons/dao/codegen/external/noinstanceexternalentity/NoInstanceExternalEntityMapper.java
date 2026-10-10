package com.levin.commons.dao.codegen.external.noinstanceexternalentity;

import com.levin.commons.dao.codegen.external.NoInstanceExternalEntity;
import com.levin.commons.dao.codegen.external.noinstanceexternalentity.info.NoInstanceExternalEntityInfo;
import com.levin.commons.dao.support.CycleAvoidingMappingContext;

public interface NoInstanceExternalEntityMapper {

    NoInstanceExternalEntityInfo toInfo(NoInstanceExternalEntity entity, boolean allowLazyLoading,
                                        CycleAvoidingMappingContext cycleContext);

    NoInstanceExternalEntityInfo toInfo(NoInstanceExternalEntityInfo info,
                                        CycleAvoidingMappingContext cycleContext);
}
