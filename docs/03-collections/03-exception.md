# Bài 3.3 — Exception

> **Mục tiêu:** Hiểu exception di chuyển trong chương trình thế nào (stack unwinding), vì sao Java có loại lỗi mà **compiler bắt phải xử lý** (checked exception), và `finally` / try-with-resources thực sự được biên dịch thành gì.
>
> **Code thực hành:** [`bai-tap/03-collections/NgoaiLe.java`](../../bai-tap/03-collections/NgoaiLe.java)

---

## 1. Vấn đề: một hàm báo "tôi thất bại" bằng cách nào?

Cách cổ điển (C, và cả JS khi trả `null` / `undefined` / `-1`) là **giá trị trả về đặc biệt**:

```java
int viTri = timViTri(ds, "An");   // -1 nếu không thấy
String dh = timDonHang(id);       // null nếu không có
```

| Lỗ hổng | Hệ quả |
|---|---|
| Người gọi **quên kiểm tra** | `null` đi tiếp rồi nổ NPE ở chỗ khác, đúng cái bẫy "chỗ gây lỗi cách xa chỗ phát hiện" ở [bài 3.2](02-generics.md) |
| Giá trị trả về bị **chiếm chỗ** | Hàm trả `int` thì báo lỗi bằng gì nếu `-1` cũng hợp lệ? |
| Lỗi tầng sâu phải **chuyền tay từng tầng** | Mỗi tầng một đoạn `if (ketQua == null) return null;` |

**Exception** tách **đường đi khi lỗi** khỏi **đường đi bình thường**. Hàm gặp lỗi *ném* (`throw`) một object, object đó tự bay ngược lên tới chỗ nào chịu *bắt* (`catch`), bỏ qua mọi tầng ở giữa.

### So với JS/TS

| | JavaScript / TypeScript | Java |
|---|---|---|
| Ném được gì? | **Bất cứ thứ gì**: `throw "loi"`, `throw 42` | **Chỉ** object thuộc lớp con của `Throwable` |
| `catch (e)` | `e` là `unknown` / `any`, phải tự đoán | Khai báo kiểu: `catch (ClassCastException e)` |
| Bắt theo loại | Tự viết `if (e instanceof X)` | Nhiều khối `catch`, JVM tự chọn |
| Compiler ép xử lý lỗi? | Không bao giờ | **Có** — checked exception (mục 4) |
| Lỗi gốc | `new Error(msg, { cause })` (ES2022) | `new XxxException(msg, cause)` (Java 1.4) |

---

## 2. Exception là một object — và chúng xếp thành cây

```
                       Throwable
                      /         \
                 Error           Exception
                   │            /          \
       OutOfMemoryError   IOException     RuntimeException
       StackOverflowError SQLException     ├── NullPointerException        (bài 1.5)
                          KhongTimThay...  ├── ClassCastException          (bài 2.4)
                          (tự viết)        ├── ArrayIndexOutOfBounds...    (bài 1.7)
                                           ├── ArrayStoreException         (bài 3.2)
                                           ├── ConcurrentModification...   (bài 3.1)
                                           └── IllegalArgument/StateException

      └── JVM hỏng ──┘   └─ CHECKED ─┘    └──────── UNCHECKED ─────────┘
```

| Nhánh | Nghĩa | Ai gây ra | Có nên bắt? |
|---|---|---|---|
| `Error` | JVM gặp sự cố: hết bộ nhớ, tràn stack | Môi trường | Gần như không bao giờ |
| `Exception` (trừ `RuntimeException`) | Sự cố **bên ngoài** mà chương trình đúng vẫn gặp: file mất, mạng rớt | Thế giới bên ngoài | Có — và compiler **bắt** bạn quyết định |
| `RuntimeException` | **Bug**: `null`, sai index, ép sai kiểu | Code của bạn | Thường không — **sửa code** |

Exception là object nên mang dữ liệu: `getMessage()`, `getCause()`, và **stack trace** — ảnh chụp chuỗi lời gọi method *tại lúc object được tạo* (không phải lúc ném).

---

## 3. `throw` và stack unwinding

Mỗi lần gọi method, JVM đẩy một **frame** lên Stack ([bài 1.4](../01-java-core/04-string-va-kieu-tham-chieu.md)). Khi có `throw`:

