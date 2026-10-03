# Bài 2.4 — Đa hình (Polymorphism)

> **Mục tiêu:** Hiểu late binding ở tầng bytecode, biết chính xác **cái gì đa hình và cái gì không**, và thấy vì sao đa hình mới là lý do thực sự OOP tồn tại.
>
> **Code thực hành:** [`bai-tap/02-oop/DaHinh.java`](../../bai-tap/02-oop/DaHinh.java)

---

## 1. Đây mới là lý do thực sự OOP tồn tại

[Bài 2.2](02-ke-thua-super-override.md) nói *"tái sử dụng code không phải mục đích chính của kế thừa"*. Giờ là mục đích chính.

Code **không có** đa hình:

```java
for (Object o : danhSach) {
    if (o instanceof Cho)      { ((Cho) o).keuGau();   }
    else if (o instanceof Meo) { ((Meo) o).keuMeo();   }
    else if (o instanceof Vit) { ((Vit) o).keuQuac();  }
}
```

Thêm con bò? Phải tìm **mọi** chuỗi `if-else` như trên trong toàn dự án và sửa. Sót một chỗ là bò im lặng không kêu.

Với đa hình:

```java
for (DongVat d : danhSach) {
    d.keu();
}
```

Thêm con bò: viết `class Bo extends DongVat` là xong. **Vòng lặp này không sửa một ký tự nào.**

```
   Thêm loại mới
        │
        ├─ Không đa hình → sửa MỌI chỗ if-else trên toàn dự án
        └─ Có đa hình    → chỉ thêm một class mới, không đụng code cũ
```

> 📌 Đây là **nguyên tắc Open/Closed**: code nên **mở để mở rộng** (thêm loại mới) nhưng **đóng với sửa đổi** (không phải sửa code đang chạy tốt). Đa hình là công cụ chính để đạt được điều đó, và là lý do interface ([bài 2.3](03-interface-va-abstract-class.md)) có giá trị.

---

## 2. Cơ chế: **late binding** — JVM tra bảng lúc chạy

Nếu bytecode được sinh lúc compile, mà lúc đó `javac` **không biết** biến sẽ trỏ vào object nào — thì làm sao gọi đúng bản?

Soi bytecode của `x.dong()` với `ChaTest x = new ConTest();`:

```
invokevirtual #37    // Method ChaTest.dong:()V
```

Bytecode ghi rõ **`ChaTest.dong`**. Nhưng khi chạy, kết quả in ra `[instance] CON`.

**Vì sao?** Vì `invokevirtual` không có nghĩa *"gọi đúng method này"*. Nó có nghĩa *"gọi method có chữ ký này, trên object đang nằm trên stack"*. JVM làm thêm một bước:

```
   Bytecode:  invokevirtual ChaTest.dong
                        │
                        │  JVM lúc CHẠY:
                        ├─ ① nhìn object thật trên Heap  →  là ConTest
                        ├─ ② tra bảng method của ConTest
                        └─ ③ gọi bản của ConTest           →  in "[instance] CON"
```

Bảng đó gọi là **vtable** (virtual method table) — mỗi class có một bảng ánh xạ "chữ ký method → địa chỉ code thật". Cơ chế tra bảng lúc chạy này tên là **late binding** (ràng buộc muộn) hay **dynamic dispatch**.

Đối lập với nó là **early binding** — quyết định xong ngay lúc compile, không tra lại.

---

## 3. 🚨 **Chỉ instance method** mới đa hình

Phần mà rất nhiều người học OOP vài năm vẫn tưởng nhầm. Thực nghiệm với `ChaTest x = new ConTest();`:

```
x.nhan            = CHA      ← field:           theo KIỂU BIẾN
x.tinh()          → CHA      ← static method:   theo KIỂU BIẾN
x.dong()          → CON      ← instance method: theo OBJECT THẬT  ✅
```

Bytecode giải thích chính xác vì sao:

| Thành phần | Lệnh bytecode | Chốt lúc nào |
|---|---|---|
| Field `nhan` | `getfield ChaTest.nhan` | **compile-time** — tên class bị khóa cứng |
| `static` method | `invokestatic ChaTest.tinh` | **compile-time** — chạy đúng cái ghi, không tra |
| Instance method | `invokevirtual ...` | **runtime** — JVM tra vtable |

Và sau khi ép kiểu, bytecode đổi hẳn mục tiêu:

```
getfield #24   // Field ChaTest.nhan      ← x.nhan
getfield #42   // Field ConTest.nhan      ← ((ConTest) x).nhan
```

Compiler chọn field **ngay lúc biên dịch**, dựa vào kiểu nó nhìn thấy.

> 📌 **Quy tắc:** đa hình **chỉ** áp dụng cho **instance method**. Field và `static` method luôn theo **kiểu khai báo của biến**.

