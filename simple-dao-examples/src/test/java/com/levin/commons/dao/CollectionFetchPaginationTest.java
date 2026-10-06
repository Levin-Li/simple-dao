package com.levin.commons.dao;

import com.levin.commons.dao.annotation.misc.Fetch;
import com.levin.commons.dao.domain.Group;
import com.levin.commons.dao.support.SimplePaging;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.Hibernate;
import org.hibernate.SessionFactory;
import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/** 真实 Hibernate 查询：集合抓取必须在数据库分页后执行。 */
@SpringBootTest(classes = TestConfiguration.class, properties = {
        "spring.datasource.url=jdbc:h2:mem:collection_fetch_pagination;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa", "spring.datasource.password=",
        "spring.datasource.druid.initial-size=1", "spring.datasource.druid.min-idle=1",
        "spring.jpa.database=H2", "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.query.fail_on_pagination_over_collection_fetch=true",
        "spring.jpa.properties.hibernate.generate_statistics=true",
        "spring.jpa.properties.hibernate.session_factory.statement_inspector=com.levin.commons.dao.CollectionFetchPaginationTest$SqlRecorder",
        "com.levin.commons.service.support.DefaultSpringMvcEnumFormatterConfiguration.enabled=false",
        "com.levin.commons.service.support.DefaultSpringMvcJsonDeserializerConfiguration.enabled=false"
})
@Transactional
class CollectionFetchPaginationTest {
    @Autowired SimpleDao dao;
    @Autowired EntityManager em;
    @Autowired EntityManagerFactory emf;
    @Autowired PlatformTransactionManager transactionManager;
    private final List<Long> seededIds = new ArrayList<>();
    private Group first;
    private Group empty;
    private Group last;

    public static class SqlRecorder implements StatementInspector {
        static final List<String> statements = new CopyOnWriteArrayList<>();
        @Override public String inspect(String sql) {
            if (sql.stripLeading().toLowerCase().startsWith("select")) statements.add(sql.toLowerCase());
            return sql;
        }
    }

