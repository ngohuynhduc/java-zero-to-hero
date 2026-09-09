# Bài 0.1 — Java hoạt động như thế nào? JDK vs JRE vs JVM

> **Mục tiêu:** Hiểu mình đang cài *cái gì* và *vì sao*, trước khi gõ dòng code đầu tiên.
> Đây cũng là câu hỏi phỏng vấn kinh điển cho Java Developer.

---

## 1. Java là ngôn ngữ biên dịch *và* thông dịch

Với Python hay JavaScript, bạn viết file rồi chạy luôn. Java thì khác — có **2 bước**:

```
   Hello.java          →         Hello.class         →      Chạy trên máy
  (code bạn viết)            (bytecode - máy đọc)         (kết quả)
        │                            │                         │
        └──── javac (compiler) ──────┴────── java (JVM) ───────┘
              BƯỚC 1: Biên dịch            BƯỚC 2: Thông dịch
```

**Bước 1 — Biên dịch (compile):**
`javac` đọc file `.java`, kiểm tra lỗi cú pháp và lỗi kiểu dữ liệu, rồi dịch ra file `.class` chứa **bytecode**.

Bytecode **không phải** mã máy — nó là một "ngôn ngữ trung gian" mà không CPU nào hiểu trực tiếp.

**Bước 2 — Thông dịch (run):**
`java` khởi động **JVM**. JVM đọc bytecode và dịch tiếp thành mã máy *của đúng hệ điều hành đang chạy*.

### Hệ quả thực tế: lỗi được phát hiện sớm

Vì có bước compile, Java bắt lỗi **trước khi chạy**:

```java
int soLuong = "mot tram";   // ❌ javac báo lỗi ngay, chương trình không chạy được
```

Trong JavaScript, dòng tương đương sẽ chạy bình thường và chỉ nổ khi vào đúng nhánh code đó ở production. Đây là điểm mạnh của Java gọi là **static typing** (kiểu tĩnh) — sẽ học sâu ở Giai đoạn 1.

---

## 2. Vì sao phải qua 2 bước? → "Write Once, Run Anywhere"

Đây chính là lý do Java thống trị backend doanh nghiệp:

```
                    Hello.class (bytecode)
                            │
        ┌───────────────────┼───────────────────┐
        ▼                   ▼                   ▼
   JVM cho Windows     JVM cho Linux      JVM cho macOS
        │                   │                   │
        ▼                   ▼                   ▼
   Chạy trên Win       Chạy trên Linux    Chạy trên Mac
```

Bạn compile **một lần** trên máy Windows, file `.class` đó mang lên **server Linux** chạy được ngay — không cần compile lại. Vì phần "biết cách nói chuyện với hệ điều hành" đã được đóng gói sẵn trong JVM của từng nền tảng.

> 💡 **Điều này rất quan trọng cho phần Cloud (Giai đoạn 11–12):**
> Bạn code trên Windows, nhưng app sẽ chạy trong container Linux trên cloud.
> Nhờ bytecode nên việc đó không thành vấn đề — cùng một file `.jar` chạy được ở cả hai nơi.

---

## 3. JDK vs JRE vs JVM

Ba khái niệm này lồng vào nhau như búp bê Nga:

```
┌─────────────────────────────────────────────┐
│ JDK  (Java Development Kit)                 │
│ = Bộ công cụ để LẬP TRÌNH VIÊN phát triển   │
│                                             │
│  javac (compiler), javadoc, jar, debugger…  │
│                                             │
│  ┌───────────────────────────────────────┐  │
│  │ JRE (Java Runtime Environment)        │  │
│  │ = Bộ tối thiểu để CHẠY app Java       │  │
│  │                                       │  │
│  │  Thư viện chuẩn (String, List, Math…) │  │
│  │                                       │  │
│  │  ┌─────────────────────────────────┐  │  │
│  │  │ JVM (Java Virtual Machine)      │  │  │
│  │  │ = Máy ảo thực thi bytecode      │  │  │
│  │  │   + Garbage Collector (dọn RAM) │  │  │
│  │  └─────────────────────────────────┘  │  │
│  └───────────────────────────────────────┘  │
└─────────────────────────────────────────────┘
```

| | Chứa gì | Ai cần |
|---|---|---|
| **JVM** | Máy ảo chạy bytecode + tự động dọn bộ nhớ (Garbage Collector) | Lõi bên trong, không cài riêng |
| **JRE** | JVM + thư viện chuẩn | Người chỉ **chạy** app Java |
| **JDK** | JRE + `javac` + công cụ dev | **Lập trình viên** — vì cần compile |

### ✅ Kết luận: phải cài JDK, không phải JRE

Vì bạn cần `javac` để biên dịch code mình viết.

---

## 4. Khái niệm cần ghi nhớ

### Garbage Collector (GC)

Cơ chế **tự động giải phóng bộ nhớ** không còn được dùng nữa.

- Trong C/C++: lập trình viên phải tự gọi `free()` / `delete`. Quên → **memory leak**.
- Trong Java: GC tự phát hiện object không còn ai tham chiếu tới và thu hồi RAM.

Đây là lý do Java an toàn về bộ nhớ hơn C++, nhưng cũng là chủ đề **tối ưu hiệu năng** quan trọng khi lên production (chọn loại GC, tinh chỉnh heap size).

### LTS (Long Term Support)

Java phát hành bản mới mỗi 6 tháng, nhưng chỉ một số bản là **LTS** — được hỗ trợ bản vá bảo mật nhiều năm.

Các bản LTS: **8, 11, 17, 21, 25**. Doanh nghiệp hầu như chỉ dùng bản LTS.

---

## 5. Tự kiểm tra

Trả lời được 4 câu này là đã hiểu bài:

1. File `.class` chứa gì? Nó có phải mã máy không?
2. Vì sao cùng một file `.class` chạy được trên cả Windows và Linux?
3. Nếu máy chỉ có JRE mà không có JDK, bạn có compile được code không? Vì sao?
4. Java có cần tự giải phóng bộ nhớ như C++ không? Cơ chế nào lo việc đó?

<details>
<summary>Đáp án</summary>

1. Chứa **bytecode** — ngôn ngữ trung gian. **Không** phải mã máy, CPU không hiểu trực tiếp.
2. Vì mỗi hệ điều hành có JVM riêng, JVM đó chịu trách nhiệm dịch bytecode sang mã máy của nền tảng mình.
3. **Không.** JRE chỉ có JVM + thư viện để *chạy*, thiếu `javac` để *biên dịch*.
4. **Không cần.** **Garbage Collector** tự động thu hồi bộ nhớ của object không còn được tham chiếu.

</details>

---

➡️ **Bài tiếp:** [0.2 — Cài đặt JDK 21](02-cai-dat-jdk.md)
