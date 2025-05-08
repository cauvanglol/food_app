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
import com.example.oderfoodapp.object.Food;
import com.example.oderfoodapp.recyclerViewAdapter.QuanLyMonAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QuanLyMon extends AppCompatActivity {

    private EditText edtFoodId, edtFoodName, edtDescription, edtCategoryID, edtQuantity, edtPrice;
    private Button btnAddImg, btnAdd, btnUpdate, btnDelete;
    private RecyclerView rcvFoodManage;
    private TextView txtImg;

    private QuanLyMonAdapter qlAdapter;
    private List<Food> mListFood;

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_PERMISSION = 100;

    private Uri selectedImageUri;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private void deleteDatabase() {
        // Đường dẫn và tên của cơ sở dữ liệu
        String databaseName = "OrderFoodApp.db";

        // Xóa cơ sở dữ liệu
        boolean isDeleted = this.deleteDatabase(databaseName);

        if (isDeleted) {
            Toast.makeText(this, "Đã xóa cơ sở dữ liệu thành công!", Toast.LENGTH_SHORT).show();
            refreshData(); // Làm mới dữ liệu hiển thị
        } else {
            Toast.makeText(this, "Xóa cơ sở dữ liệu thất bại!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //deleteDatabase();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.quanlymon);

        init();

        // khi nhấn vào item thì sẽ gán vào các edittext
        qlAdapter = new QuanLyMonAdapter(food -> {
            edtFoodId.setText(food.getFoodID());
            edtFoodName.setText(food.getName());
            edtDescription.setText(food.getDescription());
            edtQuantity.setText(String.valueOf(food.getQuantity()));
            edtPrice.setText(String.valueOf(food.getPrice()));
            edtCategoryID.setText(food.getCategoriesID());
            txtImg.setText("Ảnh đã chọn");
            txtImg.setTextColor(Color.GREEN);

        });

        rcvFoodManage.setLayoutManager(new LinearLayoutManager(this));
        rcvFoodManage.setAdapter(qlAdapter);

        checkStoragePermission();

        btnAddImg.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                checkStoragePermission();
            }
        });

        btnAdd.setOnClickListener(view -> addFood());

        btnUpdate.setOnClickListener(view -> updateFood());

        btnDelete.setOnClickListener(view -> deleteFood());

        refreshData();
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

    private void addFood() {
        String sFoodID = edtFoodId.getText().toString().trim();
        String sFoodName = edtFoodName.getText().toString().trim();
        String sDescription = edtDescription.getText().toString().trim();
        String sCategoryID = edtCategoryID.getText().toString().trim();
        String sQuantity = edtQuantity.getText().toString().trim();
        String sPrice = edtPrice.getText().toString().trim();

        if (TextUtils.isEmpty(sFoodID) || TextUtils.isEmpty(sFoodName) ||
                TextUtils.isEmpty(sDescription) || TextUtils.isEmpty(sCategoryID) ||
                TextUtils.isEmpty(sQuantity) || TextUtils.isEmpty(sPrice) || selectedImageUri == null) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        Food food = new Food(sFoodID, sFoodName, sDescription, Integer.parseInt(sQuantity),
                Float.parseFloat(sPrice), selectedImageUri.toString(), sCategoryID, false);


        executorService.execute(() -> {
            if (isFoodIDExist(food)) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Mã món đã tồn tại!", Toast.LENGTH_SHORT).show();
                });
            } else if (!checkCategoryID(food.getCategoriesID())) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Mã danh mục không tồn tại!", Toast.LENGTH_SHORT).show();
                });
            } else {
                AppDatabase.getInstance(this).foodDAO().insert(food);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Thêm món thành công!", Toast.LENGTH_SHORT).show();
                    resetFormFields();
                    refreshData();
                });
            }
        });
    }

    // Phương thức cập nhật món ăn
    private void updateFood() {
        String sFoodID = edtFoodId.getText().toString().trim();
        String sFoodName = edtFoodName.getText().toString().trim();
        String sDescription = edtDescription.getText().toString().trim();
        String sCategoryID = edtCategoryID.getText().toString().trim();
        String sQuantity = edtQuantity.getText().toString().trim();
        String sPrice = edtPrice.getText().toString().trim();

        if (TextUtils.isEmpty(sFoodID) || TextUtils.isEmpty(sFoodName) ||
                TextUtils.isEmpty(sDescription) || TextUtils.isEmpty(sCategoryID) ||
                TextUtils.isEmpty(sQuantity) || TextUtils.isEmpty(sPrice) || selectedImageUri == null) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        Food food = new Food(sFoodID, sFoodName, sDescription, Integer.parseInt(sQuantity),
                Float.parseFloat(sPrice), selectedImageUri.toString(), sCategoryID, false);

        executorService.execute(() -> {
            AppDatabase.getInstance(this).foodDAO().update(food);
            runOnUiThread(() -> {
                Toast.makeText(this, "Cập nhật món thành công!", Toast.LENGTH_SHORT).show();
                resetFormFields();
                refreshData();
            });
        });
    }

    // Phương thức xóa món ăn
    private void deleteFood() {
        String sFoodID = edtFoodId.getText().toString().trim();

        if (TextUtils.isEmpty(sFoodID)) {
            Toast.makeText(this, "Vui lòng nhập mã món cần xóa", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lấy thông tin món ăn từ ID
        executorService.execute(() -> {
            List<Food> foods = AppDatabase.getInstance(this).foodDAO().checkFoodID(sFoodID);
            if (foods != null && !foods.isEmpty()) {
                Food foodToDelete = foods.get(0);

                // Xóa món ăn trong cơ sở dữ liệu
                AppDatabase.getInstance(this).foodDAO().delete(foodToDelete);

                // Xóa ảnh từ thư mục nội bộ nếu có
                File imageFile = new File(foodToDelete.getImage());
                if (imageFile.exists()) {
                    imageFile.delete();
                }

                runOnUiThread(() -> {
                    Toast.makeText(this, "Xóa món thành công!", Toast.LENGTH_SHORT).show();
                    resetFormFields();
                    refreshData();
                });
            } else {
                runOnUiThread(() -> Toast.makeText(this, "Món ăn không tồn tại!", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void refreshData() {
        executorService.execute(() -> {
            mListFood = AppDatabase.getInstance(this).foodDAO().getAllFoods();
            runOnUiThread(() -> qlAdapter.setData(mListFood));
        });
    }

    private void resetFormFields() {
        edtFoodId.setText("");
        edtFoodName.setText("");
        edtDescription.setText("");
        edtCategoryID.setText("");
        edtQuantity.setText("");
        edtPrice.setText("");
        txtImg.setText("Chưa có ảnh");
        txtImg.setTextColor(Color.RED);
        selectedImageUri = null;
    }

    private void init() {
        edtFoodId = findViewById(R.id.edtFoodID);
        edtFoodName = findViewById(R.id.edtFoodName);
        edtDescription = findViewById(R.id.edtDescription);
        edtCategoryID = findViewById(R.id.edtCategoryID);
        edtQuantity = findViewById(R.id.edtQuantity);
        edtPrice = findViewById(R.id.edtPrice);
        btnAdd = findViewById(R.id.btnAdd);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnDelete = findViewById(R.id.btnDelete);
        btnAddImg = findViewById(R.id.btnAddImg);
        txtImg = findViewById(R.id.txtImg);
        rcvFoodManage = findViewById(R.id.rcvFoodManage);
    }

    private boolean isFoodIDExist(Food food){
        List<Food> list = AppDatabase.getInstance(this).foodDAO().checkFoodID(food.getFoodID());
        return list != null && !list.isEmpty();
    }

    private boolean checkCategoryID(String categoryID) {
        // Kiểm tra xem categoryID có tồn tại trong bảng Category không
        int count = AppDatabase.getInstance(this).categoryDAO().checkDanhMucChuaTonTai(categoryID);
        return count > 0;
    }

}
