# Bài 2.5 — `equals()`, `hashCode()` và `record`

> **Mục tiêu:** Tự viết `equals()` đúng, hiểu `HashSet`/`HashMap` hoạt động thế nào, và thấy tận mắt vì sao override `equals()` mà quên `hashCode()` là bug kinh điển nhất Java.
>
> **Code thực hành:** [`bai-tap/02-oop/EqualsHashCode.java`](../../bai-tap/02-oop/EqualsHashCode.java)
>
> Bài cuối của Giai đoạn 2.

---

## 1. Vấn đề: `equals()` mặc định chẳng khác gì `==`

```java
User a = new User("duc@x.com");
User b = new User("duc@x.com");
a.equals(b);      // false
```

Mọi class đều ngầm `extends Object` ([bài 2.2](02-ke-thua-super-override.md)), và bản `equals()` của `Object` viết đúng thế này:

```java
public boolean equals(Object obj) {
    return (this == obj);          // ← so sánh ĐỊA CHỈ, không hơn
}
```

Nên với class tự viết, cho tới khi override, `equals()` **chính là** `==`.

Đây là mặt còn lại của câu chuyện `==` vs `equals()` đã gặp ba lần ([1.4](../01-java-core/04-string-va-kieu-tham-chieu.md), [1.5](../01-java-core/05-wrapper-autoboxing-ep-kieu.md), [1.6](../01-java-core/06-toan-tu-va-luong-dieu-khien.md)). Lần đó bạn là **người dùng** `equals()` của `String`, `Integer`, `Double` — đã được override sẵn. Giờ bạn là **người viết**.

### So với JavaScript

```javascript
new Set([{email: "duc@x.com"}]).has({email: "duc@x.com"})    // false
```

JS **không có cách nào** định nghĩa "hai object bằng nhau" cho `Set`/`Map` — chúng luôn so tham chiếu. Java cho phép class **tự định nghĩa** thế nào là bằng nhau, và mọi cấu trúc dữ liệu đều tôn trọng định nghĩa đó. Quyền lực đó đi kèm trách nhiệm.

---

## 2. Viết `equals()` đúng — từng dòng một

```java
@Override
public boolean equals(Object o) {
    if (this == o) return true;                         // ①
    if (!(o instanceof User khac)) return false;        // ②
    return email.equals(khac.email);                    // ③
}
```

| Dòng | Làm gì | Vì sao |
|---|---|---|
| ① | Cùng địa chỉ thì chắc chắn bằng | Tối ưu: khỏi so từng field |
| ② | Không phải `User` (kể cả `null`) → `false` | `instanceof` trả `false` với `null`, nên dòng này **chặn luôn NPE**. Pattern matching ([bài 2.4](04-da-hinh.md)) cho sẵn biến `khac` đã ép kiểu |
| ③ | So **nội dung** từng field quan trọng | Đây là định nghĩa "bằng nhau" của bạn |

### ⚠️ Tham số **phải** là `Object`, không phải `User`

```java
public boolean equals(User o) { ... }       // ❌ đây là OVERLOAD, không phải override
```

Đúng cái bẫy ở [bài 2.2](02-ke-thua-super-override.md): tham số khác → tạo method mới. `HashSet`, `List.contains()` gọi `equals(Object)` — bản của `Object` — nên method của bạn **không bao giờ được gọi**. Có `@Override` thì compiler chặn ngay.

### Hợp đồng của `equals()` — 5 điều kiện

| Tính chất | Nghĩa là | Vi phạm thì |
|---|---|---|
| **Phản xạ** | `x.equals(x)` luôn `true` | Bỏ vào list rồi tìm lại không thấy |
| **Đối xứng** | `x.equals(y)` ⟺ `y.equals(x)` | `contains()` phụ thuộc thứ tự |
| **Bắc cầu** | `x=y` và `y=z` thì `x=z` | Sắp xếp, gom nhóm vô lý |
| **Nhất quán** | Gọi nhiều lần cho cùng kết quả (khi object không đổi) | Hành vi ngẫu nhiên |
| **Với `null`** | `x.equals(null)` luôn `false`, không ném NPE | Nổ ở chỗ không ngờ |

Không cần thuộc lòng — chỉ cần biết **thư viện Java giả định bạn tuân thủ cả 5**. Nhớ [bài 1.6](../01-java-core/06-toan-tu-va-luong-dieu-khien.md): `Double.equals()` cố tình coi mọi `NaN` là bằng nhau, chính để giữ **tính phản xạ** — nếu không thì `NaN` bỏ vào `Set` sẽ không bao giờ tìm lại được.

