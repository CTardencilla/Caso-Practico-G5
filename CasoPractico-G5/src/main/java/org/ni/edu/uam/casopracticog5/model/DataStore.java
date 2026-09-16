package org.ni.edu.uam.casopracticog5.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Almacén centralizado en memoria para persistencia temporal de clientes.
 * Todos los módulos leen y escriben desde esta misma lista observable.
 */
public class DataStore {

    private static final ObservableList<Cliente> clientes = FXCollections.observableArrayList();

    public static ObservableList<Cliente> getClientes() {
        return clientes;
    }
}
