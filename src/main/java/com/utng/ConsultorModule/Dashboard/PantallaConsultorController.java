package com.utng.ConsultorModule.Dashboard;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.Event;
import javafx.fxml.FXML;
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
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.bson.types.ObjectId;

import com.utng.ConsultorModule.model.EquipoConsulta;
import com.utng.ConsultorModule.model.TecnicoChat;
import com.utng.EquipoModule.model.equipo.Equipo;
import com.utng.EquipoModule.model.sistemaOperativo.SistemaOperativo;
import com.utng.EquipoModule.repository.EquipoRepository;
import com.utng.SistemasOperativosModule.repository.SistemaOperativoRepository;
import com.utng.UserModule.UsuarioRepository;
import com.utng.UserModule.model.usuario.TipoUsuario;
import com.utng.UserModule.model.usuario.Usuario;
import com.utng.chatModule.model.Chat;
import com.utng.chatModule.model.Mensaje;
import com.utng.chatModule.service.ChatService;
import com.utng.util.Navigator;
import com.utng.util.SesionManager;

/**
 * Controlador de la pantalla del rol CONSULTOR.
 *
 * Permisos de este rol:
 * - VER el inventario de equipos (13 campos), buscar, filtrar y exportar.
 * - NO puede crear, editar ni eliminar equipos.
 * - Puede chatear con usuarios de tipo TECNICO para reportar fallas (via
 * MongoDB), con contactos reales sacados de la tabla usuarios (rol =
 * tecnico).
 */
public class PantallaConsultorController {

    // ═══════════ SESION (real, viene del login) ═══════════
    private Long idConsultorSesion;
    private String nombreConsultorSesion;
    private final UsuarioRepository usuarioRepository = new UsuarioRepository();

    // ───────────── ENCABEZADO ─────────────
    @FXML
    private Button btnMenu;
    @FXML
    private Label lblFechaHoy;

    // ───────────── KPIs ─────────────
    @FXML
    private Label lblTotalEquipos;
    @FXML
    private Label lblEquiposActivos;
    @FXML
    private Label lblEnMantenimiento;
    @FXML
    private Label lblEquiposBaja;

    // ───────────── FILTROS ─────────────
    @FXML
    private TextField txtBuscar;
    @FXML
    private ComboBox<String> cmbLugar;
    @FXML
    private ComboBox<String> cmbSistemaOperativo;

    @FXML
    private Button chipTodos;
    @FXML
    private HBox chipActivos;
    @FXML
    private HBox chipMantenimiento;
    @FXML
    private HBox chipInactivos;
    @FXML
    private HBox chipBaja;
    @FXML
    private Label lblEstadoActivos;
    @FXML
    private Label lblEstadoMantenimiento;
    @FXML
    private Label lblEstadoInactivos;
    @FXML
    private Label lblEstadoBaja;

    // ───────────── TABLA ─────────────
    @FXML
    private TableView<EquipoConsulta> tablaEquipos;
    @FXML
    private TableColumn<EquipoConsulta, String> colId;
    @FXML
    private TableColumn<EquipoConsulta, String> colEquipos;
    @FXML
    private TableColumn<EquipoConsulta, String> colModelo;
    @FXML
    private TableColumn<EquipoConsulta, String> colProcesador;
    @FXML
    private TableColumn<EquipoConsulta, String> colMemoriaRam;
    @FXML
    private TableColumn<EquipoConsulta, String> colAlmacenamiento;
    @FXML
    private TableColumn<EquipoConsulta, String> colSistemaOperativo;
    @FXML
    private TableColumn<EquipoConsulta, String> colLugar;
    @FXML
    private TableColumn<EquipoConsulta, String> colEstado;
    @FXML
    private TableColumn<EquipoConsulta, String> colAnioCreacion;
    @FXML
    private TableColumn<EquipoConsulta, String> colUsuarioResponsable;
    @FXML
    private TableColumn<EquipoConsulta, String> colFechaCreacion;
    @FXML
    private TableColumn<EquipoConsulta, String> colFechaActualizacion;
    @FXML
    private Label lblConteo;
    @FXML
    private Button btnReportar;

