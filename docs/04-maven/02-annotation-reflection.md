# Bài 4.2 — Annotation và Reflection

> **Mục tiêu:** Hiểu annotation chỉ là **nhãn dán** (tự nó không làm gì), reflection là **cửa sau** cho chương trình tự soi chính nó lúc chạy, và hai thứ ghép lại thành "phép màu" của Spring. Tự viết một validator kiểu `@NotBlank` để thấy cơ chế.
>
> **Code thực hành:** [`bai-tap/04-maven/AnnotationReflection.java`](../../bai-tap/04-maven/AnnotationReflection.java)
>
> Bài này **không cần Maven**, chỉ dùng `javac` + `java`.

---

## 1. Vấn đề: framework cần hiểu code mà nó chưa từng thấy

Ở Giai đoạn 8 bạn sẽ viết:

```java
public class DangKyRequest {
    @NotBlank
    private String ten;
}
```

Request gửi lên có `ten` rỗng thì Spring tự trả lỗi 400, dù **bạn không viết một dòng `if` nào**.

Spring được compile từ nhiều năm trước, khi class `DangKyRequest` của bạn **chưa tồn tại**. Vậy làm sao code của Spring biết class này có field `ten`, và field đó "không được rỗng"?

Phải có **hai mảnh ghép**:

| Mảnh | Vai trò | Ví von |
|---|---|---|
| **Annotation** | Bạn **dán nhãn** lên code: "field này không được rỗng" | Tờ giấy note dán lên hộp |
| **Reflection** | Lúc chạy, framework **mở class ra xem** có field nào, mỗi field dán nhãn gì | Người kiểm hàng đi đọc giấy note |

Thiếu một trong hai thì không có gì xảy ra.

**Nếu không có annotation thì sao?** Spring thời đầu (khoảng 2004) bắt khai mọi thứ trong **file XML riêng**: `<bean class="vn.hoc.UserService">`. Code ở một nơi, cấu hình ở một nơi khác. Đổi tên class mà quên sửa XML thì `javac` không biết, phải tới lúc chạy mới nổ. Annotation đưa cấu hình về **ngay trên dòng code mà nó mô tả**.

---

## 2. Annotation là gì

Bạn đã gặp một annotation rồi: `@Override` ở [bài 2.2](../02-oop/02-ke-thua-super-override.md).

**Annotation là dữ liệu (metadata) gắn lên code. Tự nó không làm gì cả.** Có tác dụng hay không là do **ai đọc nó**:

- `@Override`: người đọc là **`javac`**. Method không thật sự ghi đè thì báo lỗi compile.
- `@NotBlank`: người đọc là **code của framework**, lúc chương trình đang chạy.

### Khai báo và sử dụng

```java
@interface KhongDeTrong {                            // @interface = khai báo annotation
    String thongBao() default "khong duoc de trong";  // thuộc tính, có giá trị mặc định
}

@interface DoDaiToiDa {
    int value();                                     // 'value' là tên đặc biệt
}
```

```java
@KhongDeTrong(thongBao = "ten bat buoc")   // gán thuộc tính theo tên
@KhongDeTrong                              // không gán → dùng default
@DoDaiToiDa(5)                             // thuộc tính tên 'value' thì bỏ được "value ="
```

Thuộc tính được viết **giống method không thân**, vì lúc đọc ra bạn gọi đúng như method: `kdt.thongBao()`.

### 🪤 TypeScript decorator nhìn giống nhưng bản chất khác hẳn

| | TS decorator `@log` | Java annotation `@KhongDeTrong` |
|---|---|---|
| Bản chất | Một **hàm** | **Dữ liệu**, như một object chỉ đọc |
| Tự chạy? | ✅ Chạy khi class được định nghĩa | ❌ Không bao giờ |
| Sửa được class/method? | ✅ Bọc hoặc thay method được | ❌ Không |
| Muốn có tác dụng | Có sẵn, vì nó là code | Phải có **người khác đọc** nó |

Nếu bạn quen với việc "decorator tự làm việc", Java đi ngược hoàn toàn: **dán nhãn mà không ai đọc thì nhãn vô dụng.**

---

## 3. `@Retention`: annotation sống được bao lâu?

Annotation có thể bị vứt bỏ ở một trong ba chặng:

```
  AnnotationReflection.java ──javac──► NguoiDung.class ──JVM nạp──► Bộ nhớ lúc chạy
        (mã nguồn)                      (bytecode)                  (Class object)

  SOURCE  ●━━━━━━━━━━━━━━━━━━━━━━━━━┫ javac vứt bỏ
  CLASS   ●━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┫ có trong .class, reflection KHÔNG thấy
  RUNTIME ●━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━► đọc được lúc chạy
```

