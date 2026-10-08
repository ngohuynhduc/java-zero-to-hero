import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class ChonCollection {

    static long tinhMs(long batDau) {
        return (System.nanoTime() - batDau) / 1_000_000;
    }

    public static void main(String[] args) {
        int n = 100_000;
        List<Integer> mangDong = new ArrayList<>();
        List<Integer> chuoiNode = new LinkedList<>();
        for (int i = 0; i < n; i++) {
            mangDong.add(i);
            chuoiNode.add(i);
        }

        System.out.println("--- 1. get(i) " + n + " lan ---");
        long t = System.nanoTime();
        long tong = 0;
        for (int i = 0; i < n; i++) { tong += mangDong.get(i); }
        System.out.println("ArrayList  : " + tinhMs(t) + " ms");

        t = System.nanoTime();
        tong = 0;
        for (int i = 0; i < n; i++) { tong += chuoiNode.get(i); }
        System.out.println("LinkedList : " + tinhMs(t) + " ms");

        System.out.println();
        System.out.println("--- 2. Chen vao DAU " + n + " lan ---");
        List<Integer> a = new ArrayList<>();
        t = System.nanoTime();
        for (int i = 0; i < n; i++) { a.add(0, i); }
        System.out.println("ArrayList  : " + tinhMs(t) + " ms");

        LinkedList<Integer> l = new LinkedList<>();
        t = System.nanoTime();
        for (int i = 0; i < n; i++) { l.addFirst(i); }
        System.out.println("LinkedList : " + tinhMs(t) + " ms");

        System.out.println();
        System.out.println("--- 3. contains 10000 lan ---");
        Set<Integer> tapHop = new HashSet<>(mangDong);
        t = System.nanoTime();
        for (int i = 0; i < 10_000; i++) { mangDong.contains(n - 1 - i); }
        System.out.println("ArrayList  : " + tinhMs(t) + " ms");

        t = System.nanoTime();
        for (int i = 0; i < 10_000; i++) { tapHop.contains(n - 1 - i); }
        System.out.println("HashSet    : " + tinhMs(t) + " ms");

        System.out.println();
        System.out.println("--- 4. Thu tu cua Map ---");
        String[] ten = {"Minh", "An", "Tuan", "Binh", "Lan", "Cuong"};
        Map<String, Integer> hm = new HashMap<>();
        Map<String, Integer> lhm = new LinkedHashMap<>();
        Map<String, Integer> tm = new TreeMap<>();
        for (int i = 0; i < ten.length; i++) {
            hm.put(ten[i], i);
            lhm.put(ten[i], i);
            tm.put(ten[i], i);
        }
        System.out.println("Thu tu them   : " + Arrays.toString(ten));
        System.out.println("HashMap       : " + hm.keySet());
        System.out.println("LinkedHashMap : " + lhm.keySet());
        System.out.println("TreeMap       : " + tm.keySet());

        System.out.println();
        System.out.println("--- 5. put trung khoa ---");
        Map<String, Integer> diem = new HashMap<>();
        diem.put("An", 7);
        diem.put("An", 9);
        System.out.println("size = " + diem.size() + ", An = " + diem.get("An"));

        System.out.println();
        System.out.println("--- 6. Xoa trong luc duyet ---");
        List<Integer> so = new ArrayList<>(List.of(1, 2, 3, 4));
        try {
            for (Integer x : so) {
                if (x % 2 == 0) { so.remove(x); }
            }
        } catch (Exception e) {
            System.out.println("Loi : " + e);
        }
        System.out.println("so sau do : " + so);
    }
}
