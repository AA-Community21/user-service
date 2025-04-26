package com.tcket.utils;

import com.couchbase.client.core.env.TimeoutConfig;
import com.couchbase.client.core.error.CouchbaseException;
import com.couchbase.client.core.service.ServiceType;
import com.couchbase.client.java.*;
import com.couchbase.client.java.env.ClusterEnvironment;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.kv.MutationResult;
import com.couchbase.client.java.manager.bucket.BucketManager;
import com.couchbase.client.java.manager.bucket.BucketSettings;
import com.couchbase.client.java.manager.collection.CollectionSpec;
import com.couchbase.client.java.query.QueryResult;

import com.tcket.CouchbaseConstants;
import com.tcket.cbbuckets.BucketEnum;
import com.tcket.cbbuckets.BucketInfo;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import static com.couchbase.client.java.kv.UpsertOptions.upsertOptions;
import static com.couchbase.client.java.query.QueryOptions.queryOptions;

public class CouchbaseUtils {

    private static final Logger logger = LoggerFactory.getLogger(CouchbaseUtils.class);

    private static Cluster cluster;

    Pair<String, Boolean> clusterCheck = Pair.of("", false);

    Map<BucketInfo, Collection> bucketInfoMap = new HashMap<>();
    Map<BucketEnum, BucketInfo> buckets = new EnumMap<>(BucketEnum.class);

    @PostConstruct
    private synchronized void init() {
        cluster = createCluster();
        createCollections();
    }

    private void createCollections() {
        logger.info("creating the required Collections");
        BucketEnum.getValues()
                .forEach(bucket ->{
                    createBucketScopeCollectionIfNotExist(bucket);
                    BucketInfo bucketInfo = new BucketInfo(bucket.getBucket(), bucket.getScope(), bucket.getCollection());
                    bucketInfoMap.put(bucketInfo, cluster.bucket(bucket.getBucket()).scope(bucket.getScope()).collection(bucket.getCollection()));
                    buckets.put(bucket, bucketInfo);
                });
    }
    public static Cluster createCluster() {
        ClusterEnvironment environment = ClusterEnvironment.builder()
                .ioConfig(ioConfig ->
                        ioConfig.captureTraffic(ServiceType.MANAGER))
                .timeoutConfig(TimeoutConfig.kvTimeout(Duration.ofSeconds(20))
                        .queryTimeout(Duration.ofSeconds(50))
                        .connectTimeout(Duration.ofSeconds(60)))
                .build();
        logger.info("Initiating the Couchbase cluster Connection");
        return Cluster.connect(CBConnectionUtils.getConnectionString(), ClusterOptions.clusterOptions(CBConnectionUtils.getUsername(), CBConnectionUtils.getPassword()).environment(environment));
    }

    public void createBucketScopeCollectionIfNotExist(BucketEnum bucketEnum) {
        BucketManager bucketManager = cluster.buckets();
        String bucketName = bucketEnum.getBucket();
        String scopeName = bucketEnum.getScope();
        String collectionName = bucketEnum.getCollection();
        boolean bucketExists = bucketManager.getAllBuckets().containsKey(bucketName);
        if (!bucketExists) {
            bucketManager.createBucket(BucketSettings.create(bucketName).ramQuotaMB(100));
            Bucket bucket = cluster.bucket(bucketName);
            bucket.collections().createScope(scopeName);
            bucket.collections().createCollection(CollectionSpec.create(collectionName, scopeName));
            clusterCheck = Pair.of(bucketName, true);
        } else {
            Bucket bucket = cluster.bucket(bucketName);
            boolean scopeExists = bucket.collections().getAllScopes().stream()
                    .anyMatch(scope -> scope.name().equals(scopeName));
            if (scopeExists) {
                boolean collectionExists = bucket.collections().getAllScopes().stream()
                        .filter(scope -> scope.name().equals(scopeName))
                        .flatMap(scope -> scope.collections().stream())
                        .anyMatch(collection -> collection.name().equals(collectionName));
                if (!collectionExists) {
                    bucket.collections().createCollection(CollectionSpec.create(collectionName, scopeName));
                    clusterCheck = Pair.of(collectionName, true);
                }
            } else {
                bucket.collections().createScope(scopeName);
                bucket.collections().createCollection(CollectionSpec.create(collectionName, scopeName));
                clusterCheck = Pair.of(scopeName, true);
            }
        }
    }

    public void insertDoc(String id, String docValue, BucketEnum bucketEnum) {
        insertDoc(id, JsonObject.fromJson(docValue), bucketInfoMap.get(buckets.get(bucketEnum)));
    }

    public QueryResult getUsernameDetails(String username){
        String usernameQuery = buildQuery("username");
        return cluster.query(usernameQuery,
                queryOptions().parameters(JsonObject.create().put("username_1", username)));
    }


    public QueryResult getEmailDetails(String email) {
        String emailQuery = buildQuery("email");
        return cluster.query(emailQuery,
                queryOptions().parameters(JsonObject.create().put("email_1", email)));
    }

    public QueryResult getPhoneDetails(String phone) {
        String phoneQuery = buildQuery("phone");
        return cluster.query(phoneQuery,
                queryOptions().parameters(JsonObject.create().put("phone_1", phone)));
    }

    public QueryResult getUserDetails(String id) {
        String userQuery = buildQuery(id);
        return cluster.query(userQuery,
                queryOptions().parameters(JsonObject.create().put("id_1", id)));
    }

    private String buildQuery(String fieldName) {
        return "SELECT " + "`"+BucketEnum.USER_DETAILS.getBucket() +"` " +
                "FROM " + BucketEnum.USER_DETAILS.getKeyspacePath() + " " +
                "WHERE " + fieldName + " = $" + fieldName +"_1" + " LIMIT 1";
    }

    private void insertDoc(String id, JsonObject jsonObject, Collection collection) {
            MutationResult result = null;
            int retryCnt = 0;
            Throwable exception = null;
            boolean toRetry;
            do {
                try {
                    result = collection.upsert(id, jsonObject,
                            upsertOptions().timeout(Duration.of(CouchbaseConstants.DB_TIMEOUT, ChronoUnit.SECONDS)));
                    toRetry = false;
                } catch (Exception ex) {
                    logger.warn("Exception Occurred : {0}", ex);
                    toRetry = false;
                    exception = ex;
                    if (retryCnt <= CouchbaseConstants.DB_RETRY_COUNT) {
                        retryCnt++;
                        logger.warn("going for retry, with retry count {}/{}",retryCnt,CouchbaseConstants.DB_RETRY_COUNT);
                        toRetry = true;
                    } else {
                        logger.warn("retry limit has been exhausted");
                    }
                }
            } while (toRetry);

            if(result == null){
                logger.warn("Document upsert has resulted in an error : {0}", exception);
                throw new CouchbaseException("unexpected issue occurred", exception);
            }
    }

    public void deleteUser(String username, BucketEnum bucketEnum) {
        deleteDoc(username, bucketInfoMap.get(buckets.get(bucketEnum)));
    }

    private void deleteDoc(String id, Collection collection) {
        try {
            collection.remove(id);
        } catch (Exception e){
            throw new CouchbaseException("cannot delete the provided doc", e);
        }
    }
}
