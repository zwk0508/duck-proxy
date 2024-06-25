package com.zwk.proxy;


import net.sf.cglib.proxy.Enhancer;

/**
 * @author zwk
 * @version 1.0
 * @date 2024/6/25 13:26
 */
@SuppressWarnings("unchecked")
public class Proxy {
    public static <T> T createProxy(T obj, MethodProxy methodProxy, Object... targets) {
        DispatcherMethodInterceptor methodInterceptor = new DispatcherMethodInterceptor(obj, methodProxy, targets);
        return (T) Enhancer.create(obj.getClass(), methodInterceptor);
    }

    public static <T> T createProxy(Class<T> clazz, MethodProxy methodProxy, Object... targets) {
        DispatcherMethodInterceptor methodInterceptor = new DispatcherMethodInterceptor(methodProxy, targets);
        return (T) Enhancer.create(clazz, methodInterceptor);
    }
}
