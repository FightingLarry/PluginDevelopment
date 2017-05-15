//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.tcl.dc.utils;

import android.text.TextUtils;
import com.tcl.dc.PLog;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public final class InvokeUtil {
    private static final int INSTANCE_DENIED = 0;
    private static final int INSTANCE_OK = 1;
    private static final int INSTANCE_CONV = 2;
    private static final int METHOD_MATCH_NONE = 0;
    private static final int METHOD_MATCH_PUBLIC = 1;
    private static final int METHOD_MATCH_PARAMS_TYPE = 2;
    private static final int METHOD_MATCH_STRICTLY = 3;

    public InvokeUtil() {}

    public static Object invokeMethod(Object o, String methodName, Object... params)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = matchMethod(o.getClass(), methodName, params);
        if (method == null) {
            throw new NoSuchMethodException(
                    "class " + o.getClass().getCanonicalName() + " cannot find method " + methodName);
        } else {
            Object out = method.invoke(o, params);
            return out;
        }
    }

    public static Object invokeStaticMethod(Class clz, String methodName, Object... params)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = matchMethod(clz, methodName, params);
        if (method == null) {
            throw new NoSuchMethodException("class " + clz.getCanonicalName() + " cannot find method " + methodName);
        } else {
            Object out = method.invoke((Object) null, params);
            return out;
        }
    }

    private static Class getObjectClass(Object o) {
        Class clz = o.getClass();
        Class inner = wrappedClass(clz);
        return inner != null && inner.isPrimitive() ? inner : clz;
    }

    public static boolean isWrapClass(Class clz) {
        Class inner = wrappedClass(clz);
        return inner != null ? inner.isPrimitive() : false;
    }

    public static Class wrappedClass(Class clz) {
        try {
            return (Class) clz.getField("TYPE").get((Object) null);
        } catch (Exception var2) {
            return null;
        }
    }

    public static Method[] methodsForName(Class clz, String name) {
        Method[] methods = clz.getDeclaredMethods();
        if (methods != null && methods.length != 0) {
            List<Method> out = new ArrayList();
            Method[] var4 = methods;
            int var5 = methods.length;

            for (int var6 = 0; var6 < var5; ++var6) {
                Method method = var4[var6];
                if (method.getName().equals(name)) {
                    out.add(method);
                }
            }

            if (out.size() == 0) {
                return null;
            } else {
                return (Method[]) out.toArray(new Method[0]);
            }
        } else {
            return null;
        }
    }

    public static Method matchMethod(Class clz, String name, Object... params) {
        Method[] methods = methodsForName(clz, name);
        if (methods != null && methods.length != 0) {
            Method found = null;
            int maxMatch = 0;
            Method[] var6 = methods;
            int var7 = methods.length;

            for (int var8 = 0; var8 < var7; ++var8) {
                Method method = var6[var8];
                int v = matchMethodParameterTypes(method, params);
                if (v > maxMatch) {
                    maxMatch = v;
                    found = method;
                }
            }

            if (maxMatch == 0) {
                return null;
            } else {
                if ((maxMatch & 1) == 0) {
                    found.setAccessible(true);
                }

                return found;
            }
        } else {
            return null;
        }
    }

    private static int instanceOf(Object o, Class<?> clz) {
        if (o == null) {
            return clz.isPrimitive() ? 0 : 1;
        } else if (!clz.isPrimitive()) {
            return clz.isInstance(o) ? 1 : 0;
        } else if (clz == Void.TYPE) {
            return 0;
        } else {
            Class wclz = wrappedClass(o.getClass());
            return wclz == null ? 0 : (wclz == clz ? 1 : (clz == Long.TYPE && wclz == Integer.TYPE
                    ? 2
                    : (clz != Double.TYPE || wclz != Float.TYPE && wclz != Long.TYPE && wclz != Integer.TYPE
                            ? (clz == Float.TYPE && wclz == Integer.TYPE
                                    ? 2
                                    : (clz != Integer.TYPE
                                            || wclz != Byte.TYPE && wclz != Short.TYPE && wclz != Character.TYPE
                                                    ? 0
                                                    : 2))
                            : 2)));
        }
    }

    private static int matchMethodParameterTypes(Method method, Object... params) {
        Class[] types = method.getParameterTypes();
        int tlen = ArrayUtils.length(types);
        int plen = ArrayUtils.length(params);
        int value = 0;
        if (tlen != plen) {
            return 0;
        } else {
            if (plen > 0) {
                int[] pos = new int[plen];
                int size = 0;

                int v;
                for (int i = 0; i < plen; ++i) {
                    Object p = params[i];
                    v = instanceOf(p, types[i]);
                    if (v == 0) {
                        return 0;
                    }

                    if (v != 1) {
                        pos[size++] = i;
                    }
                }

                if (size > 0) {
                    int[] var15 = pos;
                    int var16 = pos.length;

                    for (v = 0; v < var16; ++v) {
                        int index = var15[v];
                        Object p = params[index];
                        if (p instanceof Number) {
                            Number n = (Number) p;
                            if (types[index] == Integer.TYPE) {
                                params[index] = Integer.valueOf(n.intValue());
                            } else if (types[index] == Long.TYPE) {
                                params[index] = Long.valueOf(n.longValue());
                            } else if (types[index] == Double.TYPE) {
                                params[index] = Double.valueOf(n.doubleValue());
                            } else if (types[index] == Float.TYPE) {
                                params[index] = Float.valueOf(n.floatValue());
                            } else if (types[index] == Byte.TYPE) {
                                params[index] = Byte.valueOf(n.byteValue());
                            } else if (types[index] == Short.TYPE) {
                                params[index] = Short.valueOf(n.shortValue());
                            }
                        } else if (p instanceof Character) {
                            char c = ((Character) p).charValue();
                            if (types[index] == Integer.TYPE) {
                                params[index] = Integer.valueOf(c);
                            } else if (types[index] == Long.TYPE) {
                                params[index] = Long.valueOf((long) c);
                            } else if (types[index] == Byte.TYPE) {
                                params[index] = Byte.valueOf((byte) c);
                            } else if (types[index] == Short.TYPE) {
                                params[index] = Short.valueOf((short) c);
                            }
                        }
                    }
                }
            }

            value = value | 2;
            if (Modifier.isPublic(method.getModifiers())) {
                value |= 1;
            }

            return value;
        }
    }

    public static Object valueOfField(Object o, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        if (TextUtils.isEmpty(fieldName)) {
            throw new IllegalArgumentException("param fieldName is empty");
        } else {
            Class clz = o.getClass();
            Field field = fieldByNameRecursive(clz, fieldName);
            if (!Modifier.isPublic(field.getModifiers())) {
                field.setAccessible(true);
            }

            return field.get(o);
        }
    }

    public static Object valueOfStaticField(Class clz, String fieldName)
            throws NoSuchFieldException, IllegalAccessException {
        if (TextUtils.isEmpty(fieldName)) {
            throw new IllegalArgumentException("param fieldName is empty");
        } else {
            Field field = fieldByNameRecursive(clz, fieldName);
            if (!Modifier.isPublic(field.getModifiers())) {
                field.setAccessible(true);
            }

            return field.get((Object) null);
        }
    }

    public static void setValueOfField(Object o, String fieldName, Object value)
            throws NoSuchFieldException, IllegalAccessException {
        if (TextUtils.isEmpty(fieldName)) {
            throw new IllegalArgumentException("param fieldName is empty");
        } else {
            Class clz = o.getClass();
            Field field = fieldByNameRecursive(clz, fieldName);
            if (!Modifier.isPublic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())) {
                field.setAccessible(true);
            }

            field.set(o, value);
        }
    }

    public static void setStaticValueOfField(Class clz, String fieldName, Object value)
            throws NoSuchFieldException, IllegalAccessException {
        if (TextUtils.isEmpty(fieldName)) {
            throw new IllegalArgumentException("param fieldName is empty");
        } else {
            Field field = fieldByNameRecursive(clz, fieldName);
            if (!Modifier.isPublic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())) {
                field.setAccessible(true);
            }

            field.set((Object) null, value);
        }
    }

    public static Field fieldByNameRecursive(Class clz, String fieldName) throws NoSuchFieldException {
        Class target = clz;

        while (!target.equals(Object.class)) {
            try {
                Field field = target.getDeclaredField(fieldName);
                return field;
            } catch (NoSuchFieldException var4) {
                target = clz.getSuperclass();
            }
        }

        throw new NoSuchFieldException(fieldName);
    }

    public static void printAllFields(Class clz) {
        Class target = clz;
        String prefix = "===";
        int depth = 1;
        String p = null;

        while (true) {
            Field[] fields;
            do {
                do {
                    if (target.equals(Object.class)) {
                        return;
                    }

                    fields = target.getDeclaredFields();
                    p = StringUtils.repeat(prefix, depth);
                    PLog.i("%s%s Fields:", new Object[] {p, target.getName()});
                } while (fields == null);
            } while (fields.length <= 0);

            for (int i = 0; i < fields.length; ++i) {
                PLog.i("%s Field[%d]: %s%s %s", new Object[] {p, Integer.valueOf(i),
                        modifiers(fields[i].getModifiers()), className(fields[i].getType()), fields[i].getName()});
            }
        }
    }

    private static String className(Class clz) {
        if (clz.isPrimitive()) {
            Class s = wrappedClass(clz);
            return s != null ? s.getName() : null;
        } else {
            return clz.getName();
        }
    }

    private static String modifiers(int modifiers) {
        StringBuilder sb = new StringBuilder();
        if (Modifier.isPublic(modifiers)) {
            sb.append("public ");
        } else if (Modifier.isPrivate(modifiers)) {
            sb.append("private ");
        } else if (Modifier.isProtected(modifiers)) {
            sb.append("protected ");
        }

        if (Modifier.isFinal(modifiers)) {
            sb.append("final ");
        }

        if (Modifier.isStatic(modifiers)) {
            sb.append("static ");
        }

        if (Modifier.isVolatile(modifiers)) {
            sb.append("volatile ");
        }

        return sb.toString();
    }

    public static void test(String a) {
        System.out.println("test " + a);
    }

    public static void main(String[] args) {
        String test = "okabc";

        try {
            Object value = Float.valueOf(1.0F);
            System.out.println(String.class.isInstance(value));
            Object o = invokeMethod(test, "equals", new Object[] {Integer.valueOf(1)});
            System.out.println(o);
            invokeStaticMethod(InvokeUtil.class, "test", new Object[] {null});
        } catch (NoSuchMethodException var4) {
            var4.printStackTrace();
        } catch (InvocationTargetException var5) {
            var5.printStackTrace();
        } catch (IllegalAccessException var6) {
            var6.printStackTrace();
        }

    }
}
