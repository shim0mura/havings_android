package work.t_s.shim0mura.havings.util;

import com.squareup.okhttp.ResponseBody;

import java.io.IOException;
import java.lang.annotation.Annotation;

import retrofit.Converter;
import retrofit.Response;
import retrofit.Retrofit;
import work.t_s.shim0mura.havings.model.entity.ModelErrorEntity;

/**
 * Created by shim0mura on 2016/01/04.
 */
public class ApiErrorUtil {
    public static ModelErrorEntity parseError(Response<?> response, Retrofit retrofit) {
        Converter<ResponseBody, ModelErrorEntity> converter = retrofit.responseConverter(ModelErrorEntity.class, new Annotation[0]);

        ModelErrorEntity error;

        try {
            error = converter.convert(response.errorBody());
        } catch (IOException e) {
            return new ModelErrorEntity();
        }

        return error;
    }
}
