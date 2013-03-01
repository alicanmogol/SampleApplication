package com.fererlab.dto;

import java.io.Serializable;

/**
 * acm 10/15/12
 */
public class Response implements Serializable {

    private ParamMap<String, Param<String, Object>> headers;
    private Session session;
    private Status status;
    private String content;

    public Response(ParamMap<String, Param<String, Object>> headers, Session session, Status status, String content) {
        this.headers = headers;
        this.session = session;
        this.status = status;
        this.content = content;
    }

    public ParamMap<String, Param<String, Object>> getHeaders() {
        return headers;
    }

    public void setHeaders(ParamMap<String, Param<String, Object>> headers) {
        this.headers = headers;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "Response{" +
                "headers=" + headers +
                ", session=" + session +
                ", status=" + status +
                ", content='" + content + '\'' +
                '}';
    }

    /*
    static create response method
     */

    public static Response create(final Request request, String content, Status status) {
        return new Response(
                new ParamMap<String, Param<String, Object>>(),
                request.getSession(),
                status,
                content
        );
    }

}
