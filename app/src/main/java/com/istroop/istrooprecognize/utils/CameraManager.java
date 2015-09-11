package com.istroop.istrooprecognize.utils;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;

import com.istroop.istrooprecognize.WMDetectorThread;

import java.io.IOException;
import java.util.List;

/**
 * Created by joshua-zr on 8/18/15.
 */
public class CameraManager {
    private static int mPreviewHeight;
    private static int mPreviewWidth;

    public static Camera getCameraInstance() {
        Camera c;
        c = Camera.open();
        return c;
    }

    private static void selectMaxPreviewSize( Camera.Parameters params ) {
        List<Camera.Size> listSizes = params.getSupportedPreviewSizes();
        for ( int i = 0; i < listSizes.size(); i++ ) {
            Camera.Size size = listSizes.get( i );

            if ( mPreviewHeight < size.height ) {
                mPreviewHeight = size.height;
                mPreviewWidth = size.width;
            } else if ( mPreviewHeight == size.height ) {
                if ( mPreviewWidth < size.width )
                    mPreviewWidth = size.width;
            }
        }
    }

    private static final String TAG = CameraManager.class.getSimpleName();

    private static final int MIN_FRAME_WIDTH  = 240;
    private static final int MIN_FRAME_HEIGHT = 240;
    private static final int MAX_FRAME_WIDTH  = 1200;   // = 5/8 * 1920
    private static final int MAX_FRAME_HEIGHT = 675;    // = 5/8 * 1080

    private final Context                    context;
    private final CameraConfigurationManager configManager;
    private       Camera                     camera;
    private       AutoFocusManager           autoFocusManager;
    private       Rect                       framingRect;
    private       Rect                       framingRectInPreview;
    private       boolean                    initialized;
    private       boolean                    previewing;
    private int requestedCameraId = OpenCameraInterface.NO_REQUESTED_CAMERA;
    private int              requestedFramingRectWidth;
    private int              requestedFramingRectHeight;
    private Handler          handler;
    private WMDetectorThread wmDetectorThread;
    /**
     * Preview frames are delivered here, which we pass on to the registered handler. Make sure to
     * clear the handler so it will only receive one message.
     */
    private CameraPreview    previewCallback;

    public CameraManager( Context context, Handler handler, WMDetectorThread wmDetectorThread ) {
        this.context = context;
        this.handler = handler;
        this.configManager = new CameraConfigurationManager( context );
        this.wmDetectorThread = wmDetectorThread;
    }

    /**
     * Opens the camera driver and initializes the hardware parameters.
     *
     * @param holder The surface object which the camera will draw preview frames into.
     * @throws IOException Indicates the camera driver failed to open.
     */
    public synchronized void openDriver( SurfaceHolder holder ) throws IOException {
        Camera theCamera = camera;
        if ( theCamera == null ) {

            theCamera = OpenCameraInterface.open( requestedCameraId );
            if ( theCamera == null ) {
                throw new IOException();
            }
            camera = theCamera;
        }
        theCamera.setPreviewDisplay( holder );
        if ( !initialized ) {
            initialized = true;
            configManager.initFromCameraParameters( theCamera );
            if ( requestedFramingRectWidth > 0 && requestedFramingRectHeight > 0 ) {
                setManualFramingRect( requestedFramingRectWidth, requestedFramingRectHeight );
                requestedFramingRectWidth = 0;
                requestedFramingRectHeight = 0;
            }
        }
        Camera.Parameters parameters = theCamera.getParameters();
        String parametersFlattened = parameters.flatten(); // Save these, temporarily
        theCamera.setDisplayOrientation( 90 );
        selectMaxPreviewSize( parameters );
        parameters.setPreviewSize( mPreviewWidth, mPreviewHeight );
        Utils.log( TAG, "mWidth:" + mPreviewWidth + "   mHeight:" + mPreviewHeight, 6 );
        parameters.setPreviewFormat( ImageFormat.NV21 );
        theCamera.setParameters( parameters );
        try {
            configManager.setDesiredCameraParameters( theCamera, false );
        } catch ( RuntimeException re ) {
            // Driver failed
            Log.w( TAG, "Camera rejected parameters. Setting only minimal safe-mode parameters" );
            Log.i( TAG, "Resetting to saved camera params: " + parametersFlattened );
            // Reset:
            parameters = theCamera.getParameters();
            parameters.unflatten( parametersFlattened );
            try {
                theCamera.setParameters( parameters );
                configManager.setDesiredCameraParameters( theCamera, true );
            } catch ( RuntimeException re2 ) {
                // Well, darn. Give up
                Log.w( TAG, "Camera rejected even safe-mode parameters! No configuration" );
            }
        }
        previewCallback = new CameraPreview(
                mPreviewWidth, mPreviewHeight, wmDetectorThread );
    }

