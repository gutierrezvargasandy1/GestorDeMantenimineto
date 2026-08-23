package com.utng.TecnicoModule.controller;

import com.utng.EquipoModule.repository.EquipoRepository;
import com.utng.TecnicoModule.model.Equipo;
import com.utng.TecnicoModule.model.EquipoPrograma;
import com.utng.TecnicoModule.model.HistorialRegistro;
import com.utng.TecnicoModule.model.RegistroActualizacion;
import com.utng.TecnicoModule.model.RegistroMantenimiento;
import com.utng.TecnicoModule.model.UsuarioChat;
import com.utng.TecnicoModule.repository.ActualizacionTecnicoRepository;
import com.utng.TecnicoModule.repository.HistorialTecnicoRepository;
import com.utng.TecnicoModule.repository.MantenimientoTecnicoRepository;
import com.utng.UserModule.UsuarioRepository;
import com.utng.UserModule.model.usuario.TipoUsuario;
import com.utng.UserModule.model.usuario.Usuario;

import com.utng.chatModule.model.Chat;
import com.utng.chatModule.model.Mensaje;
import com.utng.chatModule.service.ChatService;
import com.utng.util.Navigator;
import com.utng.util.SesionManager;

import org.bson.types.ObjectId;

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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Controlador de la pantalla del rol TECNICO.
 *
 * Permisos de este rol:
 * - VER equipos, programas instalados e historial.
 * - CREAR y EDITAR registros_mantenimiento y registros_actualizaciones.
 * - Cambiar el estado operativo de un equipo.
 * - Chatear con los consultores para atender sus reportes (via MongoDB),
 * con contactos reales sacados de la tabla usuarios (rol = consulta).
 * - NO tiene ningun acceso a la tabla usuarios (alta, baja, edicion); del
 * chat solo se leen id, nombre_completo y rol.
 */
public class PantallaTecnicoController {

    // ═══════════ SESION (real, viene del login) ═══════════
    private Long idTecnicoSesion;
    private String nombreTecnicoSesion;
    private final UsuarioRepository usuarioRepository = new UsuarioRepository();
    // junto a usuarioRepository:
    private final MantenimientoTecnicoRepository mantenimientoRepository = new MantenimientoTecnicoRepository();
    private final ActualizacionTecnicoRepository actualizacionRepository = new ActualizacionTecnicoRepository();
    private final HistorialTecnicoRepository historialRepository = new HistorialTecnicoRepository();
    private final EquipoRepository equipoRealRepository = new EquipoRepository();

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
    private final ObservableList<HistorialRegistro> historial = FXCollections.observableArrayList();
    private final ObservableList<UsuarioChat> consultores = FXCollections.observableArrayList();

    private FilteredList<Equipo> fEquipos;
    private FilteredList<RegistroMantenimiento> fMantenimientos;
    private FilteredList<RegistroActualizacion> fActualizaciones;
    private FilteredList<HistorialRegistro> fHistorial;
    private FilteredList<UsuarioChat> fConsultores;

    private String estadoMantSeleccionado = "TODOS";
    private UsuarioChat consultorActual;

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    // ═══════════ CHAT (MongoDB) ═══════════
    private final ChatService chatService = new ChatService();
    private ObjectId miId;
    private ObjectId chatActualId;
    private int mensajesMostrados = 0;

