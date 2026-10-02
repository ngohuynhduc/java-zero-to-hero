# Bài 2.3 — Interface và abstract class

> **Mục tiêu:** Hiểu vì sao Java cấm đa kế thừa class nhưng cho implement nhiều interface, phân biệt `abstract class` với `interface`, và thấy vì sao interface là trái tim của Spring.
>
> **Code thực hành:** [`bai-tap/02-oop/InterfaceVaAbstract.java`](../../bai-tap/02-oop/InterfaceVaAbstract.java)

---

## 1. Vấn đề: kế thừa đơn không đủ

[Bài 2.2](02-ke-thua-super-override.md) đã nói Java chỉ cho `extends` **một** class. Thử mô hình hóa con vịt:

- Vịt **là một** động vật → `extends DongVat` ✅
- Vịt **bay được**, **bơi được**, **chạy được** → ba khả năng khác nhau

```java
class Vit extends DongVat, CoTheBay, CoTheBoi { }    // ❌ Java cấm
```

### Vì sao Java cấm đa kế thừa class? — Diamond problem

```
              DongVat
             /       \
          Chim       Ca
             \       /
              Vit            ← object Vit chứa MẤY bản dữ liệu của
                               DongVat? Một hay hai?
                               Và keu() lấy từ Chim hay từ Ca?
```

Nhớ sơ đồ "object con chứa phần của cha" ở [bài 2.2](02-ke-thua-super-override.md). Hai đường dẫn tới `DongVat` thì phần dữ liệu đó bị nhân đôi, mọi thứ trở nên mơ hồ.

C++ cho phép đa kế thừa và phải đẻ ra `virtual inheritance` để xử lý — một trong những góc phức tạp nhất của ngôn ngữ đó. Java nhìn vào và chọn **cấm thẳng**.

Nhưng nhu cầu *"một thứ đảm nhiệm nhiều vai trò"* là có thật. Java giải quyết bằng **interface**.

---

## 2. `abstract class` — class **chưa hoàn chỉnh**

```java
abstract class DongVat {
    protected String ten;                        // ✅ có trạng thái

    DongVat(String ten) { this.ten = ten; }      // ✅ có constructor

    abstract void keu();                         // ❗ KHÔNG có thân — con phải tự viết

    public void gioiThieu() {                    // ✅ có thân — con dùng luôn
        System.out.println("Toi la " + ten);
    }
}
```

Hai loại method sống chung: loại **đã hoàn thiện** (con dùng lại) và loại **bỏ trống** (con bắt buộc điền).

### Không tạo object được

```
error: HinhHoc is abstract; cannot be instantiated
```

**Vì sao?** Vì nó **chưa hoàn chỉnh**. Nếu `new` được thì gọi `keu()` sẽ chạy cái gì? Không có thân để chạy. Java chặn tại cửa 1 thay vì để nổ lúc runtime.

### Nhưng constructor của nó **vẫn chạy**

Thực nghiệm in ra `[abstract] constructor DongVat chay` khi tạo `Vit`. Nghe mâu thuẫn, nhưng nhất quán với sơ đồ tầng ở [bài 2.2](02-ke-thua-super-override.md):

```
   object Vit trên Heap
   ┌──────────────────────────┐
   │  phần của DongVat        │  ← vẫn tồn tại, vẫn cần dựng
   │  (field ten)             │     → constructor của nó vẫn phải chạy
   ├──────────────────────────┤
   │  phần của Vit            │
   └──────────────────────────┘
```

`abstract` không có nghĩa "không tồn tại". Nó có nghĩa **"không đứng một mình được"** — luôn phải nằm trong một object con cụ thể.

> 📌 Tinh thần của abstract class: *"Đây **là một** DongVat. Tôi biết nó có tên và biết tự giới thiệu. Nhưng nó kêu thế nào thì tôi không biết — class con quyết định."*

---

## 3. `interface` — hợp đồng thuần túy

```java
interface CoTheBay {
    void bay();          // ngầm hiểu là public abstract
}
```

Không trạng thái, không constructor — chỉ là **danh sách việc phải làm**.

### Không làm đủ việc thì không được ký

```
error: Chim is not abstract and does not override abstract method haCanh() in CoTheBay
```

Thông báo gợi ý **hai** lối thoát:

1. Implement đủ `haCanh()`
2. Khai `abstract class Chim` luôn — **đẩy trách nhiệm xuống con của `Chim`**

Đó là lý do câu lỗi viết `Chim **is not abstract** and does not override...`.

### Field trong interface tự động là hằng số

```java
interface A { int SO = 10; }
A.SO = 20;
```
```
error: cannot assign a value to static final variable SO
```

Mọi field trong interface ngầm mang `public static final`. Vì interface là **hợp đồng**, không phải nơi chứa dữ liệu — một hợp đồng không có "trạng thái đang thay đổi".

