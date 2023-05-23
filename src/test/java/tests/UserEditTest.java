package tests;

import io.qameta.allure.Description;
import io.restassured.response.Response;
import lib.ApiCoreRequests;
import lib.Assertions;
import lib.BaseTestCase;
import lib.DataGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static lib.DataGenerator.getRegistrationData;

public class UserEditTest extends BaseTestCase {

    private final ApiCoreRequests apiCoreRequests = new ApiCoreRequests();

    private Map<String, String> createdUserData = new HashMap<>();
    private String createdUserId;


    @BeforeEach
    public void beforeEach() {
        //Create user
        createdUserData =
                getRegistrationData();

        Response responseCreateAuth =
                apiCoreRequests.makePostRequest(
                        "https://playground.learnqa.ru/api/user/",
                        createdUserData);

        Assertions.assertResponseCodeEquals(responseCreateAuth, 200); //На уроке обещали объяснить, но не объяснили, куда лучше выложить ассерты из beforeAll?
        Assertions.assertJsonHasField(responseCreateAuth, "id");

        createdUserId = responseCreateAuth.jsonPath().get("id");
    }

    @Test
    @DisplayName("Ex17_1: Изменить данные пользователя, будучи неавторизованными")
    @Description("Ex17: Негативные тесты на PUT")
    public void testEx17_1() {

        String newName = UUID.randomUUID().toString();

        Map<String, String> editData = new HashMap<>();
        editData.put("firstName", newName);

        Response responseEditUser =
                apiCoreRequests.makePutRequestUnauthorized(
                        "https://playground.learnqa.ru/api/user/" + createdUserId,
                        editData);

        Assertions.assertResponseCodeEquals(responseEditUser, 400);
        Assertions.assertResponseTextEquals(responseEditUser, "Auth token not supplied");

    }

    @Test
    @DisplayName("Ex17_2: Изменить данные пользователя, будучи авторизованными другим пользователем")
    @Description("Ex17: Негативные тесты на PUT")
    //вариант 1
    public void testEx17_2_1() {

        Map<String, String> userDataForAuthorization = new HashMap<>();

        userDataForAuthorization.put("email", "vinkotov@example.com");
        userDataForAuthorization.put("password", "1234");


        Response responseGetAuth = login(userDataForAuthorization);

        //EDIT pre created user
        String newName = UUID.randomUUID().toString();

        Map<String, String> editData = new HashMap<>();
        editData.put("firstName", newName);


        getResponseAndCheckIt(responseGetAuth, editData, "Please, do not edit test users with ID 1, 2, 3, 4 or 5.");


    }

    @Test
    @DisplayName("Ex17_2: Изменить данные пользователя, будучи авторизованными другим пользователем")
    @Description("Ex17: Негативные тесты на PUT")
    //Вариант 2 - где добавляется генерация юзера для авторизации
    public void testEx17_2_2() {
        //Create user for authorization
        Map<String, String> userDataForAuthorizationByNewUser =
                getRegistrationData();

        Response responseCreateAuth =
                apiCoreRequests.makePostRequest(
                        "https://playground.learnqa.ru/api/user/",
                        userDataForAuthorizationByNewUser);

        Assertions.assertResponseCodeEquals(responseCreateAuth, 200);
        Assertions.assertJsonHasField(responseCreateAuth, "id");

        //LOGIN by just created user
        Response responseGetAuth = login(userDataForAuthorizationByNewUser);
        Assertions.assertResponseCodeEquals(responseGetAuth, 200);
        String header = this.getHeader(responseGetAuth, "x-csrf-token");
        String cookie = this.getCookie(responseGetAuth, "auth_sid");


        //EDIT pre created user with just created users cookie
        String newName = UUID.randomUUID().toString();

        Map<String, String> editData = new HashMap<>();
        editData.put("firstName", newName);

        Response responseEditUser =
                apiCoreRequests.makePutRequest(
                        "https://playground.learnqa.ru/api/user/" + createdUserId,
                        editData,
                        header,
                        cookie);

        System.out.println(responseEditUser.asString());
        Assertions.assertResponseCodeEquals(responseEditUser, 400);

        //LOGIN by changed user
        userDataForAuthorizationByNewUser.put("firstName", newName);
        userDataForAuthorizationByNewUser.put("email", createdUserData.get("email"));

        Response responseGetAuthByChangedUser = login(userDataForAuthorizationByNewUser);
        Assertions.assertResponseCodeEquals(responseGetAuthByChangedUser, 200);

        String headerByChangedUser = this.getHeader(responseGetAuthByChangedUser, "x-csrf-token");
        String cookieByChangedUser = this.getCookie(responseGetAuthByChangedUser, "auth_sid");

        //GET by changed user
        Response responseUserData =
                apiCoreRequests.makeGetRequest(
                        "https://playground.learnqa.ru/api/user/" + createdUserId,
                        headerByChangedUser,
                        cookieByChangedUser);

        Assertions.assertJsonByName(responseUserData, "firstName", newName);

        // Тк в сваггере указано -> Update user (must be logged in as this user) ->
        // Если мы создаем 2 пользователей, далее авторизуемся одним из них и пытаемся изменить данные другого -
        // должна быть ошибка при редактировании пользователя, однако status code 200 (хотя данные не меняются)

    }




    @Test
    @DisplayName("Ex17_3: Изменить email пользователя, будучи авторизованными тем же пользователем, на новый email без символа @")
    @Description("Ex17: Негативные тесты на PUT")
    public void testEx17_3() {
        //LOGIN
        Response responseGetAuth =
                login(createdUserData);


        //EDIT created user
        String email = DataGenerator.getRandomEmail()
                .replaceAll("@", "");

        Map<String, String> editDataForFirstNameChange = new HashMap<>();
        editDataForFirstNameChange.put("email", email);


        getResponseAndCheckIt(responseGetAuth, editDataForFirstNameChange, "Invalid email format");

    }

    @Test
    @DisplayName("Ex17_4: Изменить firstName пользователя, будучи авторизованными тем же пользователем, на очень короткое значение в один символ")
    @Description("Ex17: Негативные тесты на PUT")
    public void testEx17_4() {
        //LOGIN
        Response responseGetAuth =
                login(createdUserData);

        //EDIT created user
        String newName = "a";

        Map<String, String> editDataForFirstNameChange = new HashMap<>();
        editDataForFirstNameChange.put("firstName", newName);

        getResponseAndCheckIt(
                responseGetAuth,
                editDataForFirstNameChange,
                "{\"error\":\"Too short value for field firstName\"}");

    }

    private void getResponseAndCheckIt(
            Response responseGetAuth,
            Map<String, String> editData,
            String expectedAnswer) {

        Assertions.assertResponseCodeEquals(responseGetAuth, 200);

        String header = this.getHeader(responseGetAuth, "x-csrf-token");
        String cookie = this.getCookie(responseGetAuth, "auth_sid");

        Response responseEditUser =
                apiCoreRequests.makePutRequest(
                        "https://playground.learnqa.ru/api/user/" + createdUserId,
                        editData,
                        header,
                        cookie);

        Assertions.assertResponseCodeEquals(responseEditUser, 400);
        Assertions.assertResponseTextEquals(responseEditUser, expectedAnswer);

    }

}
