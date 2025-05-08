package com.example.oderfoodapp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.oderfoodapp.database.AppDatabase;
import com.example.oderfoodapp.object.NhanVien;
import com.example.oderfoodapp.recyclerViewAdapter.NhanVienAdapter;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NvHienActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NhanVienAdapter adapter;
    private List<NhanVien> nhanVienList;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nv_hien);

        recyclerView = findViewById(R.id.recyclerViewNhanVien);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        executorService = Executors.newSingleThreadExecutor();

        loadNhanVienData();
    }

    private void loadNhanVienData() {
        executorService.execute(() -> {
            // Lấy danh sách nhân viên từ Room Database
            nhanVienList = AppDatabase.getInstance(NvHienActivity.this).nhanVienDAO().getAll();

            // Cập nhật giao diện (UI thread)
            runOnUiThread(() -> {
                // Tạo adapter và gán vào RecyclerView
                adapter = new NhanVienAdapter(nhanVienList);
                recyclerView.setAdapter(adapter);
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