```
   ┌──────────┐
   │  c()     │ ← throw! có catch khớp không? KHÔNG → gỡ frame c
   ├──────────┤
   │  b()     │ ← KHÔNG → gỡ frame b    (dòng "b: ket thuc" không bao giờ chạy)
   ├──────────┤
   │  a()     │ ← KHÔNG → gỡ frame a    (dòng "a: ket thuc" không bao giờ chạy)
   ├──────────┤
   │  main()  │ ← CÓ → nhảy vào khối catch, chạy tiếp từ đó
   └──────────┘

 Không ai bắt tới tận đáy → JVM in stack trace ra stderr, thread chết, exit code = 1
```

Quá trình gỡ frame gọi là **stack unwinding**. Frame đã gỡ thì phần còn lại của method **không bao giờ chạy**.

Giống JS khi chạy đồng bộ. Khác ở bất đồng bộ: trong JS lỗi trong `Promise` thành *rejection*; trong Java mỗi thread có stack riêng, lỗi chỉ unwind trong thread đó.

**Cắn ở backend:** trong Spring Boot (Giai đoạn 6) mỗi request chạy trên một thread. Exception không ai bắt unwind tới tận framework → Spring trả **500 Internal Server Error**. Server không chết, nhưng client nhận lỗi vô nghĩa. Giai đoạn 8 học cách biến exception thành 404 / 400 tử tế.

### Đọc stack trace

Output thật của mục 7 trong bài thực hành:

```
Exception in thread "main" LoiDichVu: lay don hang that bai      ← khối 1: lỗi ngoài cùng
	at NgoaiLe.layDonHang(NgoaiLe.java:72)                        ← dòng trên cùng = nơi object được TẠO
	at NgoaiLe.main(NgoaiLe.java:135)                             ← ai gọi nó
Caused by: KhongTimThayDonHang: khong co don hang id=5           ← khối 2: lỗi gốc
	at NgoaiLe.timDonHang(NgoaiLe.java:63)
	at NgoaiLe.layDonHang(NgoaiLe.java:70)
	... 1 more                                                    ← 1 frame trùng khối trên (main), không in lại
```

Khi debug production: **đọc `Caused by` cuối cùng trước**. Đó là nguyên nhân thật; các khối trên chỉ là lớp bọc.

---

## 4. Checked vs unchecked — compiler canh cửa 1

**Là gì?** Với *checked exception*, compiler buộc method chọn một trong hai:

```java
try { timDonHang(3); } catch (KhongTimThayDonHang e) { ... }   // tự bắt
static void xuLy() throws KhongTimThayDonHang { ... }          // khai báo: người gọi tôi lo
```

Không làm cả hai:

```
NgoaiLe.java:98: error: unreported exception KhongTimThayDonHang; must be caught or declared to be thrown
        String dh = timDonHang(3);
                              ^
```

**Vì sao tồn tại?** Lỗi "file không tồn tại" không phải bug — nó *chắc chắn* xảy ra một ngày nào đó. Ghi trong tài liệu thì người ta quên; để compiler nhắc thì không quên được. Còn bug (`NullPointerException`) mà bắt khai báo thì dòng nào cũng phải `throws` — vô nghĩa, nên bug là *unchecked*.

So với TS: TS **không có cách nào** khai báo hàm ném gì. `function f(): string` không nói gì về việc `f` có `throw` hay không.

### 🔬 Checked chỉ tồn tại ở cửa 1 — JVM không quan tâm

Giống generic ở [bài 3.2](02-generics.md), `throws` là thông tin **cho compiler**. Trong file `.class` nó chỉ là một dòng ghi chú (`javap -v`):

```
    Exceptions:
      throws KhongTimThayDonHang
```

JVM **không kiểm tra** dòng này. Bằng chứng — mượn chính type erasure để lừa compiler:

```java
@SuppressWarnings("unchecked")
static <E extends Throwable> void nemLen(Throwable t) throws E {
    throw (E) t;                       // E bị xóa → chẳng ai kiểm tra
}

static void khongKhaiBao() {           // KHÔNG có throws
    Lau.<RuntimeException>nemLen(new Exception("checked nhung lot qua"));
}
```

```
Exception in thread "main" java.lang.Exception: checked nhung lot qua
	at Lau.khongKhaiBao(Lau.java:9)
	at Lau.main(Lau.java:13)
```

