package de.blankedv.sx4draw.util;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.transform.Scale;

public class ZoomableScrollPane extends ScrollPane {

    Group zoomGroup;
    Scale scaleTransform;
    Node content;

    public ZoomableScrollPane(Node content) {
        this.content = content;
        Group contentGroup = new Group();
        zoomGroup = new Group();
        contentGroup.getChildren().add(zoomGroup);
        zoomGroup.getChildren().add(content);
        setContent(contentGroup);
        scaleTransform = new Scale(1.0,1.0, 0,0);
        zoomGroup.getTransforms().add(scaleTransform);
    }

    public void zoomIn() {
        scaleTransform = new Scale(2.0,2.0, 0,0);
        zoomGroup.getTransforms().add(scaleTransform);
    }

    public void zoomOut() {
        scaleTransform = new Scale(0.5,0.5, 0,0);
        zoomGroup.getTransforms().add(scaleTransform);
    }

}
