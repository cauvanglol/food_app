package com.example.oderfoodapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.oderfoodapp.database.AppDatabase;
import com.example.oderfoodapp.object.NhanVien;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NhanVienAc extends AppCompatActivity {

    private EditText edtMaNV, edtTen, edtChucVu, edtHSL, edtLCB, edtGioiTinh, edtDiaChi;
    private Button btnLuuNhanVien, btnhien;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nv_nhap);

        // Khởi tạo các View
        edtMaNV = findViewById(R.id.edtMaNV);
        edtTen = findViewById(R.id.edtTen);
        edtChucVu = findViewById(R.id.edtChucVu);
        edtHSL = findViewById(R.id.edtHSL);
        edtLCB = findViewById(R.id.edtLCB);
        edtGioiTinh = findViewById(R.id.edtGioiTinh);
        edtDiaChi = findViewById(R.id.edtDiaChi);
        btnLuuNhanVien = findViewById(R.id.btnSave);
        btnhien = findViewById(R.id.btnhiennv);

        // Tạo ExecutorService để chạy các tác vụ nền
        executorService = Executors.newSingleThreadExecutor();

        // Cài đặt sự kiện cho nút lưu
        btnLuuNhanVien.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lấy thông tin từ các EditText
                String maNV = edtMaNV.getText().toString().trim();
                String ten = edtTen.getText().toString().trim();
                String chucVu = edtChucVu.getText().toString().trim();
                String hslStr = edtHSL.getText().toString().trim();
                String lcbStr = edtLCB.getText().toString().trim();
                String gioiTinh = edtGioiTinh.getText().toString().trim();
                String diaChi = edtDiaChi.getText().toString().trim();

                // Kiểm tra nếu các trường không bị trống
                if (maNV.isEmpty() || ten.isEmpty() || chucVu.isEmpty() ||
                        hslStr.isEmpty() || lcbStr.isEmpty() || gioiTinh.isEmpty() || diaChi.isEmpty()) {
                    Toast.makeText(NhanVienAc.this, "Vui lòng nhập đủ thông tin!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Chuyển đổi HSL và LCB từ String sang float
                float hsl = Float.parseFloat(hslStr);
                float lcb = Float.parseFloat(lcbStr);

                // Tạo đối tượng NhanVien
                NhanVien nhanVien = new NhanVien(maNV, ten, chucVu, hsl, lcb, gioiTinh, diaChi);

                // Thực hiện lưu nhân viên trong ExecutorService (luồng nền)
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        // Lưu nhân viên vào Room Database
                        AppDatabase.getInstance(NhanVienAc.this).nhanVienDAO().insert(nhanVien);

                        // Cập nhật UI sau khi lưu dữ liệu (chạy trên UI thread)
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // Thông báo thành công và làm trống các trường nhập liệu
                                Toast.makeText(NhanVienAc.this, "Nhân viên đã được lưu!", Toast.LENGTH_SHORT).show();

                                // Chuyển tới màn hình hiển thị danh sách nhân viên (NvHienActivity)
                                Intent intent = new Intent(NhanVienAc.this, NvHienActivity.class);
                                startActivity(intent);
                                clearFields();
                            }
                        });
                    }
                });
            }
        });

        btnhien.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(NhanVienAc.this, NvHienActivity.class);
                startActivity(intent);
            }
        });


    }

    private void clearFields() {
        edtMaNV.setText("");
        edtTen.setText("");
        edtChucVu.setText("");
        edtHSL.setText("");
        edtLCB.setText("");
        edtGioiTinh.setText("");
        edtDiaChi.setText("");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
