import org.junit.Test;
import org.springframework.beans.DefaultApplicationContext;

public class ApplicationContextTest {


    @Test
    public  void testGetBean() {
        DefaultApplicationContext defaultApplicationContext = new DefaultApplicationContext();
        defaultApplicationContext.registerBean("person",new Person());
        Person person = (Person) defaultApplicationContext.getBean("person");
        person.sayHello();
    }



    public class Person {
        public void sayHello(){
            System.out.println("Hello World!");
        }
    }

}
