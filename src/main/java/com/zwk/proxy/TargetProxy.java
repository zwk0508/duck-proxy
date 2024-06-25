package com.zwk.proxy;

public interface TargetProxy {
    /**
     * 执行目标对象对应的方法
     */
    Object invokeTarget(Object... args) throws Throwable;

    /**
     * 执行被代理对象对应的方法
     */
    Object invokeSuper(Object... args) throws Throwable;
}
