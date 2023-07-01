package org.example;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class RequestRateLimiterMultiThreadedTest {

    public static void main(String[] args) throws InterruptedException {
        AtomicInteger atomicInteger = new AtomicInteger();
        RequestRateLimiter requestRateLimiter = new RequestRateLimiter(new JedisPool("localhost", 6379));
        Thread thread1 = new Thread(() -> {
            for (int i = 0; i < 60; i++) {
                try {
                    if (Objects.equals(requestRateLimiter.setKey("1"), "200")) {
                        atomicInteger.incrementAndGet();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        Thread thread2 = new Thread(() -> {
            for (int i = 0; i < 60; i++) {
                try {
                    if (Objects.equals(requestRateLimiter.setKey("1"), "200")) {
                        atomicInteger.incrementAndGet();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();
        System.out.println(atomicInteger.get());

    }

}
