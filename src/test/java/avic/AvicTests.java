package avic;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.openqa.selenium.By.xpath;
import static org.openqa.selenium.Keys.ENTER;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class AvicTests {

    ChromeOptions ops = new ChromeOptions();
    private WebDriver driver;

    @BeforeTest
    public void profileSetUp() {
        System.setProperty("webdriver.chrome.driver", "src\\main\\resources\\chromedriver.exe");
    }

    @BeforeMethod
    public void testsSetUp() {
        ops.addArguments("--disable-notifications");
        driver = new ChromeDriver(ops);//создаем экзаемпляр хром драйвера
        driver.manage().window().maximize();//открыли браузер на весь экран
        driver.get("https://avic.ua/");//открыли сайт
    }

    @Test(priority = 1)
    public void checkNegativeTestLogin() {
        driver.findElement(xpath("//div[@class='header-bottom__right-icon']//i[@class='icon icon-user-big']")).click();
        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
        driver.findElement(xpath("//div[@class='container ']//input[@name='login']")).sendKeys("example@mail.com");
        driver.findElement(xpath("//div[@class='container ']//input[@name='password']")).sendKeys("password");
        driver.findElement(xpath("//div[@class='container ']//button[@type='submit']")).click();
        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
        assertEquals(driver.findElement(xpath("//div[@class='col-xs-12 js_message']")).getText(), "Неверные данные авторизации.");
    }

    @Test(priority = 2)
    public void checkListDisplaysOnlyAsusComputers() {
        driver.findElement(xpath("//span[@class='sidebar-item']")).click();
        driver.findElement(xpath("//ul[contains(@class,'sidebar-list')]//a[contains(@href, 'elektronika')]")).click();
        driver.findElement(xpath("//div[contains(@class,'brand-box__title')]//a[contains(@href, 'gotovyie-pk')]")).click();
        driver.findElement(xpath("//label[@for='fltr-proizvoditel-asus']/a[contains(@href, 'proizvoditel--asus')]")).click();
        new WebDriverWait(driver, 30).until(
                webDriver -> ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete"));//wait for page loading
        int count = driver.findElements(xpath("//label[@for='fltr-proizvoditel-asus']/a[contains(@href, 'proizvoditel--asus')]")).size();
        //Check what all computers are from ASUS brand.
        for (int i = 0; i < count; i++) {
            assertTrue(driver.findElements(xpath("//label[@for='fltr-proizvoditel-asus']/a[contains(@href, 'proizvoditel--asus')]")).get(i).getAttribute("data-ecomm").contains("Asus"));
        }
    }

    @Test(priority = 3)
    public void checkListSortingIsWorkingCorrectlyAscendingOrder() {
        driver.findElement(xpath("//span[@class='sidebar-item']")).click();
        driver.findElement(xpath("//ul[contains(@class,'sidebar-list')]//a[contains(@href, 'elektronika')]")).click();
        driver.findElement(xpath("//div[contains(@class,'brand-box__title')]//a[contains(@href, 'gotovyie-pk')]")).click();
        int countSortList = driver.findElements(xpath("//span[@class='select2-selection__rendered']")).size();
        for (int i = 0; i < countSortList; i++) {
            if (driver.findElements(xpath("//span[@class='select2-selection__rendered']")).get(i).isDisplayed()) {
                driver.findElements(xpath("//span[@class='select2-selection__rendered']")).get(i).click();
                break;
            }
        }
        WebDriverWait wait = new WebDriverWait(driver, 30);
        wait.until(ExpectedConditions.visibilityOfElementLocated(xpath("//ul[@class='select2-results__options']//li[contains(text(),'По возрастанию')]")));
        driver.findElement(xpath("//ul[@class='select2-results__options']//li[contains(text(),'По возрастанию')]")).click();
        boolean sortedCorrectly = true; //переменная используемая при проверке цены
        int countProduct = driver.findElements(xpath("//div[@class='prod-cart__prise-new']")).size(); //количество элементов-товаров
        //Проверим цену каждого товара, если следующий товар стоит дешевле предыдущего, значит сортировка работает неправильно, т.к. цены должны идти по возрастанию.
        for (int i = 1; i < countProduct; i++) {
            String pricePrevious = driver.findElements(xpath("//div[@class='prod-cart__prise-new']")).get(i).getText();
            int pricePreviousInt = Integer.parseInt(pricePrevious.replaceAll("[^0-9.]", ""));

            String priceCurrent = driver.findElements(xpath("//div[@class='prod-cart__prise-new']")).get(i).getText();
            int priceCurrentInt = Integer.parseInt(priceCurrent.replaceAll("[^0-9.]", ""));

            if (pricePreviousInt<priceCurrentInt){
                sortedCorrectly=false;
                break;
            }
        }
        assertTrue(sortedCorrectly);
    }

    @Test(priority = 4)
    public void checkThatSearchFindResultUsingRussianWord() {
        driver.findElement(xpath("//input[@id='input_search']")).sendKeys("айфон 11", ENTER); //Вводим Iphone 11 но по русски.
        List<WebElement> elementList = driver.findElements(xpath("//div[@class='prod-cart__descr']"));//собрали элементы поиска в лист
        for (WebElement webElement : elementList) { //прошлись циклом и проверили что каждый элемент листа содержит текс iPhone 11
            assertTrue(webElement.getText().contains("iPhone 11"));
        }
    }

    @AfterMethod
    public void tearDown() {
        driver.close();//закрытие драйвера
    }
}
