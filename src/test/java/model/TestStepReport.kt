package model

data class TestStepReport(
    val stepName: String,
    val stepParameters: List<String>,
    var screenshotPath: String,
    val timestamp: Long,
    val displayName: String = "",
    var testPassed: Boolean = true
)