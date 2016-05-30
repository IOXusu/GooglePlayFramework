package activity.example.chaosxu.googleplayframework.ui.widget;

import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import java.util.List;

import activity.example.chaosxu.googleplayframework.R;
import activity.example.chaosxu.googleplayframework.util.Utils;

/**
 * 负责管理界面加载数据的逻辑
 * @author Administrator
 *
 */
public abstract class LoadingPage extends FrameLayout{
	//定义3种状态常量
	enum PageState{
		STATE_LOADING,//加载中的状态
		STATE_ERROR,//加载失败的状态
		STATE_SUCCESS;//加载成功的状态
	}
	private PageState mState = PageState.STATE_LOADING;//表示界面当前的state，默认是加载中
	private View loadingView;
	private View errorView;
	private View successView;
	
	public LoadingPage(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initLoadingPage();
	}
	public LoadingPage(Context context, AttributeSet attrs) {
		super(context, attrs);
		initLoadingPage();
	}
	public LoadingPage(Context context) {
		super(context);
		initLoadingPage();
	}
	
	/**
	 * 天然地往LoadingPage中添加3个view
	 */
	private void initLoadingPage(){
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
		//1.依次添加3个view对象
		if(loadingView==null){
			loadingView = View.inflate(getContext(), R.layout.page_loading, null);
		}
		addView(loadingView,params);
		
		if(errorView==null){
			errorView = View.inflate(getContext(), R.layout.page_error, null);
			Button btn_reload = (Button) errorView.findViewById(R.id.btn_reload);
			btn_reload.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					//1.先显示loadingView
					mState = PageState.STATE_LOADING;
					showPage();
					//2.重新加载数据，然后刷新Page
					loadData();
				}
			});
		}
		addView(errorView,params);
		
		if(successView==null){
			successView = createSuccessView();//需要不固定的successView
		}
		if(successView==null){
			throw new IllegalArgumentException("The method createSuccessView() can not return null!");
		}else {
			addView(successView,params);
		}
		
		//2.显示默认的loadingView
		showPage();

		//3.加载数据
		loadData();
	}
	/**
	 * 根据当前的mState显示对应的View
	 */
	private void showPage(){
		//1.先隐藏所有的view
		loadingView.setVisibility(View.INVISIBLE);
		errorView.setVisibility(View.INVISIBLE);
		successView.setVisibility(View.INVISIBLE);
		//2.谁的状态谁显示
		switch (mState) {
		case STATE_LOADING://加载中的状态
			loadingView.setVisibility(View.VISIBLE);
			break;
		case STATE_ERROR://加载失败的状态
			errorView.setVisibility(View.VISIBLE);
			break;
		case STATE_SUCCESS://加载成功的状态
			successView.setVisibility(View.VISIBLE);
			break;
		}
	}
	/**
	 * 请求数据，然后根据回来的数据去刷新Page
	 */
	public void loadData(){
		new Thread(){
			public void run() {
				try {
					//模拟请求服务器的耗时
					SystemClock.sleep(1000);

					//1.去服务器请求数据，
					Object data = onLoad();
					//2.判断data是否为空，如果为空则为error，否则为success状态
					mState = checkState(data);
					//3.根据新的state，更新page
					//在主线程更新UI
					Utils.runOnUIThread(new Runnable() {
						@Override
						public void run() {
							showPage();

						}
					});
				}catch (Exception e){
					e.printStackTrace();
				}

			}
		}.start();
	}

	private PageState checkState(Object data){
		if(data==null){
			return PageState.STATE_ERROR;
		}else{
			if(data instanceof List){
				List list = (List) data;
				if(list.size()==0){
					return PageState.STATE_ERROR;
				}
			}
			return PageState.STATE_SUCCESS;
		}
	}
	
	/**
	 * 获取successView，由于每个界面的successView都不一样，那么应该由每个界面自己实现
	 * @return
	 */
	public abstract View createSuccessView();
	
	/**
	 * 加载数据，由于我不关心具体的数据类型和过程，由子类实现
	 * @return
	 */
	public abstract Object onLoad();

}
