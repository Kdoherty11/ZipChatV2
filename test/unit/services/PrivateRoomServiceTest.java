package unit.services;

import daos.PrivateRoomDao;
import daos.RequestDao;
import factories.PrivateRoomFactory;
import models.entities.PrivateRoom;
import models.entities.Request;
import models.entities.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import services.PrivateRoomService;
import services.UserService;
import services.impl.PrivateRoomServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.fest.assertions.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Created by kdoherty on 7/6/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class PrivateRoomServiceTest {

    private PrivateRoomService privateRoomService;

    private PrivateRoomFactory privateRoomFactory;

    @Mock
    private PrivateRoomDao privateRoomDao;

    @Mock
    private RequestDao requestDao;

    @Mock
    private UserService userService;

    @Before
    public void setUp() {
        privateRoomService = spy(new PrivateRoomServiceImpl(privateRoomDao, requestDao, userService));
        privateRoomFactory = new PrivateRoomFactory();
    }

    @Test
    public void removeUserNotInRoom() throws InstantiationException, IllegalAccessException {
        User mockSender = mock(User.class);
        User mockReceiver = mock(User.class);
        when(mockSender.userId).thenReturn(1l);
        when(mockReceiver.userId).thenReturn(2l);

        Request request = new Request(mockSender, mockReceiver);
        PrivateRoom privateRoom = new PrivateRoom(request);

        boolean removed = privateRoomService.removeUser(privateRoom, 3l);

        assertEquals(removed).isFalse();
        assertEquals(privateRoom.senderInRoom).isTrue();
        assertEquals(privateRoom.receiverInRoom).isTrue();
        verifyZeroInteractions(requestDao, privateRoomDao);
        assertEquals(privateRoom.request).isNotNull();
    }

    @Test
    public void removeSender() {
        User mockSender = mock(User.class);
        User mockReceiver = mock(User.class);
        long senderId = 1;
        long receiverId = 2;
        when(mockSender.userId).thenReturn(senderId);
        when(mockReceiver.userId).thenReturn(receiverId);

        Request request = new Request(mockSender, mockReceiver);
        PrivateRoom privateRoom = new PrivateRoom(request);

        boolean removed = privateRoomService.removeUser(privateRoom, senderId);

        assertEquals(removed).isTrue();
        assertEquals(privateRoom.senderInRoom).isFalse();
        assertEquals(privateRoom.receiverInRoom).isTrue();
        verifyZeroInteractions(privateRoomDao);
        verify(requestDao).remove(request);
        assertEquals(privateRoom.request).isNull();
    }

    @Test
    public void removeReceiver() {
        User mockSender = mock(User.class);
        User mockReceiver = mock(User.class);
        long senderId = 1;
        long receiverId = 2;
        when(mockSender.userId).thenReturn(senderId);
        when(mockReceiver.userId).thenReturn(receiverId);

        Request request = new Request(mockSender, mockReceiver);
        PrivateRoom privateRoom = new PrivateRoom(request);

        boolean removed = privateRoomService.removeUser(privateRoom, receiverId);

        assertEquals(removed).isTrue();
        assertEquals(privateRoom.senderInRoom).isTrue();
        assertEquals(privateRoom.receiverInRoom).isFalse();
        verifyZeroInteractions(privateRoomDao);
        verify(requestDao).remove(request);
        assertEquals(privateRoom.request).isNull();
    }

    @Test
    public void removeSenderReceiverAlreadyLeft() {
        User mockSender = mock(User.class);
        User mockReceiver = mock(User.class);
        long senderId = 1;
        long receiverId = 2;
        when(mockSender.userId).thenReturn(senderId);
        when(mockReceiver.userId).thenReturn(receiverId);

        Request request = new Request(mockSender, mockReceiver);
        PrivateRoom privateRoom = new PrivateRoom(request);
        privateRoomService.removeUser(privateRoom, receiverId);

        boolean removed = privateRoomService.removeUser(privateRoom, senderId);

        assertEquals(removed).isTrue();
        verify(privateRoomDao).remove(privateRoom);
        verify(requestDao).remove(request);
    }

    @Test
    public void removeReceiverSenderAlreadyLeft() {
        User mockSender = mock(User.class);
        User mockReceiver = mock(User.class);
        long senderId = 1;
        long receiverId = 2;
        when(mockSender.userId).thenReturn(senderId);
        when(mockReceiver.userId).thenReturn(receiverId);

        Request request = new Request(mockSender, mockReceiver);
        PrivateRoom privateRoom = new PrivateRoom(request);
        privateRoomService.removeUser(privateRoom, senderId);
        boolean removed = privateRoomService.removeUser(privateRoom, receiverId);

        assertEquals(removed).isTrue();
        verify(privateRoomDao).remove(privateRoom);
        verify(requestDao).remove(request);
    }

    @Test
    public void isUserInRoomSenderTrue() {
        User sender = mock(User.class);
        User receiver = mock(User.class);
        long senderId = 1;
        long receiverId = 2;
        when(sender.userId).thenReturn(senderId);
        when(receiver.userId).thenReturn(receiverId);
        Request request = new Request(sender, receiver);
        PrivateRoom room = new PrivateRoom(request);

        boolean isUserInRoom = privateRoomService.isUserInRoom(room, senderId);
        assertEquals(isUserInRoom).isTrue();
    }

    @Test
    public void isUserInRoomReceiverTrue() {
        User sender = mock(User.class);
        User receiver = mock(User.class);
        long senderId = 1;
        long receiverId = 2;
        when(sender.userId).thenReturn(senderId);
        when(receiver.userId).thenReturn(receiverId);
        Request request = new Request(sender, receiver);
        PrivateRoom room = new PrivateRoom(request);

        boolean isUserInRoom = privateRoomService.isUserInRoom(room, receiverId);
        assertEquals(isUserInRoom).isTrue();
    }

    @Test
    public void isUserInRoomReceiverFalse() {
        User sender = mock(User.class);
        User receiver = mock(User.class);
        long senderId = 1;
        long receiverId = 2;
        when(sender.userId).thenReturn(senderId);
        when(receiver.userId).thenReturn(receiverId);
        Request request = new Request(sender, receiver);
        PrivateRoom room = new PrivateRoom(request);
        room.receiverInRoom = false;

        boolean isUserInRoom = privateRoomService.isUserInRoom(room, receiverId);
        assertEquals(isUserInRoom).isFalse();
    }

    @Test
    public void isUserInRoomSenderFalse() {
        User sender = mock(User.class);
        User receiver = mock(User.class);
        long senderId = 1;
        long receiverId = 2;
        when(sender.userId).thenReturn(senderId);
        when(receiver.userId).thenReturn(receiverId);
        Request request = new Request(sender, receiver);
        PrivateRoom room = new PrivateRoom(request);
        room.senderInRoom = false;

        boolean isUserInRoom = privateRoomService.isUserInRoom(room, senderId);
        assertEquals(isUserInRoom).isFalse();
    }

    @Test
    public void findByUserId() {
        long userId = 1;
        List<PrivateRoom> expected = new ArrayList<>();
        when(privateRoomDao.findByUserId(userId)).thenReturn(expected);
        List<PrivateRoom> actual = privateRoomService.findByUserId(userId);
        assertEquals(actual == expected).isTrue();
    }

    @Test
    public void findBySenderAndReceiver() {
        long senderId = 1;
        long receiverId = 2;
        Optional<PrivateRoom> expected = Optional.empty();

        when(privateRoomDao.findBySenderAndReceiver(senderId, receiverId)).thenReturn(expected);

        Optional<PrivateRoom> actual = privateRoomService.findBySenderAndReceiver(senderId, receiverId);

        assertEquals(actual == expected).isTrue();
    }
}
