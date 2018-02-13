/*
 * This file is part of Program JB.
 *
 * Program JB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Program JB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with Program JB. If not, see <http://www.gnu.org/licenses/>.
 */
package org.goldrenard.jb.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.StringWriter;

public class DomUtils {

    private static final Logger log = LoggerFactory.getLogger(DomUtils.class);

    public static Node parseFile(String fileName) throws Exception {
        File file = new File(fileName);

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        // from AIMLProcessor.evalTemplate and AIMLProcessor.validTemplate:
        //   dbFactory.setIgnoringComments(true); // fix this
        Document doc = dBuilder.parse(file);
        doc.getDocumentElement().normalize();
        return doc.getDocumentElement();
    }

    public static Node parseString(String string) throws Exception {
        InputStream is = new ByteArrayInputStream(string.getBytes("UTF-16"));

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        // from AIMLProcessor.evalTemplate and AIMLProcessor.validTemplate:
        //   dbFactory.setIgnoringComments(true); // fix this
        Document doc = dBuilder.parse(is);
        doc.getDocumentElement().normalize();
        return doc.getDocumentElement();
    }

    /**
     * convert an XML node to an XML statement
     *
     * @param node current XML node
     * @return XML string
     */
    public static String nodeToString(Node node) {
        if (log.isTraceEnabled()) {
            log.trace("nodeToString(node: {})", node);
        }
        StringWriter sw = new StringWriter();
        try {
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            t.setOutputProperty(OutputKeys.INDENT, "no");
            t.transform(new DOMSource(node), new StreamResult(sw));
        } catch (TransformerException e) {
            log.error("nodeToString Transformer Exception", e);
        }
        String result = sw.toString();
        if (log.isTraceEnabled()) {
            log.trace("nodeToString() returning: {}", result);
        }
        return result;
    }
}
