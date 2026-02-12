package guru.qa.niffler.api.clients;

import guru.qa.niffler.api.model.SpendJson;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface SpendApi {
    @POST("/addSpend")
    Call<SpendJson> addSpend(@Body SpendJson spend);
}
