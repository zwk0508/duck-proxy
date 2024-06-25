package a;

import com.zwk.proxy.MethodProxy;
import com.zwk.proxy.Proxy;
import com.zwk.proxy.TargetProxy;

import java.lang.reflect.Method;

/**
 * @author zwk
 * @version 1.0
 * @date 2024/6/25 18:08
 */

public class SimpleTest {
    public static void main(String[] args) {
        sample1();
        sample2();
    }

    public static void sample1() {
        MethodProxy p = new MethodProxy() {
            @Override
            public Object invokeMethod(Method method, Object[] args, TargetProxy proxy) throws Throwable {
                Object object = proxy.invokeSuper(args);
                System.out.println(object);//-->target zs
                return proxy.invokeTarget(args);
            }
        };
        Target proxy = Proxy.createProxy(new Target(), p, new Target1());
        System.out.println(proxy.say("zs"));//-->target1 zs
    }

    public static void sample2() {
        MethodProxy p = new MethodProxy() {
            @Override
            public Object invokeMethod(Method method, Object[] args, TargetProxy proxy) throws Throwable {
                return proxy.invokeTarget(args);
            }
        };
        Runnable proxy = Proxy.createProxy(Runnable.class, p, new Target());
        proxy.run();//-->target run
    }
}
