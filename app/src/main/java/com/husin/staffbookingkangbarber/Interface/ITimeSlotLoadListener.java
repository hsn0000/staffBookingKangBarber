package com.husin.staffbookingkangbarber.Interface;

import com.husin.staffbookingkangbarber.Model.TimeSlot;

import java.util.List;

public interface ITimeSlotLoadListener {
    void onTimeSlotLoadSuccess (List<TimeSlot> timeSlotList);
    void onTimeSlotLoadFailed (String message);
    void onTimeSlotLoadEmpty();
}
