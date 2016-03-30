package com.xic.master.picturechoose.sourcelibrary;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * ProjectName: PictureChoose
 * Describe: 图片选择器
 * Author: 熊建昌
 * Date: 2016/3/16 10:07
 * Email: jianchang1230@163.com
 * QQ: 939635660
 * Copyright (c) 2016, *******.com All Rights Reserved.
 */
public class PictureChooser {

    private static final String TAG = "PictureChooser";
    /**
     * 从相册选择
     */
    private final int PIC_GALLERY_REQUESTCODE = 10101;
    /**
     * 打开相机拍照
     */
    private final int PIC_CAMERA_REQUESTCODE = 10102;
    /**
     * 裁剪图片
     */
    private final int PIC_CLIP_REQUESTCODE = 10103;

    private Activity mContext;
    private Fragment mFragment;

    /**
     * 图片名字   temp为默认值
     */
    private String cameraPicName = "temp.jpg";
    private File tempFile;
    private String cameraFilePath;
    private String galleryFilePath;
    private String clipFilePath;
    private String fileDir = "pic";

    /**
     * 是否裁切
     */
    private boolean isClip = false;

    /**
     * 是否压缩
     */
    private boolean isCompressor = false;
    /**
     * 切图 宽高比例
     */
    private int aspectX = 1;
    private int aspectY = 1;
    /**
     * 输出图片宽高
     */
    private int outputX = 0;
    private int outputY = 0;
    /**
     * 压缩图片最大质量
     */
    private int maxkb = 0;
    /**
     * 压缩图片宽高
     */
    private int reqWidth = 0;
    private int reqHeight = 0;
    /**
     * 记录时间戳
     */
    private long currentTimeMillis = 0;
    /**
     * 设置缓存做多3张
     */
    private int maxFile = 3;

    private OnPicturePickListener mOnPicturePickListener;

