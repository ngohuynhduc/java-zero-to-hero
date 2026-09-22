import java.util.Arrays;

public class MangVaMethod {
    static void doiSo(int x) {
            x = 99;
    }

    static void suaPhanTu(int[] m) {
        m[0] = 99;
    }

    static void ganLaiMang(int[] m) {
        m = new int[]{99, 99, 99};
    }

    static void doiChuoi(String s) {
        s = s + "DA SUA";
    }

    static void in(long x) {
        System.out.println("    -> ban long");
    }
    static void in(Integer x) {
        System.out.println("    -> ban Integer");
    }

    public static void main(String[] args) {
        System.out.println("--- Mang co ban ---");
        int[] soMacDinh = new int[3];
        String[] chuoiMacDinh = new String[2];
        System.out.println("int[] mac dinh:" + Arrays.toString(soMacDinh));
        System.out.println("String[] mac dinh:" + Arrays.toString(chuoiMacDinh));

        int[] diem = {10, 20, 30};
        System.out.println("diem.length = " + diem.length);
        System.out.println("In ra thanh mang = " + diem);
        System.out.println("Arrays.toString = " + Arrays.toString(diem));

        System.out.println();
        System.out.println("--- Tham so dong lenh ---");
        System.out.println("args.length : " + args.length);
        for (String thamSo : args) {
            System.out.println("   " + thamSo);
        }

        System.out.println();
        System.out.println("--- Overloading: long hay Integer? ---");
        in(5);

        System.out.println();
        System.out.println("--- Truyen tham so ---");
        int so = 1;
        doiSo(so);
        System.out.println("A. primitive         : " + so);

        int[] mang1 = {1, 2, 3};
        suaPhanTu(mang1);
        System.out.println("B. mang, sua phan tu : " + Arrays.toString(mang1));

        int[] mang2 = {1, 2, 3};
        ganLaiMang(mang2);
        System.out.println("C. mang, gan lai     : " + Arrays.toString(mang2));

        String ten = "Duc";
        doiChuoi(ten);
        System.out.println("D. String            : " + ten);
    }
}
