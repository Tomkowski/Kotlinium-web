package tools

import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.support.ui.WebDriverWait
import java.io.File
import java.util.concurrent.ThreadLocalRandom
import kotlin.streams.asSequence

fun Collection<Annotation>.findValue(annotation: String): Annotation? {
    return find { it.annotationClass.simpleName == annotation }
}

fun openPage(website: String) {
    driver.get(website).also {
        WebDriverWait(driver, 60).until { driver.executeScript("return document.readyState").equals("complete") }
    }
}

val driver = run {
    System.setProperty(
        "webdriver.chrome.driver",
        "./src/test/resources/chromedriver.exe"
    )
    val driverOptions = File("./src/test/resources/driver.properties").readLines().filter { !it.startsWith("#") }
    val options = ChromeOptions().addArguments(driverOptions)
    println(driverOptions)
    ChromeDriver(options)
}

fun randomString(size: Int): String {
    val stringSize = size.coerceAtLeast(0)
    val characters = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    return ThreadLocalRandom.current()
        .ints(stringSize.toLong(), 0, characters.size)
        .asSequence()
        .map(characters::get)
        .joinToString("")
}