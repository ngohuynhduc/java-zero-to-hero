# Bài 2.1 — Class và Object

> **Mục tiêu:** Hiểu vì sao OOP tồn tại (từ vấn đề, không từ định nghĩa), phân biệt class với object, nắm `this`, constructor và đóng gói.
>
> **Code thực hành:** [`bai-tap/02-oop/ThuNghiemOOP.java`](../../bai-tap/02-oop/ThuNghiemOOP.java)
>
> Bài mở đầu Giai đoạn 2.

---

## 1. Vì sao OOP tồn tại? Bắt đầu từ **vấn đề**

Quản lý sinh viên với những gì đã học ở Giai đoạn 1:

```java
String ten1 = "An";    int tuoi1 = 20;   double diem1 = 8.5;
String ten2 = "Binh";  int tuoi2 = 21;   double diem2 = 7.0;
```

100 sinh viên là 300 biến. Thử dùng mảng ([bài 1.7](../01-java-core/07-mang-va-method.md)):

```java
String[] ten  = new String[100];
int[]    tuoi = new int[100];
double[] diem = new double[100];
```

Gọn hơn, nhưng xuất hiện vấn đề nghiêm trọng hơn:

```
   ten[]   │ "An"  │ "Binh" │ "Cuc" │ ...
   tuoi[]  │  20   │   21   │  19   │ ...
   diem[]  │  8.5  │  7.0   │  9.0  │ ...
              ▲        ▲
              └────────┴──── ba mảng PHẢI luôn khớp chỉ số
```

Xóa sinh viên "Binh" thì phải xóa ở **cả ba mảng**. Quên một chỗ là **tuổi của An gắn với điểm của Cúc** — dữ liệu sai mà chương trình vẫn chạy bình thường.

**Vấn đề gốc: những dữ liệu vốn thuộc về nhau lại bị xé lẻ ra ba nơi**, và không có gì trong ngôn ngữ ràng buộc chúng đi cùng nhau.

OOP **gom chúng thành một đơn vị**:

```java
SinhVien[] danhSach = new SinhVien[100];    // mỗi ô là một sinh viên trọn vẹn
```

Và không dừng ở dữ liệu — **hành vi liên quan cũng gom vào cùng chỗ**: tính điểm trung bình, xếp loại, kiểm tra tốt nghiệp.

> 📌 Một câu cho toàn bộ OOP: **đóng gói dữ liệu và hành vi liên quan vào cùng một đơn vị.**

### Khác JavaScript ở đâu?

```javascript
// JavaScript — tạo object ngay, không cần khai báo gì trước
const sv = { ten: "An", tuoi: 20 };
```

```java
// Java — BẮT BUỘC định nghĩa class trước
class SinhVien { String ten; int tuoi; }
SinhVien sv = new SinhVien();
```

Vì sao Java bắt buộc? Vì **static typing** ([bài 1.3](../01-java-core/03-bien-va-kieu-du-lieu.md)). Compiler phải biết trước object có field nào, kiểu gì — có vậy nó mới chặn được `sv.tenn` (gõ sai) ngay lúc compile. Trong JS, `sv.tenn` trả `undefined` và bug đi tiếp.

---

## 2. Class và Object khác nhau thế nào?

**Class là bản thiết kế. Object là thực thể tạo ra từ bản thiết kế đó.**

```
         class SinhVien              ← BẢN THIẾT KẾ: chỉ có MỘT
        ┌──────────────┐               mô tả "sinh viên gồm những gì"
        │ ten          │               bản thân nó không chứa dữ liệu
        │ tuoi         │
        │ diem         │
        └──────────────┘
               │
               │  new  (tạo thực thể)
        ┌──────┼──────┐
        ▼      ▼      ▼
     ┌──────┬──────┬──────┐
     │ "An" │"Binh"│ "Cuc"│          ← OBJECT: có thể có RẤT NHIỀU
     │  20  │  21  │  19  │             mỗi cái một vùng nhớ riêng trên Heap
     │ 8.5  │ 7.0  │ 9.0  │
     └──────┴──────┴──────┘
```

Class là **khuôn làm bánh**, object là **từng chiếc bánh**. Một khuôn, nhiều bánh; đập vỡ một chiếc không ảnh hưởng khuôn hay các bánh khác.

### `new` thực sự làm gì?

