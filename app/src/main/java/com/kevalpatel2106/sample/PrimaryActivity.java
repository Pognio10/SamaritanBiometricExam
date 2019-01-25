package com.kevalpatel2106.sample;

import android.Manifest;
import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.androidhiddencamera.HiddenCameraFragment;
import com.microsoft.projectoxford.face.contract.Person;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PrimaryActivity extends AppCompatActivity
{


	private ArrayList<PersonObj> personObjs;
	private TextView numberPerson;


	private HiddenCameraFragment mHiddenCameraFragment;
	private boolean start = false;
	private ScheduledExecutorService exec;

	private DevicePolicyManager deviceManger;
	ActivityManager activityManager;
	ComponentName compName;
	static final int RESULT_ENABLE = 1;

	private Identify restIdentify;
	private RVAdapter adapter;
	private RecyclerView rv;

	private TextView statusTrain;
	private TextView statusService;

	private Button startService;
	private Button stopService;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_primary);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
		intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName);
		intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Without these permissions it is not possible to start the application in the background");
		startActivityForResult(intent, RESULT_ENABLE);

		deviceManger = (DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);
		activityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
		compName = new ComponentName(this, MyAdmin.class);

		stopService = findViewById(R.id.btn_deactive_service);
		startService = findViewById(R.id.btn_active_service);

		restIdentify = new Identify(DataObj.getSubscriptionKey(), getApplicationContext());

		int SDK_INT = android.os.Build.VERSION.SDK_INT;
		if (SDK_INT > 8)
		{
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
			//your codes here

		}

		int PERMISSION_ALL = 1;
		String[] PERMISSIONS = {
				Manifest.permission.SYSTEM_ALERT_WINDOW,
				Manifest.permission.CAMERA,
				Manifest.permission.WRITE_EXTERNAL_STORAGE,
				Manifest.permission.READ_EXTERNAL_STORAGE,
				Manifest.permission.WAKE_LOCK,
				Manifest.permission.DISABLE_KEYGUARD,
				Manifest.permission.INTERNET,
				Manifest.permission.ACCESS_WIFI_STATE,
				Manifest.permission.ACCESS_NETWORK_STATE
		};

		if(!hasPermissions(this, PERMISSIONS)){
			ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
		}

		statusTrain = findViewById(R.id.statusTrain);
		statusService = findViewById(R.id.statusText);

		if(start)
		{
			statusService.setText("ACTIVED");
			statusService.setTextColor(Color.GREEN);

			startService.setEnabled(false);
			stopService.setEnabled(true);
		}
		else
		{
			statusService.setText("STOPPED");
			statusService.setTextColor(Color.RED);
			startService.setEnabled(true);
			stopService.setEnabled(false);
		}

		FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
		fab.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{

				Intent intent = new Intent(getApplicationContext(), PersonActivity.class);
				startActivity(intent, null);
			}
		});

		// This method creates an ArrayList that has three Person objects
		// Checkout the project associated with this tutorial on Github if
		// you want to use the same images.

		this.personObjs = new ArrayList<>();

		numberPerson = findViewById(R.id.personNumber);

		rv = findViewById(R.id.rv);
		LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext());
		rv.setLayoutManager(llm);
		this.adapter = new RVAdapter(this.personObjs);
		rv.setAdapter(this.adapter);

		initializeData();

		numberPerson.setText(Integer.toString(personObjs.size()));

		startService.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				start = true;
				stopService.setEnabled(true);
				startService.setEnabled(false);
				statusService.setText("ACTIVED");
				statusService.setTextColor(Color.GREEN);
				service();
			}
		});

		stopService.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				stopService.setEnabled(false);
				startService.setEnabled(true);
				start = false;
				statusService.setText("STOPPED");
				statusService.setTextColor(Color.RED);
				exec.shutdown();
			}
		});


		Person[] p = restIdentify.getPerson();
		Log.d("PERSON NUMBER", p.length +"" );
		if(p.length > 0)
		{
			restIdentify.trainByGroupId();

			for (Person person : p)
			{
				Log.d("PERSON_ON", "################################################");
				Log.d("PERSON_ON", person.name);
				Log.d("PERSON_ON UUID", person.personId.toString());
				for (UUID uuid : person.persistedFaceIds)
					Log.d("PERSON_ON UUID FACE", uuid.toString());

				Log.d("PERSON_ON", "################################################");

			}
		}

		Log.d("GROUP NUMBER", restIdentify.getGroup().size()+"");
		for (String group : restIdentify.getGroup())
			Log.d("GROUP", group);

	}




	private void service()
	{
		exec = Executors.newSingleThreadScheduledExecutor();
		exec.scheduleAtFixedRate(new Runnable()
		{
			@Override
			public void run()
			{
				Log.d("TEST CAMERA", "STATUS: " + start);
				if (start)
				{
					startService(new Intent(PrimaryActivity.this, DemoCamService.class));
				}


			}
		}, 0, 10, TimeUnit.SECONDS);
	}

	private boolean isMyServiceRunning(Class<?> serviceClass) {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceClass.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}


	private void initializeData()
	{
		Log.d("initializeData", "initializeData");
		personObjs.clear();
		for (String personId : DataObj.getListPersonWhitelist(((MyApplication)getApplication()).getSharedPreferences()))
		{
			String name = DataObj.getPersonWhiteList(((MyApplication)getApplication()).getSharedPreferences(), personId);
			Log.d("PERSON initializeData", name);
			Log.d("PERSON initializeData", personId);

			Bitmap photo = getFirstImage(personId) != null ? getFirstImage(personId) : BitmapFactory.decodeResource(this.getResources(), R.drawable.personal);

			personObjs.add(new PersonObj(name, personId, photo));
		}

		adapter.notifyDataSetChanged();


	}

	@Override
	protected void onStart()
	{
		super.onStart();
		String folder_main = "BiometricSystem";

		File f = new File(Environment.getExternalStorageDirectory(), folder_main);
		if (!f.exists())
		{
			f.mkdirs();
			DataObj.saveMainPath(((MyApplication)getApplication()).getSharedPreferences(),f.getAbsolutePath()+"");
		}
	}


	@Override
	protected void onResume()
	{
		super.onResume();
		initializeData();
		Log.d("RESUME", "RESUME");
		numberPerson.setText(Integer.toString(personObjs.size()));
		checkStatusTrain();
	}


	public static boolean hasPermissions(Context context, String... permissions) {
		if (context != null && permissions != null) {
			for (String permission : permissions) {
				if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
					return false;
				}
			}
		}
		return true;
	}


	private Bitmap getFirstImage(String personId)
	{
		File path = new File(DataObj.getMainPath(((MyApplication)getApplication()).getSharedPreferences())+"/"+personId);
		String[] fileNames = new String[0];

		if(path.exists())
		{
			fileNames = path.list();
		}

		if(fileNames.length > 0)
			return BitmapFactory.decodeFile(path.getPath()+"/"+ fileNames[0]);
		else
			return null;
	}

	private void checkStatusTrain()
	{
		final ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
		exec.scheduleAtFixedRate(new Runnable()
		{
			@Override
			public void run()
			{
				String status = restIdentify.trainStatus();
				statusTrain.setText(status.toUpperCase());

				if(!status.equalsIgnoreCase("running"))
				{
					exec.shutdown();
				}
			}
		}, 0, 2, TimeUnit.SECONDS);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_activity_primary, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		if (id == R.id.edit_group_id)
		{
			generateRandomGroup();
		}

		return super.onOptionsItemSelected(item);
	}

	private void generateRandomGroup()
	{
		ArrayList<String> oldGroups = restIdentify.getGroup();
		if(oldGroups.size() > 0)
		{
			for(String group : oldGroups)
				restIdentify.deleteGroup(group);
		}

		String id = UUID.randomUUID().toString();
		restIdentify.createGroupId(id);
	}
}
