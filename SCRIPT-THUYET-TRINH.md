# KỊCH BẢN THUYẾT TRÌNH (SCRIPT) · PROJECT CUỐI KỲ HSF
**Đề tài (Option 1):** Ứng dụng Chat AI + RAG + Hoán chuyển nền tảng + User Guide Setup & Code

---

## MỞ ĐẦU (20 giây)
- **[Thao tác]:** Mở sẵn trình duyệt ở địa chỉ `http://localhost:8081`.
- **[Lời nói]:**
  > "Em chào thầy và các bạn. Ở phần Project cuối kỳ, nhóm em chọn làm **Option 1**: Xây dựng một ứng dụng Spring Boot kết hợp Thymeleaf và SQL Server, đáp ứng đầy đủ 4 yêu cầu của đề bài gồm:
  > 1. **Chat với AI (Chat ra ngoài)**
  > 2. **Chat với AI (RAG)** dựa trên bộ Quy chế thi của Đại học FPT
  > 3. **Cấu hình API Key để hoán chuyển nền tảng chat** giữa Google Gemini và Groq
  > 4. **Tài liệu User Guide về Setup và Code**"

---

## PHẦN 1: DEMO "CHAT VỚI AI (CHAT RA NGOÀI)" (1 phút)
- **[Thao tác]:**
  1. Chỉ chuột vào cột Cấu hình bên trái: Nền tảng đang chọn **Google Gemini**, công tắc **Chế độ RAG** đang **TẮT**.
  2. Gõ vào ô chat: `Giải thích ngắn gọn Spring Boot trong 3 gạch đầu dòng` rồi bấm **Gửi**.
  3. Khi câu trả lời hiện ra, chỉ chuột vào mục **Lịch sử chat** bên trái.
- **[Lời nói]:**
  > "Đầu tiên là yêu cầu số 1: **Chat với AI (Chat ra ngoài)**.
  > Khi công tắc RAG đang tắt, ứng dụng hoạt động giống như khi chúng ta chat trực tiếp với Gemini hoặc ChatGPT ngoài Internet: câu hỏi của người dùng được gửi nguyên bản qua REST API tới mô hình AI để trả lời các kiến thức chung.
  > Đồng thời, ngay từ câu hỏi đầu tiên, hệ thống tự động cắt 35 ký tự đầu để tạo tiêu đề phiên chat ở thanh Lịch sử bên trái và lưu vào SQL Server."

---

## PHẦN 2: DEMO "CHAT VỚI AI (RAG)" (2 phút)
- **[Thao tác]:**
  1. Bấm nút **"+ Đoạn chat mới"** ở góc trên bên trái.
  2. Gạt **BẬT** công tắc **"Chế độ RAG (hỏi theo quy chế thi FPT)"**.
  3. **Câu test 1:** Gõ `Sinh viên vắng quá bao nhiêu phần trăm thì bị cấm thi?` -> Bấm **Gửi**.
  4. **Câu test 2:** Gõ `Thi PE môn HSF có được dùng thư viện Lombok không?` -> Bấm **Gửi**.
  5. **Câu test 3 (test ngoài tài liệu):** Gõ `Căn tin trường bán phở giá bao nhiêu?` -> Bấm **Gửi**.
- **[Lời nói]:**
  > "Tiếp theo là yêu cầu số 2: **Chat với AI theo cơ chế RAG**.
  > Bình thường AI bên ngoài sẽ không biết nội quy riêng của trường FPT hoặc quy định chấm thi PE của từng môn. Nhóm em đã tổng hợp quy chế thi của trường và quy định thi PE môn HSF vào file `quy_che_thi_pe.txt`.
  > - Ở câu đầu tiên, khi hỏi *'Vắng quá bao nhiêu % thì bị cấm thi?'*, AI tra cứu tài liệu và trả lời chính xác là **quá 20%** sẽ bị cấm thi.
  > - Ở câu thứ hai, khi hỏi *'Thi PE môn HSF có được dùng Lombok không?'*, AI trích xuất đúng quy chế riêng của môn HSF là **tuyệt đối không được dùng Lombok, vi phạm sẽ bị 0 điểm**.
  > - Và nếu em hỏi một câu không hề có trong tài liệu như *'Căn tin bán phở giá bao nhiêu?'*, AI sẽ báo rõ là **không tìm thấy thông tin trong tài liệu** chứ tuyệt đối không tự bịa ra câu trả lời."

