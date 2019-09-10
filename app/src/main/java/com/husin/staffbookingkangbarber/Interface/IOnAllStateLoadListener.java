package com.husin.staffbookingkangbarber.Interface;

import com.husin.staffbookingkangbarber.Model.City;

import java.util.List;

public interface IOnAllStateLoadListener {
    void onAllStateLoadSuccess(List<City> cityList);
     void onAllStateLoadFailed(String message);
}
