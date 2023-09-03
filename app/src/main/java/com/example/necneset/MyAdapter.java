package com.example.necneset;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    private List<Prayer> mData;
    private int coming;
    int num;
    public MyAdapter(List<Prayer> data) {
        mData = data;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        String item = mData.get(position).name + ": "+ mData.get(position).hour ;
        holder.text.setText(item);
        String key = InfoBeitCneset.key;

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("locations/"+key +"/prayers/" ).child(String.valueOf(position));
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                num = snapshot.child("/coming").getValue(Integer.class);

                }catch (Exception e){
                    databaseReference.child("coming").setValue(0);
                    num = 0;
                }
                holder.coming.setText(" באים: "+ num);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("onClick", "click:"+ view.toString());
                String key = InfoBeitCneset.key;

                Log.d("keyyy", key + ' ' + position);

                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("locations/"+key +"/prayers/" ).child(String.valueOf(position));
                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users/"+firebaseAuth.getCurrentUser().getUid());
                boolean[] flag = new boolean[2];
                databaseReference.addValueEventListener(new ValueEventListener() {
                                                  @Override
                                                  public void onDataChange(DataSnapshot dataSnapshot) {
                                                      userRef.addValueEventListener(new ValueEventListener() {
                                                          @Override
                                                          public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                             if(snapshot.getChildrenCount() > 0){
                                                                 Toast.makeText(InfoBeitCneset.context,"כבר נרשמת למניין היום. בטל הגעה לפני שאתה נרשם שוב", Toast.LENGTH_LONG).show();
                                                             }else{

                                                                 Handler handler = new Handler();
                                                                 Runnable task = new Runnable() {
                                                                     @Override
                                                                     public void run() {
                                                                             coming = dataSnapshot.child("/coming").getValue(Integer.class);
                                                                             Map<String, Object> updateFields = new HashMap<>();
                                                                             updateFields.put("coming", coming + 1);
                                                                             databaseReference.updateChildren(updateFields);
                                                                             userRef.child(key).setValue(position);
                                                                     };

                                                                 };
                                                                 handler.postDelayed(task, 10);
                                                             }
                                                             userRef.removeEventListener(this);
                                                          }

                                                          @Override
                                                          public void onCancelled(@NonNull DatabaseError error) {

                                                          }
                                                      });

                                                      databaseReference.removeEventListener(this);


                                                  }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


                                    }





        });
    }


    @Override
    public int getItemCount() {
        try {
            return mData.size();
        }catch (Exception e){
            return 0;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView text, coming;
        public Button button;

        public ViewHolder(View itemView) {
            super(itemView);
            text = (TextView) itemView.findViewById(R.id.item_text);
            button = (Button) itemView.findViewById(R.id.item_button);
            coming = (TextView) itemView.findViewById(R.id.item_coming);
        }
    }
}
