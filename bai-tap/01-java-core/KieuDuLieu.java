public class KieuDuLieu {

    public static void main(String[] args) {
        // ----- Nhom so nguyen -----
        byte  soByte  = 127;
        short soShort = 32000;
        int   soInt   = 2000000000;
        long  soLong  = 9000000000L;

        // ----- Nhom so thuc -----
        float  soFloat  = 3.14f;
        double soDouble = 3.141592653589793;

        // ----- Ky tu va logic -----
        char    kyTu    = 'A';
        boolean dungSai = true;

        System.out.println("byte    : " + soByte);
        System.out.println("short   : " + soShort);
        System.out.println("int     : " + soInt);
        System.out.println("long    : " + soLong);
        System.out.println("float   : " + soFloat);
        System.out.println("double  : " + soDouble);
        System.out.println("char    : " + kyTu);
        System.out.println("boolean : " + dungSai);

        System.out.println();
        System.out.println("--- Gioi han cua int ---");
        System.out.println("int lon nhat : " + Integer.MAX_VALUE);
        System.out.println("Cong them 1  : " + (Integer.MAX_VALUE + 1));

        System.out.println();
        System.out.println("--- char thuc chat la so ---");
        System.out.println("'A' + 1      : " + ('A' + 1));
        System.out.println("Ep ve char   : " + (char) ('A' + 1));

        System.out.println();
        System.out.println("--- So thuc khong chinh xac ---");
        System.out.println("0.1 + 0.2    : " + (0.1 + 0.2));
    }
}
