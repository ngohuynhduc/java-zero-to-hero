# Bài 3.2 — Generics

> **Mục tiêu:** Hiểu generic giải quyết vấn đề gì, vì sao nó **biến mất** sau khi biên dịch (type erasure), và vì sao `List<Cho>` **không phải** là `List<DongVat>`.
>
> **Code thực hành:** [`bai-tap/03-collections/Generics.java`](../../bai-tap/03-collections/Generics.java)

---

## 1. Thế giới trước khi có generic

Generic xuất hiện ở **Java 5** (2004). Trước đó, `List` chứa mọi thứ dưới dạng `Object`:

```java
List ds = new ArrayList();      // không có <...>
ds.add("An");
ds.add("Binh");
ds.add(42);                     // ← lỡ tay bỏ một Integer vào

String ten = (String) ds.get(2);   // đọc ra phải TỰ ÉP KIỂU
```

Hai vấn đề:

1. **Ép kiểu thủ công ở khắp nơi** — mỗi lần lấy ra là một lần `(String)`.
2. **Không ai kiểm tra lúc bỏ vào** — `add()` nhận `Object`, mà cái gì cũng là `Object` ([bài 2.2](../02-oop/02-ke-thua-super-override.md)).

Code kiểu này hôm nay vẫn compile được, nhưng `javac` nhắc:

```
Note: M1.java uses unchecked or unsafe operations.
```

Kiểu `List` trần không có `<...>` gọi là **raw type** — một hóa thạch nữa của tương thích ngược.

### 🔬 Chỗ gây lỗi và chỗ phát hiện lỗi cách xa nhau

```
add xong 3 phan tu          ← add(42) chạy êm ru — đây mới là chỗ SAI
  AN
  BINH
  Loi: ClassCastException   ← lộ ra ở dòng ép (String) — chỗ chẳng có gì sai
```

Trong ví dụ, hai dòng cách nhau vài dòng. Trong dự án thật, chỗ bỏ sai vào có thể là một service khác, chạy từ ba ngày trước. Stack trace chỉ vào chỗ **đọc**.

---

## 2. Generic — nói cho compiler biết danh sách chứa gì

```java
List<String> ds = new ArrayList<>();
ds.add("An");
ds.add(42);                    // ❌ lỗi compile — chặn ngay ở cửa 1
String ten = ds.get(0);        // ✅ không cần ép kiểu
```

| | Raw type (trước Java 5) | Generic |
|---|---|---|
| Bỏ sai kiểu vào | Không ai cản | ❌ Lỗi compile |
| Lấy ra | Phải ép kiểu tay | Tự động |
| Lỗi lộ ra ở | Cửa 2 — runtime, xa chỗ gây ra | **Cửa 1** — compile, đúng chỗ gây ra |

Đúng cái lợi của static typing ở [bài 1.3](../01-java-core/03-bien-va-kieu-du-lieu.md): đẩy lỗi từ runtime lên compile-time.

Nếu đã dùng TypeScript thì cú pháp gần như y hệt: `Array<string>`, `function dau<T>(arr: T[]): T`.

---

## 3. Type erasure — generic **biến mất** sau khi biên dịch

Soi bytecode của:

```java
static String layDau(List<String> ds) { return ds.get(0); }
```

```
invokeinterface  List.get:(I)Ljava/lang/Object;     ← get trả về OBJECT
checkcast        class java/lang/String             ← compiler TỰ CHÈN ép kiểu
...
invokestatic     layDau:(Ljava/util/List;)...       ← tham số chỉ còn là List trần
```

```
   Bạn viết:              Compiler làm:                    Bytecode thực sự:
   ───────────            ──────────────                   ─────────────────
   List<String>     ──►   ① kiểm tra mọi add/get     ──►   List   (xóa <String>)
   ds.get(0)              ② xóa sạch <String>             (String) ds.get(0)
                          ③ chèn checkcast vào chỗ đọc
```

**Generic chỉ tồn tại lúc compile.** `javac` dùng nó để kiểm tra, rồi **xóa đi** (*erase*), và tự chèn đúng phép ép kiểu `(String)` mà lập trình viên Java 1.4 từng gõ tay.

Bằng chứng lúc chạy:

```java
List<String>  a = new ArrayList<>();
List<Integer> b = new ArrayList<>();
a.getClass() == b.getClass()      // true — cả hai đều chỉ là java.util.ArrayList
```