```
   new SinhVien("An")
        │
        ├─ ① Cấp một vùng nhớ mới trên HEAP
        ├─ ② Điền GIÁ TRỊ MẶC ĐỊNH cho mọi field (0 / null / false)
        ├─ ③ Chạy constructor  → gán giá trị thật
        └─ ④ Trả về ĐỊA CHỈ của vùng nhớ đó
```

Bước ④ giải thích vì sao object là **reference type** ([bài 1.4](../01-java-core/04-string-va-kieu-tham-chieu.md)): biến chỉ giữ địa chỉ, dữ liệu nằm trên Heap.

---

## 3. 🔬 Field có giá trị mặc định — **khác hẳn biến cục bộ**

```java
int bienCucBo;
System.out.println(bienCucBo);    // ❌ error: variable bienCucBo might not have been initialized
```

Nhưng field thì không sao:

```java
class SanPham {
    int soLuong;          // không khởi tạo gì cả
}
new SanPham().soLuong     // ✅ = 0
```

Đã kiểm chứng: field `int` ra `0`, `double` ra `0.0`, `boolean` ra `false`, `String` ra `null`.

### Vì sao hai chỗ lại khác nhau?

Không phải quy ước tùy tiện — lý do nằm ở **chỗ chúng được cấp phát**:

| | Biến cục bộ | Field của object |
|---|---|---|
| Nằm ở | **Stack** | Trong object, trên **Heap** |
| Vòng đời | Sinh và hủy liên tục mỗi lần gọi method | Sống cùng object |
| JVM có xóa sạch trước khi dùng? | **Không** — vùng Stack đó còn rác của lời gọi trước | **Có** — bước ② của `new` luôn xóa sạch về 0 |
| Hậu quả | Java **bắt bạn** gán trước khi dùng | Luôn có giá trị xác định |

Stack được tái sử dụng liên tục với tốc độ cao; xóa sạch mỗi lần gọi method sẽ rất tốn kém. Nên Java chọn cách khác: **để compiler chặn** việc đọc biến chưa gán — cửa kiểm soát 1 của [bài 1.2](../01-java-core/02-compile-time-vs-runtime.md).

> ⚠️ **Mặt trái:** field kiểu tham chiếu mặc định là `null`. Object "hợp lệ" nhưng bên trong đầy `null` chờ gây `NullPointerException` — giống `String[] ten = new String[3]` ở [bài 1.7](../01-java-core/07-mang-va-method.md). Đây là lý do constructor quan trọng.

---

## 4. Constructor — nghi thức khai sinh của object

```java
class SinhVien {
    String ten;

    SinhVien(String ten) {       // ← constructor
        this.ten = ten;
    }
}
```

Hai đặc điểm nhận dạng:

- **Tên trùng hệt tên class** (kể cả hoa/thường)
- **Không có kiểu trả về** — kể cả `void` cũng không được viết

> 💡 Vì sao không có kiểu trả về? Constructor không phải method thông thường. Việc trả địa chỉ object là do `new` làm (bước ④), không phải constructor. Nếu lỡ viết `void SinhVien(...)` thì đó **không còn là constructor** mà thành method bình thường tên `SinhVien` — và Java sẽ không gọi nó khi `new`. Bug rất khó thấy.

### ⚠️ Viết một constructor là constructor mặc định **biến mất**

Không viết constructor nào thì Java tự cấp một cái rỗng. Nhưng ngay khi viết **bất kỳ** constructor nào:

```java
SinhVien a = new SinhVien();
```
```
error: constructor SinhVien in class SinhVien cannot be applied to given types;
  required: String
  found:    no arguments
  reason: actual and formal argument lists differ in length
```

**Vì sao Java làm vậy?** Vì bạn đã tuyên bố *"muốn tạo sinh viên thì phải có tên"*. Nếu Java vẫn lén giữ constructor rỗng, người khác sẽ tạo được `SinhVien` không tên — đúng cái bạn đang cố ngăn.

Muốn có cả hai thì viết cả hai — chính là **overloading** ([bài 1.7](../01-java-core/07-mang-va-method.md)) áp dụng cho constructor:

```java
SinhVien() { this("Chua dat ten"); }    // this(...) gọi constructor kia
SinhVien(String ten) { this.ten = ten; }
```

---

## 5. `this` — và vì sao nó **dễ hơn** `this` của JavaScript

`this` là tham chiếu tới **chính object đang thực thi method**.

Công dụng phổ biến nhất: phân biệt field với tham số trùng tên.

```java
SinhVien(String ten) {
    this.ten = ten;
    //  ▲         ▲
    //  field     tham số
}
```

