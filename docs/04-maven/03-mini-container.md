# Bài 4.3 — Tự viết mini container: annotation + reflection tạo object và tiêm phụ thuộc

> **Mục tiêu:** Hiểu Dependency Injection (DI) và Inversion of Control (IoC) bằng cách **tự viết** một container nhỏ khoảng 40 dòng: đọc nhãn `@ThanhPhan`, soi constructor bằng reflection, tạo object theo đúng thứ tự, mỗi class chỉ tạo một lần. Đây là phiên bản thu nhỏ của Spring IoC container ở Giai đoạn 5.
>
> **Code thực hành:** [`bai-tap/04-maven/MiniContainer.java`](../../bai-tap/04-maven/MiniContainer.java)
>
> Bài này **không cần Maven**, chỉ dùng `javac` + `java`.

---

## 1. Vấn đề: ai sẽ gọi `new`?

Backend thật có hàng trăm class phụ thuộc lẫn nhau. Ví dụ nhỏ của bài:

```
DonHangService ──cần──► KhoHang
      │
      └────────cần──► ThongBao (interface)
                          ▲
                ┌─────────┴─────────┐
          EmailThongBao        SmsThongBao
```

### Cách 1: class tự `new` thứ nó cần

```java
class DonHangService {
    private KhoHang kho = new KhoHang();
    private ThongBao thongBao = new EmailThongBao();   // ← dính chặt vào Email
}
```

| Vấn đề | Hệ quả |
|---|---|
| Muốn đổi sang SMS thì phải **sửa code** `DonHangService` | Thay đổi hạ tầng kéo theo sửa code nghiệp vụ |
| 10 service cùng `new KhoHang()` | 10 kho riêng. Nếu `KhoHang` giữ connection pool tới DB thì có 10 pool |
| Viết test (GĐ 10) không thay được `EmailThongBao` bằng bản giả | Mỗi lần chạy test là **gửi email thật** |

### Cách 2: Dependency Injection, tức nhận từ bên ngoài

```java
class DonHangService {
    private final KhoHang kho;
    private final ThongBao thongBao;

    DonHangService(KhoHang kho, ThongBao thongBao) {   // "đưa tôi thứ tôi cần"
        this.kho = kho;
        this.thongBao = thongBao;
    }
}
```

`DonHangService` chỉ **khai báo** nhu cầu qua constructor. Nó không biết `ThongBao` thật ra là Email hay SMS. Đó là lý do [bài 2.3](../02-oop/03-interface-va-abstract-class.md) gọi interface là trái tim của Spring, và [bài 2.4](../02-oop/04-da-hinh.md) giải thích vì sao gọi `thongBao.gui(...)` vẫn chạy đúng bản cài đặt.

Nhưng vẫn phải có **ai đó** gọi `new` theo đúng thứ tự:

```java
KhoHang kho = new KhoHang();
ThongBao tb = new EmailThongBao();
DonHangService sv = new DonHangService(kho, tb);
```

Với 3 class thì làm tay được. Với 300 class, đoạn lắp ráp này dài hàng nghìn dòng, phải tự sắp thứ tự (thứ được cần phải tạo trước), và thêm một tham số vào constructor là phải đi sửa mọi chỗ `new`.

### Inversion of Control: đảo quyền điều khiển

| | Không có container | Có container |
|---|---|---|
| Ai gọi `new`? | Code của bạn | Container |
| Ai quyết định thứ tự tạo? | Bạn tự sắp | Container suy ra từ constructor |
| Bạn làm gì? | Viết đoạn lắp ráp | Chỉ **dán nhãn** "class này do container quản lý" |

"Đảo ngược" nghĩa là: trước kia code của bạn điều khiển việc tạo object, giờ framework điều khiển, code của bạn chỉ khai báo.

### 🌉 So sánh với JS/TS

