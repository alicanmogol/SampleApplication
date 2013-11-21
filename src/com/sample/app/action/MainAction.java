package com.sample.app.action;

import com.fererlab.action.BaseAction;
import com.fererlab.collect.DBExec;
import com.fererlab.collect.Exec;
import com.fererlab.collect.HttpExec;
import com.fererlab.dto.*;
import com.sample.app.model.Product;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.Random;

/**
 * acm | 1/21/13
 */
public class MainAction extends BaseAction {

    public Response welcomePost(Request request) {
        return Response.create(request, "POST", Status.STATUS_OK);
    }

    public Response favicon(Request request) {
        return Response.create(request, "POST", Status.STATUS_OK);
    }

    public Response welcome(Request request) {
        ProductCRUDAction productAction = new ProductCRUDAction();

        List<Product> productList = productAction.findAll(new ParamMap<String, Param<String, Object>>());
        getXStream().alias("products", productList.getClass());
        getXStreamJSON().alias("products", productList.getClass());

        Product product = null;
        if (request.getParams().containsKey("id")) {
            product = productAction.find(Long.valueOf(String.valueOf(request.getParams().getValue("id"))));
        }

        // add encoded cookie
        request.getSession().putEncoded("encoded-content", "content here");

        // add encrypted cookie
        try {
            request.getSession().putEncrypt("KEY_HEREKEY_HERE", "encrypted-content", "content here");
        } catch (Exception e) {
            e.printStackTrace();
        }

        //request.getSession().deleteAll();
        request.getSession().put("test", "" + new Random().nextDouble());
        return Response.create(
                request,
                toContent(request, productList, product),
                Status.STATUS_OK
        );
    }

    public Response sayHi(Request request) {
        return Response.create(
                request,
                "Hi " + request.getParams().get("name").getValue(),
                Status.STATUS_OK
        );
    }

    public Response main(final Request request) {
        /*
        //
        Object response = collect();
        return Response.create(request, toContent(request, productList), Status.STATUS_OK);
         */
        List<Object> response = collect(
                2 * 60 * 1000 /*max 2 minutes for these executions to complete*/,
                new DBExec() {
                    @Override
                    public Object run() {
                        ProductCRUDAction productAction = new ProductCRUDAction();
                        return productAction.findAll(new ParamMap<String, Param<String, Object>>());
                    }
                },
                new HttpExec() {
                    @Override
                    public Object run() {
                        try {
                            URL url = new URL("http://78.47.240.166:443/");
                            Object content = url.getContent();
                            StringBuilder stringBuilder = new StringBuilder();
                            if (content instanceof InputStream) {
                                InputStreamReader inputStreamReader = new InputStreamReader((InputStream) content);
                                int c;
                                char ch;
                                while ((c = inputStreamReader.read()) != -1) {
                                    ch = (char) c;
                                    stringBuilder.append(ch);
                                }
                            }
                            return stringBuilder.toString();
                        } catch (Exception e) {
                            return e.getMessage();
                        }
                    }
                },
                new Exec() {
                    @Override
                    public Object run() {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            return e.getMessage();
                        }
                        return "OK";
                    }
                }
        );
        return Response.create(request, toContent(request, response), Status.STATUS_OK);
    }

}