---

## 4. `implements` nhiều interface — giải quyết vấn đề ban đầu

```java
class Vit extends DongVat implements CoTheBay, CoTheBoi, CoTheChay {
```

**`extends` đúng một, `implements` bao nhiêu tùy ý.**

**Vì sao interface thì được mà class thì không?** Vì diamond problem sinh ra từ việc không biết lấy **dữ liệu** của cha nào. Interface **không mang dữ liệu** — không có gì để nhân đôi, không có gì để mơ hồ.

Thực nghiệm xác nhận một object mang **nhiều danh tính** cùng lúc:

```
vit instanceof DongVat  : true
vit instanceof CoTheBay : true
vit instanceof CoTheBoi : true
```

```
   Vit ──is-a──────► DongVat        "nó LÀ cái gì"      (bản chất)
    │
    ├──can-do──────► CoTheBay       "nó LÀM ĐƯỢC gì"    (khả năng)
    ├──can-do──────► CoTheBoi
    └──can-do──────► CoTheChay
```

> 📌 **Quy ước đặt tên:** class là **danh từ** (`DongVat`, `SanPham`). Interface thường là **khả năng** — hay có đuôi `-able` trong thư viện chuẩn (`Comparable`, `Runnable`, `Serializable`) — hoặc **vai trò** (`UserRepository`, `PaymentService`).

### 🎯 Interface giúp mô hình hóa **đúng** thực tế

Thực nghiệm: `canhCut instanceof CoTheBay` cho `false`, vì `ChimCanhCut` chỉ `implements CoTheBoi`. Chim cánh cụt ngoài đời không bay được, và compiler sẽ chặn nếu ai lỡ gọi `canhCut.bay()`.

Thử làm bằng kế thừa thay vì interface:

```java
class Chim extends DongVat { public void bay() { ... } }     // "mọi con chim đều bay"
class ChimCanhCut extends Chim { }                            // ...thừa hưởng bay() 💀
```

Giờ cánh cụt **bay được** về mặt kiểu dữ liệu — đúng **bẫy kế thừa sai quan hệ** mà [bài 2.2](02-ke-thua-super-override.md) đã nhắc. Interface tránh được vì mỗi class **tự chọn ký hợp đồng nào**, không bị ép nhận khả năng nó không có.

---

## 5. Biến kiểu interface — bạn chỉ thấy phần trong hợp đồng

```java
CoTheBoi conBoi = vit;      // biến kiểu interface, object thật là Vit
conBoi.boi();               // ✅ chạy bản của Vit
conBoi.bay();               // ❌ lỗi compile!
```
```
error: cannot find symbol
  symbol:   method bay()
  location: variable conBoi of type CoTheBoi
```

```
   Thực tế trên Heap          Bạn nhìn qua biến kiểu CoTheBoi
   ┌─────────────────┐        ┌─────────────────┐
   │ Vit             │        │                 │
   │  • keu()        │        │                 │
   │  • gioiThieu()  │   ───► │                 │
   │  • bay()        │        │                 │
   │  • boi()        │        │  • boi()        │  ← chỉ thấy đúng cái này
   └─────────────────┘        └─────────────────┘
```

### 🔑 Hai khái niệm khác nhau mà người mới hay gộp

```java
   CoTheBoi conBoi = vit;
       ▲                ▲
       │                └─ RUNTIME TYPE  = Vit       (object thật trên Heap)
       └─ COMPILE-TIME TYPE = CoTheBoi               (kiểu khai báo của biến)
```

Hai câu hỏi khác nhau, do hai bên trả lời ở hai thời điểm khác nhau:

| Câu hỏi | Ai trả lời | Dựa vào | Kết quả |
|---|---|---|---|
| *"Được phép gọi method nào?"* | `javac`, lúc **compile** | **Kiểu của biến** | `conBoi.bay()` → ❌ lỗi |
| *"Chạy bản cài đặt nào?"* | JVM, lúc **chạy** | **Object thật** | `conBoi.boi()` → chạy bản của `Vit` |

Nhớ [bài 1.2](../01-java-core/02-compile-time-vs-runtime.md): lúc biên dịch, `javac` **không biết** biến sẽ trỏ vào object nào — nó chỉ có kiểu bạn khai, nên buộc phải kiểm tra theo hợp đồng. Lúc chạy thì object **tự biết nó là ai**, nên JVM gọi đúng bản của `Vit`.

> 🔗 Bảng so sánh ở [bài 2.2](02-ke-thua-super-override.md) giờ khép lại trọn vẹn:
> - **Overload** chọn bản nào → lúc **compile-time**, theo kiểu khai báo
> - **Override** chọn bản nào → lúc **runtime**, theo object thật
>
> Đó chính là định nghĩa của **đa hình** — nội dung bài 2.4.

