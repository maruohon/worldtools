package fi.dy.masa.worldutils.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

public class MethodHandleUtils
{
    public static class UnableToFindMethodHandleException extends RuntimeException
    {
        private static final long serialVersionUID = 1L;

        public UnableToFindMethodHandleException(String[] methodNames, Exception failed)
        {
            super(failed);
        }

    }

    public static <E> MethodHandle getMethodHandleVirtual(Class<? super E> clazz, String[] methodNames, Class<?>... paramTypes)
    {
        Exception failed = null;

        try
        {
            Method method = reflectMethod(clazz, methodNames, paramTypes);
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            MethodHandle handle = lookup.unreflect(method);
            method.setAccessible(false);
            return handle;
        }
        catch (IllegalAccessException e)
        {
            failed = e;
        }

        throw new UnableToFindMethodHandleException(methodNames, failed);
    }

    @SuppressWarnings("deprecation")
    public static <E> Method reflectMethod(Class<? super E> clazz, String[] methodNames, Class<?>... methodTypes)
    {
        Exception failed = null;

        for (String methodName : methodNames)
        {
            try
            {
                Method m = clazz.getDeclaredMethod(methodName, methodTypes);
                m.setAccessible(true);
                return m;
            }
            catch (Exception e)
            {
                failed = e;
            }
        }

        throw new net.minecraftforge.fml.relauncher.ReflectionHelper.UnableToFindMethodException(methodNames, failed);
    }
}
