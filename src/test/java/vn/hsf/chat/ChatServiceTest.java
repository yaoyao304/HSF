package vn.hsf.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ChatServiceTest {

    @Test
    void cleanStripsWhitespaceAndNewlines() {
        assertEquals("gsk_abc", ChatService.clean("  gsk_abc\r\n"));
        assertEquals("", ChatService.clean(null));
    }

    @Test
    void makeTitleTruncatesAt35Chars() {
        assertEquals("Xin chào", ChatSession.makeTitle("  Xin   chào "));
        String longQ = "Thi PE môn HSF302 có được sử dụng thư viện Lombok không?";
        assertEquals(38, ChatSession.makeTitle(longQ).length());
    }
}
