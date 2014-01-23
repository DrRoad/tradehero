package com.tradehero.th.fragments.competition;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.tradehero.common.persistence.DTOCache;
import com.tradehero.common.utils.THLog;
import com.tradehero.common.utils.THToast;
import com.tradehero.th.R;
import com.tradehero.th.api.competition.CompetitionId;
import com.tradehero.th.api.competition.CompetitionIdList;
import com.tradehero.th.api.competition.ProviderConstants;
import com.tradehero.th.api.competition.ProviderDTO;
import com.tradehero.th.api.competition.ProviderId;
import com.tradehero.th.api.leaderboard.LeaderboardDefDTO;
import com.tradehero.th.api.portfolio.OwnedPortfolioId;
import com.tradehero.th.fragments.competition.zone.CompetitionZoneLegalMentionsView;
import com.tradehero.th.fragments.competition.zone.dto.CompetitionZoneDTO;
import com.tradehero.th.fragments.competition.zone.dto.CompetitionZoneLeaderboardDTO;
import com.tradehero.th.fragments.competition.zone.dto.CompetitionZoneLegalDTO;
import com.tradehero.th.fragments.competition.zone.dto.CompetitionZonePortfolioDTO;
import com.tradehero.th.fragments.competition.zone.dto.CompetitionZoneTradeNowDTO;
import com.tradehero.th.fragments.competition.zone.dto.CompetitionZoneVideoDTO;
import com.tradehero.th.fragments.competition.zone.dto.CompetitionZoneWizardDTO;
import com.tradehero.th.fragments.leaderboard.CompetitionLeaderboardMarkUserListViewFragment;
import com.tradehero.th.fragments.leaderboard.LeaderboardMarkUserListViewFragment;
import com.tradehero.th.fragments.position.PositionListFragment;
import com.tradehero.th.fragments.web.WebViewFragment;
import com.tradehero.th.models.intent.THIntent;
import com.tradehero.th.models.intent.THIntentPassedListener;
import com.tradehero.th.models.intent.competition.ProviderPageIntent;
import com.tradehero.th.persistence.competition.CompetitionCache;
import com.tradehero.th.persistence.competition.CompetitionListCache;
import java.util.List;
import javax.inject.Inject;

/**
 * Created by xavier on 1/17/14.
 */
public class MainCompetitionFragment extends CompetitionFragment
{
    public static final String TAG = MainCompetitionFragment.class.getSimpleName();

    private ActionBar actionBar;
    private ProgressBar progressBar;
    private AbsListView listView;
    private CompetitionZoneListItemAdapter competitionZoneListItemAdapter;

    private THIntentPassedListener webViewTHIntentPassedListener;
    private WebViewFragment webViewFragment;

    @Inject CompetitionListCache competitionListCache;
    @Inject CompetitionCache competitionCache;
    protected List<CompetitionId> competitionIds;
    private DTOCache.Listener<ProviderId, CompetitionIdList> competitionListCacheListener;
    private DTOCache.GetOrFetchTask<ProviderId, CompetitionIdList> competitionListCacheFetchTask;

