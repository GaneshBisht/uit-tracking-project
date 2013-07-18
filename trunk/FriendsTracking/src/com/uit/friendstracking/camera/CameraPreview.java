package com.uit.friendstracking.camera;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

	private SurfaceHolder mHolder;
	private Camera camera;
	private Context context;

	@SuppressWarnings("deprecation")
	public CameraPreview(Context context) {
		super(context);
		this.context = context;

		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

	}

	public void surfaceCreated(SurfaceHolder holder) {
		camera = Camera.open();
		try {
			camera.setPreviewDisplay(holder);
		} catch (IOException e) {
			Toast.makeText(context, "Error while trying to sets the surface holder: " + e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		camera.stopPreview();
		camera.release();
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		Camera.Parameters parameters = camera.getParameters();
		List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
		parameters.setPreviewSize(previewSizes.get(0).width, previewSizes.get(0).height);
		camera.setParameters(parameters);

		camera.startPreview();
	}

	public Camera getCamera() {
		return camera;
	}

}