> 🔗 **Lần thứ hai** gặp syntactic sugar kiểu "compiler viết hộ code":
> - [Bài 1.5](../01-java-core/05-wrapper-autoboxing-ep-kieu.md): autoboxing → compiler chèn `.intValue()`
> - Bài này: generic → compiler chèn `checkcast`
>
> Vì sao thiết kế vậy? **Tương thích ngược** — lần thứ năm trong lộ trình. Generic được thêm vào mà không phải sửa JVM, và mọi thư viện viết bằng raw type vẫn chạy chung với code generic.

### Giống hệt TypeScript

[Bài 1.3](../01-java-core/03-bien-va-kieu-du-lieu.md) nói: *"TypeScript kiểm tra kiểu lúc build rồi xóa sạch khi ra JS; Java giữ thông tin kiểu trong bytecode."* Generic là **ngoại lệ** của câu đó — ở chỗ này Java cư xử **y hệt TypeScript**.

### Hệ quả: hai method mà Java coi là một

```java
static void xuLy(List<String> ds)  { }
static void xuLy(List<Integer> ds) { }
```
```
error: name clash: xuLy(List<Integer>) and xuLy(List<String>) have the same erasure
```

Theo luật overload ở [bài 1.7](../01-java-core/07-mang-va-method.md), tham số khác kiểu là hai method khác nhau. Nhưng sau khi xóa `<...>`, cả hai đều thành `xuLy(List)` — trùng chữ ký.

### 🔗 Lời giải cho câu hỏi từ bài 1.5

[Bài 1.5](../01-java-core/05-wrapper-autoboxing-ep-kieu.md) nói `List<int>` không compile được *"vì type erasure"*. Giờ thì rõ: sau khi xóa, mọi `T` thành `Object` — mà `int` **không phải** `Object`.

---

## 4. Tự viết class và method generic

### Class generic

```java
class Hop<T> {
    private T giaTri;
    Hop(T giaTri) { this.giaTri = giaTri; }
    T lay() { return giaTri; }
}

Hop<String>  h1 = new Hop<>("chu");
Hop<Integer> h2 = new Hop<>(42);
String s = h1.lay();        // không cần ép kiểu
```

`T` là **biến kiểu** — chỗ trống được điền khi ai đó dùng class. Dấu `<>` trống ở `new Hop<>(...)` gọi là **diamond** — compiler tự suy ra kiểu từ vế trái.

### Method generic

```java
static <T> T phanTuDau(List<T> ds) {
    return ds.get(0);
}
// ▲
// └── khai báo biến kiểu T, đặt TRƯỚC kiểu trả về
```

Gọi với `List<String>` thì trả `String`, với `List<Integer>` thì trả `Integer` — compiler suy ra từ đối số.

| Tên | Thường dùng cho | Ví dụ trong JDK |
|---|---|---|
| `T` | Type — kiểu bất kỳ | `Optional<T>` |
| `E` | Element — phần tử collection | `List<E>` |
| `K`, `V` | Key, Value | `Map<K, V>` |

---

## 5. 🧩 Vì sao `List<Cho>` **không phải** là `List<DongVat>`?

```java
List<Cho> dsCho = new ArrayList<>();
List<DongVat> dsDV = dsCho;
```
```
error: incompatible types: List<Cho> cannot be converted to List<DongVat>
```

### Thí nghiệm tư duy: nếu Java cho phép thì sao?

```java
dsDV.add(new Meo());                  // dsDV kiểu List<DongVat> — Meo là DongVat, hợp lệ
Cho cho = dsCho.get(0);               // dsCho và dsDV là CÙNG MỘT object...
```

```
   STACK                        HEAP
   dsCho │ ●──────┐
                  ├──────►  [ 🐱 Meo ]     ← một con mèo nằm trong "danh sách chó"
   dsDV  │ ●──────┘
```

Hai biến cùng trỏ một object ([bài 1.7](../01-java-core/07-mang-va-method.md)). Bỏ mèo vào qua cửa `dsDV`, lấy ra qua cửa `dsCho` sẽ được một con mèo trong biến kiểu `Cho`.

### Mảng có từ Java 1.0 — và nó chọn **khác**

```java
Cho[] mangCho = new Cho[2];
DongVat[] mangDV = mangCho;           // ✅ compile được
mangDV[0] = new Meo();                // 💥 ArrayStoreException lúc chạy
```

