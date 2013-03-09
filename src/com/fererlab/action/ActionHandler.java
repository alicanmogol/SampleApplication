package com.fererlab.action;

import com.fererlab.aa.AuthenticationAuthorizationMap;
import com.fererlab.aa.ExecutionMap;
import com.fererlab.dto.*;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * acm | 1/16/13
 */
public class ActionHandler {

    private ExecutionMap executionMap = new ExecutionMap();
    private AuthenticationAuthorizationMap authenticationAuthorizationMap = new AuthenticationAuthorizationMap();

    public ActionHandler(URL executionMapFile, URL authenticationAuthorizationMapFile) {
        executionMap.readUriExecutionMap(executionMapFile);
        authenticationAuthorizationMap.readAuthenticationAuthorizationMap(authenticationAuthorizationMapFile);
    }

    public Response runAction(final Request request) {

        // prepare method and action class
        Method method = null;
        Class<?> actionClass = null;

        // get the request Method(GET, POST etc.) and URI
        String requestMethod = request.getParams().get(RequestKeys.REQUEST_METHOD.getValue()).getValue().toString();
        String requestURI = request.getParams().get(RequestKeys.URI.getValue()).getValue().toString();

        // URI starting with /_/ indicates it is a resource but not an action
        if (requestURI.startsWith("/_/")) {

            // remove the first 3 chars, {/,_,/}
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
        if (executionMap.containsKey(requestMethod) || executionMap.containsKey("*")) {

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
                        // set the current uri to requestURI
                        requestURI = uri;
                        //   com.sample.app.action.MainAction, welcome
                        Param<String, String> executionParam = uriExecutionMap.get(requestURI);
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
                        // set the current uri to requestURI
                        requestURI = "/";
                        //   com.sample.app.action.MainAction, main
                        Param<String, String> executionParam = uriExecutionMap.get(requestURI);
                        //   com.sample.app.action.MainAction
                        className = executionParam.getKey();
                        //   main
                        methodName = executionParam.getValue();
                        //   main
                        templateName = executionParam.getValueSecondary();
                    }
                }
            }


            // check the AuthenticationAuthorizationMap contains requestMethod
            if (authenticationAuthorizationMap.containsKey(requestMethod)
                    || authenticationAuthorizationMap.containsKey("*")) {

                // find the user's group names
                String[] groupNamesCommaSeparated = new String[]{"admin"};
                if (request.getSession().containsKey(SessionKeys.GROUP_NAMES.getValue())) {
                    groupNamesCommaSeparated = ((String) request.getSession().get(SessionKeys.GROUP_NAMES.getValue())).split(",");
                }

                // authorization flag for user's group
                boolean userAuthorized = false;

                // for this http request method, like GET, POST or PUT
                Map<String, List<String>> uriGroupNames = authenticationAuthorizationMap.get(requestMethod);

                // check this requested uri has any authentication/authorization
                if (uriGroupNames.containsKey(requestURI)) {

                    // the user does not have any groups but this uri needs some
                    // return STATUS_UNAUTHORIZED and redirect
                    if (groupNamesCommaSeparated == null) {
                        ParamMap<String, Param<String, Object>> stringParamParamMap = new ParamMap<String, Param<String, Object>>();
                        Object hostName = request.getHeaders().getValue(RequestKeys.HOST_NAME.getValue());
                        Object hostPort = request.getHeaders().getValue(RequestKeys.HOST_PORT.getValue());
                        Object applicationUri = request.getParams().getValue(RequestKeys.APPLICATION_URI.getValue());
                        String redirectUrl = hostName + ":" + (hostPort != null ? hostPort : "") + "/" + applicationUri;
                        stringParamParamMap.addParam(new Param<String, Object>("Refresh", "0; url=http://" + redirectUrl));
                        return new Response(
                                stringParamParamMap,
                                request.getSession(),
                                Status.STATUS_UNAUTHORIZED,
                                ""
                        );
                    }

                    // authorized groups for this uri
                    List<String> authorizedGroups = uriGroupNames.get(requestURI);

                    // user has at least one group, it means user is authenticated
                    // if the authorizedGroups contains (*) it means any authenticated client may request this uri
                    if (authorizedGroups.contains("*")) {
                        userAuthorized = true;
                    }

                    // find the required group names
                    else {
                        for (String userGroupName : groupNamesCommaSeparated) {
                            if (authorizedGroups.contains(userGroupName)) {
                                userAuthorized = true;
                                break;
                            }
                        }
                    }


                    // if the user is not authorized return STATUS_UNAUTHORIZED and redirect
                    if (!userAuthorized) {
                        return new Response(
                                new ParamMap<String, Param<String, Object>>() {{
                                    Object hostName = request.getParams().getValue(RequestKeys.HOST_NAME.getValue());
                                    Object hostPort = request.getParams().getValue(RequestKeys.HOST_PORT.getValue());
                                    Object applicationUri = request.getParams().getValue(RequestKeys.APPLICATION_URI.getValue());
                                    String redirectUrl = hostName + ":" + (hostPort != null ? hostPort : "") + "/" + applicationUri;
                                    addParam(new Param<String, Object>("Refresh", "0; url=http://" + redirectUrl));
                                }},
                                request.getSession(),
                                Status.STATUS_UNAUTHORIZED,
                                ""
                        );
                    }
                }


                // check for the [*] all http method request map
                if (!userAuthorized) {

                    // [*] http request method
                    uriGroupNames = authenticationAuthorizationMap.get("*");

                    // check this requested uri has any authentication/authorization
                    if (uriGroupNames.containsKey(requestURI)) {

                        // the user does not have any groups but this uri needs some
                        // return STATUS_UNAUTHORIZED and redirect
                        if (groupNamesCommaSeparated == null) {
                            return new Response(
                                    new ParamMap<String, Param<String, Object>>() {{
                                        Object hostName = request.getParams().getValue(RequestKeys.HOST_NAME.getValue());
                                        Object hostPort = request.getParams().getValue(RequestKeys.HOST_PORT.getValue());
                                        Object applicationUri = request.getParams().getValue(RequestKeys.APPLICATION_URI.getValue());
                                        String redirectUrl = hostName + ":" + (hostPort != null ? hostPort : "") + "" + applicationUri;
                                        addParam(new Param<String, Object>("Refresh", "0; url=http://" + redirectUrl));
                                    }},
                                    request.getSession(),
                                    Status.STATUS_UNAUTHORIZED,
                                    ""
                            );
                        }

                        // find the required group names
                        List<String> authorizedGroups = uriGroupNames.get(requestURI);
                        for (String userGroupName : groupNamesCommaSeparated) {
                            if (authorizedGroups.contains(userGroupName)) {
                                userAuthorized = true;
                                break;
                            }
                        }

                        // if the user is not authorized return STATUS_UNAUTHORIZED and redirect
                        if (!userAuthorized) {
                            return new Response(
                                    new ParamMap<String, Param<String, Object>>() {{
                                        Object hostName = request.getParams().getValue(RequestKeys.HOST_NAME.getValue());
                                        Object hostPort = request.getParams().getValue(RequestKeys.HOST_PORT.getValue());
                                        Object applicationUri = request.getParams().getValue(RequestKeys.APPLICATION_URI.getValue());
                                        String redirectUrl = hostName + ":" + (hostPort != null ? hostPort : "") + "/" + applicationUri;
                                        addParam(new Param<String, Object>("Refresh", "0; url=http://" + redirectUrl));
                                    }},
                                    request.getSession(),
                                    Status.STATUS_UNAUTHORIZED,
                                    ""
                            );
                        }
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
