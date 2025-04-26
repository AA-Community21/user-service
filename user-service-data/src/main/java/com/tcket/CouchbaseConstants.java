package com.tcket;

@SuppressWarnings("java:S1118")
public class CouchbaseConstants {

    public static final String KEYSPACE_PATH_STRING_FORMAT = "`%s`.`%s`.`%s`";
    public static final long DB_TIMEOUT = 5000L;
    public static final int DB_RETRY_COUNT = 3;

    public static class BucketInformation {
        public static class UserDetails {
            public static final String BUCKET_NAME = "UserService";
            public static final String BUCKET_SCOPE = "UserService";
            public static final String BUCKET_COLLECTION = "UserService";
        }
    }
}
