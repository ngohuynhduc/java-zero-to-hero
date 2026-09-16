# Bài 1.4 — `String` và kiểu tham chiếu

> **Mục tiêu:** Hiểu Stack/Heap, vì sao `==` không dùng được cho `String`, và vì sao `String` bất biến.
>
> **Code thực hành:** [`bai-tap/01-java-core/ChuoiVaThamChieu.java`](../../bai-tap/01-java-core/ChuoiVaThamChieu.java)

---

## 1. Java chia dữ liệu thành **hai thế giới**

[Bài 1.3](03-bien-va-kieu-du-lieu.md) học 8 kiểu nguyên thủy. Mọi thứ còn lại — `String`, mảng, và mọi class bạn tự viết — thuộc thế giới thứ hai:

| | **Primitive** (nguyên thủy) | **Reference** (tham chiếu) |
|---|---|---|
| Gồm | Đúng 8 kiểu: `int`, `double`, `char`… | **Tất cả** phần còn lại |
| Viết hoa? | thường: `int` | Hoa: `String`, `Integer` |
| Biến chứa gì | **Chính giá trị** | **Địa chỉ** trỏ tới giá trị |
| Có thể `null`? | ❌ Không bao giờ | ✅ Có |
| Có method? | ❌ `5.abc()` vô nghĩa | ✅ `"abc".length()` |

Cách nhận biết nhanh: **tên kiểu viết hoa chữ đầu → là reference type.** `String` viết hoa chữ `S` — nó là một **class**, không phải kiểu nguyên thủy.

---

## 2. Bộ nhớ: **Stack** và **Heap**

Kiến thức nền giải thích gần như mọi hành vi "kỳ lạ" trong bài này.

```
            STACK                              HEAP
    (biến cục bộ — nhỏ, nhanh)      (object — lớn, do Garbage Collector dọn)
   ┌────────────────────────┐      ┌──────────────────────────────┐
   │  int soLuong  │   5    │      │                              │
   │                        │      │    ┌──────────────────┐      │
   │  String ten   │  ●─────┼──────┼───►│     "Duc"        │      │
   │                        │      │    └──────────────────┘      │
   │  String ten2  │  ●─────┼──────┼───►│     "Duc"        │      │
   └────────────────────────┘      │    └──────────────────┘      │
                                   └──────────────────────────────┘
      Biến chứa GIÁ TRỊ                  Object nằm ở đây
      hoặc chứa ĐỊA CHỈ
```

- **Primitive**: giá trị `5` nằm **trực tiếp** trong ô biến.
- **Reference**: ô biến chỉ chứa **địa chỉ**; nội dung `"Duc"` nằm ở Heap.

Nhớ [bài 0.1](../00-moi-truong/01-java-hoat-dong-nhu-the-nao.md) — **Garbage Collector** chỉ dọn **Heap**. Giờ bạn biết chính xác nó dọn gì: những object trên Heap mà không còn biến nào trên Stack trỏ tới.

---

## 3. Hệ quả số 1: `==` so sánh **địa chỉ**, không so sánh nội dung

Toán tử `==` luôn làm đúng **một** việc: so sánh **nội dung của ô biến**.

```java
int a = 5;
int b = 5;
a == b     // true  — so sánh 5 với 5     ✅ đúng ý bạn muốn
```

```java
String x = new String("hello");
String y = new String("hello");
x == y     // false — so sánh ĐỊA CHỈ ≠ ĐỊA CHỈ    ❌ không phải điều bạn muốn
```

```
   STACK                        HEAP
 x │ ●──────────────────────► "hello"   ← object #1, địa chỉ 0x1A
 y │ ●──────────────────────► "hello"   ← object #2, địa chỉ 0x2B

   0x1A == 0x2B  →  false
```

Hai object **khác nhau** trên Heap, dù nội dung y hệt.

### 🪤 Cái bẫy lớn nhất với dân JavaScript

