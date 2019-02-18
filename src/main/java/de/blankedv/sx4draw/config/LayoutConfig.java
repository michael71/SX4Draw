package de.blankedv.sx4draw.config;

import java.util.ArrayList;

import javax.xml.bind.annotation.*;

import static de.blankedv.sx4draw.config.WriteConfig.FILENAME_XML;

//This statement means that class "LayoutConfig.java" is the root-element
@XmlRootElement(name = "layout-config")
public class LayoutConfig {
    // XmLElementWrapper generates a wrapper element around XML representation

    @XmlAttribute
    private String name = "noName";

    @XmlAttribute
    public String filename = FILENAME_XML;

    @XmlAttribute
    public String version = "";

    @XmlElementWrapper(name = "panels")
    @XmlElement(name = "panel")
    private ArrayList<PanelConfig> panelCfg = new ArrayList<>();

    public String getName2() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setVersion(String v) {
        this.version = v;
    }

    public void setPanelCfg(PanelConfig pc) {
    panelCfg.add(pc);

    }

}
