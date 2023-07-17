import io.qameta.allure.Description;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.example.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.notNullValue;

public class OrderListTest {
    Courier courier;
    Order order;
    int idCourier;
    int idTrack;
    int idOrder;
    List<Integer> listId;

    @Before
    public void setUp() {
        RestAssured.baseURI = Const.URL;
        int random = (int)(Math.random()*10000);
        courier = new Courier(String.format("%s %d","Иван", random),"123",String.format("%s %d","Иван", random));
        given()
                .header("Content-type", "application/json")
                .and()
                .body(courier)
                .when()
                .post(Const.URL_CREATE_COURIER);
        idCourier = given()
                .header("Content-type", "application/json")
                .and()
                .body(courier)
                .when()
                .post(Const.URL_LOGIN_COURIER)
                .then()
                .extract()
                .body()
                .path("id");
        order = new Order("Петр","Петров","г.Москва, ул.Ясногорская, д.13, кв.234","5",
                "+79851234567",5,"2023-07-25","Какой-то комментарий", new String[]{"BLACK,GREY"});
        Response response = given()
                .header("Content-type", "application/json")
                .and()
                .body(order)
                .when()
                .post(Const.URL_CREATE_ORDER);
        idTrack = response.then().extract().body().path("track");
        idOrder = given()
                .header("Content-type", "application/json")
                .and()
                .queryParam("t",idTrack)
                .get(Const.URL_GET_ORDER)
                .then()
                .extract()
                .body()
                .path("order.id");
        given()
                .header("Content-type", "application/json")
                .and()
                .queryParam("courierId",idCourier)
                .when()
                .put(Const.URL_ACCEPT_ORDER+idOrder);

    }

    @Test
    @Description("Проверка получения списка заказов курьера")
    public void getOrderListSuccess(){
        Response response = given()
                .header("Content-type", "application/json")
                .and()
                .queryParam("courierId",idCourier)
                .when()
                .get(Const.URL_ORDER_LIST);
              response.then()
                .assertThat()
                .body("orders", notNullValue())
                .and()
                .statusCode(200);
        listId = response.then().extract().body().path("orders.id");
    }


    @After
    public void tearDown(){
        for (Integer integer : listId) {
            given()
                    .header("Content-type", "application/json")
                    .and()
                    .put(Const.URL_FINISH_ORDER + integer);
        }
        given()
                .header("Content-type", "application/json")
                .and()
                .delete(Const.URL_DELETE_COURIER+idCourier);

    }

}