Một `java.lang.Exception` (checked) bay ra khỏi `main` không khai báo `throws` gì — compile OK, JVM chạy bình thường. Lombok `@SneakyThrows` dùng đúng mẹo này; Kotlin gọi method Java mà không cần bắt checked cũng vì vậy.

### Nó cắn bạn ở đâu?

| Vết cắn | Chuyện gì xảy ra |
|---|---|
| **Nuốt lỗi cho compiler im** | `catch (Exception e) {}` để qua cửa → lỗi biến mất, production chạy sai mà log trống. Anti-pattern số 1 |
| **Không đi qua lambda** | Lambda trong Stream API (bài 3.4) không ném checked ra ngoài được → phải bọc lủng củng |
| **`@Transactional` không rollback** ⚠️ | Mặc định Spring **chỉ rollback khi gặp `RuntimeException` / `Error`**. Ném checked giữa chừng → dữ liệu ghi trước đó **vẫn commit**: đã trừ tiền nhưng chưa tạo đơn (Giai đoạn 7) |

Xu hướng hiện đại **ưu tiên unchecked**: Spring bọc `SQLException` (checked) thành `DataAccessException` (unchecked); Kotlin, C# bỏ hẳn checked.

---

## 5. `try` / `catch` / `finally`

```
            ┌─────────┐
            │   try   │
            └────┬────┘
         êm      │       có lỗi
     ┌───────────┴───────────┐
     │                       ▼
     │        so lần lượt từng catch từ TRÊN xuống
     │        (khớp = lỗi instanceof kiểu trong catch)
     │           khớp │             │ không khớp
     │                ▼             │
     │           khối catch         │
     ▼                ▼             ▼
   ┌──────────────────────────────────┐
   │          finally (luôn)          │
   └───────┬────────────────┬─────────┘
           ▼                ▼
      chạy tiếp       lỗi bay tiếp lên frame trên
```

### Thứ tự `catch` — đa hình quyết định

"Khớp" là `instanceof` ([bài 2.4](../02-oop/04-da-hinh.md)). Trong bytecode, mỗi `catch` là một dòng trong **exception table**, JVM duyệt từ trên xuống, dừng ở dòng khớp đầu tiên:

```
    Exception table:
       from    to  target type
         147   155   158   Class java/lang/ClassCastException
         147   155   170   Class java/lang/RuntimeException
```

Đặt `RuntimeException` lên trên thì mọi `ClassCastException` đều bị nó bắt trước — khối dưới thành **code chết**. `javac` từ chối:

```
NgoaiLe.java:107: error: exception ClassCastException has already been caught
        } catch (ClassCastException e) {
          ^
```

Quy tắc: **cụ thể trước, chung chung sau**. Lưu ý thêm: khối `try` **không sinh ra lệnh nào** — nó chỉ là một khoảng `from..to` trong bảng. Đường đi êm của `try` gần như miễn phí; cái đắt là **tạo** exception (chụp stack trace), nên đừng dùng exception để điều khiển luồng bình thường.

### 🔬 `finally` được biên dịch thành gì?

JVM **không có lệnh `finally`**. `javac` **chép code finally vào mọi lối ra** của `try`. `javap -c` của `thuFinally()`:

```
       8: iconst_1
       9: istore_0          ← cất giá trị trả về (1) vào biến tạm
      10: ... "finally chay" ← bản sao 1: lối ra bình thường
      18: iload_0
      19: ireturn           ← trả về giá trị ĐÃ CẤT
      20: astore_1          ← lối ra khi có lỗi: cất exception
      21: ... "finally chay" ← bản sao 2
      29: aload_1
      30: athrow            ← ném tiếp exception đã cất
    Exception table:
           0    10    20   any
```

Hai hệ quả:

**(a) `return x` chụp giá trị trước khi `finally` chạy** — sửa biến trong `finally` không đổi kết quả, nhưng sửa *object* thì có (lại là pass-by-value, [bài 1.7](../01-java-core/07-mang-va-method.md)):

```java
int x = 1;
try { return x; } finally { x = 99; }              // suaSauReturn() = 1

StringBuilder sb = new StringBuilder("a");
try { return sb; } finally { sb.append("b"); }     // suaObject() = ab
```

**(b) `return` trong `finally` nuốt exception.** Bytecode của `nuotLoi()`:

```
       9: athrow            ← ném IllegalStateException
      10: astore_0          ← finally bắt nó, cất vào biến...
      11: iconst_2
      12: ireturn           ← ...rồi return 2. Không còn athrow nào. Lỗi BIẾN MẤT
```

`javac` mặc định **im lặng**; chỉ khi chạy `javac -Xlint:all` mới thấy `warning: [finally] finally clause cannot complete normally`.

---

## 6. try-with-resources

Tài nguyên có hạn (connection DB, file, socket) phải trả lại. Viết tay bằng `finally` vừa dài vừa có lỗi tinh vi:

```java
HongKhiDong r = new HongKhiDong();
try {
    throw new RuntimeException("loi THAT trong try");
} finally {
    r.close();          // close() cũng ném lỗi → lỗi THẬT bị che mất
}
```

Java 7 trở đi:

```java
try (TaiNguyen ketNoi = new TaiNguyen("ketNoi");
     TaiNguyen file = new TaiNguyen("file")) {
    ...
}
```

Điều kiện: class `implements AutoCloseable` — interface một method `close()`, hợp đồng thuần túy như [bài 2.3](../02-oop/03-interface-va-abstract-class.md). TS 5.2 có `using` + `Symbol.dispose` — cùng ý tưởng, Java có trước ~15 năm.

### Ba quy tắc (đều thấy được trong bytecode)

```
  mo ketNoi
  mo file                  ┐
  dang dung tai nguyen     │ phạm vi try-with-resources
  dong file     ← mở SAU, đóng TRƯỚC (như stack: file có thể dựa vào ketNoi)
  dong ketNoi              ┘
  catch: hong giua chung   ← catch chạy SAU khi mọi thứ đã đóng
  finally
```

1. **Đóng ngược thứ tự mở** (LIFO).
2. **Đóng xong rồi mới tới `catch`/`finally`** — biến `ketNoi`, `file` chỉ sống trong khối `try`; trong exception table, khoảng của `catch (IllegalStateException)` (271–343) **bao trùm** cả code đóng tài nguyên.
3. **Lỗi khi `close()` không che lỗi thật** — compiler sinh lời gọi `Throwable.addSuppressed`:

```
kieu cu bat duoc: loi khi close            ← finally tự viết: lỗi thật mất tích
kieu moi bat duoc: loi THAT trong try      ← try-with-resources: lỗi thật được giữ
  suppressed: loi khi close                ← lỗi close đi kèm, không mất
```

---

## 7. Exception tự viết và bọc lỗi (chaining)

```java
class KhongTimThayDonHang extends Exception { ... }           // checked
class LoiDichVu extends RuntimeException {                     // unchecked
    LoiDichVu(String message, Throwable cause) { super(message, cause); }
}
```

Mỗi tầng nói ngôn ngữ của tầng đó: tầng dữ liệu ném lỗi kỹ thuật, tầng service bọc thành lỗi nghiệp vụ **và giữ lỗi gốc bên trong**:

```
  LoiDichVu("lay don hang that bai")          ← tầng ngoài thấy cái này
     └── cause: KhongTimThayDonHang(...)      ← nguyên nhân thật vẫn ở đây
```

Bọc lỗi cũng là cách hợp pháp biến checked thành unchecked — đúng cái Spring làm với `SQLException`.

---

## 8. Kết quả thực nghiệm

```
--- 1. Stack unwinding ---
a: bat dau
b: bat dau
c: bat dau
main bat duoc: Index 5 out of bounds for length 3

--- 2. finally ---
  try: return 1
  finally chay
thuFinally() = 1
nuotLoi() = 2

--- 3. Checked exception ---
Don hang #1
Loi: khong co don hang id=2

--- 4. Thu tu catch ---
bat boi ClassCastException

--- 5. Boc loi (exception chaining) ---
Loi: lay don hang that bai
Nguyen nhan: khong co don hang id=9
Kieu nguyen nhan: KhongTimThayDonHang

--- 6. try-with-resources ---
  mo ketNoi
  mo file
  dang dung tai nguyen
  dong file
  dong ketNoi
  catch: hong giua chung
  finally

--- 7. Khong ai bat ---
Exception in thread "main" LoiDichVu: lay don hang that bai
	at NgoaiLe.layDonHang(NgoaiLe.java:72)
	at NgoaiLe.main(NgoaiLe.java:135)
Caused by: KhongTimThayDonHang: khong co don hang id=5
	at NgoaiLe.timDonHang(NgoaiLe.java:63)
	at NgoaiLe.layDonHang(NgoaiLe.java:70)
	... 1 more
```

