package com.tradehero.th.fragments.settings;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SectionIndexer;
import android.widget.TextView;
import com.tradehero.th2.R;
import com.tradehero.th.adapters.ArrayDTOAdapter;
import com.tradehero.th.api.social.UserFriendsDTO;
import com.tradehero.th.api.social.UserFriendsDTONameComparator;
import com.tradehero.th.api.social.UserFriendsContactEntryDTO;
import com.tradehero.th.api.social.UserFriendsFacebookDTO;
import com.tradehero.th.api.social.UserFriendsLinkedinDTO;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

public class FriendListAdapter extends ArrayDTOAdapter<UserFriendsDTO, UserFriendDTOView>
        implements StickyListHeadersAdapter, SectionIndexer
{
    private static final long NO_NAME_HEADER_ID = ' ';

    private String[] collectedNames;
    private Integer[] sectionIndices;
    private Character[] sections;
    private List<UserFriendsDTO> originalItems;

    public FriendListAdapter(Context context, LayoutInflater layoutInflater, int itemLayoutId)
    {
        super(context, layoutInflater, itemLayoutId);
    }

    @Override public void setItems(List<UserFriendsDTO> items)
    {
        filterOutInvitedFriends(items);

        originalItems = items != null ? Collections.unmodifiableList(items) : null;
        setItemsInternal(items);
    }

    private void setItemsInternal(List<UserFriendsDTO> items)
    {
        super.setItems(items);
        sortUserFriendListByName();
        initNamesFromDTOList();
        initSectionIndices();
        initDistinctFirstCharacterNames();
    }

    private void filterOutInvitedFriends(List<UserFriendsDTO> items)
    {
        ListIterator<UserFriendsDTO> listIterator = items.listIterator();
        while (listIterator.hasNext()){
            UserFriendsDTO userFriendsDTO = listIterator.next();
            if (userFriendsDTO != null && userFriendsDTO.alreadyInvited)
            {
                listIterator.remove();
            }
        }
    }

    @Override protected void fineTune(int position, UserFriendsDTO dto, UserFriendDTOView dtoView)
    {
    }

    @Override public View getHeaderView(int position, View convertView, ViewGroup parent)
    {
        SectionViewHolder sectionViewHolder;
        if (convertView == null)
        {
            convertView = inflater.inflate(R.layout.refer_friend_header_view, parent, false);
            TextView sectionTextView = (TextView) convertView.findViewById(R.id.refer_friend_list_header);
            sectionViewHolder = new SectionViewHolder();
            sectionViewHolder.labelText = sectionTextView;

            convertView.setTag(sectionViewHolder);
        }
        else
        {
            sectionViewHolder = (SectionViewHolder) convertView.getTag();
        }

        if (sectionViewHolder != null && sectionViewHolder.labelText != null && sections != null)
        {
            sectionViewHolder.labelText.setText("" + sections[getSectionForPosition(position)]);
        }

        return convertView;
    }

    @Override public long getHeaderId(int position)
    {
        UserFriendsDTO item = (UserFriendsDTO) getItem(position);
        if (item != null && item.name != null && item.name.length() > 0)
        {
            return Character.toUpperCase(item.name.charAt(0));
        }
        return NO_NAME_HEADER_ID;
    }

    @Override public Object[] getSections()
    {
        return sections;
    }

    private void sortUserFriendListByName()
    {
        if (items != null)
        {
            Collections.sort(items, new UserFriendsDTONameComparator());
        }
    }

    private Character[] initDistinctFirstCharacterNames()
    {
        sections = null;
        if (sectionIndices != null)
        {
            sections = new Character[sectionIndices.length];
            for (int i = 0; i < sectionIndices.length; ++i)
            {
                sections[i] = collectedNames[sectionIndices[i]].charAt(0);
            }
        }
        return sections;
    }

    /**
     * Position of where each section started
     */
    private void initSectionIndices()
    {
        sectionIndices = null;

        // supposedly, each collected name is not an empty string
        if (collectedNames != null && collectedNames.length > 0)
        {
            // collect distinct list of first appearance character
            List<Integer> firstCharacterAppearanceIndices = new ArrayList<>();
            char lastBeginningCharacter = collectedNames[0].charAt(0);
            firstCharacterAppearanceIndices.add(0);
            if (collectedNames.length > 1)
            {
                for (int characterIndex = 1; characterIndex < collectedNames.length; ++characterIndex)
                {
                    String name = collectedNames[characterIndex];
                    if (name != null && name.length() > 0 && name.charAt(0) != lastBeginningCharacter)
                    {
                        lastBeginningCharacter = name.charAt(0);
                        firstCharacterAppearanceIndices.add(characterIndex);
                    }
                }
            }

            // populate sectionIndices array
            sectionIndices = new Integer[firstCharacterAppearanceIndices.size()];
            firstCharacterAppearanceIndices.toArray(sectionIndices);
        }
    }

    private void initNamesFromDTOList()
    {
        collectedNames = null;
        if (items != null)
        {
            ArrayList<String> nameList = new ArrayList<>();
            for (UserFriendsDTO userFriendsDTO: items)
            {
                if (userFriendsDTO.name != null && !userFriendsDTO.name.isEmpty())
                {
                    nameList.add(userFriendsDTO.name);
                }
            }
            collectedNames = new String[nameList.size()];
            nameList.toArray(collectedNames);
        }
    }

    @Override public int getPositionForSection(int section)
    {
        if (sections == null)
        {
            return 0;
        }

        Integer[] sectionIndicesCopy = sectionIndices;
        if (section >= sectionIndicesCopy.length)
        {
            return sectionIndicesCopy.length - 1;
        }
        if (section < 0)
        {
            return 0;
        }
        return sectionIndicesCopy[section];
    }

    @Override public int getSectionForPosition(int position)
    {
        if (sections == null)
        {
            return 0;
        }

        Integer[] sectionIndicesCopy = sectionIndices;
        for (int i = 0; i < sectionIndicesCopy.length; i++)
        {
            if (position < sectionIndicesCopy[i])
            {
                return i - 1;
            }
        }
        return sectionIndicesCopy.length - 1;
    }

    public void filter(String searchText)
    {
        List<UserFriendsDTO> newItems = new ArrayList<>();
        if (originalItems != null && searchText != null)
        {
            for (UserFriendsDTO userFriendsDTO: originalItems)
            {
                if (userFriendsDTO != null && userFriendsDTO.name != null)
                {
                    if (userFriendsDTO.name.toUpperCase().contains(searchText.toUpperCase()))
                    {
                        newItems.add(userFriendsDTO);
                    }
                }
            }
        }
        setItemsInternal(newItems);
    }

    public void resetItems()
    {
        setItemsInternal(new ArrayList<>(originalItems));
    }

    public int getSelectedCount()
    {
        int count = 0;
        for (UserFriendsDTO userFriendsDTO: originalItems)
        {
            if (userFriendsDTO != null && userFriendsDTO.selected)
            {
                ++count;
            }
        }
        return count;
    }

    public List<UserFriendsLinkedinDTO> getSelectedLinkedInFriends()
    {
        List<UserFriendsLinkedinDTO> selectedItems = new ArrayList<>();
        for (UserFriendsDTO userFriendsDTO: originalItems)
        {
            if (userFriendsDTO.selected && userFriendsDTO instanceof UserFriendsLinkedinDTO)
            {
                selectedItems.add((UserFriendsLinkedinDTO) userFriendsDTO);
            }
        }
        return selectedItems;
    }

    public List<UserFriendsFacebookDTO> getSelectedFacebookFriends()
    {
        List<UserFriendsFacebookDTO> selectedItems = new ArrayList<>();
        for (UserFriendsDTO userFriendsDTO: originalItems)
        {
            if (userFriendsDTO.selected && userFriendsDTO instanceof UserFriendsFacebookDTO)
            {
                selectedItems.add((UserFriendsFacebookDTO) userFriendsDTO);
            }
        }
        return selectedItems;
    }

    public List<UserFriendsDTO> getSelectedContacts()
    {
        List<UserFriendsDTO> selectedItems = new ArrayList<>();
        for (UserFriendsDTO userFriendsDTO: originalItems)
        {
            if (userFriendsDTO.selected && userFriendsDTO instanceof UserFriendsContactEntryDTO)
            {
                selectedItems.add(userFriendsDTO);
            }
        }
        return selectedItems;
    }

    public void toggleLinkedInSelection(boolean isSelected)
    {
        for (UserFriendsDTO userFriendsDTO: originalItems)
        {
            if (userFriendsDTO instanceof UserFriendsLinkedinDTO)
            {
                userFriendsDTO.selected = isSelected;
            }
        }
    }

    public void toggleFacebookSelection(boolean isSelected)
    {
        for (UserFriendsDTO userFriendsDTO: originalItems)
        {
            if (userFriendsDTO instanceof UserFriendsFacebookDTO)
            {
                userFriendsDTO.selected = isSelected;
            }
        }
    }

    public void toggleContactSelection(boolean isSelected)
    {
        for (UserFriendsDTO userFriendsDTO: originalItems)
        {
            if (userFriendsDTO instanceof UserFriendsContactEntryDTO)
            {
                userFriendsDTO.selected = isSelected;
            }
        }
    }

    private static class SectionViewHolder
    {
        TextView labelText = null;
    }
}
