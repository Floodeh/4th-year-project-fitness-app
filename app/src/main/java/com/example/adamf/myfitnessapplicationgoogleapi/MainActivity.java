package com.example.adamf.myfitnessapplicationgoogleapi;

import android.Manifest;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.content.Intent;
import android.content.IntentSender;
import android.nfc.Tag;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;


import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.data.Subscription;
import com.google.android.gms.fitness.request.DataDeleteRequest;
import com.google.android.gms.fitness.request.DataUpdateRequest;
import com.google.android.gms.fitness.request.SessionInsertRequest;
import com.google.android.gms.fitness.request.SessionReadRequest;
import com.google.android.gms.fitness.result.DailyTotalResult;
import com.google.android.gms.fitness.result.ListSubscriptionsResult;
import com.google.android.gms.fitness.result.SessionReadResult;
import com.google.android.gms.fitness.result.SessionStopResult;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
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
import com.txusballesteros.widgets.FitChart;
import com.txusballesteros.widgets.FitChartValue;


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

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 0;
    private Button mButtonViewWeek;
    private Button mButtonViewWeekStepsGraph;
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
    private Button mStartSessionBtn;
    private Button mStopSessionBtn;
    private Button mInsertSegmentBtn;
    private Button mReadSessionBtn;
    private Button mButtonViewWeekDistanceGraph;



    private ResultCallback<Status> mSubscribeResultCallback;
    private ResultCallback<Status> mCancelSubscriptionResultCallback;
    private ResultCallback<ListSubscriptionsResult> mListSubscriptionsResultCallback;

    private MobileServiceClient mClient;
    private static final int REQUEST_OAUTH = 1;
    private static final String AUTH_PENDING = "auth_state_pending";
    private boolean authInProgress = false;
    private GoogleApiClient mApiClient;
    String TAG = "YOUR-TAG-NAME";
    private static final String KEY_ = "AIzaSyBo-K-NkIn7edBi1DZckt4Xmb4qkfPJrRw";


    private int seventhDay = 0;
    private int sixthDay = 0;
    private int fifthDay = 0;
    private int fourthDay = 0;
    private int thirdDay = 0;
    private int secondDay = 0;
    private int yesterDay = 0;
    private double averageSteps = 0.0;

    private float seventhDayDistance = 0;
    private float sixthDayDistance = 0;
    private float fifthDayDistance = 0;
    private float fourthDayDistance = 0;
    private float thirdDayDistance = 0;
    private float secondDayDistance = 0;
    private float yesterDayDistance = 0;
    private float distanceToday = 0;
    private float averageDistance = 0;

    public int walkingtime = 0;
    public int stilltime = 0;
    public int runningtime = 0;

    private final String SESSION_NAME = "session name";
    private Session mSession;

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
                .addApi(ActivityRecognition.API)
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                .addScope(new Scope(Scopes.FITNESS_LOCATION_READ_WRITE))
                .addScope(new Scope(Scopes.FITNESS_BODY_READ_WRITE))
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                //.enableAutoManage(this, 0, this)
                .build();

        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        //int permissionCheck2 = ContextCompat.checkSelfPermission(this, Manifest.permission.AC)

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);


        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            //Execute location service call if user has explicitly granted ACCESS_FINE_LOCATION..
            Fitness.RecordingApi.subscribe(mApiClient, DataType.TYPE_DISTANCE_DELTA)
                    .setResultCallback(mSubscribeResultCallback);
        }

        initViews();
    }

    public static class ActivityRecognizedService extends IntentService {

        public int walkingtime = 0;
        public int stilltime = 0;
        public int runningtime = 0;


        public ActivityRecognizedService() {
            super("ActivityRecognizedService");
        }

        public ActivityRecognizedService(String name) {
            super(name);
        }

        @Override
        protected void onHandleIntent(Intent intent)
        {
            if(ActivityRecognitionResult.hasResult(intent)) {
                ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
                handleDetectedActivities( result.getProbableActivities() );
            }
        }
        private void handleDetectedActivities(List<DetectedActivity> probableActivities) {
            for( DetectedActivity activity : probableActivities ) {
                switch( activity.getType() ) {
                    case DetectedActivity.IN_VEHICLE: {
                        Log.e( "ActivityRecognition", "In Vehicle: " + activity.getConfidence() );
                        if( activity.getConfidence() >= 75 ) {
                            NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                            builder.setContentText( "Are you Driving?" );
                            builder.setSmallIcon( R.mipmap.ic_launcher );
                            builder.setContentTitle( getString( R.string.app_name ) );
                            NotificationManagerCompat.from(this).notify(0, builder.build());
                        }
                        break;
                    }
                    case DetectedActivity.ON_BICYCLE: {
                        Log.e( "ActivityRecognition", "On Bicycle: " + activity.getConfidence() );
                        if( activity.getConfidence() >= 75 ) {
                            NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                            builder.setContentText( "Are you Cycling?" );
                            builder.setSmallIcon( R.mipmap.ic_launcher );
                            builder.setContentTitle( getString( R.string.app_name ) );
                            NotificationManagerCompat.from(this).notify(0, builder.build());
                        }
                        break;
                    }
                    case DetectedActivity.ON_FOOT: {
                        Log.e( "ActivityRecognition", "On Foot: " + activity.getConfidence() );
                        break;
                    }
                    case DetectedActivity.RUNNING: {
                        Log.e( "ActivityRecognition", "Running: " + activity.getConfidence() );
                        runningtime = activity.getConfidence();
                        if( activity.getConfidence() >= 75 ) {

                            NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                            builder.setContentText( "Are you Running?" );
                            builder.setSmallIcon( R.mipmap.ic_launcher );
                            builder.setContentTitle( getString( R.string.app_name ) );
                            NotificationManagerCompat.from(this).notify(0, builder.build());
                        }
                        break;
                    }
                    case DetectedActivity.STILL: {
                        Log.e( "ActivityRecognition", "Still: " + activity.getConfidence() );
                        stilltime = activity.getConfidence();
                        if( activity.getConfidence() >= 75 ) {
                            NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                            builder.setContentText( "Are you standing still?" );
                            builder.setSmallIcon( R.mipmap.ic_launcher );
                            builder.setContentTitle( getString( R.string.app_name ) );
                            NotificationManagerCompat.from(this).notify(0, builder.build());
                        }
                        break;
                    }
                    case DetectedActivity.TILTING: {
                        Log.e( "ActivityRecognition", "Tilting: " + activity.getConfidence() );
                        break;
                    }
                    case DetectedActivity.WALKING: {
                        Log.e( "ActivityRecognition", "Walking: " + activity.getConfidence() );
                        walkingtime = activity.getConfidence();
                        if( activity.getConfidence() >= 75 ) {
                            NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                            builder.setContentText( "Are you walking?" );
                            builder.setSmallIcon( R.mipmap.ic_launcher );
                            builder.setContentTitle( getString( R.string.app_name ) );
                            NotificationManagerCompat.from(this).notify(0, builder.build());
                        }
                        break;
                    }
                    case DetectedActivity.UNKNOWN: {
                        Log.e( "ActivityRecogition", "Unknown: " + activity.getConfidence() );
                        break;
                    }
                }
            }
        }
    }


    public void startSession() {
        mSession = new Session.Builder()
                .setName("NewSessionTest")
                .setIdentifier(getString(R.string.app_name) + " " + System.currentTimeMillis())
                .setDescription("Running Session Description")
                .setStartTime(Calendar.getInstance().getTimeInMillis(), TimeUnit.MILLISECONDS)
                .setActivity(FitnessActivities.WALKING)
                .build();

        PendingResult<Status> pendingResult =
                Fitness.SessionsApi.startSession(mApiClient, mSession);

        pendingResult.setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            Log.i("App: ", "Successfully started session");
                        } else {
                            Log.i("App: +", "Failed to start session: " + status.getStatusMessage());
                        }
                    }
                }
        );
    }

    public void stopSession() {

        PendingResult<SessionStopResult> pendingResult =
                Fitness.SessionsApi.stopSession(mApiClient, mSession.getIdentifier());


        pendingResult.setResultCallback(new ResultCallback<SessionStopResult>() {
            @Override
            public void onResult(SessionStopResult sessionStopResult) {
                if( sessionStopResult.getStatus().isSuccess() ) {
                    Log.i("App", "Successfully stopped session");
                    if( sessionStopResult.getSessions() != null && !sessionStopResult.getSessions().isEmpty() ) {
                        Log.i("App:", "Session name: " + sessionStopResult.getSessions().get(0).getName());
                        Log.i("App:", "Session start: " + sessionStopResult.getSessions().get(0).getStartTime(TimeUnit.MILLISECONDS));
                        Log.i("App:", "Session end: " + sessionStopResult.getSessions().get(0).getEndTime(TimeUnit.MILLISECONDS));
                    }
                } else {
                    Log.i("App:", "Failed to stop session: " + sessionStopResult.getStatus().getStatusMessage());
                }
            }

        });
    }

    public void insertSegment() {
        if( !mApiClient.isConnected() ) {
            Toast.makeText(this, "Not connected to Google", Toast.LENGTH_SHORT).show();
            return;
        }

        Calendar calendar = Calendar.getInstance();
        Date now = new Date();
        calendar.setTime(now);

        long endTime = calendar.getTimeInMillis();
        calendar.add(Calendar.MINUTE, -15);
        long walkEndTime = calendar.getTimeInMillis();
        calendar.add(Calendar.MINUTE, -5);
        long walkStartTime = calendar.getTimeInMillis();
        calendar.add(Calendar.MINUTE, -15);
        long startTime = calendar.getTimeInMillis();

        float firstRunSpeed = 15;
        float walkSpeed = 5;
        float secondRunSpeed = 13;

        DataSource speedSegmentDataSource = new DataSource.Builder()
                .setAppPackageName(this.getPackageName())
                .setDataType(DataType.TYPE_SPEED)
                .setName("App speed dataset")
                .setType(DataSource.TYPE_RAW)
                .build();

        DataSource activitySegmentDataSource = new DataSource.Builder()
                .setAppPackageName(this.getPackageName())
                .setDataType(DataType.TYPE_ACTIVITY_SEGMENT)
                .setName("App activity segments dataset")
                .setType(DataSource.TYPE_RAW)
                .build();

        DataSet speedDataSet = DataSet.create(speedSegmentDataSource);
        DataSet activityDataSet = DataSet.create(activitySegmentDataSource);

        //Create speed data point for first run segment
        DataPoint firstRunSpeedDataPoint = speedDataSet.createDataPoint()
                .setTimeInterval(startTime, walkStartTime, TimeUnit.MILLISECONDS);
        firstRunSpeedDataPoint.getValue(Field.FIELD_SPEED).setFloat(firstRunSpeed);
        speedDataSet.add(firstRunSpeedDataPoint);

        //Create speed data point for walking segment
        DataPoint walkingSpeedDataPoint = speedDataSet.createDataPoint()
                .setTimeInterval(walkStartTime, walkEndTime, TimeUnit.MILLISECONDS);
        walkingSpeedDataPoint.getValue(Field.FIELD_SPEED).setFloat(walkSpeed);
        speedDataSet.add(walkingSpeedDataPoint);

        //Create speed data point for second run segment
        DataPoint secondRunSpeedDataPoint = speedDataSet.createDataPoint()
                .setTimeInterval(walkEndTime, endTime, TimeUnit.MILLISECONDS);
        secondRunSpeedDataPoint.getValue(Field.FIELD_SPEED).setFloat(secondRunSpeed);
        speedDataSet.add(secondRunSpeedDataPoint);

        //Create activity data point for first run segment
        DataPoint firstRunActivityDataPoint = activityDataSet.createDataPoint()
                .setTimeInterval(startTime, walkStartTime, TimeUnit.MILLISECONDS);
        firstRunActivityDataPoint.getValue(Field.FIELD_ACTIVITY).setActivity(FitnessActivities.RUNNING);
        activityDataSet.add(firstRunActivityDataPoint);

        //Create activity data point for walking segment
        DataPoint walkingActivityDataPoint = activityDataSet.createDataPoint()
                .setTimeInterval(walkStartTime, walkEndTime, TimeUnit.MILLISECONDS);
        walkingActivityDataPoint.getValue(Field.FIELD_ACTIVITY).setActivity(FitnessActivities.WALKING);
        activityDataSet.add(walkingActivityDataPoint);

        //Create activity data point for second run segment
        DataPoint secondRunActivityDataPoint = activityDataSet.createDataPoint()
                .setTimeInterval(walkEndTime, endTime, TimeUnit.MILLISECONDS);
        secondRunActivityDataPoint.getValue(Field.FIELD_ACTIVITY).setActivity(FitnessActivities.RUNNING);
        activityDataSet.add(secondRunActivityDataPoint);

        Session session = new Session.Builder()
                .setName(SESSION_NAME)
                .setIdentifier(getString(R.string.app_name) + " " + System.currentTimeMillis())
                .setDescription("Running in Segments")
                .setStartTime(startTime, TimeUnit.MILLISECONDS)
                .setEndTime(endTime, TimeUnit.MILLISECONDS)
                .setActivity(FitnessActivities.RUNNING)
                .build();

        SessionInsertRequest insertRequest = new SessionInsertRequest.Builder()
                .setSession(session)
                .addDataSet(speedDataSet)
                .addDataSet(activityDataSet)
                .build();

        PendingResult<Status> pendingResult =
                Fitness.SessionsApi.insertSession(mApiClient, insertRequest);

        pendingResult.setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                if( status.isSuccess() ) {
                    Log.i("Tuts+", "successfully inserted running session");
                } else {
                    Log.i("Tuts+", "Failed to insert running session: " + status.getStatusMessage());
                }
            }
        });

    }

    public void readSession() {
        if( !mApiClient.isConnected() ) {
            Toast.makeText(this, "Not connected to Google", Toast.LENGTH_SHORT).show();
            return;
        }

        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.MONTH, -1);
        long startTime = cal.getTimeInMillis();

        SessionReadRequest readRequest = new SessionReadRequest.Builder()
                .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
                .read(DataType.TYPE_SPEED)
                //.read(DataType.TYPE_STEP_COUNT_DELTA)
                //.read(DataType.TYPE_CALORIES_EXPENDED)
                //.read(DataType.TYPE_ACTIVITY_SAMPLES)
                //.read(DataType.TYPE_DISTANCE_DELTA)
                .setSessionName(SESSION_NAME)
                .build();

        PendingResult<SessionReadResult> sessionReadResult =
                Fitness.SessionsApi.readSession(mApiClient, readRequest);

        sessionReadResult.setResultCallback(new ResultCallback<SessionReadResult>() {
            @Override
            public void onResult(SessionReadResult sessionReadResult) {
                if (sessionReadResult.getStatus().isSuccess()) {
                    Log.i("App:", "Successfully read session data");
                    for (Session session : sessionReadResult.getSessions()) {
                        Log.i("App:", "Session name: " + session.getName());
                        for (DataSet dataSet : sessionReadResult.getDataSet(session)) {
                            for (DataPoint dataPoint : dataSet.getDataPoints()) {
                                Log.i("App:", "Speed: " + dataPoint.getValue(Field.FIELD_SPEED));
                                //Log.i("App:", "Steps: " + dataPoint.getValue(Field.FIELD_STEPS));
                                //Log.i("App:", "Distance: " + dataPoint.getValue(Field.FIELD_DISTANCE));
                                //Log.i("App:", "Calories: " + dataPoint.getValue(Field.FIELD_CALORIES));
                                //Log.i("App:", "Time: " + dataPoint.getValue(Field.FIELD_DURATION));
                            }
                        }
                    }
                } else {
                    Log.i("App:", "Failed to read session data");
                }
            }
        });
    }

    private void initViews() {

        //TextView textView = (TextView) findViewById(R.id.textView01);
        //textView.setText("Today's Steps: ");

        mCancelSubscriptionsBtn = (Button) findViewById(R.id.btn_cancel_subscriptions);
        mShowSubscriptionsBtn = (Button) findViewById(R.id.btn_show_subscriptions);
        mButtonViewWeek = (Button) findViewById(R.id.btn_view_week);
        mButtonViewWeekStepsGraph = (Button) findViewById(R.id.btn_view_week_steps_graph);
        mButtonViewWeekCalorie = (Button) findViewById(R.id.btn_view_week_calorie);
        mButtonViewWeekTime = (Button) findViewById(R.id.btn_view_week_time);
        mButtonViewToday = (Button) findViewById(R.id.btn_view_today);
        mButtonViewTodayTime = (Button) findViewById(R.id.btn_view_today_time);
        mButtonViewTodayCalorie = (Button) findViewById(R.id.btn_view_today_calorie);
        mButtonViewTodayDistance = (Button) findViewById(R.id.btn_view_today_distance);
//        mButtonAddSteps = (Button) findViewById(R.id.btn_add_steps);
//        mButtonUpdateSteps = (Button) findViewById(R.id.btn_update_steps);
        mButtonDeleteSteps = (Button) findViewById(R.id.btn_delete_steps);
        mStartSessionBtn = (Button) findViewById(R.id.btn_start_session);
        mStopSessionBtn = (Button) findViewById(R.id.btn_stop_session);
        mInsertSegmentBtn = (Button) findViewById(R.id.btn_insert_segment);
        mReadSessionBtn = (Button) findViewById(R.id.btn_read_session);
        mButtonViewWeekDistanceGraph = (Button) findViewById(R.id.btn_view_week_distance_graph);



        mButtonViewWeek.setOnClickListener(this);
        mButtonViewWeekStepsGraph.setOnClickListener(this);
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
        mStartSessionBtn.setOnClickListener(this);
        mStopSessionBtn.setOnClickListener(this);
        mInsertSegmentBtn.setOnClickListener(this);
        mReadSessionBtn.setOnClickListener(this);
        mButtonViewWeekDistanceGraph.setOnClickListener(this);







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

        Intent intent = new Intent( this, ActivityRecognizedService.class );
        PendingIntent pendingIntent = PendingIntent.getService( this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT );
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates( mApiClient, 3000, pendingIntent );

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
//        Fitness.RecordingApi.subscribe(mApiClient, DataType.TYPE_DISTANCE_DELTA)
//              .setResultCallback(mSubscribeResultCallback);
        Fitness.RecordingApi.subscribe(mApiClient, DataType.TYPE_ACTIVITY_SEGMENT)
                .setResultCallback(mSubscribeResultCallback);
        Fitness.RecordingApi.subscribe(mApiClient, DataType.TYPE_ACTIVITY_SAMPLES)
                .setResultCallback(mSubscribeResultCallback);
        Fitness.RecordingApi.subscribe(mApiClient, DataType.TYPE_CALORIES_EXPENDED)
                .setResultCallback(mSubscribeResultCallback);


        new FetchStepsAsync().execute();
        new ViewWeekStepCountGraphTask().execute();
        new ViewWeekDistanceCountGraphTask().execute();
        new ViewTodaysDistanceCountGraphTask().execute();
        //new ViewWeekDistanceCountGraphTask().execute();
        //new FetchTimeAsync().execute();
        //new FetchCalorieAsync().execute();


        //Fitness.HistoryApi.readDailyTotal(mApiClient, DataType.TYPE_STEP_COUNT_DELTA);

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
            case R.id.btn_view_week_steps_graph: {
                new ViewWeekStepCountGraphTask().execute();
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
            case R.id.btn_view_today_distance: {
                new ViewTodaysDistanceCountTask().execute();
                break;
            }
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
            case R.id.btn_start_session: {
                startSession();
                break;
            }
            case R.id.btn_stop_session: {
                stopSession();
                break;
            }
            case R.id.btn_insert_segment: {
                insertSegment();
                break;
            }
            case R.id.btn_read_session: {
                readSession();
                break;
            }
            case R.id.btn_view_week_distance_graph: {
                new ViewWeekDistanceCountGraphTask().execute();
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

    private class ViewWeekStepCountGraphTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            weekStepsGraph();
            return null;
        }
    }

    private class ViewWeekDistanceCountGraphTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            weekDistanceGraph();
            return null;
        }
    }

    private class ViewTodaysDistanceCountGraphTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            todayDistanceGraph();
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

    private class ViewTodaysDistanceCountTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            displayDistanceDataForToday();
            return null;
        }
    }



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


    private void weekStepsGraph()
    {
        stepsYesterday();
        stepsSecondDay();
        stepsThirdDay();
        stepsFourthDay();
        stepsFifthDay();
        stepsSixthDay();
        stepsSeventhDay();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                BarChart mBarChart = (BarChart) findViewById(R.id.barchart);
//
//
//                mBarChart.addBar(new BarModel(seventhDay, 0xFF123456));
//                mBarChart.addBar(new BarModel(sixthDay,  0xFF343456));
//                mBarChart.addBar(new BarModel(fifthDay, 0xFF563456));
//                mBarChart.addBar(new BarModel(fourthDay, 0xFF873F56));
//                mBarChart.addBar(new BarModel(thirdDay, 0xFF56B7F1));
//                mBarChart.addBar(new BarModel(secondDay,  0xFF343456));
//                mBarChart.addBar(new BarModel(yesterDay, 0xFF1FF4AC));
//
//                mBarChart.startAnimation();

                ValueLineChart mCubicValueLineChart = (ValueLineChart) findViewById(R.id.cubiclinechart);

                ValueLineSeries series = new ValueLineSeries();
                series.setColor(0xFF56B7F1);

                series.addPoint(new ValueLinePoint("", 0));
                series.addPoint(new ValueLinePoint("Day 7", seventhDay));
                series.addPoint(new ValueLinePoint("Day 6", sixthDay));
                series.addPoint(new ValueLinePoint("Day 5", fifthDay));
                series.addPoint(new ValueLinePoint("Day 4", fourthDay));
                series.addPoint(new ValueLinePoint("Day 3", thirdDay));
                series.addPoint(new ValueLinePoint("Day 2", secondDay));
                series.addPoint(new ValueLinePoint("Day 1", yesterDay));
                series.addPoint(new ValueLinePoint("", 0));

                mCubicValueLineChart.addSeries(series);
                mCubicValueLineChart.startAnimation();
            }
        });
    }

    private void stepsSeventhDay() {
        //1 week ago
        Calendar cal1 = Calendar.getInstance();
        Date now1 = new Date();
        cal1.setTime(now1);
        long endTime1 = cal1.getTimeInMillis();
        cal1.add(Calendar.DATE, -7);
        long startTime1 = cal1.getTimeInMillis();

        //RESETTING VARIABLES TO ZERO
        seventhDay = 0;


        java.text.DateFormat dateFormat = DateFormat.getDateInstance();
        Log.e("History", "Range Start: " + dateFormat.format(startTime1));
        Log.e("History", "Range End: " + dateFormat.format(endTime1));


        //Check how many steps were walked and recorded 7 days ago
        DataReadRequest readRequest1 = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime1, endTime1, TimeUnit.MILLISECONDS)
                .enableServerQueries()
                .build();


        DataReadResult dataReadResult1 = Fitness.HistoryApi.readData(mApiClient, readRequest1).await(1, TimeUnit.MINUTES);


        //THIS IS FOR SEVEN DAYS AGO
        if (dataReadResult1.getBuckets().size() > 0) {
            Log.e("History", "Number of buckets: " + dataReadResult1.getBuckets().size());
            for (Bucket bucket : dataReadResult1.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    //showDataSet(dataSet);
                    //DailyTotalResult result = Fitness.HistoryApi.readDailyTotal( mApiClient, DataType.TYPE_STEP_COUNT_DELTA ).await(1, TimeUnit.MINUTES);
                    //String adam = adamDp.getValue(adamField).toString();
                    //final int adamflood = Integer.parseInt(adam);
                    for (DataPoint dp : dataSet.getDataPoints()) {
                        for (Field field : dp.getDataType().getFields()) {
                            if (field.getName().equals("steps")) {
                                seventhDay += dp.getValue(field).asInt();
                                Log.d(TAG, "Seventh day: " + seventhDay);
                            }
                        }
                    }

                }
            }
        }


        //Used for non-aggregated data
        else if (dataReadResult1.getDataSets().size() > 0) {
            Log.e("History", "Number of returned DataSets: " + dataReadResult1.getDataSets().size());
            for (DataSet dataSet : dataReadResult1.getDataSets()) {
                showDataSet(dataSet);
            }
        }

        averageSteps = seventhDay / 7;
        seventhDay = seventhDay - sixthDay - fifthDay - fourthDay - thirdDay - secondDay - yesterDay;

    }

    private void stepsYesterday() {
        //YESTERDAY
        Calendar cal1 = Calendar.getInstance();
        Date now1 = new Date();
        cal1.setTime(now1);
        long endTime1 = cal1.getTimeInMillis();
        cal1.add(Calendar.DAY_OF_WEEK, -1);
        long startTime1 = cal1.getTimeInMillis();

        //RESETTING VARIABLES TO ZERO
        yesterDay = 0;


        java.text.DateFormat dateFormat = DateFormat.getDateInstance();
        Log.e("History", "Range Start: " + dateFormat.format(startTime1));
        Log.e("History", "Range End: " + dateFormat.format(endTime1));


        //Check how many steps were walked and recorded yesterday
        DataReadRequest readRequest1 = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime1, endTime1, TimeUnit.MILLISECONDS)
                .enableServerQueries()
                .build();


        DataReadResult dataReadResult1 = Fitness.HistoryApi.readData(mApiClient, readRequest1).await(1, TimeUnit.MINUTES);


        //THIS IS FOR YESTERDAY
        if (dataReadResult1.getBuckets().size() > 0) {
            Log.e("History", "Number of buckets: " + dataReadResult1.getBuckets().size());
            for (Bucket bucket : dataReadResult1.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    //showDataSet(dataSet);
                    //DailyTotalResult result = Fitness.HistoryApi.readDailyTotal( mApiClient, DataType.TYPE_STEP_COUNT_DELTA ).await(1, TimeUnit.MINUTES);
                    //String adam = adamDp.getValue(adamField).toString();
                    //final int adamflood = Integer.parseInt(adam);
                    for (DataPoint dp : dataSet.getDataPoints()) {
                        for (Field field : dp.getDataType().getFields()) {
                            if (field.getName().equals("steps")) {
                                yesterDay += dp.getValue(field).asInt();
                                Log.d(TAG, "Yesterday: " + yesterDay);
                            }
                        }
                    }

                }
            }
        }



        //Used for non-aggregated data
        else if (dataReadResult1.getDataSets().size() > 0) {
            Log.e("History", "Number of returned DataSets: " + dataReadResult1.getDataSets().size());
            for (DataSet dataSet : dataReadResult1.getDataSets()) {
                showDataSet(dataSet);
            }
        }

    }

    private void stepsSecondDay() {
        //2 DAYS AGO
        Calendar cal1 = Calendar.getInstance();
        Date now1 = new Date();
        cal1.setTime(now1);
        long endTime1 = cal1.getTimeInMillis();
        cal1.add(Calendar.DAY_OF_WEEK, -2);
        long startTime1 = cal1.getTimeInMillis();

        //RESETTING VARIABLES TO ZERO
        secondDay = 0;


        java.text.DateFormat dateFormat = DateFormat.getDateInstance();
        Log.e("History", "Range Start: " + dateFormat.format(startTime1));
        Log.e("History", "Range End: " + dateFormat.format(endTime1));


        //Check how many steps were walked and recorded 2 days ago
        DataReadRequest readRequest1 = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime1, endTime1, TimeUnit.MILLISECONDS)
                .enableServerQueries()
                .build();


        DataReadResult dataReadResult1 = Fitness.HistoryApi.readData(mApiClient, readRequest1).await(1, TimeUnit.MINUTES);


        //THIS IS FOR 2 DAYS AGO
        if (dataReadResult1.getBuckets().size() > 0) {
            Log.e("History", "Number of buckets: " + dataReadResult1.getBuckets().size());
            for (Bucket bucket : dataReadResult1.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    //showDataSet(dataSet);
                    //DailyTotalResult result = Fitness.HistoryApi.readDailyTotal( mApiClient, DataType.TYPE_STEP_COUNT_DELTA ).await(1, TimeUnit.MINUTES);
                    //String adam = adamDp.getValue(adamField).toString();
                    //final int adamflood = Integer.parseInt(adam);
                    for (DataPoint dp : dataSet.getDataPoints()) {
                        for (Field field : dp.getDataType().getFields()) {
                            if (field.getName().equals("steps")) {
                                secondDay += dp.getValue(field).asInt();
                                Log.d(TAG, "Second Day: " + secondDay);
                            }
                        }
                    }

                }
            }
        }



        //Used for non-aggregated data
        else if (dataReadResult1.getDataSets().size() > 0) {
            Log.e("History", "Number of returned DataSets: " + dataReadResult1.getDataSets().size());
            for (DataSet dataSet : dataReadResult1.getDataSets()) {
                showDataSet(dataSet);
            }
        }
        secondDay = secondDay - yesterDay;

    }

    private void stepsThirdDay() {
        //3 DAYS AGO
        Calendar cal1 = Calendar.getInstance();
        Date now1 = new Date();
        cal1.setTime(now1);
        long endTime1 = cal1.getTimeInMillis();
        cal1.add(Calendar.DAY_OF_WEEK, -3);
        long startTime1 = cal1.getTimeInMillis();

        //RESETTING VARIABLES TO ZERO
        thirdDay = 0;


        java.text.DateFormat dateFormat = DateFormat.getDateInstance();
        Log.e("History", "Range Start: " + dateFormat.format(startTime1));
        Log.e("History", "Range End: " + dateFormat.format(endTime1));


        //Check how many steps were walked and recorded 3 days ago
        DataReadRequest readRequest1 = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime1, endTime1, TimeUnit.MILLISECONDS)
                .enableServerQueries()
                .build();


        DataReadResult dataReadResult1 = Fitness.HistoryApi.readData(mApiClient, readRequest1).await(1, TimeUnit.MINUTES);


        //THIS IS FOR 3 DAYS AGO
        if (dataReadResult1.getBuckets().size() > 0) {
            Log.e("History", "Number of buckets: " + dataReadResult1.getBuckets().size());
            for (Bucket bucket : dataReadResult1.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    //showDataSet(dataSet);
                    //DailyTotalResult result = Fitness.HistoryApi.readDailyTotal( mApiClient, DataType.TYPE_STEP_COUNT_DELTA ).await(1, TimeUnit.MINUTES);
                    //String adam = adamDp.getValue(adamField).toString();
                    //final int adamflood = Integer.parseInt(adam);
                    for (DataPoint dp : dataSet.getDataPoints()) {
                        for (Field field : dp.getDataType().getFields()) {
                            if (field.getName().equals("steps")) {
                                thirdDay += dp.getValue(field).asInt();
                                Log.d(TAG, "Third Day: " + thirdDay);
                            }
                        }
                    }

                }
            }
        }



        //Used for non-aggregated data
        else if (dataReadResult1.getDataSets().size() > 0) {
            Log.e("History", "Number of returned DataSets: " + dataReadResult1.getDataSets().size());
            for (DataSet dataSet : dataReadResult1.getDataSets()) {
                showDataSet(dataSet);
            }
        }
        thirdDay = thirdDay - secondDay - yesterDay;

    }

    private void stepsFourthDay() {
        //4 DAYS AGO
        Calendar cal1 = Calendar.getInstance();
        Date now1 = new Date();
        cal1.setTime(now1);
        long endTime1 = cal1.getTimeInMillis();
        cal1.add(Calendar.DAY_OF_WEEK, -4);
        long startTime1 = cal1.getTimeInMillis();

        //RESETTING VARIABLES TO ZERO
        fourthDay = 0;


        java.text.DateFormat dateFormat = DateFormat.getDateInstance();
        Log.e("History", "Range Start: " + dateFormat.format(startTime1));
        Log.e("History", "Range End: " + dateFormat.format(endTime1));


        //Check how many steps were walked and recorded 4 days ago
        DataReadRequest readRequest1 = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime1, endTime1, TimeUnit.MILLISECONDS)
                .enableServerQueries()
                .build();


        DataReadResult dataReadResult1 = Fitness.HistoryApi.readData(mApiClient, readRequest1).await(1, TimeUnit.MINUTES);


        //THIS IS FOR 4 DAYS AGO
        if (dataReadResult1.getBuckets().size() > 0) {
            Log.e("History", "Number of buckets: " + dataReadResult1.getBuckets().size());
            for (Bucket bucket : dataReadResult1.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    //showDataSet(dataSet);
                    //DailyTotalResult result = Fitness.HistoryApi.readDailyTotal( mApiClient, DataType.TYPE_STEP_COUNT_DELTA ).await(1, TimeUnit.MINUTES);
                    //String adam = adamDp.getValue(adamField).toString();
                    //final int adamflood = Integer.parseInt(adam);
                    for (DataPoint dp : dataSet.getDataPoints()) {
                        for (Field field : dp.getDataType().getFields()) {
                            if (field.getName().equals("steps")) {
                                fourthDay += dp.getValue(field).asInt();
                                Log.d(TAG, "Fourth Day: " + fourthDay);
                            }
                        }
                    }

                }
            }
        }



        //Used for non-aggregated data
        else if (dataReadResult1.getDataSets().size() > 0) {
            Log.e("History", "Number of returned DataSets: " + dataReadResult1.getDataSets().size());
            for (DataSet dataSet : dataReadResult1.getDataSets()) {
                showDataSet(dataSet);
            }
        }
        fourthDay = fourthDay - thirdDay - secondDay - yesterDay;

    }

    private void stepsFifthDay() {
        //5 DAYS AGO
        Calendar cal1 = Calendar.getInstance();
        Date now1 = new Date();
        cal1.setTime(now1);
        long endTime1 = cal1.getTimeInMillis();
        cal1.add(Calendar.DAY_OF_WEEK, -5);
        long startTime1 = cal1.getTimeInMillis();

        //RESETTING VARIABLES TO ZERO
        fifthDay = 0;


        java.text.DateFormat dateFormat = DateFormat.getDateInstance();
        Log.e("History", "Range Start: " + dateFormat.format(startTime1));
        Log.e("History", "Range End: " + dateFormat.format(endTime1));


        //Check how many steps were walked and recorded 5 days ago
        DataReadRequest readRequest1 = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime1, endTime1, TimeUnit.MILLISECONDS)
                .enableServerQueries()
                .build();


        DataReadResult dataReadResult1 = Fitness.HistoryApi.readData(mApiClient, readRequest1).await(1, TimeUnit.MINUTES);


        //THIS IS FOR 5 DAYS AGO
        if (dataReadResult1.getBuckets().size() > 0) {
            Log.e("History", "Number of buckets: " + dataReadResult1.getBuckets().size());
            for (Bucket bucket : dataReadResult1.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    //showDataSet(dataSet);
                    //DailyTotalResult result = Fitness.HistoryApi.readDailyTotal( mApiClient, DataType.TYPE_STEP_COUNT_DELTA ).await(1, TimeUnit.MINUTES);
                    //String adam = adamDp.getValue(adamField).toString();
                    //final int adamflood = Integer.parseInt(adam);
                    for (DataPoint dp : dataSet.getDataPoints()) {
                        for (Field field : dp.getDataType().getFields()) {
                            if (field.getName().equals("steps")) {
                                fifthDay += dp.getValue(field).asInt();
                                Log.d(TAG, "Fifth Day: " + fifthDay);
                            }
                        }
                    }

                }
            }
        }



        //Used for non-aggregated data
        else if (dataReadResult1.getDataSets().size() > 0) {
            Log.e("History", "Number of returned DataSets: " + dataReadResult1.getDataSets().size());
            for (DataSet dataSet : dataReadResult1.getDataSets()) {
                showDataSet(dataSet);
            }
        }
        fifthDay = fifthDay - fourthDay - thirdDay - secondDay - yesterDay;

    }

    private void stepsSixthDay() {
        //6 DAYS AGO
        Calendar cal1 = Calendar.getInstance();
        Date now1 = new Date();
        cal1.setTime(now1);
        long endTime1 = cal1.getTimeInMillis();
        cal1.add(Calendar.DAY_OF_WEEK, -6);
        long startTime1 = cal1.getTimeInMillis();

        //RESETTING VARIABLES TO ZERO
        sixthDay = 0;


        java.text.DateFormat dateFormat = DateFormat.getDateInstance();
        Log.e("History", "Range Start: " + dateFormat.format(startTime1));
        Log.e("History", "Range End: " + dateFormat.format(endTime1));


        //Check how many steps were walked and recorded 6 days ago
        DataReadRequest readRequest1 = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime1, endTime1, TimeUnit.MILLISECONDS)
                .enableServerQueries()
                .build();


        DataReadResult dataReadResult1 = Fitness.HistoryApi.readData(mApiClient, readRequest1).await(1, TimeUnit.MINUTES);


        //THIS IS FOR 6 DAYS AGO
        if (dataReadResult1.getBuckets().size() > 0) {
            Log.e("History", "Number of buckets: " + dataReadResult1.getBuckets().size());
            for (Bucket bucket : dataReadResult1.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    //showDataSet(dataSet);
                    //DailyTotalResult result = Fitness.HistoryApi.readDailyTotal( mApiClient, DataType.TYPE_STEP_COUNT_DELTA ).await(1, TimeUnit.MINUTES);
                    //String adam = adamDp.getValue(adamField).toString();
                    //final int adamflood = Integer.parseInt(adam);
                    for (DataPoint dp : dataSet.getDataPoints()) {
                        for (Field field : dp.getDataType().getFields()) {
                            if (field.getName().equals("steps")) {
                                sixthDay += dp.getValue(field).asInt();
                                Log.d(TAG, "Sixth Day: " + sixthDay);
                            }
                        }
                    }

                }
            }
        }



        //Used for non-aggregated data
        else if (dataReadResult1.getDataSets().size() > 0) {
            Log.e("History", "Number of returned DataSets: " + dataReadResult1.getDataSets().size());
            for (DataSet dataSet : dataReadResult1.getDataSets()) {
                showDataSet(dataSet);
            }
        }
        sixthDay = sixthDay - fifthDay - fourthDay - thirdDay - secondDay - yesterDay;

    }

    private void weekDistanceGraph()
    {
        distanceYesterday();
        distanceSecondDay();
        distanceThirdDay();
        distanceFourthDay();
        distanceFifthDay();
        distanceSixthDay();
        distanceSeventhDay();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                BarChart mBarChart = (BarChart) findViewById(R.id.barchart);


                mBarChart.addBar(new BarModel(seventhDayDistance, 0xFF123456));
                mBarChart.addBar(new BarModel(sixthDayDistance,  0xFF343456));
                mBarChart.addBar(new BarModel(fifthDayDistance, 0xFF563456));
                mBarChart.addBar(new BarModel(fourthDayDistance, 0xFF873F56));
                mBarChart.addBar(new BarModel(thirdDayDistance, 0xFF56B7F1));
                mBarChart.addBar(new BarModel(secondDayDistance,  0xFF343456));
                mBarChart.addBar(new BarModel(yesterDayDistance, 0xFF1FF4AC));

                mBarChart.startAnimation();

            }
        });
    }

    private void todayDistanceGraph()
    {
        distanceToday();
        final String myText=Float.toString(distanceToday);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PieChart mPieChart = (PieChart) findViewById(R.id.piechart_distance_today);

                mPieChart.addPieSlice(new PieModel("Distance Today", distanceToday,Color.parseColor("#FE6DA8")));
                mPieChart.addPieSlice(new PieModel("Average Distance", averageDistance,Color.parseColor("#56B7F1")));
                //mPieChart.addPieSlice(new PieModel("Goal", 10000, Color.parseColor("#56B7F1")));

                mPieChart.startAnimation();

//                String adam = distanceToday.toString();
//                int adamVal = Integer.parseInt(adam);

                TextView textView = (TextView) findViewById(R.id.textView_distance);
                textView.setText(myText);

            }
        });
    }

    private void distanceSeventhDay() {
        //1 week ago
        Calendar cal1 = Calendar.getInstance();
        Date now1 = new Date();
        cal1.setTime(now1);
        long endTime1 = cal1.getTimeInMillis();
        cal1.add(Calendar.DATE, -7);
        long startTime1 = cal1.getTimeInMillis();

        //RESETTING VARIABLES TO ZERO
        seventhDayDistance = 0;


        java.text.DateFormat dateFormat = DateFormat.getDateInstance();
        Log.e("History", "Range Start: " + dateFormat.format(startTime1));
        Log.e("History", "Range End: " + dateFormat.format(endTime1));


        //Check how many distance were walked and recorded 7 days ago
        DataReadRequest readRequest1 = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_DISTANCE_DELTA, DataType.AGGREGATE_DISTANCE_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime1, endTime1, TimeUnit.MILLISECONDS)
                .enableServerQueries()
                .build();


        DataReadResult dataReadResult1 = Fitness.HistoryApi.readData(mApiClient, readRequest1).await(1, TimeUnit.MINUTES);


        //THIS IS FOR SEVEN DAYS AGO
        if (dataReadResult1.getBuckets().size() > 0) {
            Log.e("History", "Number of buckets: " + dataReadResult1.getBuckets().size());
            for (Bucket bucket : dataReadResult1.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    //showDataSet(dataSet);
                    //DailyTotalResult result = Fitness.HistoryApi.readDailyTotal( mApiClient, DataType.TYPE_DISTANCE_DELTA ).await(1, TimeUnit.MINUTES);
                    //String adam = adamDp.getValue(adamField).toString();
                    //final int adamflood = Integer.parseInt(adam);
                    for (DataPoint dp : dataSet.getDataPoints()) {
                        for (Field field : dp.getDataType().getFields()) {
                            if (field.getName().equals("distance")) {
                                seventhDayDistance += dp.getValue(field).asFloat();
                                Log.d(TAG, "Seventh day: " + seventhDayDistance);
                            }
                        }
                    }

                }
            }
        }


        //Used for non-aggregated data
        else if (dataReadResult1.getDataSets().size() > 0) {
            Log.e("History", "Number of returned DataSets: " + dataReadResult1.getDataSets().size());
            for (DataSet dataSet : dataReadResult1.getDataSets()) {
                showDataSet(dataSet);
            }
        }

        averageDistance = seventhDayDistance / 7;
        seventhDayDistance = seventhDayDistance - sixthDayDistance - fifthDayDistance - fourthDayDistance - thirdDayDistance - secondDayDistance - yesterDayDistance;

    }

    private void distanceYesterday() {
        //YESTERDAY
        Calendar cal1 = Calendar.getInstance();
        Date now1 = new Date();
        cal1.setTime(now1);
        long endTime1 = cal1.getTimeInMillis();
        cal1.add(Calendar.DAY_OF_WEEK, -1);
        long startTime1 = cal1.getTimeInMillis();

        //RESETTING VARIABLES TO ZERO
        yesterDayDistance = 0;


        java.text.DateFormat dateFormat = DateFormat.getDateInstance();
        Log.e("History", "Range Start: " + dateFormat.format(startTime1));
        Log.e("History", "Range End: " + dateFormat.format(endTime1));


        //Check how many distance were walked and recorded yesterday
        DataReadRequest readRequest1 = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_DISTANCE_DELTA, DataType.AGGREGATE_DISTANCE_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime1, endTime1, TimeUnit.MILLISECONDS)
                .enableServerQueries()
                .build();


        DataReadResult dataReadResult1 = Fitness.HistoryApi.readData(mApiClient, readRequest1).await(1, TimeUnit.MINUTES);


        //THIS IS FOR YESTERDAY
        if (dataReadResult1.getBuckets().size() > 0) {
            Log.e("History", "Number of buckets: " + dataReadResult1.getBuckets().size());
            for (Bucket bucket : dataReadResult1.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    //showDataSet(dataSet);
                    //DailyTotalResult result = Fitness.HistoryApi.readDailyTotal( mApiClient, DataType.TYPE_DISTANCE_DELTA ).await(1, TimeUnit.MINUTES);
                    //String adam = adamDp.getValue(adamField).toString();
                    //final int adamflood = Integer.parseInt(adam);
                    for (DataPoint dp : dataSet.getDataPoints()) {
                        for (Field field : dp.getDataType().getFields()) {
                            if (field.getName().equals("distance")) {
                                yesterDayDistance += dp.getValue(field).asFloat();
                                Log.d(TAG, "Yesterday: " + yesterDayDistance);
                            }
                        }
                    }

                }
            }
        }



        //Used for non-aggregated data
        else if (dataReadResult1.getDataSets().size() > 0) {
            Log.e("History", "Number of returned DataSets: " + dataReadResult1.getDataSets().size());
            for (DataSet dataSet : dataReadResult1.getDataSets()) {
                showDataSet(dataSet);
            }
        }

    }

    private void distanceSecondDay() {
        //2 DAYS AGO
        Calendar cal1 = Calendar.getInstance();
        Date now1 = new Date();
        cal1.setTime(now1);
        long endTime1 = cal1.getTimeInMillis();
        cal1.add(Calendar.DAY_OF_WEEK, -2);
        long startTime1 = cal1.getTimeInMillis();

        //RESETTING VARIABLES TO ZERO
        secondDayDistance = 0;


        java.text.DateFormat dateFormat = DateFormat.getDateInstance();
        Log.e("History", "Range Start: " + dateFormat.format(startTime1));
        Log.e("History", "Range End: " + dateFormat.format(endTime1));


        //Check how many distance were walked and recorded 2 days ago
        DataReadRequest readRequest1 = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_DISTANCE_DELTA, DataType.AGGREGATE_DISTANCE_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime1, endTime1, TimeUnit.MILLISECONDS)
                .enableServerQueries()
                .build();


        DataReadResult dataReadResult1 = Fitness.HistoryApi.readData(mApiClient, readRequest1).await(1, TimeUnit.MINUTES);


        //THIS IS FOR 2 DAYS AGO
        if (dataReadResult1.getBuckets().size() > 0) {
            Log.e("History", "Number of buckets: " + dataReadResult1.getBuckets().size());
            for (Bucket bucket : dataReadResult1.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    //showDataSet(dataSet);
                    //DailyTotalResult result = Fitness.HistoryApi.readDailyTotal( mApiClient, DataType.TYPE_DISTANCE_DELTA ).await(1, TimeUnit.MINUTES);
                    //String adam = adamDp.getValue(adamField).toString();
                    //final int adamflood = Integer.parseInt(adam);
                    for (DataPoint dp : dataSet.getDataPoints()) {
                        for (Field field : dp.getDataType().getFields()) {
                            if (field.getName().equals("distance")) {
                                secondDayDistance += dp.getValue(field).asFloat();
                                Log.d(TAG, "Second Day: " + secondDayDistance);
                            }
                        }
                    }

                }
            }
        }



        //Used for non-aggregated data
        else if (dataReadResult1.getDataSets().size() > 0) {
            Log.e("History", "Number of returned DataSets: " + dataReadResult1.getDataSets().size());
            for (DataSet dataSet : dataReadResult1.getDataSets()) {
                showDataSet(dataSet);
            }
        }
        secondDayDistance = secondDayDistance - yesterDayDistance;

    }

    private void distanceThirdDay() {
        //3 DAYS AGO
        Calendar cal1 = Calendar.getInstance();
        Date now1 = new Date();
        cal1.setTime(now1);
        long endTime1 = cal1.getTimeInMillis();
        cal1.add(Calendar.DAY_OF_WEEK, -3);
        long startTime1 = cal1.getTimeInMillis();

        //RESETTING VARIABLES TO ZERO
        thirdDayDistance = 0;


        java.text.DateFormat dateFormat = DateFormat.getDateInstance();
        Log.e("History", "Range Start: " + dateFormat.format(startTime1));
        Log.e("History", "Range End: " + dateFormat.format(endTime1));


        //Check how many distance were walked and recorded 3 days ago
        DataReadRequest readRequest1 = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_DISTANCE_DELTA, DataType.AGGREGATE_DISTANCE_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime1, endTime1, TimeUnit.MILLISECONDS)
                .enableServerQueries()
                .build();


        DataReadResult dataReadResult1 = Fitness.HistoryApi.readData(mApiClient, readRequest1).await(1, TimeUnit.MINUTES);


        //THIS IS FOR 3 DAYS AGO
        if (dataReadResult1.getBuckets().size() > 0) {
            Log.e("History", "Number of buckets: " + dataReadResult1.getBuckets().size());
            for (Bucket bucket : dataReadResult1.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    //showDataSet(dataSet);
                    //DailyTotalResult result = Fitness.HistoryApi.readDailyTotal( mApiClient, DataType.TYPE_DISTANCE_DELTA ).await(1, TimeUnit.MINUTES);
                    //String adam = adamDp.getValue(adamField).toString();
                    //final int adamflood = Integer.parseInt(adam);
                    for (DataPoint dp : dataSet.getDataPoints()) {
                        for (Field field : dp.getDataType().getFields()) {
                            if (field.getName().equals("distance")) {
                                thirdDayDistance += dp.getValue(field).asFloat();
                                Log.d(TAG, "Third Day: " + thirdDayDistance);
                            }
                        }
                    }

                }
            }
        }



        //Used for non-aggregated data
        else if (dataReadResult1.getDataSets().size() > 0) {
            Log.e("History", "Number of returned DataSets: " + dataReadResult1.getDataSets().size());
            for (DataSet dataSet : dataReadResult1.getDataSets()) {
                showDataSet(dataSet);
            }
        }
        thirdDayDistance = thirdDayDistance - secondDayDistance - yesterDayDistance;

    }


    private void distanceFourthDay() {
        //4 DAYS AGO
        Calendar cal1 = Calendar.getInstance();
        Date now1 = new Date();
        cal1.setTime(now1);
        long endTime1 = cal1.getTimeInMillis();
        cal1.add(Calendar.DAY_OF_WEEK, -4);
        long startTime1 = cal1.getTimeInMillis();

        //RESETTING VARIABLES TO ZERO
        fourthDayDistance = 0;


        java.text.DateFormat dateFormat = DateFormat.getDateInstance();
        Log.e("History", "Range Start: " + dateFormat.format(startTime1));
        Log.e("History", "Range End: " + dateFormat.format(endTime1));


        //Check how many distance were walked and recorded 4 days ago
        DataReadRequest readRequest1 = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_DISTANCE_DELTA, DataType.AGGREGATE_DISTANCE_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime1, endTime1, TimeUnit.MILLISECONDS)
                .enableServerQueries()
                .build();


        DataReadResult dataReadResult1 = Fitness.HistoryApi.readData(mApiClient, readRequest1).await(1, TimeUnit.MINUTES);


        //THIS IS FOR 4 DAYS AGO
        if (dataReadResult1.getBuckets().size() > 0) {
            Log.e("History", "Number of buckets: " + dataReadResult1.getBuckets().size());
            for (Bucket bucket : dataReadResult1.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    //showDataSet(dataSet);
                    //DailyTotalResult result = Fitness.HistoryApi.readDailyTotal( mApiClient, DataType.TYPE_DISTANCE_DELTA ).await(1, TimeUnit.MINUTES);
                    //String adam = adamDp.getValue(adamField).toString();
                    //final int adamflood = Integer.parseInt(adam);
                    for (DataPoint dp : dataSet.getDataPoints()) {
                        for (Field field : dp.getDataType().getFields()) {
                            if (field.getName().equals("distance")) {
                                fourthDayDistance += dp.getValue(field).asFloat();
                                Log.d(TAG, "Fourth Day: " + fourthDayDistance);
                            }
                        }
                    }

                }
            }
        }



        //Used for non-aggregated data
        else if (dataReadResult1.getDataSets().size() > 0) {
            Log.e("History", "Number of returned DataSets: " + dataReadResult1.getDataSets().size());
            for (DataSet dataSet : dataReadResult1.getDataSets()) {
                showDataSet(dataSet);
            }
        }
        fourthDayDistance = fourthDayDistance - thirdDayDistance - secondDayDistance - yesterDayDistance;

    }

    private void distanceFifthDay() {
        //5 DAYS AGO
        Calendar cal1 = Calendar.getInstance();
        Date now1 = new Date();
        cal1.setTime(now1);
        long endTime1 = cal1.getTimeInMillis();
        cal1.add(Calendar.DAY_OF_WEEK, -5);
        long startTime1 = cal1.getTimeInMillis();

        //RESETTING VARIABLES TO ZERO
        fifthDayDistance = 0;


        java.text.DateFormat dateFormat = DateFormat.getDateInstance();
        Log.e("History", "Range Start: " + dateFormat.format(startTime1));
        Log.e("History", "Range End: " + dateFormat.format(endTime1));


        //Check how many distance were walked and recorded 5 days ago
        DataReadRequest readRequest1 = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_DISTANCE_DELTA, DataType.AGGREGATE_DISTANCE_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime1, endTime1, TimeUnit.MILLISECONDS)
                .enableServerQueries()
                .build();


        DataReadResult dataReadResult1 = Fitness.HistoryApi.readData(mApiClient, readRequest1).await(1, TimeUnit.MINUTES);


        //THIS IS FOR 5 DAYS AGO
        if (dataReadResult1.getBuckets().size() > 0) {
            Log.e("History", "Number of buckets: " + dataReadResult1.getBuckets().size());
            for (Bucket bucket : dataReadResult1.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    //showDataSet(dataSet);
                    //DailyTotalResult result = Fitness.HistoryApi.readDailyTotal( mApiClient, DataType.TYPE_DISTANCE_DELTA ).await(1, TimeUnit.MINUTES);
                    //String adam = adamDp.getValue(adamField).toString();
                    //final int adamflood = Integer.parseInt(adam);
                    for (DataPoint dp : dataSet.getDataPoints()) {
                        for (Field field : dp.getDataType().getFields()) {
                            if (field.getName().equals("distance")) {
                                fifthDayDistance += dp.getValue(field).asFloat();
                                Log.d(TAG, "Fifth Day: " + fifthDayDistance);
                            }
                        }
                    }

                }
            }
        }



        //Used for non-aggregated data
        else if (dataReadResult1.getDataSets().size() > 0) {
            Log.e("History", "Number of returned DataSets: " + dataReadResult1.getDataSets().size());
            for (DataSet dataSet : dataReadResult1.getDataSets()) {
                showDataSet(dataSet);
            }
        }
        fifthDayDistance = fifthDayDistance - fourthDayDistance - thirdDayDistance - secondDayDistance - yesterDayDistance;

    }

    private void distanceSixthDay() {
        //6 DAYS AGO
        Calendar cal1 = Calendar.getInstance();
        Date now1 = new Date();
        cal1.setTime(now1);
        long endTime1 = cal1.getTimeInMillis();
        cal1.add(Calendar.DAY_OF_WEEK, -6);
        long startTime1 = cal1.getTimeInMillis();

        //RESETTING VARIABLES TO ZERO
        sixthDayDistance = 0;


        java.text.DateFormat dateFormat = DateFormat.getDateInstance();
        Log.e("History", "Range Start: " + dateFormat.format(startTime1));
        Log.e("History", "Range End: " + dateFormat.format(endTime1));


        //Check how many distance were walked and recorded 6 days ago
        DataReadRequest readRequest1 = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_DISTANCE_DELTA, DataType.AGGREGATE_DISTANCE_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime1, endTime1, TimeUnit.MILLISECONDS)
                .enableServerQueries()
                .build();


        DataReadResult dataReadResult1 = Fitness.HistoryApi.readData(mApiClient, readRequest1).await(1, TimeUnit.MINUTES);


        //THIS IS FOR 6 DAYS AGO
        if (dataReadResult1.getBuckets().size() > 0) {
            Log.e("History", "Number of buckets: " + dataReadResult1.getBuckets().size());
            for (Bucket bucket : dataReadResult1.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    //showDataSet(dataSet);
                    //DailyTotalResult result = Fitness.HistoryApi.readDailyTotal( mApiClient, DataType.TYPE_DISTANCE_DELTA ).await(1, TimeUnit.MINUTES);
                    //String adam = adamDp.getValue(adamField).toString();
                    //final int adamflood = Integer.parseInt(adam);
                    for (DataPoint dp : dataSet.getDataPoints()) {
                        for (Field field : dp.getDataType().getFields()) {
                            if (field.getName().equals("distance")) {
                                sixthDayDistance += dp.getValue(field).asFloat();
                                Log.d(TAG, "Sixth Day: " + sixthDayDistance);
                            }
                        }
                    }

                }
            }
        }



        //Used for non-aggregated data
        else if (dataReadResult1.getDataSets().size() > 0) {
            Log.e("History", "Number of returned DataSets: " + dataReadResult1.getDataSets().size());
            for (DataSet dataSet : dataReadResult1.getDataSets()) {
                showDataSet(dataSet);
            }
        }
        sixthDayDistance = sixthDayDistance - fifthDayDistance - fourthDayDistance - thirdDayDistance - secondDayDistance - yesterDayDistance;

    }

    private void displayLastWeeksData() {
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.WEEK_OF_YEAR, -1);
        long startTime = cal.getTimeInMillis();

        //YESTERDAY
