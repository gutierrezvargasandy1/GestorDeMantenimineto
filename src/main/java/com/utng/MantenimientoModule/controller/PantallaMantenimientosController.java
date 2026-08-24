package com.utng.MantenimientoModule.controller;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.utng.EquipoModule.model.equipo.Equipo;
import com.utng.EquipoModule.model.equipo.EstadoEquipo;
import com.utng.EquipoModule.model.sistemaOperativo.SistemaOperativo;
import com.utng.EquipoModule.repository.EquipoRepository;
import com.utng.MantenimientoModule.model.mantenimiento.RegistroMantenimiento;
import com.utng.MantenimientoModule.model.mantenimiento.TipoMantenimiento;
import com.utng.MantenimientoModule.repository.MantenimientoRepository;
import com.utng.SistemasOperativosModule.repository.SistemaOperativoRepository;
import com.utng.UserModule.UsuarioRepository;
import com.utng.UserModule.model.usuario.Usuario;
import com.utng.util.Navigator;
import com.utng.util.SesionManager;

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
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.util.StringConverter;

/**
 * Controlador de la pantalla de MANTENIMIENTOS (CRUD).
 *
 * Todo (alta, consulta, edición y baja) se hace con ventanas modales, igual que
 * en las pantallas de equipos y de sistemas operativos.
 *
 * Los campos y validaciones siguen EXACTAMENTE la tabla
 * {@code registros_mantenimiento} de DB.sql:
 *
 * <pre>
 * id                       BIGSERIAL PRIMARY KEY
 * id_equipo                BIGINT NOT NULL -&gt; equipos(id) ON DELETE CASCADE
 * fecha                    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
 * motivo                   TEXT NOT NULL
 * tipo                     tipo_mantenimiento NOT NULL
 * fecha_proxima            TIMESTAMP
 * mantenimiento_realizado  BOOLEAN NOT NULL DEFAULT FALSE
 * </pre>
 *
 * Además de los datos del mantenimiento, la tabla y el detalle muestran los
 * datos relevantes del equipo al que pertenece (modelo, lugar, responsable,
 * especificaciones, sistema operativo y estado).
 *
 * IMPORTANTE: por ahora trabaja con DATOS ESTÁTICOS EN MEMORIA.
 * Para conectarlo a PostgreSQL basta con sustituir las líneas marcadas
 * con "// TODO BD:" por llamadas a un MantenimientoRepository.
 */
public class PantallaMantenimientosController {

    // ============================================================
    // CONSTANTES DE FILTRO
    // ============================================================
    private static final String CAMPO_TODOS = "Todos los campos";
    private static final String TIPO_TODOS = "Todos los tipos";
    private static final String SITUACION_TODAS = "Todas las situaciones";
    private static final String EQUIPO_TODOS = "Todos los equipos";
    private final MantenimientoRepository mantenimientoRepository = new MantenimientoRepository();
    private final EquipoRepository equipoRepository = new EquipoRepository();
    private final SistemaOperativoRepository sistemaOperativoRepository = new SistemaOperativoRepository();
    private final UsuarioRepository usuarioRepository = new UsuarioRepository();

    /** Situación calculada a partir de mantenimiento_realizado y fecha_proxima. */
    private static final String SITUACION_REALIZADO = "Realizado";
    private static final String SITUACION_PENDIENTE = "Pendiente";
    private static final String SITUACION_VENCIDO = "Vencido";

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /** Hora que se usa para la fecha próxima cuando se captura una nueva. */
    private static final LocalTime HORA_POR_DEFECTO = LocalTime.of(9, 0);

    /** Marca visual para valores nulos. */
    private static final String SIN_DATO = "—";

    /** Opción "sin equipo" del ComboBox del formulario (id_equipo es NOT NULL). */
    private static final Equipo SIN_EQUIPO = crearEquipoVacio();

    // ============================================================
    // KPIs
    // ============================================================
    @FXML
    private Label lblTotalMantenimientos;
    @FXML
    private Label lblRealizados;
    @FXML
    private Label lblPendientes;
    @FXML
    private Label lblVencidos;

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
    private ComboBox<String> cmbSituacion;
    @FXML
    private ComboBox<String> cmbEquipo;
    @FXML
    private HBox contenedorChips;
    @FXML
    private Button chipTodos;
    @FXML
    private Button chipRealizados;
    @FXML
    private Button chipPendientes;
    @FXML
    private Button chipVencidos;
    @FXML
    private Label lblResultados;

    // ============================================================
    // TABLA
    // ============================================================
    @FXML
    private TableView<RegistroMantenimiento> tablaMantenimientos;
    @FXML
    private TableColumn<RegistroMantenimiento, String> colId;
    @FXML
    private TableColumn<RegistroMantenimiento, String> colEquipo;
    @FXML
    private TableColumn<RegistroMantenimiento, String> colLugar;
    @FXML
    private TableColumn<RegistroMantenimiento, String> colResponsable;
    @FXML
    private TableColumn<RegistroMantenimiento, String> colTipo;
    @FXML
    private TableColumn<RegistroMantenimiento, String> colMotivo;
    @FXML
    private TableColumn<RegistroMantenimiento, String> colFecha;
    @FXML
    private TableColumn<RegistroMantenimiento, String> colProxima;
    @FXML
    private TableColumn<RegistroMantenimiento, String> colSituacion;
    @FXML
    private TableColumn<RegistroMantenimiento, String> colAcciones;

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

    @FXML
    private Label lblAvatarUsuario, lblNombreUsuario, lblRolUsuario;

