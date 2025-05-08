package com.example.oderfoodapp.recyclerViewAdapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.oderfoodapp.R;
import com.example.oderfoodapp.object.NhanVien;

import java.util.List;

public class NhanVienAdapter extends RecyclerView.Adapter<NhanVienAdapter.NhanVienViewHolder> {

    private List<NhanVien> nhanVienList;

    // Constructor để nhận danh sách nhân viên
    public NhanVienAdapter(List<NhanVien> nhanVienList) {
        this.nhanVienList = nhanVienList;
    }

    @NonNull
    @Override
    public NhanVienViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate layout cho mỗi item trong RecyclerView
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.nv_hien_item, parent, false);
        return new NhanVienViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NhanVienViewHolder holder, int position) {
        // Lấy đối tượng nhân viên tại vị trí hiện tại
        NhanVien nhanVien = nhanVienList.get(position);

        // Bind dữ liệu vào các TextView trong item layout
        holder.tvMaNV.setText(nhanVien.getMaNV());
        holder.tvTen.setText(nhanVien.getTen());
        holder.tvChucVu.setText(nhanVien.getChucVu());
        holder.tvHSL.setText("Hệ số lương: " + nhanVien.getHSL());
        holder.tvLCB.setText("Lương cơ bản: " + nhanVien.getLCB());
        //holder.tvGioiTinh.setText("Giới tính: " + nhanVien.getGioiTinh());
        holder.tvGioiTinh.setText("Giới tính: " + (nhanVien.getGioiTinh().equals("1") ? "Nam" : "Nữ"));

        holder.tvDiaChi.setText("Địa chỉ: " + nhanVien.getDiaChi());
    }

    @Override
    public int getItemCount() {
        // Trả về số lượng phần tử trong danh sách
        return nhanVienList.size();
    }

    // ViewHolder giữ các tham chiếu tới các views trong item layout
    public static class NhanVienViewHolder extends RecyclerView.ViewHolder {
        TextView tvMaNV, tvTen, tvChucVu, tvHSL, tvLCB, tvGioiTinh, tvDiaChi;

        public NhanVienViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ các TextView trong item layout
            tvMaNV = itemView.findViewById(R.id.tvMaNV);
            tvTen = itemView.findViewById(R.id.tvTen);
            tvChucVu = itemView.findViewById(R.id.tvChucVu);
            tvHSL = itemView.findViewById(R.id.tvHSL);
            tvLCB = itemView.findViewById(R.id.tvLCB);
            tvGioiTinh = itemView.findViewById(R.id.tvGioiTinh);
            tvDiaChi = itemView.findViewById(R.id.tvDiaChi);
        }
    }

    // Hàm cập nhật danh sách nhân viên và thông báo RecyclerView cập nhật dữ liệu
    public void updateData(List<NhanVien> newNhanVienList) {
        nhanVienList = newNhanVienList;
        notifyDataSetChanged();
    }
}
