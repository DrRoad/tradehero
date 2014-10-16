package com.tradehero.th.fragments.chinabuild.fragment.competition;

import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.squareup.picasso.Picasso;
import com.tradehero.common.persistence.DTOCacheNew;
import com.tradehero.common.utils.THToast;
import com.tradehero.common.widget.BetterViewAnimator;
import com.tradehero.th.R;
import com.tradehero.th.adapters.CompetitionListAdapter;
import com.tradehero.th.fragments.base.DashboardFragment;
import com.tradehero.th.fragments.chinabuild.cache.CompetitionListType;
import com.tradehero.th.fragments.chinabuild.cache.CompetitionListTypeMine;
import com.tradehero.th.fragments.chinabuild.cache.CompetitionListTypeOffical;
import com.tradehero.th.fragments.chinabuild.cache.CompetitionListTypeUser;
import com.tradehero.th.fragments.chinabuild.cache.CompetitionListTypeVip;
import com.tradehero.th.fragments.chinabuild.cache.CompetitionNewCache;
import com.tradehero.th.fragments.chinabuild.data.CompetitionDataItem;
import com.tradehero.th.fragments.chinabuild.data.CompetitionInterface;
import com.tradehero.th.fragments.chinabuild.data.UserCompetitionDTO;
import com.tradehero.th.fragments.chinabuild.data.UserCompetitionDTOList;
import com.tradehero.th.fragments.chinabuild.listview.SecurityListView;
import com.viewpagerindicator.CirclePageIndicator;
import dagger.Lazy;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import org.jetbrains.annotations.NotNull;
import timber.log.Timber;

/**
 * Created by huhaiping on 14-9-9. 显示所有比赛和我参加的比赛
 */
public class CompetitionBaseFragment extends DashboardFragment
{
    @Inject Lazy<Picasso> picasso;
    @Inject Lazy<CompetitionNewCache> competitionNewCacheLazy;
    private DTOCacheNew.Listener<CompetitionListType, UserCompetitionDTOList> competitionListCacheListenerOffical;
    private DTOCacheNew.Listener<CompetitionListType, UserCompetitionDTOList> competitionListCacheListenerUser;
    private DTOCacheNew.Listener<CompetitionListType, UserCompetitionDTOList> competitionListCacheListenerVip;
    private DTOCacheNew.Listener<CompetitionListType, UserCompetitionDTOList> competitionListCacheListenerMine;

    @InjectView(R.id.bvaViewAll) BetterViewAnimator betterViewAnimator;
    @InjectView(android.R.id.progress) ProgressBar progressBar;
    @InjectView(R.id.imgEmpty) ImageView imgEmpty;

    @InjectView(R.id.listCompetitions) SecurityListView listCompetitions;//比赛列表
    @InjectView(R.id.llCompetitionAdv) RelativeLayout llCompetitionAdv;//广告栏
    @InjectView(R.id.pager) ViewPager pager;
    @InjectView(R.id.indicator) CirclePageIndicator indicator;
    private List<View> views = new ArrayList<View>();
    private CompetitionListAdapter adapterList;

    private UserCompetitionDTOList userCompetitionVipDTOs;
    public static boolean needRefresh = false;

    CompetitionListTypeMine mineKey = new CompetitionListTypeMine();
    CompetitionListTypeUser userKey = new CompetitionListTypeUser();

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        competitionListCacheListenerOffical = createCompetitionListCacheListenerOffical();
        competitionListCacheListenerUser = createCompetitionListCacheListenerUser();
        competitionListCacheListenerVip = createCompetitionListCacheListenerVip();
        competitionListCacheListenerMine = createCompetitionListCacheListenerMine();
        adapterList = new CompetitionListAdapter(getActivity(), getCompetitionPageType());
        //fetchCompetition(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.competition_base_layout, container, false);
        ButterKnife.inject(this, view);
        initListView();

        if (adapterList.getCount() == 0)
        {
            betterViewAnimator.setDisplayedChildByLayoutId(R.id.progress);
        }
        else
        {
            betterViewAnimator.setDisplayedChildByLayoutId(R.id.listCompetitions);
        }

