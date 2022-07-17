package aspects

import model.DisplayName
import model.TestStepReport
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.After
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.aspectj.lang.reflect.MethodSignature
import org.openqa.selenium.OutputType
import org.openqa.selenium.TakesScreenshot
import tools.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


// Added to screenshot name to avoid screenshot name duplication
private var counter = 0L

/**
 * @param testName - name of the Test Set
 * @param stepName - name of currently finished step
 * Takes screenshot of the currently finished step
 * @return relative path to newly created screenshot
 */
fun getScreenshot(testName: String, stepName: String): String {
    val dateName = SimpleDateFormat("yyyyMMddhhmmss").format(Date())
    val ts = driver as TakesScreenshot
    val source = ts.getScreenshotAs(OutputType.FILE)
    val screenshotName = normalizeScreenshotName("$stepName$dateName-$counter.png").also { counter += 1 }
    val destination = "./reports/$testName/$screenshotName"
    val finalDestination = File(destination)
    source.copyTo(finalDestination)
    return screenshotName
}

private fun normalizeScreenshotName(name: String): String {
    return name.replace("(['\"<>:/#&])".toRegex(), "_")
}

@Aspect
class PageAspect {

    /**
     * @param joinPoint - reference to a method for which the function is called
     */
    @After("execution(void pages.*.*(..))")
    fun collectTest(joinPoint: JoinPoint) {
        if (!isInvokedByTestCase()) return

        val stepName = joinPoint.signature.name
        val methodArgs = joinPoint.args.map { it.toString() }
        val screenshotPath = if(System.getProperty("screenshot.strategy").trim().lowercase() == "always") getScreenshot(testName, stepName) else ""
        val timestamp = System.currentTimeMillis()
        val displayName = (joinPoint.signature as MethodSignature).method.annotations.findValue(DisplayName::class)?.let{ (it as DisplayName).name}?: ""
        testCaseSteps.add(TestStepReport(stepName, methodArgs, screenshotPath, timestamp, displayName))
        logger.info("Finished step: $stepName - $methodArgs")
    }
    /**
     * Creates name of currently run test case. Adds arguments' values to the test case name if it has any.
     * @param joinPoint - reference to a method for which the function is called
     */
    @Before("execution(void tests.*.*(..))")
    fun createTestName(joinPoint: JoinPoint) {
        testNameMap[Thread.currentThread().name.also { logger.info("Setting up testNameMap for $it") }] =
            joinPoint.signature.name + if (joinPoint.args.isNotEmpty()) " - ${joinPoint.args.joinToString(" ") { "[$it]" }}" else ""
        logger.info("$testName is now being run.")
    }
}

//Check if object invoking Test Step was in 'tests' package
// 0 for this function, 1 for PageAspect check, 2 for a calling function (test step), 3 for a caller
fun isInvokedByTestCase(): Boolean {
    return "${Exception().stackTrace[3]}".startsWith("tests")
}