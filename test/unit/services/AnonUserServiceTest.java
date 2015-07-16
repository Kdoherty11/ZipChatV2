package unit.services;

import daos.AnonUserDao;
import factories.AnonUserFactory;
import factories.PublicRoomFactory;
import factories.UserFactory;
import models.entities.AnonUser;
import models.entities.PublicRoom;
import models.entities.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import services.AnonUserService;
import services.impl.AnonUserServiceImpl;
import utils.TestUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.fest.assertions.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Created by kdoherty on 7/7/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class AnonUserServiceTest {

    private AnonUserService anonUserService;

    @Mock
    private AnonUserDao anonUserDao;

    private PublicRoomFactory publicRoomFactory;
    private UserFactory userFactory;
    private AnonUserFactory anonUserFactory;


    @Before
    public void setUp() {
        anonUserService = spy(new AnonUserServiceImpl(anonUserDao));
        publicRoomFactory = new PublicRoomFactory();
        userFactory = new UserFactory();
        anonUserFactory = new AnonUserFactory();
    }

    @Test
    public void getOrCreateAnonUserUsesExistingAnonUserIfExists() {
        User mockActual = mock(User.class);
        PublicRoom mockRoom = mock(PublicRoom.class);
        when(anonUserService.getAnonUser(mockActual, mockRoom)).thenReturn(Optional.empty());
        AnonUser mockAnonUser = mock(AnonUser.class);
        Optional<AnonUser> existing = Optional.of(mockAnonUser);
        when(anonUserService.getAnonUser(mockActual, mockRoom)).thenReturn(existing);

        AnonUser result = anonUserService.getOrCreateAnonUser(mockActual, mockRoom);

        assertEquals(result).isEqualTo(existing.get());
    }

    @Test
    public void getOrCreateAnonUserCreatesANewAnonUserIfNoneExist() {
        User mockActual = mock(User.class);
        PublicRoom mockRoom = mock(PublicRoom.class);
        when(anonUserService.getAnonUser(mockActual, mockRoom)).thenReturn(Optional.empty());
        Optional<AnonUser> existing = Optional.empty();
        when(anonUserService.getAnonUser(mockActual, mockRoom)).thenReturn(existing);

        AnonUser result = anonUserService.getOrCreateAnonUser(mockActual, mockRoom);

        assertEquals(result).isNotNull();
        verify(anonUserDao).save(any(AnonUser.class));
    }

    @Test
    public void getOrCreateUserCreatesAnAnonUserWithAnUnusedAlias() throws InstantiationException, IllegalAccessException, NoSuchFieldException {
        PublicRoom room = publicRoomFactory.create();
        User user = userFactory.create();
        when(anonUserService.getAnonUser(user, room)).thenReturn(Optional.empty());

        @SuppressWarnings("unchecked") Set<String> fullNames =
                (Set<String>) TestUtils.getPrivateStaticField(AnonUserServiceImpl.class, "FULL_NAMES");
        int numPossibleAliasNames = fullNames.size();
        int numCreatedAnonUsers = numPossibleAliasNames - 1;
        List<AnonUser> anonUsers = anonUserFactory.createList(numCreatedAnonUsers);
        Set<String> usedAliases = new HashSet<>();
        int anonUsersIndex = 0;
        for (String alias : fullNames) {
            if (numCreatedAnonUsers == anonUsersIndex) {
                break;
            }
            anonUsers.get(anonUsersIndex++).name = alias;
            usedAliases.add(alias);
        }
        room.anonUsers = anonUsers;

        AnonUser anonUser = anonUserService.getOrCreateAnonUser(user, room);

        assertEquals(usedAliases.contains(anonUser.name)).isFalse();
    }

    @Test
    public void getOrCreateAnonUsersThrowsAnExceptionIfNoMoreAliasesExist() throws InstantiationException, IllegalAccessException, NoSuchFieldException {
        PublicRoom room = publicRoomFactory.create();
        User user = userFactory.create();
        when(anonUserService.getAnonUser(user, room)).thenReturn(Optional.empty());

        @SuppressWarnings("unchecked") Set<String> fullNames =
                (Set<String>) TestUtils.getPrivateStaticField(AnonUserServiceImpl.class, "FULL_NAMES");
        int numPossibleAliasNames = fullNames.size();
        List<AnonUser> anonUsers = anonUserFactory.createList(numPossibleAliasNames);
        int anonUsersIndex = 0;
        for (String alias : fullNames) {
            anonUsers.get(anonUsersIndex++).name = alias;
        }
        room.anonUsers = anonUsers;

        boolean illegalStateExceptionThrown = false;
        try {
            anonUserService.getOrCreateAnonUser(user, room);
        } catch (IllegalStateException e) {
            assertEquals(e.getMessage()).contains("There are no more available aliases");
            illegalStateExceptionThrown = true;
        }

        assertEquals(illegalStateExceptionThrown).isTrue();
    }



}
