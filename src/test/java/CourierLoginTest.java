import io.qameta.allure.Description;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.example.Const;
import org.example.Courier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;

public class CourierLoginTest {
    Courier courier;

    @Before
    public void setUp() {
        RestAssured.baseURI = Const.URL;
        int random = (int)(Math.random()*10000);
        courier = new Courier(String.format("%s %d","Иван", random),"123");
        given()
                .header("Content-type", "application/json")
                .and()
                .body(courier)
                .when()
                .post(Const.URL_CREATE_COURIER);
    }

    @Test
    @Description("Проверка успешной авторизации курьера")
    public void courierLoginSuccess(){
        Response response=loginCourier(courier);
        response.then()
                .statusCode(200)
                .and()
                .assertThat().body("id",notNullValue());
    }
    @Test
    @Description("Проверка обязательности поля логин при авторизации")
    public void courierLoginWithoutLoginShowsError(){
        courier = new Courier("",courier.getPassword());
        Response response=loginCourier(courier);
        response.then()
                .statusCode(400)
                .and()
                .assertThat().body("message",equalTo("Недостаточно данных для входа"));
    }
    @Test
    @Description("Проверка обязательности поля пароль при авторизации")
    public void courierLoginWithoutPasswordShowsError(){
        courier = new Courier(courier.getLogin(),"");
        Response response=loginCourier(courier);
        response.then()
                .statusCode(400)
                .and()
                .assertThat().body("message",equalTo("Недостаточно данных для входа"));
    }
    @Test
    @Description("Проверка ошибки авторизации при вводе неправильного логина")
    public void courierLoginWithWrongLoginShowsError(){
        courier = new Courier(courier.getLogin()+"test",courier.getPassword());
        Response response=loginCourier(courier);
        response.then()
                .statusCode(404)
                .and()
                .assertThat().body("message",equalTo("Учетная запись не найдена"));
    }
    @Test
    @Description("Проверка ошибки авторизации при вводе неправильного пароля")
    public void courierLoginWithWrongPasswordShowsError(){
        courier = new Courier(courier.getLogin(),courier.getPassword()+"test");
        Response response=loginCourier(courier);
        response.then()
                .statusCode(404)
                .and()
                .assertThat().body("message",equalTo("Учетная запись не найдена"));
    }
    @Test
    @Description("Проверка ошибки авторизации несуществующего курьера")
    public void courierNotExistsShowsError(){
        courier = new Courier(courier.getLogin()+"test",courier.getPassword()+"test");
        Response response=loginCourier(courier);
        response.then()
                .statusCode(404)
                .and()
                .assertThat().body("message",equalTo("Учетная запись не найдена"));
    }
    public Response loginCourier(Courier courier){
        return given()
                .header("Content-type", "application/json")
                .and()
                .body(courier)
                .when()
                .post(Const.URL_LOGIN_COURIER);
    }
    @After
    public void tearDown(){
        try{int idCourier = given()
                .header("Content-type", "application/json")
                .and()
                .body(courier)
                .when()
                .post(Const.URL_LOGIN_COURIER)
                .then()
                .extract()
                .body()
                .path("id");
        given()
                .header("Content-type", "application/json")
                .and()
                .delete(Const.URL_DELETE_COURIER+idCourier);
        } catch (Exception exception){
            System.out.println("Нечего удалять");
        }

    }


}
