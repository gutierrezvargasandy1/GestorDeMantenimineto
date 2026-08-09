package com.utng.util;

import javafx.geometry.Insets;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MarkdownRenderer {

    // Solo negritas: **texto**
    private static final Pattern BOLD_PATTERN = Pattern.compile("\\*\\*(.+?)\\*\\*");

    // Viñeta al inicio de línea: "* " o "- " pero NO "**"
    private static final Pattern BULLET_PATTERN = Pattern.compile("^\\s*[*\\-](?!\\*)\\s+");

    // Código inline: `texto`
    private static final Pattern CODE_PATTERN = Pattern.compile("`(.+?)`");

    private MarkdownRenderer() {}

    public static TextFlow render(String markdown, double fontSize, String colorHex) {
        TextFlow flow = new TextFlow();
        flow.setLineSpacing(4);
        flow.setPadding(new Insets(0));

        if (markdown == null || markdown.isBlank()) {
            return flow;
        }

        String[] lines = markdown.split("\n", -1);

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            // Línea vacía -> salto de párrafo
            if (line.isBlank()) {
                flow.getChildren().add(new Text("\n"));
                continue;
            }

            Matcher bulletMatcher = BULLET_PATTERN.matcher(line);
            String contenido = line;

            if (bulletMatcher.find()) {
                // Reemplaza "* " o "- " por una viñeta real
                Text bullet = new Text("•  ");
                bullet.setStyle("-fx-font-size: " + fontSize + "px; -fx-fill: " + colorHex + ";");
                flow.getChildren().add(bullet);
                contenido = line.substring(bulletMatcher.end());
            }

            addFormattedLine(flow, contenido, fontSize, colorHex);

            // Salto de línea, excepto en la última línea
            if (i < lines.length - 1) {
                flow.getChildren().add(new Text("\n"));
            }
        }

        return flow;
    }

    /** Procesa negritas y código dentro de una sola línea de texto ya sin viñeta. */
    private static void addFormattedLine(TextFlow flow, String line, double fontSize, String colorHex) {
        int lastEnd = 0;

        Matcher combined = Pattern.compile(
                BOLD_PATTERN.pattern() + "|" + CODE_PATTERN.pattern()
        ).matcher(line);

        while (combined.find()) {
            if (combined.start() > lastEnd) {
                flow.getChildren().add(plainText(line.substring(lastEnd, combined.start()), fontSize, colorHex));
            }

            if (combined.group(1) != null) {          // negrita
                flow.getChildren().add(boldText(combined.group(1), fontSize, colorHex));
            } else if (combined.group(2) != null) {   // código
                flow.getChildren().add(codeText(combined.group(2), fontSize));
            }

            lastEnd = combined.end();
        }

        if (lastEnd < line.length()) {
            flow.getChildren().add(plainText(line.substring(lastEnd), fontSize, colorHex));
        }
    }

    private static Text plainText(String content, double size, String colorHex) {
        Text t = new Text(content);
        t.setStyle("-fx-font-size: " + size + "px; -fx-fill: " + colorHex + ";");
        return t;
    }

    private static Text boldText(String content, double size, String colorHex) {
        Text t = new Text(content);
        t.setFont(Font.font("System", FontWeight.BOLD, size));
        t.setStyle("-fx-fill: " + colorHex + ";");
        return t;
    }

    private static Text codeText(String content, double size) {
        Text t = new Text(content);
        t.setStyle("-fx-font-size: " + (size - 1) + "px; -fx-font-family: 'Consolas', monospace; "
                + "-fx-fill: #dc2626;");
        return t;
    }
}