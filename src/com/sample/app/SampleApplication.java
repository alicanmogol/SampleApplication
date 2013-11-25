package com.sample.app;

import com.fererlab.app.BaseApplication;
import com.fererlab.dto.Request;
import com.fererlab.dto.Response;
import com.fererlab.dto.Status;
import com.sample.app.model.Product;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

/**
 * acm | 1/10/13
 */
public class SampleApplication extends BaseApplication {

    private EntityManager em = null;

    @Override
    public void start() {
        try {
            if (em == null) {
                em = Persistence.createEntityManagerFactory("hsqldb-ds").createEntityManager();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Response runApplication(final Request request) {
        // read the cookie to Session object
        request.getSession().fromCookie(this.getClass().getPackage().getName() + "." + this.getClass().getName(),
                "7a8631cb477c052289b4837bd3c6c611cdc97954-9956608b326e04590c57a839c7fe");

        EntityTransaction transaction = em.getTransaction();
        transaction.begin();
        Product product = new Product();
        product.setSerialNumber("XZCZ32423XCZX");
        em.persist(product);
        transaction.commit();

        System.out.println("Generated ID is: " + product.getId());

        // run application
        return Response.create(
                request,
                product.getSerialNumber(),
                Status.STATUS_OK
        );
    }

    @Override
    public void stop() {
        em.close();
    }

}