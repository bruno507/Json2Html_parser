import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public final class Json2Html {

   // private static final StringBuilder html = new StringBuilder();

    public static void renderDoc(String fileIn, String fileOut) throws IOException {

        StringBuilder html = new StringBuilder();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(new File(fileIn + ".json"));
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
    html.append(renderHead(root.path("head")));
    html.append(renderBody(root.path("body")));
    html.append("</html>");
    writeToFile(fileOut, html);
    }
private static void writeToFile(String out, StringBuilder html) throws IOException {
    try (FileWriter writer = new FileWriter(out + ".html")) {
        writer.write(html.toString());
        }
        IO.println("HTML written to file" + out + ".html");
    }

private static String indent(int x) {
    String add = "\t";
    String indent = "";
    while (x > 0) {
        indent = indent.concat(add);
        x--;
    }
    return indent;
    }

private static StringBuilder renderHead(JsonNode head) {
    indent(2);
    StringBuilder html = new StringBuilder();
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
    if (head.path("title").isTextual()) {
        html.append("\t\t<title>")
            .append(head.path("title").asText())
            .append("</title>\n");
    }
    html.append("\t</head>\n");
    return html;
}

private static StringBuilder renderBody(JsonNode body) {
    StringBuilder html = new StringBuilder();
        html.append("\t<body");

    JsonNode attrs = body.path("attributes");
    if (!attrs.isMissingNode()) {
        html.append(appendAttributes(attrs));
    }

    html.append(">\n");

    body.fields().forEachRemaining(e -> {
        if (!e.getKey().equals("attributes")) {
            html.append(renderElement(e.getKey(), e.getValue()));
        }
    });

    html.append("\t</body>\n");
    return html;
}

private static StringBuilder renderElement(String tag, JsonNode node) {
    StringBuilder html = new StringBuilder();
    html.append("\t\t<").append(tag);

    if (node.isObject() && node.has("attributes")) {
        html.append(appendAttributes(node.get("attributes")));
    }

    html.append(">");

    if (node.isTextual()) {
        html.append(node.asText());
        html.append("</").append(tag).append(">\n");
    } else if (node.isObject()) {
        html.append("\n\t");
        node.fields().forEachRemaining(e -> {
            if (!e.getKey().equals("attributes")) {
                html.append(renderElement(e.getKey(), e.getValue()));
            }
        });
        html.append("\t\t</").append(tag).append(">\n");
    }
    return html;
}

private static StringBuilder appendAttributes(JsonNode attrs) {
    StringBuilder html = new StringBuilder();
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
        return html;
}}