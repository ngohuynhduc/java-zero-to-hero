# Bài 0.2 — Cài đặt JDK 21 trên Windows

> **Yêu cầu trước:** đã đọc [bài 0.1](01-java-hoat-dong-nhu-the-nao.md) để hiểu vì sao cài JDK chứ không phải JRE.

---

## 1. Chọn phiên bản & nhà phân phối

Java có nhiều **nhà phân phối** (distribution) — cùng tuân theo một chuẩn (Java SE Specification) nhưng do các đơn vị khác nhau build ra.

### Phiên bản: **JDK 21 (LTS)**

| Lý do | Giải thích |
|---|---|
| Là bản **LTS** | Được hỗ trợ bản vá bảo mật nhiều năm. Doanh nghiệp gần như chỉ dùng LTS |
| Spring Boot 3.x yêu cầu **tối thiểu Java 17** | Nên 8 và 11 là quá cũ, không dùng được |
| Chọn 21 thay vì 17 | Dùng được lâu dài, không phải nâng cấp giữa lộ trình học |

### Nhà phân phối: **Eclipse Temurin (Adoptium)**

| Nhà phân phối | Nhận xét |
|---|---|
| **Eclipse Temurin** ✅ | Miễn phí hoàn toàn, không ràng buộc license thương mại. Phổ biến nhất trong doanh nghiệp |
| Oracle JDK | Điều khoản license phức tạp, có thể phát sinh phí khi dùng thương mại |
| Amazon Corretto | Tốt, phù hợp nếu deploy hẳn trên AWS |
| Azul Zulu / Microsoft Build of OpenJDK | Đều ổn, là lựa chọn thay thế được |

> Về mặt code, các bản này **hành xử giống nhau** — chọn Temurin chỉ để tránh rắc rối license và vì nó phổ thông nhất.

---

## 2. Các bước cài đặt

### Bước 1 — Tải installer

Truy cập: **https://adoptium.net/temurin/releases/?version=21&os=windows&arch=x64**

Chọn đúng:

| Mục | Giá trị |
|---|---|
| Operating System | **Windows** |
| Architecture | **x64** |
| Package Type | **JDK** ← không phải JRE |
| File | đuôi **`.msi`** |

### Bước 2 — Chạy file `.msi`

Bấm Next tới màn hình **Custom Setup**.

### Bước 3 ⚠️ — Bật 2 tùy chọn quan trọng nhất

Mặc định 2 tùy chọn này **bị tắt** (hiện dấu ✗ đỏ). Bắt buộc phải bật:

| Tùy chọn | Ý nghĩa | Vì sao cần |
|---|---|---|
| **Add to PATH** | Thêm thư mục `bin` của JDK vào biến môi trường `PATH` | Để gõ `java`, `javac` ở **bất kỳ thư mục nào** trong terminal mà Windows vẫn tìm thấy chương trình |
| **Set JAVA_HOME variable** | Tạo biến môi trường `JAVA_HOME` trỏ tới thư mục gốc của JDK | Maven, Gradle, Spring Boot, Docker… đều đọc biến này để biết JDK ở đâu. **Thiếu nó là lỗi phổ biến nhất của người mới** |

**Cách bật:** click vào icon ổ đĩa bên cạnh mỗi dòng → chọn **"Will be installed on local hard drive"**.

### Bước 4 — Install

Next → Install → chờ hoàn tất.

### Bước 5 ⚠️ — Đóng hoàn toàn VS Code / terminal rồi mở lại

**Vì sao:** biến môi trường `PATH` chỉ được đọc khi một tiến trình **khởi động**. VS Code hay terminal đang mở sẽ vẫn giữ `PATH` cũ (chưa có Java) cho đến khi bạn restart nó.

> Đây là lý do bạn cài xong mà `java -version` vẫn báo "command not found" — không phải cài sai, chỉ là chưa restart.

---

## 3. Kiểm tra cài đặt thành công

Mở terminal **mới** (PowerShell), chạy lần lượt:

```powershell
java -version
```

Kết quả mong đợi (số phiên bản có thể lệch chút):

```
openjdk version "21.0.5" 2024-10-15 LTS
OpenJDK Runtime Environment Temurin-21.0.5+11 (build 21.0.5+11-LTS)
OpenJDK 64-Bit Server VM Temurin-21.0.5+11 (build 21.0.5+11-LTS, mixed mode, sharing)
```

```powershell
javac -version
```

```
javac 21.0.5
```

```powershell
echo $env:JAVA_HOME
```

```
C:\Program Files\Eclipse Adoptium\jdk-21.0.5.11-hotspot
```

### ✅ Cả 3 lệnh đều ra kết quả → cài xong

| Lệnh | Xác nhận điều gì |
|---|---|
| `java -version` | JVM chạy được → **chạy** được app Java |
| `javac -version` | Compiler tồn tại → **biên dịch** được code (đúng là JDK, không phải JRE) |
| `echo $env:JAVA_HOME` | Các tool build (Maven/Gradle) sẽ tìm được JDK |

---

## 4. Xử lý lỗi thường gặp

| Triệu chứng | Nguyên nhân | Cách sửa |
|---|---|---|
| `java: command not found` sau khi cài | Chưa restart terminal/VS Code | Đóng hoàn toàn rồi mở lại |
| `java -version` chạy được, `javac` thì không | Cài JRE thay vì JDK, hoặc quên bật Add to PATH | Cài lại, chọn Package Type = **JDK** |
| `$env:JAVA_HOME` trả về rỗng | Quên bật **Set JAVA_HOME variable** | Xem mục 5 bên dưới để set tay |
| Maven báo `JAVA_HOME is not set` | Cùng nguyên nhân trên | Như trên |
| `error: illegal character: '\ufeff'` khi compile | File `.java` bị lưu kèm **BOM** (Byte Order Mark) — thường do tạo file bằng PowerShell `Out-File -Encoding utf8` hoặc `>` | Tạo/sửa file `.java` bằng **VS Code** (mặc định UTF-8 không BOM). Nếu file đã bị lỗi: mở file → click `UTF-8 with BOM` ở góc dưới phải → **Save with Encoding** → chọn `UTF-8` |

---

## 5. Set `JAVA_HOME` bằng tay (nếu quên bật lúc cài)

Không cần cài lại. Chạy PowerShell với quyền **Administrator**:

```powershell
# 1. Tìm đường dẫn JDK đã cài
Get-ChildItem "C:\Program Files\Eclipse Adoptium" -Directory

# 2. Set JAVA_HOME ở cấp Machine (thay đường dẫn cho đúng bản của bạn)
[Environment]::SetEnvironmentVariable(
    "JAVA_HOME",
    "C:\Program Files\Eclipse Adoptium\jdk-21.0.5.11-hotspot",
    "Machine"
)
```

**Giải thích:**

- `[Environment]::SetEnvironmentVariable(...)` — gọi API .NET để ghi biến môi trường **vĩnh viễn** vào registry. Nếu chỉ gán `$env:JAVA_HOME = "..."` thì biến **mất khi đóng terminal**.
- Tham số thứ 3 `"Machine"` — set cho **toàn máy** (mọi user). Dùng `"User"` nếu chỉ muốn set cho user hiện tại (không cần quyền Admin).

Sau đó **restart terminal** và kiểm tra lại.

---

➡️ **Bài tiếp:** [0.3 — Cấu hình VS Code cho Java & Spring](03-vscode-cho-java-spring.md)
