# Control de Equipos de Laboratorio

Aplicación de escritorio para registrar, consultar, actualizar y eliminar equipos de laboratorio. Permite buscar por una parte del nombre, filtrar por tipo de equipo y combinar ambos criterios.

**Asignatura:** Programación de Aplicaciones de Escritorio  
**Actividad:** Evaluación #1, Corte III

## Requisitos

- JDK 21.
- IntelliJ IDEA con el proyecto abierto como Maven.
- PostgreSQL en ejecución. pgAdmin puede usarse para preparar y verificar la base.
- Maven Wrapper si el proyecto incluye `mvnw.cmd`, o una instalación de Maven.

Las versiones configuradas en `pom.xml` son JavaFX **21.0.6**, Hibernate ORM **7.4.14.Final** y el controlador JDBC de PostgreSQL **42.7.14**.

## Preparar PostgreSQL

1. Abrí el Query Tool de la base `postgres` en pgAdmin. Si todavía no existe la base solicitada para la evaluación, ejecutá `sql/01_crear_base.sql` por separado.

   ```sql
   CREATE DATABASE bd_laboratorio_orm;
   ```

2. Actualizá la lista de bases y abrí el Query Tool de **`bd_laboratorio_orm`**. Ejecutá `sql/02_esquema_y_datos.sql` para crear el esquema `laboratorio`, sus tablas y los datos iniciales.
3. Comprobá que una instalación nueva tenga **3 tipos** y **8 equipos**.

   ```sql
   SELECT COUNT(*) AS tipos FROM laboratorio.tipo_equipo;
   SELECT COUNT(*) AS equipos FROM laboratorio.equipo;
   ```

Si ya cargaste los datos en una base local llamada `bdlaboratoriorm`, usá ese nombre para ejecutar tu copia local. Para la entrega, verificá que los scripts preparen `bd_laboratorio_orm`, como indica la evaluación.

## Configurar y ejecutar

1. Abrí la carpeta que contiene `pom.xml` en IntelliJ IDEA, importá el proyecto Maven y seleccioná **JDK 21**.
2. En `src/main/resources/META-INF/persistence.xml`, verificá que la unidad se llame `laboratorioPU` y ajustá la URL y el usuario a tu instalación.

   | Configuración | Valor de referencia |
   |---|---|
   | URL JDBC | `jdbc:postgresql://localhost:5432/bd_laboratorio_orm` |
   | Usuario de PostgreSQL | `postgres` |
   | Unidad de persistencia | `laboratorioPU` |
   | Contraseña | Variable de entorno `BD_PASSWORD` |

3. En **Run → Edit Configurations**, seleccioná la configuración que ejecuta `ni.uam.edu.controlequiposlaboratorio.Launcher`. Agregá `BD_PASSWORD` en **Environment variables** con la contraseña de tu usuario de PostgreSQL. Mantené la contraseña en tu configuración local.
4. Desde la carpeta del `pom.xml`, compilá y comprobá que Maven haya copiado el archivo de persistencia.

   ```powershell
   .\mvnw.cmd clean compile
   Test-Path ".\target\classes\META-INF\persistence.xml"
   ```

   Si usás Maven instalado, reemplazá `.\mvnw.cmd` por `mvn`. El resultado esperado es `BUILD SUCCESS` y `True`.

5. Ejecutá **Launcher** desde la configuración de IntelliJ del paso 3. La ventana debe cargar los tipos de equipo y mostrar el inventario inicial.

Los scripts SQL crean el esquema y Hibernate lo verifica con `hibernate.hbm2ddl.auto=validate`. El valor `laboratorioPU` debe coincidir en `persistence.xml` y en la llamada a `Persistence.createEntityManagerFactory()` de `JPAUtil`.

## Entidades y persistencia

| Entidad | Tabla | Datos principales |
|---|---|---|
| `TipoEquipo` | `laboratorio.tipo_equipo` | Identificador, nombre y descripción. |
| `Equipo` | `laboratorio.equipo` | Identificador, código, nombre, marca, estado, cantidad y tipo. |

Cada equipo pertenece a un tipo mediante `@ManyToOne` y `@JoinColumn(name = "id_tipo")`. Un tipo puede tener varios equipos mediante `@OneToMany(mappedBy = "tipoEquipo")`. PostgreSQL valida el código único, la cantidad no negativa, los estados permitidos y la existencia del tipo referenciado.

- `model` contiene las entidades.
- `dao` consulta y modifica los datos con Hibernate.
- `database/JPAUtil.java` administra la fábrica y proporciona los `EntityManager`.
- `controller/EquipoController.java` conecta el formulario, la tabla y las acciones con los DAO.
- `equipo-view.fxml` define la vista de inventario.

El listado carga el tipo de cada equipo con `JOIN FETCH`. La búsqueda usa JPQL/HQL con parámetros para comparar una parte del nombre sin distinguir mayúsculas. El filtro por tipo puede aplicarse por sí solo o junto con la búsqueda. Las escrituras se confirman con `commit()` y, si ocurre un error con la transacción todavía activa, se revierten con `rollback()`.

## Pruebas de la evaluación

Registrá el resultado obtenido y una evidencia de cada caso en la matriz de pruebas.

| Prueba | Comprobación |
|---|---|
| P1 | Guardar un equipo válido y verificar que aparezca en la tabla. |
| P2 | Intentar guardar un código duplicado y comprobar que PostgreSQL conserve un solo registro con ese código. |
| P3 | Actualizar un equipo y verificar los cambios, incluido el tipo si se modifica. |
| P4 | Eliminar un equipo con confirmación y verificar su ausencia en PostgreSQL. |
| P5 | Buscar por un fragmento del nombre. |
| P6 | Filtrar por tipo y combinarlo con la búsqueda. |
| P7 | Intentar guardar una cantidad negativa y comprobar que se rechace. |
| P8 | Reiniciar la aplicación y verificar que los cambios confirmados se mantengan. |
