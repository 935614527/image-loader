package com.novoda.imageloader.core.cache;

import android.graphics.Bitmap;

public interface CacheManager {

	boolean hasBitmap(String url);

	Bitmap get(String url);

	void put(String url, Bitmap bmp);

	void clean();
	
}