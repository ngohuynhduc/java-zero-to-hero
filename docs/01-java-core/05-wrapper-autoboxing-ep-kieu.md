# Bài 1.5 — Wrapper class, autoboxing và ép kiểu

> **Mục tiêu:** Hiểu vì sao Java cần hai cách biểu diễn một con số, và vì sao sự tiện lợi của autoboxing lại là nơi `NullPointerException` ẩn nấp.
>
> **Code thực hành:** [`bai-tap/01-java-core/WrapperVaEpKieu.java`](../../bai-tap/01-java-core/WrapperVaEpKieu.java)

---

## 1. Vấn đề: hai thế giới không nói chuyện được với nhau

[Bài 1.4](04-string-va-kieu-tham-chieu.md) chia Java thành **primitive** và **reference**. Sự chia đôi đó tạo ra một vấn đề rất thật.

Muốn có một danh sách số nguyên — trong Java, danh sách là `List`:

```java
List<int> danhSach = new ArrayList<>();     // ❌ KHÔNG compile được
```

`javac` từ chối thẳng:

```
Tn3.java:5: error: unexpected type
        List<int> ds = new ArrayList<>();
             ^
  required: reference
  found:    int
```

Hai dòng cuối nói rõ luật: **`required: reference`** — chỗ đó chỉ nhận kiểu tham chiếu.

### Vì sao `List` không chứa được primitive?

Vì **generic** (phần `<...>`) được cài đặt bằng kỹ thuật **type erasure**: sau khi biên dịch, mọi `List<T>` đều trở thành `List<Object>` ở tầng bytecode. Bên trong, `List` lưu mọi thứ dưới dạng `Object`.

Mà `int` **không phải** `Object` — nó thuộc thế giới primitive, không kế thừa từ đâu, không có method nào.

> Generic và type erasure sẽ học kỹ ở **Giai đoạn 3**. Hiện tại chỉ cần nắm: **mọi cấu trúc dữ liệu của Java (`List`, `Map`, `Set`) chỉ chứa được object.**

Ở backend, bạn xử lý danh sách liên tục — danh sách ID, bản ghi từ DB, tham số. Nếu primitive không vào được `List` thì cần **một cây cầu**.

---

## 2. Wrapper class — lớp bọc cho primitive

| Primitive | Wrapper | | Primitive | Wrapper |
|---|---|---|---|---|
| `byte` | `Byte` | | `float` | `Float` |
| `short` | `Short` | | `double` | `Double` |
| `int` | **`Integer`** ⚠️ | | `char` | **`Character`** ⚠️ |
| `long` | `Long` | | `boolean` | `Boolean` |

Sáu cái chỉ viết hoa chữ đầu. **Hai cái đổi tên hẳn** — `int` → `Integer`, `char` → `Character`.

```java
List<Integer> danhSach = new ArrayList<>();     // ✅ chạy được
```

Vì là object nên wrapper mang đầy đủ tính chất của thế giới reference:

```
        STACK                          HEAP
   ┌──────────────────┐        ┌────────────────────┐
   │ int x     │  5   │        │                    │   ← x: giá trị NẰM TRONG biến
   │                  │        │   ┌────────────┐   │
   │ Integer y │  ●───┼────────┼──►│  5         │   │   ← y: biến chứa ĐỊA CHỈ
   └──────────────────┘        │   └────────────┘   │
                               └────────────────────┘
```

| | `int` | `Integer` |
|---|---|---|
| Nằm ở đâu | Giá trị ngay trong biến (Stack) | Object trên Heap, biến giữ địa chỉ |
| Gán `null` được? | ❌ Không bao giờ | ✅ Được |
| Có method? | ❌ | ✅ `.equals()`, `.compareTo()`… |
| Vào được `List`? | ❌ | ✅ |
| Tốn bộ nhớ | 4 byte | ~16 byte + 4-8 byte cho tham chiếu |
| Tốc độ | Nhanh hơn | Chậm hơn (phải đi qua địa chỉ) |

---

## 3. Khác biệt cốt tử: `null`

Không phải chi tiết vụn vặt — đây là lý do wrapper tồn tại ở tầng dữ liệu.

```sql
CREATE TABLE nguoi_dung (
    id       BIGINT  NOT NULL,
    tuoi     INT     NULL      -- cho phép trống!
);
```

Ánh xạ sang Java:

```java
int tuoi;        // ❌ không biểu diễn được "chưa khai"
```

