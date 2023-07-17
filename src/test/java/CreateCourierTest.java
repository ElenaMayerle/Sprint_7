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


public class CreateCourierTest {
    @Before
    public void setUp() {
        RestAssured.baseURI = Const.URL;
    }
    Courier courier;
    int random = (int)(Math.random()*10000);

    @Test
    @Description("Проверка успешного создания курьера")
    public void createCourierSuccess(){
        courier = new Courier(String.format("%s %d","Иван", random),"123",String.format("%s %d","Иван", random));
        Response response = createCourier(courier);
        response.then()
                .statusCode(201)
                 .and()
                 .assertThat().body("ok",equalTo(true));

    }
   @Test
   @Description("Проверка невозможности создания двух одинаковых курьеров")
    public void createTwoIdenticalCouriersShowsError(){
        courier = new Courier(String.format("%s %d","Иван", random),"123",String.format("%s %d","Иван", random));
        createCourier(courier);
        Response response = createCourier(courier);
        response.then()
                .statusCode(409)
                .and()
                .assertThat().body("message",equalTo("Этот логин уже используется. Попробуйте другой."));
    }
   @Test
   @Description("Проверка невозможности создания курьера с уже существующим логином")
    public void createCourierWithExistingLoginShowsError(){
        int random1 = (int)(Math.random()*10000);
        courier = new Courier(String.format("%s %d","Иван", random),"123",String.format("%s %d","Иван", random));
        Courier courier1=new Courier(String.format("%s %d","Иван", random),"1234",String.format("%s %d","Иван", random1));
        createCourier(courier);
        Response response = createCourier(courier1);
        response.then()
                .statusCode(409)
                .and()
                .assertThat().body("message",equalTo("Этот логин уже используется. Попробуйте другой."));
    }
    @Test
    @Description("Проверка обязательности логина при создании курьера")
    public void createCourierWithoutLoginShowsError(){
        courier = new Courier("","123",String.format("%s %d","Иван", random));
        Response response = createCourier(courier);
        response.then()
                .statusCode(400)
                .and()
                .assertThat().body("message",equalTo("Недостаточно данных для создания учетной записи"));
    }
    @Test
    @Description("Проверка обязательности пароля при создании курьера")
    public void createCourierWithoutPasswordShowsError(){
        courier = new Courier(String.format("%s %d","Иван", random),"",String.format("%s %d","Иван", random));
        Response response = createCourier(courier);
        response.then()
                .statusCode(400)
                .and()
                .assertThat().body("message",equalTo("Недостаточно данных для создания учетной записи"));
    }
    @Test
    @Description("Проверка необязательности имени при создании курьера")
    public void createCourierWithoutFirstNameIsOk(){
        courier = new Courier(String.format("%s %d","Иван", random),"123","");
        Response response = createCourier(courier);
        response.then()
                .statusCode(201)
                .and()
                .assertThat().body("ok",equalTo(true));
    }
    public Response createCourier(Courier courier){
        return given()
                .header("Content-type", "application/json")
                .and()
                .body(courier)
                .when()
                .post(Const.URL_CREATE_COURIER);
    }
    @After
    public void tearDown(){
        try {int idCourier = given()
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
