# Ứng dụng Chat AI + RAG · Project Cuối Kỳ HSF

Ứng dụng web Spring Boot tích hợp trí tuệ nhân tạo hỗ trợ **Chat tự do ra ngoài**, **Chat RAG (tra cứu Quy chế thi Đại học FPT)**, **Cấu hình API Key để hoán chuyển nền tảng chat (Google Gemini / Groq)** và **Lưu lịch sử từng đoạn chat vào SQL Server**.

---

## 1. Hướng dẫn Setup môi trường & Chạy ứng dụng (Setup Guide)

### 1.1. Yêu cầu hệ thống
- **JDK:** 17 hoặc 21 trở lên (`java -version`)
- **Maven:** 3.6.3 trở lên (`mvn -v`)
- **Cơ sở dữ liệu:** Microsoft SQL Server (chạy ở cổng `1433`, bật chế độ xác thực tài khoản `sa`)

### 1.2. Khởi tạo Database trong SQL Server
1. Mở **SQL Server Management Studio (SSMS)**, đăng nhập bằng tài khoản `sa`.
2. Mở **New Query** và chạy lệnh tạo Database trống:

```sql
CREATE DATABASE ai_chat_rag;
```

> **Lưu ý:** Không cần chạy lệnh `CREATE TABLE`. Khi ứng dụng khởi động, cấu hình `spring.jpa.hibernate.ddl-auto=update` sẽ tự động tạo 2 bảng `chat_sessions` và `chat_messages` kèm khóa ngoại.

### 1.3. Cấu hình API Key để hoán chuyển nền tảng (`.env` & `application.properties`)

Ứng dụng tách biệt khóa bảo mật trong file `.env` (được nạp tự động vào `application.properties` qua `spring.config.import`).

