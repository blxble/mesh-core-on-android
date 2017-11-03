package com.blxble.meshpanel.element;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.color.CircleView;
import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.afollestad.materialdialogs.internal.ThemeSingleton;
import com.afollestad.materialdialogs.util.DialogUtils;
import com.blxble.meshpanel.element.view.TimerPickerView;
import com.bumptech.glide.Glide;
import com.blxble.meshpanel.adapter.StateAdapter;
import com.blxble.meshpanel.MeshApplication;
import com.blxble.meshpanel.R;
import com.blxble.meshpanel.adapter.StateItem;
import com.blxble.meshpanel.db.DeviceNode;
import com.blxble.meshpanel.db.DeviceSupportElement;
import com.blxble.meshpanel.db.ElementAppItem;
import com.dexafree.materialList.card.Card;
import com.dexafree.materialList.card.provider.ListCardProvider;
import com.dexafree.materialList.listeners.RecyclerItemClickListener;
import com.dexafree.materialList.view.MaterialListView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;

import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator;

public class AttributeActivity extends AppCompatActivity implements ColorChooserDialog.ColorCallback{

	private static final String TAG = "AttributeActivity";
	private MaterialListView mListView;
	private TimerPickerView mTimerPickerView;
	private int primaryPreselect;
	private int accentPreselect;

