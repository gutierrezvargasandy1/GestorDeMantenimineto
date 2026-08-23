package com.utng.DashboardModule.controller;

import com.utng.AiModule.OllamaService;
import com.utng.AiModule.PromptContextBuilder;
import com.utng.AiModule.data.MaintenanceContextRepository;
import com.utng.DashboardModule.model.MantenimientoModel;
import com.utng.DashboardModule.repository.MantenimientoRepository;
import com.utng.config.OllamaConfig;
import com.utng.util.AppException;
import com.utng.util.MarkdownRenderer;
import com.utng.util.Navigator;
import com.utng.util.SesionManager;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

public class PantallaDashboardController {

    private final OllamaService ollamaService = new OllamaService();
    MaintenanceContextRepository repo = new MaintenanceContextRepository();
    private final PromptContextBuilder contextBuilder = new PromptContextBuilder(repo);

    // ============================================================
    // ENCABEZADO
    // ============================================================
    @FXML
    private Label lblFechaHoy;

    // ============================================================
    // TARJETAS KPI
    // ============================================================
    @FXML
    private Label lblTotalEquipos;
    @FXML
    private Label lblEquiposActivos;
    @FXML
    private Label lblEnMantenimiento;
    @FXML
    private Label lblEquiposBaja;

    // ============================================================
    // CHIPS DE ESTADO - Labels de conteo
    // ============================================================
    @FXML
    private Label lblEstadoActivos;
    @FXML
    private Label lblEstadoMantenimiento;
    @FXML
    private Label lblEstadoInactivos;
    @FXML
    private Label lblEstadoBaja;

    // ============================================================
    // CHIPS DE ESTADO - Nodos clickeables (NECESARIO para filtros)
    // ============================================================
    @FXML
    private Button chipTodos; // Button con onAction
    @FXML
    private HBox chipActivos; // HBox con onMouseClicked
    @FXML
    private HBox chipMantenimiento;// HBox con onMouseClicked
    @FXML
    private HBox chipInactivos; // HBox con onMouseClicked
    @FXML
    private HBox chipBaja; // HBox con onMouseClicked

    // ============================================================
    // FILTROS Y TABLA
    // ============================================================
    @FXML
    private TextField txtBuscar;
    @FXML
    private ComboBox<String> cmbTipo;
    @FXML
    private TableView<MantenimientoModel> tablaMantenimientos;
    @FXML
    private TableColumn<MantenimientoModel, String> colEquipo;
    @FXML
    private TableColumn<MantenimientoModel, String> colUsuario;
    @FXML
    private TableColumn<MantenimientoModel, String> colTipo;
    @FXML
    private TableColumn<MantenimientoModel, String> colMotivo;
    @FXML
    private TableColumn<MantenimientoModel, String> colTecnico;
    @FXML
    private TableColumn<MantenimientoModel, String> colFecha;
    @FXML
    private TableColumn<MantenimientoModel, String> colEstado;

    // ============================================================
    // MENU LATERAL
    // ============================================================
    @FXML
    private Region overlayMenu;
    @FXML
    private VBox panelMenu;

    // ============================================================
    // ASISTENTE IA
    // ============================================================
    @FXML
    private ScrollPane scrollChat;
    @FXML
    private VBox contenedorChat;
    @FXML
    private TextField txtPregunta;

    @FXML
    private Label lblAvatarUsuario, lblNombreUsuario, lblRolUsuario;

    // ============================================================
    // DATOS EN MEMORIA
    // ============================================================
    private final ObservableList<MantenimientoModel> datos = FXCollections.observableArrayList();
    private FilteredList<MantenimientoModel> datosFiltrados;
    private String estadoFiltro = "TODOS";
    private Long idTecnicoSesion;
    private String nombreTecnicoSesion;
    private static final String BIENVENIDA = "Hola Gerardo! Soy tu asistente del CGTI. Puedo consultar el historial de un "
            +
            "equipo, contar mantenimientos preventivos y correctivos del mes o resumir los " +
            "servicios por tecnico. Que necesitas?";

