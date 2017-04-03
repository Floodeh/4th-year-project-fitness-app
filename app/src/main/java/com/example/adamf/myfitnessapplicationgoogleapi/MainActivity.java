package com.example.adamf.myfitnessapplicationgoogleapi;

import android.graphics.Color;
import android.os.AsyncTask;
import android.content.Intent;
import android.content.IntentSender;
import android.nfc.Tag;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;


import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.fitness.data.Subscription;
import com.google.android.gms.fitness.request.DataDeleteRequest;
import com.google.android.gms.fitness.request.DataUpdateRequest;
import com.google.android.gms.fitness.result.DailyTotalResult;
import com.google.android.gms.fitness.result.ListSubscriptionsResult;
import com.microsoft.windowsazure.mobileservices.*;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.DataSourcesRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.fitness.result.DataSourcesResult;
import com.google.android.gms.fitness.result.DataReadResult;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.Bucket;
import android.view.View;
import android.widget.Button;
import android.view.View.OnClickListener;
import com.google.android.gms.fitness.*;
import com.google.android.gms.fitness.Fitness.*;


import org.eazegraph.lib.charts.BarChart;
import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.charts.ValueLineChart;
import org.eazegraph.lib.models.BarModel;
import org.eazegraph.lib.models.PieModel;
import org.eazegraph.lib.models.ValueLinePoint;
import org.eazegraph.lib.models.ValueLineSeries;

import java.util.concurrent.TimeUnit;

import static com.google.android.gms.fitness.data.Field.FIELD_STEPS;
import static java.util.concurrent.TimeUnit.SECONDS;

