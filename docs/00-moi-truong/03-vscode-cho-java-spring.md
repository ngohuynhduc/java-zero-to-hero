# Bài 0.3 — Cấu hình VS Code cho Java & Spring

> **Câu hỏi:** Không có IntelliJ IDEA Ultimate thì VS Code có làm Spring được không?
> **Trả lời:** Được, và khá tốt. VS Code + 2 extension pack dưới đây là bộ hoàn toàn miễn phí, đủ dùng cho cả lộ trình học.

---

## 1. Bối cảnh: IntelliJ Community không hỗ trợ Spring

Đây là điều nhiều người mới không biết:

| IDE | Giá | Hỗ trợ Spring |
|---|---|---|
| IntelliJ IDEA **Ultimate** | ~$169/năm | ✅ Tốt nhất thị trường |
| IntelliJ IDEA **Community** | Miễn phí | ❌ **Không** có Spring support |
| **VS Code + extensions** | Miễn phí | ✅ Khá tốt (~80% Ultimate) |

Nên nếu không mua Ultimate, **VS Code là lựa chọn miễn phí tốt hơn IntelliJ Community** cho việc học Spring.

---

## 2. Bắt buộc: 2 extension pack

### 2.1. Extension Pack for Java (Microsoft)

**ID:** `vscjava.vscode-java-pack`

Đây là nền tảng cho mọi việc liên quan Java. Nó là một **pack** — cài 1 lần sẽ tự kéo về 6 extension con:

| Extension con | ID | Làm gì |
|---|---|---|
| Language Support for Java by Red Hat | `redhat.java` | **Quan trọng nhất.** Chạy Eclipse JDT Language Server ở nền → autocomplete, báo lỗi khi đang gõ, go-to-definition, rename/refactor |
| Debugger for Java | `vscjava.vscode-java-debug` | Đặt breakpoint, chạy từng dòng, xem giá trị biến |
| Test Runner for Java | `vscjava.vscode-java-test` | Chạy JUnit/TestNG bằng nút ▶ ngay cạnh test (dùng nhiều ở Giai đoạn 10) |
| Maven for Java | `vscjava.vscode-maven` | Quản lý `pom.xml`, chạy lifecycle (clean/compile/package) từ UI |
| Gradle for Java | `vscjava.vscode-gradle` | Tương tự cho Gradle |
| Project Manager for Java | `vscjava.vscode-java-dependency` | Panel cây project, xem thư viện đang dùng, tạo class/package bằng UI |

> 💡 **Language Server là gì?** Là một tiến trình chạy riêng biệt, hiểu ngữ nghĩa code của bạn (biến này kiểu gì, method này ở đâu) và trả lời cho editor qua **LSP (Language Server Protocol)**.
> Đây là lý do lần đầu mở project Java, VS Code hiện thanh loading "Importing projects…" — nó đang build index cho toàn bộ source + thư viện.

### 2.2. Spring Boot Extension Pack

**ID:** `vmware.vscode-boot-dev-pack`
*(Do team Spring phát triển — publisher hiện là VMware/Broadcom)*

Đây là phần làm nên khác biệt khi làm Spring. Gồm 3 extension:

| Extension con | ID | Làm gì |
|---|---|---|
| **Spring Boot Tools** | `vmware.vscode-spring-boot` | Autocomplete + validate cho `application.properties` / `application.yml`. Navigate nhanh tới `@RequestMapping`, xem Bean nào đang được inject |
| **Spring Initializr Java Support** | `vscjava.vscode-spring-initializr` | Tạo project Spring Boot mới ngay trong VS Code (chọn version, dependency…) — thay cho việc vào web start.spring.io |
| **Spring Boot Dashboard** | `vscjava.vscode-spring-boot-dashboard` | Panel start/stop/debug app bằng 1 click. Xem danh sách **Beans** và **REST endpoints** đang có |

**Vì sao 2 extension này giá trị cao khi học Spring:**

- `application.properties` có **hàng trăm** key cấu hình. Không có autocomplete thì phải tra docs liên tục, và gõ sai một chữ là app chạy sai mà không báo lỗi.
- Spring Boot Dashboard cho bạn **nhìn thấy** Bean và endpoint — cực kỳ hữu ích để hiểu Dependency Injection ở Giai đoạn 5, vì DI vốn "vô hình" trong code.

---

## 3. Nên có thêm

