package com.utng.SistemasOperativosModule.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.utng.EquipoModule.model.sistemaOperativo.SistemaOperativo;
import com.utng.SistemasOperativosModule.repository.SistemaOperativoRepository;
import com.utng.util.Navigator;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;

/**
 * Controlador de la pantalla del catálogo de SISTEMAS OPERATIVOS (CRUD).
 *
 * Todo (alta, consulta y edición) se hace con ventanas modales, igual que en la
 * pantalla de equipos.
 *
 * Los campos y validaciones siguen EXACTAMENTE la tabla
 * {@code sistemas_operativos} de DB.sql:
 *
 * <pre>
 * id              BIGSERIAL PRIMARY KEY
 * tipo            VARCHAR(50)
 * nombre          VARCHAR(100)  NOT NULL
 * version_actual  VARCHAR(50)   NOT NULL
 * UNIQUE (nombre, version_actual)
 * </pre>
 *
 * IMPORTANTE: por ahora trabaja con DATOS ESTÁTICOS EN MEMORIA.
 * Para conectarlo a PostgreSQL basta con sustituir las líneas marcadas
 * con "// TODO BD:" por llamadas a un SistemaOperativoRepository.
 */
public class Pantallasistemasoperativoscontroller {

    // ============================================================
    // CONSTANTES DE FILTRO
    // ============================================================
    private static final String CAMPO_TODOS = "Todos los campos";
    private static final String TIPO_TODOS = "Todos los tipos";
    private static final String NOMBRE_TODOS = "Todos los nombres";
    private final SistemaOperativoRepository sistemaOperativoRepository = new SistemaOperativoRepository();

    /** Opción del filtro para las filas con {@code tipo} NULL o vacío. */
    private static final String SIN_TIPO = "Sin tipo";

    /** Límites tomados de los VARCHAR de la tabla sistemas_operativos. */
    private static final int MAX_NOMBRE = 100;
    private static final int MAX_VERSION = 50;
    private static final int MAX_TIPO = 50;

    /** Sugerencias del ComboBox editable de tipo (la columna es texto libre). */
    private static final List<String> TIPOS_SUGERIDOS = List.of(
            "escritorio", "servidor", "movil", "virtualizacion", "embebido");

    /** Paleta para los chips y las insignias de tipo. {fondo, texto, borde} */
    private static final String[][] PALETA_TIPOS = {
            { "#eef2ff", "#1d4ed8", "#e0e7ff" },
            { "#dcfce7", "#15803d", "#bbf7d0" },
            { "#fef3c7", "#b45309", "#fde68a" },
            { "#fae8ff", "#a21caf", "#f5d0fe" },
            { "#e0f2fe", "#0369a1", "#bae6fd" },
            { "#ffe4e6", "#be123c", "#fecdd3" }
    };

    /** Colores para "Sin tipo". */
    private static final String[] COLOR_SIN_TIPO = { "#f1f5f9", "#64748b", "#e2e8f0" };

    // ============================================================
    // KPIs
    // ============================================================
    @FXML
    private Label lblTotalSistemas;
    @FXML
    private Label lblTotalNombres;
    @FXML
    private Label lblTotalTipos;
    @FXML
    private Label lblSinTipo;

    // ============================================================
    // BARRA BUSCADORA Y FILTROS
    // ============================================================
    @FXML
    private TextField txtBuscar;
    @FXML
    private ComboBox<String> cmbCampo;
    @FXML
    private ComboBox<String> cmbTipo;
    @FXML
    private ComboBox<String> cmbNombre;
    @FXML
    private HBox contenedorChips;
    @FXML
    private Button chipTodos;
    @FXML
    private Label lblResultados;