    // ============================================================
    // DATOS EN MEMORIA
    // ============================================================
    private final ObservableList<RegistroMantenimiento> datos = FXCollections.observableArrayList();
    private final List<Equipo> catalogoEquipos = new ArrayList<>();
    private final List<SistemaOperativo> catalogoSistemasOperativos = new ArrayList<>();
    private final List<Usuario> catalogoResponsables = new ArrayList<>();

    private FilteredList<RegistroMantenimiento> datosFiltrados;

    /** Evita que la recarga programática de los ComboBox dispare el filtro. */
    private boolean actualizandoFiltros = false;

    // ============================================================
    // INICIALIZACIÓN
    // ============================================================
    @FXML
    public void initialize() {
        cargarCatalogos();
        configurarTabla();
        configurarFiltros();
        refrescarCombosDeFiltro();
        aplicarFiltros();
        configurarUsuarioSesion();
        cargarDatosDesdeBD();
        cargarEstadisticas();
        actualizarEstadoBotones();
    }

    private void configurarUsuarioSesion() {
        String nombreCompleto = SesionManager.getInstance().getNombreCompleto();
        lblNombreUsuario.setText(nombreCompleto);
        lblRolUsuario.setText("Técnico CGTI"); // o el rol real si lo tienes en SesionManager
        lblAvatarUsuario.setText(iniciales(nombreCompleto));
    }

    private String iniciales(String nombre) {
        String[] p = nombre.trim().split("\\s+");
        if (p.length == 1)
            return p[0].substring(0, Math.min(2, p[0].length())).toUpperCase();
        return ("" + p[0].charAt(0) + p[1].charAt(0)).toUpperCase();
    }

