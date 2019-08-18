package de.blankedv.sx4draw.views;

import javafx.scene.Scene;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import static javafx.stage.Modality.*;

public class PopupChoice {

    public static void open(Stage primaryStage) {
        ChoiceDialog<String> dlg = new ChoiceDialog<>("Grün",
                "Gelb", "Rot");
        dlg.setTitle("Signalstelleung?");
        String optionalMasthead = "";
        dlg.getDialogPane().setContentText("?");

        dlg.show();
    }
}
