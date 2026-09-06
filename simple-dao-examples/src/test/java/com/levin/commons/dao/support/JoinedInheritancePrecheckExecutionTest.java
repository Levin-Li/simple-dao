package com.levin.commons.dao.support;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JoinedInheritancePrecheckExecutionTest {

    @Test
    void precheckAndUnlimitedJoinedUpdateShouldExecute() {
        StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .applySetting(AvailableSettings.URL, "jdbc:h2:mem:joined_precheck;DB_CLOSE_DELAY=-1")
                .applySetting(AvailableSettings.USER, "sa")
                .applySetting(AvailableSettings.PASS, "")
                .applySetting(AvailableSettings.HBM2DDL_AUTO, "create-drop")
                .build();

        try (SessionFactory sessionFactory = new MetadataSources(registry)
                .addAnnotatedClass(JoinedRoot.class)
                .addAnnotatedClass(JoinedChild.class)
                .buildMetadata()
                .buildSessionFactory();
             Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.persist(new JoinedChild(1L, "before", "child"));
            session.getTransaction().commit();

            session.beginTransaction();
            int matched = session.createSelectionQuery(
                            "Select 1 From JoinedRoot e Where e.id = :id", Integer.class)
                    .setParameter("id", 1L)
                    .setMaxResults(2)
                    .getResultList()
                    .size();
            assertEquals(1, matched);

            int updated = session.createMutationQuery(
                            "Update JoinedRoot e Set e.value = :value Where e.id = :id")
                    .setParameter("value", "after")
                    .setParameter("id", 1L)
                    .executeUpdate();
            session.getTransaction().commit();
            assertEquals(1, updated);
        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }

    @Test
    void precheckAndUnlimitedJoinedDeleteShouldExecute() {
        StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .applySetting(AvailableSettings.URL, "jdbc:h2:mem:joined_precheck_delete;DB_CLOSE_DELAY=-1")
                .applySetting(AvailableSettings.USER, "sa")
                .applySetting(AvailableSettings.PASS, "")
                .applySetting(AvailableSettings.HBM2DDL_AUTO, "create-drop")
                .build();

        try (SessionFactory sessionFactory = new MetadataSources(registry)
                .addAnnotatedClass(JoinedRoot.class)
                .addAnnotatedClass(JoinedChild.class)
                .buildMetadata()
                .buildSessionFactory();
             Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.persist(new JoinedChild(1L, "before", "child"));
            session.getTransaction().commit();

            session.beginTransaction();
            int matched = session.createSelectionQuery(
                            "Select 1 From JoinedRoot e Where e.id = :id", Integer.class)
                    .setParameter("id", 1L)
                    .setMaxResults(2)
                    .getResultList()
                    .size();
            assertEquals(1, matched);

            int deleted = session.createMutationQuery("Delete From JoinedRoot e Where e.id = :id")
                    .setParameter("id", 1L)
                    .executeUpdate();
            session.getTransaction().commit();
            assertEquals(1, deleted);
        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }

    @Entity(name = "JoinedRoot")
    @Inheritance(strategy = InheritanceType.JOINED)
    static class JoinedRoot {
        @Id
        Long id;

        @Column(name = "mutation_value")
        String value;

        JoinedRoot() {
        }

        JoinedRoot(Long id, String value) {
            this.id = id;
            this.value = value;
        }
    }

    @Entity(name = "JoinedChild")
    static class JoinedChild extends JoinedRoot {
        String childValue;

        JoinedChild() {
        }

        JoinedChild(Long id, String value, String childValue) {
            super(id, value);
            this.childValue = childValue;
        }
    }
}
