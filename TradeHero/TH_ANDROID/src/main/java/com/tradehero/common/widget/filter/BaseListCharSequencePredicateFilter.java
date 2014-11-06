package com.tradehero.common.widget.filter;

import java.util.ArrayList;
import java.util.List;

public class BaseListCharSequencePredicateFilter<T> implements ListCharSequencePredicateFilter<T>
{
    protected CharSequencePredicate<? super T> predicate;

    //<editor-fold desc="Constructors">
    public BaseListCharSequencePredicateFilter(CharSequencePredicate<? super T> predicate)
    {
        super();
        this.predicate = predicate;
    }
    //</editor-fold>

    @Override public void setDefaultPredicate(CharSequencePredicate<? super T> predicate)
    {
        this.predicate = predicate;
    }

    @Override public void setCharSequence(CharSequence charSequence)
    {
        this.predicate.setCharSequence(charSequence);
    }

    @Override public List<T> filter(List<? extends T> unfiltered)
    {
        return filter(unfiltered, predicate);
    }

    @Override public List<T> filter(List<? extends T> unfiltered, CharSequencePredicate<? super T> predicate)
    {
        if (unfiltered == null)
        {
            return null;
        }

        if (predicate == null || predicate.getCharSequence() == null || predicate.getCharSequence().length() == 0)
        {
            return new ArrayList<>(unfiltered);
        }

        ArrayList<T> filtered = new ArrayList<>();

        for (T subject: unfiltered)
        {
            if (predicate.apply(subject))
            {
                filtered.add(subject);
            }
        }

        return filtered;
    }
}
