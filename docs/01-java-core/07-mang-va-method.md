# Bài 1.7 — Mảng và method

> **Mục tiêu:** Hiểu mảng cố định kích thước, cách Java chọn overload, và **truyền tham số theo giá trị** — chủ đề bị hiểu sai nhiều nhất trong Java.
>
> **Code thực hành:** [`bai-tap/01-java-core/MangVaMethod.java`](../../bai-tap/01-java-core/MangVaMethod.java)
>
> Bài cuối của Giai đoạn 1.

---

## 1. Mảng trong Java **không co giãn**

```javascript
// JavaScript — Array co giãn tự do
const arr = [1, 2, 3];
arr.push(4);           // giờ có 4 phần tử
arr.length = 0;        // xóa sạch
```

```java
// Java — kích thước cố định VĨNH VIỄN
int[] arr = new int[3];
// không có push, không có pop, không thêm bớt được gì
```

**Vì sao?** Vì mảng Java là **một khối bộ nhớ liền nhau** được cấp phát một lần trên Heap:

```
    STACK                        HEAP
                        ┌─────────────────────────┐
  diem │ ●──────────────┼──► [ 10 │ 20 │ 30 ]     │
                        │       ▲    ▲    ▲       │
                        │       0    1    2       │  ← 3 ô int liền nhau,
                        └─────────────────────────┘     mỗi ô 4 byte
```

Các ô nằm sát nhau nên máy tính tính được địa chỉ phần tử thứ `i` bằng một phép nhân đơn giản — đó là lý do truy cập mảng cực nhanh. Nhưng cũng chính vì liền nhau mà **không thể "nối thêm"**: vùng nhớ ngay sau đó có thể đã bị thứ khác chiếm.

Muốn "thêm phần tử" thì phải **cấp mảng mới lớn hơn rồi copy toàn bộ sang**. Đó chính là việc `ArrayList` làm hộ bạn — **Giai đoạn 3**.

> 📌 Mảng là **object** (tên kiểu có `[]` → reference type theo [bài 1.4](04-string-va-kieu-tham-chieu.md)). Biến `diem` nằm trên Stack và chỉ chứa **địa chỉ**; dữ liệu nằm trên Heap. Mục 7 dựa hoàn toàn vào điều này.

### Ba cách khai báo

```java
int[] a = new int[3];              // cấp phát 3 ô, điền giá trị mặc định
int[] b = {10, 20, 30};            // khởi tạo luôn (chỉ dùng được lúc khai báo)
int[] c = new int[] {10, 20, 30};  // dạng đầy đủ, dùng được ở mọi nơi
```

> 💡 Java cũng cho viết `int a[]` (ngoặc sau tên biến) như C, nhưng convention là **`int[] a`** — vì `int[]` mới là *kiểu*, nên nó nên đứng liền nhau.

---

## 2. Giá trị mặc định của mảng — nơi `null` ẩn nấp

`new int[3]` **tự điền sẵn** giá trị mặc định cho mọi ô, theo đúng bảng ở [bài 1.3](03-bien-va-kieu-du-lieu.md):

| Kiểu phần tử | Giá trị mặc định |
|---|---|
| `int`, `long`, `short`, `byte` | `0` |
| `double`, `float` | `0.0` |
| `boolean` | `false` |
| `char` | ký tự NUL |
| **Mọi kiểu tham chiếu** (`String`, `Integer`…) | **`null`** |

Dòng cuối là chỗ cần cảnh giác:

```java
String[] ten = new String[3];
System.out.println(ten[0].length());     // 💥 NullPointerException
```

Mảng **đã tồn tại**, có đủ 3 ô — nhưng mỗi ô chứa `null` chứ không chứa chuỗi. Đây là dạng NPE khác với [bài 1.5](05-wrapper-autoboxing-ep-kieu.md): ở đó là unboxing, ở đây là ô mảng chưa được điền.

---

## 3. `length` là **field**, không phải method

```java
diem.length        // ✅ 3
diem.length()      // ❌ error: cannot find symbol — method length()
```

Chỗ cực dễ nhầm, vì `String` thì ngược lại:

| | Java | JavaScript |
|---|---|---|
| Mảng | `arr.length` — **field** | `arr.length` |
| Chuỗi | `s.length()` — **method** | `s.length` |
| Danh sách (GĐ 3) | `list.size()` — **method** | — |

Ba thứ cùng hỏi "dài bao nhiêu", ba cú pháp khác nhau. Không có logic nào biện minh — đây là hệ quả lịch sử: mảng có từ Java 1.0 và được cài đặt ở tầng JVM, còn `String` và `List` là class bình thường.

---

## 4. In mảng ra màn hình — cái bẫy debug

```java
System.out.println(diem);        // [I@5b2133b1     ← không phải nội dung!
```

Giải mã chuỗi đó:

```
  [I@5b2133b1
  │ │ └────────► mã băm (hashcode) của object, viết dạng hex
  │ └──────────► I = int     (kiểu phần tử)
  └────────────► [  = đây là một mảng
```

**Vì sao?** Vì mọi object trong Java đều có method `toString()` kế thừa từ class gốc `Object`, và bản mặc định chỉ in `tênKiểu@hashcode`. `String` **viết đè** method đó để in nội dung, còn **mảng thì không**.

```java
import java.util.Arrays;
System.out.println(Arrays.toString(diem));       // [10, 20, 30]
System.out.println(Arrays.deepToString(mang2D)); // cho mảng nhiều chiều
```

> 🪤 Lỗi debug kinh điển: in mảng ra kiểm tra, thấy `[I@5b2133b1`, tưởng dữ liệu hỏng, rồi đi tìm bug ở chỗ hoàn toàn không có bug.

### Truy cập ngoài phạm vi

```
Exception in thread "main" java.lang.ArrayIndexOutOfBoundsException:
    Index 3 out of bounds for length 3
```

Chỉ số bắt đầu từ `0`, nên mảng 3 phần tử có chỉ số hợp lệ là `0, 1, 2`. Đây là **lỗi runtime** ([bài 1.2](02-compile-time-vs-runtime.md)) — `javac` không thể biết trước chỉ số lúc chạy.

> 💡 Lý do nên ưu tiên **for-each** ([bài 1.6](06-toan-tu-va-luong-dieu-khien.md)): không có chỉ số thì không thể sai chỉ số.

---

## 5. `args` — cuối cùng cũng dùng tới nó

Từ [bài 1.1](01-chuong-trinh-dau-tien.md) tới giờ vẫn viết `String[] args` mà chưa đọc nó. Đó là **một mảng bình thường**, chứa tham số gõ sau tên class.

```powershell
java MangVaMethod alpha beta
```
```
args.length = 2
args[0] = "alpha"
args[1] = "beta"
```

Hai điều cần nhớ:

- **Mọi phần tử luôn là `String`.** Muốn dùng như số phải chuyển: `int n = Integer.parseInt(args[0]);`
- **Không truyền gì thì `args.length == 0`**, chứ **không phải** `args == null`. Nên `args.length` luôn gọi được an toàn.

---

## 6. Method và overloading

```java
static void in(int x) { ... }
 │      │     │   │
 │      │     │   └──► tham số
 │      │     └──────► tên method (camelCase)
 │      └────────────► kiểu trả về
 └───────────────────► modifier
```

**Overloading** là viết nhiều method **cùng tên** nhưng **khác danh sách tham số**.

### 🔗 Khép lại một chuyện từ bài 1.2

Ở [bài 1.2](02-compile-time-vs-runtime.md), thí nghiệm 4 xóa `String[] args` khỏi `main` và JVM báo `Main method not found`. Lúc đó chỉ nói *"JVM tìm theo chữ ký, không theo tên"*. Giờ thì rõ: `main()` và `main(String[])` là **hai method hoàn toàn khác nhau** — chúng là hai overload.

**Chữ ký (signature) = tên method + danh sách kiểu tham số.** Kiểu trả về **không** thuộc chữ ký:

```java
static int  tinh(int x) { }
static long tinh(int x) { }     // ❌ lỗi compile — cùng chữ ký
```

### Java chọn overload nào? — Ba pha

Thực nghiệm: có `in(long)` và `in(Integer)`, gọi `in(5)` → chọn **`long`**.

```
   in(5)   ← tham số là int
     │
     ├─ PHA 1: chỉ xét khớp chính xác + MỞ RỘNG KIỂU
     │         (int → long → float → double)
     │         → tìm thấy in(long)   ✅  DỪNG TẠI ĐÂY
     │
     ├─ PHA 2: cho phép boxing/unboxing        ← không bao giờ tới
     │         (in(Integer) nằm ở pha này)
     │
     └─ PHA 3: cho phép varargs                ← không bao giờ tới
```

`in(Integer)` **thậm chí không được đem ra so sánh**, vì pha 1 đã có kết quả.

#### Vì sao thiết kế ba pha?

**Pha 1 tái hiện chính xác hành vi của Java trước phiên bản 5.**

Cả **autoboxing** lẫn **varargs** đều mới có từ Java 5 (2004). Nếu Java đem chúng xét chung một lượt với widening, thì một chương trình viết năm 2003 — đang gọi đúng `in(long)` — có thể **đột nhiên gọi sang method khác** chỉ vì nâng cấp JDK. Không đổi dòng code nào mà hành vi đổi: cơn ác mộng.

Nên luật là: *"Xét hết mọi khả năng theo kiểu cũ trước đã. Chỉ khi bế tắc mới dùng tới tính năng mới."*

