package com.utng.UserModule.controller;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import com.utng.UserModule.model.usuario.TipoUsuario;
import com.utng.UserModule.model.usuario.Usuario;
import com.utng.util.Navigator;
import com.utng.util.PasswordUtil;

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
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
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
 * Controlador de la pantalla de gestión de usuarios (CRUD).
 *
 * IMPORTANTE: por ahora trabaja con DATOS ESTÁTICOS EN MEMORIA.
 * Para conectarlo a PostgreSQL basta con sustituir los métodos marcados
 * con "// TODO BD:" por llamadas a UsuarioRepository.
 */
public class PantallaUsuariosController {

    // ============================================================
    // CONSTANTES DE FILTRO
    // ============================================================
    private static final String CAMPO_TODOS = "Todos los campos";
    private static final String ROL_TODOS = "Todos los roles";
    private static final String ESTADO_TODOS = "Todos";
    private static final String ESTADO_ACTIVOS = "Activos";
    private static final String ESTADO_INACTIVOS = "Inactivos";

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final Pattern PATRON_CORREO = Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[\\w.-]+$");

    /**
     * Marca para los registros de demostración: nunca se valida contra este valor.
     */
    private static final String HASH_DEMO = "$2a$12$demoDemoDemoDemoDemoDe";

    // ============================================================
    // KPIs
    // ============================================================
    @FXML
    private Label lblTotalUsuarios;
    @FXML
    private Label lblAdministradores;
    @FXML
    private Label lblTecnicos;
    @FXML
    private Label lblConsulta;
    @FXML
    private Label lblInactivos;

    // ============================================================
    // BARRA BUSCADORA Y FILTROS
    // ============================================================
    @FXML
    private TextField txtBuscar;
    @FXML
    private ComboBox<String> cmbCampo;
    @FXML
    private ComboBox<String> cmbRol;
    @FXML
    private ComboBox<String> cmbEstado;
    @FXML
    private HBox contenedorChips;
    @FXML
    private Button chipTodos;
    @FXML
    private Button chipAdministrador;
    @FXML
    private Button chipTecnico;
    @FXML
    private Button chipConsulta;
    @FXML
    private Label lblResultados;

    // ============================================================
    // TABLA
    // ============================================================
    @FXML
    private TableView<Usuario> tablaUsuarios;
    @FXML
    private TableColumn<Usuario, String> colId;
    @FXML
    private TableColumn<Usuario, String> colNombre;
    @FXML
    private TableColumn<Usuario, String> colApellidos;
    @FXML
    private TableColumn<Usuario, String> colCorreo;
    @FXML
    private TableColumn<Usuario, String> colRol;
    @FXML
    private TableColumn<Usuario, String> colEstado;
    @FXML
    private TableColumn<Usuario, String> colFecha;
    @FXML
    private TableColumn<Usuario, String> colAcciones;

    // ============================================================
    // BOTONERA
    // ============================================================
    @FXML
    private Button btnMenu;
    @FXML
    private Button btnVer;
    @FXML
    private Button btnEditar;
    @FXML
    private Button btnReactivar;
    @FXML
    private Button btnEliminar;
    @FXML
    private Button btnUsuarios;

    // ============================================================
    // MENÚ LATERAL
    // ============================================================
    @FXML
    private Region overlayMenu;
    @FXML
    private VBox panelMenu;

    @FXML
    private void irAEquipos() {
        toggleMenu();
        Navigator.navigate("/com/utng/ui/equipoModules/pantallaEquipos/PantallaEquipos.fxml");
    }

    // ============================================================
    // DATOS EN MEMORIA
    // ============================================================
    private final ObservableList<Usuario> datos = FXCollections.observableArrayList();
    private FilteredList<Usuario> datosFiltrados;

    // ============================================================
    // INICIALIZACIÓN
    // ============================================================
    @FXML
    public void initialize() {
        configurarTabla();
        configurarFiltros();
        cargarDatosEstaticos(); // TODO BD: reemplazar por usuarioRepository.obtenerTodos()
        aplicarFiltros();
        cargarEstadisticas();
        actualizarEstadoBotones();
    }

