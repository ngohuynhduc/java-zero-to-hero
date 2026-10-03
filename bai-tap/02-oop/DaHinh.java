import java.util.List;

abstract class DongVat {
    protected String ten;

    DongVat(String ten) { this.ten = ten; }

    abstract void keu();
}

class Cho extends DongVat {
    Cho(String ten) { super(ten); }

    @Override
    void keu() { System.out.println("  " + ten + ": Gau gau"); }

    void giuNha() { System.out.println("  " + ten + " dang giu nha"); }
}

class Meo extends DongVat {
    Meo(String ten) { super(ten); }

    @Override
    void keu() { System.out.println("  " + ten + ": Meo meo"); }
}

class ChaTest {
    String nhan = "CHA";
    static void tinh() { System.out.println("  [static] CHA"); }
    void dong()        { System.out.println("  [instance] CHA"); }
}

class ConTest extends ChaTest {
    String nhan = "CON";
    static void tinh() { System.out.println("  [static] CON"); }

    @Override
    void dong() { System.out.println("  [instance] CON"); }
}

public class DaHinh {

    static void xuLy(DongVat d) {
        d.keu();
        if (d instanceof Cho cho) {
            cho.giuNha();
        }
    }

    public static void main(String[] args) {
        System.out.println("--- Vong lap khong he biet con nao la con gi ---");
        List<DongVat> ds = List.of(new Cho("Milu"), new Meo("Mun"), new Cho("Vang"));
        for (DongVat d : ds) {
            xuLy(d);
        }

        System.out.println();
        System.out.println("--- Cai gi da hinh, cai gi khong? ---");
        ChaTest x = new ConTest();
        System.out.println("  x.nhan = " + x.nhan);
        x.tinh();
        x.dong();

        System.out.println();
        System.out.println("--- Downcasting sai ---");
        DongVat meo = new Meo("Mun");
        Cho epSai = (Cho) meo;
        epSai.giuNha();
    }
}
