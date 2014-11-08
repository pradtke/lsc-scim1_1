package org.lsc.plugins.connectors.scim1_1;

import com.google.common.collect.Lists;
import com.google.gdata.data.appsforyourdomain.provisioning.UserEntry;
import com.unboundid.scim.data.Entry;
import com.unboundid.scim.data.Name;
import com.unboundid.scim.data.UserResource;
import com.unboundid.scim.schema.CoreSchema;
import com.unboundid.scim.sdk.Resources;
import com.unboundid.scim.sdk.SCIMEndpoint;
import com.unboundid.scim.sdk.SCIMException;
import com.unboundid.scim.sdk.SCIMService;
import org.lsc.LscDatasets;
import org.lsc.LscModifications;
import org.lsc.beans.IBean;
import org.lsc.beans.SimpleBean;
import org.lsc.configuration.TaskType;
import org.lsc.exception.LscServiceCommunicationException;
import org.lsc.exception.LscServiceConfigurationException;
import org.lsc.exception.LscServiceException;
import org.lsc.service.IWritableService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by patrick.radtke on 11/7/14.
 */
public class Scim1_1UserDstService implements IWritableService {

    protected static final Logger LOGGER = LoggerFactory.getLogger(Scim1_1UserDstService.class);


    SCIMService scimService;

    SCIMEndpoint<UserResource> userEndpoint;


    String byIdFilter = "emails eq \"%s\"";


    public Scim1_1UserDstService(final TaskType task) throws LscServiceConfigurationException, LscServiceCommunicationException {

        try {
            scimService = new SCIMService(new URI("http://localhost:8080/"), "bjensen", "password");
            scimService.setAcceptType(MediaType.APPLICATION_JSON_TYPE);
            scimService.setContentType(MediaType.APPLICATION_JSON_TYPE);
            userEndpoint = scimService.getUserEndpoint();
        } catch (URISyntaxException e) {
            throw new LscServiceConfigurationException(e);
        }


    }

    @Override
    public boolean apply(LscModifications lm) throws LscServiceException {
        try {
            switch (lm.getOperation()) {
                case CREATE_OBJECT:
                    userEndpoint.create(convertLMToUserEntry(lm, new UserEntry()));
                    break;
                case UPDATE_OBJECT:
                case CHANGE_ID:
                    //updateUser(lm.getMainIdentifier(), convertLMToUserEntry(lm, usersCache.get(lm.getMainIdentifier())));
                    break;
                case DELETE_OBJECT:
//            if(System.getenv("I_UNDERSTAND_THAT_GOOGLEAPPS_ACCOUNTS_WILL_BE_DELETED_WITH_THEIR_DATA") != null) {
//                deleteUser(lm.getMainIdentifier());
//            } else {
//                if (lm.getSourceBean().getDatasetById(DATASET_NAME_NICKNAME) != null) {
//                    deleteNickname(lm.getSourceBean().getDatasetFirstValueById(DATASET_NAME_NICKNAME));
//                }
//                UserEntry userEntry = usersCache.get(lm.getMainIdentifier());
//                userEntry.getLogin().setSuspended(true);
//                updateUser(lm.getMainIdentifier(), userEntry);
//            }
                    break;
                default:
                    throw new LscServiceException("Unknown mod type " + lm.getOperation());
            }
        } catch (SCIMException e) {
            throw new LscServiceException(e);
        }
        return true;
    }

    @Override
    public List<String> getWriteDatasetIds() {
        return Lists.newArrayList("username", "familyName", "mail");
        //CoreSchema.USER_DESCRIPTOR.getAttributes();
    }

    @Override
    public IBean getBean(String pivotName, LscDatasets pivotAttributes, boolean fromSameService) throws LscServiceException {
        // search user endpoint

        try {
            Resources<UserResource> queryResponse = userEndpoint.query(String.format(byIdFilter, pivotName));
            if (queryResponse.getTotalResults() == 0) {
                return null;
            } else if (queryResponse.getTotalResults() > 1) {
                throw new LscServiceException("1 search result expected for " + pivotName + ", but found " + queryResponse.getTotalResults());
            }

            UserResource userResource = queryResponse.iterator().next();
            SimpleBean simpleBean = new SimpleBean();
            //TODO: makes the configurable: externalId, username or primary Email
            simpleBean.setMainIdentifier(userResource.getUserName());
            LscDatasets datasets = new LscDatasets();
            datasets.put("familyName", userResource.getName().getFamilyName());

            simpleBean.setDatasets(datasets);

            return simpleBean;

        } catch (SCIMException e) {
            throw new LscServiceException(e);
        }

    }

    @Override
    public Map<String, LscDatasets> getListPivots() throws LscServiceException {
        Map<String, LscDatasets> pivots = new HashMap<String, LscDatasets>();
        try {
            Resources<UserResource> queryResponse = userEndpoint.query(null, null, null, "username");
            for (UserResource user : queryResponse) {
                pivots.put(user.getUserName(), new LscDatasets());
            }
            return pivots;
        } catch (SCIMException e) {
            throw new LscServiceException(e);
        }
    }

    private UserResource convertLMToUserEntry(LscModifications lm, UserEntry userEntry) {
        UserResource resource = new UserResource(CoreSchema.USER_DESCRIPTOR);
        Map<String, List<Object>> mods = lm.getModificationsItemsByHash();
        if (mods.containsKey("username")) {
            resource.setUserName(mods.get("username").get(0).toString());
        }
        if (mods.containsKey("familyName")) {
            String sn = mods.get("familyName").get(0).toString();
            com.unboundid.scim.data.Name name = new Name(sn, sn, null, null, null, null);
            resource.setName(name);
        }

        if (mods.containsKey("mail")) {
            Collection<Entry<String>> emails = Lists.newArrayList(new Entry<String>(mods.get("mail").get(0).toString(), "work", true));
            resource.setEmails(emails);
        }

        return resource;
    }

}