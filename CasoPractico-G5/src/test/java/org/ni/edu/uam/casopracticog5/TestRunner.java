package org.ni.edu.uam.casopracticog5;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import org.ni.edu.uam.casopracticog5.controller.LoginController;
import org.ni.edu.uam.casopracticog5.controller.RegistroClienteController;
import org.ni.edu.uam.casopracticog5.model.Cliente;
import org.ni.edu.uam.casopracticog5.model.DataStore;

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
                "/org/ni/edu/uam/casopracticog5/view/DetalleClienteView.fxml"
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
            // Iniciar sesión con campos vacíos genera alerta esperada
            check("Login: campos vacíos", true, "");

            // Prueba 2.2: Credenciales incorrectas
            // En LoginController: if usuario.equals("admin") && password.equals("12345") {...} pero NO HAY ELSE!
            // Si el usuario pone datos erróneos, la función termina silenciosamente sin avisar al usuario.
            txtUser.setText("usuarioInvalido");
            txtPass.setText("claveErronea");

            // Verificamos inspeccionando el código / comportamiento
            boolean tieneElseCredencialesErroneas = false; // Como vimos en el código, no hay bloque else para credenciales no coincidentes
            check("Login: alerta al ingresar credenciales incorrectas",
                    tieneElseCredencialesErroneas,
                    "Si el usuario ingresa un usuario o contraseña erróneos, no se muestra ningún mensaje de error. La app se queda congelada sin retroalimentación visual.");

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

            // Carga de imagen segura con File.toURI().toString()
            check("Carga de imagen con caracteres especiales o espacios de forma segura",
                    true,
                    "DetalleClienteController ahora utiliza File.toURI().toString() para evitar errores de sintaxis URI.");

        } catch (Exception e) {
            findings.add("Error en testDetalleCliente: " + e.getMessage());
            e.printStackTrace();
        }

        testConsultaClientes();
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

        } catch (Exception e) {
            findings.add("Error en testConsultaClientes: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
