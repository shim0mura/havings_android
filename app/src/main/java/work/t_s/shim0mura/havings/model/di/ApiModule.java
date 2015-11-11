package work.t_s.shim0mura.havings.model.di;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by shim0mura on 2015/11/05.
 */

@Module
public class ApiModule {

    private final Context context;

    public ApiModule(Context context) {
        this.context = context;
    }

    @Provides @Singleton
    public Api provideApi(){
        return new WebApiImpl(context);
    }
}
