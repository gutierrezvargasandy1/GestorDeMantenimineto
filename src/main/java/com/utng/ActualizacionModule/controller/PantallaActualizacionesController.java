package com.utng.ActualizacionModule.controller;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.utng.ActualizacionModule.model.actualizacion.RegistroActualizacion;
import com.utng.ActualizacionModule.model.actualizacion.TipoActualizacion;
import com.utng.EquipoModule.model.equipo.Equipo;
import com.utng.EquipoModule.model.equipo.EstadoEquipo;
import com.utng.EquipoModule.model.sistemaOperativo.SistemaOperativo;
import com.utng.UserModule.model.usuario.TipoUsuario;
import com.utng.UserModule.model.usuario.Usuario;
import com.utng.util.Navigator;

import javafx.application.Platform;
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
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
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

/**
 * Controlador de la pantalla de ACTUALIZACIONES (CRUD).
 *
 * Todo (alta, consulta, edición y baja) se hace con ventanas modales, igual que
 * en las pantallas de equipos, sistemas operativos y mantenimientos.
 *
 * Los campos y validaciones siguen EXACTAMENTE la tabla
 * {@code registros_actualizaciones} de DB.sql:
 *
 * <pre>
 * id                   BIGSERIAL PRIMARY KEY
 * id_equipo            BIGINT NOT NULL -&gt; equipos(id) ON DELETE CASCADE
 * tipo                 tipo_actualizacion NOT NULL
 * fecha                TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
 * nombre_actualizado   VARCHAR(150) NOT NULL
 * version_actual       VARCHAR(50)
 * version_actualizada  VARCHAR(50) NOT NULL
 * </pre>
 *
 * Además de los datos de la actualización, la tabla y el detalle muestran los
 * datos relevantes del equipo al que pertenece (modelo, lugar, responsable,
 * especificaciones, sistema operativo y estado).
 *
 * IMPORTANTE: por ahora trabaja con DATOS ESTÁTICOS EN MEMORIA.
 * Para conectarlo a PostgreSQL basta con sustituir las líneas marcadas
 * con "// TODO BD:" por llamadas a un ActualizacionRepository.
 */
public class PantallaActualizacionesController {

    // ============================================================
    // CONSTANTES DE FILTRO
    // ============================================================
    private static final String CAMPO_TODOS = "Todos los campos";
    private static final String TIPO_TODOS = "Todos los tipos";
    private static final String EQUIPO_TODOS = "Todos los equipos";

    private static final String PERIODO_TODOS = "Todos los periodos";
    private static final String PERIODO_30_DIAS = "Últimos 30 días";
    private static final String PERIODO_ESTE_ANIO = "Este año";
    private static final String PERIODO_ANIO_ANTERIOR = "Año anterior";
    private static final String PERIODO_ANTERIORES = "Anteriores";

    // ============================================================
    // LÍMITES DE LA BASE DE DATOS
    // ============================================================
    /** registros_actualizaciones.nombre_actualizado VARCHAR(150) */
    private static final int MAX_NOMBRE = 150;
    /** registros_actualizaciones.version_actual / version_actualizada VARCHAR(50) */
    private static final int MAX_VERSION = 50;

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /** Hora que se usa para la fecha cuando se captura un registro nuevo. */
    private static final LocalTime HORA_POR_DEFECTO = LocalTime.of(9, 0);

    /** Marca visual para valores nulos. */
    private static final String SIN_DATO = "—";

    /** Opción "sin equipo" del ComboBox del formulario (id_equipo es NOT NULL). */
    private static final Equipo SIN_EQUIPO = crearEquipoVacio();

    // ============================================================
    // KPIs
    // ============================================================
    @FXML
    private Label lblTotalActualizaciones;
    @FXML
    private Label lblEquiposActualizados;
    @FXML
    private Label lblEsteAnio;
    @FXML
    private Label lblUltimos30;

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
    private ComboBox<String> cmbPeriodo;
    @FXML
    private ComboBox<String> cmbEquipo;
    @FXML
    private HBox contenedorChips;
    @FXML
    private Button chipTodos;
    @FXML
    private Button chipSistemaOperativo;
    @FXML
    private Button chipPrograma;
    @FXML
    private Button chipFirmware;
    @FXML
    private Button chipDriver;
    @FXML
    private Label lblResultados;

    // ============================================================
    // TABLA
    // ============================================================
    @FXML
    private TableView<RegistroActualizacion> tablaActualizaciones;
    @FXML
    private TableColumn<RegistroActualizacion, String> colId;
    @FXML
    private TableColumn<RegistroActualizacion, String> colEquipo;
    @FXML
    private TableColumn<RegistroActualizacion, String> colLugar;
    @FXML
    private TableColumn<RegistroActualizacion, String> colResponsable;
    @FXML
    private TableColumn<RegistroActualizacion, String> colTipo;
    @FXML
    private TableColumn<RegistroActualizacion, String> colNombre;
    @FXML
    private TableColumn<RegistroActualizacion, String> colVersionAnterior;
    @FXML
    private TableColumn<RegistroActualizacion, String> colVersionNueva;
    @FXML
    private TableColumn<RegistroActualizacion, String> colFecha;
    @FXML
    private TableColumn<RegistroActualizacion, String> colAcciones;

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
    private final ObservableList<RegistroActualizacion> datos = FXCollections.observableArrayList();
    private final List<Equipo> catalogoEquipos = new ArrayList<>();
    private final List<SistemaOperativo> catalogoSistemasOperativos = new ArrayList<>();
    private final List<Usuario> catalogoResponsables = new ArrayList<>();

    private FilteredList<RegistroActualizacion> datosFiltrados;

    /** Evita que la recarga programática de los ComboBox dispare el filtro. */
    private boolean actualizandoFiltros = false;

    // ============================================================
    // INICIALIZACIÓN
    // ============================================================
    @FXML
    public void initialize() {
        cargarCatalogos(); // TODO BD: equipoRepository / usuarioRepository / sistemaOperativoRepository
        configurarTabla();
        configurarFiltros();
        cargarDatosEstaticos(); // TODO BD: actualizacionRepository.obtenerTodos()
        refrescarCombosDeFiltro();
        aplicarFiltros();
        cargarEstadisticas();
        actualizarEstadoBotones();
    }