Bỏ `this` thì `ten = ten;` là **gán tham số cho chính nó** — field không bao giờ được set. Và `javac` **không báo lỗi** vì câu lệnh hợp lệ về cú pháp. Object tạo ra có `ten` là `null`. Bug im lặng.

**Vì sao cứ đặt tên trùng cho khổ?** Vì `ten` là cái tên đúng nhất cho cả hai. Đặt `tenMoi`, `_ten`, `pTen` chỉ làm code xấu đi. `this` cho phép giữ tên đẹp mà vẫn không nhập nhằng.

### So với JavaScript

| | JavaScript | Java |
|---|---|---|
| `this` được quyết định bởi | **Cách hàm được gọi** (call-site) | **Object chứa method** |
| Có đổi giữa chừng không? | ✅ Có — mất `this` khi truyền callback, phải `bind()` hoặc arrow function | ❌ **Không bao giờ** |
| Nguồn bug | Rất phổ biến | Gần như không có |

Nếu từng debug `this is undefined` trong React class component thì đây là tin vui: **trong Java, `this` luôn là object đang chạy method đó.** Không ngoại lệ, không cần `bind`.

---

## 6. `static` — giờ mới thấy trọn vẹn

[Bài 1.1](../01-java-core/01-chuong-trinh-dau-tien.md) nói `static` = *"thuộc về class, không cần `new` vẫn gọi được"*. Lúc đó chưa có object nào để so sánh. Giờ thì rõ:

```
            class SanPham
     ┌────────────────────────────┐
     │  static int tongSoSanPham  │   ← MỘT bản duy nhất
     │            = 2             │      mọi object dùng chung
     └────────────────────────────┘
                   │
       ┌───────────┴───────────┐
       ▼                       ▼
   ┌────────┐             ┌────────┐
   │ten:Ban │             │ten:Chu │   ← mỗi object có bản RIÊNG
   │gia:500k│             │gia:200k│
   └────────┘             └────────┘
```

Thực nghiệm: tạo 2 object, mỗi constructor chạy `tongSoSanPham++`, kết quả là `2` — và cả hai object đều thấy cùng con số.

Biến `static` đếm được số lần `new` chính vì nó **không thuộc object nào** — nó sống ở class, nên nó là chỗ duy nhất "nhìn thấy" toàn bộ.

> 📌 Truy cập qua **tên class**: `SanPham.tongSoSanPham`. Java cũng cho viết `a.tongSoSanPham` nhưng **đừng** — nó khiến người đọc tưởng đó là dữ liệu riêng của `a`.

---

## 7. Đóng gói (Encapsulation) — lời giải cho vấn đề ở bài 1.7

[Bài 1.7](../01-java-core/07-mang-va-method.md) kết thúc bằng cảnh báo: method nhận `List` có thể `clear()` sạch dữ liệu của bạn. Vấn đề chung là **ai cũng sửa được dữ liệu của tôi, theo cách tôi không lường trước**.

Nếu field để `public`:

```java
tk.soDu = -999999;        // ✅ compile được — số dư âm 1 triệu
```

Đổi thành `private`:

```
error: soLuong has private access in SanPham
```

Compiler chặn ngay tại **cửa 1**. Muốn đổi thì phải đi qua cánh cửa class cho phép:

```java
public void rutTien(double soTien) {
    if (soTien <= 0)   { throw new IllegalArgumentException("So tien phai duong"); }
    if (soTien > soDu) { throw new IllegalStateException("Khong du so du"); }
    soDu -= soTien;
}
```

### 🔑 Hai nửa của cùng một ý

```
   Cửa chính có bảo vệ          Cửa sau đã khóa
   ─────────────────────        ─────────────────────
   nhapHang(-5)                 banPhim.soLuong = -5
        │                              │
        ▼                              ▼
   kiểm tra → từ chối          ❌ error: soLuong has private access
```

Nếu field để `public`, thì method với đủ mọi kiểm tra bên trong **trở nên vô nghĩa** — ai cũng đi vòng qua nó được.

> 📌 `private` không phải để giấu cho bí ẩn. Nó tồn tại để **đảm bảo không có lối đi nào khác ngoài cửa chính**.

### Bốn mức truy cập

| Modifier | Trong class | Cùng package | Class con | Mọi nơi |
|---|---|---|---|---|
| `private` | ✅ | ❌ | ❌ | ❌ |
| *(không ghi gì)* — package-private | ✅ | ✅ | ❌ | ❌ |
| `protected` | ✅ | ✅ | ✅ | ❌ |
| `public` | ✅ | ✅ | ✅ | ✅ |