Exit code: `1`. Dòng `dong nay co in khong?` không in.

**Chi tiết về file `.class` ở hai thí nghiệm lỗi compile:** `NgoaiLe.class` **không** được sinh ra, nhưng `TaiNguyen.class`, `KhongTimThayDonHang.class`, `LoiDichVu.class` **vẫn có** — `javac` xử lý từng class, class không có lỗi vẫn được ghi ra. Vì vậy trước khi kiểm tra "có `.class` không" phải xóa file cũ và nhìn đúng tên class bị lỗi.

---

## 9. Cạm bẫy production

| Cạm bẫy | Hệ quả | Cách đúng |
|---|---|---|
| `catch (Exception e) {}` | Lỗi biến mất, dữ liệu sai âm thầm | Ít nhất log kèm `e`; tốt hơn: không bắt nếu không xử lý được |
| `throw new LoiDichVu("...")` quên `e` | Mất stack trace gốc, không biết lỗi thật ở đâu | Luôn truyền `cause` |
| Log rồi ném lại ở **mọi** tầng | Một lỗi in ra 5 lần, log rối | Hoặc xử lý, hoặc ném lên — chọn một |
| Checked exception trong `@Transactional` | **Commit** dữ liệu dở dang | Dùng unchecked, hoặc `rollbackFor = Exception.class` |
| `return` trong `finally` | Nuốt mọi exception, `javac` không cảnh báo mặc định | Không bao giờ `return`/`throw` trong `finally` |
| Quên đóng connection | Pool (HikariCP, ~10 connection) cạn → mọi request treo rồi chết với `Connection is not available, request timed out after 30000ms` | try-with-resources |
| Exception làm luồng điều khiển | Chậm (chụp stack trace mỗi lần `new`) | Dùng `if` cho trường hợp bình thường |

---

## 10. Tự kiểm tra

1. Vì sao `b: ket thuc` không in, dù `b()` không hề có lỗi nào?
2. `throws` trong chữ ký method có tồn tại lúc chạy không? Bằng chứng?
3. Vì sao `javac` từ chối khi `catch (RuntimeException)` đứng trên `catch (ClassCastException)`?
4. `try { return x; } finally { x = 99; }` trả về gì, và vì sao? Nếu `x` là `StringBuilder` thì sao?
5. Trong try-with-resources, `catch` chạy trước hay sau khi tài nguyên đóng? Tài nguyên nào đóng trước?
6. Vì sao ném checked exception trong method `@Transactional` của Spring có thể làm hỏng dữ liệu?

<details>
<summary>Đáp án</summary>

1. Vì lỗi từ `c()` bay qua `b()` mà `b()` không có `catch` khớp → frame `b` bị gỡ (stack unwinding), phần còn lại của `b` không bao giờ chạy.
2. Không có ý nghĩa lúc chạy. Trong `.class` chỉ là một dòng ghi chú `Exceptions: throws ...`, JVM không kiểm tra. Mẹo `throw (E) t` với generic (bị xóa kiểu) ném được `java.lang.Exception` ra khỏi `main` không khai báo `throws` gì — compile OK, chạy bình thường.
3. JVM duyệt exception table từ trên xuống, dừng ở dòng khớp đầu tiên (`instanceof`). Mọi `ClassCastException` đều là `RuntimeException` nên bị khối trên bắt hết — khối dưới là code chết: `exception ClassCastException has already been caught`.
4. Trả `1`. `javac` cất giá trị vào biến tạm (`istore`) **trước** khi chép code finally, rồi `ireturn` giá trị đã cất. Với `StringBuilder`, cái được cất là **tham chiếu** — finally sửa object thì người gọi thấy (`"ab"`), giống pass-by-value ở bài 1.7.
5. Đóng hết **rồi** mới tới `catch`/`finally`. Đóng ngược thứ tự mở: mở sau đóng trước.
6. Mặc định Spring chỉ rollback với `RuntimeException`/`Error`. Checked exception bay ra → transaction vẫn **commit** phần đã ghi trước đó.

</details>

---

⬅️ **Bài trước:** [3.2 — Generics](02-generics.md)
➡️ **Bài tiếp:** [3.4 — Lambda và Stream API](04-lambda-stream.md)