    private final ScheduledExecutorService hiloChat = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "chat-refresco-tecnico");
        t.setDaemon(true);
        return t;
    });

    // ══════════════════════════════════════════════════════════════
    // INICIALIZACION
    // ══════════════════════════════════════════════════════════════
    @FXML
    private void initialize() {
        // ── Sesion real (viene del login) ──
        idTecnicoSesion = SesionManager.getInstance().getIdUsuario();
        nombreTecnicoSesion = SesionManager.getInstance().getNombreCompleto();

        configurarFecha();
        configurarTablaEquipos();
        configurarTablaMantenimientos();
        configurarTablaActualizaciones();
        configurarTablaHistorial();
        configurarListaConsultores();
        cargarEquiposDesdeBD();
        cargarMantenimientosDesdeBD();
        cargarActualizacionesDesdeBD();
        configurarFiltros();
        cargarHistorialDesdeBD();
        refrescarTodo();
        resaltarChipMant(chipMantTodos);

        lblNombreUsuario.setText(nombreTecnicoSesion);
        lblRolUsuario.setText("Técnico CGTI");
        lblAvatarUsuario.setText(iniciales(nombreTecnicoSesion));

        txtMensaje.setDisable(true);
        btnEnviar.setDisable(true);
        btnCrearDesdeChat.setDisable(true);

        // ── Chat real contra Mongo, con id real de sesion ──
        miId = ChatService.idDesdeLong(idTecnicoSesion);
        cargarConsultoresReales();
        hiloChat.scheduleWithFixedDelay(this::refrescarChatsEnSegundoPlano, 3, 3, TimeUnit.SECONDS);
    }

    private void cargarEquiposDesdeBD() {
        equipos.setAll(
                equipoRealRepository.obtenerTodos().stream().map(e -> new Equipo(
                        e.getIdEquipo(),
                        e.getModelo(),
                        e.getModelo(),
                        e.getProcesador() == null ? "-" : e.getProcesador(),
                        e.getMemoriaRam() == null ? "-" : e.getMemoriaRam(),
                        e.getAlmacenamiento() == null ? "-" : e.getAlmacenamiento(),
                        e.getIdSistemaOperativo() == null ? 0 : e.getIdSistemaOperativo().intValue(),
                        "-", // el nombre del SO se resuelve aparte si lo necesitas mostrar
                        e.getLugar() == null ? "-" : e.getLugar(),
                        e.getEstado().getEtiqueta(),
                        // ⚠️ FIX: e.getAnioCreacion() venia como Short (tipo boxed de la entidad de
                        // BD).
                        // Se fuerza a int con .intValue() igual que ya haces arriba con
                        // idSistemaOperativo,
                        // porque el constructor de Equipo (TecnicoModule.model) espera int en esta
                        // posicion.
                        e.getAnioCreacion() == null ? 0 : e.getAnioCreacion().intValue(),
                        // FIX confirmado: Equipo.java pide int idUsuarioResponsable, pero la
                        // entidad de BD devuelve Long. Se agrega .intValue().
                        e.getIdUsuarioResponsable() == null ? 0 : e.getIdUsuarioResponsable().intValue(),
                        "-",
                        e.getFechaCreacion().toLocalDateTime().toLocalDate(),
                        e.getFechaActualizacion().toLocalDateTime().toLocalDate()))
                        .toList());
    }

    private void cargarMantenimientosDesdeBD() {
        mantenimientos.setAll(mantenimientoRepository.obtenerTodos());
    }

    private void cargarActualizacionesDesdeBD() {
        actualizaciones.setAll(actualizacionRepository.obtenerTodos());
    }

    private void configurarFecha() {
        LocalDate hoy = LocalDate.now();
        // Locale(String,String) esta deprecado desde Java 19; se reemplaza por
        // Locale.of(...)
        DateTimeFormatter f = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale.of("es", "MX"));
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

    private void cargarHistorialDesdeBD() {
        historial.setAll(historialRepository.obtenerTodos());
    }

    private void configurarTablaMantenimientos() {
        mtId.setCellValueFactory(c -> txt(c.getValue().getId()));
        mtIdEquipo.setCellValueFactory(c -> txt(c.getValue().getIdEquipo()));
        mtEquipo.setCellValueFactory(c -> txt(c.getValue().getEquipoNombre()));
        mtTipo.setCellValueFactory(c -> txt(c.getValue().getTipo()));
        mtMotivo.setCellValueFactory(c -> txt(c.getValue().getMotivo()));
        // ⚠️ FIX: el getter real es isMantenimientoRealizado() (booleano), no
        // getMantenimientoRealizado().
        // Ya lo usas correctamente mas abajo en completarMantenimiento().
        mtRealizado.setCellValueFactory(c -> txt(c.getValue().isMantenimientoRealizado() ? "Si" : "No"));
        mtFecha.setCellValueFactory(c -> txt(f(c.getValue().getFecha())));
        mtFechaProx.setCellValueFactory(c -> txt(f(c.getValue().getFechaProxima())));
        mtEstado.setCellValueFactory(c -> txt(c.getValue().getEstado()));
        mtResponsable.setCellValueFactory(c -> txt(c.getValue().getUsuarioResponsable()));
        // ⚠️ VERIFICAR: RegistroMantenimiento no tiene (todavia) un getFechaRegistro().
        // Mientras no confirmes el nombre real del campo/getter en tu modelo, se usa
        // getFecha() como sustituto temporal para que compile. Si en tu tabla SQL
        // existe una columna fecha_registro distinta de fecha, agrega el getter
        // correspondiente en RegistroMantenimiento y cambia esta linea.
        mtFechaReg.setCellValueFactory(c -> txt(f(c.getValue().getFecha())));

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
        // ⚠️ VERIFICAR: mismo caso que arriba, RegistroActualizacion tampoco tiene
        // getFechaRegistro() todavia. Sustituto temporal con getFecha().
        acFechaReg.setCellValueFactory(c -> txt(f(c.getValue().getFecha())));

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
        long mant = mantenimientos.stream().filter(m -> m.getIdEquipo().equals(eq.getId())).count();
        long act = actualizaciones.stream().filter(a -> a.getIdEquipo() == eq.getId()).count();

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
                + "actualizaciones        : " + act + "\n";

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
                "activo", "en_mantenimineto", "inactivo", "de_baja");
        d.setTitle("Cambiar estado");
        d.setHeaderText(eq.getEquipos() + " - estado actual: " + eq.getEstado());
        d.setContentText("Nuevo estado:");

        Optional<String> r = d.showAndWait();
        if (!r.isPresent() || r.get().equals(eq.getEstado()))
            return;

        String anterior = eq.getEstado();
        String nuevoEstado = r.get();
        LocalDate fechaAnterior = eq.getFechaActualizacion();

        try {

            equipoRealRepository.actualizarEstado(eq.getId(), nuevoEstado);

            // Solo se actualiza la UI/memoria si la BD confirmo el cambio
            eq.setEstado(nuevoEstado);
            eq.setFechaActualizacion(LocalDate.now());

            agregarHistorial(eq, "Equipo", 0, 0,
                    "Estado cambiado de \"" + anterior + "\" a \"" + nuevoEstado + "\"");
            tablaEquipos.refresh();
            refrescarTodo();

        } catch (Exception ex) {
            // Rollback: si la BD fallo, no dejamos el estado "fantasma" en memoria
            eq.setEstado(anterior);
            eq.setFechaActualizacion(fechaAnterior);
            error("No se pudo actualizar el estado en la base de datos:\n\n" + ex.getMessage());
        }
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
                mantenimientoRepository.guardar(r); // asigna el id real
                mantenimientos.add(r);
                historialRepository.registrarPorMantenimiento(r.getIdEquipo(), r.getId());
            } else {
                mantenimientoRepository.actualizar(r);
                tablaMantenimientos.refresh();
                historialRepository.registrarPorMantenimiento(r.getIdEquipo(), r.getId());
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
                "/com/utng/ui/tecnicoModule/pantallaDialogoMantenimineto/" + nombreArchivo,
                "/com/utng/ui/tecnicoModule/pantallaDialogoActualizacion/" + nombreArchivo,
                "/com/utng/ui/tecnicoModule/" + nombreArchivo,
                "/com/utng/TecnicoModule/view/" + nombreArchivo,
                "/com/utng/TecnicoModule/" + nombreArchivo,
                "/com/utng/view/" + nombreArchivo,
                "/view/" + nombreArchivo,
                "/fxml/" + nombreArchivo,
                "/" + nombreArchivo,
                nombreArchivo
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
        if (r.isMantenimientoRealizado()) {
            aviso("Ese registro ya esta completado.");
            return;
        }

        TextInputDialog d = new TextInputDialog(r.getNotasRealizado());
        d.setTitle("Cerrar mantenimiento");
        d.setHeaderText(r.getEquipoNombre() + " - " + r.getTipo());
        d.setContentText("Describe que se hizo:");
        d.getDialogPane().setPrefWidth(460);

        Optional<String> res = d.showAndWait();
        if (!res.isPresent() || res.get().trim().isEmpty()) {
            if (res.isPresent())
                aviso("Hay que describir que se hizo para poder cerrarlo.");
            return;
        }

        r.setNotasRealizado(res.get().trim());
        r.setMantenimientoRealizado(true);
        mantenimientoRepository.actualizar(r);
        historialRepository.registrarPorMantenimiento(r.getIdEquipo(), r.getId());

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

    private void abrirDialogoActualizacion(RegistroActualizacion existente, EquipoPrograma programaPre) {
        try {
            FXMLLoader loader = cargarVista("DialogoActualizacion.fxml");
            Node contenido = loader.load();
            DialogoActualizacionController ctrl = loader.getController();
            // ⚠️ FIX: configurar(...) espera Long, no int. Se quita el .intValue().
            ctrl.configurar(equipos, idTecnicoSesion, nombreTecnicoSesion);

            if (existente != null) {
                ctrl.cargarRegistro(existente, equipos);
            } else if (programaPre != null) {
                // ⚠️ FIX: buscarEquipo(Long) recibe un int (EquipoPrograma.getIdEquipo()).
                // Se ensancha explicitamente a long para que autoboxee a Long.
                ctrl.precargarPrograma(buscarEquipo((long) programaPre.getIdEquipo()),
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
                actualizacionRepository.guardar(r);
                actualizaciones.add(r);
                historialRepository.registrarPorActualizacion(r.getIdEquipo(), r.getId());
            } else {
                actualizacionRepository.actualizar(r);
                tablaActualizaciones.refresh();
                historialRepository.registrarPorActualizacion(r.getIdEquipo(), r.getId());
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
                eq == null ? 0 : eq.getId().intValue(),
                eq == null ? "-" : eq.getEquipos(),
                tipo, idMant, idAct, descripcion, LocalDate.now()));
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
    // CHAT CON CONSULTORES (MongoDB real, contactos reales de SQL)
    // ══════════════════════════════════════════════════════════════
    /**
     * Carga consultores reales (rol = consulta, activos) desde la tabla usuarios.
     */
    private void cargarConsultoresReales() {
        List<Usuario> consultoresBD = usuarioRepository.obtenerActivosPorRol(TipoUsuario.CONSULTA);

        consultores.clear();
        for (Usuario u : consultoresBD) {
            String nombreCompleto = (u.getNombreCompleto() + " " + u.getApellidoPaterno()).trim();
            UsuarioChat uc = new UsuarioChat(u.getIdUsuario(), nombreCompleto, "Consultor", "-", true);
            consultores.add(uc);
        }
        lblTotalConsultores.setText(String.valueOf(consultores.size()));
    }

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
        mensajesMostrados = 0;

        if (u == null) {
            lblAvatarConsultor.setText("--");
            lblNombreConsultor.setText("Selecciona un consultor");
            lblRolConsultor.setText("Aqui llegan los reportes de equipos");
            txtMensaje.setDisable(true);
            btnEnviar.setDisable(true);
            btnCrearDesdeChat.setDisable(true);
            chatActualId = null;
            return;
        }

        lblAvatarConsultor.setText(u.getIniciales());
        lblNombreConsultor.setText(u.getNombreCompleto());
        lblRolConsultor.setText(u.getRol() + " - " + u.getLugar());
        lblEstadoConexion.setText(u.isEnLinea() ? "En linea" : "Ausente");

        txtMensaje.setDisable(false);
        btnEnviar.setDisable(false);
        btnCrearDesdeChat.setDisable(false);

        ObjectId otroId = ChatService.idDesdeLong(u.getId());
        Chat chat = chatService.obtenerOCrearChat(miId, otroId);
        chatActualId = chat.getId();

        cargarMensajes();
        chatService.marcarConversacionLeida(chatActualId, miId);
        u.setNoLeidos(0);
        listaConsultores.refresh();
    }

    private void cargarMensajes() {
        List<Mensaje> mensajes = chatService.obtenerMensajes(chatActualId);

        if (mensajes.isEmpty()) {
            pintarSistema("Sin mensajes con " + consultorActual.getNombreCompleto() + " todavia.");
        } else {
            for (Mensaje m : mensajes) {
                boolean mio = m.getEmisorId().equals(miId);
                pintarBurbuja(m.getMensaje(), m.getFechaEnvio().format(HORA), mio);
            }
        }
        mensajesMostrados = mensajes.size();
        bajarScroll();
    }

    @FXML
    private void enviarMensaje() {
        if (consultorActual == null || chatActualId == null) {
            aviso("Selecciona un consultor.");
            return;
        }
        String t = texto(txtMensaje, false);
        if (t.isEmpty())
            return;

        ObjectId otroId = ChatService.idDesdeLong(consultorActual.getId());
        Mensaje m = chatService.enviarMensaje(chatActualId, miId, otroId, t);
        pintarBurbuja(m.getMensaje(), m.getFechaEnvio().format(HORA), true);
        mensajesMostrados++;
        txtMensaje.clear();
        bajarScroll();
    }

    /**
     * Toma el ultimo reporte del consultor (desde Mongo) y abre el alta de
     * mantenimiento con el motivo ya puesto.
     */
    @FXML
    private void mantenimientoDesdeChat() {
        if (consultorActual == null || chatActualId == null) {
            aviso("Selecciona un consultor.");
            return;
        }

        List<Mensaje> mensajes = chatService.obtenerMensajes(chatActualId);
        String ultimoReporte = null;
        for (int i = mensajes.size() - 1; i >= 0; i--) {
            Mensaje m = mensajes.get(i);
            if (!m.getEmisorId().equals(miId)) {
                ultimoReporte = m.getMensaje();
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
                    // ⚠️ FIX: buscarEquipo(Long) — se usa Long.parseLong en vez de
                    // Integer.parseInt para que el resultado autoboxee directo a Long.
                    equipoDetectado = buscarEquipo(
                            Long.parseLong(ultimoReporte.substring(ini + 9, fin).trim()));
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
        if (chatActualId == null)
            return;

        Alert conf = new Alert(Alert.AlertType.CONFIRMATION,
                "Se borraran todos los mensajes de esta conversacion. Continuar?",
                ButtonType.YES, ButtonType.NO);
        conf.setHeaderText(null);
        if (conf.showAndWait().orElse(ButtonType.NO) != ButtonType.YES)
            return;

        chatService.eliminarConversacion(chatActualId);
        contenedorChat.getChildren().clear();
        mensajesMostrados = 0;
        pintarSistema("Conversacion limpiada.");
    }

    // ── Polling en segundo plano: mensajes nuevos + contadores de no leidos ──
    private void refrescarChatsEnSegundoPlano() {
        try {
            Map<Long, Long> noLeidosPorConsultor = new HashMap<>();
            ObjectId chatAbiertoSnapshot = chatActualId;

            for (UsuarioChat u : consultores) {
                ObjectId otroId = ChatService.idDesdeLong(u.getId());
                Chat chat = chatService.obtenerOCrearChat(miId, otroId);
                noLeidosPorConsultor.put(u.getId(), chatService.contarNoLeidos(chat.getId(), miId));
            }

            List<Mensaje> mensajesChatAbierto = chatAbiertoSnapshot != null
                    ? chatService.obtenerMensajes(chatAbiertoSnapshot)
                    : null;

            Platform.runLater(
                    () -> aplicarRefrescoChat(noLeidosPorConsultor, chatAbiertoSnapshot, mensajesChatAbierto));
        } catch (Exception ex) {
            // no tumbamos el hilo de refresco por un error puntual de red/Mongo
            ex.printStackTrace();
        }
    }

    private void aplicarRefrescoChat(Map<Long, Long> noLeidos, ObjectId chatAbierto, List<Mensaje> mensajes) {
        for (UsuarioChat u : consultores) {
            Long n = noLeidos.get(u.getId());
            if (n != null)
                u.setNoLeidos(u == consultorActual ? 0 : n.intValue());
        }
        listaConsultores.refresh();

        if (chatAbierto != null && chatAbierto.equals(chatActualId)
                && mensajes != null && mensajes.size() != mensajesMostrados) {
            contenedorChat.getChildren().clear();
            if (mensajes.isEmpty()) {
                pintarSistema("Sin mensajes con " + consultorActual.getNombreCompleto() + " todavia.");
            } else {
                for (Mensaje m : mensajes) {
                    boolean mio = m.getEmisorId().equals(miId);
                    pintarBurbuja(m.getMensaje(), m.getFechaEnvio().format(HORA), mio);
                }
            }
            mensajesMostrados = mensajes.size();
            chatService.marcarConversacionLeida(chatAbierto, miId);
            bajarScroll();
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
        hiloChat.shutdownNow();
        SesionManager.getInstance().limpiar();
        aviso("Sesion cerrada (conecta aqui tu pantalla de login).");
        Navigator.navigate("/com/utng/ui/Auth/pantallaLogin/PantallaLogin.fxml");
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
        lblConteoHist.setText(fHistorial.size() + " de " + historial.size() + " movimientos");
        lblTotalConsultores.setText(String.valueOf(fConsultores.size()));
    }

    private Equipo buscarEquipo(Long id) {
        return equipos.stream().filter(e -> id != null && id.equals(e.getId())).findFirst().orElse(null);
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
    // NOTA: se elimino cargarDatosDemo(). Ese metodo nunca se llamaba desde
    // initialize() (ya usas cargarEquiposDesdeBD/cargarMantenimientosDesdeBD/
    // cargarActualizacionesDesdeBD con datos reales), y sus constructores de
    // Equipo/RegistroMantenimiento/RegistroActualizacion/HistorialRegistro ya
    // no coincidian con tus clases modelo actuales — era codigo muerto que
    // generaba ~15 errores de compilacion sin ningun beneficio.
    // ══════════════════════════════════════════════════════════════
}