---

## 3. `HashSet` / `HashMap` hoạt động thế nào

1 triệu user trong `List`: `list.contains(x)` phải gọi `equals()` với **từng phần tử** — tới 1 triệu lần.

`HashSet` chia dữ liệu vào nhiều **ngăn** (bucket), và dùng `hashCode()` để biết **ngay** object nằm ở ngăn nào:

```
   set.add(user)
        │
        ├─ ① tính user.hashCode()  →  1301634908
        ├─ ② lấy số đó chọn ngăn   →  ngăn số 4
        └─ ③ cất user vào ngăn 4

   ┌────────┬────────┬────────┬────────┬────────┬────────┐
   │ ngăn 0 │ ngăn 1 │ ngăn 2 │ ngăn 3 │ ngăn 4 │ ngăn 5 │
   │        │  ...   │        │  ...   │ [user] │        │
   └────────┴────────┴────────┴────────┴────────┴────────┘

   set.contains(x)
        │
        ├─ ① tính x.hashCode()     →  chọn ngăn
        ├─ ② CHỈ nhìn vào ngăn đó  →  bỏ qua toàn bộ các ngăn khác
        └─ ③ trong ngăn đó, gọi equals() với từng phần tử (thường chỉ 0–2 cái)
```

Thay vì 1 triệu lần `equals()`, chỉ cần **một** lần `hashCode()` và vài lần `equals()`. Đó là lý do `HashSet`/`HashMap` tra cứu gần như **tức thì** bất kể kích thước.

> 📌 **`hashCode()` quyết định tìm ở ngăn nào. `equals()` chỉ được hỏi tới bên trong ngăn đó.** Nếu `hashCode()` dẫn tới sai ngăn, `equals()` **không bao giờ được gọi**.

`hashCode()` mặc định của `Object` sinh ra từ **danh tính** object — hai object khác nhau gần như chắc chắn có hai số khác nhau, bất kể nội dung.

### Hợp đồng `equals` ↔ `hashCode`

> **Nếu `a.equals(b)` là `true` thì `a.hashCode()` BẮT BUỘC bằng `b.hashCode()`.**
>
> Chiều ngược lại không bắt buộc: hai object khác nhau được phép trùng hashCode — chúng chỉ chung ngăn.

### Cách viết `hashCode()` đúng

```java
@Override
public int hashCode() {
    return Objects.hash(email);       // dùng ĐÚNG các field đã dùng trong equals()
}
```

**Field nào tham gia `equals()` thì đúng field đó tham gia `hashCode()`** — không thừa, không thiếu.

---

## 4. 🚨 Bug kinh điển: override `equals()` mà quên `hashCode()`

Kết quả thực nghiệm với `UserChiEquals`:

```
b1.equals(b2)     : true
hashCode b1, b2   : 868693306, 1746572565     ← khác nhau
list.contains(b2) : true                       ← List chỉ dùng equals → đúng
set.contains(b2)  : false                      ← HashSet hỏi hashCode trước → sai ngăn
set.size()        : 2                          ← Set chứa HAI phần tử bằng nhau!
```

### `size() = 2`: `Set` phá vỡ cam kết duy nhất của nó

`Set` chỉ có **một** cam kết: *không chứa phần tử trùng lặp*. Vậy mà giờ nó chứa `b1` và `b2` — hai object mà chính `b1.equals(b2)` khẳng định là bằng nhau.

```
   add(b1)  →  hashCode 868693306  →  ngăn A   [b1]
   add(b2)  →  hashCode 1746572565 →  ngăn B   [b2]
                                        │
               HashSet chỉ hỏi equals() TRONG ngăn B → ngăn B trống
               → "chưa có, thêm vào"
```

`equals()` viết đúng — nhưng **không bao giờ được hỏi tới**.

### Vì sao đây là bug kinh điển?

Vì nó **không có triệu chứng ở chỗ viết sai**. `equals()` chạy đúng, test so sánh hai object chạy đúng, `List` chạy đúng. Nó chỉ lộ ra ở nơi khác hẳn — khi ai đó bỏ object vào `HashSet`, làm khóa `HashMap`, hay một framework dùng chúng bên trong mà bạn không nhìn thấy.

