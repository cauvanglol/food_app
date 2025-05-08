package com.example.oderfoodapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.oderfoodapp.database.AppDatabase;
import com.example.oderfoodapp.object.Category;
import com.example.oderfoodapp.recyclerViewAdapter.QuanLyDanhMucAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QuanLyDanhMuc extends AppCompatActivity {
    private EditText edtCategoryId, edtCategoryName;
    private Button btnAddImg, btnAdd, btnUpdate, btnDelete;
    private RecyclerView rcvCategoryManage;
    private TextView txtImg;

    private QuanLyDanhMucAdapter qlDanhMucAdapter;
    private List<Category> mListCategory;

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_PERMISSION = 100;

    private Uri selectedImageUri;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quanlydanhmuc);

        init();

        qlDanhMucAdapter = new QuanLyDanhMucAdapter(category -> {
            edtCategoryId.setText(category.getCategoryID());
            edtCategoryName.setText(category.getName());
            txtImg.setText("Ảnh đã chọn");
            txtImg.setTextColor(Color.GREEN);
        });

        rcvCategoryManage.setLayoutManager(new LinearLayoutManager(this));
        rcvCategoryManage.setAdapter(qlDanhMucAdapter);

        checkStoragePermission();

        btnAddImg.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                checkStoragePermission();
            }
        });

        btnAdd.setOnClickListener(view -> addCategory());
        refreshData();

        btnUpdate.setOnClickListener(view -> updateCategory());
        btnDelete.setOnClickListener(view -> deleteCategory());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
        }
    }

    // phương thức lấy ảnh từ bộ nhớ app
    private String copyImageToInternalStorage(Uri imageUri) {
        File imageFolder = new File(getFilesDir(), "images");
        if (!imageFolder.exists()) {
            imageFolder.mkdirs();
        }

        String fileName = UUID.randomUUID().toString() + ".jpg"; // Tạo tên ngẫu nhiên
        File imageFile = new File(imageFolder, fileName);

        try (InputStream inputStream = getContentResolver().openInputStream(imageUri);
             FileOutputStream outputStream = new FileOutputStream(imageFile)) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            return imageFile.getAbsolutePath(); // Trả về đường dẫn nội bộ của ảnh
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                String internalImagePath = copyImageToInternalStorage(selectedImageUri);
                if (internalImagePath != null) {
                    txtImg.setText("Ảnh đã chọn");
                    txtImg.setTextColor(Color.GREEN);
                    selectedImageUri = Uri.parse(internalImagePath); // Cập nhật `Uri` với đường dẫn nội bộ
                }
            }
        }
    }

    private void addCategory() {
        String sCategoryID = edtCategoryId.getText().toString().trim();
        String sCategoryName = edtCategoryName.getText().toString().trim();

        if (TextUtils.isEmpty(sCategoryID) || TextUtils.isEmpty(sCategoryName) || selectedImageUri == null) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        Category category = new Category(sCategoryID, sCategoryName, selectedImageUri.toString());


        executorService.execute(() -> {
            if (isCategoryIDExist(category)) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Mã danh mục đã tồn tại!", Toast.LENGTH_SHORT).show();
                });
            } else {
                AppDatabase.getInstance(this).categoryDAO().insert(category);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Thêm danh mục thành công!", Toast.LENGTH_SHORT).show();
                    resetFormFields();
                    refreshData();
                });
            }

        });
    }

    // Phương thức cập nhật món ăn
    private void updateCategory() {
        String sCategoryID = edtCategoryId.getText().toString().trim();
        String sCategoryName = edtCategoryName.getText().toString().trim();

        if (TextUtils.isEmpty(sCategoryID) || TextUtils.isEmpty(sCategoryName) || selectedImageUri == null) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        Category category = new Category(sCategoryID, sCategoryName, selectedImageUri.toString());

        executorService.execute(() -> {
            AppDatabase.getInstance(this).categoryDAO().update(category);
            runOnUiThread(() -> {
                Toast.makeText(this, "Cập nhật danh mục thành công!", Toast.LENGTH_SHORT).show();
                resetFormFields();
                refreshData();
            });
        });
    }

    // Phương thức xóa món ăn
    private void deleteCategory() {
        String sCategoryID = edtCategoryId.getText().toString().trim();

        if (TextUtils.isEmpty(sCategoryID)) {
            Toast.makeText(this, "Vui lòng nhập mã danh mục cần xóa", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lấy thông tin món ăn từ ID
        executorService.execute(() -> {
            List<Category> categories = AppDatabase.getInstance(this).categoryDAO().checkCategoryID(sCategoryID);
            if (categories != null && !categories.isEmpty()) {
                Category categoryToDelete = categories.get(0);

                // Xóa món ăn trong cơ sở dữ liệu
                AppDatabase.getInstance(this).categoryDAO().delete(categoryToDelete);

                // Xóa ảnh từ thư mục nội bộ nếu có
                File imageFile = new File(categoryToDelete.getImage());
                if (imageFile.exists()) {
                    imageFile.delete();
                }

                runOnUiThread(() -> {
                    Toast.makeText(this, "Xóa danh mục thành công!", Toast.LENGTH_SHORT).show();
                    resetFormFields();
                    refreshData();
                });
            } else {
                runOnUiThread(() -> Toast.makeText(this, "Danh mục không tồn tại!", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void refreshData() {
        executorService.execute(() -> {
            mListCategory = AppDatabase.getInstance(this).categoryDAO().getAllCategory();
            runOnUiThread(() -> qlDanhMucAdapter.setData(mListCategory));
        });
    }

    private void resetFormFields() {
        edtCategoryId.setText("");
        edtCategoryName.setText("");
        txtImg.setText("Chưa có ảnh");
        txtImg.setTextColor(Color.RED);
        selectedImageUri = null;
    }

    private void init() {
        edtCategoryId = findViewById(R.id.edtDanhMucID);
        edtCategoryName = findViewById(R.id.edtTenDanhMuc);
        btnAdd = findViewById(R.id.btnAddCategory);
        btnUpdate = findViewById(R.id.btnUpdateCategory);
        btnDelete = findViewById(R.id.btnDeleteCategory);
        btnAddImg = findViewById(R.id.btnAddImgCategory);
        txtImg = findViewById(R.id.txtImgCategory);
        rcvCategoryManage = findViewById(R.id.rcvCategoryManage);
    }

    private boolean isCategoryIDExist(Category category){
        List<Category> list = AppDatabase.getInstance(this).categoryDAO().checkCategoryID(category.getCategoryID());
        return list != null && !list.isEmpty();
    }
}
