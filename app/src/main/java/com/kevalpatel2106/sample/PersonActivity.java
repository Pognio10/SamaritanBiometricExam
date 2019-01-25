package com.kevalpatel2106.sample;

import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.microsoft.projectoxford.face.contract.CreatePersonResult;
import com.microsoft.projectoxford.face.contract.Face;

import org.apache.commons.io.FileUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.UUID;

public class PersonActivity extends AppCompatActivity
{


	private String name;
	private String personId;

	private String folderName = "test";

	private EditText inputName;


	private Identify restIdentify;

	private GridView gridView;
	private ArrayList<Item> gridArray = new ArrayList<Item>();
	private ImageGridAdapter customGridAdapter;

	private int PICK_IMAGE_REQUEST = 1;
	private int IDENTIFY_IMAGE_REQUEST = 2;

	private ArrayList<Bitmap> imageToAdd;

	private ProgressBar loader;

	private final String NOBACK = "NOBACK";


	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_person);
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		loader = findViewById(R.id.progressBarPerson);

		imageToAdd = new ArrayList<>();

		restIdentify = new Identify(DataObj.getSubscriptionKey(), getApplicationContext());
		gridView = (GridView) findViewById(R.id.gridView1);
		customGridAdapter = new ImageGridAdapter(PersonActivity.this, R.layout.row_grid, gridArray);
		gridView.setAdapter(customGridAdapter);


		FloatingActionButton fab = findViewById(R.id.fab);
		fab.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{

				Intent intent = new Intent();
				// Show only images, no videos or anything else
				intent.setType("image/*");
				intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
				intent.setAction(Intent.ACTION_GET_CONTENT);
				// Always show the chooser (if there are multiple options available)
				startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);

			}
		});

		inputName = findViewById(R.id.nameEditText);

		Intent data = getIntent();
		if (data != null && data.getExtras() != null)
		{
			if (data.getExtras().get("name") != null)
			{
				this.name = data.getExtras().get("name").toString();
				this.personId = data.getExtras().get("personId").toString();
				inputName.setText(name);
				inputName.setInputType(InputType.TYPE_NULL);
			}
		}

		if (this.personId != null)
		{
			loadImage(this.personId);
			this.folderName = this.personId;
		}

	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_activity_person, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		loader.setVisibility(View.VISIBLE);

		if (id == R.id.save_person)
		{
			String personUUID = savePerson();
			Log.d("PERSON UUID SAVED", personUUID);
			if (personUUID != null && !personUUID.equals("") && !personUUID.equals(NOBACK))
			{
				Log.d("PERSON SAVED", personUUID);

				String path = DataObj.getMainPath(((MyApplication) getApplication()).getSharedPreferences());

				if (folderName.equals("test"))
				{
					File testFolder = new File(path + "/test");
					File newFolder = new File(path + "/" + this.personId);
					boolean success = testFolder.renameTo(newFolder);
					Log.d("SUCCESS CREATE", success + "");
				}

				restIdentify.trainByGroupId();
				onBackPressed();
			}
			else
				Log.d("PERSON SAVED CLICK", "NULL");
			return true;
		}
		if (id == R.id.delete_person)
		{
//			loader.setVisibility(View.VISIBLE);

			if (this.personId != null)
			{
				deleteFolder(this.folderName);
				restIdentify.deletePerson(UUID.fromString(this.personId));
			}

			onBackPressed();
		}

		return super.onOptionsItemSelected(item);
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null)
		{

			if (data.getData() != null)
			{
				Uri uri = data.getData();
				loadImageByIntent(uri);

			}
			else
			{
				if (data.getClipData() != null)
				{
					ClipData mClipData = data.getClipData();
					ArrayList<Uri> mArrayUri = new ArrayList<Uri>();
					for (int i = 0; i < mClipData.getItemCount(); i++)
					{

						ClipData.Item item = mClipData.getItemAt(i);
						mArrayUri.add(item.getUri());
						Uri uri = item.getUri();
						loadImageByIntent(uri);


					}
					Log.v("LOG_TAG", "Selected Images" + mArrayUri.size());
				}
			}
		}
		else
		{
			Toast.makeText(this, "You haven't picked Image", Toast.LENGTH_LONG).show();

		}
	}


	private void loadImageByIntent(Uri uri)
	{
		try
		{
			Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
			InputStream imageInputStream = new ByteArrayInputStream(stream.toByteArray());

			Face[] faces = restIdentify.detectFace(imageInputStream);
			Log.d("FACES NUMBER", faces.length +"");
 			if(faces.length > 0)
			{
//				for(Face face : faces)
//				{
//					FaceAttribute faceAttribute = face.faceAttributes;
//					Log.d("AGE", faceAttribute.age +"");
//					Log.d("GENDER", faceAttribute.gender);
//				}

				imageToAdd.add(bitmap);
				gridArray.add(new Item(bitmap));
				customGridAdapter = new ImageGridAdapter(PersonActivity.this, R.layout.row_grid, gridArray);
				gridView.setAdapter(customGridAdapter);

				try
				{
					File folder = new File(DataObj.getMainPath(((MyApplication) getApplication()).getSharedPreferences()) + "/" + folderName);
					FileOutputStream out = new FileOutputStream(folder + "/" + System.currentTimeMillis() + ".jpg");
					bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
					out.flush();
					out.close();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			else
			{
				Snackbar.make(getCurrentFocus(), "no face detected in image ", Snackbar.LENGTH_LONG).setAction("Action", null).show();

			}


		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void loadImage(String personId)
	{
		File path = new File(DataObj.getMainPath(((MyApplication) getApplication()).getSharedPreferences()) + "/" + personId);
		String[] fileNames = new String[0];

		if (path.exists())
		{
			fileNames = path.list();
		}
		for (int i = 0; i < fileNames.length; i++)
		{
			Bitmap mBitmap = BitmapFactory.decodeFile(path.getPath() + "/" + fileNames[i]);
			gridArray.add(new Item(mBitmap));
			///Now set this bitmap on imageview
		}
	}

	private void createFolder(String folder)
	{
		File f = new File(DataObj.getMainPath(((MyApplication) getApplication()).getSharedPreferences()) + "/" + folder);
		if (!f.exists())
		{
			f.mkdirs();
		}
		Log.d("FOLDER", f.getAbsolutePath());
	}

	private void deleteFolder(String folder)
	{
		String path = DataObj.getMainPath(((MyApplication) getApplication()).getSharedPreferences()) + "/" + folder;

		File dir = new File(path);
		try
		{
			FileUtils.deleteDirectory(dir);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		if (this.personId == null)
			createFolder("test");

	}

	@Override
	public void onBackPressed()
	{
		super.onBackPressed();
		deleteFolder("test");
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		deleteFolder("test");
	}


	private String savePerson()
	{
		CreatePersonResult createPersonResult = null;

		if (this.personId == null)
		{
			if (inputName.getText().toString().equals(""))
			{
				loader.setVisibility(View.INVISIBLE);
				Snackbar.make(getCurrentFocus(), "IL CAMPO NAME NON PUO' ESSERE VUOTO!", Snackbar.LENGTH_LONG).setAction("Action", null).show();
				return NOBACK;
			}
			else
			{
				createPersonResult = restIdentify.createPerson(inputName.getText().toString());
				this.personId = createPersonResult.personId.toString();
			}

			if (imageToAdd.size() == 0)
			{
				Snackbar.make(getCurrentFocus(), "NESSUNA IMMAGINE SELEZIONATA!", Snackbar.LENGTH_LONG).setAction("Action", null).show();
				loader.setVisibility(View.INVISIBLE);
				return NOBACK;
			}
			else
			{
				for (Bitmap photo : imageToAdd)
				{
					ByteArrayOutputStream stream = new ByteArrayOutputStream();
					photo.compress(Bitmap.CompressFormat.JPEG, 100, stream);
					InputStream imageInputStream = new ByteArrayInputStream(stream.toByteArray());
					restIdentify.addFaceGroup(imageInputStream, createPersonResult.personId);
				}
			}
		}
		else
		{
			if (imageToAdd.size() > 0)
			{
				for (Bitmap photo : imageToAdd)
				{
					ByteArrayOutputStream stream = new ByteArrayOutputStream();
					photo.compress(Bitmap.CompressFormat.JPEG, 100, stream);
					InputStream imageInputStream = new ByteArrayInputStream(stream.toByteArray());
					restIdentify.addFaceGroup(imageInputStream, UUID.fromString(this.personId));
				}
			}
		}
		return this.personId == null ? "" : this.personId;
	}
}
