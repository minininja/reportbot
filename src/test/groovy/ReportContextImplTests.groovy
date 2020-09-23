import org.dorkmaster.reportbot.config.Config
import org.dorkmaster.reportbot.service.ReportContextContainerImpl
import org.dorkmaster.reportbot.service.ReportContextImpl
import org.junit.Assert
import org.junit.Test

import static TestConstants.A1
import static TestConstants.A2
import static TestConstants.A3
import static TestConstants.GUILD
import static TestConstants.Q1
import static TestConstants.Q3

class ReportContextImplTests {

    @Test
    void testAbortResponse() {
        Config config = new Config();
        def rcc = new ReportContextContainerImpl()
        def rc = new ReportContextImpl(GUILD, "test", config, rcc)

        Assert.assertFalse(rc.isAborting("test message"))
        Assert.assertTrue(rc.isAborting("abort report"))
        Assert.assertTrue(rc.isAborting("Abort Report"))
        Assert.assertTrue(rc.isAborting("  Abort                  Report  "))
        Assert.assertTrue(rc.isAborting("AbortReport"))
    }

    @Test
    void testQuestions() {

        Config config = new Config();
        def rcc = new ReportContextContainerImpl()
        def rc = new ReportContextImpl("1", "test", config, rcc)

        Assert.assertEquals(Q1, rc.currentQuestion())
        // no answer so should return the same one
        Assert.assertEquals(Q1, rc.currentQuestion())
        Assert.assertTrue(rc.hasNextQuestion())
        Assert.assertTrue(rc.answer(A1))

        Assert.assertEquals(TestConstants.Q2, rc.currentQuestion())
        Assert.assertTrue(rc.hasNextQuestion())
        Assert.assertTrue(rc.answer(A2))

        Assert.assertEquals(Q3, rc.currentQuestion())
        Assert.assertTrue(rc.hasNextQuestion())
        Assert.assertTrue(rc.answer(A3))

        // Shouldn't be a next question so it should fail
        Assert.assertFalse(rc.answer("Who cares?"))

        Assert.assertEquals(3, rc.getQuestionsAnswers().size())
        Assert.assertEquals(A1, rc.getQuestionsAnswers().get(Q1.toString()))
        Assert.assertEquals(A2, rc.getQuestionsAnswers().get(TestConstants.Q2.toString()))
        Assert.assertEquals(A3, rc.getQuestionsAnswers().get(Q3.toString()))
    }
}