package tests

import model.Description
import model.Jira
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import tools.KotliniumTest
import tools.createNewDocument
import tools.loginPage
import tools.myDocuments

class RegressionSet: KotliniumTest(){

    @Test
    @Jira("IDM-18564")
    @Description("User can log in to IDM Direct using valid credentials")
    fun `user can log in to Direct`() {
        loginPage {
            `fill credentials of user`("idmt07")
            `press Login button`()
        }

        myDocuments {
            `verify disclaimer is visible`()
            `accept disclaimer`()
        }
    }

    @Test
    @Description("User can create Module 1 Document from Quality template")
    fun `user can create document based on Quality template`(){
        loginPage {
            `fill credentials of user`("idmt05")
            `press Login button`()
        }

        myDocuments {
            `accept disclaimer`()
            `press Create button`()
        }

        createNewDocument {
            `open Search Document Types`()
            `open All Document Types`()
            `select domain`("Pharmacovigilance")
            `select domain`("Safety Communications")
            `select domain`("Safety Communications Compound")
            `select All Document Types template`("DHPC Letter")
            `fill property`("Doc Type Decision Date", "22 Jul 2025")
            `fill property`("Product", "alec")
            `fill mandatory properties`()
        }
    }
}