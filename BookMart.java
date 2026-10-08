import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.util.*;

public class BookMartServer {

    static final String DB_URL = "jdbc:mysql://localhost:3306/bookmart";
    static final String DB_USER = "root";
    static final String DB_PASSWORD ="1234";

    public static void main(String[] args) throws Exception {

        Class.forName("com.mysql.cj.jdbc.Driver");

        HttpServer server = HttpServer.create(
                new InetSocketAddress(8080), 0);

        server.createContext("/", BookMartServer::home);
        server.createContext("/api/books", BookMartServer::books);
        server.createContext("/api/login", BookMartServer::login);
        server.createContext("/api/order", BookMartServer::order);
        server.createContext("/api/review", BookMartServer::review);

        server.setExecutor(null);
        server.start();

        System.out.println("BOOKMART SERVER STARTED");
        System.out.println("Open: http://localhost:8080/");
    }

    static Connection connectDB() throws SQLException {
        return DriverManager.getConnection(
                DB_URL, DB_USER, DB_PASSWORD);
    }

    static void home(HttpExchange exchange) throws IOException {

        if (!exchange.getRequestMethod().equals("GET")) {
            send(exchange, 405, "Method Not Allowed", "text/plain");
            return;
        }

        try {
            byte[] data = Files.readAllBytes(
                    Paths.get("frontend/index.html"));

            exchange.getResponseHeaders().set(
                    "Content-Type", "text/html");

            exchange.sendResponseHeaders(200, data.length);

            OutputStream os = exchange.getResponseBody();
            os.write(data);
            os.close();

        } catch (Exception e) {
            send(exchange, 500,
                    "Frontend file not found",
                    "text/plain");
        }
    }

    static void books(HttpExchange exchange) throws IOException {

        StringBuilder json = new StringBuilder("[");
        boolean first = true;

        try (Connection con = connectDB();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT id,title,author,category,price,stock FROM books")) {

            while (rs.next()) {

                if (!first) {
                    json.append(",");
                }

                json.append("{")
                    .append("\"id\":").append(rs.getInt("id")).append(",")
                    .append("\"title\":\"").append(escape(rs.getString("title"))).append("\",")
                    .append("\"author\":\"").append(escape(rs.getString("author"))).append("\",")
                    .append("\"category\":\"").append(escape(rs.getString("category"))).append("\",")
                    .append("\"price\":").append(rs.getDouble("price")).append(",")
                    .append("\"stock\":").append(rs.getInt("stock"))
                    .append("}");

                first = false;
            }

            json.append("]");

            send(exchange, 200,
                    json.toString(),
                    "application/json");

        } catch (Exception e) {

            send(exchange, 500,
                    "{\"success\":false,\"message\":\"Database error\"}",
                    "application/json");
        }
    }

