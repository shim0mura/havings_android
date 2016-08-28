package work.t_s.shim0mura.havings.model;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.realm.DynamicRealm;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;
import io.realm.RealmResults;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;
import work.t_s.shim0mura.havings.model.realm.TagEntity;
import work.t_s.shim0mura.havings.model.realm.TagMigrationEntity;

/**
 * Created by shim0mura on 2015/12/29.
 */
public class DefaultTag {

    private static DefaultTag tag;
    private List<work.t_s.shim0mura.havings.model.entity.TagEntity> tagEntities = new ArrayList<work.t_s.shim0mura.havings.model.entity.TagEntity>();
    private Activity activity;
    static ApiService service;

    private static RealmConfiguration defaultConfig;

    public static final int TAG_TYPE_PLACE = 0;
    public static final int TAG_TYPE_CATEGORY = 3;
    public static final int TAG_TYPE_ITEM = 2;
    public static final int TAG_TYPE_CLOSET = 1;

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
        Call<List<work.t_s.shim0mura.havings.model.entity.TagMigrationEntity>> call = service.getDefaultTag();

        call.enqueue(getTagMigrateCallback("DefaultTag"));
    }

    public void migrateTag(){
        Call<List<work.t_s.shim0mura.havings.model.entity.TagMigrationEntity>> call = service.getTagMigration(getCurrentMigrationVersionOfLocal());

        call.enqueue(getTagMigrateCallback("MigrateTag"));
    }

    public void checkMigrationVersion(){
        Timber.d("tag_mig: start");

        Call<work.t_s.shim0mura.havings.model.entity.TagMigrationEntity> call = service.getTagMigrationVersion();

        call.enqueue(new Callback<work.t_s.shim0mura.havings.model.entity.TagMigrationEntity>() {
            @Override
            public void onResponse(Response<work.t_s.shim0mura.havings.model.entity.TagMigrationEntity> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    work.t_s.shim0mura.havings.model.entity.TagMigrationEntity migration = response.body();

                    Timber.d("tag_mig: get version from api %s", migration.migrationVersion);
                    Timber.d("tag_mig: get version from local %s", getCurrentMigrationVersionOfLocal());

                    if(getCurrentMigrationVersionOfLocal() == 0){
                        Timber.d("tag_mig: tag missing");
                        getDefaultTag();
                    }else if(getCurrentMigrationVersionOfLocal() != migration.migrationVersion){
                        Timber.d("tag_mig: tag migration version differences");
                        migrateTag();
                    }else{
                        Timber.d("tag_mig: same migration version");
                    }

                } else if (response.code() == StatusCode.Unauthorized) {
                    Log.d("failed to authorize", "401 failed to authorize");
                } else {
                    Timber.d("tag_mig: error");

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
        Realm realm = Realm.getInstance(getRealmConfig(activity));
        RealmResults<TagMigrationEntity> result = realm.where(TagMigrationEntity.class).findAll();
        if(result.size() == 0){
            return 0;
        }else{
            return result.get(0).getMigrationVersion();
        }
    }

    public List<work.t_s.shim0mura.havings.model.entity.TagEntity> getTagEntities(){
        if(tagEntities.isEmpty()) {
            Realm realm = Realm.getInstance(getRealmConfig(activity));
            RealmResults<TagEntity> result = realm.where(TagEntity.class).equalTo("isDeleted", false).findAll();

            List<work.t_s.shim0mura.havings.model.entity.TagEntity> tags = new ArrayList<work.t_s.shim0mura.havings.model.entity.TagEntity>();

            for (TagEntity t : result) {
                work.t_s.shim0mura.havings.model.entity.TagEntity tagEntity = new work.t_s.shim0mura.havings.model.entity.TagEntity();

                tagEntity.id = t.getId();
                tagEntity.name = t.getName();
                tagEntity.yomiJp = t.getYomiJp();
                tagEntity.yomiRoma = t.getYomiRoma();
                tagEntity.priority = t.getPriority();
                tagEntity.tagType = t.getTagType();
                tags.add(tagEntity);
            }

            tagEntities = tags;
        }

        return tagEntities;
    }

    private Callback<List<work.t_s.shim0mura.havings.model.entity.TagMigrationEntity>> getTagMigrateCallback(final String loggingTag){
        return new Callback<List<work.t_s.shim0mura.havings.model.entity.TagMigrationEntity>>() {
            @Override
            public void onResponse(Response<List<work.t_s.shim0mura.havings.model.entity.TagMigrationEntity>> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    List<work.t_s.shim0mura.havings.model.entity.TagMigrationEntity> list = response.body();

                    Timber.d("tag_mig: success");
                    for(work.t_s.shim0mura.havings.model.entity.TagMigrationEntity tagMigration: list){
                        setTag(tagMigration);
                    }

                } else if (response.code() == StatusCode.Unauthorized) {
                    Timber.d("tag_mig: failed auth");
                } else {
                    Timber.d("tag_mig: failed load");

                }
            }

            @Override
            public void onFailure(Throwable t) {
                Timber.d("tag_mig, failed to " + loggingTag);
                t.printStackTrace();
            }
        };
    }

    private void setTag(work.t_s.shim0mura.havings.model.entity.TagMigrationEntity tagMigrationEntity){
        Realm realm = Realm.getInstance(getRealmConfig(activity));
        List<TagEntity> realmTags = new ArrayList<TagEntity>();

        for(work.t_s.shim0mura.havings.model.entity.TagEntity tag: tagMigrationEntity.updatedTags){
            TagEntity tagRealm = new TagEntity();
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
        RealmResults<TagMigrationEntity> result = realm.where(TagMigrationEntity.class).findAll();
        TagMigrationEntity migration;
        if(result.size() == 0){
            migration = new TagMigrationEntity();
        }else{
            migration = result.get(0);
        }
        migration.setMigrationVersion(tagMigrationEntity.migrationVersion);
        realm.copyToRealmOrUpdate(migration);

        realm.commitTransaction();
    }

    public static RealmConfiguration getRealmConfig(Context context){

        if (defaultConfig == null) {
            /*
            defaultConfig = new RealmConfiguration.Builder(context)
                    .assetFile(context, "tag.realm")
                    .schemaVersion(1)
                    .migration(new RealmMigration() {
                        @Override
                        public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {

                        }
                    })
                    .build();
                    */
            defaultConfig = new RealmConfiguration.Builder(context)
                    .assetFile(context, "tag.realm")
                    .schemaVersion(1)
                    .migration(new RealmMigration() {
                        @Override
                        public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {

                        }
                    })
                    .build();
            Realm realm = Realm.getInstance(defaultConfig);

            RealmResults<TagEntity> result = realm.where(TagEntity.class).equalTo("id", 18).findAll();
            Timber.d("test_tag %s", result.first().getName());
        }

        return defaultConfig;
    }

}