public class MainActivity extends AppCompatActivity implements OnDataPointListener,
GoogleApiClient.ConnectionCallbacks,
GoogleApiClient.OnConnectionFailedListener,
View.OnClickListener{

    private Button mButtonViewWeek;
    private Button mButtonViewWeekCalorie;
    private Button mButtonViewWeekTime;
    private Button mButtonViewToday;
    private Button mButtonViewTodayTime;
    private Button mButtonViewTodayCalorie;
    private Button mButtonViewTodayDistance;
    private Button mButtonAddSteps;
    private Button mButtonUpdateSteps;
    private Button mButtonDeleteSteps;
    private Button mCancelSubscriptionsBtn;
    private Button mShowSubscriptionsBtn;



    private ResultCallback<Status> mSubscribeResultCallback;
    private ResultCallback<Status> mCancelSubscriptionResultCallback;
    private ResultCallback<ListSubscriptionsResult> mListSubscriptionsResultCallback;

    private MobileServiceClient mClient;
    private static final int REQUEST_OAUTH = 1;
    private static final String AUTH_PENDING = "auth_state_pending";
    private boolean authInProgress = false;
    private GoogleApiClient mApiClient;
    String TAG = "YOUR-TAG-NAME";


    private int monday = 0;
    private int tuesday = 0;
    private int wednesday = 0;
    private int thursday = 0;
    private int friday = 0;
    private int saturday = 0;
    private int sunday = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            authInProgress = savedInstanceState.getBoolean(AUTH_PENDING);
        }

        //initViews();
        initCallbacks();


        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(Fitness.SENSORS_API)
                .addApi(Fitness.HISTORY_API)
                .addApi(Fitness.RECORDING_API)
                .addApi(Fitness.GOALS_API)
                .addApi(Fitness.SESSIONS_API)
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                .addScope(new Scope(Scopes.FITNESS_LOCATION_READ_WRITE))
                .addScope(new Scope(Scopes.FITNESS_BODY_READ_WRITE))
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                //.enableAutoManage(this, 0, this)
                .build();

        initViews();
    }

    private void initViews() {

        //TextView textView = (TextView) findViewById(R.id.textView01);
        //textView.setText("Today's Steps: ");

        mCancelSubscriptionsBtn = (Button) findViewById(R.id.btn_cancel_subscriptions);
        mShowSubscriptionsBtn = (Button) findViewById(R.id.btn_show_subscriptions);
        mButtonViewWeek = (Button) findViewById(R.id.btn_view_week);
        mButtonViewWeekCalorie = (Button) findViewById(R.id.btn_view_week_calorie);
        mButtonViewWeekTime = (Button) findViewById(R.id.btn_view_week_time);
        mButtonViewToday = (Button) findViewById(R.id.btn_view_today);
        mButtonViewTodayTime = (Button) findViewById(R.id.btn_view_today_time);
        mButtonViewTodayCalorie = (Button) findViewById(R.id.btn_view_today_calorie);
        mButtonViewTodayDistance = (Button) findViewById(R.id.btn_view_today_distance);
//        mButtonAddSteps = (Button) findViewById(R.id.btn_add_steps);
//        mButtonUpdateSteps = (Button) findViewById(R.id.btn_update_steps);
        mButtonDeleteSteps = (Button) findViewById(R.id.btn_delete_steps);


        mButtonViewWeek.setOnClickListener(this);
        mButtonViewWeekCalorie.setOnClickListener(this);
        mButtonViewWeekTime.setOnClickListener(this);
        mButtonViewToday.setOnClickListener(this);
        mButtonViewTodayTime.setOnClickListener(this);
        mButtonViewTodayCalorie.setOnClickListener(this);
        mButtonViewTodayDistance.setOnClickListener(this);
//        mButtonAddSteps.setOnClickListener(this);
//        mButtonUpdateSteps.setOnClickListener(this);
        mButtonDeleteSteps.setOnClickListener(this);
        mCancelSubscriptionsBtn.setOnClickListener(this);
        mShowSubscriptionsBtn.setOnClickListener(this);






        //DailyTotalResult result = Fitness.HistoryApi.readDailyTotal( mApiClient, DataType.TYPE_STEP_COUNT_DELTA ).await(1, TimeUnit.MINUTES);
        //showDataSet(result.getTotal());


//        DailyTotalResult result = Fitness.HistoryApi.readDailyTotal( mApiClient, DataType.TYPE_STEP_COUNT_DELTA ).await(1, TimeUnit.MINUTES);
//        String adam = result.toString();
//        int adamflood = Integer.parseInt(adam);
//
//        PieChart mPieChart = (PieChart) findViewById(R.id.piechart);
//
//        mPieChart.addPieSlice(new PieModel("Steps", 5000,Color.parseColor("#FE6DA8")));
//        mPieChart.addPieSlice(new PieModel("Goal", 10000, Color.parseColor("#56B7F1")));
//
//        mPieChart.startAnimation();

//        ValueLineChart mCubicValueLineChart = (ValueLineChart) findViewById(R.id.cubiclinechart);
//
//        ValueLineSeries series = new ValueLineSeries();
//        series.setColor(0xFF56B7F1);
//
//        series.addPoint(new ValueLinePoint("Jan", 2.4f));
//        series.addPoint(new ValueLinePoint("Feb", 3.4f));
//        series.addPoint(new ValueLinePoint("Mar", .4f));
//        series.addPoint(new ValueLinePoint("Apr", 1.2f));
//        series.addPoint(new ValueLinePoint("Mai", 2.6f));
//        series.addPoint(new ValueLinePoint("Jun", 1.0f));
//        series.addPoint(new ValueLinePoint("Jul", 3.5f));
//        series.addPoint(new ValueLinePoint("Aug", 2.4f));
//        series.addPoint(new ValueLinePoint("Sep", 2.4f));
//        series.addPoint(new ValueLinePoint("Oct", 3.4f));
//        series.addPoint(new ValueLinePoint("Nov", .4f));
//        series.addPoint(new ValueLinePoint("Dec", 1.3f));
//
//        mCubicValueLineChart.addSeries(series);
//        mCubicValueLineChart.startAnimation();
    }

    private void initCallbacks() {
        mSubscribeResultCallback = new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if (status.isSuccess()) {
                    if (status.getStatusCode() == FitnessStatusCodes.SUCCESS_ALREADY_SUBSCRIBED) {
                        Log.e( "RecordingAPI", "Already subscribed to the Recording API");
                    } else {
                        Log.e("RecordingAPI", "Subscribed to the Recording API");
                    }
                }
            }
        };

        mCancelSubscriptionResultCallback = new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if (status.isSuccess()) {
                    Log.e( "RecordingAPI", "Canceled subscriptions!");
                } else {
                    // Subscription not removed
                    Log.e("RecordingAPI", "Failed to cancel subscriptions");
                }
            }
        };

        mListSubscriptionsResultCallback = new ResultCallback<ListSubscriptionsResult>() {
            @Override
            public void onResult(@NonNull ListSubscriptionsResult listSubscriptionsResult) {
                for (Subscription subscription : listSubscriptionsResult.getSubscriptions()) {
                    DataType dataType = subscription.getDataType();
                    Log.e( "RecordingAPI", dataType.getName() );
                    for (Field field : dataType.getFields() ) {
                        Log.e( "RecordingAPI", field.toString() );
                    }
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        mApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();

        Fitness.SensorsApi.remove(mApiClient, this)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            mApiClient.disconnect();
                        }
                    }
                });
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(AUTH_PENDING, authInProgress);
    }

    private void registerFitnessDataListener(DataSource dataSource, DataType dataType) {

        SensorRequest request = new SensorRequest.Builder()
                .setDataSource(dataSource)
                .setDataType(dataType)
                .setSamplingRate(2, SECONDS)
                .build();

        Fitness.SensorsApi.add(mApiClient, request, this)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            Log.e("GoogleFit", "SensorApi successfully added");
                        }
                    }
                });
    }

    @Override
    public void onConnected(Bundle bundle) {
        DataSourcesRequest dataSourceRequest = new DataSourcesRequest.Builder()
                .setDataTypes(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                .setDataSourceTypes(DataSource.TYPE_RAW)
                .build();

        DataSourcesRequest dataSourceRequest1 = new DataSourcesRequest.Builder()
                .setDataTypes(DataType.TYPE_ACTIVITY_SEGMENT)
                .setDataSourceTypes(DataSource.TYPE_RAW)
                .build();

        DataSourcesRequest dataSourceRequest2 = new DataSourcesRequest.Builder()
                .setDataTypes(DataType.TYPE_CALORIES_EXPENDED)
                .setDataSourceTypes(DataSource.TYPE_RAW)
                .build();

        //Fitness.HistoryApi.readDailyTotal(mApiClient, DataType.TYPE_DISTANCE_CUMULATIVE);



        Fitness.RecordingApi.subscribe(mApiClient, DataType.TYPE_STEP_COUNT_DELTA)
                .setResultCallback(mSubscribeResultCallback);
        //Fitness.RecordingApi.subscribe(mApiClient, DataType.TYPE_DISTANCE_DELTA)
        //      .setResultCallback(mSubscribeResultCallback);
        Fitness.RecordingApi.subscribe(mApiClient, DataType.TYPE_ACTIVITY_SEGMENT)
                .setResultCallback(mSubscribeResultCallback);
        Fitness.RecordingApi.subscribe(mApiClient, DataType.TYPE_ACTIVITY_SAMPLES)
                .setResultCallback(mSubscribeResultCallback);
        Fitness.RecordingApi.subscribe(mApiClient, DataType.TYPE_CALORIES_EXPENDED)
                .setResultCallback(mSubscribeResultCallback);


        new FetchStepsAsync().execute();
        //new FetchTimeAsync().execute();
        //new FetchCalorieAsync().execute();


        //Fitness.HistoryApi.readDailyTotal(mApiClient, DataType.TYPE_STEP_COUNT_DELTA);

//lol
//        BarChart mBarChart = (BarChart) findViewById(R.id.barchart);
//
//        mBarChart.addBar(new BarModel(monday, 0xFF123456));
//        mBarChart.addBar(new BarModel(tuesday,  0xFF343456));
//        mBarChart.addBar(new BarModel(wednesday, 0xFF563456));
//        mBarChart.addBar(new BarModel(thursday, 0xFF873F56));
//        mBarChart.addBar(new BarModel(friday, 0xFF56B7F1));
//        mBarChart.addBar(new BarModel(saturday,  0xFF343456));
//        mBarChart.addBar(new BarModel(sunday, 0xFF1FF4AC));
//
//        mBarChart.startAnimation();

        ResultCallback<DataSourcesResult> dataSourcesResultCallback = new ResultCallback<DataSourcesResult>() {
            @Override
            public void onResult(DataSourcesResult dataSourcesResult) {
                for (DataSource dataSource : dataSourcesResult.getDataSources()) {
                    if (DataType.TYPE_STEP_COUNT_CUMULATIVE.equals(dataSource.getDataType())) {
                        registerFitnessDataListener(dataSource, DataType.TYPE_STEP_COUNT_CUMULATIVE);

                    }
                }
            }
        };

        ResultCallback<DataSourcesResult> dataSourcesResultCallback1 = new ResultCallback<DataSourcesResult>() {
            @Override
            public void onResult(DataSourcesResult dataSourcesResult) {
                for (DataSource dataSource : dataSourcesResult.getDataSources()) {
                    if (DataType.TYPE_CALORIES_EXPENDED.equals(dataSource.getDataType())) {
                        registerFitnessDataListener(dataSource, DataType.TYPE_CALORIES_EXPENDED);

                    }
                }
            }
        };

        Fitness.SensorsApi.findDataSources(mApiClient, dataSourceRequest)
                .setResultCallback(dataSourcesResultCallback);

        Fitness.SensorsApi.findDataSources(mApiClient, dataSourceRequest1)
                .setResultCallback(dataSourcesResultCallback);

        Fitness.SensorsApi.findDataSources(mApiClient, dataSourceRequest2)
                .setResultCallback(dataSourcesResultCallback1);

    }


    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btn_view_week: {
                new ViewWeekStepCountTask().execute();
                break;
            }
            case R.id.btn_view_today: {
                new ViewTodaysStepCountTask().execute();
                break;
            }
            case R.id.btn_view_today_time: {
                new ViewTodaysTimeCountTask().execute();
                break;
            }
            case R.id.btn_view_week_time: {
                new ViewWeekTimeCountTask().execute();
                break;
            }
            case R.id.btn_view_today_calorie: {
                new ViewTodaysCalorieCountTask().execute();
                break;
            }
            case R.id.btn_view_week_calorie: {
                new ViewWeekCalorieCountTask().execute();
                break;
            }
//            case R.id.btn_view_today_distance: {
//                new ViewTodaysDistanceCountTask().execute();
//                break;
//            }
//            case R.id.btn_add_steps: {
//                new AddStepsToGoogleFitTask().execute();
//                break;
//            }
//            case R.id.btn_update_steps: {
//                new UpdateStepsOnGoogleFitTask().execute();
//                break;
//            }
            case R.id.btn_delete_steps: {
                new DeleteYesterdaysStepsTask().execute();
                break;
            }
            case R.id.btn_cancel_subscriptions: {
                cancelSubscriptions();
                break;
            }
            case R.id.btn_show_subscriptions: {
                showSubscriptions();
                break;
            }
        }
    }

    private void showSubscriptions() {
        Fitness.RecordingApi.listSubscriptions(mApiClient)
                .setResultCallback(mListSubscriptionsResultCallback);
    }

    private void cancelSubscriptions() {
        Fitness.RecordingApi.unsubscribe(mApiClient, DataType.TYPE_STEP_COUNT_DELTA)
                .setResultCallback(mCancelSubscriptionResultCallback);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e("HistoryAPI", "onConnectionSuspended");

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (!authInProgress) {
            try {
                authInProgress = true;
                connectionResult.startResolutionForResult(MainActivity.this, REQUEST_OAUTH);
            } catch (IntentSender.SendIntentException e) {

            }
        } else {
            Log.e("GoogleFit", "authInProgress");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_OAUTH) {
            authInProgress = false;
            if (resultCode == RESULT_OK) {
                if (!mApiClient.isConnecting() && !mApiClient.isConnected()) {
                    mApiClient.connect();
                }
            } else if (resultCode == RESULT_CANCELED) {
                Log.e("GoogleFit", "RESULT_CANCELED");
            }
        } else {
            Log.e("GoogleFit", "requestCode NOT request_oauth");
        }
    }

    @Override
    public void onDataPoint(DataPoint dataPoint) {
        for (final Field field : dataPoint.getDataType().getFields()) {
            final Value value = dataPoint.getValue(field);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "Field: " + field.getName() + " Value: " + value, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    private class ViewWeekStepCountTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            displayLastWeeksData();
            return null;
        }
    }

    private class ViewWeekCalorieCountTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            displayLastWeeksCaloriesData();
            return null;
        }
    }

    private class ViewWeekTimeCountTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            displayLastWeeksTimeData();
            return null;
        }
    }

    private class ViewTodaysStepCountTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            displayStepDataForToday();
            return null;
        }
    }

    private class ViewTodaysTimeCountTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            displayTimeDataForToday();
            return null;
        }
    }

    private class ViewTodaysCalorieCountTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            displayCalorieDataForToday();
            return null;
        }
    }

