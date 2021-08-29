package tools

import business.environmentURL
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.support.ui.WebDriverWait
import java.io.File
import java.util.concurrent.ThreadLocalRandom
import kotlin.streams.asSequence

lateinit var driver: ChromeDriver

fun Array<Annotation>.findValue(annotation: String): Annotation? {
    return find { it.annotationClass.simpleName == annotation }
}

fun openPage(website: String) {
    driver.get(website).also {
        WebDriverWait(driver, 60).until { driver.executeScript("return document.readyState") == "complete" }
    }
    println("opened $environmentURL")
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

fun File.mkSubDirs(){
    with(this.path){
        //file separator is either '/' or '\', but is received as String. [0] to get char.
        File(substring(0, indexOfLast { it == System.getProperty("file.separator")[0] })).mkdirs()
    }
}