    @Override public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.webViewTHIntentPassedListener = new MainCompetitionWebViewTHIntentPassedListener();
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_main_competition, container, false);
        initViews(view);
        return view;
    }

    @Override protected void initViews(View view)
    {
        this.competitionZoneListItemAdapter = new CompetitionZoneListItemAdapter(
                getActivity(),
                getActivity().getLayoutInflater(),
                R.layout.competition_zone_item,
                R.layout.competition_zone_trade_now,
                R.layout.competition_zone_header,
                R.layout.competition_zone_leaderboard_item,
                R.layout.competition_zone_legal_mentions);
        this.competitionZoneListItemAdapter.setParentOnLegalElementClicked(new MainCompetitionLegalClickedListener());

        this.progressBar = (ProgressBar) view.findViewById(android.R.id.empty);
        if (this.progressBar != null)
        {
            this.progressBar.setVisibility(View.VISIBLE);
        }
        this.listView = (AbsListView) view.findViewById(R.id.competition_zone_list);
        if (this.listView != null)
        {
            this.listView.setAdapter(this.competitionZoneListItemAdapter);
            this.listView.setOnItemClickListener(new MainCompetitionFragmentItemClickListener());
        }

        this.competitionListCacheListener = new MainCompetitionCompetitionListCacheListener();
    }

    //<editor-fold desc="ActionBar">
    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        actionBar = getSherlockActivity().getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_HOME);
        displayActionBarTitle();

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override public void onDestroyOptionsMenu()
    {
        super.onDestroyOptionsMenu();
        this.actionBar = null;
    }
    //</editor-fold>

    @Override public void onStart()
    {
        super.onStart();
        detachCompetitionListCacheTask();

        competitionListCacheFetchTask = competitionListCache.getOrFetch(providerId, competitionListCacheListener);
        competitionListCacheFetchTask.execute();
    }

    @Override public void onResume()
    {
        super.onResume();
        // We came back into view so we have to forget the web fragment
        if (this.webViewFragment != null)
        {
            this.webViewFragment.setThIntentPassedListener(null);
        }
        this.webViewFragment = null;
    }

    @Override public void onStop()
    {
        detachCompetitionListCacheTask();
        super.onStop();
    }

    @Override public void onDestroyView()
    {
        if (this.competitionZoneListItemAdapter != null)
        {
            this.competitionZoneListItemAdapter.setParentOnLegalElementClicked(null);
        }
        this.competitionZoneListItemAdapter = null;

        this.progressBar = null;

        if (this.listView != null)
        {
            this.listView.setOnItemClickListener(null);
        }
        this.listView = null;

        this.competitionListCacheListener = null;

        super.onDestroyView();
    }

    @Override public void onDestroy()
    {
        this.webViewTHIntentPassedListener = null;
        super.onDestroy();
    }

    private void detachCompetitionListCacheTask()
    {
        if (competitionListCacheFetchTask != null)
        {
            competitionListCacheFetchTask.setListener(null);
        }
        competitionListCacheFetchTask = null;
    }

    @Override protected void linkWith(ProviderDTO providerDTO, boolean andDisplay)
    {
        super.linkWith(providerDTO, andDisplay);
        this.competitionZoneListItemAdapter.setProvider(providerDTO);
        this.competitionZoneListItemAdapter.notifyDataSetChanged();
        if (progressBar != null)
        {
            progressBar.setVisibility(View.GONE);
        }
        if (andDisplay)
        {
            displayActionBarTitle();
        }
    }

    protected void linkWith(List<CompetitionId> competitionIds, boolean andDisplay)
    {
        this.competitionIds = competitionIds;
        this.competitionZoneListItemAdapter.setCompetitionDTOs(competitionCache.get(competitionIds));
        this.competitionZoneListItemAdapter.notifyDataSetChanged();
    }

    @Override public boolean isTabBarVisible()
    {
        return false;
    }

    private void displayActionBarTitle()
    {
        if (this.actionBar != null)
        {
            if (this.providerDTO == null || this.providerDTO.name == null)
            {
                this.actionBar.setTitle("");
            }
            else
            {
                this.actionBar.setTitle(this.providerDTO.name);
            }
        }
    }

    //<editor-fold desc="Click Handling">
    private void handleItemClicked(CompetitionZoneDTO competitionZoneDTO)
    {
        if (competitionZoneDTO instanceof CompetitionZoneTradeNowDTO)
        {
            pushTradeNowElement((CompetitionZoneTradeNowDTO) competitionZoneDTO);
        }
        else if (competitionZoneDTO instanceof CompetitionZonePortfolioDTO)
        {
            pushPortfolioElement((CompetitionZonePortfolioDTO) competitionZoneDTO);
        }
        else if (competitionZoneDTO instanceof CompetitionZoneVideoDTO)
        {
            pushVideoElement((CompetitionZoneVideoDTO) competitionZoneDTO);
        }
        else if (competitionZoneDTO instanceof CompetitionZoneWizardDTO)
        {
            pushWizardElement((CompetitionZoneWizardDTO) competitionZoneDTO);
        }
        else if (competitionZoneDTO instanceof CompetitionZoneLeaderboardDTO)
        {
            pushLeaderboardElement((CompetitionZoneLeaderboardDTO) competitionZoneDTO);
        }
        else if (competitionZoneDTO instanceof CompetitionZoneLegalDTO)
        {
            pushLegalElement((CompetitionZoneLegalDTO) competitionZoneDTO);
        }

        // TODO others?
    }

    private void pushTradeNowElement(CompetitionZoneTradeNowDTO competitionZoneDTO)
    {
        Bundle args = new Bundle();
        args.putBundle(ProviderSecurityListFragment.BUNDLE_KEY_PROVIDER_ID, providerId.getArgs());
        args.putBundle(ProviderSecurityListFragment.BUNDLE_KEY_PURCHASE_APPLICABLE_PORTFOLIO_ID_BUNDLE, userInteractor.getApplicablePortfolioId().getArgs());
        navigator.pushFragment(ProviderSecurityListFragment.class, args);
    }

    private void pushPortfolioElement(CompetitionZonePortfolioDTO competitionZoneDTO)
    {
        // TODO We need to be able to launch async when the portfolio Id is finally not null
        OwnedPortfolioId portfolioId = userInteractor.getApplicablePortfolioId();
        if (portfolioId != null)
        {
            Bundle args = new Bundle();
            args.putBundle(PositionListFragment.BUNDLE_KEY_SHOW_PORTFOLIO_ID_BUNDLE, portfolioId.getArgs());
            navigator.pushFragment(PositionListFragment.class, args);
        }
    }

    private void pushVideoElement(CompetitionZoneVideoDTO competitionZoneDTO)
    {
        Bundle args = new Bundle();
        args.putBundle(ProviderVideoListFragment.BUNDLE_KEY_PROVIDER_ID, providerId.getArgs());
        args.putBundle(ProviderVideoListFragment.BUNDLE_KEY_PURCHASE_APPLICABLE_PORTFOLIO_ID_BUNDLE,
                providerDTO.associatedPortfolio.getPortfolioId().getArgs());
        navigator.pushFragment(ProviderVideoListFragment.class, args);
    }

    private void pushWizardElement(CompetitionZoneWizardDTO competitionZoneDTO)
    {
        Bundle args = new Bundle();
        args.putString(WebViewFragment.BUNDLE_KEY_URL, ProviderConstants.getWizardPage(providerId) + "&previous=whatever");
        this.webViewFragment = (WebViewFragment) navigator.pushFragment(WebViewFragment.class, args);
        this.webViewFragment.setThIntentPassedListener(this.webViewTHIntentPassedListener);
    }

    private void pushLeaderboardElement(CompetitionZoneLeaderboardDTO competitionZoneDTO)
    {
        LeaderboardDefDTO leaderboardDefDTO = competitionZoneDTO.competitionDTO.leaderboard;
        Bundle args = new Bundle();
        if (competitionZoneDTO.competitionDTO.leaderboard.isWithinUtcRestricted())
        {
            args.putInt(CompetitionLeaderboardMarkUserListViewFragment.BUNDLE_KEY_LEADERBOARD_ID, leaderboardDefDTO.id);
            args.putString(CompetitionLeaderboardMarkUserListViewFragment.BUNDLE_KEY_LEADERBOARD_DEF_TITLE, leaderboardDefDTO.name);
            args.putInt(CompetitionLeaderboardMarkUserListViewFragment.BUNDLE_KEY_CURRENT_SORT_TYPE, leaderboardDefDTO.getDefaultSortType().getFlag());
            args.putString(CompetitionLeaderboardMarkUserListViewFragment.BUNDLE_KEY_LEADERBOARD_DEF_DESC, leaderboardDefDTO.desc);
            args.putInt(CompetitionLeaderboardMarkUserListViewFragment.BUNDLE_KEY_SORT_OPTION_FLAGS, leaderboardDefDTO.getSortOptionFlags());
            navigator.pushFragment(CompetitionLeaderboardMarkUserListViewFragment.class, args);
        }
        else
        {
            args.putInt(LeaderboardMarkUserListViewFragment.BUNDLE_KEY_LEADERBOARD_ID, leaderboardDefDTO.id);
            args.putString(LeaderboardMarkUserListViewFragment.BUNDLE_KEY_LEADERBOARD_DEF_TITLE, leaderboardDefDTO.name);
            args.putInt(LeaderboardMarkUserListViewFragment.BUNDLE_KEY_CURRENT_SORT_TYPE, leaderboardDefDTO.getDefaultSortType().getFlag());
            args.putString(LeaderboardMarkUserListViewFragment.BUNDLE_KEY_LEADERBOARD_DEF_DESC, leaderboardDefDTO.desc);
            args.putInt(LeaderboardMarkUserListViewFragment.BUNDLE_KEY_SORT_OPTION_FLAGS, leaderboardDefDTO.getSortOptionFlags());
            navigator.pushFragment(LeaderboardMarkUserListViewFragment.class, args);
        }
    }

    private void pushLegalElement(CompetitionZoneLegalDTO competitionZoneDTO)
    {
        Bundle args = new Bundle();
        if ((competitionZoneDTO).requestedLink.equals(CompetitionZoneLegalDTO.LinkType.RULES))
        {
            args.putString(WebViewFragment.BUNDLE_KEY_URL, ProviderConstants.getRulesPage(providerId));
        }
        else
        {
            args.putString(WebViewFragment.BUNDLE_KEY_URL, ProviderConstants.getTermsPage(providerId));
        }
        navigator.pushFragment(WebViewFragment.class, args);
    }
    //</editor-fold>

    private class MainCompetitionFragmentItemClickListener implements AdapterView.OnItemClickListener
    {
        @Override public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
        {
            THLog.d(TAG, "onItemClient");
            handleItemClicked((CompetitionZoneDTO) adapterView.getItemAtPosition(i));
        }
    }

    private class MainCompetitionWebViewTHIntentPassedListener implements THIntentPassedListener
    {
        @Override public void onIntentPassed(THIntent thIntent)
        {
            if (thIntent instanceof ProviderPageIntent)
            {
                THLog.d(TAG, "Intent is ProviderPageIntent");
                if (webViewFragment != null)
                {
                    THLog.d(TAG, "Passing on " + ((ProviderPageIntent) thIntent).getCompleteForwardUriPath());
                    webViewFragment.loadUrl(((ProviderPageIntent) thIntent).getCompleteForwardUriPath());
                }
                else
                {
                    THLog.d(TAG, "WebFragment is null");
                }
            }
            else if (thIntent == null)
            {
                navigator.popFragment();
            }
            else
            {
                THLog.w(TAG, "Unhandled intent " + thIntent);
            }
        }
    }

    private class MainCompetitionLegalClickedListener implements CompetitionZoneLegalMentionsView.OnElementClickedListener
    {
        @Override public void onElementClicked(CompetitionZoneDTO competitionZoneLegalDTO)
        {
            handleItemClicked(competitionZoneLegalDTO);
        }
    }

    private class MainCompetitionCompetitionListCacheListener implements DTOCache.Listener<ProviderId, CompetitionIdList>
    {
        @Override public void onDTOReceived(ProviderId providerId, CompetitionIdList value)
        {
            linkWith(value, true);
        }

        @Override public void onErrorThrown(ProviderId key, Throwable error)
        {
            THToast.show(getString(R.string.error_fetch_provider_competition_leaderboard_list));
            THLog.e(TAG, "Error fetching the list of competition info " + key, error);
        }
    }
}
