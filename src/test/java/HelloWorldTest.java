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

}
