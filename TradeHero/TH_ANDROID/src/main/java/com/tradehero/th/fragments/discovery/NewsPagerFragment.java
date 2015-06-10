package com.tradehero.th.fragments.discovery;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnItemSelected;
import com.tradehero.th.R;

public final class NewsPagerFragment extends Fragment
{
    @InjectView(R.id.spinner_news) Spinner newsSpinner;

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.discovery_news_pager, container, false);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);

        newsSpinner.setAdapter(new NewsSpinnerAdapter(getActivity(), NewsType.values()));
    }

    @Override public void onDestroyView()
    {
        ButterKnife.reset(this);
        super.onDestroyView();
    }

    @SuppressWarnings("unused")
    @OnItemSelected(value = R.id.spinner_news, callback = OnItemSelected.Callback.ITEM_SELECTED)
    public void onNewsItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        Fragment f = NewsHeadlineFragment.newInstance(NewsType.values()[position]);
        getChildFragmentManager().beginTransaction().replace(R.id.news_container, f).addToBackStack(null).commit();
    }

    class NewsSpinnerAdapter extends ArrayAdapter<NewsType>
    {
        public NewsSpinnerAdapter(Context context, NewsType[] objects)
        {
            super(context, 0, objects);
        }

        @Override public View getView(int position, View convertView, ViewGroup parent)
        {
            NewsType type = getItem(position);
            if (convertView == null)
            {
                convertView = getActivity().getLayoutInflater().inflate(type.titleViewResourceId, parent, false);
            }
            return convertView;
        }

        @Override public View getDropDownView(int position, View convertView, ViewGroup parent)
        {
            NewsType type = getItem(position);
            View rootView = getActivity().getLayoutInflater().inflate(type.titleViewResourceId, parent, false);
            View view = rootView.findViewById(R.id.spinner_arrow);
            if (view != null)
            {
                view.setVisibility(View.GONE);
            }
            return rootView;
        }
    }
}
