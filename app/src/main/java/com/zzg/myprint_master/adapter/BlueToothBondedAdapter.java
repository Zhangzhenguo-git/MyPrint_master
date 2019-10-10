package com.zzg.myprint_master.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zzg.myprint_master.bean.BlueToothBean;
import com.zzg.myprint_master.R;

import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Zhangzhenguo
 * @create 2019/10/8
 * @Email 18311371235@163.com
 * @Describe
 */
public class BlueToothBondedAdapter extends RecyclerView.Adapter<BlueToothBondedAdapter.Holder> {
    private Context context;
    private List<BluetoothDevice> showData;
    private MyOnItemClickListener myOnItemClickListener;

    public BlueToothBondedAdapter(Context context, List<BluetoothDevice> showData) {
        this.context=context;
        this.showData=showData;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Holder holder=null;
        View view = LayoutInflater.from(context).inflate(R.layout.bluetooth_item_layout, parent, false);
        holder = new Holder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        BluetoothDevice device=showData.get(position);
        holder.tvArg0.setText(device.getName().toString());
        holder.tvArg1.setText(device.getName().toString());
        if (myOnItemClickListener!=null){
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    myOnItemClickListener.onItemClick(showData.get(position));
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        if (showData==null || showData.size()==0){
            return 0;
        }
        return showData.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    class Holder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_Arg0)
        TextView tvArg0;
        @BindView(R.id.tv_Arg1)
        TextView tvArg1;
        public Holder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }

    public interface MyOnItemClickListener{
        void onItemClick(BluetoothDevice... item);
    }
    public void MyOnItemClickListener(MyOnItemClickListener myOnItemClickListener){
        this.myOnItemClickListener=myOnItemClickListener;
    }
}
