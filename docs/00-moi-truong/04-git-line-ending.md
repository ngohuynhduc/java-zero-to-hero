# Bài 0.4 — Git cho Java project: line ending & `.gitattributes`

> **Bối cảnh:** Khi commit lần đầu, Git cảnh báo `LF will be replaced by CRLF the next time Git touches it`.
> Bài này giải thích cảnh báo đó và cách xử lý gốc rễ — kiến thức dùng cho mọi project, không riêng Java.

---

## 1. Vấn đề: CR, LF, CRLF

Ký tự xuống dòng là di sản từ máy đánh chữ cơ khí, vốn cần **2 động tác riêng biệt**:

| Ký tự | Tên | Mã byte | Động tác gốc |
|---|---|---|---|
| `\r` | **CR** (Carriage Return) | 13 | Đẩy con lăn về đầu dòng |
| `\n` | **LF** (Line Feed) | 10 | Cuộn giấy xuống 1 dòng |

Khi lên máy tính, các hệ điều hành chọn khác nhau:

| Hệ điều hành | Dùng | Byte |
|---|---|---|
| Windows | **CRLF** (`\r\n`) | 13, 10 |
| Linux, macOS | **LF** (`\n`) | 10 |

> Đó là lý do mở file Linux bằng Notepad cũ thì thấy nội dung dồn thành **một dòng dài** — Notepad chỉ hiểu CRLF.

---

## 2. Vì sao đây là vấn đề của Git

Git so sánh file theo **từng byte**. Nếu bạn commit file dùng LF, rồi mở ở máy khác và editor lưu lại thành CRLF, thì với Git **toàn bộ mọi dòng đều đã bị sửa**:

```
- public class Hello {      ← Git thấy: xóa dòng này
+ public class Hello {      ← Git thấy: thêm dòng này
```

Nội dung y hệt, chỉ khác byte xuống dòng vô hình. Hậu quả thực tế:

| Hậu quả | Mô tả |
|---|---|
| Diff rác | `git diff` hiện file 500 dòng "thay đổi toàn bộ" → không review được |
| Mất `git blame` | Không truy được ai viết dòng nào, khi nào → mất lịch sử điều tra bug |
| Merge conflict giả | Xung đột ở những chỗ vốn không ai sửa cùng |

---

## 3. Hai cách xử lý — vì sao `core.autocrlf` chưa đủ

| | `core.autocrlf = true` | `.gitattributes` |
|---|---|---|
| Phạm vi | **Cấu hình của từng máy** | **File nằm trong repo** |
| Đi theo repo khi clone? | ❌ Không | ✅ Có |
| Ai phải làm gì | Mỗi người tự set trên máy mình | Không ai phải làm gì |
| Độ ưu tiên | Thấp hơn | ✅ **Thắng `core.autocrlf`** |

`core.autocrlf` là cấu hình cá nhân — clone repo sang máy Mac/Linux (mặc định `false`) là vấn đề quay lại ngay.

**`.gitattributes` giải quyết gốc:** quy tắc nằm trong repo, clone tới đâu áp dụng tới đó, và ghi đè `core.autocrlf` của máy.

---

## 4. Cơ chế: Git tách biệt 2 nơi lưu trữ

```
   Working Directory                  Git Repository
   (file bạn đang sửa)                (.git — nơi lưu commit)

   CRLF trên Windows      ──add──►
   LF trên Linux/macOS     (chuẩn hóa)     LUÔN LƯU LF
                          ◄─checkout──
   (theo eol= khai báo)    (chuyển đổi)
```

**Nguyên tắc: trong repo luôn lưu LF.** Working directory thì tùy khai báo.

### Cú pháp

| Khai báo | Nghĩa |
|---|---|
| `text=auto` | Git tự đoán text/binary; nếu là text thì chuẩn hóa về LF khi lưu vào repo |
| `text eol=lf` | Là text, working directory **cũng luôn dùng LF** (kể cả trên Windows) |
| `text eol=crlf` | Là text, nhưng working directory **phải là CRLF** — dùng cho `.bat`, `.cmd` |
| `binary` | **Không chuyển đổi gì cả** — tương đương `-text -diff` |

### ⚠️ `binary` — phần dễ bỏ qua nhưng nguy hiểm nhất

File `.jar` chứa byte 13 và 10 nằm rải rác trong dữ liệu nén. Nếu Git tưởng đó là text rồi "chuẩn hóa" byte 13 → file `.jar` **hỏng hoàn toàn**, và lỗi rất khó đoán ra nguyên nhân.

### ⚠️ `mvnw` / `gradlew` — bắt buộc LF

Đây là **shell script** chạy trên Linux/macOS. Nếu bị đổi thành CRLF, Linux đọc dòng `#!/bin/sh\r` và tưởng tên chương trình là `sh\r`:

```
bad interpreter: /bin/sh^M: No such file or directory
```

Đây là lỗi kinh điển khi CI/CD trên Linux fail mà máy Windows chạy vẫn ổn.

