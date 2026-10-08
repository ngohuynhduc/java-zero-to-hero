# Bài 3.1 — Collections: chọn đúng cấu trúc dữ liệu

> **Mục tiêu:** Hiểu cấu trúc bên trong của `ArrayList`, `LinkedList`, `HashSet`, `HashMap`, `TreeMap` — và từ đó biết **khi nào dùng cái nào**, chọn sai thì chậm tới mức nào (đo thật, không kể suông).
>
> **Code thực hành:** [`bai-tap/03-collections/ChonCollection.java`](../../bai-tap/03-collections/ChonCollection.java)
>
> Bài mở đầu Giai đoạn 3.

---

## 1. Vì sao cần Collections?

[Bài 1.7](../01-java-core/07-mang-va-method.md) kết luận: mảng Java là **một khối bộ nhớ liền nhau, kích thước cố định vĩnh viễn**.

Nhưng backend xử lý dữ liệu co giãn liên tục: đơn hàng hôm nay, tập email đã đăng ký, bảng tra "mã sản phẩm → tồn kho". Không ai biết trước có bao nhiêu phần tử.

**Collections Framework** là bộ cấu trúc dữ liệu có sẵn trong `java.util` cho việc đó. Bạn đã dùng nó từ [bài 2.4](../02-oop/04-da-hinh.md) (`List.of(...)`) và [bài 2.5](../02-oop/05-equals-hashcode-record.md) (`HashSet`).

---

## 2. Bản đồ: ba **hợp đồng**, nhiều **cách cài đặt**

```
       «interface»            «interface»            «interface»
          List                    Set                    Map
   (có thứ tự, cho trùng)   (không cho trùng)    (khóa → giá trị)
           │                       │                      │
     ┌─────┴─────┐          ┌──────┼──────┐        ┌──────┼──────────┐
 ArrayList  LinkedList   HashSet  Linked  TreeSet HashMap Linked   TreeMap
                                  HashSet                 HashMap
```

Đây chính là cấu trúc ở [bài 2.3](../02-oop/03-interface-va-abstract-class.md): **một interface, nhiều class cài đặt**. `ArrayList` và `LinkedList` cùng thực hiện hợp đồng `List`, nhưng **bên trong hoàn toàn khác nhau**.

Cách khai báo chuẩn:

```java
List<Integer> ds = new ArrayList<>();       // ✅ biến kiểu INTERFACE
ArrayList<Integer> ds = new ArrayList<>();  // ⚠️ tự trói mình vào một cài đặt
```

Đúng nguyên tắc Dependency Inversion ở bài 2.3: muốn đổi sang `LinkedList` thì chỉ sửa **một chỗ** bên phải dấu `=`.

### Phần `<Integer>` là gì?

**Generic** — nói cho compiler biết danh sách chứa kiểu gì. Nhờ nó, `ds.add("abc")` bị chặn ngay ở **cửa 1**.

Phải viết `Integer` chứ không phải `int` vì [bài 1.5](../01-java-core/05-wrapper-autoboxing-ep-kieu.md) đã cho thấy `List<int>` báo `required: reference`. Collections chỉ chứa object. Generic có bài riêng trong giai đoạn này.

### So với JavaScript

| JavaScript | Java tương đương gần nhất | Khác ở đâu |
|---|---|---|
| `Array` | `ArrayList` | Gần như y hệt cơ chế bên trong |
| `Set` | `LinkedHashSet` | Set của JS **giữ thứ tự chèn**, `HashSet` của Java **không** |
| `Map` | `LinkedHashMap` | Tương tự — JS giữ thứ tự chèn |
| Object literal `{}` làm từ điển | `HashMap<String, ...>` | |

JS chỉ cho **một** cách cài đặt mỗi loại. Java cho nhiều cách — và bắt bạn **chọn**.

---

## 3. `ArrayList` — mảng biết tự nới rộng

