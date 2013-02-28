package com.fererlab.action;

import com.fererlab.dto.*;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * acm | 1/16/13
 */
public class ActionHandler {

    private Map<String, Map<String, Param<String, String>>> executionMap = new TreeMap<String, Map<String, Param<String, String>>>();

    public ActionHandler(URL executionmapFile) {
        readUriExecutionMap(executionmapFile);
    }

    private void readUriExecutionMap(URL file) {
        try {

            /*
            request method      ->    uri                ->   className, method
            GET                 ->    /welcome           ->   com.sample.app.action.MainAction, welcome       welcomeTemplate
            POST                ->    /product/details   ->   com.sample.app.action.ProductCRUDAction, details
             */

            // comparator for the keys
            Comparator<String> stringComparator = new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    if (o1.length() < o2.length()) {
                        return 1;
                    } else if (o1.length() > o2.length()) {
                        return -1;
                    }
                    return o1.compareTo(o2);
                }
            };

            executionMap = new TreeMap<String, Map<String, Param<String, String>>>(stringComparator);

            // read executionmap.properties
            if (file == null) {
                file = getClass().getClassLoader().getResource("executionmap.properties");
            }
            Properties properties = new Properties();
            if (file != null) {
                properties.load(file.openStream());
            }

            //      /welcome            [GET,POST]                       com.sample.app.action.MainAction            welcome        welcome
            for (String uri : properties.stringPropertyNames()) {
                // uri  ->  /welcome
                uri = uri.trim();

                // methodExecutePart    ->          [GET,POST]                       com.sample.app.action.MainAction            welcome       welcome
                String methodExecutePart = properties.getProperty(uri).trim();
                methodExecutePart = methodExecutePart.substring(1, methodExecutePart.length());
                /*
                methodExecuteParts
                [0]  GET,POST
                [1]  com.sample.app.action.MainAction            welcome          welcome
                 */
                String[] methodExecuteParts = methodExecutePart.split("]");

                /*
                 requestMethods
                 [0] GET
                 [1] POST
                  */
                String[] requestMethods = methodExecuteParts[0].trim().split(",");
                String className = null;
                String methodName = null;
                String templateName = null;
                for (String s : methodExecuteParts[1].trim().split(" ")) {
                    s = s.trim();
                    if (!s.isEmpty()) {
                        if (className == null) {
                            // com.sample.app.action.MainAction
                            className = s;
                        } else if (methodName == null) {
                            // welcome
                            methodName = s;
                        } else {
                            // welcome
                            templateName = s;
                            break;
                        }
                    }
                }

                // requestMethods are like GET, POST, DELETE, PUT etc.
                for (String requestMethod : requestMethods) {
                    // trim the request method string, there may be some empty strings coming from properties entry
                    requestMethod = requestMethod.trim();
                    // if there is not entry until now, put an empty HashMap
                    if (!executionMap.containsKey(requestMethod)) {
                        executionMap.put(requestMethod, new TreeMap<String, Param<String, String>>(stringComparator));
                    }
                    // add this (uri -> className, methodName) to this request method's map
                    executionMap.get(requestMethod).put(uri, new Param<String, String>(className, methodName, templateName));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Response runAction(Request request) {

        // prepare method and action class
        Method method = null;
        Class<?> actionClass = null;

        // get the request Method(GET, POST etc.) and URI
        String requestMethod = request.getParams().get(RequestKeys.REQUEST_METHOD.getValue()).getValue().toString();
        String requestURI = request.getParams().get(RequestKeys.URI.getValue()).getValue().toString();

        // URI starting with /_/ indicates it is a resource but not an action
        if (requestURI.startsWith("/_/")) {

            requestURI = requestURI.substring(3);

            // request URI is either one of these; xsl, css, js, image, file,
            String[] foldersAndFile = requestURI.split("/", 2);
            String xslTemplate = requestURI.substring((foldersAndFile[0] + "/").length());
            FileContentHandler fileContentHandler = new FileContentHandler();
            String templateContent = fileContentHandler.getContent(foldersAndFile[0], xslTemplate);
            return new Response(
                    new ParamMap<String, Param<String, Object>>(),
                    request.getSession(),
                    Status.STATUS_OK,
                    templateContent
            );
        }

        // remove the forward slash if there is any
        if (requestURI.endsWith("/")) {
            requestURI = requestURI.substring(0, requestURI.length() - 1);
        }

        // define the className and methodName here
        String className = null;
        String methodName = null;
        String templateName = null;

        // first check for the exact match
        // requestMethod    ->      GET
        if (executionMap.containsKey(requestMethod)) {
            // uriExecutionMap contains all the URI -> execution mapping for this request method
            Map<String, Param<String, String>> uriExecutionMap = executionMap.get(requestMethod);
            // requestURI           /welcome        or       /welcome/
            if (uriExecutionMap.containsKey(requestURI) || uriExecutionMap.containsKey(requestURI + "/")) {
                //   com.sample.app.action.MainAction, welcome
                Param<String, String> executionParam = uriExecutionMap.get(requestURI);
                //   com.sample.app.action.MainAction
                className = executionParam.getKey();
                //   welcome
                methodName = executionParam.getValue();
                //   welcome
                templateName = executionParam.getValueSecondary();
            }

            // if not found, try the starts with
            if (className == null) {

                for (String uri : uriExecutionMap.keySet()) {
                    if (uri.startsWith(requestURI) || requestURI.startsWith(uri)) {
                        //   com.sample.app.action.MainAction, welcome
                        Param<String, String> executionParam = uriExecutionMap.get(uri);
                        //   com.sample.app.action.MainAction
                        className = executionParam.getKey();
                        //   welcome
                        methodName = executionParam.getValue();
                        //   welcome
                        templateName = executionParam.getValueSecondary();
                        break;
                    }
                }
            }

            // if still not found, check the default
            if (className == null) {
                if (executionMap.containsKey("*")) {
                    uriExecutionMap = executionMap.get("*");
                    if (uriExecutionMap.containsKey("/")) {
                        //   com.sample.app.action.MainAction, main
                        Param<String, String> executionParam = uriExecutionMap.get("/");
                        //   com.sample.app.action.MainAction
                        className = executionParam.getKey();
                        //   main
                        methodName = executionParam.getValue();
                        //   main
                        templateName = executionParam.getValueSecondary();
                    }
                }
            }


        }


        // set Class and Method
        try {
            actionClass = Class.forName(className);
            method = actionClass.getMethod(methodName, Request.class);
        } catch (Exception e) {
            e.printStackTrace();
        }


        if (method != null) {
            try {
                // add template to the request if template exists
                if (templateName != null) {
                    request.getParams().addParam(new Param<String, Object>(RequestKeys.RESPONSE_TEMPLATE.getValue(), templateName));
                }

                // return the response
                return (Response) method.invoke(actionClass.newInstance(), request);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // something went wrong, return an error message
        return new Response(
                new ParamMap<String, Param<String, Object>>(),
                request.getSession(),
                Status.STATUS_SERVICE_UNAVAILABLE,
                ""
        );

    }

}
