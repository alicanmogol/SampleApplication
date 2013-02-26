package com.fererlab.action;

import com.fererlab.dto.Request;
import com.fererlab.dto.RequestKeys;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import com.thoughtworks.xstream.io.xml.StaxDriver;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * acm | 1/23/13
 */
public class BaseAction implements Action {

    private final Logger logger = Logger.getLogger(getClass().getSimpleName());

    private XStream xStreamJSON = new XStream(new JettisonMappedXmlDriver());
    private XStream xstream = new XStream(new StaxDriver());

    public BaseAction() {
        xstream.autodetectAnnotations(true);
        xStreamJSON.autodetectAnnotations(true);
    }

    public XStream getXStreamJSON() {
        return xStreamJSON;
    }

    public XStream getXStream() {
        return xstream;
    }

    @Override
    public String toContent(Request request, Object... objects) {

        // if RESPONSE_TYPE is defined and is JSON return toJson
        if (request.getHeaders().containsKey(RequestKeys.RESPONSE_TYPE.getValue())
                && ((String) request.getHeaders().get(RequestKeys.RESPONSE_TYPE.getValue()).getValue()).equalsIgnoreCase("JSON")) {
            return toJSON(objects);
        }

        // else if RESPONSE_TEMPLATE exists exists return XML with template
        else if (request.getParams().containsKey(RequestKeys.RESPONSE_TEMPLATE.getValue())) {
            StringBuilder responseContent = new StringBuilder();
            responseContent.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            responseContent.append("<?xml-stylesheet type=\"text/xsl\" href=\"")
                    .append(String.valueOf(request.getParams().getValue(RequestKeys.RESPONSE_TEMPLATE.getValue())))
                    .append(".xsl\"?>");
            responseContent.append("<root>");
            responseContent.append(toXML(objects));
            responseContent.append("</root>");
            return responseContent.toString();
        }

        // else return XML
        else {
            return toXML(objects);
        }

    }

    @Override
    public void log(String message) {
        logger.log(Level.INFO, message);
    }

    public String toJSON(Object... objects) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Object object : objects) {
            stringBuilder.append(toJSON(object));
        }
        return stringBuilder.toString();
    }

    public String toJSON(Object o) {
        return xStreamJSON.toXML(o);
    }

    public String toXML(Object... objects) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Object object : objects) {
            stringBuilder.append(toXML(object));
        }
        return stringBuilder.toString();
    }

    public String toXML(Object o) {
        return xstream.toXML(o).substring("<?xml version=\"1.0\" ?>".length());
    }


}
