package com.utng.TecnicoModule.controller;

import com.utng.TecnicoModule.model.Equipo;
import com.utng.TecnicoModule.model.RegistroMantenimiento;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/** Formulario de alta / edicion de registros_mantenimiento. */
public class DialogoMantenimientoController {

    @FXML
    private Label lblTitulo;
    @FXML
    private Label lblIdRegistro;
    @FXML
    private ComboBox<Equipo> cmbEquipo;
    @FXML
    private ComboBox<String> cmbTipo;
    @FXML
    private TextArea txtMotivo;
    @FXML
    private TextArea txtRealizado;
    @FXML
    private DatePicker dpFecha;
    @FXML
    private DatePicker dpFechaProxima;
    @FXML
    private ComboBox<String> cmbEstado;
    @FXML
    private Label lblResponsable;
    @FXML
    private Label lblError;

    private RegistroMantenimiento registro; // null = alta nueva
    private int idTecnico;
    private String nombreTecnico;

    @FXML
    private void initialize() {
        cmbTipo.getItems().setAll(RegistroMantenimiento.TIPOS);
        cmbEstado.getItems().setAll(RegistroMantenimiento.ESTADOS);
        cmbEstado.getSelectionModel().selectFirst();
        formatoISO(dpFecha);
        formatoISO(dpFechaProxima);
        dpFecha.setValue(LocalDate.now());
    }

    /**
     * Muestra y acepta las fechas como yyyy-MM-dd, igual que en la base de datos.
     */
    static void formatoISO(DatePicker dp) {
        final DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        dp.setPromptText("yyyy-MM-dd");
        dp.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate d) {
                return d == null ? "" : f.format(d);
            }

            @Override
            public LocalDate fromString(String s) {
                if (s == null || s.trim().isEmpty())
                    return null;
                try {
                    return LocalDate.parse(s.trim(), f);
                } catch (Exception e) {
                    return null;
                }
            }
        });
    }

    /** Carga los equipos disponibles y los datos del tecnico logueado. */
    public void configurar(ObservableList<Equipo> equipos, int idTecnico, String nombreTecnico) {
        cmbEquipo.getItems().setAll(equipos);
        this.idTecnico = idTecnico;
        this.nombreTecnico = nombreTecnico;
        lblResponsable.setText("#" + idTecnico + " - " + nombreTecnico);
    }

    /** Precarga el equipo (por ejemplo cuando viene de un reporte del chat). */
    public void preseleccionarEquipo(Equipo equipo) {
        if (equipo != null)
            cmbEquipo.getSelectionModel().select(equipo);
    }

    public void precargarMotivo(String motivo) {
        txtMotivo.setText(motivo);
    }

    /** Pasa el dialogo a modo edicion sobre un registro existente. */
    public void cargarRegistro(RegistroMantenimiento r, ObservableList<Equipo> equipos) {
        this.registro = r;
        lblTitulo.setText("Editar mantenimiento");
        lblIdRegistro.setText("id " + r.getId());

        equipos.stream()
                .filter(e -> e.getId() == r.getIdEquipo())
                .findFirst()
                .ifPresent(e -> cmbEquipo.getSelectionModel().select(e));

        cmbTipo.getSelectionModel().select(r.getTipo());
        txtMotivo.setText(r.getMotivo());
        txtRealizado.setText(r.getMantenimientoRealizado());
        dpFecha.setValue(r.getFecha());
        dpFechaProxima.setValue(r.getFechaProxima());
        cmbEstado.getSelectionModel().select(r.getEstado());
    }

    public boolean esEdicion() {
        return registro != null;
    }

    /** @return true si el formulario esta completo; si no, pinta el error. */
    public boolean validar() {
        if (cmbEquipo.getValue() == null)
            return fallar("Selecciona el equipo (id_equipo).");
        if (cmbTipo.getValue() == null)
            return fallar("Selecciona el tipo de mantenimiento.");
        if (txtMotivo.getText() == null
                || txtMotivo.getText().trim().isEmpty())
            return fallar("El motivo no puede quedar vacio.");
        if (dpFecha.getValue() == null)
            return fallar("Indica la fecha del mantenimiento.");
        if (cmbEstado.getValue() == null)
            return fallar("Selecciona el estado.");
        if (dpFechaProxima.getValue() != null
                && dpFechaProxima.getValue().isBefore(dpFecha.getValue()))
            return fallar("La fecha_proxima no puede ser anterior a la fecha del mantenimiento.");
        if ("Completado".equalsIgnoreCase(cmbEstado.getValue())
                && (txtRealizado.getText() == null || txtRealizado.getText().trim().isEmpty()))
            return fallar("Para marcarlo como Completado describe el mantenimiento_realizado.");

        ocultarError();
        return true;
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

    /**
     * Devuelve el registro con los datos del formulario.
     * En alta el id llega en 0: asignalo con el que regrese tu INSERT.
     */
    public RegistroMantenimiento obtenerRegistro() {
        Equipo eq = cmbEquipo.getValue();

        if (registro == null) {
            registro = new RegistroMantenimiento();
            registro.setId(0);
            registro.setFechaRegistro(LocalDate.now());
        }

        registro.setIdEquipo(eq.getId());
        registro.setEquipoNombre(eq.getEquipos());
        registro.setTipo(cmbTipo.getValue());
        registro.setMotivo(txtMotivo.getText().trim());
        registro.setMantenimientoRealizado(
                txtRealizado.getText() == null ? "" : txtRealizado.getText().trim());
        registro.setFecha(dpFecha.getValue());
        registro.setFechaProxima(dpFechaProxima.getValue());
        registro.setEstado(cmbEstado.getValue());
        registro.setIdUsuarioResponsable(idTecnico);
        registro.setUsuarioResponsable("#" + idTecnico + " " + nombreTecnico);

        return registro;
    }
}