//        Calendar cal1 = Calendar.getInstance();
//        Date now1 = new Date();
//        cal1.setTime(now1);
//        long endTime1 = cal1.getTimeInMillis();
//        cal1.add(Calendar.DAY_OF_WEEK, -1);
//        long startTime1 = cal1.getTimeInMillis();

        //RESETTING VARIABLES TO ZERO
//        sixthDay = 0;
//        seventhDay = 0;

        java.text.DateFormat dateFormat = DateFormat.getDateInstance();
        Log.e("History", "Range Start: " + dateFormat.format(startTime));
        Log.e("History", "Range End: " + dateFormat.format(endTime));

        //Check how many steps were walked and recorded in the last 7 days
        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .enableServerQueries()
                .build();

        DataReadResult dataReadResult = Fitness.HistoryApi.readData(mApiClient, readRequest).await(1, TimeUnit.MINUTES);

//        Check how many steps were walked and recorded yesterday
//        DataReadRequest readRequest1 = new DataReadRequest.Builder()
//                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
//                .bucketByTime(1, TimeUnit.DAYS)
//                .setTimeRange(startTime1, endTime1, TimeUnit.MILLISECONDS)
//                .build();
//
//
//        DataReadResult dataReadResult1 = Fitness.HistoryApi.readData(mApiClient, readRequest1).await(1, TimeUnit.MINUTES);

        //Used for aggregated data
        if (dataReadResult.getBuckets().size() > 0) {
            Log.e("History", "Number of buckets: " + dataReadResult.getBuckets().size());
            for (Bucket bucket : dataReadResult.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    showDataSet(dataSet);
                    //DailyTotalResult result = Fitness.HistoryApi.readDailyTotal( mApiClient, DataType.TYPE_STEP_COUNT_DELTA ).await(1, TimeUnit.MINUTES);
                    //String adam = adamDp.getValue(adamField).toString();
                    //final int adamflood = Integer.parseInt(adam);
//                    for (DataPoint dp : dataSet.getDataPoints()) {
//                        for (Field field : dp.getDataType().getFields()) {
//                            if (field.getName().equals("steps")) {
//                                seventhDay += dp.getValue(field).asInt();
//                                Log.d(TAG, "seventhday: " + seventhDay);
//                            }
//                        }
//                    }

                }
            }
        }

        //THIS IS FOR YESTERDAY
