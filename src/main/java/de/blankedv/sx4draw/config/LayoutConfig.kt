package de.blankedv.sx4draw.config

import java.util.ArrayList

import javax.xml.bind.annotation.*

import de.blankedv.sx4draw.config.WriteConfig.FILENAME_XML

//This statement means that class "LayoutConfig.java" is the root-element
@XmlRootElement(name = "layout-config")
class LayoutConfig {
    // XmLElementWrapper generates a wrapper element around XML representation

    @XmlAttribute
    var name = "noName"

    @XmlAttribute
    var fileName = "panel_test.xml"

    @XmlAttribute
    var version = "0001"

    @XmlElementWrapper(name = "panels")
    @XmlElement(name = "panel")
    val panelCfgs  = ArrayList<PanelConfig>()


    /*fun setName(name: String) {
        this.name2 = name
    } */

    fun addPanelCfg(pc: PanelConfig) {
        panelCfgs.add(pc)
    }

}
