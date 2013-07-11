package sample;

/**
 * Created by the ISATeam.
 * User: agbeltran
 * Date: 04/06/2013
 * Time: 14:59
 *
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 */
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.StringMemberValue;

import java.lang.reflect.Method;

/**
 * Created by the ISATeam.
 * User: agbeltran
 * Date: 04/06/2013
 * Time: 14:56
 *
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 */


public class AddRuntimeAnnotation {

    public static void addPersonneNameAnnotationToMethod(String className,String methodName) throws Exception{

        //pool creation
        ClassPool pool = ClassPool.getDefault();
        //extracting the class
        CtClass cc = pool.get(className);//pool.getCtClass(className);
        //looking for the method to apply the annotation on
        CtMethod sayHelloMethodDescriptor = cc.getDeclaredMethod(methodName);
        // create the annotation
        ClassFile ccFile = cc.getClassFile();
        ConstPool constpool = ccFile.getConstPool();
        AnnotationsAttribute attr = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
        Annotation annot = new Annotation("sample.PersonneName", constpool);
        annot.addMemberValue("name", new StringMemberValue("World!! (dynamic annotation)",ccFile.getConstPool()));
        attr.addAnnotation(annot);
        // add the annotation to the method descriptor
        sayHelloMethodDescriptor.getMethodInfo().addAttribute(attr);


        // transform the ctClass to java class
        Class dynamiqueBeanClass = cc.toClass();
        //instanciating the updated class
        SayHelloBean sayHelloBean = (SayHelloBean) dynamiqueBeanClass.newInstance();

        try{

            Method helloMessageMethod = sayHelloBean.getClass().getDeclaredMethod(methodName, String.class);
            //getting the annotation
            PersonneName personneName = (PersonneName) helloMessageMethod.getAnnotation(PersonneName.class);
            System.out.println(sayHelloBean.sayHelloTo(personneName.name()));
        }
        catch(Exception e){
            e.printStackTrace();
        }


    }
    public static void main(String[] args) {

        try {
            AddRuntimeAnnotation.addPersonneNameAnnotationToMethod("sample.SayHelloBean", "sayHelloTo");
        } catch (Exception e) {

            e.printStackTrace();
        }

    }
}