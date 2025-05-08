package com.example.oderfoodapp.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.oderfoodapp.R;
import com.example.oderfoodapp.database.AppDatabase;
import com.example.oderfoodapp.object.User;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ManageAccFrag extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_PERMISSION_STORAGE = 100;
    private static final int REQUEST_PERMISSION = 100;

    private EditText edtUsername, edtEmail, edtAddress, edtPhone;
    private ImageView imgAvatar;
    private TextView txtImgAvatarQL;
    private Button btnChangeAvatar, btnSave;
    private SharedPreferences sharedPreferences;
    private String currentUsername;

    public ManageAccFrag() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.manage_acc_frag, container, false);

        // Initialize views
        edtUsername = view.findViewById(R.id.edtUsernameQL);
        edtEmail = view.findViewById(R.id.edtEmailQL);
        edtAddress = view.findViewById(R.id.edtAddressQL);
        edtPhone = view.findViewById(R.id.edtPhoneQL);
        imgAvatar = view.findViewById(R.id.imgAnhDaiDien);
        btnChangeAvatar = view.findViewById(R.id.btnChangeAvatar);
        btnSave = view.findViewById(R.id.btnSaveQL);
        txtImgAvatarQL = view.findViewById(R.id.txtImgAvatarQL);

        // Load SharedPreferences to get the current logged-in user data
        sharedPreferences = getActivity().getSharedPreferences("user_prefs", Activity.MODE_PRIVATE);
        currentUsername = sharedPreferences.getString("username", "");

        if (!TextUtils.isEmpty(currentUsername)) {
            loadUserData(currentUsername);
        }

        // Button to change avatar
        btnChangeAvatar.setOnClickListener(v -> {
            /*
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                // Nếu quyền đã được cấp, mở thư viện ảnh
                openGallery();
            } else {
                // Yêu cầu cấp quyền nếu chưa có
                requestStoragePermission();
            }

            Toast.makeText(getContext(), "Chức năng đang bảo trì!", Toast.LENGTH_SHORT).show();*/
            if (ContextCompat.checkSelfPermission(this.getContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                checkStoragePermission();
            }
        });

        // Save button to save user information
        btnSave.setOnClickListener(v -> saveUserData());

        return view;
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(this.getContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this.getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
        }
    }

    /*
    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Toast.makeText(getContext(), "Ứng dụng cần quyền để chọn ảnh từ thư viện", Toast.LENGTH_SHORT).show();
        }
        ActivityCompat.requestPermissions(requireActivity(),
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                REQUEST_PERMISSION_STORAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSION_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(getContext(), "Quyền truy cập bị từ chối", Toast.LENGTH_SHORT).show();
            }
        }
    }
     */

    private String saveImageToInternalStorage(Uri imageUri) {
        File imageFolder = new File(requireContext().getFilesDir(), "images");
        if (!imageFolder.exists()) {
            imageFolder.mkdirs();
        }

        String fileName = UUID.randomUUID().toString() + ".jpg"; // Tạo tên ngẫu nhiên
        File imageFile = new File(imageFolder, fileName);

        try (InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
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


    private void loadUserData(String username) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            User user = AppDatabase.getInstance(getContext()).userDAO().checkUser(username);
            getActivity().runOnUiThread(() -> {
                if (user != null) {
                    edtUsername.setText(user.getUsername());
                    edtEmail.setText(user.getEmail());
                    edtAddress.setText(user.getAddress());
                    edtPhone.setText(user.getPhone());

                    // Load avatar from SharedPreferences
                    String avatarUri = sharedPreferences.getString("avatar", "");
                    if (!TextUtils.isEmpty(avatarUri)) {
                        imgAvatar.setImageURI(Uri.parse(user.getAvatar())); // Cập nhật ảnh bằng URI
                    } else {
                        imgAvatar.setImageResource(R.drawable.profile); // Avatar mặc định
                    }
                }
            });
        });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == PICK_IMAGE_REQUEST) {
            Uri selectedImageUri = data.getData();
            String savedImagePath = saveImageToInternalStorage(selectedImageUri);
            imgAvatar.setImageURI(Uri.parse(savedImagePath));
            txtImgAvatarQL.setText("Ảnh đã chọn");
            txtImgAvatarQL.setTextColor(Color.BLUE);

            // Save the selected image URI to SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("avatar", savedImagePath);
            editor.apply();

        }
        /*super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == PICK_IMAGE_REQUEST) {
            Uri selectedImageUri = data.getData();

            // Lưu ảnh vào bộ nhớ trong
            String savedImagePath = saveImageToInternalStorage(selectedImageUri);
            if (savedImagePath != null) {
                imgAvatar.setImageURI(Uri.parse(savedImagePath));
                txtImgAvatarQL.setText("Ảnh đã chọn");
                txtImgAvatarQL.setTextColor(Color.BLUE);

                // Lưu đường dẫn vào SharedPreferences
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("avatar", savedImagePath);
                editor.apply();
            } else {
                Toast.makeText(getContext(), "Lưu ảnh thất bại", Toast.LENGTH_SHORT).show();
            }
        }*/
    }

    private void saveUserData() {
        String username = edtUsername.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email) || TextUtils.isEmpty(address) || TextUtils.isEmpty(phone)) {
            Toast.makeText(getContext(), "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            User user = AppDatabase.getInstance(getContext()).userDAO().checkUser(username);

            if (user == null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Username không tồn tại", Toast.LENGTH_SHORT).show();
                });
                return;
            }

            // Cập nhật thông tin người dùng trong cơ sở dữ liệu
            user.setEmail(email);
            user.setAddress(address);
            user.setPhone(phone);

            // Lấy avatar URI từ SharedPreferences
            String avatarUri = sharedPreferences.getString("avatar", "");
            if (!TextUtils.isEmpty(avatarUri)) {
                user.setAvatar(avatarUri);
            }

            // Cập nhật cơ sở dữ liệu
            AppDatabase.getInstance(getContext()).userDAO().update(user);

            // Lưu thông tin đã cập nhật vào SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("email", email);
            editor.putString("address", address);
            editor.putString("phone", phone);
            editor.putString("avatar", avatarUri);
            editor.apply();

            // Cập nhật lại giao diện
            getActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), "Thông tin đã được cập nhật", Toast.LENGTH_SHORT).show();
                loadUserData(currentUsername); // Gọi lại loadUserData để cập nhật lại giao diện với dữ liệu mới
            });
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserData(currentUsername);
    }
}