Bên trong `ArrayList` là **một mảng bình thường** (đúng loại ở bài 1.7). Khi đầy, nó cấp mảng **lớn hơn** rồi copy toàn bộ sang:

```
   add() liên tục:
   [ 10 │ 20 │ 30 │ 40 ]              ← mảng cũ đã đầy
             │
             │  cấp mảng mới lớn hơn ~1.5 lần, copy sang
             ▼
   [ 10 │ 20 │ 30 │ 40 │ 50 │    ]    ← mảng mới, còn chỗ trống
```

**`get(i)` cực nhanh:** các ô liền nhau, địa chỉ phần tử thứ `i` tính bằng một phép nhân. Lấy phần tử thứ 5 hay thứ 50.000 tốn như nhau.

**Chèn vào đầu rất chậm:**

```
   add(0, 99):
   [ 10 │ 20 │ 30 │ 40 │    ]
      └────┴────┴────┴───► dịch TOÀN BỘ sang phải một ô
   [ 99 │ 10 │ 20 │ 30 │ 40 ]
```

---

## 4. `LinkedList` — chuỗi toa tàu

Không có mảng. Mỗi phần tử là một object riêng (**node**) nằm rải rác trên Heap, giữ địa chỉ node trước và node sau:

```
   head                                                        tail
    │                                                            │
    ▼                                                            ▼
  ┌──────┐      ┌──────┐      ┌──────┐      ┌──────┐
  │  10  │ ───► │  20  │ ───► │  30  │ ───► │  40  │
  │      │ ◄─── │      │ ◄─── │      │ ◄─── │      │
  └──────┘      └──────┘      └──────┘      └──────┘
```

**Chèn đầu cực nhanh:** tạo node mới, nối hai con trỏ, xong.

**`get(i)` rất chậm:** không có phép tính nào ra được địa chỉ node thứ `i`. Cách duy nhất là **đi bộ** theo con trỏ, đếm từng toa (từ đầu gần hơn — `head` hoặc `tail`).

### Big-O — cách nói của dân trong nghề

| Thao tác | `ArrayList` | `LinkedList` |
|---|---|---|
| `get(i)` | **O(1)** — như nhau bất kể kích thước | **O(n)** — tỉ lệ với số phần tử |
| Chèn/xóa ở đầu | **O(n)** — phải dịch toàn bộ | **O(1)** — chỉ nối con trỏ |
| Thêm vào cuối | O(1) (thỉnh thoảng nới mảng) | O(1) |

**O(1)** — "hằng số": 10 phần tử hay 10 triệu tốn như nhau. **O(n)** — "tuyến tính": gấp đôi dữ liệu thì gấp đôi thời gian.

> ⚠️ **O(n) nằm trong vòng lặp n lần thành O(n²)** — gấp đôi dữ liệu là **gấp bốn** thời gian.

---

## 5. `HashSet` / `HashMap` — đã biết bên trong rồi

[Bài 2.5](../02-oop/05-equals-hashcode-record.md) đã mổ xẻ cơ chế ngăn: `hashCode()` chọn ngăn, `equals()` chỉ so trong ngăn. Nên `contains()` của `HashSet` là **O(1)**. Còn `ArrayList.contains()` phải gọi `equals()` với từng phần tử — **O(n)**.

```
   "Email này đăng ký chưa?"  với 1 triệu email

   ArrayList.contains()  →  so tới 1.000.000 lần
   HashSet.contains()    →  1 lần hashCode + vài lần equals
```

Lỗi hiệu năng phổ biến nhất của người mới làm backend: dùng `List` để **kiểm tra tồn tại**. Chạy ngon với 100 dòng test, treo khi lên production.

---

## 6. Ba loại `Map` — khác nhau ở **thứ tự**

| Cài đặt | Duyệt ra theo thứ tự nào | Tốc độ `get`/`put` | Dùng khi |
|---|---|---|---|
| `HashMap` | **Không đảm bảo gì cả** | O(1) — nhanh nhất | Mặc định |
| `LinkedHashMap` | Thứ tự **chèn vào** | O(1) | Cần giữ thứ tự thêm (giống `Map` của JS) |
| `TreeMap` | **Sắp xếp** theo khóa | O(log n) | Cần duyệt theo thứ tự khóa |

