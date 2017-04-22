package com.example.adamf.myfitnessapplicationgoogleapi;

import android.Manifest;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.content.Intent;
import android.content.IntentSender;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;


import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.request.DataUpdateRequest;
import com.google.android.gms.fitness.request.SessionInsertRequest;
import com.google.android.gms.fitness.request.SessionReadRequest;
import com.google.android.gms.fitness.result.DailyTotalResult;
import com.google.android.gms.fitness.result.SessionReadResult;
import com.google.android.gms.fitness.result.SessionStopResult;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
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
import com.google.android.gms.fitness.*;


import org.eazegraph.lib.charts.BarChart;
import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.charts.ValueLineChart;
import org.eazegraph.lib.models.BarModel;
import org.eazegraph.lib.models.PieModel;
import org.eazegraph.lib.models.ValueLinePoint;
import org.eazegraph.lib.models.ValueLineSeries;


import static com.google.android.gms.fitness.data.Field.FIELD_STEPS;
import static java.util.concurrent.TimeUnit.SECONDS;

public class MainActivity extends AppCompatActivity implements OnDataPointListener,
GoogleApiClient.ConnectionCallbacks,
GoogleApiClient.OnConnectionFailedListener{

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 0;

    //Below buttons work when uncommented with their respective methods

//    private Button mButtonAddSteps;
//    private Button mButtonUpdateSteps;
//    private Button mButtonDeleteSteps;
//    private Button mCancelSubscriptionsBtn;
//    private Button mShowSubscriptionsBtn;
//    private Button mStartSessionBtn;
//    private Button mStopSessionBtn;
//    private Button mInsertSegmentBtn;
//    private Button mReadSessionBtn;



    //for creating a callback service with data
    private ResultCallback<Status> mSubscribeResultCallback;

    //Google O auth authentication
    private static final int REQUEST_OAUTH = 1;
    private static final String AUTH_PENDING = "auth_state_pending";
    private boolean authInProgress = false;
    private GoogleApiClient mApiClient;

    //Tag for debugging
    String TAG = "YOUR-TAG-NAME";


    //step variables for creating graphs
    private int seventhDay = 0;
    private int sixthDay = 0;
    private int fifthDay = 0;
    private int fourthDay = 0;
    private int thirdDay = 0;
    private int secondDay = 0;
    private int yesterDay = 0;
    private float averageSteps = 0;
    private int lifetimeTotalSteps = 0;

    //time variables for creating graphs
    private int seventhDayTime = 0;
    private int sixthDayTime = 0;
    private int fifthDayTime = 0;
    private int fourthDayTime = 0;
    private int thirdDayTime = 0;
    private int secondDayTime = 0;
    private int yesterDayTime = 0;
    private float averageTime = 0;
    private int lifetimeTotalTime = 0;

    //distance variables for creating graphs
    private float seventhDayDistance = 0;
    private float sixthDayDistance = 0;
    private float fifthDayDistance = 0;
    private float fourthDayDistance = 0;
    private float thirdDayDistance = 0;
    private float secondDayDistance = 0;
    private float yesterDayDistance = 0;
    private float averageDistance = 0;
    private float lifetimeTotalDistance = 0;

    //calories variables for creating graphs
    private float seventhDayCalories = 0;
    private float sixthDayCalories = 0;
    private float fifthDayCalories = 0;
    private float fourthDayCalories = 0;
    private float thirdDayCalories = 0;
    private float secondDayCalories = 0;
    private float yesterDayCalories = 0;
    private float averageCalories = 0;
    private float lifetimeTotalCalories = 0;

    //today values for each datatype displayed in the application
    private float distanceToday = 0;
    private int timeToday = 0;
    private float caloriesToday = 0;
    private int stepsToday = 0;


    //confidence variables to recognise activities
    public static int walkingTime = 0;
    public static int stillTime = 0;
    public static int runningTime = 0;
    public static int vehicleTime = 0;

    //unfinished development for working with sessions API
    private final String SESSION_NAME = "session name";
    private Session mSession;

    //when the application is ran the oncreate method is called
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            authInProgress = savedInstanceState.getBoolean(AUTH_PENDING);
        }

        initCallbacks();

        //building the users data and requesting permissions
        //invoking each api to be used in the application
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
                .build();

        //self permission check method to check for access fine location permission to be able to work with distance
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);


        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Explanation if access denied
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                //in this case the user needs to accept permission so an explanation is not needed

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

            }
        }

        //if the permission is granted
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            //Execute location service call if user has explicitly granted ACCESS_FINE_LOCATION..
            //we can now start recording the distance datatype
            Fitness.RecordingApi.subscribe(mApiClient, DataType.TYPE_DISTANCE_DELTA)
                    .setResultCallback(mSubscribeResultCallback);
        }
    }

    //class to recognise the users activity
    public static class ActivityRecognizedService extends IntentService {

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
        //each different activity detected in the form of cases
        private void handleDetectedActivities(List<DetectedActivity> probableActivities) {
            for( DetectedActivity activity : probableActivities ) {
                switch( activity.getType() ) {
                    case DetectedActivity.IN_VEHICLE: {
                        //when the detected activity is driving from the api we enter this method
                        Log.e( "ActivityRecognition", "In Vehicle: " + activity.getConfidence() );
                        if( activity.getConfidence() >= 75 ) {
                            //if the api is confident that this is the users activity it is given a
                            //value of over 75. we can then show a push notification to the user
                            //showing them their current activity along with a custom vehicle icon
                            NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                            builder.setContentText( "Are you Driving?" );
                            builder.setSmallIcon( R.drawable.driving_icon);
                            builder.setContentTitle( getString( R.string.app_name ) );
                            NotificationManagerCompat.from(this).notify(0, builder.build());
                            vehicleTime = activity.getConfidence();
                        }
                        break;
                    }
                    case DetectedActivity.ON_BICYCLE: {
                        Log.e( "ActivityRecognition", "On Bicycle: " + activity.getConfidence() );
                        if( activity.getConfidence() >= 75 ) {
                            //if the api is confident that this is the users activity it is given a
                            //value of over 75. we can then show a push notification to the user
                            //showing them their current activity along with a custom cycling icon
                            NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                            builder.setContentText( "Are you Cycling?" );
                            builder.setSmallIcon( R.drawable.cycling_icon );
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
                        runningTime = activity.getConfidence();
                        if( activity.getConfidence() >= 75 ) {
                            //if the api is confident that this is the users activity it is given a
                            //value of over 75. we can then show a push notification to the user
                            //showing them their current activity along with a custom running icon
                            NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                            builder.setContentText( "Are you Running?" );
                            builder.setSmallIcon( R.drawable.running_icon );
                            builder.setContentTitle( getString( R.string.app_name ) );
                            NotificationManagerCompat.from(this).notify(0, builder.build());
                        }
                        break;
                    }
                    case DetectedActivity.STILL: {
                        Log.e( "ActivityRecognition", "Still: " + activity.getConfidence() );
                        stillTime = activity.getConfidence();
                        if( activity.getConfidence() >= 75 ) {
                            //if the api is confident that this is the users activity it is given a
                            //value of over 75. we can then show a push notification to the user
                            //showing them their current activity along with a custom standing icon
                            NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                            builder.setContentText( "Are you standing still?");
                            builder.setSmallIcon( R.drawable.standing_still_icon );
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
                        walkingTime = activity.getConfidence();
                        if( activity.getConfidence() >= 75 ) {
                            //if the api is confident that this is the users activity it is given a
                            //value of over 75. we can then show a push notification to the user
                            //showing them their current activity along with a custom walking icon
                            NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                            builder.setContentText( "Are you walking?" );
                            builder.setSmallIcon( R.drawable.walking_icon );
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


    //unfinished developement
    //user can start and stop a session and statistics will be displayed
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

    //when the user stops a session the sessions api method is invoked
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

    //inserting a session into the sessions api
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
                    Log.i("App: ", "successfully inserted running session");
                } else {
                    Log.i("App: +", "Failed to insert running session: " + status.getStatusMessage());
                }
            }
        });

    }

    //invoking the sessions api to display statistics about the sessions that were previously saved
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

//        mCancelSubscriptionResultCallback = new ResultCallback<Status>() {
//            @Override
//            public void onResult(@NonNull Status status) {
//                if (status.isSuccess()) {
//                    Log.e( "RecordingAPI", "Canceled subscriptions!");
//                } else {
//                    // Subscription not removed
//                    Log.e("RecordingAPI", "Failed to cancel subscriptions");
//                }
//            }
//        };
//
//        mListSubscriptionsResultCallback = new ResultCallback<ListSubscriptionsResult>() {
//            @Override
//            public void onResult(@NonNull ListSubscriptionsResult listSubscriptionsResult) {
//                for (Subscription subscription : listSubscriptionsResult.getSubscriptions()) {
//                    DataType dataType = subscription.getDataType();
//                    Log.e( "RecordingAPI", dataType.getName() );
//                    for (Field field : dataType.getFields() ) {
//                        Log.e( "RecordingAPI", field.toString() );
//                    }
//                }
//            }
//        };
    }

    //when the app is started the users client is connected
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


    //save the users authentication
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(AUTH_PENDING, authInProgress);
    }

    //registering the sensors api to detect movement
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



    //everytime the application is connected eg unlocked, app opened, phone tilted the onconnected method is invoked
    @Override
    public void onConnected(Bundle bundle) {

        //creating an intent to get the current activity
        //and set the activity to be the current activity
        Intent intent = new Intent( this, ActivityRecognizedService.class );
        PendingIntent pendingIntent = PendingIntent.getService( this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT );
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates( mApiClient, 3000, pendingIntent );

        //creating datasource requests for each datatype
    DataSourcesRequest dataSourceRequest = new DataSourcesRequest.Builder()
                .setDataTypes(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                .setDataSourceTypes(DataSource.TYPE_RAW)
                .build();

        DataSourcesRequest dataSourceRequest1 = new DataSourcesRequest.Builder()
                .setDataTypes(DataType.TYPE_ACTIVITY_SEGMENT)
                .setDataSourceTypes(DataSource.TYPE_RAW)
                .build();

        new AddWeight().execute();
        new AddHeight().execute();

        DataSourcesRequest dataSourceRequest2 = new DataSourcesRequest.Builder()
                .setDataTypes(DataType.TYPE_CALORIES_EXPENDED)
                .setDataSourceTypes(DataSource.TYPE_RAW)
                .build();




        //subscribe to each different datatype we wish to record
        //distance is not placed here as we need the users permissions first
        Fitness.RecordingApi.subscribe(mApiClient, DataType.TYPE_STEP_COUNT_DELTA)
                .setResultCallback(mSubscribeResultCallback);
        Fitness.RecordingApi.subscribe(mApiClient, DataType.TYPE_ACTIVITY_SEGMENT)
                .setResultCallback(mSubscribeResultCallback);
        Fitness.RecordingApi.subscribe(mApiClient, DataType.TYPE_ACTIVITY_SAMPLES)
                .setResultCallback(mSubscribeResultCallback);
        Fitness.RecordingApi.subscribe(mApiClient, DataType.TYPE_CALORIES_EXPENDED)
                .setResultCallback(mSubscribeResultCallback);


        //beginnning of asynchronous tasks
        //each task retrieves data from the cloud and runs concurrently with each other to minimise wait time
        new FetchStepsAsync().execute();
        new ViewLifetimeTotalCountGraphTask().execute();
        new ViewWeekStepCountGraphTask().execute();
        new ViewWeekDistanceCountGraphTask().execute();
        new ViewWeekCaloriesCountGraphTask().execute();
        new ViewWeekTimeCountGraphTask().execute();
        new ViewTodaysDistanceCountGraphTask().execute();
        new ViewTodaysTimeCountGraphTask().execute();
        new ViewTodaysCalorieCountGraphTask().execute();
        new ViewTodaysStepCountGraphTask().execute();


        //creating a result callback with a constant count (only needed for steps and calories)
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

        //assigning sensors apis to detect movement
        Fitness.SensorsApi.findDataSources(mApiClient, dataSourceRequest)
                .setResultCallback(dataSourcesResultCallback);

        Fitness.SensorsApi.findDataSources(mApiClient, dataSourceRequest1)
                .setResultCallback(dataSourcesResultCallback);

        Fitness.SensorsApi.findDataSources(mApiClient, dataSourceRequest2)
                .setResultCallback(dataSourcesResultCallback1);

    }


//uncomment methods to see how they work
//    private void showSubscriptions() {
//        Fitness.RecordingApi.listSubscriptions(mApiClient)
//                .setResultCallback(mListSubscriptionsResultCallback);
//    }

//    private void cancelSubscriptions() {
//        Fitness.RecordingApi.unsubscribe(mApiClient, DataType.TYPE_STEP_COUNT_DELTA)
//                .setResultCallback(mCancelSubscriptionResultCallback);
//    }

    //when connection is lost display a reason why
    @Override
    public void onConnectionSuspended(int i) {
        Log.e("HistoryAPI", "onConnectionSuspended");

    }

    //when the authentication failed for the google fit api
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

    //when the user approves the application and connects
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_OAUTH) {
            authInProgress = false;
            //auth in progress is no longer true as the user is no approved
            if (resultCode == RESULT_OK) {
                if (!mApiClient.isConnecting() && !mApiClient.isConnected()) {
                    mApiClient.connect();
                    //connect the user
                }
            } else if (resultCode == RESULT_CANCELED) {
                Log.e("GoogleFit", "RESULT_CANCELED");
            }
        } else {
            Log.e("GoogleFit", "requestCode NOT request_oauth");
        }
    }

    //upon detected movement assign the value to the value variable
    @Override
    public void onDataPoint(DataPoint dataPoint) {
        for (final Field field : dataPoint.getDataType().getFields()) {
            final Value value = dataPoint.getValue(field);
        }
    }


    //async class to add default weight
    private class AddWeight extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            addWeightDataToGoogleFit();
            return null;
        }
    }

    //async class to add default height
    private class AddHeight extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            addHeightDataToGoogleFit();
            return null;
        }
    }


    //async class to view the weeks steps graph
    //calls another method that performs the graph build
    private class ViewWeekStepCountGraphTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            weekStepsGraph();
            return null;
        }
    }

    //async class to view the distance steps graph
    //calls another method that performs the graph build
    private class ViewWeekDistanceCountGraphTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            weekDistanceGraph();
            return null;
        }
    }

    //async class to view the weeks calories graph
    //calls another method that performs the graph build
    private class ViewWeekCaloriesCountGraphTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            weekCaloriesGraph();
            return null;
        }
    }

    //async class to view the weeks time graph
    //calls another method that performs the graph build
    private class ViewWeekTimeCountGraphTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            weekTimeGraph();
            return null;
        }
    }

    //async class to view todays distance graph
    //calls another method that performs the graph build
    private class ViewTodaysDistanceCountGraphTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            todayDistanceGraph();
            return null;
        }
    }

    //async class to view todays steps graph
    //calls another method that performs the graph build
    private class ViewTodaysStepCountGraphTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            todayStepsGraph();
            return null;
        }
    }

    //async class to view todays time graph
    //calls another method that performs the graph build
    private class ViewTodaysTimeCountGraphTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            todayTimeGraph();
            return null;
        }
    }

    //async class to view todays calories graph
    //calls another method that performs the graph build
    private class ViewTodaysCalorieCountGraphTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            todayCaloriesGraph();
            return null;
        }
    }

    //async class to view lifetime total statistics
    //calls another method that performs the graph build
    private class ViewLifetimeTotalCountGraphTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            lifetimeTotalGraph();
            return null;
        }
    }



    //to view these functions uncomment them
    //allows the user to add update and delete steps
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

