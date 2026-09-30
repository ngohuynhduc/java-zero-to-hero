# Bài 2.2 — Kế thừa (`extends`), `super` và `@Override`

> **Mục tiêu:** Hiểu kế thừa giải quyết vấn đề gì, object con được **dựng lên theo thứ tự nào** trong bộ nhớ, override hoạt động ra sao — và vì sao gọi method có thể override từ constructor là một cái bẫy.
>
> **Code thực hành:** [`bai-tap/02-oop/KeThua.java`](../../bai-tap/02-oop/KeThua.java)

---

## 1. Vì sao kế thừa tồn tại? Bắt đầu từ **vấn đề**

Công ty có nhân viên thường và quản lý. Chỉ với kiến thức [bài 2.1](01-class-va-object.md):

```java
class NhanVien {
    private String ten;
    private double luongCoBan;
    public double tinhLuong() { return luongCoBan; }
    public void gioiThieu()   { ... }
}

class QuanLy {
    private String ten;            // ┐
    private double luongCoBan;     // │ chép y hệt NhanVien
    public void gioiThieu() {...}  // ┘
    private double phuCap;         // ← chỉ phần này là mới
    public double tinhLuong() { return luongCoBan + phuCap; }
}
```

Hai vấn đề:

1. **Code bị chép lại.** Công ty yêu cầu thêm `maSoThue` cho mọi nhân viên → sửa ở 2 chỗ. Có thêm `KeToan`, `BaoVe`, `ThucTapSinh` thì là 5 chỗ. Quên một chỗ = dữ liệu lệch. Đây đúng là bệnh "mảng song song" của bài 2.1, chỉ là ở cấp class.
2. **Mất quan hệ.** Về nghiệp vụ, *quản lý là một nhân viên*. Nhưng với compiler, hai class không liên quan gì. Không có cách nào nói *"method này nhận mọi loại nhân viên"*.

Kế thừa giải quyết cả hai:

```java
class QuanLy extends NhanVien {    // "QuanLy LÀ MỘT NhanVien, cộng thêm..."
    private double phuCap;
}
```

> 📌 `extends` = **"là một" (is-a)**. Class con tự động có mọi thứ của class cha, và chỉ cần viết **phần khác biệt**.

### So với JavaScript — cú pháp gần như y hệt, nhưng...

| | JavaScript | Java |
|---|---|---|
| Cơ chế thật sự | **Prototype chain** — `class` chỉ là cú pháp phủ lên | Kế thừa ở cấp kiểu, compiler kiểm tra |
| Gắn thêm method vào object lúc chạy | ✅ `obj.moi = () => {}` | ❌ Hình dạng class cố định lúc compile |
| Class con không viết constructor | Tự sinh `constructor(...args) { super(...args) }` — **chuyển tiếp mọi tham số** | Tự sinh `super()` — **không tham số nào** (mục 4) |
| Kế thừa nhiều class | ❌ | ❌ (mục 6) |

---

## 2. Một object con trông thế nào trong bộ nhớ?

Hiểu lầm phổ biến: `new QuanLy(...)` tạo ra **hai** object. **Sai.** Chỉ có **một** object, bên trong có nhiều lớp xếp chồng như tầng nhà:

```
  Stack                        Heap
 ┌──────────┐          ┌────────────────────────────────┐
 │ binh  ●──┼─────────►│  object QuanLy (MỘT object)    │
 └──────────┘          │ ┌────────────────────────────┐ │
                       │ │ phần Object   (gốc)        │ │  ← tầng móng
                       │ ├────────────────────────────┤ │
                       │ │ phần NhanVien              │ │
                       │ │   ten        = "Binh"      │ │
                       │ │   luongCoBan = 8000000.0   │ │  ← private: CÓ trong
                       │ ├────────────────────────────┤ │     object, nhưng code
                       │ │ phần QuanLy                │ │     của QuanLy không
                       │ │   phuCap     = 1500000.0   │ │     chạm vào được
                       │ │   phongBan   = ...         │ │
                       │ └────────────────────────────┘ │
                       └────────────────────────────────┘
```

