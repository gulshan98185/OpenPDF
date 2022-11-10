package com.lowagie.text.helper;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.lowagie.text.BadElementException;
import com.lowagie.text.DocumentException;
import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.Image;

import java.awtandroid.Color;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class BitmapImageDecoder {
    public static Image getInstance(String path, Color color, boolean forceBW) throws BadElementException, IOException {
        Bitmap bitmap = null;
        try (InputStream stream = new FileInputStream(path)) {
            bitmap = BitmapFactory.decodeStream(stream);
            return getInstance(bitmap, color, forceBW);
        } finally {
            if (bitmap != null) {
                bitmap.recycle();
            }
        }
    }

    public static Image getInstance(InputStream inputStream, Color color, boolean forceBW) throws BadElementException, IOException {
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(inputStream);
            return getInstance(bitmap, color, forceBW);
        } finally {
            if (bitmap != null) {
                bitmap.recycle();
            }
        }
    }

    public static Image getInstance(byte data[], Color color, boolean forceBW) throws BadElementException, IOException {
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            return getInstance(bitmap, color, forceBW);
        } finally {
            if (bitmap != null) {
                bitmap.recycle();
            }
        }
    }

    /**
     * Gets an instance of an Image from a java.awt.Image.
     *
     * @param bitmap  the <CODE>java.awt.Image</CODE> to convert
     * @param color   if different from <CODE>null</CODE> the transparency pixels
     *                are replaced by this color
     * @param forceBW if <CODE>true</CODE> the image is treated as black and white
     * @return an object of type <CODE>ImgRaw</CODE>
     * @throws BadElementException on error
     * @throws IOException         on error
     */
    public static Image getInstance(Bitmap bitmap, Color color, boolean forceBW) throws BadElementException, IOException {

        /*if(image instanceof BufferedImage){
            BufferedImage bi = (BufferedImage) image;
            if(bi.getType() == BufferedImage.TYPE_BYTE_BINARY && bi.getColorModel().getNumColorComponents() <= 2) {
                forceBW=true;
            }
        }*/

        //ImageDecoder.createSource(new File(path));
        /*java.awt.image.PixelGrabber pg = new java.awt.image.PixelGrabber(image,
                0, 0, -1, -1, true);*/
        /*try {
            pg.grabPixels();
        } catch (InterruptedException e) {
            throw new IOException(MessageLocalization.getComposedMessage("java.awt.image.interrupted.waiting.for.pixels"));
        }*/
        /*if ((pg.getStatus() & java.awt.image.ImageObserver.ABORT) != 0) {
            throw new IOException(MessageLocalization.getComposedMessage("java.awt.image.fetch.aborted.or.errored"));
        }*/
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        //int[] pixels = (int[]) bitmap.getPixels();
        int[] pixels = new int[bitmap.getWidth() * bitmap.getHeight()];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        if (forceBW) {
            int byteWidth = (w / 8) + ((w & 7) != 0 ? 1 : 0);
            byte[] pixelsByte = new byte[byteWidth * h];

            int index = 0;
            int size = h * w;
            int transColor = 1;
            if (color != null) {
                transColor = (color.getRed() + color.getGreen()
                        + color.getBlue() < 384) ? 0 : 1;
            }
            int[] transparency = null;
            int cbyte = 0x80;
            int wMarker = 0;
            int currByte = 0;
            if (color != null) {
                for (int j = 0; j < size; j++) {
                    int alpha = (pixels[j] >> 24) & 0xff;
                    if (alpha < 250) {
                        if (transColor == 1)
                            currByte |= cbyte;
                    } else {
                        if ((pixels[j] & 0x888) != 0)
                            currByte |= cbyte;
                    }
                    cbyte >>= 1;
                    if (cbyte == 0 || wMarker + 1 >= w) {
                        pixelsByte[index++] = (byte) currByte;
                        cbyte = 0x80;
                        currByte = 0;
                    }
                    ++wMarker;
                    if (wMarker >= w)
                        wMarker = 0;
                }
            } else {
                for (int j = 0; j < size; j++) {
                    if (transparency == null) {
                        int alpha = (pixels[j] >> 24) & 0xff;
                        if (alpha == 0) {
                            transparency = new int[2];
                            /* bugfix by M.P. Liston, ASC, was: ... ? 1: 0; */
                            transparency[0] = transparency[1] = ((pixels[j] & 0x888) != 0) ? 0xff : 0;
                        }
                    }
                    if ((pixels[j] & 0x888) != 0)
                        currByte |= cbyte;
                    cbyte >>= 1;
                    if (cbyte == 0 || wMarker + 1 >= w) {
                        pixelsByte[index++] = (byte) currByte;
                        cbyte = 0x80;
                        currByte = 0;
                    }
                    ++wMarker;
                    if (wMarker >= w)
                        wMarker = 0;
                }
            }
            return Image.getInstance(w, h, 1, 1, pixelsByte, transparency);
        } else {
            byte[] pixelsByte = new byte[w * h * 3];
            byte[] smask = null;

            int index = 0;
            int size = h * w;
            int red = 255;
            int green = 255;
            int blue = 255;
            if (color != null) {
                red = color.getRed();
                green = color.getGreen();
                blue = color.getBlue();
            }
            int[] transparency = null;
            if (color != null) {
                for (int j = 0; j < size; j++) {
                    int alpha = (pixels[j] >> 24) & 0xff;
                    if (alpha < 250) {
                        pixelsByte[index++] = (byte) red;
                        pixelsByte[index++] = (byte) green;
                        pixelsByte[index++] = (byte) blue;
                    } else {
                        pixelsByte[index++] = (byte) ((pixels[j] >> 16) & 0xff);
                        pixelsByte[index++] = (byte) ((pixels[j] >> 8) & 0xff);
                        pixelsByte[index++] = (byte) ((pixels[j]) & 0xff);
                    }
                }
            } else {
                int transparentPixel = 0;
                smask = new byte[w * h];
                boolean shades = false;
                for (int j = 0; j < size; j++) {
                    byte alpha = smask[j] = (byte) ((pixels[j] >> 24) & 0xff);
                    /* bugfix by Chris Nokleberg */
                    if (!shades) {
                        if (alpha != 0 && alpha != -1) {
                            shades = true;
                        } else if (transparency == null) {
                            if (alpha == 0) {
                                transparentPixel = pixels[j] & 0xffffff;
                                transparency = new int[6];
                                transparency[0] = transparency[1] = (transparentPixel >> 16) & 0xff;
                                transparency[2] = transparency[3] = (transparentPixel >> 8) & 0xff;
                                transparency[4] = transparency[5] = transparentPixel & 0xff;
                                for (int prevPixel = 0; prevPixel < j; prevPixel++) {
                                    if ((pixels[prevPixel] & 0xffffff) == transparentPixel) {
                                        shades = true;
                                        break;
                                    }
                                }
                            }
                        } else if ((pixels[j] & 0xffffff) != transparentPixel && alpha == 0
                                || (pixels[j] & 0xffffff) == transparentPixel && alpha != 0) {
                            shades = true;
                        }
                    }
                    pixelsByte[index++] = (byte) ((pixels[j] >> 16) & 0xff);
                    pixelsByte[index++] = (byte) ((pixels[j] >> 8) & 0xff);
                    pixelsByte[index++] = (byte) ((pixels[j]) & 0xff);
                }
                if (shades)
                    transparency = null;
                else
                    smask = null;
            }
            Image img = Image.getInstance(w, h, 3, 8, pixelsByte, transparency);
            if (smask != null) {
                Image sm = Image.getInstance(w, h, 1, 8, smask);
                try {
                    sm.makeMask();
                    img.setImageMask(sm);
                } catch (DocumentException de) {
                    throw new ExceptionConverter(de);
                }
            }
            return img;
        }
    }
}