//        if (dataReadResult1.getBuckets().size() > 0) {
//            Log.e("History", "Number of buckets: " + dataReadResult1.getBuckets().size());
//            for (Bucket bucket : dataReadResult1.getBuckets()) {
//                List<DataSet> dataSets = bucket.getDataSets();
//                for (DataSet dataSet : dataSets) {
//                    //showDataSet(dataSet);
//                    //DailyTotalResult result = Fitness.HistoryApi.readDailyTotal( mApiClient, DataType.TYPE_STEP_COUNT_DELTA ).await(1, TimeUnit.MINUTES);
//                    //String adam = adamDp.getValue(adamField).toString();
//                    //final int adamflood = Integer.parseInt(adam);
//                    for (DataPoint dp : dataSet.getDataPoints()) {
//                        for (Field field : dp.getDataType().getFields()) {
//                            if (field.getName().equals("steps")) {
//                                sixthDay += dp.getValue(field).asInt();
//                                Log.d(TAG, "SixthDay: " + sixthDay);
//                            }
//                        }
//                    }
//
//                }
//            }
//        }

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
                //String adam = adamDp.getValue(adamField).toString();
                //final int adamflood = Integer.parseInt(adam);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        BarChart mBarChart = (BarChart) findViewById(R.id.barchart);
