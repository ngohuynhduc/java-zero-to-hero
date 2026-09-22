public class ToanTuVaDieuKhien {
    public static void main(String[] args) {
        System.out.println("--- Chia so nguyen ---");
        int tongDiem = 7;
        int soMon = 2;
        System.out.println("7/2 = " + (tongDiem / soMon));
        System.out.println("7/2.0 = " + (tongDiem / 2.0));
        double cach1 = (double) (tongDiem / soMon);
        double cach2 = (double) tongDiem / soMon;
        System.out.println("Cach 1: (double) (7/2) = " + cach1);
        System.out.println("Cach 2: (double) 7/2 = " + cach2);

        System.out.println();
        System.out.println("--- Modulo voi so am ---");
        System.out.println("7 % 3   = " + (7 % 3));
        System.out.println("-7 % 3  = " + (-7 % 3));

        System.out.println();
        System.out.println("--- Chia cho 0 (so thuc) ---");
        System.out.println("5.0 / 0   = " + (5.0 / 0));
        double khongPhaiSo = 0.0 / 0.0;
        System.out.println("0.0 / 0.0 = " + khongPhaiSo);
        System.out.println("NaN == NaN = " + (khongPhaiSo == khongPhaiSo));

        System.out.println();
        System.out.println("--- switch THIEU break ---");
        int ngay = 2;
        switch (ngay) {
            case 1: System.out.println("Thu hai");
            case 2: System.out.println("Thu ba");
            case 3: System.out.println("Thu tu");
            default: System.out.println("Khong hop le");
        }

        System.out.println();
        System.out.println("--- switch bieu thuc ---");
        String loaiNgay = switch (ngay) {
            case 1,7 -> "Cuoi tuan";
            case 2,3,4,5,6 -> "Trong tuan";
            default -> "Khong hop le";
        };
        System.out.println("ngay " + ngay + " -> " + loaiNgay);
    }
}
