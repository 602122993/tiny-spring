package com.xiaoazhai;

import junit.framework.TestCase;
import org.junit.Test;
import org.springframework.beans.BeanDefinition;
import org.springframework.beans.DefaultApplicationContext;

public class ApplicationContextTest  extends TestCase {


    @Test
    public  void testGetBean() {
        DefaultApplicationContext defaultApplicationContext = new DefaultApplicationContext();
        defaultApplicationContext.registerBean("person",new Person());
        Person person = (Person) defaultApplicationContext.getBean("person");
        person.sayHello();
    }


    @Test
    public void testBeanDefinition() throws InstantiationException, IllegalAccessException {
        DefaultApplicationContext defaultApplicationContext = new DefaultApplicationContext();
        defaultApplicationContext.registerBeanDefinition("person",Person.class);
        Person person= (Person) defaultApplicationContext.getBean("person");
        person.sayHello();
    }


    @Test
    public void testAutoload(){
        DefaultApplicationContext defaultApplicationContext= new DefaultApplicationContext();
        defaultApplicationContext.init(ApplicationContextTest.class);
        Person person= (Person) defaultApplicationContext.getBean("person");
        person.sayHello();
    }



//    public static void main(String[] args) throws  Exception {
//        ThreadFactory factory = Thread.ofVirtual().name("route-",0).factory();
//        try (var executor = Executors.newThreadPerTaskExecutor(factory)) {
//            IntStream.range(0,20).forEach(i->executor.submit(()->{
//                System.out.println(Thread.currentThread()+"wo de ming zi shi "+i);
//                try {
//                    Thread.sleep(1000L);
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
//            }));
//        }
////        var taskList = new ArrayList<Future>();
////        for (int i = 0; i <30 ; i++) {
////            int finalI = i;
////            var first = executor.submit(() -> {
////                System.out.println("我是第"+ finalI +"个任务");
////                try {
////                    Thread.sleep(finalI *100L);
////                } catch (InterruptedException e) {
////                    throw new RuntimeException(e);
////                }
////                System.out.println(Thread.currentThread());
////            });
////            taskList.add(first);
////        }
////        taskList.forEach(task-> {
////            try {
////                task.get();
////            } catch (InterruptedException e) {
////                throw new RuntimeException(e);
////            } catch (ExecutionException e) {
////                throw new RuntimeException(e);
////            }
////        });
//
//    }
}
