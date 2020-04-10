/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package github.hotstu.lib.barscaner;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.google.zxing.ResultPoint;

/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder rectangle and partial
 * transparency outside it, as well as the laser scanner animation and result points.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class ViewfinderView extends View {

    private static final int[] SCANNER_ALPHA = {0, 64, 128, 192, 255, 192, 128, 64};

    private static final int MIN_FRAME_WIDTH = 240;
    private static final int MIN_FRAME_HEIGHT = 240;
    private static final int MAX_FRAME_WIDTH = 1200; // = 5/8 * 1920
    private static final int MAX_FRAME_HEIGHT = 675; // = 5/8 * 1080

    private final Paint paint;
    private final int maskColor;
    private final int laserColor;
    private int scannerAlpha;
    private final ValueAnimator mAnim;
    private final RectF ovalRect;

    // This constructor is used when the class is built from an XML resource.
    public ViewfinderView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Initialize these once for performance rather than calling them every time in onDraw().
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Resources resources = getResources();
        maskColor = resources.getColor(R.color.viewfinder_mask);
        laserColor = resources.getColor(R.color.viewfinder_laser);
        scannerAlpha = 0;
        mAnim = ValueAnimator.ofFloat(0, 1f);
        mAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        mAnim.setRepeatMode(ValueAnimator.RESTART);
        mAnim.setRepeatCount(ValueAnimator.INFINITE);
        mAnim.setDuration(2000);
        ovalRect = new RectF();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mAnim.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        mAnim.cancel();
        super.onDetachedFromWindow();
    }



    @Override
    public void onDraw(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();

        int SqureWidth = findDesiredDimensionInRange(width, MIN_FRAME_WIDTH, MAX_FRAME_WIDTH);
        int SqureHeight = findDesiredDimensionInRange(height, MIN_FRAME_HEIGHT, MAX_FRAME_HEIGHT);

        int leftOffset = (width - SqureWidth) / 2;
        int topOffset = (height - SqureHeight) / 2;

        paint.setColor(maskColor);
        canvas.drawRect(0, 0, width, topOffset, paint);
        canvas.drawRect(0, topOffset, leftOffset, topOffset + SqureHeight + 1, paint);
        canvas.drawRect(leftOffset + SqureWidth + 1, topOffset, width, topOffset + SqureHeight + 1, paint);
        canvas.drawRect(0, topOffset + SqureHeight + 1, width, height, paint);
        paint.setColor(laserColor);
        paint.setAlpha(SCANNER_ALPHA[scannerAlpha]);
        scannerAlpha = (scannerAlpha + 1) % SCANNER_ALPHA.length;
        float fragtion = mAnim.getAnimatedFraction();
        float middle =  (SqureHeight*fragtion + topOffset);
        ovalRect.set(leftOffset + 2, middle - 2, leftOffset + SqureWidth - 1, middle + 2);
        canvas.drawOval(ovalRect, paint);

        postInvalidateOnAnimation();

    }

    private static int findDesiredDimensionInRange(int resolution, int hardMin, int hardMax) {
        int dim = 5 * resolution / 8; // Target 5/8 of each dimension
        if (dim < hardMin) {
            return hardMin;
        }
        if (dim > hardMax) {
            return hardMax;
        }
        return dim;
    }

    public void drawViewfinder() {
        postInvalidate();
    }


    public void addPossibleResultPoint(ResultPoint point) {
    }

}