---

## PHẦN 3: DEMO "CẤU HÌNH API KEY ĐỂ HOÁN CHUYỂN NỀN TẢNG CHAT" (1 phút 30 giây)
- **[Thao tác]:**
  1. Trên trình duyệt, bấm vào Dropdown **Nền tảng AI**, chuyển từ **Google Gemini** sang **Groq**.
  2. Gõ vào ô chat: `Nội quy thi môn prj` -> Bấm **Gửi**.
  3. Sau khi Groq trả lời, mở IntelliJ IDEA, bật file `.env` và `application.properties` lên màn hình.
- **[Lời nói]:**
  > "Thứ ba là yêu cầu **Cấu hình API Key để hoán chuyển nền tảng chat**.
  > Trên giao diện, người dùng có thể chuyển đổi tức thì từ **Google Gemini** sang **Groq** mà không cần khởi động lại ứng dụng. Khi em hỏi *'Nội quy thi môn prj'*, nền tảng Groq lập tức tiếp nhận ngữ cảnh RAG và trả lời đúng yêu cầu của môn PRJ301 là dùng NetBeans 13, JDK 8 và Tomcat 9.
  >
  > Về mặt cấu hình ở phía dưới (em xin mở file `.env` và `application.properties`):
  > - Toàn bộ API Key và tên model của cả hai nền tảng (`GEMINI_API_KEY`, `APP_GROQ_API_KEY`) cùng nền tảng mặc định (`AI_PROVIDER`) được cấu hình tập trung trong file `.env`.
  > - File `application.properties` tự động nạp file `.env` này thông qua `spring.config.import`. Khi muốn thay đổi API Key hoặc đổi model, người dùng chỉ cần sửa tại file `.env`."

---

## PHẦN 4: TRÌNH BÀY "USER GUIDE VỀ SETUP VÀ CODE" (2 phút)
- **[Thao tác]:**
  1. Trong IntelliJ, mở file `README.md` (cuộn nhanh qua mục 1 Setup Guide và mục 2 Code Guide).
  2. Chuyển qua tab `RagService.java` -> chỉ vào hàm `RagService()` và `retrieve()`.
  3. Chuyển qua tab `ChatService.java` -> chỉ vào hàm `ask()`.
  4. Chuyển qua tab `GeminiClient.java` / `GroqClient.java` và mở nhanh SSMS cho xem 2 bảng `chat_sessions`, `chat_messages`.
- **[Lời nói]:**
  > "Cuối cùng là phần **User Guide về Setup và Code** được nhóm em trình bày chi tiết trong file `README.md` của project:
  > 1. **Về phần Setup:** Tài liệu hướng dẫn rõ từ bước tạo Database `ai_chat_rag` trong SQL Server, cách lấy API Key miễn phí trên Google AI Studio và Groq Console, cách cấu hình file `.env`, cho đến lệnh chạy `mvn spring-boot:run`.
  > 2. **Về phần Code RAG (`RagService.java`):** Khi ứng dụng khởi động, `RagService` đọc file `quy_che_thi_pe.txt` và cắt thành từng đoạn (chunk) theo từng phần và từng môn học. Khi người dùng đặt câu hỏi, hàm `retrieve()` tách từ khóa (hỗ trợ cả gõ tắt mã môn như `prj` khớp với `prj301`, `hsf` khớp với `hsf302`), chấm điểm theo công thức trọng số IDF và lấy ra 2 đoạn quy chế sát nhất.
  > 3. **Về phần Code điều phối AI (`ChatService.java`):** Hàm `ask()` kiểm tra nếu công tắc RAG bật thì ghép 2 đoạn quy chế vừa tìm được vào Prompt, sau đó dựa vào tham số `provider` để gọi sang `GeminiClient` hoặc `GroqClient`. Đặc biệt trong `GeminiClient`, nếu một model miễn phí bị hết lượt gọi trong ngày (mã lỗi `429`), code sẽ tự động fallback sang model dự phòng (`gemma-3-27b-it`) để không bị gián đoạn.
  > 4. **Về phần Database:** Hai Entity `ChatSession` và `ChatMessage` được thiết kế theo quan hệ 1–Nhiều (`@OneToMany` / `@ManyToOne`) lưu toàn bộ lịch sử chat xuống SQL Server.
  >
  > Phần trình bày của nhóm em đến đây là hết, em cảm ơn thầy và các bạn đã lắng nghe ạ!"
