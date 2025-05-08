package com.example.oderfoodapp.fragment;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.oderfoodapp.R;
import com.example.oderfoodapp.TrangChung;
import com.example.oderfoodapp.database.AppDatabase;
import com.example.oderfoodapp.object.History;
import com.example.oderfoodapp.object.HistoryDetail;
import com.example.oderfoodapp.recyclerViewAdapter.HistoryChiTietAdapter;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class HistoryChiTietFrag extends Fragment {

    History history;
    List<HistoryDetail> historyDetailList;
    TextView totalAmount,transactionDate,fullname,phonenumber,address,wayToPay;
    RecyclerView listMon;
    Button btnBack;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    TrangChung mainActivity;
    private SharedPreferences sharedPreferences;
    String username;
    int transactionID;
    // Khai bao Adapter
    HistoryChiTietAdapter historyChiTietAdapter;

    public HistoryChiTietFrag() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = (TrangChung) getActivity();
        sharedPreferences = getActivity().getSharedPreferences("user_prefs", Activity.MODE_PRIVATE);
        username = sharedPreferences.getString("username", "");
        if(getArguments() != null){
            transactionID = getArguments().getInt("transactionID");
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        listMon = view.findViewById(R.id.listMon);
        btnBack = view.findViewById(R.id.btnBack);
        totalAmount = view.findViewById(R.id.totalAmount);
        transactionDate = view.findViewById(R.id.transactionDate);
        fullname = view.findViewById(R.id.fullname);
        phonenumber = view.findViewById(R.id.phonenumber);
        address = view.findViewById(R.id.address);
        wayToPay = view.findViewById(R.id.wayToPay);

        Future<History> future = (Future<History>)executorService.submit(new Runnable() {
            @Override
            public void run() {
                history = AppDatabase.getInstance(getContext()).historyDAO().getAHistoryByID(transactionID,username);
                historyDetailList = AppDatabase.getInstance(getContext()).daoDetailHistory().getListHistory(transactionID);
            }
        });

        try {
            future.get();
        }catch (Exception e){
            e.printStackTrace();
        }

        //

        // Gắn dữ liệu History lấy được cho các View
        Locale locale = new Locale("vi","VN");
        Currency currency = Currency.getInstance(locale);
        NumberFormat numberFormat = NumberFormat.getCurrencyInstance(locale);
        numberFormat.setCurrency(currency);
        String strTongTien = numberFormat.format(history.getTongtien());
        strTongTien = strTongTien.substring(0,strTongTien.length()-2);
        strTongTien += " VND";
        fullname.setText(history.getFullname());
        address.setText(history.getAddress());
        phonenumber.setText(history.getPhonenumber());
        wayToPay.setText(history.getPttt());
        transactionDate.setText("Ngày đặt hàng  "+ history.getTransactionDate());
        totalAmount.setText(strTongTien);


        historyChiTietAdapter = new HistoryChiTietAdapter(mainActivity,historyDetailList);
        listMon.setLayoutManager(new LinearLayoutManager(mainActivity,LinearLayoutManager.VERTICAL,false));
        listMon.setAdapter(historyChiTietAdapter);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HistoryFrag hf = new HistoryFrag();
                mainActivity.replaceFrag(hf,"Lịch sử mua hàng");
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.history_chitiet_frag, container, false);
    }
}