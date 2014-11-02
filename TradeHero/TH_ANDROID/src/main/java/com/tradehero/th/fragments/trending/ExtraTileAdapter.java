package com.tradehero.th.fragments.trending;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.WrapperListAdapter;
import com.tradehero.th.api.competition.ProviderDTOList;
import com.tradehero.th.api.competition.key.ProviderListKey;
import com.tradehero.th.api.users.CurrentUserId;
import com.tradehero.th.api.users.UserProfileDTO;
import com.tradehero.th.inject.HierarchyInjector;
import com.tradehero.th.persistence.competition.ProviderListCacheRx;
import com.tradehero.th.persistence.user.UserProfileCacheRx;
import com.tradehero.th.utils.StringUtils;
import dagger.Lazy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import org.jetbrains.annotations.NotNull;
import timber.log.Timber;

public class ExtraTileAdapter extends BaseAdapter
        implements WrapperListAdapter
{
    private static final int EXTRA_TILE_FREQUENCY = 16;
    private static final int EXTRA_TILE_MIN_DISTANCE = 10;

    private int itemHeight = 0;

    @NotNull private final ListAdapter wrappedAdapter;
    @NotNull private final LayoutInflater inflater;

    @Inject CurrentUserId currentUserId;
    @Inject Lazy<UserProfileCacheRx> userProfileCache;
    @Inject Lazy<ProviderListCacheRx> providerListCache;

    private final SharedPreferences mPref;

    private Pair<TileType, Integer>[] extraTilesMarker;
    // selected marker which contain the most number of tiles and positions
    private Pair<TileType, Integer>[] masterTilesMarker;
    private boolean surveyEnabled;
    private boolean providerDataAvailable;
    private int headingTilesCount;

    //<editor-fold desc="Constructors">
    public ExtraTileAdapter(Context context, @NotNull ListAdapter wrappedAdapter)
    {
        this.inflater = LayoutInflater.from(context);
        this.wrappedAdapter = wrappedAdapter;
        wrappedAdapter.registerDataSetObserver(wrappedAdapterDataSetObserver);
        HierarchyInjector.inject(context, this);

        mPref = context.getSharedPreferences("trade_hero", Context.MODE_PRIVATE);
    }
    //</editor-fold>

    @Override public void registerDataSetObserver(DataSetObserver observer)
    {
        wrappedAdapter.registerDataSetObserver(observer);
    }

    @Override public void unregisterDataSetObserver(DataSetObserver observer)
    {
        wrappedAdapter.unregisterDataSetObserver(observer);
    }

    @Override public int getCount()
    {
        return wrappedAdapter.getCount() + (extraTilesMarker != null ? extraTilesMarker.length : 0);
    }

    private int getWrappedPosition(int position)
    {
        if (extraTilesMarker != null)
        {
            //Timber.d("getWrappedPosition begin, length=%d", extraTilesMarker.length);
            for (int i = 0; i < extraTilesMarker.length; ++i)
            {
                //Timber.d("mark position: %d", extraTilesMarker[i].second);
                if (position == extraTilesMarker[i].second)
                {
                    return -1;
                }
                else if (position < extraTilesMarker[i].second)
                {
                    return position - i;
                }
            }

            //Timber.d("%d ---> %d (extraTilesMarker)", position, position - extraTilesMarker.length);
            return position - extraTilesMarker.length;
        }
        else
        {
            return position;
        }
    }

    @Override public Object getItem(int position)
    {
        if (extraTilesMarker != null)
        {
            for (Pair<TileType, Integer> marker : extraTilesMarker)
            {
                if (position == marker.second)
                {
                    return marker.first;
                }
            }

            int wrappedPosition = getWrappedPosition(position);
            if (wrappedPosition >= 0)
            {
                return wrappedAdapter.getItem(wrappedPosition);
            }
        }
        else
        {
            return wrappedAdapter.getItem(position);
        }

        Timber.d("getItem return null, extraTilesMarker=%s, ", (Object[]) extraTilesMarker);
        return null;
    }

    @Override public long getItemId(int position)
    {
        return position;
    }

    @Override public boolean hasStableIds()
    {
        return wrappedAdapter.hasStableIds();
    }

    @Override public View getView(int position, View convertView, ViewGroup parent)
    {
        int viewType = getItemViewType(position);
        Timber.d("getView position:%d viewType:%d",position,viewType);
        if (viewType == TileType.Normal.ordinal())
        {
            return wrappedAdapter.getView(getWrappedPosition(position), convertView, parent);
        }

        if (convertView == null)
        {
            convertView = inflater.inflate(TileType.at(viewType).getLayoutResourceId(), parent, false);
        }

        // TODO @Liang change this to IntPreference
        itemHeight = mPref.getInt("trending_item_height", 0);
        if (itemHeight != 0 && convertView.getHeight() != itemHeight)
        {
            ViewGroup.LayoutParams lp = convertView.getLayoutParams();
            lp.height = itemHeight;
        }

        return convertView;
    }

    @Override public int getItemViewType(int position)
    {
        int wrappedPosition = getWrappedPosition(position);

        if (wrappedPosition >= 0)
        {
            return TileType.Normal.ordinal();
        }
        else
        {
            Object item = getItem(position);
            if (item instanceof TileType)
            {
                return ((TileType) item).ordinal();
            }
            throw new IllegalAccessError("Item without viewType at " + position + ", value " + item);
        }
    }

    @Override public int getViewTypeCount()
    {
        return TileType.values().length;
    }

    @Override public boolean isEmpty()
    {
        return wrappedAdapter.isEmpty();
    }

    @Override @NotNull public ListAdapter getWrappedAdapter()
    {
        return wrappedAdapter;
    }

    @Override public boolean areAllItemsEnabled()
    {
        return wrappedAdapter.areAllItemsEnabled();
    }

    @Override public boolean isEnabled(int position)
    {
        if (getItemViewType(position) == TileType.Normal.ordinal())
        {
            return wrappedAdapter.isEnabled(getWrappedPosition(position));
        }
        else
        {
            Object item = getItem(position);
            if (item instanceof TileType)
            {
                return ((TileType) item).isEnable();
            }
        }
        return false;
    }

    @Override public void notifyDataSetChanged()
    {
        // consider uncomment following line when the hack in securityListFragment is resolved
        // regenerateExtraTiles();
        super.notifyDataSetChanged();
    }

    public void regenerateExtraTiles()
    {
        regenerateExtraTiles(false, false);
    }

    public void regenerateExtraTiles(boolean refreshIndexes, boolean refreshTiles)
    {
        int extraTileCount = Math.round(wrappedAdapter.getCount() / EXTRA_TILE_FREQUENCY);

        if (extraTileCount > 0)
        {
            Pair<TileType, Integer>[] tempMarker = null;
            if (!refreshIndexes && !refreshTiles && masterTilesMarker != null && extraTileCount < masterTilesMarker.length)
            {
                tempMarker = Arrays.copyOf(masterTilesMarker, extraTileCount);
            }
            else
            {
                updateHeadingTilesStatus();

                // regenerate indexes for tiles, reuse as much as possible
                int[] extraTileIndexes = null;
                if (!refreshIndexes && masterTilesMarker != null && extraTileCount < masterTilesMarker.length)
                {
                    extraTileIndexes = new int[extraTileCount];
                    for (int i=0; i<extraTileCount; ++i)
                    {
                        extraTileIndexes[i] = masterTilesMarker[i].second;
                    }
                }
                else
                {
                    extraTileIndexes = generateExtraTileIndexes(extraTileCount);
                }

                // regenerate tile types, reuse as much as possible
                TileType[] showingTiles = null;
                if (!refreshTiles && masterTilesMarker != null && extraTileCount < masterTilesMarker.length)
                {
                    showingTiles = new TileType[extraTileCount];
                    for (int i=0; i<extraTileCount; ++i)
                    {
                        showingTiles[i] = masterTilesMarker[i].first;
                    }
                }
                else
                {
                    showingTiles = generateRandomTypeForTiles(extraTileIndexes);
                }

                tempMarker = new Pair[extraTileCount];

                // TODO make it better by only generate new tile positions
                for (int i = 0; i < extraTileCount; ++i)
                {
                    tempMarker[i] = Pair.create(showingTiles[i], extraTileIndexes[i]);
                }

                if (!refreshTiles && masterTilesMarker != null)
                {
                    System.arraycopy(masterTilesMarker, 0, tempMarker, 0, masterTilesMarker.length);
                }
                masterTilesMarker = tempMarker;
            }

            // add some special tile at the beginning of the list
            extraTilesMarker = createHeadingTiles(tempMarker);
        }
        else
        {
            extraTilesMarker = null;
        }
    }

    private void updateHeadingTilesStatus()
    {
        surveyEnabled = isSurveyEnabled();
        providerDataAvailable = isProviderDataAvailable();

        headingTilesCount = 0;
        headingTilesCount += surveyEnabled ? 1 : 0;
        headingTilesCount += providerDataAvailable ? 1 : 0;
    }

    private Pair<TileType, Integer>[] createHeadingTiles(Pair<TileType, Integer>[] originalMarker)
    {
        if (originalMarker == null)
        {
            return null;
        }

        Pair<TileType, Integer>[] headingTiles = new Pair[originalMarker.length + headingTilesCount];

        int specialTileIndex = 0;
        if (surveyEnabled)
        {
            headingTiles[specialTileIndex] = Pair.create(TileType.Survey, specialTileIndex);
            ++specialTileIndex;
        }

        if (providerDataAvailable)
        {
            headingTiles[specialTileIndex] = Pair.create(TileType.FromProvider, specialTileIndex);
            ++specialTileIndex;
        }

        System.arraycopy(originalMarker, 0, headingTiles, specialTileIndex, originalMarker.length);
        return headingTiles;
    }

    private boolean isProviderDataAvailable()
    {
        ProviderDTOList providerDTOs = providerListCache.get().getValue(new ProviderListKey());
        if (providerDTOs != null)
        {
            //Timber.d("Provider has %d items", providerDTOs.size());
        }
        return providerDTOs != null && !providerDTOs.isEmpty();
    }

    private TileType[] generateRandomTypeForTiles(int[] extraTileIndexes)
    {
        List<TileType> showingTileTypes = getShowingTileTypes();
        List<TileType> showingTiles = new ArrayList<>();

        for (int i = 0; i < extraTileIndexes.length; ++i)
        {
            showingTiles.add(showingTileTypes.get(i % showingTileTypes.size()));
        }
        // and shuffle the tiles
        Collections.shuffle(showingTiles);

        TileType[] retArray = new TileType[showingTiles.size()];
        showingTiles.toArray(retArray);
        return retArray;
    }

    private List<TileType> getShowingTileTypes()
    {
        List<TileType> showingTileTypes = new ArrayList<>();
        for (TileType tileType : TileType.values())
        {
            if (tileType.isExtra())
            {
                showingTileTypes.add(tileType);
            }
        }
        if (!surveyEnabled)
        {
            showingTileTypes.remove(TileType.Survey);
        }
        if (!providerDataAvailable)
        {
            showingTileTypes.remove(TileType.FromProvider);
        }
        return Collections.unmodifiableList(showingTileTypes);
    }

    private int[] generateExtraTileIndexes(int extraTileCount)
    {
        Timber.d("Regenerating ... with headingTilesCount=%d", headingTilesCount);
        int maxTileIndex = wrappedAdapter.getCount() + extraTileCount - 1;
        int previousIndex = -1;
        int[] extraTileIndexes = new int[extraTileCount];

        for (int i = 0; i < extraTileCount; ++i)
        {
            int newTileIndex = i * EXTRA_TILE_FREQUENCY + (int) (Math.random() * EXTRA_TILE_FREQUENCY);
            if (previousIndex > 0 && (newTileIndex - previousIndex < EXTRA_TILE_MIN_DISTANCE))
            {
                newTileIndex = previousIndex + EXTRA_TILE_MIN_DISTANCE;
            }
            else if (previousIndex == -1 && newTileIndex <= 1)
            {
                newTileIndex += headingTilesCount;
            }
            // side effect of previous tiles insertion, also there should not be any overlapping between 2 tiles random space
            newTileIndex += i % EXTRA_TILE_MIN_DISTANCE;
            newTileIndex = Math.min(maxTileIndex, newTileIndex);
            Timber.d("Tile index: %d", newTileIndex);
            previousIndex = newTileIndex;
            extraTileIndexes[i] = newTileIndex;
        }

        return extraTileIndexes;
    }

    private boolean isSurveyEnabled()
    {
        UserProfileDTO userProfileDTO = userProfileCache.get().getValue(currentUserId.toUserBaseKey());
        return userProfileDTO != null && !StringUtils.isNullOrEmpty(userProfileDTO.activeSurveyImageURL);
    }

    // TODO memory leak
    private final DataSetObserver wrappedAdapterDataSetObserver = new DataSetObserver()
    {
        @Override public void onChanged()
        {
            //Timber.d("onChanged");
            notifyDataSetChanged();
        }

        @Override public void onInvalidated()
        {
            notifyDataSetInvalidated();
        }
    };
}
