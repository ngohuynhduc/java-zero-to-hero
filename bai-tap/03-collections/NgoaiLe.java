class TaiNguyen implements AutoCloseable {
    private String ten;

    TaiNguyen(String ten) {
        this.ten = ten;
        System.out.println("  mo " + ten);
    }

    @Override
    public void close() {
        System.out.println("  dong " + ten);
    }
}

class KhongTimThayDonHang extends Exception {
    KhongTimThayDonHang(String message) { super(message); }
}

class LoiDichVu extends RuntimeException {
    LoiDichVu(String message, Throwable cause) { super(message, cause); }
}

public class NgoaiLe {

    static void a() {
        System.out.println("a: bat dau");
        b();
        System.out.println("a: ket thuc");
    }

    static void b() {
        System.out.println("b: bat dau");
        c();
        System.out.println("b: ket thuc");
    }

    static void c() {
        System.out.println("c: bat dau");
        int[] mang = new int[3];
        mang[5] = 1;
        System.out.println("c: ket thuc");
    }

    static int thuFinally() {
        try {
            System.out.println("  try: return 1");
            return 1;
        } finally {
            System.out.println("  finally chay");
        }
    }

    static int nuotLoi() {
        try {
            throw new IllegalStateException("loi quan trong");
        } finally {
            return 2;
        }
    }

    static String timDonHang(int id) throws KhongTimThayDonHang {
        if (id != 1) {
            throw new KhongTimThayDonHang("khong co don hang id=" + id);
        }
        return "Don hang #1";
    }

    static String layDonHang(int id) {
        try {
            return timDonHang(id);
        } catch (KhongTimThayDonHang e) {
            throw new LoiDichVu("lay don hang that bai", e);
        }
    }

    public static void main(String[] args) {
        System.out.println("--- 1. Stack unwinding ---");
        try {
            a();
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("main bat duoc: " + e.getMessage());
        }

        System.out.println();
        System.out.println("--- 2. finally ---");
        System.out.println("thuFinally() = " + thuFinally());
        System.out.println("nuotLoi() = " + nuotLoi());

        System.out.println();
        System.out.println("--- 3. Checked exception ---");
        try {
            System.out.println(timDonHang(1));
            System.out.println(timDonHang(2));
            System.out.println("sau timDonHang(2)");
        } catch (KhongTimThayDonHang e) {
            System.out.println("Loi: " + e.getMessage());
        }
        // String dh = timDonHang(3);

        System.out.println();
        System.out.println("--- 4. Thu tu catch ---");
        try {
            Object o = "chu";
            Integer so = (Integer) o;
        } catch (ClassCastException e) {
            System.out.println("bat boi ClassCastException");
        } catch (RuntimeException e) {
            System.out.println("bat boi RuntimeException");
        }

        System.out.println();
        System.out.println("--- 5. Boc loi (exception chaining) ---");
        try {
            layDonHang(9);
        } catch (LoiDichVu e) {
            System.out.println("Loi: " + e.getMessage());
            System.out.println("Nguyen nhan: " + e.getCause().getMessage());
            System.out.println("Kieu nguyen nhan: " + e.getCause().getClass().getSimpleName());
        }

        System.out.println();
        System.out.println("--- 6. try-with-resources ---");
        try (TaiNguyen ketNoi = new TaiNguyen("ketNoi");
             TaiNguyen file = new TaiNguyen("file")) {
            System.out.println("  dang dung tai nguyen");
            throw new IllegalStateException("hong giua chung");
        } catch (IllegalStateException e) {
            System.out.println("  catch: " + e.getMessage());
        } finally {
            System.out.println("  finally");
        }

        System.out.println();
        System.out.println("--- 7. Khong ai bat ---");
        layDonHang(5);
        System.out.println("dong nay co in khong?");
    }
}
