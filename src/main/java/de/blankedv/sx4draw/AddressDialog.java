/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.blankedv.sx4draw;

import static de.blankedv.sx4draw.Constants.LBMAX;
import static de.blankedv.sx4draw.Constants.LBMIN;
import static de.blankedv.sx4draw.Constants.SXMAX_USED;

import de.blankedv.sx4draw.SX4Draw.PEType;

import static de.blankedv.sx4draw.SX4Draw.panelElements;
import static de.blankedv.sx4draw.ReadConfig.YOFF;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * @author Michael Blank <mblank@bawue.de>
 */
public class AddressDialog {

    final static GenericAddress genAddress = new GenericAddress();
    static GenericAddress initAddress;

    final static Spinner<Integer> spinner1000 = new Spinner<>(0, 9, 0);
    final static Spinner<Integer> spinner100 = new Spinner<>(0, 9, 0);
    final static Spinner<Integer> spinner10 = new Spinner<>(0, 9, 0);
    final static Spinner<Integer> spinner1 = new Spinner<>(0, 9, 0);
    final static Label lblAdr = new Label(" Adresse");
    final static CheckBox inv = new CheckBox();

    static GenericAddress open(PanelElement pe, Stage primaryStage, GenericAddress initVal) {

        initAddress = initVal;

        PEType type = pe.type;
        String title = "Adresse " + type.name().charAt(0)
                + type.name().toLowerCase().substring(1) + " Pos.= " + pe.x + "," + (pe.y - YOFF);
        int in1000 = initVal.addr / 1000;
        int in100 = (initVal.addr - in1000 * 1000) / 100;
        int in10 = (initVal.addr - in1000 * 1000 - in100 * 100) / 10;
        int in1 = (initVal.addr - in1000 * 1000 - in100 * 100 - in10 * 10);
        spinner1000.getValueFactory().setValue(in1000);
        spinner100.getValueFactory().setValue(in100);
        spinner10.getValueFactory().setValue(in10);
        spinner1.getValueFactory().setValue(in1);
        genAddress.addr = initVal.addr;
        updateAddress();
        System.out.println("init address =" + initVal.addr);

        Label lblInv = new Label(" invertiert");

        // select inv (ONLY for TURNOUT)
        if (type == PEType.TURNOUT) {
            genAddress.inv = initVal.inv;
            if (initVal.inv != 0) {
                inv.setSelected(true);
            } else {
                inv.setSelected(false);
            }
        } else {
            genAddress.inv = 0;
            inv.setVisible(false);
            lblInv.setVisible(false);
        }

        // select orientation (ONLY for SIGNAL)
        final ChoiceBox orient = new ChoiceBox(FXCollections.observableArrayList(
                "0°", "45°", "90°", "135°", "180°", "225°", "270°", "315°")
        );

        Label lblOrient = new Label(" Orient.");

        if (type == PEType.SIGNAL) {
            genAddress.orient = initVal.orient;
            orient.getSelectionModel().select(genAddress.orient);
        } else {
            genAddress.orient = 0;
            orient.setDisable(true);
            orient.setVisible(false);
            lblOrient.setVisible(false);
        }

        ChangeListener addrListener = new ChangeListener<Integer>() {
            @Override
            public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
                updateAddress();
            }
        };

        ChangeListener invListener = new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                updateAddress();
            }
        };

        spinner1000.valueProperty().addListener(addrListener);
        spinner100.valueProperty().addListener(addrListener);
        spinner10.valueProperty().addListener(addrListener);
        spinner1.valueProperty().addListener(addrListener);
        inv.selectedProperty().addListener(invListener);

        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.add(lblAdr, 0, 1);
        grid.add(spinner1000, 1, 1);
        grid.add(spinner100, 2, 1);
        grid.add(spinner10, 3, 1);
        grid.add(spinner1, 4, 1);

        grid.add(lblInv, 0, 3);
        grid.add(inv, 1, 3);
        grid.add(lblOrient, 2, 3);
        grid.add(orient, 3, 3);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(19);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(19);
        ColumnConstraints col3 = new ColumnConstraints();
        col3.setPercentWidth(19);
        ColumnConstraints col4 = new ColumnConstraints();
        col4.setPercentWidth(19);
        ColumnConstraints col5 = new ColumnConstraints();
        col5.setPercentWidth(19);
        ColumnConstraints col6 = new ColumnConstraints();
        col6.setPercentWidth(5);
        grid.getColumnConstraints().addAll(col1, col2, col3, col4, col5, col6);

        GridPane.setMargin(grid, new Insets(5, 5, 5, 5));

        Button btnCancel = new Button("zurück");

        Button btnSave = new Button("  OK  ");
        grid.add(btnCancel, 1, 5, 1, 1);
        grid.add(btnSave, 3, 5, 1, 1);
        //GridPane.setMargin(btnCancel, new Insets(5, 5, 5, 5));

        Scene secondScene = new Scene(grid, 420, 160);
        // New window (Stage)
        Stage newWindow = new Stage();
        btnCancel.setOnAction((e) -> {
            genAddress.addr = -1;
            newWindow.close();
        });
        btnSave.setOnAction((e) -> {
            genAddress.orient = orient.getSelectionModel().getSelectedIndex();
            newWindow.close();
        });
        newWindow.setTitle(title);
        newWindow.setScene(secondScene);

        // Specifies the modality for new window.
        newWindow.initModality(Modality.WINDOW_MODAL);

        // Specifies the owner Window (parent) for new window
        newWindow.initOwner(primaryStage);

        // Set position of second window, related to primary window.
        newWindow.setX(primaryStage.getX() + 200);
        newWindow.setY(primaryStage.getY() + 100);

        newWindow.showAndWait();

        return genAddress;
    }

    private static void updateAddress() {
        genAddress.addr = spinner1000.getValue() * 1000
                + spinner100.getValue() * 100
                + spinner10.getValue() * 10
                + spinner1.getValue();
        if (genAddress.addr >= LBMIN) {
            lblAdr.setText(" Virt.-Adr");
        } else {
            lblAdr.setText(" SX-Adr");
        }
        if (inv.isSelected()) {
            genAddress.inv = 1;
        } else {
            genAddress.inv = 0;
        }
    }

}