`TreeMap` không dùng `hashCode()` — bên trong là một **cây** sắp xếp sẵn, tìm kiếm giống tra từ điển giấy: mở giữa, so sánh, chọn nửa trái hay phải. **O(log n)** — 1 triệu phần tử chỉ cần khoảng 20 lần so sánh.

> 🪤 **Đừng bao giờ viết code phụ thuộc vào thứ tự duyệt `HashMap`.** Thứ tự đó phụ thuộc `hashCode()` và kích thước bảng — có thể đổi khi thêm phần tử, khi nâng cấp JDK.

### Hai hành vi của `Map` cần nhớ

```java
diem.put("An", 7);
diem.put("An", 9);          // khóa đã có → GHI ĐÈ, không thêm mới
diem.get("Zed");            // khóa không có → null, KHÔNG ném lỗi
int d = diem.get("Zed");    // 💥 NPE — unboxing null (bài 1.5)
diem.getOrDefault("Zed", 0) // ✅ lối thoát an toàn
```

---

## 7. Kết quả thực nghiệm

```
--- 1. get(i) 100000 lan ---
ArrayList  : 3 ms
LinkedList : 3238 ms              ← ~1.000 lần

--- 2. Chen vao DAU 100000 lan ---
ArrayList  : 332 ms
LinkedList : 5 ms                 ← ~66 lần, theo chiều ngược lại

--- 3. contains 10000 lan ---
ArrayList  : 696 ms
HashSet    : 0 ms                 ← nhanh tới mức đồng hồ không đo kịp

--- 4. Thu tu cua Map ---
Thu tu them   : [Minh, An, Tuan, Binh, Lan, Cuong]
HashMap       : [Cuong, Minh, Lan, Tuan, An, Binh]    ← không quy luật
LinkedHashMap : [Minh, An, Tuan, Binh, Lan, Cuong]    ← thứ tự chèn
TreeMap       : [An, Binh, Cuong, Lan, Minh, Tuan]    ← alphabet

--- 5. put trung khoa ---
size = 1, An = 9

--- 6. Xoa trong luc duyet ---
Loi : java.util.ConcurrentModificationException
so sau do : [1, 3, 4]                                  ← đã bị sửa dở dang
```

### Vì sao `get(i)` của `LinkedList` chậm tới ~1.000 lần?

Không phải vì mỗi lần `get()` chậm hơn 1.000 lần, mà vì **O(n) trong vòng lặp n lần thành O(n²)**:

```
   get(0)      → đi 0 toa
   get(1)      → đi 1 toa
   ...
   get(49.999) → đi ~50.000 toa
```

Kể cả đi từ đầu gần hơn, cả vòng lặp vẫn là khoảng **2,5 tỷ bước**. `ArrayList` chỉ cần 100.000 phép nhân. Gấp đôi dữ liệu là gấp bốn thời gian: 200.000 phần tử ≈ 13 giây, 1 triệu phần tử hơn 5 phút.

---

## 8. 🚨 Hai cái bẫy khi sửa Collections

### Bẫy 1: `List.of(...)` là danh sách **bất biến**

```java
List<String> ds = List.of("a", "b");
ds.add("c");                 // 💥 UnsupportedOperationException
```

Compile sạch — kiểu `List` **có** `add()` trong hợp đồng. Nhưng object thật **từ chối** lúc chạy. Lại là ranh giới [bài 2.4](../02-oop/04-da-hinh.md): `javac` chỉ biết kiểu biến, không biết object thật có chịu làm không.

Muốn sửa được: `new ArrayList<>(List.of("a", "b"))`.

> Vì sao Java cố ý tạo danh sách bất biến? Đúng lý do ở [bài 1.7](../01-java-core/07-mang-va-method.md) mục 8: truyền `List` vào method là method đó **sửa được dữ liệu của bạn**. `List.of(...)` bảo đảm không ai sửa được.