	private List<StateItem> stateItemList = new ArrayList<>();
	private DeviceNode mDeviceNode;
	private StateAdapter mStateAdapter;
	private CollapsingToolbarLayout mCollapsingToolbar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_attribute);
		init();
		primaryPreselect = DialogUtils.resolveColor(this, R.attr.colorPrimary);
		accentPreselect = DialogUtils.resolveColor(this, R.attr.colorAccent);
	}

	private void init() {
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		mCollapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
		ImageView deviceImageView = (ImageView) findViewById(R.id.device_image_view);
		mListView = (MaterialListView) findViewById(R.id.device_content_listview);
		setSupportActionBar(toolbar);
		android.support.v7.app.ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
		mDeviceNode = MeshApplication.getDeviceNode();
		mCollapsingToolbar.setTitle(mDeviceNode.getName());
		Glide.with(this).load(R.drawable.ic_device).into(deviceImageView);
		loadStateItem();
		initListView();
	}

	private void initListView(){
		mListView.setItemAnimator(new SlideInLeftAnimator());
		mListView.getItemAnimator().setAddDuration(300);
		mListView.getItemAnimator().setRemoveDuration(300);

		ListCardProvider listCardProvider = new ListCardProvider();
		Card card = new Card.Builder(this)
				.setTag("LIST_CARD")
				.setDismissible()
				.withProvider(listCardProvider)
				.setLayout(R.layout.material_list_card_layout)
				.setTitle("Detail Information")
				.setDescription("Take a list")
				.setAdapter(mStateAdapter)
				.endConfig()
				.build();
		mListView.getAdapter().add(card);
		mListView.addOnItemTouchListener(new RecyclerItemClickListener.OnItemClickListener() {
			@Override
			public void onItemClick(@NonNull Card card, int position) {
				Log.i(TAG, "onItemClick: " + card.getTag() + position);
			}

			@Override
			public void onItemLongClick(@NonNull Card card, int position) {
				Log.i(TAG, "onItemLongClick: " + card.getTag() + position);
			}
		});

		listCardProvider.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				handleItems(stateItemList.get(position));
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void loadStateItem() {
		stateItemList.add(new StateItem(ElementAppItem.APP_TYPE_NAME, mDeviceNode.getName()));
		stateItemList.add(new StateItem("Address", Integer.toString(mDeviceNode.getAddress())));
		stateItemList.add(new StateItem("DevInfo", mDeviceNode.getDevInfo()));
		if (mDeviceNode.getBdAddress() != null && !mDeviceNode.getBdAddress().isEmpty()) {
			stateItemList.add(new StateItem("BdAddress", mDeviceNode.getBdAddress()));
		}
		stateItemList.add(new StateItem("NetKey", Integer.toString(mDeviceNode.getNetKeyIdx())));

		List<DeviceSupportElement> deviceSupportElementList = mDeviceNode.getDeviceSupportElementList();
		for (int iElement = 0; iElement < deviceSupportElementList.size(); iElement++) {
			List<ElementAppItem> elementAppItemList = deviceSupportElementList.get(iElement).getElementAppItemList();
			short eltAddr = (short)(mDeviceNode.getAddress() + deviceSupportElementList.get(iElement).getElementIdx());
			for (int iItem = 0; iItem < elementAppItemList.size(); iItem++) {
				ElementAppItem elementAppItem = elementAppItemList.get(iItem);
				if (elementAppItem.getType() == ElementAppItem.APP_TYPE_POWER) {
					stateItemList.add(new StateItem(elementAppItem.getId(), eltAddr, ElementAppItem.APP_TYPE_POWER, Long.toString(elementAppItem.getState())));
				} else if (elementAppItem.getType() == ElementAppItem.APP_TYPE_COLOR) {
					String strColor = "#FF000000";
					if (elementAppItem.getState() != 0) {
						strColor = "#" + Long.toHexString(elementAppItem.getState());
					}
					stateItemList.add(new StateItem(elementAppItem.getId(), eltAddr, ElementAppItem.APP_TYPE_COLOR, strColor));
				} else if (elementAppItem.getType() == ElementAppItem.APP_TYPE_NET_TIME) {
					Date nowTime = new Date(elementAppItem.getState());
					SimpleDateFormat sdFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:dd");
					stateItemList.add(new StateItem(elementAppItem.getId(), eltAddr, ElementAppItem.APP_TYPE_NET_TIME, sdFormatter.format(nowTime)));
				}
			}
		}
		mStateAdapter = new StateAdapter(this, R.layout.attr_item, stateItemList);

		mStateAdapter.setOnPowerClickListener(new StateAdapter.OnPowerClickListener() {
			@Override
			public void onPowerClick(boolean isChecked, StateItem stateItem) {
				setStateItem(ElementAppItem.APP_TYPE_NET_TIME, Integer.toString(isChecked?ElementAppItem.LIGHT_POWER_ON:ElementAppItem.LIGHT_POWER_OFF));
				MeshApplication.getDeviceManager().notifyUpdateDeviceNodeState(isChecked?ElementAppItem.LIGHT_POWER_ON:ElementAppItem.LIGHT_POWER_OFF, stateItem.getEltAddr());
			}
		});
	}

    private void handleItems(StateItem stateItem) {
        switch (stateItem.getType()) {
            case ElementAppItem.APP_TYPE_NAME:
                handleRename(stateItem);
                break;
			case ElementAppItem.APP_TYPE_COLOR:
                handleCustomColor(stateItem);
                break;
            case ElementAppItem.APP_TYPE_NET_TIME:
                handleNetTime(stateItem);
                break;
            default:
                break;
        }
    }

    private void handleNetTime(StateItem stateItem) {
		View customView = getTimePickerView();
        new MaterialDialog.Builder(this)
                .title(R.string.time_name)
                .customView(customView, false)
                .positiveText(R.string.time_positive)
                .show();
    }

    private void handleRename(StateItem stateItem) {
		AlertDialog.Builder builder = new AlertDialog.Builder(
				AttributeActivity.this);
		builder.setTitle("Rename");
		builder.setCancelable(false);

		LayoutInflater inflater = getLayoutInflater();
		final View view = inflater.inflate(R.layout.attr_name,
				(ViewGroup) findViewById(R.id.attr_name));

		EditText editName = (EditText) view.findViewById(R.id.editText_name);
		editName.setText(stateItem.getState());
		builder.setView(view);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				EditText editName = (EditText) view.findViewById(R.id.editText_name);
				setStateItem(ElementAppItem.APP_TYPE_NAME, editName.getText().toString());
			}
		});
		builder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub

					}
				});

		AlertDialog dialog = builder.create();
		dialog.show();
	}

	private void handleCustomColor(StateItem stateItem){
		accentPreselect = Color.parseColor(stateItem.getState());
		ColorChooserDialog colorChooserDialog = new ColorChooserDialog.Builder(this, R.string.color_palette)
				.titleSub(R.string.colors)
				.accentMode(true)
				.preselect(accentPreselect)
				.show(this);
		colorChooserDialog.setCancelable(false);
	}

	// Receives callback from color chooser dialog
	@Override
	public void onColorSelection(@NonNull ColorChooserDialog dialog, @ColorInt int color) {
		if (dialog.isAccentMode()) {
			accentPreselect = color;
			ThemeSingleton.get().positiveColor = DialogUtils.getActionTextStateList(this, color);
			ThemeSingleton.get().neutralColor = DialogUtils.getActionTextStateList(this, color);
			ThemeSingleton.get().negativeColor = DialogUtils.getActionTextStateList(this, color);
			ThemeSingleton.get().widgetColor = color;
			Log.i(TAG, "onColorSelection: #"+ Integer.toHexString(color));
			setStateItem(ElementAppItem.APP_TYPE_COLOR, "#"+Integer.toHexString(color));
		} else {
			primaryPreselect = color;
			if (getSupportActionBar() != null) {
				getSupportActionBar().setBackgroundDrawable(new ColorDrawable(color));
			}
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				getWindow().setStatusBarColor(CircleView.shiftColorDown(color));
				getWindow().setNavigationBarColor(color);
			}
		}
	}

	@Override
	public void onColorChooserDismissed(@NonNull ColorChooserDialog dialog) {
	}

	private View getTimePickerView() {
		View view = View.inflate(this, R.layout.timepicker, null);
		mTimerPickerView = new TimerPickerView(view);

		/*
		Date nowTime = new Date(MeshApplication.getDeviceManager().getNetTime());
		mTimerPickerView.initDateTimePicker(nowTime.getYear(),
				nowTime.getMonth(), nowTime.getDay(),
				nowTime.getHours(), nowTime.getMinutes(), nowTime.getSeconds());
		*/
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH)+1;
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int min = calendar.get(Calendar.MINUTE);
		int second = calendar.get(Calendar.SECOND);
		mTimerPickerView.setSTART_YEAR(1995);// 设置最小年份
		mTimerPickerView.setEND_YEAR(year);// 设置最大年份
		mTimerPickerView.initDateTimePicker(year, month, day, hour, min, second);//传入-1则不显示该列
		return view;
	}

	private void setStateItem(short type, String state) {
		for (int idx = 0; idx < stateItemList.size(); idx++) {
			StateItem stateItem = stateItemList.get(idx);
			if (stateItem.getType() == type){
				stateItem.setState(state);
				if (type == ElementAppItem.APP_TYPE_NAME) {
					DeviceNode deviceNode = new DeviceNode();
					deviceNode.setName(state);
					deviceNode.update(mDeviceNode.getId());
					setAppItemState(type, state);
				} else if (type == ElementAppItem.APP_TYPE_COLOR) {
					ElementAppItem elementAppItem = new ElementAppItem();
					elementAppItem.setState(Color.parseColor(state));
					elementAppItem.update(stateItem.getId());
					setAppItemState(type, state);
				} else if (type == ElementAppItem.APP_TYPE_POWER) {
					ElementAppItem elementAppItem = new ElementAppItem();
					elementAppItem.setState(Integer.valueOf(state));
					elementAppItem.update(stateItem.getId());
					setAppItemState(type, state);
				}
				mStateAdapter.notifyDataSetChanged();
				break;
			}
		}
	}

	private void setAppItemState(short type, String state) {
		if (type == ElementAppItem.APP_TYPE_NAME) {
			mDeviceNode.setName(state);
			mCollapsingToolbar.setTitle(state);
			MeshApplication.getDeviceManager().notifyUpdateDeviceNode();
		} else {
			List<DeviceSupportElement> deviceSupportElementList = mDeviceNode.getDeviceSupportElementList();
			for (int iElement = 0; iElement < deviceSupportElementList.size(); iElement++) {
				List<ElementAppItem> elementAppItemList = deviceSupportElementList.get(iElement).getElementAppItemList();
				for (int iItem = 0; iItem < elementAppItemList.size(); iItem++) {
					ElementAppItem elementAppItem = elementAppItemList.get(iItem);
					if (elementAppItem.getType() == type) {
						if (type == ElementAppItem.APP_TYPE_POWER) {
							elementAppItem.setState(Integer.valueOf(state));
						} else if (type == ElementAppItem.APP_TYPE_COLOR) {
							elementAppItem.setState(Color.parseColor(state));
						}
						break;
					}
				}
			}
		}
	}
}
