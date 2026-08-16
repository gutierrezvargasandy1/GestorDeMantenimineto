package com.utng.TecnicoModule.controller;

import com.utng.TecnicoModule.model.Equipo;
import com.utng.TecnicoModule.model.EquipoPrograma;
import com.utng.TecnicoModule.model.HistorialRegistro;
import com.utng.TecnicoModule.model.RegistroActualizacion;
import com.utng.TecnicoModule.model.RegistroMantenimiento;
import com.utng.TecnicoModule.model.UsuarioChat;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

/**
 * Controlador de la pantalla del rol TECNICO.
 *
 * Permisos de este rol:
 * - VER equipos, programas instalados e historial.
 * - CREAR y EDITAR registros_mantenimiento y registros_actualizaciones.
 * - Cambiar el estado operativo de un equipo.
 * - Chatear con los consultores para atender sus reportes.
 * - NO tiene ningun acceso a la tabla usuarios (alta, baja, edicion ni
 * consulta);
 * del chat solo se leen id, nombre_completo y rol.
 */
public class PantallaTecnicoController {

    // ═══════════ SESION ═══════════
    // TODO: sustituye por el usuario que devuelve tu login.
    private int idTecnicoSesion = 101;
    private String nombreTecnicoSesion = "Gerardo Espindola";

    // ───────────── ENCABEZADO / KPIs ─────────────
    @FXML
    private Button btnMenu;
    @FXML
    private Label lblFechaHoy;
    @FXML
    private Label lblTotalEquipos;
    @FXML
    private Label lblPendientes;
    @FXML
    private Label lblVencidos;
    @FXML
    private Label lblActualizacionesMes;

    @FXML
    private TabPane tabs;

    // ───────────── TAB EQUIPOS ─────────────
    @FXML
    private TextField txtBuscarEquipo;
    @FXML
    private ComboBox<String> cmbLugar;
    @FXML
    private ComboBox<String> cmbEstadoEquipo;
    @FXML
    private TableView<Equipo> tablaEquipos;
    @FXML
    private TableColumn<Equipo, String> eqId, eqNombre, eqModelo, eqProcesador, eqRam,
            eqAlmacen, eqSO, eqLugar, eqEstado, eqAnio,
            eqResponsable, eqFechaCre, eqFechaAct;
    @FXML
    private Label lblConteoEquipos;

    // ───────────── TAB MANTENIMIENTOS ─────────────
    @FXML
    private TextField txtBuscarMant;
    @FXML
    private ComboBox<String> cmbTipoMant;
    @FXML
    private Button chipMantTodos;
    @FXML
    private HBox chipPendiente, chipEnProceso, chipCompletado, chipVencidos;
    @FXML
    private Label lblMantPendiente, lblMantProceso, lblMantCompletado, lblMantVencidos;
    @FXML
    private TableView<RegistroMantenimiento> tablaMantenimientos;
    @FXML
    private TableColumn<RegistroMantenimiento, String> mtId, mtIdEquipo, mtEquipo, mtTipo,
            mtMotivo, mtRealizado, mtFecha,
            mtFechaProx, mtEstado, mtResponsable,
            mtFechaReg;
    @FXML
    private Label lblConteoMant;

    // ───────────── TAB ACTUALIZACIONES ─────────────
    @FXML
    private TextField txtBuscarAct;
    @FXML
    private ComboBox<String> cmbTipoAct;
    @FXML
    private TableView<RegistroActualizacion> tablaActualizaciones;
    @FXML
    private TableColumn<RegistroActualizacion, String> acId, acIdEquipo, acEquipo, acTipo,
            acNombre, acVersion, acVersionNueva,
            acFecha, acResponsable, acFechaReg;
    @FXML
    private Label lblConteoAct;

    // ───────────── TAB PROGRAMAS ─────────────
    @FXML
    private TextField txtBuscarProg;
    @FXML
    private TableView<EquipoPrograma> tablaProgramas;
    @FXML
    private TableColumn<EquipoPrograma, String> pgId, pgIdEquipo, pgEquipo, pgIdPrograma,
            pgNombre, pgVersion, pgFecha;
    @FXML
    private Label lblConteoProg;

    // ───────────── TAB HISTORIAL ─────────────
    @FXML
    private TextField txtBuscarHist;
    @FXML
    private TableView<HistorialRegistro> tablaHistorial;
    @FXML
    private TableColumn<HistorialRegistro, String> hsId, hsFecha, hsTipo, hsIdEquipo,
            hsEquipo, hsIdMant, hsIdAct, hsDesc;
    @FXML
    private Label lblConteoHist;

    // ───────────── CHAT ─────────────
    @FXML
    private Label lblEstadoConexion;
    @FXML
    private TextField txtBuscarConsultor;
    @FXML
    private ListView<UsuarioChat> listaConsultores;
    @FXML
    private Label lblTotalConsultores;
    @FXML
    private Label lblAvatarConsultor;
    @FXML
    private Label lblNombreConsultor;
    @FXML
    private Label lblRolConsultor;
    @FXML
    private Button btnCrearDesdeChat;
    @FXML
    private ScrollPane scrollChat;
    @FXML
    private VBox contenedorChat;
    @FXML
    private TextField txtMensaje;
    @FXML
    private Button btnEnviar;

    // ───────────── MENU ─────────────
    @FXML
    private Region overlayMenu;
    @FXML
    private VBox panelMenu;
    @FXML
    private Label lblAvatarUsuario, lblNombreUsuario, lblRolUsuario;

    // ═══════════ DATOS ═══════════
    private final ObservableList<Equipo> equipos = FXCollections.observableArrayList();
    private final ObservableList<RegistroMantenimiento> mantenimientos = FXCollections.observableArrayList();
    private final ObservableList<RegistroActualizacion> actualizaciones = FXCollections.observableArrayList();
    private final ObservableList<EquipoPrograma> programas = FXCollections.observableArrayList();
    private final ObservableList<HistorialRegistro> historial = FXCollections.observableArrayList();
    private final ObservableList<UsuarioChat> consultores = FXCollections.observableArrayList();

    private FilteredList<Equipo> fEquipos;
    private FilteredList<RegistroMantenimiento> fMantenimientos;
    private FilteredList<RegistroActualizacion> fActualizaciones;
    private FilteredList<EquipoPrograma> fProgramas;
    private FilteredList<HistorialRegistro> fHistorial;
    private FilteredList<UsuarioChat> fConsultores;

    private String estadoMantSeleccionado = "TODOS";
    private UsuarioChat consultorActual;

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    // ══════════════════════════════════════════════════════════════
    // INICIALIZACION
    // ══════════════════════════════════════════════════════════════
    @FXML
    private void initialize() {
        configurarFecha();
        configurarTablaEquipos();
        configurarTablaMantenimientos();
        configurarTablaActualizaciones();
        configurarTablaProgramas();
        configurarTablaHistorial();
        configurarListaConsultores();

        cargarDatosDemo(); // <-- reemplaza por tus SELECT

        configurarFiltros();
        refrescarTodo();
        resaltarChipMant(chipMantTodos);

        lblNombreUsuario.setText(nombreTecnicoSesion);
        lblRolUsuario.setText("Técnico CGTI");
        lblAvatarUsuario.setText(iniciales(nombreTecnicoSesion));

        txtMensaje.setDisable(true);
        btnEnviar.setDisable(true);
        btnCrearDesdeChat.setDisable(true);
    }

    private void configurarFecha() {
        LocalDate hoy = LocalDate.now();
        DateTimeFormatter f = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", new Locale("es", "MX"));
        String t = hoy.format(f);
        lblFechaHoy.setText(t.substring(0, 1).toUpperCase() + t.substring(1));
    }

