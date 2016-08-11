package com.xic.master.picturechoose.sourcelibrary;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * ProjectName: PictureChoose
 * Describe: Image selector
 * Author: xiongjianchang
 * Date: 2016/3/16 10:07
 * Email: jianchang1230@163.com
 * QQ: 939635660
 * Copyright (c) 2016, *******.com All Rights Reserved.
 */
public class PictureChooser {

    private static final String TAG = "PictureChooser";
    /**
     * From the album to choose
     */
    private final int PIC_GALLERY_REQUESTCODE = 10101;
    /**
     * Open the camera take pictures
     */
    private final int PIC_CAMERA_REQUESTCODE = 10102;
    /**
     * Cut out pictures
     */
    private final int PIC_CLIP_REQUESTCODE = 10103;

    private Activity mContext;
    private Fragment mFragment;

    /**
     * Image name temp to default values
     */
    private String cameraPicName = "temp.jpg";
    private File tempFile;
    private String cameraFilePath;
    private String galleryFilePath;
    private String clipFilePath;
    private String fileDir = "pic";

    /**
     * Whether the cutting
     */
    private boolean isClip = false;

    /**
     * Whether the compression
     */
    private boolean isCompressor = false;
    /**
     * Cut the figure wide high percentage
     */
    private int aspectX = 1;
    private int aspectY = 1;
    /**
     * Wide high output images
     */
    private int outputX = 0;
    private int outputY = 0;
    /**
     * Compressed images maximum quality
     */
    private int maxkb = 0;
    /**
     * Wide high compressed images
     */
    private int reqWidth = 0;
    private int reqHeight = 0;
    /**
     * Record the timestamp
     */
    private long currentTimeMillis = 0;
    /**
     * Do more than 3 set cache
     */
    private int maxFile = 3;

    private OnPicturePickListener mOnPicturePickListener;

    /**
     * Image option
     */
    private PictureFrom mPictureFrom = PictureFrom.CAMERA;

    public PictureChooser(Fragment fragment) {
        this(null, fragment);
    }

    public PictureChooser(Context context) {
        this(context, null);
    }

    PictureChooser(Context context, Fragment fragment) {
        if (null == fragment && null == context) {
            throw new IllegalArgumentException("fragment == null && context == null");
        }
        if (null != fragment) {
            this.mFragment = fragment;
            this.mContext = mFragment.getActivity();
        } else {
            this.mContext = (Activity) context;
        }
        if (android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED)) {
            this.tempFile = new File(mContext.getExternalCacheDir().getPath() + File.separator + fileDir);
        } else {
            this.tempFile = new File(mContext.getCacheDir().getPath() + File.separator + fileDir);
        }
        tempFile.mkdirs();
        if (isHaseMaxFile()) {
            clearPics();
        }
    }

    public void setCameraPicName(String cameraPicName) {
        this.cameraPicName = cameraPicName;
    }

    public void setmPictureFrom(PictureFrom mPictureFrom) {
        this.mPictureFrom = mPictureFrom;
    }

    public void setIsClip(boolean isClip, int aspectX, int aspectY, int outputX, int outputY) {
        this.isClip = isClip;
        this.aspectX = aspectX;
        this.aspectY = aspectY;
        this.outputX = outputX;
        this.outputY = outputY;
    }

    public void setIsCompressor(boolean isCompressor, int maxkb, int reqWidth, int reqHeight) {
        this.isCompressor = isCompressor;
        this.maxkb = maxkb;
        this.reqWidth = reqWidth;
        this.reqHeight = reqHeight;
    }

    /**
     * Began to perform select image manipulation
     *
     * @param listener Perform event listening in
     */
    public void execute(OnPicturePickListener listener) {
        if (null == listener) {
            throw new NullPointerException("OnPicturePickListener == NULL");
        }
        this.mOnPicturePickListener = listener;
        if (mPictureFrom == PictureFrom.GALLERY) {
            galleryPic();
        } else {
            openCamera();
        }
    }

    /**
     * open camera
     */
    protected void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);//Take Picture
        cameraFilePath = tempFile.getPath() + File.separator + currentTimeMillis + cameraPicName;
        Uri uri = Uri.parse("file://" + cameraFilePath);
