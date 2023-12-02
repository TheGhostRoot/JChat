package jchat.load_balancer;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class RederectFilter extends OncePerRequestFilter  {


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Map<String, Object> data = new HashMap<>();
        data.put("req", request);
        data.put("res", response);

        LoadBalancer.queue.add(data);

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
