package com.activels.als.diyappmanager.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Debug;
import android.util.Log;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class FileUtils {
    public static final String TAG = "fileutils";

    protected static long memallocated, memlastlog;
    public static boolean memlog_enabled = true;

    public static void memlog_init() {
        System.gc();
        memlastlog = memallocated = Debug.getNativeHeapAllocatedSize();
    }

    public static void memlog_print(String pref) {
        if (memlog_enabled) {
            System.gc();
            long memnow = Debug.getNativeHeapAllocatedSize();
            Log.i(TAG, pref);
            Log.i(TAG, " Mem Diff: " + ((memnow - memlastlog) / 1024)
                            + "k / AppUse: " + ((memnow - memallocated) / 1024)
                            + "k | TotalUse: " + (memnow / 1024) + "k" // + Free "
                            + "Free " + (Debug.getNativeHeapFreeSize() / 1024) + "k = Heap "
                            + (Debug.getNativeHeapSize() / 1024) + "k"
            );
            memlastlog = memnow;
        }
    }

    // ----- ----- ----- ----- ----- ----- ----- ----- -----
    public static int bmploadcnt = 0, bmpfreecnt = 0;

    public static Bitmap loadbmp(String pathname) {
        Bitmap b = BitmapFactory.decodeFile(pathname);
        if (null != b)
            bmploadcnt++;
        return b;
    }

    public static Bitmap loadbmpresize(String pathname, int w, int h) {
        // get bitmapsize
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathname, options);
        int imgh = options.outHeight;
        int imgw = options.outWidth;

        if ((0 > imgh) || (0 > imgw))
            return null;

        imgh /= 2;
        imgw /= 2;
        int sample = 1;
        while ((imgh > h) && (imgw > w)) {
            imgh /= 2;
            imgw /= 2;
            sample *= 2;
        }

        options.inJustDecodeBounds = false;
        options.inSampleSize = sample;
        Bitmap b = BitmapFactory.decodeFile(pathname, options);

        bmploadcnt++;
        if (b.getWidth() == w && b.getHeight() == h)
            return b;

        Bitmap b2 = Bitmap.createScaledBitmap(b, w, h, true);
        b.recycle();
        if (null == b2)
            bmploadcnt--;
        return b2;
    }

    public static Bitmap loadbmpresizepercent(String pathname, double wpercent, double hpercent) {
        // get bitmapsize
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathname, options);
        int imgh = options.outHeight;
        int imgw = options.outWidth;
        int h = (int) (imgh * hpercent);
        int w = (int) (imgw * wpercent);

        if ((0 >= imgh) || (0 >= imgw))
            return null;

        imgh /= 2;
        imgw /= 2;
        int sample = 1;
        while ((imgh > h) && (imgw > w)) {
            imgh /= 2;
            imgw /= 2;
            sample *= 2;
        }

        options.inJustDecodeBounds = false;
        options.inSampleSize = sample;
        Bitmap b = BitmapFactory.decodeFile(pathname, options);

        if (b == null)
            return null;

        bmploadcnt++;
        if (b.getWidth() == w && b.getHeight() == h)
            return b;

        Bitmap b2 = Bitmap.createScaledBitmap(b, w, h, true);
        b.recycle();
        if (null == b2)
            bmploadcnt--;
        return b2;
    }

    // ----- ----- ----- ----- ----- ----- ----- ----- -----
    public static void freebmp(Bitmap b) {
        if (null != b)
            bmpfreecnt++;
        b.recycle();
    }

    // ----- ----- ----- ----- ----- ----- ----- ----- -----

    public static void sortStringArrAscending(String[] strArr) {
        Arrays.sort(strArr, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                return lhs.compareTo(rhs);
            }
        });
    }

    public static void sortStringArrDescending(String[] strArr) {
        Arrays.sort(strArr, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                return rhs.compareTo(lhs);
            }
        });
    }

    public static String[] ToStringArray(JSONArray ja) {
        int size = ja.length();
        if (0 >= size)
            return null;
        try {
            String[] ret = new String[size];
            for (int i = 0; i < size; i++) {
                ret[i] = ja.getString(i);
            }
            return ret;
        } catch (Exception e) {
            Log.e(TAG, "ErrorConverting JSONArray to Array");
            e.printStackTrace();
        }
        return null;
    }

    // given file need some folder, create for it if not exists
    public static boolean mkdirsforfile(String basepath, String filepath) {
        boolean ok = false;
        // Log.d(TAG, "mkdirsforfile:" + basepath + " / " + filepath);
        try {
            String[] ps = filepath.split("/");
            int depth = ps.length - 1;
            String p = basepath;
            for (int d = 0; d < depth; d++) {
                if (ps[d].length() > 0) {
                    p += ps[d];
                    File dir = new File(p);
                    if (!dir.exists()) {
                        dir.mkdir();
                    }
                    p += "/";
                }
            }
            ok = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ok;
    }

    public static String getFileDir(String filepath) {
        String p = "";
        if (filepath != null) {
            String[] ps = filepath.split("/");
            int depth = ps.length - 1;
            for (int d = 0; d < depth; d++) {
                p += ps[d];
                p += "/";
            }
        }
        return p;
    }

    public static String getFileName(String filepath) {
        if (filepath != null) {
            String[] ps = filepath.split("/");
            if (ps.length > 0)
                return ps[ps.length - 1];
        }
        return "";
    }

    public static boolean bytes2file(byte[] bytes, String filepath,
                                     boolean append) {
        boolean ok = false;
        try {
            OutputStream out = new FileOutputStream(filepath, append);
            out.write(bytes);
            out.flush();
            out.close();
            ok = true;
            // Log.d(TAG, "Res List Saved: "+filepath);
        } catch (Exception e) {
            Log.e(TAG, "failed to save reslist: " + filepath);
            e.printStackTrace();
        }
        return ok;
    }

    public static String file2str(String filepath) {
        // Log.d(TAG, "Reading File to String");
        String jstr = null;
        try {
            FileInputStream in = new FileInputStream(filepath);
            if (null != in) {
                String ret = "";
                final int bufsize = 1024 * 4;
                byte[] buffer = new byte[bufsize];
                int byteread = in.read(buffer, 0, bufsize);
                while (0 <= byteread) {
                    ret += new String(buffer, 0, byteread);
                    byteread = in.read(buffer, 0, bufsize);
                }
                in.close();
                jstr = ret + "";
            }
        } catch (Exception e) {
            Log.e(TAG, "error file2str: " + filepath);
        }
        return jstr;
    }

    public static File createFile(String filePath) {
        File file = new File(filePath);
        Log.d(TAG, "createFile exist?" + file.exists() + " at path: " + filePath);
        try {
            boolean mkdirBoolean = mkdirs(getFileDir(filePath));
            if (!mkdirBoolean)
                Log.d(TAG, "mkdirBoolean=" + mkdirBoolean + ", path=" + filePath);
            boolean createNewBoolean = file.createNewFile();
            Log.d(TAG, "createNewBoolean=" + createNewBoolean + ", path=" + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    public static boolean writeStrToFile(String path, String str) {
        if (str == null)
            return false;
        try {
            File dir = new File(getFileDir(path));
            dir.mkdirs();

            File file = new File(path);
            file.createNewFile();

            Log.d(TAG, "writeStrToFile str=" + str + ", to path=" + path);
            BufferedWriter out = new BufferedWriter(new FileWriter(path));
            out.write(str);
            out.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // old......
    //	private String SDPATH = null;
    //
    //	public String getSDPATH() {
    //		return SDPATH;
    //	}
    //
    //	public FileUtils() {
    //		// 获得当前外部存储设备SD卡的目录
    //		SDPATH = Environment.getExternalStorageDirectory() + "/";
    //	}
    //
    //	// 创建文件
    //
    //
    //	// 创建目录
    //	public File createDir(String fileName) throws IOException {
    //		File dir = new File(SDPATH + fileName);
    //		if (!dir.exists()) {
    //			dir.mkdir();
    //		}
    //		return dir;
    //	}
    //
    //	// 判断文件是否存在
    //	//	public boolean isExist(String fileName) {
    //	//		File file = new File(SDPATH + fileName);
    //	//		if (file.exists()) {
    //	//			file.delete();
    //	//		}
    //	//		return file.exists();
    //	//	}
    //
    //
    //	public File writeToSDPATHFromInput(String path, String fileName,
    //			InputStream inputstream) {
    //		File file = null;
    //		OutputStream outputstream = null;
    //
    //		try {
    //			createDir(path);
    //			if (!isExist(path + fileName)) {
    //				file = createFile(path + fileName);
    //				outputstream = new FileOutputStream(file);
    //				byte buffer[] = new byte[128];
    //				do {
    //					int length = (inputstream.read(buffer));
    //					if (length != -1) {
    //						outputstream.write(buffer, 0, length);
    //					} else {
    //						break;
    //					}
    //				} while (true);
    //			}
    //		} catch (Exception e) {
    //			e.printStackTrace();
    //		} finally {
    //			try {
    //				outputstream.close();
    //			} catch (IOException e) {
    //				e.printStackTrace();
    //			}
    //		}
    //		return file;
    //	}


    //=============================== originally from FileUtil.java

    public static boolean mkdirs(String dirPath) {
        boolean ok = false;
        //*Log.d(TAG, "mkdir:" + dirPath);
        try {
            File file = new File(dirPath);
            if (!file.exists())
                ok = file.mkdirs();
            else
                ok = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ok;
    }

    public static boolean isExist(String fileName) {
        if (fileName == null)
            return false;
        File file = new File(fileName);
        return file.exists();
    }

    public static boolean deleteFile(String fileName) {
        boolean result = false;
        File file = new File(fileName);
        if (file.exists()) {
            result = file.delete();
        }
        return result;
    }

    public static boolean deleteDir(String path) {
        File file = new File(path);
        if (file.exists()) {
            File[] files = file.listFiles();
            if (files == null)
                return true;
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDir(files[i].getAbsolutePath());
                } else {
                    files[i].delete();
                }
            }
        }
        return (file.delete());
    }

    /**
     * 删除目录下的文件
     *
     * @param path
     * @return
     */
    public static boolean deleteDir2(String path) {
        File file = new File(path);
        if (file.exists()) {
            File[] files = file.listFiles();
            if (files == null)
                return true;
            for (int i = 0; i < files.length; i++) {
                if (files[i].isFile()) {
                    files[i].delete();
                }
            }
        }
        return (file.delete());
    }

    public static boolean deleteDir(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDir(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }

    public static boolean isFolderEmpty(String fileName) {
        File file = new File(fileName);
        String[] subDirList = file.list();

        if (subDirList == null || subDirList.length == 0)
            return true;
        else
            return false;
    }

    public boolean movefile(String srFile, String dtFile) {

        File f1 = new File(srFile);
        File f2 = new File(dtFile);
        //		boolean copy = copyfile( srFile,  dtFile);
        //
        //		boolean delete = false;
        //		if(f2.exists())
        //			delete = f1.delete();s
        //
        //		return copy && delete;

        int index = dtFile.lastIndexOf("/");
        String f2Dir = dtFile.substring(0, index);

        File dir = new File(f2Dir);
        dir.mkdirs();
        return f1.renameTo(f2);
    }

    public boolean copyfile(String srFilePath, String dtFilePath) throws IOException {
        File srFile = new File(srFilePath);
        if (!srFile.exists()) {
            Log.e(TAG, "copyfile fail srFile not exists");
            return false;
        }
        Log.d(TAG, "create dtFilePath=" + createFile(dtFilePath));

        InputStream in = new FileInputStream(srFilePath);
        OutputStream out = new FileOutputStream(dtFilePath);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
        return true;
    }

    public void copyDir(String srcPath, String destPath)
            throws IOException {

        File src = new File(srcPath);
        File dest = new File(destPath);

        if (src.isDirectory()) {
            Log.d(TAG, " isDirectory");

            //if directory not exists, create it
            if (!dest.exists()) {
                dest.mkdir();
                Log.d(TAG, "Directory copied from " + src + "  to " + dest);
            }

            //list all the directory contents
            String files[] = src.list();

            for (String file : files) {
                //construct the src and dest file structure
                File srcFile = new File(src, file);
                File destFile = new File(dest, file);
                //recursive copy
                copyDir(srcFile.getAbsolutePath(), destFile.getAbsolutePath());
            }

        } else {
            Log.d(TAG, " !isDirectory");

            //if file, then copy it
            //Use bytes stream to support all file types

            copyfile(srcPath, destPath);

            //			InputStream in = new FileInputStream(src);
            //			OutputStream out = new FileOutputStream(dest);
            //
            //			byte[] buffer = new byte[1024];
            //
            //			int length;
            //			//copy the file content in bytes
            //			while ((length = in.read(buffer)) > 0){
            //				out.write(buffer, 0, length);
            //			}
            //
            //			in.close();
            //			out.close();
            Log.d(TAG, "File copied from " + src + " to " + dest);
        }
    }


    public static List<String> loadcsvfile(String csvfname) {
        List<String> result = null;
        File file = new File(csvfname);
        if (file != null && file.exists()) {
            try {
                result = new ArrayList<String>();
                FileReader reader = new FileReader(csvfname);
                BufferedReader br = new BufferedReader(reader);
                String s = null;
                while ((s = br.readLine()) != null) {
                    s = s.trim();
                    if ("".compareTo(s) == 0) {
                        // empty line
                    } else if (s.charAt(0) == '#') {
                        // remark line
                    } else {
                        result.add(s);
                    }
                }
                br.close();
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static String loadStringFile(String filePath) {
        ArrayList<String> strList = null;
        String result = "";
        File file = new File(filePath);
        if (file != null && file.exists()) {
            try {
                strList = new ArrayList<String>();
                FileReader reader = new FileReader(filePath);
                BufferedReader br = new BufferedReader(reader);
                String s = null;
                while ((s = br.readLine()) != null) {
                    s = s.trim();
                    //					if ("".compareTo(s) == 0) {
                    //						// empty line
                    //					} else {
                    strList.add(s);
                    //					}
                }
                br.close();
                reader.close();

                result = unsplit(strList, 0, strList.size(), "\n");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * Reverse the split operation.
     *
     * @param parts    The parts to combine
     * @param index    the index to the fist part to use
     * @param length   the number of parts to use
     * @param splitter The between-parts text
     */
    public static String unsplit(String[] parts, int index, int length, String splitter) {
        if (parts == null) return null;
        if ((index < 0) || (index >= parts.length)) return null;
        if (index + length > parts.length) return null;

        StringBuilder buf = new StringBuilder();
        for (int i = index; i < index + length; i++) {
            if (parts[i] != null) buf.append(parts[i]);
            buf.append(splitter);
        }

        // remove the trailing splitter
        buf.setLength(buf.length() - splitter.length());
        return buf.toString();
    }

    /**
     * Reverse the split operation.
     *
     * @param parts    The parts to combine
     * @param index    the index to the fist part to use
     * @param length   the number of parts to use
     * @param splitter The between-parts text
     */
    public static String unsplit(ArrayList<String> parts, int index, int length, String splitter) {
        if (parts == null) return "";
        if ((index < 0) || (index >= parts.size())) return "";
        if (index + length > parts.size()) return "";

        StringBuilder buf = new StringBuilder();
        for (int i = index; i < index + length; i++) {
            if (parts.get(i) != null) buf.append(parts.get(i));
            buf.append(splitter);
        }

        // remove the trailing splitter
        buf.setLength(buf.length() - splitter.length());
        return buf.toString();
    }
}
