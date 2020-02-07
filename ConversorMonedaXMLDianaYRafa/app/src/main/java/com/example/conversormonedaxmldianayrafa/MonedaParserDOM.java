package com.example.conversormonedaxmldianayrafa;


import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class MonedaParserDOM {

    private URL urlCambios;

    public MonedaParserDOM(String url){

        try{
            this.urlCambios= new URL(url);
        } catch (MalformedURLException e){
            throw new RuntimeException(e);
        }
    }

    public List<Moneda> parse(){
        DocumentBuilderFactory dbf= DocumentBuilderFactory.newInstance();
        List<Moneda> monedas= new ArrayList<Moneda>();
        try{

            DocumentBuilder db= dbf.newDocumentBuilder();

            Document doc= db.parse(this.getInputStream());
            Element primerE= doc.getDocumentElement();


            Node segundoE= primerE.getFirstChild().getNextSibling();

            Node tercerE= segundoE.getNextSibling().getNextSibling();

            Node cuartoE= tercerE.getNextSibling().getNextSibling();

            Node cubesf= cuartoE.getFirstChild().getNextSibling();

            NodeList cubes= cubesf.getChildNodes();

            for(int i= 1; i<cubes.getLength(); i+=2){
                Moneda moneda= new Moneda();
                Node cubeActual= cubes.item(i);
                NamedNodeMap attr= cubeActual.getAttributes();
                String currency= attr.getNamedItem("currency").getNodeValue();
                float rate= Float.parseFloat(attr.getNamedItem("rate").getNodeValue());
                moneda.setNombre(currency);
                moneda.setCambio(rate);
                monedas.add(moneda);
            }

            return monedas;

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private InputStream getInputStream(){
        try{
            return urlCambios.openConnection().getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