//        Uri uri = Uri.fromFile(new File(cameraFilePath));
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                | Intent.FLAG_GRANT_READ_URI_PERMISSION);
//        intent.setClipData(ClipData.newRawUri(null, uri));
//        intent.putExtra("return-data", true);
        intent.putExtra("return-data", false);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, PIC_CAMERA_REQUESTCODE);
    }

    private void startActivityForResult(Intent intent, int requestCode) {
        if (null != mFragment) {
            mFragment.startActivityForResult(intent, requestCode);
        } else {
            mContext.startActivityForResult(intent, requestCode);
        }
    }

    /**
     * The judgment of the cache file is overweight
     *
     * @return If more than a maximum cache true than false do not have
     */
    protected boolean isHaseMaxFile() {
        if (tempFile.isDirectory()) {
            if (null != tempFile.listFiles() && tempFile.listFiles().length >= maxFile) {
                // File[] files = tempFile.listFiles();
//                Log.i(TAG, tempFile.listFiles().length + "");
                return true;
            } else {
                return false;
            }
        } else {
            if (null != tempFile.getParentFile().listFiles() && tempFile.getParentFile().listFiles().length >= maxFile) {
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * wipe cache partition
     *
     * @return Clear the cache was successful
     */
    protected boolean clearPics() {
        boolean state = true;
        File directory = null;
        if (tempFile.isDirectory()) {
            directory = tempFile;
        } else {
            directory = tempFile.getParentFile();
        }
        File[] files = directory.listFiles();
        if (null != files) {
            for (File file : files) {
                state = state & file.delete();
            }
        }
        return state;
    }

    protected boolean delPic(String patch) {
        File file = new File(patch);
        return delPic(file);
    }

    /**
     * delete image
     *
     * @param file To delete pictures of address
     * @return Delete operation was successful
     */
    protected boolean delPic(File file) {
        boolean state = false;
        if (null == file) {
            return state;
        }
        if (!file.exists()) {
            return state;
        }
        return file.delete();
    }

    /**
     * From the album to choose
     */
    protected void galleryPic() {
        // Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);//picture
        Intent intent = new Intent(Intent.ACTION_PICK, null);//picture
        intent.setType("image/*");
        startActivityForResult(intent, PIC_GALLERY_REQUESTCODE);
    }

    /**
     * Cut The Photo
     *
     * @param uri To cut the figure file mapping address
     */
    private void startPhotoClip(Uri uri) {
        if (tempFile.isDirectory()) {
            clipFilePath = tempFile.getPath() + File.separator + System.currentTimeMillis() + cameraPicName;
        } else {
            clipFilePath = tempFile.getParent() + File.separator + System.currentTimeMillis() + cameraPicName;
        }
//        Log.i(TAG, "url" + clipFilePath);
        // Uri uri1 = Uri.parse(clipFilePath);
        Uri uri1 = Uri.fromFile(new File(clipFilePath));   //Some phones can only use fromFile processing, otherwise it will output become a picture
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // crop to true is display Settings in open the intent of the view can be cut
        intent.putExtra("crop", "true");

        // aspectX aspectY Is wide high proportion
        if (aspectX == aspectY && android.os.Build.MANUFACTURER.contains("HUAWEI")) {
            //HUAWEI special treatment will not show round
            intent.putExtra("aspectX", 9998);
            intent.putExtra("aspectY", 9999);
        } else if (aspectX > 0 && aspectY > 0) {
            intent.putExtra("aspectX", aspectX);
            intent.putExtra("aspectY", aspectY);
        } else if (aspectX == 0 && aspectY == 0) {
            //0 just don't do processing, cutting the proportion does not limit
        } else {
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
        }
        if (outputX > 0 && outputY > 0) {
            // outputX,outputY Wide, high is cut images
            intent.putExtra("outputX", outputX);
            intent.putExtra("outputY", outputY);
        }
        intent.putExtra("return-data", false);//passed
        //callback method       data.getExtras().getParcelable("data") false Return the data is empty
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri1);//image output
        startActivityForResult(intent, PIC_CLIP_REQUESTCODE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            /**
             * camera
             */
            case PIC_CAMERA_REQUESTCODE:
                if (resultCode == Activity.RESULT_OK) {
                    if (tempFile.isDirectory()) {
                        cameraFilePath = tempFile.getPath() + File.separator + currentTimeMillis + cameraPicName;
                    }
                    if (isClip) {
                        Uri uri1 = Uri.parse("file://" + cameraFilePath);
                        //Uri uri1 = Uri.fromFile(new File(cameraFilePath));
                        startPhotoClip(uri1);
                    } else {
                        senCompressorFile(cameraFilePath);
                    }
                }
                break;
            /**
             * Photo album to choose
             */
            case PIC_GALLERY_REQUESTCODE:
                if (data != null) {
                    //Returns the Uri of the, basically choose photos when the returned in the form of a Uri, but in the photo you machine Uri is empty, so it is important to pay special attention to
                    Uri uri = data.getData();
                    //Returns the Uri is not null, then image information data will be obtained in the Uri. If is empty, then we will get the following way
                    if (uri != null) {
                        if (isClip) {
                            startPhotoClip(uri);
                        } else {
                            galleryFilePath = getRealPathFromURI(uri);
                            senCompressorFile(galleryFilePath);
                        }
                    } else {
                        Bundle extras = data.getExtras();
                        if (extras != null) {
                            //Here are some pictures after the image is directly deposited into the Bundle so we can get Bitmap images from there
                            Bitmap image = extras.getParcelable("data");
                        }
                    }

                }
                break;
            /**
             * After scaling
             */
            case PIC_CLIP_REQUESTCODE:
                if (null != data || resultCode == Activity.RESULT_OK) {
                    if (null == clipFilePath) {
                        clipFilePath = getRealPathFromURI(data.getData());
//                        Log.i(TAG, clipFilePath);
                    }
//                    Log.i(TAG, "clipFilePath " + clipFilePath);
                    File endFile = new File(clipFilePath);
//                    Log.i(TAG, "file size" + endFile.length());
                    senCompressorFile(clipFilePath);
                }
                break;
        }
    }

    /**
     * data.getData() uri Turn the real path
     *
     * @param contentUri Want to convert the address mapping
     * @return The converted file path
     */
    public String getRealPathFromURI(Uri contentUri) {
        String res = contentUri.getPath();
//        Log.e(TAG, contentUri.getPath());
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = mContext.getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
            cursor.close();
        }
        return res;
    }

    /**
     * For image compression processing
     *
     * @param filePath Picture address
     */
    protected void senCompressorFile(final String filePath) {
//        Log.i(TAG, filePath);
        senFile(filePath);
        if (isCompressor) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    fileCompressor(filePath);
                }
            }).start();
        }
    }


    /**
     * Calculate the scale value of the picture
     *
     * @param options   Scaling parameter
     * @param reqWidth  wide
     * @param reqHeight high
     * @return ratio
     */
    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

    /**
     * Size of the compression
     * According to the path to obtain images and compression, return to the bitmap is used to display
     *
     * @param filePath  Picture address
     * @param reqWidth  Wide compressed images
     * @param reqHeight High compressed images
     * @return Compressed images
     */
    public Bitmap getSmallBitmap(String filePath, int reqWidth, int reqHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }

    /**
     * Size of the compression
     * According to the path to obtain images and compression, return to the bitmap is used to display
     *
     * @param filePath     Picture address
     * @param inSampleSize compression ratio
     * @return Compressed images
     */
    public Bitmap getSmallBitmap(String filePath, int inSampleSize) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inPreferredConfig = Bitmap.Config.RGB_565;

        BitmapFactory.decodeFile(filePath, options);
        // Calculate inSampleSize
        options.inSampleSize = inSampleSize;
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }

    /**
     * quality compression
     *
     * @param image    To compress the file
     * @param maxkb    Maximum size after compression
     * @param filePath Compressed file storage address
     */
    public void compressBitmap(Bitmap image, int maxkb, String filePath) {
//        Log.i(TAG, "The original size of the bitmap" + getBitmapSize(image));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);// Quality compression method, 100 said here without compression, the compressed data stored in the baos
        int options = 100;
//        Log.i(TAG, "original size" + baos.toByteArray().length);
        while (baos.toByteArray().length / 1024 > maxkb) { // Cycle to judge if the compressed image is greater than 50 KB (maxkb), greater than continue to compress
//            Log.i(TAG, "A compression!  options " + options + "  " + baos.toByteArray().length);
            baos.reset();// Reset the baos namely empty baos
            options -= 10;// Reduce 10 every time
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);// Compression options here %, the compressed data stored in the baos
        }
