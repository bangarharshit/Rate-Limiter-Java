package org.example;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.IOException;

public class RequestRateLimiterTest {
    public static void main(String[] args) {
        JedisPool jedisPool = new JedisPool("localhost", 6379);

        try (Jedis jedis = jedisPool.getResource()) {
            RequestRateLimiter requestRateLimiter = new RequestRateLimiter(jedis);
            for (int i = 0; i < 60; i++) {
                System.out.println(i + " " + requestRateLimiter.setKey("1"));
            }
            Thread.sleep(100);
            System.out.println("after sleep" + requestRateLimiter.setKey("1"));
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
