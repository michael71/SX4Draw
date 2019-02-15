/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.blankedv.sx4draw.views;

import static de.blankedv.sx4draw.views.SX4Draw.panelName;
import static de.blankedv.sx4draw.views.SX4Draw.routes;

import de.blankedv.sx4draw.Route;
import de.blankedv.sx4draw.util.Utils;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableRow;
import javafx.util.Callback;
import javafx.util.StringConverter;

/**
 * @author mblank
 */
public class RoutesTable {

    private final TableView<Route> tableView = new TableView<>();
    private SX4Draw app;

    Scene routingTableScene;
    // New window (Stage)
    Stage routingWindow;

    RoutesTable(Stage primaryStage, SX4Draw app) {

        BorderPane bp = new BorderPane();
        this.app = app;
        /* HBox hb = new HBox(15);
        Button btnClose = new Button("close");
        hb.getChildren().addAll(btnClose);
        bp.setBottom(hb);
        hb.setAlignment(Pos.CENTER);
        BorderPane.setMargin(hb, new Insets(8, 8, 8, 8)); */
        routingTableScene = new Scene(bp, 700, 300);
        bp.setCenter(tableView);

        // New window (Stage)
        routingWindow = new Stage();
        /* btnClose.setOnAction((e) -> {
            //sxAddress.addr = -1;
            routingWindow.close();
        }); */

        routingWindow.setTitle("Fahrstraßen Tabelle " + panelName);
        routingWindow.setScene(routingTableScene);

        // Specifies the modality for new window.
        //routingWindow.initModality(Modality.WINDOW_MODAL);
        // Specifies the owner Window (parent) for new window
        routingWindow.initOwner(primaryStage);

        // Set position of second window, related to primary window.
        routingWindow.setX(primaryStage.getX() + 200);
        routingWindow.setY(primaryStage.getY() + 100);

        createDataTables();

        routingWindow.show();

    }

    public void show() {
        routingWindow.show();
    }

    private void createDataTables() {

        TableColumn<Route, Integer> idCol = new TableColumn<>("ID");
        TableColumn<Route, Integer> btn1Col = new TableColumn<>("Start");
        TableColumn<Route, Integer> btn2Col = new TableColumn<>("Ende");
        TableColumn<Route, String> routeCol = new TableColumn<>("Fahrstraße");
        TableColumn<Route, String> sensorsCol = new TableColumn<>("Sensoren");

        /* final TextFormatter<String> formatter = new TextFormatter<String>(change -> {
            change.setText(change.getText().replaceAll("[^0-9.,]", ""));
            return change;

        }); */
        StringConverter myStringIntConverter = new StringConverter<Integer>() {
            @Override
            public String toString(Integer object) {
                if (object == null) {
                    return "-";
                }
                return "" + object;
            }

            @Override
            public Integer fromString(String string) {
                try {
                    return Integer.parseInt(string);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        };

        tableView.getColumns().setAll(idCol, btn1Col, btn2Col, routeCol, sensorsCol);
        tableView.setEditable(true);
        //idCol.setCellFactory(TextFieldTableCell.forTableColumn());
        /*idCol.setCellFactory(TextFieldTableCell.forTableColumn(myStringIntConverter));
        idCol.setOnEditCommit(new EventHandler<CellEditEvent<Route, Integer>>() {
            @Override
            public void handle(CellEditEvent<Route, Integer> ev) {
                ((Route) ev.getTableView().getItems().get(
                        ev.getTablePosition().getRow())).setRoute("" + ev.getNewValue());
            }
        }); */

        routeCol.setCellFactory(TextFieldTableCell.forTableColumn());
        routeCol.setOnEditCommit(new EventHandler<CellEditEvent<Route, String>>() {
            @Override
            public void handle(CellEditEvent<Route, String> ev) {
                ((Route) ev.getTableView().getItems().get(
                        ev.getTablePosition().getRow())).setRoute(ev.getNewValue());
            }
        });
        sensorsCol.setCellFactory(TextFieldTableCell.forTableColumn());
        sensorsCol.setOnEditCommit(new EventHandler<CellEditEvent<Route, String>>() {
            @Override
            public void handle(CellEditEvent<Route, String> ev) {
                ((Route) ev.getTableView().getItems().get(
                        ev.getTablePosition().getRow())).setSensors(ev.getNewValue());
            }
        });
        /*      tableViewData[i].setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            chanCol.setMaxWidth(1f * Integer.MAX_VALUE * 18); // 30% width
            chanCol.setStyle("-fx-alignment: CENTER;");
            dataCol.setMaxWidth(1f * Integer.MAX_VALUE * 18); // 70% width
            dataCol.setStyle("-fx-alignment: CENTER;"); */
        tableView.setCenterShape(true);
        tableView.setRowFactory(new Callback<TableView<Route>, TableRow<Route>>() {
            @Override
            public TableRow<Route> call(TableView<Route> tableView) {
                final TableRow<Route> row = new TableRow<>();
                final ContextMenu contextMenu = new ContextMenu();
                final MenuItem removeMenuItem = new MenuItem("Fahrstraße löschen");
                removeMenuItem.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        tableView.getItems().remove(row.getItem());
                    }
                });
                final MenuItem showMenuItem = new MenuItem("Fahrstraße anzeigen");
                showMenuItem.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        final Route rtShown = row.getItem();
                        app.showRoute(rtShown, 6);
                        /*rtShown.setRouteStates();
                        app.redrawPanelElements();
                        app.drawRTButtons(rtShown.getBtn1(), rtShown.getBtn2());
                        Timeline timeline = new Timeline(new KeyFrame(
                                Duration.millis(6000),
                                ae -> {
                                    rtShown.setMarked(false);
                                    app.redrawPanelElements();
                                }));

                        timeline.play(); */

                    }
                });
                contextMenu.getItems().addAll(showMenuItem, removeMenuItem);
                // Set context menu on row, but use a binding to make it only show for non-empty rows:
                row.contextMenuProperty().bind(
                        Bindings.when(row.emptyProperty())
                                .then((ContextMenu) null)
                                .otherwise(contextMenu)
                );
                return row;
            }
        });
        /*   tableView.setRowFactory(new Callback<TableView<RouteData>, TableRow<RouteData>>() {
                @Override
                public TableRow<RouteData> call(TableView<RouteData> tableView) {
                    final TableRow<RouteData> row = new TableRow<RouteData>() {
                        @Override
                        protected void updateItem(Route sxv, boolean empty) {
                            super.updateItem(sxv, empty);
                            if (!empty) {
                                if (sxv.isMarked()) {
                                    setStyle("-fx-background-color: yellow;");
                                } else {
                                    setStyle("");
                                }
                            } else {
                                setStyle("");
                            }
                        }
                    };
                    return row;
                }
            }); */

        idCol.setCellValueFactory(new PropertyValueFactory<>("adr"));
        btn1Col.setCellValueFactory(new PropertyValueFactory<>("btn1"));
        btn2Col.setCellValueFactory(new PropertyValueFactory<>("btn2"));
        routeCol.setCellValueFactory(new PropertyValueFactory<>("route"));
        sensorsCol.setCellValueFactory(new PropertyValueFactory<>("sensors"));

        tableView.setItems(routes);

        // textField.setTextFormatter(formatter);
        Utils.INSTANCE.customResize(tableView);

    }

}