    // ============================================================
    // CONFIGURACIÓN DE LA TABLA
    // ============================================================
    private void configurarTabla() {

        colId.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getIdMantenimiento() == null
                        ? SIN_DATO
                        : String.valueOf(c.getValue().getIdMantenimiento())));

        colEquipo.setCellValueFactory(c -> new SimpleStringProperty(
                modeloEquipo(c.getValue().getIdEquipo())));

        colLugar.setCellValueFactory(c -> new SimpleStringProperty(
                lugarEquipo(c.getValue().getIdEquipo())));

        colResponsable.setCellValueFactory(c -> new SimpleStringProperty(
                responsableEquipo(c.getValue().getIdEquipo())));

        colTipo.setCellValueFactory(c -> new SimpleStringProperty(
                etiquetaTipo(c.getValue().getTipo())));

        colMotivo.setCellValueFactory(c -> new SimpleStringProperty(
                valorODash(c.getValue().getMotivo())));

        colFecha.setCellValueFactory(c -> new SimpleStringProperty(
                formatearFecha(c.getValue().getFecha())));

        colProxima.setCellValueFactory(c -> new SimpleStringProperty(
                formatearFecha(c.getValue().getFechaProxima())));

        colSituacion.setCellValueFactory(c -> new SimpleStringProperty(
                situacion(c.getValue())));

        // ---- Orden correcto en las columnas numéricas y de fecha ----
        colId.setComparator(comparadorNumerico());
        colFecha.setComparator(comparadorFecha());
        colProxima.setComparator(comparadorFecha());

        // ---- Tipo con insignia de color ----
        colTipo.setCellFactory(col -> new TableCell<RegistroMantenimiento, String>() {
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

        // ---- Situación con insignia de color ----
        colSituacion.setCellFactory(col -> new TableCell<RegistroMantenimiento, String>() {
            private final Label chip = new Label();

            @Override
            protected void updateItem(String estado, boolean vacio) {
                super.updateItem(estado, vacio);
                if (vacio || estado == null) {
                    setGraphic(null);
                    return;
                }
                chip.setText(estado);
                chip.setStyle(estiloBadgeSituacion(estado));
                setGraphic(chip);
                setAlignment(Pos.CENTER_LEFT);
            }
        });

        // ---- Motivo: texto completo en un tooltip ----
        colMotivo.setCellFactory(col -> new TableCell<RegistroMantenimiento, String>() {
            @Override
            protected void updateItem(String motivo, boolean vacio) {
                super.updateItem(motivo, vacio);
                if (vacio || motivo == null) {
                    setText(null);
                    setTooltip(null);
                    return;
                }
                setText(motivo);
                setTooltip(new Tooltip(motivo));
            }
        });

        // ---- Acciones por fila ----
        colAcciones.setCellValueFactory(c -> new SimpleStringProperty(""));
        colAcciones.setCellFactory(col -> new TableCell<RegistroMantenimiento, String>() {

            private final Button bVer = miniBoton("👁", "#f1f5f9", "#475569");
            private final Button bEditar = miniBoton("✏", "#eef2ff", "#1d4ed8");
            private final Button bEliminar = miniBoton("🗑", "#fee2e2", "#b91c1c");
            private final HBox caja = new HBox(6, bVer, bEditar, bEliminar);

            {
                caja.setAlignment(Pos.CENTER_LEFT);
                bVer.setTooltip(new Tooltip("Ver detalle"));
                bEditar.setTooltip(new Tooltip("Editar"));
                bEliminar.setTooltip(new Tooltip("Eliminar mantenimiento"));
            }

            @Override
            protected void updateItem(String valor, boolean vacio) {
                super.updateItem(valor, vacio);

                int fila = getIndex();
                if (vacio || fila < 0 || fila >= getTableView().getItems().size()) {
                    setGraphic(null);
                    return;
                }

                RegistroMantenimiento mantenimiento = getTableView().getItems().get(fila);

                bVer.setOnAction(e -> mostrarDetalle(mantenimiento));
                bEditar.setOnAction(e -> abrirEdicion(mantenimiento));
                bEliminar.setOnAction(e -> eliminar(mantenimiento));

                setGraphic(caja);
                setAlignment(Pos.CENTER_LEFT);
            }
        });

        tablaMantenimientos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        // Doble clic sobre la fila -> editar
        tablaMantenimientos.setRowFactory(tv -> {
            TableRow<RegistroMantenimiento> fila = new TableRow<>();
            fila.setPrefHeight(40);
            fila.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !fila.isEmpty()) {
                    abrirEdicion(fila.getItem());
                }
            });
            return fila;
        });

        tablaMantenimientos.getSelectionModel().selectedItemProperty()
                .addListener((obs, anterior, actual) -> actualizarEstadoBotones());
    }

    // ============================================================
    // CONFIGURACIÓN DE FILTROS
    // ============================================================
    private void configurarFiltros() {

        cmbCampo.getItems().addAll(CAMPO_TODOS, "ID", "Equipo", "Lugar", "Responsable", "Motivo");
        cmbCampo.getSelectionModel().selectFirst();

        cmbTipo.getItems().add(TIPO_TODOS);
        for (TipoMantenimiento tipo : TipoMantenimiento.values()) {
            cmbTipo.getItems().add(tipo.getEtiqueta());
        }
        cmbTipo.getSelectionModel().selectFirst();

        cmbSituacion.getItems().addAll(SITUACION_TODAS,
                SITUACION_PENDIENTE, SITUACION_REALIZADO, SITUACION_VENCIDO);
        cmbSituacion.getSelectionModel().selectFirst();

        cmbEquipo.getItems().add(EQUIPO_TODOS);
        cmbEquipo.getSelectionModel().selectFirst();

        datosFiltrados = new FilteredList<>(datos, m -> true);
        SortedList<RegistroMantenimiento> ordenados = new SortedList<>(datosFiltrados);
        ordenados.comparatorProperty().bind(tablaMantenimientos.comparatorProperty());
        tablaMantenimientos.setItems(ordenados);
    }

    /** Buscador (onKeyReleased) y ComboBox de filtros (onAction). */
    @FXML
    private void filtrarMantenimientos() {
        if (actualizandoFiltros) {
            return;
        }
        aplicarFiltros();
    }

    /**
     * Chips de situación. El ComboBox de situación es la única fuente de verdad
     * del filtro.
     */
    @FXML
    private void filtrarPorSituacion(ActionEvent e) {
        Object origen = e.getSource();

        if (origen == chipRealizados) {
            cmbSituacion.setValue(SITUACION_REALIZADO);
        } else if (origen == chipPendientes) {
            cmbSituacion.setValue(SITUACION_PENDIENTE);
        } else if (origen == chipVencidos) {
            cmbSituacion.setValue(SITUACION_VENCIDO);
        } else {
            cmbSituacion.setValue(SITUACION_TODAS);
        }

        aplicarFiltros();
    }

    @FXML
    private void limpiarFiltros() {
        txtBuscar.clear();
        cmbCampo.setValue(CAMPO_TODOS);
        cmbTipo.setValue(TIPO_TODOS);
        cmbSituacion.setValue(SITUACION_TODAS);
        cmbEquipo.setValue(EQUIPO_TODOS);
        aplicarFiltros();
    }

    private void aplicarFiltros() {
        if (datosFiltrados == null) {
            return;
        }

        final String q = (txtBuscar.getText() == null) ? "" : txtBuscar.getText().trim().toLowerCase();
        final String campo = cmbCampo.getValue();
        final String tipo = cmbTipo.getValue();
        final String estado = cmbSituacion.getValue();
        final String equipo = cmbEquipo.getValue();

        datosFiltrados.setPredicate(m -> coincideTexto(m, q, campo)
                && coincideTipo(m, tipo)
                && coincideSituacion(m, estado)
                && coincideEquipo(m, equipo));

        resaltarChipActivo(estado);
        actualizarResultados();
        actualizarEstadoBotones();
    }

    private boolean coincideTexto(RegistroMantenimiento m, String q, String campo) {
        if (q.isEmpty()) {
            return true;
        }
        String buscado = (campo == null) ? CAMPO_TODOS : campo;

        return switch (buscado) {
            case "ID" -> String.valueOf(m.getIdMantenimiento()).contains(q);
            case "Equipo" -> contiene(modeloEquipo(m.getIdEquipo()), q);
            case "Lugar" -> contiene(lugarEquipo(m.getIdEquipo()), q);
            case "Responsable" -> contiene(responsableEquipo(m.getIdEquipo()), q);
            case "Motivo" -> contiene(m.getMotivo(), q);
            default -> contiene(modeloEquipo(m.getIdEquipo()), q)
                    || contiene(lugarEquipo(m.getIdEquipo()), q)
                    || contiene(responsableEquipo(m.getIdEquipo()), q)
                    || contiene(m.getMotivo(), q)
                    || contiene(etiquetaTipo(m.getTipo()), q)
                    || contiene(situacion(m), q)
                    || contiene(formatearFecha(m.getFecha()), q)
                    || contiene(formatearFecha(m.getFechaProxima()), q)
                    || String.valueOf(m.getIdMantenimiento()).contains(q);
        };
    }

    private boolean coincideTipo(RegistroMantenimiento m, String tipo) {
        if (tipo == null || TIPO_TODOS.equals(tipo)) {
            return true;
        }
        return etiquetaTipo(m.getTipo()).equalsIgnoreCase(tipo);
    }

    private boolean coincideSituacion(RegistroMantenimiento m, String estado) {
        if (estado == null || SITUACION_TODAS.equals(estado)) {
            return true;
        }
        return situacion(m).equalsIgnoreCase(estado);
    }

    private boolean coincideEquipo(RegistroMantenimiento m, String equipo) {
        if (equipo == null || EQUIPO_TODOS.equals(equipo)) {
            return true;
        }
        return equipo.equalsIgnoreCase(descripcionEquipo(m.getIdEquipo()));
    }

    private void resaltarChipActivo(String estado) {
        Button activo = chipTodos;

        if (SITUACION_REALIZADO.equalsIgnoreCase(estado)) {
            activo = chipRealizados;
        } else if (SITUACION_PENDIENTE.equalsIgnoreCase(estado)) {
            activo = chipPendientes;
        } else if (SITUACION_VENCIDO.equalsIgnoreCase(estado)) {
            activo = chipVencidos;
        }

        for (Node n : contenedorChips.getChildren()) {
            if (n instanceof Button) {
                n.setOpacity(n == activo ? 1.0 : 0.5);
            }
        }
    }

    private void actualizarResultados() {
        int visibles = datosFiltrados.size();
        lblResultados.setText(visibles == 1 ? "1 mantenimiento" : visibles + " mantenimientos");
    }

    /**
     * Reconstruye la lista de equipos del filtro a partir de los mantenimientos
     * capturados.
     */
    private void refrescarCombosDeFiltro() {
        actualizandoFiltros = true;
        try {
            String seleccion = cmbEquipo.getValue();

            List<String> items = new ArrayList<>();
            items.add(EQUIPO_TODOS);

            datos.stream()
                    .map(RegistroMantenimiento::getIdEquipo)
                    .distinct()
                    .map(this::descripcionEquipo)
                    .filter(d -> !d.isBlank())
                    .distinct()
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .forEach(items::add);

            cmbEquipo.getItems().setAll(items);
            cmbEquipo.setValue(items.contains(seleccion) ? seleccion : EQUIPO_TODOS);

        } finally {
            actualizandoFiltros = false;
        }
    }

    // ============================================================
    // CRUD - CREAR
    // ============================================================
    @FXML
    private void nuevoMantenimiento() {
        Optional<RegistroMantenimiento> resultado = abrirFormulario(null);

        resultado.ifPresent(nuevo -> {
            mantenimientoRepository.guardar(nuevo); // asigna el id real de la BD
            datos.add(nuevo);

            refrescar();
            tablaMantenimientos.getSelectionModel().select(nuevo);
            info("Mantenimiento registrado",
                    "Se registró el mantenimiento " + etiquetaTipo(nuevo.getTipo()).toLowerCase()
                            + " del equipo " + descripcionEquipo(nuevo.getIdEquipo()) + ".");
        });
    }

    // ============================================================
    // CRUD - ACTUALIZAR
    // ============================================================
    @FXML
    private void editarMantenimiento() {
        RegistroMantenimiento seleccionado = seleccionOAviso();
        if (seleccionado != null) {
            abrirEdicion(seleccionado);
        }
    }

    private void abrirEdicion(RegistroMantenimiento mantenimiento) {
        if (mantenimiento == null) {
            return;
        }
        Optional<RegistroMantenimiento> resultado = abrirFormulario(mantenimiento);

        resultado.ifPresent(actualizado -> {
            mantenimientoRepository.actualizar(actualizado);
            refrescar();
            tablaMantenimientos.getSelectionModel().select(actualizado);
            info("Cambios guardados",
                    "Se actualizó el mantenimiento #" + actualizado.getIdMantenimiento() + ".");
        });
    }

    // ============================================================
    // CRUD - ELIMINAR (borrado físico)
    // ============================================================
    @FXML
    private void eliminarMantenimiento() {
        RegistroMantenimiento seleccionado = seleccionOAviso();
        if (seleccionado != null) {
            eliminar(seleccionado);
        }
    }

    private void eliminar(RegistroMantenimiento mantenimiento) {
        if (mantenimiento == null) {
            return;
        }

        boolean confirmado = confirmar(
                "Eliminar mantenimiento",
                "¿Deseas eliminar definitivamente el mantenimiento #"
                        + mantenimiento.getIdMantenimiento() + " del equipo "
                        + descripcionEquipo(mantenimiento.getIdEquipo()) + "?",
                "Esta acción no se puede deshacer. Por la llave foránea "
                        + "historial_registros.id_registro_mantenimiento (ON DELETE CASCADE) "
                        + "también se borrarán las filas del historial que apunten a este "
                        + "registro. El equipo NO se elimina.");

        if (confirmado) {
            mantenimientoRepository.eliminar(mantenimiento.getIdMantenimiento());
            datos.remove(mantenimiento);
            refrescar();
            info("Mantenimiento eliminado",
                    "Se eliminó el registro #" + mantenimiento.getIdMantenimiento() + ".");
        }
    }

    // ============================================================
    // CRUD - VER
    // ============================================================
    @FXML
    private void verMantenimiento() {
        RegistroMantenimiento seleccionado = seleccionOAviso();
        if (seleccionado != null) {
            mostrarDetalle(seleccionado);
        }
    }

    /**
     * Modal de solo lectura: primero el mantenimiento y después los datos
     * relevantes del equipo al que pertenece.
     */
    private void mostrarDetalle(RegistroMantenimiento m) {
        if (m == null) {
            return;
        }

        Equipo equipo = buscarEquipo(m.getIdEquipo());

        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(9);
        grid.setPadding(new Insets(16, 20, 8, 20));

        int f = 0;
        f = agregarSeccion(grid, f, "Datos del mantenimiento");
        agregarDetalle(grid, f++, "ID",
                m.getIdMantenimiento() == null ? SIN_DATO : String.valueOf(m.getIdMantenimiento()));
        agregarDetalle(grid, f++, "Tipo", etiquetaTipo(m.getTipo()));
        agregarDetalle(grid, f++, "Fecha", formatearFechaHora(m.getFecha()));
        agregarDetalle(grid, f++, "Fecha próxima", formatearFechaHora(m.getFechaProxima()));
        agregarDetalle(grid, f++, "Realizado", m.isMantenimientoRealizado() ? "Sí" : "No");
        agregarDetalle(grid, f++, "Situación", situacion(m));
        agregarDetalle(grid, f++, "Motivo", valorODash(m.getMotivo()));

        f = agregarSeccion(grid, f, "Datos del equipo");
        agregarDetalle(grid, f++, "ID del equipo",
                m.getIdEquipo() == null ? SIN_DATO : String.valueOf(m.getIdEquipo()));
        agregarDetalle(grid, f++, "Modelo", modeloEquipo(m.getIdEquipo()));
        agregarDetalle(grid, f++, "Lugar", lugarEquipo(m.getIdEquipo()));
        agregarDetalle(grid, f++, "Procesador", equipo == null ? SIN_DATO : valorODash(equipo.getProcesador()));
        agregarDetalle(grid, f++, "Memoria RAM", equipo == null ? SIN_DATO : valorODash(equipo.getMemoriaRam()));
        agregarDetalle(grid, f++, "Almacenamiento",
                equipo == null ? SIN_DATO : valorODash(equipo.getAlmacenamiento()));
        agregarDetalle(grid, f++, "Año de creación",
                (equipo == null || equipo.getAnioCreacion() == null)
                        ? SIN_DATO
                        : String.valueOf(equipo.getAnioCreacion()));
        agregarDetalle(grid, f++, "Sistema operativo",
                equipo == null ? SIN_DATO : nombreSistemaOperativo(equipo.getIdSistemaOperativo()));
        agregarDetalle(grid, f++, "Responsable", responsableEquipo(m.getIdEquipo()));
        agregarDetalle(grid, f, "Estado del equipo",
                equipo == null ? SIN_DATO : etiquetaEstado(equipo.getEstado()));

        Dialog<Void> dialogo = new Dialog<>();
        dialogo.setTitle("Detalle del mantenimiento");
        dialogo.setHeaderText(etiquetaTipo(m.getTipo()) + " · " + descripcionEquipo(m.getIdEquipo()));
        dialogo.getDialogPane().setContent(grid);
        dialogo.getDialogPane().setMinWidth(540);
        dialogo.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        prepararVentana(dialogo);
        dialogo.showAndWait();
    }

    /** Inserta un título de sección y devuelve la siguiente fila libre. */
    private int agregarSeccion(GridPane grid, int fila, String titulo) {
        Label lbl = new Label(titulo);
        lbl.setStyle("-fx-font-size: 12.5px; -fx-font-weight: bold; -fx-text-fill: #1d4ed8;");

        VBox caja = new VBox(4, lbl, new Separator());
        caja.setPadding(new Insets(fila == 0 ? 0 : 10, 0, 4, 0));

        grid.add(caja, 0, fila, 2, 1);
        return fila + 1;
    }

    private void agregarDetalle(GridPane grid, int fila, String etiqueta, String valor) {
        Label lblEtiqueta = new Label(etiqueta);
        lblEtiqueta.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #64748b;");

        Label lblValor = new Label(valor);
        lblValor.setStyle("-fx-font-size: 12.5px; -fx-text-fill: #0f172a; -fx-font-weight: bold;");
        lblValor.setWrapText(true);
        lblValor.setMaxWidth(340);

        grid.add(lblEtiqueta, 0, fila);
        grid.add(lblValor, 1, fila);
    }

    // ============================================================
    // FORMULARIO MODAL (CREAR / EDITAR)
    // ============================================================
    /**
     * @param base mantenimiento a editar, o {@code null} para registrar uno nuevo.
     * @return el registro creado/actualizado, o vacío si se canceló.
     */
    private Optional<RegistroMantenimiento> abrirFormulario(RegistroMantenimiento base) {

        final boolean esNuevo = (base == null);

        Dialog<RegistroMantenimiento> dialogo = new Dialog<>();
        dialogo.setTitle(esNuevo ? "Nuevo mantenimiento" : "Editar mantenimiento");
        dialogo.setHeaderText(esNuevo
                ? "Registra un nuevo mantenimiento de un equipo"
                : "Modificando el mantenimiento #" + base.getIdMantenimiento()
                        + " · " + descripcionEquipo(base.getIdEquipo()));

        ButtonType btnGuardar = new ButtonType(esNuevo ? "Registrar mantenimiento" : "Guardar cambios",
                ButtonBar.ButtonData.OK_DONE);
        dialogo.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);

        // ---- Campos ----
        ComboBox<Equipo> cmbEquipoForm = new ComboBox<>();
        cmbEquipoForm.getItems().add(SIN_EQUIPO);
        cmbEquipoForm.getItems().addAll(catalogoEquipos);
        cmbEquipoForm.setPrefWidth(300);
        cmbEquipoForm.setConverter(new StringConverter<Equipo>() {
            @Override
            public String toString(Equipo e) {
                return e == null ? "" : descripcionCorta(e);
            }

            @Override
            public Equipo fromString(String s) {
                return null;
            }
        });

        ComboBox<TipoMantenimiento> cmbTipoForm = new ComboBox<>();
        cmbTipoForm.getItems().addAll(TipoMantenimiento.values());
        cmbTipoForm.setPrefWidth(300);
        cmbTipoForm.setConverter(new StringConverter<TipoMantenimiento>() {
            @Override
            public String toString(TipoMantenimiento tipo) {
                return tipo == null ? "" : tipo.getEtiqueta();
            }

            @Override
            public TipoMantenimiento fromString(String s) {
                return TipoMantenimiento.fromEtiqueta(s);
            }
        });

        DatePicker dpFecha = new DatePicker();
        dpFecha.setPrefWidth(300);
        dpFecha.setPromptText("dd/mm/aaaa");

        DatePicker dpProxima = new DatePicker();
        dpProxima.setPrefWidth(300);
        dpProxima.setPromptText("dd/mm/aaaa (opcional)");

        TextArea txtMotivo = new TextArea();
        txtMotivo.setPromptText("Ej. Limpieza interna y cambio de pasta térmica");
        txtMotivo.setPrefWidth(300);
        txtMotivo.setPrefRowCount(3);
        txtMotivo.setWrapText(true);

        CheckBox chkRealizado = new CheckBox("El mantenimiento ya fue realizado");
        chkRealizado.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #475569;");

        // Resumen del equipo seleccionado (datos relevantes, solo lectura)
        Label lblResumenEquipo = new Label();
        lblResumenEquipo.setStyle("-fx-font-size: 10.5px; -fx-text-fill: #64748b;");
        lblResumenEquipo.setWrapText(true);
        lblResumenEquipo.setMaxWidth(300);
        cmbEquipoForm.valueProperty().addListener(
                (obs, anterior, actual) -> lblResumenEquipo.setText(resumenEquipo(actual)));

        Label lblError = new Label();
        lblError.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 11.5px;");
        lblError.setWrapText(true);
        lblError.setMaxWidth(400);

        Label lblAyuda = new Label("El equipo, el tipo, la fecha y el motivo son obligatorios "
                + "(NOT NULL en registros_mantenimiento). La fecha próxima es opcional.");
        lblAyuda.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 10.5px;");
        lblAyuda.setWrapText(true);
        lblAyuda.setMaxWidth(400);

        // ---- Precarga ----
        if (esNuevo) {
            cmbEquipoForm.setValue(SIN_EQUIPO);
            cmbTipoForm.setValue(TipoMantenimiento.PREVENTIVO);
            dpFecha.setValue(LocalDate.now());
        } else {
            cmbEquipoForm.setValue(buscarEquipoOVacio(base.getIdEquipo()));
            cmbTipoForm.setValue(base.getTipo());
            dpFecha.setValue(aLocalDate(base.getFecha()));
            dpProxima.setValue(aLocalDate(base.getFechaProxima()));
            txtMotivo.setText(texto(base.getMotivo()));
            chkRealizado.setSelected(base.isMantenimientoRealizado());
        }
        lblResumenEquipo.setText(resumenEquipo(cmbEquipoForm.getValue()));

        // ---- Layout ----
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(18, 22, 6, 22));

        int f = 0;
        grid.add(etiqueta("Equipo *"), 0, f);
        grid.add(cmbEquipoForm, 1, f++);
        grid.add(lblResumenEquipo, 1, f++);
        grid.add(etiqueta("Tipo *"), 0, f);
        grid.add(cmbTipoForm, 1, f++);
        grid.add(etiqueta("Fecha *"), 0, f);
        grid.add(dpFecha, 1, f++);
        grid.add(etiqueta("Fecha próxima"), 0, f);
        grid.add(dpProxima, 1, f++);
        grid.add(etiqueta("Motivo *"), 0, f);
        grid.add(txtMotivo, 1, f++);
        grid.add(etiqueta("Realizado"), 0, f);
        grid.add(chkRealizado, 1, f++);
        grid.add(lblAyuda, 1, f++);
        grid.add(lblError, 1, f);

        dialogo.getDialogPane().setContent(grid);
        dialogo.getDialogPane().setMinWidth(560);
        prepararVentana(dialogo);

        javafx.application.Platform.runLater(cmbEquipoForm::requestFocus);

        // ---- Validación antes de cerrar ----
        Node nodoGuardar = dialogo.getDialogPane().lookupButton(btnGuardar);
        nodoGuardar.addEventFilter(ActionEvent.ACTION, evento -> {
            String error = validar(
                    base,
                    cmbEquipoForm.getValue(),
                    cmbTipoForm.getValue(),
                    dpFecha.getValue(),
                    dpProxima.getValue(),
                    txtMotivo.getText());

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

            RegistroMantenimiento m = esNuevo ? new RegistroMantenimiento() : base;

            Equipo equipo = cmbEquipoForm.getValue();
            m.setIdEquipo(equipo == null ? null : equipo.getIdEquipo());
            m.setTipo(cmbTipoForm.getValue());
            m.setFecha(aTimestamp(dpFecha.getValue(), esNuevo ? null : base.getFecha()));
            m.setFechaProxima(aTimestamp(dpProxima.getValue(), esNuevo ? null : base.getFechaProxima()));
            m.setMotivo(txtMotivo.getText().trim());
            m.setMantenimientoRealizado(chkRealizado.isSelected());

            return m;
        });

        return dialogo.showAndWait();
    }

    /** @return mensaje de error, o {@code null} si todo es válido. */
    private String validar(RegistroMantenimiento base, Equipo equipo, TipoMantenimiento tipo,
            LocalDate fecha, LocalDate proxima, String motivo) {

        if (equipo == null || equipo.getIdEquipo() == null) {
            return "Selecciona el equipo al que pertenece el mantenimiento "
                    + "(id_equipo es NOT NULL en la base de datos).";
        }
        if (tipo == null) {
            return "Selecciona el tipo de mantenimiento (preventivo o correctivo).";
        }
        if (fecha == null) {
            return "La fecha del mantenimiento es obligatoria.";
        }
        if (motivo == null || motivo.isBlank()) {
            return "El motivo es obligatorio.";
        }
        if (proxima != null && proxima.isBefore(fecha)) {
            return "La fecha próxima no puede ser anterior a la fecha del mantenimiento.";
        }
        if (duplicado(equipo.getIdEquipo(), tipo, fecha, base)) {
            return "Ya existe un mantenimiento " + tipo.getEtiqueta().toLowerCase()
                    + " para ese equipo en esa misma fecha.";
        }

        return null;
    }

    /**
     * Aviso de captura repetida (mismo equipo, mismo tipo y mismo día).
     * No es una restricción de la base de datos.
     */
    private boolean duplicado(Long idEquipo, TipoMantenimiento tipo, LocalDate fecha,
            RegistroMantenimiento excluido) {

        for (RegistroMantenimiento m : datos) {
            if (m == excluido) {
                continue;
            }
            boolean mismoEquipo = idEquipo.equals(m.getIdEquipo());
            boolean mismoTipo = (tipo == m.getTipo());
            boolean mismaFecha = fecha.equals(aLocalDate(m.getFecha()));

            if (mismoEquipo && mismoTipo && mismaFecha) {
                return true;
            }
        }
        return false;
    }

    // ============================================================
    // ESTADÍSTICAS
    // ============================================================
    private void cargarEstadisticas() {
        lblTotalMantenimientos.setText(String.valueOf(datos.size()));
        lblRealizados.setText(String.valueOf(contarPorSituacion(SITUACION_REALIZADO)));
        lblPendientes.setText(String.valueOf(contarPorSituacion(SITUACION_PENDIENTE)));
        lblVencidos.setText(String.valueOf(contarPorSituacion(SITUACION_VENCIDO)));
    }

    private long contarPorSituacion(String estado) {
        return datos.stream().filter(m -> situacion(m).equals(estado)).count();
    }

    /** Recalcula filtros, KPIs, contador y refresca las celdas de la tabla. */
    private void refrescar() {
        tablaMantenimientos.refresh();
        refrescarCombosDeFiltro();
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

        catalogoEquipos.clear();
        catalogoEquipos.addAll(equipoRepository.obtenerTodos());
    }

    private static Equipo crearEquipoVacio() {
        Equipo e = new Equipo();
        e.setIdEquipo(null);
        e.setModelo("— Selecciona un equipo —");
        e.setLugar("");
        return e;
    }

    // ============================================================
    // DATOS ESTÁTICOS (sustituir por la BD más adelante)
    // ============================================================

    private void cargarDatosDesdeBD() {
        datos.setAll(mantenimientoRepository.obtenerTodos());
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
        Navigator.navigate("/com/utng/ui/sitemasOperativosModules/PantallaSistemasOperativos/SistemasOperativos.fxml");
    }

    @FXML
    private void irAMantenimientos() {
        toggleMenu(); // ya estamos en esta pantalla
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
        boolean hay = tablaMantenimientos.getSelectionModel().getSelectedItem() != null;

        btnVer.setDisable(!hay);
        btnEditar.setDisable(!hay);
        btnEliminar.setDisable(!hay);
    }

    private RegistroMantenimiento seleccionOAviso() {
        RegistroMantenimiento sel = tablaMantenimientos.getSelectionModel().getSelectedItem();
        if (sel == null) {
            info("Sin selección", "Selecciona primero un mantenimiento de la tabla.");
        }
        return sel;
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
        String base = "-fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 20;"
                + "-fx-padding: 4 11 4 11; ";

        if (TipoMantenimiento.PREVENTIVO.getEtiqueta().equalsIgnoreCase(tipo)) {
            return base + "-fx-background-color: #dbeafe; -fx-text-fill: #1d4ed8;";
        }
        if (TipoMantenimiento.CORRECTIVO.getEtiqueta().equalsIgnoreCase(tipo)) {
            return base + "-fx-background-color: #fee2e2; -fx-text-fill: #b91c1c;";
        }
        return base + "-fx-background-color: #f1f5f9; -fx-text-fill: #64748b;";
    }

    private String estiloBadgeSituacion(String estado) {
        String base = "-fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 20;"
                + "-fx-padding: 4 11 4 11; ";

        if (SITUACION_REALIZADO.equalsIgnoreCase(estado)) {
            return base + "-fx-background-color: #dcfce7; -fx-text-fill: #15803d;";
        }
        if (SITUACION_VENCIDO.equalsIgnoreCase(estado)) {
            return base + "-fx-background-color: #fee2e2; -fx-text-fill: #b91c1c;";
        }
        return base + "-fx-background-color: #fef3c7; -fx-text-fill: #b45309;";
    }

    /** Asocia el diálogo a la ventana principal para que se comporte como modal. */
    private void prepararVentana(Dialog<?> dialogo) {
        if (tablaMantenimientos.getScene() != null && tablaMantenimientos.getScene().getWindow() != null) {
            dialogo.initOwner(tablaMantenimientos.getScene().getWindow());
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
    // DATOS DEL EQUIPO ASOCIADO
    // ============================================================
    private Equipo buscarEquipo(Long idEquipo) {
        if (idEquipo == null) {
            return null;
        }
        return catalogoEquipos.stream()
                .filter(e -> idEquipo.equals(e.getIdEquipo()))
                .findFirst()
                .orElse(null);
    }

    private Equipo buscarEquipoOVacio(Long idEquipo) {
        Equipo e = buscarEquipo(idEquipo);
        return e == null ? SIN_EQUIPO : e;
    }

    private String modeloEquipo(Long idEquipo) {
        Equipo e = buscarEquipo(idEquipo);
        return e == null ? "Equipo #" + idEquipo : texto(e.getModelo());
    }

    private String lugarEquipo(Long idEquipo) {
        Equipo e = buscarEquipo(idEquipo);
        return e == null ? SIN_DATO : valorODash(e.getLugar());
    }

    private String responsableEquipo(Long idEquipo) {
        Equipo e = buscarEquipo(idEquipo);
        return e == null ? "Sin asignar" : nombreResponsable(e.getIdUsuarioResponsable());
    }

    /** "HP ProDesk 400 G7 (Laboratorio de Redes)" */
    private String descripcionEquipo(Long idEquipo) {
        Equipo e = buscarEquipo(idEquipo);
        return e == null ? "Equipo #" + idEquipo : descripcionCorta(e);
    }

    private String descripcionCorta(Equipo e) {
        String modelo = texto(e.getModelo()).trim();
        String lugar = texto(e.getLugar()).trim();
        if (modelo.isEmpty()) {
            modelo = "Equipo #" + e.getIdEquipo();
        }
        return lugar.isEmpty() ? modelo : modelo + " (" + lugar + ")";
    }

    /** Línea de apoyo dentro del formulario con los datos clave del equipo. */
    private String resumenEquipo(Equipo e) {
        if (e == null || e.getIdEquipo() == null) {
            return "Selecciona un equipo para ver sus datos.";
        }
        return valorODash(e.getProcesador())
                + " · " + valorODash(e.getMemoriaRam())
                + " · " + valorODash(e.getAlmacenamiento())
                + " · " + nombreSistemaOperativo(e.getIdSistemaOperativo())
                + " · " + etiquetaEstado(e.getEstado())
                + " · Responsable: " + nombreResponsable(e.getIdUsuarioResponsable());
    }

    private String nombreSistemaOperativo(Long id) {
        if (id == null) {
            return SIN_DATO;
        }
        return catalogoSistemasOperativos.stream()
                .filter(so -> id.equals(so.getIdSistemaOperativo()))
                .findFirst()
                .map(SistemaOperativo::getDescripcion)
                .orElse(SIN_DATO);
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

    // ============================================================
    // UTILIDADES DE DATOS
    // ============================================================
    /**
     * Situación calculada: si ya se realizó es "Realizado"; si no y su fecha
     * próxima ya pasó es "Vencido"; en cualquier otro caso es "Pendiente".
     */
    private static String situacion(RegistroMantenimiento m) {
        if (m.isMantenimientoRealizado()) {
            return SITUACION_REALIZADO;
        }
        LocalDate proxima = aLocalDate(m.getFechaProxima());
        if (proxima != null && proxima.isBefore(LocalDate.now())) {
            return SITUACION_VENCIDO;
        }
        return SITUACION_PENDIENTE;
    }

    private static String etiquetaTipo(TipoMantenimiento tipo) {
        return tipo == null ? "Sin tipo" : tipo.getEtiqueta();
    }

    private static String etiquetaEstado(EstadoEquipo estado) {
        return estado == null ? "Sin estado" : estado.getEtiqueta();
    }

    private static LocalDate aLocalDate(Timestamp fecha) {
        return fecha == null ? null : fecha.toLocalDateTime().toLocalDate();
    }

    /**
     * Convierte la fecha del DatePicker en TIMESTAMP conservando la hora del
     * registro original (o 09:00 si es un alta nueva).
     */
    private static Timestamp aTimestamp(LocalDate fecha, Timestamp referencia) {
        if (fecha == null) {
            return null;
        }
        LocalTime hora = (referencia == null)
                ? HORA_POR_DEFECTO
                : referencia.toLocalDateTime().toLocalTime();
        return Timestamp.valueOf(LocalDateTime.of(fecha, hora));
    }

    private static String formatearFecha(Timestamp fecha) {
        return fecha == null ? SIN_DATO : fecha.toLocalDateTime().format(FORMATO_FECHA);
    }

    private static String formatearFechaHora(Timestamp fecha) {
        return fecha == null ? SIN_DATO : fecha.toLocalDateTime().format(FORMATO_FECHA_HORA);
    }

    /** Ordena "dd/MM/yyyy" como fecha y no como texto. Los "—" van al final. */
    private static Comparator<String> comparadorFecha() {
        return Comparator.comparing(PantallaMantenimientosController::claveFecha);
    }

    /** Ordena los IDs como números y no como texto. Los "—" van al final. */
    private static Comparator<String> comparadorNumerico() {
        return Comparator.comparingLong(PantallaMantenimientosController::claveNumerica);
    }

    /** "11/08/2026" -> "20260811", para poder compararlo como texto. */
    private static String claveFecha(String valor) {
        if (valor == null || !valor.matches("\\d{2}/\\d{2}/\\d{4}")) {
            return "99999999";
        }
        return valor.substring(6) + valor.substring(3, 5) + valor.substring(0, 2);
    }

    /** El ID como número. Las filas sin ID se van al final. */
    private static long claveNumerica(String valor) {
        if (valor == null || !valor.matches("\\d{1,18}")) {
            return Long.MAX_VALUE;
        }
        return Long.parseLong(valor);
    }

    private static String texto(String valor) {
        return valor == null ? "" : valor;
    }

    private static String valorODash(String valor) {
        return (valor == null || valor.isBlank()) ? SIN_DATO : valor;
    }

    private static boolean contiene(String valor, String busqueda) {
        return valor != null && valor.toLowerCase().contains(busqueda);
    }
}
