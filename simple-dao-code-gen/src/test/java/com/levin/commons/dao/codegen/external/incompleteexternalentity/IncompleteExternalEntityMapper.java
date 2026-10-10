package com.levin.commons.dao.codegen.external.incompleteexternalentity;

import com.levin.commons.dao.codegen.external.IncompleteExternalEntity;
import com.levin.commons.dao.codegen.external.incompleteexternalentity.info.IncompleteExternalEntityInfo;
import com.levin.commons.dao.support.CycleAvoidingMappingContext;

public interface IncompleteExternalEntityMapper {

    IncompleteExternalEntityMapper INSTANCE = (entity, allowLazyLoading, cycleContext) ->
            new IncompleteExternalEntityInfo();

    IncompleteExternalEntityInfo toInfo(IncompleteExternalEntity entity, boolean allowLazyLoading,
                                        CycleAvoidingMappingContext cycleContext);
}
