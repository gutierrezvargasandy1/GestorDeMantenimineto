package com.utng.TecnicoModule.controller;

import com.utng.TecnicoModule.model.Equipo;
import com.utng.TecnicoModule.model.RegistroActualizacion;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.time.LocalDate;

/** Formulario de alta / edicion de registros_actualizaciones. */
public class DialogoActualizacionController {

    @FXML
    private Label lblTitulo;
    @FXML
    private Label lblIdRegistro;
    @FXML
    private ComboBox<Equipo> cmbEquipo;
    @FXML
    private ComboBox<String> cmbTipo;
    @FXML
    private TextField txtNombre;
    @FXML
    private TextField txtVersionActual;
    @FXML
    private TextField txtVersionNueva;
    @FXML
    private DatePicker dpFecha;
    @FXML
    private Label lblResponsable;
    @FXML
    private Label lblError;

    private RegistroActualizacion registro; // null = alta nueva
    private int idTecnico;
    private String nombreTecnico;

    @FXML
    private void initialize() {
        cmbTipo.getItems().setAll(RegistroActualizacion.TIPOS);
        DialogoMantenimientoController.formatoISO(dpFecha);
        dpFecha.setValue(LocalDate.now());
    }

    public void configurar(ObservableList<Equipo> equipos, int idTecnico, String nombreTecnico) {
        cmbEquipo.getItems().setAll(equipos);
        this.idTecnico = idTecnico;
        this.nombreTecnico = nombreTecnico;
        lblResponsable.setText("#" + idTecnico + " - " + nombreTecnico);
    }

    /** Precarga desde equipos_programas: equipo, nombre y version instalada. */
    public void precargarPrograma(Equipo equipo, String nombrePrograma, String versionActual) {
        if (equipo != null)
            cmbEquipo.getSelectionModel().select(equipo);
        cmbTipo.getSelectionModel().select("Programa");
        txtNombre.setText(nombrePrograma);
        txtVersionActual.setText(versionActual);
    }

    public void cargarRegistro(RegistroActualizacion r, ObservableList<Equipo> equipos) {
        this.registro = r;
        lblTitulo.setText("Editar actualizacion");
        lblIdRegistro.setText("id " + r.getId());

        equipos.stream()
                .filter(e -> e.getId() == r.getIdEquipo())
                .findFirst()
                .ifPresent(e -> cmbEquipo.getSelectionModel().select(e));

        cmbTipo.getSelectionModel().select(r.getTipo());
        txtNombre.setText(r.getNombreActualizado());
        txtVersionActual.setText(r.getVersionActual());
        txtVersionNueva.setText(r.getVersionActualizada());
        dpFecha.setValue(r.getFecha());
    }

    public boolean esEdicion() {
        return registro != null;
    }

    public boolean validar() {
        if (cmbEquipo.getValue() == null)
            return fallar("Selecciona el equipo (id_equipo).");
        if (cmbTipo.getValue() == null)
            return fallar("Selecciona el tipo de actualizacion.");
        if (vacio(txtNombre))
            return fallar("Escribe que se actualizo (nombre_actualizado).");
        if (vacio(txtVersionNueva))
            return fallar("Escribe la version_actualizada.");
        if (dpFecha.getValue() == null)
            return fallar("Indica la fecha.");
        if (dpFecha.getValue().isAfter(LocalDate.now()))
            return fallar("La fecha no puede ser futura.");

        ocultarError();
        return true;
    }

    private boolean vacio(TextField t) {
        return t.getText() == null || t.getText().trim().isEmpty();
    }

    private boolean fallar(String mensaje) {
        lblError.setText(mensaje);
        lblError.setVisible(true);
        lblError.setManaged(true);
        return false;
    }

    private void ocultarError() {
        lblError.setVisible(false);
        lblError.setManaged(false);
    }

    public RegistroActualizacion obtenerRegistro() {
        Equipo eq = cmbEquipo.getValue();

        if (registro == null) {
            registro = new RegistroActualizacion();
            registro.setId(0);
            registro.setFechaRegistro(LocalDate.now());
        }

        registro.setIdEquipo(eq.getId());
        registro.setEquipoNombre(eq.getEquipos());
        registro.setTipo(cmbTipo.getValue());
        registro.setNombreActualizado(txtNombre.getText().trim());
        registro.setVersionActual(txtVersionActual.getText() == null ? "" : txtVersionActual.getText().trim());
        registro.setVersionActualizada(txtVersionNueva.getText().trim());
        registro.setFecha(dpFecha.getValue());
        registro.setIdUsuarioResponsable(idTecnico);
        registro.setUsuarioResponsable("#" + idTecnico + " " + nombreTecnico);

        return registro;
    }
}
