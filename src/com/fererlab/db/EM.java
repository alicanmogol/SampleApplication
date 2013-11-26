package com.fererlab.db;

import com.fererlab.dto.Model;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * acm
 */
public class EM {

    private static EntityManager entityManager = null;
    private static EntityManagerFactory entityManagerFactory;

    public static void start() {
        if (entityManager == null) {
            entityManagerFactory = Persistence.createEntityManagerFactory("postgresql-db");
            entityManager = entityManagerFactory.createEntityManager();
        }
    }

    public static void stop() {
        if (entityManager.isOpen()) {
            entityManager.close();
        }
        if (entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }
    }

    public static EntityManager getEntityManager() {
        return entityManager;
    }

    public static <T extends Model> T find(Class<T> type, Object id) {
        return entityManager.find(type, id);
    }

    public static <T extends Model> void persist(T t) {
        if (!entityManager.getTransaction().isActive()) {
            entityManager.getTransaction().begin();
            entityManager.persist(t);
            entityManager.getTransaction().commit();
        } else {
            entityManager.persist(t);
        }
    }

    public static <T extends Model> void remove(T t) {
        if (!entityManager.getTransaction().isActive()) {
            entityManager.getTransaction().begin();
            entityManager.remove(t);
            entityManager.getTransaction().commit();
        } else {
            entityManager.remove(t);
        }
    }

    public static <T extends Model> T merge(T t) {
        if (!entityManager.getTransaction().isActive()) {
            entityManager.getTransaction().begin();
            t = entityManager.merge(t);
            entityManager.getTransaction().commit();
            return t;
        } else {
            return entityManager.merge(t);
        }
    }
}
