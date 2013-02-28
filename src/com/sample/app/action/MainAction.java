package com.sample.app.action;

import com.fererlab.action.BaseAction;
import com.fererlab.dto.*;
import com.sample.app.model.Product;
import sun.misc.BASE64Encoder;

import java.util.List;

/**
 * acm | 1/21/13
 */
public class MainAction extends BaseAction {

    public Response welcomePost(Request request) {
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


        request.getSession().putEncoded("encoded-content", "content here");

        try {
            request.getSession().putEncrypt("KEY_HEREKEY_HERE", "encrypted-content", "content here");
        } catch (Exception e) {
            e.printStackTrace();
        }


        request.getSession().put("encoded-content", new BASE64Encoder().encode("content here".getBytes()));

        return Response.create(
                request,
                toContent(request, productList, product),
                Status.STATUS_OK
        );

    }

    public Response main(Request request) {
        ProductCRUDAction productAction = new ProductCRUDAction();
        List<Product> productList = productAction.findAll(new ParamMap<String, Param<String, Object>>());
        return Response.create(request, productAction.toContent(request, productList), Status.STATUS_OK);
    }


}
