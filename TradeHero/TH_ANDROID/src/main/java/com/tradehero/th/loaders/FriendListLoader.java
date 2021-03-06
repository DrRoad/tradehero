package com.tradehero.th.loaders;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.Contacts;
import com.tradehero.common.utils.THToast;
import com.tradehero.th.R;
import com.tradehero.th.api.social.UserFriendsContactEntryDTO;
import com.tradehero.th.api.social.UserFriendsDTO;
import com.tradehero.th.api.social.key.FriendsListKey;
import com.tradehero.th.api.users.CurrentUserId;
import com.tradehero.th.inject.HierarchyInjector;
import com.tradehero.th.network.service.UserServiceWrapper;
import dagger.Lazy;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import timber.log.Timber;

public class FriendListLoader extends ListLoader<UserFriendsDTO>
{
    private static final String[] CONTACT_PROJECTIONS =
            {
                    Contacts._ID,
                    Contacts.DISPLAY_NAME,
                    Contacts.PHOTO_URI,
            };
    private static final int CONTACT_ID_COLUMN = 0;
    private static final int CONTACT_DISPLAY_NAME_COLUMN = 1;
    private static final int CONTACT_PHOTO_URI_COLUMN = 2;

    private static final String[] EMAIL_PROJECTIONS =
            {
                    Email._ID,
                    Email.CONTACT_ID,
                    Email.ADDRESS
            };
    private static final int EMAIL_CONTACT_ID_COLUMN = 1;
    private static final int EMAIL_ADDRESS_COLUMN = 2;

    private static final String EMAIL_SELECTION = "";
    private static final String CONTACT_SELECTION = "";
    private static final String CONTACT_ID_SORT_ORDER = Contacts._ID + " ASC";
    private static final String EMAIL_CONTACT_ID_SORT_ORDER = Email.CONTACT_ID + " ASC";

    @Inject protected Lazy<UserServiceWrapper> userServiceWrapper;
    @Inject protected CurrentUserId currentUserId;

    private List<UserFriendsDTO> contactEntries;
    private List<UserFriendsDTO> userFriendsDTOs;

    public FriendListLoader(Context context)
    {
        super(context);
        HierarchyInjector.inject(context, this);
    }

    @Override public List<UserFriendsDTO> loadInBackground()
    {
        Thread emailRetrieverThread = new Thread(new Runnable()
        {
            @Override public void run()
            {
                readContacts();
            }
        });
        emailRetrieverThread.setUncaughtExceptionHandler(createExceptionHandler(R.string.error_fetch_local_contacts));

        Thread friendListRetrieverThread = new Thread(new Runnable()
        {
            @Override public void run()
            {
                retrieveFriendList();
            }
        });
        friendListRetrieverThread.setUncaughtExceptionHandler(createExceptionHandler(R.string.error_fetch_server_friends));

        List<UserFriendsDTO> friendsDTOs = new ArrayList<>();
        emailRetrieverThread.start();
        friendListRetrieverThread.start();

        try
        {
            emailRetrieverThread.join();
            friendListRetrieverThread.join();
        }
        catch (InterruptedException e)
        {
            THToast.show(R.string.error_fetch_friends);
            Timber.e(e, "Unable to get friend list");
            return friendsDTOs;
        }

        if (contactEntries != null && !contactEntries.isEmpty())
        {
            friendsDTOs.addAll(contactEntries);
        }
        if (userFriendsDTOs != null && !userFriendsDTOs.isEmpty())
        {
            friendsDTOs.addAll(userFriendsDTOs);
        }

        return friendsDTOs;
    }

    private Thread.UncaughtExceptionHandler createExceptionHandler(final int toastResId)
    {
        return new Thread.UncaughtExceptionHandler()
        {
            @Override public void uncaughtException(Thread thread, Throwable ex)
            {
                THToast.show(toastResId);
                //Timber.e(ex, getContext().getString(toastResId));
            }
        };
    }

    private void readContacts()
    {
        Cursor emailQueryCursor = getContext().getContentResolver().query(
                Email.CONTENT_URI,
                EMAIL_PROJECTIONS,
                EMAIL_SELECTION,
                null,
                EMAIL_CONTACT_ID_SORT_ORDER);

        Cursor contactQueryCursor = getContext().getContentResolver().query(
                Contacts.CONTENT_URI,
                CONTACT_PROJECTIONS,
                CONTACT_SELECTION,
                null,
                CONTACT_ID_SORT_ORDER);

        contactEntries = new ArrayList<>();
        if (emailQueryCursor != null && !emailQueryCursor.isClosed())
        {
            // 2 while-loops but complexity is O(max(number_of_emails, number_of_contact))
            while (emailQueryCursor.moveToNext())
            {
                ContactEntry contactEntry = new ContactEntry();
                String currentEmail = emailQueryCursor.getString(EMAIL_ADDRESS_COLUMN);
                if (currentEmail != null)
                {
                    contactEntry.setEmail(currentEmail);
                    int contactIdFromEmailTable = emailQueryCursor.getInt(EMAIL_CONTACT_ID_COLUMN);

                    while (contactQueryCursor.moveToNext())
                    {
                        int contactIdFromContactTable = contactQueryCursor.getInt(CONTACT_ID_COLUMN);
                        Timber.d("contactId from Contact/Email table: %d/%d",
                                contactIdFromContactTable, contactIdFromEmailTable);
                        if (contactIdFromContactTable > contactIdFromEmailTable)
                        {
                            break;
                        }
                        if (contactIdFromContactTable == contactIdFromEmailTable)
                        {
                            contactEntry.setName(contactQueryCursor.getString(CONTACT_DISPLAY_NAME_COLUMN));
                            // contactEntry.setPhotoUri(contactQueryCursor.get(CONTACT_PHOTO_URI_COLUMN));
                            break;
                        }
                    }
                    if (contactEntry.getName() == null)
                    {
                        contactEntry.setName(currentEmail);
                    }
                }
                contactEntries.add(new UserFriendsContactEntryDTO(contactEntry));
            }

            if (contactQueryCursor != null && !contactQueryCursor.isClosed())
            {
                contactQueryCursor.close();
            }
            emailQueryCursor.close();
        }
    }

    private void retrieveFriendList()
    {
        userFriendsDTOs = userServiceWrapper.get().getFriends(new FriendsListKey(currentUserId.toUserBaseKey()));
    }
}
