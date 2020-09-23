import org.dorkmaster.reportbot.ReportContext
import org.dorkmaster.reportbot.ReportContextContainer
import org.dorkmaster.reportbot.SubmitReport
import org.dorkmaster.reportbot.SurveyFactory
import org.dorkmaster.reportbot.config.Config
import org.dorkmaster.reportbot.service.ReportContextContainerImpl
import org.junit.Assert
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito

import static TestConstants.*

class ReportContextContainerImplTests {

    @Test
    void testHappySubmit() {
        def testUser = "test"
        Config config = new Config()

        SurveyFactory sf = Mockito.mock(SurveyFactory.class)
        Mockito.when(sf.mapToSurvey(Mockito.anyObject(), Mockito.anyMap(), Mockito.anyString())).thenReturn(['test':'test'])

        SubmitReport sr = Mockito.mock(SubmitReport.class)
        Mockito.when(sr.submit(Mockito.anyObject(), Mockito.anyMap())).thenReturn(true);

        ReportContextContainer rcc = new ReportContextContainerImpl(config, sf, sr)

        Assert.assertFalse(rcc.has(testUser))

        ReportContext rc = rcc.create(GUILD, testUser )
        Assert.assertTrue(rcc.has(testUser))
        Assert.assertNotNull(rc)

        rc.answer(A1)
        rc.answer(A2)
        rc.answer(A3)

        Assert.assertTrue(rc.submit())

        Assert.assertFalse(rcc.has(testUser))
    }


}
