package de.blankedv.sx4draw.config

import de.blankedv.sx4draw.config.LayoutConfig

import javax.xml.bind.JAXBContext
import javax.xml.bind.Marshaller
import java.io.File
import java.io.FileWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date

/**
 * WriteConfig - Utility turnout save Panel Config
 *
 * @author Michael Blank
 * @version 1.0
 */
object WriteConfig {

    val FILENAME_XML = "panel_test_new.xml"

    internal var writer: StringWriter? = null
    private val localPanelName = ""

    /**
     * writeConfigToXML
     *
     *
     * saves all PanelElements to an XML file configFilename will have ".(date)"
     * appended, if no filename given
     *
     *
     *
     * @return true, if succeeds - false, if not.
     */
    fun writeToXML(fname: String, lc: LayoutConfig): Boolean {


        try {
            val context = JAXBContext.newInstance(LayoutConfig::class.java)
            val m = context.createMarshaller()
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, java.lang.Boolean.TRUE)

            // Write to System.out
            m.marshal(lc, System.out)

            // Write to File
            m.marshal(lc, File(fname))

        } catch (e: Exception) {
            println("ERROR " + e.message)
            e.printStackTrace()
        }


        return true
    }


    /**
     * writeConfigToXML
     *
     *
     * saves all PanelElements (including deducted elements) to an XML file (=
     * simple XMLSerializer for lanbahn panel elements and routes)
     *
     * @param
     * @return true, if succeeds - false, if not.
     */
    private fun writeXml(layoutConfig: LayoutConfig, panelName: String, fullFilename: String, version: String): String {

        // create JAXB context and instantiate marshaller


        /*
        // get variables from our xml file, created before
        System.out.println();
        System.out.println("Output from our XML File: ");
        Unmarshaller um = context.createUnmarshaller();
        Bookstore bookstore2 = (Bookstore) um.unmarshal(new FileReader(
                FILENAME_XML));
        ArrayList<Book> list = bookstore2.getBooksList();
        for (Book book : list) {
            System.out.println("Book: " + book.getName() + " from "
                    + book.getAuthor());
        }

        //writer.write("<?xml version='1.0' encoding='UTF-8' standalone='yes' ?>\n");
        //writer.write("<layout-config filename=\"" + filename + "\">\n");
        //writer.write("<locolist name=\"" + locolistName + "\" version=\"" + version+ "\">\n");
    */
        return "OK"

    }

}
