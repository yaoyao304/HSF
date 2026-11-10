package vn.hsf.chat;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class RagServiceTest {

    private final RagService rag = build();

    private static RagService build() {
        try {
            return new RagService(new ClassPathResource("docs/quy_che_thi_pe.txt"));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    void findsHsfLombokRule() {
        assertTrue(rag.retrieve("Thi HSF302 có được dùng Lombok không?").toLowerCase().contains("lombok"));
    }

    @Test
    void findsAttendanceRule() {
        assertTrue(rag.retrieve("Vắng bao nhiêu phần trăm thì bị cấm thi?").contains("20%"));
    }

    @Test
    void findsSubjectByShortPrefix() {
        assertTrue(rag.retrieve("nội quy thi môn prj").contains("PRJ301"));
    }

    @Test
    void emptyWhenNoMatch() {
        assertTrue(rag.retrieve("zzzqqq").isEmpty());
    }
}
