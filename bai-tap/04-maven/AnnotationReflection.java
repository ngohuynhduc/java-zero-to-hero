import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface KhongDeTrong {
    String thongBao() default "khong duoc de trong";
}

@Target(ElementType.FIELD)
@interface DoDaiToiDa {
    int value();
}

class NguoiDung {
    @KhongDeTrong(thongBao = "ten bat buoc")
    private String ten;

    @KhongDeTrong
    @DoDaiToiDa(5)
    private String email;

    private int tuoi;

    NguoiDung(String ten, String email, int tuoi) {
        this.ten = ten;
        this.email = email;
        this.tuoi = tuoi;
    }

    private String biMat() {
        return "mat khau 123";
    }

    @Override
    public String toString() {
        return "NguoiDung[ten=" + ten + "]";
    }
}

public class AnnotationReflection {

    static List<String> kiemTra(Object obj) throws IllegalAccessException {
        List<String> loi = new ArrayList<>();
        Class<?> lop = obj.getClass();
        for (Field f : lop.getDeclaredFields()) {
            f.setAccessible(true);
            Object giaTri = f.get(obj);

            KhongDeTrong kdt = f.getAnnotation(KhongDeTrong.class);
            if (kdt != null && (giaTri == null || giaTri.toString().isBlank())) {
                loi.add(f.getName() + ": " + kdt.thongBao());
            }

            DoDaiToiDa dd = f.getAnnotation(DoDaiToiDa.class);
            if (dd != null && giaTri != null && giaTri.toString().length() > dd.value()) {
                loi.add(f.getName() + ": dai qua " + dd.value() + " ky tu");
            }
        }
        return loi;
    }

    public static void main(String[] args) throws Exception {
        System.out.println("--- 1. Class object ---");
        NguoiDung nd = new NguoiDung("An", "an@mail.com", 20);
        Class<?> lop = nd.getClass();
        System.out.println(lop.getName());
        System.out.println(lop == NguoiDung.class);

        System.out.println("--- 2. Liet ke field ---");
        for (Field f : lop.getDeclaredFields()) {
            System.out.println(f.getType().getSimpleName() + " " + f.getName());
        }

        System.out.println("--- 3. Pha cua private ---");
        Field fTen = lop.getDeclaredField("ten");
        fTen.setAccessible(true);
        fTen.set(nd, "Hacker");
        System.out.println(nd);

        Method m = lop.getDeclaredMethod("biMat");
        m.setAccessible(true);
        System.out.println(m.invoke(nd));

        System.out.println("--- 4. Validator tu che ---");
        NguoiDung nd2 = new NguoiDung("  ", "an@rat-dai.com", 20);
        System.out.println(kiemTra(nd2));

        System.out.println("--- 5. Goi method bang ten ---");
        Method m2 = lop.getDeclaredMethod("biMatCu");
        System.out.println("tim thay: " + m2.getName());
    }
}
