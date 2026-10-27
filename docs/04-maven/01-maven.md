# Bài 4.1 — Maven

> **Mục tiêu:** Hiểu Maven giải quyết vấn đề gì (phụ thuộc bắc cầu, quy trình build), đọc được `pom.xml`, nắm vòng đời build — và vì sao một file `.jar` build thành công vẫn có thể không chạy được.
>
> **Code thực hành:** [`bai-tap/04-maven/maven-dau-tien/`](../../bai-tap/04-maven/maven-dau-tien/)
>
> Bài mở đầu Giai đoạn 4.

---

## 1. Vấn đề: build bằng tay không còn đủ

Một dự án backend thật có hàng trăm file `.java`, hàng chục thư viện bên ngoài, và nhiều bước (compile, test, đóng gói). Thử dùng một thư viện ngoài (`commons-text`) **không có công cụ gì**:

```java
import org.apache.commons.text.WordUtils;
...
System.out.println(WordUtils.capitalizeFully("nguyen van an"));
```

**Chưa có thư viện:**
```
error: package org.apache.commons.text does not exist
```

**Tự tải `commons-text-1.15.0.jar`, chỉ cho `javac` qua `-cp`:**
```
javac -cp "commons-text-1.15.0.jar" TayKhong.java     → compile OK
java  -cp "commons-text-1.15.0.jar;." TayKhong
Exception in thread "main" java.lang.NoClassDefFoundError: org/apache/commons/lang3/StringUtils
    at org.apache.commons.text.WordUtils.capitalizeFully(WordUtils.java:250)
```

Compile qua, **chạy thì nổ**: bên trong `commons-text` lại dùng `commons-lang3` mà bạn không hề biết. Lỗi thuộc **cửa 2** ([bài 1.2](../01-java-core/02-compile-time-vs-runtime.md)) — `javac` không cần `commons-lang3` vì code của bạn không gọi thẳng nó.

```
   Code của bạn ──cần──► commons-text ──cần──► commons-lang3 ──cần──► ...?
                         (bạn biết)            (bạn KHÔNG biết)
```

Gọi là **phụ thuộc bắc cầu** (transitive dependency). Một dự án Spring Boot thật có **hơn 100** thư viện lồng nhau nhiều tầng.

> 💡 `.jar` là **file nén chứa các file `.class`** (thực chất là định dạng zip). Thư viện Java được phân phối dưới dạng file `.jar`.

---

## 2. Maven = công cụ build + quản lý thư viện

| npm (JavaScript) | Maven (Java) | Ghi chú |
|---|---|---|
| `package.json` | `pom.xml` | Khai báo dự án và thư viện |
| npm registry | **Maven Central** | Kho thư viện công cộng |
| `node_modules/` trong **từng** dự án | `~/.m2/repository/` **dùng chung** | Tải một lần, mọi dự án dùng lại |
| `npm install` | Tự động khi build | Không có bước install riêng |
| `npm run build` | `mvn package` | |
| Tên gói: `react@18.3.1` | Tọa độ: `groupId:artifactId:version` | |
| `dist/` | `target/` | Thư mục kết quả build |

Hai khác biệt đáng chú ý:

- **Không có `node_modules` trong từng dự án.** Mười dự án cùng dùng một thư viện thì nó chỉ được lưu **một lần**. Nhiều phiên bản sống cạnh nhau theo đường dẫn `groupId/artifactId/version`.
- **Maven quy định cả quy trình build** — compile, test, đóng gói theo thứ tự cố định. npm để bạn tự viết script.

---

## 3. Tọa độ: `groupId : artifactId : version`

```xml
<groupId>org.apache.commons</groupId>      ← ai làm ra (tổ chức)
<artifactId>commons-text</artifactId>       ← tên thư viện
<version>1.15.0</version>                   ← phiên bản
```

**`groupId` viết theo tên miền đảo ngược** — `org.apache.commons` là của tổ chức sở hữu `commons.apache.org`. **Tên miền là duy nhất trên toàn thế giới**, nên dùng làm tiền tố thì không bao giờ trùng. npm giải quyết cùng vấn đề bằng scope `@angular/core`.

---

## 4. Quy ước thay vì cấu hình

```
maven-dau-tien/
├── pom.xml                         ← khai báo dự án
├── src/
│   ├── main/
│   │   └── java/                   ← code chính
│   │       └── vn/hoc/maven/
│   │           └── App.java
│   └── test/
│       └── java/                   ← code test (Giai đoạn 10)
└── target/                         ← Maven tự sinh, KHÔNG commit
```

Mọi dự án Maven trên thế giới đều có hình dạng này. Spring Boot dùng đúng cấu trúc này. `target/` đã nằm trong `.gitignore` từ [bài 0.4](../00-moi-truong/04-git-line-ending.md).

### `package` — không gian tên của class

