interface CoTheBay {
    void bay();

    default void haCanh() {
        System.out.println("  [default] Ha canh binh thuong");
    }
}

interface CoTheBoi {
    void boi();
}

abstract class DongVat {
    protected String ten;

    DongVat(String ten) {
        this.ten = ten;
        System.out.println("  [abstract] constructor DongVat chay");
    }

    abstract void keu();

    public void gioiThieu() {
        System.out.println("  Toi la " + ten);
    }
}

class Vit extends DongVat implements CoTheBay, CoTheBoi {

    Vit(String ten) { super(ten); }

    @Override
    void keu() { System.out.println("  Quac quac"); }

    @Override
    public void bay() { System.out.println("  " + ten + " bay thap"); }

    @Override
    public void boi() { System.out.println("  " + ten + " boi gioi"); }
}

class ChimCanhCut extends DongVat implements CoTheBoi {

    ChimCanhCut(String ten) { super(ten); }

    @Override
    void keu() { System.out.println("  Quang quac"); }

    @Override
    public void boi() { System.out.println("  " + ten + " boi rat nhanh"); }
}

public class InterfaceVaAbstract {

    public static void main(String[] args) {
        System.out.println("--- Tao Vit ---");
        Vit vit = new Vit("Vit co");
        vit.gioiThieu();
        vit.keu();
        vit.bay();
        vit.haCanh();
        vit.boi();

        System.out.println();
        System.out.println("--- Tao Chim canh cut ---");
        ChimCanhCut canhCut = new ChimCanhCut("Canh cut");
        canhCut.keu();
        canhCut.boi();

        System.out.println();
        System.out.println("--- Mot object, nhieu danh tinh ---");
        System.out.println("vit     instanceof DongVat  : " + (vit instanceof DongVat));
        System.out.println("vit     instanceof CoTheBay : " + (vit instanceof CoTheBay));
        System.out.println("vit     instanceof CoTheBoi : " + (vit instanceof CoTheBoi));
        System.out.println("canhCut instanceof CoTheBay : " + (canhCut instanceof CoTheBay));

        System.out.println();
        System.out.println("--- Bien kieu interface ---");
        CoTheBoi conBoi = vit;
        conBoi.boi();
    }
}