| Extension | ID | Dùng ở GĐ | Lý do |
|---|---|---|---|
| **REST Client** | `humao.rest-client` | 6+ | Test API bằng file `.http` viết ngay trong project. **Thay thế Postman**, và file `.http` commit được vào git để chia sẻ với team |
| **Docker** | `ms-azuretools.vscode-docker` | 7, 11 | Quản lý container (PostgreSQL, Redis…), viết Dockerfile có autocomplete |
| **SonarQube for IDE** | `SonarSource.sonarlint-vscode` | 8+ | Phát hiện code smell & lỗi bảo mật khi đang gõ. Dạy được nhiều best practice |
| **GitLens** | `eamodio.gitlens` | Mọi GĐ | Xem ai sửa dòng nào, khi nào, vì sao |
| **Error Lens** | `usernamehw.errorlens` | Mọi GĐ | Hiện lỗi ngay cuối dòng thay vì phải hover — đỡ bỏ sót |

### Về Lombok

Lombok là thư viện giúp bỏ bớt code lặp (`getter`/`setter`/`constructor`). **Không cần cài extension riêng** — `redhat.java` đã hỗ trợ sẵn từ phiên bản 1.19 (bật mặc định qua setting `java.jdt.ls.lombokSupport.enabled`).

---

## 4. Cách cài

### Cách 1 — Command line (nhanh nhất)

```powershell
code --install-extension vmware.vscode-boot-dev-pack
code --install-extension humao.rest-client
```

### Cách 2 — Trong VS Code

`Ctrl + Shift + X` → dán ID vào ô tìm kiếm → **Install**.

### Kiểm tra

```powershell
code --list-extensions | Select-String "java|spring|boot"
```

---

## 5. Giới hạn so với IntelliJ Ultimate

Nói thật để bạn biết trước, không bị bất ngờ:

| Việc | VS Code | IntelliJ Ultimate |
|---|---|---|
| Autocomplete, báo lỗi | ✅ Tốt | ✅ Tốt |
| Debug | ✅ Tốt | ✅ Tốt |
| Refactor cơ bản (rename, extract method) | ✅ Được | ✅ Tốt hơn |
| Refactor phức tạp (đổi kiến trúc, move class kèm sửa import toàn repo) | ⚠️ Yếu hơn rõ rệt | ✅ Rất mạnh |
| Validate câu JPQL / SQL trong `@Query` | ❌ Gần như không | ✅ Có, bắt lỗi query ngay |
| Navigate Bean / cấu hình Spring | ⚠️ Cơ bản | ✅ Sâu hơn |
| Tích hợp Database (chạy query, xem schema) | ⚠️ Cần extension riêng | ✅ Có sẵn |
| Tốc độ mở project lớn | ✅ Nhẹ hơn | ⚠️ Nặng RAM |

### Kết luận cho lộ trình học

**VS Code hoàn toàn đủ cho Giai đoạn 0 → 12.** Những điểm yếu ở trên chủ yếu ảnh hưởng khi làm codebase lớn hàng nghìn file trong công ty.

> 💡 **Mẹo:** Nếu sau này cần Ultimate, JetBrains cho **license miễn phí 1 năm** cho sinh viên/giảng viên (có email `.edu`), và các dự án open-source cũng xin được license miễn phí.

---

## 6. Trạng thái máy hiện tại

Ghi lại để khi setup máy mới biết cần cài gì.

| Thành phần | Trạng thái |
|---|---|
| Git | ✅ 2.48.1 |
| VS Code | ✅ Đã có |
| Extension Pack for Java | ✅ Đã có (cả 6 extension con) |
| **Spring Boot Extension Pack** | ⬜ Cần cài (khi tới Giai đoạn 6) |
| **REST Client** | ⬜ Cần cài (khi tới Giai đoạn 6) |
| JDK 21 | ✅ Temurin 21.0.12.1 — đã verify compile + run |
| Maven | ⬜ Cần cài (khi tới Giai đoạn 4) |
| Docker | ⬜ Cần cài (khi tới Giai đoạn 7) |

> **Lưu ý:** Spring Boot Extension Pack chưa cần cài ngay ở Giai đoạn 0–5. Cài khi bắt đầu Giai đoạn 6 (Spring Boot) là hợp lý — tránh việc extension chạy nền tốn RAM khi chưa dùng tới.

---

⬅️ **Bài trước:** [0.2 — Cài đặt JDK 21](02-cai-dat-jdk.md)
➡️ **Bài tiếp:** Giai đoạn 1 — Chương trình Java đầu tiên