    // ══════════════════════════════════════════════════════════════
    // TABLAS
    // ══════════════════════════════════════════════════════════════
    private void configurarTablaEquipos() {
        eqId.setCellValueFactory(c -> txt(c.getValue().getId()));
        eqNombre.setCellValueFactory(c -> txt(c.getValue().getEquipos()));
        eqModelo.setCellValueFactory(c -> txt(c.getValue().getModelo()));
        eqProcesador.setCellValueFactory(c -> txt(c.getValue().getProcesador()));
        eqRam.setCellValueFactory(c -> txt(c.getValue().getMemoriaRam()));
        eqAlmacen.setCellValueFactory(c -> txt(c.getValue().getAlmacenamiento()));
        eqSO.setCellValueFactory(c -> txt(c.getValue().getIdSistemaOperativo()
                + " - " + c.getValue().getSistemaOperativo()));
        eqLugar.setCellValueFactory(c -> txt(c.getValue().getLugar()));
        eqEstado.setCellValueFactory(c -> txt(c.getValue().getEstado()));
        eqAnio.setCellValueFactory(c -> txt(c.getValue().getAnioCreacion()));
        eqResponsable.setCellValueFactory(c -> txt(c.getValue().getIdUsuarioResponsable()
                + " - " + c.getValue().getUsuarioResponsable()));
        eqFechaCre.setCellValueFactory(c -> txt(f(c.getValue().getFechaCreacion())));
        eqFechaAct.setCellValueFactory(c -> txt(f(c.getValue().getFechaActualizacion())));

        eqEstado.setCellFactory(col -> celdaBadge());

        tablaEquipos.setRowFactory(tv -> {
            TableRow<Equipo> fila = new TableRow<>();
            fila.setOnMouseClicked(ev -> {
                if (ev.getClickCount() == 2 && !fila.isEmpty())
                    mostrarFicha(fila.getItem());
            });
            return fila;
        });
    }

    private void configurarTablaMantenimientos() {
        mtId.setCellValueFactory(c -> txt(c.getValue().getId()));
        mtIdEquipo.setCellValueFactory(c -> txt(c.getValue().getIdEquipo()));
        mtEquipo.setCellValueFactory(c -> txt(c.getValue().getEquipoNombre()));
        mtTipo.setCellValueFactory(c -> txt(c.getValue().getTipo()));
        mtMotivo.setCellValueFactory(c -> txt(c.getValue().getMotivo()));
        mtRealizado.setCellValueFactory(c -> txt(c.getValue().getMantenimientoRealizado()));
        mtFecha.setCellValueFactory(c -> txt(f(c.getValue().getFecha())));
        mtFechaProx.setCellValueFactory(c -> txt(f(c.getValue().getFechaProxima())));
        mtEstado.setCellValueFactory(c -> txt(c.getValue().getEstado()));
        mtResponsable.setCellValueFactory(c -> txt(c.getValue().getUsuarioResponsable()));
        mtFechaReg.setCellValueFactory(c -> txt(f(c.getValue().getFechaRegistro())));

        mtEstado.setCellFactory(col -> celdaBadge());

        // fecha_proxima en rojo si ya vencio
        mtFechaProx.setCellFactory(col -> new TableCell<RegistroMantenimiento, String>() {
            @Override
            protected void updateItem(String v, boolean vacio) {
                super.updateItem(v, vacio);
                setText(vacio ? null : v);
                RegistroMantenimiento r = (vacio || getIndex() >= getTableView().getItems().size())
                        ? null
                        : getTableView().getItems().get(getIndex());
                setStyle(r != null && r.estaVencido()
                        ? "-fx-text-fill: #b91c1c; -fx-font-weight: bold;"
                        : "");
            }
        });

        tablaMantenimientos.setRowFactory(tv -> {
            TableRow<RegistroMantenimiento> fila = new TableRow<>();
            fila.setOnMouseClicked(ev -> {
                if (ev.getClickCount() == 2 && !fila.isEmpty()) {
                    tablaMantenimientos.getSelectionModel().select(fila.getItem());
                    editarMantenimiento();
                }
            });
            return fila;
        });
    }

    private void configurarTablaActualizaciones() {
        acId.setCellValueFactory(c -> txt(c.getValue().getId()));
        acIdEquipo.setCellValueFactory(c -> txt(c.getValue().getIdEquipo()));
        acEquipo.setCellValueFactory(c -> txt(c.getValue().getEquipoNombre()));
        acTipo.setCellValueFactory(c -> txt(c.getValue().getTipo()));
        acNombre.setCellValueFactory(c -> txt(c.getValue().getNombreActualizado()));
        acVersion.setCellValueFactory(c -> txt(c.getValue().getVersionActual()));
        acVersionNueva.setCellValueFactory(c -> txt(c.getValue().getVersionActualizada()));
        acFecha.setCellValueFactory(c -> txt(f(c.getValue().getFecha())));
        acResponsable.setCellValueFactory(c -> txt(c.getValue().getUsuarioResponsable()));
        acFechaReg.setCellValueFactory(c -> txt(f(c.getValue().getFechaRegistro())));

        tablaActualizaciones.setRowFactory(tv -> {
            TableRow<RegistroActualizacion> fila = new TableRow<>();
            fila.setOnMouseClicked(ev -> {
                if (ev.getClickCount() == 2 && !fila.isEmpty()) {
                    tablaActualizaciones.getSelectionModel().select(fila.getItem());
                    editarActualizacion();
                }
            });
            return fila;
        });
    }

    private void configurarTablaProgramas() {
        pgId.setCellValueFactory(c -> txt(c.getValue().getId()));
        pgIdEquipo.setCellValueFactory(c -> txt(c.getValue().getIdEquipo()));
        pgEquipo.setCellValueFactory(c -> txt(c.getValue().getEquipoNombre()));
        pgIdPrograma.setCellValueFactory(c -> txt(c.getValue().getIdPrograma()));
        pgNombre.setCellValueFactory(c -> txt(c.getValue().getNombrePrograma()));
        pgVersion.setCellValueFactory(c -> txt(c.getValue().getVersionActual()));
        pgFecha.setCellValueFactory(c -> txt(f(c.getValue().getFechaInstalacion())));
    }

    private void configurarTablaHistorial() {
        hsId.setCellValueFactory(c -> txt(c.getValue().getId()));
        hsFecha.setCellValueFactory(c -> txt(f(c.getValue().getFecha())));
        hsTipo.setCellValueFactory(c -> txt(c.getValue().getTipo()));
        hsIdEquipo.setCellValueFactory(c -> txt(c.getValue().getIdEquipo()));
        hsEquipo.setCellValueFactory(c -> txt(c.getValue().getEquipoNombre()));
        hsIdMant.setCellValueFactory(c -> txt(c.getValue().getIdRegistroMantenimiento() == 0
                ? "-"
                : String.valueOf(c.getValue().getIdRegistroMantenimiento())));
        hsIdAct.setCellValueFactory(c -> txt(c.getValue().getIdRegistroActualizacion() == 0
                ? "-"
                : String.valueOf(c.getValue().getIdRegistroActualizacion())));
        hsDesc.setCellValueFactory(c -> txt(c.getValue().getDescripcion()));
    }

