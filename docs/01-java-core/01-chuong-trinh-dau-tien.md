# Bài 1.1 — Chương trình Java đầu tiên

> **Mục tiêu:** Không học vẹt `public static void main(String[] args)`. Hiểu **từng từ** trong đó vì sao phải có mặt.
>
> **Code thực hành:** [`bai-tap/01-java-core/Hello.java`](../../bai-tap/01-java-core/Hello.java)

---

## 1. Vì sao mọi dòng code phải nằm trong `class`?

Trong JavaScript, hàm **đứng độc lập** được:

```javascript
// JavaScript — hàm tự do, không thuộc về ai
function main() {
    console.log("Hello");
}
```

Java **không có khái niệm đó**. Không tồn tại "hàm tự do" (free function). Mọi method **buộc phải thuộc về một class**:

```java
// Java — method phải nằm trong class
public class Hello {
    public static void main(String[] args) {
        System.out.println("Hello");
    }
}
```

**Vì sao Java thiết kế vậy?** Java ra đời với triết lý *"everything is an object"* — class là **đơn vị đóng gói duy nhất**. Mọi thứ đều phải có "chủ sở hữu". Không có biến toàn cục lang thang, không có hàm mồ côi.

| | Ví dụ nhỏ | Project 2000 file |
|---|---|---|
| Java (bắt buộc class) | Dài dòng, thừa | ✅ Mọi thứ có địa chỉ rõ ràng |
| JS (hàm tự do) | Ngắn gọn, tiện | ⚠️ Dễ thành mớ hỗn độn, phải tự áp quy ước |

Java đánh đổi sự gọn gàng ở ví dụ nhỏ để lấy khả năng kiểm soát ở hệ thống lớn. Đây là lý do Java thống trị backend doanh nghiệp — và cũng là lý do nó bị chê "dài dòng".

> 💡 Sự dài dòng này **không vô nghĩa**. Ở Giai đoạn 5 (Spring IoC/DI), Spring lợi dụng chính cấu trúc class chặt chẽ này để tự động "biết" phải tạo và ghép nối cái gì với cái gì.

---

## 2. Quy tắc sắt: tên file **phải** trùng tên class `public`

```
Hello.java   ──phải chứa──►   public class Hello
   ▲                                       ▲
   └───────────── TRÙNG KHỚP ──────────────┘
```

Sai một chữ, kể cả **hoa/thường**, là lỗi compile ngay.

**Vì sao?** Vì `javac` và JVM tìm class **theo tên file**. Khi chạy `java Hello`, JVM đi tìm file `Hello.class` trong thư mục. Tên class bên trong không khớp tên file thì cơ chế tra cứu này sập.

> 📌 **Convention đặt tên class Java: `PascalCase`** — `Hello`, `UserService`, `OrderRepository`. Không phải yêu cầu của compiler mà là quy ước toàn cộng đồng, nhưng nên tuân theo tuyệt đối vì mọi thư viện Java (kể cả Spring) đều giả định như vậy.

---

## 3. Mổ xẻ `public static void main(String[] args)`

Dòng này là **entry point** — cửa vào chương trình. Khi bạn chạy `java Hello`, JVM không chạy toàn bộ class, nó **đi tìm đúng một method có chữ ký chính xác như thế này** để bắt đầu.

```
 public   static   void   main   (String[] args)
   │        │        │      │          │
   │        │        │      │          └─► Tham số: mảng String
   │        │        │      └────────────► Tên bắt buộc, JVM tìm đúng chữ này
   │        │        └───────────────────► Không trả về gì
   │        └────────────────────────────► Thuộc class, không cần tạo object
   └─────────────────────────────────────► Ai cũng gọi được
```

### 3.1. `public` — phạm vi truy cập (access modifier)

Quy định **ai được phép gọi** method này. `public` = mọi nơi.

**Vì sao `main` buộc phải `public`?** Vì **JVM đứng ở ngoài class của bạn**. Nó là chương trình bên ngoài, cần gọi vào. Để `private` (chỉ trong class mới gọi được) thì JVM không nhìn thấy method này.

### 3.2. `static` — từ khóa quan trọng nhất, và khó nhất với người mới

Bình thường, muốn dùng method của một class phải **tạo object trước**:

```java
Hello h = new Hello();   // Bước 1: tạo object (thực thể) từ class
h.chao();                // Bước 2: gọi method trên object đó
```

Nhưng `main` là **điểm bắt đầu** — lúc đó *chưa có gì tồn tại cả*. Nếu `main` không `static`, JVM gặp bài toán **con gà và quả trứng**:

```
Muốn chạy main  →  phải có object Hello  →  phải chạy code để tạo object
       ▲                                              │
       └──────────────  nhưng code nào? ◄─────────────┘
```

`static` phá vỡ vòng lặp đó: **method thuộc về bản thân class, không thuộc về object nào**. JVM gọi được ngay qua `Hello.main(...)` mà không cần `new` gì cả.

