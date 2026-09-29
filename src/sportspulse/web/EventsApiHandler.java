package sportspulse.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import sportspulse.models.MatchEvent;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class EventsApiHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        exchange.getResponseHeaders().set("Content-Type", "application/json");

        if ("GET".equalsIgnoreCase(method)) {
            List<MatchEvent> events = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader("data/events.csv"))) {
                String line = br.readLine(); // skip header
                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    String[] p = line.split(",");
                    if (p.length >= 5) {
                        events.add(new MatchEvent(p[0], p[1], p[2], p[3], p[4]));
                    }
                }
            } catch (Exception e) {
                // ignore if file doesn't exist
            }
            
            String json = JsonConverter.toJson(events);
            byte[] response = json.getBytes();
            exchange.sendResponseHeaders(200, response.length);
            OutputStream os = exchange.getResponseBody();
            os.write(response);
            os.close();
        } else {
            exchange.sendResponseHeaders(405, -1);
        }
    }
}
