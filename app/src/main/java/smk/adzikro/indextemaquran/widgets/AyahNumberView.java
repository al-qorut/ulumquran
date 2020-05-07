package smk.adzikro.indextemaquran.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import smk.adzikro.indextemaquran.R;


public class AyahNumberView extends View {
  private int boxColor;
  private int nightBoxColor;
  private int boxWidth;
  private int boxHeight;
  private int padding;
  private int textSize;
  private boolean favorite;
  private String suraAyah;
  private boolean isNightMode;
  private Paint boxPaint;
  private TextPaint textPaint;
  private StaticLayout textLayout;

  public AyahNumberView(Context context) {
    this(context, null);
  }

  public AyahNumberView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    int textColor = 0;
    if (attrs != null) {
      TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.AyahNumberView);
      textColor = ta.getColor(R.styleable.AyahNumberView_android_textColor, textColor);
      boxColor = ta.getColor(R.styleable.AyahNumberView_backgroundColor, boxColor);
      nightBoxColor = ta.getColor(R.styleable.AyahNumberView_nightBackgroundColor, nightBoxColor);
      boxWidth = ta.getDimensionPixelSize(R.styleable.AyahNumberView_verseBoxWidth, boxWidth);
      boxHeight = ta.getDimensionPixelSize(R.styleable.AyahNumberView_verseBoxHeight, boxHeight);
      textSize = ta.getDimensionPixelSize(R.styleable.AyahNumberView_android_textSize, textSize);
      ta.recycle();
    }

    boxPaint = new Paint();
    boxPaint.setColor(Color.TRANSPARENT);
    textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    textPaint.setColor(textColor);
   // if(Build.VERSION.SDK_INT>=26)
    textPaint.setTypeface(Typeface.createFromAsset(context.getAssets(), "font/quran.ttf"));
    textPaint.setTextSize(textSize);
  }

  public void setAyahString(@NonNull String suraAyah) {
    if (!suraAyah.equals(this.suraAyah)) {
      this.suraAyah = suraAyah;
      this.textLayout = new StaticLayout(suraAyah, textPaint, boxWidth,
          Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);
      invalidate();
    }
  }

  public void setAyahFavorite(boolean favorite){
    if (this.favorite != favorite) {
      this.favorite= favorite;
      invalidate();
    }
  }

  public void setNightMode(boolean isNightMode) {
    if (this.isNightMode != isNightMode) {
      boxPaint.setColor(isNightMode ? nightBoxColor : boxColor);
      this.isNightMode = isNightMode;
      invalidate();
    }
  }

  public int getBoxCenterX() {
    return padding + (boxWidth / 2);
  }

  public int getBoxBottomY() {
    return padding + boxHeight;
  }

  public void setTextColor(int textColor) {
    textPaint.setColor(textColor);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    padding = (getMeasuredHeight() - boxHeight) / 2;
  }

  @Override
  protected void onDraw(Canvas canvas) {
    canvas.drawRect(padding, padding, padding + boxWidth, padding + boxHeight, boxPaint);
    if (this.textLayout != null) {
      int startY = padding + ((boxHeight - this.textLayout.getHeight()) / 2);
      if(favorite) {
        Bitmap bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.ic_favorite_black_24dp);
        canvas.drawBitmap(bitmap, boxWidth+padding, boxHeight/2+padding/2, null);
      }
      canvas.translate(padding, startY);
      this.textLayout.draw(canvas);
      canvas.translate(padding, -startY);
    }
  }
}