> 🦴 Đây là **lần thứ ba** trong lộ trình gặp cái giá của tương thích ngược:
> - Dòng JavaFX trong thông báo lỗi ([bài 1.2](02-compile-time-vs-runtime.md))
> - `array.length` là field còn `string.length()` là method (mục 3)
> - Thứ tự ba pha khi chọn overload
>
> Java 30 năm tuổi và **không bao giờ phá code cũ**. Đó là lý do nó thống trị doanh nghiệp — nhưng phải trả giá bằng những luật trông tùy tiện mà thực ra luôn có lý do lịch sử.

#### 📌 Bài học thực dụng

**Không cần học thuộc ba pha.** Điều cần rút ra: **đừng viết overload mập mờ.**

Nếu code có cả `f(long)` và `f(Integer)`, người đọc `f(5)` **không thể biết** cái nào được gọi nếu không tra đặc tả ngôn ngữ. Giải pháp: đặt tên khác nhau, hoặc chỉ overload khi các tham số **khác nhau rõ rệt** (`String` vs `int`).

---

## 7. Truyền tham số theo giá trị — chủ đề bị hiểu sai nhiều nhất

### Quy tắc, phát biểu chính xác

> **Java LUÔN LUÔN truyền tham số theo giá trị (pass-by-value). Không có ngoại lệ. Java không hề có pass-by-reference.**

Phải nói rõ **"giá trị" của một biến là gì**:

| Loại biến | "Giá trị" của nó là | Method nhận được |
|---|---|---|
| Primitive (`int`, `double`…) | Chính con số | **Bản sao của con số** |
| Reference (`int[]`, `String`, object…) | **Địa chỉ** trỏ tới Heap | **Bản sao của địa chỉ** |

Khi gọi method, Java **luôn copy** giá trị trong ô biến sang tham số. Với reference, thứ bị copy là **địa chỉ** — nên giờ có **hai biến cùng trỏ vào một object**.

### Kết quả thực nghiệm

```
A. primitive         : 1              ← không đổi
B. mang, sua phan tu : [99, 2, 3]     ← ĐỔI
C. mang, gan lai     : [1, 2, 3]      ← không đổi
D. String            : Duc            ← không đổi
```

### B — `m[0] = 99` — **sửa thứ mà địa chỉ trỏ tới**

```
   main:    mang1 │ ●────┐
                          ├──────►  [ 1 │ 2 │ 3 ]
   method:      m │ ●────┘

                    m[0] = 99
                    "đi theo địa chỉ, rồi sửa ô nhớ TRÊN HEAP"
                          │
                          ▼
   main:    mang1 │ ●────┐
                          ├──────►  [ 99 │ 2 │ 3 ]   ← mang1 nhìn thấy
   method:      m │ ●────┘
```

Hai biến vẫn trỏ cùng một object. Sửa object đó thì ai trỏ tới cũng thấy.

### C — `m = new int[]{...}` — **sửa chính ô biến chứa địa chỉ**

```
   Trước:
   main:    mang2 │ ●────┐
                          ├──────►  [ 1 │ 2 │ 3 ]
   method:      m │ ●────┘

                    m = new int[]{99, 99, 99}
                    "ghi một ĐỊA CHỈ MỚI vào ô biến m"
                    (không hề động tới ô biến mang2)
                          │
                          ▼
   Sau:
   main:    mang2 │ ●───────────►  [ 1 │ 2 │ 3 ]     ← KHÔNG đổi
   method:      m │ ●───────────►  [ 99 │ 99 │ 99 ]  ← object mới,
                                                        chết khi method kết thúc
```

`m` và `mang2` là **hai ô nhớ riêng biệt**. Ghi đè lên `m` không chạm được tới `mang2`.

### 🎯 Phép thử quyết định

| | Method nhận được | Gán lại tham số thì |
|---|---|---|
| **pass-by-value** | **Bản sao** giá trị của biến | Biến của người gọi **không đổi** |
| **pass-by-reference** | **Chính ô biến** của người gọi (một bí danh) | Biến của người gọi **đổi theo** |

Kết quả câu C là `[1, 2, 3]` — **không đổi**. Vậy Java là **pass-by-value**, dứt khoát.

**Vì sao rất nhiều người hiểu sai?** Vì họ chỉ nhìn thí nghiệm B. Thấy sửa được nội dung, họ kết luận "object thì truyền theo tham chiếu". Nhưng B **không chứng minh** điều đó — B chỉ chứng minh **hai biến đang cùng trỏ vào một object**, điều hoàn toàn nhất quán với pass-by-value.

Chỉ thí nghiệm C mới phân biệt được hai khả năng.

> 📌 **Câu nói gọn để nhớ:** *Java truyền **bản sao của địa chỉ**, chứ không truyền **bản thân biến**.*

### D — `String` có hai lớp bảo vệ

