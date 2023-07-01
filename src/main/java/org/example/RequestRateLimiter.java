package org.example;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

public class RequestRateLimiter {

    public static final String REPLENISH_RATE = "10";
    public static final String CAPACITY = String.valueOf(Integer.parseInt(REPLENISH_RATE)*5);
    private JedisPool jedisPool;

    public RequestRateLimiter(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    public String setKey(String userId) throws IOException {
        try (Jedis jedis = jedisPool.getResource()) {
            String prefix = "request_rate_limiter." + userId;
            List<String> keys = Arrays.asList(prefix+".tokens", prefix+".timestamp");
            List<String> args = Arrays.asList(REPLENISH_RATE, CAPACITY, String.valueOf(System.currentTimeMillis()/1000.0), String.valueOf(1));
            Object object = jedis.eval(Files.readString(
                    Paths.get("/Users/harshitbangar/ratelimiter/src/main/java/org/example/tokenbucket.lua")), keys, args);
            List<Long> allowedAndNewTokens = (List<Long>) object;
            if (allowedAndNewTokens.get(0) == null) {
                return "429";
            } else {
                return  "200";
            }
        }

    }

}
