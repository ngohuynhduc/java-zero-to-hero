public class WrapperVaEpKieu {
    public static void main(String[] args) {
        System.out.println("--- Integer cache ---");
        Integer a = 127;
        Integer b = 127;
        Integer c = 128;
        Integer d = 128;
        System.out.println("a == b: " + (a == b));
        System.out.println("c == d: " + (c == d));
        System.out.println("c.equals(d): " + c.equals(d));
        System.out.println("--- Mo rong: Java tu lam ---");
        int soNguyen = 100;
        long soNguyenDai = soNguyen;
        double soThuc = soNguyenDai;
        System.out.println("int -> long -> double : " + soThuc);
        System.out.println("--- Thu hep: phai ep tay ---");
        int soLon = 130;
        byte thuHep = (byte) soLon;
        System.out.println("int (130) -> byte : " + thuHep);
        double coPhanLe = 9.99;
        int catCut = (int) coPhanLe;
        System.out.println("double (9.99) -> int : " + catCut);
        long soRatLon = 10000000000L;
        int matDuLieu = (int) soRatLon;
        System.out.println("long (10000000000) -> int : " + matDuLieu);
        System.out.println();
        System.out.println("--- Bay NPE khi unboxing ---");
        Integer chuaCoDuLieu = null;
        int soSanh = chuaCoDuLieu;
        System.out.println("Dong nay khong bao gio in ra: " + soSanh);
    }
}
