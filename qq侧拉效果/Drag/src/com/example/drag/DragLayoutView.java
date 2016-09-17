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
	private ViewGroup leftGroup; // �಼��
	private ViewGroup contentGroup; // ������
	private int mRange;// �Ӳ�������϶�ֵ
	private int measuredHeight; // �����ֵĸ߲���ֵ
	private int measuredWidth;// ...
	
	private CurrentState status = CurrentState.close;//��ǰ�϶�״̬
	
	public static enum CurrentState{
		open,close,draging;
	}

	private OnDragStateChangedListener listener;

	public DragLayoutView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		System.out.println("����");
		viewDragHelper = ViewDragHelper.create(this, 1.0f, cb);
	}

	public DragLayoutView(Context context, AttributeSet attrs) {
		this(context, attrs, -1);
	}

	public DragLayoutView(Context context) {
		this(context, null);
	}

	// ������-1 ����viewdraghelper����
	ViewDragHelper.Callback cb = new ViewDragHelper.Callback() {
	

		/**
		 * ������������������һ������true��һ������һ��ֵ��left)���ܱ�֤�����¼��Ŀ���ִ��
		 */
		@Override
		public boolean tryCaptureView(View child, int pointerId) {
			return true;
		}

		// ������-4 �ص�����
		// �÷��������ƶ�����Ϊˮƽ����,�����������ƶ���λ��,��ֻ����ĳ�����������ƶ�
		public int clampViewPositionHorizontal(View child, int left, int dx) {
			// �����9 �޸ķ�����
			if (child == contentGroup) {

				left = fixLeft(left);
			}
			return left;
		}

		// �����9 ��ȡ����
		private int fixLeft(int left) {
			// �����Խ�����Ʒ�Χ,�ͷ�����Ӧ��ֵ,����ں���Χ֮�ھͷ��ش�ֵ
			if (left < 0) {
				return 0;
			} else if (left > mRange) {
				return mRange;
			}

			return left;
		}

		// ������-6 ���ɱ�Ҫ�Ļص�����
		@Override
		public void onViewDragStateChanged(int state) {
			super.onViewDragStateChanged(state);
		}

		// �����10 �ػ沼�ּ��ݵͰ汾
		@Override
		public void onViewPositionChanged(View changedView, int left, int top,
				int dx, int dy) {
			// �����������󲼾�,����ϣ���󲼾ֲ���,�����������ƶ���Ӧ�ľ���
			if (changedView == leftGroup) {
				// �̶��󲼾ֵ�λ��,measuredWidth��measuredHeight����onsizechanged�����л�ȡ����
				leftGroup.layout(0, 0, measuredWidth, measuredHeight);
				// ���ݻ�������dx�����Ҳ���Ŀǰ����߽�λ�þ�������������Ҫ�ƶ�����λ��
				int newLeft = contentGroup.getLeft() + dx;
				// ������Ϊ�����Ѿ�Ϊ���ֵ�����ƶ�������������(mRange��)����Ҫ�ж���û��Խ��
				newLeft = fixLeft(newLeft);
				// �����λ��ȷ���Ҳ����ƶ������µ�λ��
				contentGroup.layout(newLeft, 0, newLeft + measuredWidth,
						measuredHeight);

			}
			// �����15 ���ð��涯��
			// ��viewλ�øı��ͬʱ����Ҫ���ð��涯����Ч��
			disPatchTouchEvent();
			// �ػ沼��,���ݵͰ汾,3.0һ��
			invalidate();
		}

		// �����15 ���ð��涯��
		protected void disPatchTouchEvent() {
			// ���涯���Ǻ�������ͬʱ���е�,������ͬ��ʱ����,���Ƕ������еĽ�������ͬ��,������Ҫ������ҳ��ӿ�ʼ������
			// ������������������ÿһʱ���������λ�õĶ������еİٷֱ�,���涯��Ҳ������һ�ٷֱȽ����Լ��Ľ���
			float percent = contentGroup.getLeft() * 1.0f / mRange;
			// Ϊ�󲼾����ð��涯��,����Ч��
			// leftGroup.setScaleX(percent * 0.5f + 0.5f);
			// leftGroup.setScaleY(percent * 0.5f + 0.5f);
			/**
			 * �ӿ�TypeEvaluator,�鿴Դ��,�����ĸ�ʵ���� ʹ��evaluate();ʡȥƵ��������鷳
			 * ��������setScaleX,Y�����޷����ݵͰ汾����������Ҫô�޸�api�汾��Ҫôʹ�õ��������ݹ��ߣ�����ѡ��ڶ���
			 */
			// leftGroup.setScaleX(evaluate(percent, 0.5f, 1.0f));
			// leftGroup.setScaleY(evaluate(percent, 0.5f, 1.0f));
			/**
			 * ʹ��һ����nineoldandroids��jar�������ݵͰ汾,����jar�� ʹ��ViewHelper���������ط���,ע�⵼��
			 */
			// �����17
			// ������ҳ������Ŷ���
			initAnim(percent);
			// �����22 ����һ��������¼��ǰ״̬,Ŀǰ����״̬��status(Ĭ���ǹر�״̬close),������ִ�и���״̬������statusֵ�ı�
			CurrentState lastStatus = status;
			status = updateStatus(percent);
			// �����23
			if(listener != null){
				listener.onDraging(percent);
			}
			// ״̬�仯, ִ�м����ص�
//			if(lastStatus != status && listener != null){
//				if(status == CurrentState.open){
//					listener.onOpen();
//				}else if (status == CurrentState.close) {
//					listener.onClose();
//				}
//			}
			if(lastStatus == status){
				//˵����ǰ��״̬û�иı䣬����Ҫ���ýӿ�
			}else if(lastStatus != status && listener != null){
				//״̬�ɹر�״̬�ı䣬�����ֽ�������������
				if(status == CurrentState.open){
					listener.onOpen();
				}else if(status == CurrentState.close){
					listener.onClose();
				}
			}
		}
		// �����21 ���ûص�״̬
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
			// ������ҳ���ƽ�ƶ���
			ViewHelper.setTranslationX(leftGroup,
					evaluate(percent, -measuredWidth * 0.5f, 0));
			ViewHelper.setAlpha(leftGroup, evaluate(percent, 0.5f, 1.0f));

			// ������ҳ������Ŷ���
			ViewHelper.setScaleX(contentGroup, evaluate(percent, 1.0f, 0.9f));
			ViewHelper.setScaleY(contentGroup, evaluate(percent, 1.0f, 0.9f));
			// ���ñ����Ľ��䶯��// �����19 ���ø������ķ���
			getBackground().setColorFilter(
					(Integer) evaluateColor(percent, Color.BLACK,
							Color.TRANSPARENT), Mode.SRC_OVER);
		}

		// �����18 �����������Դ���и��Ƶģ��鿴ArgbEvaluator�ࣩ
		// ����֮ǰ��������evaluate,Color���Լ����ϵ�,Ϊ������һ��evaluate�������ֿ�
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

		// �����16 �����������Դ���и��Ƶģ��鿴FloatEvaluator�ࣩ
		public Float evaluate(float fraction, Number startValue, Number endValue) {
			float startFloat = startValue.floatValue();
			return startFloat + fraction * (endValue.floatValue() - startFloat);
		}

		// �����11 ���ͷ�ʱȷ���Ӳ��ֵ�λ��
		@Override
		public void onViewReleased(View releasedChild, float xvel, float yvel) {
			// releasedChild ���ͷŵ��ӿؼ�
			// xvel �ͷ�ʱˮƽ�����ٶ�, ����+, ����-
			// yvel �ͷ�ʱ��ֱ�����ٶ�, ����+, ����-
			// �ȿ��Ǵ򿪵�״̬,����֮��ȫ�ǹر�
			if (xvel == 0 && releasedChild.getLeft() > mRange / 2) {
				// ��
				open();
			} else if (xvel > 0) {
				// ��,��Ϊ����Ϊ��,���Դ���0һ���������ƶ���
				open();
			} else {
				// �����ϱ���������������Ķ�����Ҫ�رյ�״̬
				close();
			}
		}

		// �����8 ����ȡ����mrange���ù���
		@Override
		public int getViewHorizontalDragRange(View child) {
			// ��������޶����ƶ��ķ�Χ��С,����û���޶��ƶ��ľ���λ��,�޶�����λ�õ���clampViewPositionHorizontal����
			// ��ȡ�ؼ�ˮƽ�����Ͽ��ƶ������Χ,����������Ҫһ����Χ����ֵ,��ô�����ֵ��λ�ȡ,
			// ������onmeasure()��Ҳ������onsizechanged()�л�ȡ,ȥ��д,��ȡ��֮�����ø�����ֵ
			return mRange;
		};

	};

	// �����12 �������صķ���
	protected void close() {
		close(true);

	}

	protected void open() {
		open(true);
	}

	/**
	 * ��������open��close�����ط���,ʵ��ƽ���ƶ�
	 * 
	 * @param isSmooth�����Ƿ���Ҫƽ���ƶ�
	 *            ,true��false���� Ϊʲô��ֱ�Ӵ����������ķ�����,��Ϊ�ղη���Ĭ����ƽ���ƶ�,
	 *            ������Ϊ����������в���Ҫƽ���ƶ���ʱ��,��Ȼ����������Ҳ�ǿ��Ե�,�����Եø��淶
	 */
	// �����13 �������ص����ط���
	protected void open(boolean isSmooth) {
		int finalLeft = mRange;
		if (isSmooth) {
			// ����true����û���ƶ���ָ����λ��
			if (viewDragHelper.smoothSlideViewTo(contentGroup, finalLeft, 0)) {
				// ����������ػ�,ע��,����ֻ��������û�������ķ���,����Ҫ��һ������һ�����д���,�Ǹ�������computeScroll
				ViewCompat.postInvalidateOnAnimation(this);
			}
		} else {
			contentGroup.layout(finalLeft, 0, finalLeft + measuredWidth,
					measuredHeight);
		}

	}

	// �����13 �������ص����ط���
	protected void close(boolean isSmooth) {
		int finalLeft = 0;

		if (isSmooth) {
			// ������֮������������,��һ����ƽ���ƶ���ָ��λ��,�ڶ����Ƿ���booleanֵ,true����û���ƶ���ָ��λ��,��Ҫ����һ��
			// ����,false�ɹ�
			boolean smoothSlideViewTo = viewDragHelper.smoothSlideViewTo(
					contentGroup, finalLeft, 0);
			// ����ƶ�ʧ������һ������
			if (smoothSlideViewTo) {
				// ΪʲôҪ��this?��contentview�ɷ�?�ҵĲ����ǿ���ƽ���ƶ�����viewdraghelper,����
				// û��ִ�гɹ�,��������ȻҪ������
				// //����������ػ�,ע��,����ֻ��������û�������ķ���,����Ҫ��һ������һ�����д���,�Ǹ�������computeScroll
				ViewCompat.postInvalidateOnAnimation(this);// ����drawchild
				// -��child.draw->��computScroll����һ��ѭ��֪��������������ѭ��
			}
		} else {
			contentGroup.layout(0, 0, measuredWidth, measuredHeight);
		}

	}

	// �����14 ά�ֶ����ĳ���,ֻ��ʵ�������������ʵ��ƽ���ƶ�,ֻ�����ϱߵ�open(true)û��Ч��,�������ƶ��¼�����ʧ��
	@Override
	public void computeScroll() {
		super.computeScroll();
		// true ��ʾ����û���ƶ���ָ��λ�á�
		// ��Ҫ����������ػ�
		if (viewDragHelper.continueSettling(true)) {
			ViewCompat.postInvalidateOnAnimation(this); // ����drawchild
														// -��child.draw->��computScroll����һ��ѭ��֪��������������ѭ��
		}
	}

	// ������-2 ת���������¼�

	public boolean onInterceptTouchEvent(MotionEvent ev) {
		// ��viewdraghelper�������¼�

		return viewDragHelper.shouldInterceptTouchEvent(ev);

	};

	// ������-3 �������¼�
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// ���������viewdraghelper�������¼�
		try {
			viewDragHelper.processTouchEvent(event);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	// ������-5 xml���͵�child view ������ʱ�����������,����������new������,
	// �������ȡ���ֵ��ӿؼ�,����һ����ʼ����ɾͻ�ȡ����,ʡȥ��Ƶ����ȡ������鷳
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		// �ݴ��ж�
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

	// ������7-��ȡ�ӿؼ��ƶ���Χ
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// �����������ȡ,����Ҳ����ʹ��һ���µ�api,��onmeasure��onlayout�����м��и�����
		// onsizechanaged������ִ��,������Ҳ���Ի�ȡ���ؼ��Ŀ��
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	// ������7-��ȡ�ӿؼ�����ƶ���Χ
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

	// �������״̬�Ļص��ӿ�
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
