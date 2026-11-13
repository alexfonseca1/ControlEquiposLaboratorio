package ni.uam.edu.controlequiposlaboratorio.database;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.Map;

public final class JPAUtil {

    private static EntityManagerFactory entityManagerFactory;

    private JPAUtil() {
    }

    public static synchronized void iniciar() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            return;
        }

        String password = System.getenv("BD_PASSWORD");

        if (password == null || password.isEmpty()) {
            throw new IllegalStateException(
                    "Configurá BD_PASSWORD en la configuración de Launcher."
            );
        }

        entityManagerFactory = Persistence.createEntityManagerFactory(
                "laboratorioPU",
                Map.of("jakarta.persistence.jdbc.password", password)
        );
    }

    public static synchronized EntityManagerFactory getEntityManagerFactory() {
        iniciar();
        return entityManagerFactory;
    }

    public static synchronized EntityManager getEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }

    public static synchronized void cerrar() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }

        entityManagerFactory = null;
    }
}