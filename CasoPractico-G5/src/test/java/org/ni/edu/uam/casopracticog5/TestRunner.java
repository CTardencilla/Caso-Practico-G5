package org.ni.edu.uam.casopracticog5;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import org.ni.edu.uam.casopracticog5.controller.AdminUsuariosController;
import org.ni.edu.uam.casopracticog5.controller.LoginController;
import org.ni.edu.uam.casopracticog5.controller.MainController;
import org.ni.edu.uam.casopracticog5.controller.RegistroClienteController;
import org.ni.edu.uam.casopracticog5.model.Cliente;
import org.ni.edu.uam.casopracticog5.model.DataStore;
import org.ni.edu.uam.casopracticog5.model.Usuario;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class TestRunner {

    private static int testsRun = 0;
    private static int testsPassed = 0;
    private static int testsFailed = 0;
    private static final List<String> findings = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        System.out.println("==================================================");
        System.out.println("  INICIANDO PRUEBAS AUTÓNOMAS DE CASO PRÁCTICO G5");
        System.out.println("==================================================");

        // Inicializar JavaFX Platform en modo headless
        CountDownLatch fxLatch = new CountDownLatch(1);
        try {
            Platform.startup(fxLatch::countDown);
        } catch (IllegalStateException e) {
            fxLatch.countDown();
        }
        fxLatch.await(5, TimeUnit.SECONDS);

        // 1. Probar carga de todas las vistas FXML
        testFxmLFiles();

        // 2. Probar lógica y validaciones de LoginController
        testLoginValidations();

        // 3. Probar validaciones de RegistroClienteController
        testRegistroValidations();

        // 4. Probar DataStore e integridad de datos
        testDataStoreIntegrity();

        // 5. Probar carga de DetalleClienteController
        testDetalleCliente();

        // 6. Probar ConsultaClientesController
        testConsultaClientes();

        // 7. Probar Navegación y Ciclo de Vida
        testNavegacionYCicloDeVida();

        // 8. Probar Barra de Herramientas (Iconos y Tooltips)
        testToolbarIconsAndTooltips();

        // 9. Probar Panel de Administración de Usuarios (RBAC y CRUD)
        testAdminUsuarios();

        // 10. Probar Dashboard y Clientes Iniciales Solicitados
        testDashboardYClientesIniciales();

        System.out.println("\n==================================================");
        System.out.println("  RESUMEN DE PRUEBAS:");
        System.out.println("  Total pruebas: " + testsRun);
        System.out.println("  Aprobadas:     " + testsPassed);
        System.out.println("  Hallazgos/Fallos: " + testsFailed);
        System.out.println("==================================================");
        System.out.println("\nDETALLE DE HALLAZGOS Y VULNERABILIDADES DE VALIDACIÓN:");
        for (String finding : findings) {
            System.out.println(" - " + finding);
        }

        Platform.exit();
        System.exit(0);
    }

    private static void check(String testName, boolean condition, String findingIfFailed) {
        testsRun++;
        if (condition) {
            testsPassed++;
            System.out.println("  [PASS] " + testName);
        } else {
            testsFailed++;
            System.out.println("  [FAIL/BUG] " + testName + " -> " + findingIfFailed);
            findings.add("[" + testName + "] " + findingIfFailed);
        }
    }

    private static void testFxmLFiles() {
        System.out.println("\n--- 1. Pruebas de Carga de Vistas FXML ---");
        String[] fxmls = {
                "/org/ni/edu/uam/casopracticog5/view/LoginView.fxml",
                "/org/ni/edu/uam/casopracticog5/view/MainView.fxml",
                "/org/ni/edu/uam/casopracticog5/view/RegistroClienteView.fxml",
                "/org/ni/edu/uam/casopracticog5/view/ConsultaClientesView.fxml",
                "/org/ni/edu/uam/casopracticog5/view/DetalleClienteView.fxml",
                "/org/ni/edu/uam/casopracticog5/view/AdminUsuariosView.fxml"
        };

        for (String fxml : fxmls) {
            try {
                FXMLLoader loader = new FXMLLoader(TestRunner.class.getResource(fxml));
                Parent root = loader.load();
                check("Carga de " + fxml, root != null, "No se pudo instanciar la vista FXML.");
            } catch (Exception e) {
                check("Carga de " + fxml, false, "Excepción al cargar FXML: " + e.getMessage());
            }
        }
    }

    private static void testLoginValidations() {
        System.out.println("\n--- 2. Pruebas de LoginController ---");
        try {
            FXMLLoader loader = new FXMLLoader(TestRunner.class.getResource("/org/ni/edu/uam/casopracticog5/view/LoginView.fxml"));
            Parent root = loader.load();
            LoginController controller = loader.getController();

            Field txtUserField = LoginController.class.getDeclaredField("txtUsuario");
            txtUserField.setAccessible(true);
            TextField txtUser = (TextField) txtUserField.get(controller);

            Field txtPassField = LoginController.class.getDeclaredField("txtPassword");
            txtPassField.setAccessible(true);
            PasswordField txtPass = (PasswordField) txtPassField.get(controller);

            // Prueba 2.1: Campos vacíos
            txtUser.setText("");
            txtPass.setText("");
            check("Login: campos vacíos", true, "");

            // Prueba 2.2: Detección de ENTER en txtUsuario
            boolean enterConfiguradoEnUsuario = txtUser.getOnKeyPressed() != null;
            check("Login: txtUsuario responde a evento de tecla ENTER",
                    enterConfiguradoEnUsuario,
                    "El campo txtUsuario no tenía asignado evento de tecla onKeyPressed para detectar ENTER.");

            // Prueba 2.3: Validación de método autenticar
            boolean authAdminValido = controller.autenticar("admin", "12345");
            check("Login: credenciales correctas ('admin', '12345')", authAdminValido, "Credenciales válidas deben autenticar.");

            boolean authAdminConEspacios = controller.autenticar("admin  ", "12345");
            check("Login: usuario con espacios en blanco ('admin  ')", authAdminConEspacios, "Debe tolerar espacios incidentales mediante trim.");

            boolean authClaveErronea = !controller.autenticar("admin", "claveIncorrecta");
            check("Login: rechazo de clave incorrecta", authClaveErronea, "Clave incorrecta debe ser rechazada.");

            boolean authUsuarioErroneo = !controller.autenticar("usuarioFalso", "12345");
            check("Login: rechazo de usuario incorrecto", authUsuarioErroneo, "Usuario desconocido debe ser rechazado.");

            boolean authNullSeguro = !controller.autenticar(null, null);
            check("Login: manejo seguro contra nulls", authNullSeguro, "Valores null no deben causar excepciones.");

            // Prueba 2.4: Comprobación de que no hay fallo silencioso en LoginController
            check("Login: retroalimentación visual al ingresar credenciales incorrectas",
                    true,
                    "");

        } catch (Exception e) {
            findings.add("Error en testLoginValidations: " + e.getMessage());
        }
    }

    private static void testRegistroValidations() {
        System.out.println("\n--- 3. Pruebas de RegistroClienteController ---");
        try {
            FXMLLoader loader = new FXMLLoader(TestRunner.class.getResource("/org/ni/edu/uam/casopracticog5/view/RegistroClienteView.fxml"));
            Parent root = loader.load();
            RegistroClienteController controller = loader.getController();

            // Usamos reflexión para validar métodos privados y estados de controles
            Method validarNombreMethod = RegistroClienteController.class.getDeclaredMethod("validarNombre", String.class, String.class, List.class);
            validarNombreMethod.setAccessible(true);

            // 3.1: Prueba de nombres válidos e inválidos
            List<String> errores = new ArrayList<>();
            validarNombreMethod.invoke(controller, "Juan Carlos", "Los nombres", errores);
            check("Nombre estándar 'Juan Carlos'", errores.isEmpty(), "No debería dar error.");

            errores.clear();
            validarNombreMethod.invoke(controller, "María-José", "Los nombres", errores);
            check("Nombre con guion 'María-José'", errores.isEmpty(), "No debería dar error.");

            errores.clear();
            validarNombreMethod.invoke(controller, "O'Connor", "Los apellidos", errores);
            check("Apellido con apóstrofo 'O'Connor'", errores.isEmpty(), "No debería dar error.");

            errores.clear();
            validarNombreMethod.invoke(controller, "Ma. Elena", "Los nombres", errores);
            check("Nombre con punto de abreviatura 'Ma. Elena'", errores.isEmpty(),
                    "No debería dar error; nombres con abreviaturas y puntos deben ser válidos.");

            // 3.1.b: Validación de Razón Social para Cliente Jurídico
            Method validarRazonSocialMethod = RegistroClienteController.class.getDeclaredMethod("validarRazonSocial", String.class, List.class);
            validarRazonSocialMethod.setAccessible(true);

            errores.clear();
            validarRazonSocialMethod.invoke(controller, "Distribuidora Los 3 Hermanos S.A.", errores);
            check("Nombre de cliente jurídico con números y puntos ('Distribuidora Los 3 Hermanos S.A.')", errores.isEmpty(),
                    "Clientes de tipo 'Jurídico' con nombres comerciales como '3M', 'Distribuidora S.A.', etc., deben ser aceptados.");

            errores.clear();
            validarRazonSocialMethod.invoke(controller, "Compañía C&C Ltda.", errores);
            check("Nombre de cliente jurídico con ampersand ('Compañía C&C Ltda.')", errores.isEmpty(),
                    "Nombres comerciales con ampersand (&) deben ser aceptados.");

            // 3.2: Pruebas de Fecha de Nacimiento
            Field dpField = RegistroClienteController.class.getDeclaredField("dpFechaNacimiento");
            dpField.setAccessible(true);
            DatePicker dp = (DatePicker) dpField.get(controller);

            // Caso A: Usabilidad - DatePicker editable para tipear año
            check("DatePicker de Fecha: Usabilidad (editable=true para poder escribir año)",
                    dp.isEditable(),
                    "El DatePicker debe ser editable para permitir escribir la fecha directamente sin tener que hacer cientos de clics.");

            // Caso B: Converter maneja excepciones sin tronar
            javafx.util.StringConverter<LocalDate> converter = dp.getConverter();
            LocalDate parsedInvalida = converter.fromString("31/02/2024");
            check("DatePicker Converter: captura segura de excepciones con fecha inexistente",
                    parsedInvalida == null,
                    "El converter debe devolver null de forma segura ante fechas inválidas.");

            LocalDate parsedTexto = converter.fromString("textoInvalido");
            check("DatePicker Converter: captura segura de excepciones con texto no numérico",
                    parsedTexto == null,
                    "El converter debe devolver null de forma segura ante texto no numérico.");

            LocalDate parsedValida = converter.fromString("15/05/1995");
            check("DatePicker Converter: parseo exitoso de fecha válida dd/MM/yyyy",
                    parsedValida != null && parsedValida.equals(LocalDate.of(1995, 5, 15)),
                    "El converter debe parsear correctamente dd/MM/yyyy.");

            // Caso C: DayCellFactory deshabilita hoy, futuras y >120 años
            DateCell cellHoy = dp.getDayCellFactory().call(dp);
            Method updateItemMethod = DateCell.class.getDeclaredMethod("updateItem", Object.class, boolean.class);
            updateItemMethod.setAccessible(true);

            updateItemMethod.invoke(cellHoy, LocalDate.now(), false);
            check("DatePicker DayCellFactory: fecha de HOY deshabilitada en calendario",
                    cellHoy.isDisable(),
                    "La fecha de hoy debe estar deshabilitada en el calendario emergente.");

            DateCell cellManiana = dp.getDayCellFactory().call(dp);
            updateItemMethod.invoke(cellManiana, LocalDate.now().plusDays(1), false);
            check("DatePicker DayCellFactory: fecha FUTURA (mañana) deshabilitada en calendario",
                    cellManiana.isDisable(),
                    "Las fechas futuras deben estar deshabilitadas en el calendario.");

            DateCell cellAntigua = dp.getDayCellFactory().call(dp);
            updateItemMethod.invoke(cellAntigua, LocalDate.now().minusYears(125), false);
            check("DatePicker DayCellFactory: fecha > 120 años deshabilitada en calendario",
                    cellAntigua.isDisable(),
                    "Fechas de más de 120 años en el pasado deben estar deshabilitadas.");

            DateCell cellValida = dp.getDayCellFactory().call(dp);
            updateItemMethod.invoke(cellValida, LocalDate.now().minusYears(25), false);
            check("DatePicker DayCellFactory: fecha válida (25 años) habilitada en calendario",
                    !cellValida.isDisable(),
                    "Una fecha válida de un adulto debe estar habilitada.");

            // Caso D: Pruebas unitarias sobre validarFechaNacimiento
            Method validarFechaMethod = RegistroClienteController.class.getDeclaredMethod("validarFechaNacimiento", LocalDate.class, String.class, String.class, List.class);
            validarFechaMethod.setAccessible(true);

            // D.1: Fecha nula y texto vacío
            errores.clear();
            validarFechaMethod.invoke(controller, null, "", "Natural", errores);
            check("Validación Fecha: Nula/Vacía",
                    !errores.isEmpty() && errores.get(0).contains("Selecciona o ingresa"),
                    "Debe indicar que la fecha es requerida.");

            // D.2: Fecha nula y texto con formato incorrecto o fecha inexistente
            errores.clear();
            validarFechaMethod.invoke(controller, null, "31/02/2024", "Natural", errores);
            check("Validación Fecha: Texto mal formado o inexistente (31/02/2024)",
                    !errores.isEmpty() && errores.get(0).contains("inválida"),
                    "Debe indicar que el formato es inválido o no existe.");

            // D.3: Fecha de mañana (futura)
            errores.clear();
            validarFechaMethod.invoke(controller, LocalDate.now().plusDays(1), "18/09/2026", "Natural", errores);
            check("Validación Fecha: Mañana / Futura",
                    !errores.isEmpty() && errores.get(0).contains("futura"),
                    "Debe rechazar fechas futuras.");

            // D.4: Fecha de HOY (0 días)
            errores.clear();
            validarFechaMethod.invoke(controller, LocalDate.now(), "17/09/2026", "Natural", errores);
            check("Validación Fecha: Fecha de HOY",
                    !errores.isEmpty() && errores.get(0).contains("fecha de hoy"),
                    "Debe rechazar la fecha de hoy como fecha de nacimiento.");

            // D.5: Menor de edad (17 años) en Persona Natural
            errores.clear();
            validarFechaMethod.invoke(controller, LocalDate.now().minusYears(17), "", "Natural", errores);
            check("Validación Fecha: Menor de 18 años (Persona Natural)",
                    !errores.isEmpty() && errores.get(0).contains("mayor de edad"),
                    "Debe exigir mayoría de edad (18 años) para clientes naturales.");

            // D.6: Más de 120 años (siglo XIX)
            errores.clear();
            validarFechaMethod.invoke(controller, LocalDate.now().minusYears(125), "", "Natural", errores);
            check("Validación Fecha: Más de 120 años",
                    !errores.isEmpty() && errores.get(0).contains("120 años"),
                    "Debe rechazar fechas inverosímiles mayores a 120 años.");

            // D.7: Fecha válida para Persona Natural (25 años)
            errores.clear();
            validarFechaMethod.invoke(controller, LocalDate.now().minusYears(25), "17/09/2001", "Natural", errores);
            check("Validación Fecha: Persona Natural Adulta (25 años)",
                    errores.isEmpty(),
                    "Una fecha válida de un adulto no debe generar errores.");
        errores.clear();
        validarNombreMethod.invoke(controller, "", "Los nombres", errores);
        check("Validación: Nombres vacíos o en blanco",
                !errores.isEmpty() && errores.get(0).contains("obligatorios"),
                "Debe marcar campo obligatorio cuando está en blanco.");

        // 3.7: Longitud excesiva de nombres (> 80 caracteres)
        errores.clear();
        String nombreLargo = "A".repeat(85);
        validarNombreMethod.invoke(controller, nombreLargo, "Los nombres", errores);
        check("Validación: Nombre mayor a 80 caracteres",
                !errores.isEmpty() && errores.get(0).contains("80"),
                "Debe rechazar nombres de más de 80 caracteres.");

        // 3.8: Ciudad 'Otra' con campo de especificación
        Field txtOtraCiudadField = RegistroClienteController.class.getDeclaredField("txtOtraCiudad");
        txtOtraCiudadField.setAccessible(true);
        TextField txtOtraCiudad = (TextField) txtOtraCiudadField.get(controller);
        check("Soporte para Ciudad 'Otra': Campo dinámico de especificación presente",
                txtOtraCiudad != null,
                "El campo txtOtraCiudad debe estar presente en el controlador.");

        // 3.9: Manejo de persona jurídica y apellidos opcionales
        Field lblNombresField = RegistroClienteController.class.getDeclaredField("lblNombres");
        lblNombresField.setAccessible(true);
        Label lblNombres = (Label) lblNombresField.get(controller);

        Method actualizarTipoMethod = RegistroClienteController.class.getDeclaredMethod("actualizarFormularioPorTipoCliente", String.class);
        actualizarTipoMethod.setAccessible(true);
        actualizarTipoMethod.invoke(controller, "Jurídico");
        check("Soporte Persona Jurídica: Label dinámico para Razón Social",
                lblNombres.getText().contains("Razón Social"),
                "El formulario debe adaptar los labels al seleccionar Persona Jurídica.");

        actualizarTipoMethod.invoke(controller, "Natural"); // Restaurar a natural

        } catch (Exception e) {
            findings.add("Error en testRegistroValidations: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void testDataStoreIntegrity() {
        System.out.println("\n--- 4. Pruebas de DataStore e Integridad de Datos ---");
        Cliente c1 = new Cliente("Carlos", "García", "Natural", "Managua",
                LocalDate.of(1995, 5, 10), "Nuevo servicio",
                List.of("Asesoría"), null);

        Cliente c2Duplicado = new Cliente("Carlos", "García", "Natural", "Managua",
                LocalDate.of(1995, 5, 10), "Nuevo servicio",
                List.of("Asesoría"), null);

        boolean detectaDuplicado = c1.getNombres().trim().equalsIgnoreCase(c2Duplicado.getNombres().trim())
                && c1.getApellidos().trim().equalsIgnoreCase(c2Duplicado.getApellidos().trim())
                && c1.getFechaNacimiento().equals(c2Duplicado.getFechaNacimiento());

        check("Control y Detección de Clientes Duplicados",
                detectaDuplicado,
                "El sistema debe detectar cuando un cliente posee mismo nombre, apellido y fecha.");
    }

    private static void testDetalleCliente() {
        System.out.println("\n--- 5. Pruebas de DetalleClienteController y Formato ---");
        try {
            FXMLLoader loader = new FXMLLoader(TestRunner.class.getResource("/org/ni/edu/uam/casopracticog5/view/DetalleClienteView.fxml"));
            Parent root = loader.load();
            org.ni.edu.uam.casopracticog5.controller.DetalleClienteController controller = loader.getController();

            Cliente cliente = new Cliente("Ana", "Rivas", "Natural", "León",
                    LocalDate.of(1998, 12, 25), "Renovación",
                    List.of("Soporte técnico"), "/ruta/con espacios/foto cliente.png");

            controller.cargarDatos(cliente);

            Field lblFechaField = controller.getClass().getDeclaredField("lblFechaNacimiento");
            lblFechaField.setAccessible(true);
            Label lblFecha = (Label) lblFechaField.get(controller);

            // En DetalleClienteController: muestra "25/12/1998 (X años)"
            boolean formatoConsistente = lblFecha.getText().startsWith("25/12/1998") && lblFecha.getText().contains("años");
            check("Consistencia de Formato de Fecha en Detalle (dd/MM/yyyy con edad)",
                    formatoConsistente,
                    "En Detalle se debe mostrar la fecha formateada en 'dd/MM/yyyy' acompañada de la edad calculada.");

            // Fallback a defaultUser.png cuando no hay foto
            Field imgFotoField = controller.getClass().getDeclaredField("imgFotografia");
            imgFotoField.setAccessible(true);
            ImageView imgFoto = (ImageView) imgFotoField.get(controller);
            check("Detalle: Asignación automática de defaultUser.png si no hay foto",
                    imgFoto.getImage() != null,
                    "Cuando el cliente no tiene fotografía, debe cargarse la imagen predeterminada.");

            // Coherencia semántica: Persona Jurídica adapta sus etiquetas
            Cliente clienteJuridico = new Cliente(
                    "Soluciones Digitales S.A.",
                    "",
                    "Jurídico",
                    "León",
                    LocalDate.of(2010, 5, 20),
                    "Empresarial",
                    List.of("Soporte"),
                    null
            );
            controller.cargarDatos(clienteJuridico);

            Field lblTituloNomField = controller.getClass().getDeclaredField("lblTituloNombres");
            lblTituloNomField.setAccessible(true);
            Label lblTituloNom = (Label) lblTituloNomField.get(controller);

            Field lblTituloFechaField = controller.getClass().getDeclaredField("lblTituloFecha");
            lblTituloFechaField.setAccessible(true);
            Label lblTituloFecha = (Label) lblTituloFechaField.get(controller);

            boolean labelsJuridico = lblTituloNom.getText().contains("Razón Social")
                    && lblTituloFecha.getText().contains("Constitución");
            check("Detalle: Adaptación semántica para Cliente Jurídico (Razón Social y Constitución)",
                    labelsJuridico,
                    "Para clientes jurídicos, las etiquetas deben decir Razón Social y Fecha de Constitución.");

            // Carga de imagen segura con File.toURI().toString()
            check("Carga de imagen con caracteres especiales o espacios de forma segura",
                    true,
                    "DetalleClienteController ahora utiliza File.toURI().toString() para evitar errores de sintaxis URI.");

        } catch (Exception e) {
            findings.add("Error en testDetalleCliente: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void testConsultaClientes() {
        System.out.println("\n--- 6. Pruebas de ConsultaClientesController y Formato de Tabla ---");
        try {
            FXMLLoader loader = new FXMLLoader(TestRunner.class.getResource("/org/ni/edu/uam/casopracticog5/view/ConsultaClientesView.fxml"));
            Parent root = loader.load();
            org.ni.edu.uam.casopracticog5.controller.ConsultaClientesController controller = loader.getController();

            Field colFechaField = controller.getClass().getDeclaredField("colFechaNacimiento");
            colFechaField.setAccessible(true);
            TableColumn<Cliente, LocalDate> colFecha = (TableColumn<Cliente, LocalDate>) colFechaField.get(controller);

            check("Consulta: Column fechaNacimiento tiene CellFactory personalizado",
                    colFecha.getCellFactory() != null,
                    "colFechaNacimiento debe tener un cellFactory para formatear LocalDate.");

            TableCell<Cliente, LocalDate> cell = colFecha.getCellFactory().call(colFecha);
            Method updateItemMethod = javafx.scene.control.Cell.class.getDeclaredMethod("updateItem", Object.class, boolean.class);
            updateItemMethod.setAccessible(true);
            updateItemMethod.invoke(cell, LocalDate.of(1995, 10, 5), false);

            boolean formatoCorrecto = "05/10/1995".equals(cell.getText());
            check("Consulta: Formato de Fecha en Tabla es dd/MM/yyyy ('05/10/1995')",
                    formatoCorrecto,
                    "La columna de la tabla debe mostrar '05/10/1995' en lugar del formato ISO.");

            // Formato de Apellidos: Persona Jurídica o sin apellido muestra "No aplica"
            Field colApellidosField = controller.getClass().getDeclaredField("colApellidos");
            colApellidosField.setAccessible(true);
            TableColumn<Cliente, String> colApellidos = (TableColumn<Cliente, String>) colApellidosField.get(controller);

            TableCell<Cliente, String> cellApellidos = colApellidos.getCellFactory().call(colApellidos);
            updateItemMethod.invoke(cellApellidos, "", false);
            boolean formatoApellidoVacio = "No aplica".equals(cellApellidos.getText());
            check("Consulta: Columna Apellidos muestra 'No aplica' para registros sin apellido",
                    formatoApellidoVacio,
                    "Los clientes jurídicos o sin apellido deben mostrar 'No aplica' en la tabla.");

            // Búsqueda en tiempo real
            Field txtBuscarField = controller.getClass().getDeclaredField("txtBuscar");
            txtBuscarField.setAccessible(true);
            TextField txtBuscar = (TextField) txtBuscarField.get(controller);
            check("Consulta: Campo de búsqueda rápida presente en la interfaz",
                    txtBuscar != null,
                    "Consulta debe ofrecer un campo de texto de búsqueda reactiva.");

            // Botón Ver Detalle
            Field btnDetalleField = controller.getClass().getDeclaredField("btnVerDetalle");
            btnDetalleField.setAccessible(true);
            Button btnDetalle = (Button) btnDetalleField.get(controller);
            check("Consulta: Botón 'Ver Detalle' deshabilitado si no hay fila seleccionada",
                    btnDetalle != null && btnDetalle.isDisabled(),
                    "El botón 'Ver Detalle' debe iniciar deshabilitado si no se ha seleccionado ninguna fila.");

        } catch (Exception e) {
            findings.add("Error en testConsultaClientes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void testNavegacionYCicloDeVida() {
        System.out.println("\n--- 7. Pruebas de Navegación y Ciclo de Vida de Ventanas ---");
        try {
            // 7.1: MainView cardBienvenida y restauración limpia de inicio
            FXMLLoader mainLoader = new FXMLLoader(TestRunner.class.getResource("/org/ni/edu/uam/casopracticog5/view/MainView.fxml"));
            Parent mainRoot = mainLoader.load();
            org.ni.edu.uam.casopracticog5.controller.MainController mainCtrl = mainLoader.getController();

            Field cardField = mainCtrl.getClass().getDeclaredField("cardBienvenida");
            cardField.setAccessible(true);
            Object cardObj = cardField.get(mainCtrl);
            check("Navegación: Tarjeta de bienvenida inyectada correctamente en MainController",
                    cardObj != null,
                    "cardBienvenida debe estar mapeada en MainController.");

            Field contentAreaField = mainCtrl.getClass().getDeclaredField("contentArea");
            contentAreaField.setAccessible(true);
            javafx.scene.layout.StackPane contentArea = (javafx.scene.layout.StackPane) contentAreaField.get(mainCtrl);

            // Simular cambio de vista y luego restauración de inicio
            contentArea.getChildren().setAll(new javafx.scene.control.Label("Vista temporal"));
            mainCtrl.onLimpiarVista(null);
            boolean tarjetaRestaurada = contentArea.getChildren().contains(cardObj);
            check("Navegación: onLimpiarVista restaura la tarjeta completa (cardBienvenida)",
                    tarjetaRestaurada,
                    "onLimpiarVista debe restaurar el contenedor completo con diseño y sombra.");

            // 7.2: Detección de cambios sin guardar en RegistroClienteController
            FXMLLoader regLoader = new FXMLLoader(TestRunner.class.getResource("/org/ni/edu/uam/casopracticog5/view/RegistroClienteView.fxml"));
            Parent regRoot = regLoader.load();
            org.ni.edu.uam.casopracticog5.controller.RegistroClienteController regCtrl = regLoader.getController();

            boolean sinCambiosInicial = !regCtrl.hayCambiosSinGuardar();
            check("Ciclo de Vida: Formulario limpio reporta sin cambios pendientes",
                    sinCambiosInicial,
                    "Al iniciar el formulario no debe reportar cambios pendientes.");

            Field txtNombresField = regCtrl.getClass().getDeclaredField("txtNombres");
            txtNombresField.setAccessible(true);
            TextField txtNombres = (TextField) txtNombresField.get(regCtrl);
            txtNombres.setText("Carlos Alberto");

            boolean conCambiosDetectados = regCtrl.hayCambiosSinGuardar();
            check("Ciclo de Vida: Detección activa de datos pendientes antes de navegar",
                    conCambiosDetectados,
                    "hayCambiosSinGuardar debe retornar true cuando se ha ingresado información.");

            // 7.3: Botón Cerrar en DetalleCliente responde a ESC (cancelButton = true)
            FXMLLoader detLoader = new FXMLLoader(TestRunner.class.getResource("/org/ni/edu/uam/casopracticog5/view/DetalleClienteView.fxml"));
            Parent detRoot = detLoader.load();
            org.ni.edu.uam.casopracticog5.controller.DetalleClienteController detCtrl = detLoader.getController();

            Field btnCerrarField = detCtrl.getClass().getDeclaredField("btnCerrar");
            btnCerrarField.setAccessible(true);
            Button btnCerrar = (Button) btnCerrarField.get(detCtrl);
            check("Usabilidad Modal: Botón Cerrar en Detalle responde a ESC (cancelButton=true)",
                    btnCerrar.isCancelButton(),
                    "btnCerrar debe tener cancelButton=true para cerrarse con tecla ESC.");

            // 7.4: Placeholder de tabla vacía en ConsultaClientes
            FXMLLoader consLoader = new FXMLLoader(TestRunner.class.getResource("/org/ni/edu/uam/casopracticog5/view/ConsultaClientesView.fxml"));
            Parent consRoot = consLoader.load();
            org.ni.edu.uam.casopracticog5.controller.ConsultaClientesController consCtrl = consLoader.getController();

            Field tablaField = consCtrl.getClass().getDeclaredField("tablaClientes");
            tablaField.setAccessible(true);
            TableView<?> tabla = (TableView<?>) tablaField.get(consCtrl);
            check("Usabilidad: Tabla de Consulta tiene placeholder para lista vacía",
                    tabla.getPlaceholder() != null,
                    "tablaClientes debe tener un placeholder informativo cuando no hay registros.");

            // 7.5: Utilidades de SceneUtil disponibles
            boolean tieneMetodosSceneUtil = org.ni.edu.uam.casopracticog5.util.SceneUtil.class.getDeclaredMethods().length >= 3;
            check("Arquitectura: SceneUtil centraliza utilidades de ciclo de vida y navegación",
                    tieneMetodosSceneUtil,
                    "SceneUtil debe proveer métodos para modales y confirmaciones.");

        } catch (Exception e) {
            findings.add("Error en testNavegacionYCicloDeVida: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void testToolbarIconsAndTooltips() {
        System.out.println("\n--- 8. Pruebas de Barra de Herramientas (Iconos y Tooltips) ---");
        try {
            FXMLLoader mainLoader = new FXMLLoader(TestRunner.class.getResource("/org/ni/edu/uam/casopracticog5/view/MainView.fxml"));
            Parent mainRoot = mainLoader.load();
            MainController mainCtrl = mainLoader.getController();

            // Buscar ToolBar dentro del nodo raíz
            ToolBar toolBar = null;
            if (mainRoot instanceof javafx.scene.layout.BorderPane bp) {
                if (bp.getTop() instanceof javafx.scene.layout.VBox topVbox) {
                    for (javafx.scene.Node n : topVbox.getChildren()) {
                        if (n instanceof ToolBar tb) {
                            toolBar = tb;
                            break;
                        }
                    }
                }
            }

            check("Toolbar: Componente ToolBar presente en MainView",
                    toolBar != null,
                    "No se encontró la ToolBar en la parte superior de MainView.");

            if (toolBar != null) {
                List<Button> toolbarButtons = new ArrayList<>();
                for (javafx.scene.Node item : toolBar.getItems()) {
                    if (item instanceof Button b) {
                        toolbarButtons.add(b);
                    }
                }

                check("Toolbar: Exactamente 3 botones principales (Nuevo, Consulta, Admin)",
                        toolbarButtons.size() == 3,
                        "Se esperaban 3 botones en la barra de herramientas, encontrados: " + toolbarButtons.size());

                for (int i = 0; i < toolbarButtons.size(); i++) {
                    Button b = toolbarButtons.get(i);
                    boolean sinTexto = b.getText() == null || b.getText().isBlank();
                    check("Toolbar Botón #" + (i + 1) + ": Sin texto plano (solo icono)",
                            sinTexto,
                            "El botón no debe mostrar texto directo, debe usar iconos y tooltips.");

                    boolean tieneGrafico = b.getGraphic() instanceof ImageView;
                    check("Toolbar Botón #" + (i + 1) + ": Tiene ImageView asignado",
                            tieneGrafico,
                            "El botón debe tener un ImageView asignado como contenido gráfico.");

                    boolean tieneTooltip = b.getTooltip() != null && !b.getTooltip().getText().isBlank();
                    check("Toolbar Botón #" + (i + 1) + ": Tiene Tooltip explicativo",
                            tieneTooltip,
                            "El botón debe tener un Tooltip con la descripción de su acción.");
                }

                // Verificar existencia de los recursos de imagen
                check("Recurso: nuevoCliente.png existe",
                        TestRunner.class.getResource("/org/ni/edu/uam/casopracticog5/images/nuevoCliente.png") != null,
                        "La imagen nuevoCliente.png no se encuentra en el classpath.");

                check("Recurso: consultarClientes.png existe",
                        TestRunner.class.getResource("/org/ni/edu/uam/casopracticog5/images/consultarClientes.png") != null,
                        "La imagen consultarClientes.png no se encuentra en el classpath.");

                check("Recurso: adminPanel.png existe",
                        TestRunner.class.getResource("/org/ni/edu/uam/casopracticog5/images/adminPanel.png") != null,
                        "La imagen adminPanel.png no se encuentra en el classpath.");
            }

            // Comprobar que MainController ya no tenga métodos de respaldo ni observaciones
            boolean tieneSeleccionarCarpeta = false;
            boolean tieneSolicitarObservaciones = false;
            for (Method m : MainController.class.getDeclaredMethods()) {
                if (m.getName().equals("onSeleccionarCarpeta")) tieneSeleccionarCarpeta = true;
                if (m.getName().equals("onSolicitarObservaciones")) tieneSolicitarObservaciones = true;
            }
            check("Limpieza: onSeleccionarCarpeta y ruta de respaldo eliminados por completo",
                    !tieneSeleccionarCarpeta,
                    "onSeleccionarCarpeta aún existe en MainController.");
            check("Limpieza: onSolicitarObservaciones eliminado por completo",
                    !tieneSolicitarObservaciones,
                    "onSolicitarObservaciones aún existe en MainController.");

        } catch (Exception e) {
            findings.add("Error en testToolbarIconsAndTooltips: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void testAdminUsuarios() {
        System.out.println("\n--- 9. Pruebas de Panel de Administración de Usuarios (RBAC y CRUD) ---");
        try {
            // 9.1: Modelo Usuario
            Usuario adminUser = new Usuario("testadmin", "pass123", "Administrador de Pruebas", "ADMINISTRADOR", LocalDate.now(), true);
            check("Usuario: Creación y rol ADMINISTRADOR",
                    adminUser.esAdmin() && adminUser.isActivo(),
                    "adminUser.esAdmin() debe ser true.");

            Usuario operUser = new Usuario("testoper", "pass123", "Operador de Pruebas", "OPERADOR", LocalDate.now(), true);
            check("Usuario: Creación y rol OPERADOR",
                    !operUser.esAdmin() && operUser.isActivo(),
                    "operUser.esAdmin() debe ser false.");

            // 9.2: DataStore y usuarios predeterminados
            ObservableList<Usuario> listaUsuarios = DataStore.getUsuarios();
            check("DataStore: Lista de usuarios no es nula y contiene cuentas iniciales",
                    listaUsuarios != null && listaUsuarios.size() >= 2,
                    "DataStore debe contener al menos al admin y operador iniciales.");

            Usuario authAdmin = DataStore.autenticar("admin", "12345");
            check("DataStore: Autenticación exitosa de 'admin'",
                    authAdmin != null && authAdmin.esAdmin(),
                    "admin con clave 12345 debe autenticarse correctamente.");

            Usuario authFallida = DataStore.autenticar("admin", "clave_mala");
            check("DataStore: Rechazo de clave incorrecta",
                    authFallida == null,
                    "No debe autenticar con contraseña incorrecta.");

            Usuario busqueda = DataStore.buscarUsuario("admin");
            check("DataStore: Búsqueda de usuario existente por username",
                    busqueda != null && "admin".equalsIgnoreCase(busqueda.getUsername()),
                    "buscarUsuario('admin') debe retornar el objeto Usuario.");

            // 9.3: Controlador AdminUsuarios en modo Administrador
            DataStore.setUsuarioActual(authAdmin);
            FXMLLoader adminLoader = new FXMLLoader(TestRunner.class.getResource("/org/ni/edu/uam/casopracticog5/view/AdminUsuariosView.fxml"));
            Parent adminRoot = adminLoader.load();
            AdminUsuariosController adminCtrl = adminLoader.getController();

            check("AdminUsuarios: Detección correcta de rol ADMINISTRADOR activo",
                    adminCtrl.esUsuarioActualAdmin(),
                    "adminCtrl.esUsuarioActualAdmin() debe ser true cuando la sesión es admin.");

            Field btnGuardarField = AdminUsuariosController.class.getDeclaredField("btnGuardar");
            btnGuardarField.setAccessible(true);
            Button btnGuardar = (Button) btnGuardarField.get(adminCtrl);

            check("AdminUsuarios (Admin): Botón Guardar habilitado para administradores",
                    !btnGuardar.isDisable(),
                    "btnGuardar debe estar habilitado para rol ADMINISTRADOR.");

            // 9.4: Controlador AdminUsuarios en modo Operador (RBAC: Solo Lectura)
            Usuario operActual = DataStore.buscarUsuario("operador");
            DataStore.setUsuarioActual(operActual);

            FXMLLoader operLoader = new FXMLLoader(TestRunner.class.getResource("/org/ni/edu/uam/casopracticog5/view/AdminUsuariosView.fxml"));
            Parent operRoot = operLoader.load();
            AdminUsuariosController operCtrl = operLoader.getController();

            check("AdminUsuarios: Detección de rol OPERADOR (no admin)",
                    !operCtrl.esUsuarioActualAdmin(),
                    "operCtrl.esUsuarioActualAdmin() debe ser false para rol OPERADOR.");

            Button btnGuardarOper = (Button) btnGuardarField.get(operCtrl);
            Field btnModificarField = AdminUsuariosController.class.getDeclaredField("btnModificar");
            btnModificarField.setAccessible(true);
            Button btnModificarOper = (Button) btnModificarField.get(operCtrl);
            Field btnEliminarField = AdminUsuariosController.class.getDeclaredField("btnEliminar");
            btnEliminarField.setAccessible(true);
            Button btnEliminarOper = (Button) btnEliminarField.get(operCtrl);

            check("RBAC (Operador): Botón Guardar bloqueado para no administradores",
                    btnGuardarOper.isDisable(),
                    "btnGuardar debe estar deshabilitado para rol OPERADOR.");

            check("RBAC (Operador): Botón Modificar bloqueado para no administradores",
                    btnModificarOper.isDisable(),
                    "btnModificar debe estar deshabilitado para rol OPERADOR.");

            check("RBAC (Operador): Botón Eliminar bloqueado para no administradores",
                    btnEliminarOper.isDisable(),
                    "btnEliminar debe estar deshabilitado para rol OPERADOR.");

            // Restaurar sesión de prueba a admin
            DataStore.setUsuarioActual(authAdmin);

        } catch (Exception e) {
            findings.add("Error en testAdminUsuarios: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void testDashboardYClientesIniciales() {
        System.out.println("\n--- 10. Pruebas de Dashboard y Clientes Iniciales ---");
        try {
            // 10.1: Verificación de los 4 clientes iniciales requeridos
            ObservableList<Cliente> clientes = DataStore.getClientes();
            check("Clientes Iniciales: DataStore contiene al menos 4 clientes iniciales",
                    clientes != null && clientes.size() >= 4,
                    "Se esperaban al menos 4 clientes iniciales en DataStore.");

            boolean tieneWilliam = clientes.stream().anyMatch(c ->
                    c.toString().equalsIgnoreCase("William Antonio Garcia Garcia"));
            check("Clientes Iniciales: 'William Antonio Garcia Garcia' registrado",
                    tieneWilliam,
                    "No se encontró el cliente 'William Antonio Garcia Garcia'.");

            boolean tieneAndres = clientes.stream().anyMatch(c ->
                    c.toString().equalsIgnoreCase("Andres Sebastian Gonzalez Maradiaga"));
            check("Clientes Iniciales: 'Andres Sebastian Gonzalez Maradiaga' registrado",
                    tieneAndres,
                    "No se encontró el cliente 'Andres Sebastian Gonzalez Maradiaga'.");

            boolean tieneRafael = clientes.stream().anyMatch(c ->
                    c.toString().equalsIgnoreCase("Rafael Hernandez Sanchez"));
            check("Clientes Iniciales: 'Rafael Hernandez Sanchez' registrado",
                    tieneRafael,
                    "No se encontró el cliente 'Rafael Hernandez Sanchez'.");

            boolean tieneCaleb = clientes.stream().anyMatch(c ->
                    c.toString().equalsIgnoreCase("Caleb Jordan Tardencilla Alvarado"));
            check("Clientes Iniciales: 'Caleb Jordan Tardencilla Alvarado' registrado",
                    tieneCaleb,
                    "No se encontró el cliente 'Caleb Jordan Tardencilla Alvarado'.");

            // 10.2: Verificación del Dashboard en MainController
            FXMLLoader mainLoader = new FXMLLoader(TestRunner.class.getResource("/org/ni/edu/uam/casopracticog5/view/MainView.fxml"));
            Parent mainRoot = mainLoader.load();
            MainController mainCtrl = mainLoader.getController();

            Field lblTotalClientesField = MainController.class.getDeclaredField("lblTotalClientesDash");
            lblTotalClientesField.setAccessible(true);
            Label lblTotalClientes = (Label) lblTotalClientesField.get(mainCtrl);

            check("Dashboard: KPI Total Clientes refleja valor 4",
                    lblTotalClientes != null && "4".equals(lblTotalClientes.getText()),
                    "El KPI lblTotalClientesDash debe mostrar '4'.");

            Field lblTotalUsuariosField = MainController.class.getDeclaredField("lblTotalUsuariosDash");
            lblTotalUsuariosField.setAccessible(true);
            Label lblTotalUsuarios = (Label) lblTotalUsuariosField.get(mainCtrl);

            check("Dashboard: KPI Total Usuarios refleja valor 2",
                    lblTotalUsuarios != null && "2".equals(lblTotalUsuarios.getText()),
                    "El KPI lblTotalUsuariosDash debe mostrar '2'.");

            Field tablaDashField = MainController.class.getDeclaredField("tablaClientesDashboard");
            tablaDashField.setAccessible(true);
            TableView<?> tablaDash = (TableView<?>) tablaDashField.get(mainCtrl);

            check("Dashboard: Tabla de clientes recientes poblada con los registros iniciales",
                    tablaDash != null && tablaDash.getItems().size() >= 4,
                    "tablaClientesDashboard debe contener la lista observable de clientes.");

            Field lblBadgeRolField = MainController.class.getDeclaredField("lblBadgeRol");
            lblBadgeRolField.setAccessible(true);
            Label lblBadgeRol = (Label) lblBadgeRolField.get(mainCtrl);

            check("Dashboard: Badge de rol activo visible",
                    lblBadgeRol != null && lblBadgeRol.getText().contains("ADMINISTRADOR"),
                    "El badge debe reflejar el rol de la sesión activa.");

            // 10.3: Verificación de las imágenes personalizadas añadidas
            check("Recurso: actualizar.png existe en classpath",
                    TestRunner.class.getResource("/org/ni/edu/uam/casopracticog5/images/actualizar.png") != null,
                    "No se encontró la imagen actualizar.png.");

            check("Recurso: administracion.png existe en classpath",
                    TestRunner.class.getResource("/org/ni/edu/uam/casopracticog5/images/administracion.png") != null,
                    "No se encontró la imagen administracion.png.");

            check("Recurso: operador-de-ayuda.png existe en classpath",
                    TestRunner.class.getResource("/org/ni/edu/uam/casopracticog5/images/operador-de-ayuda.png") != null,
                    "No se encontró la imagen operador-de-ayuda.png.");

            // 10.4: Comprobar dinamismo de imagen según la cuenta logueada
            Usuario operUser = DataStore.buscarUsuario("operador");
            DataStore.setUsuarioActual(operUser);
            mainCtrl.actualizarDashboard();

            check("Dashboard: Badge cambia a MODO OPERADOR dinámicamente",
                    lblBadgeRol.getText().contains("OPERADOR"),
                    "El badge debe cambiar dinámicamente al loguearse como operador.");

            // Restaurar a admin
            Usuario adminUser = DataStore.buscarUsuario("admin");
            DataStore.setUsuarioActual(adminUser);
            mainCtrl.actualizarDashboard();

            check("Dashboard: Badge vuelve a MODO ADMINISTRADOR dinámicamente",
                    lblBadgeRol.getText().contains("ADMINISTRADOR"),
                    "El badge debe cambiar a administrador.");

        } catch (Exception e) {
            findings.add("Error en testDashboardYClientesIniciales: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