//    private class ViewTodaysDistanceCountTask extends AsyncTask<Void, Void, Void> {
//        protected Void doInBackground(Void... params) {
//            displayDistanceDataForToday();
//            return null;
//        }
//    }

//    private class AddStepsToGoogleFitTask extends AsyncTask<Void, Void, Void> {
//        protected Void doInBackground(Void... params) {
//            addStepDataToGoogleFit();
//            displayLastWeeksData();
//            return null;
//        }
//    }
//
//    private class UpdateStepsOnGoogleFitTask extends AsyncTask<Void, Void, Void> {
//        protected Void doInBackground(Void... params) {
//            updateStepDataOnGoogleFit();
//            displayLastWeeksData();
//            return null;
//        }
//    }

    private class DeleteYesterdaysStepsTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            deleteStepDataOnGoogleFit();
            displayLastWeeksData();
            return null;
        }
    }


    private void displayLastWeeksData() {
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.WEEK_OF_YEAR, -1);
        long startTime = cal.getTimeInMillis();

        java.text.DateFormat dateFormat = DateFormat.getDateInstance();
        Log.e("History", "Range Start: " + dateFormat.format(startTime));
        Log.e("History", "Range End: " + dateFormat.format(endTime));

        //Check how many steps were walked and recorded in the last 7 days
        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();

        DataReadResult dataReadResult = Fitness.HistoryApi.readData(mApiClient, readRequest).await(1, TimeUnit.MINUTES);

        //Used for aggregated data
        if (dataReadResult.getBuckets().size() > 0) {
            Log.e("History", "Number of buckets: " + dataReadResult.getBuckets().size());
            for (Bucket bucket : dataReadResult.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    showDataSet(dataSet);
                }
            }
        }
        //Used for non-aggregated data
        else if (dataReadResult.getDataSets().size() > 0) {
            Log.e("History", "Number of returned DataSets: " + dataReadResult.getDataSets().size());
            for (DataSet dataSet : dataReadResult.getDataSets()) {
                showDataSet(dataSet);
            }
        }

    }

    private void displayLastWeeksCaloriesData() {
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.WEEK_OF_YEAR, -1);
        long startTime = cal.getTimeInMillis();

        java.text.DateFormat dateFormat = DateFormat.getDateInstance();
        Log.e("History", "Range Start: " + dateFormat.format(startTime));
        Log.e("History", "Range End: " + dateFormat.format(endTime));

        //Check how many calories were burned and recorded in the last 7 days
        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_CALORIES_EXPENDED, DataType.AGGREGATE_CALORIES_EXPENDED)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();

        DataReadResult dataReadResult = Fitness.HistoryApi.readData(mApiClient, readRequest).await(1, TimeUnit.MINUTES);

        //Used for aggregated data
        if (dataReadResult.getBuckets().size() > 0) {
            Log.e("History", "Number of buckets: " + dataReadResult.getBuckets().size());
            for (Bucket bucket : dataReadResult.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    showDataSet(dataSet);
                }
            }
        }
        //Used for non-aggregated data
        else if (dataReadResult.getDataSets().size() > 0) {
            Log.e("History", "Number of returned DataSets: " + dataReadResult.getDataSets().size());
            for (DataSet dataSet : dataReadResult.getDataSets()) {
                showDataSet(dataSet);
            }
        }

    }

    private void displayLastWeeksTimeData() {
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.WEEK_OF_YEAR, -1);
        long startTime = cal.getTimeInMillis();

        java.text.DateFormat dateFormat = DateFormat.getDateInstance();
        Log.e("History", "Range Start: " + dateFormat.format(startTime));
        Log.e("History", "Range End: " + dateFormat.format(endTime));

        //Check how much time was spent active in the last 7 days
        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_ACTIVITY_SEGMENT, DataType.AGGREGATE_ACTIVITY_SUMMARY)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();

        DataReadResult dataReadResult = Fitness.HistoryApi.readData(mApiClient, readRequest).await(1, TimeUnit.MINUTES);

        //Used for aggregated data
        if (dataReadResult.getBuckets().size() > 0) {
            Log.e("History", "Number of buckets: " + dataReadResult.getBuckets().size());
            for (Bucket bucket : dataReadResult.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    showDataSet(dataSet);
                }
            }
        }
        //Used for non-aggregated data
        else if (dataReadResult.getDataSets().size() > 0) {
            Log.e("History", "Number of returned DataSets: " + dataReadResult.getDataSets().size());
            for (DataSet dataSet : dataReadResult.getDataSets()) {
                showDataSet(dataSet);
            }
        }

    }

    private void showDataSet(DataSet dataSet) {
        Log.e("History", "Data returned for Data type: " + dataSet.getDataType().getName());
        final DateFormat dateFormat = DateFormat.getDateInstance();
        final DateFormat timeFormat = DateFormat.getTimeInstance();

        for (DataPoint dp : dataSet.getDataPoints()) {
            Log.e("History", "Data point:");
            Log.e("History", "\tType: " + dp.getDataType().getName());
            Log.e("History", "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)) + " " + timeFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            Log.e("History", "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)) + " " + timeFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            for(Field field : dp.getDataType().getFields()) {
                Log.e("History", "\tField: " + field.getName() +
                        " Value: " + dp.getValue(field));

                final DataPoint adamDp = dp;
                final Field adamField = field;
                //DailyTotalResult result = Fitness.HistoryApi.readDailyTotal( mApiClient, DataType.TYPE_STEP_COUNT_DELTA ).await(1, TimeUnit.MINUTES);
//                String adam = adamDp.getValue(adamField).toString();
//                final int adamflood = Integer.parseInt(adam);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        BarChart mBarChart = (BarChart) findViewById(R.id.barchart);
//
//                        mBarChart.addBar(new BarModel(adamflood, 0xFF123456));
//                        mBarChart.addBar(new BarModel(adamflood,  0xFF343456));
//                        mBarChart.addBar(new BarModel(wednesday, 0xFF563456));
//                        mBarChart.addBar(new BarModel(thursday, 0xFF873F56));
//                        mBarChart.addBar(new BarModel(friday, 0xFF56B7F1));
//                        mBarChart.addBar(new BarModel(saturday,  0xFF343456));
//                        mBarChart.addBar(new BarModel(sunday, 0xFF1FF4AC));
//
//                        mBarChart.startAnimation();
                        Toast.makeText(getApplicationContext(), "History for: " + adamField.getName() + "\t\tStart: " + dateFormat.format(adamDp.getStartTime(TimeUnit.MILLISECONDS)) + " " + timeFormat.format(adamDp.getStartTime(TimeUnit.MILLISECONDS)) + "\t\tEnd: " + dateFormat.format(adamDp.getEndTime(TimeUnit.MILLISECONDS)) + " " + timeFormat.format(adamDp.getEndTime(TimeUnit.MILLISECONDS)) + "\tValue: " + adamDp.getValue(adamField), Toast.LENGTH_SHORT).show();
                    }
                });

            }


        }
    }

    private void displayStepDataForToday() {
        DailyTotalResult result = Fitness.HistoryApi.readDailyTotal( mApiClient, DataType.TYPE_STEP_COUNT_DELTA ).await(1, TimeUnit.MINUTES);
        showDataSet(result.getTotal());

        //Trying to get the step count value for today
        PendingResult<DailyTotalResult> result1 =
                Fitness.HistoryApi.readDailyTotal(mApiClient, DataType.TYPE_STEP_COUNT_DELTA);
        DailyTotalResult totalResult = result1.await(30, SECONDS);
        if (totalResult.getStatus().isSuccess()) {
            DataSet totalSet = totalResult.getTotal();
            long total = totalSet.isEmpty()
                    ? 0
                    : totalSet.getDataPoints().get(0).getValue(FIELD_STEPS).asInt();
        } else {
            // handle failure
        }


    }

    private void displayTimeDataForToday() {
        DailyTotalResult result = Fitness.HistoryApi.readDailyTotal( mApiClient, DataType.TYPE_ACTIVITY_SEGMENT ).await(1, TimeUnit.MINUTES);
        showDataSet(result.getTotal());
    }

    private void displayCalorieDataForToday() {
        DailyTotalResult result = Fitness.HistoryApi.readDailyTotal( mApiClient, DataType.TYPE_CALORIES_EXPENDED ).await(1, TimeUnit.MINUTES);
        showDataSet(result.getTotal());
    }

