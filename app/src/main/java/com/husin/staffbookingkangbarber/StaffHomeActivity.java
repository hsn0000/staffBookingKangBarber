package com.husin.staffbookingkangbarber;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.husin.staffbookingkangbarber.Adapter.MyTimeSlotAdapter;
import com.husin.staffbookingkangbarber.Common.Common;
import com.husin.staffbookingkangbarber.Common.SpacesItemDecoration;
import com.husin.staffbookingkangbarber.Interface.ITimeSlotLoadListener;
import com.husin.staffbookingkangbarber.Model.TimeSlot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.HorizontalCalendarView;
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;

import static java.security.AccessController.getContext;

public class StaffHomeActivity extends AppCompatActivity implements ITimeSlotLoadListener {

    @BindView(R.id.activity_main)
    DrawerLayout drawerLayout;

    @BindView(R.id.navigation_view)
    NavigationView navigationView;

    ActionBarDrawerToggle actionBarDrawerToggle;

    //***
    DocumentReference barberDoc;
    ITimeSlotLoadListener iTimeSlotLoadListener;
    AlertDialog alertDialog;

    @BindView(R.id.recycler_time_slot)
    RecyclerView recycler_time_slot;
    @BindView(R.id.calendarView)
    HorizontalCalendarView calendarView;
    SimpleDateFormat simpleDateFormat;
    //***
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_home);

        ButterKnife.bind(this);

        init();
        initView();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item))
            return true;
        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout,
                R.string.open,
                R.string.close);

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.menu_exit)
                    logOut();
                return true;
            }
        });

        //**
        alertDialog = new SpotsDialog.Builder().setCancelable(false).setContext(this).build();

        Calendar date = Calendar.getInstance();
        date.add(Calendar.DATE,0);
        loadAvailableTimeSlotOfBarber(Common.currentBarber.getBarberId(),
                Common.simpleDateFormat.format(date.getTime()));

        recycler_time_slot.setHasFixedSize(true);
        GridLayoutManager LayoutManager = new GridLayoutManager(this,3);
        recycler_time_slot.setLayoutManager(LayoutManager);
        recycler_time_slot.addItemDecoration(new SpacesItemDecoration(8));

        // calendar
        Calendar starDate = Calendar.getInstance();
        starDate.add(Calendar.DATE,0);
        Calendar endDate = Calendar.getInstance();
        endDate.add(Calendar.DATE,2);

        HorizontalCalendar horizontalCalendar = new HorizontalCalendar.Builder(this,R.id.calendarView)
                .range(starDate,endDate)
                .datesNumberOnScreen(1)
                .mode(HorizontalCalendar.Mode.DAYS)
                .defaultSelectedDate(starDate)
                .configure()
                .end()
                .build();

        horizontalCalendar.setCalendarListener(new HorizontalCalendarListener() {
            @Override
            public void onDateSelected(Calendar date, int position) {
                if (Common.bookingDate.getTimeInMillis() != date.getTimeInMillis())
                {
                    Common.bookingDate = date; // kode ini tida akan meload lagi jika memilih hari baru dengan hari yg di pilih
                    loadAvailableTimeSlotOfBarber(Common.currentBarber.getBarberId(),
                            simpleDateFormat.format(date.getTime()));
                }
            }
        });

        // end *
    }

    private void logOut() {
        // menghapus semua Remember Key dan start MainActivity
        Paper.init(this);
        Paper.book().delete(Common.SALON_KEY);
        Paper.book().delete(Common.BARBER_KEY);
        Paper.book().delete(Common.STATE_KEY);
        Paper.book().delete(Common.LOGGED_KEY);

        new AlertDialog.Builder(this)
                .setMessage("Apa anda yakin ingin keluar?")
                .setCancelable(false)
                .setPositiveButton(" IYAH ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {

                        Intent mainActivity = new Intent(StaffHomeActivity.this,MainActivity.class);
                        mainActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        mainActivity.addFlags(Intent. FLAG_ACTIVITY_NEW_TASK);
                        startActivity(mainActivity);
                        finish();

                    }
                }).setNegativeButton(" BATAL ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).show();
    }

    private void loadAvailableTimeSlotOfBarber( final String barberId,final String bookDate) {
        // **
        alertDialog.show();

        barberDoc = FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.state_name)
                .collection("Branch")
                .document(Common.selected_salon.getSalonId())
                .collection("Barber")
                .document(barberId);

        barberDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful())
                {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if(documentSnapshot.exists()) // jika barber tersedia
                    {
                        // ambil informasi dari pemesanan
                        //  jika tida di buat, kembalikan kosong
                        CollectionReference date =  FirebaseFirestore.getInstance()
                                .collection("AllSalon")
                                .document(Common.state_name)
                                .collection("Branch")
                                .document(Common.selected_salon.getSalonId())
                                .collection("Barber")
                                .document(barberId)
                                .collection(bookDate); // format tanggal sederhana  dengan dd_MM_yyyy 04_09_2019

                        date.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful())
                                {
                                    QuerySnapshot querySnapshot = task.getResult();
                                    if (querySnapshot.isEmpty()) // jika tida punya janji apapun
                                        iTimeSlotLoadListener.onTimeSlotLoadEmpty();
                                    else
                                    {
                                        // jika punya janji
                                        List<TimeSlot> timeSlots = new ArrayList<>();
                                        for (QueryDocumentSnapshot document:task.getResult())
                                            timeSlots.add(document.toObject(TimeSlot.class));
                                        iTimeSlotLoadListener.onTimeSlotLoadSuccess(timeSlots);
                                    }
                                }

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                iTimeSlotLoadListener.onTimeSlotLoadFailed(e.getMessage());
                            }
                        });

                    }
                }

            }
        });


        // end
    }

    private void init() {
        iTimeSlotLoadListener = this;

    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Anda yakin ingin keluar?")
                .setCancelable(false)
                .setPositiveButton("IYAH", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        Toast.makeText(StaffHomeActivity.this, "pungsi keluar ini palsu :D", Toast.LENGTH_SHORT).show();
                    }
                }).setNegativeButton("BATAL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).show();
    }

    @Override
    public void onTimeSlotLoadSuccess(List<TimeSlot> timeSlot) {
        MyTimeSlotAdapter adapter = new MyTimeSlotAdapter(this,timeSlot);
        recycler_time_slot.setAdapter(adapter);

        alertDialog.dismiss();

    }

    @Override
    public void onTimeSlotLoadFailed(String message) {
        Toast.makeText(this,""+message,Toast.LENGTH_SHORT).show();
        alertDialog.dismiss();

    }

    @Override
    public void onTimeSlotLoadEmpty() {
        MyTimeSlotAdapter adapter = new MyTimeSlotAdapter(this);
        recycler_time_slot.setAdapter(adapter);

        alertDialog.dismiss();
    }
}