| JS/TS | Tương ứng trong Java |
|---|---|
| React: truyền props xuống component con | DI bằng tay: cha đưa thứ con cần |
| React Context (`useContext`) | Một dạng DI: con xin, cây bên trên cung cấp |
| Angular / NestJS: `constructor(private http: HttpClient)` + `@Injectable()` | **Gần như giống hệt** Spring. NestJS được thiết kế theo khuôn Spring |
| `import db from './db'` (module chỉ chạy một lần) | Singleton, nhưng **cứng**: muốn thay bằng bản giả phải nhờ công cụ mock module |

NestJS phải bật `emitDecoratorMetadata` để TypeScript **ghi lại** kiểu tham số constructor lúc build, vì kiểu TS bị xóa khi sang JS. Java không cần mẹo đó: **kiểu tham số còn nguyên trong `.class`**, reflection đọc thẳng (cùng lý do tên field còn nguyên ở [bài 4.2](02-annotation-reflection.md)).

---

## 2. Container làm đúng ba việc

```
lay(DonHangService.class)
   │
   ├─ ① TÌM:  class nào có nhãn @ThanhPhan dùng được cho kiểu này?
   │          (kiểu là interface ThongBao → tìm class implements nó)
   │
   ├─ ② ĐÃ CÓ CHƯA? Tra Map "daTao". Có rồi → trả lại luôn
   │
   └─ ③ TẠO:
          • Soi constructor → cần tham số kiểu gì?
          • Với TỪNG tham số → gọi lại chính lay(...)      ← ĐỆ QUY
          • Đủ tham số → newInstance(...) → cất vào Map
```

```java
<T> T lay(Class<T> kieu) throws Exception {
    Class<?> lopThat = timLopThat(kieu);                       // ①

    Object daCo = daTao.get(lopThat);                          // ②
    if (daCo != null) {
        return kieu.cast(daCo);
    }

    Constructor<?> hamTao = lopThat.getDeclaredConstructors()[0];   // ③
    Class<?>[] kieuThamSo = hamTao.getParameterTypes();
    Object[] thamSo = new Object[kieuThamSo.length];
    for (int i = 0; i < kieuThamSo.length; i++) {
        thamSo[i] = lay(kieuThamSo[i]);
    }

    Object moi = hamTao.newInstance(thamSo);
    daTao.put(lopThat, moi);
    return kieu.cast(moi);
}
```

Các API reflection mới, tiếp nối bảng ở [bài 4.2](02-annotation-reflection.md):

| Muốn | Viết | Ghi chú |
|---|---|---|
| Lấy các constructor | `lop.getDeclaredConstructors()` | `Constructor<?>[]` |
| Constructor cần tham số kiểu gì | `hamTao.getParameterTypes()` | `Class<?>[]`, đúng thứ tự trái → phải |
| Gọi constructor | `hamTao.newInstance(mangThamSo)` | Tương đương `new X(a, b)` |
| Class có dán nhãn không | `c.isAnnotationPresent(ThanhPhan.class)` | Thiếu `@Retention(RUNTIME)` thì luôn `false` (bẫy bài 4.2) |
| Biến kiểu A nhận được object kiểu B không | `A.isAssignableFrom(B)` | Hỏi: "`A x = new B()` có hợp lệ không?" |

⚠️ Chiều của `isAssignableFrom` rất hay bị đọc ngược. Đọc là: **"kiểu bên trái có nhận object của kiểu trong ngoặc không?"**

**`<T> T lay(Class<T> kieu)`** là generic method ở [bài 3.2](../03-collections/02-generics.md). Truyền `DonHangService.class` (kiểu `Class<DonHangService>`) thì `T = DonHangService`, kết quả gán thẳng vào biến `DonHangService` không cần ép. Map bên trong chỉ biết `Object`, nên `kieu.cast(...)` ép **có kiểm tra**. Kỹ thuật dùng `Class<T>` làm tham số như vậy gọi là **type token**; Spring dùng đúng nó: `context.getBean(DonHangService.class)`.

---

## 3. Đệ quy = đi theo đồ thị phụ thuộc từ dưới lên