```javascript
// JavaScript — string là PRIMITIVE
"hello" === "hello"     // true, luôn luôn đúng
```

```java
// Java — String là OBJECT
x == y                  // có thể false, dù nội dung giống hệt
```

Trực giác của bạn vốn đúng, chỉ là đang áp cho sai loại: trong JS, `{} === {}` cho `false` vì so sánh hai object là so sánh tham chiếu. Java cũng vậy — chỉ khác ở chỗ **`String` trong Java là object, không phải primitive như trong JS**.

### Cách đúng: `equals()`

```java
x.equals(y)    // true — so sánh NỘI DUNG
```

`equals()` là method mà **mọi object trong Java đều có** (kế thừa từ class gốc `Object`). Class `String` viết đè nó để so sánh từng ký tự.

> 📌 **Quy tắc ghi xương:** `==` cho primitive, `equals()` cho object. Không có ngoại lệ.

---

## 4. Hệ quả số 2: `String` là **bất biến** (immutable)

Một object `String` sau khi tạo ra thì **không bao giờ thay đổi được nội dung**.

```java
String s = "Hello";
s.toUpperCase();              // ❗ KHÔNG đổi s
System.out.println(s);        // vẫn in "Hello"

s = s.toUpperCase();          // phải GÁN LẠI
System.out.println(s);        // giờ mới "HELLO"
```

`toUpperCase()` **không sửa** object cũ — nó **tạo object mới** trên Heap rồi trả về địa chỉ của object đó. Không hứng lấy kết quả thì nó bị vứt đi ngay.

```
Trước:   s ──► "Hello"

Gọi s.toUpperCase():
         s ──► "Hello"        ← object cũ, KHÔNG đổi
               "HELLO"        ← object mới, không ai giữ → GC dọn

Sau s = s.toUpperCase():
         s ──────────────────► "HELLO"
               "Hello"        ← giờ object cũ mới thành rác
```

### Vì sao Java thiết kế `String` bất biến?

| Lý do | Giải thích |
|---|---|
| **An toàn đa luồng** | Nhiều thread đọc cùng một `String` mà không cần khóa — không ai sửa được nó |
| **Dùng làm khóa** | `Map` cần khóa không đổi. Nếu `String` đổi được, khóa sẽ "lạc" khỏi vị trí đã lưu |
| **Bảo mật** | Đường dẫn file, chuỗi kết nối DB truyền vào hàm thì hàm đó không thể lén sửa |
| **Chia sẻ được** | Bất biến nên nhiều biến dùng chung một object an toàn → dẫn tới String pool ở mục 5 |

> ⚠️ **Cái giá phải trả:** nối chuỗi trong vòng lặp tạo ra **một object mới mỗi vòng**. Lặp 10.000 lần là 10.000 object rác. Giải pháp là `StringBuilder` — Giai đoạn 3.

---

## 5. Hệ quả số 3: **String pool**

Vì `String` bất biến nên **chia sẻ được an toàn**. Java tận dụng điều đó: giữ một vùng nhớ đặc biệt tên **String pool**, chứa mọi chuỗi viết thẳng trong code (*string literal*).

```java
String a = "hello";     // tạo "hello" trong pool
String b = "hello";     // KHÔNG tạo mới — dùng lại object có sẵn trong pool
```

```
   STACK                    HEAP — String pool
 a │ ●─────────┐
                ├────────► "hello"     ← CHỈ MỘT object
 b │ ●─────────┘

   a == b  →  true   (cùng một địa chỉ)
```

Chuỗi xuất hiện dày đặc trong mọi chương trình, nên tái sử dụng giúp tiết kiệm bộ nhớ đáng kể.

### 🚨 Vì sao pool khiến `==` trở thành quả bom hẹn giờ

Vì literal nằm chung pool nên `==` **trông có vẻ hoạt động**:

```java
String a = "hello";
String b = "hello";
if (a == b) { ... }        // true — chạy đúng!
```

