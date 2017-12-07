package com.hipad.bluetoothantilost.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hipad.bluetoothantilost.R;

import java.util.List;

/**
 * Created by wk
 */

public class BleAdapter extends BaseAdapter{
    private List<BluetoothDevice> bltList;
    private LayoutInflater mInflater;

    public BleAdapter(Context context,List<BluetoothDevice> list){
        this.bltList = list;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return bltList == null ? 0 : bltList.size();
    }

    @Override
    public Object getItem(int position) {
        return bltList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BleViewHolder holder;
        if (convertView == null){
            convertView = mInflater.inflate(R.layout.item_blt,null);
            holder = new BleViewHolder(convertView);
            convertView.setTag(holder);
        }else {
            holder = (BleViewHolder) convertView.getTag();
        }
        BluetoothDevice device = bltList.get(position);
        holder.blt_name.setText(device.getName() == null ? device.getAddress() : device.getName());
        holder.blt_address.setText(device.getAddress());
        return convertView;
    }


    public class BleViewHolder{
        public TextView blt_name, blt_address;

        public BleViewHolder(View itemView) {
            blt_name = (TextView) itemView.findViewById(R.id.blt_name);
            blt_address = (TextView) itemView.findViewById(R.id.blt_address);
        }

    }

}