`lay(DonHangService)` được gọi **đầu tiên**, nhưng `DonHangService` được tạo **cuối cùng**. Lý do: dòng `println("tao DonHangService")` nằm **trong constructor**, mà constructor chỉ chạy ở `newInstance`, sau khi vòng `for` đã lấy đủ tham số.

```
lay(DonHangService)                       ← bắt đầu, CHƯA tạo
 ├─ lay(KhoHang)
 │    └─ không cần gì → newInstance  ──►  "tao KhoHang"          (1)
 ├─ lay(ThongBao) → tìm ra EmailThongBao
 │    └─ không cần gì → newInstance  ──►  "tao EmailThongBao"    (2)
 └─ đủ 2 tham số → newInstance       ──►  "tao DonHangService"   (3)
```

Đây là **duyệt cây theo chiều sâu, xử lý con trước cha** (post-order). Kết quả trùng khít với thứ tự bạn tự viết ở mục 1: thứ tự đó không phải do bạn nghĩ ra, mà **bắt buộc** phải như vậy, và container suy ra được nó từ chữ ký constructor.

Ở mục 2 danh sách truyền `DonHangService` **đứng đầu**, cố ý để cho thấy: thứ tự trong danh sách **không** quyết định thứ tự tạo. Đồ thị phụ thuộc mới quyết định.

---

## 4. Singleton: mỗi class chỉ một object

```
        Container c                          HEAP
  ┌───────────────────────────┐
  │ daTao (HashMap)           │       ┌──────────────────────┐
  │  KhoHang.class        ●───┼──────►│ KhoHang object       │◄──┐
  │  EmailThongBao.class  ●───┼──┐    └──────────────────────┘   │ kho
  │  DonHangService.class ●───┼┐ │    ┌──────────────────────┐   │
  └───────────────────────────┘│ └───►│ EmailThongBao object │◄┐ │
                               │      └──────────────────────┘ │ │ thongBao
   sv  ●───┐                   │      ┌──────────────────────┐ │ │
   sv2 ●───┴───────────────────┴─────►│ DonHangService object├─┴─┘
                                      └──────────────────────┘
```

- `sv == sv2` là `true`: lần gọi thứ hai dừng ở bước ②, trả lại **cùng địa chỉ**, nên `==` (so địa chỉ, [bài 1.4](../01-java-core/04-string-va-kieu-tham-chieu.md)) cho `true`. Không có dòng `tao ...` nào in thêm.
- `sv.getKho() == c.lay(KhoHang.class)` cũng `true`: object `KhoHang` đã tiêm vào service chính là object nằm trong Map. Cả ứng dụng **dùng chung một kho**.

Key của Map là `Class` object. Dùng được làm key vì mỗi class chỉ có **một** `Class` object ([bài 4.2](02-annotation-reflection.md)), và `HashMap` cần key có `equals`/`hashCode` ổn định ([bài 2.5](../02-oop/05-equals-hashcode-record.md)).

Spring cũng mặc định như vậy: mỗi bean là **singleton** trong một container.

**Nó cắn bạn ở đâu?** Singleton nghĩa là mọi request HTTP cùng chạy trên **một object service duy nhất**, nhiều thread cùng lúc. Nếu service có field lưu trạng thái theo người dùng:

```java
@Service
class GioHangService {
    private String nguoiDungHienTai;   // ❌ mọi request dùng chung field này
}
```

thì request của người A ghi `nguoiDungHienTai = "A"`, request của B ghi đè thành `"B"`, rồi A đọc ra `"B"`. Đây là bug lộ dữ liệu người này sang người khác có thật. Nó không hiện ra khi test một mình trên máy, chỉ xuất hiện khi có tải đồng thời. Quy tắc: **service phải không có trạng thái (stateless)**, dữ liệu theo request truyền qua tham số. Thread sẽ học kỹ ở các giai đoạn sau.

---

## 5. Đổi Email sang SMS mà không sửa `DonHangService`

