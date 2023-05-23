import io.restassured.RestAssured;
import io.restassured.http.Cookies;
import io.restassured.http.Headers;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HelloWorldTest {

    @Test
    @DisplayName("Ex4: GET-запрос")
    public void testEx4() {
        Response response = RestAssured
                .get("https://playground.learnqa.ru/api/get_text")
                .andReturn();
        response.prettyPrint();
    }


    @Test
    @DisplayName("Ex5: Парсинг JSON")
    public void testEx5() {
        JsonPath response =
                RestAssured
                        .get("https://playground.learnqa.ru/api/get_json_homework")
                        .jsonPath();

        // response.prettyPrint();

        String secondMsgFromResponse =
                response.get("messages[1].message");
        System.out.println(secondMsgFromResponse);
    }

    @Test
    @DisplayName("Ex6: Редирект")
    public void testEx6() {
        Response response =
                RestAssured
                        .given()
                        .redirects()
                        .follow(false)
                        .when()
                        .get("https://playground.learnqa.ru/api/long_redirect")
                        .andReturn();

        String locationHeader = response.getHeader("Location");
        System.out.println(locationHeader);
    }


    @Test
    @DisplayName("Ex7: Долгий редирект")
    public void testEx7() {

        Response response =
                makeRequest("https://playground.learnqa.ru/api/long_redirect");

        int statusCode = response.getStatusCode();
        int cntRedirects = 0;
        while (statusCode != 200) {
            cntRedirects++;
            System.out.println("cntRedirects = " + cntRedirects);

            response = makeRequest(response.getHeader("Location"));
            statusCode = response.getStatusCode();
        }
    }

    private Response makeRequest(String url) {
        return RestAssured
                .given()
                .redirects()
                .follow(false)
                .when()
                .get(url)
                .andReturn();
    }


    @Test
    @DisplayName("Ex8: Токены")
    public void testEx8() {

        String statusUnready = "Job is NOT ready";
        String statusReady = "Job is ready";

        JsonPath responseFirst =
                makeRequestForLongTimeJob();

        String token = responseFirst.get("token").toString();
        long seconds = ((Number) responseFirst.get("seconds")).longValue();

        JsonPath responseSecond =
                makeRequestForLongTimeJob(token);

        assertEquals(statusUnready, responseSecond.get("status"));

        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        JsonPath responseThird =
                makeRequestForLongTimeJob(token);

        assertEquals(statusReady, responseThird.get("status"));
        assertNotNull(responseThird.get("result"));
    }

    private JsonPath makeRequestForLongTimeJob() {
        return RestAssured
                .get("https://playground.learnqa.ru/ajax/api/longtime_job")
                .jsonPath();
    }

    private JsonPath makeRequestForLongTimeJob(String token) {
        return RestAssured
                .given()
                .queryParam("token", token)
                .get("https://playground.learnqa.ru/ajax/api/longtime_job")
                .jsonPath();
    }


    @Test
    @DisplayName("Ex9: Подбор пароля")
    public void testEx9() {

        String rightMsg = "You are authorized";

        List<String> passwordsList = asList(
                "password",
                "123456",
                "123456789",
                "12345678",
                "12345",
                "qwerty",
                "abc123",
                "football",
                "1234567",
                "monkey",
                "111111",
                "letmein",
                "1234",
                "1234567890",
                "dragon",
                "baseball",
                "sunshine",
                "iloveyou",
                "trustno1",
                "princess",
                "adobe123",
                "123123",
                "welcome",
                "login",
                "admin",
                "qwerty123",
                "solo",
                "1q2w3e4r",
                "master",
                "666666",
                "photoshop",
                "1qaz2wsx",
                "qwertyuiop",
                "ashley",
                "mustang",
                "121212",
                "starwars",
                "654321",
                "bailey",
                "access",
                "flower",
                "555555",
                "passw0rd",
                "shadow",
                "lovely",
                "7777777",
                "michael",
                "!@#$%^&*",
                "jesus",
                "password1",
                "superman",
                "hello",
                "charlie",
                "888888",
                "696969",
                "hottie",
                "freedom",
                "aa123456",
                "qazwsx",
                "ninja",
                "azerty",
                "loveme",
                "whatever",
                "donald",
                "batman",
                "zaq1zaq1",
                "Football",
                "000000",
                "123qwe"
        );

        Map<String, String> params = new HashMap<>();
        params.put("login", "super_admin");

        for (String path : passwordsList) {

            params.put("password", path);

            Response response =
                    RestAssured
                            .given()
                            .body(params)
                            .post("https://playground.learnqa.ru/ajax/api/get_secret_password_homework")
                            .andReturn();

            Cookies cookies =
                    response.getDetailedCookies();

            String textFromResponse =
                    RestAssured
                            .given()
                            .cookies(cookies)
                            .get("https://playground.learnqa.ru/ajax/api/check_auth_cookie")
                            .andReturn()
                            .asString();

            if (rightMsg.equals(textFromResponse)) {
                System.out.println(textFromResponse);
                System.out.println("Correct password is: " + path);
                return;
            }
        }

    }

    @ParameterizedTest
    @ValueSource(strings = {"This String is longest String in the World", "This is not"})
    @DisplayName("Ex10: Тест на короткую фразу")
    public void testEx10(String s) {
        assertTrue(s.length() > 15,
                "String \"" + s + "\" is shorter than 15 characters, but it must be longer");
    }

    @ParameterizedTest
    @ValueSource(strings = {"cookie", "header"})
    @DisplayName("Ex11: Тест запроса на метод cookie. " + "Ex12: Тест запроса на метод header")
    public void testEx12(String uri) {
        Response response = RestAssured
                .get("https://playground.learnqa.ru/api/homework_" + uri)
                .andReturn();

        assertEquals(200, response.getStatusCode(), "Unexpected status code: " + response.getStatusCode());

        if (uri.equals("cookie")) {
            Map<String, String> cookies = response.getCookies();

            assertTrue(cookies.containsKey("HomeWork"), "Response doesn't have 'HomeWork' cookie");
            assertEquals(cookies.get("HomeWork"), "hw_value", "Unexpected 'HomeWork' cookie value: " + cookies.get("HomeWork"));
        } else if (uri.equals("header")) {
            Headers headers = response.getHeaders();

            assertTrue(headers.hasHeaderWithName("x-secret-homework-header"), "Response doesn't have 'x-secret-homework-header' header");
            assertEquals(headers.get("x-secret-homework-header").getValue(), "Some secret value", "Unexpected 'x-secret-homework-header' header value: " + headers.get("x-secret-homework-header"));
        } else {
            throw new RuntimeException("Unexpected uri for current test");
        }
    }


    private Stream<Arguments> arguments() {

        return Stream.of(Arguments.of(
                        "'Mozilla/5.0 (Linux; U; Android 4.0.2; en-us; Galaxy Nexus Build/ICL53F) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30'",
                        "Mobile", "No", "Android"),
                Arguments.of(
                        "'Mozilla/5.0 (iPad; CPU OS 13_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) CriOS/91.0.4472.77 Mobile/15E148 Safari/604.1'",
                        "Mobile", "Chrome", "iOS"),
                Arguments.of(
                        "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)",
                        "Googlebot", "Unknown", "Unknown"),
                Arguments.of(
                        "'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.77 Safari/537.36 Edg/91.0.100.0'",
                        "Web", "Chrome", "No"),
                Arguments.of(
                        "'Mozilla/5.0 (iPad; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Mobile/15E148 Safari/604.1'",
                        "Mobile", "No", "iPhone")

        );

    }

    @ParameterizedTest
    @MethodSource("arguments")
    @DisplayName("Ex13: User Agent")
    public void testEx13(String userAgent, String platform, String browser, String device) {
        Response response = RestAssured
                .given()
                .headers("User-Agent", userAgent)
                .get("https://playground.learnqa.ru/ajax/api/user_agent_check")
                .andReturn();

        assertEquals(platform,response.jsonPath().get("platform"),"Wrong platform selected");
        assertEquals(browser,response.jsonPath().get("browser"),"Wrong browser selected");
        assertEquals(device,response.jsonPath().get("device"),"Wrong device selected");

    }

}
