import org.junit.BeforeClass;
import org.junit.Test;
import org.lsc.LscDatasets;
import org.lsc.beans.IBean;
import org.lsc.configuration.TaskType;
import org.lsc.exception.LscServiceCommunicationException;
import org.lsc.exception.LscServiceConfigurationException;
import org.lsc.exception.LscServiceException;
import org.lsc.plugins.connectors.scim1_1.Scim1_1UserDstService;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by patrick.radtke on 11/7/14.
 */
public class Scim1_1UserDstServiceTest {


    static Scim1_1UserDstService userDstService;

    @BeforeClass
    public static void setupService() throws LscServiceConfigurationException, LscServiceCommunicationException {
        TaskType taskType = new TaskType();
        userDstService = new Scim1_1UserDstService(taskType);
    }


    @Test
    public void testGetBean() throws LscServiceException {
        //IBean bean = userDstService.getBean("jsmith", null, false);
        IBean bean = userDstService.getBean("jsmith@example.com", null, false);

        assertEquals("Smith", bean.datasets().getStringValueAttribute("familyName"));
    }



    @Test
    public void testGetPivots() throws LscServiceException {
        Map<String, LscDatasets> results = userDstService.getListPivots();

        assertTrue(results.containsKey("jsmith"));
    }

}
