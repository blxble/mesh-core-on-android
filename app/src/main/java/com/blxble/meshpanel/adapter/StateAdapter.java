package com.blxble.meshpanel.adapter;

import java.util.List;

import com.blxble.meshpanel.R;
import com.blxble.meshpanel.db.ElementAppItem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

public class StateAdapter extends ArrayAdapter<StateItem>{
	private OnPowerClickListener onPowerClickListener;
	public StateAdapter(Context context, int textViewResourceId,
                        List<StateItem> objects) {
		super(context, textViewResourceId, objects);
	}

	public interface OnPowerClickListener {
		void onPowerClick(boolean isChecked, StateItem stateItem);
	}

	public void setOnPowerClickListener(OnPowerClickListener listener) {
		this.onPowerClickListener = listener;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final StateItem stateItem = getItem(position);
		View view;
		ViewHolder viewHolder = null;
		ViewHolderSwitch viewHolderSwitch = null;
		if (convertView == null){
			if (stateItem.getType() == ElementAppItem.APP_TYPE_POWER) {
				view = LayoutInflater.from(getContext()).inflate(R.layout.attr_item_switch, parent, false);
				viewHolderSwitch = new ViewHolderSwitch();
				viewHolderSwitch.name = (TextView) view.findViewById(R.id.item_name);
				viewHolderSwitch.state = (Switch) view.findViewById(R.id.item_switch);
				view.setTag(viewHolderSwitch);
			} else {
				if (stateItem.getType() == ElementAppItem.APP_TYPE_DENOTE) {
					view = LayoutInflater.from(getContext()).inflate(R.layout.attr_item, parent, false);
				} else {
					view = LayoutInflater.from(getContext()).inflate(R.layout.attr_item_edit, parent, false);
				}
				viewHolder = new ViewHolder();
				viewHolder.name = (TextView) view.findViewById(R.id.item_name);
				viewHolder.state = (TextView) view.findViewById(R.id.item_state);
				view.setTag(viewHolder);
			}
		} else {
			view = convertView;
			if (stateItem.getType() == ElementAppItem.APP_TYPE_POWER) {
				viewHolderSwitch = (ViewHolderSwitch) view.getTag();
			} else {
				viewHolder = (ViewHolder) view.getTag();
			}
		}

		if (viewHolder != null) {
			viewHolder.name.setText(stateItem.getName());
			viewHolder.state.setText(stateItem.getState());
		} else {
			if (stateItem.getType() == ElementAppItem.APP_TYPE_POWER) {
				viewHolderSwitch.name.setText(stateItem.getName());
				viewHolderSwitch.state.setChecked(Integer.valueOf(stateItem.getState()) == ElementAppItem.LIGHT_POWER_ON);
				viewHolderSwitch.state.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						if (onPowerClickListener != null) {
							onPowerClickListener.onPowerClick(isChecked, stateItem);
						}
						stateItem.setState(isChecked?"1":"0");
					}
				});
			}
		}
		return view;
	}

	class ViewHolder {
		TextView name;
		TextView state;
	}

	class ViewHolderSwitch {
		TextView name;
		Switch state;
	}
	
}