---

## 3. Con được thừa hưởng những gì?

Cột **"Class con"** trong bảng truy cập của [bài 2.1](01-class-va-object.md) giờ mới có nghĩa:

| Thành viên của cha | Có nằm trong object con? | Code class con dùng trực tiếp? |
|---|---|---|
| `public` | ✅ | ✅ |
| `protected` | ✅ | ✅ — mức này sinh ra **chính là cho kế thừa** |
| `private` | ✅ **vẫn có** | ❌ |

Thí nghiệm 6b — trong `QuanLy` viết `return luongCoBan;`:

```
KeThua.java:39: error: luongCoBan has private access in NhanVien
        return luongCoBan;
               ^
1 error
```

**Vì sao khắt khe cả với con?** Cùng lý do bài 2.1: `private` đảm bảo **chỉ có một cửa chính**. *Bất kỳ ai* cũng viết được class con — nếu con sửa tùy ý field của cha, mọi kiểm tra cha viết đều bị đi vòng qua. Muốn con đọc được, cha phải **chủ động** mở: `protected` hoặc method `public`.

---

## 4. Constructor **không** được thừa kế — và `super(...)`

Field và method được thừa kế, constructor thì **không**. Nhưng phần `NhanVien` trong object vẫn cần được khởi tạo:

```java
QuanLy(String ten, double luongCoBan, double phuCap) {
    super(ten, luongCoBan);    // ← nhờ constructor CHA dựng phần của cha
    this.phuCap = phuCap;      // ← rồi con dựng phần của con
}
```

### Luật: **móng trước, tầng sau**

```
  new QuanLy(...)
     │
     ├─ ① Cấp vùng nhớ cho TOÀN BỘ object, xóa sạch về 0/null/false  (bài 2.1)
     │
     └─ ② Constructor QuanLy
            └─ super(...)  → constructor NhanVien
                   └─ super() → constructor Object
                          └─ Object xong  ─┐
                   NhanVien chạy phần còn lại ◄┘
            QuanLy chạy phần còn lại
```

**Vì sao cha phải chạy trước?** Vì con được phép *dựa vào* phần của cha. Con xây tầng 2 thì móng phải đổ xong.

Compiler cưỡng chế điều đó — viết gì trước `super(...)`:

```
error: call to super must be first statement in constructor
```

> 💡 Luật này được nới ở **Java 25** (cho phép kiểm tra tham số trước `super`, miễn chưa đụng tới `this`). Với JDK 21 vẫn là luật cứng.

### Không viết `super(...)`? Compiler **tự chèn `super()` rỗng**

Bằng chứng — class trống trơn `class Rong { }`, soi bằng `javap -c`:

```
Rong();
  Code:
     0: aload_0
     1: invokespecial #1   // Method java/lang/Object."<init>":()V   ← super() tới Object
     4: return
```

Không viết constructor, không viết `super()`, nhưng bytecode có cả hai. Đây là cách *"mọi class ngầm kế thừa `Object`"* trở thành sự thật.

**Chỗ nó cắn** — thí nghiệm 6a, xóa `super(ten, luongCoBan);`:

```
KeThua.java:31: error: constructor NhanVien in class NhanVien cannot be applied to given types;
    QuanLy(String ten, double luongCoBan, double phuCap) {
                                                         ^
  required: String,double
  found:    no arguments
  reason: actual and formal argument lists differ in length
1 error
```

Hai chi tiết đáng giá:

- **Dấu `^` chỉ vào dấu `{`** của constructor — không phải dòng bạn xóa, vì dòng đó không còn. Nó chỉ vào **đúng chỗ compiler lén chèn `super();`**. Lỗi báo cho một dòng code bạn không hề viết.
- Thông báo **giống hệt** lỗi `new SinhVien()` ở bài 2.1 — cùng một chuyện: gọi constructor rỗng không tồn tại (vì cha đã tự viết constructor có tham số).

Đây là khác biệt với JS ở bảng mục 1: JS tự chuyển tiếp tham số, Java thì không.

