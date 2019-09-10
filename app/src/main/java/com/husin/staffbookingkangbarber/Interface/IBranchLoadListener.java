package com.husin.staffbookingkangbarber.Interface;

import com.husin.staffbookingkangbarber.Model.Salon;

import java.util.List;

public interface IBranchLoadListener {
    void onBranchLoadSuccess(List<Salon> branchList);
    void onBranchLoadFailed(String message);

}

