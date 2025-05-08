package com.example.oderfoodapp.object;


import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "tblHistory",
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "username",
                childColumns = "username"
        )
)

public class History {
    @PrimaryKey(autoGenerate = true)
    @NonNull
    private int transactionID;
    private String fullname;
    private String username;
    private String address;
    private String phonenumber;
    private String pttt;
    private float tongtien;
    private String transactionDate;

    public History() {
    }

    public History(String username,String fullname, String transactionDate, float tongtien, String pttt, String phonenumber, String address) {
        this.username = username;
        this.fullname = fullname;
        this.transactionDate = transactionDate;
        this.tongtien = tongtien;
        this.pttt = pttt;
        this.phonenumber = phonenumber;
        this.address = address;
    }

    public int getTransactionID() {
        return transactionID;
    }

    public void setTransactionID(int transactionID) {
        this.transactionID = transactionID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public String getPttt() {
        return pttt;
    }

    public void setPttt(String pttt) {
        this.pttt = pttt;
    }

    public float getTongtien() {
        return tongtien;
    }

    public void setTongtien(float tongtien) {
        this.tongtien = tongtien;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }
}
