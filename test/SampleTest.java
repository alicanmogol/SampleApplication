import com.sample.app.model.Product;

import java.lang.reflect.Field;

/**
 * acm | 1/16/13
 */
public class SampleTest {

    public static void main(String[] args) {
        new SampleTest();
    }

    public SampleTest() {
        runTypeTest();
    }

    private void runTypeTest() {
        for (Field field : Product.class.getDeclaredFields()) {
            System.out.println("field: " + field.getDeclaringClass() + ", " + field.getClass() + ", " + field.getGenericType() + ", " + field.getType());
        }
    }


}
