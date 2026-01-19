import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

void main() throws Exception {

    ObjectMapper mapper = new ObjectMapper();
    JsonNode root = mapper.readTree(new File("input3.json"));

    StringBuilder html = new StringBuilder();

    html.append("<!DOCTYPE ")
            .append(root.path("doctype").asText())
            .append(">\n");

    if (root.path("language").isTextual()) {
    html.append("<html lang=\"")
            .append(root.path("language").asText())
            .append("\">\n");
    }
    else {
        html.append("<html>\n");
    }

    renderHead(root.path("head"), html);
    renderBody(root.path("body"), html);

    html.append("</html>");

    try (FileWriter writer = new FileWriter("output.html")) {
        writer.write(html.toString());
    }

    IO.println("HTML done");
}

private static void renderHead(JsonNode head, StringBuilder html) {
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

private static void renderBody(JsonNode body, StringBuilder html) {
    html.append("\t<body");

    JsonNode attrs = body.path("attributes");
    if (!attrs.isMissingNode()) {
        appendAttributes(attrs, html);
    }

    html.append(">\n");

    body.fields().forEachRemaining(e -> {
        if (!e.getKey().equals("attributes")) {
            renderElement(e.getKey(), e.getValue(), html);
        }
    });

    html.append("\t</body>\n");
}

private static void renderElement(String tag, JsonNode node, StringBuilder html) {

    html.append("\t\t<").append(tag);

    if (node.isObject() && node.has("attributes")) {
        appendAttributes(node.get("attributes"), html);
    }

    html.append(">");

    if (node.isTextual()) {
        html.append(node.asText());
        html.append("</").append(tag).append(">\n");
    } else if (node.isObject()) {
        html.append("\n\t");
        node.fields().forEachRemaining(e -> {
            if (!e.getKey().equals("attributes")) {
                renderElement(e.getKey(), e.getValue(), html);
            }
        });
        html.append("\t\t</").append(tag).append(">\n");
    }
}

private static void appendAttributes(JsonNode attrs, StringBuilder html) {
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
}
