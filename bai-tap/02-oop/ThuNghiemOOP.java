class SanPham {
    private String ten;
    private int soLuong;
    private double gia;
    private boolean conHang;

    static int tongSoSanPham;

    SanPham(String ten, double gia) {
        this.ten = ten;
        this.gia = gia;
        tongSoSanPham++;
    }

    public void nhapHang(int them) {
        if (them <= 0) {
            System.out.println("   [tu choi] So luong phai duong");
            return;
        }
        this.soLuong += them;
        this.conHang = true;
    }

    public double tinhTongTien() {
        return gia * soLuong;
    }

    @Override
    public String toString() {
        return "SanPham{ten='" + ten + "', gia=" + gia
             + ", soLuong=" + soLuong + ", conHang=" + conHang + "}";
    }
}

public class ThuNghiemOOP {

    public static void main(String[] args) {
        SanPham banPhim = new SanPham("Ban phim", 500000);
        System.out.println("--- Ngay sau khi new ---");
        System.out.println(banPhim);

        System.out.println();
        System.out.println("--- Nhap hang ---");
        banPhim.nhapHang(-5);
        banPhim.nhapHang(10);
        System.out.println(banPhim);
        System.out.println("Tong tien : " + banPhim.tinhTongTien());

        System.out.println();
        System.out.println("--- Hai object rieng biet ---");
        SanPham chuot = new SanPham("Chuot", 200000);
        chuot.nhapHang(3);
        System.out.println(banPhim);
        System.out.println(chuot);

        System.out.println();
        System.out.println("--- static dung chung ---");
        System.out.println("SanPham.tongSoSanPham : " + SanPham.tongSoSanPham);
    }
}
