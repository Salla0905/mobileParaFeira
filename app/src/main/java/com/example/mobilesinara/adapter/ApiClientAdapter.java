    package com.example.mobilesinara.adapter;

    import com.google.gson.Gson;
    import com.google.gson.GsonBuilder;

    import okhttp3.Credentials;
    import okhttp3.Interceptor;
    import okhttp3.OkHttpClient;
    import okhttp3.Request;
    import okhttp3.logging.HttpLoggingInterceptor;
    import retrofit2.Retrofit;
    import retrofit2.converter.gson.GsonConverterFactory;
    import retrofit2.converter.scalars.ScalarsConverterFactory;

    public class ApiClientAdapter {

        private static Retrofit retrofit;
        private static final String BASE_URL = "https://ms-sinara-sql-oox0.onrender.com/";

        public static Retrofit getRetrofitInstance() {
            if (retrofit == null) {

                Gson gson = new GsonBuilder()
                        .setLenient()
                        .create();

                HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                logging.setLevel(HttpLoggingInterceptor.Level.BODY);

                Interceptor authInterceptor = chain -> {
                    Request original = chain.request();
                    String credentials = Credentials.basic("Lorena", "lorena@2025");

                    Request request = original.newBuilder()
                            .header("Authorization", credentials)
                            .method(original.method(), original.body())
                            .build();

                    return chain.proceed(request);
                };

                OkHttpClient client = new OkHttpClient.Builder()
                        .addInterceptor(authInterceptor)
                        .addInterceptor(logging)
                        .build();

                retrofit = new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .client(client)
                        .addConverterFactory(ScalarsConverterFactory.create())
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .build();
            }

            return retrofit;
        }
    }