Mục 4 chỉ đổi danh sách đăng ký, `DonHangService` không đổi một chữ, kết quả in `[SMS]`. Bước ① gọi `ThongBao.class.isAssignableFrom(SmsThongBao.class)` → `true`.

Đây là lợi ích lớn nhất của DI: **chỗ dùng không biết chỗ cung cấp**. Trong dự án thật, cùng một code chạy với `EmailThongBao` ở production và với bản giả khi test.

### Nhập nhằng: hai class cùng khớp một interface

`timLopThat` trả về **cái khớp đầu tiên** trong danh sách. Đã kiểm chứng:

| Danh sách ở mục 4 | In ra |
|---|---|
| `..., SmsThongBao.class, EmailThongBao.class` | `[SMS] da dat giay` |
| `..., EmailThongBao.class, SmsThongBao.class` | `[EMAIL] da dat giay` |

Container **im lặng chọn** theo thứ tự, không báo gì. Trong dự án thật, thứ tự đó phụ thuộc thứ tự quét file, tên package... Ai đó thêm một class mới implements `ThongBao` là hành vi thay đổi mà không ai hay. Cùng loại bug "im lặng" với quên `@Retention` ở [bài 4.2](02-annotation-reflection.md).

Spring **từ chối đoán**. Chạy thử với `spring-context` 6.0 và đăng ký cả hai:

```
NoUniqueBeanDefinitionException: No qualifying bean of type 'ThongBao' available:
expected single matching bean but found 2: emailThongBao,smsThongBao
```

Bạn phải nói rõ muốn cái nào (`@Primary`, `@Qualifier`, học ở GĐ 5). Nổ ngay còn hơn chạy sai.

---

## 6. Quên đăng ký: object bị tạo dở

Mục 5 thiếu mọi class `ThongBao`, nhưng `tao KhoHang` **vẫn in ra** trước thông báo lỗi. Vòng `for` đi tham số từ trái sang phải `(KhoHang kho, ThongBao thongBao)`: `KhoHang` tạo xong và đã nằm trong Map, rồi tới `ThongBao` thì `timLopThat` ném exception. `DonHangService` không bao giờ được tạo.

Spring cho kết quả giống hệt, kể cả dòng `tao KhoHang`:

```
  tao KhoHang
UnsatisfiedDependencyException
goc: NoSuchBeanDefinitionException: No qualifying bean of type 'ThongBao' available:
expected at least 1 bean which qualifies as autowire candidate.
```

### Lazy vs eager: khi nào lỗi lộ ra?

| | Mini container | Spring |
|---|---|---|
| Tạo object lúc nào | Khi gọi `lay(...)` (**lazy**) | Tạo **hết** singleton ngay khi container khởi động (**eager**) |
| Thiếu bean thì lộ khi nào | Khi có code gọi tới, có thể là giữa giờ cao điểm | App **không khởi động được** |

Thử nghiệm với Spring: chưa gọi `getBean` lần nào nhưng ba dòng `tao ...` đã in ra trong lúc tạo context.

Eager nghe có vẻ phí, nhưng đó là thiết kế có chủ đích: **fail fast**. Deploy bản lỗi thì app chết ngay lúc khởi động, hệ thống deploy thấy và giữ lại bản cũ. Lỗi không nằm chờ tới lúc khách hàng bấm nút.

---

## 7. Gà và trứng: `StackOverflowError`

```
lay(Ga)     → Ga cần Trung   → lay(Trung)
lay(Trung)  → Trung cần Ga   → lay(Ga)
lay(Ga)     → bước ②: Ga có trong Map chưa? CHƯA (chỉ put sau newInstance)
            → lay(Trung) → lay(Ga) → ...   mãi mãi
```

Không ai được `newInstance` nên `tao Ga` hay `tao Trung` **không bao giờ in ra**. Mỗi lời gọi `lay` đặt thêm một frame lên Stack ([bài 3.3](../03-collections/03-exception.md)), cho tới khi hết chỗ:

```
         STACK của thread main (kích thước cố định)
  ┌──────────────────────────────┐  ◄── hết chỗ ở đây → StackOverflowError
  │ ...vài nghìn frame nữa...    │
  │ lay(Trung)   dòng 108        │
  │ lay(Ga)      dòng 108        │
  │ lay(Trung)   dòng 108        │
  │ lay(Ga)      dòng 108        │
  │ main                         │
  └──────────────────────────────┘
```

### Đọc stack trace

```
Exception in thread "main" java.lang.StackOverflowError
        at java.base/java.lang.reflect.Constructor.copy(Constructor.java:154)
        ...
        at java.base/java.lang.Class.getDeclaredConstructors(Class.java:2608)
        at Container.lay(MiniContainer.java:104)
        at Container.lay(MiniContainer.java:108)
        at Container.lay(MiniContainer.java:108)
        ...
```

- **Dòng `lay(...:108)` lặp lại** là dấu hiệu nhận diện đệ quy vô hạn. Dòng 108 là `thamSo[i] = lay(kieuThamSo[i]);`.
- **Dòng trên cùng là "nạn nhân", không phải thủ phạm.** Stack tràn đúng ở method nào đang cần thêm chỗ vào lúc đó. Chạy ba lần liên tiếp trên cùng máy, dòng đầu lúc là `Constructor.copy`, lúc là `ImmutableCollections$AbstractImmutableList.iterator` (vòng `for` trong `timLopThat`). Với `StackOverflowError`, đừng đọc dòng đầu, hãy **tìm đoạn lặp**.
- **Stack trace bị cắt ở 1024 dòng `at`** (đếm được đúng 1024). JVM không in hết, nên dòng `MiniContainer.main` ở đáy không xuất hiện.

### Tăng kích thước stack có cứu được không?

Đếm số lần gọi `lay` trước khi nổ (cờ `-Xss` đặt kích thước stack cho mỗi thread):

| Lệnh | Số lần gọi `lay` |
|---|---|
| `java -Xss256k MiniContainer` | ~1.300 |
| `java MiniContainer` (mặc định) | ~5.200 |
| `java -Xss4m MiniContainer` | ~20.000 |
| `java -Xss16m MiniContainer` | ~81.000 |

Stack lớn gấp 4 thì đi sâu được gấp ~4, nhưng **vẫn nổ**. Đệ quy vô hạn thì stack lớn tới đâu cũng không đủ. `-Xss` chỉ có ích cho đệ quy **hữu hạn nhưng sâu**.

### Sửa: phát hiện vòng lặp thay vì để tràn stack

Thêm một tập "đang tạo dở". Gặp lại một class đang nằm trong tập thì đó là vòng lặp:

```java
private final Set<Class<?>> dangTao = new LinkedHashSet<>();
...
if (!dangTao.add(lopThat)) {                 // add trả false nếu đã có
    throw new IllegalStateException("vong lap: " + dangTao + " -> " + lopThat.getSimpleName());
}
// ... tạo tham số, newInstance, put vào daTao ...
dangTao.remove(lopThat);
```

```
Exception in thread "main" java.lang.IllegalStateException: vong lap: [class Ga, class Trung] -> Ga
	at Container.lay(MiniContainer.java:108)
	at Container.lay(MiniContainer.java:115)
	at Container.lay(MiniContainer.java:115)
	at MiniContainer.main(MiniContainer.java:166)
```

Dùng `LinkedHashSet` thay vì `HashSet` vì nó **giữ thứ tự chèn** ([bài 3.1](../03-collections/01-chon-collection.md)), nên thông báo in đúng chuỗi `Ga → Trung → Ga`. Stack trace giờ chỉ còn 4 dòng và nói thẳng nguyên nhân.

Spring làm đúng việc này. Chạy thử `Ga`/`Trung` với `@Component`:

```
BeanCurrentlyInCreationException: Error creating bean with name 'ga':
Requested bean is currently in creation: Is there an unresolvable circular reference?
```

"Currently in creation" chính là tập `dangTao` của ta.