`ten` không đổi vì **hai lý do chồng lên nhau**:

1. **Pass-by-value** — `s = s + " DA SUA"` chỉ ghi vào biến cục bộ `s`, không chạm tới `ten`.
2. **`String` bất biến** ([bài 1.4](04-string-va-kieu-tham-chieu.md)) — kể cả muốn sửa nội dung object cũ như câu B cũng **không có cách nào**.

---

## 8. 🚨 Nó cắn bạn ở đâu

```java
public void xuLy(List<User> danhSach) {
    danhSach.clear();                  // XÓA SẠCH list của người gọi!
}
```

`List` là object. Method nhận bản sao địa chỉ, nhưng nó **trỏ vào đúng cái list của bạn**. Gọi `clear()`, `add()`, `remove()` là sửa thẳng dữ liệu của người gọi — y hệt thí nghiệm B.

Đây là nguồn của loại bug khó chịu bậc nhất: *"dữ liệu của tôi tự nhiên bị đổi, mà tôi có sửa gì đâu"*. Thủ phạm là một method nào đó gọi ba tầng phía dưới.

**Cách phòng:**

```java
xuLy(new ArrayList<>(danhSach));                   // truyền bản sao
List<User> khongDoiDuoc = List.copyOf(danhSach);   // danh sách bất biến, sửa là nổ
```

Hệ quả ngược lại: **Java không viết được method `swap(a, b)` để hoán đổi hai biến primitive.** Muốn "trả về nhiều giá trị" thì phải gói vào một object, một mảng, hoặc một `record` (Giai đoạn 2).

---

## 9. Tự kiểm tra

1. Vì sao mảng Java không có `push()` như Array của JS?
2. `String[] ten = new String[3];` — mảng đã tồn tại, vậy `ten[0].length()` có chạy được không?
3. Vì sao `arr.length` không có ngoặc mà `s.length()` lại có?
4. `System.out.println(mang)` in ra `[I@5b2133b1`. Chuyện gì đã xảy ra?
5. Có `in(long)` và `in(Integer)`, gọi `in(5)` thì bản nào chạy? Vì sao Java chọn như vậy?
6. Sửa `m[0] = 99` trong method thì mảng bên ngoài **đổi**, nhưng `m = new int[]{...}` thì **không đổi**. Giải thích bằng Stack/Heap.
7. Thí nghiệm nào chứng minh Java là pass-by-value chứ không phải pass-by-reference? Vì sao thí nghiệm kia không chứng minh được?

<details>
<summary>Đáp án</summary>

1. Vì mảng là **một khối bộ nhớ liền nhau** cấp phát một lần trên Heap. Vùng nhớ ngay sau nó có thể đã bị thứ khác chiếm, nên không thể nối thêm. Muốn "thêm" phải cấp mảng mới rồi copy — đó là việc `ArrayList` làm.
2. **Không** — ném `NullPointerException`. Mảng có đủ 3 ô nhưng mỗi ô chứa `null`, vì giá trị mặc định của mọi kiểu tham chiếu là `null`.
3. Hệ quả lịch sử: mảng có từ Java 1.0 và cài đặt ở tầng JVM nên `length` là **field**; còn `String` là class bình thường nên `length()` là **method**.
4. Mảng **không viết đè** `toString()` của class `Object`, nên dùng bản mặc định in `tênKiểu@hashcode`. `[` nghĩa là mảng, `I` nghĩa là `int`. Dùng `Arrays.toString()` để in nội dung.
5. Bản **`long`**. Java chọn overload theo ba pha và dừng ngay khi tìm được: pha 1 chỉ xét khớp chính xác + mở rộng kiểu, pha 2 mới cho boxing. Pha 1 tái hiện hành vi Java trước phiên bản 5 để không phá vỡ code cũ khi nâng cấp JDK.
6. `m[0] = 99` **đi theo địa chỉ** rồi sửa ô nhớ trên Heap — object đó là object mà cả hai biến cùng trỏ tới, nên bên ngoài thấy. Còn `m = new int[]{...}` chỉ **ghi địa chỉ mới vào ô biến `m`** — một ô nhớ riêng, không liên quan tới ô biến bên ngoài.
7. **Thí nghiệm C** (gán lại mảng) chứng minh được: nếu là pass-by-reference thì biến bên ngoài phải đổi theo, nhưng nó không đổi. Thí nghiệm B không chứng minh được vì việc sửa được nội dung object **hoàn toàn nhất quán** với pass-by-value — nó chỉ cho thấy hai biến đang cùng trỏ vào một object.

</details>

---

⬅️ **Bài trước:** [1.6 — Toán tử và luồng điều khiển](06-toan-tu-va-luong-dieu-khien.md)
➡️ **Bài tiếp:** Giai đoạn 2 — OOP: class, object, kế thừa, interface, đa hình