//    private void displayDistanceDataForToday() {
//        DailyTotalResult result = Fitness.HistoryApi.readDailyTotal( mApiClient, DataType.TYPE_DISTANCE_DELTA ).await(1, TimeUnit.MINUTES);
//        showDataSet(result.getTotal());
//    }

    private void addStepDataToGoogleFit() {
        //Adds steps spread out evenly from start time to end time
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.HOUR_OF_DAY, -1);
        long startTime = cal.getTimeInMillis();

        DataSource dataSource = new DataSource.Builder()
                .setAppPackageName(this)
                .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                .setName("Step Count")
                .setType(DataSource.TYPE_RAW)
                .build();

        int stepCountDelta = 1000000;
        DataSet dataSet = DataSet.create(dataSource);

        DataPoint point = dataSet.createDataPoint()
                .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS);
        point.getValue(FIELD_STEPS).setInt(stepCountDelta);
        dataSet.add(point);

        Status status = Fitness.HistoryApi.insertData(mApiClient, dataSet).await(1, TimeUnit.MINUTES);

        if (!status.isSuccess()) {
            Log.e( "History", "Problem with inserting data: " + status.getStatusMessage());
        } else {
            Log.e( "History", "data inserted" );
        }
    }

    private void updateStepDataOnGoogleFit() {
        //If two entries overlap, the new data is dropped when trying to insert. Instead, you need to use update
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.HOUR_OF_DAY, -1);
        long startTime = cal.getTimeInMillis();

        DataSource dataSource = new DataSource.Builder()
                .setAppPackageName(this)
                .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                .setName("Step Count")
                .setType(DataSource.TYPE_RAW)
                .build();

        int stepCountDelta = 2000000;
        DataSet dataSet = DataSet.create(dataSource);

        DataPoint point = dataSet.createDataPoint()
                .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS);
        point.getValue(FIELD_STEPS).setInt(stepCountDelta);
        dataSet.add(point);

        DataUpdateRequest updateRequest = new DataUpdateRequest.Builder().setDataSet(dataSet).setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS).build();
        Fitness.HistoryApi.updateData(mApiClient, updateRequest).await(1, TimeUnit.MINUTES);
    }

    private void deleteStepDataOnGoogleFit() {
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        long startTime = cal.getTimeInMillis();

        DataDeleteRequest request = new DataDeleteRequest.Builder()
                .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
                .build();

        Fitness.HistoryApi.deleteData(mApiClient, request).await(1, TimeUnit.MINUTES);
    }


