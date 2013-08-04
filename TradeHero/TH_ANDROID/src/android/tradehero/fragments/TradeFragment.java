/**
 * TradeFragment.java 
 * TradeHero
 *
 * Created by @author Siddesh Bingi on Jul 24, 2013
 */
package android.tradehero.fragments;

import java.util.HashMap;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.tradehero.activities.R;
import android.tradehero.application.App;
import android.tradehero.application.Config;
import android.tradehero.cache.ImageLoader;
import android.tradehero.cache.ImageLoader.ImageLoadingListener;
import android.tradehero.fragments.TrendingDetailFragment.YahooQuoteUpdateListener;
import android.tradehero.models.Trend;
import android.tradehero.utills.DateUtils;
import android.tradehero.utills.ImageUtils;
import android.tradehero.utills.Logger;
import android.tradehero.utills.Logger.LogLevel;
import android.tradehero.utills.YUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class TradeFragment extends Fragment implements YahooQuoteUpdateListener {
	
	private final static String TAG = TradeFragment.class.getSimpleName();
	
	public final static String HEADER = "header";
	public final static String BUY_DETAIL_STR = "buy_detail_str";
	public final static String LAST_PRICE = "last_price";
	public final static String QUANTITY = "quantity";
	
	
	private ImageView mStockBgLogo;
	private ImageView mStockLogo;
	private ImageView mStockChart;
	
	private TextView tvLastPrice;
	private TextView tvBidPrice;
	private TextView tvAskPrice;
	private TextView tvPriceAsOf;
	private TextView tvCashAvailable;
	private TextView tvQuantity;
	private TextView tvTradeValue;
	
	private Button mBtn5k;
	private Button mBtn10k;
	private Button mBtn25k;
	private Button mBtn50k;
	private Button mBuyBtn;
	
	private ProgressBar mProgressBar;
	
	private SeekBar mSlider;
	
	private Trend trend;
	private ImageLoader mImageLoader;
	
	double lastPrice;
	int sliderIncrement = 0;
	//int maxQuantity = 0;
	int mQuantity = 0;
	int sliderMaxValue = 0;
	
	
	//dummy
	private int defaultCashAvailable = 71543;
	private int defaultTHTransferFee = 10;
	private boolean isTransactionTypeBuy = true;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = null;
		view = inflater.inflate(R.layout.fragment_trade, container, false);
		initViews(view);
		return view;
	}
	

	private void initViews(View v) {
		mImageLoader = new ImageLoader(getActivity());
		
		mStockBgLogo = (ImageView) v.findViewById(R.id.stock_bg_logo);
		mStockLogo = (ImageView) v.findViewById(R.id.stock_logo);
		mStockChart = (ImageView) v.findViewById(R.id.stock_chart);
		
		tvLastPrice = (TextView) v.findViewById(R.id.last_price);
		tvBidPrice = (TextView) v.findViewById(R.id.bid_price);
		tvAskPrice = (TextView) v.findViewById(R.id.ask_price);
		tvPriceAsOf = (TextView) v.findViewById(R.id.vprice_as_of);
		tvCashAvailable = (TextView) v.findViewById(R.id.vcash_available);
		tvQuantity = (TextView) v.findViewById(R.id.vquantity);
		tvTradeValue = (TextView) v.findViewById(R.id.vtrade_value);
		mProgressBar = (ProgressBar) v.findViewById(R.id.progress);
		
		mBtn5k = (Button) v.findViewById(R.id.toggle5k);
		mBtn10k = (Button) v.findViewById(R.id.toggle10k);
		mBtn25k = (Button) v.findViewById(R.id.toggle25k);
		mBtn50k = (Button) v.findViewById(R.id.toggle50k);
		mBtn5k.setOnClickListener(onClickListener);
		mBtn10k.setOnClickListener(onClickListener);
		mBtn25k.setOnClickListener(onClickListener);
		mBtn50k.setOnClickListener(onClickListener);
		
		mSlider = (SeekBar) v.findViewById(R.id.seekBar);
		mSlider.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				
				//if(fromUser) {
					int q = 0;
					
					if(progress < seekBar.getMax())
						q = progress * sliderIncrement;
					else
						q = mQuantity;
					
					Logger.log(TAG, "Progress: "+progress, LogLevel.LOGGING_LEVEL_INFO);
					Logger.log(TAG, "SeeBar Max: "+seekBar.getMax(), LogLevel.LOGGING_LEVEL_INFO);
					Logger.log(TAG, "Qty: "+q, LogLevel.LOGGING_LEVEL_INFO);
					Logger.log(TAG, "sliderIncrement: "+sliderIncrement, LogLevel.LOGGING_LEVEL_INFO);
					//Logger.log(TAG, "SeeBar Max: "+seekBar.getMax(), LogLevel.LOGGING_LEVEL_INFO);
					
					updateQuantityAndTradeValue(q);
				//}
				
			}
		});
		
		trend = ((App)getActivity().getApplication()).getTrend();
		
		mBuyBtn = (Button) v.findViewById(R.id.btn_buy);
		mBuyBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				String buyDetail = String.format("Buy %s %s:%s @ %s %f\nTransaction fee: virtual US$ 10\nTotal cost: US$ %.2f", 
						tvQuantity.getText(), trend.getExchange(), trend.getSymbol(), trend.getCurrencyDisplay(),
						lastPrice, getTotalCostForBuy());
				
				Bundle b = new Bundle();
				b.putString(HEADER, String.format("%s:%s", trend.getExchange(), trend.getSymbol()));
				b.putString(BUY_DETAIL_STR, buyDetail);
				b.putString(LAST_PRICE, String.valueOf(lastPrice));
				b.putString(QUANTITY, tvQuantity.getText().toString());
				
				Fragment newFragment = Fragment.instantiate(getActivity(),
						BuyFragment.class.getName(), b);

		        // Add the fragment to the activity, pushing this transaction
		        // on to the back stack.
		        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
		        ft.replace(R.id.realtabcontent, newFragment, "trend_buy");
		        ft.addToBackStack("trend_buy");
		        ft.commit();
			}
		});
		
		
		if(!TextUtils.isEmpty(trend.getImageBlobUrl())) {
			mImageLoader.getBitmapImage(trend.getImageBlobUrl(), new ImageLoadingListener() {
				public void onLoadingComplete(Bitmap loadedImage) {
					final Bitmap b = ImageUtils.convertToMutableAndRemoveBackground(loadedImage);
					mStockLogo.setImageBitmap(b);
					mStockBgLogo.setImageBitmap(b);
				}
			});	
		}
		
		//mCashAvailableValue
		
		if(!TextUtils.isEmpty(trend.getYahooSymbol())) 
			mImageLoader.DisplayImage(String.format(Config.getTrendingChartUrl(), trend.getYahooSymbol()), 
					mStockChart);
		
		lastPrice = YUtils.parseQuoteValue(trend.getLastPrice());
		if(!Double.isNaN(lastPrice)) 
			tvLastPrice.setText(String.format("%s%.2f", trend.getCurrencyDisplay(), lastPrice));
		else
			Logger.log(TAG, "TH: Unable to parse Last Price", LogLevel.LOGGING_LEVEL_ERROR);
		
		double askPrice = YUtils.parseQuoteValue(trend.getAskPrice());
		double bidPrice = YUtils.parseQuoteValue(trend.getBidPrice());
		
		// only update ask & bid if both are present.
		if(!Double.isNaN(askPrice) && !Double.isNaN(bidPrice)) {
			tvAskPrice.setText(String.format("%.2f%s", askPrice, getString(R.string.ask_with_bracket)));
			tvBidPrice.setText(String.format(" x %.2f%s", bidPrice, getString(R.string.bid_with_bracket)));
		}
		else
			Logger.log(TAG, "TH: Unable to parse Ask & Bid Price", LogLevel.LOGGING_LEVEL_ERROR);
		
		int qty = (int)Math.ceil(defaultCashAvailable/lastPrice);
		updateQuantityAndTradeValue(qty);
		//tvQuantity.setText(String.valueOf(qty));
		
		tvPriceAsOf.setText(DateUtils.getFormatedTrendDate(trend.getLastPriceDateAndTimeUtc()));
		
		tvCashAvailable.setText(String.format("US$ %,d", defaultCashAvailable));
		
		enableFields(false);

	}
	
	private OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			int id = v.getId();
			
			switch (id) {
			case R.id.toggle5k:
				mBtn5k.setTextColor(getResources().getColor(R.color.black));
				mBtn10k.setTextColor(getResources().getColor(R.color.price_bar_text_default));
				mBtn25k.setTextColor(getResources().getColor(R.color.price_bar_text_default));
				mBtn50k.setTextColor(getResources().getColor(R.color.price_bar_text_default));
				//UpdateValues(5000, true);
				segmentedUpdate(5000);
				break;
			
			case R.id.toggle10k:
				mBtn5k.setTextColor(getResources().getColor(R.color.price_bar_text_default));
				mBtn10k.setTextColor(getResources().getColor(R.color.black));
				mBtn25k.setTextColor(getResources().getColor(R.color.price_bar_text_default));
				mBtn50k.setTextColor(getResources().getColor(R.color.price_bar_text_default));
				//UpdateValues(10000, true);
				segmentedUpdate(10000);
				break;
				
			case R.id.toggle25k:
				mBtn5k.setTextColor(getResources().getColor(R.color.price_bar_text_default));
				mBtn10k.setTextColor(getResources().getColor(R.color.price_bar_text_default));
				mBtn25k.setTextColor(getResources().getColor(R.color.black));
				mBtn50k.setTextColor(getResources().getColor(R.color.price_bar_text_default));
				//UpdateValues(25000, true);
				segmentedUpdate(25000);
				break;
				
			case R.id.toggle50k:
				mBtn5k.setTextColor(getResources().getColor(R.color.price_bar_text_default));
				mBtn10k.setTextColor(getResources().getColor(R.color.price_bar_text_default));
				mBtn25k.setTextColor(getResources().getColor(R.color.price_bar_text_default));
				mBtn50k.setTextColor(getResources().getColor(R.color.black));
				//UpdateValues(50000, true);
				segmentedUpdate(50000);
				break;

			default:
				break;
			}
		}
	};
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		((TrendingDetailFragment)getActivity().getSupportFragmentManager()
				.findFragmentByTag("trending_detail")).setYahooQuoteUpdateListener(this);
	}
	
	private void enableFields(boolean flag) {
		mBtn5k.setEnabled(flag);
		mBtn10k.setEnabled(flag);
		mBtn25k.setEnabled(flag);
		mBtn50k.setEnabled(flag);
		mBuyBtn.setEnabled(flag);
	}
	
	@Override
	public void onYahooQuoteUpdateStarted() {
		mProgressBar.setVisibility(View.VISIBLE);
	}
	
	@Override
	public void onYahooQuoteUpdateListener(HashMap<String, String> yQuotes) {
		
		mProgressBar.setVisibility(View.GONE);
		enableFields(true);
		
		double LastPrice  = YUtils.parseQuoteValue(yQuotes.get("Last Trade (Price Only)"));
		if(!Double.isNaN(LastPrice)) {
			lastPrice = LastPrice;
			tvLastPrice.setText(String.format("%s%.2f", trend.getCurrencyDisplay(), lastPrice));
		}
		else
			Logger.log(TAG, "Unable to parse Last Trade (Price Only)", LogLevel.LOGGING_LEVEL_ERROR);
		
		//TODO Format date
		String lastPriceDatetimeUtc = yQuotes.get("Last Trade Date");	
		if(TextUtils.isEmpty(lastPriceDatetimeUtc)) 
			tvPriceAsOf.setText(lastPriceDatetimeUtc);
		
		double askPrice = YUtils.parseQuoteValue(yQuotes.get("Ask"));
		if(Double.isNaN(askPrice)) {
			Logger.log(TAG, "Unable to parse Ask, will try using real-time data", LogLevel.LOGGING_LEVEL_ERROR);
			
			askPrice = YUtils.parseQuoteValue(yQuotes.get("Ask (Real-time)"));
			if(Double.isNaN(askPrice))
				Logger.log(TAG, "Unable to parse Ask (Real-time)", LogLevel.LOGGING_LEVEL_ERROR);
		}
		
		double bidPrice = YUtils.parseQuoteValue(yQuotes.get("Bid"));
		if(Double.isNaN(bidPrice)) {
			Logger.log(TAG, "Unable to parse Bid, will try using real-time data", LogLevel.LOGGING_LEVEL_ERROR);
			
			bidPrice = YUtils.parseQuoteValue(yQuotes.get("Bid (Real-time)"));
			if(Double.isNaN(bidPrice))
				Logger.log(TAG, "Unable to parse Bid (Real-time)", LogLevel.LOGGING_LEVEL_ERROR);
		}
		
		// only update ask & bid if both are present.
		if(!Double.isNaN(askPrice) && (Double.compare(askPrice, 0.0) == 0) && !Double.isNaN(bidPrice)
				&& (Double.compare(bidPrice, 0.0) == 0)) {
			tvAskPrice.setText(String.format("%.2f%s", askPrice, getString(R.string.ask_with_bracket)));
			tvBidPrice.setText(String.format(" x %.2f%s", bidPrice, getString(R.string.bid_with_bracket)));
		}
		else
			Logger.log(TAG, "Unable to parse Ask & Bid Price", LogLevel.LOGGING_LEVEL_ERROR);
		
		UpdateValues(defaultCashAvailable, false);

	}
	
	private void UpdateValues(int cash, boolean isPriceSlot) {
		
		Logger.log(TAG, "Cash: "+cash, LogLevel.LOGGING_LEVEL_INFO);
		
		mQuantity = 0;
		
		int totalCashAvailable = cash - defaultTHTransferFee;
		
		mQuantity = (int)Math.ceil(totalCashAvailable / lastPrice);
		
		
		int avgDailyVolume = (int)Math.ceil(Double.parseDouble(trend.getAverageDailyVolume()));
		int volume = (int)Math.ceil(Double.parseDouble(trend.getVolume()));
		
		
		int maxQuantity = avgDailyVolume;
		
		if(volume > avgDailyVolume)
			maxQuantity = volume;
		
		maxQuantity = (int)Math.ceil(maxQuantity*0.2);
		
		if(maxQuantity == 0)
			maxQuantity = 1;
		
		//TODO check is valid maxQuantity
		
		if(mQuantity > maxQuantity)
			mQuantity = maxQuantity;
		
		
		
		int defaultQuantity = 0;
		
		if(isTransactionTypeBuy)
			defaultQuantity = (int)Math.ceil((mQuantity/3.0));
		
		Logger.log(TAG, "defaultQuantity: "+defaultQuantity, LogLevel.LOGGING_LEVEL_INFO);
		updateQuantityAndTradeValue(defaultQuantity);
		
		
		if(isPriceSlot) {
			defaultQuantity = mQuantity;
			
			//int quantityForSlots = (int)Math.ceil(totalCashAvailable / lastPrice);
			
			//if the closest quantity is greater than the maximum the user can buy, 
			//we set the quantity to the maximum he can buy
		    //int slidervalue = (defaultQuantity/sliderIncrement);
		    		
		    		///Math.min(defaultQuantity, sliderMaxValue);
		    //mSlider.setProgress(slidervalue);
		    //mSlider.setMax(sliderMaxValue);
		    
		}
		//else {
			
			//Slider
			int sliderValue = 0; //mQuantity; //155
			
			//if(!isPriceSlot)
				sliderIncrement = (mQuantity > 1000) ? 100 : ((mQuantity > 100) ? 10 : 1);
			
			//Logger.log(TAG, "Slider Increment: "+sliderIncrement, LogLevel.LOGGING_LEVEL_INFO);
			
			int sliderMaxValue = mQuantity/sliderIncrement;
			
			//Logger.log(TAG, "Slider MaxVaule: "+sliderMaxValue, LogLevel.LOGGING_LEVEL_INFO);
			
			int currentAbsoluteValue = mQuantity;
			if(defaultQuantity >= currentAbsoluteValue)
				sliderValue = sliderMaxValue;
			else {
				int value = (int)defaultQuantity / sliderIncrement;
				sliderValue = value;
			}
				//float value2 = absoluteValue / sliderIncrement;
			
			Logger.log(TAG, "Slider Vaule: "+sliderValue, LogLevel.LOGGING_LEVEL_INFO);
			
			mSlider.setMax(sliderMaxValue);
			mSlider.incrementProgressBy(sliderIncrement);
			mSlider.setProgress(sliderValue);
		//}
	}
	
	private void segmentedUpdate(int cash) {
		
		int totalCashAvailable = cash - defaultTHTransferFee;
		
		int mQuantity = (int)Math.ceil(totalCashAvailable / lastPrice);
		
		int avgDailyVolume = (int)Math.ceil(Double.parseDouble(trend.getAverageDailyVolume()));
		int volume = (int)Math.ceil(Double.parseDouble(trend.getVolume()));
		
		
		int maxQuantity = avgDailyVolume;
		
		if(volume > avgDailyVolume)
			maxQuantity = volume;
		
		maxQuantity = (int)Math.ceil(maxQuantity*0.2);
		
		if(maxQuantity == 0)
			maxQuantity = 1;
		
		if(mQuantity > maxQuantity)
			mQuantity = maxQuantity;
		
		int sValue = mQuantity/sliderIncrement;
		mSlider.setProgress(sValue);
	}
	
	private void updateQuantityAndTradeValue(int qty) {
		
		tvQuantity.setText(String.format("%,d", qty));
		
		if(!Double.isNaN(lastPrice) && !(Double.compare(lastPrice, 0.0) == 0)) 
			tvTradeValue.setText(String.format("%,d", (int)Math.ceil((qty*lastPrice))));
	}
	
	private double getTotalCostForBuy() {
		double q = Double.parseDouble(tvQuantity.getText().toString());
		double totalCost = q*(lastPrice + defaultTHTransferFee);
		
		return totalCost;
	}
	
	@Override
	public void onDestroy() {
		((TrendingDetailFragment)getActivity().getSupportFragmentManager()
				.findFragmentByTag("trending_detail")).setYahooQuoteUpdateListener(null);
		super.onDestroy();
	}


	
}
