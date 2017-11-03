package com.blxble.meshpanel.element.view;

import android.view.View;

import com.blxble.meshpanel.R;

import java.util.Arrays;
import java.util.List;

public class TimerPickerView {

	private View view;
	private NumberPickerView np_year;
	private NumberPickerView np_month;
	private NumberPickerView np_day;
	private NumberPickerView np_hours;
	private NumberPickerView np_mins;
	private NumberPickerView np_second;
	private int START_YEAR = 1950, END_YEAR;

	public View getView() {
		return view;
	}

	public void setView(View view) {
		this.view = view;
	}

	public int getSTART_YEAR() {
		return START_YEAR;
	}

	public void setSTART_YEAR(int sTART_YEAR) {
		START_YEAR = sTART_YEAR;
	}

	public int getEND_YEAR() {
		return END_YEAR;
	}

	public void setEND_YEAR(int eND_YEAR) {
		END_YEAR = eND_YEAR;
	}

	public TimerPickerView(View view) {
		super();
		this.view = view;
		setView(view);
	}

	public TimerPickerView(View view, boolean hasSelectTime) {
		super();
		this.view = view;
		setView(view);
	}

	public void initDateTimePicker(int year, int month, int day) {
		this.initDateTimePicker(year, month, day, -1, -1, -1);
	}

	public void setDateTimePicker(int year, int month, int day, int h, int m, int s) {
		np_year.setValue(year);
		np_month.setValue(month);
		np_day.setValue(day);
		np_hours.setValue(h);
		np_mins.setValue(m);
		np_second.setValue(s);
	}


	private void setNumberPicker(NumberPickerView picker, int minValue, int maxValue, int value){
		int size = maxValue-minValue+1;
		String [] array = new String[size];
		for (int i=0;i<size;i++){
			if((minValue+i)<10){
				array[i] = "0"+(minValue+i);
			}else {
				array[i] = ""+(minValue + i) ;
			}
		}
		picker.updateContentAndIndex(array);
		picker.setMinValue(minValue);
		picker.setMaxValue(maxValue);
		if(value!=-1)picker.setValue(value);
	}

	/**
	 * 加载时间选择器,可选显示，不使用传-1
	 * @param year
	 * @param month
	 * @param day
	 * @param h
	 * @param m
	 */
	public void initDateTimePicker(int year, int month, int day, int h, int m, int s) {
		// 添加大小月月份并将其转换为list,方便之后的判断
		String[] months_big = { "1", "3", "5", "7", "8", "10", "12" };
		String[] months_little = { "4", "6", "9", "11" };

		final List<String> list_big = Arrays.asList(months_big);
		final List<String> list_little = Arrays.asList(months_little);

		np_year = (NumberPickerView) view.findViewById(R.id.year);
		np_month = (NumberPickerView) view.findViewById(R.id.month);
		np_day = (NumberPickerView) view.findViewById(R.id.day);
		np_hours = (NumberPickerView) view.findViewById(R.id.hour);
		np_mins = (NumberPickerView) view.findViewById(R.id.min);
		np_second = (NumberPickerView) view.findViewById(R.id.second);

		// 年
		if (year != -1) {
			// 设置"年"的显示数据
			setNumberPicker(np_year,START_YEAR,END_YEAR,year);
			// 添加"年"监听
			NumberPickerView.OnValueChangeListener wheelListener_year = new NumberPickerView.OnValueChangeListener() {
				public void onValueChange(NumberPickerView wheel, int oldValue, int newValue) {
					int year_num = newValue;
					int mon_num = Integer.parseInt(np_month.getContentByCurrValue());
					int day_num = Integer.parseInt(np_day.getContentByCurrValue());
					// 判断大小月及是否闰年,用来确定"日"的数据
					if (list_big.contains(String.valueOf(mon_num))) {
						setNumberPicker(np_day,1,31,day_num);
					} else if (list_little.contains(String.valueOf(mon_num))) {
						setNumberPicker(np_day,1,30,day_num);
					} else {
						if ((year_num % 4 == 0 && year_num % 100 != 0) || year_num % 400 == 0)
							setNumberPicker(np_day,1, 29,day_num);
						else
							setNumberPicker(np_day,1,28,day_num);
					}
				}
			};
			np_year.setOnValueChangedListener(wheelListener_year);
		} else {
			np_year.setVisibility(View.GONE);
		}

		// 月
		if (month != -1) {
			setNumberPicker(np_month,1,12,month);

			// 添加"月"监听
			NumberPickerView.OnValueChangeListener wheelListener_month = new NumberPickerView.OnValueChangeListener() {
				public void onValueChange(NumberPickerView wheel, int oldValue, int newValue) {
					int month_num = newValue;
					int year_num = Integer.parseInt(np_year.getContentByCurrValue());
					int day_num = Integer.parseInt(np_day.getContentByCurrValue());
					// 判断大小月及是否闰年,用来确定"日"的数据
					if (list_big.contains(String.valueOf(month_num))) {
						setNumberPicker(np_day,1,31,day_num);
					} else if (list_little.contains(String.valueOf(month_num))) {
						setNumberPicker(np_day,1,30,day_num);
					} else {
						if ((year_num % 4 == 0 && year_num % 100 != 0) || year_num % 400 == 0)
							setNumberPicker(np_day,1,29,day_num);
						else
							setNumberPicker(np_day,1,28,day_num);
					}
				}
			};
			np_month.setOnValueChangedListener(wheelListener_month);
		} else {
			np_month.setVisibility(View.GONE);
		}

		// 日
		if (day != -1) {
			// 判断大小月及是否闰年,用来确定"日"的数据
			if (list_big.contains(String.valueOf(month))) {
				setNumberPicker(np_day,1,31,day);
			} else if (list_little.contains(String.valueOf(month))) {
				setNumberPicker(np_day,1,30,day);
			} else {
				// 闰年
				if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0)
					setNumberPicker(np_day,1,29,day);
				else
					setNumberPicker(np_day,1,28,day);
			}
		} else {
			np_day.setVisibility(View.GONE);
		}

