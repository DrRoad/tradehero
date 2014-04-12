package com.tradehero.th.fragments.social.follower;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.tradehero.common.utils.THToast;
import com.tradehero.common.widget.dialog.THDialog;
import com.tradehero.th.R;
import com.tradehero.th.api.discussion.DiscussionDTO;
import com.tradehero.th.api.discussion.DiscussionType;
import com.tradehero.th.api.discussion.MessageType;
import com.tradehero.th.api.discussion.form.MessageCreateFormDTO;
import com.tradehero.th.api.discussion.form.MessageCreateFormDTOFactory;
import com.tradehero.th.api.social.FollowerSummaryDTO;
import com.tradehero.th.api.users.CurrentUserId;
import com.tradehero.th.api.users.UserBaseKey;
import com.tradehero.th.fragments.base.BaseFragment;
import com.tradehero.th.network.service.MessageServiceWrapper;
import com.tradehero.th.persistence.social.FollowerSummaryCache;
import com.tradehero.th.persistence.social.HeroKey;
import com.tradehero.th.persistence.social.HeroType;
import com.tradehero.th.utils.ProgressDialogUtil;
import dagger.Lazy;
import javax.inject.Inject;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import timber.log.Timber;

/**
 * Created by tradehero on 14-4-1.
 */
public class SendMessageFragment extends BaseFragment implements AdapterView.OnItemSelectedListener, View.OnClickListener
{
    public static final String KEY_DISCUSSION_TYPE = SendMessageFragment.class.getName() + ".discussionType";
    public static final String KEY_MESSAGE_TYPE = SendMessageFragment.class.getName() + ".messageType";

    public static enum MessageLifeTime
    {
        LIFETIME_FOREVER(0),
        LIFETIME_1_HOUR(1),
        LIFETIME_2_HOURS(2),
        LIFETIME_1_DAY(3);

        public final int id;

        private MessageLifeTime(int id)
        {
            this.id = id;
        }

        @Override public String toString()
        {
            switch (this)
            {
                case LIFETIME_FOREVER:
                    return "Forever";
                case LIFETIME_1_HOUR:
                    return "One hour";
                case LIFETIME_2_HOURS:
                    return "Two hour";
                case LIFETIME_1_DAY:
                    return "One day";
            }
            return null;
        }

        //
    }

    private MessageType messageType = MessageType.BROADCAST_ALL_FOLLOWERS;
    private DiscussionType discussionType = DiscussionType.BROADCAST_MESSAGE;
    private MessageLifeTime messageLifeTime = MessageLifeTime.LIFETIME_FOREVER;

    @InjectView(R.id.message_input_edittext) EditText inputText;
    @InjectView(R.id.message_spinner_lifetime) Spinner lifeTimeSpinner;
    @InjectView(R.id.message_spinner_target_user) Spinner targetUserSpinner;

    @InjectView(R.id.message_type_wrapper) View messageTypeWrapperView;
    @InjectView(R.id.message_type) TextView messageTypeView;

    @Inject MessageCreateFormDTOFactory messageCreateFormDTOFactory;
    @Inject Lazy<MessageServiceWrapper> messageServiceWrapper;
    //@Inject UserBaseKey user;
    @Inject CurrentUserId currentUserId;

    @Inject protected Lazy<FollowerSummaryCache> followerSummaryCache;

    Dialog progressDialog;
    Dialog chooseDialog;
    SendMessageDiscussionCallback sendMessageDiscussionCallback;

    @Override public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        int discussionTypeValue = args.getInt(SendMessageFragment.KEY_DISCUSSION_TYPE, DiscussionType.BROADCAST_MESSAGE.value);
        this.discussionType = DiscussionType.fromValue(discussionTypeValue);

        int messageTypeInt = args.getInt(SendMessageFragment.KEY_MESSAGE_TYPE);
        this.messageType = MessageType.fromId(messageTypeInt);

