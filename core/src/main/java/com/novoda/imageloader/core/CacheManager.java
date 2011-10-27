package com.novoda.imageloader.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import android.app.Activity;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.novoda.imageloader.core.cache.ImageCache;
import com.novoda.imageloader.core.exception.ImageNotFoundException;

public class CacheManager {

  private static final String TAG = "ImageLoader";

  private ImageCache cache;
  private PhotosLoader thread;
  private Stack<Image> stack;
  private List<String> notFoundImages;
  private ImageManager imageLoader;

  public static class Image {
    public String url;
    public ImageView imageView;

    public Image(String u, ImageView i) {
      url = u;
      imageView = i;
    }
  }

  public CacheManager(ImageManager imageLoader, ImageCache cache) {
    this.imageLoader = imageLoader;
    this.cache = cache;
    this.stack = new Stack<Image>();
    this.notFoundImages = new ArrayList<String>();
    thread = new PhotosLoader();
    thread.setPriority(Thread.NORM_PRIORITY - 1);
  }

  public void push(Image p) {
    p.imageView.setImageBitmap(cache.getDefaultImage());
    if (TextUtils.isEmpty(p.url)) {
      return;
    }
    pushOnStack(p);
    if (thread.getState() == Thread.State.NEW) {
      thread.start();
    } else {
      synchronized (thread) {
        if (thread.isWaiting) {
          try {
            thread.isWaiting = false;
            thread.notify();
          } catch (Exception ie) {
            Log.e(TAG, "Check and resume the thread " + ie.getMessage());
          }
        }
      }
    }
  }

  public synchronized int size() {
    return stack.size();
  }

  public synchronized Image pop() {
    Image i = stack.pop();
    return i;
  }

  private synchronized void clean(Image p) {
    for (int j = 0; j < stack.size(); j++) {
      if (stack.get(j).url != null && stack.get(j).url.equals(p.url)) {
        stack.remove(j);
        j--;
      }
    }
  }

  private synchronized void pushOnStack(Image p) {
    stack.push(p);
  }

  private class PhotosLoader extends Thread {
    boolean isWaiting = false;

    @Override
    public void run() {
      while (true) {
        pauseThreadIfnecessary();
        Image image = stack.pop();
        loadAndShowImage(image);
      }
    }

    private void pauseThreadIfnecessary() {
      if (stack.size() == 0) {
        synchronized (this) {
          try {
            isWaiting = true;
            wait();
          } catch (Exception e) {
            Log.v(TAG, "Pausing the thread error " + e.getMessage());
          }
        }
      }
    }

    private void loadAndShowImage(Image image) {
      try {
        Bitmap bmp = imageLoader.getBitmap(image.url, true);
        if (bmp == null) {
          bmp = cache.getDefaultImage();
          clean(image);
        } else {
          cache.put(image.url, bmp);
        }
        displayImage(image, bmp);
      } catch(ImageNotFoundException inf) {
        notFoundImages.add(image.url);
      } catch (Throwable e) {
        Log.e(TAG, "Throwable : " + e.getMessage(), e);
      }
    }

    private void displayImage(Image image, Bitmap bmp) {
      if (image.imageView.getTag() != null
          && ((String) image.imageView.getTag()).equals(image.url)) {
        BitmapDisplayer bd = new BitmapDisplayer(bmp, image.imageView);
        Activity a = (Activity) image.imageView.getContext();
        a.runOnUiThread(bd);
      }
    }
  }

  private class BitmapDisplayer implements Runnable {
    private Bitmap bitmap;
    private ImageView imageView;

    public BitmapDisplayer(Bitmap b, ImageView i) {
      bitmap = b;
      imageView = i;
    }

    @Override
    public void run() {
      if (bitmap != null) {
        imageView.setImageBitmap(bitmap);
      } else {
        imageView.setImageBitmap(cache.getDefaultImage());
      }
    }
  }

  public boolean hasImageInCache(String url) {
    if(notFoundImages.contains(url)) {
      return true;
    }
    return cache.hasImage(url);
  }

  public Bitmap getImageFromCache(String url) {
    if(notFoundImages.contains(url)) {
      return cache.getNotFoundImage();
    }
    return cache.get(url);
  }

  public void resetCache(ImageCache cache) {
    this.cache.clean();
  }
  
  public Bitmap getDefaultImage() {
  	return cache.getDefaultImage();
  }

}
