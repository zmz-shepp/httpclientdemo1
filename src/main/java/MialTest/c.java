package MialTest;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.Test;

public class c {

    @Test
       public void a(){
           System.out.println("c1");
       }
  @Test
    public void b(){
        System.out.println("c2");
    }

    @Test
    public void c(){
        System.out.println("c3");
    }

    @Test
    public void d(){
        System.out.println("c4");
    }


    @AfterSuite
    public  void s(){
        System.out.println("CCCCCCCCC执行完成");
    }



}
