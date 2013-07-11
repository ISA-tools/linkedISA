package sample;

import java.lang.reflect.Method;

/**
 * Created by the ISATeam.
 * User: agbeltran
 * Date: 04/06/2013
 * Time: 15:00
 *
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 */
public class SayHelloBean {

    private static final  String HELLO_MSG = "Hello ";

    @PersonneName(name="World !! (simple annotation)")
    public String sayHelloTo(String name){
        return HELLO_MSG+name;
    }

    public static void main(String[] args) {


        try{
            //instanciate the bean
            SayHelloBean simpleBean  = new SayHelloBean();
            //get the method descriptor through reflection
            Method helloMessageMethod = simpleBean.getClass().getDeclaredMethod("sayHelloTo", String.class);
            //scan the annotation
            PersonneName mySimpleAnnotation = (PersonneName) helloMessageMethod.getAnnotation(PersonneName.class);

            System.out.println(simpleBean.sayHelloTo(mySimpleAnnotation.name()));
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}
