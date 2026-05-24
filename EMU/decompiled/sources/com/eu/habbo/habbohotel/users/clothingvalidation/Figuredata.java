package com.eu.habbo.habbohotel.users.clothingvalidation;

import com.eu.habbo.messages.outgoing.rooms.items.rentablespaces.RentableSpaceInfoComposer;
import java.io.StringWriter;
import java.util.Map;
import java.util.TreeMap;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/users/clothingvalidation/Figuredata.class */
public class Figuredata {
    public Map<Integer, FiguredataPalette> palettes = new TreeMap();
    public Map<String, FiguredataSettype> settypes = new TreeMap();

    public void parseXML(String str) throws Exception {
        DocumentBuilderFactory documentBuilderFactoryNewInstance = DocumentBuilderFactory.newInstance();
        documentBuilderFactoryNewInstance.setValidating(false);
        documentBuilderFactoryNewInstance.setIgnoringElementContentWhitespace(true);
        Document document = documentBuilderFactoryNewInstance.newDocumentBuilder().parse(str);
        if (!document.getDocumentElement().getTagName().equalsIgnoreCase("figuredata") || document.getElementsByTagName("colors") == null || document.getElementsByTagName("sets") == null) {
            StringWriter stringWriter = new StringWriter();
            TransformerFactory.newInstance().newTransformer().transform(new DOMSource(document), new StreamResult(stringWriter));
            String string = stringWriter.getBuffer().toString();
            throw new Exception("The passed file is not in figuredata format. Received " + string.substring(0, Math.min(string.length(), RentableSpaceInfoComposer.NOT_ENOUGH_CREDITS)));
        }
        NodeList childNodes = document.getElementsByTagName("colors").item(0).getChildNodes();
        NodeList childNodes2 = document.getElementsByTagName("sets").item(0).getChildNodes();
        this.palettes.clear();
        this.settypes.clear();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node nodeItem = childNodes.item(i);
            if (nodeItem.getNodeType() == 1) {
                FiguredataPalette figuredataPalette = new FiguredataPalette(Integer.parseInt(((Element) nodeItem).getAttribute("id")));
                NodeList childNodes3 = nodeItem.getChildNodes();
                for (int i2 = 0; i2 < childNodes3.getLength(); i2++) {
                    if (childNodes3.item(i2).getNodeType() == 1) {
                        Element element = (Element) childNodes3.item(i2);
                        figuredataPalette.addColor(new FiguredataPaletteColor(Integer.parseInt(element.getAttribute("id")), Integer.parseInt(element.getAttribute("index")), !element.getAttribute("club").equals("0"), element.getAttribute("selectable").equals("1"), element.getTextContent()));
                    }
                }
                this.palettes.put(Integer.valueOf(figuredataPalette.id), figuredataPalette);
            }
        }
        for (int i3 = 0; i3 < childNodes2.getLength(); i3++) {
            Node nodeItem2 = childNodes2.item(i3);
            if (nodeItem2.getNodeType() == 1) {
                Element element2 = (Element) nodeItem2;
                FiguredataSettype figuredataSettype = new FiguredataSettype(element2.getAttribute("type"), Integer.parseInt(element2.getAttribute("paletteid")), element2.getAttribute("mand_m_0").equals("1"), element2.getAttribute("mand_f_0").equals("1"), element2.getAttribute("mand_m_1").equals("1"), element2.getAttribute("mand_f_1").equals("1"));
                NodeList childNodes4 = nodeItem2.getChildNodes();
                for (int i4 = 0; i4 < childNodes4.getLength(); i4++) {
                    if (childNodes4.item(i4).getNodeType() == 1) {
                        Element element3 = (Element) childNodes4.item(i4);
                        figuredataSettype.addSet(new FiguredataSettypeSet(Integer.parseInt(element3.getAttribute("id")), element3.getAttribute("gender"), !element3.getAttribute("club").equals("0"), element3.getAttribute("colorable").equals("1"), element3.getAttribute("selectable").equals("1"), element3.getAttribute("preselectable").equals("1"), element3.getAttribute("sellable").equals("1")));
                    }
                }
                this.settypes.put(figuredataSettype.type, figuredataSettype);
            }
        }
    }
}