**Cách hiểu `static`:**

| | Ví dụ | Truy cập qua |
|---|---|---|
| Thành viên **instance** (không static) | Chiều cao của *một con người cụ thể* | Object: `nguoiA.chieuCao` |
| Thành viên **static** | Số chân của *cả loài người* (= 2) | Class: `ConNguoi.SO_CHAN` |

Static là thuộc tính/hành vi của **cả loài**, không phải của từng cá thể. Dữ liệu static chỉ tồn tại **một bản duy nhất** trong bộ nhớ, dùng chung cho mọi object.

> ⚠️ `static` sẽ quay lại làm khó ở Giai đoạn 2. Tạm ghi nhớ: **static = không cần `new` object vẫn gọi được**.

### 3.3. `void` — kiểu trả về (return type)

`void` nghĩa là **không trả về gì**.

Java bắt buộc **mọi method phải khai báo kiểu trả về** — khác biệt lớn với JS. Không trả gì thì phải ghi rõ `void`, không được bỏ trống:

```java
void        inRaManHinh()   { ... }   // không trả gì
int         tinhTong()      { ... }   // trả về số nguyên
String      layTen()        { ... }   // trả về chuỗi
```

**Vì sao `main` là `void`?** Vì trả về **cho ai**? Người gọi `main` là JVM, và JVM không dùng giá trị đó. (Muốn báo trạng thái thoát cho hệ điều hành thì dùng `System.exit(0)`, không qua `return`.)

### 3.4. `main` — tên bắt buộc

`main` **không phải từ khóa** của Java (vẫn được đặt tên biến là `main`). Nó là **quy ước cứng của JVM**: JVM chỉ tìm method tên đúng là `main`.

Đổi thành `Main`, `start`, hay `run` thì **compile vẫn thành công**, nhưng lúc chạy báo lỗi không tìm thấy entry point. Đây là ví dụ đầu tiên về khác biệt giữa **lỗi compile-time** và **lỗi runtime**.

### 3.5. `String[] args` — tham số dòng lệnh

| Phần | Nghĩa |
|---|---|
| `String` | Kiểu dữ liệu chuỗi |
| `[]` | Đánh dấu đây là **mảng** (array) |
| `args` | Tên biến — **đổi được tùy ý** (viết tắt của *arguments*) |

Đây là nơi chương trình nhận **tham số truyền từ dòng lệnh**:

```powershell
java Hello alpha beta
```

Khi đó `args` chứa `["alpha", "beta"]`.

**Vì sao phải khai báo dù không dùng?** Vì JVM gọi `main` theo **đúng chữ ký** `main(String[])`. Bỏ tham số đi thì chữ ký khác, JVM không tìm thấy `main`.

> 🔬 **Đã thử nghiệm:** chạy `java Hello alpha beta` với code ở bài này thì output **không đổi** — vì chương trình nhận `args` nhưng chưa dùng tới. Việc đọc `args` ra sẽ làm ở bài sau.

---

## 4. Mổ xẻ `System.out.println("...")`

Dòng này gồm 3 tầng, không phải một lệnh nguyên khối:

```java
System  .  out  .  println("Hello")
   │         │         │
   │         │         └─► METHOD của PrintStream: in ra + xuống dòng
   │         └───────────► FIELD static của System, kiểu PrintStream
   └─────────────────────► CLASS có sẵn trong thư viện chuẩn (java.lang)
```

Dấu `.` là **toán tử truy cập thành viên**. Đọc cả chuỗi: *"lấy class `System` → lấy field `out` của nó → gọi method `println` trên field đó"*.

| Thành phần | Là gì |
|---|---|
| `System` | Class có sẵn trong package `java.lang`, chứa các thứ liên quan hệ thống |
| `out` | Field `static` kiểu `PrintStream`, đại diện **standard output** (màn hình console) |
| `println` | Method của `PrintStream` = **print** + **line** (in rồi xuống dòng) |

So sánh với `print`:

```java
System.out.print("A");     System.out.print("B");     // → AB
System.out.println("A");   System.out.println("B");   // → A
                                                      //   B
```

> 💡 Vì sao `System` dùng được luôn mà không cần `import`? Vì package `java.lang` được Java **import tự động** cho mọi file. Các class khác (`Scanner`, `List`…) thì phải `import` — sẽ gặp ở Giai đoạn 3.

---

## 5. Dấu `;` — bắt buộc

Java **bắt buộc** mọi câu lệnh kết thúc bằng dấu chấm phẩy. Không có cơ chế tự thêm dấu như JavaScript (ASI — Automatic Semicolon Insertion). Thiếu dấu là **lỗi compile**, không phải cảnh báo.

Đây thực ra là **điểm tốt**: trong JS, ASI là nguồn của những bug rất khó tìm. Java loại bỏ hoàn toàn nhóm lỗi đó.

---

## 6. Code thực hành

File: [`bai-tap/01-java-core/Hello.java`](../../bai-tap/01-java-core/Hello.java)

