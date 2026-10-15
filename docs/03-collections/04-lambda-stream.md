# Bài 3.4 — Lambda và Stream API

> **Mục tiêu:** Hiểu lambda Java thực chất là gì (object cài đặt một functional interface, **không** phải lớp ẩn danh), vì sao nó chỉ được dùng biến *effectively final*, và vì sao Stream **lười** — xử lý từng phần tử đi hết dây chuyền thay vì từng bước cho cả mảng như JS.
>
> **Code thực hành:** [`bai-tap/03-collections/LambdaStream.java`](../../bai-tap/03-collections/LambdaStream.java)

---

## 1. Vấn đề: muốn truyền một *hành vi* vào method

JS: hàm là **giá trị hạng nhất** — `arr.sort((a, b) => a - b)` là hiển nhiên.

Java: từ [bài 2.1](../02-oop/01-class-va-object.md), **mọi code phải nằm trong class**. Không có giá trị kiểu "hàm". Trước Java 8 (2014), muốn truyền hành vi phải tạo một object qua **lớp ẩn danh**:

```java
ds.sort(new Comparator<String>() {          // class không tên, implements Comparator
    @Override
    public int compare(String a, String b) {
        return a.compareTo(b);              // ← chỉ dòng này là ý nghĩa thật
    }
});
```

### Java 8 vá bằng cách nào?

Thêm "kiểu hàm" thật như TS `(a: string) => number` nghĩa là sửa hệ thống kiểu, sửa JVM, và hàng nghìn API cũ nhận `Comparator`, `Runnable`... phải viết lại. Java chọn mẹo khác:

> **Lambda không phải kiểu mới. Lambda là cách viết gọn để tạo object cài đặt một interface có đúng một method trừu tượng (functional interface).**

Mọi API cũ nhận `Comparator` **tự động** nhận lambda, không sửa dòng nào. Lần thứ ba ta thấy tương thích ngược định hình ngôn ngữ, sau autoboxing ([bài 1.5](../01-java-core/05-wrapper-autoboxing-ep-kieu.md)) và raw type ([bài 3.2](02-generics.md)).

```java
ds.sort((a, b) -> b.compareTo(a));
//       └─┬──┘    └──────┬─────┘
//   tham số của      thân của method compare()
//   method compare()
```

Đổi `a.compareTo(b)` thành `b.compareTo(a)` là đảo dấu kết quả → sắp xếp ngược (Z→A).

---

## 2. Target typing — lambda mượn kiểu từ chỗ nhận

TS: `(a: string) => a.length` tự có kiểu. Java: `s -> s.length()` **đứng một mình vô nghĩa** — compiler nhìn **chỗ nhận** để biết là interface nào, rồi khớp vào method duy nhất của nó.

```
   Function<String, Integer> doDai = s -> s.length();
   └──────────┬────────────┘        └──────┬───────┘
   interface có 1 method:            khớp vào: Integer apply(String s)
   R apply(T t)                      → s là String, kết quả phải là Integer
```

Chỗ nhận không phải functional interface → lambda không có chỗ bám:

```
O.java:3: error: incompatible types: Object is not a functional interface
        Object o = () -> {};
                   ^
```

### Bộ functional interface có sẵn (`java.util.function`)

| Interface | Method | Tương đương TS |
|---|---|---|
| `Function<T, R>` | `R apply(T t)` | `(t: T) => R` |
| `Predicate<T>` | `boolean test(T t)` | `(t: T) => boolean` |
| `Consumer<T>` | `void accept(T t)` | `(t: T) => void` |
| `Supplier<T>` | `T get()` | `() => T` |
| `BiFunction<T, U, R>` | `R apply(T t, U u)` | `(t: T, u: U) => R` |

