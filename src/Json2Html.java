import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public final class Json2Html {

   private static final StringBuilder html = new StringBuilder();
   private static JsonNode root;

    public static void renderDoc(String file) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        root = mapper.readTree(new File(file));
        html.append("<!DOCTYPE ")
                .append(root.path("doctype").asText())
                .append(">\n");

        if (root.path("language").isTextual()) {
            html.append("<html lang=\"")
                    .append(root.path("language").asText())
                    .append("\">\n");
        } else {
            html.append("<html>\n");
        }
    renderHead();
    renderBody();
    html.append("</html>");
    writeToFile();
    }
private static void writeToFile() throws IOException {
    try (FileWriter writer = new FileWriter("output.html")) {
        writer.write(html.toString());
        }
        IO.println("HTML writen to file");
    }

private static void renderHead() {
    JsonNode head = root.path("head");
    html.append("\t<head>\n");

    JsonNode meta = head.path("meta");
    meta.fields().forEachRemaining(e -> {
        if ("charset".equalsIgnoreCase(e.getKey())) {
            html.append("\t\t<meta charset=\"")
                    .append(e.getValue().asText())
                    .append("\">\n");
        } else {
            html.append("\t\t<meta name=\"")
                    .append(e.getKey())
                    .append("\" content=\"");
            if (e.getValue().isObject()) {
                e.getValue().fields().forEachRemaining(v ->
                        html.append(v.getKey())
                                .append("=")
                                .append(v.getValue().asText())
                                .append(", ")
                );
                html.setLength(html.length() - 2);
            }
                else{
                    html.append(e.getValue().asText());
                }
                    html.append("\">\n");
        }
    });

    JsonNode links = head.path("link");
    if (links.isArray()) {
        for (JsonNode link : links) {
            html.append("\t\t<link");
            link.fields().forEachRemaining(e ->
                    html.append(" ")
                            .append(e.getKey())
                            .append("=\"")
                            .append(e.getValue().asText())
                            .append("\"")
            );
            html.append(">\n");
        }
    }
    html.append("\t\t<title>")
            .append(head.path("title").asText())
            .append("</title>\n");
    html.append("\t</head>\n");
}

private static void renderBody() {
    JsonNode body = root.path("body");
        html.append("\t<body");

    JsonNode attrs = body.path("attributes");
    if (!attrs.isMissingNode()) {
        appendAttributes(attrs);
    }

    html.append(">\n");

    body.fields().forEachRemaining(e -> {
        if (!e.getKey().equals("attributes")) {
            renderElement(e.getKey(), e.getValue());
        }
    });

    html.append("\t</body>\n");
}

private static void renderElement(String tag, JsonNode node) {

    html.append("\t\t<").append(tag);

    if (node.isObject() && node.has("attributes")) {
        appendAttributes(node.get("attributes"));
    }

    html.append(">");

    if (node.isTextual()) {
        html.append(node.asText());
        html.append("</").append(tag).append(">\n");
    } else if (node.isObject()) {
        html.append("\n\t");
        node.fields().forEachRemaining(e -> {
            if (!e.getKey().equals("attributes")) {
                renderElement(e.getKey(), e.getValue());
            }
        });
        html.append("\t\t</").append(tag).append(">\n");
    }
}

private static void appendAttributes(JsonNode attrs) {
    attrs.fields().forEachRemaining(e -> {
        if (e.getValue().isObject()) {
            html.append(" ")
                    .append(e.getKey())
                    .append("=\"");
            e.getValue().fields().forEachRemaining(style ->
                    html.append(style.getKey())
                            .append(":")
                            .append(style.getValue().asText())
                            .append(";")
            );
            html.setLength(html.length() - 1);
            html.append("\"");
        } else {
            html.append(" ")
                    .append(e.getKey())
                    .append("=\"")
                    .append(e.getValue().asText())
                    .append("\"");
        }
    });
}}