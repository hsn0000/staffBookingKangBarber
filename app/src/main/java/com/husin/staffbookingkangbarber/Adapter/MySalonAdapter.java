package com.husin.staffbookingkangbarber.Adapter;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.husin.staffbookingkangbarber.Common.Common;
import com.husin.staffbookingkangbarber.Common.CustomLoginDialog;
import com.husin.staffbookingkangbarber.Interface.IDialogClickListener;
import com.husin.staffbookingkangbarber.Interface.IGetBarberListener;
import com.husin.staffbookingkangbarber.Interface.IRecyclerItemSelectedListener;
import com.husin.staffbookingkangbarber.Interface.IUserLoginRememberListener;
import com.husin.staffbookingkangbarber.Model.Barber;
import com.husin.staffbookingkangbarber.Model.Salon;
import com.husin.staffbookingkangbarber.R;
import com.husin.staffbookingkangbarber.StaffHomeActivity;

import java.util.ArrayList;
import java.util.List;

import dmax.dialog.SpotsDialog;

public class MySalonAdapter extends RecyclerView.Adapter<MySalonAdapter.MyViewHolder> implements IDialogClickListener {

    Context context;
    List<Salon>salonList;
    List<CardView> itemViewList;

    IUserLoginRememberListener iUserLoginRememberListener;
    IGetBarberListener iGetBarberListener;

    public MySalonAdapter(Context context, List<Salon> salonList, IUserLoginRememberListener iUserLoginRememberListener, IGetBarberListener iGetBarberListener) {
        this.context = context;
        this.salonList = salonList;
        itemViewList = new ArrayList<>();
        this.iGetBarberListener = iGetBarberListener;
        this.iUserLoginRememberListener = iUserLoginRememberListener;

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.layout_salon, viewGroup,false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        myViewHolder.txt_salon_name.setText(salonList.get(i).getName());
        myViewHolder.txt_salon_address.setText(salonList.get(i).getAddress());

        if (!itemViewList.contains(myViewHolder.card_salon))
            itemViewList.add(myViewHolder.card_salon);

        myViewHolder.setiRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
            @Override
            public void onItemSelected(View view, int posision) {

                Common.selected_salon = salonList.get(posision);
               showLoginDialog();
            }
        });

    }

    private void showLoginDialog() {
        CustomLoginDialog.getInstance()
                .showLoginDialog("STAFF LOGIN",
                        "LOGIN",
                        "BATAL",
                        context,this);

    }

    @Override
    public int getItemCount() {
        return salonList.size();
    }

    @Override
    public void onClickPositiveButton(DialogInterface dialogInterface, String username, String password) {
        // show loading dialog
        AlertDialog loading = new SpotsDialog.Builder().setCancelable(false)
                .setContext(context).build();

        loading.show();

       // /AllSalon/Bogor/Branch/0N1OCqVh6YJ2hB5Ke97h/Barber/HyM6cZg1jfeP7wOW0AsS
        FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.state_name)
                .collection("Branch")
                .document(Common.selected_salon.getSalonId())
                .collection("Barber")
                .whereEqualTo("username",username)
                .whereEqualTo("password",password)
                .limit(1)
                .get()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context,e.getMessage(), Toast.LENGTH_SHORT).show();
                        loading.dismiss();
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful())
                        {
                           if (task.getResult().size() > 0)
                           {
                              dialogInterface.dismiss();

                              loading.dismiss();

                              iUserLoginRememberListener.onUserLoginSuccess(username);

                              // buat barber
                               Barber barber = new Barber();
                               for (DocumentSnapshot barberSnapShot:task.getResult())
                               {
                                   barber = barberSnapShot.toObject(Barber.class);
                                   barber.setBarberId(barberSnapShot.getId());
                               }
                               iGetBarberListener.onGetBarberSuccess(barber);

                              // navigasi staff
                               Intent stafHome = new Intent(context, StaffHomeActivity.class);
                               stafHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                               stafHome.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                               context.startActivity(stafHome);
                           }
                           else
                           {
                               loading.dismiss();
                               Toast.makeText(context, "Nama pengguna / kata sandi salah, atau salon salah !!", Toast.LENGTH_SHORT).show();
                           }
                        }
                    }
                });

    }

    @Override
    public void onClickNegativeButton(DialogInterface dialogInterface) {
        dialogInterface.dismiss();

    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView txt_salon_name,txt_salon_address;
        CardView card_salon;

        IRecyclerItemSelectedListener iRecyclerItemSelectedListener;

        public void setiRecyclerItemSelectedListener(IRecyclerItemSelectedListener iRecyclerItemSelectedListener) {
            this.iRecyclerItemSelectedListener = iRecyclerItemSelectedListener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            card_salon = (CardView)itemView.findViewById(R.id.card_salon);
            txt_salon_address = (TextView)itemView.findViewById(R.id.txt_salon_address);
            txt_salon_name = (TextView)itemView.findViewById(R.id.txt_salon_name);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            iRecyclerItemSelectedListener.onItemSelected(view,getAdapterPosition());

        }
    }
}
