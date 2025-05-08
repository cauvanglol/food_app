package com.example.oderfoodapp.object;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tblNhanVien")
public class NhanVien {
    @PrimaryKey
    @NonNull
    private String MaNV = "";
    private String ten;
    private String chucVu;
    private float HSL;
    private float LCB;
    private String GioiTinh;
    private String DiaChi;

    public NhanVien() {
    }

    public NhanVien(@NonNull String maNV, String ten, String chucVu, float HSL, float LCB, String gioiTinh, String diaChi) {
        MaNV = maNV;
        this.ten = ten;
        this.chucVu = chucVu;
        this.HSL = HSL;
        this.LCB = LCB;
        GioiTinh = gioiTinh;
        DiaChi = diaChi;
    }

    @NonNull
    public String getMaNV() {
        return MaNV;
    }

    public void setMaNV(@NonNull String maNV) {
        MaNV = maNV;
    }

    public String getTen() {
        return ten;
    }

    public void setTen(String ten) {
        this.ten = ten;
    }

    public String getChucVu() {
        return chucVu;
    }

    public void setChucVu(String chucVu) {
        this.chucVu = chucVu;
    }

    public float getHSL() {
        return HSL;
    }

    public void setHSL(float HSL) {
        this.HSL = HSL;
    }

    public float getLCB() {
        return LCB;
    }

    public void setLCB(float LCB) {
        this.LCB = LCB;
    }

    public String getGioiTinh() {
        return GioiTinh;
    }

    public void setGioiTinh(String gioiTinh) {
        GioiTinh = gioiTinh;
    }

    public String getDiaChi() {
        return DiaChi;
    }

    public void setDiaChi(String diaChi) {
        DiaChi = diaChi;
    }
}