    public synchronized boolean isOpen() {
        return camera != null;
    }

    /**
     * Closes the camera driver if still in use.
     */
    public synchronized void closeDriver() {
        if ( camera != null ) {
            camera.release();
            camera = null;
            // Make sure to clear these each time we close the camera, so that any scanning rect
            // requested by intent is forgotten.
            framingRect = null;
            framingRectInPreview = null;
        }
    }

    /**
     * Asks the camera hardware to begin drawing preview frames to the screen.
     */
    public synchronized void startPreview() {
        Camera theCamera = camera;
        if ( theCamera != null && !previewing ) {
            theCamera.startPreview();
            previewing = true;
            autoFocusManager = new AutoFocusManager( context, camera );
            autoFocusManager.start();
            Log.w( TAG, "开始预览" );
        }
    }

    /**
     * Tells the camera to stop drawing preview frames.
     */
    public synchronized void stopPreview() {
        if ( autoFocusManager != null ) {
            autoFocusManager.stop();
            autoFocusManager = null;
        }
        if ( camera != null && previewing ) {
            camera.stopPreview();
            previewCallback.setHandler( null, 0 );
            previewing = false;
        }
    }

    /**
     * @param newSetting if {@code true}, light should be turned on if currently off. And vice versa.
     */
    public synchronized void setTorch( boolean newSetting ) {
        if ( newSetting != configManager.getTorchState( camera ) ) {
            if ( camera != null ) {
                if ( autoFocusManager != null ) {
                    autoFocusManager.stop();
                }
                configManager.setTorch( camera, newSetting );
                if ( autoFocusManager != null ) {
                    autoFocusManager.start();
                }
            }
        }
    }

    /**
     * A single preview frame will be returned to the handler supplied. The data will arrive as byte[]
     * in the message.obj field, with width and height encoded as message.arg1 and message.arg2,
     * respectively.
     */
    public synchronized void requestPreviewFrame() {
        Camera theCamera = camera;
        if ( theCamera != null && previewing ) {
//            previewCallback.setHandler( handler, message );
            theCamera.setOneShotPreviewCallback( previewCallback );
        }
    }

    /**
     * Calculates the framing rect which the UI should draw to show the user where to place the
     * barcode. This target helps with alignment as well as forces the user to hold the device
     * far enough away to ensure the image will be in focus.
     *
     * @return The rectangle to draw on screen in window coordinates.
     */
    public synchronized Rect getFramingRect() {
        if ( framingRect == null ) {
            if ( camera == null ) {
                return null;
            }
            Point screenResolution = configManager.getScreenResolution();
            if ( screenResolution == null ) {
                // Called early, before init even finished
                return null;
            }

            int width = findDesiredDimensionInRange( screenResolution.x, MIN_FRAME_WIDTH, MAX_FRAME_WIDTH );
            int height = findDesiredDimensionInRange( screenResolution.y, MIN_FRAME_HEIGHT, MAX_FRAME_HEIGHT );

            int leftOffset = ( screenResolution.x - width ) / 2;
            int topOffset = ( screenResolution.y - height ) / 2;
            framingRect = new Rect( leftOffset, topOffset, leftOffset + width, topOffset + height );
            Log.d( TAG, "Calculated framing rect: " + framingRect );
        }
        return framingRect;
    }