**Nó cắn bạn ở đâu?** Vòng lặp phụ thuộc giữa service rất hay gặp: `DonHangService` cần `KhachHangService` để lấy địa chỉ, rồi một ngày ai đó cho `KhachHangService` cần `DonHangService` để đếm số đơn. Với constructor injection thì vòng lặp **không thể giải**, vì không thể gọi constructor nào trước. App không khởi động. Đó thật ra là tín hiệu tốt: hai class đang ôm việc của nhau, nên tách phần dùng chung ra class thứ ba.

---

## 8. Kết quả thực nghiệm

Chạy trên JDK 19. Ở mục 4, người học sửa danh sách thành `SmsThongBao.class, EmailThongBao.class` để tự kiểm chứng câu hỏi nhập nhằng; output vẫn là `[SMS]` vì Sms đứng trước.

```
--- 1. Lap rap bang tay ---
  tao KhoHang
  tao EmailThongBao
  tao DonHangService
  [EMAIL] da dat ao
--- 2. Container tu lap rap ---
  tao KhoHang
  tao EmailThongBao
  tao DonHangService
  [EMAIL] da dat quan
--- 3. Lay them lan nua ---
true
true
--- 4. Doi Email sang SMS ---
  tao KhoHang
  tao SmsThongBao
  tao DonHangService
  [SMS] da dat giay
--- 5. Quen dang ky ---
  tao KhoHang
khong co thanh phan nao cho kieu ThongBao
--- 6. Ga va trung ---
Exception in thread "main" java.lang.StackOverflowError
        at java.base/java.lang.reflect.Constructor.copy(Constructor.java:154)
        at java.base/java.lang.reflect.ReflectAccess.copyConstructor(ReflectAccess.java:113)
        at java.base/jdk.internal.reflect.ReflectionFactory.copyConstructor(ReflectionFactory.java:290)
        at java.base/java.lang.Class.copyConstructors(Class.java:3692)
        at java.base/java.lang.Class.getDeclaredConstructors(Class.java:2608)
        at Container.lay(MiniContainer.java:104)
        at Container.lay(MiniContainer.java:108)
        at Container.lay(MiniContainer.java:108)
        ...
```

- **Mục 1 và mục 2 in ra giống hệt nhau.** Container tự suy ra đúng thứ tự mà ta phải tự nghĩ khi lắp bằng tay.
- **Mục 3 không có dòng `tao ...` nào.** Bước ② trả object cũ.
- **Mục 5 có `tao KhoHang`** trước lỗi: object tạo dở, giống hệt Spring.

Lỗi Spring trích ở mục 5–7 lấy từ việc chạy thật `spring-context` 6.0.23 với `AnnotationConfigApplicationContext`.

---

## 9. Mini container vs Spring

| Việc | Mini container | Spring (GĐ 5) |
|---|---|---|
| Tìm class | Danh sách truyền tay | **Component scan** quét cả package tìm `@Component`, `@Service`... |
| Nhãn | `@ThanhPhan` | `@Component` và các biến thể |
| Tiêm qua | Constructor đầu tiên | Constructor (khuyên dùng), field `@Autowired`, setter |
| Thời điểm tạo | Lazy | Eager khi khởi động |
| Nhiều class khớp | Im lặng chọn cái đầu | `NoUniqueBeanDefinitionException` |
| Vòng lặp | `StackOverflowError` (bản đầu) | `BeanCurrentlyInCreationException` |
| Bọc thêm hành vi | Không | Proxy cho `@Transactional`, `@Async`... |

Phần lõi, gồm đọc nhãn, soi constructor, đệ quy, và cache singleton, chính là thứ bạn vừa viết.

---

## 10. Cạm bẫy production

