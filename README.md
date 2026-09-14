# Study Java — Học Backend từ Java Spring đến Cloud

Repo ghi lại toàn bộ quá trình tự học Backend với Java. Mỗi bài gồm **lý thuyết giải thích trước**, rồi **code mẫu + thực hành**.

> Cách học đã chọn: giảng lý thuyết → đưa code mẫu → tự gõ lại code, không copy-paste.

---

## Lộ trình

| GĐ | Nội dung | Mục tiêu | Trạng thái |
|---|---|---|---|
| **0** | Môi trường: JDK, VS Code, Git | Chạy được dòng Java đầu tiên | ✅ Xong |
| **1** | Java core: biến, kiểu dữ liệu, điều kiện, vòng lặp, method | Viết logic cơ bản | 🔄 Đang làm |
| **2** | OOP: class, object, kế thừa, interface, đa hình | Tư duy hướng đối tượng — nền tảng bắt buộc để hiểu Spring | ⬜ |
| **3** | Collections, Exception, Generics, Stream API | Xử lý dữ liệu thực tế | ⬜ |
| **4** | Maven, annotation, reflection cơ bản | Hiểu *tại sao* Spring "tự động" làm được nhiều thứ | ⬜ |
| **5** | Spring Core: IoC / DI / Bean | Trái tim của Spring | ⬜ |
| **6** | Spring Boot + REST API | Viết được API đầu tiên | ⬜ |
| **7** | Spring Data JPA + Database (Docker + PostgreSQL) | CRUD thật, có DB thật | ⬜ |
| **8** | Validation, Exception handling, DTO, layered architecture | Code chuẩn production | ⬜ |
| **9** | Spring Security + JWT | Đăng nhập / phân quyền | ⬜ |
| **10** | Testing: JUnit, Mockito | Tự tin sửa code | ⬜ |
| **11** | Docker hóa app, CI/CD | Đóng gói & tự động deploy | ⬜ |
| **12** | Cloud: AWS/Azure, microservices, message queue | Đích đến | ⬜ |

---

## Mục lục tài liệu

### Giai đoạn 0 — Môi trường

| Bài | Nội dung |
|---|---|
| [0.1](docs/00-moi-truong/01-java-hoat-dong-nhu-the-nao.md) | Java hoạt động như thế nào? JDK vs JRE vs JVM |
| [0.2](docs/00-moi-truong/02-cai-dat-jdk.md) | Cài đặt JDK 21 trên Windows |
| [0.3](docs/00-moi-truong/03-vscode-cho-java-spring.md) | Cấu hình VS Code cho Java & Spring |
| [0.4](docs/00-moi-truong/04-git-line-ending.md) | Git cho Java project: line ending & `.gitattributes` |

### Giai đoạn 1 — Java core

| Bài | Nội dung | Code |
|---|---|---|
| [1.1](docs/01-java-core/01-chuong-trinh-dau-tien.md) | Chương trình đầu tiên. Mổ xẻ `public static void main(String[] args)` từng từ | [`Hello.java`](bai-tap/01-java-core/Hello.java) |
| [1.2](docs/01-java-core/02-compile-time-vs-runtime.md) | Compile-time vs Runtime: hai cửa kiểm soát. 6 thí nghiệm phá vỡ chương trình | — |
| [1.3](docs/01-java-core/03-bien-va-kieu-du-lieu.md) | Static typing, 8 kiểu nguyên thủy, integer overflow, `char` là số, IEEE 754 | [`KieuDuLieu.java`](bai-tap/01-java-core/KieuDuLieu.java) |

---

## 📍 Đang học đến đâu

**Vừa xong:** Bài 1.3 — static typing vs dynamic typing, 8 kiểu nguyên thủy, và ba cái bẫy: integer overflow, numeric promotion, số thực IEEE 754.

**Việc tiếp theo:** Bài 1.4 — `String`, wrapper class (`int` vs `Integer`) và ép kiểu.

---

## Cấu trúc repo

```
├── docs/           Lý thuyết, chia theo giai đoạn
├── bai-tap/        Code thực hành, chia theo giai đoạn
├── .vscode/        Cấu hình editor dùng chung (line ending, tab size, extension gợi ý)
├── .gitattributes  Chuẩn hóa line ending giữa các máy  → bài 0.4
└── .gitignore      Bỏ qua file build được & file secret → bài 0.4
```

---

## Setup nhanh khi clone về máy mới

1. **Cài JDK 21** → xem [bài 0.2](docs/00-moi-truong/02-cai-dat-jdk.md).
   Lúc cài nhớ bật **cả** `Add to PATH` **và** `Set JAVA_HOME variable` (mặc định tắt).

2. **Mở thư mục repo bằng VS Code.** Editor sẽ tự gợi ý cài extension cần thiết
   (đã khai trong [`.vscode/extensions.json`](.vscode/extensions.json)) — bấm **Install All**.
   Giải thích từng extension: [bài 0.3](docs/00-moi-truong/03-vscode-cho-java-spring.md).

3. **Kiểm tra môi trường** — mở terminal mới, cả 3 lệnh phải ra kết quả:

   ```powershell
   java -version      # JVM chạy được
   javac -version     # có compiler => đúng là JDK, không phải JRE
   echo $env:JAVA_HOME
   ```

4. **Chạy thử code đã có:**

   ```powershell
   cd bai-tap/01-java-core
   javac Hello.java
   java Hello
   ```

> ⚠️ Cài JDK xong phải **đóng hoàn toàn VS Code rồi mở lại**, nếu không `java` vẫn báo not found (biến `PATH` chỉ được đọc lúc tiến trình khởi động).
