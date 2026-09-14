# Bài 1.2 — Compile-time vs Runtime: hai cửa kiểm soát

> **Mục tiêu:** Phân biệt lỗi `javac` chặn và lỗi JVM chặn. Đây là khái niệm dùng suốt cả lộ trình, và là nền tảng để hiểu vì sao Giai đoạn 10 (Testing) tồn tại.
>
> **Cách học:** đoán trước → chạy sau → đối chiếu.

---

## 1. Chương trình của bạn bị kiểm tra **hai lần**

[Bài 0.1](../00-moi-truong/01-java-hoat-dong-nhu-the-nao.md) nói Java có 2 bước: `javac` rồi `java`. Điều quan trọng chưa nói: **mỗi bước là một cửa kiểm soát riêng, với những loại lỗi hoàn toàn khác nhau.**

```
  Hello.java ──[ javac ]──► Hello.class ──[ java / JVM ]──► Chạy
                   │                            │
            CỬA KIỂM SOÁT 1              CỬA KIỂM SOÁT 2
            Lỗi COMPILE-TIME             Lỗi RUNTIME
                   │                            │
       Không sinh ra .class            .class ĐÃ tồn tại,
       → chương trình chưa từng        nhưng nổ lúc đang chạy
         được chạy                     → có thể nổ giữa nghiệp vụ
```

### Cửa 1 — `javac`: kiểm tra **quy tắc của ngôn ngữ**

`javac` chỉ đọc code, không chạy nó. Nên nó chỉ bắt được những gì **suy ra được từ văn bản code**:

| Loại lỗi | Ví dụ |
|---|---|
| Cú pháp sai | Thiếu `;`, thiếu `}`, viết `pubic` |
| Kiểu dữ liệu không khớp | `int x = "abc";` |
| Gọi thứ không tồn tại | Gọi method chưa hề khai báo |
| Vi phạm quy tắc file/class | Tên class `public` lệch tên file |

### Cửa 2 — JVM: kiểm tra **những gì chỉ biết được khi chạy**

| Loại lỗi | Ví dụ |
|---|---|
| Phép tính không hợp lệ | Chia cho 0 |
| Truy cập vào "không có gì" | `NullPointerException` |
| Tài nguyên bên ngoài | File không tồn tại, DB mất kết nối |
| Vấn đề khởi động | Không tìm thấy entry point |

`javac` **không thể** biết trước file có tồn tại lúc chạy hay không, hay biến sẽ mang giá trị gì. Nhóm lỗi này về bản chất phải chờ tới lúc chạy.

---

## 2. Điều gây bất ngờ nhất: `javac` **không** yêu cầu class phải có `main`

Đây là chỗ trực giác của người mới thường sai. Class này compile hoàn toàn sạch:

```java
public class KhongCoMain {
    public static void main() {          // sai chữ ký, không phải entry point
        System.out.println("Hi");
    }
}
```

**Vì sao `javac` không phàn nàn?** Vì trong một project thật, **đa số class không phải entry point**:

```
UserService.java       ← class nghiệp vụ, không có main
UserRepository.java    ← class truy cập DB, không có main
User.java              ← class dữ liệu, không có main
Application.java       ← CHỈ file này có main
```

Một project Spring Boot 500 file thì **chỉ 1 file có `main`**. Nếu `javac` bắt buộc mọi class phải có `main` thì không viết được gì cả.

> 📌 **Rút ra:** `main` **không phải quy tắc của ngôn ngữ Java** — nó là **hợp đồng giữa bạn và JVM**, chỉ được kiểm tra lúc khởi động.

---

## 3. Vì sao phân biệt hai loại lỗi này lại quan trọng?

| | Lỗi compile-time | Lỗi runtime |
|---|---|---|
| Phát hiện lúc | Đang code / build | Đang chạy thật |
| Ai thấy | Chỉ bạn | **Có thể là khách hàng** |
| Chi phí sửa | Rẻ | Đắt — mất dữ liệu, mất uy tín |
| Ai chặn được | `javac` chặn tự động | Chỉ có **testing** chặn được |

Java được ca ngợi là "bắt lỗi sớm" nhờ static typing. Nhưng nó chỉ chuyển được **một nhóm** lỗi từ runtime sang compile-time — nhóm còn lại vẫn nguyên đó.

Đó là lý do Giai đoạn 10 (Testing) tồn tại, và cũng là lý do `NullPointerException` là ngoại lệ nổi tiếng nhất lịch sử Java: `javac` không hề bắt được nó.

---

## 4. Kết quả 6 thí nghiệm

Sửa `Hello.java`, compile, chạy, rồi `git restore Hello.java` để hoàn tác.

