package com.zwk.proxy;



import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zwk
 * @version 1.0
 * @date 2024/6/25 11:50
 */

public class DispatcherMethodInterceptor implements MethodInterceptor {

    private final Map<Method, MethodHandleInfo> map = new HashMap<>();
    private Object[] targets;
    private Object obj;
    private com.zwk.proxy.MethodProxy methodProxy;

    public DispatcherMethodInterceptor(com.zwk.proxy.MethodProxy methodProxy, Object[] targets) {
        this.targets = targets;
        this.methodProxy = methodProxy;
    }

    public DispatcherMethodInterceptor(Object obj, com.zwk.proxy.MethodProxy methodProxy, Object[] targets) {
        this.obj = obj;
        this.targets = targets;
        this.methodProxy = methodProxy;
    }

    private MethodHandles.Lookup lookup = MethodHandles.lookup();


    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {

        MethodHandleInfo methodHandleInfo = map.get(method);
        if (methodHandleInfo == null) {
            synchronized (map) {
                MethodHandle methodHandle = null;
                Object t = null;
                for (Object target : targets) {
                    MethodType methodType = MethodType.methodType(method.getReturnType(), method.getParameterTypes());
                    try {
                        methodHandle = lookup.findVirtual(target.getClass(), method.getName(), methodType);
                        t = target;
                        break;
                    } catch (Exception e) {
                        // continue;
                    }
                }
                methodHandleInfo = new MethodHandleInfo();
                methodHandleInfo.methodHandle = methodHandle;
                methodHandleInfo.target = t;
                map.put(method, methodHandleInfo);
            }
        }
        MethodHandleInfo mhi = methodHandleInfo;
        return this.methodProxy.invokeMethod(method, objects, new TargetProxy() {
            @Override
            public Object invokeTarget(Object... args) throws Throwable {
                MethodHandle methodHandle = mhi.methodHandle;
                if (methodHandle == null) {
                    throw new UnsupportedOperationException("no such method: " + method.getName());
                }
                methodHandle = methodHandle.bindTo(mhi.target);
                int len = args.length;
                switch (len) {
                    case 0:
                        return methodHandle.invoke();
                    case 1:
                        return methodHandle.invoke(args[0]);
                    case 2:
                        return methodHandle.invoke(args[0], args[1]);
                    case 3:
                        return methodHandle.invoke(args[0], args[1], args[2]);
                    case 4:
                        return methodHandle.invoke(args[0], args[1], args[2], args[3]);
                    case 5:
                        return methodHandle.invoke(args[0], args[1], args[2], args[3], args[4]);
                    case 6:
                        return methodHandle.invoke(args[0], args[1], args[2], args[3], args[4], args[5]);
                    case 7:
                        return methodHandle.invoke(args[0], args[1], args[2], args[3], args[4], args[5], args[6]);
                    case 8:
                        return methodHandle.invoke(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7]);
                    case 9:
                        return methodHandle.invoke(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8]);
                    case 10:
                        return methodHandle.invoke(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9]);
                }

                return invokeMethodHandle(methodHandle, args);
            }

            @Override
            public Object invokeSuper(Object... args) throws Throwable {
                if (obj != null) {
                    return method.invoke(obj, args);
                }
                return methodProxy.invokeSuper(o, args);
            }
        });
    }


    private static class MethodHandleInfo {
        MethodHandle methodHandle;
        Object target;
    }


    private final Map<Integer, MethodHandleInvoker> invokerMap = new HashMap<>();

    public interface MethodHandleInvoker {
        Object invoke(MethodHandle mhi, Object... args);
    }

    private Object invokeMethodHandle(MethodHandle mhi, Object... args) {
        int length = args.length;
        MethodHandleInvoker invoker = invokerMap.get(length);
        if (invoker == null) {
            synchronized (invokerMap) {
                invoker = invokerMap.get(length);
                if (invoker == null) {
                    invoker = generateInvoker(length);
                    invokerMap.put(length, invoker);
                }
            }
        }
        return invoker.invoke(mhi, args);
    }

    private MethodHandleInvoker generateInvoker(int length) {

        ClassWriter writer = new ClassWriter(0);

        writer.visit(
                Opcodes.V1_8,
                Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL,
                "com/zwk/proxy/MethodHandleInvoker_$_" + length,
                null,
                "java/lang/Object",
                new String[]{Type.getInternalName(MethodHandleInvoker.class)}
        );
        MethodVisitor mv = writer.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
        mv = writer.visitMethod(
                Opcodes.ACC_PUBLIC,
                "invoke",
                "(Ljava/lang/invoke/MethodHandle;[Ljava/lang/Object;)Ljava/lang/Object;",
                null,
                null
        );
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        StringBuilder descriptor = new StringBuilder("(");
        for (int i = 0; i < length; i++) {
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitIntInsn(Opcodes.BIPUSH, i);
            mv.visitInsn(Opcodes.AALOAD);
            descriptor.append("Ljava/lang/Object;");
        }
        descriptor.append(")Ljava/lang/Object;");
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/invoke/MethodHandle", "invoke", descriptor.toString(), false);
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(length + 3, 3);
        mv.visitEnd();
        byte[] byteArray = writer.toByteArray();

        try {
            Class<?> clazz = (Class<?>) defineClass.invoke(Thread.currentThread().getContextClassLoader(), byteArray, 0, byteArray.length);
            return (MethodHandleInvoker) clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Method defineClass;

    static {
        try {
            defineClass = ClassLoader.class.getDeclaredMethod("defineClass", byte[].class, int.class, int.class);
            defineClass.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

}
