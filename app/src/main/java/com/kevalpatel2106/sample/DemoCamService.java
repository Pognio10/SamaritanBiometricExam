/*
 * Copyright 2016 Keval Patel.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kevalpatel2106.sample;

import android.Manifest;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.androidhiddencamera.CameraConfig;
import com.androidhiddencamera.CameraError;
import com.androidhiddencamera.HiddenCameraService;
import com.androidhiddencamera.HiddenCameraUtils;
import com.androidhiddencamera.config.CameraFacing;
import com.androidhiddencamera.config.CameraFocus;
import com.androidhiddencamera.config.CameraImageFormat;
import com.androidhiddencamera.config.CameraResolution;
import com.androidhiddencamera.config.CameraRotation;
import com.microsoft.projectoxford.face.contract.Candidate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by Keval on 11-Nov-16.
 *
 * @author {@link 'https://github.com/kevalpatel2106'}
 */

public class DemoCamService extends HiddenCameraService
{
	private Identify restIdentify;
	private DevicePolicyManager deviceManger;

	@Nullable
	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{

		deviceManger = (DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);

		restIdentify = new Identify(DataObj.getSubscriptionKey(), getApplicationContext());

		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
		{

			if (HiddenCameraUtils.canOverDrawOtherApps(this))
			{
				CameraConfig cameraConfig = new CameraConfig()
						.getBuilder(this)
						.setCameraFacing(CameraFacing.FRONT_FACING_CAMERA)
						.setCameraResolution(CameraResolution.MEDIUM_RESOLUTION)
						.setImageFormat(CameraImageFormat.FORMAT_JPEG)
						.setCameraFocus(CameraFocus.AUTO)
						.setImageRotation(CameraRotation.ROTATION_270)
						.build();

				startCamera(cameraConfig);

				new android.os.Handler().postDelayed(new Runnable()
				{
					@Override
					public void run()
					{
						//Toast.makeText(DemoCamService.this, "Capturing image.", Toast.LENGTH_SHORT).show();
						takePicture();
					}
				}, 1500L);
			}
			else
			{

				//Open settings to grant permission for "Draw other apps".
				HiddenCameraUtils.openDrawOverPermissionSetting(this);
			}
		}
		else
		{

			//Ask your parent activity for providing runtime permission
			//Toast.makeText(this, "Camera permission not available", Toast.LENGTH_SHORT).show();
			Log.d("TEST CAMERA", "Camera permission not available");
		}
		return START_NOT_STICKY;
	}

	@Override
	public void onImageCapture(@NonNull File imageFile)
	{
		//Toast.makeText(this, "Captured image size is : " + imageFile.length(), Toast.LENGTH_SHORT).show();
		// Do something with the image...

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Bitmap.Config.RGB_565;
		Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);

		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
		InputStream imageInputStream = new ByteArrayInputStream(stream.toByteArray());

		ArrayList<Candidate> candidates = restIdentify.identificaizone(imageInputStream);
		ArrayList<Candidate> acceptCandidateList = new ArrayList<>();

		if(candidates.size() > 0)
		{
			for(Candidate candidate : candidates)
			{
				if(DataObj.existPersonWhiteList(((MyApplication)getApplication()).getSharedPreferences(), candidate.personId.toString()))
				{
//					String name = DataObj.getPersonWhiteList(((MyApplication)getApplication()).getSharedPreferences(), candidate.personId.toString());
//					Log.d("TI HO RICONOSCIUTO!:D ", "NOME: " + name + " CONFIDANCE: " + candidate.confidence );
					acceptCandidateList.add(candidate);
//					Toast.makeText(this, "TI HO RICONOSCIUTO!:D NOME: " + name + " CONFIDANCE: " + candidate.confidence, Toast.LENGTH_LONG).show();
//					Snackbar.make(, "Ciao "+ name + " ! CONDIFANCE: " + candidate.confidence, Snackbar.LENGTH_LONG).setAction("Action", null).show();
				}
//				else
//				{
//					deviceManger.lockNow();
//					this.stopSelf();
//				}
			}
		}
		else
		{
			deviceManger.lockNow();
			stopSelf();

		}

		if(acceptCandidateList.size() > 0)
		{
			Log.d("PERSONE RICONOSCIUTE", acceptCandidateList.size()+"");
			ArrayList<String> names = new ArrayList<>();
			ArrayList<String> confidences = new ArrayList<>();
			for (Candidate candidate : acceptCandidateList)
			{
				String name = DataObj.getPersonWhiteList(((MyApplication)getApplication()).getSharedPreferences(), candidate.personId.toString());
//				Log.d("TI HO RICONOSCIUTO!:D ", "NOME: " + name + " CONFIDANCE: " + candidate.confidence );
//				Log.d("RICONOSCIUTO ", "NOME: " + name + " CONFIDANCE: " + candidate.confidence );
//			 	Toast.makeText(this, "TI HO RICONOSCIUTO!:D NOME: " + name + " CONFIDANCE: " + candidate.confidence, Toast.LENGTH_LONG).show();
//				Toast.makeText(this, "NOME: " + name + " CONFIDANCE: " + candidate.confidence, Toast.LENGTH_LONG).show();

				names.add(name);
				confidences.add(candidate.confidence+"");

			}

			Log.d("RICONOSCIUTO ", "NOME: " + names.toString() + " CONFIDANCE: " + confidences.toString() );
			Toast.makeText(this, "NOME: " + names.toString() + " CONFIDANCE: " + confidences.toString(), Toast.LENGTH_LONG).show();


		}
		else
		{
			deviceManger.lockNow();
			stopSelf();
		}

		stopSelf();
	}

	@Override
	public void onCameraError(@CameraError.CameraErrorCodes int errorCode)
	{
		switch (errorCode)
		{
			case CameraError.ERROR_CAMERA_OPEN_FAILED:
				//Camera open failed. Probably because another application
				//is using the camera
				Log.d("TEST CAMERA", "R.string.error_cannot_open");
				Toast.makeText(this, R.string.error_cannot_open, Toast.LENGTH_LONG).show();
				break;
			case CameraError.ERROR_IMAGE_WRITE_FAILED:
				//Image write failed. Please check if you have provided WRITE_EXTERNAL_STORAGE permission
				Log.d("TEST CAMERA", "R.string.error_cannot_write");
				Toast.makeText(this, R.string.error_cannot_write, Toast.LENGTH_LONG).show();
				break;
			case CameraError.ERROR_CAMERA_PERMISSION_NOT_AVAILABLE:
				//camera permission is not available
				//Ask for the camera permission before initializing it.
				Log.d("TEST CAMERA", "R.string.error_cannot_get_permission");
				Toast.makeText(this, R.string.error_cannot_get_permission, Toast.LENGTH_LONG).show();
				break;
			case CameraError.ERROR_DOES_NOT_HAVE_OVERDRAW_PERMISSION:
				//Display information dialog to the user with steps to grant "Draw over other app"
				//permission for the app.
				HiddenCameraUtils.openDrawOverPermissionSetting(this);
				break;
			case CameraError.ERROR_DOES_NOT_HAVE_FRONT_CAMERA:
				Log.d("TEST CAMERA", "R.string.error_not_having_camera");
				Toast.makeText(this, R.string.error_not_having_camera, Toast.LENGTH_LONG).show();
				break;
		}

		stopSelf();
	}
}
