package com.kevalpatel2106.sample;

import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import static android.support.v4.content.ContextCompat.startActivity;

public class RVAdapter extends RecyclerView.Adapter<RVAdapter.PersonViewHolder>
{

	ArrayList<PersonObj> personObjs;

	RVAdapter(ArrayList<PersonObj> personObjs)
	{
		super();
		this.personObjs = personObjs;
	}

	@Override
	public int getItemCount()
	{
		return personObjs.size();
	}

	@Override
	public PersonViewHolder onCreateViewHolder(ViewGroup viewGroup, int i)
	{

		View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item, viewGroup, false);
		PersonViewHolder pvh = new PersonViewHolder(v);
		return pvh;
	}

	@Override
	public void onBindViewHolder(final PersonViewHolder personViewHolder, final int i) {
		personViewHolder.personName.setText(personObjs.get(i).name);
		personViewHolder.personId.setText(personObjs.get(i).id);
		personViewHolder.personPhoto.setImageBitmap(personObjs.get(i).photo);

		personViewHolder.cv.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(v.getContext(), PersonActivity.class);
				intent.putExtra("name", personObjs.get(i).name);
				intent.putExtra("personId", personObjs.get(i).id);
				startActivity(v.getContext(), intent, null);
			}
		});
	}

	@Override
	public void onAttachedToRecyclerView(RecyclerView recyclerView) {
		super.onAttachedToRecyclerView(recyclerView);
	}

	public void updateData(ArrayList<PersonObj> list)
	{
		this.personObjs.clear();
		this.personObjs.addAll(list);
		notifyDataSetChanged();
	}

	public static class PersonViewHolder extends RecyclerView.ViewHolder {
		CardView cv;
		TextView personName;
		TextView personId;
		ImageView personPhoto;

		PersonViewHolder(View itemView) {
			super(itemView);
			cv = (CardView)itemView.findViewById(R.id.cv);
			personName = (TextView)itemView.findViewById(R.id.person_name);
			personId = (TextView)itemView.findViewById(R.id.person_id);
			personPhoto = (ImageView)itemView.findViewById(R.id.person_photo);
		}

	}
}