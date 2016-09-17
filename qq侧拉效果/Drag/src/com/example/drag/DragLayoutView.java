package com.example.drag;

import com.nineoldandroids.view.ViewHelper;

import android.R.color;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class DragLayoutView extends FrameLayout {
	private ViewDragHelper viewDragHelper;
	private ViewGroup leftGroup; // 侧布局
	private ViewGroup contentGroup; // 主布局
	private int mRange;// 子布局最大拖动值
	private int measuredHeight; // 父布局的高测量值
	private int measuredWidth;// ...
	
	private CurrentState status = CurrentState.close;//当前拖动状态
	
	public static enum CurrentState{
		open,close,draging;
	}

	private OnDragStateChangedListener listener;

	public DragLayoutView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		System.out.println("开启");
		viewDragHelper = ViewDragHelper.create(this, 1.0f, cb);
	}

	public DragLayoutView(Context context, AttributeSet attrs) {
		this(context, attrs, -1);
	}

	public DragLayoutView(Context context) {
		this(context, null);
	}

	// ★★★★★-1 创建viewdraghelper对象
	ViewDragHelper.Callback cb = new ViewDragHelper.Callback() {
	

		/**
		 * 起码满足这两个方法一个返回true，一个返回一个值（left)才能保证滑动事件的可以执行
		 */
		@Override
		public boolean tryCaptureView(View child, int pointerId) {
			return true;
		}

		// ★★★★★-4 回掉方法
		// 该方法决定移动方向为水平方向,并且限制了移动的位置,既只能在某坐标区间中移动
		public int clampViewPositionHorizontal(View child, int left, int dx) {
			// ★★★★9 修改方法体
			if (child == contentGroup) {

				left = fixLeft(left);
			}
			return left;
		}

		// ★★★★9 提取方法
		private int fixLeft(int left) {
			// 如果逾越了限制范围,就返回相应的值,如果在合理范围之内就返回此值
			if (left < 0) {
				return 0;
			} else if (left > mRange) {
				return mRange;
			}

			return left;
		}

		// ★★★★★-6 生成必要的回掉方法
		@Override
		public void onViewDragStateChanged(int state) {
			super.onViewDragStateChanged(state);
		}

		// ★★★★10 重绘布局兼容低版本
		@Override
		public void onViewPositionChanged(View changedView, int left, int top,
				int dx, int dy) {
			// 如果点击的是左布局,我们希望左布局不懂,但是主布局移动相应的距离
			if (changedView == leftGroup) {
				// 固定左布局的位置,measuredWidth和measuredHeight是在onsizechanged方法中获取到的
				leftGroup.layout(0, 0, measuredWidth, measuredHeight);
				// 根据滑动距离dx加上右布局目前的左边界位置就是理论上他将要移动到的位置
				int newLeft = contentGroup.getLeft() + dx;
				// 但是因为我们已经为布局的最大移动区间做了限制(mRange内)所以要判断有没有越界
				newLeft = fixLeft(newLeft);
				// 有这个位置确定右布局移动到的新的位置
				contentGroup.layout(newLeft, 0, newLeft + measuredWidth,
						measuredHeight);

			}
			// ★★★★15 设置伴随动画
			// 在view位置改变的同时还需要设置伴随动画的效果
			disPatchTouchEvent();
			// 重绘布局,兼容低版本,3.0一下
			invalidate();
		}

		// ★★★★15 设置伴随动画
		protected void disPatchTouchEvent() {
			// 伴随动画是和主动画同时进行的,所以相同的时间下,他们动画进行的进度是相同的,所以需要根据主页面从开始滑动到
			// 滑动结束整个过程中每一时间点下所处位置的动画进行的百分比,伴随动画也依据这一百分比进行自己的进度
			float percent = contentGroup.getLeft() * 1.0f / mRange;
			// 为左布局设置伴随动画,渐变效果
			// leftGroup.setScaleX(percent * 0.5f + 0.5f);
			// leftGroup.setScaleY(percent * 0.5f + 0.5f);
			/**
			 * 接口TypeEvaluator,查看源码,他有四个实现类 使用evaluate();省去频繁计算的麻烦
			 * 但是由于setScaleX,Y方法无法兼容低版本，所以这里要么修改api版本，要么使用第三方兼容工具，这里选择第二种
			 */
			// leftGroup.setScaleX(evaluate(percent, 0.5f, 1.0f));
			// leftGroup.setScaleY(evaluate(percent, 0.5f, 1.0f));
			/**
			 * 使用一个叫nineoldandroids的jar包来兼容低版本,导入jar包 使用ViewHelper对象调用相关方法,注意导包
			 */
			// ★★★★17
			// 设置左页面的缩放动画
			initAnim(percent);
			// ★★★★22 定义一个变量记录当前状态,目前最后的状态是status(默认是关闭状态close),接下来执行更新状态方法，status值改变
			CurrentState lastStatus = status;
			status = updateStatus(percent);
			// ★★★★23
			if(listener != null){
				listener.onDraging(percent);
			}
			// 状态变化, 执行监听回调
//			if(lastStatus != status && listener != null){
//				if(status == CurrentState.open){
//					listener.onOpen();
//				}else if (status == CurrentState.close) {
//					listener.onClose();
//				}
//			}
			if(lastStatus == status){
				//说明当前的状态没有改变，不需要设置接口
			}else if(lastStatus != status && listener != null){
				//状态由关闭状态改变，有两种结果，分情况考虑
				if(status == CurrentState.open){
					listener.onOpen();
				}else if(status == CurrentState.close){
					listener.onClose();
				}
			}
		}
		// ★★★★21 设置回调状态
		protected CurrentState updateStatus(float percent) {
			if(percent == 0){
				return CurrentState.close;
			}else if(percent == 1){
				return CurrentState.open;
			}
			return CurrentState.draging;
		}


		private void initAnim(float percent) {
			ViewHelper.setScaleX(leftGroup, evaluate(percent, 0.5f, 1.0f));
			ViewHelper.setScaleY(leftGroup, evaluate(percent, 0.5f, 1.0f));
			// 设置左页面的平移动画
			ViewHelper.setTranslationX(leftGroup,
					evaluate(percent, -measuredWidth * 0.5f, 0));
			ViewHelper.setAlpha(leftGroup, evaluate(percent, 0.5f, 1.0f));

			// 设置主页面的缩放动画
			ViewHelper.setScaleX(contentGroup, evaluate(percent, 1.0f, 0.9f));
			ViewHelper.setScaleY(contentGroup, evaluate(percent, 1.0f, 0.9f));
			// 设置背景的渐变动画// ★★★★19 调用复制来的方法
			getBackground().setColorFilter(
					(Integer) evaluateColor(percent, Color.BLACK,
							Color.TRANSPARENT), Mode.SRC_OVER);
		}

		// ★★★★18 这个方法是在源码中复制的（查看ArgbEvaluator类）
		// 复制之前是名字是evaluate,Color是自己加上的,为了与另一个evaluate方法区分开
		public Object evaluateColor(float fraction, Object startValue,
				Object endValue) {
			int startInt = (Integer) startValue;
			int startA = (startInt >> 24) & 0xff;
			int startR = (startInt >> 16) & 0xff;
			int startG = (startInt >> 8) & 0xff;
			int startB = startInt & 0xff;

			int endInt = (Integer) endValue;
			int endA = (endInt >> 24) & 0xff;
			int endR = (endInt >> 16) & 0xff;
			int endG = (endInt >> 8) & 0xff;
			int endB = endInt & 0xff;

			return (int) ((startA + (int) (fraction * (endA - startA))) << 24)
					| (int) ((startR + (int) (fraction * (endR - startR))) << 16)
					| (int) ((startG + (int) (fraction * (endG - startG))) << 8)
					| (int) ((startB + (int) (fraction * (endB - startB))));
		}

		// ★★★★16 这个方法是在源码中复制的（查看FloatEvaluator类）
		public Float evaluate(float fraction, Number startValue, Number endValue) {
			float startFloat = startValue.floatValue();
			return startFloat + fraction * (endValue.floatValue() - startFloat);
		}

		// ★★★★11 在释放时确定子布局的位置
		@Override
		public void onViewReleased(View releasedChild, float xvel, float yvel) {
			// releasedChild 被释放的子控件
			// xvel 释放时水平方向速度, 向右+, 向左-
			// yvel 释放时竖直方向速度, 向下+, 向上-
			// 先考虑打开的状态,除此之外全是关闭
			if (xvel == 0 && releasedChild.getLeft() > mRange / 2) {
				// 打开
				open();
			} else if (xvel > 0) {
				// 打开,因为向右为正,所以大于0一定是向右移动的
				open();
			} else {
				// 除了上边两种情况下其他的都是需要关闭的状态
				close();
			}
		}

		// ★★★★8 将获取到的mrange设置过来
		@Override
		public int getViewHorizontalDragRange(View child) {
			// 这个方法限定了移动的范围大小,但是没有限定移动的具体位置,限定具体位置的是clampViewPositionHorizontal方法
			// 获取控件水平方向上可移动的最大范围,所以这里需要一个范围的数值,那么这个数值如何获取,
			// 可以在onmeasure()中也可以在onsizechanged()中获取,去重写,获取到之后设置给返回值
			return mRange;
		};

	};

	// ★★★★12 创建开关的方法
	protected void close() {
		close(true);

	}

	protected void open() {
		open(true);
	}

	/**
	 * 创建两个open和close的重载方法,实现平滑移动
	 * 
	 * @param isSmooth代表是否需要平滑移动
	 *            ,true是false不是 为什么不直接创建待参数的方法呢,因为空参方法默认是平滑移动,
	 *            这是因为其他情况下有不需要平滑移动的时候,当然不适用重载也是可以的,这样显得更规范
	 */
	// ★★★★13 创建开关的重载方法
	protected void open(boolean isSmooth) {
		int finalLeft = mRange;
		if (isSmooth) {
			// 返回true代表没有移动到指定的位置
			if (viewDragHelper.smoothSlideViewTo(contentGroup, finalLeft, 0)) {
				// 引发界面的重绘,注意,这里只是引发并没有真正的发生,还需要进一步在另一方法中处理,那个方法是computeScroll
				ViewCompat.postInvalidateOnAnimation(this);
			}
		} else {
			contentGroup.layout(finalLeft, 0, finalLeft + measuredWidth,
					measuredHeight);
		}

	}

	// ★★★★13 创建开关的重载方法
	protected void close(boolean isSmooth) {
		int finalLeft = 0;

		if (isSmooth) {
			// 这句代码之行了两个动作,第一就是平滑移动到指定位置,第二就是返回boolean值,true代表没有移动到指定位置,需要做进一步
			// 处理,false成功
			boolean smoothSlideViewTo = viewDragHelper.smoothSlideViewTo(
					contentGroup, finalLeft, 0);
			// 如果移动失败做进一步处理
			if (smoothSlideViewTo) {
				// 为什么要用this?用contentview可否?我的猜想是开启平滑移动的是viewdraghelper,所以
				// 没有执行成功,接下来当然要处理他
				// //引发界面的重绘,注意,这里只是引发并没有真正的发生,还需要进一步在另一方法中处理,那个方法是computeScroll
				ViewCompat.postInvalidateOnAnimation(this);// 引发drawchild
				// -＞child.draw->　computScroll这是一个循环知道满足条件跳出循环
			}
		} else {
			contentGroup.layout(0, 0, measuredWidth, measuredHeight);
		}

	}

	// ★★★★14 维持动画的持续,只有实现这个方法才能实现平滑移动,只是用上边的open(true)没有效果,反而连移动事件都丢失了
	@Override
	public void computeScroll() {
		super.computeScroll();
		// true 表示动画没有移动到指定位置。
		// 需要引发界面的重绘
		if (viewDragHelper.continueSettling(true)) {
			ViewCompat.postInvalidateOnAnimation(this); // 引发drawchild
														// -＞child.draw->　computScroll这是一个循环知道满足条件跳出循环
		}
	}

	// ★★★★★-2 转交处理触摸事件

	public boolean onInterceptTouchEvent(MotionEvent ev) {
		// 由viewdraghelper处理触摸事件

		return viewDragHelper.shouldInterceptTouchEvent(ev);

	};

	// ★★★★★-3 处理触摸事件
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// 在这里调用viewdraghelper处理触摸事件
		try {
			viewDragHelper.processTouchEvent(event);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	// ★★★★★-5 xml类型的child view 添加完成时调用这个方法,不包括代码new出来的,
	// 在这里获取布局的子控件,布局一旦初始化完成就获取对象,省去了频繁获取对象的麻烦
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		// 容错判断
		if (getChildCount() < 2) {
			throw new IllegalStateException(
					"you viewgroup must contains at least 2 child");
		}
		if (!(getChildAt(0) instanceof ViewGroup || !(getChildAt(1) instanceof ViewGroup))) {
			throw new IllegalArgumentException(
					"the child you added to your viewgroup should be an instance of viewgroup");
		}
		leftGroup = (ViewGroup) getChildAt(0);
		contentGroup = (ViewGroup) getChildAt(1);
	}

	// ★★★★★7-获取子控件移动范围
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// 可以在这里获取,但是也可以使用一个新的api,在onmeasure和onlayout方法中间有个方法
		// onsizechanaged方法会执行,在这里也可以获取到控件的宽高
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	// ★★★★★7-获取子控件最大移动范围
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		measuredWidth = getMeasuredWidth();
		measuredHeight = getMeasuredHeight();
		mRange = (int) (measuredWidth * 0.6f);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
	}

	// 定义监听状态的回调接口
	public interface OnDragStateChangedListener {
		public void onOpen();

		public void onClose();

		public void onDraging(float percent);
	}

	public void setOnDragStateChangedListener(
			OnDragStateChangedListener listener) {
		this.listener = listener;
	}

}
