package com.wangkaixuan.svpserver;

import io.lettuce.core.KeyValue;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

import java.util.HashMap;
import java.util.List;

public class RedisConn {
    private static RedisClient redisClient;
    private static StatefulRedisConnection<String, String> connection;
    private static RedisCommands<String, String> sync;

    public static RedisCommands<String, String> Sync() {
        if (sync != null) {
            return sync;
        }
        var uri = (String) Config.ins().get("RedisUri");
        redisClient = RedisClient.create(uri);
        connection = redisClient.connect();
        sync = connection.sync();
        return sync;
    }

    private void runTest() {
        var user = new HashMap<String, String>();
        user.put("token", "122EE42F-FA57-4779-96D9-EA3821DFE4DE");
        sync.hmset("user:wangkaixuan", user);

        user.clear();
        user.put("token", "7CF2532C-539F-4574-B767-75C868A51F42");
        sync.hmset("user:chengangrrong", user);

        var session = new HashMap<String, String>();
        session.put("videoName", "百万美元宝贝.rmvb");
        session.put("videoSHA3", "e855149c7e691bba168579461784cac2df328709fee2bcebd328994a06557b8");
        session.put("progress", "3600");
        session.put("slaves", "session:wangkaixuan");

        sync.hmset("session:wangkaixuan", session);
        sync.lpush("slaves:wangkaixuan", "chengangrong");

        connection.close();
        redisClient.shutdown();
    }

    private void getTest() {
        var user = (List<KeyValue<String, String>>) sync.hmget("user:wangkaixuan", "token");
        for (KeyValue<String, String> k : user) {
            System.out.printf("%s\n", k.getKey());
            System.out.printf("%s\n", k.getValue());
        }
        var slaves = (List<String>) sync.lrange("slaves:wangkaixuan", 0, -1);
        System.out.printf("%s\n", slaves);
    }
}