    // ───────────── CHAT ─────────────
    @FXML
    private Label lblEstadoConexion;
    @FXML
    private TextField txtBuscarTecnico;
    @FXML
    private ListView<TecnicoChat> listaTecnicos;
    @FXML
    private Label lblTotalTecnicos;
    @FXML
    private HBox cabeceraChat;
    @FXML
    private Label lblAvatarTecnico;
    @FXML
    private Label lblNombreTecnico;
    @FXML
    private Label lblAreaTecnico;
    @FXML
    private Label lblEquipoContexto;
    @FXML
    private ScrollPane scrollChat;
    @FXML
    private VBox contenedorChat;
    @FXML
    private TextField txtMensaje;
    @FXML
    private Button btnEnviar;

    // ───────────── MENU LATERAL ─────────────
    @FXML
    private Region overlayMenu;
    @FXML
    private VBox panelMenu;
    @FXML
    private Button btnMenuEquipos;
    @FXML
    private Button btnMenuChat;
    @FXML
    private Label lblAvatarUsuario;
    @FXML
    private Label lblNombreUsuario;
    @FXML
    private Label lblRolUsuario;

    // ───────────── DATOS ─────────────
    private final ObservableList<EquipoConsulta> equipos = FXCollections.observableArrayList();
    private final ObservableList<TecnicoChat> tecnicos = FXCollections.observableArrayList();

    private FilteredList<EquipoConsulta> equiposFiltrados;
    private FilteredList<TecnicoChat> tecnicosFiltrados;

    // ═══════════ REPOSITORIOS ═══════════
    private final EquipoRepository equipoRepository = new EquipoRepository();
    private final SistemaOperativoRepository sistemaOperativoRepository = new SistemaOperativoRepository();

    // ═══════════ CATALOGOS (para resolver nombres) ═══════════
    private final List<SistemaOperativo> catalogoSistemasOperativos = new ArrayList<>();
    private final List<Usuario> catalogoUsuarios = new ArrayList<>();

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private String estadoSeleccionado = "TODOS";

    /** Equipo que el consultor adjunto al chat como contexto del reporte. */
    private EquipoConsulta equipoContexto;

    private TecnicoChat tecnicoActual;

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    // ═══════════ CHAT (MongoDB) ═══════════
    private final ChatService chatService = new ChatService();
    private ObjectId miId;
    private ObjectId chatActualId;
    private int mensajesMostrados = 0;