Người mới thấy vậy liền kết luận "`==` dùng được cho `String`". Nhưng chuỗi **không phải literal** thì không vào pool:

```java
String tuNguoiDung = scanner.nextLine();      // từ bàn phím
String tuDatabase  = resultSet.getString(1);  // từ DB
String tuApi       = request.getParam("ten"); // từ HTTP request

if (tuNguoiDung == "hello") { ... }           // ❌ LUÔN LUÔN false
```

Nghĩa là: **code chạy đúng trên dữ liệu test viết cứng, rồi sai khi gặp dữ liệu thật ở production.** Đúng kiểu bug tệ nhất — nó im lặng chờ.

Ở backend, gần như **toàn bộ** chuỗi bạn xử lý đều đến từ request, database hay file. **Không có chuỗi nào trong đó nằm ở String pool.**

---

## 6. Kết quả thực nghiệm

```
--- So sanh bang == ---
a == b : true         (hai literal — cùng object trong pool)
a == c : false        (c = new String(...) — object riêng trên Heap)

--- So sanh bang equals ---
a.equals(b) : true
a.equals(c) : true    (so sánh nội dung — luôn đúng)

--- String la bat bien ---
Sau khi goi toUpperCase() : hello    (KHÔNG đổi)
Sau khi gan lai           : HELLO

--- Ghep chuoi: luc compile vs luc chay ---
a == d : true         d = "hel" + "lo"       — ghép LÚC COMPILE
a == e : false        e = phanDau + "lo"     — ghép LÚC CHẠY
```

### 6.1. `"hel" + "lo"` được ghép **lúc compile**

Cả hai toán hạng đều là **hằng số mà `javac` biết chắc từ lúc đọc code**. Compiler tính sẵn kết quả, rồi đặt chuỗi `"hello"` thành literal như thể bạn viết thẳng nó. Literal thì vào **String pool** → dùng chung object với `a` → `==` cho `true`.

Kỹ thuật này tên là **constant folding** (gấp hằng số).

### 6.2. `phanDau + "lo"` được ghép **lúc chạy**

`phanDau` là biến thường. Giá trị của biến chỉ chắc chắn lúc chạy, nên compiler không tính trước. Phép ghép hoãn tới runtime, kết quả là **object `String` mới trên Heap**, không liên quan tới pool → `false`.

### 6.3. 🔬 Bằng chứng: chỉ thêm `final` là đổi kết quả

```java
String bienThuong = "hel";
String e1 = bienThuong + "lo";     // e1 == a  →  false

final String bienFinal = "hel";
String e2 = bienFinal + "lo";      // e2 == a  →  TRUE
```

Nhìn bytecode do `javap -c` in ra, hai dòng này biên dịch ra **hai thứ hoàn toàn khác nhau**:

| Biểu thức | Bytecode | Nghĩa là |
|---|---|---|
| `bienThuong + "lo"` | `invokedynamic makeConcatWithConstants` | **Gọi hàm ghép chuỗi lúc chạy** → sinh object mới |
| `bienFinal + "lo"` | `ldc #7  // String hello` | **Nạp thẳng hằng số** `#7` — đúng hằng số mà `a` đang dùng |

`ldc` = *load constant*. Với biến `final`, phép ghép **biến mất hoàn toàn** khỏi chương trình — `javac` đã tính xong từ lúc biên dịch.

Vì sao `final` đổi được cục diện? Vì `final` + khởi tạo bằng literal khiến biến trở thành **compile-time constant** — giá trị được đảm bảo không bao giờ đổi, nên compiler có quyền thay nó bằng chính giá trị đó.

> Ví dụ đẹp nối lại [bài 1.2](02-compile-time-vs-runtime.md): cùng một dòng code, việc nó xảy ra ở **cửa 1** hay **cửa 2** dẫn tới kết quả khác hẳn.

### 6.4. `.intern()`

