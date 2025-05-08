package com.example.oderfoodapp.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.oderfoodapp.object.Cart;
import com.example.oderfoodapp.object.Category;
import com.example.oderfoodapp.object.FavoriteFood;
import com.example.oderfoodapp.object.Feedback;
import com.example.oderfoodapp.object.Food;
import com.example.oderfoodapp.object.History;
import com.example.oderfoodapp.object.HistoryDetail;
import com.example.oderfoodapp.object.NhanVien;
import com.example.oderfoodapp.object.User;
import com.example.oderfoodapp.object.Voucher;
import com.example.oderfoodapp.object.VoucherDetail;

import java.util.concurrent.Executors;


@Database(entities = {Food.class, Category.class, User.class, Feedback.class, Cart.class, History.class, FavoriteFood.class, HistoryDetail.class, Voucher.class, VoucherDetail.class, NhanVien.class}, version = 16)
public abstract class AppDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "OrderFoodApp.db";
    private static AppDatabase instance;

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    .addCallback(roomCallback) // Thêm callback
                    .build();
        }
        return instance;
    }

    public abstract com.example.oderfoodapp.database.FoodDAO foodDAO();
    public abstract com.example.oderfoodapp.database.CategoryDAO categoryDAO();
    public abstract com.example.oderfoodapp.database.UserDAO userDAO();
    public abstract com.example.oderfoodapp.database.CartDAO cartDAO();
    public abstract com.example.oderfoodapp.database.HistoryDAO historyDAO();
    public abstract com.example.oderfoodapp.database.FavoriteDAO favoriteDAO();
    public abstract com.example.oderfoodapp.database.daoDetailHistory daoDetailHistory();
    public abstract com.example.oderfoodapp.database.daoVoucher DaoVoucher();
    public abstract com.example.oderfoodapp.database.daoVoucherDetail voucherDetaildao();
    public abstract com.example.oderfoodapp.database.NhanVienDAO nhanVienDAO();

    private static RoomDatabase.Callback roomCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) { // chạy khi được tạo mới toàn bộ cơ sở dữ liệu
            super.onCreate(db);
            Executors.newSingleThreadExecutor().execute(() -> {
                // Tạo tài khoản admin mặc định
                User giangAdmin = new User("giang","Trần Trường Giang", "giang@gmail.com", "Hà Nội", "0123456789", "profile", "Giang1234", true);
                User vietAdmin = new User("viet","Quách Hùng Việt", "viet@gmail.com", "Hà Nội", "0987654321", "profile", "Viet1234", true);
                User manhAdmin = new User("manh","Nguyễn Văn Mạnh", "manh@gmail.com", "Hà Nội", "0984321678", "profile", "Manh1234", true);
                User quanghonAdmin = new User("quanghon","Ngô Quang Hơn","quanghon777@gmail.com","96 Định Công, Hà Nội","0367890997","profile","Qhon1234",true);
                User baotu = new User("baotus","Nguyễn Bảo Tú","baotu@gmail.com","807 Giải Phóng","0934512999","profile","baotu1234",false);
                User vankhanh = new User("vankhanh","Ngô Văn Khánh","vankhanh@gmail.com","Ngõ 5 Láng Hạ","0938374698","profile","vankhanh123",false);
                User balam = new User("balam","Nguyễn Bá Lâm","balam@gmail.com","25c Đ.TRần Duy Hưng, Trung Hòa,Cầu Giấy,Hà Nội","0983746521","profile","balam123",false);
                User vantoan = new User("vantoan","Nguyễn Văn Toàn","vantoan@gmail.com","462 Đ.Âu Cơ,Nhật Tân,Tây Hồ,Hà Nội","0987898456","profile","vantoan123",false);
                User congphuong = new User("congphuong","Nguyễn Công Phượng","congphuong@gmail.com","2 P. Lê Thạch, Tràng Tiền,Hoàn Kiếm,Hà Nội","0986789687","profile","congphuong123",false);

                getInstance(null).userDAO().insert(giangAdmin);
                getInstance(null).userDAO().insert(vietAdmin);
                getInstance(null).userDAO().insert(manhAdmin);
                getInstance(null).userDAO().insert(quanghonAdmin);
                getInstance(null).userDAO().insert(baotu);
                getInstance(null).userDAO().insert(vankhanh);
                getInstance(null).userDAO().insert(balam);
                getInstance(null).userDAO().insert(vantoan);
                getInstance(null).userDAO().insert(congphuong);
                // Tạo các đối tượng Voucher
                Voucher voucher1 = new Voucher("FREESHIP20", "Miễn phí vận chuyển cho đơn hàng từ 200K", "freeship", 200000, 0.0f);
                Voucher voucher2 = new Voucher("SALE30K", "Giảm giá 10% cho đơn hàng từ 300K", "percentage", 300000, 10.0f);
                Voucher voucher3 = new Voucher("NEWUSER10%", "Giảm 10% cho khách hàng mới, đơn hàng từ 100K", "percentage", 100000, 10.0f);
                Voucher voucher4 = new Voucher("SALE5%", "Giảm 5% cho đơn hàng từ 50K", "percentage", 50000, 5.0f);
                getInstance(null).DaoVoucher().insertVoucher(voucher1);
                getInstance(null).DaoVoucher().insertVoucher(voucher2);
                getInstance(null).DaoVoucher().insertVoucher(voucher3);
                getInstance(null).DaoVoucher().insertVoucher(voucher4);
                VoucherDetail vc1 = new VoucherDetail(1,"baotus");
                VoucherDetail vc2 = new VoucherDetail(2,"baotus");
                VoucherDetail vc3 = new VoucherDetail(1,"manh");
                VoucherDetail vc4 = new VoucherDetail(2,"manh");
                VoucherDetail vc5 = new VoucherDetail(3,"congphuong");
                getInstance(null).voucherDetaildao().insertVoucherDetail(vc1);
                getInstance(null).voucherDetaildao().insertVoucherDetail(vc2);
                getInstance(null).voucherDetaildao().insertVoucherDetail(vc3);
                getInstance(null).voucherDetaildao().insertVoucherDetail(vc4);
                getInstance(null).voucherDetaildao().insertVoucherDetail(vc5);
            });
        }
    };
}
