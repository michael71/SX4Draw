/*
 * Copyright (C) 2018 mblank
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.blankedv.sx4draw;

import com.sun.deploy.uitoolkit.impl.fx.HostServicesFactory;
import com.sun.javafx.application.HostServicesDelegate;

import static de.blankedv.sx4draw.Constants.LBMAX;
import static de.blankedv.sx4draw.Constants.LBMIN;
import static de.blankedv.sx4draw.Constants.SXMAX_USED;
import static de.blankedv.sx4draw.SX4Draw.panelElements;

import java.util.Optional;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Pair;

/**
 * @author mblank
 */
public class Dialogs {

    public static void InfoAlert(String title, String header, String msg, Application app) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(msg);
        alert.setTitle(title);
        alert.setHeaderText(header);
        Window window = alert.getDialogPane().getScene().getWindow();
        window.setOnCloseRequest(event -> window.hide());

        ButtonType openOpensx = new ButtonType("-> opensx.net/sx4");
        alert.getButtonTypes().addAll(openOpensx);
        Optional<ButtonType> option = alert.showAndWait();

        if ((option.isPresent()) && (option.get() == openOpensx)) {
            try {
//                HostServicesDelegate hostServices = HostServicesFactory.getInstance(app);
                app.getHostServices().showDocument("https://opensx.net/sx4");
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public static String RequestAddress(String title, String header, String msg) {

        Dialog dialog = new TextInputDialog("851");

        dialog.setTitle(title);
        dialog.setHeaderText("Selectrix Addresse und Bit");
        Optional<String> result = dialog.showAndWait();
        String entered = "none.";
        if (result.isPresent()) {
            entered = result.get();
        }
        return entered;
    }

    /* custom dialog to enter an sx address

     */
    public static Optional<String> GetVirtualAddress() {
        Dialog<String> dialog = new Dialog<>();

        dialog.setTitle("Virtuelle Addresse");
        dialog.setHeaderText("Adr (>= 1200) ?");

// Set the icon (must be included in the project).
//dialog.setGraphic(new ImageView(this.getClass().getResource("login.png").toString()));
// Set the button types.
        ButtonType saveButtonType = new ButtonType("Speichern", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

// Create the sxAddr and sxBit labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField addr = new TextField();
        addr.setPromptText("Adresse");


        grid.add(new Label("Adresse:"), 0, 0);
        grid.add(addr, 1, 0);


// Enable/Disable login button depending on whether a sxAddr was entered.
        Node loginButton = dialog.getDialogPane().lookupButton(saveButtonType);
        loginButton.setDisable(true);

// Do some validation (using the Java 8 lambda syntax).
        addr.textProperty().addListener((observable, oldValue, newValue) -> {
            loginButton.setDisable(newValue.trim().isEmpty());
        });

        dialog.getDialogPane().setContent(grid);

// Request focus on the sxAddr field by default.
        Platform.runLater(() -> addr.requestFocus());


        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return addr.getText();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(res -> {
            System.out.println("Addr=" + res);
        });


        return result;
    }

    /* custom dialog to enter an sx address

     */
    public static Optional<Pair<String, String>> GetAddress() {
        Dialog<Pair<String, String>> dialog = new Dialog<>();

        dialog.setTitle("GetAddress Dialog");
        dialog.setHeaderText("SX Addr ?");

// Set the icon (must be included in the project).
//dialog.setGraphic(new ImageView(this.getClass().getResource("login.png").toString()));
// Set the button types.
        ButtonType saveButtonType = new ButtonType("Speichern", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

// Create the sxAddr and sxBit labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField sxAddr = new TextField();
        sxAddr.setPromptText("SX Adresse");
        TextField sxBit = new TextField();
        sxBit.setPromptText("SX bit");

        grid.add(new Label("SX Adresse:"), 0, 0);
        grid.add(sxAddr, 1, 0);
        grid.add(new Label("SX Bit:"), 0, 1);
        grid.add(sxBit, 1, 1);

// Enable/Disable login button depending on whether a sxAddr was entered.
        Node loginButton = dialog.getDialogPane().lookupButton(saveButtonType);
        loginButton.setDisable(true);

// Do some validation (using the Java 8 lambda syntax).
        sxAddr.textProperty().addListener((observable, oldValue, newValue) -> {
            loginButton.setDisable(newValue.trim().isEmpty());
        });

        dialog.getDialogPane().setContent(grid);

// Request focus on the sxAddr field by default.
        Platform.runLater(() -> sxAddr.requestFocus());

// Convert the result to a sxAddr-sxBit-pair when the login button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return new Pair<>(sxAddr.getText(), sxBit.getText());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();

        result.ifPresent(sx -> {
            System.out.println("SX-Addr=" + sx.getKey() + ", Bit=" + sx.getValue());
        });


        /*      String[] styleClasses = new String[]{"", // Default.
            Spinner.STYLE_CLASS_ARROWS_ON_RIGHT_HORIZONTAL, //
            Spinner.STYLE_CLASS_ARROWS_ON_LEFT_VERTICAL, //
            Spinner.STYLE_CLASS_ARROWS_ON_LEFT_HORIZONTAL, //
            Spinner.STYLE_CLASS_SPLIT_ARROWS_VERTICAL, //
            Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL

        };

        FlowPane root = new FlowPane();
        root.setHgap(10);
        root.setVgap(10);
        root.setPadding(new Insets(10));

        for (String styleClass : styleClasses) {
            Spinner<Integer> spinner = new Spinner<Integer>(1, 20, 10);
            spinner.getStyleClass().add(styleClass);
            root.getChildren().add(spinner);
        }

        Scene scene = new Scene(root, 400, 200);

        dialogStage.setScene(scene);
        dialogStage.show();
        
        return "100";
         */
        return result;
    }

    public static boolean checkAddress(GenericAddress oldValue, GenericAddress newAddress) {
        System.out.println("check address = " + newAddress.addr);
        if (
                ((newAddress.addr >= 0) &&
                        (newAddress.addr <= SXMAX_USED) &&
                        ((newAddress.addr % 10) != 0) &&
                        ((newAddress.addr % 10) != 9)
                )
                        || // ==valid sx address
                        ((newAddress.addr >= LBMIN) && (newAddress.addr <= LBMAX))) { // valid lanbahn address
            // the address is valid
            boolean found = false;
            PanelElement foundPE = null;
            for (PanelElement pe : panelElements) {
                if ((newAddress.addr != oldValue.addr) && (newAddress.addr == pe.adr)) {
                    found = true;
                    foundPE = pe;
                    foundPE.toggleShapeSelected();
                    break;
                }
            }
            if (found) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Warnung");
                alert.setHeaderText("Die Adresse " + newAddress.toString() + "wird bereits verwendet!");
                alert.setContentText("Trotzdem verwenden?");

                Optional<ButtonType> result = alert.showAndWait();
                foundPE.toggleShapeSelected();   // never =null
                if (result.get() == ButtonType.OK) {
                    return true;
                } else {
                    return false;
                }
            }
            return true;

        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR: " + newAddress.toString() + " ist ungültig!");
            alert.setHeaderText("Bitte eine gültige Adresse eingeben");
            alert.setContentText("SX Bereich: 0..106(1..8) - virtuell: 1200-9999");
            alert.showAndWait();
            return false;
        }

    }


}
