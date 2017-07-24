package com.retoandroidlectorrss.utils;

import com.retoandroidlectorrss.models.ReadRss;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;


public class XML extends DefaultHandler {
    private List<ReadRss> allNews;
    private ReadRss currentNew;
    private StringBuilder sbText;

    public List<ReadRss> getNoticias() {
        return allNews;
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);
        if (this.currentNew != null)

            //Añade a nuestro StringBuilder la secuencia ch
            sbText.append(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String name) throws SAXException {
        super.endElement(uri, localName, name);

        //usar trim para las cadenas ya que vienen con space line completa

        if (this.currentNew != null) {
            if (localName.equals(Constantes.XML_TITLE)) {
                currentNew.setTitle(sbText.toString().trim());
            } else if (localName.equals(Constantes.XML_LINK)) {
                currentNew.setLink(sbText.toString().trim());
            } else if (localName.equals(Constantes.XML_DESCRIPTION)) {
                currentNew.setDescription(sbText.toString().trim());
            } else if (localName.equals(Constantes.XML_PUB_DATE)) {
                currentNew.setDate(sbText.toString().trim());
            } else if (localName.equals(Constantes.XML_ITEM)) {
                allNews.add(currentNew);
            }
            sbText.setLength(0);
        }
    }

    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
        allNews = new ArrayList<>();
        sbText = new StringBuilder();
    }

    @Override
    public void startElement(String uri, String localName, String name, Attributes attributes) throws
            SAXException {
        super.startElement(uri, localName, name, attributes);
        if (localName.equals(Constantes.XML_ITEM)) {

            //Creamos una noticia nueva para cada item encontrado
            currentNew = new ReadRss();

        } else {
            if (localName.equals(Constantes.XML_IMAGE_DIV)) {
                currentNew.setImage(attributes.getValue(Constantes.XML_URL));
            }
        }
    }
}