1. **Lấy API Key miễn phí:**
   - **Google Gemini:** Truy cập [Google AI Studio](https://aistudio.google.com/apikey) -> bấm **Create API key**.
   - **Groq:** Truy cập [Groq Console](https://console.groq.com/keys) -> bấm **Create API Key**.
2. **Tạo và điền file `.env`:**
   Sao chép file `.env.example` thành `.env` tại thư mục gốc `ai-chat-rag` và điền key:

```properties
AI_PROVIDER=gemini
GEMINI_API_KEY=dan_key_gemini_vao_day
GEMINI_MODEL=gemini-3.8-flash-lite

APP_GROQ_API_KEY=dan_key_groq_vao_day
GROQ_MODEL=llama-3.1-8b-instant
```

3. **Cấu hình trong `src/main/resources/application.properties`:**

```properties
spring.config.import=optional:file:.env[.properties],optional:file:asigmanet/project/ai-chat-rag/.env[.properties]
server.port=8081

# Kết nối SQL Server
spring.datasource.url=${DB_URL:jdbc:sqlserver://localhost:1433;databaseName=ai_chat_rag;encrypt=true;trustServerCertificate=true}
spring.datasource.username=${DB_USERNAME:sa}
spring.datasource.password=${DB_PASSWORD:123456}
spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver
spring.jpa.hibernate.ddl-auto=update

# Cấu hình AI & Hoán chuyển nền tảng
ai.provider=${AI_PROVIDER:gemini}
ai.gemini.api-key=${GEMINI_API_KEY:}
ai.gemini.model=${GEMINI_MODEL:gemini-3.8-flash-lite}
ai.gemini.fallback-models=gemini-flash-lite-latest,gemma-3-27b-it
ai.groq.api-key=${APP_GROQ_API_KEY:}
ai.groq.model=${GROQ_MODEL:llama-3.1-8b-instant}
```

### 1.4. Chạy ứng dụng
Từ thư mục `ai-chat-rag` (hoặc chạy trực tiếp file `AiChatApplication.java` trong IntelliJ IDEA):

```powershell
mvn spring-boot:run
```

Khi ứng dụng khởi động xong, mở trình duyệt tại: **[http://localhost:8081](http://localhost:8081)**

Để chạy bộ kiểm thử tự động (Unit Test cho RAG và xử lý chuỗi):

```powershell
mvn test
```

---

## 2. Cấu trúc & Giải thích Code (Code Guide)

### 2.1. Tổng quan các tệp mã nguồn

| Tệp | Trách nhiệm chính |
| --- | --- |
| `AiChatApplication.java` | Điểm khởi chạy ứng dụng Spring Boot. |
| `AiHttpConfig.java` | Cấu hình `RestClient` sử dụng `JdkClientHttpRequestFactory` với timeout và `User-Agent`. |
| `ChatController.java` | Nhận `GET /` (hiển thị trang chat & lịch sử), `POST /chat` (gửi tin nhắn) và `POST /sessions/{id}/delete` (xóa đoạn chat). |
| `ChatService.java` | Điều phối luồng chat: làm sạch API Key, ghép ngữ cảnh RAG (nếu bật) và chuyển cuộc gọi sang Gemini hoặc Groq. |
| `RagService.java` | Đọc `docs/quy_che_thi_pe.txt` lúc khởi động, chia đoạn (chunking) và truy xuất các đoạn quy chế liên quan nhất theo trọng số IDF. |
| `GeminiClient.java` | Gọi REST API `generateContent` của Google Gemini, tự động chuyển sang model dự phòng (`gemma-3-27b-it`) nếu hết quota (`429`) hoặc quá tải (`503`). |
| `GroqClient.java` | Gọi REST API chuẩn OpenAI (`/openai/v1/chat/completions`) của Groq, tự động dò model khả dụng nếu tên model cấu hình không khớp. |
| `ChatSession.java` | Entity JPA ánh xạ bảng `chat_sessions` (quan hệ `1-N` với `ChatMessage`), tự cắt 35 ký tự đầu của câu hỏi đầu tiên làm tiêu đề. |
| `ChatMessage.java` | Entity JPA ánh xạ bảng `chat_messages`, lưu câu hỏi, câu trả lời, nền tảng AI và trạng thái RAG. |
| `ChatSessionRepository.java` | Repository JPA truy vấn danh sách đoạn chat mới nhất lên đầu. |
| `ChatMessageRepository.java` | Repository JPA truy vấn các tin nhắn thuộc một đoạn chat theo thứ tự thời gian. |
| `templates/index.html` | Giao diện một trang sử dụng **Thymeleaf + Bootstrap 5.3**. |
| `docs/quy_che_thi_pe.txt` | Dữ liệu kiến thức nội bộ cho RAG (Điều kiện dự thi, Nội quy phòng thi QĐ 1234/QĐ-ĐHFPT và Quy định PE từng môn). |

---

### 2.2. Giải thích chi tiết 3 tính năng cốt lõi theo đề bài

#### A. Chat với AI (Chat ra ngoài) & Hoán chuyển nền tảng (`ChatService.java`)
Khi công tắc RAG tắt (`useRag = false`), câu hỏi của người dùng được gửi nguyên bản tới nền tảng AI đang chọn trên giao diện (`gemini` hoặc `groq`):

```java
public String ask(String provider, String message, boolean useRag) {
    if (message == null || message.isBlank()) throw new IllegalArgumentException("Tin nhắn trống");
    boolean useGroq = "groq".equals(provider);
    String key = clean(useGroq ? groqKey : geminiKey);
    // ...
    String prompt = message;
    if (useRag) {
        String context = rag.retrieve(message);
        if (context.isEmpty()) context = "(không tìm thấy đoạn nào liên quan)";
        prompt = "Chỉ trả lời dựa trên TÀI LIỆU dưới đây. Nếu tài liệu không có thông tin thì nói rõ là không tìm thấy.\n\n"
                + "TÀI LIỆU:\n" + context + "\n\nCÂU HỎI: " + message;
    }
    return useGroq ? groq.ask(key, prompt) : gemini.ask(key, prompt);
}
```

- **Hoán chuyển nền tảng:** Người dùng có thể đổi nền tảng mặc định qua biến `AI_PROVIDER` trong `.env` **hoặc** chuyển đổi trực tiếp giữa **Google Gemini** và **Groq** bằng menu Dropdown trên giao diện mà không cần khởi động lại server.
- **Tự động chuyển đổi dự phòng (Fallback):** Trong `GeminiClient.java`, nếu model chính trả về mã lỗi `429` (vượt giới hạn lượt gọi miễn phí trong ngày) hoặc `404`, hệ thống lập tức chuyển sang model dự phòng (`gemma-3-27b-it` với 14.400 lượt miễn phí/ngày).

#### B. Chat với AI theo tài liệu nội bộ — RAG (`RagService.java`)
Luồng xử lý **RAG (Retrieval-Augmented Generation)** gồm 3 bước:

1. **Nạp & Chia nhỏ tài liệu (Chunking) khi khởi động:**
   `RagService` đọc file `classpath:docs/quy_che_thi_pe.txt`, tách thành từng đoạn độc lập dựa trên tiêu đề `PHẦN ...` hoặc `MÔN ...`, đồng thời thống kê tần suất từ khóa (`docFreq`) và tách tiền tố 3 chữ cái của mã môn (ví dụ `prj301` tách thêm `prj`, `hsf302` tách thêm `hsf`) để sinh viên gõ tắt tên môn vẫn tìm trúng.
2. **Truy xuất đoạn văn liên quan (Retrieval):**
   Khi có câu hỏi, hàm `retrieve(question)` tính điểm theo công thức **IDF (Inverse Document Frequency)** — ưu tiên những từ khóa đặc trưng xuất hiện ở ít đoạn văn (như tên môn học `HSF302`, `PRJ301`, `Lombok`, `20%`) và lấy ra `TOP_K = 2` đoạn có điểm cao nhất:

```java
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
```

3. **Tăng cường Prompt (Augmentation):**
   Đoạn văn bản tìm được sẽ ghép vào Prompt kèm chỉ thị ép AI **chỉ trả lời dựa trên tài liệu được cung cấp**, giúp câu trả lời chính xác theo quy chế trường và không bịa đặt (hallucination).

#### C. Lưu lịch sử theo từng đoạn chat với SQL Server (`ChatSession` & `ChatMessage`)
- Khi bắt đầu đoạn chat mới (`sessionId == null`), `ChatController` tự động tạo `ChatSession` mới với tiêu đề cắt gọn 35 ký tự đầu của câu hỏi đầu tiên (`ChatSession.makeTitle(firstQuestion)`).
- Các câu hỏi tiếp theo trong cùng phiên sẽ gắn khóa ngoại `session_id` vào đoạn chat hiện tại.
- Giao diện `templates/index.html` sử dụng Thymeleaf (`th:each="s : ${sessions}"` và `th:each="m : ${messages}"`) để hiển thị danh sách đoạn chat bên cột trái và nội dung tin nhắn bên cột phải.

---

## 3. Kịch bản Demo thuyết trình (5–7 phút)

1. **Bước 1 — Demo Chat ra ngoài (Tắt công tắc RAG):**
   - Để công tắc **Chế độ RAG** ở trạng thái **Tắt**.
   - Gõ câu hỏi kiến thức chung: *"Giải thích ngắn gọn mô hình MVC trong Spring Boot"*.
   - Chỉ vào thanh **Lịch sử chat** bên trái: một đoạn chat mới vừa tự động xuất hiện với tiêu đề lấy từ câu hỏi đầu tiên.
2. **Bước 2 — Demo Chat với AI (Bật công tắc RAG):**
   - Bấm **"+ Đoạn chat mới"**, gạt bật công tắc **Chế độ RAG (hỏi theo quy chế thi FPT)**.
   - Hỏi câu 1: *"Sinh viên vắng quá bao nhiêu phần trăm số buổi thì bị cấm thi?"* -> AI trích xuất đúng quy định cấm thi khi vắng quá **20%**.
   - Hỏi câu 2: *"Thi PE môn HSF có được dùng thư viện Lombok không?"* -> AI trả lời **tuyệt đối không được dùng Lombok (vi phạm bị 0 điểm)** và nhắc lệnh `mvn clean` trước khi nộp EOS.
   - Hỏi câu 3 (test gõ tắt mã môn): *"Nội quy thi môn prj"* -> AI trích xuất đúng yêu cầu **NetBeans 13, JDK 8, Tomcat 9.0.113, sqljdbc4.jar** của môn PRJ301/PRJ302.
3. **Bước 3 — Demo Hoán chuyển nền tảng Chat:**
   - Tại cột trái, bấm Dropdown **Nền tảng AI** chuyển từ **Google Gemini** sang **Groq**.
   - Gõ hỏi tiếp một câu hỏi quy chế hoặc câu hỏi chung -> Hệ thống lập tức gọi sang API của Groq và trả lời bằng tiếng Việt.
4. **Bước 4 — Kiểm tra Lịch sử Chat & Database:**
   - Bấm chuyển qua lại giữa các đoạn chat ở thanh bên trái để xem lại nội dung cũ, bấm F5 tải lại trang để chứng minh dữ liệu được lưu bền vững.
   - Mở **SQL Server Management Studio (SSMS)**, query 2 bảng `SELECT * FROM chat_sessions` và `SELECT * FROM chat_messages` để minh chứng dữ liệu lưu trong SQL Server.

---

## 4. Câu hỏi phản biện thường gặp

- **Chat thường và Chat RAG khác nhau ở điểm nào?**
  Chat thường chỉ dựa vào kiến thức chung có sẵn của mô hình AI nên không biết quy định nội bộ hoặc dễ trả lời chung chung. Chat RAG tìm đúng đoạn quy chế trong file `quy_che_thi_pe.txt` đính kèm vào câu hỏi để ép AI trả lời chính xác theo tài liệu của trường.
- **Làm sao ứng dụng chuyển đổi được giữa Google Gemini và Groq?**
  `ChatService` nhận tham số `provider` từ giao diện (hoặc mặc định từ `.env`), sau đó điều hướng cuộc gọi sang `GeminiClient` (chuẩn REST API của Google) hoặc `GroqClient` (chuẩn REST API tương thích OpenAI) với API Key tương ứng.
- **Nếu một model Gemini miễn phí bị hết lượt gọi trong ngày (lỗi 429) thì sao?**
  `GeminiClient` bắt mã lỗi `429` và tự động chuyển tiếp (fallback) sang model dự phòng trong danh sách (`gemma-3-27b-it`) mà người dùng không bị gián đoạn.
