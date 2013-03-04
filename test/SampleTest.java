import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.SqlRow;
import com.avaje.ebean.config.DataSourceConfig;
import com.avaje.ebean.config.ServerConfig;
import com.fererlab.dto.*;
import com.sample.app.SampleApplication;
import com.sample.app.action.ProductCRUDAction;
import com.sample.app.model.Product;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * acm | 1/16/13
 */
public class SampleTest {

    public static void main(String[] args) {
        new SampleTest();
    }

    public SampleTest() {
        runXStreamTest();
        //runPropertiesTest();
        //runActionHandlerTest();
        //runTests();
    }

    private void runXStreamTest() {
        XStream xstream = new XStream(new StaxDriver());
        xstream.autodetectAnnotations(true);
        xstream.setMode(XStream.NO_REFERENCES);

        Product product = new Product();
        product.setId(1L);
        product.setSerialNumber("TEST123");
        String productXML = xstream.toXML(product);
        System.out.println("productXML: " + productXML);
    }

    private void runPropertiesTest() {
        try {

            /*
            request method      ->    uri   ->   className, method
             */
            Map<String, Map<String, Param<String, String>>> executionMap
                    = new HashMap<String, Map<String, Param<String, String>>>();

            String executionMapFile = getClass().getResource(".").getPath() + "ExecutionMap.properties";
            Properties properties = new Properties();
            properties.load(new FileReader(executionMapFile));

            for (String uri : properties.stringPropertyNames()) {
                String methodExecutePart = properties.getProperty(uri).trim();
                methodExecutePart = methodExecutePart.substring(1, methodExecutePart.length());
                String[] methodExecuteParts = methodExecutePart.split("]");
                String[] requestMethods = methodExecuteParts[0].trim().split(",");
                String className = null;
                String methodName = null;
                for (String s : methodExecuteParts[1].trim().split(" ")) {
                    if (!s.trim().isEmpty()) {
                        if (className == null) {
                            className = s;
                        } else {
                            methodName = s;
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
                        executionMap.put(requestMethod, new HashMap<String, Param<String, String>>());
                    }
                    // add this (uri -> className, methodName) to this request method's map
                    executionMap.get(requestMethod).put(uri, new Param<String, String>(className, methodName));
                }
            }

            System.out.println(executionMap);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void runActionHandlerTest() {
        long start = System.currentTimeMillis();
        connectToDB();
        System.out.println("connectToDB ------> " + (System.currentTimeMillis() - start));
        ParamMap<String, Param<String, Object>> headers = new ParamMap<String, Param<String, Object>>();
        ParamMap<String, Param<String, Object>> params = new ParamMap<String, Param<String, Object>>();
        Session session = new Session("");

        params.addParam(new Param<String, Object>(RequestKeys.REQUEST_METHOD.getValue(), "GET"));
        params.addParam(new Param<String, Object>(RequestKeys.URI.getValue(), "/*/get/product"));
        params.addParam(new Param<String, Object>(RequestKeys.PROTOCOL.getValue(), "HTTP/1.1"));

        // add id to params
        params.addParam(new Param<String, Object>("id", "1"));

        // set response type, if no type set, it is xml
        headers.addParam(new Param<String, Object>(RequestKeys.RESPONSE_TYPE.getValue(), "JSON"));

        // create a request
        Request request = new Request(
                params, headers, session
        );

        Response response = new SampleApplication().runApplication(request);
        System.out.println(response);
        System.out.println("SampleApplication ------> " + (System.currentTimeMillis() - start));
    }

    private void runTests() {
        connectToDB();
        runTestCreate();
        runTestFind();
        runTestFindAll();
        runTestDelete();
    }

    private void connectToDB() {

        ServerConfig config = new ServerConfig();
        config.setName("pgtest");

        DataSourceConfig postgresDb = new DataSourceConfig();
        postgresDb.setDriver("org.postgresql.Driver");
        postgresDb.setUsername("acm");
        postgresDb.setPassword("123456");
        postgresDb.setUrl("jdbc:postgresql://127.0.0.1:5432/sample");
        postgresDb.setHeartbeatSql("select count(*) from heart_beat");

        config.setDataSourceConfig(postgresDb);
        config.setDdlGenerate(false);
        config.setDdlRun(false);
        config.setDefaultServer(true);
        config.setRegister(true);
        config.addPackage("com.sample.app.model");

        EbeanServerFactory.create(config);

        List<SqlRow> sqlRows = Ebean.createSqlQuery("select * from fer_et_product").findList();
        for (SqlRow sqlRow : sqlRows) {
            System.out.print("\n");
            for (String key : sqlRow.keySet()) {
                System.out.println(key + ": " + sqlRow.get(key));
            }
        }

    }

    private void runTestCreate() {

        ParamMap<String, Param<String, Object>> paramMap = new ParamMap<String, Param<String, Object>>();
        paramMap.addParam(new Param<String, Object>("id", 1L));
        paramMap.addParam(new Param<String, Object>("serialNumber", "NPR-0001234"));

        ProductCRUDAction action = new ProductCRUDAction();
        Product product = action.create(paramMap);
        System.out.println("product: " + product);

        XStream xstream = new XStream(new StaxDriver());
        xstream.autodetectAnnotations(true);

        String productXml = xstream.toXML(product);
        System.out.println("productXml: " + productXml);

        Product p = (Product) xstream.fromXML(productXml);
        System.out.println("product: " + p.getId());

    }

    private void runTestFind() {

        ProductCRUDAction action = new ProductCRUDAction();
        Product product = action.find(1L);
        System.out.println("product: " + product);

        XStream xstream = new XStream(new StaxDriver());
        xstream.autodetectAnnotations(true);

        String productXml = xstream.toXML(product);
        System.out.println("productXml: " + productXml);

        Product p = (Product) xstream.fromXML(productXml);
        System.out.println("product: " + p.getId());
    }

    private void runTestFindAll() {

        ParamMap<String, Param<String, Object>> paramMap = new ParamMap<String, Param<String, Object>>();
        paramMap.addParam(new Param<String, Object>("id", 2L, ParamRelation.LT));

        ProductCRUDAction action = new ProductCRUDAction();
        List<Product> products = action.findAll(paramMap);
        System.out.println("products: " + products);

        XStream xstream = new XStream(new StaxDriver());
        xstream.autodetectAnnotations(true);
        xstream.alias("beans", com.avaje.ebean.common.BeanList.class);

        String productsXml = xstream.toXML(products);
        System.out.println("productsXml: " + productsXml);

        List<Product> productsFromXml = new ArrayList<Product>();
        List list = (List) xstream.fromXML(productsXml);
        for (Object o : list) {
            productsFromXml.add((Product) o);
        }
        System.out.println("productsFromXml: " + productsFromXml);
        for (Product p : productsFromXml) {
            System.out.println("product: " + p.getId());
        }

    }

    private void runTestDelete() {
        ProductCRUDAction action = new ProductCRUDAction();
        int result = action.delete(1L);
        System.out.println("delete result: " + result);
    }

}