---

## 5. Override — con **thay** hành vi của cha

```java
@Override
public double tinhLuong() {
    return super.tinhLuong() + phuCap;
}
```

### a) `super.tinhLuong()` — mở rộng thay vì viết lại

`super.` = *"gọi bản của cha, bỏ qua bản của tôi"*. Con **không cần biết** cha tính lương cơ bản thế nào (và cũng không đọc được `luongCoBan`). Cha đổi công thức → con tự hưởng.

Thí nghiệm 6c — bỏ `super.`, chỉ còn `return tinhLuong() + phuCap;`. Kết quả: **compile thành công**, kể cả với `javac -Xlint:all` cũng không có cảnh báo nào. Nhưng khi chạy:

```
NhanVien{ten='An', luong=8000000.0}
Exception in thread "main" java.lang.StackOverflowError
	at QuanLy.tinhLuong(KeThua.java:39)
	at QuanLy.tinhLuong(KeThua.java:39)
	at QuanLy.tinhLuong(KeThua.java:39)
	...
```

`tinhLuong()` không có `super.` nghĩa là `this.tinhLuong()` — **gọi lại chính nó**, vô tận:

```
   Stack của thread main
  ┌──────────────────────┐
  │ QuanLy.tinhLuong()   │ ← frame thứ N ... cho tới khi hết chỗ
  │ QuanLy.tinhLuong()   │
  │ QuanLy.tinhLuong()   │
  │ ...                  │   mỗi lời gọi = một frame mới trên Stack (bài 1.4)
  │ NhanVien.toString()  │
  │ main()               │
  └──────────────────────┘
```

Hai điều cần để ý:

- Dòng `luong` của **An vẫn in bình thường** — `an` là `NhanVien`, chạy bản của cha. Chỉ khi tới `binh` mới nổ. Bug chỉ xuất hiện với **đúng loại object** đó — ở production, có thể chạy êm hàng tuần cho tới khi có người tạo quản lý đầu tiên.
- `javac` im lặng vì đệ quy là hợp pháp — nó không biết bạn **định** gọi bản của cha (bài 1.2: compiler kiểm tra tính hợp pháp, không đọc ý định).

> Stack trace có đúng 1024 dòng `at ...` — JVM mặc định chỉ in tối đa 1024 frame, dù thực tế đệ quy sâu hơn nhiều.

### b) 🔑 Luật quan trọng nhất: **object tự biết nó là ai**

Khi method bị override, **bản nào chạy do class thật sự của object quyết định** — lúc chạy, kể cả khi lời gọi nằm trong code của cha.

```
    object là QuanLy
           │
   gọi tinhLuong()  ──►  JVM hỏi: "object này thật ra là class gì?"
                                    │
                         QuanLy ──► có override? ── có ──► chạy bản QuanLy
                                                  └ không ─► leo lên NhanVien
                                                             └ không ─► leo lên Object
```

Toàn bộ sức mạnh của điều này (**đa hình**) là chủ đề bài 2.4. So với JS: giống hệt cách JS tìm method ngược lên prototype chain.

### Luật của override — compiler kiểm tra gì

| Luật | Vi phạm thì (đã chạy thử) | Vì sao |
|---|---|---|
| Cùng tên + cùng tham số | Thành **overload**, `@Override` báo `method does not override...` | Khác tham số là method khác |
| Kiểu trả về tương thích | `return type int is not compatible with double` | Ai gọi qua kiểu cha đang chờ `double` |
| Không hạ mức truy cập | `attempting to assign weaker access privileges; was public` | Cha hứa "ai cũng gọi được" — con không được thất hứa |
| Method cha không `final` | `overridden method is final` | Cha tuyên bố "hành vi này không được đổi" |

| | Overload ([bài 1.7](../01-java-core/07-mang-va-method.md)) | Override |
|---|---|---|
| Ở đâu | Cùng một class | Giữa cha và con |
| Tham số | **Khác nhau** | **Giống hệt** |
| Chọn bản nào, khi nào | **Compile-time**, theo kiểu tham số | **Runtime**, theo class thật của object |

