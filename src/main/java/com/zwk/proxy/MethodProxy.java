package com.zwk.proxy;

import java.lang.reflect.Method;

public interface MethodProxy {
    Object invokeMethod(Method method, Object[] args, TargetProxy proxy) throws Throwable;
}