| `RetentionPolicy` | Sống tới | Dùng cho | Ví dụ |
|---|---|---|---|
| `SOURCE` | Chỉ trong mã nguồn | `javac`, công cụ chạy lúc compile | `@Override`, Lombok |
| `CLASS` **(mặc định)** | File `.class` | Công cụ xử lý bytecode | Ít gặp |
| `RUNTIME` | Lúc chạy | Framework đọc qua reflection | `@NotBlank`, `@Test`, `@Entity` |

> ⚠️ **Không ghi `@Retention` thì mặc định là `CLASS`, không phải `RUNTIME`.** Đây là nguồn gốc của bug ở mục 7.2.

**Vì sao không giữ hết tới lúc chạy?** Vì giữ thì tốn: thêm dữ liệu trong `.class`, thêm object trong bộ nhớ. `@Override` chỉ có ích cho `javac`, mang theo lúc chạy là lãng phí.

**`@Target`** quy định annotation được dán ở đâu: `FIELD`, `METHOD`, `TYPE` (class)... Dán sai chỗ thì `javac` báo lỗi.

---

## 4. Reflection: chương trình tự soi chính nó

### Trong JS, bạn dùng reflection hằng ngày

```javascript
const user = { ten: "An", tuoi: 20 };
Object.keys(user);      // ["ten", "tuoi"]  ← liệt kê field lúc chạy
user["ten"];            // đọc field bằng CHUỖI
user["chao"]?.();       // gọi method bằng CHUỖI
```

Object trong JS vốn là một **từ điển**, tên field là chuỗi và tra được bất cứ lúc nào.

Java thì khác: `javac` đã kiểm tra hết lúc compile ([bài 1.2](../01-java-core/02-compile-time-vs-runtime.md)). Viết `user.ten` thì `javac` xác nhận field tồn tại, đúng kiểu, được phép truy cập. Code thường **không viết được** `user[bienChuoi]`.

**Reflection là cửa sau** để làm việc đó: lấy field, method theo **tên dạng chuỗi**, **lúc chạy**.

### Mọi thứ bắt đầu từ `Class` object

[Bài 4.1](01-maven.md) cho thấy JVM **nạp class khi cần tới**. Khi nạp, JVM tạo trên Heap **đúng một** object kiểu `Class` để **mô tả** class đó.

```
        STACK                              HEAP
  ┌──────────────────┐        ┌───────────────────────────────┐
  │ nd   │  ●────────┼───────►│ NguoiDung object (dữ liệu)    │
  │      │           │        │   ten = "An", tuoi = 20       │
  │ lop  │  ●────────┼──┐     └──────────────┬────────────────┘
  └──────────────────┘  │                    │ "tôi thuộc class nào?"
                        │                    ▼
                        └────────►┌───────────────────────────────┐
     NguoiDung.class ────────────►│ Class object của NguoiDung    │  ← duy nhất
                                  │   tên: "NguoiDung"            │
                                  │   fields: ten, email, tuoi    │
                                  │   methods: biMat, toString    │
                                  │   annotation RUNTIME của field│
                                  └───────────────────────────────┘
```

| Muốn | Viết | Giống JS |
|---|---|---|
| Lấy `Class` từ object | `obj.getClass()` | `obj.constructor` |
| Lấy `Class` từ tên kiểu | `NguoiDung.class` | `NguoiDung` |
| Mọi field **khai báo trong class này** (kể cả private) | `lop.getDeclaredFields()` | `Object.keys(obj)` |
| Một field theo tên | `lop.getDeclaredField("ten")` | `obj["ten"]` |
| Xin tắt kiểm tra quyền truy cập | `field.setAccessible(true)` | (không có) |
| Đọc / ghi giá trị | `field.get(obj)` / `field.set(obj, v)` | `obj[k]` / `obj[k] = v` |
| Gọi method theo tên | `lop.getDeclaredMethod("x").invoke(obj)` | `obj["x"]()` |
| Đọc annotation | `field.getAnnotation(KhongDeTrong.class)` | (không có) |

`Class<?>` dùng wildcard của [bài 3.2](../03-collections/02-generics.md): "Class của một kiểu nào đó, không biết trước".

### Vì sao reflection tồn tại?

Spring, Jackson (object ↔ JSON), Hibernate (object ↔ bảng DB), JUnit đều phải làm việc với **class của bạn, thứ chưa tồn tại lúc chúng được viết ra**. Cách duy nhất là **hỏi lúc chạy**. Không có reflection thì không có framework Java hiện đại nào.