```java
public class Hello {

    public static void main(String[] args) {
        System.out.println("Hello, Java!");
        System.out.println("Toi dang hoc Backend.");
    }
}
```

**Lưu ý khi gõ:**

- Java **phân biệt hoa/thường** tuyệt đối. `System` khác `system`, `String` khác `string`.
- Thụt lề **4 space** (convention Java, khác 2 space của JS). Đã cấu hình sẵn trong [`.vscode/settings.json`](../../.vscode/settings.json).
- Dùng chữ **không dấu** trong chuỗi ở bài này — dấu tiếng Việt trên Windows console cần xử lý encoding riêng.
- Nhớ đủ **2 cặp ngoặc nhọn**: một cho `class`, một cho `main`.

---

## 7. Compile và chạy

```powershell
cd "bai-tap/01-java-core"
javac Hello.java
java Hello
```

**Kết quả:**

```
Hello, Java!
Toi dang hoc Backend.
```

### Ba điểm dễ sai

| Sai | Đúng | Vì sao |
|---|---|---|
| `java Hello.class` | `java Hello` | `java` nhận **tên class**, không phải tên file. Nó tự tìm `Hello.class` |
| `java Hello.java` | `javac Hello.java` rồi `java Hello` | *(xem ghi chú dưới)* |
| `javac hello.java` | `javac Hello.java` | Phân biệt hoa/thường |

Sau khi chạy `javac`, thư mục xuất hiện file **`Hello.class`** (455 bytes) — chính là **bytecode** mà [bài 0.1](../00-moi-truong/01-java-hoat-dong-nhu-the-nao.md) nói tới. Thử mở bằng VS Code: toàn ký tự lạ, vì nó không dành cho người đọc.

> `Hello.class` **không được commit** — đã nằm trong `.gitignore`, vì `javac` sinh lại được bất cứ lúc nào. Xem [bài 0.4 mục 6](../00-moi-truong/04-git-line-ending.md).

> 💡 **Ghi chú:** Java 11+ cho phép chạy trực tiếp `java Hello.java` — compile trong bộ nhớ rồi chạy luôn, không sinh file `.class`. Tiện, nhưng **ở giai đoạn này nên làm 2 bước riêng** để thực sự *nhìn thấy* file `.class` được sinh ra. VS Code cũng có nút ▶ Run, nhưng nó che mất cả quá trình — dùng terminal trước, hiểu rồi thì dùng nút cho nhanh.

---

## 8. Tự kiểm tra

1. Vì sao `main` phải là `public`?
2. Vì sao `main` phải là `static`? Bài toán con gà - quả trứng ở đây là gì?
3. `System.out.println` gồm mấy thành phần? Mỗi thành phần là class, field, hay method?
4. Đổi tên method `main` thành `Main` thì lỗi xảy ra lúc **compile** hay lúc **chạy**? Vì sao?

<details>
<summary>Đáp án</summary>

1. Vì **JVM đứng ngoài class của bạn** — nó là chương trình bên ngoài cần gọi vào. `private` thì JVM không truy cập được.
2. Vì `main` là điểm bắt đầu, lúc đó chưa có object nào tồn tại. Nếu không `static`, JVM phải tạo object `Hello` để gọi `main`, nhưng muốn tạo object thì phải có code chạy trước đã — vòng lặp không có điểm khởi đầu. `static` cho phép gọi trực tiếp qua class.
3. **Ba** thành phần: `System` là **class**, `out` là **field** static (kiểu `PrintStream`), `println` là **method** của `PrintStream`.
4. Lúc **chạy** (runtime). Vì `Main` là một tên method hợp lệ về mặt cú pháp nên `javac` không có gì để phàn nàn. Chỉ khi JVM đi tìm entry point tên `main` mà không thấy thì mới báo lỗi.

</details>

---

## 9. ➡️ Bước tiếp theo (chưa làm)

**Thí nghiệm phá vỡ chương trình.** Sẽ cố tình làm sai 6 cách, **đoán lỗi trước khi chạy**, rồi chạy để đối chiếu:

| # | Phá thế nào | Đoán: lỗi compile hay runtime? |
|---|---|---|
| 1 | Đổi `class Hello` thành `class Hi` (file vẫn tên `Hello.java`) | ? |
| 2 | Xóa từ khóa `static` khỏi `main` | ? |
| 3 | Đổi `main` thành `Main` | ? |
| 4 | Xóa tham số `String[] args` | ? |
| 5 | Đổi `public` thành `private` | ? |
| 6 | Xóa một dấu chấm phẩy | ? |

Mục đích: hiểu *vì sao* mỗi từ khóa phải có mặt, và phân biệt được **lỗi compile-time** (`javac` chặn) với **lỗi runtime** (JVM chặn) — khái niệm dùng suốt cả lộ trình.

---

⬅️ **Bài trước:** [0.4 — Git cho Java project: line ending](../00-moi-truong/04-git-line-ending.md)