    // ============================================================
    // TABLA
    // ============================================================
    @FXML
    private TableView<SistemaOperativo> tablaSistemas;
    @FXML
    private TableColumn<SistemaOperativo, String> colId;
    @FXML
    private TableColumn<SistemaOperativo, String> colNombre;
    @FXML
    private TableColumn<SistemaOperativo, String> colVersion;
    @FXML
    private TableColumn<SistemaOperativo, String> colTipo;
    @FXML
    private TableColumn<SistemaOperativo, String> colEtiqueta;
    @FXML
    private TableColumn<SistemaOperativo, String> colAcciones;

    // ============================================================
    // BOTONERA
    // ============================================================
    @FXML
    private Button btnVer;
    @FXML
    private Button btnEditar;
    @FXML
    private Button btnEliminar;

    // ============================================================
    // MENÚ LATERAL
    // ============================================================
    @FXML
    private Region overlayMenu;
    @FXML
    private VBox panelMenu;

    // ============================================================
    // DATOS EN MEMORIA
    // ============================================================
    private final ObservableList<SistemaOperativo> datos = FXCollections.observableArrayList();

    private FilteredList<SistemaOperativo> datosFiltrados;

    /** Evita que la recarga programática de los ComboBox dispare el filtro. */
    private boolean actualizandoFiltros = false;

    // ============================================================
    // INICIALIZACIÓN
    // ============================================================
    @FXML
    public void initialize() {
        configurarTabla();
        configurarFiltros();
        cargarDatosDesdeBD(); // antes: cargarDatosEstaticos()
        refrescarCombosDeFiltro();
        refrescarChips();
        aplicarFiltros();
        cargarEstadisticas();
        actualizarEstadoBotones();
    }

