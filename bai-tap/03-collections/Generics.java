import java.util.ArrayList;
import java.util.List;

class DongVat {
    String ten;

    DongVat(String ten) { this.ten = ten; }

    @Override
    public String toString() { return ten; }
}

class Cho extends DongVat {
    Cho(String ten) { super(ten); }
}

class Meo extends DongVat {
    Meo(String ten) { super(ten); }
}

class Hop<T> {
    private T giaTri;

    Hop(T giaTri) { this.giaTri = giaTri; }

    T lay() { return giaTri; }
}

public class Generics {

    static <T> T phanTuDau(List<T> ds) {
        return ds.get(0);
    }

    static void inTen(List<? extends DongVat> ds) {
        for (DongVat d : ds) {
            System.out.print(d.ten + " ");
        }
        System.out.println();
        // ds.add(new Cho("Lau"));
    }

    static void themCho(List<? super Cho> ds) {
        ds.add(new Cho("Milu"));
        Object docRa = ds.get(0);
        System.out.println("  doc ra: " + docRa);
    }

    public static void main(String[] args) {
        System.out.println("--- 1. Raw type (kieu Java 1.4) ---");
        List dsTho = new ArrayList();
        dsTho.add("An");
        dsTho.add("Binh");
        dsTho.add(42);
        System.out.println("add xong 3 phan tu");
        try {
            for (int i = 0; i < dsTho.size(); i++) {
                String ten = (String) dsTho.get(i);
                System.out.println("  " + ten.toUpperCase());
            }
        } catch (Exception e) {
            System.out.println("  Loi: " + e.getClass().getSimpleName());
        }

        System.out.println();
        System.out.println("--- 2. Type erasure ---");
        List<String> dsChuoi = new ArrayList<>();
        List<Integer> dsSo = new ArrayList<>();
        System.out.println("cung class? " + (dsChuoi.getClass() == dsSo.getClass()));

        System.out.println();
        System.out.println("--- 3. Class va method generic ---");
        Hop<String> hopChu = new Hop<>("chu");
        Hop<Integer> hopSo = new Hop<>(42);
        String s = hopChu.lay();
        int n = hopSo.lay();
        System.out.println(s + " | " + n);
        System.out.println(phanTuDau(List.of("An", "Binh")));
        System.out.println(phanTuDau(List.of(7, 8)));

        System.out.println();
        System.out.println("--- 4. List<Cho> va List<DongVat> ---");
        List<Cho> dsCho = new ArrayList<>(List.of(new Cho("Vang"), new Cho("Den")));
        List<DongVat> dsDV = dsCho;

        System.out.println();
        System.out.println("--- 5. Mang thi sao? ---");
        Cho[] mangCho = new Cho[2];
        try {
            DongVat[] mangDV = mangCho;
            System.out.println("gan mang: compile OK");
            mangDV[0] = new Meo("Mun");
            System.out.println("bo Meo vao xong");
        } catch (Exception e) {
            System.out.println("  Loi: " + e.getClass().getSimpleName());
        }

        System.out.println();
        System.out.println("--- 6. Wildcard ---");
        List<Meo> dsMeo = List.of(new Meo("Mun"));
        System.out.print("inTen(dsCho): ");
        inTen(dsCho);
        System.out.print("inTen(dsMeo): ");
        inTen(dsMeo);

        List<DongVat> dsDongVat = new ArrayList<>();
        List<Object> dsObject = new ArrayList<>();
        themCho(dsDongVat);
        themCho(dsObject);
    }
}
