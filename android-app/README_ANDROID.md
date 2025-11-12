# Android frontend (Kotlin + Jetpack Compose)

Projekt przykładowy znajduje się w katalogu `android-app` — to prosty szkielet do szybkiego uruchomienia w Android Studio.

Co jest w środku:
- `app/` — moduł aplikacji z Compose, Retrofit i prostym ViewModelem.

Jak uruchomić (szybkie kroki, najlepiej korzystając z Android Studio):

1. Uruchom backend (Spring Boot) w IntelliJ IDEA (albo w terminalu):

```powershell
# w katalogu projektu (gdzie jest mvnw)
.\mvnw.cmd spring-boot:run
```

2. Otwórz Android Studio i wybierz `Open` -> wskaż folder `android-app` (nie cały repozytorium) — Android Studio zaimportuje projekt Gradle.

3. Wybierz/emulator albo fizyczne urządzenie i uruchom aplikację.

Uwaga do połączenia z lokalnym backendem:
- Jeśli używasz standardowego Android Emulatora: base URL w projekcie ustawiony jest na `http://10.0.2.2:8080/` (to przekierowanie emulator -> host).
- Jeśli testujesz na fizycznym urządzeniu: użyj adresu IP twojego komputera (np. `http://192.168.1.100:8080/`) i upewnij się że firewall pozwala połączenia.
- Android Manifest w tym szkicu ma `android:usesCleartextTraffic="true"`, więc HTTP lokalne działa dla debug.

Co możesz zrobić dalej:
- Dodać persistence (Room) dla lokalnej listy zakupów.
- Dodać auth (użyć `/api/users/login` i tokenowanie).
- Dostosować UI i testy.
