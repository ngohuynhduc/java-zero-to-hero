import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

class UserKhongOverride {
    String email;

    UserKhongOverride(String email) { this.email = email; }
}

class UserChiEquals {
    String email;

    UserChiEquals(String email) { this.email = email; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserChiEquals khac)) return false;
        return email.equals(khac.email);
    }
}

class UserDayDu {
    String email;

    UserDayDu(String email) { this.email = email; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserDayDu khac)) return false;
        return email.equals(khac.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }
}

record UserRecord(String email) { }

public class EqualsHashCode {

    public static void main(String[] args) {
        System.out.println("--- 1. Khong override gi ---");
        UserKhongOverride a1 = new UserKhongOverride("duc@x.com");
        UserKhongOverride a2 = new UserKhongOverride("duc@x.com");
        System.out.println("a1.equals(a2)     : " + a1.equals(a2));

        System.out.println();
        System.out.println("--- 2. Chi override equals ---");
        UserChiEquals b1 = new UserChiEquals("duc@x.com");
        UserChiEquals b2 = new UserChiEquals("duc@x.com");
        System.out.println("b1.equals(b2)     : " + b1.equals(b2));
        System.out.println("hashCode b1, b2   : " + b1.hashCode() + ", " + b2.hashCode());

        List<UserChiEquals> danhSach = new ArrayList<>();
        danhSach.add(b1);
        System.out.println("list.contains(b2) : " + danhSach.contains(b2));

        Set<UserChiEquals> tapHop = new HashSet<>();
        tapHop.add(b1);
        System.out.println("set.contains(b2)  : " + tapHop.contains(b2));
        tapHop.add(b2);
        System.out.println("set.size()        : " + tapHop.size());

        System.out.println();
        System.out.println("--- 3. Override ca hai ---");
        UserDayDu c1 = new UserDayDu("duc@x.com");
        UserDayDu c2 = new UserDayDu("duc@x.com");
        Set<UserDayDu> tapHopDung = new HashSet<>();
        tapHopDung.add(c1);
        System.out.println("hashCode c1, c2   : " + c1.hashCode() + ", " + c2.hashCode());
        System.out.println("set.contains(c2)  : " + tapHopDung.contains(c2));

        System.out.println();
        System.out.println("--- 4. Sua field sau khi bo vao Set ---");
        c1.email = "khac@x.com";
        System.out.println("set.contains(c1)  : " + tapHopDung.contains(c1));
        System.out.println("set.size()        : " + tapHopDung.size());

        System.out.println();
        System.out.println("--- 5. record ---");
        UserRecord r1 = new UserRecord("duc@x.com");
        UserRecord r2 = new UserRecord("duc@x.com");
        System.out.println("r1                : " + r1);
        System.out.println("r1.equals(r2)     : " + r1.equals(r2));
        System.out.println("r1 == r2          : " + (r1 == r2));
    }
}
