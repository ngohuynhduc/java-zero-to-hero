# Bài 1.6 — Toán tử và luồng điều khiển

> **Mục tiêu:** Nắm các toán tử và cấu trúc điều khiển của Java, cùng bốn cái bẫy mà dân JavaScript chắc chắn dính: không có truthy/falsy, chia số nguyên, `switch` chảy tràn, và `NaN`.
>
> **Code thực hành:** [`bai-tap/01-java-core/ToanTuVaDieuKhien.java`](../../bai-tap/01-java-core/ToanTuVaDieuKhien.java)

---

## 1. Java **không có** truthy/falsy

Trong JavaScript, mọi giá trị đều "ngả" được về đúng/sai:

```javascript
if (1)        { }    // chạy
if ("")       { }    // không chạy
if ([])       { }    // chạy (!)
if (0)        { }    // không chạy
```

Java từ chối thẳng:

```java
int x = 1;
if (x) { }           // ❌ error: incompatible types: int cannot be converted to boolean
```

**Điều kiện trong Java bắt buộc phải là `boolean`.** Không ngoại lệ, không quy đổi ngầm.

### Được gì từ sự cứng nhắc này?

| | JavaScript | Java |
|---|---|---|
| Phải nhớ bảng truthy/falsy | ✅ Có (`[]` thật, `""` giả, `"0"` thật, `0` giả…) | ❌ Không cần |
| `if (0)` vs `if ("0")` | Hai kết quả khác nhau — bug kinh điển | Cả hai đều là lỗi compile |
| Gõ nhầm `=` thành `==` | Âm thầm chạy sai | Thường bị chặn ở compile |

```java
int so = 5;
if (so = 10) { }     // ❌ error: int cannot be converted to boolean
```

Trong JS, `if (so = 10)` **chạy được** và luôn đúng — bug im lặng. Trong Java, `so = 10` là biểu thức **kiểu `int`**, mà `if` đòi `boolean`, nên `javac` chặn.

### ⚠️ Nhưng cái bẫy vẫn còn một lối thoát

```java
boolean daDangNhap = false;
if (daDangNhap = true) {         // ✅ COMPILE ĐƯỢC, và luôn chạy vào trong
    System.out.println("Vào được!");
}
```

Đã chạy thử: nó **in ra "Vào được"**, và `daDangNhap` bị đổi thành `true` luôn.

Vì sao lọt? Vì `daDangNhap = true` là biểu thức **kiểu `boolean`** — đúng thứ `if` cần. Compiler không có cơ sở từ chối.

> 🛡️ **Cách phòng:** đừng bao giờ viết `== true` hay `== false`:
> ```java
> if (daDangNhap)   { }    // thay cho  if (daDangNhap == true)
> if (!daDangNhap)  { }    // thay cho  if (daDangNhap == false)
> ```
> Không gõ `==` thì không gõ nhầm được. Đây cũng là code sạch hơn.

---

## 2. 🪤 Phép chia số nguyên — cái bẫy đắt nhất bài này

```java
System.out.println(7 / 2);      // 3      ← KHÔNG phải 3.5
```

**Quy tắc:** khi **cả hai** toán hạng là số nguyên, Java thực hiện **phép chia nguyên** — lấy phần nguyên, vứt phần dư. Chỉ cần **một** bên là số thực thì mới ra kết quả thực:

```java
7 / 2      →  3       (int / int    → int)
7 / 2.0    →  3.5     (int / double → double)
```

JavaScript không có chuyện này vì JS chỉ có **một** kiểu số là `double` ([bài 1.3](03-bien-va-kieu-du-lieu.md)) — `7/2` luôn cho `3.5`.

### Nó cắn bạn ở đâu

```java
int tongDiem = 7;
int soMon = 2;
double trungBinh = tongDiem / soMon;    // 3.0  — SAI, đáng lẽ 3.5
```

Điều làm bẫy này khó thấy: **kiểu của biến nhận là `double`**, nên mắt thấy "à, số thực, chắc đúng rồi". Nhưng phép chia xảy ra **trước** khi gán, và lúc đó vẫn là `int / int`.

Và đây là phần khiến nhiều người sửa sai:

```java
double sai  = (double) (tongDiem / soMon);    // 3.0  — vẫn SAI!
double dung = (double) tongDiem / soMon;      // 3.5  — ĐÚNG
```

Dòng đầu: ngoặc bắt tính `7 / 2` trước → ra `3`, rồi mới ép `3` thành `3.0`. **Ép kiểu sau khi đã mất dữ liệu thì không cứu được gì** — nối thẳng với quy luật narrowing ở [bài 1.5](05-wrapper-autoboxing-ep-kieu.md).

Dòng sau: ép `tongDiem` thành `7.0` **trước**, nên phép chia là `double / int` → `3.5`.

> 📌 Mọi chỗ tính **trung bình, phần trăm, tỉ lệ, chia đều** ở backend đều là nơi bẫy này rình. Kết quả không nổ, không cảnh báo — chỉ đơn giản là sai.

Chiều âm: `-7 / 2` cho `-3`, **cắt về phía số 0** chứ không làm tròn xuống `-4`.

---

## 3. Toán tử `%` với số âm

```java
 7 % 3   →   1
-7 % 3   →  -1        ← âm!
 7 % -3  →   1
```

**Quy tắc: dấu của kết quả theo dấu của số bị chia** (toán hạng bên trái).

> Python cho `-7 % 3 = 2`. Hai ngôn ngữ chọn hai quy ước khác nhau — đừng mang thói quen từ bên kia sang.

### Bẫy thật: kiểm tra chẵn/lẻ

```java
if (n % 2 == 1) { }      // ❌ SAI với số âm: -3 % 2 = -1, không phải 1
if (n % 2 != 0) { }      // ✅ đúng với mọi số
```

Đoạn sai vẫn chạy đúng suốt trong test (ai lại test với số âm), rồi lặng lẽ bỏ sót toàn bộ dữ liệu âm ở production.

---

## 4. Chia cho `0`: **hai thế giới, hai hành vi**

```java
5 / 0        →  💥 ArithmeticException: / by zero
5.0 / 0      →  Infinity          ← không nổ!
-5.0 / 0     →  -Infinity
0.0 / 0.0    →  NaN               (Not a Number)
```

**Vì sao khác nhau?** Kiểu số nguyên **không có cách biểu diễn "vô cực"** — mọi mẫu bit của `int` đều đã dùng cho một số cụ thể. Không biểu diễn được thì chỉ còn cách ném ngoại lệ.

Số thực theo chuẩn **IEEE 754** ([bài 1.3](03-bien-va-kieu-du-lieu.md)) **có dành sẵn** mẫu bit cho `Infinity`, `-Infinity` và `NaN`. Có chỗ chứa nên không cần nổ.

### 🚨 Chính vì không nổ nên nó nguy hiểm hơn

```java
double tiLe = tong / soLuong;      // soLuong = 0 → tiLe = NaN
double diem = tiLe * 100;          // NaN
double cuoi = diem + 10;           // vẫn NaN
luuVaoDatabase(cuoi);              // ghi NaN vào DB
```

`NaN` **lây lan**: mọi phép tính chạm vào nó đều cho ra `NaN`. Không exception nào đánh động.

Cùng chủ đề với integer overflow ở [bài 1.3](03-bien-va-kieu-du-lieu.md): **những lỗi không kêu mới là những lỗi đắt nhất.**

---

## 5. `NaN != NaN` — không phải quái chiêu tùy hứng

`NaN` không phải *một* giá trị cụ thể — nó là **kết quả của một phép toán không xác định được**. Ba phép toán hoàn toàn khác nhau, cùng cho `NaN`:

```
0.0 / 0.0                      →  NaN
Infinity - Infinity            →  NaN
Math.sqrt(-1)                  →  NaN
```

Ba câu hỏi khác nhau, cả ba đều không có câu trả lời. Vậy **`0.0/0.0` có "bằng" `sqrt(-1)` không?**

Câu hỏi đó vô nghĩa. Chúng không phải hai con số trùng nhau — chúng là **hai lần máy tính từ chối trả lời**.

Nên IEEE 754 quy định dứt khoát: **`NaN` không bằng bất cứ thứ gì, kể cả chính nó.**

### Hệ quả: `NaN` là giá trị **duy nhất** trong Java mà `x == x` cho `false`