//
//                        mBarChart.addBar(new BarModel(seventhDay, 0xFF123456));
//                        mBarChart.addBar(new BarModel(sixthDay,  0xFF343456));
//                        mBarChart.addBar(new BarModel(fifthDay, 0xFF563456));
//                        mBarChart.addBar(new BarModel(fourthDay, 0xFF873F56));
//                        mBarChart.addBar(new BarModel(thirdDay, 0xFF56B7F1));
//                        mBarChart.addBar(new BarModel(secondDay,  0xFF343456));
//                        mBarChart.addBar(new BarModel(yesterDay, 0xFF1FF4AC));
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

    private void distanceToday()
    {
        //today
        //RESETTING VARIABLES TO ZERO
        distanceToday = 0;

        PendingResult<DailyTotalResult> result1 = Fitness.HistoryApi.readDailyTotal(mApiClient, DataType.TYPE_DISTANCE_DELTA);
        DailyTotalResult totalResult = result1.await(30, TimeUnit.SECONDS);
        if (totalResult.getStatus().isSuccess()) {
            DataSet totalSet = totalResult.getTotal();
            distanceToday = totalSet.isEmpty()
                    ? 0
                    : totalSet.getDataPoints().get(0).getValue(Field.FIELD_DISTANCE).asFloat();
        } else {
            Log.w(TAG, "There was a problem getting the distance count.");
        }


        Log.i(TAG, "Total distance today: " + distanceToday);

    }

    private void displayDistanceDataForToday() {
        DailyTotalResult result = Fitness.HistoryApi.readDailyTotal( mApiClient, DataType.TYPE_DISTANCE_DELTA ).await(1, TimeUnit.MINUTES);
        showDataSet(result.getTotal());
    }


    private DataSet createDataForRequest(DataType dataType, int dataSourceType, Object values,
                                         long startTime, long endTime, TimeUnit timeUnit) {
        DataSource dataSource = new DataSource.Builder()
                .setAppPackageName(this)
                .setDataType(dataType)
                .setType(dataSourceType)
                .build();

        DataSet dataSet = DataSet.create(dataSource);
        DataPoint dataPoint = dataSet.createDataPoint().setTimeInterval(startTime, endTime, timeUnit);

        if (values instanceof Integer) {
            dataPoint = dataPoint.setIntValues((Integer)values);
        } else {
            dataPoint = dataPoint.setFloatValues((Float)values);
        }

        dataSet.add(dataPoint);

        return dataSet;
    }

    private void addWeightDataToGoogleFit() {
        //Adds weight spread out evenly from start time to end time
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.HOUR_OF_DAY, -1);
        long startTime = cal.getTimeInMillis();

        float weight = 81;
        DataSet weightDataSet = createDataForRequest(
                DataType.TYPE_WEIGHT,    // for height, it would be DataType.TYPE_HEIGHT
                DataSource.TYPE_RAW,
                weight,                  // weight in kgs
                startTime,              // start time
                endTime,                // end time
                TimeUnit.MILLISECONDS                // Time Unit, for example, TimeUnit.MILLISECONDS
        );

        com.google.android.gms.common.api.Status weightInsertStatus =
                Fitness.HistoryApi.insertData(mApiClient, weightDataSet)
                        .await(1, TimeUnit.MINUTES);

    }

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
//            PieChart mPieChart = (PieChart) findViewById(R.id.piechart);
//
//            mPieChart.addPieSlice(new PieModel("Steps Today", adamVal,Color.parseColor("#FE6DA8")));
//            //mPieChart.addPieSlice(new PieModel("Goal", 10000, Color.parseColor("#56B7F1")));
//
//            mPieChart.startAnimation();

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



