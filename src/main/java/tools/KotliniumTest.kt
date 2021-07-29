package tools

import aspects.testCaseSteps
import business.environmentURL
import org.junit.*
import org.junit.rules.TestWatcher
import java.io.File


annotation class Jira(val id: String = "")
annotation class Description(val text: String = "")

lateinit var watcher: KotliniumTest.MyWatcher
private val reportsGenerated = mutableListOf<String>()

abstract class KotliniumTest {

    class MyWatcher : TestWatcher() {
        lateinit var annotations: Collection<Annotation>
        lateinit var methodName: String
        lateinit var testSetName: String
        override fun starting(description: org.junit.runner.Description?) {
            annotations = description?.annotations ?: emptyList()
            methodName = description?.methodName ?: ""
            //tests.RegressionSet -> RegressionSet
            testSetName = description?.className?.split(".")?.get(1) ?: ""
        }

        override fun succeeded(description: org.junit.runner.Description?) {
            super.succeeded(description)
            setStackTrace("")
        }

        override fun failed(e: Throwable?, description: org.junit.runner.Description?) {
            super.failed(e, description)
            e?.let {
                setStackTrace("${e.message}<br>${e.stackTrace?.joinToString("<br>")}")
            }
        }

        private fun setStackTrace(message: String){

            val methodName = watcher.methodName
            val destination = "$methodName/report.html"

            val reportSummary = File("./reports/$destination")
            reportSummary.readText().apply {
                val refactor = replace("%stacktrace%", message)
                reportSummary.writeText(refactor)
            }
        }
    }

    @Before
    fun setUp() {
        File("./reports/${watcher.methodName}").deleteRecursively()
        openPage(environmentURL)
    }

    // set watcher globally - used in AspectJ for screenshot naming
    @Rule
    @JvmField
    val testName = MyWatcher().also { watcher = it }

    @After
    fun after() {
        val methodName = watcher.methodName
        val jiraId = watcher.annotations.findValue("Jira")?.let { (it as Jira).id } ?: "No Jira ID specified"
        val description = watcher.annotations.findValue("Description")?.let { (it as Description).text } ?: ""

        val destination = "$methodName/report.html"
        val reportTemplate = File("./src/main/resources/reportTemplate.html")
        val reportSummary = File("./reports/$destination").apply { delete() }
        reportTemplate.copyTo(reportSummary)

        reportSummary.readText().apply {
            val refactor = replace("%title%", methodName)
                .replace("%jiraticket%", jiraId)
                .replace("%description%", description)
                .replace("%report%", testCaseSteps.joinToString(separator = "\n"))

            reportSummary.writeText(refactor)
        }
        reportsGenerated.add(
            """
            <li onclick="location.href='$destination'"><a href='$destination'>${methodName}</a></li>
        """.trimIndent()
        )
        // clear already added steps
        testCaseSteps.clear()
        //reset browser
        driver.manage().deleteAllCookies()
    }

    companion object {

        @BeforeClass
        @JvmStatic
        fun init() {
            File("./reports/").deleteRecursively()
        }

        @AfterClass
        @JvmStatic
        fun generateSummary() {
            println("Done all tests: ${watcher.testSetName}")
            driver.quit()
            val summaryTemplate = File("./src/main/resources/summaryTemplate.html")
            val reportSummary = File("./reports/${watcher.testSetName}.html").apply { delete() }
            summaryTemplate.copyTo(reportSummary)

            reportSummary.readText().apply {
                val refactor = replace("%title%", watcher.testSetName)
                    .replace("%summary%", reportsGenerated.joinToString(separator = "\n"))
                reportSummary.writeText(refactor)
            }
            println("Summary available at: ${reportSummary.absolutePath}")
        }
    }
}