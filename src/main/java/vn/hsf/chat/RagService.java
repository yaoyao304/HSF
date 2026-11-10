package vn.hsf.chat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

/**
 * RAG: nap tai lieu luc khoi dong, cat thanh doan theo tieu de (MON/PHAN),
 * khi chat thi lay vai doan lien quan nhat de dua vao prompt.
 */
@Service
public class RagService {

    private static final int TOP_K = 2;

    private final List<String> chunks = new ArrayList<>();
    private final Map<String, Integer> docFreq = new HashMap<>();

    public RagService(@Value("classpath:docs/quy_che_thi_pe.txt") Resource doc) throws IOException {
        StringBuilder cur = new StringBuilder();
        for (String line : doc.getContentAsString(StandardCharsets.UTF_8).split("\\R")) {
            String t = line.strip();
            if (t.matches("[-=]{5,}")) continue;
            if ((t.startsWith("MÔN ") || t.startsWith("PHẦN ")) && cur.length() > 0) {
                chunks.add(cur.toString());
                cur.setLength(0);
            }
            cur.append(line).append('\n');
        }
        if (cur.length() > 0) chunks.add(cur.toString());
        chunks.forEach(c -> tokens(c).forEach(w -> docFreq.merge(w, 1, Integer::sum)));
    }

    /** Tra ve noi dung cac doan lien quan nhat, rong neu khong khop tu nao. */
    // shortcut: khop tu khoa + IDF thay vi vector embedding, nang cap khi tai lieu lon hoac hay hoi dong nghia
    public String retrieve(String question) {
        Set<String> q = tokens(question);
        return chunks.stream()
                .map(c -> Map.entry(c, score(q, tokens(c))))
                .filter(e -> e.getValue() > 0)
                .sorted(Map.Entry.<String, Double>comparingByValue(Comparator.reverseOrder()))
                .limit(TOP_K)
                .map(Map.Entry::getKey)
                .collect(Collectors.joining("\n---\n"));
    }

    private double score(Set<String> q, Set<String> chunk) {
        double s = 0;
        for (String w : q) {
            if (chunk.contains(w)) s += Math.log(1 + (double) chunks.size() / docFreq.get(w));
        }
        return s;
    }

    private static Set<String> tokens(String text) {
        Set<String> out = new HashSet<>();
        for (String w : text.toLowerCase().split("[^\\p{L}\\p{N}]+")) {
            if (w.length() >= 2) {
                out.add(w);
                if (w.matches("[a-z]{3}\\d{3}")) out.add(w.substring(0, 3));
            }
        }
        return out;
    }
}
