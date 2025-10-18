# Hướng dẫn gọi server trong android

## 1. Giới thiệu các class cần thiết

- ApiCallback: xử lý bất đồng bộ dữ liệu trả về từ response
- \*\*Api: interface dùng làm lớp biên cung cấp các api endpoint
- \*\*ApiHandler: xử lý việc gọi api để tương tác với dữ liệu

- BaseJsonResponse: Nhận dữ liệu json trả về bởi api

## 2. Cách xử lý với các class trên

1. Định nghĩa URL trỏ tới endpoint chung nhất
   **Ví dụ**: /v1/api/papers có các endpoint con là /details, /citation, /\*\* thì định nghĩa 1 base url là http://10.0.2.2:<'port'>/v1/api/papers/ (được định nghĩa trong gateway rồi)

Lưu ý: cuối chuỗi phải có dấu /, không là chết toi

2. Khởi tạo các thuộc tính retrofit để gọi được api

Những thứ cần khởi tạo:

- Gson: dùng để duyệt dữ liệu, phục vụ binding vào các trường trong đối tượng

```java
var gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
                .create();
```

- Logging, OkHttpClient: Để kiểm tra xem kết nối có ok không. Cái này tùy, cẩn thận thì làm cx dc

```java
 var logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
 var client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();
```

- Retrofit: Kết nối ứng dụng android với server

```java
var retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build();
```

Khởi tạo xong thì dùng hàm create() để liên kết với endpoint đích định nghĩa trong interface \*\*Api.class

3. Xử lý dữ liệu trong miền nghiệp vụ cụ thể

**Lưu ý**:

- Trong tất cả các phương thức nghiệp vụ đều phải định nghĩa args đầu vào có mặt ApiCallback. Đơn giản là vì các hàm Call mà Retrofit trả về đều là hàm bất đồng bộ, nên cần có nó để lấy dữ liệu trong các lớp xử lý UI.

- Trong các hàm Call, không được phép truyền obj trực tiếp, mà phải gói vào trong BaseJsonResponse vì dữ liệu trả về có các trường không tương ứng với obj đó

**Cấu trúc cơ bản của phương thức nghiệp vụ như sau**:

Repo tương tác với endpoint từ server:

```java
public void businessFunction(String args, ApiCallback<Object> callback) {
        Call<BaseJsonResponse<Object>> call = apiService.endpoint(args);
        call.enqueue(new Callback<>() {
            // Hàm xử lý response thành công
            @Override
            public void onResponse(@NonNull Call<BaseJsonResponse<Object>> call, @NonNull Response<BaseJsonResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    var result = response.body().getData();
                    callback.onSuccess(result);
                } else {
                    callback.onError(response.message());
                }
            }

            // Hàm xử lý lỗi trong quá trình phản hồi
            @Override
            public void onFailure(@NonNull Call<BaseJsonResponse<Object>> call, @NonNull Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }
```

Tương tác với repo trong UI:

```java
private void bindingData(View view, String args) {
        apiHandler.businessFunction(args, new ApiCallback<>() {
            @Override
            public void onSuccess(List<Citation> data) {
                // Bắt buộc phải thực hiện trên UI Thread
                requireActivity().runOnUiThread(() -> {
                    // Xử lý các thứ với TextView, Button,...
                });
            }

            @Override
            public void onError(String error) {
                // Bắt buộc phải thực hiện trên UI Thread
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
```
