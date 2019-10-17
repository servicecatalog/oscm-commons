/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 31.07.2012                                                      
 *                                                                              
 *******************************************************************************/
package oscm.org.identity.mapper;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.oscm.identity.mapper.UserMapper;
import org.oscm.identity.model.UserInfo;
import org.oscm.internal.types.enumtypes.Salutation;
import org.oscm.internal.vo.VOUserDetails;

public class UserMapperTest {
    

    @Test
    public void testMapGenderToSalutationMale() {
        // given
        String gender = "male";
        Salutation expected = Salutation.MR;

        // when
        Salutation result = UserMapper.mapGenderToSalutation(gender);

        // then
        assertEquals(expected, result);
    }

    @Test
    public void testMapGenderToSalutationFemale() {
        // given
        String gender = "female";
        Salutation expected = Salutation.MS;

        // when
        Salutation result = UserMapper.mapGenderToSalutation(gender);

        // then
        assertEquals(expected, result);
    }

    @Test
    public void testMapGenderToSalutationUnknown() {
        // given
        String gender = "?";
        Salutation expected = Salutation.MS;

        // when
        Salutation result = UserMapper.mapGenderToSalutation(gender);

        // then
        assertEquals(expected, result);
    }

    @Test
    public void testMapGenderToSalutationDefault() {
        // given
        String gender = " ";
        Salutation expected = Salutation.MS;

        // when
        Salutation result = UserMapper.mapGenderToSalutation(gender);

        // then
        assertEquals(expected, result);
    }

    @Test
    public void testMapUserInfoToUserDetailsFirstname() {
        // given
        UserInfo userInfoModel = new UserInfo();
        userInfoModel.setFirstName("first");

        // when
        VOUserDetails result = UserMapper.from(userInfoModel);

        // then
        assertEquals("", "first", result.getFirstName());
    }

    @Test
    public void testMapUserInfoToUserDetailsLastname() {
        // given
        UserInfo userInfoModel = new UserInfo();
        userInfoModel.setLastName("last");

        // when
        VOUserDetails result = UserMapper.from(userInfoModel);

        // then
        assertEquals("", "last", result.getLastName());
    }

    @Test
    public void testMapUserInfoToUserDetailsAdress() {
        // given
        UserInfo userInfoModel = new UserInfo();
        userInfoModel.setAddress("teststreet 123");

        // when
        VOUserDetails result = UserMapper.from(userInfoModel);

        // then
        assertEquals("", "teststreet 123", result.getAddress());
    }

    @Test
    public void testMapUserInfoToUserDetailsEmail() {
        // given
        UserInfo userInfoModel = new UserInfo();
        userInfoModel.setEmail("first.last@test.com");

        // when
        VOUserDetails result = UserMapper.from(userInfoModel);

        // then
        assertEquals("", "first.last@test.com", result.getEMail());
    }

    @Test
    public void testMapUserInfoToUserDetailsSaluatation() {
        // given
        UserInfo userInfoModel = new UserInfo();
        userInfoModel.setGender("male");

        // when
        VOUserDetails result = UserMapper.from(userInfoModel);

        // then
        assertEquals("", Salutation.MR, result.getSalutation());
    }
    
    @Test
    public void testMapUserInfoSetToVO() {
        //given
        UserInfo userInfoModel = new UserInfo();
        userInfoModel.setAddress("teststreet 123");
        userInfoModel.setEmail("first.last@test.com");
        userInfoModel.setUserId("first.last@test.com");
        userInfoModel.setFirstName("test");
        Set<UserInfo> userInfoModels = new HashSet<UserInfo>();
        userInfoModels.add(userInfoModel);
        
        VOUserDetails expectedModel = new VOUserDetails();
        expectedModel.setAddress("teststreet 123");
        expectedModel.setEMail("first.last@test.com");
        expectedModel.setUserId("first.last@test.com");
        expectedModel.setFirstName("test");
        List<VOUserDetails> expected = new ArrayList<VOUserDetails>();
        expected.add(expectedModel);
        
        //when
        List<VOUserDetails> result = (List<VOUserDetails>) UserMapper.fromSet(userInfoModels);
        
        //then
        assertEquals(expected.get(0).getAddress(), result.get(0).getAddress());
        assertEquals(expected.get(0).getEMail(), result.get(0).getEMail());
        assertEquals(expected.get(0).getUserId(), result.get(0).getUserId());
        assertEquals(expected.get(0).getFirstName(), result.get(0).getFirstName());
    }
    
    @Test
    public void testMapUVOSetToUserInfo() {
        //given
        UserInfo userInfoModel = new UserInfo();
        userInfoModel.setAddress("teststreet 123");
        userInfoModel.setEmail("first.last@test.com");
        userInfoModel.setUserId("first.last@test.com");
        userInfoModel.setFirstName("test");
        List<UserInfo> expected = new ArrayList<UserInfo>();
        expected.add(userInfoModel);
        
        VOUserDetails vOModel = new VOUserDetails();
        vOModel.setAddress("teststreet 123");
        vOModel.setEMail("first.last@test.com");
        vOModel.setUserId("first.last@test.com");
        vOModel.setFirstName("test");
        Set<VOUserDetails> vOModels = new HashSet<VOUserDetails>();
        vOModels.add(vOModel);
        
        //when
        List<UserInfo> result = (List<UserInfo>) UserMapper.fromSet(vOModels);
        
        //then
        assertEquals(expected.get(0).getAddress(), result.get(0).getAddress());
        assertEquals(expected.get(0).getEmail(), result.get(0).getEmail());
        assertEquals(expected.get(0).getUserId(), result.get(0).getUserId());
        assertEquals(expected.get(0).getFirstName(), result.get(0).getFirstName());
    }
}
