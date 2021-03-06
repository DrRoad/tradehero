package com.tradehero.th.utils.route;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import com.tradehero.route.Routable;
import com.tradehero.route.Router;
import com.tradehero.route.RouterOptions;
import com.tradehero.route.RouterParams;
import com.tradehero.th.fragments.DashboardNavigator;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.inject.Provider;
import javax.inject.Singleton;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import timber.log.Timber;

@Singleton
public class THRouter extends Router
{
    @NonNull private final Provider<DashboardNavigator> navigatorProvider;
    private Map<String, String> aliases;

    //<editor-fold desc="Constructors">
    public THRouter(
            @NonNull Context context,
            @NonNull Provider<DashboardNavigator> navigatorProvider)
    {
        super(context);
        this.navigatorProvider = navigatorProvider;
        aliases = new LinkedHashMap<>();
    }
    //</editor-fold>

    @Override public void open(String url, Bundle extras, Context context)
    {
        if (context == null)
        {
            throw new RuntimeException("You need to supply a context for Router " + this.toString());
        }
        if (aliases.containsKey(url))
        {
            url = aliases.get(url);
        }

        RouterParams params = this.paramsForUrl(url);
        if (params.routerOptions instanceof THRouterOptions)
        {
            openFragment(params, extras);
        }
        else
        {
            super.open(url, extras, context);
        }
    }

    @Override public Router registerRoutes(Class<?>... targets)
    {
        super.registerRoutes(targets);
        for (Class<?> target : targets)
        {
            if (Fragment.class.isAssignableFrom(target))
            {
                //noinspection unchecked
                Class<? extends Fragment> fragmentTarget = (Class<? extends Fragment>) target;
                Class<? extends Runnable>[] runnableClasses = null;

                if (target.isAnnotationPresent(PreRoutable.class))
                {
                    PreRoutable preRoutable = target.getAnnotation(PreRoutable.class);
                    runnableClasses = preRoutable.preOpenRunnables();
                }

                if (target.isAnnotationPresent(Routable.class))
                {
                    Routable routable = target.getAnnotation(Routable.class);

                    String[] routes = routable.value();
                    if (routes != null)
                    {
                        for (String route : routes)
                        {
                            mapFragment(
                                    route,
                                    new THRouterOptions(runnableClasses, fragmentTarget));
                        }
                    }
                }
            }
        }
        return this;
    }

    public void registerAlias(String alias, String url)
    {
        this.aliases.put(alias, url);
    }

    public void mapFragment(String format, @NonNull THRouterOptions options)
    {
        this.routes.put(format, options);
    }

    private void openFragment(RouterParams params, Bundle extras)
    {
        if (params != null)
        {
            THRouterOptions options = (THRouterOptions) params.routerOptions;

            executePreOpenFragment(options.getPreOpenRunnableClasses());

            Bundle args = new Bundle();
            if (extras != null)
            {
                args.putAll(extras);
            }
            if (params.openParams != null)
            {
                for (Map.Entry<String, String> param : params.openParams.entrySet())
                {
                    args.putString(param.getKey(), param.getValue());
                }
            }

            Fragment currentFragment = navigatorProvider.get().getCurrentFragment();

            /** If the opening fragment is active, and not yet detached, resume it with routing bundle **/
            if (currentFragment != null && !currentFragment.isDetached() &&
                    ((Object) currentFragment).getClass().equals(options.getOpenFragmentClass()))
            {
                currentFragment.getArguments().putAll(args);
                currentFragment.onResume();
            }
            else
            {
                navigatorProvider.get().pushFragment(options.getOpenFragmentClass(), args);
            }
        }
    }

    protected void executePreOpenFragment(@Nullable Class<? extends Runnable>[] preOpenRunnables)
    {
        if (preOpenRunnables != null)
        {
            for (Class<? extends Runnable> runnableClass : preOpenRunnables)
            {
                try
                {
                    runnableClass.newInstance().run();
                }
                catch (InstantiationException|IllegalAccessException e)
                {
                    Timber.e(e, "Failed to instantiate %s", runnableClass.getCanonicalName());
                }
            }
        }
    }

    public void inject(Fragment fragment)
    {
        if (fragment.getArguments() != null)
        {
            inject(fragment, fragment.getArguments());
        }
    }

    public static class THRouterOptions extends RouterOptions
    {
        private Class<? extends Runnable>[] preOpenRunnableClasses;
        private Class<? extends Fragment> fragmentClass;

        //<editor-fold desc="Constructors">
        public THRouterOptions(Class<? extends Fragment> fragmentClass)
        {
            this.fragmentClass = fragmentClass;
        }

        public THRouterOptions(Class<? extends Runnable>[] preOpenRunnableClasses, Class<? extends Fragment> fragmentClass)
        {
            this.preOpenRunnableClasses = preOpenRunnableClasses;
            this.fragmentClass = fragmentClass;
        }
        //</editor-fold>

        public void setOpenFragmentClass(Class<? extends Fragment> fragmentClass)
        {
            this.fragmentClass = fragmentClass;
        }

        public Class<? extends Fragment> getOpenFragmentClass()
        {
            return fragmentClass;
        }

        public Class<? extends Runnable>[] getPreOpenRunnableClasses()
        {
            return preOpenRunnableClasses;
        }

        public void setPreOpenRunnableClasses(Class<? extends Runnable>[] preOpenRunnableClasses)
        {
            this.preOpenRunnableClasses = preOpenRunnableClasses;
        }
    }
}