    private <T> TableCell<T, String> celdaBadge() {
        return new TableCell<T, String>() {
            @Override
            protected void updateItem(String estado, boolean vacio) {
                super.updateItem(estado, vacio);
                if (vacio || estado == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                Label b = new Label(estado);
                b.setStyle(estiloBadge(estado));
                setGraphic(b);
                setText(null);
            }
        };
    }

    private String estiloBadge(String estado) {
        String fondo, texto;
        switch (estado.toLowerCase()) {
            case "activo":
            case "completado":
                fondo = "#dcfce7";
                texto = "#15803d";
                break;
            case "en mantenimiento":
            case "pendiente":
                fondo = "#fef3c7";
                texto = "#b45309";
                break;
            case "en proceso":
                fondo = "#dbeafe";
                texto = "#1d4ed8";
                break;
            case "de baja":
            case "cancelado":
                fondo = "#fee2e2";
                texto = "#b91c1c";
                break;
            default:
                fondo = "#f1f5f9";
                texto = "#475569";
                break;
        }
        return "-fx-background-color: " + fondo + "; -fx-text-fill: " + texto + ";"
                + "-fx-font-size: 10.5px; -fx-font-weight: bold;"
                + "-fx-background-radius: 12; -fx-padding: 3 10 3 10;";
    }

    // ══════════════════════════════════════════════════════════════
    // FILTROS
    // ══════════════════════════════════════════════════════════════
    private void configurarFiltros() {
        fEquipos = new FilteredList<>(equipos, e -> true);
        SortedList<Equipo> sEq = new SortedList<>(fEquipos);
        sEq.comparatorProperty().bind(tablaEquipos.comparatorProperty());
        tablaEquipos.setItems(sEq);

        fMantenimientos = new FilteredList<>(mantenimientos, m -> true);
        SortedList<RegistroMantenimiento> sMt = new SortedList<>(fMantenimientos);
        sMt.comparatorProperty().bind(tablaMantenimientos.comparatorProperty());
        tablaMantenimientos.setItems(sMt);

        fActualizaciones = new FilteredList<>(actualizaciones, a -> true);
        SortedList<RegistroActualizacion> sAc = new SortedList<>(fActualizaciones);
        sAc.comparatorProperty().bind(tablaActualizaciones.comparatorProperty());
        tablaActualizaciones.setItems(sAc);

        fProgramas = new FilteredList<>(programas, p -> true);
        SortedList<EquipoPrograma> sPg = new SortedList<>(fProgramas);
        sPg.comparatorProperty().bind(tablaProgramas.comparatorProperty());
        tablaProgramas.setItems(sPg);

        fHistorial = new FilteredList<>(historial, h -> true);
        SortedList<HistorialRegistro> sHs = new SortedList<>(fHistorial);
        sHs.comparatorProperty().bind(tablaHistorial.comparatorProperty());
        tablaHistorial.setItems(sHs);

        ObservableList<String> lugares = FXCollections.observableArrayList("Todos los lugares");
        equipos.forEach(e -> {
            if (!lugares.contains(e.getLugar()))
                lugares.add(e.getLugar());
        });
        cmbLugar.setItems(lugares);
        cmbLugar.getSelectionModel().selectFirst();

        cmbEstadoEquipo.setItems(FXCollections.observableArrayList(
                "Todos los estados", "Activo", "En mantenimiento", "Inactivo", "De baja"));
        cmbEstadoEquipo.getSelectionModel().selectFirst();

        ObservableList<String> tiposMant = FXCollections.observableArrayList("Todos los tipos");
        tiposMant.addAll(RegistroMantenimiento.TIPOS);
        cmbTipoMant.setItems(tiposMant);
        cmbTipoMant.getSelectionModel().selectFirst();

        ObservableList<String> tiposAct = FXCollections.observableArrayList("Todos los tipos");
        tiposAct.addAll(RegistroActualizacion.TIPOS);
        cmbTipoAct.setItems(tiposAct);
        cmbTipoAct.getSelectionModel().selectFirst();
    }

    @FXML
    private void filtrarEquipos(Event e) {
        String q = texto(txtBuscarEquipo);
        String lugar = cmbLugar.getValue();
        String estado = cmbEstadoEquipo.getValue();

        fEquipos.setPredicate(eq -> {
            if (!q.isEmpty() && !eq.textoBusqueda().contains(q))
                return false;
            if (lugar != null && !lugar.startsWith("Todos") && !lugar.equals(eq.getLugar()))
                return false;
            if (estado != null && !estado.startsWith("Todos")
                    && !estado.equalsIgnoreCase(eq.getEstado()))
                return false;
            return true;
        });
        lblConteoEquipos.setText(fEquipos.size() + " de " + equipos.size() + " equipos");
    }

    @FXML
    private void filtrarMantenimientos(Event e) {
        aplicarFiltroMant();
    }

    private void aplicarFiltroMant() {
        String q = texto(txtBuscarMant);
        String tipo = cmbTipoMant.getValue();

        fMantenimientos.setPredicate(m -> {
            if (!q.isEmpty() && !m.textoBusqueda().contains(q))
                return false;
            if (tipo != null && !tipo.startsWith("Todos") && !tipo.equals(m.getTipo()))
                return false;
            if ("VENCIDOS".equals(estadoMantSeleccionado))
                return m.estaVencido();
            if (!"TODOS".equals(estadoMantSeleccionado)
                    && !estadoMantSeleccionado.equalsIgnoreCase(m.getEstado()))
                return false;
            return true;
        });
        lblConteoMant.setText(fMantenimientos.size() + " de " + mantenimientos.size() + " registros");
    }

    @FXML
    private void filtrarPorEstadoMant(Event e) {
        Node origen = (Node) e.getSource();
        if (origen == chipMantTodos)
            estadoMantSeleccionado = "TODOS";
        else if (origen == chipPendiente)
            estadoMantSeleccionado = "Pendiente";
        else if (origen == chipEnProceso)
            estadoMantSeleccionado = "En proceso";
        else if (origen == chipCompletado)
            estadoMantSeleccionado = "Completado";
        else if (origen == chipVencidos)
            estadoMantSeleccionado = "VENCIDOS";

        resaltarChipMant(origen);
        aplicarFiltroMant();
    }

    private void resaltarChipMant(Node activo) {
        chipMantTodos.setStyle(
                (activo == chipMantTodos
                        ? "-fx-background-color: #0f172a; -fx-text-fill: white;"
                        : "-fx-background-color: #f4f6fa; -fx-text-fill: #475569;")
                        + "-fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 16;"
                        + "-fx-padding: 5 13 5 13; -fx-cursor: hand;");

        estiloChip(chipPendiente, activo == chipPendiente, "#fffbeb", "#fde68a", "#f59e0b");
        estiloChip(chipEnProceso, activo == chipEnProceso, "#eff6ff", "#bfdbfe", "#3b82f6");
        estiloChip(chipCompletado, activo == chipCompletado, "#f0fdf4", "#bbf7d0", "#16a34a");
        estiloChip(chipVencidos, activo == chipVencidos, "#fef2f2", "#fecaca", "#dc2626");
    }

    private void estiloChip(HBox chip, boolean activo, String fondo, String borde, String acento) {
        chip.setStyle("-fx-background-color: " + fondo + "; -fx-background-radius: 16;"
                + "-fx-border-color: " + (activo ? acento : borde) + ";"
                + "-fx-border-width: " + (activo ? "2" : "1") + "; -fx-border-radius: 16;"
                + "-fx-padding: 5 12 5 12; -fx-cursor: hand;");
    }

    @FXML
    private void filtrarActualizaciones(Event e) {
        String q = texto(txtBuscarAct);
        String tipo = cmbTipoAct.getValue();
        fActualizaciones.setPredicate(a -> {
            if (!q.isEmpty() && !a.textoBusqueda().contains(q))
                return false;
            if (tipo != null && !tipo.startsWith("Todos") && !tipo.equals(a.getTipo()))
                return false;
            return true;
        });
        lblConteoAct.setText(fActualizaciones.size() + " de " + actualizaciones.size() + " registros");
    }

    @FXML
    private void filtrarProgramas(Event e) {
        String q = texto(txtBuscarProg);
        fProgramas.setPredicate(p -> q.isEmpty() || p.textoBusqueda().contains(q));
        lblConteoProg.setText(fProgramas.size() + " de " + programas.size() + " programas");
    }

    @FXML
    private void filtrarHistorial(Event e) {
        String q = texto(txtBuscarHist);
        fHistorial.setPredicate(h -> q.isEmpty()
                || (h.getEquipoNombre() + " " + h.getTipo() + " " + h.getDescripcion())
                        .toLowerCase().contains(q));
        lblConteoHist.setText(fHistorial.size() + " de " + historial.size() + " movimientos");
    }

    // ══════════════════════════════════════════════════════════════
    // EQUIPOS
    // ══════════════════════════════════════════════════════════════
    @FXML
    private void verFichaEquipo() {
        Equipo eq = tablaEquipos.getSelectionModel().getSelectedItem();
        if (eq == null) {
            aviso("Selecciona un equipo.");
            return;
        }
        mostrarFicha(eq);
    }

    private void mostrarFicha(Equipo eq) {
        long mant = mantenimientos.stream().filter(m -> m.getIdEquipo() == eq.getId()).count();
        long act = actualizaciones.stream().filter(a -> a.getIdEquipo() == eq.getId()).count();
        long prog = programas.stream().filter(p -> p.getIdEquipo() == eq.getId()).count();

        String ficha = "id                     : " + eq.getId() + "\n"
                + "equipos                : " + eq.getEquipos() + "\n"
                + "modelo                 : " + eq.getModelo() + "\n"
                + "procesador             : " + eq.getProcesador() + "\n"
                + "memoria_ram            : " + eq.getMemoriaRam() + "\n"
                + "almacenamiento         : " + eq.getAlmacenamiento() + "\n"
                + "id_sistema_operativo   : " + eq.getIdSistemaOperativo() + " (" + eq.getSistemaOperativo() + ")\n"
                + "lugar                  : " + eq.getLugar() + "\n"
                + "estado                 : " + eq.getEstado() + "\n"
                + "anio_creacion          : " + eq.getAnioCreacion() + "\n"
                + "id_usuario_responsable : " + eq.getIdUsuarioResponsable() + " (" + eq.getUsuarioResponsable() + ")\n"
                + "fecha_creacion         : " + f(eq.getFechaCreacion()) + "\n"
                + "fecha_actualizacion    : " + f(eq.getFechaActualizacion()) + "\n"
                + "-------------------------------------------\n"
                + "mantenimientos         : " + mant + "\n"
                + "actualizaciones        : " + act + "\n"
                + "programas instalados   : " + prog;

        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Ficha tecnica");
        a.setHeaderText(eq.getEquipos() + "  -  " + eq.getModelo());
        TextArea area = new TextArea(ficha);
        area.setEditable(false);
        area.setPrefRowCount(17);
        area.setStyle("-fx-font-family: 'monospaced'; -fx-font-size: 12px;");
        a.getDialogPane().setContent(area);
        a.getDialogPane().setPrefWidth(520);
        a.showAndWait();
    }

    @FXML
    private void cambiarEstadoEquipo() {
        Equipo eq = tablaEquipos.getSelectionModel().getSelectedItem();
        if (eq == null) {
            aviso("Selecciona un equipo.");
            return;
        }

        ChoiceDialog<String> d = new ChoiceDialog<>(eq.getEstado(),
                "Activo", "En mantenimiento", "Inactivo", "De baja");
        d.setTitle("Cambiar estado");
        d.setHeaderText(eq.getEquipos() + " - estado actual: " + eq.getEstado());
        d.setContentText("Nuevo estado:");

        Optional<String> r = d.showAndWait();
        if (!r.isPresent() || r.get().equals(eq.getEstado()))
            return;

        String anterior = eq.getEstado();
        eq.setEstado(r.get());
        eq.setFechaActualizacion(LocalDate.now());
        // TODO: UPDATE equipos SET estado = ?, fecha_actualizacion = ? WHERE id = ?

        agregarHistorial(eq, "Equipo", 0, 0,
                "Estado cambiado de \"" + anterior + "\" a \"" + r.get() + "\"");
        tablaEquipos.refresh();
        refrescarTodo();
    }

    // ══════════════════════════════════════════════════════════════
    // MANTENIMIENTOS (CREAR / EDITAR)
    // ══════════════════════════════════════════════════════════════
    @FXML
    private void nuevoMantenimiento() {
        abrirDialogoMantenimiento(null, tablaEquipos.getSelectionModel().getSelectedItem(), null);
    }

    @FXML
    private void editarMantenimiento() {
        RegistroMantenimiento r = tablaMantenimientos.getSelectionModel().getSelectedItem();
        if (r == null) {
            aviso("Selecciona el registro que quieres editar.");
            return;
        }
        abrirDialogoMantenimiento(r, null, null);
    }

    private void abrirDialogoMantenimiento(RegistroMantenimiento existente,
            Equipo equipoPre, String motivoPre) {
        try {
            FXMLLoader loader = cargarVista("DialogoMantenimiento.fxml");
            Node contenido = loader.load();
            DialogoMantenimientoController ctrl = loader.getController();
            ctrl.configurar(equipos, idTecnicoSesion, nombreTecnicoSesion);

            if (existente != null)
                ctrl.cargarRegistro(existente, equipos);
            else {
                if (equipoPre != null)
                    ctrl.preseleccionarEquipo(equipoPre);
                if (motivoPre != null)
                    ctrl.precargarMotivo(motivoPre);
            }

            Dialog<ButtonType> dialogo = new Dialog<>();
            dialogo.setTitle(existente == null ? "Registrar mantenimiento" : "Editar mantenimiento");
            dialogo.getDialogPane().setContent(contenido);
            dialogo.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
            estilizarBotones(dialogo, "Guardar");

            // no dejar cerrar si el formulario no pasa la validacion
            Button ok = (Button) dialogo.getDialogPane().lookupButton(ButtonType.OK);
            ok.addEventFilter(ActionEvent.ACTION, ev -> {
                if (!ctrl.validar())
                    ev.consume();
            });

            if (dialogo.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK)
                return;

            boolean esNuevo = !ctrl.esEdicion();
            RegistroMantenimiento r = ctrl.obtenerRegistro();

            if (esNuevo) {
                r.setId(siguienteId(mantenimientos.stream()
                        .mapToInt(RegistroMantenimiento::getId).max().orElse(0)));
                mantenimientos.add(r);
                // TODO: INSERT INTO registros_mantenimiento (...)
                agregarHistorial(buscarEquipo(r.getIdEquipo()), "Mantenimiento", r.getId(), 0,
                        "Alta de mantenimiento " + r.getTipo() + ": " + r.getMotivo());
            } else {
                tablaMantenimientos.refresh();
                // TODO: UPDATE registros_mantenimiento SET ... WHERE id = ?
                agregarHistorial(buscarEquipo(r.getIdEquipo()), "Mantenimiento", r.getId(), 0,
                        "Edicion del mantenimiento (estado: " + r.getEstado() + ")");
            }

            // si se atiende el equipo, se refleja en la tabla equipos
            Equipo eq = buscarEquipo(r.getIdEquipo());
            if (eq != null) {
                if ("Pendiente".equalsIgnoreCase(r.getEstado())
                        || "En proceso".equalsIgnoreCase(r.getEstado())) {
                    eq.setEstado("En mantenimiento");
                } else if ("Completado".equalsIgnoreCase(r.getEstado())
                        && "En mantenimiento".equalsIgnoreCase(eq.getEstado())) {
                    eq.setEstado("Activo");
                }
                eq.setFechaActualizacion(LocalDate.now());
                tablaEquipos.refresh();
            }

            refrescarTodo();
            tabs.getSelectionModel().select(1);
            tablaMantenimientos.getSelectionModel().select(r);

        } catch (IOException ex) {
            error("No se pudo abrir el formulario de mantenimiento:\n\n" + ex.getMessage());
        }
    }

    private FXMLLoader cargarVista(String nombreArchivo) throws IOException {
        String[] rutas = {
                // ── rutas reales de este proyecto ──
                "/com/utng/ui/tecnicoModule/pantallaDialogoMantenimineto/" + nombreArchivo,
                "/com/utng/ui/tecnicoModule/pantallaDialogoActualizacion/" + nombreArchivo,
                "/com/utng/ui/tecnicoModule/" + nombreArchivo,
                // ── respaldos por si mueves los archivos ──
                "/com/utng/TecnicoModule/view/" + nombreArchivo,
                "/com/utng/TecnicoModule/" + nombreArchivo,
                "/com/utng/view/" + nombreArchivo,
                "/view/" + nombreArchivo,
                "/fxml/" + nombreArchivo,
                "/" + nombreArchivo,
                nombreArchivo // misma carpeta que este controlador
        };

        for (String ruta : rutas) {
            java.net.URL url = getClass().getResource(ruta);
            if (url != null) {
                return new FXMLLoader(url);
            }
        }

        throw new IOException(
                "No se encontro " + nombreArchivo + " en el classpath.\n\n"
                        + "Rutas probadas:\n  " + String.join("\n  ", rutas)
                        + "\n\nCopia el archivo a la misma carpeta donde tienes "
                        + "PantallaTecnico.fxml y vuelve a compilar el proyecto.");
    }

    @FXML
    private void completarMantenimiento() {
        RegistroMantenimiento r = tablaMantenimientos.getSelectionModel().getSelectedItem();
        if (r == null) {
            aviso("Selecciona el mantenimiento a cerrar.");
            return;
        }
        if ("Completado".equalsIgnoreCase(r.getEstado())) {
            aviso("Ese registro ya esta completado.");
            return;
        }

        TextInputDialog d = new TextInputDialog(r.getMantenimientoRealizado());
        d.setTitle("Cerrar mantenimiento");
        d.setHeaderText(r.getEquipoNombre() + " - " + r.getTipo());
        d.setContentText("mantenimiento_realizado:");
        d.getDialogPane().setPrefWidth(460);

        Optional<String> res = d.showAndWait();
        if (!res.isPresent() || res.get().trim().isEmpty()) {
            if (res.isPresent())
                aviso("Hay que describir que se hizo para poder cerrarlo.");
            return;
        }

        r.setMantenimientoRealizado(res.get().trim());
        r.setEstado("Completado");
        // TODO: UPDATE registros_mantenimiento SET estado, mantenimiento_realizado
        // WHERE id = ?

        Equipo eq = buscarEquipo(r.getIdEquipo());
        if (eq != null && "En mantenimiento".equalsIgnoreCase(eq.getEstado())) {
            eq.setEstado("Activo");
            eq.setFechaActualizacion(LocalDate.now());
            tablaEquipos.refresh();
        }

        agregarHistorial(eq, "Mantenimiento", r.getId(), 0,
                "Mantenimiento completado: " + r.getMantenimientoRealizado());
        tablaMantenimientos.refresh();
        refrescarTodo();
    }

    // ══════════════════════════════════════════════════════════════
    // ACTUALIZACIONES (CREAR / EDITAR)
    // ══════════════════════════════════════════════════════════════
    @FXML
    private void nuevaActualizacion() {
        abrirDialogoActualizacion(null, null);
    }

    @FXML
    private void editarActualizacion() {
        RegistroActualizacion r = tablaActualizaciones.getSelectionModel().getSelectedItem();
        if (r == null) {
            aviso("Selecciona la actualizacion que quieres editar.");
            return;
        }
        abrirDialogoActualizacion(r, null);
    }

    @FXML
    private void actualizarPrograma() {
        EquipoPrograma p = tablaProgramas.getSelectionModel().getSelectedItem();
        if (p == null) {
            aviso("Selecciona el programa instalado que actualizaste.");
            return;
        }
        abrirDialogoActualizacion(null, p);
    }

    private void abrirDialogoActualizacion(RegistroActualizacion existente, EquipoPrograma programaPre) {
        try {
            FXMLLoader loader = cargarVista("DialogoActualizacion.fxml");
            Node contenido = loader.load();
            DialogoActualizacionController ctrl = loader.getController();
            ctrl.configurar(equipos, idTecnicoSesion, nombreTecnicoSesion);

            if (existente != null) {
                ctrl.cargarRegistro(existente, equipos);
            } else if (programaPre != null) {
                ctrl.precargarPrograma(buscarEquipo(programaPre.getIdEquipo()),
                        programaPre.getNombrePrograma(), programaPre.getVersionActual());
            }

            Dialog<ButtonType> dialogo = new Dialog<>();
            dialogo.setTitle(existente == null ? "Registrar actualizacion" : "Editar actualizacion");
            dialogo.getDialogPane().setContent(contenido);
            dialogo.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
            estilizarBotones(dialogo, "Guardar");

            Button ok = (Button) dialogo.getDialogPane().lookupButton(ButtonType.OK);
            ok.addEventFilter(ActionEvent.ACTION, ev -> {
                if (!ctrl.validar())
                    ev.consume();
            });

            if (dialogo.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK)
                return;

            boolean esNuevo = !ctrl.esEdicion();
            RegistroActualizacion r = ctrl.obtenerRegistro();

            if (esNuevo) {
                r.setId(siguienteId(actualizaciones.stream()
                        .mapToInt(RegistroActualizacion::getId).max().orElse(0)));
                actualizaciones.add(r);
                // TODO: INSERT INTO registros_actualizaciones (...)
                agregarHistorial(buscarEquipo(r.getIdEquipo()), "Actualizacion", 0, r.getId(),
                        r.getNombreActualizado() + ": " + r.getVersionActual()
                                + " -> " + r.getVersionActualizada());
            } else {
                tablaActualizaciones.refresh();
                // TODO: UPDATE registros_actualizaciones SET ... WHERE id = ?
                agregarHistorial(buscarEquipo(r.getIdEquipo()), "Actualizacion", 0, r.getId(),
                        "Edicion de la actualizacion de " + r.getNombreActualizado());
            }

            // reflejar la nueva version en equipos_programas
            if ("Programa".equalsIgnoreCase(r.getTipo())) {
                programas.stream()
                        .filter(p -> p.getIdEquipo() == r.getIdEquipo()
                                && p.getNombrePrograma().equalsIgnoreCase(r.getNombreActualizado()))
                        .forEach(p -> p.setVersionActual(r.getVersionActualizada()));
                tablaProgramas.refresh();
            }

            Equipo eq = buscarEquipo(r.getIdEquipo());
            if (eq != null) {
                if ("Sistema operativo".equalsIgnoreCase(r.getTipo())) {
                    eq.setSistemaOperativo(r.getNombreActualizado());
                }
                eq.setFechaActualizacion(LocalDate.now());
                tablaEquipos.refresh();
            }

            refrescarTodo();
            tabs.getSelectionModel().select(2);
            tablaActualizaciones.getSelectionModel().select(r);

        } catch (IOException ex) {
            error("No se pudo abrir el formulario de actualizacion:\n\n" + ex.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════
    // HISTORIAL
    // ══════════════════════════════════════════════════════════════
    private void agregarHistorial(Equipo eq, String tipo, int idMant, int idAct, String descripcion) {
        int nuevoId = siguienteId(historial.stream().mapToInt(HistorialRegistro::getId).max().orElse(0));
        historial.add(0, new HistorialRegistro(
                nuevoId,
                eq == null ? 0 : eq.getId(),
                eq == null ? "-" : eq.getEquipos(),
                tipo, idMant, idAct, descripcion, LocalDate.now()));
        // TODO: INSERT INTO historial_registros (...)
    }

    @FXML
    private void exportarHistorial() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Exportar historial");
        fc.setInitialFileName("historial_registros.csv");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        File destino = fc.showSaveDialog(tablaHistorial.getScene().getWindow());
        if (destino == null)
            return;

        try (BufferedWriter w = new BufferedWriter(new FileWriter(destino))) {
            w.write("id,fecha,tipo,id_equipo,equipo,id_registro_mantenimiento,"
                    + "id_registro_actualizacion,descripcion");
            w.newLine();
            for (HistorialRegistro h : fHistorial) {
                w.write(String.join(",",
                        String.valueOf(h.getId()), f(h.getFecha()), csv(h.getTipo()),
                        String.valueOf(h.getIdEquipo()), csv(h.getEquipoNombre()),
                        String.valueOf(h.getIdRegistroMantenimiento()),
                        String.valueOf(h.getIdRegistroActualizacion()),
                        csv(h.getDescripcion())));
                w.newLine();
            }
            aviso("Se exportaron " + fHistorial.size() + " movimientos.");
        } catch (IOException ex) {
            error("No se pudo exportar: " + ex.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════
    // CHAT CON CONSULTORES
    // ══════════════════════════════════════════════════════════════
    private void configurarListaConsultores() {
        fConsultores = new FilteredList<>(consultores, c -> true);
        listaConsultores.setItems(fConsultores);

        listaConsultores.setCellFactory(lv -> new ListCell<UsuarioChat>() {
            @Override
            protected void updateItem(UsuarioChat u, boolean vacio) {
                super.updateItem(u, vacio);
                if (vacio || u == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                Label avatar = new Label(u.getIniciales());
                avatar.setStyle("-fx-font-size: 10.5px; -fx-font-weight: bold; -fx-text-fill: white;"
                        + "-fx-background-color: " + (u.isEnLinea() ? "#6366f1" : "#94a3b8") + ";"
                        + "-fx-background-radius: 14; -fx-min-width: 28; -fx-min-height: 28;"
                        + "-fx-alignment: center;");

                Label nombre = new Label(u.getNombreCompleto());
                nombre.setStyle("-fx-font-size: 11.5px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
                Label sub = new Label(u.getRol() + " - " + u.getLugar());
                sub.setStyle("-fx-font-size: 10px; -fx-text-fill: #94a3b8;");
                VBox datos = new VBox(1, nombre, sub);

                Region sep = new Region();
                HBox.setHgrow(sep, Priority.ALWAYS);

                HBox fila = new HBox(9, avatar, datos, sep);
                if (u.getNoLeidos() > 0) {
                    Label badge = new Label(String.valueOf(u.getNoLeidos()));
                    badge.setStyle("-fx-text-fill: white; -fx-font-size: 9.5px; -fx-font-weight: bold;"
                            + "-fx-background-color: #dc2626; -fx-background-radius: 9;"
                            + "-fx-min-width: 18; -fx-alignment: center; -fx-padding: 2 6 2 6;");
                    fila.getChildren().add(badge);
                } else {
                    Label est = new Label(u.isEnLinea() ? "En linea" : "Ausente");
                    est.setStyle("-fx-font-size: 9px; -fx-font-weight: bold;"
                            + (u.isEnLinea()
                                    ? "-fx-text-fill: #16a34a; -fx-background-color: #f0fdf4;"
                                    : "-fx-text-fill: #64748b; -fx-background-color: #f1f5f9;")
                            + "-fx-background-radius: 10; -fx-padding: 2 7 2 7;");
                    fila.getChildren().add(est);
                }
                fila.setAlignment(Pos.CENTER_LEFT);
                fila.setPadding(new Insets(7, 8, 7, 8));

                setGraphic(fila);
                setText(null);
                setStyle("-fx-background-color: transparent;");
            }
        });

        listaConsultores.getSelectionModel().selectedItemProperty()
                .addListener((o, ant, nuevo) -> abrirConversacion(nuevo));
    }

    @FXML
    private void filtrarConsultores(Event e) {
        String q = texto(txtBuscarConsultor);
        fConsultores.setPredicate(u -> q.isEmpty()
                || u.getNombreCompleto().toLowerCase().contains(q)
                || u.getLugar().toLowerCase().contains(q));
        lblTotalConsultores.setText(String.valueOf(fConsultores.size()));
    }

    private void abrirConversacion(UsuarioChat u) {
        consultorActual = u;
        contenedorChat.getChildren().clear();

        if (u == null) {
            lblAvatarConsultor.setText("--");
            lblNombreConsultor.setText("Selecciona un consultor");
            lblRolConsultor.setText("Aqui llegan los reportes de equipos");
            txtMensaje.setDisable(true);
            btnEnviar.setDisable(true);
            btnCrearDesdeChat.setDisable(true);
            return;
        }

        u.setNoLeidos(0);
        listaConsultores.refresh();

        lblAvatarConsultor.setText(u.getIniciales());
        lblNombreConsultor.setText(u.getNombreCompleto());
        lblRolConsultor.setText(u.getRol() + " - " + u.getLugar());
        lblEstadoConexion.setText(u.isEnLinea() ? "En linea" : "Ausente");

        txtMensaje.setDisable(false);
        btnEnviar.setDisable(false);
        btnCrearDesdeChat.setDisable(false);

        if (u.getConversacion().isEmpty()) {
            pintarSistema("Sin mensajes con " + u.getNombreCompleto() + " todavia.");
        } else {
            u.getConversacion().forEach(m -> pintarBurbuja(m.getTexto(), m.getHora(), m.isMio()));
        }
        bajarScroll();
    }

    @FXML
    private void enviarMensaje() {
        if (consultorActual == null) {
            aviso("Selecciona un consultor.");
            return;
        }
        String t = texto(txtMensaje, false);
        if (t.isEmpty())
            return;

        String hora = LocalTime.now().format(HORA);
        consultorActual.agregarMensaje(new UsuarioChat.Mensaje(t, hora, true));
        pintarBurbuja(t, hora, true);
        txtMensaje.clear();
        bajarScroll();

        // acuse simulado: sustituye por tu mensajeria real
        PauseTransition p = new PauseTransition(Duration.seconds(1.1));
        UsuarioChat destino = consultorActual;
        p.setOnFinished(ev -> {
            String h = LocalTime.now().format(HORA);
            String resp = "Gracias, quedo enterado.";
            destino.agregarMensaje(new UsuarioChat.Mensaje(resp, h, false));
            if (destino == consultorActual) {
                pintarBurbuja(resp, h, false);
                bajarScroll();
            }
        });
        p.play();
    }

    /**
     * Toma el ultimo reporte del consultor y abre el alta de mantenimiento con el
     * motivo ya puesto.
     */
    @FXML
    private void mantenimientoDesdeChat() {
        if (consultorActual == null) {
            aviso("Selecciona un consultor.");
            return;
        }

        String ultimoReporte = null;
        for (int i = consultorActual.getConversacion().size() - 1; i >= 0; i--) {
            UsuarioChat.Mensaje m = consultorActual.getConversacion().get(i);
            if (!m.isMio()) {
                ultimoReporte = m.getTexto();
                break;
            }
        }
        if (ultimoReporte == null) {
            aviso("Este consultor todavia no manda ningun reporte.");
            return;
        }

        // El consultor antepone [Equipo #id - nombre | lugar] al reportar
        Equipo equipoDetectado = null;
        int ini = ultimoReporte.indexOf("[Equipo #");
        if (ini >= 0) {
            int fin = ultimoReporte.indexOf(' ', ini + 9);
            if (fin > ini) {
                try {
                    equipoDetectado = buscarEquipo(
                            Integer.parseInt(ultimoReporte.substring(ini + 9, fin).trim()));
                } catch (NumberFormatException ignore) {
                    /* sin id en el mensaje */ }
            }
        }

        String motivo = "Reporte de " + consultorActual.getNombreCompleto() + ": " + ultimoReporte;
        abrirDialogoMantenimiento(null, equipoDetectado, motivo);
    }

    @FXML
    private void usarSugerencia(ActionEvent e) {
        Button b = (Button) e.getSource();
        txtMensaje.setText(b.getText());
        txtMensaje.requestFocus();
        txtMensaje.positionCaret(txtMensaje.getText().length());
    }

    @FXML
    private void limpiarChat() {
        contenedorChat.getChildren().clear();
        if (consultorActual != null) {
            consultorActual.limpiarConversacion();
            pintarSistema("Conversacion limpiada.");
        }
    }

    private void pintarBurbuja(String texto, String hora, boolean mio) {
        Label cuerpo = new Label(texto);
        cuerpo.setWrapText(true);
        cuerpo.setMaxWidth(430);
        cuerpo.setStyle(mio
                ? "-fx-background-color: linear-gradient(to bottom right, #8b5cf6, #6366f1);"
                        + "-fx-text-fill: white; -fx-font-size: 11.5px;"
                        + "-fx-background-radius: 12 12 2 12; -fx-padding: 9 12 9 12;"
                : "-fx-background-color: white; -fx-text-fill: #0f172a; -fx-font-size: 11.5px;"
                        + "-fx-background-radius: 12 12 12 2; -fx-padding: 9 12 9 12;"
                        + "-fx-border-color: #e8ecf2; -fx-border-radius: 12 12 12 2;");

        Label sello = new Label(hora);
        sello.setStyle("-fx-font-size: 9px; -fx-text-fill: #94a3b8;");

        VBox b = new VBox(3, cuerpo, sello);
        b.setAlignment(mio ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        HBox fila = new HBox(b);
        fila.setAlignment(mio ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        contenedorChat.getChildren().add(fila);
    }

    private void pintarSistema(String texto) {
        Label l = new Label(texto);
        l.setWrapText(true);
        l.setMaxWidth(450);
        l.setStyle("-fx-font-size: 10.5px; -fx-text-fill: #64748b;"
                + "-fx-background-color: #eef2ff; -fx-background-radius: 10;"
                + "-fx-padding: 8 12 8 12;");
        HBox fila = new HBox(l);
        fila.setAlignment(Pos.CENTER);
        contenedorChat.getChildren().add(fila);
    }

    private void bajarScroll() {
        Platform.runLater(() -> scrollChat.setVvalue(1.0));
    }

    // ══════════════════════════════════════════════════════════════
    // MENU LATERAL
    // ══════════════════════════════════════════════════════════════
    @FXML
    private void toggleMenu(Event e) {
        boolean abierto = panelMenu.isVisible();
        panelMenu.setVisible(!abierto);
        panelMenu.setManaged(!abierto);
        overlayMenu.setVisible(!abierto);
        overlayMenu.setManaged(!abierto);
    }

    @FXML
    private void irAEquipos(ActionEvent e) {
        irATab(e, 0);
    }

    @FXML
    private void irAMantenimientos(ActionEvent e) {
        irATab(e, 1);
    }

    @FXML
    private void irAActualizaciones(ActionEvent e) {
        irATab(e, 2);
    }

    @FXML
    private void irAProgramas(ActionEvent e) {
        irATab(e, 3);
    }

    @FXML
    private void irAHistorial(ActionEvent e) {
        irATab(e, 4);
    }

    private void irATab(ActionEvent e, int indice) {
        toggleMenu(e);
        tabs.getSelectionModel().select(indice);
    }

    @FXML
    private void irAChat(ActionEvent e) {
        toggleMenu(e);
        listaConsultores.requestFocus();
    }

    @FXML
    private void cerrarSesion(ActionEvent e) {
        // TODO: enlaza aqui tu navegacion a la pantalla de login
        aviso("Sesion cerrada (conecta aqui tu pantalla de login).");
    }

    // ══════════════════════════════════════════════════════════════
    // UTILIDADES
    // ══════════════════════════════════════════════════════════════
    private void refrescarTodo() {
        long pend = mantenimientos.stream()
                .filter(m -> "Pendiente".equalsIgnoreCase(m.getEstado())
                        || "En proceso".equalsIgnoreCase(m.getEstado()))
                .count();
        long venc = mantenimientos.stream().filter(RegistroMantenimiento::estaVencido).count();
        long actMes = actualizaciones.stream()
                .filter(a -> a.getFecha() != null
                        && a.getFecha().getMonth() == LocalDate.now().getMonth()
                        && a.getFecha().getYear() == LocalDate.now().getYear())
                .count();

        lblTotalEquipos.setText(String.valueOf(equipos.size()));
        lblPendientes.setText(String.valueOf(pend));
        lblVencidos.setText(String.valueOf(venc));
        lblActualizacionesMes.setText(String.valueOf(actMes));

        lblMantPendiente.setText(String.valueOf(mantenimientos.stream()
                .filter(m -> "Pendiente".equalsIgnoreCase(m.getEstado())).count()));
        lblMantProceso.setText(String.valueOf(mantenimientos.stream()
                .filter(m -> "En proceso".equalsIgnoreCase(m.getEstado())).count()));
        lblMantCompletado.setText(String.valueOf(mantenimientos.stream()
                .filter(m -> "Completado".equalsIgnoreCase(m.getEstado())).count()));
        lblMantVencidos.setText(String.valueOf(venc));

        lblConteoEquipos.setText(fEquipos.size() + " de " + equipos.size() + " equipos");
        lblConteoMant.setText(fMantenimientos.size() + " de " + mantenimientos.size() + " registros");
        lblConteoAct.setText(fActualizaciones.size() + " de " + actualizaciones.size() + " registros");
        lblConteoProg.setText(fProgramas.size() + " de " + programas.size() + " programas");
        lblConteoHist.setText(fHistorial.size() + " de " + historial.size() + " movimientos");
        lblTotalConsultores.setText(String.valueOf(fConsultores.size()));
    }

    private Equipo buscarEquipo(int id) {
        return equipos.stream().filter(e -> e.getId() == id).findFirst().orElse(null);
    }

    private int siguienteId(int maximo) {
        return maximo + 1;
    }

    private SimpleStringProperty txt(Object v) {
        return new SimpleStringProperty(v == null ? "" : String.valueOf(v));
    }

    private String f(LocalDate d) {
        return d == null ? "" : d.format(FECHA);
    }

    private String texto(TextField t) {
        return texto(t, true);
    }

    private String texto(TextField t, boolean minusculas) {
        String v = t.getText() == null ? "" : t.getText().trim();
        return minusculas ? v.toLowerCase() : v;
    }

    private String csv(String v) {
        if (v == null)
            return "";
        return v.contains(",") ? "\"" + v.replace("\"", "\"\"") + "\"" : v;
    }

    private String iniciales(String nombre) {
        String[] p = nombre.trim().split("\\s+");
        if (p.length == 1)
            return p[0].substring(0, Math.min(2, p[0].length())).toUpperCase();
        return ("" + p[0].charAt(0) + p[1].charAt(0)).toUpperCase();
    }

    private void estilizarBotones(Dialog<?> d, String textoOk) {
        Button ok = (Button) d.getDialogPane().lookupButton(ButtonType.OK);
        ok.setText(textoOk);
        ok.setStyle("-fx-background-color: linear-gradient(to bottom right, #3b82f6, #1d4ed8);"
                + "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px;"
                + "-fx-background-radius: 8; -fx-padding: 8 18 8 18; -fx-cursor: hand;");

        Button cancelar = (Button) d.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelar.setText("Cancelar");
        cancelar.setStyle("-fx-background-color: #f4f6fa; -fx-text-fill: #64748b;"
                + "-fx-font-weight: bold; -fx-font-size: 12px; -fx-background-radius: 8;"
                + "-fx-border-color: #e2e8f0; -fx-border-radius: 8;"
                + "-fx-padding: 8 16 8 16; -fx-cursor: hand;");
    }

    private void aviso(String m) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, m, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }

    private void error(String m) {
        Alert a = new Alert(Alert.AlertType.ERROR, m, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }

    // ══════════════════════════════════════════════════════════════
    // DATOS DE EJEMPLO -> reemplaza por tus SELECT
    // ══════════════════════════════════════════════════════════════
    private void cargarDatosDemo() {
        equipos.addAll(
                new Equipo(1, "PC-LAB-014", "HP ProDesk 400 G7", "Intel Core i5-10500", "8 GB",
                        "512 GB SSD", 1, "Windows 11 Pro", "Laboratorio A", "Activo", 2021,
                        101, "Gerardo E.", LocalDate.of(2021, 3, 15), LocalDate.of(2026, 8, 10)),
                new Equipo(2, "PC-LAB-015", "Dell OptiPlex 3080", "Intel Core i3-10100", "8 GB",
                        "256 GB SSD", 2, "Windows 10 Pro", "Laboratorio A", "En mantenimiento", 2021,
                        101, "Gerardo E.", LocalDate.of(2021, 3, 15), LocalDate.of(2026, 8, 12)),
                new Equipo(3, "PC-ADM-002", "Lenovo ThinkCentre M70q", "Intel Core i7-11700", "16 GB",
                        "1 TB SSD", 1, "Windows 11 Pro", "Administracion", "Activo", 2022,
                        204, "Marisol R.", LocalDate.of(2022, 7, 1), LocalDate.of(2026, 6, 30)),
                new Equipo(4, "LAP-DOC-009", "Dell Latitude 5420", "Intel Core i5-1135G7", "16 GB",
                        "512 GB SSD", 1, "Windows 11 Pro", "Sala de docentes", "Activo", 2022,
                        310, "Luis M.", LocalDate.of(2022, 9, 12), LocalDate.of(2026, 7, 22)),
                new Equipo(5, "PC-LAB-021", "HP EliteDesk 800 G6", "Intel Core i7-10700", "32 GB",
                        "1 TB SSD", 3, "Ubuntu 22.04 LTS", "Laboratorio B", "Activo", 2023,
                        101, "Gerardo E.", LocalDate.of(2023, 1, 20), LocalDate.of(2026, 8, 1)),
                new Equipo(6, "PC-LAB-022", "HP EliteDesk 800 G6", "Intel Core i7-10700", "16 GB",
                        "512 GB SSD", 3, "Ubuntu 22.04 LTS", "Laboratorio B", "En mantenimiento", 2023,
                        101, "Gerardo E.", LocalDate.of(2023, 1, 20), LocalDate.of(2026, 8, 14)),
                new Equipo(7, "PC-BIB-003", "Acer Veriton X2665G", "Intel Core i3-9100", "4 GB",
                        "1 TB HDD", 2, "Windows 10 Pro", "Biblioteca", "Inactivo", 2019,
                        415, "Ana T.", LocalDate.of(2019, 11, 5), LocalDate.of(2025, 12, 18)),
                new Equipo(8, "PC-BIB-004", "Acer Veriton X2665G", "Intel Core i3-9100", "4 GB",
                        "1 TB HDD", 2, "Windows 10 Pro", "Biblioteca", "De baja", 2019,
                        415, "Ana T.", LocalDate.of(2019, 11, 5), LocalDate.of(2026, 2, 9)),
                new Equipo(9, "LAP-DIR-001", "MacBook Air M2", "Apple M2", "16 GB",
                        "512 GB SSD", 4, "macOS Sonoma", "Direccion", "Activo", 2024,
                        500, "Direccion", LocalDate.of(2024, 2, 28), LocalDate.of(2026, 8, 5)),
                new Equipo(10, "PC-CGTI-007", "Custom Workstation", "AMD Ryzen 7 5800X", "32 GB",
                        "2 TB SSD", 1, "Windows 11 Pro", "CGTI", "Activo", 2024,
                        101, "Gerardo E.", LocalDate.of(2024, 5, 14), LocalDate.of(2026, 8, 15)));

        mantenimientos.addAll(
                new RegistroMantenimiento(1, 2, "PC-LAB-015", "Correctivo",
                        "No enciende, se sospecha de la fuente de poder", LocalDate.of(2026, 8, 12),
                        LocalDate.of(2026, 8, 18), "", "En proceso", 101, "#101 Gerardo Espindola",
                        LocalDate.of(2026, 8, 12)),
                new RegistroMantenimiento(2, 6, "PC-LAB-022", "Preventivo",
                        "Limpieza semestral programada", LocalDate.of(2026, 8, 14),
                        LocalDate.of(2027, 2, 14), "", "Pendiente", 101, "#101 Gerardo Espindola",
                        LocalDate.of(2026, 8, 14)),
                new RegistroMantenimiento(3, 1, "PC-LAB-014", "Preventivo",
                        "Limpieza y cambio de pasta termica", LocalDate.of(2026, 6, 10),
                        LocalDate.of(2026, 12, 10), "Se limpio el disipador y se aplico pasta termica nueva",
                        "Completado", 101, "#101 Gerardo Espindola", LocalDate.of(2026, 6, 10)),
                new RegistroMantenimiento(4, 7, "PC-BIB-003", "Correctivo",
                        "Disco duro con sectores danados", LocalDate.of(2026, 5, 3),
                        LocalDate.of(2026, 7, 3), "", "Pendiente", 101, "#101 Gerardo Espindola",
                        LocalDate.of(2026, 5, 3)),
                new RegistroMantenimiento(5, 3, "PC-ADM-002", "Revision",
                        "El usuario reporta lentitud al abrir archivos", LocalDate.of(2026, 7, 28),
                        LocalDate.of(2026, 10, 28), "Se amplio la memoria virtual y se limpio el arranque",
                        "Completado", 101, "#101 Gerardo Espindola", LocalDate.of(2026, 7, 28)));

        actualizaciones.addAll(
                new RegistroActualizacion(1, 1, "PC-LAB-014", "Sistema operativo", "Windows 11 Pro",
                        "22H2", "23H2", LocalDate.of(2026, 8, 10), 101, "#101 Gerardo Espindola",
                        LocalDate.of(2026, 8, 10)),
                new RegistroActualizacion(2, 5, "PC-LAB-021", "Programa", "LibreOffice",
                        "7.4.2", "24.2.1", LocalDate.of(2026, 8, 4), 101, "#101 Gerardo Espindola",
                        LocalDate.of(2026, 8, 4)),
                new RegistroActualizacion(3, 10, "PC-CGTI-007", "Driver", "NVIDIA Studio Driver",
                        "551.23", "560.94", LocalDate.of(2026, 8, 15), 101, "#101 Gerardo Espindola",
                        LocalDate.of(2026, 8, 15)),
                new RegistroActualizacion(4, 3, "PC-ADM-002", "Parche de seguridad", "KB5041585",
                        "-", "instalado", LocalDate.of(2026, 7, 30), 101, "#101 Gerardo Espindola",
                        LocalDate.of(2026, 7, 30)));

        programas.addAll(
                new EquipoPrograma(1, 1, "PC-LAB-014", 1, "Visual Studio Code", "1.92.0", LocalDate.of(2025, 9, 1)),
                new EquipoPrograma(2, 1, "PC-LAB-014", 2, "NetBeans IDE", "21", LocalDate.of(2025, 9, 1)),
                new EquipoPrograma(3, 5, "PC-LAB-021", 3, "LibreOffice", "24.2.1", LocalDate.of(2026, 8, 4)),
                new EquipoPrograma(4, 5, "PC-LAB-021", 4, "MySQL Workbench", "8.0.36", LocalDate.of(2025, 11, 20)),
                new EquipoPrograma(5, 3, "PC-ADM-002", 5, "Microsoft Office", "2021", LocalDate.of(2022, 7, 5)),
                new EquipoPrograma(6, 10, "PC-CGTI-007", 1, "Visual Studio Code", "1.92.0", LocalDate.of(2024, 5, 20)),
                new EquipoPrograma(7, 10, "PC-CGTI-007", 6, "Docker Desktop", "4.33.1", LocalDate.of(2025, 3, 12)));

        historial.addAll(
                new HistorialRegistro(4, 10, "PC-CGTI-007", "Actualizacion", 0, 3,
                        "NVIDIA Studio Driver: 551.23 -> 560.94", LocalDate.of(2026, 8, 15)),
                new HistorialRegistro(3, 6, "PC-LAB-022", "Mantenimiento", 2, 0,
                        "Alta de mantenimiento Preventivo: Limpieza semestral programada", LocalDate.of(2026, 8, 14)),
                new HistorialRegistro(2, 2, "PC-LAB-015", "Mantenimiento", 1, 0,
                        "Alta de mantenimiento Correctivo: No enciende", LocalDate.of(2026, 8, 12)),
                new HistorialRegistro(1, 1, "PC-LAB-014", "Actualizacion", 0, 1,
                        "Windows 11 Pro: 22H2 -> 23H2", LocalDate.of(2026, 8, 10)));

        UsuarioChat c1 = new UsuarioChat(201, "Marisol Ramos", "Consultor", "Administracion", true);
        c1.agregarMensaje(new UsuarioChat.Mensaje(
                "[Equipo #2 - PC-LAB-015 | Laboratorio A] Este equipo no enciende desde ayer, "
                        + "ya revise el cable.",
                "09:14", false));
        c1.setNoLeidos(1);

        UsuarioChat c2 = new UsuarioChat(202, "Luis Mendoza", "Consultor", "Sala de docentes", true);
        c2.agregarMensaje(new UsuarioChat.Mensaje(
                "[Equipo #4 - LAP-DOC-009 | Sala de docentes] La laptop se desconecta del wifi "
                        + "cada rato.",
                "08:40", false));
        c2.agregarMensaje(new UsuarioChat.Mensaje(
                "Ya la reviso, la reinicio el driver de red al rato.", "08:52", true));

        UsuarioChat c3 = new UsuarioChat(203, "Ana Torres", "Consultor", "Biblioteca", false);
        c3.agregarMensaje(new UsuarioChat.Mensaje(
                "[Equipo #7 - PC-BIB-003 | Biblioteca] Sigue muy lenta, cuando la ven?", "17:05", false));
        c3.setNoLeidos(2);

        UsuarioChat c4 = new UsuarioChat(204, "Carlos Vega", "Consultor", "Laboratorio B", true);

        consultores.addAll(c1, c2, c3, c4);
        lblTotalConsultores.setText(String.valueOf(consultores.size()));
    }
}
