package com.sample.app.action;

import com.fererlab.action.BaseAction;
import com.fererlab.dto.*;
import com.sample.app.model.Product;

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

        Product product = productAction.find(Long.valueOf(String.valueOf(request.getParams().getValue("id"))));

        String responseContent;
        if (request.getParams().containsKey(RequestKeys.RESPONSE_TEMPLATE.getValue())) {
            responseContent = toResponseContent(
                    request,
                    String.valueOf(request.getParams().getValue(RequestKeys.RESPONSE_TEMPLATE.getValue())),
                    toContent(request, productList, product)
            );
        } else {
            responseContent = toContent(request, productList, product);
        }
        return Response.create(
                request,
                responseContent,
                Status.STATUS_OK
        );

    }

    public Response main(Request request) {
        return Response.create(request, "main page", Status.STATUS_OK);
    }


}
