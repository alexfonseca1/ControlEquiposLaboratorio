package ni.uam.edu.controlequiposlaboratorio.controller;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import ni.uam.edu.controlequiposlaboratorio.dao.EquipoDAO;
import ni.uam.edu.controlequiposlaboratorio.dao.TipoEquipoDAO;
import ni.uam.edu.controlequiposlaboratorio.model.Equipo;
import ni.uam.edu.controlequiposlaboratorio.model.TipoEquipo;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class EquipoController {

    private static final List<String> ESTADOS = List.of(
            "Disponible",
            "En mantenimiento",
            "Fuera de servicio"
    );

    @FXML private TextField txtCodigo;
    @FXML private TextField txtNombre;
    @FXML private TextField txtMarca;
    @FXML private TextField txtCantidad;
    @FXML private ComboBox<TipoEquipo> cmbTipoEquipo;
    @FXML private ComboBox<String> cmbEstado;

    @FXML private Button btnActualizar;
    @FXML private Button btnEliminar;

    @FXML private TextField txtBuscarNombre;
    @FXML private ComboBox<TipoEquipo> cmbFiltroTipo;

    @FXML private TableView<Equipo> tablaEquipos;
    @FXML private TableColumn<Equipo, String> colCodigo;
    @FXML private TableColumn<Equipo, String> colNombre;
    @FXML private TableColumn<Equipo, String> colMarca;
    @FXML private TableColumn<Equipo, String> colEstado;
    @FXML private TableColumn<Equipo, Integer> colCantidad;
    @FXML private TableColumn<Equipo, String> colTipo;

    private final EquipoDAO equipoDAO = new EquipoDAO();
    private final TipoEquipoDAO tipoEquipoDAO = new TipoEquipoDAO();

    @FXML
    private void initialize() {
        colCodigo.setCellValueFactory(celda ->
                new ReadOnlyStringWrapper(celda.getValue().getCodigo()));
        colNombre.setCellValueFactory(celda ->
                new ReadOnlyStringWrapper(celda.getValue().getNombre()));
        colMarca.setCellValueFactory(celda ->
                new ReadOnlyStringWrapper(celda.getValue().getMarca()));
        colEstado.setCellValueFactory(celda ->
                new ReadOnlyStringWrapper(celda.getValue().getEstado()));
        colCantidad.setCellValueFactory(celda ->
                new ReadOnlyObjectWrapper<>(celda.getValue().getCantidad()));
        colTipo.setCellValueFactory(celda ->
                new ReadOnlyStringWrapper(
                        celda.getValue().getTipoEquipo().getNombre()));

        var seleccion = tablaEquipos.getSelectionModel().selectedItemProperty();

        btnActualizar.disableProperty().bind(seleccion.isNull());
        btnEliminar.disableProperty().bind(seleccion.isNull());

        seleccion.addListener((observable, anterior, equipo) -> {
            if (equipo != null) {
                mostrarEquipoEnFormulario(equipo);
            }
        });

        cmbEstado.setItems(FXCollections.observableArrayList(ESTADOS));

        try {
            List<TipoEquipo> tipos = tipoEquipoDAO.listar();
            cmbTipoEquipo.setItems(FXCollections.observableArrayList(tipos));
            cmbFiltroTipo.setItems(FXCollections.observableArrayList(tipos));
        } catch (RuntimeException ex) {
            mostrarError("No se pudieron cargar los tipos de equipo.", ex);
        }

        buscarEquipos();
    }

    private void mostrarEquipoEnFormulario(Equipo equipo) {
        txtCodigo.setText(equipo.getCodigo());
        txtNombre.setText(equipo.getNombre());
        txtMarca.setText(equipo.getMarca() == null ? "" : equipo.getMarca());
        txtCantidad.setText(String.valueOf(equipo.getCantidad()));
        cmbEstado.setValue(equipo.getEstado());

        Long idTipo = equipo.getTipoEquipo().getIdTipo();

        TipoEquipo tipoDelFormulario = cmbTipoEquipo.getItems().stream()
                .filter(tipo -> Objects.equals(tipo.getIdTipo(), idTipo))
                .findFirst()
                .orElse(null);

        cmbTipoEquipo.setValue(tipoDelFormulario);
    }

    private Equipo leerFormulario() {
        String codigo = txtCodigo.getText().trim();
        String nombre = txtNombre.getText().trim();
        String marca = txtMarca.getText().trim();
        String estado = cmbEstado.getValue();
        TipoEquipo tipo = cmbTipoEquipo.getValue();

        if (codigo.isEmpty() || nombre.isEmpty()) {
            throw new IllegalArgumentException("Ingresá el código y el nombre.");
        }

        if (codigo.length() > 30 || nombre.length() > 150 || marca.length() > 100) {
            throw new IllegalArgumentException(
                    "El código admite 30 caracteres, el nombre 150 y la marca 100."
            );
        }

        if (tipo == null) {
            throw new IllegalArgumentException("Seleccioná un tipo de equipo.");
        }

        if (estado == null || !ESTADOS.contains(estado)) {
            throw new IllegalArgumentException("Seleccioná un estado válido.");
        }

        int cantidad;

        try {
            cantidad = Integer.parseInt(txtCantidad.getText().trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Ingresá una cantidad entera válida.");
        }

        if (cantidad < 0) {
            throw new IllegalArgumentException("La cantidad no puede ser negativa.");
        }

        return new Equipo(codigo, nombre, marca, estado, cantidad, tipo);
    }

    @FXML
    private void guardarEquipo() {
        try {
            equipoDAO.guardar(leerFormulario());
        } catch (IllegalArgumentException ex) {
            mostrarAlerta(Alert.AlertType.WARNING, ex.getMessage());
            return;
        } catch (RuntimeException ex) {
            mostrarError("No se pudo guardar el equipo.", ex);
            return;
        }

        finalizarOperacion("Equipo guardado correctamente.");
    }

    @FXML
    private void actualizarEquipo() {
        Equipo seleccionado = tablaEquipos.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            return;
        }

        boolean actualizado;

        try {
            actualizado = equipoDAO.actualizar(
                    seleccionado.getIdEquipo(),
                    leerFormulario()
            );
        } catch (IllegalArgumentException ex) {
            mostrarAlerta(Alert.AlertType.WARNING, ex.getMessage());
            return;
        } catch (RuntimeException ex) {
            mostrarError("No se pudo actualizar el equipo.", ex);
            return;
        }

        if (!actualizado) {
            mostrarAlerta(Alert.AlertType.WARNING, "El equipo seleccionado ya no existe.");
            buscarEquipos();
            return;
        }

        finalizarOperacion("Equipo actualizado correctamente.");
    }

    @FXML
    private void eliminarEquipo() {
        Equipo seleccionado = tablaEquipos.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            return;
        }

        Alert confirmacion = new Alert(
                Alert.AlertType.CONFIRMATION,
                "¿Querés eliminar el equipo " + seleccionado.getCodigo() + "?",
                ButtonType.OK,
                ButtonType.CANCEL
        );
        confirmacion.setTitle("Eliminar equipo");
        confirmacion.setHeaderText(null);

        if (confirmacion.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        boolean eliminado;

        try {
            eliminado = equipoDAO.eliminar(seleccionado.getIdEquipo());
        } catch (RuntimeException ex) {
            mostrarError("No se pudo eliminar el equipo.", ex);
            return;
        }

        if (!eliminado) {
            mostrarAlerta(Alert.AlertType.WARNING, "El equipo seleccionado ya no existe.");
            buscarEquipos();
            return;
        }

        finalizarOperacion("Equipo eliminado correctamente.");
    }

    @FXML
    private void buscarEquipos() {
        try {
            cargarTabla();
        } catch (RuntimeException ex) {
            mostrarError("No se pudieron consultar los equipos.", ex);
        }
    }

    private void cargarTabla() {
        TipoEquipo tipo = cmbFiltroTipo.getValue();
        Long idTipo = tipo == null ? null : tipo.getIdTipo();

        List<Equipo> equipos = equipoDAO.buscar(
                txtBuscarNombre.getText().trim(),
                idTipo
        );

        tablaEquipos.setItems(FXCollections.observableArrayList(equipos));
        limpiarFormulario();
    }

    @FXML
    private void limpiarFiltros() {
        txtBuscarNombre.clear();
        cmbFiltroTipo.setValue(null);
        buscarEquipos();
    }

    @FXML
    private void limpiarFormulario() {
        tablaEquipos.getSelectionModel().clearSelection();
        txtCodigo.clear();
        txtNombre.clear();
        txtMarca.clear();
        txtCantidad.clear();
        cmbTipoEquipo.setValue(null);
        cmbEstado.setValue(null);
        txtCodigo.requestFocus();
    }

    private void finalizarOperacion(String mensaje) {
        limpiarFormulario();

        try {
            cargarTabla();
        } catch (RuntimeException ex) {
            mostrarError(mensaje + " No se pudo recargar la tabla.", ex);
            return;
        }

        mostrarAlerta(Alert.AlertType.INFORMATION, mensaje);
    }

    private void mostrarError(String mensaje, RuntimeException ex) {
        ex.printStackTrace();

        for (Throwable causa = ex; causa != null; causa = causa.getCause()) {
            if (causa instanceof SQLException sql && "23505".equals(sql.getSQLState())) {
                mostrarAlerta(Alert.AlertType.WARNING, "Ya existe un equipo con ese código.");
                return;
            }
        }

        mostrarAlerta(Alert.AlertType.ERROR, mensaje);
    }

    private void mostrarAlerta(Alert.AlertType tipo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle("Control de equipos");
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}