    static void login(HttpExchange exchange) throws IOException {

        if (!exchange.getRequestMethod().equals("POST")) {
            send(exchange, 405,
                    "{\"success\":false}",
                    "application/json");
            return;
        }

        String body = readBody(exchange);
        Map<String, String> data = parseForm(body);

        String username = data.get("username");
        String password = data.get("password");

        String sql =
                "SELECT user_id,username FROM users " +
                "WHERE username=? AND password=?";

        try (Connection con = connectDB();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                String result =
                        "{\"success\":true," +
                        "\"userId\":" + rs.getInt("user_id") +
                        ",\"username\":\"" +
                        escape(rs.getString("username")) +
                        "\"}";

                send(exchange, 200,
                        result,
                        "application/json");

            } else {

                send(exchange, 401,
                        "{\"success\":false,\"message\":\"Invalid username or password\"}",
                        "application/json");
            }

        } catch (Exception e) {

            send(exchange, 500,
                    "{\"success\":false,\"message\":\"Database error\"}",
                    "application/json");
        }
    }

    static void order(HttpExchange exchange) throws IOException {

        if (!exchange.getRequestMethod().equals("POST")) {
            send(exchange, 405,
                    "{\"success\":false}",
                    "application/json");
            return;
        }

        String body = readBody(exchange);
        Map<String, String> data = parseForm(body);

        int userId = Integer.parseInt(data.get("userId"));
        double total = Double.parseDouble(data.get("total"));
        String items = data.get("items");

        Connection con = null;

        try {

            con = connectDB();
            con.setAutoCommit(false);

            String[] products = items.split(",");

            for (String product : products) {

                String[] parts = product.split(":");

                int bookId = Integer.parseInt(parts[0]);
                int quantity = Integer.parseInt(parts[1]);

                PreparedStatement check =
                        con.prepareStatement(
                                "SELECT stock FROM books WHERE id=?");

                check.setInt(1, bookId);

                ResultSet rs = check.executeQuery();

                if (!rs.next() ||
                    rs.getInt("stock") < quantity) {

                    con.rollback();

                    send(exchange, 400,
                            "{\"success\":false,\"message\":\"Insufficient stock\"}",
                            "application/json");

                    return;
                }

                PreparedStatement update =
                        con.prepareStatement(
                                "UPDATE books SET stock=stock-? WHERE id=?");

                update.setInt(1, quantity);
                update.setInt(2, bookId);

                update.executeUpdate();
            }

            PreparedStatement insert =
                    con.prepareStatement(
                            "INSERT INTO orders(user_id,total_amount,order_status) VALUES(?,?,?)");

            insert.setInt(1, userId);
            insert.setDouble(2, total);
            insert.setString(3, "CONFIRMED");

            insert.executeUpdate();

            con.commit();

            send(exchange, 200,
                    "{\"success\":true,\"message\":\"Order confirmed\"}",
                    "application/json");

        } catch (Exception e) {

            try {
                if (con != null) {
                    con.rollback();
                }
            } catch (Exception ignored) {
            }

            send(exchange, 500,
                    "{\"success\":false,\"message\":\"Order failed\"}",
                    "application/json");

        } finally {

            try {
                if (con != null) {
                    con.close();
                }
            } catch (Exception ignored) {
            }
        }
    }

    static void review(HttpExchange exchange) throws IOException {

        if (!exchange.getRequestMethod().equals("POST")) {
            send(exchange, 405,
                    "{\"success\":false}",
                    "application/json");
            return;
        }

        String body = readBody(exchange);
        Map<String, String> data = parseForm(body);

        try (Connection con = connectDB();
             PreparedStatement ps = con.prepareStatement(
                     "INSERT INTO reviews(book_id,rating,comment) VALUES(?,?,?)")) {

            ps.setInt(1, Integer.parseInt(data.get("bookId")));
            ps.setInt(2, Integer.parseInt(data.get("rating")));
            ps.setString(3, data.get("comment"));

            ps.executeUpdate();

            send(exchange, 200,
                    "{\"success\":true,\"message\":\"Review submitted\"}",
                    "application/json");

        } catch (Exception e) {

            send(exchange, 500,
                    "{\"success\":false,\"message\":\"Review failed\"}",
                    "application/json");
        }
    }

    static String readBody(HttpExchange exchange)
            throws IOException {

        InputStream input = exchange.getRequestBody();

        ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];
        int length;

        while ((length = input.read(buffer)) != -1) {
            output.write(buffer, 0, length);
        }

        return new String(
                output.toByteArray(),
                StandardCharsets.UTF_8);
    }

    static Map<String, String> parseForm(String body)
            throws UnsupportedEncodingException {

        Map<String, String> map = new HashMap<>();

        if (body == null || body.isEmpty()) {
            return map;
        }

        String[] pairs = body.split("&");

        for (String pair : pairs) {

            String[] parts = pair.split("=", 2);

            if (parts.length == 2) {

                String key =
                        URLDecoder.decode(parts[0], "UTF-8");

                String value =
                        URLDecoder.decode(parts[1], "UTF-8");

                map.put(key, value);
            }
        }

        return map;
    }

    static String escape(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    static void send(
            HttpExchange exchange,
            int status,
            String response,
            String contentType)
            throws IOException {

        byte[] data =
                response.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set(
                "Content-Type",
                contentType + "; charset=UTF-8");

        exchange.sendResponseHeaders(
                status,
                data.length);

        OutputStream os =
                exchange.getResponseBody();

        os.write(data);
        os.close();
    }
}