### 🔑 Vì sao hai quyết định khác nhau?

| | `List<Cho>` → `List<DongVat>` | `Cho[]` → `DongVat[]` |
|---|---|---|
| Lúc chạy, JVM có biết kiểu phần tử không? | ❌ **Không** — `<Cho>` đã bị xóa | ✅ **Có** — mảng nhớ kiểu của nó |
| Nếu để lọt, ai bắt lỗi được? | **Không ai cả** — con mèo nằm im, tới lúc đọc mới nổ ở chỗ xa tít | **JVM** — kiểm tra ngay lúc bỏ vào |
| Nên compiler quyết định | Chặn từ cửa 1 | Cho qua, để JVM kiểm tra ở cửa 2 |
| Kết quả | ❌ Lỗi compile | 💥 `ArrayStoreException` |

Đây là hệ quả trực tiếp của type erasure. Vì generic **sẽ bị xóa**, không còn ai ở runtime canh gác — compiler buộc phải chặn từ đầu, nếu không lỗi sẽ lọt hẳn ra ngoài và nổ ở chỗ không liên quan (đúng kịch bản mục 1).

Mảng mang theo kiểu phần tử lúc chạy (chữ `I` trong `[I@...` ở [bài 1.7](../01-java-core/07-mang-va-method.md)), nên JVM kiểm tra mỗi lần ghi và ném lỗi **ngay tại dòng bỏ mèo vào**.

> 📌 Hai thiết kế, hai cửa kiểm soát. Generic chọn cách an toàn hơn: lỗi bị bắt **trước khi chạy**. Đó là lý do Java hiện đại ưu tiên `List<T>` hơn mảng ở hầu hết mọi chỗ.

### So với TypeScript

```typescript
const dsDV: DongVat[] = dsCho;   // TS cho phép
dsDV.push(new Meo());            // TS cũng cho phép — compile sạch, không cảnh báo
```

TypeScript cố ý chọn **tiện lợi hơn an toàn** ở chỗ này. Java generic thì không.

---

## 6. Wildcard: `? extends` và `? super`

### `? extends DongVat` — danh sách của **một loại con nào đó** của DongVat

```java
static void inTen(List<? extends DongVat> ds) {
    for (DongVat d : ds) System.out.println(d.ten);     // ✅ ĐỌC an toàn
    ds.add(new Cho("Lau"));                              // ❌
}
```
```
error: incompatible types: Cho cannot be converted to CAP#1
  where CAP#1 is a fresh type-variable:
    CAP#1 extends DongVat from capture of ? extends DongVat
```

Bên trong method, `ds` có thể là `List<Cho>` **hoặc** `List<Meo>`. Thêm một con chó vào `List<Meo>` là sai — nên compiler cấm thêm mọi thứ.

### `? super Cho` — danh sách chứa được **Cho hoặc tổ tiên của Cho**

```java
static void themCho(List<? super Cho> ds) {
    ds.add(new Cho("Milu"));             // ✅ GHI an toàn
    Object docRa = ds.get(0);            // ⚠️ đọc ra CHỈ được Object
}
```

`List<? super Cho>` có thể là:

```
       ├─ List<Cho>       → phần tử là Cho
       ├─ List<DongVat>   → phần tử có thể là Cho, Meo, ...
       └─ List<Object>    → phần tử có thể là Cho, "abc", 42, ...
```

Nếu người gọi truyền `List<Object>` **đã có sẵn** `"abc"`, thì `ds.get(0)` ra `"abc"` — không phải `Cho`. Method chỉ biết chắc **chính nó** vừa thêm một con chó, không biết danh sách đã chứa gì từ trước. Thử gán vào `Cho`:

```
error: incompatible types: CAP#1 cannot be converted to Cho
  where CAP#1 is a fresh type-variable:
    CAP#1 extends Object super: Cho from capture of ? super Cho
```

*"Kiểu chưa biết, **cận dưới** là `Cho`, **cận trên** là `Object`"* — compiler chỉ hứa được phần cận trên.

### PECS — **P**roducer **E**xtends, **C**onsumer **S**uper

| | `? extends DongVat` | `? super Cho` |
|---|---|---|
| Đọc ra | ✅ Chắc chắn là `DongVat` | ⚠️ Chỉ biết là `Object` |
| Thêm vào | ❌ Không thêm được gì | ✅ Thêm `Cho` an toàn |
| Đóng vai | **Producer** — chỉ lấy ra | **Consumer** — chỉ đưa vào |

