package com.kevalpatel2106.sample;

import android.content.Context;
import android.util.Log;

import com.microsoft.projectoxford.face.FaceServiceRestClient;
import com.microsoft.projectoxford.face.contract.AddPersistedFaceResult;
import com.microsoft.projectoxford.face.contract.Candidate;
import com.microsoft.projectoxford.face.contract.CreatePersonResult;
import com.microsoft.projectoxford.face.contract.Face;
import com.microsoft.projectoxford.face.contract.IdentifyResult;
import com.microsoft.projectoxford.face.contract.Person;
import com.microsoft.projectoxford.face.contract.PersonGroup;
import com.microsoft.projectoxford.face.contract.TrainingStatus;
import com.microsoft.projectoxford.face.rest.ClientException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;

public class Identify
{

	// Replace `<API endpoint>` with the Azure region associated with
	// your subscription key. For example,
	// apiEndpoint = "https://westcentralus.api.cognitive.microsoft.com/face/v1.0"
//	private final String apiEndpoint = "https://westeurope.api.cognitive.microsoft.com/face/v1.0/";
	private final String apiEndpoint = "https://westus.api.cognitive.microsoft.com/face/v1.0";
	private ScheduledExecutorService exec;
	private Context context;



	//5dd960417e644538a662445a78a28552
	private String subscirptionKey;
//	private String groupId = "whitelistexam";
//	private String groupId;
	private FaceServiceRestClient faceServiceRestClient;


	public Identify(String subscirptionKey, Context context)
	{
		this.subscirptionKey = subscirptionKey;
		this.faceServiceRestClient = new FaceServiceRestClient(apiEndpoint, subscirptionKey);
		this.context = context;
	}


