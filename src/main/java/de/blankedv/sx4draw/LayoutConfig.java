package de.blankedv.sx4draw;

import de.blankedv.sxdraw.Trip;

import java.util.ArrayList;

import javax.xml.bind.annotation.*;

import static de.blankedv.sx4draw.SX4Draw.panelConfig;
import static de.blankedv.sx4draw.SX4Draw.panelName;
import static de.blankedv.sx4draw.WriteConfigNew.FILENAME_XML;

//This statement means that class "LayoutConfig.java" is the root-element
@XmlRootElement(name = "layout-config")
public class LayoutConfig {
    // XmLElementWrapper generates a wrapper element around XML representation

    @XmlAttribute
    private String name = "noName";

    @XmlAttribute
    private String filename = FILENAME_XML;

    @XmlElementWrapper(name = "panels")
    @XmlElement(name = "panel")
    private ArrayList<PanelConfig> panelCfg = new ArrayList<>();

    public String getName2() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPanelCfg(PanelConfig pc) {
    panelCfg.add(pc);

    }
}
