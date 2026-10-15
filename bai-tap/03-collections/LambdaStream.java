import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

record NhanVien(String ten, String phong, int luong) {}

public class LambdaStream {

    public static void main(String[] args) {
        System.out.println("--- 1. Lop an danh vs lambda ---");
        List<String> dsTen = new ArrayList<>(List.of("Chi", "An", "Binh"));

        dsTen.sort(new Comparator<String>() {
            @Override
            public int compare(String a, String b) {
                return a.compareTo(b);
            }
        });
        System.out.println("lop an danh: " + dsTen);

        dsTen.sort((a, b) -> b.compareTo(a));
        System.out.println("lambda:      " + dsTen);

        System.out.println();
        System.out.println("--- 2. Functional interface ---");
        Predicate<Integer> laSoChan = n -> n % 2 == 0;
        Function<String, Integer> doDai = s -> s.length();
        Function<String, String> vietHoa = String::toUpperCase;
        Supplier<List<String>> taoDs = ArrayList::new;
        System.out.println(laSoChan.test(4) + " " + laSoChan.test(7));
        System.out.println(doDai.apply("Java"));
        System.out.println(vietHoa.apply("abc"));
        System.out.println(taoDs.get().size());

        System.out.println();
        System.out.println("--- 3. Lambda bat bien ---");
        int heSo = 10;
        Function<Integer, Integer> nhan = x -> x * heSo;
        System.out.println(nhan.apply(5));
        // heSo = 20;
        // Function<String, String> docFile = duongDan -> Files.readString(Path.of(duongDan));

        System.out.println();
        System.out.println("--- 4. Stream luoi (lazy) ---");
        Stream<String> luong = Stream.of("an", "binh", "chi", "dung")
                .filter(x -> {
                    System.out.println("  filter " + x);
                    return x.length() > 2;
                })
                .map(x -> {
                    System.out.println("  map " + x);
                    return x.toUpperCase();
                });
        System.out.println("da tao stream xong");
        String dau = luong.findFirst().orElse("khong co");
        System.out.println("findFirst = " + dau);

        System.out.println();
        System.out.println("--- 5. Dung lai stream ---");
        try {
            System.out.println(luong.count());
        } catch (IllegalStateException e) {
            System.out.println("Loi: " + e.getMessage());
        }

        System.out.println();
        System.out.println("--- 6. Collectors ---");
        List<NhanVien> dsNV = List.of(
                new NhanVien("An", "IT", 2000),
                new NhanVien("Binh", "HR", 1500),
                new NhanVien("Chi", "IT", 2500),
                new NhanVien("Dung", "HR", 1800));

        Map<String, Long> soNguoi = dsNV.stream()
                .collect(Collectors.groupingBy(NhanVien::phong, Collectors.counting()));
        System.out.println("so nguoi: " + soNguoi);

        int tongLuongIT = dsNV.stream()
                .filter(nv -> nv.phong().equals("IT"))
                .mapToInt(NhanVien::luong)
                .sum();
        System.out.println("tong luong IT = " + tongLuongIT);

        System.out.println();
        System.out.println("--- 7. toList() tra ve gi? ---");
        List<String> ketQua = dsNV.stream().map(NhanVien::ten).toList();
        System.out.println(ketQua);
        try {
            ketQua.add("Em");
            System.out.println("add OK: " + ketQua);
        } catch (UnsupportedOperationException e) {
            System.out.println("Loi: " + e.getClass().getSimpleName());
        }
    }
}
