package com.webssa.guestbest.utils;

import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.webssa.library.utils.DigestOutputStream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.Hashtable;
import java.util.UUID;

/**
 * Created by stanislav.perchenko on 3/30/2016.
 */
public class FileUtils {

    /******************  Define and access cache directories for the application  *****************/



    /**
     * @param ctx
     * @param path relative path against cache directory.
     * @return
     */
    public static File getFinalCacheDirectory(Context ctx, String path) throws IOException {
        File dir = getDiskCacheDir(ctx, path);
        return createDirIfNeeded(dir);
    }

    public static File getInternalCacheDirectory(Context ctx, String path) throws IOException {
        File dir = new File(ctx.getCacheDir(), path);
        return createDirIfNeeded(dir);
    }

    /**
     * Get a usable cache directory (external if available, internal otherwise).
     *
     * @param context    The context to use
     * @param uniqueName A unique directory name to append to the cache dir
     * @return The cache dir
     */
    public static File getDiskCacheDir(Context context, String uniqueName) {
        // Check if media is mounted or storage is built-in, if so, try and use
        // external cache dir
        // otherwise use internal cache dir
        boolean useExternalMemory = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || !isExternalStorageRemovable();
        File cacheFolder;
        if (useExternalMemory) {
            cacheFolder = getExternalCacheDir(context);
        } else {
            try {
                cacheFolder = context.getCacheDir();
            } catch (Exception e) {
                cacheFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS); // Lastly, try this as a last ditch effort
            }
        }