```java
String e3 = (bienThuong + "lo").intern();
e3 == a    // true
```

`.intern()` đưa một chuỗi runtime vào String pool và trả về tham chiếu trong pool. Ít dùng thực tế — biết để hiểu cơ chế, đừng dùng nó để "làm cho `==` chạy đúng".

---

## 7. 🎯 Bài học thật sự

Kết quả của `==` trên `String` phụ thuộc vào:

- chuỗi có phải literal viết thẳng trong code không
- biến có `final` không
- compiler có gấp hằng được không
- chuỗi đến từ bàn phím, database hay HTTP request

Nói cách khác: **phụ thuộc vào những chi tiết bạn không kiểm soát và không nên phải nghĩ tới.** Đó là lý do quy tắc phải tuyệt đối — **luôn dùng `equals()`**, không bao giờ cân nhắc `==` cho `String`.

---

## 8. ⚠️ `equals()` cũng có bẫy — rất quan trọng cho backend

```java
String tuRequest = request.getParam("trangThai");   // có thể là null!

tuRequest.equals("ACTIVE")      // 💥 NullPointerException nếu null
```

Hai cách viết an toàn:

```java
"ACTIVE".equals(tuRequest)              // ✅ literal đứng trước, không bao giờ null
Objects.equals(tuRequest, "ACTIVE")     // ✅ rõ ràng hơn, xử lý null cả hai phía
```

Cách đầu gọi là **Yoda condition** — trông ngược nhưng cực kỳ phổ biến trong code Java, vì dữ liệu từ request, database hay API đều có thể `null`. Bạn sẽ gặp liên tục từ Giai đoạn 6.

---

## 9. Tự kiểm tra

1. Vì sao `String` viết hoa chữ `S` còn `int` viết thường?
2. `==` thực chất so sánh cái gì? Vì sao nó đúng với `int` mà sai với `String`?
3. Trong JS, `"a" === "a"` luôn `true`. Vì sao Java lại khác?
4. `s.toUpperCase()` mà `s` không đổi — object `"HELLO"` vừa tạo ra đi đâu?
5. Vì sao String pool khiến lỗi dùng `==` **nguy hiểm hơn** là nếu `==` luôn sai?
6. `tuRequest.equals("ACTIVE")` có gì rủi ro? Viết lại thế nào cho an toàn?

<details>
<summary>Đáp án</summary>

1. Vì `String` là **class** (reference type) còn `int` là **kiểu nguyên thủy**. Convention Java: tên class viết `PascalCase`.
2. `==` so sánh **nội dung của ô biến**. Với primitive, ô biến chứa chính giá trị → so sánh đúng. Với reference, ô biến chứa **địa chỉ** → nó so sánh địa chỉ chứ không so sánh nội dung object.
3. Vì trong JavaScript, string là **primitive**; trong Java, `String` là **object**. So sánh object bằng `===` trong JS (`{} === {}`) cũng cho `false` — cùng một nguyên lý.
4. Nó nằm trên Heap nhưng **không có biến nào trỏ tới**, nên **Garbage Collector** sẽ thu hồi.
5. Vì literal nằm chung pool nên `==` **trông như chạy đúng** trong test viết cứng. Người viết tin là đúng, rồi code sai khi gặp dữ liệu thật từ request/DB ở production. Nếu `==` luôn sai thì đã bị phát hiện ngay từ lần chạy đầu.
6. `tuRequest` có thể `null` → ném `NullPointerException`. Viết `"ACTIVE".equals(tuRequest)` (Yoda condition) hoặc `Objects.equals(tuRequest, "ACTIVE")`.

</details>

---

⬅️ **Bài trước:** [1.3 — Biến và kiểu dữ liệu nguyên thủy](03-bien-va-kieu-du-lieu.md)
➡️ **Bài tiếp:** 1.5 — Wrapper class (`int` vs `Integer`), autoboxing và ép kiểu