`int` **buộc** phải mang một con số. Không có tuổi thì lấy gì? `0`? Vậy trẻ sơ sinh thì sao. `-1`? Vậy mọi chỗ đọc giá trị này đều phải nhớ quy ước ngầm, và chỉ cần một chỗ quên là thống kê sai.

```java
Integer tuoi;    // ✅ null = "không có dữ liệu", khác hẳn 0
```

`null` mang ý nghĩa mà con số không mang được: **sự vắng mặt của dữ liệu**.

> 📌 **Quy tắc cho Giai đoạn 7 (JPA):** field ánh xạ cột `NULL` được thì **phải** dùng wrapper. Dùng primitive ở đó sẽ khiến chương trình nổ khi đọc phải bản ghi có giá trị trống.

---

## 4. Autoboxing và unboxing — cây cầu tự động

Từ Java 5, compiler tự chèn code chuyển đổi:

```java
Integer a = 10;                       // AUTOBOXING — bạn viết
Integer a = Integer.valueOf(10);      //            — compiler sinh ra

int b = a;                            // UNBOXING   — bạn viết
int b = a.intValue();                 //            — compiler sinh ra
```

```
    int  ──────[ autoboxing ]──────►  Integer
   (giá trị)                          (object)
    int  ◄─────[  unboxing  ]───────  Integer
```

Rất tiện. Nhưng đây là **sự tiện lợi vô hình** — và cái gì vô hình thì khó debug.

---

## 5. 🚨 Bẫy 1: `NullPointerException` ở nơi không thấy method nào

```java
Integer chuaCoDuLieu = null;
int soSanh = chuaCoDuLieu;          // compile sạch, nổ lúc chạy
```

`javac` **không chặn** — về mặt kiểu dữ liệu, gán `Integer` cho `int` hợp lệ hoàn toàn. Đây là **lỗi runtime** theo phân loại ở [bài 1.2](02-compile-time-vs-runtime.md).

### Kết quả thực tế

```
Exception in thread "main" java.lang.NullPointerException:
    Cannot invoke "java.lang.Integer.intValue()" because "<local18>" is null
    at WrapperVaEpKieu.main(WrapperVaEpKieu.java:29)
```

Nổ ở **chính dòng gán** (dòng 29), chương trình không bao giờ chạy tới dòng `println` phía dưới.

### 🔬 Bằng chứng bytecode

`javap -c` cho thấy chính xác compiler đã chèn gì:

```
221: aconst_null                                  ← đẩy null lên stack
222: astore        18                             ← lưu vào biến cục bộ slot 18
                                                     (dòng 28: Integer chuaCoDuLieu = null)
224: aload         18                             ← nạp biến slot 18 ra
226: invokevirtual Integer.intValue:()I           ← 💥 GỌI METHOD
                                                     (dòng 29: int soSanh = chuaCoDuLieu)
```

Cụm `<local18>` trong thông báo lỗi khớp **chính xác** với `astore 18` / `aload 18`. JVM không biết bạn đặt tên biến là `chuaCoDuLieu` — trong bytecode nó chỉ là **slot số 18**.

### Vì sao bắt buộc phải có lời gọi `.intValue()`?

`chuaCoDuLieu` là **object trên Heap**; `soSanh` là **ô nhớ chứa trực tiếp 4 byte** trên Stack. Hai thế giới khác nhau — không thể "đổ" cái này sang cái kia. Phải **hỏi object** giá trị bên trong nó là bao nhiêu. Việc hỏi đó chính là `.intValue()`.

Object là `null` nghĩa là **không có object nào để hỏi**. Nên nổ.

### 🎯 Định nghĩa NPE không hề sai

"NPE xảy ra khi gọi method trên `null`" — **hoàn toàn đúng**. Chỉ là ở đây **lời gọi method do compiler viết hộ**, nên mắt không nhìn thấy nó trong source.

Đây là ví dụ đầu tiên về **syntactic sugar** (đường cú pháp): cú pháp ngắn gọn được compiler khai triển thành code đầy đủ. Tiện — nhưng **che giấu cả chi phí lẫn rủi ro**. Khái niệm này quay lại nhiều lần, đặc biệt ở Giai đoạn 5 khi Spring làm những việc trông như phép thuật.

> 📌 **Cách đọc code từ giờ:** mỗi lần một `Integer` (hay `Long`, `Double`, `Boolean`) đứng ở vị trí mà ngữ cảnh đòi hỏi primitive — phép so sánh, phép tính, phép gán — là **ở đó có một lời gọi method vô hình**. Chỗ nào có lời gọi method thì chỗ đó NPE được.

### Dạng hay gặp nhất ở backend

