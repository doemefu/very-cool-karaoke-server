package ch.uzh.ifi.hase.soprafs26.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private UserService userService;

	private User testUser;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);

		// given
		testUser = new User();
		testUser.setId(1L);
		testUser.setUsername("testUsername");
        testUser.setPassword("testPassword");

		// when -> any object is being save in the userRepository -> return the dummy
		// testUser
		Mockito.when(userRepository.save(Mockito.any())).thenReturn(testUser);
	}

	@Test
	public void createUser_validInputs_success() {
		// when -> any object is being save in the userRepository -> return the dummy
		// testUser
		User createdUser = userService.createUser(testUser);

		// then
		Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any());

		assertEquals(testUser.getId(), createdUser.getId());
		assertEquals(testUser.getUsername(), createdUser.getUsername());
		assertNotNull(createdUser.getToken());
	}

	@Test
	public void createUser_duplicateName_throwsException() {
		// given -> a first user has already been created
		userService.createUser(testUser);

		// when -> setup additional mocks for UserRepository
		Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(testUser);

		// then -> attempt to create second user with same user -> check that an error
		// is thrown
		assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser));
	}

	@Test
	public void createUser_duplicateInputs_throwsException() {
		// given -> a first user has already been created
		userService.createUser(testUser);

		// when -> setup additional mocks for UserRepository
		Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(testUser);

		// then -> attempt to create second user with same user -> check that an error
		// is thrown
		assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser));
	}

    @Test
    public void changePassword_success() {
        testUser.setToken("valid-token");
        testUser.setPassword(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("oldPass"));

        Mockito.when(userRepository.findByToken("valid-token")).thenReturn(testUser);

        userService.changePassword(1L, "valid-token", "oldPass", "newPass");

        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any());
    }

    @Test
    public void changePassword_wrongCurrentPassword_throws403() {
        testUser.setToken("valid-token");
        testUser.setPassword(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("oldPass"));

        Mockito.when(userRepository.findByToken("valid-token")).thenReturn(testUser);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                userService.changePassword(1L, "valid-token", "wrongPass", "newPass"));
        assertEquals(403, ex.getStatusCode().value());
    }

    @Test
    public void changePassword_wrongUser_throws403() {
        testUser.setToken("valid-token");

        Mockito.when(userRepository.findByToken("valid-token")).thenReturn(testUser);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                userService.changePassword(999L, "valid-token", "oldPass", "newPass"));
        assertEquals(403, ex.getStatusCode().value());
    }

    @Test
    public void changePassword_invalidToken_throws401() {
        Mockito.when(userRepository.findByToken("bad-token")).thenReturn(null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                userService.changePassword(1L, "bad-token", "oldPass", "newPass"));
        assertEquals(401, ex.getStatusCode().value());
    }

}
