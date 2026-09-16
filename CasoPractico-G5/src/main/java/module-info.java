module org.ni.edu.uam.casopracticog5 {
    requires javafx.controls;
    requires javafx.fxml;

    opens org.ni.edu.uam.casopracticog5 to javafx.fxml;
    opens org.ni.edu.uam.casopracticog5.controller to javafx.fxml;
    opens org.ni.edu.uam.casopracticog5.model to javafx.base;

    exports org.ni.edu.uam.casopracticog5;
    exports org.ni.edu.uam.casopracticog5.model;
    exports org.ni.edu.uam.casopracticog5.controller;
    exports org.ni.edu.uam.casopracticog5.util;
}