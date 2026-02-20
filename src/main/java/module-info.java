module com.example.encs3390_project2 {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.encs3390_project2 to javafx.fxml;
    exports com.example.encs3390_project2;
}