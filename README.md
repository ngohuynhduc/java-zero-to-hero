# Study Java — Học Backend từ Java Spring đến Cloud

Repo ghi lại toàn bộ quá trình tự học Backend với Java. Mỗi bài gồm **lý thuyết giải thích trước**, rồi **code mẫu + thực hành**.

> Cách học đã chọn: giảng lý thuyết → đưa code mẫu → tự gõ lại code, không copy-paste.

---

## Lộ trình

| GĐ | Nội dung | Mục tiêu | Trạng thái |
|---|---|---|---|
| **0** | Môi trường: JDK, Maven, VS Code | Chạy được dòng Java đầu tiên | 🔄 Đang làm |
| **1** | Java core: biến, kiểu dữ liệu, điều kiện, vòng lặp, method | Viết logic cơ bản | ⬜ |
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

---

## Setup nhanh khi clone về máy mới

1. Cài JDK 21 → xem [bài 0.2](docs/00-moi-truong/02-cai-dat-jdk.md)
2. Cài extension VS Code → xem [bài 0.3](docs/00-moi-truong/03-vscode-cho-java-spring.md)
3. Kiểm tra: mở terminal mới, chạy `java -version` và `javac -version`