```java
package vn.hoc.maven;
```

Tên đầy đủ của class giờ là `vn.hoc.maven.App`. Nhờ vậy `App` của bạn không đụng hàng với hàng nghìn class `App` khác.

**Tên package phải khớp đường dẫn thư mục** bên dưới `src/main/java/`:

```
package vn.hoc.maven;    ⟺    src/main/java/vn/hoc/maven/App.java
```

Giống quy tắc "tên file trùng tên class public" ở [bài 1.1](../01-java-core/01-chuong-trinh-dau-tien.md), ở cấp thư mục. Quy ước đặt tên: tên miền đảo ngược, viết thường toàn bộ ([bài 1.3](../01-java-core/03-bien-va-kieu-du-lieu.md)).

---

## 5. Giải phẫu `pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>                          <!-- ① -->

    <groupId>vn.hoc</groupId>                                   <!-- ② -->
    <artifactId>maven-dau-tien</artifactId>
    <version>1.0.0</version>

    <properties>                                                <!-- ③ -->
        <maven.compiler.release>21</maven.compiler.release>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>                                              <!-- ④ -->
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-text</artifactId>
            <version>1.15.0</version>
        </dependency>
    </dependencies>
</project>
```

| | Phần | Ý nghĩa |
|---|---|---|
| ① | `modelVersion` | Phiên bản **định dạng** file pom, luôn `4.0.0`. Không liên quan phiên bản Maven |
| ② | Tọa độ dự án | Như mục 3 |
| ③ | `maven.compiler.release` | Compile cho **Java 21** |
| ③ | `sourceEncoding` | Đọc file nguồn theo UTF-8 — tránh lỗi tiếng Việt trên Windows |
| ④ | `dependencies` | Chỉ khai thư viện **trực tiếp dùng** — cái bắc cầu Maven tự lo |

---

## 6. Vòng đời build (lifecycle)

```
   validate → compile → test → package → verify → install → deploy
      │          │        │       │                  │          │
   kiểm tra  biên dịch  chạy   đóng gói            chép vào   đẩy lên
   pom.xml   src/main   test    thành .jar         ~/.m2      kho chung
```

**Gọi một phase là chạy luôn MỌI phase đứng trước nó.** `mvn package` thực tế chạy:

```
--- resources:3.4.0:resources       ← chép file tài nguyên
--- compiler:3.15.0:compile         ← biên dịch src/main/java
--- resources:3.4.0:testResources
--- compiler:3.15.0:testCompile     ← biên dịch src/test/java
--- surefire:3.5.4:test             ← chạy test
--- jar:3.5.0:jar                   ← đóng gói
```

Mỗi phase giao việc cho một **plugin** (`compiler` gọi `javac` hộ bạn). Lần đầu chạy Maven tải cả các plugin này — chính Maven cũng là một tập hợp thư viện.

| Lệnh | Dùng khi |
|---|---|
| `mvn compile` | Chỉ biên dịch, kiểm tra lỗi |
| `mvn package` | Tạo file `.jar` |
| `mvn clean` | Xóa sạch `target/` |
| `mvn clean package` | Build lại từ đầu |
| `mvn dependency:tree` | Xem cây thư viện, kể cả bắc cầu |

---

## 7. Scope — thư viện cần ở giai đoạn nào?

| Scope | Compile? | Test? | Chạy? | Ví dụ |
|---|---|---|---|---|
| `compile` *(mặc định)* | ✅ | ✅ | ✅ | `commons-text`, Spring |
| `test` | ❌ | ✅ | ❌ | JUnit (GĐ 10) |
| `runtime` | ❌ | ✅ | ✅ | Driver PostgreSQL (GĐ 7) |
| `provided` | ✅ | ✅ | ❌ — môi trường đã có sẵn | Lombok, Servlet API |

---

## 8. Maven Wrapper

[Bài 0.4](../00-moi-truong/04-git-line-ending.md) có dòng `mvnw text eol=lf` và *"phải commit Maven Wrapper"*. `mvnw` / `mvnw.cmd` là script **tự tải đúng phiên bản Maven** dự án cần — người clone chạy `./mvnw package` mà **không cần cài Maven**, cả team dùng chung một phiên bản. Dự án Spring Boot tạo từ Spring Initializr luôn kèm sẵn wrapper (GĐ 6).

---

## 9. Cài Maven trên Windows

Bản dùng: **3.9.16** (bản ổn định mới nhất trên Maven Central lúc học; Maven 4 vẫn ở bản RC).

1. Tải `https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.16/apache-maven-3.9.16-bin.zip`
2. Giải nén vào `C:\tools\apache-maven-3.9.16\`
3. Thêm `bin` vào `PATH` của user (PowerShell, không cần Admin):

   ```powershell
   $cu = [Environment]::GetEnvironmentVariable("Path", "User")
   [Environment]::SetEnvironmentVariable("Path", "$cu;C:\tools\apache-maven-3.9.16\bin", "User")
   ```

   Dòng 1 đọc `PATH` hiện tại — bắt buộc, nếu không dòng 2 sẽ **ghi đè mất** đường dẫn cũ. Cách ghi vĩnh viễn như `JAVA_HOME` ở [bài 0.2](../00-moi-truong/02-cai-dat-jdk.md).

4. Đóng hoàn toàn VS Code rồi mở lại.
5. `mvn -v` phải thấy `Apache Maven 3.9.16` và `Java version: 21...` — Maven tìm JDK qua `JAVA_HOME`.

### ⚠️ Bẫy PowerShell: tham số `-D` có dấu chấm phải bọc nháy

```powershell
mvn exec:java -Dexec.mainClass=vn.hoc.maven.App       # ❌
```
```
[ERROR] Unknown lifecycle phase ".mainClass=vn.hoc.maven.App".
```

PowerShell cắt tham số tại dấu chấm. Viết đúng: `mvn exec:java "-Dexec.mainClass=vn.hoc.maven.App"`.

---

## 10. Kết quả thực nghiệm

```
mvn compile lần 1:
  Downloaded from central: .../commons-text/1.15.0/commons-text-1.15.0.jar
  Downloaded from central: .../commons-lang3/3.20.0/commons-lang3-3.20.0.jar   ← không hề khai báo
  ... (cùng nhiều plugin của Maven)
  Total time:  5.214 s