| # | Phá thế nào | Đáp án |
|---|---|---|
| 1 | `public class Hello` → `public class Hi` | **C** |
| 2 | Xóa `static` khỏi `main` | **R** |
| 3 | `main` → `Main` | **R** |
| 4 | Xóa tham số: `main()` | **R** |
| 5 | `public` → `private` | **R** |
| 6 | Xóa một dấu `;` | **C** |

### Thí nghiệm 1 — **C**

```
Hello.java:1: error: class Hi is public, should be declared in a file named Hi.java
public class Hi {
       ^
1 error
```

Quy tắc của ngôn ngữ (ghi trong Java Language Specification): class `public` **phải** nằm trong file cùng tên. `javac` đọc văn bản là suy ra được vi phạm.

#### 🔬 Nuance đã kiểm chứng

Bỏ chữ `public` đi — chỉ để `class Hi` — thì **compile thành công**, sinh ra `Hi.class`, và `java Hi` chạy bình thường.

**Vì sao?** Quy tắc trên **chỉ áp dụng cho class `public`**. Class không modifier (*package-private*) không bị ràng buộc tên file.

> ⚠️ Java **cho phép** nhưng cộng đồng **không bao giờ làm vậy**. Convention tuyệt đối: **1 file = 1 class public trùng tên file**.

### Thí nghiệm 2 — **R**

```
Error: Main method is not static in class Hello, please define the main method as:
   public static void main(String[] args)
```

`public void main(String[] args)` là method **hoàn toàn hợp pháp** — với `javac` nó chỉ là một method thường. `Hello.class` được sinh ra bình thường.

