package org.example;

import redis.clients.jedis.JedisPool;

import java.io.IOException;

public class RequestRateLimiterTest {
    public static void main(String[] args) throws IOException, InterruptedException {

        RequestRateLimiter requestRateLimiter = new RequestRateLimiter(new JedisPool("localhost", 6379));
        for (int i = 0; i < 60; i++) {
            ServerRequestHandler serverRequestHandler = new ServerRequestHandler(requestRateLimiter);
            System.out.println(i + " " + serverRequestHandler.request("1"));
        }
        Thread.sleep(100);
        System.out.println("after sleep" + requestRateLimiter.setKey("1"));
    }


    public static class ServerRequestHandler {

        private RequestRateLimiter requestRateLimiter;

        public ServerRequestHandler(RequestRateLimiter requestRateLimiter) {
            this.requestRateLimiter = requestRateLimiter;
        }

        public String request(String userId) throws IOException {
            if (requestRateLimiter.setKey(userId).equals("429")) {
                return "rejected";
            } else {
                return "accepted";
            }
        }
    }
}
