package org.example;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class ConcurrentRequestRateLimiter {

    private Jedis jedis;

    public ConcurrentRequestRateLimiter(Jedis jedis) {
        this.jedis = jedis;
    }

    public static void main(String[] args) {
        JedisPool jedisPool = new JedisPool("localhost", 6379);

        for (int i = 0; i < 5; i++) {
            int finalI = i;
            new Thread(() -> {
                try (Jedis jedis = jedisPool.getResource()) {
                    ConcurrentRequestRateLimiter requestRateLimiter = new ConcurrentRequestRateLimiter(jedis);
                    requestRateLimiter.startRequest("5", String.valueOf(finalI));
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        }
    }

    public void startRequest(String userId, String requestId) throws IOException, InterruptedException {
        String key = userId+".concurrent";
        jedis.zremrangeByScore(key, "-inf", String.valueOf((System.currentTimeMillis()-20000.0)/1000.0));
        List<String> keys = List.of(key);
        List<String> args = Arrays.asList("3", String.valueOf(System.currentTimeMillis()/1000.0), requestId);
        Object object = jedis.eval(Files.readString(
                Paths.get("/Users/harshitbangar/ratelimiter/src/main/java/org/example/concurrent_requests_limiter.lua")), keys, args);
        List<Long> allowedAndNewTokens = (List<Long>) object;
        if (allowedAndNewTokens.get(0) == null) {
            System.out.println("429");
        } else {
            System.out.println("200");
        }
        Thread.sleep(10000);
        jedis.zrem(key, requestId);
    }


}
