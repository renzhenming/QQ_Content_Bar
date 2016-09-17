package com.example.drag;

import java.util.Random;

import com.example.drag.DragLayoutView.OnDragStateChangedListener;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;

import android.os.Bundle;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.Interpolator;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.CycleInterpolator;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity {

	private ListView left;
	private ListView content;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// 声明一个DragLayoutView对象
		final DragLayoutView drag;
		left = (ListView) findViewById(R.id.leftlistview);
		content = (ListView) findViewById(R.id.contentlistview);

		left.setAdapter(new ArrayAdapter<String>(getApplicationContext(),
				android.R.layout.simple_list_item_1, Cheeses.sCheeseStrings) {
			/**
			 * 重写adapter的getview方法，获取到每个条目的view对象，就可以为每个条目设置一些需要的
			 * 属性，比如字体颜色大小等，重写的是adapter的方法，注意大括号加的位置
			 */
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View view = super.getView(position, convertView, parent);
				TextView textview = (TextView) view;
				textview.setTextColor(Color.WHITE);
				return view;
			}
		});

		content.setAdapter(new ArrayAdapter<String>(getApplicationContext(),
				android.R.layout.simple_list_item_1, Cheeses.NAMES) {
			/**
			 * 重写adapter的getview方法，获取到每个条目的view对象，就可以为每个条目设置一些需要的
			 * 属性，比如字体颜色大小等，重写的是adapter的方法，注意大括号加的位置
			 */
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View view = super.getView(position, convertView, parent);
				TextView textView = (TextView) view;
				textView.setTextColor(Color.BLACK);
				return view;
			}
		});

		// ★★★★24点击事件和监听事件
		drag = (DragLayoutView) findViewById(R.id.draglayout);
		final ImageView iv_header = (ImageView) findViewById(R.id.contentimage);
		final ImageView contentiamge = (ImageView) findViewById(R.id.contentimage);
		iv_header.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				drag.open(true);
			}
		});
		drag.setOnDragStateChangedListener(new OnDragStateChangedListener() {
			
			@Override
			public void onOpen() {
				System.out.println("打开");
				left.smoothScrollToPosition(new Random().nextInt(Cheeses.sCheeseStrings.length));
			}
			
			@Override
			public void onDraging(float percent) {
				System.out.println("拖动");
				ViewHelper.setAlpha(contentiamge, 1-percent);
			}
			
			@Override
			public void onClose() {
				System.out.println("关闭");
				//contentiamge.setTranslationX(translationX)
				ObjectAnimator ofFloat = ObjectAnimator.ofFloat(contentiamge, "translationX", 15f);
				ofFloat.setDuration(300);
				ofFloat.setInterpolator(new CycleInterpolator(10));
				ofFloat.start();
			}
		});

	}

}