////total steps taken
    private class FetchStepsAsync extends AsyncTask<Object, Object, Long> {
        protected Long doInBackground(Object... params) {
            long total = 0;
            PendingResult<DailyTotalResult> result = Fitness.HistoryApi.readDailyTotal(mApiClient, DataType.TYPE_STEP_COUNT_DELTA);
            DailyTotalResult totalResult = result.await(30, SECONDS);
            if (totalResult.getStatus().isSuccess()) {
                DataSet totalSet = totalResult.getTotal();
                if (totalSet != null) {
                    total = totalSet.isEmpty()
                            ? 0
                            : totalSet.getDataPoints().get(0).getValue(FIELD_STEPS).asInt();
                }
            } else {
                Log.w(TAG, "There was a problem getting the step count.");
            }

            return total;
        }


        @Override
        protected void onPostExecute(final Long aLong) {
            super.onPostExecute(aLong);

            //Total steps covered for that day
            Log.i(TAG, "Total steps: " + aLong);


            String adam = aLong.toString();
            int adamVal = Integer.parseInt(adam);
            PieChart mPieChart = (PieChart) findViewById(R.id.piechart);

            mPieChart.addPieSlice(new PieModel("Steps Today", adamVal,Color.parseColor("#FE6DA8")));
            //mPieChart.addPieSlice(new PieModel("Goal", 10000, Color.parseColor("#56B7F1")));

            mPieChart.startAnimation();

            TextView textView = (TextView) findViewById(R.id.textView01);
            textView.setText(adam);

//            BarChart mBarChart = (BarChart) findViewById(R.id.barchart);
//
//            mBarChart.addBar(new BarModel(adamVal, 0xFF123456));
//            mBarChart.addBar(new BarModel(5000,  0xFF343456));
//            mBarChart.addBar(new BarModel(2000, 0xFF563456));
//            mBarChart.addBar(new BarModel(100, 0xFF873F56));
//            mBarChart.addBar(new BarModel(5000, 0xFF56B7F1));
//            mBarChart.addBar(new BarModel(2.f,  0xFF343456));
//            mBarChart.addBar(new BarModel(0.4f, 0xFF1FF4AC));
//            mBarChart.addBar(new BarModel(4.f,  0xFF1BA4E6));
//
//            mBarChart.startAnimation();
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    Toast.makeText(getApplicationContext(), "Total Steps: " + aLong, Toast.LENGTH_SHORT).show();
//                }
//            });
        }
    }

    ////total time active
