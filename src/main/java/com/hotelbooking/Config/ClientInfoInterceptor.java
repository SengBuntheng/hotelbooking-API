package com.hotelbooking.Config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.net.InetAddress;

@Component
public class ClientInfoInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null) clientIp = request.getRemoteAddr();
        String clientHost = "unknown";
        try {
            InetAddress ia = InetAddress.getByName(clientIp);
            clientHost = ia.getHostName();
        } catch (Exception ignored) {}

        MDC.put("clientIp", clientIp);
        MDC.put("clientHost", clientHost);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {
        MDC.remove("clientIp");
        MDC.remove("clientHost");
    }
}