---

## 6. `final`, kế thừa đơn, và `Object` ở đỉnh

### Chỉ được `extends` **một** class

```java
class C extends A, B { }     // error: '{' expected
```

**Vì sao?** Bài toán **kim cương**: `A` và `B` đều có `tinhLuong()` khác nhau — `C` nhận bản nào? Java cắt vấn đề từ gốc. Nhu cầu "thuộc nhiều nhóm" giải bằng **interface** (bài 2.3). Kết quả: mọi class tạo thành **một cây**, gốc là `Object`:

```
                 Object
            ┌──────┼───────┐
         String  NhanVien  SanPham (bài 2.1)
                   │
                 QuanLy
```

### `final` — "cấm kế thừa tiếp"

```java
class ChuoiXin extends String { }
// error: cannot inherit from final String
```

**Vì sao `String` là `final`?** [Bài 1.4](../01-java-core/04-string-va-kieu-tham-chieu.md): String pool chia sẻ một object cho nhiều biến, chỉ an toàn vì String **bất biến**. Nếu viết được class con của `String` có method sửa nội dung rồi truyền vào chỗ nhận `String`, toàn bộ bảo đảm sụp đổ.

### `instanceof` — "object này có phải là một X không?"

Trả lời theo cả chuỗi đi lên cây: `binh` là `QuanLy`, là `NhanVien`, là `Object`. Nhưng `an` **không** là `QuanLy` — cha không phải là con.

---

## 7. 🔬 Mổ xẻ: vì sao `phongBan` in ra `null`?

Đây là dòng quan trọng nhất của output:

```
--- Tao QuanLy ---
  [NhanVien] constructor bat dau
  Xin chao, toi la Binh, quan ly phong null     ← ???
  [NhanVien] constructor ket thuc
  [QuanLy] constructor chay
```

Trong code, `phongBan` được gán ngay tại chỗ khai báo: `private String phongBan = "Ky thuat";`. Trông như nó có giá trị "ngay từ đầu". **Không phải.** Soi bytecode constructor `QuanLy` bằng `javap -c -p QuanLy`:

```
QuanLy(java.lang.String, double, double);
  Code:
     0: aload_0
     1: aload_1
     2: dload_2
     3: invokespecial #1   // Method NhanVien."<init>":(Ljava/lang/String;D)V   ← ① super(...)
     6: aload_0
     7: ldc           #7   // String Ky thuat
     9: putfield      #9   // Field phongBan:Ljava/lang/String;                 ← ② phongBan = "Ky thuat"
    12: getstatic     #15  // Field java/lang/System.out
    15: ldc           #21  // String   [QuanLy] constructor chay                  ← ③ thân constructor
    ...
    23: putfield      #29  // Field phuCap:D
```

**Compiler đã dời dòng khởi tạo field vào bên trong constructor, đặt ngay SAU `super(...)`.** Không có "ngay từ đầu" nào cả. Ghép ba mảnh lại:

```
 thời điểm                          phongBan    chuyện gì xảy ra
 ─────────────────────────────────────────────────────────────────────
 new: cấp nhớ, xóa sạch            null        bước ① — bài 2.1
 ① super(...) → constructor cha    null
     └─ gioiThieu()                null   ◄── object là QuanLy → chạy bản QuanLy
                                                (mục 5b) → đọc phongBan: null
 ② phongBan = "Ky thuat"           "Ky thuat"  ← giờ mới được gán
 ③ thân constructor QuanLy         "Ky thuat"
 binh.gioiThieu() sau đó           "Ky thuat"  ← nên lần gọi sau in đúng
```

Không phải vì "không truy cập được" — `gioiThieu()` của `QuanLy` đọc field của chính `QuanLy`, hoàn toàn hợp lệ. Vấn đề thuần túy là **thời điểm**: method của con chạy khi **phần của con chưa được dựng**. Cha gọi lên một tầng nhà chưa xây.