### Bẫy 2: xóa phần tử **trong lúc đang duyệt**

```java
for (Integer x : so) {
    if (x % 2 == 0) so.remove(x);      // 💥
}
```

#### Cơ chế thật — Java **không** phát hiện lỗi lúc bạn xóa

`remove()` chạy **thành công hoàn toàn**. Lỗi được phát hiện ở **bước sau**, khi `for-each` định lấy phần tử tiếp theo.

`for-each` trên `List` thực chất chạy bằng một **iterator** giữ:

- `cursor`: vị trí phần tử **tiếp theo** sẽ lấy
- `expectedModCount`: số lần danh sách đã bị sửa, tính tới lúc bắt đầu duyệt

`ArrayList` tự đếm số lần bị sửa vào `modCount`. Mỗi vòng:

```
   hasNext():  cursor != size ?                → còn phần tử thì đi tiếp
   next():     modCount == expectedModCount ?  → khác nhau thì NÉM CME
               (rồi mới lấy phần tử)
```

#### 🔬 Thí nghiệm: lưới an toàn có lỗ thủng

```
  [1, 2, 3, 4] xoa 2 -> CME, ket qua [1, 3, 4]
  [1, 2, 3, 4] xoa 3 -> KHONG LOI, ket qua [1, 2, 4]
  [1, 2, 3, 4] xoa 4 -> CME, ket qua [1, 2, 3]
```

```
   Xóa 2:  lấy xong 2, cursor = 2   →  remove   →  size = 3, modCount tăng
           hasNext: 2 != 3  → true  →  next()   →  modCount lệch → 💥 CME

   Xóa 3:  lấy xong 3, cursor = 3   →  remove   →  size = 3, modCount tăng
           hasNext: 3 != 3  → FALSE →  vòng lặp KẾT THÚC BÌNH THƯỜNG
           → không lỗi, và số 4 KHÔNG BAO GIỜ được kiểm tra

   Xóa 4:  lấy xong 4, cursor = 4   →  remove   →  size = 3, modCount tăng
           hasNext: 4 != 3  → true  →  next()   →  modCount lệch → 💥 CME
```

Cơ chế này gọi là **fail-fast** — nhưng tài liệu Java nói rõ nó chỉ là **nỗ lực tốt nhất**, không phải đảm bảo. Xóa phần tử **áp chót** thì không có lỗi gì, và phần tử cuối bị bỏ qua trong im lặng.

Tưởng tượng code lọc đơn hàng bị hủy theo kiểu này: nếu dữ liệu test tình cờ chỉ có đơn bị hủy ở vị trí áp chót, test xanh, phần tử cuối không bao giờ được kiểm tra. Lỗi chỉ lộ ra khi dữ liệu thật có hình dạng khác.

#### Cách đúng

```java
so.removeIf(x -> x % 2 == 0);
```

`x -> x % 2 == 0` là một **lambda** — hàm nhỏ không tên, nhận `x` trả về `boolean`. Gần như y hệt arrow function của JS (`x => x % 2 === 0`), chỉ đổi `=>` thành `->`. Học kỹ ở bài Stream API trong giai đoạn này.

> 📌 **Không bao giờ sửa một Collection trong lúc đang `for-each` trên chính nó.** Dùng `removeIf()`, hoặc duyệt trên bản sao. Đừng trông chờ CME cứu bạn — có lúc nó không đến.

---

## 9. Bảng chọn

| Bạn cần… | Dùng |
|---|---|
| Danh sách có thứ tự, truy cập theo vị trí | **`ArrayList`** (mặc định) |
| Kiểm tra "có tồn tại không" thật nhanh, không trùng | **`HashSet`** |
| Không trùng, giữ thứ tự thêm vào | `LinkedHashSet` |
| Tra cứu theo khóa | **`HashMap`** (mặc định) |
| Tra cứu theo khóa, giữ thứ tự thêm vào | `LinkedHashMap` |
| Duyệt theo khóa đã sắp xếp | `TreeMap` |
| Danh sách không ai được sửa | `List.of(...)` |