---

## 5. Áp dụng

### Bước 1 — Tạo file `.gitattributes` ở gốc repo

Xem file [`.gitattributes`](../../.gitattributes) của repo này (có comment giải thích từng nhóm).

### Bước 2 — Kiểm tra Git hiểu đúng quy tắc

`git check-attr` cho biết một file *sẽ* được xử lý thế nào, **không cần file đó tồn tại**:

```powershell
git check-attr text eol diff -- README.md Hello.java run.bat mvnw app.jar
```

Kết quả mong đợi:

```
README.md:  text: set      eol: lf      diff: unspecified
Hello.java: text: set      eol: lf      diff: java
run.bat:    text: set      eol: crlf    diff: unspecified
mvnw:       text: set      eol: lf      diff: unspecified
app.jar:    text: unset    eol: lf      diff: unset
```

> **Lưu ý về dòng `app.jar`:** thấy `eol: lf` nhưng **không đáng lo**. Giá trị `eol` chỉ có tác dụng khi file được coi là text. Ở đây `text: unset` (do khai báo `binary`) nên Git bỏ qua `eol` hoàn toàn — không có chuyển đổi nào xảy ra.

### Bước 3 — Chuẩn hóa lại file đã commit trước đó

```powershell
git add --renormalize .
git status --short
```

`--renormalize` bắt Git đọc lại **toàn bộ** file đang được theo dõi và áp quy tắc mới.

- Nếu có file hiện lên → line ending của chúng vừa được sửa, commit lại là xong.
- Nếu **không** file nào hiện lên → repo vốn đã lưu LF sẵn, không cần làm gì thêm. (Đây là trường hợp của repo này, vì máy đang set `core.autocrlf=true`.)

> Chỉ cần chạy `--renormalize` **một lần** khi thêm `.gitattributes` vào repo đã có commit. Về sau Git tự xử lý.

---

## 6. Về `.gitignore` cho Java

Cùng nhóm kiến thức. Nguyên tắc: **không commit thứ build lại được, và không commit secret.**

| Bỏ qua | Vì sao |
|---|---|
| `*.class` | Bytecode — `javac` sinh lại được từ `.java` |
| `target/` (Maven), `build/` (Gradle) | Toàn bộ output build — `mvn clean package` sinh lại được |
| `.settings/`, `.classpath`, `bin/` | Cache của Eclipse JDT Language Server, chỉ có ý nghĩa trên máy hiện tại |
| `.env`, `application-secret*` | **Mật khẩu DB, JWT secret, API key** — commit lên là sự cố bảo mật |

### ⚠️ Ngoại lệ quan trọng: PHẢI commit Maven/Gradle Wrapper

```gitignore
# GIỮ LẠI: .mvn/wrapper/, mvnw, mvnw.cmd
```

Wrapper là script tự tải đúng phiên bản Maven/Gradle mà project cần. Nhờ nó, người clone repo **build được ngay mà không cần cài Maven** trên máy — và cả team dùng chung một phiên bản build tool. Sẽ học kỹ ở Giai đoạn 4.

> 💡 Git chỉ theo dõi file **chưa** được commit theo `.gitignore`. Nếu đã commit nhầm secret rồi mới thêm vào `.gitignore` thì file **vẫn còn trong lịch sử** — phải xóa khỏi history và **đổi luôn mật khẩu đó**, vì coi như đã bị lộ.

---

## 7. Tự kiểm tra

1. Vì sao repo luôn lưu LF, mà file trên máy Windows vẫn có thể là CRLF?
2. `.gitattributes` và `core.autocrlf` — cái nào thắng? Vì sao nên dùng cái đó?
3. Vì sao `*.jar` phải khai báo `binary`?
4. Vì sao `mvnw` bắt buộc LF nhưng `mvnw.cmd` lại phải CRLF?

<details>
<summary>Đáp án</summary>

1. Git chuyển đổi **hai chiều**: chuẩn hóa về LF khi `add` vào repo, và chuyển sang định dạng khai báo ở `eol=` khi `checkout` ra working directory.
2. **`.gitattributes` thắng.** Nên dùng nó vì file này nằm trong repo → đi theo mọi lần clone, không phụ thuộc cấu hình từng máy, không cần ai làm gì thêm.
3. File `.jar` là dữ liệu nén, chứa byte 13 và 10 rải rác. Nếu Git coi là text và chuẩn hóa byte 13 thì file bị hỏng.
4. `mvnw` là **shell script cho Linux/macOS** — CRLF sẽ gây lỗi `bad interpreter: /bin/sh^M`. `mvnw.cmd` là **batch script cho Windows** — `cmd.exe` cần CRLF.

</details>

---

⬅️ **Bài trước:** [0.3 — Cấu hình VS Code cho Java & Spring](03-vscode-cho-java-spring.md)
➡️ **Bài tiếp:** Giai đoạn 1 — Chương trình Java đầu tiên