//        Log.i(TAG, "Compression parameter options" + options);
//        Log.i(TAG, "Compressed size" + baos.toByteArray().length);
//        Log.i(TAG, "After the compression bitmap size" + getBitmapSize(image));
//        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// The compressed data baos deposit into a ByteArrayInputStream
//        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);// The ByteArrayInputStream data generated images
//        File file = cf(options, bitmap);
        bitmapToFile(options, image, filePath);
        // If the picture is not recycled, compulsory recycling
        if (!image.isRecycled()) {
            // Log.i(TAG, "recycled image");
            image.recycle();
        }
    }

    /**
     * The bitmap size using
     *
     * @param bitmap To calculate the image files
     * @return The quality of the image size
     */
    public int getBitmapSize(Bitmap bitmap) {
        if (null == bitmap)
            return -1;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {    //API 19
            return bitmap.getAllocationByteCount();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {//API 12
            return bitmap.getByteCount();
        }
        return bitmap.getRowBytes() * bitmap.getHeight();                //earlier version
    }

    /**
     * Bitmap directly compressed into a file
     *
     * @param options  The threshold of compression
     * @param bitmap   To compress the image
     * @param filePath Compressed file storage address
     */
    private void bitmapToFile(int options, Bitmap bitmap, String filePath) {
        try {
            FileOutputStream fos = null;
            fos = new FileOutputStream(filePath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, options, fos);
//            Log.i(TAG, filePath);
            fos.flush();
            fos.close();
            //Compression success callback
            compressorSuccess(filePath);
            // If the picture is not recycled, compulsory recycling
            if (!bitmap.isRecycled()) {
                // Log.i(TAG, "recycled bitmap");
                bitmap.recycle();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * The bitmap is transformed into a String
     *
     * @param filePath  Picture address
     * @param reqWidth  The transformed image width
     * @param reqHeight The transformed image is high
     * @return A 64 - bit image files
     */
    public String bitmapToString(String filePath, int reqWidth, int reqHeight) {
        Bitmap bm = getSmallBitmap(filePath, reqWidth, reqHeight);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 40, baos);
        byte[] b = baos.toByteArray();
        // If the picture is not recycled, compulsory recycling
        if (!bm.isRecycled()) {
            // Log.i(TAG, "recycled bm");
            bm.recycle();
        }
        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    /**
     * The image file compression
     *
     * @param filePath Picture address
     */
    public void fileCompressor(String filePath) {
        File endFile = new File(filePath);
//        Log.i(TAG, "file size" + endFile.length());
        if (0 == maxkb) {
            bitmapToFile(100, getSmallBitmap(filePath, reqWidth, reqHeight), filePath);
        } else if (0 == reqWidth || 0 == reqHeight) {
            try {
                Bitmap bit = BitmapFactory.decodeFile(filePath);
                if (bit != null) {
                    compressBitmap(bit, maxkb, filePath);
                }
            } catch (OutOfMemoryError oom) {
                oom.printStackTrace();
                compressBitmap(getSmallBitmap(filePath, 4), maxkb, filePath);
            }
        } else {
            compressBitmap(getSmallBitmap(filePath, reqWidth, reqHeight), maxkb, filePath);
        }
    }

    /**
     * Reset the selection data
     */
    public void reset() {
        this.cameraPicName = "temp.jpg";
        this.isClip = false;
        this.isCompressor = false;
        this.mPictureFrom = PictureFrom.GALLERY;
        this.aspectX = 1;
        this.aspectY = 1;
        this.outputX = 0;
        this.outputY = 0;
        this.maxkb = 0;
        this.reqWidth = 0;
        this.reqHeight = 0;
    }

    /**
     * Get photo after the trigger
     *
     * @param filePath Picture address
     */
    protected void senFile(String filePath) {
        if (null != mOnPicturePickListener) {
            mOnPicturePickListener.senFile(filePath);
        }
    }

    /**
     * Compressed trigger
     *
     * @param filePath Picture address
     */
    protected void compressorSuccess(String filePath) {
        if (null != mOnPicturePickListener) {
            mOnPicturePickListener.compressorSuccess(filePath);
        }
    }


    public interface OnPicturePickListener {

        void senFile(String filePath);

        void compressorSuccess(String filePath);

    }

    public enum PictureFrom {
        GALLERY, CAMERA
    }

    public static class Builder {

    }
}
