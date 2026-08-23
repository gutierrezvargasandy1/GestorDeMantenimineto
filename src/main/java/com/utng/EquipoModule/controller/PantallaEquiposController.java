package com.utng.EquipoModule.controller;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.utng.EquipoModule.model.equipo.Equipo;
import com.utng.EquipoModule.model.equipo.EstadoEquipo;
import com.utng.EquipoModule.model.sistemaOperativo.SistemaOperativo;
import com.utng.UserModule.model.usuario.TipoUsuario;
import com.utng.UserModule.model.usuario.Usuario;
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
import javafx.util.StringConverter;
// agrega estos imports
import com.utng.EquipoModule.repository.EquipoRepository;
import com.utng.SistemasOperativosModule.repository.SistemaOperativoRepository;
import com.utng.UserModule.UsuarioRepository;

/**
 * Controlador de la pantalla de gestión de equipos (CRUD).
 *
 * Los campos y validaciones siguen EXACTAMENTE la tabla {@code equipos} de
 * DB.sql (longitudes de VARCHAR, SMALLINT del año y el ENUM estado_equipo),
 * de modo que al conectar el repositorio no haya que tocar la interfaz.
 *
 * IMPORTANTE: por ahora trabaja con DATOS ESTÁTICOS EN MEMORIA.
 * Para conectarlo a PostgreSQL basta con sustituir las líneas marcadas
 * con "// TODO BD:" por llamadas a un EquipoRepository.
 */
public class PantallaEquiposController {

    // ============================================================
    // CONSTANTES DE FILTRO
    // ============================================================
    private static final String CAMPO_TODOS = "Todos los campos";
    private static final String ESTADO_TODOS = "Todos los estados";
    private static final String LUGAR_TODOS = "Todos los lugares";
    // agrega estos campos junto a los catálogos existentes
    private final EquipoRepository equipoRepository = new EquipoRepository();
    private final SistemaOperativoRepository sistemaOperativoRepository = new SistemaOperativoRepository();
    private final UsuarioRepository usuarioRepository = new UsuarioRepository();

    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /** Límites tomados de los VARCHAR de la tabla equipos. */
    private static final int MAX_MODELO = 100;
    private static final int MAX_LUGAR = 150;
    private static final int MAX_ALMACENAMIENTO = 50;
    private static final int MAX_RAM = 50;
    private static final int MAX_PROCESADOR = 100;

    private static final int ANIO_MINIMO = 1990;

    /**
     * Opción "sin FK" para los ComboBox: representa un NULL en la base de datos.
     */
    private static final SistemaOperativo SIN_SISTEMA_OPERATIVO = new SistemaOperativo(null, null, "— Sin asignar —",
            "");
    private static final Usuario SIN_RESPONSABLE = crearResponsableVacio();

    // ============================================================
    // KPIs
    // ============================================================
    @FXML
    private Label lblTotalEquipos;
    @FXML
    private Label lblEquiposActivos;
    @FXML
    private Label lblEnMantenimiento;
    @FXML
    private Label lblInactivos;
    @FXML
    private Label lblDeBaja;

    @FXML
    private void irAEquipos() {
        toggleMenu();
        Navigator.navigate("/com/utng/ui/equipoModules/pantallaEquipos/PantallaEquipos.fxml");
    }

    // ============================================================
    // BARRA BUSCADORA Y FILTROS
    // ============================================================
    @FXML
    private TextField txtBuscar;
    @FXML
    private ComboBox<String> cmbCampo;
    @FXML
    private ComboBox<String> cmbEstado;
    @FXML
    private ComboBox<String> cmbLugar;
    @FXML
    private HBox contenedorChips;
    @FXML
    private Button chipTodos;
    @FXML
    private Button chipActivos;
    @FXML
    private Button chipMantenimiento;
    @FXML
    private Button chipInactivos;
    @FXML
    private Button chipBaja;
    @FXML
    private Label lblResultados;

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

    // ============================================================
    // TABLA
    // ============================================================
    @FXML
    private TableView<Equipo> tablaEquipos;
    @FXML
    private TableColumn<Equipo, String> colId;
    @FXML
    private TableColumn<Equipo, String> colModelo;
    @FXML
    private TableColumn<Equipo, String> colLugar;
    @FXML
    private TableColumn<Equipo, String> colProcesador;
    @FXML
    private TableColumn<Equipo, String> colRam;
    @FXML
    private TableColumn<Equipo, String> colAlmacenamiento;
    @FXML
    private TableColumn<Equipo, String> colAnio;
    @FXML
    private TableColumn<Equipo, String> colSistemaOp;
    @FXML
    private TableColumn<Equipo, String> colResponsable;
    @FXML
    private TableColumn<Equipo, String> colEstado;
    @FXML
    private TableColumn<Equipo, String> colAcciones;

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
    private final ObservableList<Equipo> datos = FXCollections.observableArrayList();
    private final List<SistemaOperativo> catalogoSistemasOperativos = new ArrayList<>();
    private final List<Usuario> catalogoResponsables = new ArrayList<>();

