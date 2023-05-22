import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
}