Nghe như hạn chế, nhưng đây chính là **điểm mạnh**: nó cho phép viết code chỉ phụ thuộc vào *"cái gì đó biết bơi"* mà không cần biết đó là vịt, cá hay tàu ngầm.

---

## 6. `default` method (Java 8) — và vì sao nó phải ra đời

Trước Java 8, interface **tuyệt đối không** được có method có thân. Điều đó tạo ra vấn đề nghiêm trọng:

**Thêm một method vào interface là phá vỡ mọi class đã implement nó — trên toàn thế giới.**

Java 8 muốn thêm `stream()` vào `Collection`. Mà `Collection` được hàng triệu class implement, trong hàng trăm nghìn dự án. Thêm method mới là toàn bộ chúng **không compile nữa**.

Giải pháp: interface được có method **đã cài sẵn**, class con không bắt buộc override:

```java
interface CoTheBay {
    void bay();                                      // bắt buộc implement
    default void haCanh() {                          // có sẵn, dùng luôn cũng được
        System.out.println("Ha canh binh thuong");
    }
}
```

> 🦴 Lại là **tương thích ngược** — lần thứ tư trong lộ trình, sau dòng JavaFX ([bài 1.2](../01-java-core/02-compile-time-vs-runtime.md)), `array.length` vs `string.length()` và overload ba pha ([bài 1.7](../01-java-core/07-mang-va-method.md)).

### Diamond problem quay lại — nhưng chỉ với `default`

Giờ interface **có** thân method, nên câu hỏi "lấy bản nào" lại xuất hiện:

```java
interface X { default void chao() { } }
interface Y { default void chao() { } }
class Z implements X, Y { }
```
```
error: types X and Y are incompatible;
  class Z inherits unrelated defaults for chao() from types X and Y
```

Java **không tự chọn hộ** — nó bắt bạn quyết:

```java
class Z implements X, Y {
    @Override
    public void chao() {
        X.super.chao();        // chỉ rõ: lấy bản của X
    }
}
```

Cú pháp `X.super.chao()` chỉ tồn tại cho đúng tình huống này.

> Để ý triết lý nhất quán của Java: chỗ nào **mơ hồ** thì **bắt lập trình viên nói rõ**, thay vì đoán hộ. Giống việc bắt ép kiểu tường minh khi narrowing ([bài 1.5](../01-java-core/05-wrapper-autoboxing-ep-kieu.md)).

---

## 7. Chọn `abstract class` hay `interface`?

| | `abstract class` | `interface` |
|---|---|---|
| Quan hệ diễn tả | **is-a** (nó *là* cái gì) | **can-do** (nó *làm được* gì) |
| Một class dùng được bao nhiêu | **1** | **nhiều** |
| Field trạng thái | ✅ Có | ❌ Chỉ hằng số `public static final` |
| Constructor | ✅ Có | ❌ Không |
| Method có thân | ✅ | ✅ (từ Java 8, qua `default`) |
| Mức truy cập của method | tự do (`private`, `protected`…) | mặc định `public` |

**Quy tắc chọn:**

1. Cần **trạng thái dùng chung** (field, constructor) → `abstract class`
2. Chỉ cần định nghĩa **khả năng / hợp đồng** → `interface`
3. **Phân vân → chọn `interface`.** Lý do: class chỉ `extends` được một, nên mỗi lần dùng `abstract class` là bạn tiêu mất "suất" duy nhất đó của người dùng.

---

## 8. 🔑 Vì sao interface là trái tim của Spring

[Bài 2.2](02-ke-thua-super-override.md) nói *"favor composition over inheritance"*. Ghép composition với interface:

```java
class UserService {
    private UserRepository repo;        // ← kiểu INTERFACE, không phải class cụ thể
}
```

`UserRepository` chỉ là một hợp đồng: *"có `findById`, có `save`"*. Lúc chạy, Spring đưa vào một object cụ thể:

```
                    «interface»
   UserService ───► UserRepository
   (phụ thuộc vào       ▲        ▲
    hợp đồng)           │        │
              UserRepositoryJpa  UserRepositoryMock
               (chạy thật,        (dữ liệu giả,
                nói với Postgres)  dùng khi test)
```

`UserService` **không biết và không cần biết** nó đang nhận cái nào. Nó chỉ biết "cái gì đó thỏa hợp đồng".

Ba điều này đều đến từ đúng một thiết kế:

| Lợi ích | Vì sao có được | Học ở |
|---|---|---|
| Test được mà không cần database thật | Thay bằng bản mock | GĐ 10 |
| Đổi PostgreSQL sang MongoDB không sửa logic nghiệp vụ | Chỉ thay class implement | GĐ 7 |
| Spring tự tiêm object vào | Spring biết cần tìm thứ thỏa interface nào | GĐ 5 |

