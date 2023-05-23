package tests;

import io.qameta.allure.Description;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import lib.ApiCoreRequests;
import lib.Assertions;
import lib.BaseTestCase;
import lib.DataGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.Map;

import static lib.DataGenerator.getRegistrationData;

public class UserRegisterTest extends BaseTestCase {

    private final ApiCoreRequests apiCoreRequests = new ApiCoreRequests();


    @Test
    @DisplayName("Ex15_1: Создание пользователя с некорректным email - без символа @")
    @Description("Ex15: Тесты на метод user")
    public void testEx15_1() {
        String email = DataGenerator.getRandomEmail()
                .replaceAll("@", "");

        Map<String, String> userData = new HashMap<>();
        userData.put("email", email);

        userData =
                getRegistrationData(userData);

        Response responseCreateAuth =
                apiCoreRequests
                        .makePostRequest(
                                "https://playground.learnqa.ru/api/user/",
                                userData);

        Assertions.assertResponseCodeEquals(responseCreateAuth, 400);
        Assertions.assertResponseTextEquals(responseCreateAuth, "Invalid email format");
    }

    @ParameterizedTest
    @ValueSource(strings = {"email", "password", "username", "firstName", "lastName"})
    @DisplayName("Ex15_2: Создание пользователя без указания одного из полей")
    @Description("Ex15: Тесты на метод user")
    public void testEx15_2(String emptyField) {
        Map<String, String> userData = new HashMap<>();

        userData.put(emptyField, null);

        userData =
                getRegistrationData(userData);

        Response responseCreateAuth =
                apiCoreRequests
                        .makePostRequest(
                                "https://playground.learnqa.ru/api/user/",
                                userData);

        Assertions.assertResponseCodeEquals(responseCreateAuth, 400);
        Assertions.assertResponseTextEquals(responseCreateAuth, "The following required params are missed: " + emptyField);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "g",
            "Hubert Blaine Wolfeschlegelsteinhausenbergerdorff Sr.Hubert Blaine Wolfeschlegelsteinhausenbergerdorff Sr.Hubert Blaine Wolfeschlegelsteinhausenbergerdorff Sr. Sr.Hubert Blaine Wolfeschlegelsteinhausenbergerdorff Sr. Sr.Hubert Blaine Wolfeschlegelsteinhausenbergerdorff Sr." })
    @DisplayName("Ex15_3/Ex15_4: Создание пользователя с очень коротким/длинным именем")
    @Description("Ex15: Тесты на метод user")
    public void testEx15_3(String username) {
        Map<String, String> userData = new HashMap<>();
        userData.put("username", username);

        userData =
                getRegistrationData(userData);

        Response responseCreateAuth =
                apiCoreRequests
                        .makePostRequest(
                                "https://playground.learnqa.ru/api/user/",
                                userData);

        Assertions.assertResponseCodeEquals(responseCreateAuth, 400);
        if (username.length() == 1) {
            Assertions.assertResponseTextEquals(responseCreateAuth, "The value of 'username' field is too short");
        }
        if (username.length() > 250) {
            Assertions.assertResponseTextEquals(responseCreateAuth, "The value of 'username' field is too long");
        }

    }

}