```java
Integer diem = repository.timDiem(userId);   // không có bản ghi → trả null
if (diem > 5) {                              // 💥 NPE ngay tại đây
    ...
}
```

Phép so sánh `diem > 5` buộc phải unbox `diem` thành `int`. Một dấu `>` vô hại kéo theo một lời gọi method ngầm.

---

## 6. 🚨 Bẫy 2: `Integer` cache — bản sao của String pool

Java tạo sẵn và giữ lại object `Integer` cho giá trị từ **−128 đến 127**. Mã nguồn thật của JDK:

```java
public static Integer valueOf(int i) {
    if (i >= IntegerCache.low && i <= IntegerCache.high)   // low = -128, high = 127
        return IntegerCache.cache[i + 128];                // ← lấy object CÓ SẴN
    return new Integer(i);                                 // ← TẠO object mới
}
```

Một câu `if` duy nhất giải thích toàn bộ hiện tượng:

```
        IntegerCache.cache[]  —  256 object, tạo sẵn một lần khi JVM khởi động
   ┌──────┬──────┬─────┬──────┬──────┐
   │ -128 │ -127 │ ... │ 126  │ 127  │
   └──────┴──────┴─────┴──────┴───▲──┘
                                  │
   a ─────────────────────────────┤        a == b  →  TRUE
   b ─────────────────────────────┘        (cùng một địa chỉ)


   c ──────────►  [ Integer 128 ]   ← new, địa chỉ 0x1A
                                            c == d  →  FALSE
   d ──────────►  [ Integer 128 ]   ← new, địa chỉ 0x2B    (hai object khác nhau)
```

**Vì sao đúng khoảng `-128 → 127`?** Đó là phạm vi của `byte`, và cũng là vùng giá trị nhỏ được dùng dày đặc nhất: biến đếm, chỉ số vòng lặp, cờ trạng thái, mã lỗi.

### 🔗 So sánh với String pool ở bài 1.4

| | String pool | Integer cache |
|---|---|---|
| Ý tưởng | Chia sẻ object bất biến để tiết kiệm bộ nhớ | Y hệt |
| Áp dụng cho | Chuỗi literal | Số trong `-128 → 127` |
| Hậu quả | `==` **trông như** chạy đúng | Y hệt |
| Khi nào lộ ra | Chuỗi đến từ request/DB | Số vượt 127 |

**Phần đáng sợ ở backend:**

```java
Long idTest = 1L;      // test với id nhỏ  → == chạy đúng  → lọt qua test
Long idThat = 5000L;   // id thật từ DB    → == luôn sai   → nổ ở production
```

Cùng một dòng code, đúng hay sai phụ thuộc vào **giá trị dữ liệu chạy qua nó**. Loại bug này không phát hiện được bằng đọc code.

> 📌 Kết luận lặp lại lần thứ hai, và sẽ còn đúng mãi: **`==` cho primitive, `equals()` cho object.**

---

## 7. Ép kiểu (casting)

### Mở rộng (widening) — Java tự làm

```
byte ──► short ──► int ──► long ──► float ──► double
                    ▲
         char ──────┘
```

Đi **theo chiều mũi tên** thì không mất dữ liệu (thùng nhỏ đổ sang thùng to), nên Java tự chuyển:

```java
int soNguyen = 100;
long soDai = soNguyen;        // tự động
double soThuc = soDai;        // tự động  → in ra 100.0
```

### Thu hẹp (narrowing) — phải tự ép

```java
double coPhanLe = 9.99;
int catCut = coPhanLe;             // ❌ error: possible lossy conversion from double to int
int catCut = (int) coPhanLe;       // ✅ "tôi biết có thể mất dữ liệu, cứ làm"
```

Cụm `(int)` là **chữ ký xác nhận**. Java không cấm bạn làm việc nguy hiểm — nó chỉ không cho bạn làm **một cách vô tình**.

---

## 8. Kết quả thực nghiệm

```
--- Integer cache ---
a == b (127) : true
c == d (128) : false
c.equals(d)  : true

--- Mo rong: Java tu lam ---
int -> long -> double : 100.0

--- Thu hep: phai ep tay ---
int 130      -> byte : -126
double 9.99  -> int  : 9
long 10 ty   -> int  : 1410065408
```

### 8.1. `(byte) 130` ra `−126`, **không phải** `−128`

Cơ chế là quay vòng — nhưng quay **bao nhiêu bước** mới là chỗ dễ nhầm.

**Cách đếm bước:**

```
... 125   126   127  │ -128   -127   -126 ...
                     │
       hết phạm vi ──┘  ↑128    ↑129    ↑130
```

