package com.tradehero.th.fragments.settings;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Pair;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.Bind;
import com.tradehero.th.R;
import com.tradehero.th.models.push.urbanairship.UrbanAirshipPushNotificationManager;
import com.urbanairship.UAirship;
import com.urbanairship.actions.ActionArguments;
import com.urbanairship.actions.ActionValue;
import com.urbanairship.actions.ActionValueException;
import rx.Observable;
import rx.subjects.BehaviorSubject;
import timber.log.Timber;

public class ManualPushActionView extends ScrollView
{
    @Bind(R.id.channel_id) TextView channelIdView;
    @Bind(R.id.situation_spinner) Spinner situationSpinner;
    @Bind(R.id.arguments) EditText argumentView;
    @Bind(R.id.action_name) EditText actionNameView;

    ArrayAdapter<SituationDTO> situationAdapter;
    private BehaviorSubject<Pair<String, ActionArguments>> actionArgumentObservable;

    //<editor-fold desc="Constructors">
    public ManualPushActionView(Context context)
    {
        super(context);
        actionArgumentObservable = BehaviorSubject.create();
    }

    public ManualPushActionView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        actionArgumentObservable = BehaviorSubject.create();
    }

    public ManualPushActionView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        actionArgumentObservable = BehaviorSubject.create();
    }
    //</editor-fold>

    @Override protected void onFinishInflate()
    {
        super.onFinishInflate();
        ButterKnife.bind(this);
        situationAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, SituationDTO.getAll());
        situationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        situationSpinner.setAdapter(situationAdapter);

        UAirship uAirship = UrbanAirshipPushNotificationManager.getUAirship();
        if (uAirship == null)
        {
            channelIdView.setText("uAirship is null");
        }
        else
        {
            channelIdView.setText("Channel Id: " + uAirship.getPushManager().getChannelId());
        }
    }

    @Override protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        ButterKnife.bind(this);
    }

    @Override protected void onDetachedFromWindow()
    {
        try
        {
            actionArgumentObservable.onNext(Pair.create(
                    actionNameView.getText().toString(),
                    new ActionArguments(
                            ((SituationDTO) situationSpinner.getSelectedItem()).situation,
                            ActionValue.wrap(argumentView.getText().toString()),
                            new Bundle())));
        } catch (ActionValueException e)
        {
            Timber.e(e, "Failed to pass on action value");
            actionArgumentObservable.onError(e);
        }
        ButterKnife.unbind(this);
        super.onDetachedFromWindow();
    }

    @NonNull public Observable<Pair<String, ActionArguments>> getActionArgumentObservable()
    {
        return actionArgumentObservable.asObservable();
    }
}