### Cái giá phải trả

| Giá | Hệ quả | Thấy ở |
|---|---|---|
| Mất sự bảo vệ của `javac` | Tên chỉ là chuỗi, gõ sai thì lỗi chuyển sang lúc chạy | Mục 7.3 |
| Xuyên thủng `private` | Tính đóng gói của [bài 2.1](../02-oop/01-class-va-object.md) không còn tuyệt đối | Mục 7.1 |
| Chậm hơn gọi trực tiếp | Phải tra cứu, kiểm tra quyền lúc chạy | Framework tra một lần lúc khởi động rồi lưu lại |

---

## 5. Ghép lại: tự viết một `@NotBlank`

```java
static List<String> kiemTra(Object obj) throws IllegalAccessException {
    List<String> loi = new ArrayList<>();
    Class<?> lop = obj.getClass();
    for (Field f : lop.getDeclaredFields()) {
        f.setAccessible(true);
        Object giaTri = f.get(obj);

        KhongDeTrong kdt = f.getAnnotation(KhongDeTrong.class);
        if (kdt != null && (giaTri == null || giaTri.toString().isBlank())) {
            loi.add(f.getName() + ": " + kdt.thongBao());
        }
        // ... tương tự cho DoDaiToiDa
    }
    return loi;
}
```

`kiemTra` nhận `Object`, nên **không biết gì về `NguoiDung`**. Đây đúng là vị trí của Spring: viết một lần, dùng cho mọi class.

```
   kiemTra(nd2)
       │
       ▼
   nd2.getClass() ──► Class object của NguoiDung
       │
       ▼  với từng field: ten, email, tuoi
   ┌──────────────────────────────────────────────┐
   │ setAccessible(true) → get(nd2) → giá trị      │
   │ getAnnotation(KhongDeTrong.class) → null?     │
   │   không null + giá trị rỗng → thêm lỗi        │
   └──────────────────────────────────────────────┘
       │
       ▼
   [ten: ten bat buoc, ...]
```

Các method reflection ném **checked exception** (`IllegalAccessException`, `NoSuchMethodException`...), nên phải `throws` hoặc `catch` ([bài 3.3](../03-collections/03-exception.md)).

---

## 6. Kết quả thực nghiệm

Chạy trên JDK 19:

```
> javac AnnotationReflection.java
> dir *.class
AnnotationReflection.class   DoDaiToiDa.class   KhongDeTrong.class   NguoiDung.class

> java AnnotationReflection
--- 1. Class object ---
NguoiDung
true
--- 2. Liet ke field ---
String ten
String email
int tuoi
--- 3. Pha cua private ---
NguoiDung[ten=Hacker]
mat khau 123
--- 4. Validator tu che ---
[ten: ten bat buoc]
--- 5. Goi method bang ten ---
Exception in thread "main" java.lang.NoSuchMethodException: NguoiDung.biMatCu()
        at java.base/java.lang.Class.getDeclaredMethod(Class.java:2728)
        at AnnotationReflection.main(AnnotationReflection.java:96)
```

- **4 file `.class` từ 1 file nguồn.** `javac` sinh `.class` theo **từng kiểu**, không theo file. Annotation cũng là một kiểu (thực chất là interface đặc biệt), nên cũng có `.class` riêng.
- **`lop == NguoiDung.class` là `true`.** Mỗi class chỉ có **một** `Class` object, nên so địa chỉ bằng `==` là đúng ở đây ([bài 1.4](../01-java-core/04-string-va-kieu-tham-chieu.md)).
- **Tên field còn nguyên lúc chạy.** Bytecode Java không bị minify ([bài 4.1](01-maven.md) mục 10.1): tên field, method, class đều nằm trong `.class`. Đó là thứ reflection đọc. `tuoi` không có annotation vẫn được liệt kê, vì `getDeclaredFields()` lấy **mọi** field.

---

## 7. Mổ xẻ ba điều bất ngờ

### 7.1. `private` không chặn được reflection

Viết thẳng thì `javac` chặn:

```
ThuTruyCap.java:5: error: ten has private access in NguoiDung
        System.out.println(nd.ten);
                             ^
```

Qua reflection thì `javac` chỉ thấy một **chuỗi** `"ten"` truyền vào method `getDeclaredField`. Nó không biết chuỗi đó là tên một field private. `setAccessible(true)` yêu cầu JVM bỏ qua kiểm tra quyền, và với class **của bạn** thì JVM đồng ý.

