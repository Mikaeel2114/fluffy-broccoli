# Minecraft Server Runner

یک برنامه‌ی ساده‌ی Java برای Windows که فایل `start.bat` سرور Minecraft را انتخاب می‌کند، در همان پوشه اجرا می‌کند و خروجی را داخل برنامه نشان می‌دهد.

## اجرا

1. از بخش **Actions** در GitHub، workflow را اجرا کن یا روی `main` push کن.
2. از بخش Artifacts فایل `minecraft-server-runner` را دانلود و extract کن.
3. روی فایل JAR دوبار کلیک کن، بعد `start.bat` را انتخاب کن.

Java 17 یا بالاتر لازم است. مسیر انتخاب‌شده ذخیره می‌شود و دفعه‌ی بعد لازم نیست دوباره انتخابش کنی.

## Build محلی

```bash
mvn package
java -jar target/minecraft-server-runner-1.0.0.jar
```

نکته: این برنامه فقط روی Windows اجرا می‌شود، چون برای اجرای `.bat` از `cmd.exe` استفاده می‌کند.