Hậu quả thật: dùng `Set<User>` để khử trùng email đăng ký → khử không được gì. Dùng `Map<User, GioHang>` → mỗi lần user đăng nhập lại tạo giỏ hàng **mới**, giỏ cũ mất dấu.

---

## 5. 🚨 Bẫy thứ hai: sửa field **sau khi** đã bỏ vào `Set`

Kể cả viết đủ cả hai method:

```java
set.add(c1);                  // cất vào ngăn tính theo email CŨ
c1.email = "khac@x.com";      // hashCode giờ ĐỔI
```

Thực nghiệm:

```
contains(c1)        : false     ← không tìm thấy CHÍNH object vừa bỏ vào
remove(c1)          : false     ← không xóa được
size sau remove     : 1         ← nó vẫn nằm đó
duyệt for-each thấy : true      ← lặp qua thì vẫn gặp nó
add(c1) lần nữa     : size = 2  ← CÙNG MỘT object, nằm trong Set HAI lần
```

```
   add(c1)   (email = duc@x.com)  →  hashCode cũ  →  cất vào ngăn A  [c1]
   c1.email = "khac@x.com"         →  hashCode MỚI →  trỏ sang ngăn B
                                                         │
   contains(c1) / remove(c1)  →  tìm ở ngăn B  →  trống  →  false
   add(c1)                    →  ngăn B trống  →  thêm   →  c1 nằm ở CẢ A lẫn B
```

Object **mất tích bên trong chính `Set` của nó**: vẫn chiếm bộ nhớ, vẫn hiện ra khi duyệt, nhưng không thể tìm, không thể xóa. Ở server chạy liên tục nhiều tuần, đó là một dạng **memory leak** — `Set` phình dần mà không ai dọn được.

> 📌 Quy tắc: **field dùng trong `equals()`/`hashCode()` phải bất biến** — khai `final`. Không đổi được thì không thể mất tích. Và đó đúng là thứ `record` làm cho bạn.

---

## 6. `record` (Java 16+) — một dòng thay cả trăm dòng

Một class chỉ chứa dữ liệu, viết đúng chuẩn, cần: field `private final`, constructor, getter, `equals()`, `hashCode()`, `toString()`. Dễ 40–50 dòng, và dễ quên một cái.

```java
record User(String email, String ten) { }
```

Soi bằng `javap -p` xem compiler sinh ra gì:

```
final class UserR extends java.lang.Record {
  private final java.lang.String email;           ← field private + final
  UserR(java.lang.String);                         ← constructor
  public final java.lang.String toString();        ← toString
  public final int hashCode();                     ← hashCode
  public final boolean equals(java.lang.Object);   ← equals (đúng chữ ký Object!)
  public java.lang.String email();                 ← "getter"
}
```

| Đặc điểm | Ý nghĩa |
|---|---|
| `final class` | Không ai `extends` được — giống `String` ([bài 2.2](02-ke-thua-super-override.md)) |
| `extends java.lang.Record` | Đã dùng hết suất kế thừa duy nhất, nên record **không `extends` gì khác được** — nhưng vẫn `implements` interface thoải mái ([bài 2.3](03-interface-va-abstract-class.md)) |
| Field `private final` | **Bất biến** — giải quyết luôn bẫy mục 5 |
| `equals`/`hashCode` sinh tự động | Dùng **toàn bộ** field, luôn nhất quán với nhau |
| Getter tên `email()` | Không phải `getEmail()` — khác convention cũ |

Thử sửa field:

```
error: x has private access in Diem
```

### Khi nào dùng `record`, khi nào dùng class thường?

| Dùng `record` | Dùng class thường |
|---|---|
| Dữ liệu thuần túy, không đổi sau khi tạo | Object có trạng thái thay đổi theo thời gian |
| DTO nhận/trả qua API (GĐ 6, 8) | Entity JPA ánh xạ bảng DB (GĐ 7) |
| Khóa của `Map`, phần tử của `Set` | Object cần kế thừa từ class khác |

> 💡 Vì sao entity JPA **không** dùng `record` được? Hibernate cần constructor rỗng, cần field sửa được, và cần **kế thừa class của bạn** để tạo proxy (bẫy 3 ở [bài 2.2](02-ke-thua-super-override.md)) — mà record là `final`. Chuyện `equals()`/`hashCode()` cho entity là vấn đề nổi tiếng sẽ gặp lại ở GĐ 7.

---

## 7. Kết quả thực nghiệm đầy đủ