> 📌 **Quy tắc thực dụng: field luôn `private`.** Method thì `public` nếu là phần muốn người khác dùng. Ở Giai đoạn 7, JPA và Spring đều giả định bạn theo quy tắc này.

So với JS: JavaScript mãi tới 2022 mới có `#privateField` thật; trước đó chỉ có quy ước `_ten` — một lời đề nghị lịch sự mà ai cũng phá được. Java **cưỡng chế bằng compiler** từ 1995.

---

## 8. `toString()` — khép lại chuyện `[I@hashcode` ở bài 1.7

```java
System.out.println(sanPham);      // SanPham@1c20c684
```

Lý do giống hệt mảng ở [bài 1.7](../01-java-core/07-mang-va-method.md): **mọi class đều ngầm kế thừa class gốc `Object`**, và `Object.toString()` mặc định chỉ in `tênClass@hashcode`. Bạn **viết đè** (override):

```java
@Override
public String toString() {
    return "SanPham{ten='" + ten + "', gia=" + gia + "}";
}
```

---

## 8b. `@Override` và annotation — nền móng của Spring

### Annotation là **nhãn dán**, không phải code chạy

`@Override` là một **annotation** — dạng **metadata**: thông tin *về* code, gắn vào code, nhưng bản thân nó **không phải code thực thi**.

```
   ┌─────────────────┐
   │   ⚠ DỄ VỠ      │  ← nhãn dán (annotation)
   ├─────────────────┤
   │                 │
   │   hàng hóa      │  ← code thật
   │                 │
   └─────────────────┘
```

Nhãn "DỄ VỠ" **không làm thùng cứng hơn**. Nó là **chỉ dẫn cho người khác đọc rồi hành động**.

Câu hỏi quan trọng với mọi annotation luôn là: **ai đọc nó, và đọc lúc nào?**

### `@Override` nói gì với compiler

> *"Tôi **tin rằng** method này đang viết đè một method của lớp cha. Kiểm tra giúp tôi."*

- Đúng → **không có gì xảy ra**, chương trình chạy y hệt như khi không viết nó.
- Sai → lỗi compile.

Vậy `@Override` **không thay đổi hành vi chương trình**. Nó chỉ là một lời nhờ kiểm tra.

### Vì sao cần nhờ? Vì `javac` không đọc được ý định

Thí nghiệm: gõ nhầm `toString` thành `toStrring`, **không** có `@Override`:

```
javac: KHÔNG báo lỗi gì
Kết quả in ra: SanPham@4517d9a3
```

Compiler im lặng hoàn toàn, và `toString()` thật vẫn là bản mặc định của `Object`.

**Vì sao không kêu?** Vì `toStrring()` là một **method hoàn toàn hợp lệ** — chỉ là method mới với cái tên lạ. Không vi phạm luật ngôn ngữ nào. Nhớ [bài 1.2](../01-java-core/02-compile-time-vs-runtime.md): `javac` chỉ trả lời *"code này có hợp pháp không?"* — nó không biết bạn **định** làm gì.

Thêm `@Override`:

```
error: method does not override or implement a method from a supertype
    @Override
    ^
```

> 📌 Đây chính là lý do annotation tồn tại: **viết ý định của bạn ra thành thứ máy đọc được, để máy kiểm tra hộ.**

### 🔬 Ba mức "sống lâu" (retention)

Annotation khác nhau ở chỗ **nó sống tới giai đoạn nào**:

```
   Mã nguồn .java  ──javac──►  Bytecode .class  ──JVM──►  Chương trình đang chạy
         │                           │                            │
   ┌─────┴─────┐              ┌──────┴──────┐              ┌──────┴──────┐
   │  SOURCE   │              │    CLASS    │              │   RUNTIME   │
   │ ✂ bị xóa  │              │ có, nhưng   │              │ ✅ đọc được │
   │ ở đây     │              │ JVM bỏ qua  │              │  lúc chạy   │
   └───────────┘              └─────────────┘              └─────────────┘
```

| Retention | Sống tới đâu | Ví dụ | Ai đọc nó |
|---|---|---|---|
| **SOURCE** | Chỉ lúc compile, rồi **bị xóa sạch** | `@Override`, `@SuppressWarnings` | compiler |
| **CLASS** | Nằm trong `.class` nhưng JVM không nạp | (ít gặp) | công cụ phân tích bytecode |
| **RUNTIME** | **Đọc được khi chương trình đang chạy** | `@Deprecated`, **mọi annotation của Spring** | thư viện, framework |