> 📌 `private` là hàng rào chống **nhầm lẫn**, không phải hàng rào **bảo mật**. Đừng tin rằng field private thì không ai đọc được. Mật khẩu để trong field private vẫn lộ qua reflection, qua heap dump, qua log.

**Nhưng với class của JDK thì khác** (từ Java 16, JDK đóng chặt các module của nó):

```java
Field f = String.class.getDeclaredField("value");
System.out.println("tim thay field: " + f.getName());   // in được
f.setAccessible(true);                                   // nổ
```
```
tim thay field: value
Exception in thread "main" java.lang.reflect.InaccessibleObjectException: Unable to make field private final byte[] java.lang.String.value accessible: module java.base does not "opens java.lang" to unnamed module @8efb846
```

**Cạm bẫy có thật:** nhiều dự án nâng từ Java 8/11 lên 17 thì các thư viện cũ (phiên bản cũ của Lombok, Gradle, một số thư viện serialize) nổ đúng exception này, vì chúng từng dùng reflection để chọc vào ruột JDK. Cách xử lý đúng là **nâng phiên bản thư viện**. Cách vá tạm là thêm cờ `--add-opens` khi chạy `java`.

### 7.2. 🚨 Quên `@Retention`: validator im lặng cho qua

`email = "an@rat-dai.com"` dài 14 ký tự, giới hạn là 5, nhưng **không có lỗi nào được báo**. Khác biệt duy nhất giữa hai annotation:

```java
@Retention(RetentionPolicy.RUNTIME)   ← KhongDeTrong có dòng này
@Target(ElementType.FIELD)
@interface KhongDeTrong { ... }

@Target(ElementType.FIELD)            ← DoDaiToiDa không có → mặc định CLASS
@interface DoDaiToiDa { ... }
```

`javap -v -p NguoiDung.class` cho thấy cả hai **đều có trong file `.class`**, nhưng nằm ở hai mục khác nhau:

```
  private java.lang.String email;
    RuntimeVisibleAnnotations:
      0: #27()
        KhongDeTrong
    RuntimeInvisibleAnnotations:        ← JVM bỏ qua mục này khi nạp class
      0: #31(#32=I#33)
        DoDaiToiDa(
          value=5
```

Vì vậy `f.getAnnotation(DoDaiToiDa.class)` trả về `null`, và `kiemTra` hiểu là "field này không có ràng buộc độ dài".

Còn `@Override` (`SOURCE`) thì **không có mặt** trong `NguoiDung.class`: tìm chữ `Override` trong output của `javap` ra 0 kết quả.

Thêm một dòng `@Retention(RetentionPolicy.RUNTIME)` cho `DoDaiToiDa`:

```
--- 4. Validator tu che ---
[ten: ten bat buoc, email: dai qua 5 ky tu]
```

**Vì sao đây là loại bug tệ nhất:** không lỗi compile, không exception, không warning. Validator **trông như** đang chạy, test với dữ liệu hợp lệ vẫn xanh. Dữ liệu sai đi thẳng vào database và chỉ lộ ra khi đã có hàng nghìn bản ghi hỏng. Đây là cùng một gốc rễ với nhiều bug "annotation không có tác dụng" trong Spring: **nhãn có đó, nhưng người cần đọc không đọc được.**

### 7.3. Gọi method bằng tên: lỗi rơi xuống cửa 2

`biMatCu` không tồn tại mà `javac` vẫn compile được, và mục 1–4 vẫn in đầy đủ trước khi nổ. Soi bytecode của `main`:

```
     228: ldc           #153                // String biMatCu
     234: invokevirtual #128                // Method java/lang/Class.getDeclaredMethod:(Ljava/lang/String;...)
```

Đối với `javac`, `"biMatCu"` chỉ là một **chuỗi**, giống mọi chuỗi khác. Không có gì để kiểm tra. Method chỉ được tìm khi dòng 96 **thực sự chạy**, giống cách JVM tìm class ở [bài 4.1](01-maven.md) mục 10.2. Exception bay lên khỏi `main`, nên dòng `tim thay: ...` không bao giờ được in ([bài 3.3](../03-collections/03-exception.md), stack unwinding).

**Cạm bẫy có thật:** đổi tên method bằng chức năng Rename của IDE. IDE sửa mọi lời gọi **trực tiếp**, nhưng không sửa được chuỗi `"biMat"` nằm trong code reflection, file cấu hình hay câu query. Build xanh, deploy xong, chức năng đó nổ `NoSuchMethodException`. Vì vậy framework hiện đại ưu tiên annotation (gắn **trên** chính method, đổi tên vẫn đi theo) hơn tên dạng chuỗi.

---

## 8. Cạm bẫy production