```java
if (x != x) { }               // true ⟺ x là NaN   (đúng nhưng khó đọc)
if (Double.isNaN(x)) { }      // ✅ nên dùng cái này
```

### 🚨 Bẫy ở backend

```java
if (giaTri == 0) { ... }          // ❌ không bắt được NaN
if (giaTri > 0)  { ... }          // ❌ NaN cho false
if (giaTri <= 0) { ... }          // ❌ NaN cũng cho false!
```

Mọi phép so sánh với `NaN` đều cho `false`. Nghĩa là `NaN` **lọt qua tất cả các nhánh kiểm tra** và rơi vào `else` — nhánh bạn viết cho "trường hợp còn lại bình thường".

---

## 6. 🔬 `==` và `equals()` bất đồng theo **cả hai chiều**

Kết quả đã chạy thật:

```
Double boc1 = 0.0 / 0.0;                        // NaN
Double boc2 = Infinity - Infinity;              // NaN

boc1 == boc2         →  false
boc1.equals(boc2)    →  TRUE      ← ngược nhau!

0.0 == -0.0                      →  TRUE
Double.valueOf(0.0).equals(-0.0) →  false      ← lại ngược nhau, theo chiều kia!
```

**Vì sao?** Hai toán tử trả lời **hai câu hỏi khác nhau**:

| | Trả lời câu hỏi | Dựa trên |
|---|---|---|
| `==` trên `double` | *"Hai số này có bằng nhau về mặt toán học không?"* | Quy tắc số học IEEE 754 |
| `.equals()` của `Double` | *"Hai giá trị này có cùng mẫu bit không?"* | So sánh từng bit |

- **`NaN`**: về toán học không bằng nhau (`==` → `false`), nhưng mọi `NaN` dùng **cùng một mẫu bit chuẩn** (`equals` → `true`).
- **`+0.0` và `−0.0`**: về toán học bằng nhau (`==` → `true`), nhưng **khác nhau ở bit dấu** (`equals` → `false`).

> ⚠️ Cho chính xác: dòng `boc1 == boc2` cho `false` còn có thêm một lý do — `boc1` và `boc2` là **hai object `Double` riêng biệt**, và `==` trên object là so sánh **địa chỉ** ([bài 1.4](04-string-va-kieu-tham-chieu.md)). Khác với `Integer`, lớp `Double` **không có cache**, nên mỗi lần boxing là một object mới. Hai nguyên nhân cùng dẫn tới `false`.

### Nó cắn bạn ở đâu

`HashSet`, `HashMap`, `List.contains()`, `sort()` — **tất cả đều dùng `equals()`**, không dùng `==`:

```java
Set<Double> tap = new HashSet<>();
tap.add(0.0 / 0.0);          // NaN
tap.add(Math.sqrt(-1));      // NaN
// → tập hợp chỉ có MỘT phần tử, vì equals() coi hai NaN là một

tap.add(0.0);
tap.add(-0.0);
// → thêm HAI phần tử, vì equals() coi chúng khác nhau
```

Cùng một dữ liệu, so sánh bằng toán tử thì ra một đằng, bỏ vào cấu trúc dữ liệu thì ra một nẻo.

> 📌 **Không dùng `double` làm khóa của `Map` hay phần tử của `Set`.**
>
> Và một lần nữa, củng cố kết luận đã gặp ở bài 1.4 và 1.5: **`==` và `equals()` không phải hai cách viết của cùng một phép so sánh.** Chúng trả lời hai câu hỏi khác nhau.

---

## 7. `&&` và `||` — "nối tắt" (short-circuit)

```java
false && f()      →  f() KHÔNG được gọi
true  || f()      →  f() KHÔNG được gọi
```

Vế trái đã đủ quyết định kết quả nên Java **không tính vế phải**. Java còn có `&` và `|` (một ký tự) — **luôn tính cả hai vế**.

### Đây không phải chuyện tối ưu tốc độ — nó là chuyện an toàn

```java
if (user != null && user.getTen().equals("admin")) { }    // ✅ an toàn
if (user != null &  user.getTen().equals("admin")) { }    // 💥 NPE khi user == null
```

