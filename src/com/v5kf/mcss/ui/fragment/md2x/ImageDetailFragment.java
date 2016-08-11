package com.v5kf.mcss.ui.fragment.md2x;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.easemob.chatuidemo.widget.photoview.PhotoViewAttacher;
import com.easemob.chatuidemo.widget.photoview.PhotoViewAttacher.OnPhotoTapListener;
import com.v5kf.mcss.R;
import com.v5kf.mcss.ui.activity.md2x.ShowImageGallaryActivity;
import com.v5kf.mcss.utils.cache.ImageLoader;

/**
 * 单张图片显示Fragment
 */
public class ImageDetailFragment extends Fragment {
	private String mImageUrl;
	private ImageView mImageView;
	private ProgressBar progressBar;
	private PhotoViewAttacher mAttacher;

	public static ImageDetailFragment newInstance(String imageUrl) {
		final ImageDetailFragment f = new ImageDetailFragment();

		final Bundle args = new Bundle();
		args.putString("url", imageUrl);
		f.setArguments(args);

		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mImageUrl = getArguments() != null ? getArguments().getString("url") : null;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.md2x_show_image_item, container, false);
		mImageView = (ImageView) v.findViewById(R.id.id_image);
		mAttacher = new PhotoViewAttacher(mImageView);

		mAttacher.setOnPhotoTapListener(new OnPhotoTapListener() {

			@Override
			public void onPhotoTap(View arg0, float arg1, float arg2) {
				getActivity().finish();
			}
		});
		mAttacher.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
			
			@Override
			public void onViewTap(View view, float x, float y) {
				getActivity().finish();
			}
		});
		mAttacher.setOnLongClickListener(new View.OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				((ShowImageGallaryActivity)getActivity()).showSaveImgOptionDialog(mImageUrl);
				return true;
			}
		});
		
		progressBar = (ProgressBar) v.findViewById(R.id.id_loading_progress);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		ImageLoader imgLoader = new ImageLoader(getActivity(), true, R.drawable.v5_img_src_loading, new ImageLoader.ImageLoaderListener() {
			
			@Override
			public void onSuccess(String url, ImageView imageView, Bitmap bmp) {
				mAttacher.update();
				progressBar.setVisibility(View.GONE);
			}
			
			@Override
			public void onFailure(ImageLoader imageLoader, String url,
					ImageView imageView) {
				mAttacher.update();
				progressBar.setVisibility(View.GONE);
			}
		});
    	imgLoader.DisplayImage(mImageUrl, mImageView);
	}
}
