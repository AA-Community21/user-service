package com.tcket.utils;


public class CBConnectionUtils {

    private final String connectionString = "couchbase://127.0.0.1";

    private final String username = "Administrator";

    private final String password = "abhi@2003";

    public static String getConnectionString() {
        return CBConnectionUtilsHolder.INSTANCE.connectionString;
    }

    public static String getUsername() {
        return CBConnectionUtilsHolder.INSTANCE.username;
    }

    public static String getPassword() {
        return CBConnectionUtilsHolder.INSTANCE.password;
    }

    private static class CBConnectionUtilsHolder {
        private static final CBConnectionUtils INSTANCE = new CBConnectionUtils();
    }

}