### Hai hệ quả thực tế

**1. Đừng khai field trùng tên ở class con.** Hiện tượng đó gọi là *field hiding*. Nó không phải override — nó tạo ra **hai field cùng tên** trong một object, và bạn nhận được cái nào là tùy kiểu biến. Gần như không có lý do chính đáng để làm.

**2. `static` method không override được.** Viết method `static` cùng tên ở class con chỉ là *method hiding* — nó **che** method của cha khi gọi qua kiểu con, chứ không thay thế. Không có late binding, nên không có đa hình.

---

## 4. Upcasting và downcasting

```java
DongVat d = new Cho("Milu");      // UPCASTING   — tự động, luôn an toàn
Cho c = (Cho) d;                   // DOWNCASTING — phải ép tay, có thể sai
```

| | Hướng | Cần ép tay? | An toàn? |
|---|---|---|---|
| **Upcasting** | con → cha | ❌ Tự động | ✅ Luôn đúng |
| **Downcasting** | cha → con | ✅ Bắt buộc | ⚠️ Có thể nổ |

**Vì sao bất đối xứng?** Vì *"mọi `Cho` đều là `DongVat`"* luôn đúng. Nhưng *"mọi `DongVat` đều là `Cho`"* thì sai — có thể nó là mèo.

### `ClassCastException`

```java
DongVat meo = new Meo("Mun");
Cho epSai = (Cho) meo;
```
```
Exception in thread "main" java.lang.ClassCastException:
    class Meo cannot be cast to class Cho (Meo and Cho are in unnamed module of loader 'app')
```

> 🔗 Thêm một ngoại lệ runtime vào bộ sưu tập: `NullPointerException` ([bài 1.5](../01-java-core/05-wrapper-autoboxing-ep-kieu.md)), `ArithmeticException` ([bài 1.6](../01-java-core/06-toan-tu-va-luong-dieu-khien.md)), `ArrayIndexOutOfBoundsException` ([bài 1.7](../01-java-core/07-mang-va-method.md)), giờ là `ClassCastException`.

---

## 5. 🔑 Vì sao ép kiểu sai là **R** mà gọi method lạ là **C**?

Đây là điểm tinh tế nhất của bài. Hai dòng dưới đây cùng dùng một biến kiểu cha, nhưng rơi vào hai cửa kiểm soát khác nhau:

| | `d.giuNha()` | `(Cho) meo` |
|---|---|---|
| `javac` nhìn thấy | Kiểu `DongVat` **không hề có** method `giuNha` | Một `DongVat` **có thể** là `Cho` |
| Nó kết luận được gì | **Chắc chắn sai**, không ngoại lệ nào | **Chưa chắc** — tùy object thật |
| Hành động | ❌ Chặn ngay (**C**) | ✅ Cho qua, để JVM kiểm tra (**R**) |

```
error: cannot find symbol
  symbol:   method giuNha()
  location: variable d of type DongVat
```

> 📌 Nguyên tắc chung, khép lại [bài 1.2](../01-java-core/02-compile-time-vs-runtime.md): **`javac` chỉ chặn những gì nó chứng minh được là sai.** Thứ gì *có thể* đúng thì nó cho qua và đẩy trách nhiệm xuống runtime.
>
> Ranh giới giữa hai cửa kiểm soát **không phải** "lỗi nặng thì compile-time, lỗi nhẹ thì runtime", mà là **"chứng minh được thì chặn, không chứng minh được thì để chạy rồi tính"**.

---

## 6. `instanceof` + pattern matching (Java 16+)

Cách cũ phải viết tên kiểu **ba lần**:

```java
if (d instanceof Cho) {
    Cho cho = (Cho) d;        // ép lại thủ công
    cho.giuNha();
}
```

Cách mới gộp kiểm tra và ép kiểu vào một dòng:

```java
if (d instanceof Cho cho) {   // nếu đúng → biến `cho` tự có, đã ép sẵn
    cho.giuNha();
}
```

Biến `cho` chỉ tồn tại trong nhánh điều kiện đúng — không thể vô tình dùng nhầm, không còn nguy cơ ép sai kiểu.

> ⚠️ **Nhưng dùng nhiều `instanceof` là dấu hiệu thiết kế kém.** Nếu liên tục phải hỏi *"nó là loại gì?"* để quyết định làm gì, thì hành vi đó nên nằm **trong chính class đó** dưới dạng một method override. Đó mới là tinh thần của mục 1.

---

## 7. 🔑 Đa hình trong Spring

Nhớ [bài 2.3](03-interface-va-abstract-class.md) mục 8:

```java
class UserService {
    private UserRepository repo;       // kiểu interface
}
```