Bản primitive (`IntPredicate`, `ToIntFunction<T>`, `IntStream`...) tồn tại vì generic không chứa được `int` ([bài 3.2](02-generics.md)) → `Predicate<Integer>` phải **boxing** từng số ([bài 1.5](../01-java-core/05-wrapper-autoboxing-ep-kieu.md)).

### Method reference

| Dạng | Ví dụ | Tương đương |
|---|---|---|
| Method static | `Integer::parseInt` | `s -> Integer.parseInt(s)` |
| Method của chính tham số | `String::toUpperCase`, `NhanVien::phong` | `s -> s.toUpperCase()` |
| Method của object cụ thể | `System.out::println` | `x -> System.out.println(x)` |
| Constructor | `ArrayList::new` | `() -> new ArrayList<>()` |

---

## 3. 🔬 Lambda bị biên dịch thành gì?

Code thực hành có **7 lambda + 1 lớp ẩn danh**. Sau `javac`:

```
LambdaStream$1.class     ← lớp ẩn danh: MỘT FILE RIÊNG
LambdaStream.class
NhanVien.class           ← record
```

Không có file nào cho 7 lambda. Vậy chúng ở đâu? `javap -p LambdaStream`:

```
  private static int lambda$main$0(java.lang.String, java.lang.String);   ← Comparator
  private static boolean lambda$main$1(java.lang.Integer);                ← laSoChan
  private static java.lang.Integer lambda$main$2(java.lang.String);       ← doDai
  private static java.lang.Integer lambda$main$3(int, java.lang.Integer); ← nhan (!)
  private static boolean lambda$main$4(java.lang.String);                 ← filter
  private static java.lang.String lambda$main$5(java.lang.String);        ← map
  private static boolean lambda$main$6(NhanVien);                         ← filter nv
```

- **Thân lambda → một method `private static`** ngay trong class chứa nó.
- **Method reference** (`String::toUpperCase`, `NhanVien::luong`...) **không sinh method nào** — nó trỏ thẳng vào method có sẵn.
- Chỗ tạo lambda là lệnh **`invokedynamic`** (cùng họ với `makeConcatWithConstants` ghép chuỗi đã gặp trong `javap` ở [bài 3.3](03-exception.md)). Lần đầu chạy tới, JVM nhờ `LambdaMetafactory` **sinh class lúc chạy**:

```
V$$Lambda$18/0x0000000801007000      ← nhan.getClass().getName()  (JDK 19)
true true                            ← isSynthetic(), isHidden()
[interface java.util.function.Function]
```

Class "ẩn" (hidden class), không có file `.class`, cài đặt đúng interface `Function`.

| | Lớp ẩn danh | Lambda |
|---|---|---|
| File `.class` | Một file / lớp ẩn danh (`X$1.class`) | Không có |
| Class tạo lúc | Compile | **Chạy** (lần đầu gặp `invokedynamic`) |
| `this` bên trong | Chính object lớp ẩn danh | Object **bao ngoài** (như arrow function JS) |
| Có state riêng (field) | Có | Không |

Điểm `this` đáng nhớ với dân JS: lambda Java giống **arrow function** — không có `this` của riêng mình.

---

## 4. Lambda bắt biến — effectively final

JS: closure bắt **chính cái biến** (binding), sửa qua lại thoải mái:

```js
let dem = 0;
const tang = () => dem++;   // hợp lệ
```

Java: biến cục bộ dùng trong lambda phải **`final` hoặc effectively final** (không bị gán lại lần nào). Bỏ comment `heSo = 20;`:

```
LambdaStream.java:46: error: local variables referenced from a lambda expression must be final or effectively final
        Function<Integer, Integer> nhan = x -> x * heSo;
                                                   ^
```

Lỗi chỉ vào **chỗ dùng trong lambda**, không phải dòng `heSo = 20;` — dù dòng gán nằm **sau** lambda. "Effectively final" là tính chất của biến trên **toàn method**: chỉ một lần gán lại ở bất cứ đâu là biến mất tư cách.