`127` là giá trị cuối. Thêm 1 (thành 128) mới quay về `-128`. `130` vượt `127` tới **3 đơn vị**, nên đi tiếp 2 bước nữa.

**Cách nhìn bit — đây mới là cơ chế thật:**

```
int 130   =  00000000 00000000 00000000 10000010     ← 32 bit
                                         └────────┘
(byte) cắt lấy 8 bit cuối, VỨT phần còn lại:  10000010
                                              ▲
                                     bit dấu = 1 → số ÂM
```

Đọc `10000010` theo bù hai ([bài 1.3](03-bien-va-kieu-du-lieu.md)): đảo bit được `01111101` = 125, cộng 1 được 126, gắn dấu âm → **−126**.

**Công thức nhanh:** với `n` trong `128 → 255`: `(byte) n = n − 256`. Vậy `130 − 256 = −126`.

### 8.2. `(int) 9.99` ra `9` — cắt cụt, không làm tròn

```java
(int) 9.99     →  9
(int) 9.999999 →  9        ← sát 10 tới đâu cũng vẫn là 9
(int) -9.99    →  -9       ← cắt về phía số 0, không phải làm tròn xuống

Math.round(9.99) → 10      ← muốn làm tròn phải nói rõ
```

**Bẫy thực tế:** tính tiền giảm giá, chia trung bình, quy đổi đơn vị — dùng `(int)` là mất tiền một cách có hệ thống, và luôn mất về phía bất lợi cho một bên.

### 8.3. `(int) 10000000000L` ra `1410065408`

Cùng nguyên lý: **cắt bit**. `long` có 64 bit, `int` giữ **32 bit thấp**, vứt 32 bit cao.

```
long 10.000.000.000:
  00000000 00000000 00000000 00000010 │ 01010100 00001011 11100100 00000000
  └──────── 32 bit cao: VỨT ─────────┘ └───────── 32 bit thấp: GIỮ ────────┘
                                                        │
                                                        ▼
                                                  1.410.065.408
```

Kiểm chứng: `10.000.000.000 − (2 × 4.294.967.296) = 1.410.065.408`.

#### 🚨 Vì sao câu này nguy hiểm hơn 8.1 rất nhiều

`−126` là số **âm** — nếu đó là số lượng hàng hay tuổi, bạn nhận ra ngay có gì hỏng.

Còn `1.410.065.408` thì **dương, lớn, trông hoàn toàn bình thường**. Nếu đó là một ID, một số tiền, hay một lượt xem — **không có gì báo cho bạn biết nó sai**.

Đây là lý do [bài 1.3](03-bien-va-kieu-du-lieu.md) nhấn mạnh **"ID trong database phải dùng `long`"**. Chỉ cần một chỗ trong pipeline lỡ ép `long` xuống `int`, dữ liệu hỏng trong im lặng.

### 8.4. 📌 Quy luật chung của narrowing

**Ép kiểu thu hẹp = vứt bit thừa, giữ lại bit thấp.**

| | Nhiều người tưởng | Thực tế |
|---|---|---|
| Vượt phạm vi | Kẹp về giá trị biên (`130` → `127`) | **Quay vòng**, có thể **đổi dấu** (`130` → `−126`) |
| Số thập phân | Làm tròn (`9.99` → `10`) | **Cắt cụt** (`9.99` → `9`) |
| Khi mất dữ liệu | Có cảnh báo gì đó | **Hoàn toàn im lặng** |

### 8.5. 🔗 Khép lại cái bẫy ở bài 1.3

```java
byte a = 10, b = 20;
byte c = a + b;      // ❌ incompatible types: possible lossy conversion from int to byte
```

Ghép hai mảnh:

1. **Numeric promotion** ([bài 1.3](03-bien-va-kieu-du-lieu.md)) — `byte + byte` được nâng lên `int` trước khi tính. Kết quả `a + b` mang kiểu `int`.
2. **Narrowing** (bài này) — gán `int` vào `byte` là đi ngược chiều mũi tên, phải ép tay.

Compiler **không quan tâm** giá trị thực tế là `30` hay `3000` — nó chỉ xét **kiểu**. Kiểu `int` *có thể* chứa giá trị vượt sức `byte`, nên luật áp dụng vô điều kiện.

---

## 9. Static analysis — tầng kiểm soát sớm hơn cả `javac`

VS Code cảnh báo dòng `int soSanh = chuaCoDuLieu;` dù `javac` không báo lỗi. Hai công cụ làm hai việc khác nhau:

