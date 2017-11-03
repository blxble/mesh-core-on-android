package com.blxble.meshpanel.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.anarchy.classify.simple.widget.MiViewHolder;
import com.blxble.meshpanel.MeshApplication;
import com.blxble.meshpanel.R;
import com.blxble.meshpanel.db.DeviceNode;
import com.blxble.meshpanel.db.DeviceNodeGroup;
import com.squareup.picasso.Picasso;

import java.util.List;


public class MockFolderAdapter extends FolderAdapter<MockFolderAdapter.ViewHolder> {

    public final String TAG = "MockFolderAdapter";
    public final static byte ITEM_SINGLE_DEVICE = 1;
    public final static byte ITEM_MORE_DEVICES = 2;
    public OnItemClickListener onItemClickListener;

    public interface OnItemClickListener{
        void OnItemClick(int mainPosition, int subPosition);
    }

    public MockFolderAdapter(List<DeviceNodeGroup> mData) {
        super(mData);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @Override
    protected ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_device, parent, false);
        return new MockFolderAdapter.ViewHolder(view);
    }

    @Override
    public View getView(ViewGroup parent, View convertView, int mainPosition, int subPosition) {
        DeviceNode deviceNode = mData.get(mainPosition).getDeviceNodeList().get(subPosition);
        ItemViewHolder itemViewHolder = null;
        if (convertView == null) {
            itemViewHolder = new ItemViewHolder();
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_device_inner, parent, false);
            itemViewHolder.imageView = (ImageView) convertView.findViewById(R.id.image);
            convertView.setTag(itemViewHolder);
        }else {
            itemViewHolder = (ItemViewHolder) convertView.getTag();
        }
        Log.i(TAG, "getView: resId = " + deviceNode.getResId());
        Picasso.with(parent.getContext()).load(deviceNode.getResId()).into(itemViewHolder.imageView);
        return convertView;
    }

    @Override
    protected void onItemClick(View view, int parentIndex, int index) {
        Log.i(TAG, "parentIndex: " + parentIndex + ", index: " + index);
        if (this.onItemClickListener != null) {
            onItemClickListener.OnItemClick(parentIndex, index);
        }
    }

    @Override
    protected void onBindMainViewHolder(ViewHolder holder, int position) {
        int iSize = mData.get(position).getDeviceNodeList().size();
        if(iSize >= ITEM_MORE_DEVICES){
            holder.name.setText(mData.get(position).getGroupName());
        } else if(iSize == ITEM_SINGLE_DEVICE){
            holder.name.setText(mData.get(position).getDeviceNodeList().get(0).getName());
        }
    }

    @Override
    protected void onBindSubViewHolder(ViewHolder holder, int mainPosition, int subPosition) {
        holder.name.setText(mData.get(mainPosition).getDeviceNodeList().get(subPosition).getName()+"");
    }

    static class ViewHolder extends FolderAdapter.ViewHolder {
        TextView name;
        public ViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.text_name);
        }
    }

    static class ItemViewHolder extends MiViewHolder {
        ImageView imageView;
    }
}
