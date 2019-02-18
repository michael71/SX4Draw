package de.blankedv.sx4draw.config;

import de.blankedv.sx4draw.config.LayoutConfig;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * WriteConfig - Utility turnout save Panel Config
 *
 * @author Michael Blank
 * @version 1.0
 */
public class WriteConfig {

    public static final String FILENAME_XML = "panel_test_new.xml";

    static StringWriter writer;
    private static String localPanelName = "";

    /**
     * writeConfigToXML
     * <p>
     * saves all PanelElements to an XML file configFilename will have ".(date)"
     * appended, if no filename given
     *
     *
     *
     * @return true, if succeeds - false, if not.
     */
    public static boolean writeToXML(String fname, LayoutConfig lc) {




        try {
            JAXBContext context = JAXBContext.newInstance(LayoutConfig.class);
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            // Write to System.out
            m.marshal(lc, System.out);

            // Write to File
            m.marshal(lc, new File(fname));

        } catch (Exception e) {
            System.out.println("ERROR "+e.getMessage());
            e.printStackTrace();
        }


        return true;
    }


    /**
     * writeConfigToXML
     * <p>
     * saves all PanelElements (including deducted elements) to an XML file (=
     * simple XMLSerializer for lanbahn panel elements and routes)
     *
     * @param
     * @return true, if succeeds - false, if not.
     */
    private static String writeXml(LayoutConfig layoutConfig, String panelName, String fullFilename, String version) {

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
        return "OK";

    }

}
