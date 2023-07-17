import io.qameta.allure.Description;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.example.Const;
import org.example.Order;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.notNullValue;

@RunWith(Parameterized.class)
public class OrdersTest {
    static String[] color;
    Order order;
    int idTrack;
    @Before
    public void setUp() {
        RestAssured.baseURI = Const.URL;
    }
   public OrdersTest(String[] color){
        this.color=color;
    }
    @Parameterized.Parameters
    public static Object[][] getParams() {
        return new Object[][]{
                {new String[]{"BLACK","GREY"}},
                {new String[]{"BLACK"}},
                {new String[]{"GREY"}},
                {new String[]{}}
        };
    }
    @Test
    @Description("Проверка создания заказов с разными цветами самоката")
    public void createOrderWithDifferentColorsSuccess(){
        order = new Order("Петр","Петров","г.Москва, ул.Ясногорская, д.13, кв.234","5",
                "+79851234567",5,"2023-07-25","Какой-то комментарий",color);
        Response response=given()
                .header("Content-type", "application/json")
                .and()
                .body(order)
                .when()
                .post(Const.URL_CREATE_ORDER);
        response.then()
                .statusCode(201)
                .and()
                .assertThat().body("track",notNullValue());
        idTrack = response.then().extract().body().path("track");
        System.out.println(idTrack);
    }

    @After
    public void tearDown(){
          given()
                .header("Content-type", "application/json")
                .and().queryParam("track",idTrack)
                .put(Const.URL_CANCEL_ORDER);
    }
}
