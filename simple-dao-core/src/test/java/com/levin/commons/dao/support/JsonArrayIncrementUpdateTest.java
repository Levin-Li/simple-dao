package com.levin.commons.dao.support;

import com.levin.commons.dao.MiniDao;
import com.levin.commons.dao.PhysicalNamingStrategy;
import com.levin.commons.dao.PrimitiveValueWrapper;
import com.levin.commons.dao.TargetOption;
import com.levin.commons.dao.annotation.update.Update;
import lombok.Data;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonArrayIncrementUpdateTest {

    @Test
    void chainIncrementSetWithListExpandsParams() {
        UpdateDaoImpl<TestUser> dao = new UpdateDaoImpl<>(stubDao(), false, TestUser.class, "u");

        dao.set(true, true, TestUser::getRoleList, Arrays.asList("R_A", "R_B"))
                .where("u.id = :?", 1L)
                .limit(0, 1);

        String statement = dao.genFinalStatement();
        List<Object> params = unwrapPrimitiveValues(dao.genFinalParamList());

        assertTrue(statement.contains("u.roleList = json_array_append(COALESCE(u.roleList , json_array()) , '$' , :?, :?)"), statement);
        assertTrue(params.contains("R_A"), String.valueOf(params));
        assertTrue(params.contains("R_B"), String.valueOf(params));
    }

    @Test
    void chainIncrementSetWithListKeepsJsonParameterSemantics() {
        UpdateDaoImpl<TestUser> dao = new UpdateDaoImpl<>(stubDao(), false, TestUser.class, "u");

        dao.set(true, true, TestUser::getRoleList, Arrays.asList("R_A", "R_B"))
                .where("u.id = :?", 1L)
                .limit(0, 1);

        List<Object> params = dao.genFinalParamList();

        assertTrue(params.stream().anyMatch(JsonParam.class::isInstance), String.valueOf(params));
    }

    @Test
    void annotationIncrementUpdateWithListExpandsParams() {
        UpdateDaoImpl<TestUser> dao = new UpdateDaoImpl<>(stubDao(), false, TestUser.class, "u");

        dao.appendByQueryObj(new AppendRolesReq().setRoleList(Arrays.asList("R_A", "R_B")))
                .where("u.id = :?", 1L)
                .limit(0, 1);

        String statement = dao.genFinalStatement();
        List<Object> params = unwrapPrimitiveValues(dao.genFinalParamList());

        assertTrue(statement.contains("u.roleList = json_array_append(COALESCE(u.roleList , json_array()) , '$' , :?, :?)"), statement);
        assertTrue(params.contains("R_A"), String.valueOf(params));
        assertTrue(params.contains("R_B"), String.valueOf(params));
    }

    private static List<Object> unwrapPrimitiveValues(List<?> values) {
        return values.stream()
                .map(item -> item instanceof PrimitiveValueWrapper ? ((PrimitiveValueWrapper<?>) item).get() : item)
                .toList();
    }

    private MiniDao stubDao() {
        return (MiniDao) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{MiniDao.class},
                (proxy, method, args) -> {
                    if ("getParamPlaceholder".equals(method.getName())) {
                        return ":?";
                    }
                    if ("getSafeModeMaxLimit".equals(method.getName())) {
                        return 10;
                    }
                    if ("getNamingStrategy".equals(method.getName())) {
                        return PhysicalNamingStrategy.DEFAULT_PHYSICAL_NAMING_STRATEGY;
                    }

                    if (method.getReturnType() == boolean.class) {
                        return false;
                    } else if (method.getReturnType() == int.class) {
                        return 0;
                    }

                    return null;
                });
    }

    static class TestUser {
        private List<String> roleList;

        public List<String> getRoleList() {
            return roleList;
        }
    }

    @Data
    @Accessors(chain = true)
    @TargetOption(entityClass = TestUser.class, alias = "u")
    static class AppendRolesReq {

        @Update(value = "roleList", incrementMode = true)
        private List<String> roleList;
    }
}