Trong JDK: `Collections.copy(List<? super T> dich, List<? extends T> nguon)` — nguồn là producer, đích là consumer.

---

## 7. Generic trong Spring

```java
interface UserRepository extends JpaRepository<User, Long> { }   // GĐ 7
ResponseEntity<UserDto> layUser(...)                              // GĐ 6
Optional<User> timTheoEmail(String email)                         // GĐ 3–7
```

Chỉ một dòng `JpaRepository<User, Long>` mà Spring sinh sẵn `findById(Long)` trả `Optional<User>`, `save(User)`, `findAll()` trả `List<User>` — đúng kiểu, không ép tay.

---

## 8. Kết quả thực nghiệm

```
--- 1. Raw type (kieu Java 1.4) ---
add xong 3 phan tu
  AN
  BINH
  Loi: ClassCastException

--- 2. Type erasure ---
cung class? true

--- 3. Class va method generic ---
chu | 42
An
7

--- 5. Mang thi sao? ---
gan mang: compile OK
  Loi: ArrayStoreException

--- 6. Wildcard ---
inTen(dsCho): Vang Den
inTen(dsMeo): Mun
  doc ra: Milu
  doc ra: Milu
```

---

## 9. Tự kiểm tra

1. Trước Java 5, vì sao lỗi bỏ nhầm kiểu vào `List` lại lộ ra ở chỗ **đọc** chứ không phải chỗ **ghi**?
2. Type erasure là gì? Bằng chứng nào cho thấy `<String>` không còn lúc chạy?
3. Vì sao không overload được `xuLy(List<String>)` và `xuLy(List<Integer>)`?
4. Vì sao `List<int>` không hợp lệ?
5. Nếu Java cho gán `List<Cho>` vào `List<DongVat>` thì chuyện gì có thể xảy ra?
6. Mảng cho gán `Cho[]` vào `DongVat[]` còn generic thì không. Vì sao hai thiết kế khác nhau dẫn tới hai cửa kiểm soát khác nhau?
7. Vì sao không `add()` được vào `List<? extends DongVat>`?
8. Vì sao đọc từ `List<? super Cho>` chỉ được `Object`?

<details>
<summary>Đáp án</summary>

1. Vì `add()` của raw `List` nhận `Object` nên không ai kiểm tra lúc bỏ vào. Lỗi chỉ lộ ra khi đọc ra và ép kiểu `(String)` — chỗ đó có thể cách rất xa chỗ gây lỗi.
2. Compiler dùng `<String>` để kiểm tra rồi **xóa đi**, thay bằng `Object` và tự chèn `checkcast` ở chỗ đọc. Bằng chứng: `List<String>` và `List<Integer>` có `getClass()` giống hệt nhau; bytecode cho thấy `List.get` trả `Object` kèm `checkcast String`.
3. Sau khi xóa `<...>`, cả hai đều thành `xuLy(List)` — trùng chữ ký (`name clash ... have the same erasure`).
4. Sau erasure, `T` thành `Object`, mà `int` là primitive, không phải `Object`. Phải dùng `Integer`.
5. Hai biến trỏ cùng một object: bỏ `Meo` vào qua `List<DongVat>`, rồi lấy ra qua `List<Cho>` sẽ được một con mèo trong biến kiểu `Cho`.
6. Generic bị xóa lúc chạy nên **không ai ở runtime kiểm tra được** — compiler buộc phải chặn từ cửa 1. Mảng **nhớ kiểu phần tử** lúc chạy nên JVM tự kiểm tra mỗi lần ghi — compiler cho qua, lỗi là `ArrayStoreException` ở cửa 2.
7. Vì `ds` có thể là `List<Cho>` hoặc `List<Meo>` hoặc loại con khác — compiler không biết loại nào, nên không có kiểu nào an toàn để thêm vào cho mọi khả năng.
8. Vì `ds` có thể là `List<Cho>`, `List<DongVat>` hoặc `List<Object>` — danh sách có thể đã chứa phần tử bất kỳ từ trước. Kiểu duy nhất chắc chắn là `Object`.

</details>

---

⬅️ **Bài trước:** [3.1 — Collections: chọn đúng cấu trúc dữ liệu](01-chon-collection.md)
➡️ **Bài tiếp:** 3.3 — Exception
