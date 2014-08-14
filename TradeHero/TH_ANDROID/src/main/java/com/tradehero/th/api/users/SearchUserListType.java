package com.tradehero.th.api.users;

import com.tradehero.common.api.PagedDTOKey;

public class SearchUserListType extends UserListType
    implements PagedDTOKey
{
    //<editor-fold desc="Fields">
    public final String searchString;
    public final Integer page;
    public final Integer perPage;
    //</editor-fold>

    //<editor-fold desc="Constructors">
    public SearchUserListType(String searchString, Integer page, Integer perPage)
    {
        this.searchString = searchString;
        this.page = page;
        this.perPage = perPage;
    }
    //</editor-fold>

    @Override public Integer getPage()
    {
        return page;
    }

    @Override public int hashCode()
    {
        return (searchString == null ? 0 : searchString.hashCode()) ^
                (page == null ? 0 : page.hashCode()) ^
                (perPage == null ? 0 : perPage.hashCode());
    }

    @Override public boolean equalFields(UserListType other)
    {
        return equalFields ((SearchUserListType) other);
    }

    public boolean equalFields(SearchUserListType other)
    {
        return (searchString == null ? other.searchString == null : searchString.equals(other.searchString)) &&
                (page == null ? other.page == null : page.equals(other.page)) &&
                (perPage == null ? other.perPage == null : perPage.equals(other.perPage));
    }

    //<editor-fold desc="Comparable">
    @Override public int compareTo(UserListType other)
    {
        if (other == null)
        {
            return 1;
        }
        if (!(other instanceof SearchUserListType))
        {
            return SearchUserListType.class.getName().compareTo(other.getClass().getName());
        }

        SearchUserListType searchUserListType = (SearchUserListType) other;

        int stringCompare = searchString.compareTo(searchUserListType.searchString);
        if (stringCompare != 0)
        {
            return stringCompare;
        }
        int pageCompare = page.compareTo(searchUserListType.page);
        if (pageCompare != 0)
        {
            return pageCompare;
        }
        return perPage.compareTo(searchUserListType.perPage);
    }
    //</editor-fold>

    @Override public String toString()
    {
        return "SearchUserListType{" +
                "searchString='" + searchString + '\'' +
                ", page=" + page +
                ", perPage=" + perPage +
                '}';
    }
}
