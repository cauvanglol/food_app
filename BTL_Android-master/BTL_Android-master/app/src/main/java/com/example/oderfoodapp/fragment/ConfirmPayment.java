package com.example.oderfoodapp.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.oderfoodapp.R;
import com.example.oderfoodapp.TrangChung;
import com.example.oderfoodapp.database.AppDatabase;
import com.example.oderfoodapp.object.Cart;
import com.example.oderfoodapp.object.History;
import com.example.oderfoodapp.object.HistoryDetail;
import com.example.oderfoodapp.object.User;
import com.example.oderfoodapp.object.Voucher;
import com.example.oderfoodapp.object.VoucherDetail;
import com.example.oderfoodapp.recyclerViewAdapter.PaymentAdapter;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ConfirmPayment extends Fragment {
    PaymentAdapter paymentAdapter;
    List<Cart> listCart = new ArrayList<>();
    RecyclerView recyclerView;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    Button btnHuy;
    Button btnPay;
    TextView showpttt;
    TextView showdiscount;
    TextView tongtien;
    List<VoucherDetail> listVoucherCustomer = new ArrayList<>();
    float totalCart = 0; // biến totalCart này áp dụng cho giá không áp dụng voucher
    boolean checkedItems[] = new boolean[1000000]; //mảng naày để lưu các mã giảm giá đã chọn khi bật lại
    // alert dialog lên nó không bị mất
    int selectionPosition = -1;  // đan dấu chọn phương thức thanh toán
    TextInputEditText fullname;
    TextInputEditText phonenumber;
    TextInputEditText address;
    String wayToPay = "";// Phương thức thanh toán
    User user; // lấy ra thông tin của user
    float applyDiscount = 0; // biến này là giá sử dung để áp dụng voucher
    TrangChung mainactivity;
    // KHai báo các TextInPutLayout;
    TextInputLayout lfullname, lphonenumber, laddress;

    public ConfirmPayment(List<Cart> listCartt) {
        listCart = listCartt;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        btnHuy = view.findViewById(R.id.btnhuy);
        btnPay = view.findViewById(R.id.btnpay);
        showdiscount = view.findViewById(R.id.btndiscount);
        showpttt = view.findViewById(R.id.btnpttt);
        tongtien = view.findViewById(R.id.tongtien);
        fullname = view.findViewById(R.id.fullname);
        phonenumber = view.findViewById(R.id.phonenumber);
        address = view.findViewById(R.id.address);
        lfullname = view.findViewById(R.id.lfullname);
        lphonenumber = view.findViewById(R.id.lphonenumber);
        laddress = view.findViewById(R.id.laddress);
        //gán giá trị cho biến mainacitvity
        if (getActivity() instanceof TrangChung) {
            mainactivity = (TrangChung) getActivity();
        } else {
            return; // Ngăn chặn lỗi nếu không thể ép kiểu
        }
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user_prefs", Activity.MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "");

        // Lấy toàn bộ thông tin của User;
        Future<User> futureUser = (Future<User>) executorService.submit(new Runnable() {
            @Override
            public void run() {
                user = AppDatabase.getInstance(getContext()).userDAO().checkUser(username);
            }
        });
        try {
            futureUser.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Hiển thị dữ liệu của User lên TextView
        fullname.setText(user.getFullname());
        address.setText(user.getAddress());
        phonenumber.setText(user.getPhone());
        // chỉ dòng code nào cần chạy luồng khác thì mới cho vào executorSerrviec để chạy nhé
        // thường các thao tác với cơ sở dữ liệu thì mới cho vào exceutor để chạy
//        Future<Cart> future = (Future<Cart>) executorService.submit(() -> {
//            listCart = AppDatabase.getInstance(getActivity()).cartDAO().getAllCartsByUserId(username);
//        });
//
//        try {
//            future.get(); // nó sẽ chặn luống chạy của ứng dụng cho đến khi thằng code trong future hoàn thành
//            } catch (Exception e) {
//             e.printStackTrace();
//        }
        paymentAdapter = new PaymentAdapter(getActivity(), listCart);
        recyclerView = view.findViewById(R.id.listMon);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(paymentAdapter);
        // Tính toán tiền cho giỏ hàng

        for (int i = 0; i < listCart.size(); i++) {
            totalCart += listCart.get(i).getCartPrice();
        }
        applyDiscount = totalCart;
        updateTotal(totalCart);

        // xử lý chọn phương thức thanh toán
        showpttt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPaymentDialog();
            }
        });

        // chọn voucher
        showdiscount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDiscoutVoucher(username);
            }
        });

        // nút hủy
        btnHuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                CartFrag cf = new CartFrag();
                mainactivity.replaceFrag(cf, "Giỏ hàng");

            }
        });

        // nút thanh toán
        btnPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean check = true;
                if (fullname.getText().toString().isEmpty()) {
                    lfullname.setError("Require");
                    check = false;
                } else {
                    lfullname.setError("");
                }
                if (phonenumber.getText().toString().isEmpty()) {
                    lphonenumber.setError("Require");
                    check = false;
                } else {
                    lphonenumber.setError("");
                }
                if (address.getText().toString().isEmpty()) {
                    laddress.setError("Require");
                    check = false;
                } else {
                    laddress.setError("");
                }
                if (wayToPay == "") {
                    Toast.makeText(getContext(), "Chưa chọn phương thức thanh toán", Toast.LENGTH_LONG).show();
                    check = false;
                }
                if (check) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
                    String transactionDate = sdf.format(new Date());
                    String fullNameInfo = fullname.getText().toString();
                    String addressInfo = address.getText().toString();
                    String phoneNumberInfo = phonenumber.getText().toString();
                    History history = new History(username, fullNameInfo, transactionDate, applyDiscount, wayToPay, phoneNumberInfo, addressInfo);
                    executorService.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                long lastID = AppDatabase.getInstance(getContext()).historyDAO().insert(history);
                                for (int i = 0; i < listCart.size(); i++) {
                                    HistoryDetail historyDetail = new HistoryDetail((int) lastID, listCart.get(i).getFoodID(), listCart.get(i).getFoodQuantity());
                                    AppDatabase.getInstance(getContext()).daoDetailHistory().insertDetailHistory(historyDetail);
                                }
                                mainactivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                        builder.setTitle("Thanh toán thành công!");
                                        builder.setIcon(R.drawable.baseline_done_24);
                                        builder.setPositiveButton("Về trang chủ", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                mainactivity.replaceFrag(new TrangChu(), "Trang chủ");
                                            }
                                        });
                                        builder.setNegativeButton("Chi tiết đơn hàng", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                HistoryChiTietFrag order = new HistoryChiTietFrag();
                                                Bundle bundle = new Bundle();
                                                bundle.putInt("transactionID", (int) lastID);
                                                order.setArguments(bundle);
                                                mainactivity.replaceFrag(order, "Chi tiết đơn hàng");
                                            }
                                        });
                                        builder.create().show();
                                    }
                                });
                            } catch (Exception e) {
                                mainactivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                        builder.setTitle("Thanh toán thất bại!");
                                        builder.setIcon(R.drawable.baseline_error_24);
                                        builder.create().show();
                                    }
                                });

                            }

                        }
                    });
                } else {
                    return;
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.thanh_toan_frag, container, false);
    }

    // hiện box lựa chọn phương thức thanh toán
    private void showPaymentDialog() {
        String[] paymentMethods = {"Thanh toán bằng tiền mặt", "Thanh toán bằng thẻ", "Ví điện tử"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Chọn phương thức thanh toán");

        builder.setSingleChoiceItems(paymentMethods, selectionPosition, (dialog, which) -> {
            wayToPay = paymentMethods[which];
            selectionPosition = which;
        });

        builder.setPositiveButton("OK", (dialog, which) -> {

        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private void showDiscoutVoucher(String username) {
        // lấy ra danh sách mã Voucher của khách hàng đó
        //
        Future<VoucherDetail> fvc = (Future<VoucherDetail>) executorService.submit(new Runnable() {
            @Override
            public void run() {
                listVoucherCustomer = AppDatabase.getInstance(getContext()).voucherDetaildao().getVoucherOfCustomer(username);
            }

        });

        try {
            fvc.get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Chuẩn bị dữ liệu để hiển thị lên alertdialog
        String CodeVoucher[] = new String[listVoucherCustomer.size()];
        //Tù đó lấy ra chi tiết voucher đó ánh xạ sang bảng tblVoucher
        List<Voucher> listVoucher = new ArrayList<>();
        Future<Voucher> fv = (Future<Voucher>) executorService.submit(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < listVoucherCustomer.size(); i++) {
                    VoucherDetail vd = listVoucherCustomer.get(i);
                    Voucher voucher = AppDatabase.getInstance(getContext()).DaoVoucher().getVoucherByID(vd.getVoucherid());
                    // chỉ lấy các voucher có theer áp dụng cho đơn hàng này
                    if (voucher.getMin() < totalCart) {
                        listVoucher.add(voucher);
                        CodeVoucher[i] = voucher.getVouchercode();
                    }

                }
            }
        });
        try {
            fv.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (listVoucher.isEmpty()) {
            Toast.makeText(getContext(), "Bạn chưa có voucher giảm giá hoặc voucher của bạn không thể áp dụng", Toast.LENGTH_LONG).show();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Voucher giảm giá");
        builder.setIcon(R.drawable.baseline_discount_24);

        // tạo multichoiceitem cho phép chọn nhiều item
        builder.setMultiChoiceItems(CodeVoucher, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which, boolean isChecked) {
                Voucher voucher = listVoucher.get(which);

                if (isChecked) {

                    checkedItems[which] = true;
                    if (voucher.getTypevoucher().equals("freeship")) {
                        applyDiscount = applyDiscount - 30000;
                    } else
                        applyDiscount = (float) (totalCart - (totalCart * voucher.getDiscount_percent() / 100));
                    updateTotal(applyDiscount);
                } else {
                    checkedItems[which] = false;
                    if (voucher.getTypevoucher().equals("freeship")) {
                        applyDiscount = applyDiscount + 30000;
                    } else
                        applyDiscount = (float) (applyDiscount + (totalCart * voucher.getDiscount_percent() / 100));
                    updateTotal(applyDiscount);
                }
            }
        });
        builder.setPositiveButton("OK", (dialogInterface, i) -> {

        });

        builder.create().show();
    }

    void updateTotal(float totalCart) {
        // xử lý tiền tệ
        Locale locale = new Locale("vi", "VN");
        Currency currency = Currency.getInstance(locale);
        NumberFormat numberFormat = NumberFormat.getCurrencyInstance(locale);
        numberFormat.setCurrency(currency);
        String strTongTien = numberFormat.format(totalCart);
        strTongTien = strTongTien.substring(0, strTongTien.length() - 2);
        tongtien.setText(strTongTien + " VND");
    }
}