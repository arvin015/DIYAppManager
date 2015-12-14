package com.activels.als.diyappmanager.qrcode;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.Hashtable;

public class QRCodeUtils {
    private final static String TAG = "QRCodeUtils";

    public static Bitmap createImage(String text, int width, int height,
                                     Bitmap logo) {
        try {
            // 需要引入core包
            QRCodeWriter writer = new QRCodeWriter();

            if (text == null || "".equals(text) || text.length() < 1) {
                return null;
            }

            // 把输入的文本转为二维码
            BitMatrix martix = writer.encode(text, BarcodeFormat.QR_CODE,
                    width, height);

            Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            BitMatrix bitMatrix = new QRCodeWriter().encode(text,
                    BarcodeFormat.QR_CODE, width, height, hints);
            int[] pixels = new int[width * height];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * width + x] = 0xff404040;
                    } else {
                        pixels[y * width + x] = 0xffffffff;
                    }

                }
            }

            Bitmap bitmap = Bitmap.createBitmap(width, height,
                    Bitmap.Config.ARGB_8888);

            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);

            return setQRCodeLogo(bitmap, logo);
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }

    }

    private static Bitmap setQRCodeLogo(Bitmap qr_img, Bitmap logo) {

        int logoWidth = qr_img.getWidth() / 5; // 设置logo图片宽度为二维码图片的五分之一

        int logoHeight = qr_img.getHeight() / 5; // 设置logo图片高度为二维码图片的五分之一
        logo = BitmapHelper.resizeImage(logo, logoWidth, logoHeight);
        int logoX = (qr_img.getWidth() - logoWidth) / 2; // 设置logo图片的位置,这里令其居中

        int logoY = (qr_img.getHeight() - logoHeight) / 2; // 设置logo图片的位置,这里令其居中
        int right = logoX + logoWidth;
        int bottom = logoY + logoHeight;

        Rect mRect = new Rect(logoX, logoY, right, bottom);

        Canvas canvas = new Canvas(qr_img);

        Rect mRectLogo = new Rect(0, 0, logoWidth, logoHeight);
        canvas.drawBitmap(logo, mRectLogo, mRect, null);

        return qr_img;
    }
}
