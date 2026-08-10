package com.utng.DashboardModule.controller;

import com.utng.AiModule.OllamaService;
import com.utng.AiModule.PromptContextBuilder;
import com.utng.AiModule.data.MaintenanceContextRepository;
import com.utng.DashboardModule.model.MantenimientoModel;
import com.utng.config.OllamaConfig;
import com.utng.util.AppException;
import com.utng.util.MarkdownRenderer;
import com.utng.util.Navigator;

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
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
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
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class PantallaDashboardController {
    private final OllamaService ollamaService = new OllamaService();
    private final MaintenanceContextRepository repo = new MaintenanceContextRepository();
    private final PromptContextBuilder contextBuilder = new PromptContextBuilder(repo);

    // ============================================================
    // ENCABEZADO
    // ============================================================
    @FXML
    private Label lblFechaHoy;

    // ============================================================
    // TARJETAS PRINCIPALES
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
    // CHIPS DE ESTADO (antes panel "Estado de equipos")
    // ============================================================
    @FXML
    private Label lblEstadoActivos;
    @FXML
    private Label lblEstadoMantenimiento;
    @FXML
    private Label lblEstadoInactivos;
    @FXML
    private Label lblEstadoBaja;
    @FXML
    private Button chipTodos;

    // ============================================================
    // FILTROS Y TABLA DE MANTENIMIENTOS
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
    // ASISTENTE IA (antes "Actividad reciente")
    // ============================================================
    @FXML
    private ScrollPane scrollChat;
    @FXML
    private VBox contenedorChat;
    @FXML
    private TextField txtPregunta;

    // ============================================================
    // DATOS EN MEMORIA
    // ============================================================
    private final ObservableList<MantenimientoModel> datos = FXCollections.observableArrayList();
    private FilteredList<MantenimientoModel> datosFiltrados;
    private String estadoFiltro = "TODOS";

    private static final String BIENVENIDA = "Hola Gerardo \uD83D\uDC4B Soy tu asistente del CGTI. Puedo consultar el historial de un "
            + "equipo, contar mantenimientos preventivos y correctivos del mes o resumir los servicios "
            + "por técnico. ¿Qué necesitas?";

    // ============================================================
    // INICIALIZACIÓN
    // ============================================================
    @FXML
    public void initialize() {
        configurarFecha();
        configurarTablaMantenimientos();
        configurarFiltros();
        cargarDatosDemo(); // TODO: reemplazar por MantenimientoRepository (PostgreSQL)
        cargarEstadisticas();
        configurarChat();
    }

    // ============================================================
    // FECHA DEL ENCABEZADO
    // ============================================================
    private void configurarFecha() {
        DateTimeFormatter f = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", new Locale("es", "MX"));
        String texto = LocalDate.now().format(f);
        lblFechaHoy.setText(Character.toUpperCase(texto.charAt(0)) + texto.substring(1));
    }

    // ============================================================
    // CONFIGURACIÓN DE TABLA
    // ============================================================
    private void configurarTablaMantenimientos() {
        colEquipo.setCellValueFactory(new PropertyValueFactory<>("equipo"));
        colUsuario.setCellValueFactory(new PropertyValueFactory<>("usuario"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colMotivo.setCellValueFactory(new PropertyValueFactory<>("motivo"));
        colTecnico.setCellValueFactory(new PropertyValueFactory<>("tecnico"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        // Badge de color para la columna Estado
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

        // Resalta el tipo de servicio con color
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
        String base = "-fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 20; "
                + "-fx-padding: 4 11 4 11; ";
        if (estado.equalsIgnoreCase("Activo"))
            return base + "-fx-background-color: #dcfce7; -fx-text-fill: #15803d;";
        if (estado.equalsIgnoreCase("En mantenimiento"))
            return base + "-fx-background-color: #fef3c7; -fx-text-fill: #b45309;";
        if (estado.equalsIgnoreCase("De baja"))
            return base + "-fx-background-color: #fee2e2; -fx-text-fill: #b91c1c;";
        return base + "-fx-background-color: #f1f5f9; -fx-text-fill: #475569;";
    }

    // ============================================================
    // FILTROS (buscador + combo + chips)
    // ============================================================
    private void configurarFiltros() {
        cmbTipo.getItems().addAll(
                "Todos los tipos", "Preventivo", "Correctivo",
                "Instalación de software", "Configuración");
        cmbTipo.getSelectionModel().selectFirst();

        datosFiltrados = new FilteredList<>(datos, m -> true);
        SortedList<MantenimientoModel> ordenados = new SortedList<>(datosFiltrados);
        ordenados.comparatorProperty().bind(tablaMantenimientos.comparatorProperty());
        tablaMantenimientos.setItems(ordenados);
    }

    /** Buscador (onKeyReleased) y ComboBox (onAction). */
    @FXML
    private void filtrarMantenimientos() {
        aplicarFiltros();
    }

    /** Chips de estado: botón "Todos" (ActionEvent) y chips HBox (MouseEvent). */
    @FXML
    private void filtrarPorEstado(Event e) {
        Object origen = e.getSource();
        if (!(origen instanceof Node))
            return;

        Node chip = (Node) origen;
        Object etiqueta = chip.getUserData();
        estadoFiltro = (etiqueta == null) ? "TODOS" : etiqueta.toString();

        // Resalta el chip activo y atenúa los demás
        if (chip.getParent() instanceof HBox) {
            for (Node n : ((HBox) chip.getParent()).getChildrenUnmodifiable()) {
                n.setOpacity(n == chip ? 1.0 : 0.55);
            }
        }
        aplicarFiltros();
    }

    private void aplicarFiltros() {
        final String q = (txtBuscar.getText() == null) ? "" : txtBuscar.getText().trim().toLowerCase();
        final String tipo = cmbTipo.getValue();

        datosFiltrados.setPredicate(m -> {
            boolean texto = q.isEmpty()
                    || m.getEquipo().toLowerCase().contains(q)
                    || m.getUsuario().toLowerCase().contains(q)
                    || m.getTecnico().toLowerCase().contains(q)
                    || m.getMotivo().toLowerCase().contains(q);

            boolean porTipo = tipo == null || tipo.startsWith("Todos") || m.getTipo().equals(tipo);
            boolean porEstado = "TODOS".equals(estadoFiltro) || m.getEstado().equalsIgnoreCase(estadoFiltro);

            return texto && porTipo && porEstado;
        });
    }

    // ============================================================
    // DATOS DE PRUEBA
    // ============================================================
    private void cargarDatosDemo() {
        datos.setAll(
                new MantenimientoModel("PC-LAB-014", "Ana Ramírez", "Preventivo",
                        "Limpieza interna y cambio de pasta térmica", "Luis Ortega", "12/08/2026", "Activo"),
                new MantenimientoModel("PC-ADM-003", "Jorge Medina", "Correctivo",
                        "No enciende, se sospecha de fuente", "Karla Núñez", "13/08/2026", "En mantenimiento"),
                new MantenimientoModel("LAP-DOC-021", "Mtra. Beltrán", "Instalación de software",
                        "Instalación de MATLAB y actualización de SO", "Luis Ortega", "14/08/2026", "Activo"),
                new MantenimientoModel("PC-BIB-008", "Biblioteca", "Preventivo",
                        "Revisión de disco y desfragmentación", "Diego Salas", "17/08/2026", "Activo"),
                new MantenimientoModel("PC-LAB-002", "Lab. Redes", "Correctivo",
                        "Pantalla sin señal, cambio de tarjeta de video", "Karla Núñez", "18/08/2026",
                        "En mantenimiento"),
                new MantenimientoModel("PC-ADM-011", "Control Escolar", "Preventivo",
                        "Respaldo de información y limpieza", "Diego Salas", "20/08/2026", "Inactivo"),
                new MantenimientoModel("PC-LAB-030", "Lab. Software", "Correctivo",
                        "Equipo obsoleto, se propone baja", "Luis Ortega", "21/08/2026", "De baja"));
    }

    @FXML
    private Region overlayMenu;
    @FXML
    private VBox panelMenu;

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
        toggleMenu(); // cierra el menú, ya estás en dashboard
    }

    @FXML
    private void irAUsuarios() {
        toggleMenu();
        Navigator.navigate("/com/utng/ui/usuarioModules/pantallaUsuarios/PantallaUsuarios.fxml");

    }

    // ============================================================
    // ESTADÍSTICAS (calculadas desde los datos)
    // ============================================================
    private void cargarEstadisticas() {
        long total = datos.stream().map(MantenimientoModel::getEquipo).distinct().count();
        long activos = contarPorEstado("Activo");
        long enMant = contarPorEstado("En mantenimiento");
        long inactivos = contarPorEstado("Inactivo");
        long deBaja = contarPorEstado("De baja");

        lblTotalEquipos.setText(String.valueOf(total));
        lblEquiposActivos.setText(String.valueOf(activos));
        lblEnMantenimiento.setText(String.valueOf(enMant));
        lblEquiposBaja.setText(String.valueOf(deBaja));

        lblEstadoActivos.setText(String.valueOf(activos));
        lblEstadoMantenimiento.setText(String.valueOf(enMant));
        lblEstadoInactivos.setText(String.valueOf(inactivos));
        lblEstadoBaja.setText(String.valueOf(deBaja));
    }

    private long contarPorEstado(String estado) {
        return datos.stream().filter(m -> m.getEstado().equalsIgnoreCase(estado)).count();
    }

    // ============================================================
    // ASISTENTE IA
    // ============================================================
    private void configurarChat() {
        contenedorChat.getChildren().clear(); // quita las burbujas de plantilla del FXML
        agregarMensajeIA(BIENVENIDA);
        // Auto-scroll al final cuando entra un mensaje nuevo
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
                } catch (AppException e) {
                    contexto = "No se pudo consultar la base de datos: " + e.getMessage();
                }
                return ollamaService.generateConContexto(pregunta, contexto);
            }
        };

        task.setOnSucceeded(e -> agregarMensajeIA(task.getValue()));
        task.setOnFailed(e -> agregarMensajeIA("⚠ Error: " + task.getException().getMessage()));

        new Thread(task, "ollama-request").start();
    }

    @FXML
    private void usarSugerencia(ActionEvent e) {
        Button chip = (Button) e.getSource();
        txtPregunta.setText(chip.getText());
        txtPregunta.requestFocus();
        txtPregunta.positionCaret(txtPregunta.getText().length());
    }

    @FXML
    private void limpiarChat() {
        contenedorChat.getChildren().clear();
        agregarMensajeIA(BIENVENIDA);
    }

    /**
     * Respuestas locales basadas en palabras clave.
     * TODO: sustituir por consultas al Repository o por un servicio de IA.
     */
    private String responder(String pregunta) {
        String p = pregunta.toLowerCase();

        if (p.contains("correctivo")) {
            long n = datos.stream().filter(m -> m.getTipo().equalsIgnoreCase("Correctivo")).count();
            return "Se tienen " + n + " mantenimientos correctivos registrados en el periodo actual. "
                    + "Los equipos involucrados son: " + equiposPorTipo("Correctivo") + ".";
        }
        if (p.contains("preventivo")) {
            long n = datos.stream().filter(m -> m.getTipo().equalsIgnoreCase("Preventivo")).count();
            return "Hay " + n + " mantenimientos preventivos programados. "
                    + "El más próximo es a " + datos.get(0).getEquipo() + " el " + datos.get(0).getFecha() + ".";
        }
        if (p.contains("software") || p.contains("instalaci")) {
            long n = datos.stream()
                    .filter(m -> m.getTipo().equalsIgnoreCase("Instalación de software")).count();
            return "Se registran " + n + " instalaciones o actualizaciones de software pendientes.";
        }
        if (p.contains("técnico") || p.contains("tecnico")) {
            return "Servicios por técnico:\n" + resumenPorTecnico();
        }
        if (p.contains("historial") || p.contains("equipo") || p.contains("pc-") || p.contains("lap-")) {
            return "Escribe la clave del equipo en el buscador de “Próximos mantenimientos” "
                    + "para ver su historial completo, o dime la clave exacta (ej. PC-LAB-014).";
        }
        if (p.contains("reporte") || p.contains("mes")) {
            return "Resumen del periodo: " + lblTotalEquipos.getText() + " equipos registrados, "
                    + lblEquiposActivos.getText() + " activos, "
                    + lblEnMantenimiento.getText() + " en mantenimiento y "
                    + lblEquiposBaja.getText() + " de baja.";
        }
        return "Aún no tengo esa información conectada a la base de datos. "
                + "Prueba con: “reporte del mes”, “servicios por técnico” o "
                + "“mantenimientos correctivos”.";
    }

    private String equiposPorTipo(String tipo) {
        StringBuilder sb = new StringBuilder();
        datos.stream().filter(m -> m.getTipo().equalsIgnoreCase(tipo))
                .forEach(m -> sb.append(sb.length() == 0 ? "" : ", ").append(m.getEquipo()));
        return sb.toString();
    }

    private String resumenPorTecnico() {
        StringBuilder sb = new StringBuilder();
        datos.stream().map(MantenimientoModel::getTecnico).distinct().sorted().forEach(t -> {
            long n = datos.stream().filter(m -> m.getTecnico().equals(t)).count();
            sb.append("• ").append(t).append(": ").append(n).append(" servicio(s)\n");
        });
        return sb.toString().trim();
    }

    // ------------------------------------------------------------
    // Burbujas del chat
    // ------------------------------------------------------------
    private void agregarMensajeIA(String textoMarkdown) {
        HBox burbuja = new HBox(8);
        burbuja.setAlignment(Pos.TOP_LEFT);

        Label avatar = new Label("IA");
        avatar.setStyle("-fx-font-size: 9.5px; -fx-font-weight: bold; -fx-text-fill: white; "
                + "-fx-background-color: linear-gradient(to bottom right, #8b5cf6, #6366f1); "
                + "-fx-background-radius: 14; -fx-min-width: 28; -fx-min-height: 28; -fx-alignment: center;");

        TextFlow contenido = MarkdownRenderer.render(textoMarkdown, 11.5, "#334155");
        contenido.setMaxWidth(600);
        contenido.setStyle("-fx-background-color: white; -fx-background-radius: 10 10 10 2; "
                + "-fx-padding: 10 13 10 13; -fx-border-color: #e8ecf2; -fx-border-radius: 10 10 10 2;");

        burbuja.getChildren().addAll(avatar, contenido);
        contenedorChat.getChildren().add(burbuja);

        scrollChat.layout();
        scrollChat.setVvalue(1.0); // auto-scroll hacia abajo
    }

    private HBox agregarMensajeUsuario(String texto) {
        Label burbuja = new Label(texto);
        burbuja.setWrapText(true);
        burbuja.setMaxWidth(620);
        burbuja.setStyle("-fx-background-color: linear-gradient(to bottom right, #3b82f6, #1d4ed8);"
                + "-fx-background-radius: 12 12 2 12; -fx-padding: 12 15 12 15;"
                + "-fx-font-size: 12.5px; -fx-text-fill: white;");

        Label avatar = new Label("GE");
        avatar.setStyle("-fx-font-size: 10.5px; -fx-font-weight: bold; -fx-text-fill: #475569;"
                + "-fx-background-color: #e2e8f0; -fx-background-radius: 16;"
                + "-fx-min-width: 32; -fx-min-height: 32; -fx-alignment: center;");

        HBox fila = new HBox(10, burbuja, avatar);
        fila.setAlignment(Pos.TOP_RIGHT);
        fila.setPadding(new Insets(0));
        contenedorChat.getChildren().add(fila);
        return fila;
    }

    // ============================================================
    // BOTONES DE NAVEGACIÓN
    // ============================================================
    @FXML
    private void nuevoMantenimiento() {
        System.out.println("Botón: Nuevo mantenimiento");
        // TODO: abrir PantallaRegistroMantenimiento.fxml
    }

    @FXML
    private void verMantenimientos() {
        System.out.println("Botón: Ver mantenimientos");
        // TODO: navegar al módulo de mantenimientos
    }
}