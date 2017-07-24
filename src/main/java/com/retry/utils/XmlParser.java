package com.retry.utils;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zbyte on 17-7-24.
 */
public class XmlParser {

    private final static String CLASS = "com.kepler.service.imported.ImportedServiceFactory";
    private final static String PARENT = "kepler.service.imported.abstract";

    private static XMLReader xmlReader = null;

    private static boolean isStart = false;
    private static String id;
    private static String interfc;
    private static List<Item> list = new ArrayList<>();

    static {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = null;
        try {
            parser = factory.newSAXParser();
            xmlReader = parser.getXMLReader();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }

        xmlReader.setContentHandler(new DefaultHandler(){

            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                if ("bean".equals(qName) && CLASS.equals(attributes.getValue("class"))
                        && PARENT.equals(attributes.getValue("parent"))) {
                    id = attributes.getValue("id");
                    isStart = true;
                } else if (isStart && "constructor-arg".equals(qName)) {
                    interfc = attributes.getValue("value");
                }
            }

            @Override
            public void endElement(String uri, String localName, String qName) throws SAXException {
                if (isStart && "bean".equals(qName)) {
                    list.add(new Item(id, interfc));
                    isStart = false;
                }
            }
        });
    }

    public static List<Item> parse(InputStream input) {
        list.clear();
        try {
            xmlReader.parse(new InputSource(input));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static class Item {
        String beanId;
        String interfc;
        public Item(String beanId, String interfc) {
            this.beanId = beanId;
            this.interfc = interfc;
        }

        public String getBeanId() {
            return beanId;
        }

        public String getInterfc() {
            return interfc;
        }
    }
}
