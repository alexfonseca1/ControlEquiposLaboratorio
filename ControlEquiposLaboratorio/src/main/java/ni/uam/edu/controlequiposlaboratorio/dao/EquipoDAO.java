package ni.uam.edu.controlequiposlaboratorio.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import ni.uam.edu.controlequiposlaboratorio.database.JPAUtil;
import ni.uam.edu.controlequiposlaboratorio.model.Equipo;
import ni.uam.edu.controlequiposlaboratorio.model.TipoEquipo;

import java.util.List;

public class EquipoDAO {

    public void guardar(Equipo equipo) {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            EntityTransaction tx = em.getTransaction();

            try {
                tx.begin();

                equipo.setTipoEquipo(obtenerTipoAdministrado(em, equipo));
                em.persist(equipo);

                tx.commit();
            } catch (RuntimeException ex) {
                if (tx.isActive()) {
                    tx.rollback();
                }
                throw ex;
            }
        }
    }

    public List<Equipo> buscar(String nombre, Long idTipo) {
        String texto = nombre == null ? "" : nombre.trim();

        String jpql = """
                SELECT e
                FROM Equipo e
                JOIN FETCH e.tipoEquipo t
                WHERE LOCATE(LOWER(:nombre), LOWER(e.nombre)) > 0
                """;

        if (idTipo != null) {
            jpql += " AND t.idTipo = :idTipo";
        }

        jpql += " ORDER BY e.codigo";

        try (EntityManager em = JPAUtil.getEntityManager()) {
            TypedQuery<Equipo> consulta = em.createQuery(jpql, Equipo.class);
            consulta.setParameter("nombre", texto);

            if (idTipo != null) {
                consulta.setParameter("idTipo", idTipo);
            }

            return consulta.getResultList();
        }
    }

    public boolean actualizar(Long idEquipo, Equipo datos) {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            EntityTransaction tx = em.getTransaction();

            try {
                tx.begin();

                Equipo equipo = em.find(Equipo.class, idEquipo);

                if (equipo == null) {
                    tx.rollback();
                    return false;
                }

                TipoEquipo tipo = obtenerTipoAdministrado(em, datos);

                equipo.setCodigo(datos.getCodigo());
                equipo.setNombre(datos.getNombre());
                equipo.setMarca(datos.getMarca());
                equipo.setEstado(datos.getEstado());
                equipo.setCantidad(datos.getCantidad());
                equipo.setTipoEquipo(tipo);

                tx.commit();
                return true;
            } catch (RuntimeException ex) {
                if (tx.isActive()) {
                    tx.rollback();
                }
                throw ex;
            }
        }
    }

    public boolean eliminar(Long idEquipo) {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            EntityTransaction tx = em.getTransaction();

            try {
                tx.begin();

                Equipo equipo = em.find(Equipo.class, idEquipo);

                if (equipo == null) {
                    tx.rollback();
                    return false;
                }

                em.remove(equipo);

                tx.commit();
                return true;
            } catch (RuntimeException ex) {
                if (tx.isActive()) {
                    tx.rollback();
                }
                throw ex;
            }
        }
    }

    private TipoEquipo obtenerTipoAdministrado(EntityManager em, Equipo equipo) {
        TipoEquipo seleccionado = equipo.getTipoEquipo();

        if (seleccionado == null || seleccionado.getIdTipo() == null) {
            throw new IllegalArgumentException("Seleccioná un tipo de equipo.");
        }

        TipoEquipo tipo = em.find(TipoEquipo.class, seleccionado.getIdTipo());

        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de equipo seleccionado ya no existe.");
        }

        return tipo;
    }
}