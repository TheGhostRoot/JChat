package jchat.load_balancer;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Order(1)
@WebFilter("/**")
public class RederectFilter extends OncePerRequestFilter  {

    private static final int REQUEST_THRESHOLD = 2;
    private static final long TIME_WINDOW_SECONDS = 1;

    private final ConcurrentHashMap<String, Long> requestCountMap = new ConcurrentHashMap<>();
    private final List<String> blockedIPs = new CopyOnWriteArrayList<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String IP = request.getRemoteAddr();
        IP = "0:0:0:0:0:0:0:1".equals(IP) ? "127.0.0.1" : IP;

        if (!blockedIPs.contains(IP) && isAllowed(IP)) {
            Map<String, Object> data = new HashMap<>();
            data.put("req", request);
            data.put("res", response);

            LoadBalancer.queue.add(data);

            filterChain.doFilter(request, response);

        } else {
            response.getWriter().write("Rate limit exceeded or IP blocked");
            response.setContentType("text/plain");
            response.setStatus(429); // 429 Too Many Requests
        }
    }

    private boolean isAllowed(String clientIP) {
        long currentTime = System.currentTimeMillis();
        long previousRequestTime = requestCountMap.getOrDefault(clientIP, 0L);

        if (currentTime - previousRequestTime > TimeUnit.SECONDS.toMillis(TIME_WINDOW_SECONDS)) {
            // Reset count if the time window has passed
            requestCountMap.put(clientIP, currentTime);
            return true;
        } else {
            // Increment request count
            requestCountMap.put(clientIP, previousRequestTime);
            return ((int) requestCountMap.keySet()
                    .stream()
                    .filter(ip -> ip.equals(clientIP))
                    .count()) <= REQUEST_THRESHOLD;
        }
    }



    public void sendRederect() {
        if (LoadBalancer.useServer1) {
            for (String ip : LoadBalancer.servers1) {
                try {
                    HttpServletRequest request = (HttpServletRequest) LoadBalancer.queue.get(0).get("req");
                    HttpServletResponse response = (HttpServletResponse) LoadBalancer.queue.get(0).get("res");

                    LoadBalancer.queue.remove(0);

                    String targetUrl = ip + request.getRequestURI();

                    URL url = new URL(targetUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod(request.getMethod());

                    copyHeaders(request, connection);

                    connection.setDoOutput(true);
                    copyBody(request.getInputStream(), connection.getOutputStream());

                    int responseCode = connection.getResponseCode();
                    response.setStatus(responseCode);

                    copyHeaders(connection, response);
                    copyBody(connection.getInputStream(), response.getOutputStream());

                    connection.disconnect();

                } catch (Exception e) {}
            }


        } else {
            for (String ip : LoadBalancer.servers2) {
                try {
                    HttpServletRequest request = (HttpServletRequest) LoadBalancer.queue.get(0).get("req");
                    HttpServletResponse response = (HttpServletResponse) LoadBalancer.queue.get(0).get("res");

                    LoadBalancer.queue.remove(0);

                    String targetUrl = ip + request.getRequestURI();

                    URL url = new URL(targetUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod(request.getMethod());

                    copyHeaders(request, connection);

                    connection.setDoOutput(true);
                    copyBody(request.getInputStream(), connection.getOutputStream());

                    int responseCode = connection.getResponseCode();
                    response.setStatus(responseCode);

                    copyHeaders(connection, response);
                    copyBody(connection.getInputStream(), response.getOutputStream());

                    connection.disconnect();

                } catch (Exception e) {}
            }

        }
    }

    private void copyHeaders(HttpServletRequest request, HttpURLConnection connection) {
        request.getHeaderNames().asIterator()
                .forEachRemaining(headerName -> connection.setRequestProperty(headerName, request.getHeader(headerName)));
    }

    private void copyHeaders(HttpURLConnection connection, HttpServletResponse response) {
        for (String header : connection.getHeaderFields().keySet()) {
            if (header != null) {
                for (String value : connection.getHeaderFields().get(header)) {
                    response.addHeader(header, value);
                }
            }
        }
    }

    private void copyBody(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
    }
}
