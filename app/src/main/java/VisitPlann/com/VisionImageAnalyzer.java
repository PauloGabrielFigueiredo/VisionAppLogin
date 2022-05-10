package VisitPlann.com;

import static android.graphics.ImageFormat.YUV_420_888;
import static android.graphics.ImageFormat.YUV_422_888;
import static android.graphics.ImageFormat.YUV_444_888;
import static androidx.core.math.MathUtils.clamp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.label.Category;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Comparator;
import java.util.List;

import VisitPlann.com.ml.Model;


public class VisionImageAnalyzer implements ImageAnalysis.Analyzer {
    private Context context;
    private VisionListener vslistener;

    public VisionImageAnalyzer(Context applicationContext, VisionListener visionListener) {
        this.context = applicationContext;
        this.vslistener = visionListener;
    }

    @Override
    public void analyze(@NonNull ImageProxy image) {

        if (image.getFormat() == YUV_420_888 || image.getFormat() == YUV_422_888 || image.getFormat() == YUV_444_888) {
            int [] RGB_Array =  yuv420ToBitmap(image);
            int imageWidth = image.getWidth();
            int imageHeight = image.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(RGB_Array, 0,imageWidth , imageWidth,imageHeight , Bitmap.Config.ARGB_8888);
            List<Category> scores = null;
            try {
                Model model = Model.newInstance(context);

                // Creates inputs for reference.
                TensorImage image2 = TensorImage.fromBitmap(bitmap);

                // Runs model inference and gets result.
                Model.Outputs outputs = model.process(image2);
                scores = outputs.getScoresAsCategoryList();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    scores.sort(new Comparator<Category>() {
                        @Override
                        public int compare(Category category, Category t1) {
                            if(category.getScore()> t1.getScore()){
                                return -1;
                            }
                            else if(category.getScore()< t1.getScore()){
                                return 1;
                            }
                            else{
                                return 0;
                            }
                        }
                    });
                }
                // Releases model resources if no longer used.
                model.close();
            } catch (IOException e) {
                // TODO Handle the exception
            }

            image.close();
            vslistener.onImageFound(scores.get(0).toString());

    }
}

    int [] yuv420ToBitmap(ImageProxy image) {
        if (image.getFormat() != ImageFormat.YUV_420_888) {
            throw new IllegalArgumentException("Invalid image format");
        }
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();
        // ARGB array needed by Bitmap static factory method I use below.
        int[] argbArray = new int[imageWidth * imageHeight];
        ByteBuffer yBuffer = image.getPlanes()[0].getBuffer();
        yBuffer.position(0);
        // A YUV Image could be implemented with planar or semi planar layout.
        // A planar YUV image would have following structure:
        // YYYYYYYYYYYYYYYY
        // ................
        // UUUUUUUU
        // ........
        // VVVVVVVV
        // ........
        //
        // While a semi-planar YUV image would have layout like this:
        // YYYYYYYYYYYYYYYY
        // ................
        // UVUVUVUVUVUVUVUV   <-- Interleaved UV channel
        // ................
        // This is defined by row stride and pixel strides in the planes of the
        // image.

        // Plane 1 is always U & plane 2 is always V
        // https://developer.android.com/reference/android/graphics/ImageFormat#YUV_420_888
        ByteBuffer uBuffer = image.getPlanes()[1].getBuffer();
        uBuffer.position(0);
        ByteBuffer vBuffer = image.getPlanes()[2].getBuffer();
        vBuffer.position(0);

        // The U/V planes are guaranteed to have the same row stride and pixel
        // stride.
        int yRowStride = image.getPlanes()[0].getRowStride();
        int yPixelStride = image.getPlanes()[0].getPixelStride();
        int uvRowStride = image.getPlanes()[1].getRowStride();
        int uvPixelStride = image.getPlanes()[1].getPixelStride();

        int r, g, b;
        int yValue, uValue, vValue;

        for (int y = 0; y < imageHeight; ++y) {
            for (int x = 0; x < imageWidth; ++x) {
                int yIndex = (y * yRowStride) + (x * yPixelStride);
                // Y plane should have positive values belonging to [0...255]
                yValue = (yBuffer.get(yIndex) & 0xff);

                int uvx = x / 2;
                int uvy = y / 2;
                // U/V Values are subsampled i.e. each pixel in U/V chanel in a
                // YUV_420 image act as chroma value for 4 neighbouring pixels
                int uvIndex = (uvy * uvRowStride) +  (uvx * uvPixelStride);

                // U/V values ideally fall under [-0.5, 0.5] range. To fit them into
                // [0, 255] range they are scaled up and centered to 128.
                // Operation below brings U/V values to [-128, 127].
                uValue = (uBuffer.get(uvIndex) & 0xff) - 128;
                vValue = (vBuffer.get(uvIndex) & 0xff) - 128;

                // Compute RGB values per formula above.
                r = (int) (yValue + 1.370705f * vValue);
                g = (int) (yValue - (0.698001f * vValue) - (0.337633f * uValue));
                b = (int) (yValue + 1.732446f * uValue);
                r = clamp(r, 0, 255);
                g = clamp(g, 0, 255);
                b = clamp(b, 0, 255);

                // Use 255 for alpha value, no transparency. ARGB values are
                // positioned in each byte of a single 4 byte integer
                // [AAAAAAAARRRRRRRRGGGGGGGGBBBBBBBB]
                int argbIndex = y * imageWidth + x;
                argbArray[argbIndex]
                        = (255 << 24) | (r & 255) << 16 | (g & 255) << 8 | (b & 255);
            }
        }
        return argbArray;
    }


}