```
--- 1. Khong override gi ---
a1.equals(a2)     : false

--- 2. Chi override equals ---
b1.equals(b2)     : true
hashCode b1, b2   : 868693306, 1746572565
list.contains(b2) : true
set.contains(b2)  : false
set.size()        : 2

--- 3. Override ca hai ---
hashCode c1, c2   : 1301634908, 1301634908
set.contains(c2)  : true

--- 4. Sua field sau khi bo vao Set ---
set.contains(c1)  : false
set.size()        : 1

--- 5. record ---
r1                : UserRecord[email=duc@x.com]
r1.equals(r2)     : true
r1 == r2          : false          ← == không bao giờ override được, luôn so địa chỉ
```

> Số hashCode mặc định (phần 2) thay đổi mỗi lần chạy, vì nó sinh từ danh tính object. Số ở phần 3 thì luôn giống nhau, vì nó tính từ nội dung.

### Tổng kết

| Viết thế nào | `List` | `HashSet` / `HashMap` |
|---|---|---|
| Không override gì | So địa chỉ | So địa chỉ |
| Chỉ `equals()` | ✅ Đúng | ❌ **Sai im lặng** — trùng lặp, tìm không thấy |
| Cả hai, field đổi được | ✅ | ⚠️ Đúng **cho tới khi** ai đó sửa field |
| Cả hai, field `final` / `record` | ✅ | ✅ |

---

## 8. Tự kiểm tra

1. Vì sao `a.equals(b)` cho `false` với class tự viết dù cùng nội dung?
2. Vì sao tham số của `equals()` phải là `Object` chứ không phải `User`?
3. Dòng `if (!(o instanceof User khac)) return false;` xử lý trường hợp `o == null` thế nào?
4. `HashSet.contains()` dùng `hashCode()` và `equals()` theo thứ tự nào? Vì sao nhanh hơn `List.contains()`?
5. Chỉ override `equals()`: vì sao `list.contains(b2)` đúng mà `set.contains(b2)` sai?
6. Vì sao `Set` lại chứa được hai phần tử bằng nhau?
7. Đổi email của `c1` sau khi bỏ vào `Set` thì `contains`, `remove`, `add` lại hành xử thế nào? Vì sao đây là memory leak?
8. `record` giải quyết những bẫy nào ở trên? Vì sao entity JPA không dùng `record` được?

<details>
<summary>Đáp án</summary>

1. Vì `Object.equals()` chỉ là `this == obj` — so **địa chỉ**. Hai object khác vùng nhớ Heap thì `false`, bất kể nội dung.
2. Vì `equals(User)` có chữ ký khác nên là **overload**, không phải override. Thư viện gọi `equals(Object)` nên method của bạn không bao giờ chạy. `@Override` sẽ bắt lỗi này.
3. `null instanceof User` luôn cho `false`, nên dòng này trả `false` ngay — không bao giờ chạm tới `khac.email` để gây NPE.
4. **`hashCode()` trước** để chọn ngăn, rồi mới gọi `equals()` với vài phần tử trong ngăn đó. `List` phải gọi `equals()` với **toàn bộ** phần tử.
5. `List` chỉ dùng `equals()` (đã viết đúng). `HashSet` hỏi `hashCode()` trước — mà `hashCode()` mặc định sinh từ danh tính nên `b1`, `b2` rơi vào **hai ngăn khác nhau**; `equals()` không bao giờ được hỏi.
6. Vì `add(b2)` chỉ kiểm tra trùng **trong ngăn của `b2`** — ngăn đó trống, nên `HashSet` kết luận "chưa có" và thêm vào.
7. `hashCode()` mới trỏ sang ngăn khác trong khi object vẫn nằm ở ngăn cũ: `contains` → `false`, `remove` → `false` (không xóa được), `add` → thêm lần nữa, cùng object nằm hai chỗ. Object chiếm bộ nhớ mà không cách nào dọn được → leak.
8. Field `private final` (không mất tích), `equals`/`hashCode` sinh tự động và nhất quán (không quên được), chữ ký `equals(Object)` đúng (không overload nhầm). Entity JPA cần constructor rỗng, field sửa được, và Hibernate phải kế thừa class để tạo proxy — record là `final` và bất biến.

</details>

---

⬅️ **Bài trước:** [2.4 — Đa hình](04-da-hinh.md)
➡️ **Bài tiếp:** Giai đoạn 3 — Collections, Exception, Generics, Stream API
