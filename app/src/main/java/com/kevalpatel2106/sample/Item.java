package com.kevalpatel2106.sample;

import android.graphics.Bitmap;

public class Item {
	Bitmap image;

	public Item(Bitmap image) {
		super();
		this.image = image;
	}
	public Bitmap getImage() {
		return image;
	}
	public void setImage(Bitmap image) {
		this.image = image;
	}
}