| | `javac` | Eclipse JDT (trong VS Code) |
|---|---|---|
| Kiểm tra | **Luật của ngôn ngữ** | Luật ngôn ngữ **+ phân tích luồng giá trị** |
| Với dòng này | `Integer` → `int` hợp lệ → **không lỗi** | Lần theo luồng: biến *chỉ có thể* là `null` ở đây → **cảnh báo** |
| Mức độ | `error` — chặn build | `warning` — **không** chặn build |

Thứ IDE làm thêm gọi là **static analysis** (phân tích tĩnh) — đọc code để suy ra giá trị *có thể* nhận, mà không cần chạy. Sơ đồ cửa kiểm soát giờ có thêm một bậc:

```
  Bạn gõ ─► Static analysis ─► javac ─► JVM ─► Test ─► Production
             (cảnh báo)        (chặn)   (nổ)
                 │                │        │       │         │
             không chắc       chắc chắn         tốn công   ĐẮT NHẤT
```

**Vì sao `javac` không làm luôn?** Vì phân tích `null` ở dạng tổng quát là bài toán **không giải được trọn vẹn**. Trường hợp này dễ vì `null` gán thẳng ngay dòng trên. Nhưng ngoài đời:

```java
Integer diem = repository.timDiem(userId);   // method này có trả null không?
if (diem > 5) { ... }                        // KHÔNG công cụ nào cảnh báo được
```

Không ai biết `timDiem()` trả về gì nếu không chạy thật. **Không có cảnh báo, và nó nổ ở production.**

Chính vì lỗ hổng này mà Java sinh ra `Optional<T>` (Java 8) và annotation `@Nullable` / `@NonNull` — công cụ để lập trình viên **nói rõ ý định** cho máy hiểu. Học ở Giai đoạn 3, dùng liên tục từ Giai đoạn 7.

Trước mắt, hai cách phòng thân:

```java
if (diem != null && diem > 5) { ... }               // kiểm tra tường minh
int d = Objects.requireNonNullElse(diem, 0);        // có giá trị thay thế
```

---

## 10. Tự kiểm tra

1. Vì sao `List<int>` không compile được, nhưng `List<Integer>` thì được?
2. Dòng `int x = someInteger;` không có dấu chấm nào. Vậy `NullPointerException` lấy đâu ra method để gọi?
3. Vì sao cột database cho phép `NULL` thì field Java **phải** dùng wrapper?
4. `Integer a = 127, b = 127;` cho `a == b` là `true`, nhưng đổi thành `128` lại `false`. Vì sao?
5. `(byte) 130` ra `−126`. Vì sao không phải `127` (kẹp biên) và không phải `−128`?
6. Vì sao `(int) 10000000000L` nguy hiểm hơn `(byte) 130` trong hệ thống thật?

<details>
<summary>Đáp án</summary>

1. Vì generic dùng **type erasure** — sau biên dịch mọi `List<T>` thành `List<Object>`. `int` là primitive, không phải `Object`, nên không khớp. `Integer` là object nên vào được.
2. **Compiler tự chèn** lời gọi `.intValue()` (unboxing). Bytecode có `invokevirtual Integer.intValue:()I`, dù source không có dấu chấm nào. Định nghĩa NPE vẫn đúng — lời gọi chỉ bị giấu đi.
3. Vì primitive **không thể** mang giá trị `null`. Không có wrapper thì phải chọn một con số làm "không có dữ liệu" (`0`, `-1`), gây nhập nhằng với giá trị thật; và chương trình sẽ nổ khi đọc phải bản ghi có cột trống.
4. Vì `Integer.valueOf()` trả về object **từ cache** cho giá trị trong `-128 → 127` (nên `a` và `b` là cùng một object), còn ngoài khoảng đó thì `new` object mới mỗi lần.
5. Vì ép kiểu thu hẹp là **cắt bit**, không kẹp biên và không làm tròn. Giữ 8 bit cuối của `130` được `10000010`, bit dấu là `1` nên là số âm, giá trị `-126`. Không phải `-128` vì `130` vượt `127` tới **3** đơn vị chứ không phải 1.
6. Vì `-126` là số **âm** nên bất thường lộ ra ngay. Còn `1.410.065.408` **dương và trông hợp lý** — nếu là ID hay số tiền thì không có dấu hiệu nào cho biết dữ liệu đã hỏng.

</details>

---

⬅️ **Bài trước:** [1.4 — `String` và kiểu tham chiếu](04-string-va-kieu-tham-chieu.md)
➡️ **Bài tiếp:** 1.6 — Toán tử và luồng điều khiển (`if`, `switch`, vòng lặp)