//    private class FetchTimeAsync extends AsyncTask<Object, Object, Long> {
//        protected Long doInBackground(Object... params) {
//            long total = 0;
//            PendingResult<DailyTotalResult> result = Fitness.HistoryApi.readDailyTotal(mApiClient, DataType.TYPE_ACTIVITY_SEGMENT);
//            DailyTotalResult totalResult = result.await(30, TimeUnit.SECONDS);
//            if (totalResult.getStatus().isSuccess()) {
//                DataSet totalSet = totalResult.getTotal();
//                if (totalSet != null) {
//                    total = totalSet.isEmpty()
//                            ? 0
//                            : totalSet.getDataPoints().get(0).getValue(Field.FIELD_DURATION).asInt();
//                }
//            } else {
//                Log.w(TAG, "There was a problem getting the time count.");
//            }
//            return total;
//        }
//
//
//        @Override
//        protected void onPostExecute(final Long aLong) {
//            super.onPostExecute(aLong);
//
//            //Total steps covered for that day
//            Log.i(TAG, "Total time: " + aLong);
//
//        }
//    }

    private class FetchCalorieAsync extends AsyncTask<Object, Object, Long> {
        protected Long doInBackground(Object... params) {
            long total = 0;
            PendingResult<DailyTotalResult> result = Fitness.HistoryApi.readDailyTotal(mApiClient, DataType. TYPE_CALORIES_EXPENDED);
            DailyTotalResult totalResult = result.await(30, TimeUnit.SECONDS);
            if (totalResult.getStatus().isSuccess()) {
                DataSet totalSet = totalResult.getTotal();
                if (totalSet != null) {
                    total = totalSet.isEmpty()
                            ? 0
                            : totalSet.getDataPoints().get(0).getValue(Field.FIELD_CALORIES).asInt();
                }
            } else {
                Log.w(TAG, "There was a problem getting the calories.");
            }
            return total;
        }


        @Override
        protected void onPostExecute(Long aLong) {
            super.onPostExecute(aLong);

//            String adam = aLong.toString();
//            int adamVal = Integer.parseInt(adam);
//            PieChart mPieChart = (PieChart) findViewById(R.id.piechart1);
//
//            mPieChart.addPieSlice(new PieModel("Calories", adamVal,Color.parseColor("#FE6DA8")));
//            //mPieChart.addPieSlice(new PieModel("Goal", 10000, Color.parseColor("#56B7F1")));
//
//            mPieChart.startAnimation();

            //Total calories burned for that day
            Log.i(TAG, "Total calories: " + aLong);

        }
    }

    public void subscribe() {
        // To create a subscription, invoke the Recording API. As soon as the subscription is
        // active, fitness data will start recording.
        Fitness.RecordingApi.subscribe(mApiClient, DataType.TYPE_STEP_COUNT_CUMULATIVE)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            if (status.getStatusCode()
                                    == FitnessStatusCodes.SUCCESS_ALREADY_SUBSCRIBED) {
                                Log.i(TAG, "Existing subscription for activity detected.");
                            } else {
                                Log.i(TAG, "Successfully subscribed!");
                            }
                        } else {
                            Log.w(TAG, "There was a problem subscribing.");
                        }
                    }
                });

    }

    private class VerifyDataTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {

            long total = 0;

            PendingResult<DailyTotalResult> result = Fitness.HistoryApi.readDailyTotal(mApiClient, DataType.TYPE_STEP_COUNT_DELTA);
            DailyTotalResult totalResult = result.await(30, SECONDS);
            if (totalResult.getStatus().isSuccess()) {
                DataSet totalSet = totalResult.getTotal();
                total = totalSet.isEmpty()
                        ? 0
                        : totalSet.getDataPoints().get(0).getValue(FIELD_STEPS).asInt();
            } else {
                Log.w(TAG, "There was a problem getting the step count.");
            }

            Log.i(TAG, "Total steps: " + total);

            return null;
        }
    }

    private void readData() {
        new VerifyDataTask().execute();
    }
}



