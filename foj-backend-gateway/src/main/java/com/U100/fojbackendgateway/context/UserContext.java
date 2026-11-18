package com.U100.fojbackendgateway.context;

public class UserContext {
    private static final ThreadLocal<Long> TL = new ThreadLocal<>();
    public static void set(Long id) { TL.set(id); }
    public static Long get() { return TL.get(); }
    public static void clear() { TL.remove(); }
}