Với `&&`, nếu `user` là `null` thì vế phải **không bao giờ chạy**. Với `&`, vế phải vẫn chạy → gọi method trên `null` → `NullPointerException` ([bài 1.5](05-wrapper-autoboxing-ep-kieu.md)).

> 📌 Mẫu `if (x != null && x.somethingElse())` là một trong những dòng bạn viết nhiều nhất ở backend. Nó chỉ an toàn nhờ short-circuit. **Luôn dùng `&&` và `||` hai ký tự.**

---

## 8. `switch` cổ điển và cái bẫy "chảy tràn" (fallthrough)

Người mới đọc `switch` như "chọn đúng một nhánh". **Sai.**

```
        switch (ngay)              ngay = 2
             │
             ▼
    ┌── case 1: ... ────┐          ①  NHẢY tới nhãn khớp (case 2)
    │                   │
    ├──►case 2: ... ────┤          ②  CHẠY từ đó xuống dưới...
    │                   │
    ├── case 3: ... ────┤          ③  ...và KHÔNG dừng lại
    │                   │
    └── default: ... ───┘          ④  cho tới khi gặp `break` hoặc hết khối
```

Nó là lệnh **nhảy tới nhãn**, không phải lệnh **chọn nhánh**. Thiếu `break` thì chạy tuột xuống các case phía dưới — kể cả `default`.

Di sản từ ngôn ngữ C, và **JavaScript cũng y hệt**.

Fallthrough **có** công dụng chính đáng — gộp nhiều giá trị vào chung một xử lý:

```java
case 1:
case 7:
    System.out.println("Cuoi tuan");
    break;
```

Nhưng hầu hết trường hợp còn lại, thiếu `break` là **lỗi gõ thiếu**. Và compiler **không cảnh báo**, vì nó không thể biết bạn cố ý hay không.

---

## 9. `switch` biểu thức (Java 14+) — bản vá cho vấn đề đó

```java
String loaiNgay = switch (ngay) {
    case 1, 7          -> "Cuoi tuan";
    case 2, 3, 4, 5, 6 -> "Ngay thuong";
    default            -> "Khong hop le";
};
```

| | `switch` cổ điển | `switch` biểu thức (`->`) |
|---|---|---|
| Fallthrough | ✅ Có — phải nhớ `break` | ❌ **Không**, mỗi nhánh độc lập |
| Nhiều giá trị chung | Phải xếp chồng `case` | `case 1, 7 ->` |
| Trả về giá trị | ❌ Không — phải gán vào biến bên ngoài | ✅ **Có** — gán thẳng được |
| Bắt buộc đủ mọi trường hợp | ❌ Không | ✅ Có (khi dùng làm biểu thức) |

Điểm cuối rất đáng giá: dùng làm biểu thức thì compiler **bắt buộc** phủ hết mọi khả năng (thường là phải có `default`). Thiếu là lỗi compile — chứ không phải nhận về `null` rồi nổ ở đâu đó sau này.

> 💡 Dấu `;` sau `}` đóng là **bắt buộc**, vì cả khối `switch` đó là một **biểu thức** nằm trong câu lệnh gán — giống `int x = 5;`.

**Quy tắc thực dụng: từ giờ ưu tiên `switch` biểu thức.** Chỉ dùng bản cổ điển khi thật sự cần fallthrough.

---

## 10. Vòng lặp

```java
for (int i = 0; i < 5; i++) { }          // biết trước số lần lặp

while (dieuKien) { }                      // không biết trước, kiểm tra TRƯỚC

do { } while (dieuKien);                  // chạy ít nhất 1 lần, kiểm tra SAU

for (int x : mangSo) { }                  // duyệt từng phần tử
```

Dạng cuối là **for-each**, đọc là *"với mỗi `x` trong `mangSo`"*. Tương đương `for (const x of arr)` của JS — Java dùng dấu **`:`** chứ không phải từ khóa `of`.

| Tình huống | Dùng gì |
|---|---|
| Biết số lần lặp, cần chỉ số `i` | `for` |
| Lặp tới khi thỏa điều kiện | `while` |
| Phải chạy ít nhất một lần (menu, nhập liệu) | `do-while` |
| Duyệt mảng / danh sách | **`for-each`** — ưu tiên, vì không thể sai chỉ số |