### Vì sao cấm? Bằng chứng bytecode

```
     218: bipush        10
     220: istore        6         ← heSo = 10 (biến cục bộ số 6)
     222: iload         6         ← đọc GIÁ TRỊ 10 ra
     224: invokedynamic apply:(I)Ljava/util/function/Function;
                                  ← truyền 10 vào lúc TẠO lambda
```

Và `lambda$main$3(int, java.lang.Integer)` nhận **thêm một tham số `int`** — chính là bản sao của `heSo`. Lambda **không** giữ biến; nó giữ **một bản sao giá trị** chụp lúc tạo.

```
   STACK (frame của main)               HEAP
   ┌────────────────────┐           ┌──────────────────────────┐
   │ heSo = 10          │  ──copy─► │ object lambda            │
   └────────────────────┘           │   bản sao: 10            │
     frame có thể bị gỡ             │   (sống tiếp, có thể chạy │
     trước khi lambda chạy          │    ở thread khác)         │
                                    └──────────────────────────┘
```

Vì sao phải sao chép? Biến cục bộ sống trong **frame** ([bài 3.3](03-exception.md)), frame bị gỡ khi method kết thúc — còn lambda là object trên Heap, có thể chạy **sau đó** hoặc ở **thread khác**. Đã sao chép mà còn cho sửa biến gốc thì bản gốc và bản sao **lệch nhau âm thầm**. Java chặn luôn ở cửa 1.

JS không gặp vấn đề vì engine tự đưa biến bị bắt lên heap, và JS chạy một thread.

**Cắn ở đâu:** lách luật bằng `int[] dem = {0}; ... dem[0]++` (mảng là object, biến `dem` vẫn effectively final) → compile qua, nhưng với `parallelStream()` nhiều thread cùng `dem[0]++` → **đếm thiếu ngẫu nhiên** (race condition).

---

## 5. Stream — dây chuyền lười

```
  NGUỒN               TRUNG GIAN (lazy)               KẾT THÚC (terminal)
 ┌──────────┐      ┌────────┐     ┌────────┐       ┌──────────────┐
 │ Stream.of│ ───► │ filter │ ──► │  map   │ ────► │ findFirst    │ ──► kết quả
 └──────────┘      └────────┘     └────────┘       │ toList/count │
                   chỉ GHI LẠI việc cần làm         └──────────────┘
                                                    lúc này mới CHẠY
```

| | JS `Array` | Java `Stream` |
|---|---|---|
| Khi nào chạy | Ngay (eager) | Khi gặp thao tác kết thúc |
| Thứ tự xử lý | **Ngang**: cả mảng qua `filter`, rồi cả mảng qua `map` | **Dọc**: từng phần tử đi hết dây chuyền |
| Bộ nhớ trung gian | Mỗi bước một mảng mới | Không có |
| Dừng sớm | Không | Có (`findFirst`, `limit`, `anyMatch`) |
| Dùng lại | Thoải mái | **Một lần** |

### 🔬 Xử lý dọc — bằng chứng

Với `findFirst()`:

```
da tao stream xong          ← chưa có dòng filter/map nào trước đây
  filter an                 ← "an" trượt, bị loại
  filter binh               ← "binh" qua filter...
  map binh                  ← ...đi tiếp ngay sang map
findFirst = BINH            ← có kết quả → DỪNG. "chi", "dung" không bao giờ được chạm tới
```

Cùng dây chuyền nhưng kết thúc bằng `toList()` (cần hết phần tử):

```
  filter an
  filter binh
  map binh
  filter chi
  map chi
  filter dung
  map dung
[BINH, CHI, DUNG]
```

`filter` và `map` **xen kẽ** — không bao giờ "filter cả 4 rồi mới map". Xử lý dọc chính là thứ cho phép dừng sớm: theo kiểu ngang của JS, muốn biết phần tử đầu tiên qua cả hai bước thì phải filter hết mảng trước đã.

