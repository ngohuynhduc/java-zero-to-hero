import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@interface ThanhPhan {
}

interface ThongBao {
    void gui(String noiDung);
}

@ThanhPhan
class EmailThongBao implements ThongBao {
    EmailThongBao() {
        System.out.println("  tao EmailThongBao");
    }

    public void gui(String noiDung) {
        System.out.println("  [EMAIL] " + noiDung);
    }
}

@ThanhPhan
class SmsThongBao implements ThongBao {
    SmsThongBao() {
        System.out.println("  tao SmsThongBao");
    }

    public void gui(String noiDung) {
        System.out.println("  [SMS] " + noiDung);
    }
}

@ThanhPhan
class KhoHang {
    KhoHang() {
        System.out.println("  tao KhoHang");
    }

    boolean conHang(String sanPham) {
        return true;
    }
}

@ThanhPhan
class DonHangService {
    private final KhoHang kho;
    private final ThongBao thongBao;

    DonHangService(KhoHang kho, ThongBao thongBao) {
        System.out.println("  tao DonHangService");
        this.kho = kho;
        this.thongBao = thongBao;
    }

    void datHang(String sanPham) {
        if (kho.conHang(sanPham)) {
            thongBao.gui("da dat " + sanPham);
        }
    }

    KhoHang getKho() {
        return kho;
    }
}

@ThanhPhan
class Ga {
    Ga(Trung trung) {
        System.out.println("  tao Ga");
    }
}

@ThanhPhan
class Trung {
    Trung(Ga ga) {
        System.out.println("  tao Trung");
    }
}

class Container {
    private final List<Class<?>> danhSach;
    private final Map<Class<?>, Object> daTao = new HashMap<>();

    Container(List<Class<?>> danhSach) {
        this.danhSach = danhSach;
    }

    <T> T lay(Class<T> kieu) throws Exception {
        Class<?> lopThat = timLopThat(kieu);

        Object daCo = daTao.get(lopThat);
        if (daCo != null) {
            return kieu.cast(daCo);
        }

        Constructor<?> hamTao = lopThat.getDeclaredConstructors()[0];
        Class<?>[] kieuThamSo = hamTao.getParameterTypes();
        Object[] thamSo = new Object[kieuThamSo.length];
        for (int i = 0; i < kieuThamSo.length; i++) {
            thamSo[i] = lay(kieuThamSo[i]);
        }

        Object moi = hamTao.newInstance(thamSo);
        daTao.put(lopThat, moi);
        return kieu.cast(moi);
    }

    private Class<?> timLopThat(Class<?> kieu) {
        for (Class<?> c : danhSach) {
            if (c.isAnnotationPresent(ThanhPhan.class) && kieu.isAssignableFrom(c)) {
                return c;
            }
        }
        throw new IllegalStateException("khong co thanh phan nao cho kieu " + kieu.getSimpleName());
    }
}

public class MiniContainer {
    public static void main(String[] args) throws Exception {
        System.out.println("--- 1. Lap rap bang tay ---");
        KhoHang kho = new KhoHang();
        ThongBao tb = new EmailThongBao();
        DonHangService svTay = new DonHangService(kho, tb);
        svTay.datHang("ao");

        System.out.println("--- 2. Container tu lap rap ---");
        Container c = new Container(List.of(DonHangService.class, KhoHang.class, EmailThongBao.class));
        DonHangService sv = c.lay(DonHangService.class);
        sv.datHang("quan");

        System.out.println("--- 3. Lay them lan nua ---");
        DonHangService sv2 = c.lay(DonHangService.class);
        System.out.println(sv == sv2);
        System.out.println(sv.getKho() == c.lay(KhoHang.class));

        System.out.println("--- 4. Doi Email sang SMS ---");
        Container c2 = new Container(List.of(DonHangService.class, KhoHang.class, SmsThongBao.class));
        c2.lay(DonHangService.class).datHang("giay");

        System.out.println("--- 5. Quen dang ky ---");
        Container c3 = new Container(List.of(DonHangService.class, KhoHang.class));
        try {
            c3.lay(DonHangService.class);
        } catch (IllegalStateException e) {
            System.out.println(e.getMessage());
        }

        System.out.println("--- 6. Ga va trung ---");
        Container c4 = new Container(List.of(Ga.class, Trung.class));
        c4.lay(Ga.class);
    }
}
