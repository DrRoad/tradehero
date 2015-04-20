package com.tradehero.th.fragments.news;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnItemClick;
import butterknife.Optional;
import com.tradehero.common.annotation.ViewVisibilityValue;
import com.tradehero.common.text.ClickableTagProcessor;
import com.tradehero.common.widget.BetterViewAnimator;
import com.tradehero.th.R;
import com.tradehero.th.api.news.NewsItemCompactDTO;
import com.tradehero.th.api.news.NewsItemDTO;
import com.tradehero.th.api.security.SecurityCompactDTO;
import com.tradehero.th.api.security.SecurityId;
import com.tradehero.th.fragments.discussion.AbstractDiscussionCompactItemViewHolder;
import com.tradehero.th.fragments.security.SimpleSecurityItemViewAdapter;
import com.tradehero.th.models.discussion.NewNewsDiscussionAction;
import com.tradehero.th.models.discussion.OpenWebUserAction;
import com.tradehero.th.models.discussion.SecurityUserAction;
import com.tradehero.th.models.discussion.UserDiscussionAction;
import com.tradehero.th.models.discussion.UserDiscussionActionFactory;
import com.tradehero.th.widget.MarkdownTextView;
import java.util.Collections;
import java.util.List;
import org.ocpsoft.prettytime.PrettyTime;
import rx.Observable;
import rx.functions.Func1;