### Stream chỉ dùng một lần

```
Loi: stream has already been operated upon or closed
```

Stream giống `Iterator`: đi qua rồi là hết, không có dữ liệu nào để "đi lại". Muốn chạy lại → tạo stream mới từ nguồn (`ds.stream()` lần nữa).

---

## 6. Collectors và `toList()`

### `groupingBy` dùng `HashMap`

```
so nguoi: {HR=2, IT=2}
```

Danh sách có `IT` đứng trước (An), nhưng kết quả in `HR` trước — vì `groupingBy` mặc định tạo **`HashMap`** (đã kiểm: `m.getClass()` → `java.util.HashMap`), thứ tự theo bucket của hash, không theo thứ tự chèn ([bài 3.1](01-chon-collection.md)). Cần thứ tự → truyền "nhà máy" Map:

```java
Collectors.groupingBy(NhanVien::phong, TreeMap::new, Collectors.counting())   // sắp theo khóa
Collectors.groupingBy(NhanVien::phong, LinkedHashMap::new, Collectors.counting()) // theo thứ tự gặp
```

`counting()` trả `Long` vì `Stream.count()` trả `long`: một stream có thể dài hơn `Integer.MAX_VALUE` (~2,1 tỉ), và `int` sẽ tràn số âm thầm ([bài 1.3](../01-java-core/03-bien-va-kieu-du-lieu.md)).

### `Stream.toList()` vs `Collectors.toList()`

```
[An, Binh, Chi, Dung]
Loi: UnsupportedOperationException                         ← stream.toList() (Java 16+)
Collectors.toList(): [a, b, c] java.util.ArrayList         ← collect(Collectors.toList())
```

`stream.toList()` trả list **bất biến** (`java.util.ImmutableCollections$ListN` — cùng họ với `List.of`, [bài 3.1](01-chon-collection.md)). `Collectors.toList()` cũ thực tế trả `ArrayList` sửa được (dù tài liệu không cam kết).

**Cắn ở production:** IDE hay gợi ý "đổi `.collect(Collectors.toList())` thành `.toList()` cho gọn". Đổi xong, chỗ nào phía sau còn `add()` vào list đó → compile vẫn OK, nổ **lúc chạy**.

---

## 7. Lambda và checked exception

`R apply(T t)` **không có `throws`**. Thân lambda là thân `apply()`, nên checked exception bên trong không có chỗ khai báo ([bài 3.3](03-exception.md)):

```
LambdaStream.java:49: error: unreported exception IOException; must be caught or declared to be thrown
        Function<String, String> docFile = duongDan -> Files.readString(Path.of(duongDan));
                                                                       ^
```

Cách chuẩn: bắt bên trong lambda và **bọc thành unchecked** (giữ `cause`):

```java
Function<String, String> docFile = duongDan -> {
    try {
        return Files.readString(Path.of(duongDan));
    } catch (IOException e) {
        throw new UncheckedIOException(e);
    }
};
```

```
Loi: UncheckedIOException <- NoSuchFileException
```

`java.io.UncheckedIOException` có sẵn trong JDK cho đúng mục đích này. Đây là lý do thực tế lớn nhất khiến thế giới Java hiện đại nghiêng về unchecked.

---

## 8. Kết quả thực nghiệm

```
--- 1. Lop an danh vs lambda ---
lop an danh: [An, Binh, Chi]
lambda:      [Chi, Binh, An]

--- 2. Functional interface ---
true false
4
ABC
0

--- 3. Lambda bat bien ---
50

--- 4. Stream luoi (lazy) ---
da tao stream xong
  filter an
  filter binh
  map binh
findFirst = BINH

--- 5. Dung lai stream ---
Loi: stream has already been operated upon or closed

--- 6. Collectors ---
so nguoi: {HR=2, IT=2}
tong luong IT = 4500

--- 7. toList() tra ve gi? ---
[An, Binh, Chi, Dung]
Loi: UnsupportedOperationException
```

