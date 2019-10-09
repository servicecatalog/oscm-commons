/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2019
 *
 *  Creation Date: 23.09.2019
 *
 *******************************************************************************/
package org.oscm.identity.mapper;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.oscm.identity.model.UserInfo;
import org.oscm.internal.types.enumtypes.Salutation;
import org.oscm.internal.vo.VOUserDetails;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.enumtypes.LogMessageIdentifier;

public class UserMapper {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(UserMapper.class);

    public static UserInfo from(VOUserDetails userDetails) {

        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(userDetails.getUserId());
        userInfo.setFirstName(userDetails.getFirstName());
        userInfo.setLastName(userDetails.getLastName());
        userInfo.setEmail(userDetails.getEMail());
        userInfo.setPhone(userDetails.getPhone());
        userInfo.setLocale("en"); // use en as default language
        userInfo.setAddress(userDetails.getAddress());
        return userInfo;
    }

    public static VOUserDetails from(UserInfo userInfo) {

        VOUserDetails userDetails = new VOUserDetails();
        userDetails.setUserId(userInfo.getUserId());
        userDetails.setFirstName(userInfo.getFirstName());
        userDetails.setLastName(userInfo.getLastName());

        if (userInfo.getEmail() != null && !userInfo.getEmail().isEmpty()
                && !userInfo.getEmail().equalsIgnoreCase("null")) {
            userDetails.setEMail(userInfo.getEmail());
        } else {
            userDetails.setEMail(userInfo.getUserId());
        }
        userDetails.setPhone(userInfo.getPhone());
        userDetails.setLocale("en"); // use en as default language here
        userDetails.setSalutation(mapGenderToSalutation(userInfo.getGender()));
        userDetails.setAddress(userInfo.getAddress());
        return userDetails;
    }

    public static <T> List<?> fromSet(Set<T> userInfo) {
        List<Object> userInfos = new ArrayList();
        if (userInfo.getClass() != null) {
            for (T user : userInfo) {
                try {
                    Method from = UserMapper.class
                            .getMethod("from", user.getClass());
                    userInfos.add(from.invoke(null, user));

                } catch (Exception e) {
                    logger.logError(
                            LogMessageIdentifier.ERROR_OBJECT_ENCODING_FAILED,
                            "An error occured while mapping userInfo and userDetails");
                }
            }
        }
        return userInfos;
    }

    protected static Salutation mapGenderToSalutation(String gender) {

        if (gender == null) {
            return Salutation.MS;
        }
        switch (gender) {
        case "male":
            return Salutation.MR;
        case "female":
            return Salutation.MS;
        case "?":
            return Salutation.MS;
        default:
            return Salutation.MS;
        }
    }

}
