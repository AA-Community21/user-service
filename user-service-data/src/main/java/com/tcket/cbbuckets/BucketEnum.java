package com.tcket.cbbuckets;

import com.tcket.CouchbaseConstants;
import com.tcket.CouchbaseConstants.BucketInformation.UserDetails;

import java.util.List;

public enum BucketEnum {

    USER_DETAILS(UserDetails.BUCKET_NAME, UserDetails.BUCKET_SCOPE, UserDetails.BUCKET_COLLECTION);

    private final String bucket;

    private final String scope;

    private final String collection;


    public String getBucket() {
        return this.bucket;
    }

    public String getScope() {
        return this.scope;
    }

    public String getCollection() {
        return this.collection;
    }


    public String getKeyspacePath() {
        return String.format(CouchbaseConstants.KEYSPACE_PATH_STRING_FORMAT, this.bucket, this.scope, this.collection);
    }

    BucketEnum(String bucket, String scope, String collection) {
        this.bucket = bucket;
        this.scope = scope;
        this.collection = collection;
    }

    public static List<BucketEnum> getValues(){
        return List.of(BucketEnum.values());
    }

    @Override
    public String toString() {
        return "BucketsEnum{" +
                ", bucket='" + bucket + '\'' +
                ", scope='" + scope + '\'' +
                ", collection='" + collection + '\'' +
                '}';
    }

}
