package model

data class TestStepReport(
    val stepName: String,
    val stepParameters: List<String>,
    val screenshotPath: String,
    val timestamp: Long
)