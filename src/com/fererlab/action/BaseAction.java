package com.fererlab.action;

import com.fererlab.dto.Request;
import com.fererlab.dto.RequestKeys;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import com.thoughtworks.xstream.io.xml.StaxDriver;

/**
 * acm | 1/23/13
 */
public class BaseAction implements Action {

    private XStream xStreamJSON = new XStream(new JettisonMappedXmlDriver());
    private XStream xstream = new XStream(new StaxDriver());

    public XStream getXStreamJSON() {
        return xStreamJSON;
    }

    public XStream getXStream() {
        return xstream;
    }

    public BaseAction() {
        xstream.autodetectAnnotations(true);
        xStreamJSON.autodetectAnnotations(true);
    }

    public String toContent(Request request, Object... o) {
        StringBuilder stringBuilder = new StringBuilder();
        if (request.getHeaders().containsKey(RequestKeys.RESPONSE_TYPE.getValue())
                && ((String) request.getHeaders().get(RequestKeys.RESPONSE_TYPE.getValue()).getValue()).equalsIgnoreCase("JSON")) {
            stringBuilder.append(toJSON(o));
        } else {
            for (Object object : o) {
                stringBuilder.append(toXML(object));
            }
        }
        return stringBuilder.toString();
    }

    public String toResponseContent(Request request, String templateName, String content) {

        if (request.getHeaders().containsKey(RequestKeys.RESPONSE_TYPE.getValue())
                && ((String) request.getHeaders().get(RequestKeys.RESPONSE_TYPE.getValue()).getValue()).equalsIgnoreCase("JSON")) {
            return content;
        } else {
            StringBuilder responseContent = new StringBuilder();
            responseContent.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            responseContent.append("<?xml-stylesheet type=\"text/xsl\" href=\"").append(templateName).append(".xsl\"?>");
            responseContent.append("<root>");
            responseContent.append(content);
            responseContent.append("</root>");
            return responseContent.toString();
        }
    }

    public String toJSON(Object o) {
        return xStreamJSON.toXML(o);
    }

    public String toXML(Object o) {
        return xstream.toXML(o).substring("<?xml version=\"1.0\" ?>".length());
    }


}
