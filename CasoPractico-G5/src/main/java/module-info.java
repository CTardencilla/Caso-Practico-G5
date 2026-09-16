module org.ni.edu.uam.casopracticog5 {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.ni.edu.uam.casopracticog5 to javafx.fxml;
    exports org.ni.edu.uam.casopracticog5;
}