- Khi khai báo các layout cha, nhớ dùng thuộc tính sau để đảm bảo phần camera trước với phần đáy maàn hình không đè nội dung:

```text
    android:clipChildren="false"
    android:clipToPadding="false"
    android:fitsSystemWindows="true"
```

- Khi khai báo layout trên cùng thì thêm thuộc tính sau:

```text
android:fitsSystemWindows="true"
```

