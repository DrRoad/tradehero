package com.tradehero.th.fragments.onboarding.stock;

import android.support.annotation.Nullable;
import android.view.View;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.tradehero.th.R;
import com.tradehero.th.api.DTOView;
import com.tradehero.th.fragments.security.SecurityItemView;

@Deprecated
public class SecuritySelectableViewHolder implements DTOView<SelectableSecurityDTO>
{
    @InjectView(R.id.security_view) protected SecurityItemView securityView;
    @InjectView(android.R.id.icon2) protected View tickSelectedView;

    @Nullable protected SelectableSecurityDTO selectableDTO;

    //<editor-fold desc="Constructors">

    public SecuritySelectableViewHolder()
    {
    }
    //</editor-fold>

    void attachView(View view)
    {
        ButterKnife.inject(this, view);
    }

    void detachView()
    {
        ButterKnife.reset(this);
    }

    public void display(SelectableSecurityDTO selectableSecurityDTO)
    {
        this.selectableDTO = selectableSecurityDTO;
        display();
    }

    public void display()
    {
        displaySecurity();
        displayTickSelected();
    }

    void displaySecurity()
    {
        if (securityView != null)
        {
            securityView.display(selectableDTO == null ? null : selectableDTO.value);
        }
    }

    void displayTickSelected()
    {
        if (tickSelectedView != null)
        {
            tickSelectedView.setVisibility(selectableDTO != null
                    && selectableDTO.selected ? View.VISIBLE : View.GONE);
        }
    }
}