Lúc chạy, Spring tiêm vào một `UserRepositoryJpa` — đó chính là **upcasting**. Khi bạn gọi `repo.findById(1L)`, **late binding** tra vtable của object thật rồi chạy bản JPA.

Toàn bộ Spring đứng trên cơ chế này. Không có đa hình thì không có Dependency Injection, không có mock khi test, không đổi được database.

> 💡 [Bài 2.2](02-ke-thua-super-override.md) có nhắc bẫy *"Spring và Hibernate kế thừa class của bạn lúc chạy"* — chúng sinh ra một class con (**proxy**) để chèn thêm logic (transaction, cache, log). Bạn vẫn gọi method như thường và **không hề biết** mình đang nói chuyện với proxy. Thứ khiến điều đó trong suốt chính là late binding.

---

## 8. Kết quả thực nghiệm

```
--- Vong lap khong he biet con nao la con gi ---
  Milu: Gau gau
  Milu dang giu nha          ← chỉ chó mới có
  Mun: Meo meo
  Vang: Gau gau
  Vang dang giu nha

--- Cai gi da hinh, cai gi khong? ---
  x.nhan = CHA               ← field:           theo kiểu biến
  [static] CHA               ← static method:   theo kiểu biến
  [instance] CON             ← instance method: theo object thật ✅

--- Downcasting sai ---
Exception in thread "main" java.lang.ClassCastException:
    class Meo cannot be cast to class Cho
```

Bỏ `instanceof` và gọi thẳng `d.giuNha()`:

```
error: cannot find symbol
  symbol:   method giuNha()
  location: variable d of type DongVat
```

---

## 9. Tự kiểm tra

1. Vì sao "tái sử dụng code" không phải lý do chính của kế thừa, mà đa hình mới là?
2. Bytecode ghi `invokevirtual ChaTest.dong`, nhưng chạy ra `[instance] CON`. Giải thích.
3. `invokevirtual` khác `invokestatic` ở điểm căn bản nào?
4. `x.nhan` cho `CHA` dù object thật là `ConTest`. Vì sao field không đa hình?
5. Vì sao upcasting không cần ép tay mà downcasting thì bắt buộc?
6. `d.giuNha()` là lỗi compile, còn `(Cho) meo` là lỗi runtime. Cùng là biến kiểu cha — vì sao hai cửa khác nhau?
7. Dùng nhiều `instanceof` trong code báo hiệu điều gì?
8. Spring tiêm `UserRepositoryJpa` vào biến kiểu `UserRepository`. Cơ chế nào khiến `repo.findById()` chạy đúng bản JPA?

<details>
<summary>Đáp án</summary>

1. Vì tái sử dụng code có thể đạt được bằng **composition** (an toàn hơn). Đa hình mới là thứ chỉ kế thừa/interface làm được: viết code xử lý **một kiểu chung** rồi thêm loại mới mà **không sửa code cũ** (nguyên tắc Open/Closed).
2. `invokevirtual` nghĩa là *"gọi method có chữ ký này trên object đang ở trên stack"*. Lúc chạy, JVM nhìn object thật (`ConTest`), tra **vtable** của nó, rồi gọi bản của `ConTest` — gọi là **late binding**.
3. `invokestatic` chạy **đúng method ghi trong bytecode**, không tra lại (early binding). `invokevirtual` **tra vtable của object thật** lúc chạy (late binding).
4. Vì field dùng lệnh `getfield` và compiler **khóa cứng tên class** vào bytecode ngay lúc biên dịch, dựa trên **kiểu khai báo của biến**. Không có bước tra bảng lúc chạy.
5. Vì *"mọi Cho đều là DongVat"* luôn đúng nên upcasting an toàn tuyệt đối. Còn *"mọi DongVat đều là Cho"* thì sai, nên downcasting phải có chữ ký xác nhận của lập trình viên và vẫn có thể nổ `ClassCastException`.
6. `javac` **chứng minh được** kiểu `DongVat` không có `giuNha()` → chặn ngay (C). Còn `(Cho) meo` thì nó **không chứng minh được là sai** — một `DongVat` hoàn toàn *có thể* là `Cho` — nên cho qua và để JVM kiểm tra (R).
7. Dấu hiệu thiết kế kém: hành vi đang nằm **bên ngoài** class thay vì bên trong. Thay bằng một method được override ở mỗi class con.
8. **Late binding.** Biến khai kiểu interface (upcasting), nhưng JVM tra vtable của object thật (`UserRepositoryJpa`) lúc chạy.

</details>

---

⬅️ **Bài trước:** [2.3 — Interface và abstract class](03-interface-va-abstract-class.md)
➡️ **Bài tiếp:** 2.5 — `equals()`, `hashCode()` và `record`
