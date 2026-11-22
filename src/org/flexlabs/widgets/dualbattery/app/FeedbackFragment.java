/*
 * Copyright 2012 Artiom Chilaru (http://flexlabs.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.flexlabs.widgets.dualbattery.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.fragment.app.Fragment;
import org.flexlabs.widgets.dualbattery.BatteryLevel;
import org.flexlabs.widgets.dualbattery.Constants;
import org.flexlabs.widgets.dualbattery.R;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FeedbackFragment extends Fragment {
    private EditText feedbackEditor;
    private boolean resetFeedback = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.feedback_form, container, false);
        feedbackEditor = (EditText) view.findViewById(R.id.feedbackEditor);
        feedbackEditor.requestFocus();
        view.findViewById(R.id.sendFeedback).setOnClickListener(feedbackListener);
        return view;
    }

    private final View.OnClickListener feedbackListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            CharSequence feedbackText = feedbackEditor.getText();
            if (!FeedbackValidator.isValid(feedbackText)) {
                new AlertDialog.Builder(requireContext())
                        .setTitle(R.string.app_name)
                        .setMessage(R.string.feedback_hint)
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
                return;
            }
            sendFeedback(requireContext(), feedbackText);
            resetFeedback = true;
        }
    };

    @Override
    public void onStop() {
        super.onStop();
        if (resetFeedback)
            feedbackEditor.setText(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        resetFeedback = false;
    }

    public static void sendFeedback(Context context, CharSequence feedback) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setDataAndType(Uri.parse("mailto:"), "message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[] { Constants.FeedbackDestination });
        intent.putExtra(Intent.EXTRA_SUBJECT, "Dual Battery Widget Feedback (v" + Constants.VERSION + ")");
        intent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(getDeviceDetails(context, feedback)));
        context.startActivity(Intent.createChooser(intent, "Email"));
    }

    private static String getDeviceDetails(Context context, CharSequence feedback) {
        StringBuilder sb = new StringBuilder(TextUtils.htmlEncode(feedback.toString()));
        sb.append("<br />\n<h4>Device details:</h4>");
        sb.append("<br />\n<b>App version:</b> v").append(Constants.VERSION);
        sb.append("<br />\n<b>Brand:</b> ").append(Build.MANUFACTURER);
        sb.append("<br />\n<b>Model:</b> ").append(Build.MODEL);
        sb.append("<br />\n<b>Device:</b> ").append(Build.DEVICE);
        sb.append("<br />\n<b>Android version:</b> ").append(Build.VERSION.RELEASE);
        sb.append("<br />\n<b>Version details:</b> ").append(Build.VERSION.INCREMENTAL);

        Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (intent != null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                String allKeys = TextUtils.join(", ", extras.keySet());
                sb.append("<br />\n<b>Battery intent keys:</b> ").append(allKeys);
                int dockStatus = extras.getInt("dock_status", -1);
                String dockStatusStr = "";
                switch (dockStatus) {
                    case 0: dockStatusStr = " (Unknown)"; break;
                    case 1: dockStatusStr = " (Undocked)"; break;
                    case 2: dockStatusStr = " (Charging)"; break;
                    case 3: dockStatusStr = " (Docked)"; break;
                    case 4: dockStatusStr = " (Discharging)"; break;
                }
                sb.append("<br />\n<b>Battery dock status</b> ").append(extras.get("dock_status")).append(dockStatusStr);
                sb.append("<br />\n<b>Battery dock level</b> ").append(extras.get("dock_level"));
            }
        }
        sb.append("<br />\n<b>Is Dock supported:</b> ").append(BatteryLevel.getCurrent().is_dockFriendly());
        sb.append("<br />\n<b>Kernel:</b> ").append(getFormattedKernelVersion().replace("\n", "<br />\n"));
        return sb.toString();
    }

    private static String getFormattedKernelVersion() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("/proc/version"), 256);
            String procVersionStr;
            try {
                procVersionStr = reader.readLine();
            } finally {
                reader.close();
            }

            final String PROC_VERSION_REGEX =
                    "\\w+\\s+" +
                            "\\w+\\s+" +
                            "([^\\s]+)\\s+" +
                            "\\(([^\\s@]+(?:@[^\\s.]+)?)[^)]*\\)\\s+" +
                            "\\(.*?(?:\\(.*?\\)).*?\\)\\s+" +
                            "([^\\s]+)\\s+" +
                            "(?:PREEMPT\\s+)?" +
                            "(.+)";
            Matcher m = Pattern.compile(PROC_VERSION_REGEX).matcher(procVersionStr);
            if (!m.matches() || m.groupCount() < 4) {
                Log.e(Constants.LOG, "Could not parse /proc/version");
                return "Unavailable";
            }
            return m.group(1) + "\n" + m.group(2) + " " + m.group(3) + "\n" + m.group(4);
        } catch (IOException e) {
            Log.e(Constants.LOG, "IO Exception when getting kernel version for Device Info screen", e);
            return "Unavailable";
        }
    }
}
