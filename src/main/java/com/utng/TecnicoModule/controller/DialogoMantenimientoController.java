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
import java.util.Objects;

/** Formulario de alta / edicion de registros_mantenimiento. */
public class DialogoMantenimientoController {

    private static final String ESTADO_PENDIENTE = "Pendiente";
    private static final String ESTADO_COMPLETADO = "Completado";

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
    private TextArea txtRealizado; // ahora: notas_realizado
    @FXML
    private DatePicker dpFecha;
    @FXML
    private DatePicker dpFechaProxima;
    @FXML
    private ComboBox<String> cmbEstado; // ahora: Pendiente / Completado (mantenimiento_realizado)
    @FXML
    private Label lblResponsable;
    @FXML
    private Label lblError;

    private RegistroMantenimiento registro; // null = alta nueva
    private Long idTecnico;
    private String nombreTecnico;

    @FXML
    private void initialize() {
        cmbTipo.getItems().setAll(RegistroMantenimiento.TIPOS);
        cmbEstado.getItems().setAll(ESTADO_PENDIENTE, ESTADO_COMPLETADO);
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
    public void configurar(ObservableList<Equipo> equipos, Long idTecnico, String nombreTecnico) {
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
                .filter(e -> Objects.equals(e.getId(), r.getIdEquipo()))
                .findFirst()
                .ifPresent(e -> cmbEquipo.getSelectionModel().select(e));

        cmbTipo.getSelectionModel().select(r.getTipo());
        txtMotivo.setText(r.getMotivo());
        txtRealizado.setText(r.getNotasRealizado());
        dpFecha.setValue(r.getFecha());
        dpFechaProxima.setValue(r.getFechaProxima());
        cmbEstado.getSelectionModel().select(r.isMantenimientoRealizado() ? ESTADO_COMPLETADO : ESTADO_PENDIENTE);
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
        if (ESTADO_COMPLETADO.equalsIgnoreCase(cmbEstado.getValue())
                && (txtRealizado.getText() == null || txtRealizado.getText().trim().isEmpty()))
            return fallar("Para marcarlo como Completado describe que se hizo.");

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
     * Devuelve el registro con los datos del formulario. En alta, el id
     * queda en null: lo asigna el repositorio real con RETURNING id.
     */
    public RegistroMantenimiento obtenerRegistro() {
        Equipo eq = cmbEquipo.getValue();

        if (registro == null) {
            registro = new RegistroMantenimiento();
        }

        registro.setIdEquipo(eq.getId());
        registro.setEquipoNombre(eq.getEquipos());
        registro.setTipo(cmbTipo.getValue());
        registro.setMotivo(txtMotivo.getText().trim());
        registro.setNotasRealizado(
                txtRealizado.getText() == null ? null : txtRealizado.getText().trim());
        registro.setFecha(dpFecha.getValue());
        registro.setFechaProxima(dpFechaProxima.getValue());
        registro.setMantenimientoRealizado(ESTADO_COMPLETADO.equalsIgnoreCase(cmbEstado.getValue()));
        registro.setIdUsuarioResponsable(idTecnico);
        registro.setUsuarioResponsable("#" + idTecnico + " " + nombreTecnico);

        return registro;
    }
}