Chỉ khi JVM khởi động mới phát hiện vấn đề. Đây chính là **bài toán con gà - quả trứng** ở [bài 1.1](01-chuong-trinh-dau-tien.md#32-static--từ-khóa-quan-trọng-nhất-và-khó-nhất-với-người-mới).

**Chú ý:** JVM nói `is not static` — nó **đã tìm thấy** `main` nhưng **từ chối**. Tương phản với 3 thí nghiệm sau.

### Thí nghiệm 3, 4, 5 — đều **R**, và cùng một thông báo

```
Error: Main method not found in class Hello, please define the main method as:
   public static void main(String[] args)
or a JavaFX application class must extend javafx.application.Application
```

| # | Làm gì | Vì sao compile sạch | Vì sao JVM không thấy |
|---|---|---|---|
| 3 | `main` → `Main` | `Main` là tên method hợp lệ | JVM tìm đúng chữ `main` (chữ thường) |
| 4 | Xóa `String[] args` | `main()` là method hợp lệ | JVM tìm theo **chữ ký đầy đủ**, không theo tên |
| 5 | `public` → `private` | `private static void main` hợp lệ | JVM ở ngoài class, không truy cập được |

#### Ba điểm rút ra

**1. JVM tìm theo *chữ ký*, không theo *tên*.** `main()` và `main(String[] args)` là **hai method khác nhau hoàn toàn** — đây là *overloading* (Giai đoạn 2). Không phải "cùng một method thiếu tham số".

**2. Với JVM, "không truy cập được" = "không tồn tại".** Ở thí nghiệm 5, JVM **không** nói "main is not public" mà nói thẳng `not found`.

**3. ⚠️ Bài học debug quan trọng nhất: ba nguyên nhân khác nhau → một thông báo giống hệt.**

Thông báo lỗi giống nhau **không** có nghĩa nguyên nhân giống nhau. Gặp `Main method not found` thì phải tự soát **cả 5 thành phần** của chữ ký.

Tương phản với thí nghiệm 2: ở đó JVM tìm thấy đúng tên và đúng tham số, chỉ sai modifier — nên nó "đoán" được ý định và báo cụ thể hơn. **Thông báo càng cụ thể nghĩa là JVM càng có nhiều thông tin về ý định của bạn**, không phải lỗi càng nhẹ.

### Thí nghiệm 6 — **C**

```
Hello.java:3: error: ';' expected
        System.out.println("x")
                               ^
1 error
```

**Cách đọc thông báo của `javac`:**

```
Hello.java : 3 : error: ';' expected
    │        │              │
 tên file   dòng      mô tả vấn đề
```

Dấu `^` trỏ vào **cuối dòng 3, ngay sau dấu `)`** — không phải đầu dòng 4. Vì `javac` kỳ vọng dấu `;` xuất hiện **ngay tại đó**. Dấu `^` chỉ **vị trí nó mong đợi thứ còn thiếu**.

---

## 5. 🔑 Quy luật chung

```
┌──────────────────────────────────────────────────────────────┐
│  javac kiểm tra:  QUY TẮC CỦA NGÔN NGỮ                       │
│                   • cú pháp                                  │
│                   • kiểu dữ liệu                             │
│                   • quan hệ tên file ↔ tên class public      │
│                                                              │
│  JVM  kiểm tra:   HỢP ĐỒNG ENTRY POINT                       │
│                   public + static + void + "main" + String[] │
└──────────────────────────────────────────────────────────────┘
```

**`javac` hoàn toàn không biết chương trình của bạn sẽ *được chạy* như thế nào.** Nó chỉ trả lời: *"code này có hợp pháp không?"*

| Thay đổi ảnh hưởng tới | Cửa nào chặn |
|---|---|
| Ngữ pháp, kiểu dữ liệu, tên file/class public | **C** — `javac` |
| Chữ ký của `main` (bất kỳ thành phần nào) | **R** — JVM |

---

## 6. 🔬 Năm biến thể đã kiểm chứng — ranh giới của hợp đồng

Tất cả **đều chạy được**:

| Biến thể | Vì sao vẫn chạy |
|---|---|
| `static public void main(...)` — đảo thứ tự | Thứ tự modifier không quan trọng. *(Convention vẫn là `public static`)* |
| `main(String... args)` — varargs | Varargs được compile **thành mảng** — chữ ký bytecode y hệt `String[]` |
| `main(String args[])` — ngoặc sau tên biến | Cú pháp kế thừa từ C, Java vẫn cho phép. *(Convention: `String[] args`)* |
| `main(String[] thamSo)` — đổi tên biến | **Tên tham số không thuộc chữ ký.** JVM chỉ quan tâm **kiểu** |
| `class Hi` (không `public`) trong `Hello.java` | Quy tắc tên file chỉ áp cho class `public` |

**Hợp đồng entry point gồm đúng 5 thứ:** `public`, `static`, `void`, tên `main`, tham số kiểu `String[]`. Tên biến và thứ tự modifier **không** thuộc hợp đồng.

---

## 7. 🦴 Một "hóa thạch" trong thông báo lỗi

Dòng cuối trong thông báo lỗi:

```
or a JavaFX application class must extend javafx.application.Application
```

**JavaFX** là framework làm app desktop, từng được đóng gói **bên trong JDK** (Java 8–10). Hồi đó JVM hỗ trợ khởi động app JavaFX theo cơ chế riêng, không cần `main`. Từ **Java 11, JavaFX đã bị tách khỏi JDK** thành thư viện độc lập — nhưng dòng thông báo này vẫn còn nguyên.

Bạn sẽ gặp nhiều hóa thạch như vậy. Java 30 năm tuổi và **cực kỳ coi trọng tương thích ngược**: code viết năm 2005 vẫn compile và chạy được hôm nay. Đó là điểm mạnh lớn nhất của Java trong doanh nghiệp — và cũng là lý do tồn tại những API kỳ quái mà ai cũng khuyên tránh (`Date`, `Calendar`, `Vector`…).

---

## 8. Lệnh git đã dùng

```powershell
git restore Hello.java
```

Ném bỏ mọi thay đổi **chưa commit** của file đó, lấy lại đúng bản trong commit mới nhất.

> ⚠️ **Không hỏi lại và không hoàn tác được.** Rất tiện khi thử nghiệm, nhưng gõ nhầm file là mất công việc chưa commit.
>
> 💡 Đây là lý do commit thường xuyên có giá trị: mỗi commit là một **điểm lùi an toàn**.

---

## 9. Tự kiểm tra

1. Vì sao `javac` không báo lỗi khi một class không có method `main`?
2. Thí nghiệm 3, 4, 5 có nguyên nhân khác nhau nhưng cùng một thông báo lỗi. Điều đó dạy bạn gì khi debug?
3. Vì sao thí nghiệm 2 lại có thông báo lỗi **cụ thể hơn** (`is not static`) so với 3, 4, 5 (`not found`)?
4. `main(String[] args)` và `main(String... args)` — vì sao cả hai đều chạy được?

<details>
<summary>Đáp án</summary>

1. Vì `main` không phải quy tắc của ngôn ngữ mà là hợp đồng với JVM. Trong project thật, đa số class không phải entry point — bắt buộc có `main` thì không viết được gì.
2. Thông báo lỗi giống nhau **không** có nghĩa nguyên nhân giống nhau. Không thể suy ngược từ thông báo ra nguyên nhân — phải tự soát đủ mọi khả năng.
3. Vì JVM **tìm thấy** method tên `main` với đúng tham số `String[]`, chỉ thiếu `static`. Nó suy được ý định của bạn nên báo cụ thể. Ở 3/4/5 thì không có method nào khớp chữ ký nên nó chỉ biết nói "không tìm thấy".
4. Vì varargs (`...`) được compiler dịch **thành mảng**. Trong bytecode, hai chữ ký này giống hệt nhau.

</details>

---

⬅️ **Bài trước:** [1.1 — Chương trình Java đầu tiên](01-chuong-trinh-dau-tien.md)
➡️ **Bài tiếp:** [1.3 — Biến và kiểu dữ liệu nguyên thủy](03-bien-va-kieu-du-lieu.md)