    private static int findDesiredDimensionInRange( int resolution, int hardMin, int hardMax ) {
        int dim = 5 * resolution / 8; // Target 5/8 of each dimension
        if ( dim < hardMin ) {
            return hardMin;
        }
        if ( dim > hardMax ) {
            return hardMax;
        }
        return dim;
    }

    /**
     * Like {@link #getFramingRect} but coordinates are in terms of the preview frame,
     * not UI / screen.
     *
     * @return {@link Rect} expressing barcode scan area in terms of the preview size
     */
    public synchronized Rect getFramingRectInPreview() {
        if ( framingRectInPreview == null ) {
            Rect framingRect = getFramingRect();
            if ( framingRect == null ) {
                return null;
            }
            Rect rect = new Rect( framingRect );
            Point cameraResolution = configManager.getCameraResolution();
            Point screenResolution = configManager.getScreenResolution();
            if ( cameraResolution == null || screenResolution == null ) {
                // Called early, before init even finished
                return null;
            }
            rect.left = rect.left * cameraResolution.x / screenResolution.x;
            rect.right = rect.right * cameraResolution.x / screenResolution.x;
            rect.top = rect.top * cameraResolution.y / screenResolution.y;
            rect.bottom = rect.bottom * cameraResolution.y / screenResolution.y;
            framingRectInPreview = rect;
        }
        return framingRectInPreview;
    }


    /**
     * Allows third party apps to specify the camera ID, rather than determine
     * it automatically based on available cameras and their orientation.
     *
     * @param cameraId camera ID of the camera to use. A negative value means "no preference".
     */
    public synchronized void setManualCameraId( int cameraId ) {
        requestedCameraId = cameraId;
    }

    /**
     * Allows third party apps to specify the scanning rectangle dimensions, rather than determine
     * them automatically based on screen resolution.
     *
     * @param width  The width in pixels to scan.
     * @param height The height in pixels to scan.
     */
    public synchronized void setManualFramingRect( int width, int height ) {
        if ( initialized ) {
            Point screenResolution = configManager.getScreenResolution();
            if ( width > screenResolution.x ) {
                width = screenResolution.x;
            }
            if ( height > screenResolution.y ) {
                height = screenResolution.y;
            }
            int leftOffset = ( screenResolution.x - width ) / 2;
            int topOffset = ( screenResolution.y - height ) / 2;
            framingRect = new Rect( leftOffset, topOffset, leftOffset + width, topOffset + height );
            Log.d( TAG, "Calculated manual framing rect: " + framingRect );
            framingRectInPreview = null;
        } else {
            requestedFramingRectWidth = width;
            requestedFramingRectHeight = height;
        }
    }

    private static boolean isFocusModeSupported( Camera.Parameters params, String focusMode ) {
        List<String> listModes = params.getSupportedFocusModes();
        for ( int i = 0; i < listModes.size(); i++ ) {
            if ( listModes.get( i ).equals( focusMode ) )
                return true;
        }
        return false;
    }

    public boolean flashModeSwitch() {
        if ( camera != null ) {
            Camera.Parameters mParam = camera.getParameters();
            if ( Camera.Parameters.FLASH_MODE_OFF.equals( mParam.getFlashMode() ) ) {
                Utils.log( TAG, "开启闪光灯", 5 );
                mParam.setFlashMode( Camera.Parameters.FLASH_MODE_TORCH );
                camera.setParameters( mParam );
                return true;
            } else if ( Camera.Parameters.FLASH_MODE_TORCH.equals( mParam.getFlashMode() ) ) {
                mParam.setFlashMode( Camera.Parameters.FLASH_MODE_OFF );
                Utils.log( TAG, "关闭闪光灯", 5 );
                camera.setParameters( mParam );
                return false;
            }
        }
        return false;
    }

}
