package com.aware.tests;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.BatteryManager;

import com.aware.Aware;
import com.aware.ESM;
import com.aware.Screen;
import com.aware.providers.Battery_Provider;
import com.aware.providers.Scheduler_Provider;
import com.aware.providers.Screen_Provider;
import com.aware.ui.ESM_Queue;
import com.aware.ui.esms.ESMFactory;
import com.aware.ui.esms.ESM_PAM;
import com.aware.utils.Aware_TTS;
import com.aware.utils.Scheduler;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by denzil on 11/11/2016.
 */

public class TestScheduler implements AwareTest {

    @Override
    public void test(Context context) {
//        testTimer(context);
//        testContextual(context);
        testConditional(context);
//        testTime(context);
        Aware.startScheduler(context);
    }

    private void testTime(Context c) {
        try {
            Scheduler.Schedule conditional = new Scheduler.Schedule("time");
            conditional
                    .addHour(16)
                    .addMinute(42)
                    .setActionType(Scheduler.ACTION_TYPE_SERVICE)
                    .setActionClass(c.getPackageName() + "/" + Aware_TTS.class.getName())
                    .addActionExtra(Aware_TTS.EXTRA_TTS_TEXT, "Yay!")
                    .addActionExtra(Aware_TTS.EXTRA_TTS_REQUESTER, c.getPackageName());
            Scheduler.saveSchedule(c, conditional);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * This test makes a scheduler that:
     * - Asks the user how the device can help when the screen is turned on
     * @param c
     */
    private void testConditional(Context c) {
        try {
            Scheduler.Schedule conditional = new Scheduler.Schedule("screen_on");
            conditional
                    .addCondition(Uri.parse("content://com.aware.phone.provider.screen/screen"), Screen_Provider.Screen_Data.SCREEN_STATUS + "=" + Screen.STATUS_SCREEN_ON)
                    .setActionType(Scheduler.ACTION_TYPE_SERVICE)
                    .setActionClass(c.getPackageName() + "/" + Aware_TTS.class.getName())
                    .addActionExtra(Aware_TTS.EXTRA_TTS_TEXT, "How can I help?")
                    .addActionExtra(Aware_TTS.EXTRA_TTS_REQUESTER, c.getPackageName());
            Scheduler.saveSchedule(c, conditional);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * This test makes a scheduler that reacts to the events of screen ON and starts the Text-To-Speech AWARE service to notify the state
     *
     * @param c
     */
    private void testContextual(Context c) {
        try {
            Scheduler.Schedule contextual = new Scheduler.Schedule("test_contextual");
            contextual.addContext(Screen.ACTION_AWARE_SCREEN_ON);
            contextual.setActionType(Scheduler.ACTION_TYPE_SERVICE);
            contextual.setActionClass(c.getPackageName() + "/" + Aware_TTS.class.getName());
            contextual.addActionExtra(Aware_TTS.EXTRA_TTS_TEXT, "Screen is on!");
            contextual.addActionExtra(Aware_TTS.EXTRA_TTS_REQUESTER, c.getPackageName());

            Scheduler.saveSchedule(c, contextual);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void testESMTrigger(Context c) {
        try {

            ESM_PAM esmPAM = new ESM_PAM();
            esmPAM.setTitle("PAM")
                    .setInstructions("Pick the closest to how you feel right now.")
                    .setSubmitButton("OK")
                    .setNotificationTimeout(10)
                    .setTrigger("AWARE Test");

            ESMFactory factory = new ESMFactory();
            factory.addESM(esmPAM);

            Scheduler.Schedule contextual = new Scheduler.Schedule("test_contextual");
            contextual.addContext(Screen.ACTION_AWARE_SCREEN_ON);
            contextual.setActionType(Scheduler.ACTION_TYPE_BROADCAST);
            contextual.setActionClass(ESM.ACTION_AWARE_QUEUE_ESM);
            contextual.addActionExtra(ESM.EXTRA_ESM, factory.build());

            Scheduler.saveSchedule(c, contextual);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * This test creates a 5 randomly assigned timestamps, with 5 minutes in between, starting on next 5 minutes until 40 minutes from now,
     *
     * @param c
     */
    private void testTimer(Context c) {
        Calendar start = Calendar.getInstance();
        start.setTimeInMillis(System.currentTimeMillis());
        start.add(Calendar.MINUTE, 5);

        Calendar end = Calendar.getInstance();
        end.setTimeInMillis(System.currentTimeMillis());
        end.add(Calendar.MINUTE, 40);

        ArrayList<Long> random = Scheduler.random_times(start, end, 5, 5);
        for (Long time : random) {
            Calendar aux = Calendar.getInstance();
            aux.setTimeInMillis(time);
            try {
                Scheduler.Schedule timer = new Scheduler.Schedule("test_scheduler_" + aux.getTimeInMillis());
                timer.setTimer(aux)
                        .setActionType(Scheduler.ACTION_TYPE_SERVICE)
                        .setActionClass(c.getPackageName() + "/" + Aware_TTS.class.getName())
                        .addActionExtra(Aware_TTS.EXTRA_TTS_TEXT, "Random triggered!")
                        .addActionExtra(Aware_TTS.EXTRA_TTS_REQUESTER, c.getPackageName());

                Scheduler.saveSchedule(c, timer);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