| Cạm bẫy | Hệ quả | Cách tránh |
|---|---|---|
| Tự viết annotation mà quên `@Retention(RUNTIME)` | Framework/validator **im lặng bỏ qua**, không báo lỗi gì | Luôn ghi `@Retention` rõ ràng cho annotation dùng lúc chạy |
| Tên method/field dạng chuỗi trong reflection | Đổi tên không ai báo, nổ `NoSuchMethodException` lúc chạy | Hạn chế dùng chuỗi, ưu tiên annotation; có test chạy qua đoạn đó |
| Tin rằng `private` là bảo mật | Dữ liệu nhạy cảm vẫn đọc được qua reflection, heap dump | Không giữ bí mật lâu trong object, không log object chứa bí mật |
| Thư viện cũ dùng reflection vào JDK | Nâng lên Java 17 thì nổ `InaccessibleObjectException` | Nâng phiên bản thư viện trước khi nâng JDK |
| `@Transactional` gọi từ **chính class đó** | Spring đọc annotation qua một lớp bọc (proxy); gọi nội bộ thì đi vòng qua lớp bọc, annotation **bị bỏ qua**, không rollback | Học kỹ ở GĐ 5 (proxy) và GĐ 7 (transaction); bẫy rollback khác đã gặp ở [bài 3.3](../03-collections/03-exception.md) |

Điểm chung của cả bảng: **annotation không tự làm gì cả**. Mỗi khi "gắn annotation mà không có tác dụng", hãy hỏi: *ai là người đọc nhãn này, và lúc đó họ có nhìn thấy nó không?*

---

## 9. Tự kiểm tra

1. Annotation Java khác TypeScript decorator ở điểm cốt lõi nào?
2. Một file `.java` có 1 class public, 1 class thường và 2 annotation. `javac` sinh ra bao nhiêu file `.class`? Vì sao?
3. Vì sao `lop == NguoiDung.class` cho `true`, trong khi bài 1.4 dặn không dùng `==` cho object?
4. Vì sao `javac` chặn `nd.ten` nhưng không chặn `getDeclaredField("ten")` + `setAccessible(true)`?
5. Validator không bắt lỗi email quá dài. Nguyên nhân là gì, và `javap` cho thấy điều gì?
6. Vì sao `getDeclaredMethod("biMatCu")` compile được, và tại sao mục 1–4 vẫn in ra trước khi nổ?

<details>
<summary>Đáp án</summary>

1. Decorator là **hàm**, tự chạy khi class được định nghĩa và có thể sửa class. Annotation là **dữ liệu**, không bao giờ tự chạy. Phải có người đọc nó (`javac`, hoặc framework qua reflection) thì nó mới có tác dụng.
2. 4 file. `javac` sinh `.class` theo **từng kiểu**, không theo file nguồn. Annotation cũng là một kiểu nên có `.class` riêng.
3. Vì mỗi class chỉ có **đúng một** `Class` object trên Heap. `getClass()` và `NguoiDung.class` cùng trỏ tới object đó, nên so địa chỉ cho `true`. Quy tắc "dùng `equals()`" áp dụng khi có thể tồn tại nhiều object cùng nội dung, còn ở đây chỉ có một object.
4. Với `nd.ten`, `javac` thấy một truy cập field và kiểm tra quyền. Với reflection, `javac` chỉ thấy một **chuỗi** truyền vào method, nên không có gì để kiểm tra. Lúc chạy, `setAccessible(true)` xin JVM bỏ kiểm tra quyền, và JVM đồng ý với class của bạn (nhưng từ chối với class nội bộ của JDK).
5. `DoDaiToiDa` thiếu `@Retention`, nên mặc định là `CLASS`. `javap` cho thấy nó vẫn có trong `.class` nhưng ở mục `RuntimeInvisibleAnnotations`, mục mà JVM bỏ qua khi nạp. `getAnnotation(DoDaiToiDa.class)` trả `null`, nên validator coi như field không có ràng buộc, và không báo lỗi gì.
6. Vì với `javac`, `"biMatCu"` chỉ là một chuỗi (bytecode chỉ có lệnh `ldc` nạp chuỗi). Method chỉ được tìm khi dòng đó thực sự chạy, nên lỗi thuộc **cửa 2**. Các dòng trước đó đã chạy xong; exception bay ra khỏi `main` nên dòng sau không chạy.

</details>

---

⬅️ **Bài trước:** [4.1 — Maven](01-maven.md)
➡️ **Bài tiếp:** 4.3 — Tự viết mini container: annotation + reflection tạo object và tiêm phụ thuộc