    // ============================================================
    // CONFIGURACIÓN DE LA TABLA
    // ============================================================
    private void configurarTabla() {

        colId.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getIdSistemaOperativo() == null
                        ? "—"
                        : String.valueOf(c.getValue().getIdSistemaOperativo())));

        colNombre.setCellValueFactory(c -> new SimpleStringProperty(
                texto(c.getValue().getNombre())));

        colVersion.setCellValueFactory(c -> new SimpleStringProperty(
                valorODash(c.getValue().getVersionActual())));

        colTipo.setCellValueFactory(c -> new SimpleStringProperty(
                etiquetaTipo(c.getValue().getTipo())));

        colEtiqueta.setCellValueFactory(c -> new SimpleStringProperty(
                valorODash(c.getValue().getDescripcion())));

        // ---- Tipo con insignia de color ----
        colTipo.setCellFactory(col -> new TableCell<SistemaOperativo, String>() {
            private final Label chip = new Label();

            @Override
            protected void updateItem(String tipo, boolean vacio) {
                super.updateItem(tipo, vacio);
                if (vacio || tipo == null) {
                    setGraphic(null);
                    return;
                }
                chip.setText(tipo);
                chip.setStyle(estiloBadgeTipo(tipo));
                setGraphic(chip);
                setAlignment(Pos.CENTER_LEFT);
            }
        });

        // ---- Acciones por fila ----
        colAcciones.setCellValueFactory(c -> new SimpleStringProperty(""));
        colAcciones.setCellFactory(col -> new TableCell<SistemaOperativo, String>() {

            private final Button bVer = miniBoton("👁", "#f1f5f9", "#475569");
            private final Button bEditar = miniBoton("✏", "#eef2ff", "#1d4ed8");
            private final Button bEliminar = miniBoton("🗑", "#fee2e2", "#b91c1c");
            private final HBox caja = new HBox(6, bVer, bEditar, bEliminar);

            {
                caja.setAlignment(Pos.CENTER_LEFT);
                bVer.setTooltip(new Tooltip("Ver detalle"));
                bEditar.setTooltip(new Tooltip("Editar"));
                bEliminar.setTooltip(new Tooltip("Eliminar sistema operativo"));
            }

            @Override
            protected void updateItem(String valor, boolean vacio) {
                super.updateItem(valor, vacio);

                int fila = getIndex();
                if (vacio || fila < 0 || fila >= getTableView().getItems().size()) {
                    setGraphic(null);
                    return;
                }

                SistemaOperativo so = getTableView().getItems().get(fila);

                bVer.setOnAction(e -> mostrarDetalle(so));
                bEditar.setOnAction(e -> abrirEdicion(so));
                bEliminar.setOnAction(e -> eliminar(so));

                setGraphic(caja);
                setAlignment(Pos.CENTER_LEFT);
            }
        });

        tablaSistemas.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        // Doble clic sobre la fila -> editar
        tablaSistemas.setRowFactory(tv -> {
            TableRow<SistemaOperativo> fila = new TableRow<>();
            fila.setPrefHeight(40);
            fila.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !fila.isEmpty()) {
                    abrirEdicion(fila.getItem());
                }
            });
            return fila;
        });

        tablaSistemas.getSelectionModel().selectedItemProperty()
                .addListener((obs, anterior, actual) -> actualizarEstadoBotones());
    }

    // ============================================================
    // CONFIGURACIÓN DE FILTROS
    // ============================================================
    private void configurarFiltros() {

        cmbCampo.getItems().addAll(CAMPO_TODOS, "ID", "Nombre", "Versión actual", "Tipo");
        cmbCampo.getSelectionModel().selectFirst();

        cmbTipo.getItems().add(TIPO_TODOS);
        cmbTipo.getSelectionModel().selectFirst();

        cmbNombre.getItems().add(NOMBRE_TODOS);
        cmbNombre.getSelectionModel().selectFirst();

        datosFiltrados = new FilteredList<>(datos, so -> true);
        SortedList<SistemaOperativo> ordenados = new SortedList<>(datosFiltrados);
        ordenados.comparatorProperty().bind(tablaSistemas.comparatorProperty());
        tablaSistemas.setItems(ordenados);
    }

    /** Buscador (onKeyReleased) y ComboBox de filtros (onAction). */
    @FXML
    private void filtrarSistemas() {
        if (actualizandoFiltros) {
            return;
        }
        aplicarFiltros();
    }

    /**
     * Chips de tipo. El ComboBox de tipo es la única fuente de verdad del filtro.
     */
    @FXML
    private void filtrarPorTipo(ActionEvent e) {
        Object origen = e.getSource();

        if (origen == chipTodos) {
            cmbTipo.setValue(TIPO_TODOS);
        } else if (origen instanceof Button boton && boton.getUserData() instanceof String tipo) {
            cmbTipo.setValue(tipo);
        }

        aplicarFiltros();
    }

    @FXML
    private void limpiarFiltros() {
        txtBuscar.clear();
        cmbCampo.setValue(CAMPO_TODOS);
        cmbTipo.setValue(TIPO_TODOS);
        cmbNombre.setValue(NOMBRE_TODOS);
        aplicarFiltros();
    }

    private void aplicarFiltros() {
        if (datosFiltrados == null) {
            return;
        }

        final String q = (txtBuscar.getText() == null) ? "" : txtBuscar.getText().trim().toLowerCase();
        final String campo = cmbCampo.getValue();
        final String tipo = cmbTipo.getValue();
        final String nombre = cmbNombre.getValue();

        datosFiltrados.setPredicate(so -> coincideTexto(so, q, campo)
                && coincideTipo(so, tipo)
                && coincideNombre(so, nombre));

        resaltarChipActivo(tipo);
        actualizarResultados();
        actualizarEstadoBotones();
    }

    private boolean coincideTexto(SistemaOperativo so, String q, String campo) {
        if (q.isEmpty()) {
            return true;
        }
        String buscado = (campo == null) ? CAMPO_TODOS : campo;

        return switch (buscado) {
            case "ID" -> String.valueOf(so.getIdSistemaOperativo()).contains(q);
            case "Nombre" -> contiene(so.getNombre(), q);
            case "Versión actual" -> contiene(so.getVersionActual(), q);
            case "Tipo" -> contiene(etiquetaTipo(so.getTipo()), q);
            default -> contiene(so.getNombre(), q)
                    || contiene(so.getVersionActual(), q)
                    || contiene(etiquetaTipo(so.getTipo()), q)
                    || contiene(so.getDescripcion(), q)
                    || String.valueOf(so.getIdSistemaOperativo()).contains(q);
        };
    }

    private boolean coincideTipo(SistemaOperativo so, String tipo) {
        if (tipo == null || TIPO_TODOS.equals(tipo)) {
            return true;
        }
        if (SIN_TIPO.equals(tipo)) {
            return esVacio(so.getTipo());
        }
        return tipo.equalsIgnoreCase(texto(so.getTipo()).trim());
    }

    private boolean coincideNombre(SistemaOperativo so, String nombre) {
        if (nombre == null || NOMBRE_TODOS.equals(nombre)) {
            return true;
        }
        return nombre.equalsIgnoreCase(texto(so.getNombre()).trim());
    }

    private void actualizarResultados() {
        int visibles = datosFiltrados.size();
        lblResultados.setText(visibles == 1 ? "1 sistema" : visibles + " sistemas");
    }

    /**
     * Reconstruye las listas de tipos y nombres del filtro a partir de los datos
     * actuales (la columna tipo es texto libre, así que se descubre en tiempo
     * real).
     */
    private void refrescarCombosDeFiltro() {
        actualizandoFiltros = true;
        try {
            // ---- Tipos ----
            String tipoSeleccionado = cmbTipo.getValue();

            List<String> tipos = new ArrayList<>();
            tipos.add(TIPO_TODOS);
            tipos.addAll(tiposDistintos());
            if (hayRegistrosSinTipo()) {
                tipos.add(SIN_TIPO);
            }

            cmbTipo.getItems().setAll(tipos);
            cmbTipo.setValue(tipos.contains(tipoSeleccionado) ? tipoSeleccionado : TIPO_TODOS);

            // ---- Nombres ----
            String nombreSeleccionado = cmbNombre.getValue();

            List<String> nombres = new ArrayList<>();
            nombres.add(NOMBRE_TODOS);
            nombres.addAll(nombresDistintos());

            cmbNombre.getItems().setAll(nombres);
            cmbNombre.setValue(nombres.contains(nombreSeleccionado) ? nombreSeleccionado : NOMBRE_TODOS);

        } finally {
            actualizandoFiltros = false;
        }
    }

    /** Genera un chip por cada tipo existente, conservando el chip "Todos". */
    private void refrescarChips() {
        contenedorChips.getChildren().retainAll(chipTodos);

        for (String tipo : tiposDistintos()) {
            contenedorChips.getChildren().add(crearChip(tipo));
        }
        if (hayRegistrosSinTipo()) {
            contenedorChips.getChildren().add(crearChip(SIN_TIPO));
        }
    }

    private Button crearChip(String tipo) {
        Button chip = new Button(etiquetaTipo(tipo));
        chip.setUserData(tipo);

        String[] color = colorDeTipo(tipo);
        chip.setStyle("-fx-background-color: " + color[0] + "; -fx-text-fill: " + color[1] + ";"
                + "-fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 16;"
                + "-fx-border-color: " + color[2] + "; -fx-border-radius: 16;"
                + "-fx-padding: 5 13 5 13; -fx-cursor: hand;");

        chip.setOnAction(this::filtrarPorTipo);
        return chip;
    }

    private void resaltarChipActivo(String tipo) {
        boolean sinFiltro = (tipo == null || TIPO_TODOS.equals(tipo));

        for (Node n : contenedorChips.getChildren()) {
            if (!(n instanceof Button boton)) {
                continue;
            }
            boolean activo = (boton == chipTodos)
                    ? sinFiltro
                    : String.valueOf(boton.getUserData()).equalsIgnoreCase(tipo);

            boton.setOpacity(activo ? 1.0 : 0.5);
        }
    }

    // ============================================================
    // CRUD - CREAR
    // ============================================================
    @FXML
    private void nuevoSistema() {
        Optional<SistemaOperativo> resultado = abrirFormulario(null);

        resultado.ifPresent(nuevo -> {
            sistemaOperativoRepository.guardar(nuevo); // asigna el id real de la BD
            datos.add(nuevo);

            refrescar();
            tablaSistemas.getSelectionModel().select(nuevo);
            info("Sistema operativo registrado",
                    "Se dio de alta " + nuevo.getDescripcion() + " en el catálogo.");
        });
    }

    // ============================================================
    // CRUD - ACTUALIZAR
    // ============================================================
    @FXML
    private void editarSistema() {
        SistemaOperativo seleccionado = seleccionOAviso();
        if (seleccionado != null) {
            abrirEdicion(seleccionado);
        }
    }

    private void abrirEdicion(SistemaOperativo so) {
        if (so == null) {
            return;
        }
        Optional<SistemaOperativo> resultado = abrirFormulario(so);

        resultado.ifPresent(actualizado -> {
            sistemaOperativoRepository.actualizar(actualizado);
            refrescar();
            tablaSistemas.getSelectionModel().select(actualizado);
            info("Cambios guardados",
                    "Se actualizó el sistema operativo " + actualizado.getDescripcion() + ".");
        });
    }

    // ============================================================
    // CRUD - ELIMINAR (borrado físico)
    // ============================================================
    @FXML
    private void eliminarSistema() {
        SistemaOperativo seleccionado = seleccionOAviso();
        if (seleccionado != null) {
            eliminar(seleccionado);
        }
    }

    private void eliminar(SistemaOperativo so) {
        if (so == null) {
            return;
        }

        boolean confirmado = confirmar(
                "Eliminar sistema operativo",
                "¿Deseas eliminar definitivamente " + so.getDescripcion() + " del catálogo?",
                "Esta acción no se puede deshacer. Por la llave foránea "
                        + "equipos.id_sistema_operativo (ON DELETE SET NULL), los equipos que lo "
                        + "tuvieran asignado quedarán SIN sistema operativo, pero no se borrarán.");

        if (confirmado) {
            sistemaOperativoRepository.eliminar(so.getIdSistemaOperativo());
            datos.remove(so);
            refrescar();
            info("Sistema operativo eliminado",
                    "Se eliminó " + so.getDescripcion() + " del catálogo.");
        }
    }

    // ============================================================
    // CRUD - VER
    // ============================================================
    @FXML
    private void verSistema() {
        SistemaOperativo seleccionado = seleccionOAviso();
        if (seleccionado != null) {
            mostrarDetalle(seleccionado);
        }
    }

    private void mostrarDetalle(SistemaOperativo so) {
        if (so == null) {
            return;
        }

        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(9);
        grid.setPadding(new Insets(16, 20, 8, 20));

        int f = 0;
        agregarDetalle(grid, f++, "ID",
                so.getIdSistemaOperativo() == null ? "—" : String.valueOf(so.getIdSistemaOperativo()));
        agregarDetalle(grid, f++, "Nombre", texto(so.getNombre()));
        agregarDetalle(grid, f++, "Versión actual", valorODash(so.getVersionActual()));
        agregarDetalle(grid, f++, "Tipo", etiquetaTipo(so.getTipo()));
        agregarDetalle(grid, f, "Etiqueta", valorODash(so.getDescripcion()));

        Dialog<Void> dialogo = new Dialog<>();
        dialogo.setTitle("Detalle del sistema operativo");
        dialogo.setHeaderText(so.getDescripcion());
        dialogo.getDialogPane().setContent(grid);
        dialogo.getDialogPane().setMinWidth(460);
        dialogo.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        prepararVentana(dialogo);
        dialogo.showAndWait();
    }

    private void agregarDetalle(GridPane grid, int fila, String etiqueta, String valor) {
        Label lblEtiqueta = new Label(etiqueta);
        lblEtiqueta.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #64748b;");

        Label lblValor = new Label(valor);
        lblValor.setStyle("-fx-font-size: 12.5px; -fx-text-fill: #0f172a; -fx-font-weight: bold;");
        lblValor.setWrapText(true);

        grid.add(lblEtiqueta, 0, fila);
        grid.add(lblValor, 1, fila);
    }

    // ============================================================
    // FORMULARIO MODAL (CREAR / EDITAR)
    // ============================================================
    /**
     * @param base sistema operativo a editar, o {@code null} para registrar uno
     *             nuevo.
     * @return el registro creado/actualizado, o vacío si se canceló.
     */
    private Optional<SistemaOperativo> abrirFormulario(SistemaOperativo base) {

        final boolean esNuevo = (base == null);

        Dialog<SistemaOperativo> dialogo = new Dialog<>();
        dialogo.setTitle(esNuevo ? "Nuevo sistema operativo" : "Editar sistema operativo");
        dialogo.setHeaderText(esNuevo
                ? "Registra un nuevo sistema operativo en el catálogo"
                : "Modificando " + base.getDescripcion());

        ButtonType btnGuardar = new ButtonType(esNuevo ? "Registrar sistema" : "Guardar cambios",
                ButtonBar.ButtonData.OK_DONE);
        dialogo.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);

        // ---- Campos ----
        TextField txtNombre = campo("Ej. Windows 11 Pro");
        TextField txtVersion = campo("Ej. 23H2");

        // tipo es VARCHAR(50) libre: ComboBox editable con sugerencias
        ComboBox<String> cmbTipoForm = new ComboBox<>();
        cmbTipoForm.setEditable(true);
        cmbTipoForm.setPrefWidth(280);
        cmbTipoForm.setPromptText("Ej. escritorio (opcional)");
        cmbTipoForm.getItems().addAll(sugerenciasDeTipo());

        Label lblError = new Label();
        lblError.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 11.5px;");
        lblError.setWrapText(true);
        lblError.setMaxWidth(400);

        Label lblAyuda = new Label("La combinación Nombre + Versión actual no se puede repetir "
                + "(restricción UNIQUE de la base de datos).");
        lblAyuda.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 10.5px;");
        lblAyuda.setWrapText(true);
        lblAyuda.setMaxWidth(400);

        // ---- Precarga ----
        if (!esNuevo) {
            txtNombre.setText(texto(base.getNombre()));
            txtVersion.setText(texto(base.getVersionActual()));
            cmbTipoForm.setValue(texto(base.getTipo()));
        }

        // ---- Layout ----
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(18, 22, 6, 22));

        int f = 0;
        grid.add(etiqueta("Nombre *"), 0, f);
        grid.add(txtNombre, 1, f++);
        grid.add(etiqueta("Versión actual *"), 0, f);
        grid.add(txtVersion, 1, f++);
        grid.add(etiqueta("Tipo"), 0, f);
        grid.add(cmbTipoForm, 1, f++);
        grid.add(lblAyuda, 1, f++);
        grid.add(lblError, 1, f);

        dialogo.getDialogPane().setContent(grid);
        dialogo.getDialogPane().setMinWidth(520);
        prepararVentana(dialogo);

        javafx.application.Platform.runLater(txtNombre::requestFocus);

        // ---- Validación antes de cerrar ----
        Node nodoGuardar = dialogo.getDialogPane().lookupButton(btnGuardar);
        nodoGuardar.addEventFilter(ActionEvent.ACTION, evento -> {
            String error = validar(
                    base,
                    txtNombre.getText(),
                    txtVersion.getText(),
                    cmbTipoForm.getEditor().getText());

            if (error != null) {
                lblError.setText("⚠  " + error);
                evento.consume();
            }
        });

        // ---- Construcción del resultado ----
        dialogo.setResultConverter(boton -> {
            if (boton != btnGuardar) {
                return null;
            }

            SistemaOperativo so = esNuevo ? new SistemaOperativo() : base;

            so.setNombre(txtNombre.getText().trim());
            so.setVersionActual(txtVersion.getText().trim());
            so.setTipo(nuloSiVacio(cmbTipoForm.getEditor().getText()));

            return so;
        });

        return dialogo.showAndWait();
    }

    /** @return mensaje de error, o {@code null} si todo es válido. */
    private String validar(SistemaOperativo base, String nombre, String version, String tipo) {

        if (nombre == null || nombre.isBlank()) {
            return "El nombre es obligatorio.";
        }
        if (nombre.trim().length() > MAX_NOMBRE) {
            return "El nombre no puede exceder " + MAX_NOMBRE + " caracteres.";
        }
        if (version == null || version.isBlank()) {
            return "La versión actual es obligatoria.";
        }
        if (version.trim().length() > MAX_VERSION) {
            return "La versión actual no puede exceder " + MAX_VERSION + " caracteres.";
        }
        if (tipo != null && tipo.trim().length() > MAX_TIPO) {
            return "El tipo no puede exceder " + MAX_TIPO + " caracteres.";
        }
        if (duplicado(nombre.trim(), version.trim(), base)) {
            return "Ya existe ese sistema operativo con esa misma versión.";
        }

        return null;
    }

    /** Replica la restricción UNIQUE (nombre, version_actual) de la tabla. */
    private boolean duplicado(String nombre, String version, SistemaOperativo excluido) {
        for (SistemaOperativo so : datos) {
            if (so == excluido) {
                continue;
            }
            boolean mismoNombre = nombre.equalsIgnoreCase(texto(so.getNombre()).trim());
            boolean mismaVersion = version.equalsIgnoreCase(texto(so.getVersionActual()).trim());
            if (mismoNombre && mismaVersion) {
                return true;
            }
        }
        return false;
    }

    // ============================================================
    // ESTADÍSTICAS
    // ============================================================
    private void cargarEstadisticas() {
        lblTotalSistemas.setText(String.valueOf(datos.size()));
        lblTotalNombres.setText(String.valueOf(nombresDistintos().size()));
        lblTotalTipos.setText(String.valueOf(tiposDistintos().size()));
        lblSinTipo.setText(String.valueOf(
                datos.stream().filter(so -> esVacio(so.getTipo())).count()));
    }

    /** Recalcula filtros, chips, KPIs, contador y refresca las celdas. */
    private void refrescar() {
        tablaSistemas.refresh();
        refrescarCombosDeFiltro();
        refrescarChips();
        aplicarFiltros();
        cargarEstadisticas();
        actualizarEstadoBotones();
    }

    private List<String> tiposDistintos() {
        return datos.stream()
                .map(SistemaOperativo::getTipo)
                .filter(t -> !esVacio(t))
                .map(String::trim)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    private List<String> nombresDistintos() {
        return datos.stream()
                .map(SistemaOperativo::getNombre)
                .filter(n -> !esVacio(n))
                .map(String::trim)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    private boolean hayRegistrosSinTipo() {
        return datos.stream().anyMatch(so -> esVacio(so.getTipo()));
    }

    /** Sugerencias fijas + los tipos que ya existen en el catálogo. */
    private List<String> sugerenciasDeTipo() {
        List<String> lista = new ArrayList<>(TIPOS_SUGERIDOS);
        for (String tipo : tiposDistintos()) {
            boolean repetido = lista.stream().anyMatch(t -> t.equalsIgnoreCase(tipo));
            if (!repetido) {
                lista.add(tipo);
            }
        }
        return lista;
    }

    private void cargarDatosDesdeBD() {
        datos.setAll(sistemaOperativoRepository.obtenerTodos());
    }

    // ============================================================
    // NAVEGACIÓN (mismo esquema que el resto del proyecto)
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
        Navigator.navigate("/com/utng/ui/pantallaDashboard/PantallaDashboard.fxml");
    }

    @FXML
    private void irAUsuarios() {
        Navigator.navigate("/com/utng/ui/usuarioModules/pantallaUsuarios/PantallaUsuarios.fxml");
    }

    @FXML
    private void irAEquipos() {
        Navigator.navigate("/com/utng/ui/equipoModules/pantallaEquipos/PantallaEquipos.fxml");
    }

    @FXML
    private void irASistemasOperativos() {
        toggleMenu();
        Navigator.navigate(
                "/com/utng/ui/equipoModules/pantallaSistemasOperativos/PantallaSistemasOperativos.fxml");
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
    private void volver() {
        Navigator.goBack();
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

    // ============================================================
    // UTILIDADES DE UI
    // ============================================================
    private void actualizarEstadoBotones() {
        boolean hay = tablaSistemas.getSelectionModel().getSelectedItem() != null;

        btnVer.setDisable(!hay);
        btnEditar.setDisable(!hay);
        btnEliminar.setDisable(!hay);
    }

    private SistemaOperativo seleccionOAviso() {
        SistemaOperativo sel = tablaSistemas.getSelectionModel().getSelectedItem();
        if (sel == null) {
            info("Sin selección", "Selecciona primero un sistema operativo de la tabla.");
        }
        return sel;
    }

    private TextField campo(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setPrefWidth(280);
        return tf;
    }

    private Label etiqueta(String texto) {
        Label l = new Label(texto);
        l.setStyle("-fx-font-size: 12px; -fx-text-fill: #475569;");
        return l;
    }

    private Button miniBoton(String texto, String fondo, String color) {
        Button b = new Button(texto);
        b.setStyle(estiloMiniBoton(fondo, color));
        return b;
    }

    private String estiloMiniBoton(String fondo, String color) {
        return "-fx-background-color: " + fondo + "; -fx-text-fill: " + color + ";"
                + "-fx-font-size: 11px; -fx-background-radius: 6; -fx-cursor: hand;"
                + "-fx-padding: 3 8 3 8;";
    }

    private String estiloBadgeTipo(String tipo) {
        String[] color = colorDeTipo(tipo);
        return "-fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 20;"
                + "-fx-padding: 4 11 4 11;"
                + "-fx-background-color: " + color[0] + "; -fx-text-fill: " + color[1] + ";";
    }

    /**
     * Color estable por tipo: el mismo texto siempre recibe el mismo color, sin
     * necesidad de un ENUM (la columna tipo es VARCHAR libre).
     */
    private String[] colorDeTipo(String tipo) {
        if (esVacio(tipo) || SIN_TIPO.equalsIgnoreCase(tipo.trim())) {
            return COLOR_SIN_TIPO;
        }
        int indice = Math.floorMod(tipo.trim().toLowerCase().hashCode(), PALETA_TIPOS.length);
        return PALETA_TIPOS[indice];
    }

    /** Asocia el diálogo a la ventana principal para que se comporte como modal. */
    private void prepararVentana(Dialog<?> dialogo) {
        if (tablaSistemas.getScene() != null && tablaSistemas.getScene().getWindow() != null) {
            dialogo.initOwner(tablaSistemas.getScene().getWindow());
            dialogo.initModality(Modality.WINDOW_MODAL);
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

    private void info(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        prepararVentana(alerta);
        alerta.showAndWait();
    }

    // ============================================================
    // UTILIDADES DE DATOS
    // ============================================================
    /** "escritorio" -> "Escritorio"; null/vacío -> "Sin tipo". */
    private static String etiquetaTipo(String tipo) {
        if (esVacio(tipo)) {
            return SIN_TIPO;
        }
        String limpio = tipo.trim();
        return Character.toUpperCase(limpio.charAt(0)) + limpio.substring(1);
    }

    private static boolean esVacio(String valor) {
        return valor == null || valor.isBlank();
    }

    private static String texto(String valor) {
        return valor == null ? "" : valor;
    }

    private static String valorODash(String valor) {
        return esVacio(valor) ? "—" : valor;
    }

    private static String nuloSiVacio(String valor) {
        return esVacio(valor) ? null : valor.trim();
    }

    private static boolean contiene(String valor, String busqueda) {
        return valor != null && valor.toLowerCase().contains(busqueda);
    }

}