    // ============================================================
    // CONFIGURACIÓN DE LA TABLA
    // ============================================================
    private void configurarTabla() {

        colId.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getIdUsuario() == null ? "—" : String.valueOf(c.getValue().getIdUsuario())));

        colNombre.setCellValueFactory(c -> new SimpleStringProperty(
                texto(c.getValue().getNombreCompleto())));

        colApellidos.setCellValueFactory(c -> new SimpleStringProperty(
                (texto(c.getValue().getApellidoPaterno()) + " " + texto(c.getValue().getApellidoMaterno())).trim()));

        colCorreo.setCellValueFactory(c -> new SimpleStringProperty(
                texto(c.getValue().getCorreo())));

        colRol.setCellValueFactory(c -> new SimpleStringProperty(
                etiquetaRol(c.getValue().getTipoUsuario())));

        colEstado.setCellValueFactory(c -> new SimpleStringProperty(
                Boolean.TRUE.equals(c.getValue().getActivo()) ? "Activo" : "Inactivo"));

        colFecha.setCellValueFactory(c -> new SimpleStringProperty(
                formatear(c.getValue().getFechaCreacion(), FORMATO_FECHA)));

        // ---- Rol con color ----
        colRol.setCellFactory(col -> new TableCell<Usuario, String>() {
            private final Label chip = new Label();

            @Override
            protected void updateItem(String rol, boolean vacio) {
                super.updateItem(rol, vacio);
                if (vacio || rol == null) {
                    setGraphic(null);
                    return;
                }
                chip.setText(rol);
                chip.setStyle(estiloBadgeRol(rol));
                setGraphic(chip);
                setAlignment(Pos.CENTER_LEFT);
            }
        });

        // ---- Estado con badge ----
        colEstado.setCellFactory(col -> new TableCell<Usuario, String>() {
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
        colAcciones.setCellFactory(col -> new TableCell<Usuario, String>() {

            private final Button bVer = miniBoton("👁", "#f1f5f9", "#475569");
            private final Button bEditar = miniBoton("✏", "#eef2ff", "#1d4ed8");
            private final Button bEstado = miniBoton("🗑", "#fee2e2", "#b91c1c");
            private final HBox caja = new HBox(6, bVer, bEditar, bEstado);

            {
                caja.setAlignment(Pos.CENTER_LEFT);
            }

            @Override
            protected void updateItem(String valor, boolean vacio) {
                super.updateItem(valor, vacio);

                int fila = getIndex();
                if (vacio || fila < 0 || fila >= getTableView().getItems().size()) {
                    setGraphic(null);
                    return;
                }

                Usuario u = getTableView().getItems().get(fila);
                boolean activo = Boolean.TRUE.equals(u.getActivo());

                bVer.setTooltip(new Tooltip("Ver detalle"));
                bEditar.setTooltip(new Tooltip("Editar"));

                bEstado.setText(activo ? "🗑" : "⟳");
                bEstado.setStyle(estiloMiniBoton(activo ? "#fee2e2" : "#dcfce7",
                        activo ? "#b91c1c" : "#15803d"));
                bEstado.setTooltip(new Tooltip(activo ? "Dar de baja" : "Reactivar"));

                bVer.setOnAction(e -> mostrarDetalle(u));
                bEditar.setOnAction(e -> abrirEdicion(u));
                bEstado.setOnAction(e -> {
                    if (Boolean.TRUE.equals(u.getActivo())) {
                        darDeBaja(u);
                    } else {
                        reactivar(u);
                    }
                });

                setGraphic(caja);
                setAlignment(Pos.CENTER_LEFT);
            }
        });

        tablaUsuarios.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Doble clic sobre la fila -> editar
        tablaUsuarios.setRowFactory(tv -> {
            TableRow<Usuario> fila = new TableRow<>();
            fila.setPrefHeight(40);
            fila.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !fila.isEmpty()) {
                    abrirEdicion(fila.getItem());
                }
            });
            return fila;
        });

        tablaUsuarios.getSelectionModel().selectedItemProperty()
                .addListener((obs, anterior, actual) -> actualizarEstadoBotones());
    }

    // ============================================================
    // CONFIGURACIÓN DE FILTROS
    // ============================================================
    private void configurarFiltros() {

        cmbCampo.getItems().addAll(CAMPO_TODOS, "ID", "Nombre(s)", "Apellidos", "Correo", "Rol");
        cmbCampo.getSelectionModel().selectFirst();

        cmbRol.getItems().addAll(ROL_TODOS, "Administrador", "Técnico", "Consulta");
        cmbRol.getSelectionModel().selectFirst();

        cmbEstado.getItems().addAll(ESTADO_TODOS, ESTADO_ACTIVOS, ESTADO_INACTIVOS);
        cmbEstado.getSelectionModel().selectFirst();

        datosFiltrados = new FilteredList<>(datos, u -> true);
        SortedList<Usuario> ordenados = new SortedList<>(datosFiltrados);
        ordenados.comparatorProperty().bind(tablaUsuarios.comparatorProperty());
        tablaUsuarios.setItems(ordenados);
    }

    /** Buscador (onKeyReleased) y ComboBox de filtros (onAction). */
    @FXML
    private void filtrarUsuarios() {
        aplicarFiltros();
    }

    /** Chips de rol. El ComboBox de rol es la única fuente de verdad del filtro. */
    @FXML
    private void filtrarPorRol(ActionEvent e) {
        Object origen = e.getSource();

        if (origen == chipAdministrador) {
            cmbRol.setValue(etiquetaRol(TipoUsuario.ADMINISTRADOR));
        } else if (origen == chipTecnico) {
            cmbRol.setValue(etiquetaRol(TipoUsuario.TECNICO));
        } else if (origen == chipConsulta) {
            cmbRol.setValue(etiquetaRol(TipoUsuario.CONSULTA));
        } else {
            cmbRol.setValue(ROL_TODOS);
        }

        aplicarFiltros();
    }

    @FXML
    private void limpiarFiltros() {
        txtBuscar.clear();
        cmbCampo.setValue(CAMPO_TODOS);
        cmbRol.setValue(ROL_TODOS);
        cmbEstado.setValue(ESTADO_TODOS);
        aplicarFiltros();
    }

    private void aplicarFiltros() {
        if (datosFiltrados == null) {
            return;
        }

        final String q = (txtBuscar.getText() == null) ? "" : txtBuscar.getText().trim().toLowerCase();
        final String campo = cmbCampo.getValue();
        final String rol = cmbRol.getValue();
        final String estado = cmbEstado.getValue();

        datosFiltrados.setPredicate(u -> coincideTexto(u, q, campo)
                && coincideRol(u, rol)
                && coincideEstado(u, estado));

        resaltarChipActivo(rol);
        actualizarResultados();
        actualizarEstadoBotones();
    }

    private boolean coincideTexto(Usuario u, String q, String campo) {
        if (q.isEmpty()) {
            return true;
        }
        String buscado = (campo == null) ? CAMPO_TODOS : campo;

        return switch (buscado) {
            case "ID" -> String.valueOf(u.getIdUsuario()).contains(q);
            case "Nombre(s)" -> contiene(u.getNombreCompleto(), q);
            case "Apellidos" -> contiene(u.getApellidoPaterno(), q) || contiene(u.getApellidoMaterno(), q);
            case "Correo" -> contiene(u.getCorreo(), q);
            case "Rol" -> contiene(etiquetaRol(u.getTipoUsuario()), q);
            default -> contiene(u.getNombreCompleto(), q)
                    || contiene(u.getApellidoPaterno(), q)
                    || contiene(u.getApellidoMaterno(), q)
                    || contiene(u.getCorreo(), q)
                    || contiene(etiquetaRol(u.getTipoUsuario()), q)
                    || String.valueOf(u.getIdUsuario()).contains(q);
        };
    }

    private boolean coincideRol(Usuario u, String rol) {
        if (rol == null || ROL_TODOS.equals(rol)) {
            return true;
        }
        return etiquetaRol(u.getTipoUsuario()).equalsIgnoreCase(rol);
    }

    private boolean coincideEstado(Usuario u, String estado) {
        if (estado == null || ESTADO_TODOS.equals(estado)) {
            return true;
        }
        boolean activo = Boolean.TRUE.equals(u.getActivo());
        return ESTADO_ACTIVOS.equals(estado) ? activo : !activo;
    }

    private void resaltarChipActivo(String rol) {
        Button activo = chipTodos;
        if ("Administrador".equalsIgnoreCase(rol)) {
            activo = chipAdministrador;
        } else if ("Técnico".equalsIgnoreCase(rol)) {
            activo = chipTecnico;
        } else if ("Consulta".equalsIgnoreCase(rol)) {
            activo = chipConsulta;
        }

        for (Node n : contenedorChips.getChildren()) {
            if (n instanceof Button) {
                n.setOpacity(n == activo ? 1.0 : 0.5);
            }
        }
    }

    private void actualizarResultados() {
        int visibles = datosFiltrados.size();
        lblResultados.setText(visibles == 1 ? "1 usuario" : visibles + " usuarios");
    }

    // ============================================================
    // CRUD - CREAR
    // ============================================================
    @FXML
    private void nuevoUsuario() {
        Optional<Usuario> resultado = abrirFormulario(null);

        resultado.ifPresent(nuevo -> {
            // TODO BD: usuarioRepository.guardar(nuevo);
            nuevo.setIdUsuario(siguienteId());
            nuevo.setFechaCreacion(new Timestamp(System.currentTimeMillis()));
            datos.add(nuevo);

            refrescar();
            tablaUsuarios.getSelectionModel().select(nuevo);
            info("Usuario creado", "Se registró la cuenta " + nuevo.getCorreo() + ".");
        });
    }

    // ============================================================
    // CRUD - ACTUALIZAR
    // ============================================================
    @FXML
    private void editarUsuario() {
        Usuario seleccionado = seleccionOAviso();
        if (seleccionado != null) {
            abrirEdicion(seleccionado);
        }
    }

    private void abrirEdicion(Usuario usuario) {
        if (usuario == null) {
            return;
        }
        Optional<Usuario> resultado = abrirFormulario(usuario);

        resultado.ifPresent(actualizado -> {
            // TODO BD: usuarioRepository.actualizar(actualizado);
            refrescar();
            tablaUsuarios.getSelectionModel().select(actualizado);
            info("Cambios guardados", "Se actualizó la cuenta " + actualizado.getCorreo() + ".");
        });
    }

    // ============================================================
    // CRUD - ELIMINAR (baja lógica) Y REACTIVAR
    // ============================================================
    @FXML
    private void eliminarUsuario() {
        Usuario seleccionado = seleccionOAviso();
        if (seleccionado != null) {
            darDeBaja(seleccionado);
        }
    }

    private void darDeBaja(Usuario usuario) {
        if (usuario == null || Boolean.FALSE.equals(usuario.getActivo())) {
            return;
        }

        boolean confirmado = confirmar(
                "Dar de baja usuario",
                "¿Deseas desactivar la cuenta de " + nombreCompleto(usuario) + "?",
                "El usuario dejará de tener acceso al sistema, pero su historial de "
                        + "mantenimientos se conserva. Podrás reactivarlo cuando lo necesites.");

        if (confirmado) {
            // TODO BD: usuarioRepository.eliminar(usuario.getIdUsuario());
            usuario.setActivo(false);
            refrescar();
        }
    }

    @FXML
    private void reactivarUsuario() {
        Usuario seleccionado = seleccionOAviso();
        if (seleccionado != null) {
            reactivar(seleccionado);
        }
    }

    private void reactivar(Usuario usuario) {
        if (usuario == null || Boolean.TRUE.equals(usuario.getActivo())) {
            return;
        }

        boolean confirmado = confirmar(
                "Reactivar usuario",
                "¿Deseas reactivar la cuenta de " + nombreCompleto(usuario) + "?",
                "El usuario recuperará el acceso al sistema con el rol "
                        + etiquetaRol(usuario.getTipoUsuario()) + ".");

        if (confirmado) {
            // TODO BD: usuario.setActivo(true); usuarioRepository.actualizar(usuario);
            usuario.setActivo(true);
            refrescar();
        }
    }

    // ============================================================
    // CRUD - VER
    // ============================================================
    @FXML
    private void verUsuario() {
        Usuario seleccionado = seleccionOAviso();
        if (seleccionado != null) {
            mostrarDetalle(seleccionado);
        }
    }

    private void mostrarDetalle(Usuario u) {
        if (u == null) {
            return;
        }

        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(9);
        grid.setPadding(new Insets(16, 20, 8, 20));

        int f = 0;
        agregarDetalle(grid, f++, "ID", u.getIdUsuario() == null ? "—" : String.valueOf(u.getIdUsuario()));
        agregarDetalle(grid, f++, "Nombre(s)", texto(u.getNombreCompleto()));
        agregarDetalle(grid, f++, "Apellido paterno", texto(u.getApellidoPaterno()));
        agregarDetalle(grid, f++, "Apellido materno", u.getApellidoMaterno() == null || u.getApellidoMaterno().isBlank()
                ? "—"
                : u.getApellidoMaterno());
        agregarDetalle(grid, f++, "Correo", texto(u.getCorreo()));
        agregarDetalle(grid, f++, "Rol", etiquetaRol(u.getTipoUsuario()));
        agregarDetalle(grid, f++, "Estado", Boolean.TRUE.equals(u.getActivo()) ? "Activo" : "Inactivo");
        agregarDetalle(grid, f++, "Contraseña", "•••••••• (almacenada con hash)");
        agregarDetalle(grid, f++, "Fecha de alta", formatear(u.getFechaCreacion(), FORMATO_FECHA_HORA));
        agregarDetalle(grid, f++, "Intentos de recuperación",
                u.getIntentosRecuperacion() == null ? "0" : String.valueOf(u.getIntentosRecuperacion()));
        agregarDetalle(grid, f++, "Recuperación activa",
                Boolean.TRUE.equals(u.getRecuperacionActiva()) ? "Sí" : "No");
        agregarDetalle(grid, f, "Último código enviado", formatear(u.getFechaCodigo(), FORMATO_FECHA_HORA));

        Dialog<Void> dialogo = new Dialog<>();
        dialogo.setTitle("Detalle del usuario");
        dialogo.setHeaderText(nombreCompleto(u));
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
     * @param base usuario a editar, o {@code null} para crear uno nuevo.
     * @return el usuario creado/actualizado, o vacío si se canceló.
     */
    private Optional<Usuario> abrirFormulario(Usuario base) {

        final boolean esNuevo = (base == null);

        Dialog<Usuario> dialogo = new Dialog<>();
        dialogo.setTitle(esNuevo ? "Nuevo usuario" : "Editar usuario");
        dialogo.setHeaderText(esNuevo
                ? "Registra una nueva cuenta del sistema"
                : "Modificando la cuenta de " + nombreCompleto(base));

        ButtonType btnGuardar = new ButtonType(esNuevo ? "Crear usuario" : "Guardar cambios",
                ButtonBar.ButtonData.OK_DONE);
        dialogo.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);

        // ---- Campos ----
        TextField txtNombre = campo("Nombre(s)");
        TextField txtPaterno = campo("Apellido paterno");
        TextField txtMaterno = campo("Apellido materno (opcional)");
        TextField txtCorreo = campo("usuario@utng.edu.mx");

        PasswordField txtPassword = new PasswordField();
        txtPassword.setPromptText(esNuevo ? "Mínimo 8 caracteres" : "Dejar vacío para conservar la actual");
        txtPassword.setPrefWidth(260);

        PasswordField txtConfirmar = new PasswordField();
        txtConfirmar.setPromptText("Repite la contraseña");
        txtConfirmar.setPrefWidth(260);

        ComboBox<TipoUsuario> cmbTipo = new ComboBox<>();
        cmbTipo.getItems().addAll(TipoUsuario.values());
        cmbTipo.setPrefWidth(260);
        cmbTipo.setConverter(new StringConverter<TipoUsuario>() {
            @Override
            public String toString(TipoUsuario tipo) {
                return etiquetaRol(tipo);
            }

            @Override
            public TipoUsuario fromString(String s) {
                return tipoDesdeEtiqueta(s);
            }
        });

        CheckBox chkActivo = new CheckBox("Cuenta activa");
        chkActivo.setSelected(true);

        Label lblError = new Label();
        lblError.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 11.5px;");
        lblError.setWrapText(true);
        lblError.setMaxWidth(380);

        // ---- Precarga en modo edición ----
        if (!esNuevo) {
            txtNombre.setText(texto(base.getNombreCompleto()));
            txtPaterno.setText(texto(base.getApellidoPaterno()));
            txtMaterno.setText(base.getApellidoMaterno() == null ? "" : base.getApellidoMaterno());
            txtCorreo.setText(texto(base.getCorreo()));
            cmbTipo.setValue(base.getTipoUsuario());
            chkActivo.setSelected(Boolean.TRUE.equals(base.getActivo()));
        } else {
            cmbTipo.setValue(TipoUsuario.CONSULTA);
        }

        // ---- Layout ----
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(18, 22, 6, 22));

        int f = 0;
        grid.add(etiqueta("Nombre(s) *"), 0, f);
        grid.add(txtNombre, 1, f++);
        grid.add(etiqueta("Apellido paterno *"), 0, f);
        grid.add(txtPaterno, 1, f++);
        grid.add(etiqueta("Apellido materno"), 0, f);
        grid.add(txtMaterno, 1, f++);
        grid.add(etiqueta("Correo *"), 0, f);
        grid.add(txtCorreo, 1, f++);
        grid.add(etiqueta("Rol *"), 0, f);
        grid.add(cmbTipo, 1, f++);
        grid.add(etiqueta(esNuevo ? "Contraseña *" : "Nueva contraseña"), 0, f);
        grid.add(txtPassword, 1, f++);
        grid.add(etiqueta("Confirmar"), 0, f);
        grid.add(txtConfirmar, 1, f++);
        grid.add(etiqueta("Estado"), 0, f);
        grid.add(chkActivo, 1, f++);
        grid.add(lblError, 1, f);

        dialogo.getDialogPane().setContent(grid);
        dialogo.getDialogPane().setMinWidth(500);
        prepararVentana(dialogo);

        javafx.application.Platform.runLater(txtNombre::requestFocus);

        // ---- Validación antes de cerrar ----
        Node nodoGuardar = dialogo.getDialogPane().lookupButton(btnGuardar);
        nodoGuardar.addEventFilter(ActionEvent.ACTION, evento -> {
            String error = validar(
                    esNuevo,
                    base,
                    txtNombre.getText(),
                    txtPaterno.getText(),
                    txtCorreo.getText(),
                    cmbTipo.getValue(),
                    txtPassword.getText(),
                    txtConfirmar.getText());

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

            Usuario u = esNuevo ? new Usuario() : base;

            u.setNombreCompleto(txtNombre.getText().trim());
            u.setApellidoPaterno(txtPaterno.getText().trim());
            u.setApellidoMaterno(txtMaterno.getText() == null || txtMaterno.getText().isBlank()
                    ? null
                    : txtMaterno.getText().trim());
            u.setCorreo(txtCorreo.getText().trim().toLowerCase());
            u.setTipoUsuario(cmbTipo.getValue());
            u.setActivo(chkActivo.isSelected());

            String password = txtPassword.getText();
            if (password != null && !password.isBlank()) {
                u.setPassword(PasswordUtil.hashPassword(password));
            }

            if (esNuevo) {
                u.setIntentosRecuperacion(0);
                u.setRecuperacionActiva(false);
                u.setCodigoRecuperacion(null);
                u.setFechaCodigo(null);
            }

            return u;
        });

        return dialogo.showAndWait();
    }

    /** @return mensaje de error, o {@code null} si todo es válido. */
    private String validar(boolean esNuevo, Usuario base, String nombre, String paterno,
            String correo, TipoUsuario tipo, String password, String confirmar) {

        if (nombre == null || nombre.isBlank()) {
            return "El nombre es obligatorio.";
        }
        if (paterno == null || paterno.isBlank()) {
            return "El apellido paterno es obligatorio.";
        }
        if (correo == null || correo.isBlank()) {
            return "El correo es obligatorio.";
        }
        if (!PATRON_CORREO.matcher(correo.trim()).matches()) {
            return "El formato del correo no es válido.";
        }
        if (correoDuplicado(correo.trim(), base)) {
            return "Ya existe un usuario registrado con ese correo.";
        }
        if (tipo == null) {
            return "Selecciona un rol para el usuario.";
        }

        boolean cambiaPassword = password != null && !password.isBlank();

        if (esNuevo && !cambiaPassword) {
            return "La contraseña es obligatoria para un usuario nuevo.";
        }
        if (cambiaPassword && password.length() < 8) {
            return "La contraseña debe tener al menos 8 caracteres.";
        }
        if (cambiaPassword && !password.equals(confirmar)) {
            return "Las contraseñas no coinciden.";
        }

        return null;
    }

    private boolean correoDuplicado(String correo, Usuario excluido) {
        for (Usuario u : datos) {
            if (u != excluido && correo.equalsIgnoreCase(u.getCorreo())) {
                return true;
            }
        }
        return false;
    }

    // ============================================================
    // ESTADÍSTICAS
    // ============================================================
    private void cargarEstadisticas() {
        long total = datos.size();
        long admins = contarPorRol(TipoUsuario.ADMINISTRADOR);
        long tecnicos = contarPorRol(TipoUsuario.TECNICO);
        long consulta = contarPorRol(TipoUsuario.CONSULTA);
        long inactivos = datos.stream().filter(u -> !Boolean.TRUE.equals(u.getActivo())).count();

        lblTotalUsuarios.setText(String.valueOf(total));
        lblAdministradores.setText(String.valueOf(admins));
        lblTecnicos.setText(String.valueOf(tecnicos));
        lblConsulta.setText(String.valueOf(consulta));
        lblInactivos.setText(String.valueOf(inactivos));
    }

    private long contarPorRol(TipoUsuario tipo) {
        return datos.stream().filter(u -> u.getTipoUsuario() == tipo).count();
    }

    /** Recalcula filtros, KPIs, contador y refresca las celdas de la tabla. */
    private void refrescar() {
        tablaUsuarios.refresh();
        aplicarFiltros();
        cargarEstadisticas();
        actualizarEstadoBotones();
    }

    // ============================================================
    // DATOS ESTÁTICOS (sustituir por la BD más adelante)
    // ============================================================
    private void cargarDatosEstaticos() {
        List<Usuario> demo = List.of(
                crear(1L, "Gerardo", "Espíndola", "Ramírez", "gerardo.espindola@utng.edu.mx",
                        TipoUsuario.ADMINISTRADOR, true, LocalDateTime.of(2025, 1, 15, 9, 30)),
                crear(2L, "Luis Ángel", "Ortega", "Mendoza", "luis.ortega@utng.edu.mx",
                        TipoUsuario.TECNICO, true, LocalDateTime.of(2025, 2, 3, 11, 5)),
                crear(3L, "Karla", "Núñez", "Salinas", "karla.nunez@utng.edu.mx",
                        TipoUsuario.TECNICO, true, LocalDateTime.of(2025, 2, 18, 8, 45)),
                crear(4L, "Diego", "Salas", "Ibarra", "diego.salas@utng.edu.mx",
                        TipoUsuario.TECNICO, true, LocalDateTime.of(2025, 3, 2, 16, 20)),
                crear(5L, "Ana Sofía", "Ramírez", "Cortés", "ana.ramirez@utng.edu.mx",
                        TipoUsuario.CONSULTA, true, LocalDateTime.of(2025, 3, 22, 10, 0)),
                crear(6L, "Jorge", "Medina", "Aguilar", "jorge.medina@utng.edu.mx",
                        TipoUsuario.CONSULTA, true, LocalDateTime.of(2025, 4, 9, 13, 15)),
                crear(7L, "Mariana", "Beltrán", "Ochoa", "mariana.beltran@utng.edu.mx",
                        TipoUsuario.ADMINISTRADOR, true, LocalDateTime.of(2025, 5, 6, 9, 0)),
                crear(8L, "Ricardo", "Vega", null, "ricardo.vega@utng.edu.mx",
                        TipoUsuario.TECNICO, false, LocalDateTime.of(2025, 5, 27, 15, 40)),
                crear(9L, "Paola", "Hernández", "Luna", "paola.hernandez@utng.edu.mx",
                        TipoUsuario.CONSULTA, true, LocalDateTime.of(2025, 6, 11, 12, 10)),
                crear(10L, "Emilio", "Cárdenas", "Rojas", "emilio.cardenas@utng.edu.mx",
                        TipoUsuario.CONSULTA, false, LocalDateTime.of(2025, 7, 1, 17, 55)),
                crear(11L, "Fernanda", "Zamora", "Ríos", "fernanda.zamora@utng.edu.mx",
                        TipoUsuario.TECNICO, true, LocalDateTime.of(2025, 8, 19, 8, 5)),
                crear(12L, "Hugo", "Peralta", "Ávila", "hugo.peralta@utng.edu.mx",
                        TipoUsuario.CONSULTA, true, LocalDateTime.of(2026, 1, 14, 14, 30)));

        datos.setAll(demo);
    }

    private Usuario crear(Long id, String nombre, String paterno, String materno, String correo,
            TipoUsuario tipo, boolean activo, LocalDateTime alta) {

        Usuario u = new Usuario();
        u.setIdUsuario(id);
        u.setNombreCompleto(nombre);
        u.setApellidoPaterno(paterno);
        u.setApellidoMaterno(materno);
        u.setCorreo(correo);
        u.setPassword(HASH_DEMO);
        u.setTipoUsuario(tipo);
        u.setIntentosRecuperacion(0);
        u.setRecuperacionActiva(false);
        u.setFechaCodigo(null);
        u.setCodigoRecuperacion(null);
        u.setFechaCreacion(Timestamp.valueOf(alta));
        u.setActivo(activo);
        return u;
    }

    private Long siguienteId() {
        long max = datos.stream()
                .filter(u -> u.getIdUsuario() != null)
                .mapToLong(Usuario::getIdUsuario)
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
        Usuario sel = tablaUsuarios.getSelectionModel().getSelectedItem();
        boolean hay = (sel != null);
        boolean activo = hay && Boolean.TRUE.equals(sel.getActivo());

        btnVer.setDisable(!hay);
        btnEditar.setDisable(!hay);
        btnEliminar.setDisable(!hay || !activo);
        btnReactivar.setDisable(!hay || activo);
    }

    private Usuario seleccionOAviso() {
        Usuario sel = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (sel == null) {
            info("Sin selección", "Selecciona primero un usuario de la tabla.");
        }
        return sel;
    }

    private TextField campo(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setPrefWidth(260);
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

    private String estiloBadgeRol(String rol) {
        String base = "-fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 20;"
                + "-fx-padding: 4 11 4 11; ";
        if ("Administrador".equalsIgnoreCase(rol)) {
            return base + "-fx-background-color: #ede9fe; -fx-text-fill: #6d28d9;";
        }
        if ("Técnico".equalsIgnoreCase(rol)) {
            return base + "-fx-background-color: #dbeafe; -fx-text-fill: #1d4ed8;";
        }
        return base + "-fx-background-color: #ccfbf1; -fx-text-fill: #0f766e;";
    }

    private String estiloBadgeEstado(String estado) {
        String base = "-fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 20;"
                + "-fx-padding: 4 11 4 11; ";
        return "Activo".equalsIgnoreCase(estado)
                ? base + "-fx-background-color: #dcfce7; -fx-text-fill: #15803d;"
                : base + "-fx-background-color: #f1f5f9; -fx-text-fill: #64748b;";
    }

    /** Asocia el diálogo a la ventana principal para que se comporte como modal. */
    private void prepararVentana(Dialog<?> dialogo) {
        if (tablaUsuarios.getScene() != null && tablaUsuarios.getScene().getWindow() != null) {
            dialogo.initOwner(tablaUsuarios.getScene().getWindow());
            dialogo.initModality(Modality.WINDOW_MODAL);
        }
    }

    private boolean confirmar(String titulo, String encabezado, String detalle) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(encabezado);
        alerta.setContentText(detalle);
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
    private static String texto(String valor) {
        return valor == null ? "" : valor;
    }

    private static boolean contiene(String valor, String busqueda) {
        return valor != null && valor.toLowerCase().contains(busqueda);
    }

    private String nombreCompleto(Usuario u) {
        return (texto(u.getNombreCompleto()) + " "
                + texto(u.getApellidoPaterno()) + " "
                + texto(u.getApellidoMaterno())).trim().replaceAll("\\s+", " ");
    }

    private static String formatear(Timestamp fecha, DateTimeFormatter formato) {
        return fecha == null ? "—" : fecha.toLocalDateTime().format(formato);
    }

    private static String etiquetaRol(TipoUsuario tipo) {
        if (tipo == null) {
            return "Sin rol";
        }
        return switch (tipo) {
            case ADMINISTRADOR -> "Administrador";
            case TECNICO -> "Técnico";
            case CONSULTA -> "Consulta";
        };
    }

    private static TipoUsuario tipoDesdeEtiqueta(String etiqueta) {
        if (etiqueta == null) {
            return null;
        }
        return switch (etiqueta.trim().toLowerCase()) {
            case "administrador" -> TipoUsuario.ADMINISTRADOR;
            case "técnico", "tecnico" -> TipoUsuario.TECNICO;
            case "consulta" -> TipoUsuario.CONSULTA;
            default -> null;
        };
    }
}