    @BeforeEach
    void seed() {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            first = persist("first", "visible", 10, null);
            empty = persist("empty", "visible", 20, null);
            last = persist("last", "visible", 30, null);
            Group foreign = persist("foreign", "hidden", 40, null);
            for (int i = 0; i < 4; i++) persist("first-child-" + i, "children", i, first);
            for (int i = 0; i < 3; i++) persist("last-child-" + i, "children", i, last);
            persist("foreign-child", "children", 0, foreign);
            em.flush();
            em.clear();
        });
        SqlRecorder.statements.clear();
    }

    private Group persist(String name, String category, int score, Group parent) {
        Group group = new Group(name);
        group.setCategory(category).setScore(score);
        if (parent != null) group.setParentId(parent.getId());
        em.persist(group);
        seededIds.add(group.<Long>getId());
        return group;
    }

    @Test
    void collectionFetchMustPageRootIdsThenFetchCompleteCollectionsInOriginalOrder() {
        Group notFetched = em.find(Group.class, first.getId());
        assertFalse(Hibernate.isInitialized(notFetched.getChildren()));
        em.clear();
        SqlRecorder.statements.clear();
        String jpql = "select g from jpa_dao_test_Group g left join fetch g.children c "
                + "where g.category = :scope order by g.score desc";
        List<Group> rows = dao.find(false, Group.class, 1, 2, jpql, java.util.Map.of("scope", "visible"));
        assertEquals(List.of(empty.getId(), first.getId()), ids(rows));
        assertTrue(Hibernate.isInitialized(rows.get(0).getChildren()));
        assertTrue(rows.get(0).getChildren().isEmpty());
        assertTrue(Hibernate.isInitialized(rows.get(1).getChildren()));
        assertEquals(4, rows.get(1).getChildren().size());
        assertDatabasePageThenFetch();
        em.clear();
        SqlRecorder.statements.clear();
        assertEquals(ids(rows), ids(dao.find(false, Group.class, 1, 2, jpql, java.util.Map.of("scope", "visible"))));
        assertDatabasePageThenFetch();
    }

    @Test
    void builderPagingMustPreserveTotalsScopeAndQueryDefinition() {
        SelectDao<Group> query = dao.selectFrom(Group.class, "g").eq("g.category", "visible")
                .joinFetch(true, Fetch.JoinType.Left, "g.children").orderByDesc("g.score");
        assertEquals(3L, query.count());
        assertTrue(SqlRecorder.statements.stream().anyMatch(sql -> sql.matches("(?s).*count\\([^)]*\\.id\\).*")));
        PagingData<Group> page = query.findPaging(Group.class, new SimplePaging()
                .setRequireTotals(true).setPageSize(2).setPageIndex(1));
        assertEquals(3L, page.getTotals());
        assertEquals(List.of(last.getId(), empty.getId()), ids(page.getItems()));
        assertEquals(3, page.getItems().get(0).getChildren().size());
        assertTrue(page.getItems().get(1).getChildren().isEmpty());
        em.clear();
        assertEquals(ids(page.getItems()), ids(query.find(Group.class)));
        assertEquals(3L, query.count());
    }

    @Test
    void emptyIdPageMustNotExecuteFetchQuery() {
        List<Group> rows = dao.find(false, Group.class, 20, 2,
                "select g from jpa_dao_test_Group g left join fetch g.children where g.category = :scope order by g.score",
                java.util.Map.of("scope", "visible"));
        assertTrue(rows.isEmpty());
        assertEquals(1, SqlRecorder.statements.size());
        assertTrue(hasLimit(SqlRecorder.statements.get(0)));
    }

    @Test
    void toOneFetchAndOrdinaryCollectionJoinMustKeepSingleSqlPagination() {
        List<Group> children = dao.find(false, Group.class, 0, 2,
                "select g from jpa_dao_test_Group g join fetch g.parent where g.category = :scope order by g.name",
                java.util.Map.of("scope", "children"));
        assertEquals(2, children.size());
        assertTrue(children.stream().allMatch(g -> Hibernate.isInitialized(g.getParent())));
        assertEquals(1, SqlRecorder.statements.size());
        assertTrue(hasLimit(SqlRecorder.statements.get(0)));
        em.clear();
        SqlRecorder.statements.clear();
        dao.find(false, Group.class, 0, 2,
                "select g from jpa_dao_test_Group g join g.children c where g.category = :scope order by g.score",
                java.util.Map.of("scope", "visible"));
        assertEquals(1, SqlRecorder.statements.size());
        assertTrue(hasLimit(SqlRecorder.statements.get(0)));
    }

    @Test
    void fetchKeywordMustAcceptHibernateWhitespaceAndCase() {
        for (String join : List.of("LeFt JoIn   FeTcH", "left join\nfetch", "left join\tfetch", "\tleft\tjoin\tfetch", "left join\r\nfetch")) {
            em.clear();
            SqlRecorder.statements.clear();
            List<Group> rows = dao.find(false, Group.class, 1, 2,
                    "select g from jpa_dao_test_Group g " + join
                            + " g.children where g.category = :scope order by g.score desc",
                    java.util.Map.of("scope", "visible"));
            assertEquals(List.of(empty.getId(), first.getId()), ids(rows));
            assertEquals(4, rows.get(1).getChildren().size());
            assertDatabasePageThenFetch();
        }
    }

    @Test
    void fetchWordInStringLiteralMustKeepOrdinarySingleQuery() {
        List<Group> rows = dao.find(false, Group.class, 0, 2,
                "select g from jpa_dao_test_Group g where g.category = :scope "
                        + "and g.name <> 'join fetch g.children' order by g.score desc",
                java.util.Map.of("scope", "visible"));
        assertEquals(List.of(last.getId(), empty.getId()), ids(rows));
        assertEquals(1, SqlRecorder.statements.size());
        assertTrue(hasLimit(SqlRecorder.statements.get(0)));
        assertFalse(Hibernate.isInitialized(rows.get(0).getChildren()));
    }

    @Test
    void nestedFetchAliasMustRemainResolvableWhileOnlyRootsArePaged() {
        List<Group> rows = dao.find(false, Group.class, 0, 2,
                "select g from jpa_dao_test_Group g left join fetch g.children c "
                        + "left join fetch c.parent p where g.category = :scope order by g.score desc",
                java.util.Map.of("scope", "visible"));
        assertEquals(List.of(last.getId(), empty.getId()), ids(rows));
        assertTrue(Hibernate.isInitialized(rows.get(1).getChildren()));
        assertTrue(rows.get(1).getChildren().isEmpty());
        Group root = rows.get(0);
        assertEquals(3, root.getChildren().size());
        assertTrue(root.getChildren().stream().allMatch(child -> Hibernate.isInitialized(child.getParent())));
        assertTrue(root.getChildren().stream().allMatch(child -> child.getParent() == root));
        assertEquals(2, SqlRecorder.statements.size());
        assertTrue(hasLimit(SqlRecorder.statements.get(0)));
        assertFalse(SqlRecorder.statements.get(0).contains(" join "), SqlRecorder.statements.get(0));
        assertFalse(hasLimit(SqlRecorder.statements.get(1)));
        assertTrue(SqlRecorder.statements.get(1).contains(" in ("));
    }

    @Test
    void predicatesAndParameterizedSortMustAffectOnlyIdPage() {
        List<Group> rows = dao.find(false, Group.class, 0, 2,
                "select g from jpa_dao_test_Group g left join fetch g.children "
                        + "where (g.category = :scope or g.name = :emptyName) "
                        + "and exists (select s.id from jpa_dao_test_Group s where s.id = g.id and s.category <> :hidden) "
                        + "order by case when g.name = :preferred then 0 else 1 end, g.score desc",
                java.util.Map.of("scope", "visible", "emptyName", "empty", "hidden", "hidden", "preferred", "first"));
        assertEquals(List.of(first.getId(), last.getId()), ids(rows));
        assertEquals(4, rows.get(0).getChildren().size());
        assertEquals(3, rows.get(1).getChildren().size());
        assertEquals(2, SqlRecorder.statements.size());
        String idPage = SqlRecorder.statements.get(0);
        assertTrue(hasLimit(idPage));
        assertTrue(idPage.contains("exists"));
        assertTrue(idPage.contains("case"));
        String fetch = SqlRecorder.statements.get(1);
        assertTrue(fetch.contains(" in ("));
        assertFalse(hasLimit(fetch));
        assertFalse(fetch.contains("exists"));
        assertFalse(fetch.contains("case"));
        assertFalse(fetch.contains("category=?"));
    }

    @Test
    void innerCollectionFetchMustRetainJoinRowPaginationWithoutDistinct() {
        List<Group> rows = dao.find(false, Group.class, 0, 2,
                "select g from jpa_dao_test_Group g join fetch g.children c "
                        + "where g.category = :scope order by g.score asc",
                java.util.Map.of("scope", "visible"));
        assertEquals(List.of(first.getId(), first.getId()), ids(rows));
        assertSame(rows.get(0), rows.get(1));
        assertEquals(4, rows.get(0).getChildren().size());
        assertEquals(2, SqlRecorder.statements.size());
        assertTrue(SqlRecorder.statements.get(0).contains(" join "));
        assertTrue(hasLimit(SqlRecorder.statements.get(0)));
        assertFalse(SqlRecorder.statements.get(0).contains(" distinct "));
        assertFalse(hasLimit(SqlRecorder.statements.get(1)));
    }

    @Test
    void fetchedAliasUsedByPredicateMustKeepOriginalIdFilterButLoadFullCollection() {
        List<Group> rows = dao.find(false, Group.class, 0, 2,
                "select g from jpa_dao_test_Group g left join fetch g.children c "
                        + "where g.category = :scope and c.score = :score order by g.score desc",
                java.util.Map.of("scope", "visible", "score", 0));
        assertEquals(List.of(last.getId(), first.getId()), ids(rows));
        assertEquals(3, rows.get(0).getChildren().size());
        assertEquals(4, rows.get(1).getChildren().size());
        assertTrue(SqlRecorder.statements.get(0).contains(" join "));
        assertTrue(hasLimit(SqlRecorder.statements.get(0)));
        assertFalse(hasLimit(SqlRecorder.statements.get(1)));
    }

    @Test
    void largeIdPageMustFetchInBatchesWithoutChangingOrderOrPageSize() {
        for (int i = 0; i < 501; i++) {
            persist("batch-" + i, "batch", i, null);
        }
        em.flush();
        em.clear();
        SqlRecorder.statements.clear();
        List<Group> rows = dao.find(false, Group.class, 0, 501,
                "select g from jpa_dao_test_Group g left join fetch g.children "
                        + "where g.category = :scope order by g.score desc", java.util.Map.of("scope", "batch"));
        assertEquals(501, rows.size());
        assertEquals(500, rows.get(0).getScore());
        assertEquals(0, rows.get(500).getScore());
        assertTrue(rows.stream().allMatch(g -> Hibernate.isInitialized(g.getChildren()) && g.getChildren().isEmpty()));
        assertEquals(3, SqlRecorder.statements.size());
        assertTrue(hasLimit(SqlRecorder.statements.get(0)));
        assertFalse(hasLimit(SqlRecorder.statements.get(1)));
        assertFalse(hasLimit(SqlRecorder.statements.get(2)));
    }

    @Test
    void subqueryAliasMustNotKeepAnUnrelatedOuterFetchJoin() {
        List<Group> rows = dao.find(false, Group.class, 0, 2,
                "select g from jpa_dao_test_Group g left join fetch g.children c "
                        + "where g.category = :scope and exists "
                        + "(select c.id from jpa_dao_test_Group c where c.id = g.id) order by g.score desc",
                java.util.Map.of("scope", "visible"));
        assertEquals(List.of(last.getId(), empty.getId()), ids(rows));
        assertDatabasePageThenFetch();
    }

    @Test
    void rawJoinFetchBuilderMustUseTheSameIdCountPlan() {
        SelectDao<Group> query = dao.selectFrom(Group.class, "g").eq("g.category", "visible")
                .join("left join   fetch g.children").orderByDesc("g.score");
        PagingData<Group> page = query.findPaging(Group.class,
                new SimplePaging().setRequireTotals(true).setPageIndex(1).setPageSize(2));
        assertEquals(3L, page.getTotals());
        assertEquals(List.of(last.getId(), empty.getId()), ids(page.getItems()));
    }

    @Test
    void queryObjectFetchAnnotationMustUseDatabasePagination() {
        SelectDao<Group> query = dao.selectFrom(Group.class, "g")
                .appendByQueryObj(new FetchRequest()).orderByDesc("g.score");
        PagingData<Group> page = query.findPaging(Group.class,
                new SimplePaging().setRequireTotals(true).setPageSize(2).setPageIndex(1));
        assertEquals(3L, page.getTotals());
        assertEquals(List.of(last.getId(), empty.getId()), ids(page.getItems()));
        assertEquals(3, page.getItems().get(0).getChildren().size());
        assertTrue(page.getItems().get(1).getChildren().isEmpty());
    }

    static class FetchRequest {
        @com.levin.commons.dao.annotation.Eq
        String category = "visible";
        @Fetch(value = "children", isBindToField = false)
        Boolean withChildren = true;
    }

    @Test
    void uniqueResultAndDefaultConverterMustReceiveCompleteEntity() {
        java.util.concurrent.atomic.AtomicInteger calls = new java.util.concurrent.atomic.AtomicInteger();
        SelectDao<Group> query = dao.selectFrom(Group.class, "g").eq("g.id", first.getId())
                .joinFetch(true, Fetch.JoinType.Left, "g.children")
                .setDefaultResultConverter(Integer.class, group -> {
                    calls.incrementAndGet();
                    return group.getChildren().size();
                });
        assertEquals(4, query.findUnique(Integer.class));
        assertEquals(List.of(4), query.find(Integer.class));
        assertEquals(2, calls.get());
    }

    @Test
    void implicitEntitySelectionMustPreserveDeepToOneFetchOwnership() {
        List<Group> rows = dao.find(false, Group.class, 0, 2,
                "from jpa_dao_test_Group g left join fetch g.parent "
                        + "left join fetch g.parent.parent p left join fetch g.children c "
                        + "where g.category = :scope order by g.score desc", java.util.Map.of("scope", "visible"));
        assertEquals(List.of(last.getId(), empty.getId()), ids(rows));
        assertEquals(3, rows.get(0).getChildren().size());
        assertTrue(rows.get(1).getChildren().isEmpty());
    }

    @Test
    void implicitTypedRootWithOrdinaryJoinMustKeepEntityResultShape() {
        List<Group> rows = dao.find(false, Group.class, 0, 2,
                "from jpa_dao_test_Group g left join g.parent p left join fetch g.children "
                        + "where g.category = :scope order by g.score desc", java.util.Map.of("scope", "visible"));
        assertEquals(List.of(last.getId(), empty.getId()), ids(rows));
        assertEquals(3, rows.get(0).getChildren().size());
    }

    @Test
    void derivedQueriesMustPreserveListParametersAndReservedNameCollisions() {
        List<Group> rows = dao.find(false, Group.class, 0, 2,
                "select g from jpa_dao_test_Group g left join fetch g.children "
                        + "where g.category in :__simpleDaoPageIds order by g.score desc",
                java.util.Map.of("__simpleDaoPageIds", List.of("visible")));
        assertEquals(List.of(last.getId(), empty.getId()), ids(rows));
        assertEquals(3, rows.get(0).getChildren().size());
    }

    @Test
    void unsupportedProjectionDistinctAndGroupingMustFailBeforeDatabaseQuery() {
        for (String jpql : List.of(
                "select g.name from jpa_dao_test_Group g left join fetch g.children",
                "select distinct g from jpa_dao_test_Group g left join fetch g.children",
                "select g as picked from jpa_dao_test_Group g left join fetch g.children order by picked",
                "select g from jpa_dao_test_Group g left join fetch g.children group by g")) {
            SqlRecorder.statements.clear();
            RuntimeException failure = assertThrows(RuntimeException.class,
                    () -> dao.find(false, null, 0, 2, jpql));
            assertFalse(failure.getMessage().contains("in-memory pagination"), failure.toString());
            assertTrue(SqlRecorder.statements.isEmpty(), SqlRecorder.statements.toString());
        }
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void collectionFetchOutsideTransactionMustReuseAndCloseItsActualSession() {
        var statistics = emf.unwrap(SessionFactory.class).getStatistics();
        long openedBefore = statistics.getSessionOpenCount();
        long closedBefore = statistics.getSessionCloseCount();
        try {
            List<Group> rows = dao.find(false, Group.class, 1, 2,
                    "select g from jpa_dao_test_Group g left join fetch g.children "
                            + "where g.category = :scope order by g.score desc",
                    java.util.Map.of("scope", "visible"));
            assertEquals(List.of(empty.getId(), first.getId()), ids(rows));
            assertTrue(Hibernate.isInitialized(rows.get(0).getChildren()));
            assertTrue(rows.get(0).getChildren().isEmpty());
            assertTrue(Hibernate.isInitialized(rows.get(1).getChildren()));
            assertEquals(4, rows.get(1).getChildren().size());
            assertDatabasePageThenFetch();
            long opened = statistics.getSessionOpenCount() - openedBefore;
            long closed = statistics.getSessionCloseCount() - closedBefore;
            assertTrue(opened > 0, "事务外查询应创建实际 Session");
            assertEquals(opened, closed, "事务外查询结束后必须关闭所有创建的 Session");
        } finally {
            // 该用例没有测试事务回滚；仅删除本用例创建的 ID，先子节点后根节点。
            new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
                em.createQuery("delete from jpa_dao_test_Group g where g.id in :ids")
                        .setParameter("ids", seededIds.subList(4, seededIds.size())).executeUpdate();
                em.createQuery("delete from jpa_dao_test_Group g where g.id in :ids")
                        .setParameter("ids", seededIds.subList(0, 4)).executeUpdate();
            });
        }
    }

    private static List<Long> ids(List<Group> rows) {
        return rows.stream().map(g -> g.<Long>getId()).collect(Collectors.toList());
    }

    private static boolean hasLimit(String sql) {
        return sql.contains("fetch first") || sql.contains("fetch next") || sql.contains(" limit ");
    }

    private static void assertDatabasePageThenFetch() {
        assertEquals(2, SqlRecorder.statements.size(), SqlRecorder.statements.toString());
        String idPage = SqlRecorder.statements.get(0);
        assertTrue(hasLimit(idPage), idPage);
        assertFalse(idPage.contains(" distinct "), idPage);
        assertFalse(idPage.contains(" join "), idPage);
        String fetch = SqlRecorder.statements.get(1);
        assertTrue(fetch.contains(" join "), fetch);
        assertTrue(fetch.contains(" in ("), fetch);
        assertFalse(hasLimit(fetch), fetch);
        assertFalse(fetch.contains("category=?"), "第二阶段只保留本页 ID 过滤：" + fetch);
    }
}