`super.tinhLuong()` trong bytecode cũng đáng nhìn: nó là `invokespecial NhanVien.tinhLuong` — gọi **cố định** bản của cha, không hỏi class thật của object. Còn lời gọi bình thường là `invokevirtual` — hỏi object "bạn là ai" rồi mới chọn bản. Chính là khác biệt giữa 6c và bản gốc.

---

## 8. `toString()` in ra `NhanVien{...}` cho một `QuanLy`

```
NhanVien{ten='Binh', luong=9500000.0}
```

Một dòng chứa **cả hai** cơ chế:

| Phần | Vì sao |
|---|---|
| `NhanVien{` | `QuanLy` **không** override `toString()`, nên chạy bản của cha — và trong đó `"NhanVien{"` là **chuỗi gõ cứng**. Chữ đó không tự đổi theo object |
| `luong=9500000.0` | `toString()` của cha gọi `tinhLuong()` → object là `QuanLy` → chạy bản `QuanLy` (mục 5b) |

Cách sửa: hỏi object tên class thật của nó lúc chạy:

```java
return getClass().getSimpleName() + "{ten='" + ten + "', luong=" + tinhLuong() + "}";
```

```
NhanVien{ten='An', luong=8000000.0}
QuanLy{ten='Binh', luong=9500000.0}
```

`getClass()` được thừa kế từ `Object` — một ví dụ nữa về cái cây ở mục 6.

---

## 9. Kết quả thực nghiệm

```
--- Tao NhanVien ---
  [NhanVien] constructor bat dau
  Xin chao, toi la An
  [NhanVien] constructor ket thuc

--- Tao QuanLy ---
  [NhanVien] constructor bat dau                 ← cha chạy TRƯỚC
  Xin chao, toi la Binh, quan ly phong null      ← bản QuanLy, phần con chưa dựng
  [NhanVien] constructor ket thuc
  [QuanLy] constructor chay                      ← con chạy SAU

--- Luong ---
NhanVien{ten='An', luong=8000000.0}
NhanVien{ten='Binh', luong=9500000.0}            ← chữ gõ cứng + override

--- Goi lai gioiThieu sau khi tao xong ---
  Xin chao, toi la Binh, quan ly phong Ky thuat  ← giờ field đã được gán

--- instanceof ---
binh instanceof QuanLy   : true
binh instanceof NhanVien : true
binh instanceof Object   : true
an   instanceof QuanLy   : false
```

`javac KeThua.java` sinh ra **ba** file: `NhanVien.class`, `QuanLy.class`, `KeThua.class`.

---

## 10. Cạm bẫy production

### 🪤 Bẫy 1: Gọi method có thể override từ constructor

Chính là mục 7. Ở code thật, thay `phongBan` bằng một `List` hay một kết nối DB là `null` → **`NullPointerException` ngay trong lúc tạo object**, với stack trace chỉ vào class con dù lỗi thật nằm ở thiết kế của class cha. Tệ hơn: người viết class cha và class con thường là **hai người khác nhau**, không ai thấy toàn cảnh.

> 🛡️ **Quy tắc:** constructor chỉ được gọi method `private` hoặc `final` (không thể override). Đây là luật được ghi trong *Effective Java* (Item 19) — sách gối đầu của dân Java.

### 🪤 Bẫy 2: Kế thừa sai quan hệ — ngay trong JDK

`java.util.Stack` (ngăn xếp: chỉ thêm/lấy ở **đỉnh**) được viết từ 1996 là `Stack extends Vector` (`javap` xác nhận). `Vector` cho chèn/xóa **ở bất kỳ đâu**, và kế thừa kéo theo **toàn bộ** method `public` của cha:

```java
s.push("A"); s.push("B"); s.push("C");
s.add(0, "CHEN_DAY");      // chèn vào ĐÁY ngăn xếp
s.remove(2);               // rút ở GIỮA
System.out.println(s);     // [CHEN_DAY, A, C]
```

Vì hàng triệu dòng code đã phụ thuộc, lỗi này **không bao giờ sửa được** — tài liệu chỉ còn biết khuyên *"đừng dùng `Stack`"*. `Properties extends Hashtable` là lỗi cùng loại.