mvn compile lần 2:
  Nothing to compile - all classes are up to date.
  Total time:  0.407 s                                                          ← ~12 lần

mvn dependency:tree:
  vn.hoc:maven-dau-tien:jar:1.0.0
  \- org.apache.commons:commons-text:jar:1.15.0:compile
     \- org.apache.commons:commons-lang3:jar:3.20.0:compile

Bên trong target/maven-dau-tien-1.0.0.jar (2.231 byte):
  META-INF/MANIFEST.MF
  vn/hoc/maven/App.class                     ← 651 byte, toàn bộ code của bạn
  META-INF/maven/vn.hoc/maven-dau-tien/pom.xml
  META-INF/maven/vn.hoc/maven-dau-tien/pom.properties

mvn -q compile exec:java "-Dexec.mainClass=vn.hoc.maven.App":
  Nguyen Van An
```

### 10.1. Jar nhỏ vì **chỉ chứa code của bạn**, không phải vì minify

Bytecode Java **không bị minify** như JavaScript. Jar có nén (định dạng zip), nhưng lý do chính khiến nó nhẹ là **không có `commons-text` hay `commons-lang3` trong đó** — chúng nằm ở `~/.m2`. `mvn package` mặc định chỉ đóng gói code **của dự án này**.

### 10.2. 🚨 `java -cp target/...jar` — lỗi **R**, không phải **C**

```
java -cp target/maven-dau-tien-1.0.0.jar vn.hoc.maven.App
Exception in thread "main" java.lang.NoClassDefFoundError: org/apache/commons/text/WordUtils
```

Đây là lệnh **`java`**, không phải `javac`. Bước biên dịch đã xong bên trong `mvn package`, lúc đó Maven cấp đủ thư viện cho `javac` — nên bytecode hoàn toàn hợp lệ. Có **hai classpath khác nhau**, và bạn chỉ cấp một:

```
   COMPILE (trong mvn package)              RUN (lệnh java bạn gõ)
   ───────────────────────────              ───────────────────────
   javac được Maven đưa cho:                java chỉ được bạn đưa cho:
     • code của bạn                            • maven-dau-tien-1.0.0.jar
     • commons-text-1.15.0.jar                 (không có commons-text!)
     • commons-lang3-3.20.0.jar
           │                                          │
           ▼                                          ▼
     compile OK ✅                         NoClassDefFoundError 💥
```

#### 🔬 JVM nạp class **khi cần tới** — lazy class loading

Thêm hai dòng in trước lời gọi `WordUtils`:

```
1. main da chay
2. chuan bi goi WordUtils...
Exception in thread "main" java.lang.NoClassDefFoundError: org/apache/commons/text/WordUtils
    at vn.hoc.maven.App.main(App.java:9)
