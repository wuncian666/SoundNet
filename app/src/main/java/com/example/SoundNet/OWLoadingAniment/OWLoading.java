package com.example.SoundNet.OWLoadingAniment;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class OWLoading extends View {
  private static final String TAG = "OWLoading";
  private final int ShowAnimatorFlag = 0x1137, HideAnimatorFlag = 0x1139;
  private final Point center = new Point();
  private final Point[] hexagonCenters = new Point[6];
  private final Hexagon[] hexagons = new Hexagon[7];
  private final float sin30 = (float) Math.sin(30f * 2f * Math.PI / 360f);
  private final float cos30 = (float) Math.cos(30f * 2f * Math.PI / 360f);
  private int viewWidth, viewHeight;
  private float space;
  private float hexagonRadius;
  private int color = Color.parseColor("#ff9900"); // 默认橙色
  private Paint paint;
  private ValueAnimator animator;
  private int nowAnimatorFlag = ShowAnimatorFlag;
  private final ValueAnimator.AnimatorUpdateListener animatorUpdateListener =
      new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {

          if (nowAnimatorFlag == ShowAnimatorFlag) { // 逐个显示出来
            hexagons[0].addScale();
            hexagons[0].addAlpha();
            for (int i = 0; i < hexagons.length - 1; i++) {
              if (hexagons[i].getScale() >= 0.7) {
                hexagons[i + 1].addScale();
                hexagons[i + 1].addAlpha();
              }
            }

            if (hexagons[6].getScale() == 1) { // 当最后一个六边形都完全显示时，切换模式，下一轮逐个消失
              nowAnimatorFlag = HideAnimatorFlag;
            }

          } else { // 逐个消失
            hexagons[0].subScale();
            hexagons[0].subAlpha();
            for (int i = 0; i < hexagons.length - 1; i++) {
              if (hexagons[i].getScale() <= 0.3) {
                hexagons[i + 1].subScale();
                hexagons[i + 1].subAlpha();
              }
            }
            if (hexagons[6].getScale() == 0) { // 当最后一个六边形都完全消失时，切换模式，下一轮逐个开始显示
              nowAnimatorFlag = ShowAnimatorFlag;
            }
          }
          invalidate();
        }
      };
  private boolean stopAnim = false;

  public OWLoading(Context context) {
    super(context);
    init(context, null);
  }

  public OWLoading(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context, attrs);
  }

  public OWLoading(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  private void init(Context context, AttributeSet attrs) {
    initAnimator();
  }

  private void initAnimator() {
    animator = ObjectAnimator.ofInt(0, 10);
    animator.setDuration(200);
    animator.addUpdateListener(animatorUpdateListener);
    animator.setRepeatCount(-1);
  }

  private void resetHexagons() {
    Log.v(TAG, "resetHexagons..");
    for (int i = 0; i < hexagons.length; i++) {
      hexagons[i].setScale(0);
      hexagons[i].setAlpha(0);
    }
  }

  private void initPaint() {
    paint = new Paint();
    paint.setAntiAlias(true);
    paint.setStyle(Paint.Style.FILL);
    paint.setColor(color);
    CornerPathEffect corEffect = new CornerPathEffect(hexagonRadius * 0.1f);
    paint.setPathEffect(corEffect);
  }

  /**
   * 设置颜色
   *
   * @param color
   */
  public void setColor(int color) {
    this.color = color;
    paint.setColor(color);
  }

  /** 开始动画 */
  public void startAnim() {
    stopAnim = false;
    initAnimator();
    animator.start();
  }

  /** 中止动画 */
  public void stopAnim() {
    stopAnim = true;
    if (animator != null) {
      animator.cancel();
      animator.removeAllListeners();
    }
    animator = null;
    nowAnimatorFlag = ShowAnimatorFlag;

    resetHexagons();
    invalidate();
  }

  private void initHexagonCenters() {
    float bigR = (float) ((1.5 * hexagonRadius + space) / cos30);
    hexagonCenters[0] = new Point(center.x - bigR * sin30, center.y - bigR * cos30);
    hexagonCenters[1] = new Point(center.x + bigR * sin30, center.y - bigR * cos30);
    hexagonCenters[2] = new Point(center.x + bigR, center.y);
    hexagonCenters[3] = new Point(center.x + bigR * sin30, center.y + bigR * cos30);
    hexagonCenters[4] = new Point(center.x - bigR * sin30, center.y + bigR * cos30);
    hexagonCenters[5] = new Point(center.x - bigR, center.y);

    for (int i = 0; i < 6; i++) {
      hexagons[i] = new Hexagon(hexagonCenters[i], hexagonRadius);
    }
    hexagons[6] = new Hexagon(center, hexagonRadius);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    for (int i = 0; i < 7; i++) {
      hexagons[i].drawHexagon(canvas, paint);
    }
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    viewHeight = getMeasuredHeight();
    viewWidth = getMeasuredWidth();
    if (viewWidth != 0 && viewHeight != 0) {
      center.x = viewWidth / 2f;
      center.y = viewHeight / 2f;
      float spaceRate = 1 / 100f;
      space = viewWidth <= viewHeight ? viewWidth * spaceRate : viewHeight * spaceRate;
      hexagonRadius = (float) ((viewWidth - 2 * space) / (3 * Math.sqrt(3)));
      initPaint();
      initHexagonCenters();
    }
  }

  /** 六边形 */
  private class Hexagon {

    // 六个顶点
    private final Point[] vertexs = new Point[6];
    public Point centerPoint;
    public float radius;
    // 缩放值
    private float scale = 0;
    // 透明度
    private int alpha = 0;

    public Hexagon(Point centerPoint, float radius) {
      this.centerPoint = centerPoint;
      this.radius = radius;
      calculatePointsPosition();
    }

    public void drawHexagon(Canvas canvas, Paint paint) {
      paint.setAlpha(alpha);
      canvas.drawPath(getPath(), paint);
    }

    private int calculatePointsPosition() {
      if (centerPoint == null) {
        return -1;
      }
      // 从最上方顺时针数1-6给各顶点标序号 共6个点
      vertexs[0] = new Point(centerPoint.x, centerPoint.y - radius * scale);
      vertexs[1] =
          new Point(centerPoint.x + radius * cos30 * scale, centerPoint.y - radius * sin30 * scale);
      vertexs[2] =
          new Point(centerPoint.x + radius * cos30 * scale, centerPoint.y + radius * sin30 * scale);
      vertexs[3] = new Point(centerPoint.x, centerPoint.y + radius * scale);
      vertexs[4] =
          new Point(centerPoint.x - radius * cos30 * scale, centerPoint.y + radius * sin30 * scale);
      vertexs[5] =
          new Point(centerPoint.x - radius * cos30 * scale, centerPoint.y - radius * sin30 * scale);
      return 1;
    }

    private Path getPath() {
      Path path = new Path();
      for (int i = 0; i < 6; i++) {
        if (i == 0) path.moveTo(vertexs[i].x, vertexs[i].y);
        else path.lineTo(vertexs[i].x, vertexs[i].y);
      }
      path.close();
      return path;
    }

    public int getAlpha() {
      return alpha;
    }

    /**
     * 设置透明度
     *
     * @param alpha
     */
    public void setAlpha(int alpha) {
      this.alpha = alpha;
    }

    public void addScale() {
      if (scale == 1) return;

      scale += 0.06;
      scale = scale > 1 ? 1 : scale;
      calculatePointsPosition();
    }

    public void subScale() {
      if (scale == 0) {
        return;
      }
      scale -= 0.06;
      scale = scale < 0 ? 0 : scale;
      calculatePointsPosition();
    }

    public void addAlpha() {
      if (alpha == 255) {
        return;
      }
      alpha += 15;
      alpha = alpha > 255 ? 255 : alpha;
    }

    public void subAlpha() {
      if (alpha == 0) {
        return;
      }
      alpha -= 15;
      alpha = alpha < 0 ? 0 : alpha;
    }

    /**
     * 获取当前缩放比例
     *
     * @return
     */
    public float getScale() {
      return scale;
    }

    /**
     * 设置缩放比例
     *
     * @param scale
     */
    public void setScale(float scale) {
      this.scale = scale;
      calculatePointsPosition();
    }
  }

  private class Point {
    public float x, y;

    public Point(float x, float y) {
      this.x = x;
      this.y = y;
    }

    public Point() {}
  }
}