Nguyên tắc này có tên: **Dependency Inversion** — phụ thuộc vào **trừu tượng**, không phụ thuộc vào **cài đặt cụ thể**.

> ⚠️ Nếu viết `private UserRepositoryJpa repo;` (class cụ thể) thì **mất sạch cả ba**. Đây là lỗi thiết kế mà người mới học Spring hay mắc.

---

## 9. Kết quả thực nghiệm

```
--- Tao Vit ---
  [abstract] constructor DongVat chay     ← abstract class VẪN có constructor chạy
  Toi la Vit co                            ← method có thân, kế thừa dùng luôn
  Quac quac                                ← abstract method, Vit tự viết
  Vit co bay thap
  [default] Ha canh binh thuong            ← default method, Vit không override
  Vit co boi gioi

--- Tao Chim canh cut ---
  [abstract] constructor DongVat chay
  Quang quac
  Canh cut boi rat nhanh

--- Mot object, nhieu danh tinh ---
vit     instanceof DongVat  : true
vit     instanceof CoTheBay : true
vit     instanceof CoTheBoi : true
canhCut instanceof CoTheBay : false        ← cánh cụt KHÔNG bay được

--- Bien kieu interface ---
  Vit co boi gioi                          ← biến kiểu CoTheBoi, chạy bản của Vit
```

Thêm `conBoi.bay();`:

```
error: cannot find symbol
  symbol:   method bay()
  location: variable conBoi of type CoTheBoi
```

Xóa `keu()` khỏi `ChimCanhCut`:

```
error: ChimCanhCut is not abstract and does not override abstract method keu() in DongVat
```

---

## 10. Tự kiểm tra

1. Vì sao Java cấm `extends` nhiều class nhưng cho `implements` nhiều interface?
2. `abstract class` không `new` được, nhưng constructor của nó vẫn chạy. Giải thích bằng sơ đồ bộ nhớ.
3. Thông báo `Chim is not abstract and does not override...` gợi ý **hai** cách sửa. Hai cách đó là gì?
4. Vì sao field trong interface tự động là `public static final`?
5. `CoTheBoi conBoi = vit;` rồi gọi `conBoi.bay()` bị lỗi compile, dù object thật có `bay()`. Vì sao?
6. `default` method ra đời để giải quyết vấn đề gì?
7. Chim cánh cụt: dùng `interface CoTheBay` tốt hơn `class Chim extends DongVat { void bay() }` ở điểm nào?
8. Vì sao `UserService` nên khai `private UserRepository repo;` thay vì `private UserRepositoryJpa repo;`?

<details>
<summary>Đáp án</summary>

1. Vì diamond problem sinh ra từ việc không biết lấy **dữ liệu** của cha nào — object con chứa phần dữ liệu của cha, hai đường dẫn tới cùng một tổ tiên thì phần đó bị nhân đôi. Interface **không mang dữ liệu** nên không có vấn đề này.
2. Phần dữ liệu của `DongVat` (field `ten`) **vẫn tồn tại** bên trong object `Vit`, nên vẫn cần được dựng. `abstract` nghĩa là "không đứng một mình được", không phải "không tồn tại".
3. (a) Implement đủ method còn thiếu; (b) khai chính class đó là `abstract` để đẩy trách nhiệm xuống lớp con tiếp theo.
4. Vì interface là **hợp đồng**, không phải nơi chứa dữ liệu. Hợp đồng không có "trạng thái đang thay đổi" — chỉ có hằng số.
5. Vì `javac` kiểm tra theo **compile-time type** (kiểu khai báo của biến = `CoTheBoi`), và lúc biên dịch nó **không biết** biến sẽ trỏ vào object nào. Hợp đồng `CoTheBoi` không có `bay()`.
6. Trước Java 8, thêm method vào interface sẽ **phá vỡ mọi class đã implement nó** trên toàn thế giới. Java 8 cần thêm `stream()` vào `Collection` nên phải có cơ chế thêm method mà không bắt buộc override.
7. Vì mỗi class **tự chọn ký hợp đồng nào**. Dùng kế thừa thì `ChimCanhCut` bị **ép thừa hưởng** `bay()` dù ngoài đời nó không bay được — mô hình sai, và compiler không ngăn được ai gọi `canhCut.bay()`.
8. Vì phụ thuộc vào **interface** cho phép thay cài đặt mà không sửa `UserService`: dùng mock khi test, đổi database, và để Spring tiêm object vào. Khai class cụ thể là mất cả ba (**Dependency Inversion**).

</details>

---

⬅️ **Bài trước:** [2.2 — Kế thừa, `super` và `@Override`](02-ke-thua-super-override.md)
➡️ **Bài tiếp:** 2.4 — Đa hình (polymorphism)
