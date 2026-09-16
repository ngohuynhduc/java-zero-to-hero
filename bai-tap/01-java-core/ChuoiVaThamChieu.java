public class ChuoiVaThamChieu {
    public static void main(String[] args) {
        String a = "hello";
        String b = "hello";
        String c = new String("hello");

        System.out.println("--- So sanh bang == ---");
        System.out.println("a == b: " + (a == b)); // true
        System.out.println("a == c: " + (a == c)); // false

        System.out.println("--- So sanh bang equals() ---");
        System.out.println("a.equals(b): " + a.equals(b)); // true
        System.out.println("a.equals(c): " + a.equals(c)); // true

        System.out.println("--- String la bat bien ---");
        String s = "hello";
        s.toUpperCase();
        System.out.println("Sau khi goi toUpperCase(): " + s); // hello
        s = s.toUpperCase();
        System.out.println("Sau khi gan lai: " + s); // HELLO

        System.out.println();
        System.out.println("--- Ghep chuoi: luc compile vs luc chay ---");
        String d = "hel" + "lo";
        String phanDau = "hel";
        String e = phanDau + "lo";
        System.out.println("a == d : " + (a == d));
        System.out.println("a == e : " + (a == e));
    }
}