```

JVM không kiểm tra trước chương trình cần những class nào. Nó chạy, và chỉ đi tìm `WordUtils` **đúng lúc dòng 9 cần tới**.

Ở production: một chức năng hiếm khi dùng — xuất báo cáo cuối tháng — có thể thiếu thư viện mà server vẫn khởi động bình thường, chạy êm suốt 29 ngày, rồi nổ đúng ngày 30.

> 🔗 Cùng nguyên tắc ở [bài 2.4](../02-oop/04-da-hinh.md): `javac` chỉ chặn những gì nó **chứng minh được** là sai **lúc biên dịch**. Thư viện có mặt lúc chạy hay không thì nó không thể biết.

Đây là lý do **Spring Boot** có plugin tạo **fat jar** — chứa **cả** code của bạn **lẫn** toàn bộ thư viện, chạy bằng `java -jar app.jar`. Gặp ở GĐ 6, dùng để đóng gói Docker ở GĐ 11.

Còn `java -jar target/maven-dau-tien-1.0.0.jar` thì báo `no main manifest attribute` — jar thường không ghi class nào chứa `main`.

### 10.3. 🚨 `BUILD SUCCESS` không có nghĩa là "build đúng"

Đặt `App.java` vào `src/vn/hoc/maven/` (thiếu `main/java`):

```
[INFO] No sources to compile
[WARNING] JAR will be empty - no content was marked for inclusion!
[INFO] BUILD SUCCESS
```

Maven tìm code theo **quy ước**. Thư mục quy ước trống thì đó là "dự án chưa có code" — **không phải lỗi**.

Ở môi trường thật, build chạy tự động trên server CI (GĐ 11) và người ta chỉ nhìn **xanh hay đỏ**. Một dòng `WARNING` lẫn giữa hàng trăm dòng log thì không ai đọc. Pipeline xanh, jar rỗng được deploy, lỗi chỉ lộ ra khi server khởi động.

> 📌 Khi build "thành công" mà chương trình cư xử lạ: **đọc các dòng `WARNING`**, và kiểm tra bên trong jar bằng `jar tf`.

---

## 11. Tự kiểm tra

1. Vì sao tự tải `commons-text.jar` về thì compile được mà chạy vẫn nổ?
2. `node_modules` của npm và `~/.m2` của Maven khác nhau ở điểm nào?
3. Vì sao `groupId` và tên package viết theo tên miền đảo ngược?
4. Tên package và cấu trúc thư mục liên quan với nhau thế nào?
5. Gõ `mvn package` thì Maven chạy những bước nào?
6. Jar build ra chỉ ~2 KB. Vì sao nhẹ như vậy?
7. `java -cp target/...jar vn.hoc.maven.App` nổ `NoClassDefFoundError` — vì sao là lỗi runtime chứ không phải compile?
8. Vì sao chương trình in được hai dòng đầu rồi mới nổ? Điều đó nguy hiểm thế nào ở production?
9. Đặt code sai thư mục mà vẫn `BUILD SUCCESS`. Vì sao Maven không coi đó là lỗi?

<details>
<summary>Đáp án</summary>

1. Vì `commons-text` bên trong cần `commons-lang3` (phụ thuộc bắc cầu). `javac` không cần nó vì code của bạn không gọi thẳng; JVM lúc chạy mới đi tìm và không thấy.
2. `node_modules` nằm trong **từng** dự án; `~/.m2/repository` **dùng chung** cho mọi dự án — mỗi phiên bản thư viện chỉ lưu một lần, nhiều phiên bản sống cạnh nhau theo đường dẫn `groupId/artifactId/version`.
3. Vì tên miền là duy nhất trên toàn thế giới, dùng làm tiền tố thì không bao giờ trùng tên với tổ chức khác.
4. Phải khớp nhau: `package vn.hoc.maven;` thì file nằm ở `src/main/java/vn/hoc/maven/`. Tên đầy đủ của class là `vn.hoc.maven.App`.
5. Mọi phase đứng trước `package`: chép resources, compile, chép test resources, compile test, chạy test, rồi mới đóng gói jar.
6. Vì `mvn package` chỉ đóng gói **code của dự án này** (một file `App.class` 651 byte) cùng metadata. Thư viện `commons-text`, `commons-lang3` không nằm trong jar. Bytecode không bị minify.
7. Vì bước biên dịch đã xong trong `mvn package` với đủ thư viện. Lệnh `java` chỉ chạy — classpath lúc chạy chỉ có jar của bạn, thiếu `commons-text`. Compile classpath và runtime classpath là hai thứ khác nhau.
8. Vì JVM **nạp class khi cần tới** (lazy loading), chỉ đi tìm `WordUtils` lúc dòng đó chạy. Một chức năng hiếm dùng có thể thiếu thư viện mà server vẫn khởi động và chạy bình thường rất lâu, tới lúc chức năng đó được gọi mới nổ.
9. Vì Maven tìm code theo **quy ước** `src/main/java`. Thư mục đó trống thì Maven coi là dự án chưa có code — chỉ in `WARNING: JAR will be empty`, không phải lỗi.

</details>

---

⬅️ **Bài trước:** [3.4 — Lambda và Stream](../03-collections/04-lambda-stream.md)
➡️ **Bài tiếp:** [4.2 — Annotation và Reflection](02-annotation-reflection.md)