	public ArrayList<String> getGroup()
	{
		ArrayList<String> res = new ArrayList<>();
		try
		{
			for(PersonGroup group : this.faceServiceRestClient.getPersonGroups())
				res.add(group.name);
		}
		catch (ClientException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return res;
	}

	public void createGroupId(String id)
	{
		try
		{
			faceServiceRestClient.createPersonGroup(id, id, "");
			DataObj.saveGroupId(((MyApplication)context).getSharedPreferences(), id);
		}
		catch (ClientException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void deleteGroup(String groupId)
	{
		try
		{
			faceServiceRestClient.deletePersonGroup(groupId);
		}
		catch (ClientException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public AddPersistedFaceResult addFaceGroup(InputStream img, UUID personId)
	{
		String groupId = DataObj.getGroupId(((MyApplication)context).getSharedPreferences());
		AddPersistedFaceResult result = null;
		Person person = null;
		try
		{
//			String personName = DataObj.getPersonWhiteList(((MyApplication)context).getSharedPreferences(), personId.toString());
//			Log.d("PERSON", personName);
//			Log.d("PERSON", personId.toString());

			result = this.faceServiceRestClient.addPersonFace(groupId, personId , img, null, null);

		}
		catch (ClientException e)
		{
			Log.e("ERROR ADD PERSON FACE", e.getMessage());
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return result;

	}

	public void trainByGroupId()
	{
		String groupId = DataObj.getGroupId(((MyApplication)context).getSharedPreferences());
		try
		{
			this.faceServiceRestClient.trainPersonGroup(groupId);
		}
		catch (ClientException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		TrainingStatus status = null;
		try
		{
			status = faceServiceRestClient.getPersonGroupTrainingStatus(groupId);
			Log.d("TrainingStatus 1-3", status.status.name());
		}
		catch (ClientException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

	}

	public String trainStatus()
	{
		TrainingStatus status = null;
		String groupId = DataObj.getGroupId(((MyApplication)context).getSharedPreferences());
		try
		{
			status = faceServiceRestClient.getPersonGroupTrainingStatus(groupId);
			//Log.d("TrainingStatus 2-1", status.status.name());
		}
		catch (ClientException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		//Log.d("TrainingStatus 2-3", status.status.name());
		return status.status.name();
	}



	public void deletePerson(UUID personId)
	{
		String groupId = DataObj.getGroupId(((MyApplication)context).getSharedPreferences());
		try
		{
			this.faceServiceRestClient.deletePerson(groupId, personId);
			DataObj.removePerson(((MyApplication)context).getSharedPreferences(), personId.toString());
			DataObj.listPersonWhiteListAction(((MyApplication)context).getSharedPreferences(), personId.toString(), DataObj.Action.DELETE);
		}
		catch (ClientException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public CreatePersonResult createPerson(String name)
	{
//		Person[] personExist = getPerson();
//		boolean exist = false;
//		for(Person person : personExist)
//		{
//			if(person.name.equalsIgnoreCase(name))
//			{
//				exist = true;
//				break;
//			}
//		}

		CreatePersonResult personResult = null;
		String groupId = DataObj.getGroupId(((MyApplication)context).getSharedPreferences());
		try
		{
			personResult = this.faceServiceRestClient.createPerson(groupId, name, null);
			Log.d("PERSON RESULT", personResult.personId.toString());
			DataObj.listPersonWhiteListAction(((MyApplication)context).getSharedPreferences(), personResult.personId.toString(), DataObj.Action.SAVE);
			DataObj.addPersonWhiteList(((MyApplication)context).getSharedPreferences(), personResult.personId.toString(), name);


		}
		catch (ClientException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		/*if(!exist)
		{
			try
			{
				personResult = this.faceServiceRestClient.createPerson(this.groupId, name, null);
				Log.d("PERSON RESULT", personResult.personId.toString());
				DataObj.listPersonWhiteListAction(((MyApplication)context).getSharedPreferences(), personResult.personId.toString(), DataObj.Action.SAVE);
				DataObj.addPersonWhiteList(((MyApplication)context).getSharedPreferences(), personResult.personId.toString(), name);


			}
			catch (ClientException e)
			{
				e.printStackTrace();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}*/
		return personResult;
	}

	public Person[] getPerson()
	{
		String groupId = DataObj.getGroupId(((MyApplication)context).getSharedPreferences());

		try
		{
			Person[] personList = this.faceServiceRestClient.listPersons(groupId);
			return personList;
		}
		catch (ClientException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return new Person[]{};
	}

	public ArrayList<Candidate> identificaizone(InputStream im)
	{
		//82eadd2f-e823-4a44-a545-4e1436487cf4
		//UUID[] faceIds = new UUID[]{UUID.fromString("64e28c2c-99ed-4f13-a612-68ad93b68448")};
		String groupId = DataObj.getGroupId(((MyApplication)context).getSharedPreferences());
		Face[] faces = null;
		ArrayList<Candidate> candidateList = new ArrayList<>();
		try
		{
			faces = this.faceServiceRestClient.detect(im, true, false, null);
//			Face[] faces = this.faceServiceRestClient.detect(im);
			UUID[] faceIds = new UUID[faces.length];
			for(int i = 0; i < faces.length; i++) faceIds[i] = faces[i].faceId;
 			IdentifyResult[] identifyResults = this.faceServiceRestClient.identity(groupId, faceIds,1);

			for(IdentifyResult identifyResult : identifyResults)
			{
				for(Candidate candidate : identifyResult.candidates)
				{
					candidateList.add(candidate);
					Log.d("PERSONE UUID", candidate.personId.toString());
				}
			}
		}
		catch (ClientException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return candidateList;
	}

	public Face[] detectFace(InputStream img)
	{
		Face[] faces = new Face[]{};
//		FaceServiceClient.FaceAttributeType[] faceAttributeTypes = new FaceServiceClient.FaceAttributeType[]{FaceServiceClient.FaceAttributeType.Age, FaceServiceClient.FaceAttributeType.Gender};
		try
		{

			faces =  this.faceServiceRestClient.detect(img,true,false, null /*faceAttributeTypes*/);
		}
		catch (ClientException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return faces;
	}

}
