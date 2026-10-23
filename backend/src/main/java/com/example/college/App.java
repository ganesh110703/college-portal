
package com.example.college;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class App {
    static String env(String key, String fallback) {
        return System.getenv().getOrDefault(key, fallback);
    }

    static Connection connect() throws SQLException {
        String url = "jdbc:mysql://" + env("DB_HOST", "localhost")
            + ":" + env("DB_PORT", "3306")
            + "/" + env("DB_NAME", "college");
        return DriverManager.getConnection(
            url,
            env("DB_USER", "college_app"),
            env("DB_PASSWORD", "college_dev_password")
        );
    }

    static String field(String json, String key) {
        Matcher m = Pattern.compile(
            "\"" + key + "\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\""
        ).matcher(json);

        return m.find() ? m.group(1).trim() : "";
    }

    static String escape(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");
    }

    static void send(HttpExchange x, int status, String body)
            throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        x.getResponseHeaders().set(
            "Content-Type", "application/json; charset=utf-8"
        );

        if (x.getRequestMethod().equals("OPTIONS")) {
            x.sendResponseHeaders(204, -1);
            return;
        }

        x.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = x.getResponseBody()) {
            out.write(bytes);
        }
    }

    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(env("PORT", "8080"));

        HttpServer server = HttpServer.create(
            new InetSocketAddress("0.0.0.0", port), 0
        );

        server.setExecutor(Executors.newFixedThreadPool(8));

        server.createContext("/api/health", x -> {
            try (Connection c = connect();
                 Statement s = c.createStatement();
                 ResultSet r = s.executeQuery("SELECT 1")) {
                send(x, 200,
                    "{\"status\":\"ok\",\"database\":\"connected\"}");
            } catch (Exception e) {
                send(x, 503,
                    "{\"status\":\"error\",\"database\":\"unavailable\"}");
            }
        });

        server.createContext("/api/students", x -> {
            try {
                if (x.getRequestMethod().equals("OPTIONS")) {
                    send(x, 204, "");
                    return;
                }

                if (x.getRequestMethod().equals("GET")) {
                    StringBuilder out = new StringBuilder("[");
                    try (Connection c = connect();
                         PreparedStatement p = c.prepareStatement(
                             "SELECT id,name,course,email FROM students ORDER BY id DESC"
                         );
                         ResultSet r = p.executeQuery()) {
                        boolean first = true;
                        while (r.next()) {
                            if (!first) out.append(",");
                            first = false;
                            out.append("{\"id\":").append(r.getInt("id"))
                               .append(",\"name\":\"").append(escape(r.getString("name")))
                               .append("\",\"course\":\"").append(escape(r.getString("course")))
                               .append("\",\"email\":\"").append(escape(r.getString("email")))
                               .append("\"}");
                        }
                    }
                    out.append("]");
                    send(x, 200, out.toString());
                    return;
                }

                if (x.getRequestMethod().equals("POST")) {
                    String json = new String(
                        x.getRequestBody().readAllBytes(),
                        StandardCharsets.UTF_8
                    );

                    String name = field(json, "name");
                    String course = field(json, "course");
                    String email = field(json, "email");

                    if (name.isBlank() || course.isBlank() || email.isBlank()) {
                        send(x, 400,
                            "{\"error\":\"name, course and email are required\"}");
                        return;
                    }

                    try (Connection c = connect();
                         PreparedStatement p = c.prepareStatement(
                             "INSERT INTO students(name,course,email) VALUES(?,?,?)",
                             Statement.RETURN_GENERATED_KEYS
                         )) {
                        p.setString(1, name);
                        p.setString(2, course);
                        p.setString(3, email);
                        p.executeUpdate();

                        try (ResultSet keys = p.getGeneratedKeys()) {
                            keys.next();
                            send(x, 201,
                                "{\"id\":" + keys.getInt(1)
                                + ",\"name\":\"" + escape(name)
                                + "\",\"course\":\"" + escape(course)
                                + "\",\"email\":\"" + escape(email) + "\"}");
                        }
                    } catch (SQLException e) {
                        if (e.getErrorCode() == 1062) {
                            send(x, 409,
                                "{\"error\":\"Email already exists\"}");
                        } else {
                            throw e;
                        }
                    }
                    return;
                }

                send(x, 405, "{\"error\":\"Method not allowed\"}");
            } catch (Exception e) {
                try {
                    send(x, 500, "{\"error\":\"Request failed\"}");
                } catch (Exception ignored) {
                    // The response may already be closed.
                }
            }
        });

        server.start();
        System.out.println("College API running on port " + port);
    }
}