    private final ScheduledExecutorService hiloChat = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "chat-refresco-consultor");
        t.setDaemon(true);
        return t;
    });

    // ══════════════════════════════════════════════════════════════
    // INICIALIZACION
    // ══════════════════════════════════════════════════════════════
    @FXML
    private void initialize() {
        // ── Sesion real (viene del login) ──
        idConsultorSesion = SesionManager.getInstance().getIdUsuario();
        nombreConsultorSesion = SesionManager.getInstance().getNombreCompleto();

        configurarFecha();
        configurarTabla();
        configurarListaTecnicos();
        cargarCatalogos();
        cargarEquiposDesdeBD();
        configurarFiltros();
        actualizarKPIs();
        actualizarConteo();
        resaltarChip(chipTodos);
        btnEnviar.setDisable(true);
        txtMensaje.setDisable(true);
        lblNombreUsuario.setText(nombreConsultorSesion);
        lblRolUsuario.setText("Consultor CGTI");
        lblAvatarUsuario.setText(iniciales(nombreConsultorSesion));

        // ── Chat real contra Mongo, con id real de sesion ──
        miId = ChatService.idDesdeLong(idConsultorSesion);
        cargarTecnicosReales();
        hiloChat.scheduleWithFixedDelay(this::refrescarChatsEnSegundoPlano, 3, 3, TimeUnit.SECONDS);
    }

    private void configurarFecha() {
        LocalDate hoy = LocalDate.now();
        DateTimeFormatter f = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", new Locale("es", "MX"));
        String texto = hoy.format(f);
        lblFechaHoy.setText(texto.substring(0, 1).toUpperCase() + texto.substring(1));
    }

    // ══════════════════════════════════════════════════════════════
    // TABLA (SOLO LECTURA)
    // ══════════════════════════════════════════════════════════════
    private void configurarTabla() {
        colId.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getId())));
        colEquipos.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEquipos()));
        colModelo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getModelo()));
        colProcesador.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getProcesador()));
        colMemoriaRam.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMemoriaRam()));
        colAlmacenamiento.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAlmacenamiento()));
        colSistemaOperativo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getIdSistemaOperativo()));
        colLugar.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLugar()));
        colEstado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEstado()));
        colAnioCreacion
                .setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getAnioCreacion())));
        colUsuarioResponsable
                .setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getIdUsuarioResponsable()));
        colFechaCreacion.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFechaCreacion()));
        colFechaActualizacion.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFechaActualizacion()));

        colEstado.setCellFactory(col -> new TableCell<EquipoConsulta, String>() {
            @Override
            protected void updateItem(String estado, boolean vacio) {
                super.updateItem(estado, vacio);
                if (vacio || estado == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                Label badge = new Label(estado);
                badge.setStyle(estiloBadge(estado));
                setGraphic(badge);
                setText(null);
            }
        });

        tablaEquipos.setRowFactory(tv -> {
            TableRow<EquipoConsulta> fila = new TableRow<>();
            fila.setOnMouseClicked(ev -> {
                if (ev.getClickCount() == 2 && !fila.isEmpty()) {
                    mostrarDetalle(fila.getItem());
                }
            });
            return fila;
        });

        tablaEquipos.setEditable(false);
        tablaEquipos.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }

    private String estiloBadge(String estado) {
        String fondo, texto;
        switch (estado.toLowerCase()) {
            case "activo":
                fondo = "#dcfce7";
                texto = "#15803d";
                break;
            case "en mantenimiento":
                fondo = "#fef3c7";
                texto = "#b45309";
                break;
            case "de baja":
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
        equiposFiltrados = new FilteredList<>(equipos, e -> true);
        SortedList<EquipoConsulta> ordenados = new SortedList<>(equiposFiltrados);
        ordenados.comparatorProperty().bind(tablaEquipos.comparatorProperty());
        tablaEquipos.setItems(ordenados);

        ObservableList<String> lugares = FXCollections.observableArrayList("Todos los lugares");
        ObservableList<String> sistemas = FXCollections.observableArrayList("Todos los S.O.");
        equipos.forEach(e -> {
            if (!lugares.contains(e.getLugar()))
                lugares.add(e.getLugar());
            if (!sistemas.contains(e.getIdSistemaOperativo()))
                sistemas.add(e.getIdSistemaOperativo());
        });
        cmbLugar.setItems(lugares);
        cmbSistemaOperativo.setItems(sistemas);
        cmbLugar.getSelectionModel().selectFirst();
        cmbSistemaOperativo.getSelectionModel().selectFirst();
    }

    @FXML
    private void filtrarEquipos(Event e) {
        aplicarFiltro();
    }

    private void aplicarFiltro() {
        final String texto = txtBuscar.getText() == null ? "" : txtBuscar.getText().trim().toLowerCase();
        final String lugar = cmbLugar.getValue();
        final String sistema = cmbSistemaOperativo.getValue();

        Predicate<EquipoConsulta> p = eq -> {
            if (!texto.isEmpty() && !eq.textoBusqueda().contains(texto))
                return false;
            if (lugar != null && !lugar.startsWith("Todos") && !lugar.equals(eq.getLugar()))
                return false;
            if (sistema != null && !sistema.startsWith("Todos")
                    && !sistema.equals(eq.getIdSistemaOperativo()))
                return false;
            if (!"TODOS".equals(estadoSeleccionado)
                    && !estadoSeleccionado.equalsIgnoreCase(eq.getEstado()))
                return false;
            return true;
        };

        equiposFiltrados.setPredicate(p);
        actualizarConteo();
    }

    @FXML
    private void filtrarPorEstado(Event e) {
        Node origen = (Node) e.getSource();

        if (origen == chipTodos)
            estadoSeleccionado = "TODOS";
        else if (origen == chipActivos)
            estadoSeleccionado = "Activo";
        else if (origen == chipMantenimiento)
            estadoSeleccionado = "En mantenimiento";
        else if (origen == chipInactivos)
            estadoSeleccionado = "Inactivo";
        else if (origen == chipBaja)
            estadoSeleccionado = "De baja";

        resaltarChip(origen);
        aplicarFiltro();
    }

    private void resaltarChip(Node activo) {
        chipTodos.setStyle(
                (activo == chipTodos
                        ? "-fx-background-color: #0f172a; -fx-text-fill: white;"
                        : "-fx-background-color: #f4f6fa; -fx-text-fill: #475569;")
                        + "-fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 16;"
                        + "-fx-padding: 5 13 5 13; -fx-cursor: hand;");

        aplicarEstiloChip(chipActivos, activo == chipActivos, "#f0fdf4", "#bbf7d0", "#16a34a");
        aplicarEstiloChip(chipMantenimiento, activo == chipMantenimiento, "#fffbeb", "#fde68a", "#f59e0b");
        aplicarEstiloChip(chipInactivos, activo == chipInactivos, "#f8fafc", "#e2e8f0", "#6b7280");
        aplicarEstiloChip(chipBaja, activo == chipBaja, "#fef2f2", "#fecaca", "#dc2626");
    }

    private void aplicarEstiloChip(HBox chip, boolean activo, String fondo, String borde, String acento) {
        chip.setStyle("-fx-background-color: " + fondo + "; -fx-background-radius: 16;"
                + "-fx-border-color: " + (activo ? acento : borde) + ";"
                + "-fx-border-width: " + (activo ? "2" : "1") + "; -fx-border-radius: 16;"
                + "-fx-padding: 5 12 5 12; -fx-cursor: hand;");
    }

    @FXML
    private void limpiarFiltros() {
        txtBuscar.clear();
        cmbLugar.getSelectionModel().selectFirst();
        cmbSistemaOperativo.getSelectionModel().selectFirst();
        estadoSeleccionado = "TODOS";
        resaltarChip(chipTodos);
        aplicarFiltro();
    }

    private void actualizarConteo() {
        lblConteo.setText("Mostrando " + equiposFiltrados.size() + " de " + equipos.size() + " equipos");
    }

    private void actualizarKPIs() {
        long activos = equipos.stream().filter(e -> "Activo".equalsIgnoreCase(e.getEstado())).count();
        long mant = equipos.stream().filter(e -> "En mantenimiento".equalsIgnoreCase(e.getEstado())).count();
        long inact = equipos.stream().filter(e -> "Inactivo".equalsIgnoreCase(e.getEstado())).count();
        long baja = equipos.stream().filter(e -> "De baja".equalsIgnoreCase(e.getEstado())).count();

        lblTotalEquipos.setText(String.valueOf(equipos.size()));
        lblEquiposActivos.setText(String.valueOf(activos));
        lblEnMantenimiento.setText(String.valueOf(mant));
        lblEquiposBaja.setText(String.valueOf(baja));

        lblEstadoActivos.setText(String.valueOf(activos));
        lblEstadoMantenimiento.setText(String.valueOf(mant));
        lblEstadoInactivos.setText(String.valueOf(inact));
        lblEstadoBaja.setText(String.valueOf(baja));
    }

    // ══════════════════════════════════════════════════════════════
    // DETALLE DEL EQUIPO (solo lectura)
    // ══════════════════════════════════════════════════════════════
    @FXML
    private void verDetalle() {
        EquipoConsulta eq = tablaEquipos.getSelectionModel().getSelectedItem();
        if (eq == null) {
            aviso("Selecciona un equipo de la tabla para ver su detalle.");
            return;
        }
        mostrarDetalle(eq);
    }

    private void mostrarDetalle(EquipoConsulta eq) {
        String detalle = "id                     : " + eq.getId() + "\n"
                + "equipos                : " + eq.getEquipos() + "\n"
                + "modelo                 : " + eq.getModelo() + "\n"
                + "procesador             : " + eq.getProcesador() + "\n"
                + "memoria_ram            : " + eq.getMemoriaRam() + "\n"
                + "almacenamiento         : " + eq.getAlmacenamiento() + "\n"
                + "id_sistema_operativo   : " + eq.getIdSistemaOperativo() + "\n"
                + "lugar                  : " + eq.getLugar() + "\n"
                + "estado                 : " + eq.getEstado() + "\n"
                + "anio_creacion          : " + eq.getAnioCreacion() + "\n"
                + "id_usuario_responsable : " + eq.getIdUsuarioResponsable() + "\n"
                + "fecha_creacion         : " + eq.getFechaCreacion() + "\n"
                + "fecha_actualizacion    : " + eq.getFechaActualizacion();

        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Detalle del equipo");
        a.setHeaderText(eq.getEquipos() + "  -  " + eq.getModelo());
        TextArea area = new TextArea(detalle);
        area.setEditable(false);
        area.setWrapText(false);
        area.setPrefRowCount(13);
        area.setStyle("-fx-font-family: 'monospaced'; -fx-font-size: 12px;");
        a.getDialogPane().setContent(area);
        a.getDialogPane().setPrefWidth(480);
        a.showAndWait();
    }

    // ══════════════════════════════════════════════════════════════
    // EXPORTAR
    // ══════════════════════════════════════════════════════════════
    @FXML
    private void exportarCSV() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Exportar inventario");
        fc.setInitialFileName("equipos_consulta.csv");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        File destino = fc.showSaveDialog(tablaEquipos.getScene().getWindow());
        if (destino == null)
            return;

        try (BufferedWriter w = new BufferedWriter(new FileWriter(destino))) {
            w.write("id,equipos,modelo,procesador,memoria_ram,almacenamiento,id_sistema_operativo,"
                    + "lugar,estado,anio_creacion,id_usuario_responsable,fecha_creacion,fecha_actualizacion");
            w.newLine();
            for (EquipoConsulta e : equiposFiltrados) {
                w.write(String.join(",",
                        String.valueOf(e.getId()),
                        csv(e.getEquipos()), csv(e.getModelo()), csv(e.getProcesador()),
                        csv(e.getMemoriaRam()), csv(e.getAlmacenamiento()), csv(e.getIdSistemaOperativo()),
                        csv(e.getLugar()), csv(e.getEstado()), String.valueOf(e.getAnioCreacion()),
                        csv(e.getIdUsuarioResponsable()), csv(e.getFechaCreacion()),
                        csv(e.getFechaActualizacion())));
                w.newLine();
            }
            aviso("Se exportaron " + equiposFiltrados.size() + " equipos.");
        } catch (IOException ex) {
            error("No se pudo exportar: " + ex.getMessage());
        }
    }

    private String csv(String v) {
        if (v == null)
            return "";
        return v.contains(",") ? "\"" + v.replace("\"", "\"\"") + "\"" : v;
    }

    // ══════════════════════════════════════════════════════════════
    // CHAT CON TECNICOS (MongoDB real, contactos reales de SQL)
    // ══════════════════════════════════════════════════════════════
    /** Carga tecnicos reales (rol = tecnico, activos) desde la tabla usuarios. */
    private void cargarTecnicosReales() {
        List<Usuario> tecnicosBD = usuarioRepository.obtenerActivosPorRol(TipoUsuario.TECNICO);

        tecnicos.clear();
        for (Usuario u : tecnicosBD) {
            String nombreCompleto = (u.getNombreCompleto() + " " + u.getApellidoPaterno()).trim();
            TecnicoChat tc = new TecnicoChat(u.getIdUsuario(), nombreCompleto, "Soporte tecnico", true);
            tecnicos.add(tc);
        }
        lblTotalTecnicos.setText(String.valueOf(tecnicos.size()));
    }

    private void configurarListaTecnicos() {
        tecnicosFiltrados = new FilteredList<>(tecnicos, t -> true);
        listaTecnicos.setItems(tecnicosFiltrados);

        listaTecnicos.setCellFactory(lv -> new ListCell<TecnicoChat>() {
            @Override
            protected void updateItem(TecnicoChat t, boolean vacio) {
                super.updateItem(t, vacio);
                if (vacio || t == null) {
                    setGraphic(null);
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                    return;
                }

                Label avatar = new Label(t.getIniciales());
                avatar.setStyle("-fx-font-size: 10.5px; -fx-font-weight: bold; -fx-text-fill: white;"
                        + "-fx-background-color: " + (t.isEnLinea() ? "#6366f1" : "#94a3b8") + ";"
                        + "-fx-background-radius: 14; -fx-min-width: 28; -fx-min-height: 28;"
                        + "-fx-alignment: center;");

                Label nombre = new Label(t.getNombre());
                nombre.setStyle("-fx-font-size: 11.5px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

                Label area = new Label(t.getArea());
                area.setStyle("-fx-font-size: 10px; -fx-text-fill: #94a3b8;");

                VBox datos = new VBox(1, nombre, area);

                Region sep = new Region();
                HBox.setHgrow(sep, Priority.ALWAYS);

                Label punto = new Label(t.isEnLinea() ? "En linea" : "Ausente");
                punto.setStyle("-fx-font-size: 9px; -fx-font-weight: bold;"
                        + (t.isEnLinea()
                                ? "-fx-text-fill: #16a34a; -fx-background-color: #f0fdf4;"
                                : "-fx-text-fill: #64748b; -fx-background-color: #f1f5f9;")
                        + "-fx-background-radius: 10; -fx-padding: 2 7 2 7;");

                HBox fila = new HBox(9, avatar, datos, sep, punto);
                fila.setAlignment(Pos.CENTER_LEFT);
                fila.setPadding(new Insets(7, 8, 7, 8));

                setGraphic(fila);
                setText(null);
                setStyle("-fx-background-color: transparent;");
            }
        });

        listaTecnicos.getSelectionModel().selectedItemProperty()
                .addListener((obs, anterior, nuevo) -> abrirConversacion(nuevo));
    }

    @FXML
    private void filtrarTecnicos(Event e) {
        String texto = txtBuscarTecnico.getText() == null ? "" : txtBuscarTecnico.getText().trim().toLowerCase();
        tecnicosFiltrados.setPredicate(t -> texto.isEmpty()
                || t.getNombre().toLowerCase().contains(texto)
                || t.getArea().toLowerCase().contains(texto));
        lblTotalTecnicos.setText(String.valueOf(tecnicosFiltrados.size()));
    }

    private void abrirConversacion(TecnicoChat t) {
        tecnicoActual = t;
        contenedorChat.getChildren().clear();
        mensajesMostrados = 0;

        if (t == null) {
            lblAvatarTecnico.setText("--");
            lblNombreTecnico.setText("Selecciona un tecnico");
            lblAreaTecnico.setText("Elige a quien reportar desde la lista");
            lblEstadoConexion.setText("Sin conversacion");
            txtMensaje.setDisable(true);
            btnEnviar.setDisable(true);
            chatActualId = null;
            return;
        }

        lblAvatarTecnico.setText(t.getIniciales());
        lblNombreTecnico.setText(t.getNombre());
        lblAreaTecnico.setText(t.getArea());
        lblEstadoConexion.setText(t.isEnLinea() ? "En linea" : "Ausente");
        lblEstadoConexion.setStyle(t.isEnLinea()
                ? "-fx-text-fill: #16a34a; -fx-font-size: 10.5px; -fx-font-weight: bold;"
                        + "-fx-background-color: #f0fdf4; -fx-background-radius: 16; -fx-padding: 5 10 5 10;"
                : "-fx-text-fill: #64748b; -fx-font-size: 10.5px; -fx-font-weight: bold;"
                        + "-fx-background-color: #f1f5f9; -fx-background-radius: 16; -fx-padding: 5 10 5 10;");

        txtMensaje.setDisable(false);
        btnEnviar.setDisable(false);

        ObjectId otroId = ChatService.idDesdeLong(t.getId());
        Chat chat = chatService.obtenerOCrearChat(miId, otroId);
        chatActualId = chat.getId();

        cargarMensajes();
        chatService.marcarConversacionLeida(chatActualId, miId);
    }

    private void cargarMensajes() {
        List<Mensaje> mensajes = chatService.obtenerMensajes(chatActualId);

        if (mensajes.isEmpty()) {
            pintarSistema("Inicia la conversacion con " + tecnicoActual.getNombre()
                    + ". Puedes adjuntar un equipo con el boton \"Reportar al tecnico\".");
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
        if (tecnicoActual == null || chatActualId == null) {
            aviso("Selecciona primero un tecnico de la lista.");
            return;
        }
        String texto = txtMensaje.getText() == null ? "" : txtMensaje.getText().trim();
        if (texto.isEmpty())
            return;

        // Si hay un equipo adjunto, se antepone su referencia al reporte
        if (equipoContexto != null) {
            texto = "[Equipo #" + equipoContexto.getId() + " - " + equipoContexto.getEquipos()
                    + " | " + equipoContexto.getLugar() + "] " + texto;
        }

        ObjectId otroId = ChatService.idDesdeLong(tecnicoActual.getId());
        Mensaje m = chatService.enviarMensaje(chatActualId, miId, otroId, texto);
        pintarBurbuja(m.getMensaje(), m.getFechaEnvio().format(HORA), true);
        mensajesMostrados++;
        txtMensaje.clear();
        bajarScroll();
    }

    @FXML
    private void usarSugerencia(javafx.event.ActionEvent e) {
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
        equipoContexto = null;
        lblEquipoContexto.setText("Sin equipo seleccionado");
    }

    /** Adjunta el equipo seleccionado al chat y baja el foco al cuadro de texto. */
    @FXML
    private void reportarEquipo() {
        EquipoConsulta eq = tablaEquipos.getSelectionModel().getSelectedItem();
        if (eq == null) {
            aviso("Selecciona el equipo que quieres reportar.");
            return;
        }
        equipoContexto = eq;
        lblEquipoContexto.setText("Equipo #" + eq.getId() + " - " + eq.getEquipos());

        if (tecnicoActual == null && !tecnicosFiltrados.isEmpty()) {
            listaTecnicos.getSelectionModel().selectFirst();
        }
        pintarSistema("Equipo adjunto: " + eq.getEquipos() + " (" + eq.getModelo()
                + ") - " + eq.getLugar() + " - estado: " + eq.getEstado());
        txtMensaje.requestFocus();
        bajarScroll();
    }

    // ── Polling en segundo plano: mensajes nuevos ──
    private void refrescarChatsEnSegundoPlano() {
        try {
            ObjectId chatAbiertoSnapshot = chatActualId;
            List<Mensaje> mensajesChatAbierto = chatAbiertoSnapshot != null
                    ? chatService.obtenerMensajes(chatAbiertoSnapshot)
                    : null;

            Platform.runLater(() -> aplicarRefrescoChat(chatAbiertoSnapshot, mensajesChatAbierto));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void aplicarRefrescoChat(ObjectId chatAbierto, List<Mensaje> mensajes) {
        if (chatAbierto != null && chatAbierto.equals(chatActualId)
                && mensajes != null && mensajes.size() != mensajesMostrados) {
            contenedorChat.getChildren().clear();
            if (mensajes.isEmpty()) {
                pintarSistema("Inicia la conversacion con " + tecnicoActual.getNombre() + ".");
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

    // ───────────── burbujas del chat ─────────────
    private void pintarBurbuja(String texto, String hora, boolean mio) {
        Label cuerpo = new Label(texto);
        cuerpo.setWrapText(true);
        cuerpo.setMaxWidth(420);
        cuerpo.setStyle(mio
                ? "-fx-background-color: linear-gradient(to bottom right, #8b5cf6, #6366f1);"
                        + "-fx-text-fill: white; -fx-font-size: 11.5px;"
                        + "-fx-background-radius: 12 12 2 12; -fx-padding: 9 12 9 12;"
                : "-fx-background-color: white; -fx-text-fill: #0f172a; -fx-font-size: 11.5px;"
                        + "-fx-background-radius: 12 12 12 2; -fx-padding: 9 12 9 12;"
                        + "-fx-border-color: #e8ecf2; -fx-border-radius: 12 12 12 2;");

        Label sello = new Label(hora);
        sello.setStyle("-fx-font-size: 9px; -fx-text-fill: #94a3b8;");

        VBox burbuja = new VBox(3, cuerpo, sello);
        burbuja.setAlignment(mio ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        HBox fila = new HBox(burbuja);
        fila.setAlignment(mio ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        contenedorChat.getChildren().add(fila);
    }

    private void pintarSistema(String texto) {
        Label l = new Label(texto);
        l.setWrapText(true);
        l.setMaxWidth(440);
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
    private void irAEquipos(javafx.event.ActionEvent e) {
        toggleMenu(e);
        tablaEquipos.requestFocus();
    }

    @FXML
    private void irAChat(javafx.event.ActionEvent e) {
        toggleMenu(e);
        listaTecnicos.requestFocus();
    }

    @FXML
    private void cerrarSesion(javafx.event.ActionEvent e) {
        hiloChat.shutdownNow();
        SesionManager.getInstance().limpiar();
        aviso("Sesion cerrada (conecta aqui tu pantalla de login).");
        Navigator.navigate("/com/utng/ui/Auth/pantallaLogin/PantallaLogin.fxml");

    }

    // ══════════════════════════════════════════════════════════════
    // UTILIDADES
    // ══════════════════════════════════════════════════════════════
    private void aviso(String mensaje) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, mensaje, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }

    private void error(String mensaje) {
        Alert a = new Alert(Alert.AlertType.ERROR, mensaje, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }

    private String iniciales(String nombre) {
        String[] p = nombre.trim().split("\\s+");
        if (p.length == 1)
            return p[0].substring(0, Math.min(2, p[0].length())).toUpperCase();
        return ("" + p[0].charAt(0) + p[1].charAt(0)).toUpperCase();
    }

    // ══════════════════════════════════════════════════════════════
    // CARGA DE CATALOGOS Y EQUIPOS (BD REAL)
    // ══════════════════════════════════════════════════════════════
    private void cargarCatalogos() {
        catalogoSistemasOperativos.clear();
        catalogoSistemasOperativos.addAll(sistemaOperativoRepository.obtenerTodos());

        catalogoUsuarios.clear();
        catalogoUsuarios.addAll(usuarioRepository.obtenerTodos());
    }

    private void cargarEquiposDesdeBD() {
        equipos.clear();
        List<Equipo> equiposBD = equipoRepository.obtenerTodos();
        for (Equipo e : equiposBD) {
            equipos.add(convertir(e));
        }
    }

    /** Convierte un Equipo real (BD) al modelo de solo lectura EquipoConsulta. */
    private EquipoConsulta convertir(Equipo e) {
        String nombreSO = nombreSistemaOperativo(e.getIdSistemaOperativo());
        String responsable = nombreResponsable(e.getIdUsuarioResponsable());
        String estado = e.getEstado() == null ? "Sin estado" : e.getEstado().getEtiqueta();

        return new EquipoConsulta(
                e.getIdEquipo() == null ? 0 : e.getIdEquipo().intValue(),
                valorODash(e.getModelo()), // "equipos" (no hay codigo aparte, usamos el modelo)
                valorODash(e.getModelo()), // "modelo"
                valorODash(e.getProcesador()),
                valorODash(e.getMemoriaRam()),
                valorODash(e.getAlmacenamiento()),
                nombreSO,
                valorODash(e.getLugar()),
                estado,
                e.getAnioCreacion() == null ? 0 : e.getAnioCreacion(),
                responsable,
                formatearFecha(e.getFechaCreacion()),
                formatearFecha(e.getFechaActualizacion()));
    }

    private String nombreSistemaOperativo(Long id) {
        if (id == null) {
            return "Sin S.O.";
        }
        return catalogoSistemasOperativos.stream()
                .filter(so -> id.equals(so.getIdSistemaOperativo()))
                .findFirst()
                .map(SistemaOperativo::getDescripcion)
                .orElse("Sin S.O.");
    }

    private String nombreResponsable(Long id) {
        if (id == null) {
            return "Sin asignar";
        }
        return catalogoUsuarios.stream()
                .filter(u -> id.equals(u.getIdUsuario()))
                .findFirst()
                .map(this::nombreCompletoUsuario)
                .orElse("Sin asignar");
    }

    private String nombreCompletoUsuario(Usuario u) {
        if (u == null) {
            return "Sin asignar";
        }
        return (valorODash(u.getNombreCompleto()) + " " + valorODash(u.getApellidoPaterno())).trim();
    }

    private static String formatearFecha(Timestamp fecha) {
        return fecha == null ? "—" : fecha.toLocalDateTime().toLocalDate().format(FORMATO_FECHA);
    }

    private static String valorODash(String valor) {
        return (valor == null || valor.isBlank()) ? "—" : valor;
    }

}