`LinkedList` thắng ở chèn/xóa đầu trên lý thuyết, nhưng trong backend thực tế **gần như không bao giờ** là lựa chọn đúng: `get(i)` là cái bẫy O(n²) quá dễ dẫm phải, và node rải rác trên Heap làm CPU đọc chậm hơn mảng liền nhau.

---

## 10. Tự kiểm tra

1. Vì sao nên khai `List<Integer> ds = new ArrayList<>()` thay vì `ArrayList<Integer> ds = ...`?
2. Vì sao `get(i)` của `ArrayList` là O(1) còn của `LinkedList` là O(n)? Giải thích bằng cấu trúc bên trong.
3. Một thao tác chỉ chậm hơn "một chút" vì sao lại thành chậm hơn 1.000 lần trong thực nghiệm?
4. Vì sao dùng `List` để kiểm tra "email đã đăng ký chưa" là lỗi hiệu năng?
5. `HashMap`, `LinkedHashMap`, `TreeMap` khác nhau ở điểm nào? Vì sao không được dựa vào thứ tự của `HashMap`?
6. `int d = diem.get("Zed");` nguy hiểm thế nào nếu khóa không tồn tại?
7. `List.of("a").add("b")` compile được nhưng nổ khi chạy. Vì sao `javac` không chặn?
8. Xóa phần tử trong `for-each`: vì sao xóa phần tử áp chót lại **không** có lỗi? Điều đó nguy hiểm thế nào?

<details>
<summary>Đáp án</summary>

1. Vì phụ thuộc vào **interface** (Dependency Inversion, bài 2.3): đổi cài đặt chỉ cần sửa một chỗ bên phải dấu `=`, mọi code dùng biến không phải sửa.
2. `ArrayList` dùng mảng liền nhau nên địa chỉ phần tử `i` tính bằng **một phép nhân**. `LinkedList` gồm các node rải rác trên Heap, chỉ nối với nhau bằng con trỏ, nên phải **đi bộ từng node**.
3. Vì thao tác O(n) nằm trong vòng lặp n lần thành **O(n²)**: tổng số bước đi bộ cộng dồn lên hàng tỷ. Gấp đôi dữ liệu thì gấp bốn thời gian.
4. `List.contains()` là O(n) — gọi `equals()` với từng phần tử. Với hàng triệu email thì mỗi lần kiểm tra quét cả triệu phần tử. `HashSet.contains()` là O(1) nhờ cơ chế ngăn.
5. Thứ tự duyệt: `HashMap` không đảm bảo gì, `LinkedHashMap` theo thứ tự chèn, `TreeMap` sắp xếp theo khóa. Thứ tự `HashMap` phụ thuộc `hashCode()` và kích thước bảng nội bộ — có thể đổi khi thêm phần tử hoặc nâng cấp JDK.
6. `get()` trả về `null` khi không có khóa, và gán `null` vào `int` là unboxing → `NullPointerException`. Dùng `getOrDefault()`.
7. Vì `javac` chỉ thấy **kiểu biến** `List`, mà hợp đồng `List` có `add()`. Object thật là danh sách bất biến và từ chối lúc chạy — `javac` không chứng minh được điều đó lúc biên dịch.
8. Vì sau khi xóa, `cursor` bằng đúng `size` mới nên `hasNext()` trả `false` và vòng lặp kết thúc bình thường — bước kiểm tra `modCount` trong `next()` không bao giờ chạy. Phần tử cuối bị bỏ qua **trong im lặng**, test có thể xanh dù code sai.

</details>

---

⬅️ **Bài trước:** [2.5 — `equals()`, `hashCode()` và `record`](../02-oop/05-equals-hashcode-record.md)
➡️ **Bài tiếp:** 3.2 — Generics