        Timber.d("onCreate messageType:%s,discussionType:%s", messageType, discussionType);
        sendMessageDiscussionCallback = new SendMessageDiscussionCallback();
    }

    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        ActionBar actionBar = getSherlockActivity().getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_HOME);
        actionBar.setTitle("Broadcast Message");

        MenuItem menuItem = menu.add(0, 100, 0, "Send");
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == 100)
        {
            sendMessage();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_broadcast, container, false);
        ButterKnife.inject(this, v);
        return v;
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        //setSpinner();
        initView();
    }

    private void initView()
    {
        messageTypeWrapperView.setOnClickListener(this);
        changeHeroType(messageType);
    }

    @Override public void onDestroy()
    {
        sendMessageDiscussionCallback = null;
        super.onDestroy();
    }
    //private void setSpinner()
    //{
    //    ArrayAdapter  lifeTimeAdapter = new ArrayAdapter(getActivity(),android.R.layout.simple_spinner_item, android.R.id.text1,MessageLifeTime.values());
    //    lifeTimeSpinner.setAdapter(lifeTimeAdapter);
    //    lifeTimeSpinner.setOnItemSelectedListener(this);
    //    ArrayAdapter  targetUserAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_item,android.R.id.text1,HeroType.values());
    //    targetUserSpinner.setAdapter(targetUserAdapter);
    // }

    private void changeHeroType(MessageType messageType)
    {
        this.messageType = messageType;
        this.messageTypeView.setText(messageType.toString());
        Timber.d("changeHeroType:%s, discussionType:%s", messageType, discussionType);
    }

    private void showHeroTypeDialog()
    {
        //R.layout.message_type_dialog_layout
        ListView listView = new ListView(getActivity());
        listView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        TextView headerView = new TextView(getActivity());
        headerView.setPadding(0, 20, 0, 20);
        headerView.setGravity(Gravity.CENTER);
        headerView.setText("Choose follower to send message"); //TODO
        headerView.setClickable(false);
        headerView.setBackgroundColor(getResources().getColor(android.R.color.white));
        listView.addHeaderView(headerView);
        listView.setBackgroundColor(getResources().getColor(android.R.color.white));
        listView.setSelector(R.drawable.common_dialog_item_bg);
        listView.setCacheColorHint(android.R.color.transparent);
        ArrayAdapter arrayAdapter = new ArrayAdapter(getActivity(), R.layout.common_dialog_item_layout, R.id.popup_text, MessageType.getShowingTypes());
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Object o = parent.getAdapter().getItem(position);
                Timber.d("onItemClick %d, object:%s", position, o);
                changeHeroType((MessageType) o);
                dismissDialog(chooseDialog);
            }
        });
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        linearLayout.addView(listView);
        this.chooseDialog = THDialog.showUpDialog(getSherlockActivity(), linearLayout, null);
    }

    private void
    sendMessage()
    {
        int count = getFollowerCount(messageType);
        if (count <= 0)
        {
            THToast.show("Sorry,you cannot send message because you don't have such type follower");
            return;
        }

        String text = inputText.getText().toString();
        if (TextUtils.isEmpty(text))
        {
            THToast.show("The message cannot be empty");
            return;
        }
        this.progressDialog = ProgressDialogUtil.show(getActivity(), "Waiting", "Sending message...");

        // TODO not sure about this implementation yet
        messageServiceWrapper.get().createMessage(createMessageForm(text), sendMessageDiscussionCallback);
    }

    private MessageCreateFormDTO createMessageForm(String messageText)
    {
        MessageCreateFormDTO messageCreateFormDTO = messageCreateFormDTOFactory.createEmpty(messageType);
        messageCreateFormDTO.message = messageText;
        return messageCreateFormDTO;
    }

    /**
     * return how many followers whom you will send message to
     */
    private int getFollowerCount(MessageType messageType)
    {
        UserBaseKey userBaseKey = currentUserId.toUserBaseKey();
        HeroType heroType = HeroType.ALL;

        HeroKey heroKey = new HeroKey(userBaseKey, heroType);
        FollowerSummaryDTO followerSummaryDTO = followerSummaryCache.get().get(heroKey);
        if (followerSummaryDTO != null)
        {
            int result = 0;
            switch (messageType)
            {
                case BROADCAST_FREE_FOLLOWERS:
                    result = followerSummaryDTO.freeFollowerCount;
                    break;
                case BROADCAST_ALL_FOLLOWERS:
                    result = followerSummaryDTO.freeFollowerCount + followerSummaryDTO.paidFollowerCount;
                    break;
                case BROADCAST_PAID_FOLLOWERS:
                    result = followerSummaryDTO.paidFollowerCount;
                    break;
                default:
                    throw new IllegalStateException("unknown messageType");
            }
            Timber.d("getFollowerCount %s,paidFollowerCount:%d,freeFollowerCount:%d", messageType, followerSummaryDTO.paidFollowerCount, followerSummaryDTO.freeFollowerCount);
            return result;
        }
        return 0;
    }

    private void dismissDialog(Dialog dialog)
    {
        try
        {
            if (dialog != null && dialog.isShowing())
            {
                dialog.dismiss();
                dialog = null;
            }
        }
        catch (Exception e)
        {

        }
    }

    @Override public void onClick(View v)
    {
        if (v.getId() == R.id.message_type_wrapper)
        {
            showHeroTypeDialog();
        }
    }

    @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
    }

    @Override public void onNothingSelected(AdapterView<?> parent)
    {
    }

    class SendMessageDiscussionCallback implements Callback<DiscussionDTO>
    {
        @Override public void failure(RetrofitError error)
        {
            dismissDialog(progressDialog);
            THToast.show("Send message error!");
        }

        @Override public void success(DiscussionDTO response, Response response2)
        {
            dismissDialog(progressDialog);
            THToast.show("Send message Successfully!");
            //TODO close me?
            //closeMe();
        }
    }
}