        return TextUtils.isEmpty(uniqueName) ? cacheFolder : new File(cacheFolder.getPath() + File.separator + uniqueName);
    }

    /**
     * Get the external app cache directory.
     *
     * @param context The context to use
     * @return The external cache dir
     */
    @TargetApi(8)
    public static File getExternalCacheDir(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            return context.getExternalCacheDir();
        }

        // Before Froyo we need to construct the external cache dir ourselves
        final String cacheDir = "/Android/data/" + context.getPackageName() + "/cache/";
        return new File(Environment.getExternalStorageDirectory().getPath() + cacheDir);
    }

    /**
     * Check if external storage is built-in or removable.
     *
     * @return True if external storage is removable (like an SD card), false
     * otherwise.
     */
    @TargetApi(9)
    public static boolean isExternalStorageRemovable() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD || Environment.isExternalStorageRemovable();
    }

    /**
     * Creates a directory and puts a .nomedia file in it
     *
     * @param dir
     * @return new dir
     * @throws IOException
     */
    private static File createDirIfNeeded(File dir) throws IOException {
        if ((dir != null) && !dir.exists()) {
            if (!dir.mkdirs() && !dir.isDirectory()) {
                throw new IOException("error create directory");
            }
            File noMediaFile = new File(dir, ".nomedia");
            noMediaFile.createNewFile();
        }
        return dir;
    }
    //=====================================================================================================================


    public static long calculateDiskCacheSize(File dir, float allowPart, int minSize, int maxSize) {
        long size = minSize;

        try {
            StatFs statFs = new StatFs(dir.getAbsolutePath());
            long available = ((long) statFs.getBlockCount()) * statFs.getBlockSize();
            // Target 2% of the total space.
            size = Math.round(available * allowPart);
        } catch (IllegalArgumentException ignored) {
        }

        // Bound inside min/max size for disk cache.
        return Math.max(Math.min(size, maxSize), minSize);
    }

    /**
     * Returns File with UUID-type name which does not exist in the provided directory
     * @param directory
     * @return
     */
    public static @Nullable
    File getNewRandomFile(@Nullable File directory) {
        if ((directory == null) || !directory.isDirectory()) {
            return null;
        }

        File f;
        do {
            f = new File(directory, UUID.randomUUID().toString());
        } while (f != null && f.exists());
        return f;
    }



    public static String optExtensionFromFile(String path, String defaultValue) {
        try {
            return path.substring(path.lastIndexOf('.'));
        } catch (Exception e) {
            return defaultValue;
        }
    }


    /**
     * Converts byte value to String.
     */
    public static String toReadableFileSize(long size) {
        if (size <= 0) return "0KB";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }


    /************************   Files/Resources copy helpers   ************************************/
    /**
     *
     * @param sourceFile
     * @param destFile
     * @param maxSize
     * @return Number of bytes copied
     * @throws IOException
     */
    public static long copyFileToFile(File sourceFile, File destFile, long maxSize) throws IOException {
        if (sourceFile == null) return 0;
        if(!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;
        FileInputStream fis = null;
        FileOutputStream fos = null;

        try {
            fis = new FileInputStream(sourceFile);
            fos = new FileOutputStream(destFile);
            source = fis.getChannel();
            destination = fos.getChannel();
            long count = 0;
            long size = source.size();
            if (maxSize > 0 & size > maxSize) size = maxSize;
            while((count += destination.transferFrom(source, count, size-count)) < size);
            return count;
        } finally {
            if(source != null) {
                source.close();
            }
            if (fis != null) {
                fis.close();
            }
            if(destination != null) {
                destination.close();
            }
            if (fos != null) {
                fos.close();
            }
        }
    }

    /**
     *
     * @param manager
     * @param assetName relative file name of an asset to copy (relative to Assets folder)
     * @param dstFile   destination file in the filesystem
     */
    public static void copyAssetToFile(AssetManager manager, String assetName, File dstFile) throws IOException {
        InputStream input = null;
        try {
            input = manager.open(assetName);
            streamToFile(dstFile, input);
        } finally {
            if (input != null) input.close();
        }
    }


    public static void streamToFile(File dstFile, InputStream src) throws IOException {
        OutputStream output = null;
        try {
            output = new BufferedOutputStream(new FileOutputStream(dstFile));
            byte data[] = new byte[1024];
            int count;

            while ((count = src.read(data)) != -1) {
                output.write(data, 0, count);
            }
            output.flush();
        } finally {
            if (output != null) output.close();
        }
    }


    /**
     *
     * @param toFile
     * @param uri
     * @param contentResolver We need this to get ContentResolver if Uri scheme is "content://"
     * @return
     */
    public static boolean saveUriToFile(Uri uri, File toFile, ContentResolver contentResolver) {
        if (toFile.exists()) {
            toFile.delete();
        }
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            if (uri.toString().startsWith("content:")) {
                bis = new BufferedInputStream(contentResolver.openInputStream(uri));
            } else {
                bis = new BufferedInputStream(new FileInputStream(uri.getPath()));
            }
            bos = new BufferedOutputStream(new FileOutputStream(toFile));
            byte[] buf = new byte[1024];
            bis.read(buf);
            do {
                bos.write(buf);
            } while (bis.read(buf) != -1);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (bis != null) bis.close();
                if (bos != null) bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static byte[] saveUriToFileWithDigest(Uri uri, File toFile, ContentResolver contentResolver, String digestAlgorithm) throws IOException {
        if (toFile.exists()) {
            toFile.delete();
        }

        OutputStream finOs = null;
        InputStream iStream = null;

        try {
            DigestOutputStream digestOs = new DigestOutputStream(new FileOutputStream(toFile), MessageDigest.getInstance(digestAlgorithm));
            finOs = new BufferedOutputStream(digestOs);

            String uri_str = uri.toString().toLowerCase();
            if (uri_str.startsWith("content:")) {
                iStream = new BufferedInputStream(contentResolver.openInputStream(uri));
            } else if (uri_str.startsWith("file:")) {
                iStream = new BufferedInputStream(new FileInputStream(uri.getPath()));
            } else {
                throw new IOException("Uri not supported - "+uri_str);
            }

            byte[] transBuff = new byte[2048];
            int nBytes;
            while ((nBytes = iStream.read(transBuff)) > 0) {
                finOs.write(transBuff, 0, nBytes);
            }
            finOs.flush();
            return digestOs.getDigestBytes();
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("Hash algorithm not supported - " + digestAlgorithm);
        } finally {
            try {if (finOs != null) finOs.close();} catch(IOException e) {}
            try {if (iStream != null) iStream.close();} catch(IOException e) {}
        }
    }


    public static void saveToFile(File file, String data) throws IOException {
        File tempFile = File.createTempFile("sendbird", "temp");
        FileOutputStream fos = new FileOutputStream(tempFile);
        fos.write(data.getBytes());
        fos.close();

        if(!tempFile.renameTo(file)) {
            throw new IOException("Error to rename file to " + file.getAbsolutePath());
        }
    }

    public static String loadFromFile(File file) throws IOException {
        try (FileInputStream stream = new FileInputStream(file)) {
            Reader reader = new BufferedReader(new InputStreamReader(stream));
            StringBuilder builder = new StringBuilder();
            char[] buffer = new char[8192];
            int read;
            while ((read = reader.read(buffer, 0, buffer.length)) > 0) {
                builder.append(buffer, 0, read);
            }
            return builder.toString();
        }
    }

    public static void deleteFile(File file) {
        if (file != null && file.exists()) {
            file.delete();
        }
    }



    public static int clearFolder(File folder) {
        int numDeleted = 0;
        if ((folder == null) || !folder.isDirectory()) {
            return numDeleted;
        }
        File[] files = folder.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    numDeleted += clearFolder(f);
                } else {
                    if (f.delete()) {
                        numDeleted ++;
                    }
                }
            }
        }
        return numDeleted;
    }






    /**
     * Exports database file into the "db-export" folder into into external cache dir.
     * @param context
     * @return null in case of success. Error description in case of error.
     */
    public static String exportDb(Context context, String dbName) {
        File extFolder = getExternalCacheDir(context);
        File dbSrcFile = context.getDatabasePath(dbName);
        if (extFolder == null) {
            return "no external cache directory";
        } else if (dbSrcFile == null) {
            return "no DB file found";
        } else {
            try {
                File dir = createDirIfNeeded(new File(extFolder.getPath() + File.separator + "db-export"));
                File dbDstFile = new File(dir, dbName);
                if (dbDstFile.exists()) {
                    dbDstFile.delete();
                }

                copyFileToFile(dbSrcFile, dbDstFile, 0);

                return null;
            } catch (IOException e) {
                return e.getMessage();
            }
        }


    }

    public static boolean checkMediaFileDurationLimit(String path, float durationSeconds) throws Exception {
        try {
            return  FileUtils.getMediaFileDuration(path) < (durationSeconds + 0.5f);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("A file's path can't be empty.", e);
        }
    }

    public static boolean checkMediaFileSizeLimit(String path, long sizeLimit) {
        try {
            return FileUtils.getMediaFileSize(path) < sizeLimit;
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("A file's path can't be empty.");
        }
    }

    public static float getMediaFileDuration(String filePath) throws Exception {
        if (TextUtils.isEmpty(filePath)) {
            throw new IllegalArgumentException("A file's path can't be empty.");
        }
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(filePath);
        return Float.parseFloat(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) / 1000f;
    }

    public static long getMediaFileSize(String filePath) throws Exception {
        if (TextUtils.isEmpty(filePath)) {
            throw new IllegalArgumentException("A file's path can't be empty.");
        }
        return new File(filePath).length();
    }



    /**
     * Downloads a file using DownloadManager.
     */
    public static void downloadFileWithManager(Context context, String url, String fileName) {
        DownloadManager.Request downloadRequest = new DownloadManager.Request(Uri.parse(url));
        downloadRequest.setTitle(fileName);

        // in order for this if to run, you must use the android 3.2 to compile your app
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            downloadRequest.allowScanningByMediaScanner();
            downloadRequest.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        }

        downloadRequest.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

        ((DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE)).enqueue(downloadRequest);
    }


    public static String loadAsset(Context ctx, String path) throws IOException {
        try (InputStream src = new BufferedInputStream(ctx.getAssets().open(path, AssetManager.ACCESS_STREAMING))) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream(512);
            byte[] buffer = new byte[512];
            int count;
            while ((count = src.read(buffer)) != -1) {
                bos.write(buffer, 0, count);
            }
            return bos.toString("UTF-8");
        }
    }



    /**************************  Resolve file type by Uri Authority  ******************************/

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    private static boolean isNewGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.contentprovider".equals(uri.getAuthority());
    }

    public static boolean isGoogleDriveFile(Uri uri) {
        return "com.google.android.apps.docs.storage".equals(uri.getAuthority());
    }



    /****************************  Extract file path by Uri  **************************************/

    public static String getPath(final Context context, final Uri uri) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, uri)) {

            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/"
                            + split[1];
                }

            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        Long.valueOf(id));

                return getDataColumnText(context, contentUri, null, null);

            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] { split[1] };

                return getDataColumnText(context, contentUri, selection, selectionArgs);

            } else if (isGoogleDriveFile(uri)) {
                return "google_drive";
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {

            if (isGooglePhotosUri(uri)) {
                return uri.getLastPathSegment();
            }
            return getDataColumnText(context, uri, null, null);

        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    public static String getDataColumnText(Context context, Uri uri, String selection, String[] selectionArgs) {
        final String column = "_data";
        final String[] projection = { column };

        try (Cursor c = context.getContentResolver().query(uri, projection, selection, selectionArgs, null)) {
            if (c != null && c.moveToFirst()) {
                return c.getString(c.getColumnIndexOrThrow(column));
            } else {
                return null;
            }
        }
    }

    public static long getMediaFileSizeByURI(Context context, Uri uri) {
        try (Cursor c = context.getContentResolver().query(uri, null, null, null, null)) {
            if (c != null && c.moveToFirst()) {
                return c.getLong(c.getColumnIndex(OpenableColumns.SIZE));
            } else {
                return -1;
            }
        }
    }




    /*public static String getMimeTypeForFileByExtension(File f) {
        try {
            String path = f.getAbsolutePath();
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(path.substring(path.lastIndexOf('.')+1));
        } catch (Exception e) {
            return null;
        }
    }

    public static String getMimeTypeForPathByExtension(String path) {
        try {
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(path.substring(path.lastIndexOf('.')+1));
        } catch (Exception e) {
            return null;
        }
    }*/







    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static Hashtable<String, Object> getFileInfo(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    Hashtable<String, Object> value = new Hashtable<String, Object>();
                    value.put("path", Environment.getExternalStorageDirectory() + "/" + split[1]);
                    value.put("size", (int) new File((String) value.get("path")).length());
                    value.put("mime", "application/octet-stream");

                    return value;
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{ split[1] };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            if (isNewGooglePhotosUri(uri)) {
                Hashtable<String, Object> value = getDataColumn(context, uri, null, null);
                Bitmap bitmap;
                try {
                    InputStream input = context.getContentResolver().openInputStream(uri);
                    bitmap = BitmapFactory.decodeStream(input);
                    File file = File.createTempFile("sendbird", ".jpg");
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, new BufferedOutputStream(new FileOutputStream(file)));
                    value.put("path", file.getAbsolutePath());
                    value.put("size", (int) file.length());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return value;
            } else {
                return getDataColumn(context, uri, null, null);
            }
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            Hashtable<String, Object> value = new Hashtable<String, Object>();
            value.put("path", uri.getPath());
            value.put("size", (int) new File((String) value.get("path")).length());
            value.put("mime", "application/octet-stream");

            return value;
        }
        return null;
    }


    private static Hashtable<String, Object> getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {

        Cursor c = null;
        final String COLUMN_DATA = MediaStore.MediaColumns.DATA;
        String COLUMN_MIME = MediaStore.MediaColumns.MIME_TYPE;
        final String COLUMN_SIZE = MediaStore.MediaColumns.SIZE;

        String[] projection = {
                COLUMN_DATA,
                COLUMN_MIME,
                COLUMN_SIZE
        };

        try {
            try {
                c = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            } catch(IllegalArgumentException e) {
                //Fallback with different "MIMETYPE" column name
                COLUMN_MIME = "mimetype"; // DownloadProvider.sAppReadableColumnsArray.COLUMN_MIME_TYPE
                projection[1] = COLUMN_MIME;
                c = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            }

            if (c != null && c.moveToFirst()) {
                String path = c.getString(c.getColumnIndexOrThrow(COLUMN_DATA));
                String mime = c.getString(c.getColumnIndexOrThrow(COLUMN_MIME));
                int size = c.getInt(c.getColumnIndexOrThrow(COLUMN_SIZE));

                Hashtable<String, Object> value = new Hashtable<>();
                if(path == null) path = "";
                if(mime == null) mime = "application/octet-stream";

                value.put("path", path);
                value.put("mime", mime);
                value.put("size", size);
                return value;
            } else {
                return null;
            }

        } catch(Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (c != null)
                c.close();
        }
    }
}