| Cạm bẫy | Hệ quả | Cách tránh |
|---|---|---|
| Service singleton giữ trạng thái theo người dùng trong field | Request của người này đọc được dữ liệu người khác khi có tải đồng thời | Service stateless, dữ liệu theo request đi qua tham số |
| Vòng lặp phụ thuộc giữa service | App không khởi động | Tách phần dùng chung ra class thứ ba |
| Hai bean cùng cài một interface | Spring nổ `NoUniqueBeanDefinitionException`; container tự chế thì im lặng chọn sai | Chỉ định rõ bằng `@Primary` / `@Qualifier` |
| Container lazy | Thiếu bean chỉ lộ khi code chạy tới, có thể giữa giờ cao điểm | Ưu tiên fail fast lúc khởi động |
| Tăng `-Xss` để "chữa" `StackOverflowError` | Chỉ hoãn lỗi, tốn thêm bộ nhớ cho **mọi** thread | Tìm đoạn lặp trong stack trace, sửa đệ quy vô hạn |
| Quên `@Retention(RUNTIME)` trên nhãn | `isAnnotationPresent` luôn `false`, container báo "không tìm thấy" dù class có dán nhãn | Luôn ghi `@Retention` ([bài 4.2](02-annotation-reflection.md)) |

---

## 11. Tự kiểm tra

1. DI và IoC khác nhau thế nào? Viết constructor nhận tham số đã là DI chưa, đã là IoC chưa?
2. `lay(DonHangService.class)` được gọi đầu tiên, vì sao `tao DonHangService` lại in cuối cùng?
3. Vì sao `sv == sv2` cho `true`? Điều đó gây ra rủi ro gì nếu service có field lưu tên người dùng hiện tại?
4. Ở mục 5, vì sao `tao KhoHang` vẫn in ra dù cuối cùng lỗi? Nếu là Spring thì lỗi này lộ ra lúc nào?
5. Vì sao `Ga`/`Trung` gây `StackOverflowError` dù có bước ② kiểm tra Map? Vì sao dòng đầu tiên của stack trace mỗi lần chạy một khác?
6. Đăng ký cả `EmailThongBao` lẫn `SmsThongBao`: mini container làm gì, Spring làm gì, cách nào tốt hơn?

<details>
<summary>Đáp án</summary>

1. **DI** là cách viết class: nhận phụ thuộc từ bên ngoài thay vì tự `new`. **IoC** là việc ai điều khiển quá trình tạo: chuyển từ code của bạn sang framework. Constructor nhận tham số là DI. Nếu bạn vẫn tự `new` lắp ráp trong `main` thì chưa có IoC; khi container làm việc đó thay bạn thì mới là IoC.
2. Dòng in nằm trong constructor, mà constructor chỉ chạy ở `newInstance`, sau khi vòng `for` đã đệ quy lấy đủ `KhoHang` và `ThongBao`. Đệ quy xử lý con trước cha (post-order).
3. Lần thứ hai dừng ở bước ②, trả về đúng object trong Map, cùng địa chỉ. Singleton nghĩa là mọi request dùng chung một object, nên field trạng thái bị các request ghi đè lẫn nhau, gây lộ dữ liệu giữa người dùng.
4. Tham số xử lý từ trái sang phải: `KhoHang` tạo xong trước, tới `ThongBao` mới ném exception. Spring tạo hết singleton lúc khởi động (eager), nên app không khởi động được: lỗi lộ ngay khi deploy.
5. Object chỉ được `put` vào Map **sau** `newInstance`, mà `newInstance` của `Ga` cần `Trung` trước, và ngược lại. Không ai tới được `newInstance`, Map luôn rỗng, đệ quy không dừng. Dòng đầu tiên là method đang chạy đúng lúc hết stack, nên thay đổi tùy lần; thủ phạm là đoạn `lay(...:108)` lặp lại.
6. Mini container im lặng chọn class đứng đầu danh sách. Spring ném `NoUniqueBeanDefinitionException` và bắt bạn chỉ định rõ. Spring tốt hơn: nổ lúc khởi động an toàn hơn chạy sai im lặng.

</details>

---

⬅️ **Bài trước:** [4.2 — Annotation và Reflection](02-annotation-reflection.md)
➡️ **Bài tiếp:** 5.1 — Spring IoC container: từ mini container sang `@Component` và `ApplicationContext` thật