//    private class DeleteYesterdaysStepsTask extends AsyncTask<Void, Void, Void> {
//        protected Void doInBackground(Void... params) {
//            deleteStepDataOnGoogleFit();
//            displayLastWeeksData();
//            return null;
//        }
//    }

    //graph of this weeks time spent active
    private void weekTimeGraph()
    {
        //to retrieve the last 7 days data, the data is returned in "buckets", unfortunately you can not retrieve data from a specific day
        //So the work around I implemented was to create mutliple methods for each of the seven days.
        //timeSeventhDay retrieves the value of time for the last 7 days combined. So I needed to then subtract each other day from it
        //so that the only value i was left with was the actual value for the seventh day and not the other 6 days.
        //the same was done for each other day by subtracting the previous days
        timeYesterDay();
        timeSecondDay();
        timeThirdDay();
        timeFourthDay();
        timeFifthDay();
        timeSixthDay();
        timeSeventhDay();

        //run on ui thread method that creates the graph of this weeks time

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //graph is created using the EazeGraph library
                ValueLineChart mCubicValueLineChart = (ValueLineChart) findViewById(R.id.cubiclinechart_week_time);

                //chart is cleared before adding data to it
                mCubicValueLineChart.clearChart();

                ValueLineSeries series = new ValueLineSeries();
                series.setColor(0xff171515);

                series.addPoint(new ValueLinePoint("", 0));
                series.addPoint(new ValueLinePoint("Day 7", seventhDayTime));
                series.addPoint(new ValueLinePoint("Day 6", sixthDayTime));
                series.addPoint(new ValueLinePoint("Day 5", fifthDayTime));
                series.addPoint(new ValueLinePoint("Day 4", fourthDayTime));
                series.addPoint(new ValueLinePoint("Day 3", thirdDayTime));
                series.addPoint(new ValueLinePoint("Day 2", secondDayTime));
                series.addPoint(new ValueLinePoint("Day 1", yesterDayTime));
                series.addPoint(new ValueLinePoint("", 0));

                mCubicValueLineChart.addSeries(series);
                mCubicValueLineChart.startAnimation();

            }
        });
    }

    //retrieves data from installation for time
    private void timeLifetimeTotal() {

        Calendar cal1 = Calendar.getInstance();
        Date now1 = new Date();
        cal1.setTime(now1);
        long endTime1 = cal1.getTimeInMillis();
        cal1.add(Calendar.YEAR, -1);
        long startTime1 = cal1.getTimeInMillis();

        //RESETTING VARIABLES TO ZERO
        lifetimeTotalTime = 0;


        java.text.DateFormat dateFormat = DateFormat.getDateInstance();
        Log.e("History", "Range Start: " + dateFormat.format(startTime1));
        Log.e("History", "Range End: " + dateFormat.format(endTime1));


        //Check how much time was spent active and recorded since installation
        DataReadRequest readRequest1 = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_ACTIVITY_SEGMENT, DataType.AGGREGATE_ACTIVITY_SUMMARY)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime1, endTime1, TimeUnit.MILLISECONDS)
                .enableServerQueries()
                .build();


        DataReadResult dataReadResult1 = Fitness.HistoryApi.readData(mApiClient, readRequest1).await(1, TimeUnit.MINUTES);

        if (dataReadResult1.getBuckets().size() > 0) {
            Log.e("History", "Number of buckets: " + dataReadResult1.getBuckets().size());
            for (Bucket bucket : dataReadResult1.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    for (DataPoint dp : dataSet.getDataPoints()) {
                        for (Field field : dp.getDataType().getFields()) {
                            //for every field that is called duration
                            if (field.getName().equals("duration")) {
                                //add the value to the lifetimetotal variable
                                lifetimeTotalTime += dp.getValue(field).asInt();
                                Log.d(TAG, "Lifetime Total Time: " + lifetimeTotalTime);
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


    private void timeSeventhDay() {
        //1 week ago
        Calendar cal1 = Calendar.getInstance();
        Date now1 = new Date();
        cal1.setTime(now1);
        long endTime1 = cal1.getTimeInMillis();
        cal1.add(Calendar.DATE, -7);
        long startTime1 = cal1.getTimeInMillis();

        //RESETTING VARIABLES TO ZERO
        seventhDayTime = 0;


        java.text.DateFormat dateFormat = DateFormat.getDateInstance();
        Log.e("History", "Range Start: " + dateFormat.format(startTime1));
        Log.e("History", "Range End: " + dateFormat.format(endTime1));


        //Check how much time was spent active and recorded 7 days ago
        DataReadRequest readRequest1 = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_ACTIVITY_SEGMENT, DataType.AGGREGATE_ACTIVITY_SUMMARY)
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
                    for (DataPoint dp : dataSet.getDataPoints()) {
                        for (Field field : dp.getDataType().getFields()) {
                            if (field.getName().equals("duration")) {
                                seventhDayTime += dp.getValue(field).asInt();
                                Log.d(TAG, "Seventh day Time : " + seventhDayTime);
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

        //getting the average time for the week
        averageTime = seventhDayTime / 7;
        //formatting the time and converting from milliseconds to minutes
        averageTime = (averageTime / 1000) / 60;

        //formatting time from milliseconds to minutes
        seventhDayTime = (seventhDayTime / (1000 * 60));
        //subtracting each day from seventh day to leave only the value on the seventh day
        seventhDayTime = seventhDayTime - sixthDayTime - fifthDayTime - fourthDayTime - thirdDayTime - secondDayTime - yesterDayTime;

    }

    private void timeSixthDay() {
        //1 week ago
        Calendar cal1 = Calendar.getInstance();
        Date now1 = new Date();
        cal1.setTime(now1);
        long endTime1 = cal1.getTimeInMillis();
        cal1.add(Calendar.DAY_OF_WEEK, -6);
        long startTime1 = cal1.getTimeInMillis();

        //RESETTING VARIABLES TO ZERO
        sixthDayTime = 0;


        java.text.DateFormat dateFormat = DateFormat.getDateInstance();
        Log.e("History", "Range Start: " + dateFormat.format(startTime1));
        Log.e("History", "Range End: " + dateFormat.format(endTime1));


        //Check how much time was spent active and recorded 6 days ago
        DataReadRequest readRequest1 = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_ACTIVITY_SEGMENT, DataType.AGGREGATE_ACTIVITY_SUMMARY)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime1, endTime1, TimeUnit.MILLISECONDS)
                .enableServerQueries()
                .build();


        DataReadResult dataReadResult1 = Fitness.HistoryApi.readData(mApiClient, readRequest1).await(1, TimeUnit.MINUTES);


        //THIS IS FOR SIX DAYS AGO
        if (dataReadResult1.getBuckets().size() > 0) {
            Log.e("History", "Number of buckets: " + dataReadResult1.getBuckets().size());
            for (Bucket bucket : dataReadResult1.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    for (DataPoint dp : dataSet.getDataPoints()) {
                        for (Field field : dp.getDataType().getFields()) {
                            if (field.getName().equals("duration")) {
                                sixthDayTime += dp.getValue(field).asInt();
                                Log.d(TAG, "Sixth day time: " + sixthDayTime);
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

        sixthDayTime = (sixthDayTime / (1000 * 60));
        //subtracting each day from sixth day to leave only the value on the sixth day
        sixthDayTime = sixthDayTime - fifthDayTime - fourthDayTime - thirdDayTime - secondDayTime - yesterDayTime;

    }

    private void timeFifthDay() {
        //5 days ago
        Calendar cal1 = Calendar.getInstance();
        Date now1 = new Date();
        cal1.setTime(now1);
        long endTime1 = cal1.getTimeInMillis();
        cal1.add(Calendar.DAY_OF_WEEK, -5);
        long startTime1 = cal1.getTimeInMillis();

        //RESETTING VARIABLES TO ZERO
        fifthDayTime = 0;


        java.text.DateFormat dateFormat = DateFormat.getDateInstance();
        Log.e("History", "Range Start: " + dateFormat.format(startTime1));
        Log.e("History", "Range End: " + dateFormat.format(endTime1));


        //Check how much time was spent active and recorded 5 days ago
        DataReadRequest readRequest1 = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_ACTIVITY_SEGMENT, DataType.AGGREGATE_ACTIVITY_SUMMARY)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime1, endTime1, TimeUnit.MILLISECONDS)
                .enableServerQueries()
                .build();


        DataReadResult dataReadResult1 = Fitness.HistoryApi.readData(mApiClient, readRequest1).await(1, TimeUnit.MINUTES);


        //THIS IS FOR FIVE DAYS AGO
        if (dataReadResult1.getBuckets().size() > 0) {
            Log.e("History", "Number of buckets: " + dataReadResult1.getBuckets().size());
            for (Bucket bucket : dataReadResult1.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    for (DataPoint dp : dataSet.getDataPoints()) {
                        for (Field field : dp.getDataType().getFields()) {
                            if (field.getName().equals("duration")) {
                                fifthDayTime += dp.getValue(field).asInt();
                                Log.d(TAG, "Fifth day: " + fifthDayTime);
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

        fifthDayTime = (fifthDayTime / (1000 * 60));
        //subtracting each day from fifth day to leave only the value on the fifth day
        fifthDayTime = fifthDayTime - fourthDayTime - thirdDayTime - secondDayTime - yesterDayTime;

    }

    private void timeFourthDay() {
        //4 days ago
        Calendar cal1 = Calendar.getInstance();
        Date now1 = new Date();
        cal1.setTime(now1);
        long endTime1 = cal1.getTimeInMillis();
        cal1.add(Calendar.DAY_OF_WEEK, -4);
        long startTime1 = cal1.getTimeInMillis();

        //RESETTING VARIABLES TO ZERO
        fourthDayTime = 0;


        java.text.DateFormat dateFormat = DateFormat.getDateInstance();
        Log.e("History", "Range Start: " + dateFormat.format(startTime1));
        Log.e("History", "Range End: " + dateFormat.format(endTime1));


        //Check how much time was spent active and recorded 4 days ago
        DataReadRequest readRequest1 = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_ACTIVITY_SEGMENT, DataType.AGGREGATE_ACTIVITY_SUMMARY)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime1, endTime1, TimeUnit.MILLISECONDS)
                .enableServerQueries()
                .build();


        DataReadResult dataReadResult1 = Fitness.HistoryApi.readData(mApiClient, readRequest1).await(1, TimeUnit.MINUTES);


        //THIS IS FOR FOUR DAYS AGO
        if (dataReadResult1.getBuckets().size() > 0) {
            Log.e("History", "Number of buckets: " + dataReadResult1.getBuckets().size());
            for (Bucket bucket : dataReadResult1.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    for (DataPoint dp : dataSet.getDataPoints()) {
                        for (Field field : dp.getDataType().getFields()) {
                            if (field.getName().equals("duration")) {
                                fourthDayTime += dp.getValue(field).asInt();
                                Log.d(TAG, "Fourth day: " + fourthDayTime);
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

        fourthDayTime = (fourthDayTime / (1000 * 60));
        //subtracting each day from fourth day to leave only the value on the fourth day
        fourthDayTime = fourthDayTime - thirdDayTime - secondDayTime - yesterDayTime;

    }

    private void timeThirdDay() {
        //3 days ago
        Calendar cal1 = Calendar.getInstance();
        Date now1 = new Date();
        cal1.setTime(now1);
        long endTime1 = cal1.getTimeInMillis();
        cal1.add(Calendar.DAY_OF_WEEK, -3);
        long startTime1 = cal1.getTimeInMillis();

        //RESETTING VARIABLES TO ZERO
        thirdDayTime = 0;


        java.text.DateFormat dateFormat = DateFormat.getDateInstance();
        Log.e("History", "Range Start: " + dateFormat.format(startTime1));
        Log.e("History", "Range End: " + dateFormat.format(endTime1));


        //Check how much time was spent active and recorded 3 days ago
        DataReadRequest readRequest1 = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_ACTIVITY_SEGMENT, DataType.AGGREGATE_ACTIVITY_SUMMARY)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime1, endTime1, TimeUnit.MILLISECONDS)
                .enableServerQueries()
                .build();


        DataReadResult dataReadResult1 = Fitness.HistoryApi.readData(mApiClient, readRequest1).await(1, TimeUnit.MINUTES);


        //THIS IS FOR THREE DAYS AGO
        if (dataReadResult1.getBuckets().size() > 0) {
            Log.e("History", "Number of buckets: " + dataReadResult1.getBuckets().size());
            for (Bucket bucket : dataReadResult1.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    for (DataPoint dp : dataSet.getDataPoints()) {
                        for (Field field : dp.getDataType().getFields()) {
                            if (field.getName().equals("duration")) {
                                thirdDayTime += dp.getValue(field).asInt();
                                Log.d(TAG, "Third day: " + thirdDayTime);
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

        thirdDayTime = (thirdDayTime / (1000 * 60));
        //subtracting each day from third day to leave only the value on the third day
        thirdDayTime = thirdDayTime - secondDayTime - yesterDayTime;

    }

    private void timeSecondDay() {
        //2 days ago
        Calendar cal1 = Calendar.getInstance();
        Date now1 = new Date();
        cal1.setTime(now1);
        long endTime1 = cal1.getTimeInMillis();
        cal1.add(Calendar.DAY_OF_WEEK, -2);
        long startTime1 = cal1.getTimeInMillis();

        //RESETTING VARIABLES TO ZERO
        secondDayTime = 0;


        java.text.DateFormat dateFormat = DateFormat.getDateInstance();
        Log.e("History", "Range Start: " + dateFormat.format(startTime1));
        Log.e("History", "Range End: " + dateFormat.format(endTime1));


        //Check how much time was spent active and recorded 2 days ago
        DataReadRequest readRequest1 = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_ACTIVITY_SEGMENT, DataType.AGGREGATE_ACTIVITY_SUMMARY)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime1, endTime1, TimeUnit.MILLISECONDS)
                .enableServerQueries()
                .build();


        DataReadResult dataReadResult1 = Fitness.HistoryApi.readData(mApiClient, readRequest1).await(1, TimeUnit.MINUTES);


        //THIS IS FOR TWO DAYS AGO
        if (dataReadResult1.getBuckets().size() > 0) {
            Log.e("History", "Number of buckets: " + dataReadResult1.getBuckets().size());
            for (Bucket bucket : dataReadResult1.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    for (DataPoint dp : dataSet.getDataPoints()) {
                        for (Field field : dp.getDataType().getFields()) {
                            if (field.getName().equals("duration")) {
                                secondDayTime += dp.getValue(field).asInt();
                                Log.d(TAG, "Second day: " + secondDayTime);
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

        secondDayTime = (secondDayTime / (1000 * 60));
        //subtracting each day from second day to leave only the value on the second day
        secondDayTime = secondDayTime - yesterDayTime;

    }

    private void timeYesterDay() {
        //2 days ago
        Calendar cal1 = Calendar.getInstance();
        Date now1 = new Date();
        cal1.setTime(now1);
        long endTime1 = cal1.getTimeInMillis();
        cal1.add(Calendar.DAY_OF_WEEK, -1);
        long startTime1 = cal1.getTimeInMillis();

        //RESETTING VARIABLES TO ZERO
        yesterDayTime = 0;


        java.text.DateFormat dateFormat = DateFormat.getDateInstance();
        Log.e("History", "Range Start: " + dateFormat.format(startTime1));
        Log.e("History", "Range End: " + dateFormat.format(endTime1));


        //Check how much time was spent active and recorded yesterday
        DataReadRequest readRequest1 = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_ACTIVITY_SEGMENT, DataType.AGGREGATE_ACTIVITY_SUMMARY)
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
                    for (DataPoint dp : dataSet.getDataPoints()) {
                        for (Field field : dp.getDataType().getFields()) {
                            if (field.getName().equals("duration")) {
                                yesterDayTime += dp.getValue(field).asInt();
                                Log.d(TAG, "Yesterday: " + yesterDayTime);
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

        yesterDayTime = (yesterDayTime / (1000 * 60));
        //no need for subtraction

    }

    //The same concept is applied as before with the week time graph
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

                ValueLineChart mCubicValueLineChart = (ValueLineChart) findViewById(R.id.cubiclinechart);

                mCubicValueLineChart.clearChart();

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

    //lifetime total steps
    private void stepsLifetimeTotal() {
        Calendar cal1 = Calendar.getInstance();
        Date now1 = new Date();
        cal1.setTime(now1);
        long endTime1 = cal1.getTimeInMillis();
        cal1.add(Calendar.YEAR, -1);
        long startTime1 = cal1.getTimeInMillis();

        //RESETTING VARIABLES TO ZERO
        lifetimeTotalSteps = 0;


        java.text.DateFormat dateFormat = DateFormat.getDateInstance();
        Log.e("History", "Range Start: " + dateFormat.format(startTime1));
        Log.e("History", "Range End: " + dateFormat.format(endTime1));


        //Check how many steps were walked and recorded since installation
        DataReadRequest readRequest1 = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime1, endTime1, TimeUnit.MILLISECONDS)
                .enableServerQueries()
                .build();


        DataReadResult dataReadResult1 = Fitness.HistoryApi.readData(mApiClient, readRequest1).await(1, TimeUnit.MINUTES);


        //THIS IS FOR ALL TIME
        if (dataReadResult1.getBuckets().size() > 0) {
            Log.e("History", "Number of buckets: " + dataReadResult1.getBuckets().size());
            for (Bucket bucket : dataReadResult1.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    for (DataPoint dp : dataSet.getDataPoints()) {
                        for (Field field : dp.getDataType().getFields()) {
                            if (field.getName().equals("steps")) {
                                lifetimeTotalSteps += dp.getValue(field).asInt();
                                Log.d(TAG, "Lifetime Total Steps: " + lifetimeTotalSteps);
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

    //same concept is applied as before the weeks values
    private void weekCaloriesGraph()
    {
        caloriesYesterDay();
        caloriesSecondDay();
        caloriesThirdDay();
        caloriesFourthDay();
        caloriesFifthDay();
        caloriesSixthDay();
        caloriesSeventhDay();

        //formatting each calories variable correct to one decimal place
        final DecimalFormat df = new DecimalFormat("#.");
        final String formattedSeventhDay = df.format(seventhDayCalories);
        final float seventhDayRounded = Float.parseFloat(formattedSeventhDay);

        final DecimalFormat df1 = new DecimalFormat("#.");
        final String formattedSixthDay = df1.format(sixthDayCalories);
        final float sixthDayRounded = Float.parseFloat(formattedSixthDay);

        final DecimalFormat df2 = new DecimalFormat("#.");
        final String formattedFifthDay = df2.format(fifthDayCalories);
        final float fifthDayRounded = Float.parseFloat(formattedFifthDay);

        final DecimalFormat df3 = new DecimalFormat("#.");
        final String formattedFourthDay = df3.format(fourthDayCalories);
        final float fourthDayRounded = Float.parseFloat(formattedFourthDay);

        final DecimalFormat df4 = new DecimalFormat("#.");
        final String formattedThirdDay = df4.format(thirdDayCalories);
        final float thirdDayRounded = Float.parseFloat(formattedThirdDay);

        final DecimalFormat df5 = new DecimalFormat("#.");
        final String formattedSecondDay = df5.format(secondDayCalories);
        final float secondDayRounded = Float.parseFloat(formattedSecondDay);

        final DecimalFormat df6 = new DecimalFormat("#.");
        final String formattedYesterDay = df6.format(yesterDayCalories);
        final float yesterDayRounded = Float.parseFloat(formattedYesterDay);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                ValueLineChart mValueLineChart = (ValueLineChart) findViewById(R.id.linechart_calories);

                mValueLineChart.clearChart();

                ValueLineSeries series = new ValueLineSeries();
                series.setColor(0xFF63CBB0);

                series.addPoint(new ValueLinePoint(0));
                series.addPoint(new ValueLinePoint("Day 7", seventhDayRounded));
                series.addPoint(new ValueLinePoint("Day 6", sixthDayRounded));
                series.addPoint(new ValueLinePoint("Day 5", fifthDayRounded));
                series.addPoint(new ValueLinePoint("Day 4",fourthDayRounded));
                series.addPoint(new ValueLinePoint("Day 3",thirdDayRounded));
                series.addPoint(new ValueLinePoint("Day 2",secondDayRounded));
                series.addPoint(new ValueLinePoint("Day 1",yesterDayRounded));
                series.addPoint(new ValueLinePoint(0));

                mValueLineChart.addSeries(series);
            }
        });
    }

    private void caloriesSeventhDay() {
        //1 week ago
        Calendar cal1 = Calendar.getInstance();
        Date now1 = new Date();
        cal1.setTime(now1);
        long endTime1 = cal1.getTimeInMillis();
        cal1.add(Calendar.DATE, -7);
        long startTime1 = cal1.getTimeInMillis();

        //RESETTING VARIABLES TO ZERO
        seventhDayCalories = 0;


        java.text.DateFormat dateFormat = DateFormat.getDateInstance();
        Log.e("History", "Range Start: " + dateFormat.format(startTime1));
        Log.e("History", "Range End: " + dateFormat.format(endTime1));


        //Check how many calories were burned and recorded 7 days ago
        DataReadRequest readRequest1 = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_CALORIES_EXPENDED, DataType.AGGREGATE_CALORIES_EXPENDED)
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
                    for (DataPoint dp : dataSet.getDataPoints()) {
                        for (Field field : dp.getDataType().getFields()) {
                            if (field.getName().equals("calories")) {
                                seventhDayCalories += dp.getValue(field).asFloat();
                                Log.d(TAG, "Seventh day: " + seventhDayCalories);
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

        averageCalories = seventhDayCalories / 7;
        seventhDayCalories = seventhDayCalories - sixthDayCalories - fifthDayCalories - fourthDayCalories - thirdDayCalories - secondDayCalories - yesterDayCalories;

    }

    private void caloriesLifetimeTotal() {
        //1 week ago
        Calendar cal1 = Calendar.getInstance();
        Date now1 = new Date();
        cal1.setTime(now1);
        long endTime1 = cal1.getTimeInMillis();
        cal1.add(Calendar.YEAR, -1);
        long startTime1 = cal1.getTimeInMillis();

        //RESETTING VARIABLES TO ZERO
        lifetimeTotalCalories = 0;


        java.text.DateFormat dateFormat = DateFormat.getDateInstance();
        Log.e("History", "Range Start: " + dateFormat.format(startTime1));
        Log.e("History", "Range End: " + dateFormat.format(endTime1));


        //Check how many calories were burned and recorded 7 days ago
        DataReadRequest readRequest1 = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_CALORIES_EXPENDED, DataType.AGGREGATE_CALORIES_EXPENDED)
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
                    for (DataPoint dp : dataSet.getDataPoints()) {
                        for (Field field : dp.getDataType().getFields()) {
                            if (field.getName().equals("calories")) {
                                lifetimeTotalCalories += dp.getValue(field).asFloat();
                                Log.d(TAG, "Seventh day: " + lifetimeTotalCalories);
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

    private void caloriesSixthDay() {
        //1 week ago
        Calendar cal1 = Calendar.getInstance();
        Date now1 = new Date();
        cal1.setTime(now1);
        long endTime1 = cal1.getTimeInMillis();
        cal1.add(Calendar.DAY_OF_WEEK, -6);
        long startTime1 = cal1.getTimeInMillis();

        //RESETTING VARIABLES TO ZERO
        sixthDayCalories = 0;


        java.text.DateFormat dateFormat = DateFormat.getDateInstance();
        Log.e("History", "Range Start: " + dateFormat.format(startTime1));
        Log.e("History", "Range End: " + dateFormat.format(endTime1));


        //Check how many calories were burned and recorded 6 days ago
        DataReadRequest readRequest1 = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_CALORIES_EXPENDED, DataType.AGGREGATE_CALORIES_EXPENDED)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime1, endTime1, TimeUnit.MILLISECONDS)
                .enableServerQueries()
                .build();


        DataReadResult dataReadResult1 = Fitness.HistoryApi.readData(mApiClient, readRequest1).await(1, TimeUnit.MINUTES);


        //THIS IS FOR SIX DAYS AGO
        if (dataReadResult1.getBuckets().size() > 0) {
            Log.e("History", "Number of buckets: " + dataReadResult1.getBuckets().size());
            for (Bucket bucket : dataReadResult1.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    for (DataPoint dp : dataSet.getDataPoints()) {
                        for (Field field : dp.getDataType().getFields()) {
                            if (field.getName().equals("calories")) {
                                sixthDayCalories += dp.getValue(field).asFloat();
                                Log.d(TAG, "Sixth day: " + sixthDayCalories);
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

        sixthDayCalories = sixthDayCalories - fifthDayCalories - fourthDayCalories - thirdDayCalories - secondDayCalories - yesterDayCalories;

    }


    private void caloriesFifthDay() {
        //1 week ago
        Calendar cal1 = Calendar.getInstance();
        Date now1 = new Date();
        cal1.setTime(now1);
        long endTime1 = cal1.getTimeInMillis();
        cal1.add(Calendar.DAY_OF_WEEK, -5);
        long startTime1 = cal1.getTimeInMillis();

        //RESETTING VARIABLES TO ZERO
        fifthDayCalories = 0;


        java.text.DateFormat dateFormat = DateFormat.getDateInstance();
        Log.e("History", "Range Start: " + dateFormat.format(startTime1));
        Log.e("History", "Range End: " + dateFormat.format(endTime1));


        //Check how many calories were burned and recorded 5 days ago
        DataReadRequest readRequest1 = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_CALORIES_EXPENDED, DataType.AGGREGATE_CALORIES_EXPENDED)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime1, endTime1, TimeUnit.MILLISECONDS)
                .enableServerQueries()
                .build();


        DataReadResult dataReadResult1 = Fitness.HistoryApi.readData(mApiClient, readRequest1).await(1, TimeUnit.MINUTES);


        //THIS IS FOR FIVE DAYS AGO
        if (dataReadResult1.getBuckets().size() > 0) {
            Log.e("History", "Number of buckets: " + dataReadResult1.getBuckets().size());
            for (Bucket bucket : dataReadResult1.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    for (DataPoint dp : dataSet.getDataPoints()) {
                        for (Field field : dp.getDataType().getFields()) {
                            if (field.getName().equals("calories")) {
                                fifthDayCalories += dp.getValue(field).asFloat();
                                Log.d(TAG, "Fifth day: " + fifthDayCalories);
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

        fifthDayCalories = fifthDayCalories - fourthDayCalories - thirdDayCalories - secondDayCalories - yesterDayCalories;

    }


    private void caloriesFourthDay() {
        //4 days ago
        Calendar cal1 = Calendar.getInstance();
        Date now1 = new Date();
        cal1.setTime(now1);
        long endTime1 = cal1.getTimeInMillis();
        cal1.add(Calendar.DAY_OF_WEEK, -4);
        long startTime1 = cal1.getTimeInMillis();

        //RESETTING VARIABLES TO ZERO
        fourthDayCalories = 0;


        java.text.DateFormat dateFormat = DateFormat.getDateInstance();
        Log.e("History", "Range Start: " + dateFormat.format(startTime1));
        Log.e("History", "Range End: " + dateFormat.format(endTime1));


        //Check how many calories were burned and recorded 5 days ago
        DataReadRequest readRequest1 = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_CALORIES_EXPENDED, DataType.AGGREGATE_CALORIES_EXPENDED)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime1, endTime1, TimeUnit.MILLISECONDS)
                .enableServerQueries()
                .build();


        DataReadResult dataReadResult1 = Fitness.HistoryApi.readData(mApiClient, readRequest1).await(1, TimeUnit.MINUTES);


        //THIS IS FOR FOUR DAYS AGO
        if (dataReadResult1.getBuckets().size() > 0) {
            Log.e("History", "Number of buckets: " + dataReadResult1.getBuckets().size());
            for (Bucket bucket : dataReadResult1.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    for (DataPoint dp : dataSet.getDataPoints()) {
                        for (Field field : dp.getDataType().getFields()) {
                            if (field.getName().equals("calories")) {
                                fourthDayCalories += dp.getValue(field).asFloat();
                                Log.d(TAG, "Fourth day: " + fourthDayCalories);
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

        fourthDayCalories = fourthDayCalories - thirdDayCalories - secondDayCalories - yesterDayCalories;

    }

    private void caloriesThirdDay() {
        //4 days ago
        Calendar cal1 = Calendar.getInstance();
        Date now1 = new Date();
        cal1.setTime(now1);
        long endTime1 = cal1.getTimeInMillis();
        cal1.add(Calendar.DAY_OF_WEEK, -3);
        long startTime1 = cal1.getTimeInMillis();

        //RESETTING VARIABLES TO ZERO
        thirdDayCalories = 0;


        java.text.DateFormat dateFormat = DateFormat.getDateInstance();
        Log.e("History", "Range Start: " + dateFormat.format(startTime1));
        Log.e("History", "Range End: " + dateFormat.format(endTime1));


        //Check how many calories were burned and recorded 3 days ago
        DataReadRequest readRequest1 = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_CALORIES_EXPENDED, DataType.AGGREGATE_CALORIES_EXPENDED)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime1, endTime1, TimeUnit.MILLISECONDS)
                .enableServerQueries()
                .build();


        DataReadResult dataReadResult1 = Fitness.HistoryApi.readData(mApiClient, readRequest1).await(1, TimeUnit.MINUTES);


        //THIS IS FOR THIRD DAYS AGO
        if (dataReadResult1.getBuckets().size() > 0) {
            Log.e("History", "Number of buckets: " + dataReadResult1.getBuckets().size());
            for (Bucket bucket : dataReadResult1.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    for (DataPoint dp : dataSet.getDataPoints()) {
                        for (Field field : dp.getDataType().getFields()) {
                            if (field.getName().equals("calories")) {
                                thirdDayCalories += dp.getValue(field).asFloat();
                                Log.d(TAG, "Third day: " + thirdDayCalories);
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

        thirdDayCalories = thirdDayCalories - secondDayCalories - yesterDayCalories;

    }

    private void caloriesSecondDay() {
        //2 days ago
        Calendar cal1 = Calendar.getInstance();
        Date now1 = new Date();
        cal1.setTime(now1);
        long endTime1 = cal1.getTimeInMillis();
        cal1.add(Calendar.DAY_OF_WEEK, -2);
        long startTime1 = cal1.getTimeInMillis();

        //RESETTING VARIABLES TO ZERO
        secondDayCalories = 0;


        java.text.DateFormat dateFormat = DateFormat.getDateInstance();
        Log.e("History", "Range Start: " + dateFormat.format(startTime1));
        Log.e("History", "Range End: " + dateFormat.format(endTime1));


        //Check how many calories were burned and recorded 2 days ago
        DataReadRequest readRequest1 = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_CALORIES_EXPENDED, DataType.AGGREGATE_CALORIES_EXPENDED)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime1, endTime1, TimeUnit.MILLISECONDS)
                .enableServerQueries()
                .build();


        DataReadResult dataReadResult1 = Fitness.HistoryApi.readData(mApiClient, readRequest1).await(1, TimeUnit.MINUTES);


        //THIS IS FOR TWO DAYS AGO
        if (dataReadResult1.getBuckets().size() > 0) {
            Log.e("History", "Number of buckets: " + dataReadResult1.getBuckets().size());
            for (Bucket bucket : dataReadResult1.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    for (DataPoint dp : dataSet.getDataPoints()) {
                        for (Field field : dp.getDataType().getFields()) {
                            if (field.getName().equals("calories")) {
                                secondDayCalories += dp.getValue(field).asFloat();
                                Log.d(TAG, "Second day: " + secondDayCalories);
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

        secondDayCalories = secondDayCalories - yesterDayCalories;

    }


    private void caloriesYesterDay() {
        //YESTERDAY
        Calendar cal1 = Calendar.getInstance();
        Date now1 = new Date();
        cal1.setTime(now1);
        long endTime1 = cal1.getTimeInMillis();
        cal1.add(Calendar.DAY_OF_WEEK, -1);
        long startTime1 = cal1.getTimeInMillis();

        //RESETTING VARIABLES TO ZERO
        yesterDayCalories = 0;


        java.text.DateFormat dateFormat = DateFormat.getDateInstance();
        Log.e("History", "Range Start: " + dateFormat.format(startTime1));
        Log.e("History", "Range End: " + dateFormat.format(endTime1));


        //Check how many calories were burned and recorded yesterday
        DataReadRequest readRequest1 = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_CALORIES_EXPENDED, DataType.AGGREGATE_CALORIES_EXPENDED)
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
                    for (DataPoint dp : dataSet.getDataPoints()) {
                        for (Field field : dp.getDataType().getFields()) {
                            if (field.getName().equals("calories")) {
                                yesterDayCalories += dp.getValue(field).asFloat();
                                Log.d(TAG, "Yesterday: " + yesterDayCalories);
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


    //same concept is applied as before with the weekend graphs
    private void weekDistanceGraph()
    {
        distanceYesterday();
        distanceSecondDay();
        distanceThirdDay();
        distanceFourthDay();
        distanceFifthDay();
        distanceSixthDay();
        distanceSeventhDay();
        final DecimalFormat df = new DecimalFormat("#.");
        final String formattedSeventhDay = df.format(seventhDayDistance);
        final float seventhDayRounded = Float.parseFloat(formattedSeventhDay);

        final DecimalFormat df1 = new DecimalFormat("#.");
        final String formattedSixthDay = df1.format(sixthDayDistance);
        final float sixthDayRounded = Float.parseFloat(formattedSixthDay);

        final DecimalFormat df2 = new DecimalFormat("#.");
        final String formattedFifthDay = df2.format(fifthDayDistance);
        final float fifthDayRounded = Float.parseFloat(formattedFifthDay);

        final DecimalFormat df3 = new DecimalFormat("#.");
        final String formattedFourthDay = df3.format(fourthDayDistance);
        final float fourthDayRounded = Float.parseFloat(formattedFourthDay);

        final DecimalFormat df4 = new DecimalFormat("#.");
        final String formattedThirdDay = df4.format(thirdDayDistance);
        final float thirdDayRounded = Float.parseFloat(formattedThirdDay);

        final DecimalFormat df5 = new DecimalFormat("#.");
        final String formattedSecondDay = df5.format(secondDayDistance);
        final float secondDayRounded = Float.parseFloat(formattedSecondDay);

        final DecimalFormat df6 = new DecimalFormat("#.");
        final String formattedYesterDay = df6.format(yesterDayDistance);
        final float yesterDayRounded = Float.parseFloat(formattedYesterDay);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                BarChart mBarChart = (BarChart) findViewById(R.id.barchart);

                mBarChart.clearChart();

                mBarChart.addBar(new BarModel("Day 7", seventhDayRounded, 0xFF123456));
                mBarChart.addBar(new BarModel("Day 6", sixthDayRounded,  0xFF343456));
                mBarChart.addBar(new BarModel("Day 5", fifthDayRounded, 0xFF563456));
                mBarChart.addBar(new BarModel("Day 4", fourthDayRounded, 0xFF873F56));
                mBarChart.addBar(new BarModel("Day 3", thirdDayRounded, 0xFF56B7F1));
                mBarChart.addBar(new BarModel("Day 2", secondDayRounded,  0xFF343456));
                mBarChart.addBar(new BarModel("Day 1", yesterDayRounded, 0xFF1FF4AC));

                mBarChart.startAnimation();

            }
        });
    }


    //for displaying data for today we can make a call to another method that does not deal
    //with buckets, there is a function called readDailyTotal which is used to retrieve the value for today
    private void todayDistanceGraph()
    {
        distanceToday();
        final DecimalFormat df = new DecimalFormat("#.0");
        final String formattedDistance = df.format(distanceToday);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PieChart mPieChart = (PieChart) findViewById(R.id.piechart_distance_today);

                mPieChart.clearChart();

                mPieChart.addPieSlice(new PieModel("Distance Today", distanceToday,Color.parseColor("#181c18")));
                mPieChart.addPieSlice(new PieModel("Average Distance", averageDistance,Color.parseColor("#1cb719")));

                mPieChart.startAnimation();

                //updating the text view at the top of the application
                TextView textView = (TextView) findViewById(R.id.textView_distance);
                textView.setText(formattedDistance);

            }
        });
    }

    //same concept as other today graphs
    private void todayStepsGraph()
    {
        stepsToday();
        final String stepToday = Integer.toString(stepsToday);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PieChart mPieChart = (PieChart) findViewById(R.id.piechart);

                mPieChart.clearChart();

                mPieChart.addPieSlice(new PieModel("Steps Today", stepsToday,Color.parseColor("#FE6DA8")));
                mPieChart.addPieSlice(new PieModel("Average Steps", averageSteps,Color.parseColor("#56B7F1")));

                mPieChart.startAnimation();

                TextView textView = (TextView) findViewById(R.id.textView_stepsval);
                textView.setText(stepToday);

            }
        });
    }

    //same concept as other time graphs
    private void todayTimeGraph()
    {
        timeToday();

        final String myText=Integer.toString(timeToday);
        final int timeTodayTotal = Integer.parseInt(myText);


        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                PieChart mPieChart = (PieChart) findViewById(R.id.piechart_time_today);

                mPieChart.clearChart();

                mPieChart.addPieSlice(new PieModel("Time Active Today", timeTodayTotal ,Color.parseColor("#5d5d5d")));
                mPieChart.addPieSlice(new PieModel("Average Time Active", averageTime,Color.parseColor("#ff8c46")));
                //mPieChart.addPieSlice(new PieModel("Goal", 10000, Color.parseColor("#56B7F1")));

                mPieChart.startAnimation();

                //updating the text view at the top of the app
                TextView textView = (TextView) findViewById(R.id.textView_time);
                textView.setText(myText);

            }
        });
    }

    //same concept as before with other today graphs
    private void todayCaloriesGraph()
    {
        caloriesToday();
        //final String myText=Float.toString(caloriesToday);
        final DecimalFormat df = new DecimalFormat("#.0");
        final String formattedCalories = df.format(caloriesToday);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                BarChart mBarChart = (BarChart) findViewById(R.id.barchart_calories_today);

                mBarChart.clearChart();

                mBarChart.addBar(new BarModel("Todays Calories", caloriesToday, 0xFF123456));
                mBarChart.addBar(new BarModel("Average This Week", averageCalories,  0xFF1FF4AC));

                mBarChart.startAnimation();

                //updating the text view at the top of the application
                TextView textView = (TextView) findViewById(R.id.textView_caloriesval);
                textView.setText(formattedCalories);

            }
        });
    }

    //life timetotal graph along with updating the life time total value text views
    private void lifetimeTotalGraph()
    {
        stepsLifetimeTotal();
        caloriesLifetimeTotal();
        distanceLifetimeTotal();
        timeLifetimeTotal();

        //Conversion of milliseconds to minutes
        lifetimeTotalTime = (lifetimeTotalTime / (1000 * 60));

        //converting float values to a string to update a text view
        final String lifetimeTotalCalTv = Float.toString(lifetimeTotalCalories);
        final String lifetimeTotalDisTv = Float.toString(lifetimeTotalDistance);
        final String lifetimeTotalStepTv = Float.toString(lifetimeTotalSteps);
        final String lifetimeTotalTimeTv = Float.toString(lifetimeTotalTime);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                BarChart mBarChart = (BarChart) findViewById(R.id.barchart_lifetime_totals);

                mBarChart.clearChart();

                mBarChart.addBar(new BarModel("Total Steps", lifetimeTotalSteps, 0xFF63CBB0));
                mBarChart.addBar(new BarModel("Total Calories", lifetimeTotalCalories, 0xFF1BA4E6));
                mBarChart.addBar(new BarModel("Total Distance", lifetimeTotalDistance, 0xfff88379));
                mBarChart.addBar(new BarModel("Total Time", lifetimeTotalTime, 0xFF123456));

                mBarChart.startAnimation();

                //update each text view
                TextView textView = (TextView) findViewById(R.id.textView_lifetime_calories);
                textView.setText(lifetimeTotalCalTv);
                TextView textView1 = (TextView) findViewById(R.id.textView_lifetime_distance);
                textView1.setText(lifetimeTotalDisTv);
                TextView textView2 = (TextView) findViewById(R.id.textView_lifetime_time);
                textView2.setText(lifetimeTotalTimeTv);
                TextView textView3 = (TextView) findViewById(R.id.textView_lifetime_steps);
                textView3.setText(lifetimeTotalStepTv);

            }
        });
    }

    //lifetime totals for distance
    private void distanceLifetimeTotal() {
        //1 week ago
        Calendar cal1 = Calendar.getInstance();
        Date now1 = new Date();
        cal1.setTime(now1);
        long endTime1 = cal1.getTimeInMillis();
        cal1.add(Calendar.YEAR, -1);
        long startTime1 = cal1.getTimeInMillis();

        //RESETTING VARIABLES TO ZERO
        lifetimeTotalDistance = 0;


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


        //THIS IS FOR TOTAL DISTANCE
        if (dataReadResult1.getBuckets().size() > 0) {
            Log.e("History", "Number of buckets: " + dataReadResult1.getBuckets().size());
            for (Bucket bucket : dataReadResult1.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    for (DataPoint dp : dataSet.getDataPoints()) {
                        for (Field field : dp.getDataType().getFields()) {
                            if (field.getName().equals("distance")) {
                                lifetimeTotalDistance += dp.getValue(field).asFloat();
                                Log.d(TAG, "Lifetime Total distance: " + lifetimeTotalDistance);
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



    //used for testing to view different data sets to see if data retrieval was successful
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

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        Toast.makeText(getApplicationContext(), "History for: " + adamField.getName() + "\t\tStart: " + dateFormat.format(adamDp.getStartTime(TimeUnit.MILLISECONDS)) + " " + timeFormat.format(adamDp.getStartTime(TimeUnit.MILLISECONDS)) + "\t\tEnd: " + dateFormat.format(adamDp.getEndTime(TimeUnit.MILLISECONDS)) + " " + timeFormat.format(adamDp.getEndTime(TimeUnit.MILLISECONDS)) + "\tValue: " + adamDp.getValue(adamField), Toast.LENGTH_SHORT).show();
                    }
                });

            }


        }
    }


    //today graph methods
    private void distanceToday()
    {
        //today
        //RESETTING VARIABLES TO ZERO
        distanceToday = 0;

        //utilising the readdailytotal method from the google fit api and passing in the data type
        //that we want to view data for
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

    private void stepsToday()
    {
        //today
        //RESETTING VARIABLES TO ZERO
        stepsToday = 0;

        PendingResult<DailyTotalResult> result1 = Fitness.HistoryApi.readDailyTotal(mApiClient, DataType.TYPE_STEP_COUNT_DELTA);
        DailyTotalResult totalResult = result1.await(30, TimeUnit.SECONDS);
        if (totalResult.getStatus().isSuccess()) {
            DataSet totalSet = totalResult.getTotal();
            stepsToday = totalSet.isEmpty()
                    ? 0
                    : totalSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
        } else {
            Log.w(TAG, "There was a problem getting the steps count.");
        }


        Log.i(TAG, "Total Steps today: " + stepsToday);

    }

    private void caloriesToday()
    {
        //today
        //RESETTING VARIABLES TO ZERO
        caloriesToday = 0;

        PendingResult<DailyTotalResult> result1 = Fitness.HistoryApi.readDailyTotal(mApiClient, DataType.TYPE_CALORIES_EXPENDED);
        DailyTotalResult totalResult = result1.await(30, TimeUnit.SECONDS);
        if (totalResult.getStatus().isSuccess()) {
            DataSet totalSet = totalResult.getTotal();
            caloriesToday = totalSet.isEmpty()
                    ? 0
                    : totalSet.getDataPoints().get(0).getValue(Field.FIELD_CALORIES).asFloat();
        } else {
            Log.w(TAG, "There was a problem getting the Calories count.");
        }


        Log.i(TAG, "Total calories today: " + caloriesToday);

    }

    private void timeToday()
    {
        //today
        //RESETTING VARIABLES TO ZERO
        timeToday = 0;

        PendingResult<DailyTotalResult> result1 = Fitness.HistoryApi.readDailyTotal(mApiClient, DataType.TYPE_ACTIVITY_SEGMENT);
        DailyTotalResult totalResult = result1.await(30, TimeUnit.SECONDS);
        if (totalResult.getStatus().isSuccess()) {
            DataSet totalSet = totalResult.getTotal();
            timeToday = totalSet.isEmpty()
                    ? 0
                    : totalSet.getDataPoints().get(0).getValue(Field.FIELD_DURATION).asInt();
        } else {
            Log.w(TAG, "There was a problem getting the time count.");
        }

        timeToday = (timeToday / (1000 * 60));


        Log.i(TAG, "Total time today: " + timeToday);

    }



    //to pass in specific data to the api
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

    //adding average weight for a person
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
                DataType.TYPE_WEIGHT,    // for weight, it would be DataType.TYPE_WEIGHT
                DataSource.TYPE_RAW,
                weight,                  // weight in kgs
                startTime,              // start time
                endTime,                // end time
                TimeUnit.MILLISECONDS
        );

        Fitness.HistoryApi.insertData(mApiClient, weightDataSet)
                        .await(1, TimeUnit.MINUTES);

        Log.e( "History", "data inserted" + weight );

    }

    //adding average height for a person
    private void addHeightDataToGoogleFit() {
        //Adds height spread out evenly from start time to end time
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.HOUR_OF_DAY, -1);
        long startTime = cal.getTimeInMillis();

        String newheight = "1.8";
        float height = Float.parseFloat(newheight);
        DataSet heightDataSet = createDataForRequest(
                DataType.TYPE_HEIGHT,    // for height, it would be DataType.TYPE_HEIGHT
                DataSource.TYPE_RAW,
                height,                  // height in meters
                startTime,              // start time
                endTime,                // end time
                TimeUnit.MILLISECONDS
        );

        Fitness.HistoryApi.insertData(mApiClient, heightDataSet)
                        .await(1, TimeUnit.MINUTES);

        Log.e( "History", "data inserted" + height );

    }

    //method to add steps to the api
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

    //method to update steps for a particular day
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

    //method to delete steps for a particular day
//    private void deleteStepDataOnGoogleFit() {
//        Calendar cal = Calendar.getInstance();
//        Date now = new Date();
//        cal.setTime(now);
//        long endTime = cal.getTimeInMillis();
//        cal.add(Calendar.DAY_OF_YEAR, -1);
//        long startTime = cal.getTimeInMillis();
//
//        DataDeleteRequest request = new DataDeleteRequest.Builder()
//                .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
//                .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
//                .build();
//
//        Fitness.HistoryApi.deleteData(mApiClient, request).await(1, TimeUnit.MINUTES);
//    }


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

        }
    }


//shows the users subscriptions to different datatypes
//    public void subscribe() {
//        // To create a subscription, invoke the Recording API. As soon as the subscription is
//        // active, fitness data will start recording.
//        Fitness.RecordingApi.subscribe(mApiClient, DataType.TYPE_STEP_COUNT_CUMULATIVE)
//                .setResultCallback(new ResultCallback<Status>() {
//                    @Override
//                    public void onResult(Status status) {
//                        if (status.isSuccess()) {
//                            if (status.getStatusCode()
//                                    == FitnessStatusCodes.SUCCESS_ALREADY_SUBSCRIBED) {
//                                Log.i(TAG, "Existing subscription for activity detected.");
//                            } else {
//                                Log.i(TAG, "Successfully subscribed!");
//                            }
//                        } else {
//                            Log.w(TAG, "There was a problem subscribing.");
//                        }
//                    }
//                });
//
//    }

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