        return view;
    }

    private void initListView()
    {

        fetchVipCompetition(false);//获取官方推荐比赛

        listCompetitions.setEmptyView(imgEmpty);
        listCompetitions.setMode(PullToRefreshBase.Mode.BOTH);
        listCompetitions.setAdapter(adapterList);
        listCompetitions.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override public void onItemClick(AdapterView<?> adapterView, View view, int id, long position)
            {
                CompetitionInterface item = adapterList.getItem((int) position);
                if (item instanceof CompetitionDataItem)
                {
                    gotoCompetitionDetailFragment(((CompetitionDataItem) item).userCompetitionDTO);
                }
            }
        });

        listCompetitions.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>()
        {
            @Override public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView)
            {
                Timber.d("下拉刷新");
                fetchCompetition(true);
            }

            @Override public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView)
            {
                Timber.d("上拉加载更多");
                fetchCompetitionMore(true);
            }
        });
    }

    private void gotoCompetitionDetailFragment(UserCompetitionDTO userCompetitionDTO)
    {
        Bundle bundle = new Bundle();
        bundle.putSerializable(CompetitionDetailFragment.BUNDLE_COMPETITION_DTO, userCompetitionDTO);
        gotoDashboard(CompetitionDetailFragment.class.getName(), bundle);
        //THToast.show(userCompetitionDTO.name);
    }

    private void initCompetitionAdv(UserCompetitionDTOList userCompetitionDTOs)
    {
        this.userCompetitionVipDTOs = userCompetitionDTOs;
        int sizeVip = userCompetitionDTOs.size();
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        views = new ArrayList<View>();
        if (sizeVip > 0) llCompetitionAdv.setVisibility(View.VISIBLE);
        for (int i = 0; i < sizeVip; i++)
        {
            View view = layoutInflater.inflate(R.layout.competition_adv_item, null);
            ImageView imgCompetitionAdv = (ImageView) view.findViewById(R.id.imgCompetitionAdv);
            picasso.get()
                    .load(userCompetitionVipDTOs.get(i).bannerUrl)
                    .placeholder(R.drawable.default_image)
                    .error(R.drawable.default_image)
                    .into(imgCompetitionAdv);
            views.add(view);
            view.setOnClickListener(new View.OnClickListener()
            {
                @Override public void onClick(View view)
                {
                    int position = pager.getCurrentItem();
                    gotoCompetitionDetailFragment(userCompetitionVipDTOs.get(position));
                }
            });
        }

        pager.setAdapter(pageAdapter);
        indicator.setViewPager(pager);
    }

    @OnClick(R.id.llCompetitionAdv)
    public void onCompetitionAdvClicked()
    {
        gotoDashboard(CompetitionDetailFragment.class.getName());
    }

    @Override public void onStop()
    {
        super.onStop();
    }

    @Override public void onDestroyView()
    {
        ButterKnife.reset(this);
        detachOfficalCompetition();
        detachMineCompetition();
        detachUserCompetition();
        detachVipCompetition();
        super.onDestroyView();
    }

    @Override public void onDestroy()
    {
        super.onDestroy();
    }



    public boolean isNeedRefresh()
    {
        return needRefresh&&(getCompetitionPageType() == CompetitionUtils.COMPETITION_PAGE_MINE);
    }


    @Override public void onResume()
    {
        super.onResume();
        Timber.d("OnRusme: StockGodList 1 ");
        if (isNeedRefresh() || (adapterList != null && adapterList.getCount() == 0))
        {
            fetchCompetition(true);
        }
    }

    public void fetchCompetition(boolean refresh)
    {
        if (getCompetitionPageType() == CompetitionUtils.COMPETITION_PAGE_MINE)
        {
            //我的比赛页
            mineKey = new CompetitionListTypeMine();
            mineKey.page = 1;
            fetchMineCompetition(refresh);
        }
        else if (getCompetitionPageType() == CompetitionUtils.COMPETITION_PAGE_ALL)
        {
            userKey = new CompetitionListTypeUser();
            userKey.page = 1;
            fetchOfficalCompetition(refresh);
            fetchUserCompetition(refresh);
        }
    }

    public void fetchCompetitionMore(boolean refresh)
    {
        //fetchVipCompetition(refresh);//获取官方推荐比赛
        if (getCompetitionPageType() == CompetitionUtils.COMPETITION_PAGE_MINE)
        {
            //我的比赛页
            fetchMineCompetition(refresh);
        }
        else if (getCompetitionPageType() == CompetitionUtils.COMPETITION_PAGE_ALL)
        {
            //fetchOfficalCompetition(refresh);
            fetchUserCompetition(refresh);
        }
    }

    PagerAdapter pageAdapter = new PagerAdapter()
    {
        @Override
        public void destroyItem(View container, int position, Object object)
        {
            ((ViewPager) container).removeView(views.get(position));
        }

        @Override
        public Object instantiateItem(View container, int position)
        {
            ((ViewPager) container).addView(views.get(position));
            return views.get(position);
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1)
        {
            return arg0 == arg1;
        }

        @Override
        public int getCount()
        {
            return views.size();
        }

        //@Override
        //public CharSequence getPageTitle(int position) {
        //    // TODO Auto-generated method stub
        //    return titles.get(position);
        //}
    };

    protected void detachOfficalCompetition()
    {
        competitionNewCacheLazy.get().unregister(competitionListCacheListenerOffical);
    }

    protected void detachUserCompetition()
    {
        competitionNewCacheLazy.get().unregister(competitionListCacheListenerUser);
    }

    protected void detachVipCompetition()
    {
        competitionNewCacheLazy.get().unregister(competitionListCacheListenerVip);
    }

    protected void detachMineCompetition()
    {
        competitionNewCacheLazy.get().unregister(competitionListCacheListenerMine);
    }

    public void fetchOfficalCompetition(boolean refresh)
    {
        detachOfficalCompetition();
        CompetitionListTypeOffical officalKey = new CompetitionListTypeOffical();
        competitionNewCacheLazy.get().register(officalKey, competitionListCacheListenerOffical);
        competitionNewCacheLazy.get().getOrFetchAsync(officalKey, refresh);
    }

    public void fetchUserCompetition(boolean refresh)
    {
        detachUserCompetition();

        competitionNewCacheLazy.get().register(userKey, competitionListCacheListenerUser);
        competitionNewCacheLazy.get().getOrFetchAsync(userKey, refresh);
    }

    public void fetchVipCompetition(boolean refresh)
    {
        betterViewAnimator.setDisplayedChildByLayoutId(R.id.progress);
        detachVipCompetition();
        CompetitionListTypeVip vipKey = new CompetitionListTypeVip();
        competitionNewCacheLazy.get().register(vipKey, competitionListCacheListenerVip);
        competitionNewCacheLazy.get().getOrFetchAsync(vipKey, refresh);
    }

    public void fetchMineCompetition(boolean refresh)
    {
        betterViewAnimator.setDisplayedChildByLayoutId(R.id.progress);
        detachMineCompetition();
        competitionNewCacheLazy.get().register(mineKey, competitionListCacheListenerMine);
        competitionNewCacheLazy.get().getOrFetchAsync(mineKey, refresh);
    }

    public void initVipCompetition(UserCompetitionDTOList userCompetitionDTOs)
    {
        Timber.d("初始化 广告栏 比赛");
        initCompetitionAdv(userCompetitionDTOs);
    }

    //我的比赛
    public void initMyCompetition(CompetitionListType key, UserCompetitionDTOList userCompetitionDTOs)
    {
        needRefresh = false;
        if (adapterList != null)
        {
            if (key.page == 1)
            {
                adapterList.setMyCompetitionDtoList(userCompetitionDTOs);
            }
            else
            {
                adapterList.addMyCompetitionDtoList(userCompetitionDTOs);
            }
        }
        if (userCompetitionDTOs != null && userCompetitionDTOs.size() > 0)
        {
            mineKey.page += 1;
        }
    }

    //官方比赛
    public void initOfficalCompetition(UserCompetitionDTOList userCompetitionDTOs)
    {
        if (adapterList != null)
        {
            adapterList.setOfficalCompetitionDtoList(userCompetitionDTOs);
        }
    }

    //用户创建比赛
    public void initUserCompetition(CompetitionListType key, UserCompetitionDTOList userCompetitionDTOs)
    {
        if (adapterList != null)
        {
            if (key.page == 1)
            {
                adapterList.setUserCompetitionDtoList(userCompetitionDTOs);
            }
            else
            {
                adapterList.addUserCompetitionDtoList(userCompetitionDTOs);
            }
        }
        if (userCompetitionDTOs != null && userCompetitionDTOs.size() > 0)
        {
            userKey.page += 1;
        }
    }

    public UserCompetitionDTOList removeVipCompetition(UserCompetitionDTOList userCompetitionDTOs)
    {
        if (userCompetitionDTOs != null)
        {
            ArrayList<UserCompetitionDTO> list = new ArrayList<>();
            int size = userCompetitionDTOs.size();
            for (int i = 0; i < size; i++)
            {
                list.add(userCompetitionDTOs.get(i));
            }
        }
        return null;
    }

    public int getCompetitionPageType()
    {
        return CompetitionUtils.COMPETITION_PAGE_ALL;
    }

    protected DTOCacheNew.Listener<CompetitionListType, UserCompetitionDTOList> createCompetitionListCacheListenerOffical()
    {
        return new CompetitionListCacheListener();
    }

    protected DTOCacheNew.Listener<CompetitionListType, UserCompetitionDTOList> createCompetitionListCacheListenerUser()
    {
        return new CompetitionListCacheListener();
    }

    protected DTOCacheNew.Listener<CompetitionListType, UserCompetitionDTOList> createCompetitionListCacheListenerVip()
    {
        return new CompetitionListCacheListener();
    }

    protected DTOCacheNew.Listener<CompetitionListType, UserCompetitionDTOList> createCompetitionListCacheListenerMine()
    {
        return new CompetitionListCacheListener();
    }

    protected class CompetitionListCacheListener implements DTOCacheNew.Listener<CompetitionListType, UserCompetitionDTOList>
    {
        @Override public void onDTOReceived(@NotNull CompetitionListType key, @NotNull UserCompetitionDTOList value)
        {

            //linkWith(value, true);
            if (key instanceof CompetitionListTypeOffical)
            {
                initOfficalCompetition(value);
            }
            else if (key instanceof CompetitionListTypeUser)
            {
                initUserCompetition(key, value);
            }
            else if (key instanceof CompetitionListTypeVip)
            {
                initVipCompetition(value);
            }
            else if (key instanceof CompetitionListTypeMine)
            {
                initMyCompetition(key, value);
            }
            onFinish();
        }

        @Override public void onErrorThrown(@NotNull CompetitionListType key, @NotNull Throwable error)
        {
            THToast.show(getString(R.string.fetch_error));
            onFinish();
        }

        public void onFinish()
        {
            try
            {
                betterViewAnimator.setDisplayedChildByLayoutId(R.id.listCompetitions);
                if (listCompetitions != null)
                {
                    listCompetitions.onRefreshComplete();
                }
            } catch (Exception e)
            {
            }
        }
    }
}
