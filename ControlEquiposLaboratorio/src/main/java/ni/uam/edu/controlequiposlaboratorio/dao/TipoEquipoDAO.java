package ni.uam.edu.controlequiposlaboratorio.dao;

import jakarta.persistence.EntityManager;
import ni.uam.edu.controlequiposlaboratorio.database.JPAUtil;
import ni.uam.edu.controlequiposlaboratorio.model.TipoEquipo;

import java.util.List;

public class TipoEquipoDAO {

    public List<TipoEquipo> listar() {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            return em.createQuery(
                    "SELECT t FROM TipoEquipo t ORDER BY t.nombre",
                    TipoEquipo.class
            ).getResultList();
        }
    }
}