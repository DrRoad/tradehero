package com.tradehero.th.fragments;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import com.actionbarsherlock.app.SherlockFragment;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestBuilder;
import com.squareup.picasso.Target;
import com.squareup.picasso.Transformation;
import com.tradehero.common.graphics.GaussianTransformation;
import com.tradehero.common.graphics.RoundedShapeTransformation;
import com.tradehero.common.utils.THToast;
import com.tradehero.th.R;
import com.tradehero.th.adapters.UserTimelineAdapter;
import com.tradehero.th.api.timeline.TimelineDTO;
import com.tradehero.th.api.users.UserProfileDTO;
import com.tradehero.th.base.THUser;
import com.tradehero.th.http.THAsyncClientFactory;
import com.tradehero.th.misc.callback.THCallback;
import com.tradehero.th.misc.callback.THResponse;
import com.tradehero.th.misc.exception.THException;
import com.tradehero.th.http.ImageLoader;
import com.tradehero.th.http.RequestTaskCompleteListener;
import com.tradehero.th.models.Medias;
import com.tradehero.th.models.TradeOfWeek;
import com.tradehero.th.network.NetworkEngine;
import com.tradehero.th.network.service.UserTimelineService;
import com.tradehero.th.utills.Constants;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HomeScreenFragment extends SherlockFragment
{

    private ImageView userProfileImage;
    private ListView userTimelineItemList;
    private View mBagroundImage;
    private UserProfileDTO profile;
    private BitmapDrawable drawableBitmap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.profile_screen, container, false);
        _initView(view);

        return view;
    }

    private void _initView(View view)
    {
        userTimelineItemList = (ListView) view.findViewById(R.id.list_user_content);
        profile = THUser.getCurrentUser();

        if (profile != null)
        {
            userProfileImage = (ImageView) view.findViewById(R.id.user_profile_image);
            mBagroundImage = view.findViewById(R.id.top_layout_sublauyt1);
            loadPictureWithTransformation(profile.picture, new RoundedShapeTransformation()).into(userProfileImage);
            loadPictureWithTransformation(profile.picture, new GaussianTransformation()).fetch(new Target()
            {
<<<<<<< HEAD
                @Override public void onSuccess(Bitmap bitmap)
                {
                    mBagroundImage.setBackground(new BitmapDrawable(getResources(), bitmap));
                }
=======
                mUserImg.setImageBitmap(Util.getRoundedShape(mBitmap));
                drawableBitmap = new BitmapDrawable(applyGaussianBlur(mBitmap));
                mBagroundImage.setBackgroundDrawable(drawableBitmap);
            }

            _getDataOfTrade();
        }
    }

    public static Bitmap applyGaussianBlur(Bitmap src)
    {
        double[][] GaussianBlurConfig = new double[][] {
                {1, 2, 1},
                {2, 4, 2},
                {1, 2, 1}
        };
        ConvolutionMatrix convMatrix = new ConvolutionMatrix(3);
        convMatrix.applyConfig(GaussianBlurConfig);
        convMatrix.Factor = 27;
        convMatrix.Offset = 0;
        return ConvolutionMatrix.computeConvolution3x3(src, convMatrix);
    }

    class UpdateUi extends AsyncTask<Void, Void, Void>
    {
        ImageLoader imgLoader;
        ProgressDialog dlg;

        @Override
        protected void onPreExecute()
        {
            // TODO Auto-generated method stub
            imgLoader = new ImageLoader(getActivity());
            dlg = new ProgressDialog(getActivity());
            dlg.setMessage(getResources().getString(R.string.loading_loading));
            dlg.show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0)
        {
            // TODO load user profile picture
            //mBitmap = imgLoader.getBitmap(picture);
            //mBGBtmp = imgLoader.getBitmap(picture);

            return null;
        }
>>>>>>> Refactored TrendingView with more OOP and new ImageLoader

                @Override public void onError()
                {
                    //To change body of implemented methods use File | Settings | File Templates.
                }
            });

            getSherlockActivity().getSupportActionBar().setTitle(profile.displayName);

            _getDataOfTrade();
        }
    }

    private RequestBuilder loadPictureWithTransformation(String url, Transformation transformation)
    {
        return Picasso.with(getActivity()).load(url).transform(transformation);
    }

    private void _getDataOfTrade()
    {
        NetworkEngine.createService(UserTimelineService.class)
                .getTimeline(profile.id, 42, new THCallback<TimelineDTO>()
                {
                    @Override protected void success(TimelineDTO timelineDTO, THResponse thResponse)
                    {
                        userTimelineItemList.setVisibility(View.VISIBLE);
                        refreshTimeline(timelineDTO);
                    }

                    @Override protected void failure(THException ex)
                    {
                        //To change body of implemented methods use File | Settings | File Templates.
                    }
                });
    }

    private void refreshTimeline(TimelineDTO timelineDTO)
    {
        userTimelineItemList.setAdapter(new UserTimelineAdapter(getActivity(), timelineDTO));
    }

    private void parseResponse(String response)
    {
        ArrayList<TradeOfWeek> tradweekList = new ArrayList<>();
        TradeOfWeek mTradeWeek = null;
        Medias objMedia = null;
        try
        {
            JSONObject obj = new JSONObject(response);

            JSONArray mJsonArray = obj.getJSONArray("enhancedItems");

            for (int i = 0; i < mJsonArray.length(); i++)
            {

                mTradeWeek = new TradeOfWeek();
                JSONObject mobj = mJsonArray.getJSONObject(i);

                String _id = mobj.getString("id");
                String _createdAtUtc = mobj.getString("createdAtUtc");
                String _userId = mobj.getString("userId");
                String _text = mobj.getString("text");
                //String _pushTypeId = mobj.getString("pushTypeId");
                JSONArray mediajson = mobj.getJSONArray("medias");
                for (int j = 0; j < mediajson.length(); j++)
                {

                    objMedia = new Medias();
                    JSONObject mediajobj = mediajson.getJSONObject(j);
                    String _securityId = mediajobj.getString("securityId");
                    String _exchange = mediajobj.getString("exchange");
                    String _symbol = mediajobj.getString("symbol");
                    String _url = mediajobj.getString("url");
                    String _type = mediajobj.getString("type");
                    objMedia.setExchange(_exchange);
                    objMedia.setSecurityId(_securityId);
                    objMedia.setUrl(_url);
                    objMedia.setType(_type);
                    objMedia.setSymbol(_symbol);
                }
                mTradeWeek.setCreatedAtUtc(_createdAtUtc);
                mTradeWeek.setId(_id);
                mTradeWeek.setUserId(_userId);
                mTradeWeek.setMedias(objMedia);
                mTradeWeek.setText(_text);
                //mTradeWeek.setPushTypeId(_pushTypeId);
                tradweekList.add(mTradeWeek);
            }

            System.out.println("Trade week size=======" + tradweekList.size());

            userTimelineItemList.setAdapter(new UserTimelineAdapter(getActivity(), tradweekList));
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }
}