---

## 11. Kết quả thực nghiệm

```
--- Chia so nguyen ---
7 / 2             = 3
7 / 2.0           = 3.5
(double)(7 / 2)   = 3.0        ← ép kiểu SAU khi đã mất dữ liệu
(double)7 / 2     = 3.5        ← ép kiểu TRƯỚC

--- Modulo voi so am ---
7 % 3   = 1
-7 % 3  = -1

--- Chia cho 0 (so thuc) ---
5.0 / 0   = Infinity
0.0 / 0.0 = NaN
NaN == NaN = false

--- switch THIEU break ---
Thu ba
Thu tu
Khong xac dinh                 ← chảy tuột xuống cả default

--- switch bieu thuc ---
ngay 2 -> Ngay thuong
```

---

## 12. Tự kiểm tra

1. Vì sao `if (x)` với `x` kiểu `int` lại là lỗi compile trong Java mà không phải trong JS? Java được lợi gì?
2. `if (daDangNhap = true)` vẫn compile được. Vì sao, và cách viết nào tránh được hoàn toàn?
3. `double tb = (double) (7 / 2);` cho `3.0`. Sai ở đâu và sửa thế nào?
4. Vì sao `5 / 0` ném exception còn `5.0 / 0` thì không?
5. Vì sao IEEE 754 quy định `NaN != NaN`?
6. `NaN` khiến `==`, `>`, `<=` đều cho `false`. Hệ quả nguy hiểm là gì?
7. `boc1.equals(boc2)` cho `true` với hai `NaN`, nhưng `==` cho `false`. Hai phép so sánh đó hỏi câu hỏi gì khác nhau?
8. `switch` thiếu `break` với `ngay = 2` in ra 3 dòng. Vì sao không phải 1 dòng?

<details>
<summary>Đáp án</summary>

1. Vì Java không có quy đổi ngầm sang `boolean` — điều kiện bắt buộc là `boolean`. Lợi: không phải nhớ bảng truthy/falsy, và bắt được lỗi gõ nhầm `=` thành `==` ngay lúc compile.
2. Vì `daDangNhap = true` là biểu thức **kiểu `boolean`**, đúng thứ `if` cần, nên compiler không có cơ sở từ chối. Tránh bằng cách không bao giờ viết `== true` / `== false` — viết thẳng `if (daDangNhap)` / `if (!daDangNhap)`.
3. Ngoặc bắt tính `7 / 2` trước → `int / int` → `3`, rồi mới ép thành `3.0`. Dữ liệu đã mất trước khi ép. Sửa: `(double) 7 / 2` — ép **một toán hạng** trước khi chia.
4. Vì `int` không có mẫu bit nào để biểu diễn "vô cực", nên chỉ còn cách ném exception. `double` theo IEEE 754 **có dành sẵn** mẫu bit cho `Infinity` và `NaN`.
5. Vì `NaN` không phải một giá trị cụ thể mà là **kết quả của phép toán không xác định được**. `0.0/0.0` và `sqrt(-1)` là hai lần từ chối trả lời khác nhau — không có cơ sở nào nói chúng bằng nhau.
6. `NaN` **lọt qua mọi nhánh kiểm tra** và rơi vào `else` — nhánh dành cho "trường hợp bình thường". Cộng với tính lây lan, nó đi thẳng xuống database mà không có cảnh báo nào.
7. `==` trên `double` hỏi *"có bằng nhau về mặt toán học không"* (theo IEEE 754). `.equals()` của `Double` hỏi *"có cùng mẫu bit không"*. Mọi `NaN` dùng chung một mẫu bit chuẩn nên `equals` cho `true`.
8. Vì `switch` cổ điển là lệnh **nhảy tới nhãn rồi chạy tiếp**, không phải lệnh chọn nhánh. Không gặp `break` thì chạy tuột xuống hết các case phía dưới, kể cả `default`.

</details>

---

⬅️ **Bài trước:** [1.5 — Wrapper class, autoboxing và ép kiểu](05-wrapper-autoboxing-ep-kieu.md)
➡️ **Bài tiếp:** 1.7 — Mảng và method (truyền tham số theo giá trị)