		// 时
		if (h != -1) {
			setNumberPicker(np_hours,0,23,h);
		} else {
			np_hours.setVisibility(View.GONE);
		}

		// 分
		if (m != -1) {
			setNumberPicker(np_mins,0,59,m);
		} else {
			np_mins.setVisibility(View.GONE);
		}

		// 秒
		if (s != -1) {
			setNumberPicker(np_second,0,59,s);
		} else {
			np_second.setVisibility(View.GONE);
		}

	}

	/**
	 * 获得选中时间
	 * 
	 * @param strYear
	 *            间开符号
	 * @param strMon
	 * @param strDay
	 * @param strHour
	 * @param strMins
	 * @param strSecond
	 * @return
	 */
	public String getTime(String strYear, String strMon, String strDay, String strHour, String strMins, String strSecond) {
		StringBuffer sb = new StringBuffer();
		String year = "";
		String mon = "";
		String day = "";
		String hour = "";
		String mins = "";
		String second = "";

		if (np_year.getVisibility() != View.GONE) {
			year = String.valueOf(np_year.getContentByCurrValue());
			year = new StringBuffer(year + strYear).toString();
		}
		if (np_month.getVisibility() != View.GONE) {
			mon = String.valueOf(np_month.getContentByCurrValue());
			mon = new StringBuffer(mon + strMon).toString();
		}
		if (np_day.getVisibility() != View.GONE) {
			day = String.valueOf(np_day.getContentByCurrValue());
			day = new StringBuffer(day + strDay).toString();
		}
		if (np_hours.getVisibility() != View.GONE) {
			hour = String.valueOf(np_hours.getContentByCurrValue());
			hour = new StringBuffer(hour + strHour).toString();
		}
		if (np_mins.getVisibility() != View.GONE) {
			mins = String.valueOf(np_mins.getContentByCurrValue());
			mins = new StringBuffer(mins + strMins).toString();
		}
		if (np_second.getVisibility() != View.GONE) {
			second = String.valueOf(np_second.getContentByCurrValue());
			second = new StringBuffer(second + strSecond).toString();
		}

		sb.append(year).append(mon).append(day).append(hour).append(mins).append(second);
		return sb.toString();
	}


	/**
	 * 获取当前时间  年
	 * @return year
	 */
	public String getTimeYear(){
		return np_year.getContentByCurrValue();
	}
	/**
	 * 获取当前时间  月
	 * @return month
	 */
	public String getTimeMonth(){
		return np_month.getContentByCurrValue();
	}
	/**
	 * 获取当前时间  日
	 * @return day
	 */
	public String getTimeDay(){
		return np_day.getContentByCurrValue();
	}
	/**
	 * 获取当前时间  小时
	 * @return hour
	 */
	public String getTimeHours(){
		return np_hours.getContentByCurrValue();
	}
	/**
	 * 获取当前时间  分
	 * @return minute
	 */
	public String getTimeMins(){
		return np_mins.getContentByCurrValue();
	}
	/**
	 * 获取当前时间  秒
	 * @return second
	 */
	public String getTimeSecond(){
		return np_second.getContentByCurrValue();
	}
}
