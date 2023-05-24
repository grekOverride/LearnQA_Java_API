package tests;

import io.qameta.allure.*;
import io.restassured.response.Response;
import lib.ApiCoreRequests;
import lib.Assertions;
import lib.BaseTestCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static lib.DataGenerator.getRegistrationData;

@Owner("kateSt")
@Link("https://software-testing.ru/lms/mod/assign/view.php?id=326423")
@Epic("Ex18: Тесты на DELETE")
public class UserDeleteTest extends BaseTestCase {

    private final ApiCoreRequests apiCoreRequests = new ApiCoreRequests();


    @Test
    @DisplayName("Ex18_1: Попытка удалить пользователя по ID 2")
    public void testEx18_1() {
        //Auth
        Map<String, String> userDataForAuthorization = new HashMap<>();

        userDataForAuthorization.put("email", "vinkotov@example.com");
        userDataForAuthorization.put("password", "1234");

        Response responseGetAuth = login(userDataForAuthorization);
        Assertions.assertResponseCodeEquals(responseGetAuth, 200);

        String header = this.getHeader(responseGetAuth, "x-csrf-token");
        String cookie = this.getCookie(responseGetAuth, "auth_sid");

        //del
        Response responseDeleteUser =
                apiCoreRequests.makeDeleteRequest(
                        "https://playground.learnqa.ru/api/user/" + "2",
                        header,
                        cookie
                );
        Assertions.assertResponseCodeEquals(responseDeleteUser, 400);
        Assertions.assertResponseTextEquals(responseDeleteUser, "Please, do not delete test users with ID 1, 2, 3, 4 or 5.");
    }

    @Test
    @DisplayName("Ex18_2: Удаление созданного пользователя")
    public void testEx18_2() {
        //create
        Map<String, String> createdUserData;
        String createdUserId;

        createdUserData =
                getRegistrationData();

        Response responseCreateAuth =
                apiCoreRequests.makePostRequest(
                        "https://playground.learnqa.ru/api/user/",
                        createdUserData);

        Assertions.assertResponseCodeEquals(responseCreateAuth, 200);
        Assertions.assertJsonHasField(responseCreateAuth, "id");

        createdUserId = responseCreateAuth.jsonPath().get("id");

        //Auth
        Response responseGetAuth = login(createdUserData);
        Assertions.assertResponseCodeEquals(responseGetAuth, 200);

        String header = this.getHeader(responseGetAuth, "x-csrf-token");
        String cookie = this.getCookie(responseGetAuth, "auth_sid");

        //del
        Response responseDeleteUser =
                apiCoreRequests.makeDeleteRequest(
                        "https://playground.learnqa.ru/api/user/" + createdUserId,
                        header,
                        cookie
                );
        Assertions.assertResponseCodeEquals(responseDeleteUser, 200);

        //get
        Response responseGetUserData =
                apiCoreRequests.makeGetRequest(
                        "https://playground.learnqa.ru/api/user/" + createdUserId,
                        header,
                        cookie);

        Assertions.assertResponseCodeEquals(responseGetUserData, 404);
        Assertions.assertResponseTextEquals(responseGetUserData, "User not found");

    }

    @Test
    @DisplayName("Ex18_3: Попытка удалить пользователя, будучи авторизованными другим пользователем")
    public void testEx18_3() {
        //create
        Map<String, String> createdUserData;
        String createdUserId;

        createdUserData =
                getRegistrationData();

        Response responseCreateAuth =
                apiCoreRequests.makePostRequest(
                        "https://playground.learnqa.ru/api/user/",
                        createdUserData);

        Assertions.assertResponseCodeEquals(responseCreateAuth, 200);
        Assertions.assertJsonHasField(responseCreateAuth, "id");

        createdUserId = responseCreateAuth.jsonPath().get("id");

        //Auth
        Map<String, String> userDataForAuthorization = new HashMap<>();

        userDataForAuthorization.put("email", "vinkotov@example.com");
        userDataForAuthorization.put("password", "1234");

        Response responseGetAuth = login(userDataForAuthorization);
        Assertions.assertResponseCodeEquals(responseGetAuth, 200);

        String header = this.getHeader(responseGetAuth, "x-csrf-token");
        String cookie = this.getCookie(responseGetAuth, "auth_sid");

        //del
        Response responseDeleteUser =
                apiCoreRequests.makeDeleteRequest(
                        "https://playground.learnqa.ru/api/user/" + createdUserId,
                        header,
                        cookie
                );
        Assertions.assertResponseCodeEquals(responseDeleteUser, 400);
        Assertions.assertResponseTextEquals(responseDeleteUser, "Please, do not delete test users with ID 1, 2, 3, 4 or 5.");

    }


}
