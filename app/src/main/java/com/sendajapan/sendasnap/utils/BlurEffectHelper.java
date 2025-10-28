package com.sendajapan.sendasnap.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.view.View;

public class BlurEffectHelper {

    public static void applyBlurToView(View view, float radius) {
        if (view == null)
            return;

        // Create a bitmap from the view
        Bitmap bitmap = createBitmapFromView(view);
        if (bitmap == null)
            return;

        // Apply blur effect
        Bitmap blurredBitmap = blurBitmap(view.getContext(), bitmap, radius);

        // Set the blurred bitmap as background
        view.setBackground(new android.graphics.drawable.BitmapDrawable(
                view.getContext().getResources(), blurredBitmap));
    }

    private static Bitmap createBitmapFromView(View view) {
        if (view.getWidth() <= 0 || view.getHeight() <= 0) {
            return null;
        }

        Bitmap bitmap = Bitmap.createBitmap(
                view.getWidth(),
                view.getHeight(),
                Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    private static Bitmap blurBitmap(Context context, Bitmap bitmap, float radius) {
        if (bitmap == null)
            return null;

        try {
            RenderScript rs = RenderScript.create(context);
            ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));

            Allocation inputAllocation = Allocation.createFromBitmap(rs, bitmap);
            Allocation outputAllocation = Allocation.createTyped(rs, inputAllocation.getType());

            blurScript.setRadius(radius);
            blurScript.setInput(inputAllocation);
            blurScript.forEach(outputAllocation);

            Bitmap blurredBitmap = bitmap.copy(bitmap.getConfig(), true);
            outputAllocation.copyTo(blurredBitmap);

            inputAllocation.destroy();
            outputAllocation.destroy();
            blurScript.destroy();
            rs.destroy();

            return blurredBitmap;
        } catch (Exception e) {
            // Fallback to original bitmap if blur fails
            return bitmap;
        }
    }

    public static void applyGlassEffect(View view) {
        if (view == null)
            return;

        // Create a glass effect using a semi-transparent overlay
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(0x80FFFFFF); // Semi-transparent white

        // This is a simplified glass effect
        // In a real implementation, you might want to use a custom drawable
        view.setBackgroundColor(0x80FFFFFF);
    }
}
