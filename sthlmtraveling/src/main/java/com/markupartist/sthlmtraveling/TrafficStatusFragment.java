/*
 * Copyright (C) 2009 Johan Nilsson <http://markupartist.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.markupartist.sthlmtraveling;

import java.io.IOException;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.markupartist.sthlmtraveling.provider.deviation.DeviationStore;
import com.markupartist.sthlmtraveling.provider.deviation.DeviationStore.TrafficEvent;
import com.markupartist.sthlmtraveling.provider.deviation.DeviationStore.TrafficStatus;
import com.markupartist.sthlmtraveling.provider.deviation.DeviationStore.TrafficType;

public class TrafficStatusFragment extends BaseFragment {

    private static final String TAG = "TrafficStatusActivity";
    private LinearLayout mProgress;
    private GetTrafficStatusTask mGetTrafficStatusTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        registerEvent("Traffic status");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.traffic_status_fragment, container,
                false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Button allDeviations = (Button) getActivity().findViewById(
                R.id.traffic_status_btn_all_deviations);
        allDeviations.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(),
                        DeviationsActivity.class));
            }
        });

        mProgress = (LinearLayout) getActivity().findViewById(
                R.id.search_progress);
        mProgress.setVisibility(View.GONE);

        mGetTrafficStatusTask = new GetTrafficStatusTask();
        mGetTrafficStatusTask.execute();
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        mGetTrafficStatusTask.cancel(true);
        super.onDestroyView();
    }

    private void inflateViews(TrafficStatus ts) {
        LinearLayout typeView = (LinearLayout) getActivity().findViewById(
                R.id.traffic_status_types);
        if (typeView != null) {
            typeView.removeAllViews();

            for (TrafficType tt : ts.trafficTypes) {
                typeView.addView(inflateTrafficType(tt, typeView));
            }
        }
    }

    private View inflateTrafficType(TrafficType tt, ViewGroup viewGroup) {
        View v = getActivity().getLayoutInflater().inflate(
                R.layout.traffic_status_type, viewGroup, false);

        TextView transportText = (TextView) v
                .findViewById(R.id.traffic_status_type_transport);
        transportText.setText(getStringResourceForTransport(tt.type));
        ImageView status = (ImageView) v
                .findViewById(R.id.traffic_status_status);
        status.setImageResource(getImageResourceForStatus(tt.status));

        ImageView transportImage = (ImageView) v
                .findViewById(R.id.traffic_status_type_transport_image);
        transportImage.setImageResource(getImageResourceForTransport(tt.type));

        LinearLayout events = (LinearLayout) v
                .findViewById(R.id.traffic_status_type_events);
        events.removeAllViews();
        for (TrafficEvent te : tt.events) {
            events.addView(inflateTrafficEvent(te, events));
        }

        return v;
    }

    private View inflateTrafficEvent(TrafficEvent te, ViewGroup viewGroup) {
        View v = getActivity().getLayoutInflater().inflate(
                R.layout.traffic_status_event, viewGroup, false);

        TextView message = (TextView) v
                .findViewById(R.id.traffic_status_event_message);
        message.setText(te.message);
        ImageView status = (ImageView) v
                .findViewById(R.id.traffic_status_event_status);
        status.setImageResource(getImageResourceForStatus(te.status));

        return v;
    }

    private int getStringResourceForTransport(String transport) {
        if ("MET".equals(transport)) {
            return R.string.metros;
        } else if ("TRN".equals(transport)) {
            return R.string.trains;
        } else if ("TRM".equals(transport)) {
            return R.string.trams;
        } else if ("TRC".equals(transport)) {
            return R.string.tram_cars;
        } else if ("SHP".equals(transport)) {
            return R.string.boats;
        } else {
            return R.string.buses;
        }
    }

    private int getImageResourceForTransport(String transport) {
        if ("MET".equals(transport)) {
            return R.drawable.transport_metro;
        } else if ("TRN".equals(transport)) {
            return R.drawable.transport_train;
        } else if ("TRM".equals(transport)) {
            return R.drawable.transport_lokalbana;
        } else if ("TRC".equals(transport)) {
            return R.drawable.transport_tram_car;
        } else if ("SHP".equals(transport)) {
            return R.drawable.transport_boat;
        } else {
            return R.drawable.transport_bus;
        }
    }

    private int getImageResourceForStatus(int status) {
        switch (status) {
        case 1:
            return R.drawable.traffic_status_good;
        case 2:
            return R.drawable.traffic_status_minor;
        }
        return R.drawable.traffic_status_major;
    }

    /**
     * Show progress dialog.
     */
    private void showProgress() {
        mProgress.setVisibility(View.VISIBLE);
    }

    /**
     * Dismiss the progress dialog.
     */
    private void dismissProgress() {
        mProgress.setVisibility(View.GONE);
    }

    /**
     * Background job for getting {@link TrafficStatus}s.
     */
    private class GetTrafficStatusTask extends
            AsyncTask<Void, Void, TrafficStatus> {
        private boolean mWasSuccess = true;

        @Override
        public void onPreExecute() {
            showProgress();
        }

        @Override
        protected TrafficStatus doInBackground(Void... params) {
            DeviationStore ds = new DeviationStore();
            try {
                return ds.getTrafficStatus();
            } catch (IOException e) {
                mWasSuccess = false;
                return null;
            }
        }

        @Override
        protected void onPostExecute(TrafficStatus result) {
            dismissProgress();

            if (mWasSuccess) {
                inflateViews(result);
            } else {
                Toast.makeText(getActivity(), R.string.network_problem_message,
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}