    private FilteredList<Equipo> datosFiltrados;

    /**
     * Evita que la recarga programática del ComboBox de lugares dispare el filtro.
     */
    private boolean actualizandoFiltros = false;

    // ============================================================
    // INICIALIZACIÓN
    // ============================================================
    @FXML
    public void initialize() {
        cargarCatalogos();
        configurarTabla();
        configurarFiltros();
        cargarEquiposDesdeBD();
        refrescarLugares();
        aplicarFiltros();
        cargarEstadisticas();
        actualizarEstadoBotones();
    }

    // ============================================================
    // CONFIGURACIÓN DE LA TABLA
    // ============================================================
    private void configurarTabla() {

        colId.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getIdEquipo() == null ? "—" : String.valueOf(c.getValue().getIdEquipo())));

        colModelo.setCellValueFactory(c -> new SimpleStringProperty(
                texto(c.getValue().getModelo())));

        colLugar.setCellValueFactory(c -> new SimpleStringProperty(
                valorODash(c.getValue().getLugar())));

        colProcesador.setCellValueFactory(c -> new SimpleStringProperty(
                valorODash(c.getValue().getProcesador())));

        colRam.setCellValueFactory(c -> new SimpleStringProperty(
                valorODash(c.getValue().getMemoriaRam())));

        colAlmacenamiento.setCellValueFactory(c -> new SimpleStringProperty(
                valorODash(c.getValue().getAlmacenamiento())));

        colAnio.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getAnioCreacion() == null ? "—" : String.valueOf(c.getValue().getAnioCreacion())));

        colSistemaOp.setCellValueFactory(c -> new SimpleStringProperty(
                nombreSistemaOperativo(c.getValue().getIdSistemaOperativo())));

        colResponsable.setCellValueFactory(c -> new SimpleStringProperty(
                nombreResponsable(c.getValue().getIdUsuarioResponsable())));

        colEstado.setCellValueFactory(c -> new SimpleStringProperty(
                etiquetaEstado(c.getValue().getEstado())));

        // ---- Estado con badge de color ----
        colEstado.setCellFactory(col -> new TableCell<Equipo, String>() {
            private final Label chip = new Label();

            @Override
            protected void updateItem(String estado, boolean vacio) {
                super.updateItem(estado, vacio);
                if (vacio || estado == null) {
                    setGraphic(null);
                    return;
                }
                chip.setText(estado);
                chip.setStyle(estiloBadgeEstado(estado));
                setGraphic(chip);
                setAlignment(Pos.CENTER_LEFT);
            }
        });

        // ---- Acciones por fila ----
        colAcciones.setCellValueFactory(c -> new SimpleStringProperty(""));
        colAcciones.setCellFactory(col -> new TableCell<Equipo, String>() {

            private final Button bVer = miniBoton("👁", "#f1f5f9", "#475569");
            private final Button bEditar = miniBoton("✏", "#eef2ff", "#1d4ed8");
            private final Button bEliminar = miniBoton("🗑", "#fee2e2", "#b91c1c");
            private final HBox caja = new HBox(6, bVer, bEditar, bEliminar);

            {
                caja.setAlignment(Pos.CENTER_LEFT);
                bVer.setTooltip(new Tooltip("Ver detalle"));
                bEditar.setTooltip(new Tooltip("Editar"));
                bEliminar.setTooltip(new Tooltip("Eliminar equipo"));
            }

            @Override
            protected void updateItem(String valor, boolean vacio) {
                super.updateItem(valor, vacio);

                int fila = getIndex();
                if (vacio || fila < 0 || fila >= getTableView().getItems().size()) {
                    setGraphic(null);
                    return;
                }

                Equipo equipo = getTableView().getItems().get(fila);

                bVer.setOnAction(e -> mostrarDetalle(equipo));
                bEditar.setOnAction(e -> abrirEdicion(equipo));
                bEliminar.setOnAction(e -> eliminar(equipo));

                setGraphic(caja);
                setAlignment(Pos.CENTER_LEFT);
            }
        });

        tablaEquipos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        // Doble clic sobre la fila -> editar
        tablaEquipos.setRowFactory(tv -> {
            TableRow<Equipo> fila = new TableRow<>();
            fila.setPrefHeight(40);
            fila.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !fila.isEmpty()) {
                    abrirEdicion(fila.getItem());
                }
            });
            return fila;
        });

        tablaEquipos.getSelectionModel().selectedItemProperty()
                .addListener((obs, anterior, actual) -> actualizarEstadoBotones());
    }

    // ============================================================
    // CONFIGURACIÓN DE FILTROS
    // ============================================================
    private void configurarFiltros() {

        cmbCampo.getItems().addAll(CAMPO_TODOS, "ID", "Modelo", "Lugar", "Procesador",
                "Memoria RAM", "Almacenamiento", "Sistema operativo", "Responsable");
        cmbCampo.getSelectionModel().selectFirst();

        cmbEstado.getItems().add(ESTADO_TODOS);
        for (EstadoEquipo estado : EstadoEquipo.values()) {
            cmbEstado.getItems().add(estado.getEtiqueta());
        }
        cmbEstado.getSelectionModel().selectFirst();

        cmbLugar.getItems().add(LUGAR_TODOS);
        cmbLugar.getSelectionModel().selectFirst();

        datosFiltrados = new FilteredList<>(datos, e -> true);
        SortedList<Equipo> ordenados = new SortedList<>(datosFiltrados);
        ordenados.comparatorProperty().bind(tablaEquipos.comparatorProperty());
        tablaEquipos.setItems(ordenados);
    }

    /** Buscador (onKeyReleased) y ComboBox de filtros (onAction). */
    @FXML
    private void filtrarEquipos() {
        if (actualizandoFiltros) {
            return;
        }
        aplicarFiltros();
    }

    /**
     * Chips de estado. El ComboBox de estado es la única fuente de verdad del
     * filtro.
     */
    @FXML
    private void filtrarPorEstado(ActionEvent e) {
        Object origen = e.getSource();

        if (origen == chipActivos) {
            cmbEstado.setValue(EstadoEquipo.ACTIVO.getEtiqueta());
        } else if (origen == chipMantenimiento) {
            cmbEstado.setValue(EstadoEquipo.EN_MANTENIMIENTO.getEtiqueta());
        } else if (origen == chipInactivos) {
            cmbEstado.setValue(EstadoEquipo.INACTIVO.getEtiqueta());
        } else if (origen == chipBaja) {
            cmbEstado.setValue(EstadoEquipo.DE_BAJA.getEtiqueta());
        } else {
            cmbEstado.setValue(ESTADO_TODOS);
        }

        aplicarFiltros();
    }

    @FXML
    private void limpiarFiltros() {
        txtBuscar.clear();
        cmbCampo.setValue(CAMPO_TODOS);
        cmbEstado.setValue(ESTADO_TODOS);
        cmbLugar.setValue(LUGAR_TODOS);
        aplicarFiltros();
    }

    private void aplicarFiltros() {
        if (datosFiltrados == null) {
            return;
        }

        final String q = (txtBuscar.getText() == null) ? "" : txtBuscar.getText().trim().toLowerCase();
        final String campo = cmbCampo.getValue();
        final String estado = cmbEstado.getValue();
        final String lugar = cmbLugar.getValue();

        datosFiltrados.setPredicate(equipo -> coincideTexto(equipo, q, campo)
                && coincideEstado(equipo, estado)
                && coincideLugar(equipo, lugar));

        resaltarChipActivo(estado);
        actualizarResultados();
        actualizarEstadoBotones();
    }

    private boolean coincideTexto(Equipo e, String q, String campo) {
        if (q.isEmpty()) {
            return true;
        }
        String buscado = (campo == null) ? CAMPO_TODOS : campo;

        return switch (buscado) {
            case "ID" -> String.valueOf(e.getIdEquipo()).contains(q);
            case "Modelo" -> contiene(e.getModelo(), q);
            case "Lugar" -> contiene(e.getLugar(), q);
            case "Procesador" -> contiene(e.getProcesador(), q);
            case "Memoria RAM" -> contiene(e.getMemoriaRam(), q);
            case "Almacenamiento" -> contiene(e.getAlmacenamiento(), q);
            case "Sistema operativo" -> contiene(nombreSistemaOperativo(e.getIdSistemaOperativo()), q);
            case "Responsable" -> contiene(nombreResponsable(e.getIdUsuarioResponsable()), q);
            default -> contiene(e.getModelo(), q)
                    || contiene(e.getLugar(), q)
                    || contiene(e.getProcesador(), q)
                    || contiene(e.getMemoriaRam(), q)
                    || contiene(e.getAlmacenamiento(), q)
                    || contiene(nombreSistemaOperativo(e.getIdSistemaOperativo()), q)
                    || contiene(nombreResponsable(e.getIdUsuarioResponsable()), q)
                    || contiene(etiquetaEstado(e.getEstado()), q)
                    || String.valueOf(e.getIdEquipo()).contains(q)
                    || (e.getAnioCreacion() != null && String.valueOf(e.getAnioCreacion()).contains(q));
        };
    }

    private boolean coincideEstado(Equipo e, String estado) {
        if (estado == null || ESTADO_TODOS.equals(estado)) {
            return true;
        }
        return etiquetaEstado(e.getEstado()).equalsIgnoreCase(estado);
    }

    private boolean coincideLugar(Equipo e, String lugar) {
        if (lugar == null || LUGAR_TODOS.equals(lugar)) {
            return true;
        }
        return lugar.equalsIgnoreCase(texto(e.getLugar()).trim());
    }

    private void resaltarChipActivo(String estado) {
        Button activo = chipTodos;

        if (EstadoEquipo.ACTIVO.getEtiqueta().equalsIgnoreCase(estado)) {
            activo = chipActivos;
        } else if (EstadoEquipo.EN_MANTENIMIENTO.getEtiqueta().equalsIgnoreCase(estado)) {
            activo = chipMantenimiento;
        } else if (EstadoEquipo.INACTIVO.getEtiqueta().equalsIgnoreCase(estado)) {
            activo = chipInactivos;
        } else if (EstadoEquipo.DE_BAJA.getEtiqueta().equalsIgnoreCase(estado)) {
            activo = chipBaja;
        }

        for (Node n : contenedorChips.getChildren()) {
            if (n instanceof Button) {
                n.setOpacity(n == activo ? 1.0 : 0.5);
            }
        }
    }

    private void actualizarResultados() {
        int visibles = datosFiltrados.size();
        lblResultados.setText(visibles == 1 ? "1 equipo" : visibles + " equipos");
    }

    /**
     * Reconstruye la lista de lugares del filtro a partir de los datos actuales.
     */
    private void refrescarLugares() {
        actualizandoFiltros = true;
        try {
            String seleccion = cmbLugar.getValue();

            List<String> items = new ArrayList<>();
            items.add(LUGAR_TODOS);

            datos.stream()
                    .map(Equipo::getLugar)
                    .filter(l -> l != null && !l.isBlank())
                    .map(String::trim)
                    .distinct()
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .forEach(items::add);

            cmbLugar.getItems().setAll(items);
            cmbLugar.setValue(items.contains(seleccion) ? seleccion : LUGAR_TODOS);

        } finally {
            actualizandoFiltros = false;
        }
    }

    // ============================================================
    // CRUD - CREAR
    // ============================================================
    @FXML
    private void nuevoEquipo() {
        Optional<Equipo> resultado = abrirFormulario(null);

        resultado.ifPresent(nuevo -> {
            equipoRepository.guardar(nuevo); // asigna id y fechas reales de la BD
            datos.add(nuevo);

            refrescar();
            tablaEquipos.getSelectionModel().select(nuevo);
            info("Equipo registrado", "Se dio de alta el equipo " + nuevo.getModelo() + ".");
        });
    }

    // ============================================================
    // CRUD - ACTUALIZAR
    // ============================================================
    @FXML
    private void editarEquipo() {
        Equipo seleccionado = seleccionOAviso();
        if (seleccionado != null) {
            abrirEdicion(seleccionado);
        }
    }

    private void abrirEdicion(Equipo equipo) {
        if (equipo == null) {
            return;
        }
        Optional<Equipo> resultado = abrirFormulario(equipo);

        resultado.ifPresent(actualizado -> {
            equipoRepository.actualizar(actualizado);

            // el trigger trg_equipos_actualizado puso la fecha en la BD; la traemos para
            // reflejarla
            Equipo recargado = equipoRepository.buscarPorId(actualizado.getIdEquipo());
            if (recargado != null) {
                actualizado.setFechaActualizacion(recargado.getFechaActualizacion());
            }

            refrescar();
            tablaEquipos.getSelectionModel().select(actualizado);
            info("Cambios guardados", "Se actualizó el equipo " + actualizado.getModelo() + ".");
        });
    }

    // ============================================================
    // CRUD - ELIMINAR (borrado físico)
    // ============================================================
    @FXML
    private void eliminarEquipo() {
        Equipo seleccionado = seleccionOAviso();
        if (seleccionado != null) {
            eliminar(seleccionado);
        }
    }

    private void eliminar(Equipo equipo) {
        if (equipo == null) {
            return;
        }

        boolean confirmado = confirmar(
                "Eliminar equipo",
                "¿Deseas eliminar definitivamente el equipo " + descripcionCorta(equipo) + "?",
                "Esta acción no se puede deshacer. Por las llaves foráneas ON DELETE CASCADE "
                        + "también se borrarán sus registros de mantenimiento, sus registros de "
                        + "actualizaciones, su historial y los programas instalados asociados.");

        if (confirmado) {
            equipoRepository.eliminar(equipo.getIdEquipo());
            datos.remove(equipo);
            refrescar();
            info("Equipo eliminado", "Se eliminó el equipo " + descripcionCorta(equipo) + ".");
        }
    }

    // ============================================================
    // CRUD - VER
    // ============================================================
    @FXML
    private void verEquipo() {
        Equipo seleccionado = seleccionOAviso();
        if (seleccionado != null) {
            mostrarDetalle(seleccionado);
        }
    }

    private void mostrarDetalle(Equipo e) {
        if (e == null) {
            return;
        }

        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(9);
        grid.setPadding(new Insets(16, 20, 8, 20));

        int f = 0;
        agregarDetalle(grid, f++, "ID", e.getIdEquipo() == null ? "—" : String.valueOf(e.getIdEquipo()));
        agregarDetalle(grid, f++, "Modelo", texto(e.getModelo()));
        agregarDetalle(grid, f++, "Lugar", valorODash(e.getLugar()));
        agregarDetalle(grid, f++, "Procesador", valorODash(e.getProcesador()));
        agregarDetalle(grid, f++, "Memoria RAM", valorODash(e.getMemoriaRam()));
        agregarDetalle(grid, f++, "Almacenamiento", valorODash(e.getAlmacenamiento()));
        agregarDetalle(grid, f++, "Año de creación",
                e.getAnioCreacion() == null ? "—" : String.valueOf(e.getAnioCreacion()));
        agregarDetalle(grid, f++, "Sistema operativo", nombreSistemaOperativo(e.getIdSistemaOperativo()));
        agregarDetalle(grid, f++, "Responsable", nombreResponsable(e.getIdUsuarioResponsable()));
        agregarDetalle(grid, f++, "Estado", etiquetaEstado(e.getEstado()));
        agregarDetalle(grid, f++, "Fecha de alta", formatear(e.getFechaCreacion()));
        agregarDetalle(grid, f, "Última actualización", formatear(e.getFechaActualizacion()));

        Dialog<Void> dialogo = new Dialog<>();
        dialogo.setTitle("Detalle del equipo");
        dialogo.setHeaderText(descripcionCorta(e));
        dialogo.getDialogPane().setContent(grid);
        dialogo.getDialogPane().setMinWidth(480);
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
     * @param base equipo a editar, o {@code null} para registrar uno nuevo.
     * @return el equipo creado/actualizado, o vacío si se canceló.
     */
    private Optional<Equipo> abrirFormulario(Equipo base) {

        final boolean esNuevo = (base == null);

        Dialog<Equipo> dialogo = new Dialog<>();
        dialogo.setTitle(esNuevo ? "Nuevo equipo" : "Editar equipo");
        dialogo.setHeaderText(esNuevo
                ? "Registra un nuevo equipo en el inventario"
                : "Modificando el equipo " + descripcionCorta(base));

        ButtonType btnGuardar = new ButtonType(esNuevo ? "Registrar equipo" : "Guardar cambios",
                ButtonBar.ButtonData.OK_DONE);
        dialogo.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);

        // ---- Campos ----
        TextField txtModelo = campo("Ej. HP ProDesk 400 G7");
        TextField txtLugar = campo("Ej. Laboratorio de Redes");
        TextField txtProcesador = campo("Ej. Intel Core i5-10500");
        TextField txtRam = campo("Ej. 16GB");
        TextField txtAlmacenamiento = campo("Ej. 512GB SSD");
        TextField txtAnio = campo("Ej. 2021");

        ComboBox<EstadoEquipo> cmbEstadoForm = new ComboBox<>();
        cmbEstadoForm.getItems().addAll(EstadoEquipo.values());
        cmbEstadoForm.setPrefWidth(280);
        cmbEstadoForm.setConverter(new StringConverter<EstadoEquipo>() {
            @Override
            public String toString(EstadoEquipo estado) {
                return estado == null ? "" : estado.getEtiqueta();
            }

            @Override
            public EstadoEquipo fromString(String s) {
                return EstadoEquipo.fromEtiqueta(s);
            }
        });

        ComboBox<SistemaOperativo> cmbSistemaOp = new ComboBox<>();
        cmbSistemaOp.getItems().add(SIN_SISTEMA_OPERATIVO);
        cmbSistemaOp.getItems().addAll(catalogoSistemasOperativos);
        cmbSistemaOp.setPrefWidth(280);
        cmbSistemaOp.setConverter(new StringConverter<SistemaOperativo>() {
            @Override
            public String toString(SistemaOperativo so) {
                return so == null ? "" : so.getDescripcion();
            }

            @Override
            public SistemaOperativo fromString(String s) {
                return null;
            }
        });

        ComboBox<Usuario> cmbResponsable = new ComboBox<>();
        cmbResponsable.getItems().add(SIN_RESPONSABLE);
        cmbResponsable.getItems().addAll(catalogoResponsables);
        cmbResponsable.setPrefWidth(280);
        cmbResponsable.setConverter(new StringConverter<Usuario>() {
            @Override
            public String toString(Usuario u) {
                return u == null ? "" : nombreCompleto(u);
            }

            @Override
            public Usuario fromString(String s) {
                return null;
            }
        });

        Label lblError = new Label();
        lblError.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 11.5px;");
        lblError.setWrapText(true);
        lblError.setMaxWidth(400);

        // ---- Precarga ----
        if (esNuevo) {
            cmbEstadoForm.setValue(EstadoEquipo.ACTIVO);
            cmbSistemaOp.setValue(SIN_SISTEMA_OPERATIVO);
            cmbResponsable.setValue(SIN_RESPONSABLE);
        } else {
            txtModelo.setText(texto(base.getModelo()));
            txtLugar.setText(texto(base.getLugar()));
            txtProcesador.setText(texto(base.getProcesador()));
            txtRam.setText(texto(base.getMemoriaRam()));
            txtAlmacenamiento.setText(texto(base.getAlmacenamiento()));
            txtAnio.setText(base.getAnioCreacion() == null ? "" : String.valueOf(base.getAnioCreacion()));
            cmbEstadoForm.setValue(base.getEstado() == null ? EstadoEquipo.ACTIVO : base.getEstado());
            cmbSistemaOp.setValue(buscarSistemaOperativo(base.getIdSistemaOperativo()));
            cmbResponsable.setValue(buscarResponsable(base.getIdUsuarioResponsable()));
        }

        // ---- Layout ----
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(18, 22, 6, 22));

        int f = 0;
        grid.add(etiqueta("Modelo *"), 0, f);
        grid.add(txtModelo, 1, f++);
        grid.add(etiqueta("Lugar"), 0, f);
        grid.add(txtLugar, 1, f++);
        grid.add(etiqueta("Procesador"), 0, f);
        grid.add(txtProcesador, 1, f++);
        grid.add(etiqueta("Memoria RAM"), 0, f);
        grid.add(txtRam, 1, f++);
        grid.add(etiqueta("Almacenamiento"), 0, f);
        grid.add(txtAlmacenamiento, 1, f++);
        grid.add(etiqueta("Año de creación"), 0, f);
        grid.add(txtAnio, 1, f++);
        grid.add(etiqueta("Sistema operativo"), 0, f);
        grid.add(cmbSistemaOp, 1, f++);
        grid.add(etiqueta("Responsable"), 0, f);
        grid.add(cmbResponsable, 1, f++);
        grid.add(etiqueta("Estado *"), 0, f);
        grid.add(cmbEstadoForm, 1, f++);
        grid.add(lblError, 1, f);

        dialogo.getDialogPane().setContent(grid);
        dialogo.getDialogPane().setMinWidth(540);
        prepararVentana(dialogo);

        javafx.application.Platform.runLater(txtModelo::requestFocus);

        // ---- Validación antes de cerrar ----
        Node nodoGuardar = dialogo.getDialogPane().lookupButton(btnGuardar);
        nodoGuardar.addEventFilter(ActionEvent.ACTION, evento -> {
            String error = validar(
                    base,
                    txtModelo.getText(),
                    txtLugar.getText(),
                    txtProcesador.getText(),
                    txtRam.getText(),
                    txtAlmacenamiento.getText(),
                    txtAnio.getText(),
                    cmbEstadoForm.getValue());

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

            Equipo equipo = esNuevo ? new Equipo() : base;

            equipo.setModelo(txtModelo.getText().trim());
            equipo.setLugar(nuloSiVacio(txtLugar.getText()));
            equipo.setProcesador(nuloSiVacio(txtProcesador.getText()));
            equipo.setMemoriaRam(nuloSiVacio(txtRam.getText()));
            equipo.setAlmacenamiento(nuloSiVacio(txtAlmacenamiento.getText()));
            equipo.setAnioCreacion(parsearAnio(txtAnio.getText()));
            equipo.setEstado(cmbEstadoForm.getValue());

            SistemaOperativo so = cmbSistemaOp.getValue();
            equipo.setIdSistemaOperativo(so == null ? null : so.getIdSistemaOperativo());

            Usuario responsable = cmbResponsable.getValue();
            equipo.setIdUsuarioResponsable(responsable == null ? null : responsable.getIdUsuario());

            return equipo;
        });

        return dialogo.showAndWait();
    }

    /** @return mensaje de error, o {@code null} si todo es válido. */
    private String validar(Equipo base, String modelo, String lugar, String procesador,
            String ram, String almacenamiento, String anio, EstadoEquipo estado) {

        if (modelo == null || modelo.isBlank()) {
            return "El modelo es obligatorio.";
        }
        if (modelo.trim().length() > MAX_MODELO) {
            return "El modelo no puede exceder " + MAX_MODELO + " caracteres.";
        }
        if (lugar != null && lugar.trim().length() > MAX_LUGAR) {
            return "El lugar no puede exceder " + MAX_LUGAR + " caracteres.";
        }
        if (procesador != null && procesador.trim().length() > MAX_PROCESADOR) {
            return "El procesador no puede exceder " + MAX_PROCESADOR + " caracteres.";
        }
        if (ram != null && ram.trim().length() > MAX_RAM) {
            return "La memoria RAM no puede exceder " + MAX_RAM + " caracteres.";
        }
        if (almacenamiento != null && almacenamiento.trim().length() > MAX_ALMACENAMIENTO) {
            return "El almacenamiento no puede exceder " + MAX_ALMACENAMIENTO + " caracteres.";
        }

        if (anio != null && !anio.isBlank()) {
            int maximo = Year.now().getValue() + 1;
            int valor;
            try {
                valor = Integer.parseInt(anio.trim());
            } catch (NumberFormatException ex) {
                return "El año de creación debe ser un número (ej. 2021).";
            }
            if (valor < ANIO_MINIMO || valor > maximo) {
                return "El año de creación debe estar entre " + ANIO_MINIMO + " y " + maximo + ".";
            }
        }

        if (estado == null) {
            return "Selecciona el estado del equipo.";
        }
        if (modeloDuplicadoEnLugar(modelo.trim(), lugar, base)) {
            return "Ya existe un equipo con ese modelo en ese mismo lugar.";
        }

        return null;
    }

    /** Aviso de captura repetida. No es una restricción de la base de datos. */
    private boolean modeloDuplicadoEnLugar(String modelo, String lugar, Equipo excluido) {
        String lugarNormalizado = texto(lugar).trim();

        for (Equipo e : datos) {
            if (e == excluido) {
                continue;
            }
            boolean mismoModelo = modelo.equalsIgnoreCase(texto(e.getModelo()).trim());
            boolean mismoLugar = lugarNormalizado.equalsIgnoreCase(texto(e.getLugar()).trim());
            if (mismoModelo && mismoLugar) {
                return true;
            }
        }
        return false;
    }

    // ============================================================
    // ESTADÍSTICAS
    // ============================================================
    private void cargarEstadisticas() {
        lblTotalEquipos.setText(String.valueOf(datos.size()));
        lblEquiposActivos.setText(String.valueOf(contarPorEstado(EstadoEquipo.ACTIVO)));
        lblEnMantenimiento.setText(String.valueOf(contarPorEstado(EstadoEquipo.EN_MANTENIMIENTO)));
        lblInactivos.setText(String.valueOf(contarPorEstado(EstadoEquipo.INACTIVO)));
        lblDeBaja.setText(String.valueOf(contarPorEstado(EstadoEquipo.DE_BAJA)));
    }

    private long contarPorEstado(EstadoEquipo estado) {
        return datos.stream().filter(e -> e.getEstado() == estado).count();
    }

    /** Recalcula filtros, KPIs, contador y refresca las celdas de la tabla. */
    private void refrescar() {
        tablaEquipos.refresh();
        refrescarLugares();
        aplicarFiltros();
        cargarEstadisticas();
        actualizarEstadoBotones();
    }

    // ============================================================
    // CATÁLOGOS ESTÁTICOS (sustituir por la BD más adelante)
    // ============================================================
    private void cargarCatalogos() {
        catalogoSistemasOperativos.clear();
        catalogoSistemasOperativos.addAll(sistemaOperativoRepository.obtenerTodos());

        catalogoResponsables.clear();
        catalogoResponsables.addAll(usuarioRepository.obtenerTodos());
    }

    private Usuario responsable(Long id, String nombre, String paterno, String materno, TipoUsuario tipo) {
        Usuario u = new Usuario();
        u.setIdUsuario(id);
        u.setNombreCompleto(nombre);
        u.setApellidoPaterno(paterno);
        u.setApellidoMaterno(materno);
        u.setTipoUsuario(tipo);
        u.setActivo(true);
        return u;
    }

    private static Usuario crearResponsableVacio() {
        Usuario u = new Usuario();
        u.setIdUsuario(null);
        u.setNombreCompleto("— Sin asignar —");
        u.setApellidoPaterno("");
        u.setApellidoMaterno("");
        return u;
    }

    // ============================================================
    // DATOS ESTÁTICOS (sustituir por la BD más adelante)
    // ============================================================
    private void cargarEquiposDesdeBD() {
        datos.setAll(equipoRepository.obtenerTodos());
    }

    private Equipo crear(Long id, String modelo, String lugar, String procesador, String ram,
            String almacenamiento, Short anio, EstadoEquipo estado, Long idSo, Long idResponsable,
            LocalDateTime alta) {

        Equipo e = new Equipo();
        e.setIdEquipo(id);
        e.setModelo(modelo);
        e.setLugar(lugar);
        e.setProcesador(procesador);
        e.setMemoriaRam(ram);
        e.setAlmacenamiento(almacenamiento);
        e.setAnioCreacion(anio);
        e.setEstado(estado);
        e.setIdSistemaOperativo(idSo);
        e.setIdUsuarioResponsable(idResponsable);
        e.setFechaCreacion(Timestamp.valueOf(alta));
        e.setFechaActualizacion(Timestamp.valueOf(alta));
        return e;
    }

    private Long siguienteId() {
        long max = datos.stream()
                .filter(e -> e.getIdEquipo() != null)
                .mapToLong(Equipo::getIdEquipo)
                .max()
                .orElse(0L);
        return max + 1;
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
        boolean hay = tablaEquipos.getSelectionModel().getSelectedItem() != null;

        btnVer.setDisable(!hay);
        btnEditar.setDisable(!hay);
        btnEliminar.setDisable(!hay);
    }

    private Equipo seleccionOAviso() {
        Equipo sel = tablaEquipos.getSelectionModel().getSelectedItem();
        if (sel == null) {
            info("Sin selección", "Selecciona primero un equipo de la tabla.");
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

    private String estiloBadgeEstado(String estado) {
        String base = "-fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 20;"
                + "-fx-padding: 4 11 4 11; ";

        if (EstadoEquipo.ACTIVO.getEtiqueta().equalsIgnoreCase(estado)) {
            return base + "-fx-background-color: #dcfce7; -fx-text-fill: #15803d;";
        }
        if (EstadoEquipo.EN_MANTENIMIENTO.getEtiqueta().equalsIgnoreCase(estado)) {
            return base + "-fx-background-color: #fef3c7; -fx-text-fill: #b45309;";
        }
        if (EstadoEquipo.DE_BAJA.getEtiqueta().equalsIgnoreCase(estado)) {
            return base + "-fx-background-color: #fee2e2; -fx-text-fill: #b91c1c;";
        }
        return base + "-fx-background-color: #f1f5f9; -fx-text-fill: #64748b;";
    }

    /** Asocia el diálogo a la ventana principal para que se comporte como modal. */
    private void prepararVentana(Dialog<?> dialogo) {
        if (tablaEquipos.getScene() != null && tablaEquipos.getScene().getWindow() != null) {
            dialogo.initOwner(tablaEquipos.getScene().getWindow());
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
    private SistemaOperativo buscarSistemaOperativo(Long id) {
        if (id == null) {
            return SIN_SISTEMA_OPERATIVO;
        }
        return catalogoSistemasOperativos.stream()
                .filter(so -> id.equals(so.getIdSistemaOperativo()))
                .findFirst()
                .orElse(SIN_SISTEMA_OPERATIVO);
    }

    private Usuario buscarResponsable(Long id) {
        if (id == null) {
            return SIN_RESPONSABLE;
        }
        return catalogoResponsables.stream()
                .filter(u -> id.equals(u.getIdUsuario()))
                .findFirst()
                .orElse(SIN_RESPONSABLE);
    }

    private String nombreSistemaOperativo(Long id) {
        if (id == null) {
            return "—";
        }
        return catalogoSistemasOperativos.stream()
                .filter(so -> id.equals(so.getIdSistemaOperativo()))
                .findFirst()
                .map(SistemaOperativo::getDescripcion)
                .orElse("—");
    }

    private String nombreResponsable(Long id) {
        if (id == null) {
            return "Sin asignar";
        }
        return catalogoResponsables.stream()
                .filter(u -> id.equals(u.getIdUsuario()))
                .findFirst()
                .map(this::nombreCompleto)
                .orElse("Sin asignar");
    }

    private String nombreCompleto(Usuario u) {
        if (u == null) {
            return "Sin asignar";
        }
        return (texto(u.getNombreCompleto()) + " "
                + texto(u.getApellidoPaterno()) + " "
                + texto(u.getApellidoMaterno())).trim().replaceAll("\\s+", " ");
    }

    private String descripcionCorta(Equipo e) {
        String modelo = texto(e.getModelo()).trim();
        String lugar = texto(e.getLugar()).trim();
        if (modelo.isEmpty()) {
            modelo = "Equipo #" + e.getIdEquipo();
        }
        return lugar.isEmpty() ? modelo : modelo + " (" + lugar + ")";
    }

    private static String texto(String valor) {
        return valor == null ? "" : valor;
    }

    private static String valorODash(String valor) {
        return (valor == null || valor.isBlank()) ? "—" : valor;
    }

    private static String nuloSiVacio(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }

    private static Short parsearAnio(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return Short.valueOf(valor.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static boolean contiene(String valor, String busqueda) {
        return valor != null && valor.toLowerCase().contains(busqueda);
    }

    private static String etiquetaEstado(EstadoEquipo estado) {
        return estado == null ? "Sin estado" : estado.getEtiqueta();
    }

    private static String formatear(Timestamp fecha) {
        return fecha == null ? "—" : fecha.toLocalDateTime().format(FORMATO_FECHA_HORA);
    }
}
