package work.t_s.shim0mura.havings.model;

import android.app.Activity;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;
import work.t_s.shim0mura.havings.model.entity.TagEntity;
import work.t_s.shim0mura.havings.model.entity.TagMigrationEntity;
import work.t_s.shim0mura.havings.model.realm.Tag;
import work.t_s.shim0mura.havings.model.realm.TagMigrationVersion;

/**
 * Created by shim0mura on 2015/12/29.
 */
public class DefaultTag {

    private static DefaultTag tag;
    private Activity activity;
    static ApiService service;

    private DefaultTag(Activity act){
        activity = act;
        service = ApiServiceManager.getService(activity);
    }

    public static synchronized DefaultTag getSingleton(Activity act){
        if(tag == null){
            tag = new DefaultTag(act);
        }

        return tag;
    }

    public void getDefaultTag(){
        Call<List<TagMigrationEntity>> call = service.getDefaultTag();

        call.enqueue(getTagMigrateCallback("DefaultTag"));
    }

    public void migrateTag(){
        Call<List<TagMigrationEntity>> call = service.getTagMigration(getCurrentMigrationVersionOfLocal());

        call.enqueue(getTagMigrateCallback("MigrateTag"));
    }

    public void checkMigrationVersion(){
        Call<TagMigrationEntity> call = service.getTagMigrationVersion();

        call.enqueue(new Callback<TagMigrationEntity>() {
            @Override
            public void onResponse(Response<TagMigrationEntity> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    TagMigrationEntity migration = response.body();

                    Timber.d("get version from api %s", migration.migrationVersion);
                    Timber.d("get version from local %s", getCurrentMigrationVersionOfLocal());

                    if(getCurrentMigrationVersionOfLocal() == 0){
                        Timber.d("tag missing");
                        getDefaultTag();
                    }else if(getCurrentMigrationVersionOfLocal() != migration.migrationVersion){
                        Timber.d("tag migration version differences");
                        migrateTag();
                    }else{
                        Timber.d("same migration version");
                    }

                } else if (response.code() == StatusCode.Unauthorized) {
                    Log.d("failed to authorize", "401 failed to authorize");
                } else {

                }
            }

            @Override
            public void onFailure(Throwable t) {
                Timber.d("failed to tag migration");
                t.printStackTrace();
            }
        });
    }

    public int getCurrentMigrationVersionOfLocal(){
        Realm realm = Realm.getInstance(activity);
        RealmResults<TagMigrationVersion> result = realm.where(TagMigrationVersion.class).findAll();
        if(result.size() == 0){
            return 0;
        }else{
            return result.get(0).getCurrentVersion();
        }
    }

    private Callback<List<TagMigrationEntity>> getTagMigrateCallback(final String loggingTag){
        return new Callback<List<TagMigrationEntity>>() {
            @Override
            public void onResponse(Response<List<TagMigrationEntity>> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    List<TagMigrationEntity> list = response.body();

                    for(TagMigrationEntity tagMigration: list){
                        Timber.d(loggingTag + " aaaa");
                        setTag(tagMigration);
                    }

                } else if (response.code() == StatusCode.Unauthorized) {
                    Log.d("failed to authorize", "401 failed to authorize");
                } else {

                }
            }

            @Override
            public void onFailure(Throwable t) {
                Timber.d("failed to " + loggingTag);
                t.printStackTrace();
            }
        };
    }

    private void setTag(TagMigrationEntity tagMigrationEntity){
        Realm realm = Realm.getInstance(activity);
        List<Tag> realmTags = new ArrayList<Tag>();

        for(TagEntity tag: tagMigrationEntity.updatedTags){
            Tag tagRealm = new Tag();
            tagRealm.setId(tag.id);
            tagRealm.setName(tag.name);
            tagRealm.setParentId(tag.parentId);
            tagRealm.setNest(tag.nest);
            tagRealm.setPriority(tag.priority);
            tagRealm.setTagType(tag.tagType);
            tagRealm.setIsDeleted(tag.isDeleted);
            realmTags.add(tagRealm);
        }

        realm.beginTransaction();
        realm.copyToRealmOrUpdate(realmTags);

        // set migration version
        RealmResults<TagMigrationVersion> result = realm.where(TagMigrationVersion.class).findAll();
        TagMigrationVersion migration;
        if(result.size() == 0){
            migration = new TagMigrationVersion();
        }else{
            migration = result.get(0);
        }
        migration.setCurrentVersion(tagMigrationEntity.migrationVersion);
        realm.copyToRealmOrUpdate(migration);

        realm.commitTransaction();
    }

}
