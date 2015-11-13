package work.t_s.shim0mura.havings.model.di;

import javax.inject.Singleton;

import dagger.Component;
import work.t_s.shim0mura.havings.model.ApiServiceManager;
import work.t_s.shim0mura.havings.model.User;

/**
 * Created by shim0mura on 2015/11/05.
 */

@Singleton
@Component(modules = ApiModule.class)
public interface ApiComponent {
    void inject(User user);
    void inject(ApiServiceManager asm);
}