### Bằng chứng: soi file `.class` bằng `javap -v`

```
--- Dấu vết annotation tìm thấy trong file .class ---
  #15 = Utf8               Deprecated
  #16 = Utf8               RuntimeVisibleAnnotations
  #17 = Utf8               Ljava/lang/Deprecated;
  Deprecated: true
  RuntimeVisibleAnnotations:
  java.lang.Deprecated
```

| Annotation trong mã nguồn | Có trong `.class`? |
|---|---|
| `@Deprecated` | ✅ Có — `RuntimeVisibleAnnotations` |
| `@Override` | ❌ **Không một dấu vết nào** |
| `@SuppressWarnings` | ❌ **Không một dấu vết nào** |

`@Override` **bị xóa sạch** sau khi biên dịch — bằng chứng dứt khoát cho câu *"nó không thay đổi hành vi chương trình"*. Còn `@Deprecated` **còn nguyên**, vì nó cần được đọc lúc chạy.

### 🔑 Mảnh ghép đầu tiên của Spring

Từ Giai đoạn 5 bạn sẽ viết những dòng trông như phép thuật:

```java
@Service
public class UserService {

    @Autowired
    private UserRepository repo;     // không hề new, nhưng nó vẫn có giá trị
}
```

Nửa lời giải nằm ở đây: những annotation đó thuộc loại **RUNTIME**, nên chúng **còn nguyên trong file `.class`**.

Lúc khởi động, Spring quét toàn bộ class trong project, **đọc các nhãn dán đó** (bằng cơ chế **reflection** — Giai đoạn 4), rồi hành động:

```
   Spring khởi động
        │
        ├─ đọc thấy @Service     → "class này cần một object, để tôi new cho"
        ├─ đọc thấy @Autowired   → "field này cần được điền, để tôi tìm object phù hợp"
        └─ đọc thấy @GetMapping  → "gọi method này khi có request GET tới đường dẫn đó"
```

> 📌 **Spring không phải phép thuật.** Nó là một chương trình Java bình thường, đọc nhãn dán trên code của bạn rồi làm việc tương ứng.

### So với JavaScript

Nếu từng dùng **NestJS** hay **Angular** thì đã gặp ý tưởng này — chúng gọi là **decorator**:

```typescript
@Injectable()
export class UserService { }
```

| | Decorator (JS/TS) | Annotation (Java) |
|---|---|---|
| Bản chất | Là **một hàm thật sự chạy** | **Thuần túy dữ liệu** |
| Tự thay đổi thứ nó gắn vào? | ✅ Có — thay thế class/method luôn | ❌ Không — nó chỉ nằm đó |
| Muốn có tác dụng thì | Tự nó chạy là xong | **Phải có ai đó đọc nó** |

Annotation Java thụ động hơn. Không có framework đọc thì `@Service` cũng chỉ là mấy ký tự vô nghĩa.

### Các annotation sẽ gặp trong lộ trình

| Annotation | Ý nghĩa | Gặp ở |
|---|---|---|
| `@Override` | "Tôi đang override, kiểm tra hộ" | Bài này |
| `@Deprecated` | "Đừng dùng nữa, sẽ bị xóa" | GĐ 3 |
| `@SuppressWarnings` | "Tôi biết có cảnh báo, bỏ qua đi" | GĐ 3 |
| `@FunctionalInterface` | "Interface này chỉ có đúng 1 method" | GĐ 3 |
| `@Service`, `@Autowired` | Spring: tạo và ghép nối object | GĐ 5 |
| `@RestController`, `@GetMapping` | Spring: định tuyến HTTP | GĐ 6 |
| `@Entity`, `@Column` | JPA: ánh xạ class sang bảng DB | GĐ 7 |

> 🛡️ **Quy tắc: luôn viết `@Override` mỗi khi định override.** Miễn phí, không ảnh hưởng hiệu năng, và bắt được loại bug mà code vẫn chạy ngon — chỉ là method của bạn không bao giờ được gọi.

---

## 9. Kết quả thực nghiệm

