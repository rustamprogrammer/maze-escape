import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

/** Run from the repository root with JDK 17+: java tools/Personalize.java ID "Name" "Group". */
public class Personalize {
    public static void main(String[] args) throws Exception {
        if (args.length < 1 || args[0].isBlank()) {
            System.out.println("Usage: java tools/Personalize.java STUDENT_ID \"Full name\" \"Group\"");
            return;
        }
        String id = args[0]; // Keep the ID as a string to preserve leading zeroes.
        String name = args.length > 1 ? args[1] : "Not supplied";
        String group = args.length > 2 ? args[2] : "Not supplied";
        Path bin = Path.of(System.getProperty("java.home"), "bin");
        List<String> compile = new ArrayList<>(List.of(bin.resolve("javac").toString(),
                "--release", "17", "-d", "out"));
        try (var files = Files.list(Path.of("src"))) {
            files.filter(p -> p.toString().endsWith(".java")).sorted()
                    .forEach(p -> compile.add(p.toString()));
        }
        execute(compile);
        execute(List.of(bin.resolve("java").toString(), "tools/MakeVariant.java", id, "data"));
        String output = execute(List.of(bin.resolve("java").toString(), "-cp", "out", "Main"));
        if (output.contains("WARNING") || output.contains("Cannot find"))
            throw new IllegalStateException("Main reported a problem; report was not updated");
        String firstLine;
        try (var lines = Files.lines(Path.of("data/walls.txt"))) { firstLine = lines.findFirst().orElseThrow(); }
        String[] dims = firstLine.split("\\s+");
        Matcher cost = Pattern.compile("(?m)^MST cost: (\\d+)\\s*$").matcher(output);
        if (!cost.find()) throw new IllegalStateException("Missing MST output");
        String readme = Files.readString(Path.of("README.md"));
        readme = replace(readme, "identity", "Student ID: " + id + "  \nName: " + name
                + "  \nGroup: " + group + "  \nMaze size: " + dims[0] + " x " + dims[1]);
        readme = replace(readme, "variant-status", "Personal data generated with the exact ID `" + id + "`.");
        readme = replace(readme, "program-output", "```text\n" + output.strip() + "\n```");
        readme = replace(readme, "mst-cost", "Kruskal = Prim = **" + cost.group(1) + "**.");
        Files.writeString(Path.of("README.md"), readme, StandardCharsets.UTF_8);
        Files.writeString(Path.of("output/run.txt"), output, StandardCharsets.UTF_8);
        System.out.println("Updated data, picture, HTML, run.txt and README.md. Review and commit the changes.");
    }

    static String execute(List<String> command) throws Exception {
        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        if (process.waitFor() != 0) throw new IllegalStateException(String.join(" ", command) + "\n" + output);
        if (!output.isEmpty()) System.out.print(output);
        return output;
    }

    static String replace(String text, String key, String value) {
        String start = "<!-- " + key + ":start -->", end = "<!-- " + key + ":end -->";
        Pattern pattern = Pattern.compile(Pattern.quote(start) + ".*?" + Pattern.quote(end), Pattern.DOTALL);
        Matcher matcher = pattern.matcher(text);
        if (!matcher.find()) throw new IllegalStateException("Missing README marker: " + key);
        return matcher.replaceFirst(Matcher.quoteReplacement(start + "\n" + value + "\n" + end));
    }
}
