package de.blankedv.sx4draw.config

import java.util.ArrayList

import javax.xml.bind.annotation.*

import de.blankedv.sx4draw.config.WriteConfig.FILENAME_XML

//This statement means that class "LayoutConfig.java" is the root-element
@XmlRootElement(name = "layout-config")
class LayoutConfig {
    // see https://blog.scottlogic.com/2016/04/04/practical-kotlin.html

    @get:XmlAttribute
    var fileName = ""

    @get:XmlAttribute
    var version = "0001"

    @XmlElementWrapper(name = "panels")
    @get:XmlElement(name = "panel")
    private val panel  = ArrayList<PanelConfig>()

    constructor()

    constructor(fn : String, pc: PanelConfig, ve: String) {
        this.fileName = fn
        panel.add(pc)
        this.version = ve
    }

    fun getPC0() : PanelConfig? {
        if (panel.size >= 1) {
            return panel.get(0)
        } else {
            return null
        }
    }



}
