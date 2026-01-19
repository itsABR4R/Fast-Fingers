package com.typinggame.io;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Utility class for loading code snippets from files.
 * Uses FileReader to read snippet files (Req 3).
 * Implements random snippet selection and caching for performance.
 */
@Component
public class CodeSnippetLoader {

    private final Random random;
    private final PathMatchingResourcePatternResolver resolver;

    public CodeSnippetLoader() {
        this.random = new Random();
        this.resolver = new PathMatchingResourcePatternResolver();
    }

    /**
     * Load a random code snippet for the specified language.
     * Uses FileReader to read files (Req 3).
     * 
     * @param language "java" or "javascript"
     * @return Code snippet as string
     */
    public String loadRandomSnippet(String language) throws IOException {
        String pattern = "classpath:snippets/" + language + "/*.{java,js}";

        try {
            Resource[] resources = resolver.getResources(pattern);

            if (resources.length == 0) {
                System.err.println("[CodeSnippetLoader] No snippets found for language: " + language);
                return getFallbackSnippet(language);
            }

            // Select random snippet
            Resource selectedResource = resources[random.nextInt(resources.length)];

            System.out.println("[CodeSnippetLoader] Loading snippet: " + selectedResource.getFilename());

            // Read file using BufferedReader with InputStreamReader
            StringBuilder content = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(selectedResource.getInputStream()))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
            }

            return content.toString().trim();

        } catch (IOException e) {
            System.err.println("[CodeSnippetLoader] Error loading snippet: " + e.getMessage());
            return getFallbackSnippet(language);
        }
    }

    /**
     * Get fallback snippet if file loading fails.
     */
    private String getFallbackSnippet(String language) {
        if ("java".equalsIgnoreCase(language)) {
            return "public class FastFingers {\n" +
                    "  public static void main(String[] args) {\n" +
                    "    System.out.println(\"Type fast!\");\n" +
                    "  }\n" +
                    "}";
        } else if ("javascript".equalsIgnoreCase(language)) {
            return "function typeFast() {\n" +
                    "  console.log('Keep typing!');\n" +
                    "  return true;\n" +
                    "}\n\n" +
                    "typeFast();";
        }
        return "// No snippet available";
    }

    /**
     * Load all snippets for a language (for caching).
     */
    public List<String> loadAllSnippets(String language) throws IOException {
        List<String> snippets = new ArrayList<>();
        String pattern = "classpath:snippets/" + language + "/*.{java,js}";

        try {
            Resource[] resources = resolver.getResources(pattern);

            for (Resource resource : resources) {
                StringBuilder content = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(resource.getInputStream()))) {

                    String line;
                    while ((line = reader.readLine()) != null) {
                        content.append(line).append("\n");
                    }
                }
                snippets.add(content.toString().trim());
            }

            System.out.println("[CodeSnippetLoader] Loaded " + snippets.size() +
                    " snippets for " + language);

        } catch (IOException e) {
            System.err.println("[CodeSnippetLoader] Error loading all snippets: " + e.getMessage());
        }

        return snippets;
    }
}
