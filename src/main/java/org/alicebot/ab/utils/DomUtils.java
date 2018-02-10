package org.alicebot.ab.utils;

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


	public static Node parseFile(String fileName) throws Exception {
		File file = new File(fileName);

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		// from AIMLProcessor.evalTemplate and AIMLProcessor.validTemplate:
		//   dbFactory.setIgnoringComments(true); // fix this
		Document doc = dBuilder.parse(file);
		doc.getDocumentElement().normalize();
		Node root = doc.getDocumentElement();
		return root;
	}


	public static Node parseString(String string) throws Exception {
		InputStream is = new ByteArrayInputStream(string.getBytes("UTF-16"));

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		// from AIMLProcessor.evalTemplate and AIMLProcessor.validTemplate:
		//   dbFactory.setIgnoringComments(true); // fix this
		Document doc = dBuilder.parse(is);
		doc.getDocumentElement().normalize();
		Node root = doc.getDocumentElement();
		return root;
	}


    /**
     * convert an XML node to an XML statement
     * @param node        current XML node
     * @return            XML string
     */
    public static String nodeToString(Node node) {
		//MagicBooleans.trace("nodeToString(node: " + node + ")");
        StringWriter sw = new StringWriter();
        try {
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            t.setOutputProperty(OutputKeys.INDENT, "no");
            t.transform(new DOMSource(node), new StreamResult(sw));
        } catch (TransformerException te) {
            System.out.println("nodeToString Transformer Exception");
        }
		String result = sw.toString();
		//MagicBooleans.trace("nodeToString() returning: " + result);
        return result;
    }
}