    // ============================================================
    // INICIALIZACION
    // ============================================================
    @FXML
    public void initialize() {

        idTecnicoSesion = SesionManager.getInstance().getIdUsuario();
        nombreTecnicoSesion = SesionManager.getInstance().getNombreCompleto();

        lblNombreUsuario.setText(nombreTecnicoSesion);
        lblRolUsuario.setText("Administrador CGTI");
        lblAvatarUsuario.setText(iniciales(nombreTecnicoSesion));
        configurarFecha();
        configurarTablaMantenimientos();
        configurarFiltros();
        configurarChips(); // <-- CLAVE: asigna userData a cada chip
        cargarDatosDesdeBaseDatos();
        cargarEstadisticas();
        configurarChat();
    }

    // ============================================================
    // FECHA
    // ============================================================
    private void configurarFecha() {
        DateTimeFormatter f = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale.of("es", "MX"));
        String texto = LocalDate.now().format(f);
        lblFechaHoy.setText(Character.toUpperCase(texto.charAt(0)) + texto.substring(1));
    }

    private String iniciales(String nombre) {
        String[] p = nombre.trim().split("\\s+");
        if (p.length == 1)
            return p[0].substring(0, Math.min(2, p[0].length())).toUpperCase();
        return ("" + p[0].charAt(0) + p[1].charAt(0)).toUpperCase();
    }

    // ============================================================
    // TABLA
    // ============================================================
    private void configurarTablaMantenimientos() {
        colEquipo.setCellValueFactory(new PropertyValueFactory<>("equipo"));
        colUsuario.setCellValueFactory(new PropertyValueFactory<>("usuario"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colMotivo.setCellValueFactory(new PropertyValueFactory<>("motivo"));
        colTecnico.setCellValueFactory(new PropertyValueFactory<>("tecnico"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        // Badge de color para Estado
        colEstado.setCellFactory(col -> new TableCell<MantenimientoModel, String>() {
            private final Label chip = new Label();

            @Override
            protected void updateItem(String estado, boolean vacio) {
                super.updateItem(estado, vacio);
                if (vacio || estado == null) {
                    setGraphic(null);
                    return;
                }
                chip.setText(estado);
                chip.setStyle(estiloBadge(estado));
                setGraphic(chip);
                setAlignment(Pos.CENTER_LEFT);
            }
        });

        // Color por tipo
        colTipo.setCellFactory(col -> new TableCell<MantenimientoModel, String>() {
            @Override
            protected void updateItem(String tipo, boolean vacio) {
                super.updateItem(tipo, vacio);
                setText(vacio ? null : tipo);
                if (vacio || tipo == null) {
                    setStyle("");
                } else if (tipo.equalsIgnoreCase("Correctivo")) {
                    setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
                } else if (tipo.equalsIgnoreCase("Preventivo")) {
                    setStyle("-fx-text-fill: #2563eb; -fx-font-weight: bold;");
                } else {
                    setStyle("-fx-text-fill: #7c3aed; -fx-font-weight: bold;");
                }
            }
        });

        tablaMantenimientos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tablaMantenimientos.setRowFactory(tv -> {
            javafx.scene.control.TableRow<MantenimientoModel> fila = new javafx.scene.control.TableRow<>();
            fila.setPrefHeight(40);
            return fila;
        });
    }

    private String estiloBadge(String estado) {
        String base = "-fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 20; " +
                "-fx-padding: 4 11 4 11; ";
        if (estado.equalsIgnoreCase("Activo"))
            return base + "-fx-background-color: #dcfce7; -fx-text-fill: #15803d;";
        if (estado.equalsIgnoreCase("En mantenimiento"))
            return base + "-fx-background-color: #fef3c7; -fx-text-fill: #b45309;";
        if (estado.equalsIgnoreCase("De baja"))
            return base + "-fx-background-color: #fee2e2; -fx-text-fill: #b91c1c;";
        return base + "-fx-background-color: #f1f5f9; -fx-text-fill: #475569;"; // Inactivo
    }

    // ============================================================
    // FILTROS
    // ============================================================
    private void configurarFiltros() {
        cmbTipo.getItems().addAll(
                "Todos los tipos", "Preventivo", "Correctivo",
                "Instalacion de software", "Configuracion");
        cmbTipo.getSelectionModel().selectFirst();

        datosFiltrados = new FilteredList<>(datos, m -> true);
        SortedList<MantenimientoModel> ordenados = new SortedList<>(datosFiltrados);
        ordenados.comparatorProperty().bind(tablaMantenimientos.comparatorProperty());
        tablaMantenimientos.setItems(ordenados);
    }

    /**
     * Asigna el userData a cada chip para que filtrarPorEstado sepa
     * que valor de filtro corresponde a cada uno.
     * SIN esto, getUserData() devuelve null y el filtro nunca funciona.
     */
    private void configurarChips() {
        chipTodos.setUserData("TODOS");
        chipActivos.setUserData("Activo");
        chipMantenimiento.setUserData("En mantenimiento");
        chipInactivos.setUserData("Inactivo");
        chipBaja.setUserData("De baja");

        // El chip "Todos" empieza activo visualmente
        resaltarChip(chipTodos);
    }

    /** Buscador (onKeyReleased) y ComboBox (onAction). */
    @FXML
    private void filtrarMantenimientos() {
        aplicarFiltros();
    }

    /** Chips de estado: Button "Todos" (onAction) y HBox chips (onMouseClicked). */
    @FXML
    private void filtrarPorEstado(Event e) {
        Object origen = e.getSource();
        if (!(origen instanceof Node))
            return;

        Node chipClicked = (Node) origen;
        Object userData = chipClicked.getUserData();

        // Lee el userData que configurarChips() asignó
        estadoFiltro = (userData == null) ? "TODOS" : userData.toString();

        // Resalta visualmente el chip activo
        resaltarChip(chipClicked);

        aplicarFiltros();
        System.out.println("Filtro por estado: " + estadoFiltro);
    }

    /**
     * Resalta el chip seleccionado y atenúa los demás.
     * Busca todos los chips en el mismo contenedor padre (HBox).
     */
    private void resaltarChip(Node chipActivo) {
        // Los chips están dentro de un HBox padre común
        if (chipActivo.getParent() instanceof HBox) {
            HBox contenedor = (HBox) chipActivo.getParent();
            for (Node n : contenedor.getChildrenUnmodifiable()) {
                n.setOpacity(n == chipActivo ? 1.0 : 0.50);
            }
        }
    }

    private void aplicarFiltros() {
        final String q = (txtBuscar.getText() == null)
                ? ""
                : txtBuscar.getText().trim().toLowerCase();
        final String tipo = cmbTipo.getValue();

        datosFiltrados.setPredicate(m -> {
            // Filtro de texto: busca en equipo, usuario, tecnico, motivo
            boolean texto = q.isEmpty()
                    || m.getEquipo().toLowerCase().contains(q)
                    || m.getUsuario().toLowerCase().contains(q)
                    || m.getTecnico().toLowerCase().contains(q)
                    || m.getMotivo().toLowerCase().contains(q);

            // Filtro por tipo del ComboBox
            boolean porTipo = tipo == null
                    || tipo.startsWith("Todos")
                    || m.getTipo().equalsIgnoreCase(tipo);

            // Filtro por estado del chip
            boolean porEstado = "TODOS".equals(estadoFiltro)
                    || m.getEstado().equalsIgnoreCase(estadoFiltro);

            return texto && porTipo && porEstado;
        });
    }

    // ============================================================
    // CARGA DE DATOS
    // ============================================================
    private void cargarDatosDesdeBaseDatos() {
        try {
            System.out.println("Cargando datos desde la base de datos...");
            ObservableList<MantenimientoModel> datosDB = MantenimientoRepository.obtenerTodos();

            if (datosDB != null && !datosDB.isEmpty()) {
                datos.setAll(datosDB);
                System.out.println("Se cargaron " + datos.size() + " mantenimientos desde BD");
            } else {
                System.out.println("BD vacia, cargando datos de demostracion...");
                cargarDatosDemo();
            }
        } catch (Exception e) {
            System.err.println("Error al cargar datos: " + e.getMessage());
            e.printStackTrace();
            cargarDatosDemo();
        }
    }

    private void cargarDatosDemo() {
        datos.setAll(
                new MantenimientoModel("PC-LAB-014", "Ana Ramirez", "Preventivo",
                        "Limpieza interna y cambio de pasta termica", "Luis Ortega", "12/08/2026", "Activo"),
                new MantenimientoModel("PC-ADM-003", "Jorge Medina", "Correctivo",
                        "No enciende, se sospecha de fuente", "Karla Nunez", "13/08/2026", "En mantenimiento"),
                new MantenimientoModel("LAP-DOC-021", "Mtra. Beltran", "Instalacion de software",
                        "Instalacion de MATLAB y actualizacion de SO", "Luis Ortega", "14/08/2026", "Activo"),
                new MantenimientoModel("PC-BIB-008", "Biblioteca", "Preventivo",
                        "Revision de disco y desfragmentacion", "Diego Salas", "17/08/2026", "Activo"),
                new MantenimientoModel("PC-LAB-002", "Lab. Redes", "Correctivo",
                        "Pantalla sin senal, cambio de tarjeta de video", "Karla Nunez", "18/08/2026",
                        "En mantenimiento"),
                new MantenimientoModel("PC-ADM-011", "Control Escolar", "Preventivo",
                        "Respaldo de informacion y limpieza", "Diego Salas", "20/08/2026", "Inactivo"),
                new MantenimientoModel("PC-LAB-030", "Lab. Software", "Correctivo",
                        "Equipo obsoleto, se propone baja", "Luis Ortega", "21/08/2026", "De baja"));
        System.out.println("Datos de demostracion cargados: " + datos.size());
    }

    // ============================================================
    // ESTADISTICAS
    // ============================================================
    private void cargarEstadisticas() {
        try {
            int[] stats = MantenimientoRepository.obtenerEstadisticas();

            Platform.runLater(() -> {
                lblTotalEquipos.setText(String.valueOf(stats[0]));
                lblEquiposActivos.setText(String.valueOf(stats[1]));
                lblEnMantenimiento.setText(String.valueOf(stats[2]));
                lblEquiposBaja.setText(String.valueOf(stats[4]));

                lblEstadoActivos.setText(String.valueOf(stats[1]));
                lblEstadoMantenimiento.setText(String.valueOf(stats[2]));
                lblEstadoInactivos.setText(String.valueOf(stats[3]));
                lblEstadoBaja.setText(String.valueOf(stats[4]));
            });

            System.out.println("Estadisticas cargadas desde BD");
        } catch (Exception e) {
            System.err.println("Error en estadisticas: " + e.getMessage());
            cargarEstadisticasLocal();
        }
    }

    private void cargarEstadisticasLocal() {
        long total = datos.stream().map(MantenimientoModel::getEquipo).distinct().count();
        long activos = contarPorEstado("Activo");
        long enMant = contarPorEstado("En mantenimiento");
        long inactivos = contarPorEstado("Inactivo");
        long deBaja = contarPorEstado("De baja");

        Platform.runLater(() -> {
            lblTotalEquipos.setText(String.valueOf(total));
            lblEquiposActivos.setText(String.valueOf(activos));
            lblEnMantenimiento.setText(String.valueOf(enMant));
            lblEquiposBaja.setText(String.valueOf(deBaja));
            lblEstadoActivos.setText(String.valueOf(activos));
            lblEstadoMantenimiento.setText(String.valueOf(enMant));
            lblEstadoInactivos.setText(String.valueOf(inactivos));
            lblEstadoBaja.setText(String.valueOf(deBaja));
        });
    }

    private long contarPorEstado(String estado) {
        return datos.stream().filter(m -> m.getEstado().equalsIgnoreCase(estado)).count();
    }

    // ============================================================
    // MENU LATERAL
    // ============================================================
    @FXML
    private void toggleMenu() {
        boolean abierto = panelMenu.isVisible();
        panelMenu.setVisible(!abierto);
        panelMenu.setManaged(!abierto);
        overlayMenu.setVisible(!abierto);
        overlayMenu.setManaged(!abierto);
    }

    @FXML
    private void irADashboard() {
        toggleMenu();
    }

    @FXML
    private void irAUsuarios() {
        toggleMenu();
        Navigator.navigate("/com/utng/ui/usuarioModules/pantallaUsuarios/PantallaUsuarios.fxml");
    }

    @FXML
    private void irASistemasOperativos() {
        toggleMenu();
        Navigator.navigate("/com/utng/ui/sitemasOperativosModules/PantallaSistemasOperativos/SistemasOperativos.fxml");

    }

    @FXML
    private void irAMantenimientos() {
        toggleMenu();
        Navigator.navigate(
                "/com/utng/ui/mantenimientoModules/pantallaMantenimientos/PantallaMantenimientos.fxml");
    }

    @FXML
    private void irAActualizaciones() {
        toggleMenu();
        Navigator.navigate(
                "/com/utng/ui/actualizacionModules/pantallaActualizaciones/PantallaActualizaciones.fxml");
    }

    @FXML
    private void irAEquipos() {
        toggleMenu();
        Navigator.navigate("/com/utng/ui/equipoModules/pantallaEquipos/PantallaEquipos.fxml");
    }

    @FXML
    private void cerrarSesion() {
        boolean confirmado = confirmar("Cerrar sesión",
                "¿Deseas salir del sistema?",
                "Se cerrará la sesión actual y volverás a la pantalla de inicio.");

        if (confirmado) {
            Navigator.navigate("/com/utng/ui/Auth/pantallaLogin/PantallaLogin.fxml");
        }
    }

    private boolean confirmar(String titulo, String encabezado, String detalle) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(encabezado);
        alerta.setContentText(detalle);
        alerta.getDialogPane().setMinWidth(480);
        prepararVentana(alerta);

        Optional<ButtonType> respuesta = alerta.showAndWait();
        return respuesta.isPresent() && respuesta.get() == ButtonType.OK;
    }

    private void prepararVentana(Dialog<?> dialogo) {

        dialogo.initModality(Modality.WINDOW_MODAL);
    }

    // ============================================================
    // ASISTENTE IA
    // ============================================================
    private void configurarChat() {
        contenedorChat.getChildren().clear();
        agregarMensajeIA(BIENVENIDA);
        contenedorChat.heightProperty().addListener((obs, a, b) -> scrollChat.setVvalue(1.0));
    }

    @FXML
    private void enviarPregunta() {
        String pregunta = txtPregunta.getText().trim();
        if (pregunta.isEmpty())
            return;

        agregarMensajeUsuario(pregunta);
        txtPregunta.clear();

        Task<String> task = new Task<>() {
            @Override
            protected String call() {
                String contexto;
                try {
                    contexto = OllamaConfig.ragEnabled()
                            ? contextBuilder.buildContext(pregunta)
                            : "";
                } catch (AppException ex) {
                    contexto = "No se pudo consultar la BD: " + ex.getMessage();
                }
                return ollamaService.generateConContexto(pregunta, contexto);
            }
        };

        task.setOnSucceeded(e -> agregarMensajeIA(task.getValue()));
        task.setOnFailed(e -> agregarMensajeIA("Error: " + task.getException().getMessage()));

        new Thread(task, "ollama-request").start();
    }

    @FXML
    private void usarSugerencia(ActionEvent e) {
        Button btn = (Button) e.getSource();
        txtPregunta.setText(btn.getText());
        txtPregunta.requestFocus();
        txtPregunta.positionCaret(txtPregunta.getText().length());
    }

    @FXML
    private void limpiarChat() {
        contenedorChat.getChildren().clear();
        agregarMensajeIA(BIENVENIDA);
    }

    private String responder(String pregunta) {
        String p = pregunta.toLowerCase();

        if (p.contains("correctivo")) {
            long n = datos.stream().filter(m -> m.getTipo().equalsIgnoreCase("Correctivo")).count();
            return "Se tienen " + n + " mantenimientos correctivos registrados.";
        }
        if (p.contains("preventivo")) {
            long n = datos.stream().filter(m -> m.getTipo().equalsIgnoreCase("Preventivo")).count();
            if (!datos.isEmpty())
                return "Hay " + n + " preventivos. El mas proximo es " +
                        datos.get(0).getEquipo() + " el " + datos.get(0).getFecha() + ".";
            return "Hay " + n + " mantenimientos preventivos.";
        }
        if (p.contains("tecnico")) {
            return "Servicios por tecnico:\n" + resumenPorTecnico();
        }
        if (p.contains("reporte") || p.contains("mes")) {
            return "Resumen: " + lblTotalEquipos.getText() + " equipos, " +
                    lblEquiposActivos.getText() + " activos, " +
                    lblEnMantenimiento.getText() + " en mantenimiento, " +
                    lblEquiposBaja.getText() + " de baja.";
        }
        return "Prueba con: reporte del mes, servicios por tecnico, mantenimientos correctivos.";
    }

    private String resumenPorTecnico() {
        StringBuilder sb = new StringBuilder();
        datos.stream().map(MantenimientoModel::getTecnico).distinct().sorted().forEach(t -> {
            long n = datos.stream().filter(m -> m.getTecnico().equals(t)).count();
            sb.append("- ").append(t).append(": ").append(n).append(" servicio(s)\n");
        });
        return sb.toString().trim();
    }

    // ============================================================
    // BURBUJAS DEL CHAT
    // ============================================================
    private void agregarMensajeIA(String textoMarkdown) {
        HBox burbuja = new HBox(8);
        burbuja.setAlignment(Pos.TOP_LEFT);

        Label avatar = new Label("IA");
        avatar.setStyle("-fx-font-size: 9.5px; -fx-font-weight: bold; -fx-text-fill: white; " +
                "-fx-background-color: linear-gradient(to bottom right, #8b5cf6, #6366f1); " +
                "-fx-background-radius: 14; -fx-min-width: 28; -fx-min-height: 28; -fx-alignment: center;");

        TextFlow contenido = MarkdownRenderer.render(textoMarkdown, 11.5, "#334155");
        contenido.setMaxWidth(600);
        contenido.setStyle("-fx-background-color: white; -fx-background-radius: 10 10 10 2; " +
                "-fx-padding: 10 13 10 13; -fx-border-color: #e8ecf2; -fx-border-radius: 10 10 10 2;");

        burbuja.getChildren().addAll(avatar, contenido);
        contenedorChat.getChildren().add(burbuja);
        scrollChat.layout();
        scrollChat.setVvalue(1.0);
    }

    private HBox agregarMensajeUsuario(String texto) {
        Label burbuja = new Label(texto);
        burbuja.setWrapText(true);
        burbuja.setMaxWidth(620);
        burbuja.setStyle("-fx-background-color: linear-gradient(to bottom right, #3b82f6, #1d4ed8);" +
                "-fx-background-radius: 12 12 2 12; -fx-padding: 12 15 12 15;" +
                "-fx-font-size: 12.5px; -fx-text-fill: white;");

        Label avatar = new Label("GE");
        avatar.setStyle("-fx-font-size: 10.5px; -fx-font-weight: bold; -fx-text-fill: #475569;" +
                "-fx-background-color: #e2e8f0; -fx-background-radius: 16;" +
                "-fx-min-width: 32; -fx-min-height: 32; -fx-alignment: center;");

        HBox fila = new HBox(10, burbuja, avatar);
        fila.setAlignment(Pos.TOP_RIGHT);
        fila.setPadding(new Insets(0));
        contenedorChat.getChildren().add(fila);
        return fila;
    }

    // ============================================================
    // BOTONES NAVEGACION
    // ============================================================
    @FXML
    private void nuevoMantenimiento() {
        Navigator.navigate("/com/utng/ui/mantenimientoModules/pantallaMantenimientos/PantallaMantenimientos.fxml");
    }

    @FXML
    private void verMantenimientos() {
        Navigator.navigate("/com/utng/ui/mantenimientoModules/pantallaMantenimientos/PantallaMantenimientos.fxml");

    }
}