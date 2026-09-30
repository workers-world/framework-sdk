package com.books.framework.utils;

import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.Socket;

@Slf4j
public final class NetworkUtils {

    private static final String DEFAULT_CHECK_HOST = "baidu.com";
    private static final int DEFAULT_CHECK_PORT = 443;
    private static final int DEFAULT_TIMEOUT_MS = 3000;

    private NetworkUtils() {
    }

    public static boolean isReachable() {
        return isReachable(DEFAULT_CHECK_HOST, DEFAULT_CHECK_PORT, DEFAULT_TIMEOUT_MS);
    }

    public static boolean isReachable(String host, int port, int timeoutMs) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeoutMs);
            return true;
        } catch (Exception e) {
            log.debug("网络连通性检查失败: {}:{} - {}", host, port, e.getMessage());
            return false;
        }
    }
}