> 📌 **"Composition over inheritance"**: quan hệ *"có một"* (Stack **có một** danh sách bên trong) thì dùng field. Chỉ `extends` khi *"là một"* đúng **trong mọi tình huống**.

### 🪤 Bẫy 3 (xem trước GĐ 5–7): Spring và Hibernate **kế thừa class của bạn** lúc chạy

Khi bạn viết `@Transactional` (GĐ 7), Spring tạo lúc chạy một **class con** của class bạn (gọi là *proxy*), override method để chèn thêm "mở transaction… commit". Hibernate làm tương tự với entity để lazy-load dữ liệu.

Giờ bạn đã biết đủ để đoán hệ quả: method `final` **không override được** → proxy không chèn được gì → `@Transactional` trên method `final` **im lặng không có tác dụng**. Không lỗi compile, không exception — chỉ là dữ liệu không được rollback. Đó là lý do tài liệu Spring và Hibernate dặn: đừng đánh `final` lên class/method mà framework cần proxy.

---

## 11. Tự kiểm tra

1. `new QuanLy(...)` tạo ra mấy object trên Heap? Field `private` của cha có nằm trong đó không?
2. Class con không viết `super(...)` thì chuyện gì xảy ra? Vì sao lỗi ở thí nghiệm 6a lại chỉ vào dấu `{`?
3. Vì sao Java bắt constructor cha chạy trước constructor con?
4. `private String phongBan = "Ky thuat";` — dòng gán này thực sự chạy lúc nào? Chứng minh bằng gì?
5. Vì sao `println(binh)` in `NhanVien{` nhưng lương lại là `9500000.0`?
6. Bỏ `super.` trong `tinhLuong()` thì compile có lỗi không? Chạy thì sao, và vì sao `an` vẫn in bình thường?
7. Vì sao `java.util.Stack extends Vector` bị coi là sai thiết kế?

<details>
<summary>Đáp án</summary>

1. **Một** object, bên trong có các lớp: phần `Object`, phần `NhanVien`, phần `QuanLy`. Field `private` của cha **có** nằm trong object, chỉ là code của con không truy cập trực tiếp được.
2. Compiler **tự chèn `super();`** rỗng vào đầu constructor. Nếu cha không có constructor rỗng → lỗi `cannot be applied to given types`. Dấu `^` chỉ vào `{` vì đó là **chỗ compiler chèn** lời gọi — một dòng bạn không hề viết.
3. Vì con được phép dựa vào phần của cha (dùng field `protected`, gọi method của cha). Móng phải đổ trước khi xây tầng.
4. Compiler **dời nó vào trong constructor, ngay sau `super(...)`**. Chứng minh bằng `javap -c`: `invokespecial NhanVien.<init>` đứng trước `putfield phongBan`. Nên trong lúc constructor cha chạy, `phongBan` vẫn là `null`.
5. `QuanLy` không override `toString()` nên chạy bản của cha, trong đó `"NhanVien{"` là chuỗi gõ cứng. Còn `tinhLuong()` được gọi bên trong đó thì **có** bị override → object là `QuanLy` → chạy bản `QuanLy`. Sửa bằng `getClass().getSimpleName()`.
6. Không lỗi compile (đệ quy là hợp pháp). Chạy thì `tinhLuong()` gọi chính nó vô tận → `StackOverflowError`. `an` là `NhanVien` nên chạy bản của cha, không đệ quy — bug chỉ nổ với object `QuanLy`.
7. Stack **không phải là một** Vector — nó chỉ nên cho thao tác ở đỉnh, nhưng kế thừa kéo theo mọi method `public` của `Vector` (chèn/xóa ở giữa). Lẽ ra phải dùng composition: Stack **có một** danh sách bên trong.

</details>

---

⬅️ **Bài trước:** [2.1 — Class và Object](01-class-va-object.md)
➡️ **Bài tiếp:** 2.3 — Abstract class và Interface