public class NewsItemViewHolder extends
        NewsItemCompactViewHolder
{
    @InjectView(R.id.news_detail_wrapper) @Optional protected BetterViewAnimator mNewsContentWrapper;
    @InjectView(R.id.news_detail_loading) @Optional protected View loadingTextContent;
    @InjectView(R.id.discussion_content) protected MarkdownTextView textContent;
    @InjectView(R.id.news_detail_title_placeholder) @Optional ImageView mNewsDetailTitlePlaceholder;
    @InjectView(R.id.news_detail_reference) @Optional GridView mNewsDetailReference;
    @InjectView(R.id.news_detail_reference_container) @Optional LinearLayout mNewsDetailReferenceContainer;
    @InjectView(R.id.news_view_on_web) View openOnWebView;

    @NonNull protected SimpleSecurityItemViewAdapter simpleSecurityItemViewAdapter;

    //<editor-fold desc="Constructors">
    public NewsItemViewHolder(@NonNull Context context)
    {
        super();
        simpleSecurityItemViewAdapter = new SimpleSecurityItemViewAdapter(
                context, R.layout.trending_security_item);
    }
    //</editor-fold>

    @Override public void onFinishInflate(@NonNull View view)
    {
        super.onFinishInflate(view);
        if (mNewsDetailReference != null)
        {
            mNewsDetailReference.setAdapter(simpleSecurityItemViewAdapter);
        }
    }

    @Override public void display(@NonNull AbstractDiscussionCompactItemViewHolder.DTO parentViewDto)
    {
        super.display(parentViewDto);
        DTO dto = (DTO) parentViewDto;
        if (mNewsDetailReferenceContainer != null)
        {
            ViewGroup.LayoutParams lp = mNewsDetailReferenceContainer.getLayoutParams();
            //TODO it changes with solution
            lp.width = dto.newsDetailReferenceContainerWidth;
            mNewsDetailReferenceContainer.setLayoutParams(lp);
        }
        if (mNewsDetailReference != null)
        {
            mNewsDetailReference.setVisibility(dto.newsDetailReferenceVisibility);
            mNewsDetailReference.setNumColumns(dto.securityCompactDTOs.size());
        }
        //noinspection unchecked
        simpleSecurityItemViewAdapter.setItems(dto.securityCompactDTOs);

        if (mNewsContentWrapper != null)
        {
            if (loadingTextContent != null && dto.contentWrapperIsLoading)
            {
                mNewsContentWrapper.setDisplayedChildByLayoutId(loadingTextContent.getId());
            }
            else if (textContent != null && !dto.contentWrapperIsLoading)
            {
                mNewsContentWrapper.setDisplayedChildByLayoutId(textContent.getId());
            }
        }
        if (discussionActionButtonsView != null)
        {
            discussionActionButtonsView.setShowMore(false);
        }
        if (openOnWebView != null)
        {
            openOnWebView.setVisibility(dto.openOnWebVisibility);
        }
        if (textContent != null)
        {
            textContent.setText(dto.getText());
        }
    }

    @Override public void setBackgroundResource(int resId)
    {
        if (mNewsDetailTitlePlaceholder != null)
        {
            mNewsDetailTitlePlaceholder.setBackgroundResource(resId);
        }
    }

    @NonNull @Override protected Observable<UserDiscussionAction> getMergedUserActionObservable()
    {
        return super.getMergedUserActionObservable().mergeWith(
                textContent.getUserActionObservable().flatMap(
                        new Func1<ClickableTagProcessor.UserAction, Observable<UserDiscussionAction>>()
                        {
                            @Override public Observable<UserDiscussionAction> call(ClickableTagProcessor.UserAction userAction)
                            {
                                if (viewDTO != null)
                                {
                                    return UserDiscussionActionFactory.createObservable(viewDTO.discussionDTO, userAction);
                                }
                                return Observable.empty();
                            }
                        }
                ));
    }

    @SuppressWarnings("UnusedDeclaration")
    @OnClick(R.id.news_start_new_discussion) @Optional
    protected void handleStartNewDiscussionClicked(View view)
    {
        if (viewDTO != null)
        {
            userActionSubject.onNext(new NewNewsDiscussionAction(viewDTO.discussionDTO));
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    @Optional @OnClick(R.id.news_view_on_web)
    protected void handleOpenOnWebClicked(View view)
    {
        if (viewDTO != null)
        {
            userActionSubject.onNext(new OpenWebUserAction((NewsItemCompactDTO) viewDTO.discussionDTO));
        }
    }

    @SuppressWarnings("unused")
    @Optional @OnItemClick(R.id.news_detail_reference)
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        if (viewDTO != null)
        {
            SecurityId securityId = ((SecurityCompactDTO) parent.getItemAtPosition(position)).getSecurityId();
            userActionSubject.onNext(new SecurityUserAction(viewDTO.discussionDTO, securityId));
        }
    }

    public static class Requisite extends NewsItemCompactViewHolder.Requisite
    {
        @NonNull public final List<SecurityCompactDTO> securityCompactDTOs;

        public Requisite(
                @NonNull Resources resources,
                @NonNull PrettyTime prettyTime,
                @NonNull NewsItemCompactDTO discussionDTO,
                boolean canTranslate,
                boolean isAutoTranslate,
                @NonNull List<SecurityCompactDTO> securityCompactDTOs)
        {
            super(resources, prettyTime, discussionDTO, canTranslate, isAutoTranslate);
            this.securityCompactDTOs = securityCompactDTOs;
        }
    }

    public static class DTO extends NewsItemCompactViewHolder.DTO
    {
        @NonNull public final List<SecurityCompactDTO> securityCompactDTOs;
        @ViewVisibilityValue public final int newsDetailReferenceVisibility;
        public final int newsDetailReferenceContainerWidth;
        public final boolean contentWrapperIsLoading;
        @ViewVisibilityValue public final int openOnWebVisibility;
        @Nullable private String text;

        public DTO(@NonNull Requisite requisite)
        {
            super(requisite);
            this.securityCompactDTOs = Collections.unmodifiableList(requisite.securityCompactDTOs);
            this.newsDetailReferenceVisibility = requisite.securityCompactDTOs.size() > 0 ? View.VISIBLE : View.GONE;
            this.newsDetailReferenceContainerWidth = (int) requisite.resources.getDimension(R.dimen.stock_item_width) * requisite.securityCompactDTOs.size();
            this.contentWrapperIsLoading = !(requisite.discussionDTO instanceof NewsItemDTO) || ((NewsItemDTO) requisite.discussionDTO).text == null;
            this.openOnWebVisibility = ((NewsItemCompactDTO) requisite.discussionDTO).url == null ? View.GONE : View.VISIBLE;
            this.text = createText();
        }

        @NonNull @Override protected String createTimeToDisplay(@NonNull PrettyTime prettyTime)
        {
            if (discussionDTO.createdAtUtc != null)
            {
                return prettyTime.format(discussionDTO.createdAtUtc);
            }
            return "";
        }

        @Override public void setCurrentTranslationStatus(@NonNull TranslationStatus currentTranslationStatus)
        {
            super.setCurrentTranslationStatus(currentTranslationStatus);
            this.text = createText();
        }

        @Nullable protected String createText()
        {
            switch (getCurrentTranslationStatus())
            {
                case ORIGINAL:
                case TRANSLATING:
                case FAILED:
                    if (discussionDTO instanceof NewsItemDTO)
                    {
                        return ((NewsItemDTO) discussionDTO).text;
                    }
                    return null;

                case TRANSLATED:
                    if (translatedDiscussionDTO instanceof NewsItemDTO)
                    {
                        return ((NewsItemDTO) translatedDiscussionDTO).text;
                    }
                    return null;
            }
            throw new IllegalStateException("Unhandled state TranslationStatus." + getCurrentTranslationStatus());
        }

        @Nullable public String getText()
        {
            return text;
        }
    }
}
