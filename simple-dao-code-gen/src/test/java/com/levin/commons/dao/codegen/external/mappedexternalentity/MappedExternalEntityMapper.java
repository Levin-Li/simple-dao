package com.levin.commons.dao.codegen.external.mappedexternalentity;

import com.levin.commons.dao.codegen.external.MappedExternalEntity;
import com.levin.commons.dao.codegen.external.mappedexternalentity.info.MappedExternalEntityInfo;
import com.levin.commons.dao.support.CycleAvoidingMappingContext;

public interface MappedExternalEntityMapper {

    MappedExternalEntityMapper INSTANCE = new MappedExternalEntityMapper() {
        @Override
        public MappedExternalEntityInfo toInfo(MappedExternalEntity entity, boolean allowLazyLoading,
                                               CycleAvoidingMappingContext cycleContext) {
            return new MappedExternalEntityInfo();
        }

        @Override
        public MappedExternalEntityInfo toInfo(MappedExternalEntityInfo info,
                                               CycleAvoidingMappingContext cycleContext) {
            return new MappedExternalEntityInfo();
        }
    };

    MappedExternalEntityInfo toInfo(MappedExternalEntity entity, boolean allowLazyLoading,
                                    CycleAvoidingMappingContext cycleContext);

    MappedExternalEntityInfo toInfo(MappedExternalEntityInfo info,
                                    CycleAvoidingMappingContext cycleContext);
}