```
--- Ngay sau khi new ---
SanPham{ten='Ban phim', gia=500000.0, soLuong=0, conHang=false}
                                              ▲            ▲
                          constructor không gán, nhưng vẫn có giá trị mặc định

--- Nhap hang ---
   [tu choi] So luong phai duong          ← nhapHang(-5) bị chặn
SanPham{ten='Ban phim', gia=500000.0, soLuong=10, conHang=true}
Tong tien : 5000000.0

--- Hai object rieng biet ---
SanPham{ten='Ban phim', gia=500000.0, soLuong=10, conHang=true}
SanPham{ten='Chuot', gia=200000.0, soLuong=3, conHang=true}
      ▲ chuot.nhapHang(3) KHÔNG ảnh hưởng banPhim

--- static dung chung ---
SanPham.tongSoSanPham : 2                 ← đúng bằng số lần new
```

Và khi thêm `banPhim.soLuong = 100;` vào `main`:

```
ThuNghiemOOP.java:59: error: soLuong has private access in SanPham
        banPhim.soLuong = 100;
               ^
1 error
```

> 💡 Chạy `javac ThuNghiemOOP.java` sinh ra **hai** file: `SanPham.class` và `ThuNghiemOOP.class`. Một class Java luôn thành một file `.class` riêng, bất kể viết chung file `.java`.
>
> File tên `ThuNghiemOOP.java` mà chứa hai class là hợp lệ, vì `SanPham` **không** khai `public` — quy tắc "tên file phải trùng tên class" chỉ áp cho class `public` ([bài 1.2](../01-java-core/02-compile-time-vs-runtime.md), nuance thí nghiệm 1).

---

## 10. Tự kiểm tra

1. Mảng song song (`ten[]`, `tuoi[]`, `diem[]`) sai ở đâu? OOP sửa điều đó thế nào?
2. Vì sao JavaScript tạo object không cần class mà Java thì bắt buộc?
3. Biến cục bộ chưa gán là lỗi compile, nhưng field chưa gán thì không. Vì sao?
4. Constructor khác method thường ở hai điểm nào?
5. Viết `SinhVien(String ten)` rồi gọi `new SinhVien()` bị lỗi. Vì sao Java không giữ lại constructor rỗng?
6. Trong constructor, bỏ `this` đi và viết `ten = ten;` thì chuyện gì xảy ra? Compiler có báo không?
7. `this` của Java khác `this` của JavaScript ở điểm căn bản nào?
8. Nếu `soLuong` là `public` thì method `nhapHang()` với đủ kiểm tra bên trong còn giá trị gì không?

<details>
<summary>Đáp án</summary>

1. Ba mảng **phải luôn khớp chỉ số**, nhưng không có gì trong ngôn ngữ ràng buộc điều đó. Xóa sót một mảng là dữ liệu lệch mà chương trình vẫn chạy. OOP gom các dữ liệu thuộc về nhau (và hành vi liên quan) vào **một đơn vị**, nên chúng không thể tách rời.
2. Vì Java là **static typing** — compiler phải biết trước object có field nào, kiểu gì, để chặn lỗi gõ sai ngay lúc compile. JS là dynamic typing nên `sv.tenn` chỉ trả `undefined`.
3. Biến cục bộ nằm trên **Stack**, vùng nhớ được tái sử dụng liên tục và JVM **không xóa sạch** trước khi dùng (quá tốn kém), nên compiler bắt bạn gán trước. Field nằm trong object trên **Heap**, và bước ② của `new` **luôn xóa sạch về 0**.
4. (a) Tên trùng hệt tên class; (b) **không có kiểu trả về**, kể cả `void`.
5. Vì bạn đã tuyên bố "muốn tạo sinh viên thì phải có tên". Giữ lại constructor rỗng sẽ cho phép tạo object thiếu dữ liệu — đúng cái bạn đang cố ngăn.
6. `ten = ten;` gán **tham số cho chính nó**, field không bao giờ được set và giữ giá trị mặc định `null`. Compiler **không** báo gì vì câu lệnh hợp lệ về cú pháp — bug im lặng.
7. Trong JS, `this` phụ thuộc **cách hàm được gọi**, nên có thể mất hoặc đổi (phải `bind`/arrow). Trong Java, `this` **luôn** là object đang thực thi method đó, không bao giờ đổi.
8. **Không còn giá trị gì.** Ai cũng gán thẳng `banPhim.soLuong = -5` để đi vòng qua mọi kiểm tra. `private` tồn tại để đảm bảo **không có lối đi nào khác ngoài cửa chính**.

</details>

---

⬅️ **Bài trước:** [1.7 — Mảng và method](../01-java-core/07-mang-va-method.md)
➡️ **Bài tiếp:** [2.2 — Kế thừa (`extends`), `super` và `@Override`](02-ke-thua-super-override.md)