    /**
     * 图片选择方式
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
     * 开始执行选择图片操作
     *
     * @param listener 执行事件的监听
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
     * 打开相机
     */
    protected void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);//拍摄照片
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
     * 判断缓存的文件是不是超标了
     *
     * @return 是否超过缓存最大值 ture超过 false没有
     */
    protected boolean isHaseMaxFile() {
        if (tempFile.isDirectory()) {
            if (null != tempFile.listFiles() && tempFile.listFiles().length >= maxFile) {
                // File[] files = tempFile.listFiles();
                Log.i(TAG, tempFile.listFiles().length + "");
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
     * 清除缓存
     *
     * @return   清除缓存是否成功
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
     * 删除图片
     *
     * @param file 要删除的图片地址
     * @return 删除操作是否成功
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
     * 从相册选择
     */
    protected void galleryPic() {
        // Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);//照片
        Intent intent = new Intent(Intent.ACTION_PICK, null);//照片
        intent.setType("image/*");
        startActivityForResult(intent, PIC_GALLERY_REQUESTCODE);
    }

    /**
     * 切图
     *
     * @param uri 要切图的文件映射地址
     */
    private void startPhotoClip(Uri uri) {
        if (tempFile.isDirectory()) {
            clipFilePath = tempFile.getPath() + File.separator + System.currentTimeMillis() + cameraPicName;
        } else {
            clipFilePath = tempFile.getParent() + File.separator + System.currentTimeMillis() + cameraPicName;
        }
        Log.i(TAG, "url" + clipFilePath);
        // Uri uri1 = Uri.parse(clipFilePath);
        Uri uri1 = Uri.fromFile(new File(clipFilePath));   //一些手機只能用fromFile處理，否則會輸出生成不了圖片
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // crop为true是设置在开启的intent中设置显示的view可以剪裁
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        if (aspectX > 0 && aspectY > 0) {
            intent.putExtra("aspectX", aspectX);
            intent.putExtra("aspectY", aspectY);
        } else if (aspectX == 0 && aspectY == 0) {
            //0就不做处理，切割比例不限制
        } else {
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
        }
        if (outputX > 0 && outputY > 0) {
            // outputX,outputY 是剪裁图片的宽高
            intent.putExtra("outputX", outputX);
            intent.putExtra("outputY", outputY);
        }
        intent.putExtra("return-data", false);//是否通过//回调方法data.getExtras().getParcelable("data") false返回数据为空
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri1);//图像输出
        startActivityForResult(intent, PIC_CLIP_REQUESTCODE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            /**
             * 相机
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
             * 相册选择
             */
            case PIC_GALLERY_REQUESTCODE:
                if (data != null) {
                    //取得返回的Uri,基本上选择照片的时候返回的是以Uri形式，但是在拍照中有得机子呢Uri是空的，所以要特别注意
                    Uri uri = data.getData();
                    //返回的Uri不为空时，那么图片信息数据都会在Uri中获得。如果为空，那么我们就进行下面的方式获取
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
                            //这里是有些拍照后的图片是直接存放到Bundle中的所以我们可以从这里面获取Bitmap图片
                            Bitmap image = extras.getParcelable("data");
                        }
                    }

                }
                break;
            /**
             * 缩放后
             */
            case PIC_CLIP_REQUESTCODE:
                if (null != data || resultCode == Activity.RESULT_OK) {
                    if (null == clipFilePath) {
                        clipFilePath = getRealPathFromURI(data.getData());
                        Log.i(TAG, clipFilePath);
                    }
                    Log.i(TAG, "clipFilePath " + clipFilePath);
                    File endFile = new File(clipFilePath);
                    Log.i(TAG, "file大小" + endFile.length());
                    senCompressorFile(clipFilePath);
                }
                break;
        }
    }

    /**
     * data.getData() uri 转真实路径
     *
     * @param contentUri 要转换的映射地址
     * @return 转换后的文件路径
     */
    public String getRealPathFromURI(Uri contentUri) {
        String res = contentUri.getPath();
        Log.e(TAG, contentUri.getPath());
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
     * 获取图片压缩处理
     *
     * @param filePath 图片地址
     */
    protected void senCompressorFile(final String filePath) {
        Log.i(TAG, filePath);
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
     * 计算图片的缩放值
     * @param options 缩放参数
     * @param reqWidth 宽
     * @param reqHeight 高
     * @return 比例
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
     * 尺寸压缩
     * 根据路径获得图片并压缩，返回bitmap用于显示
     * @param filePath 图片地址
     * @param reqWidth 压缩后的图片宽
     * @param reqHeight 压缩后的图片高
     * @return 压缩后的图片
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
     * 尺寸压缩
     * 根据路径获得图片并压缩，返回bitmap用于显示
     * @param filePath 图片地址
     * @param inSampleSize 压缩比例
     * @return 压缩后的图片
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
     * 质量压缩
     * @param image 要压缩的文件
     * @param maxkb 压缩后最大大小
     * @param filePath 压缩后文件存放地址
     */
    public void compressBitmap(Bitmap image, int maxkb, String filePath) {
        Log.i(TAG, "原始bitmap大小" + getBitmapSize(image));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        Log.i(TAG, "原始大小" + baos.toByteArray().length);
        while (baos.toByteArray().length / 1024 > maxkb) { // 循环判断如果压缩后图片是否大于(maxkb)50kb,大于继续压缩
            Log.i(TAG, "压缩一次!  options " + options + "  " + baos.toByteArray().length);
            baos.reset();// 重置baos即清空baos
            options -= 10;// 每次都减少10
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
        }
        Log.i(TAG, "压缩参数options" + options);
        Log.i(TAG, "压缩后大小" + baos.toByteArray().length);
        Log.i(TAG, "压缩后bitmap大小" + getBitmapSize(image));
//        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// 把压缩后的数据baos存放到ByteArrayInputStream中
//        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);// 把ByteArrayInputStream数据生成图片
//        File file = cf(options, bitmap);
        bitmapToFile(options, image, filePath);
        // 如果图片还没有回收，强制回收
        if (!image.isRecycled()) {
            Log.i(TAG, "回收了image");
            image.recycle();
        }
    }

    /**
     * 获取bitmap大小
     *
     * @param bitmap 要计算的图片文件
     * @return 图片的质量大小
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
     * 直接bitmap压缩成文件
     * @param options 压缩的阈值
     * @param bitmap 要压缩的图片
     * @param filePath 压缩后文件存放地址
     */
    private void bitmapToFile(int options, Bitmap bitmap, String filePath) {
        try {
            FileOutputStream fos = null;
            fos = new FileOutputStream(filePath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, options, fos);
            Log.i(TAG, filePath);
            fos.flush();
            fos.close();
            //压缩成功回调
            compressorSuccess(filePath);
            // 如果图片还没有回收，强制回收
            if (!bitmap.isRecycled()) {
                Log.i(TAG, "回收了bitmap");
                bitmap.recycle();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 把bitmap转换成String
     * @param filePath 图片地址
     * @param reqWidth 转换后的图片宽
     * @param reqHeight 转换后的图片高
     * @return 64位的图片文件
     */
    public String bitmapToString(String filePath, int reqWidth, int reqHeight) {
        Bitmap bm = getSmallBitmap(filePath, reqWidth, reqHeight);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 40, baos);
        byte[] b = baos.toByteArray();
        // 如果图片还没有回收，强制回收
        if (!bm.isRecycled()) {
            Log.i(TAG, "回收了bm");
            bm.recycle();
        }
        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    /**
     * 把图片文件压缩
     * @param filePath 图片地址
     */
    public void fileCompressor(String filePath) {
        File endFile = new File(filePath);
        Log.i(TAG, "file大小" + endFile.length());
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
     * 重置选择数据
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
     * 获取图片后触发
     *
     * @param filePath 图片地址
     */
    protected void senFile(String filePath) {
        if (null != mOnPicturePickListener) {
            mOnPicturePickListener.senFile(filePath);
        }
    }

    /**
     * 压缩后触发
     * @param filePath 图片地址
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
}