---

## 9. Cạm bẫy production

| Cạm bẫy | Hệ quả | Cách đúng |
|---|---|---|
| `parallelStream()` trong web server | Mọi parallel stream của cả JVM dùng chung `ForkJoinPool.commonPool` → request nặng làm chậm request khác | Mặc định dùng stream tuần tự |
| Side effect trong `map`/`filter` (`dem[0]++`, sửa list ngoài) | Sai khi song song, khó test | Dùng `count()`, `collect()` |
| Đổi `Collectors.toList()` → `.toList()` | `add()` phía sau nổ `UnsupportedOperationException` lúc chạy | Kiểm tra chỗ dùng trước khi đổi |
| Tin thứ tự của `groupingBy` | Thứ tự hiển thị đổi khi dữ liệu đổi | `TreeMap::new` / `LinkedHashMap::new` |
| `Stream<Integer>` cho tính toán số lớn | Boxing hàng loạt | `mapToInt` / `IntStream` |
| Chuỗi stream quá dài | Stack trace đầy `lambda$main$3`, khó debug | Tách bước, hoặc quay về vòng `for` |

---

## 10. Tự kiểm tra

1. Vì sao Java 8 chọn làm lambda dựa trên interface thay vì thêm "kiểu hàm" mới?
2. `Object o = () -> {};` lỗi gì, vì sao?
3. Code có 7 lambda và 1 lớp ẩn danh — có bao nhiêu file `.class` (không tính class chính và record)? Thân lambda nằm ở đâu?
4. Bằng chứng bytecode nào cho thấy lambda giữ **bản sao giá trị** chứ không giữ biến? Vì sao Java phải làm vậy?
5. Với `filter(...).map(...).findFirst()` trên `["an", "binh", "chi", "dung"]`, thứ tự in là gì? Vì sao `"chi"` không được xử lý?
6. `stream.toList()` và `collect(Collectors.toList())` khác nhau thế nào? Đổi qua lại có thể gây lỗi gì?

<details>
<summary>Đáp án</summary>

1. Để mọi API cũ nhận `Comparator`, `Runnable`... tự động nhận lambda mà không sửa gì, và không phải đổi hệ thống kiểu / JVM. Lambda chỉ là cách viết gọn để tạo object cài đặt functional interface.
2. `incompatible types: Object is not a functional interface`. Lambda không tự có kiểu — compiler lấy kiểu từ chỗ nhận (target typing); `Object` không có method trừu tượng nào để khớp.
3. Một file (`LambdaStream$1.class`) cho lớp ẩn danh; lambda không có file. Thân lambda thành method `private static lambda$main$N` trong class chứa nó; class cài đặt interface được `LambdaMetafactory` sinh **lúc chạy** qua `invokedynamic` (hidden class).
4. `iload 6` đọc giá trị `heSo` rồi truyền vào `invokedynamic` lúc tạo lambda; `lambda$main$3(int, Integer)` nhận thêm tham số `int`. Phải sao chép vì biến cục bộ sống trong frame có thể bị gỡ trước khi lambda (object trên Heap, có thể ở thread khác) chạy. Đã sao chép thì cho sửa sẽ lệch âm thầm → cấm.
5. `filter an`, `filter binh`, `map binh`, rồi dừng. Stream xử lý **dọc** (từng phần tử đi hết dây chuyền) và `findFirst` là thao tác dừng sớm: có kết quả là thôi.
6. `toList()` (Java 16+) trả list bất biến; `Collectors.toList()` thực tế trả `ArrayList` sửa được. Đổi sang `toList()` mà phía sau còn `add()` → `UnsupportedOperationException` lúc chạy, compiler không báo.

</details>

---

⬅️ **Bài trước:** [3.3 — Exception](03-exception.md)
➡️ **Bài tiếp:** 4.1 — Maven