    // ============================================================
    // CONFIGURACIÓN DE LA TABLA
    // ============================================================
    private void configurarTabla() {

        colId.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getIdActualizacion() == null
                        ? SIN_DATO
                        : String.valueOf(c.getValue().getIdActualizacion())));

        colEquipo.setCellValueFactory(c -> new SimpleStringProperty(
                modeloEquipo(c.getValue().getIdEquipo())));

        colLugar.setCellValueFactory(c -> new SimpleStringProperty(
                lugarEquipo(c.getValue().getIdEquipo())));

        colResponsable.setCellValueFactory(c -> new SimpleStringProperty(
                responsableEquipo(c.getValue().getIdEquipo())));

        colTipo.setCellValueFactory(c -> new SimpleStringProperty(
                etiquetaTipo(c.getValue().getTipo())));

        colNombre.setCellValueFactory(c -> new SimpleStringProperty(
                valorODash(c.getValue().getNombreActualizado())));

        colVersionAnterior.setCellValueFactory(c -> new SimpleStringProperty(
                valorODash(c.getValue().getVersionActual())));

        colVersionNueva.setCellValueFactory(c -> new SimpleStringProperty(
                valorODash(c.getValue().getVersionActualizada())));

        colFecha.setCellValueFactory(c -> new SimpleStringProperty(
                formatearFecha(c.getValue().getFecha())));

        // ---- Orden correcto en las columnas numéricas y de fecha ----
        colId.setComparator(comparadorNumerico());
        colFecha.setComparator(comparadorFecha());

        // ---- Tipo con insignia de color ----
        colTipo.setCellFactory(col -> new TableCell<RegistroActualizacion, String>() {
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

        // ---- Nombre actualizado: texto completo en un tooltip ----
        colNombre.setCellFactory(col -> new TableCell<RegistroActualizacion, String>() {
            @Override
            protected void updateItem(String nombre, boolean vacio) {
                super.updateItem(nombre, vacio);
                if (vacio || nombre == null) {
                    setText(null);
                    setTooltip(null);
                    return;
                }
                setText(nombre);
                setTooltip(new Tooltip(nombre));
            }
        });

        // ---- Versión nueva resaltada ----
        colVersionNueva.setCellFactory(col -> new TableCell<RegistroActualizacion, String>() {
            @Override
            protected void updateItem(String version, boolean vacio) {
                super.updateItem(version, vacio);
                if (vacio || version == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(version);
                setStyle("-fx-text-fill: #15803d; -fx-font-weight: bold;");
            }
        });

        // ---- Acciones por fila ----
        colAcciones.setCellValueFactory(c -> new SimpleStringProperty(""));
        colAcciones.setCellFactory(col -> new TableCell<RegistroActualizacion, String>() {

            private final Button bVer = miniBoton("👁", "#f1f5f9", "#475569");
            private final Button bEditar = miniBoton("✏", "#eef2ff", "#1d4ed8");
            private final Button bEliminar = miniBoton("🗑", "#fee2e2", "#b91c1c");
            private final HBox caja = new HBox(6, bVer, bEditar, bEliminar);

            {
                caja.setAlignment(Pos.CENTER_LEFT);
                bVer.setTooltip(new Tooltip("Ver detalle"));
                bEditar.setTooltip(new Tooltip("Editar"));
                bEliminar.setTooltip(new Tooltip("Eliminar actualización"));
            }

            @Override
            protected void updateItem(String valor, boolean vacio) {
                super.updateItem(valor, vacio);

                int fila = getIndex();
                if (vacio || fila < 0 || fila >= getTableView().getItems().size()) {
                    setGraphic(null);
                    return;
                }

                RegistroActualizacion actualizacion = getTableView().getItems().get(fila);

                bVer.setOnAction(e -> mostrarDetalle(actualizacion));
                bEditar.setOnAction(e -> abrirEdicion(actualizacion));
                bEliminar.setOnAction(e -> eliminar(actualizacion));

                setGraphic(caja);
                setAlignment(Pos.CENTER_LEFT);
            }
        });

        tablaActualizaciones.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        // Doble clic sobre la fila -> editar
        tablaActualizaciones.setRowFactory(tv -> {
            TableRow<RegistroActualizacion> fila = new TableRow<>();
            fila.setPrefHeight(40);
            fila.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !fila.isEmpty()) {
                    abrirEdicion(fila.getItem());
                }
            });
            return fila;
        });

        tablaActualizaciones.getSelectionModel().selectedItemProperty()
                .addListener((obs, anterior, actual) -> actualizarEstadoBotones());
    }

    // ============================================================
    // CONFIGURACIÓN DE FILTROS
    // ============================================================
    private void configurarFiltros() {

        cmbCampo.getItems().addAll(CAMPO_TODOS, "ID", "Equipo", "Lugar", "Responsable",
                "Nombre actualizado", "Versión");
        cmbCampo.getSelectionModel().selectFirst();

        cmbTipo.getItems().add(TIPO_TODOS);
        for (TipoActualizacion tipo : TipoActualizacion.values()) {
            cmbTipo.getItems().add(tipo.getEtiqueta());
        }
        cmbTipo.getSelectionModel().selectFirst();

        cmbPeriodo.getItems().addAll(PERIODO_TODOS, PERIODO_30_DIAS, PERIODO_ESTE_ANIO,
                PERIODO_ANIO_ANTERIOR, PERIODO_ANTERIORES);
        cmbPeriodo.getSelectionModel().selectFirst();

        cmbEquipo.getItems().add(EQUIPO_TODOS);
        cmbEquipo.getSelectionModel().selectFirst();

        datosFiltrados = new FilteredList<>(datos, a -> true);
        SortedList<RegistroActualizacion> ordenados = new SortedList<>(datosFiltrados);
        ordenados.comparatorProperty().bind(tablaActualizaciones.comparatorProperty());
        tablaActualizaciones.setItems(ordenados);
    }

    /** Buscador (onKeyReleased) y ComboBox de filtros (onAction). */
    @FXML
    private void filtrarActualizaciones() {
        if (actualizandoFiltros) {
            return;
        }
        aplicarFiltros();
    }

    /**
     * Chips de tipo. El ComboBox de tipo es la única fuente de verdad del
     * filtro.
     */
    @FXML
    private void filtrarPorTipo(ActionEvent e) {
        Object origen = e.getSource();

        if (origen == chipSistemaOperativo) {
            cmbTipo.setValue(TipoActualizacion.SISTEMA_OPERATIVO.getEtiqueta());
        } else if (origen == chipPrograma) {
            cmbTipo.setValue(TipoActualizacion.PROGRAMA.getEtiqueta());
        } else if (origen == chipFirmware) {
            cmbTipo.setValue(TipoActualizacion.FIRMWARE.getEtiqueta());
        } else if (origen == chipDriver) {
            cmbTipo.setValue(TipoActualizacion.DRIVER.getEtiqueta());
        } else {
            cmbTipo.setValue(TIPO_TODOS);
        }

        aplicarFiltros();
    }

    @FXML
    private void limpiarFiltros() {
        txtBuscar.clear();
        cmbCampo.setValue(CAMPO_TODOS);
        cmbTipo.setValue(TIPO_TODOS);
        cmbPeriodo.setValue(PERIODO_TODOS);
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
        final String periodo = cmbPeriodo.getValue();
        final String equipo = cmbEquipo.getValue();

        datosFiltrados.setPredicate(a -> coincideTexto(a, q, campo)
                && coincideTipo(a, tipo)
                && coincidePeriodo(a, periodo)
                && coincideEquipo(a, equipo));

        resaltarChipActivo(tipo);
        actualizarResultados();
        actualizarEstadoBotones();
    }

    private boolean coincideTexto(RegistroActualizacion a, String q, String campo) {
        if (q.isEmpty()) {
            return true;
        }
        String buscado = (campo == null) ? CAMPO_TODOS : campo;

        return switch (buscado) {
            case "ID" -> String.valueOf(a.getIdActualizacion()).contains(q);
            case "Equipo" -> contiene(modeloEquipo(a.getIdEquipo()), q);
            case "Lugar" -> contiene(lugarEquipo(a.getIdEquipo()), q);
            case "Responsable" -> contiene(responsableEquipo(a.getIdEquipo()), q);
            case "Nombre actualizado" -> contiene(a.getNombreActualizado(), q);
            case "Versión" -> contiene(a.getVersionActual(), q)
                    || contiene(a.getVersionActualizada(), q);
            default -> contiene(modeloEquipo(a.getIdEquipo()), q)
                    || contiene(lugarEquipo(a.getIdEquipo()), q)
                    || contiene(responsableEquipo(a.getIdEquipo()), q)
                    || contiene(a.getNombreActualizado(), q)
                    || contiene(a.getVersionActual(), q)
                    || contiene(a.getVersionActualizada(), q)
                    || contiene(etiquetaTipo(a.getTipo()), q)
                    || contiene(formatearFecha(a.getFecha()), q)
                    || String.valueOf(a.getIdActualizacion()).contains(q);
        };
    }

    private boolean coincideTipo(RegistroActualizacion a, String tipo) {
        if (tipo == null || TIPO_TODOS.equals(tipo)) {
            return true;
        }
        return etiquetaTipo(a.getTipo()).equalsIgnoreCase(tipo);
    }

    private boolean coincideEquipo(RegistroActualizacion a, String equipo) {
        if (equipo == null || EQUIPO_TODOS.equals(equipo)) {
            return true;
        }
        return equipo.equalsIgnoreCase(descripcionEquipo(a.getIdEquipo()));
    }

    /** Filtro por antigüedad del registro, calculado sobre la columna fecha. */
    private boolean coincidePeriodo(RegistroActualizacion a, String periodo) {
        if (periodo == null || PERIODO_TODOS.equals(periodo)) {
            return true;
        }

        LocalDate fecha = aLocalDate(a.getFecha());
        if (fecha == null) {
            return false;
        }

        LocalDate hoy = LocalDate.now();

        if (PERIODO_30_DIAS.equals(periodo)) {
            return !fecha.isBefore(hoy.minusDays(30)) && !fecha.isAfter(hoy);
        }
        if (PERIODO_ESTE_ANIO.equals(periodo)) {
            return fecha.getYear() == hoy.getYear();
        }
        if (PERIODO_ANIO_ANTERIOR.equals(periodo)) {
            return fecha.getYear() == hoy.getYear() - 1;
        }
        if (PERIODO_ANTERIORES.equals(periodo)) {
            return fecha.getYear() < hoy.getYear() - 1;
        }
        return true;
    }

    private void resaltarChipActivo(String tipo) {
        Button activo = chipTodos;

        if (TipoActualizacion.SISTEMA_OPERATIVO.getEtiqueta().equalsIgnoreCase(tipo)) {
            activo = chipSistemaOperativo;
        } else if (TipoActualizacion.PROGRAMA.getEtiqueta().equalsIgnoreCase(tipo)) {
            activo = chipPrograma;
        } else if (TipoActualizacion.FIRMWARE.getEtiqueta().equalsIgnoreCase(tipo)) {
            activo = chipFirmware;
        } else if (TipoActualizacion.DRIVER.getEtiqueta().equalsIgnoreCase(tipo)) {
            activo = chipDriver;
        }

        for (Node n : contenedorChips.getChildren()) {
            if (n instanceof Button) {
                n.setOpacity(n == activo ? 1.0 : 0.5);
            }
        }
    }

    private void actualizarResultados() {
        int visibles = datosFiltrados.size();
        lblResultados.setText(visibles == 1 ? "1 actualización" : visibles + " actualizaciones");
    }

    /**
     * Reconstruye la lista de equipos del filtro a partir de las
     * actualizaciones capturadas.
     */
    private void refrescarCombosDeFiltro() {
        actualizandoFiltros = true;
        try {
            String seleccion = cmbEquipo.getValue();

            List<String> items = new ArrayList<>();
            items.add(EQUIPO_TODOS);

            datos.stream()
                    .map(RegistroActualizacion::getIdEquipo)
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
    private void nuevaActualizacion() {
        Optional<RegistroActualizacion> resultado = abrirFormulario(null);

        resultado.ifPresent(nueva -> {
            // TODO BD: actualizacionRepository.guardar(nueva);
            // INSERT INTO registros_actualizaciones
            // (id_equipo, tipo, fecha, nombre_actualizado, version_actual, version_actualizada)
            // VALUES (?,?,?,?,?,?)
            nueva.setIdActualizacion(siguienteId());
            datos.add(nueva);

            refrescar();
            tablaActualizaciones.getSelectionModel().select(nueva);
            info("Actualización registrada",
                    "Se registró la actualización de " + valorODash(nueva.getNombreActualizado())
                            + " a la versión " + valorODash(nueva.getVersionActualizada())
                            + " en el equipo " + descripcionEquipo(nueva.getIdEquipo()) + ".");
        });
    }

    // ============================================================
    // CRUD - ACTUALIZAR
    // ============================================================
    @FXML
    private void editarActualizacion() {
        RegistroActualizacion seleccionada = seleccionOAviso();
        if (seleccionada != null) {
            abrirEdicion(seleccionada);
        }
    }

    private void abrirEdicion(RegistroActualizacion actualizacion) {
        if (actualizacion == null) {
            return;
        }
        Optional<RegistroActualizacion> resultado = abrirFormulario(actualizacion);

        resultado.ifPresent(modificada -> {
            // TODO BD: actualizacionRepository.actualizar(modificada);
            // UPDATE registros_actualizaciones SET id_equipo=?, tipo=?, fecha=?,
            // nombre_actualizado=?, version_actual=?, version_actualizada=? WHERE id=?
            refrescar();
            tablaActualizaciones.getSelectionModel().select(modificada);
            info("Cambios guardados",
                    "Se actualizó el registro #" + modificada.getIdActualizacion() + ".");
        });
    }

    // ============================================================
    // CRUD - ELIMINAR (borrado físico)
    // ============================================================
    @FXML
    private void eliminarActualizacion() {
        RegistroActualizacion seleccionada = seleccionOAviso();
        if (seleccionada != null) {
            eliminar(seleccionada);
        }
    }

    private void eliminar(RegistroActualizacion actualizacion) {
        if (actualizacion == null) {
            return;
        }

        boolean confirmado = confirmar(
                "Eliminar actualización",
                "¿Deseas eliminar definitivamente el registro #"
                        + actualizacion.getIdActualizacion() + " ("
                        + valorODash(actualizacion.getNombreActualizado()) + ") del equipo "
                        + descripcionEquipo(actualizacion.getIdEquipo()) + "?",
                "Esta acción no se puede deshacer. Por la llave foránea "
                        + "historial_registros.id_registro_actualizacion (ON DELETE CASCADE) "
                        + "también se borrarán las filas del historial que apunten a este "
                        + "registro. El equipo NO se elimina.");

        if (confirmado) {
            // TODO BD:
            // actualizacionRepository.eliminar(actualizacion.getIdActualizacion());
            // DELETE FROM registros_actualizaciones WHERE id = ?
            datos.remove(actualizacion);
            refrescar();
            info("Actualización eliminada",
                    "Se eliminó el registro #" + actualizacion.getIdActualizacion() + ".");
        }
    }

    // ============================================================
    // CRUD - VER
    // ============================================================
    @FXML
    private void verActualizacion() {
        RegistroActualizacion seleccionada = seleccionOAviso();
        if (seleccionada != null) {
            mostrarDetalle(seleccionada);
        }
    }

    /**
     * Modal de solo lectura: primero la actualización y después los datos
     * relevantes del equipo al que pertenece.
     */
    private void mostrarDetalle(RegistroActualizacion a) {
        if (a == null) {
            return;
        }

        Equipo equipo = buscarEquipo(a.getIdEquipo());

        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(9);
        grid.setPadding(new Insets(16, 20, 8, 20));

        int f = 0;
        f = agregarSeccion(grid, f, "Datos de la actualización");
        agregarDetalle(grid, f++, "ID",
                a.getIdActualizacion() == null ? SIN_DATO : String.valueOf(a.getIdActualizacion()));
        agregarDetalle(grid, f++, "Tipo", etiquetaTipo(a.getTipo()));
        agregarDetalle(grid, f++, "Nombre actualizado", valorODash(a.getNombreActualizado()));
        agregarDetalle(grid, f++, "Versión anterior", valorODash(a.getVersionActual()));
        agregarDetalle(grid, f++, "Versión nueva", valorODash(a.getVersionActualizada()));
        agregarDetalle(grid, f++, "Cambio de versión", cambioDeVersion(a));
        agregarDetalle(grid, f++, "Fecha", formatearFechaHora(a.getFecha()));

        f = agregarSeccion(grid, f, "Datos del equipo");
        agregarDetalle(grid, f++, "ID del equipo",
                a.getIdEquipo() == null ? SIN_DATO : String.valueOf(a.getIdEquipo()));
        agregarDetalle(grid, f++, "Modelo", modeloEquipo(a.getIdEquipo()));
        agregarDetalle(grid, f++, "Lugar", lugarEquipo(a.getIdEquipo()));
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
        agregarDetalle(grid, f++, "Responsable", responsableEquipo(a.getIdEquipo()));
        agregarDetalle(grid, f, "Estado del equipo",
                equipo == null ? SIN_DATO : etiquetaEstado(equipo.getEstado()));

        Dialog<Void> dialogo = new Dialog<>();
        dialogo.setTitle("Detalle de la actualización");
        dialogo.setHeaderText(etiquetaTipo(a.getTipo()) + " · " + descripcionEquipo(a.getIdEquipo()));
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
     * @param base actualización a editar, o {@code null} para registrar una
     *             nueva.
     * @return el registro creado/actualizado, o vacío si se canceló.
     */
    private Optional<RegistroActualizacion> abrirFormulario(RegistroActualizacion base) {

        final boolean esNueva = (base == null);

        Dialog<RegistroActualizacion> dialogo = new Dialog<>();
        dialogo.setTitle(esNueva ? "Nueva actualización" : "Editar actualización");
        dialogo.setHeaderText(esNueva
                ? "Registra una nueva actualización de un equipo"
                : "Modificando el registro #" + base.getIdActualizacion()
                        + " · " + descripcionEquipo(base.getIdEquipo()));

        ButtonType btnGuardar = new ButtonType(esNueva ? "Registrar actualización" : "Guardar cambios",
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

        ComboBox<TipoActualizacion> cmbTipoForm = new ComboBox<>();
        cmbTipoForm.getItems().addAll(TipoActualizacion.values());
        cmbTipoForm.setPrefWidth(300);
        cmbTipoForm.setConverter(new StringConverter<TipoActualizacion>() {
            @Override
            public String toString(TipoActualizacion tipo) {
                return tipo == null ? "" : tipo.getEtiqueta();
            }

            @Override
            public TipoActualizacion fromString(String s) {
                return TipoActualizacion.fromEtiqueta(s);
            }
        });

        DatePicker dpFecha = new DatePicker();
        dpFecha.setPrefWidth(300);
        dpFecha.setPromptText("dd/mm/aaaa");

        TextField txtNombre = new TextField();
        txtNombre.setPromptText("Ej. Windows 11 Pro, Google Chrome, BIOS…");
        txtNombre.setPrefWidth(300);

        TextField txtVersionActual = new TextField();
        txtVersionActual.setPromptText("Versión previa (opcional)");
        txtVersionActual.setPrefWidth(300);

        TextField txtVersionNueva = new TextField();
        txtVersionNueva.setPromptText("Versión a la que se actualizó");
        txtVersionNueva.setPrefWidth(300);

        // Resumen del equipo seleccionado (datos relevantes, solo lectura)
        Label lblResumenEquipo = new Label();
        lblResumenEquipo.setStyle("-fx-font-size: 10.5px; -fx-text-fill: #64748b;");
        lblResumenEquipo.setWrapText(true);
        lblResumenEquipo.setMaxWidth(300);

        Label lblError = new Label();
        lblError.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 11.5px;");
        lblError.setWrapText(true);
        lblError.setMaxWidth(400);

        Label lblAyuda = new Label("El equipo, el tipo, la fecha, el nombre y la versión nueva son "
                + "obligatorios (NOT NULL en registros_actualizaciones). La versión anterior es "
                + "opcional. Nombre: máx. " + MAX_NOMBRE + " caracteres; versiones: máx. "
                + MAX_VERSION + ".");
        lblAyuda.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 10.5px;");
        lblAyuda.setWrapText(true);
        lblAyuda.setMaxWidth(400);

        // ---- Precarga ----
        if (esNueva) {
            cmbEquipoForm.setValue(SIN_EQUIPO);
            cmbTipoForm.setValue(TipoActualizacion.SISTEMA_OPERATIVO);
            dpFecha.setValue(LocalDate.now());
        } else {
            cmbEquipoForm.setValue(buscarEquipoOVacio(base.getIdEquipo()));
            cmbTipoForm.setValue(base.getTipo());
            dpFecha.setValue(aLocalDate(base.getFecha()));
            txtNombre.setText(texto(base.getNombreActualizado()));
            txtVersionActual.setText(texto(base.getVersionActual()));
            txtVersionNueva.setText(texto(base.getVersionActualizada()));
        }
        lblResumenEquipo.setText(resumenEquipo(cmbEquipoForm.getValue()));

        // ---- Ayudas dinámicas (se registran DESPUÉS de la precarga) ----
        cmbEquipoForm.valueProperty().addListener((obs, anterior, actual) -> {
            lblResumenEquipo.setText(resumenEquipo(actual));
            sugerirDatosDelSistemaOperativo(actual, cmbTipoForm.getValue(), txtNombre, txtVersionActual);
        });

        cmbTipoForm.valueProperty().addListener((obs, anterior, actual) -> sugerirDatosDelSistemaOperativo(
                cmbEquipoForm.getValue(), actual, txtNombre, txtVersionActual));

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
        grid.add(etiqueta("Nombre actualizado *"), 0, f);
        grid.add(txtNombre, 1, f++);
        grid.add(etiqueta("Versión anterior"), 0, f);
        grid.add(txtVersionActual, 1, f++);
        grid.add(etiqueta("Versión nueva *"), 0, f);
        grid.add(txtVersionNueva, 1, f++);
        grid.add(lblAyuda, 1, f++);
        grid.add(lblError, 1, f);

        dialogo.getDialogPane().setContent(grid);
        dialogo.getDialogPane().setMinWidth(560);
        prepararVentana(dialogo);

        Platform.runLater(cmbEquipoForm::requestFocus);

        // ---- Validación antes de cerrar ----
        Node nodoGuardar = dialogo.getDialogPane().lookupButton(btnGuardar);
        nodoGuardar.addEventFilter(ActionEvent.ACTION, evento -> {
            String error = validar(
                    base,
                    cmbEquipoForm.getValue(),
                    cmbTipoForm.getValue(),
                    dpFecha.getValue(),
                    txtNombre.getText(),
                    txtVersionActual.getText(),
                    txtVersionNueva.getText());

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

            RegistroActualizacion a = esNueva ? new RegistroActualizacion() : base;

            Equipo equipo = cmbEquipoForm.getValue();
            a.setIdEquipo(equipo == null ? null : equipo.getIdEquipo());
            a.setTipo(cmbTipoForm.getValue());
            a.setFecha(aTimestamp(dpFecha.getValue(), esNueva ? null : base.getFecha()));
            a.setNombreActualizado(txtNombre.getText().trim());
            a.setVersionActual(vacioANulo(txtVersionActual.getText()));
            a.setVersionActualizada(txtVersionNueva.getText().trim());

            return a;
        });

        return dialogo.showAndWait();
    }

    /**
     * Cuando el tipo es "Sistema operativo" y el equipo tiene uno asignado,
     * propone el nombre y la versión que ya trae el equipo. Solo rellena los
     * campos que estén vacíos, nunca pisa lo que el usuario escribió.
     */
    private void sugerirDatosDelSistemaOperativo(Equipo equipo, TipoActualizacion tipo,
            TextField txtNombre, TextField txtVersionActual) {

        if (tipo != TipoActualizacion.SISTEMA_OPERATIVO || equipo == null) {
            return;
        }

        SistemaOperativo so = buscarSistemaOperativo(equipo.getIdSistemaOperativo());
        if (so == null) {
            return;
        }

        if (txtNombre.getText() == null || txtNombre.getText().isBlank()) {
            txtNombre.setText(texto(so.getNombre()));
        }
        if (txtVersionActual.getText() == null || txtVersionActual.getText().isBlank()) {
            txtVersionActual.setText(texto(so.getVersionActual()));
        }
    }

    /** @return mensaje de error, o {@code null} si todo es válido. */
    private String validar(RegistroActualizacion base, Equipo equipo, TipoActualizacion tipo,
            LocalDate fecha, String nombre, String versionActual, String versionNueva) {

        if (equipo == null || equipo.getIdEquipo() == null) {
            return "Selecciona el equipo al que pertenece la actualización "
                    + "(id_equipo es NOT NULL en la base de datos).";
        }
        if (tipo == null) {
            return "Selecciona el tipo de actualización (sistema operativo, programa, firmware o driver).";
        }
        if (fecha == null) {
            return "La fecha de la actualización es obligatoria.";
        }
        if (fecha.isAfter(LocalDate.now())) {
            return "La fecha no puede ser posterior a hoy: es el registro de una actualización ya aplicada.";
        }

        String limpioNombre = texto(nombre).trim();
        if (limpioNombre.isBlank()) {
            return "El nombre de lo actualizado es obligatorio.";
        }
        if (limpioNombre.length() > MAX_NOMBRE) {
            return "El nombre no puede pasar de " + MAX_NOMBRE + " caracteres (van "
                    + limpioNombre.length() + ").";
        }

        String limpiaNueva = texto(versionNueva).trim();
        if (limpiaNueva.isBlank()) {
            return "La versión nueva es obligatoria (version_actualizada es NOT NULL).";
        }
        if (limpiaNueva.length() > MAX_VERSION) {
            return "La versión nueva no puede pasar de " + MAX_VERSION + " caracteres (van "
                    + limpiaNueva.length() + ").";
        }

        String limpiaActual = texto(versionActual).trim();
        if (limpiaActual.length() > MAX_VERSION) {
            return "La versión anterior no puede pasar de " + MAX_VERSION + " caracteres (van "
                    + limpiaActual.length() + ").";
        }
        if (!limpiaActual.isBlank() && limpiaActual.equalsIgnoreCase(limpiaNueva)) {
            return "La versión nueva debe ser distinta de la anterior.";
        }

        if (duplicado(equipo.getIdEquipo(), limpioNombre, limpiaNueva, base)) {
            return "Ese equipo ya tiene registrada la actualización de \"" + limpioNombre
                    + "\" a la versión " + limpiaNueva + ".";
        }

        return null;
    }

    /**
     * Aviso de captura repetida (mismo equipo, mismo nombre y misma versión
     * nueva). No es una restricción de la base de datos.
     */
    private boolean duplicado(Long idEquipo, String nombre, String versionNueva,
            RegistroActualizacion excluida) {

        for (RegistroActualizacion a : datos) {
            if (a == excluida) {
                continue;
            }
            boolean mismoEquipo = idEquipo.equals(a.getIdEquipo());
            boolean mismoNombre = nombre.equalsIgnoreCase(texto(a.getNombreActualizado()).trim());
            boolean mismaVersion = versionNueva.equalsIgnoreCase(texto(a.getVersionActualizada()).trim());

            if (mismoEquipo && mismoNombre && mismaVersion) {
                return true;
            }
        }
        return false;
    }

    // ============================================================
    // ESTADÍSTICAS
    // ============================================================
    private void cargarEstadisticas() {
        LocalDate hoy = LocalDate.now();

        lblTotalActualizaciones.setText(String.valueOf(datos.size()));

        lblEquiposActualizados.setText(String.valueOf(
                datos.stream()
                        .map(RegistroActualizacion::getIdEquipo)
                        .filter(id -> id != null)
                        .distinct()
                        .count()));

        lblEsteAnio.setText(String.valueOf(
                datos.stream()
                        .map(a -> aLocalDate(a.getFecha()))
                        .filter(f -> f != null && f.getYear() == hoy.getYear())
                        .count()));

        lblUltimos30.setText(String.valueOf(
                datos.stream()
                        .map(a -> aLocalDate(a.getFecha()))
                        .filter(f -> f != null && !f.isBefore(hoy.minusDays(30)) && !f.isAfter(hoy))
                        .count()));
    }

    /** Recalcula filtros, KPIs, contador y refresca las celdas de la tabla. */
    private void refrescar() {
        tablaActualizaciones.refresh();
        refrescarCombosDeFiltro();
        aplicarFiltros();
        cargarEstadisticas();
        actualizarEstadoBotones();
    }

    // ============================================================
    // CATÁLOGOS ESTÁTICOS (sustituir por la BD más adelante)
    // ============================================================
    private void cargarCatalogos() {

        // TODO BD: SELECT id, tipo, nombre, version_actual FROM sistemas_operativos
        catalogoSistemasOperativos.addAll(List.of(
                new SistemaOperativo(1L, "escritorio", "Windows 11 Pro", "23H2"),
                new SistemaOperativo(2L, "escritorio", "Windows 10 Pro", "22H2"),
                new SistemaOperativo(3L, "servidor", "Windows Server", "2019"),
                new SistemaOperativo(4L, "escritorio", "Ubuntu", "22.04 LTS"),
                new SistemaOperativo(5L, "escritorio", "Debian", "12"),
                new SistemaOperativo(6L, "escritorio", "Fedora", "40"),
                new SistemaOperativo(7L, "escritorio", "macOS Sonoma", "14.5")));

        // TODO BD: SELECT id, nombre_completo, ... FROM usuarios WHERE activo = TRUE
        catalogoResponsables.addAll(List.of(
                responsable(1L, "Gerardo", "Espíndola", "Ramírez", TipoUsuario.ADMINISTRADOR),
                responsable(2L, "Luis Ángel", "Ortega", "Mendoza", TipoUsuario.TECNICO),
                responsable(3L, "Karla", "Núñez", "Salinas", TipoUsuario.TECNICO),
                responsable(4L, "Diego", "Salas", "Ibarra", TipoUsuario.TECNICO),
                responsable(5L, "Ana Sofía", "Ramírez", "Cortés", TipoUsuario.CONSULTA),
                responsable(6L, "Jorge", "Medina", "Aguilar", TipoUsuario.CONSULTA),
                responsable(7L, "Mariana", "Beltrán", "Ochoa", TipoUsuario.ADMINISTRADOR),
                responsable(11L, "Fernanda", "Zamora", "Ríos", TipoUsuario.TECNICO)));

        // TODO BD: SELECT * FROM equipos
        catalogoEquipos.addAll(List.of(
                equipo(1L, "HP ProDesk 400 G7", "Laboratorio de Redes", "Intel Core i5-10500",
                        "16GB", "512GB SSD", (short) 2021, EstadoEquipo.ACTIVO, 1L, 2L),
                equipo(2L, "Dell OptiPlex 3080", "Laboratorio de Redes", "Intel Core i5-10505",
                        "8GB", "256GB SSD", (short) 2021, EstadoEquipo.ACTIVO, 1L, 2L),
                equipo(3L, "Lenovo ThinkCentre M75q", "Laboratorio de Software", "AMD Ryzen 5 4600GE",
                        "16GB", "1TB SSD", (short) 2022, EstadoEquipo.ACTIVO, 1L, 3L),
                equipo(4L, "HP EliteDesk 800 G4", "Laboratorio de Software", "Intel Core i7-8700",
                        "32GB", "1TB SSD", (short) 2019, EstadoEquipo.EN_MANTENIMIENTO, 2L, 3L),
                equipo(5L, "Dell Latitude 5420", "Dirección Académica", "Intel Core i7-1165G7",
                        "16GB", "512GB SSD", (short) 2022, EstadoEquipo.ACTIVO, 1L, 7L),
                equipo(6L, "Lenovo ThinkPad E14", "Control Escolar", "AMD Ryzen 5 5500U",
                        "8GB", "256GB SSD", (short) 2022, EstadoEquipo.ACTIVO, 2L, 6L),
                equipo(7L, "HP ProDesk 600 G6", "Control Escolar", "Intel Core i5-10500",
                        "8GB", "1TB HDD", (short) 2020, EstadoEquipo.INACTIVO, 2L, 6L),
                equipo(8L, "Dell PowerEdge T40", "Site CGTI", "Intel Xeon E-2224G",
                        "32GB", "2TB HDD", (short) 2020, EstadoEquipo.ACTIVO, 3L, 1L),
                equipo(9L, "Acer Veriton X2660G", "Biblioteca", "Intel Core i3-9100",
                        "8GB", "500GB HDD", (short) 2019, EstadoEquipo.EN_MANTENIMIENTO, 2L, 4L),
                equipo(10L, "HP 280 Pro G6", "Biblioteca", "Intel Core i3-10100",
                        "8GB", "256GB SSD", (short) 2021, EstadoEquipo.ACTIVO, 1L, 4L),
                equipo(11L, "Dell OptiPlex 7010", "Laboratorio de Cómputo 1", "Intel Core i5-3470",
                        "4GB", "500GB HDD", (short) 2013, EstadoEquipo.DE_BAJA, 2L, null),
                equipo(12L, "Lenovo ThinkCentre M720q", "Laboratorio de Cómputo 1", "Intel Core i5-9400T",
                        "16GB", "512GB SSD", (short) 2020, EstadoEquipo.ACTIVO, 4L, 11L),
                equipo(13L, "Apple MacBook Air M1", "Diseño Multimedia", "Apple M1",
                        "8GB", "256GB SSD", (short) 2021, EstadoEquipo.ACTIVO, 7L, 5L),
                equipo(14L, "HP Compaq Pro 6300", "Almacén CGTI", "Intel Core i3-3220",
                        "4GB", "320GB HDD", (short) 2012, EstadoEquipo.DE_BAJA, null, null),
                equipo(15L, "Dell Vostro 3681", "Recursos Humanos", "Intel Core i5-10400",
                        "8GB", "1TB HDD", (short) 2021, EstadoEquipo.INACTIVO, 2L, 7L)));
    }

    private Equipo equipo(Long id, String modelo, String lugar, String procesador, String ram,
            String almacenamiento, Short anio, EstadoEquipo estado, Long idSo, Long idResponsable) {

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
        return e;
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
    private void cargarDatosEstaticos() {
        // TODO BD: SELECT id, id_equipo, tipo, fecha, nombre_actualizado,
        // version_actual, version_actualizada FROM registros_actualizaciones
        // ORDER BY fecha DESC
        List<RegistroActualizacion> demo = List.of(
                crear(1L, 1L, TipoActualizacion.SISTEMA_OPERATIVO,
                        LocalDateTime.of(2026, 2, 10, 9, 15),
                        "Windows 11 Pro", "22H2", "23H2"),
                crear(2L, 1L, TipoActualizacion.DRIVER,
                        LocalDateTime.of(2026, 5, 4, 11, 0),
                        "Driver de red Realtek RTL8168", "10.50.826.2021", "10.62.1120.2024"),
                crear(3L, 2L, TipoActualizacion.SISTEMA_OPERATIVO,
                        LocalDateTime.of(2026, 1, 22, 8, 40),
                        "Windows 11 Pro", "22H2", "23H2"),
                crear(4L, 2L, TipoActualizacion.PROGRAMA,
                        LocalDateTime.of(2026, 6, 18, 13, 25),
                        "Google Chrome", "124.0.6367.91", "127.0.6533.72"),
                crear(5L, 3L, TipoActualizacion.PROGRAMA,
                        LocalDateTime.of(2026, 3, 14, 10, 5),
                        "Visual Studio Code", "1.88.1", "1.92.0"),
                crear(6L, 3L, TipoActualizacion.FIRMWARE,
                        LocalDateTime.of(2026, 7, 2, 16, 30),
                        "BIOS Lenovo M2JKT4AA", "1.34", "1.41"),
                crear(7L, 4L, TipoActualizacion.SISTEMA_OPERATIVO,
                        LocalDateTime.of(2025, 11, 19, 9, 50),
                        "Windows 10 Pro", "21H2", "22H2"),
                crear(8L, 4L, TipoActualizacion.DRIVER,
                        LocalDateTime.of(2026, 4, 8, 12, 10),
                        "Driver de video Intel UHD 630", "31.0.101.2111", "31.0.101.5333"),
                crear(9L, 5L, TipoActualizacion.PROGRAMA,
                        LocalDateTime.of(2026, 5, 27, 15, 45),
                        "Microsoft Office", "2019", "2021"),
                crear(10L, 5L, TipoActualizacion.FIRMWARE,
                        LocalDateTime.of(2026, 1, 8, 8, 20),
                        "BIOS Dell Latitude 5420", "1.21.0", "1.33.0"),
                crear(11L, 6L, TipoActualizacion.SISTEMA_OPERATIVO,
                        LocalDateTime.of(2026, 6, 30, 10, 35),
                        "Windows 10 Pro", "21H2", "22H2"),
                crear(12L, 7L, TipoActualizacion.PROGRAMA,
                        LocalDateTime.of(2025, 9, 16, 14, 0),
                        "Adobe Acrobat Reader", "23.006.20380", "24.002.20857"),
                crear(13L, 8L, TipoActualizacion.SISTEMA_OPERATIVO,
                        LocalDateTime.of(2026, 2, 27, 7, 15),
                        "Windows Server", "2016", "2019"),
                crear(14L, 8L, TipoActualizacion.FIRMWARE,
                        LocalDateTime.of(2026, 7, 21, 18, 5),
                        "Firmware controladora PERC H330", "25.5.9.0001", "25.5.10.0002"),
                crear(15L, 9L, TipoActualizacion.DRIVER,
                        LocalDateTime.of(2026, 7, 9, 12, 40),
                        "Driver de video NVIDIA GT 1030", "537.13", "560.81"),
                crear(16L, 10L, TipoActualizacion.PROGRAMA,
                        LocalDateTime.of(2026, 4, 23, 11, 30),
                        "Mozilla Firefox", "125.0.3", "129.0"),
                crear(17L, 11L, TipoActualizacion.SISTEMA_OPERATIVO,
                        LocalDateTime.of(2025, 6, 5, 9, 0),
                        "Windows 10 Pro", "20H2", "21H2"),
                crear(18L, 12L, TipoActualizacion.SISTEMA_OPERATIVO,
                        LocalDateTime.of(2026, 3, 2, 8, 55),
                        "Ubuntu", "20.04 LTS", "22.04 LTS"),
                crear(19L, 12L, TipoActualizacion.PROGRAMA,
                        LocalDateTime.of(2026, 8, 4, 10, 20),
                        "LibreOffice", "7.6.7", "24.2.5"),
                crear(20L, 13L, TipoActualizacion.SISTEMA_OPERATIVO,
                        LocalDateTime.of(2026, 7, 28, 17, 35),
                        "macOS Sonoma", "14.4.1", "14.5"),
                crear(21L, 13L, TipoActualizacion.PROGRAMA,
                        LocalDateTime.of(2026, 8, 7, 9, 10),
                        "Xcode", "15.3", "15.4"),
                crear(22L, 15L, TipoActualizacion.DRIVER,
                        LocalDateTime.of(2026, 6, 11, 13, 50),
                        "Driver de impresora HP LaserJet Pro", "40.15.1153", "41.3.2653"),
                crear(23L, 6L, TipoActualizacion.PROGRAMA,
                        LocalDateTime.of(2026, 8, 10, 11, 5),
                        "7-Zip", "23.01", "24.07"),
                crear(24L, 10L, TipoActualizacion.FIRMWARE,
                        LocalDateTime.of(2025, 12, 3, 15, 15),
                        "BIOS HP 280 Pro G6", "F.10", "F.22"));

        datos.setAll(demo);
    }

    private RegistroActualizacion crear(Long id, Long idEquipo, TipoActualizacion tipo,
            LocalDateTime fecha, String nombre, String versionActual, String versionNueva) {

        RegistroActualizacion a = new RegistroActualizacion();
        a.setIdActualizacion(id);
        a.setIdEquipo(idEquipo);
        a.setTipo(tipo);
        a.setFecha(Timestamp.valueOf(fecha));
        a.setNombreActualizado(nombre);
        a.setVersionActual(versionActual);
        a.setVersionActualizada(versionNueva);
        return a;
    }

    private Long siguienteId() {
        long max = datos.stream()
                .filter(a -> a.getIdActualizacion() != null)
                .mapToLong(RegistroActualizacion::getIdActualizacion)
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
    private void irAEquipos() {
        Navigator.navigate("/com/utng/ui/equipoModules/pantallaEquipos/PantallaEquipos.fxml");
    }

    @FXML
    private void irASistemasOperativos() {
        Navigator.navigate("/com/utng/ui/sitemasOperativosModules/PantallaSistemasOperativos/SistemasOperativos.fxml");
    }

    @FXML
    private void irAMantenimientos() {
        Navigator.navigate("/com/utng/ui/mantenimientoModules/pantallaMantenimientos/PantallaMantenimientos.fxml");
    }

    @FXML
    private void irAActualizaciones() {
        toggleMenu(); // ya estamos en esta pantalla
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
        boolean hay = tablaActualizaciones.getSelectionModel().getSelectedItem() != null;

        btnVer.setDisable(!hay);
        btnEditar.setDisable(!hay);
        btnEliminar.setDisable(!hay);
    }

    private RegistroActualizacion seleccionOAviso() {
        RegistroActualizacion sel = tablaActualizaciones.getSelectionModel().getSelectedItem();
        if (sel == null) {
            info("Sin selección", "Selecciona primero una actualización de la tabla.");
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

        if (TipoActualizacion.SISTEMA_OPERATIVO.getEtiqueta().equalsIgnoreCase(tipo)) {
            return base + "-fx-background-color: #dbeafe; -fx-text-fill: #1d4ed8;";
        }
        if (TipoActualizacion.PROGRAMA.getEtiqueta().equalsIgnoreCase(tipo)) {
            return base + "-fx-background-color: #ede9fe; -fx-text-fill: #6d28d9;";
        }
        if (TipoActualizacion.FIRMWARE.getEtiqueta().equalsIgnoreCase(tipo)) {
            return base + "-fx-background-color: #fef3c7; -fx-text-fill: #b45309;";
        }
        if (TipoActualizacion.DRIVER.getEtiqueta().equalsIgnoreCase(tipo)) {
            return base + "-fx-background-color: #ccfbf1; -fx-text-fill: #0f766e;";
        }
        return base + "-fx-background-color: #f1f5f9; -fx-text-fill: #64748b;";
    }

    /** Asocia el diálogo a la ventana principal para que se comporte como modal. */
    private void prepararVentana(Dialog<?> dialogo) {
        if (tablaActualizaciones.getScene() != null && tablaActualizaciones.getScene().getWindow() != null) {
            dialogo.initOwner(tablaActualizaciones.getScene().getWindow());
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

    private SistemaOperativo buscarSistemaOperativo(Long idSistemaOperativo) {
        if (idSistemaOperativo == null) {
            return null;
        }
        return catalogoSistemasOperativos.stream()
                .filter(so -> idSistemaOperativo.equals(so.getIdSistemaOperativo()))
                .findFirst()
                .orElse(null);
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
        SistemaOperativo so = buscarSistemaOperativo(id);
        return so == null ? SIN_DATO : so.getDescripcion();
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
    /** "22H2  →  23H2", o solo la versión nueva si no se registró la anterior. */
    private static String cambioDeVersion(RegistroActualizacion a) {
        String anterior = texto(a.getVersionActual()).trim();
        String nueva = texto(a.getVersionActualizada()).trim();

        if (nueva.isEmpty()) {
            return SIN_DATO;
        }
        return anterior.isEmpty() ? nueva : anterior + "  →  " + nueva;
    }

    private static String etiquetaTipo(TipoActualizacion tipo) {
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
        return Comparator.comparing(PantallaActualizacionesController::claveFecha);
    }

    /** Ordena los IDs como números y no como texto. Los "—" van al final. */
    private static Comparator<String> comparadorNumerico() {
        return Comparator.comparingLong(PantallaActualizacionesController::claveNumerica);
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

    /** Los VARCHAR opcionales se guardan como NULL, no como cadena vacía. */
    private static String vacioANulo(String valor) {
        String limpio = texto(valor).trim();
        return limpio.isEmpty() ? null : limpio;
    }

    private static boolean contiene(String valor, String busqueda) {
        return valor != null && valor.toLowerCase().contains(busqueda);
    }
}
