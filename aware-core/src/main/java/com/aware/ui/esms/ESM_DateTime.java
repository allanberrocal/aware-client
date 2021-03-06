package com.aware.ui.esms;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import com.aware.Aware;
import com.aware.ESM;
import com.aware.R;
import com.aware.providers.ESM_Provider;

import org.json.JSONException;

import java.util.Calendar;

/**
 * Created by denzil on 01/11/2016.
 */

public class ESM_DateTime extends ESM_Question {

    private static Calendar datePicked = null;

    public ESM_DateTime() throws JSONException {
        this.setType(ESM.TYPE_ESM_DATETIME);
    }

    public class DateTimePagerAdapter extends PagerAdapter {
        private Context mContext;

        public DateTimePagerAdapter(Context context) {
            mContext = context;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            ViewGroup layout = null;
            switch (position) {
                case 0:
                    layout = (ViewGroup) inflater.inflate(R.layout.esm_datetime_date, container, false);

                    final DatePicker datePicker = (DatePicker) layout.findViewById(R.id.datePicker);

                    final Calendar cdate = Calendar.getInstance();
                    int year = cdate.get(Calendar.YEAR);
                    int month = cdate.get(Calendar.MONTH);
                    int day = cdate.get(Calendar.DAY_OF_MONTH);

                    datePicker.init(year, month, day, new DatePicker.OnDateChangedListener() {
                        @Override
                        public void onDateChanged(DatePicker datePicker, int year, int month, int day) {
                            datePicked.set(Calendar.DAY_OF_MONTH, day);
                            datePicked.set(Calendar.MONTH, month);
                            datePicked.set(Calendar.YEAR, year);
                        }
                    });

                    container.addView(layout);
                    break;
                case 1:
                    layout = (ViewGroup) inflater.inflate(R.layout.esm_datetime_time, container, false);

                    final TimePicker timePicker = (TimePicker) layout.findViewById(R.id.timePicker);
                    timePicker.setIs24HourView(DateFormat.is24HourFormat(getContext())); //makes the clock adjust to device's locale settings

                    final Calendar chour = Calendar.getInstance();
                    int hour = chour.get(Calendar.HOUR_OF_DAY);
                    int minute = chour.get(Calendar.MINUTE);
                    timePicker.setHour(hour);
                    timePicker.setMinute(minute);
                    timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
                        @Override
                        public void onTimeChanged(TimePicker timePicker, int hour, int minute) {
                            datePicked.set(Calendar.HOUR_OF_DAY, hour);
                            datePicked.set(Calendar.MINUTE, minute);
                        }
                    });

                    container.addView(layout);
                    break;
            }
            return layout;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object view) {
            container.removeView((View) view);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String title = "";
            switch (position) {
                case 0:
                    title = "Date";
                    break;
                case 1:
                    title = "Time";
                    break;
            }
            return title;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        datePicked = Calendar.getInstance();

        View ui = inflater.inflate(R.layout.esm_datetime, null);
        builder.setView(ui);

        esm_dialog = builder.create();
        esm_dialog.setCanceledOnTouchOutside(false);

        try {
            TextView esm_title = (TextView) ui.findViewById(R.id.esm_title);
            esm_title.setText(getTitle());

            TextView esm_instructions = (TextView) ui.findViewById(R.id.esm_instructions);
            esm_instructions.setText(getInstructions());

            final ViewPager datetimePager = (ViewPager) ui.findViewById(R.id.datetimepager);
            DateTimePagerAdapter dateTimePagerAdapter = new DateTimePagerAdapter(getContext());
            datetimePager.setAdapter(dateTimePagerAdapter);
            datetimePager.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        if (getExpirationThreshold() > 0 && expire_monitor != null)
                            expire_monitor.cancel(true);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            Button cancel_text = (Button) ui.findViewById(R.id.esm_cancel);
            cancel_text.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    esm_dialog.cancel();
                }
            });

            Button submit_number = (Button) ui.findViewById(R.id.esm_submit);
            submit_number.setText(getSubmitButton());
            submit_number.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if (getExpirationThreshold() > 0 && expire_monitor != null)
                            expire_monitor.cancel(true);

                        ContentValues rowData = new ContentValues();
                        rowData.put(ESM_Provider.ESM_Data.ANSWER_TIMESTAMP, System.currentTimeMillis());
                        rowData.put(ESM_Provider.ESM_Data.ANSWER, datePicked.getTimeInMillis());
                        rowData.put(ESM_Provider.ESM_Data.STATUS, ESM.STATUS_ANSWERED);

                        getActivity().getContentResolver().update(ESM_Provider.ESM_Data.CONTENT_URI, rowData, ESM_Provider.ESM_Data._ID + "=" + getID(), null);

                        Intent answer = new Intent(ESM.ACTION_AWARE_ESM_ANSWERED);
                        answer.putExtra(ESM.EXTRA_ANSWER, rowData.getAsString(ESM_Provider.ESM_Data.ANSWER));
                        getActivity().sendBroadcast(answer);

                        if (Aware.DEBUG) Log.d(Aware.TAG, "Answer:" + rowData.toString());

                        esm_dialog.dismiss();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return esm_dialog;
    }
}
