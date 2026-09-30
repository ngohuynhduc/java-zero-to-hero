class NhanVien {
    protected String ten;
    private double luongCoBan;

    NhanVien(String ten, double luongCoBan) {
        System.out.println("  [NhanVien] constructor bat dau");
        this.ten = ten;
        this.luongCoBan = luongCoBan;
        gioiThieu();
        System.out.println("  [NhanVien] constructor ket thuc");
    }

    public double tinhLuong() {
        return luongCoBan;
    }

    public void gioiThieu() {
        System.out.println("  Xin chao, toi la " + ten);
    }

    @Override
    public String toString() {
        return "NhanVien{ten='" + ten + "', luong=" + tinhLuong() + "}";
    }
}

class QuanLy extends NhanVien {
    private double phuCap;
    private String phongBan = "Ky thuat";

    QuanLy(String ten, double luongCoBan, double phuCap) {
        super(ten, luongCoBan);
        System.out.println("  [QuanLy] constructor chay");
        this.phuCap = phuCap;
    }

    @Override
    public double tinhLuong() {
        return super.tinhLuong() + phuCap;
    }

    @Override
    public void gioiThieu() {
        System.out.println("  Xin chao, toi la " + ten + ", quan ly phong " + phongBan);
    }
}

public class KeThua {

    public static void main(String[] args) {
        System.out.println("--- Tao NhanVien ---");
        NhanVien an = new NhanVien("An", 8000000);

        System.out.println();
        System.out.println("--- Tao QuanLy ---");
        QuanLy binh = new QuanLy("Binh", 8000000, 1500000);

        System.out.println();
        System.out.println("--- Luong ---");
        System.out.println(an);
        System.out.println(binh);

        System.out.println();
        System.out.println("--- Goi lai gioiThieu sau khi tao xong ---");
        binh.gioiThieu();

        System.out.println();
        System.out.println("--- instanceof ---");
        System.out.println("binh instanceof QuanLy   : " + (binh instanceof QuanLy));
        System.out.println("binh instanceof NhanVien : " + (binh instanceof NhanVien));
        System.out.println("binh instanceof Object   : " + (binh instanceof Object));
        System.out.println("an   instanceof QuanLy   : " + (an instanceof QuanLy));
    }
}
