package com.group10.koiauction.authen;

import static org.mockito.Mockito.*;

import com.group10.koiauction.entity.Account;
import com.group10.koiauction.entity.enums.AccountRoleEnum;
import com.group10.koiauction.entity.enums.AccountStatusEnum;
import com.group10.koiauction.exception.DuplicatedEntity;
import com.group10.koiauction.mapper.AccountMapper;
import com.group10.koiauction.model.request.LoginAccountRequest;
import com.group10.koiauction.model.request.RegisterAccountRequest;
import com.group10.koiauction.model.response.AccountResponse;
import com.group10.koiauction.model.response.EmailDetail;
import com.group10.koiauction.repository.AccountRepository;
import com.group10.koiauction.service.AuthenticationService;
import com.group10.koiauction.service.EmailService;
import com.group10.koiauction.service.TokenService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class AccountServiceTest {
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ModelMapper modelMapper;


    @Mock
    private TokenService tokenService;


    @Mock
    private AccountMapper   accountMapper;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthenticationService authenticationService;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void register_Success() {
        // Set up test input (with valid, non-duplicated data)
        RegisterAccountRequest request = new RegisterAccountRequest();
        request.setRoleEnum("member");
        request.setPassword("password");
        request.setPhoneNumber("123456789");
        request.setEmail("namntse1702390000@fpt.edu.vn"); // Ensure the email is not duplicated
        request.setUsername("testUser");

        // Prepare the Account object
        Account newAccount = new Account();
        newAccount.setPhoneNumber(request.getPhoneNumber());
        newAccount.setEmail(request.getEmail());
        newAccount.setUsername(request.getUsername());

        AccountResponse expectedResponse = new AccountResponse();

        // Mock the behavior of modelMapper, passwordEncoder, and accountRepository
        when(modelMapper.map(request, Account.class)).thenReturn(newAccount);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(accountRepository.save(any(Account.class))).thenReturn(newAccount); // Successful save
        when(modelMapper.map(newAccount, AccountResponse.class)).thenReturn(expectedResponse);

        // Execute the registration method
        AccountResponse response = authenticationService.register(request);

        // Assert the result is not null
        Assert.assertNotNull(response);
        // Verify that accountRepository.save() was called exactly once
        verify(accountRepository, times(1)).save(newAccount);
        // Verify that emailService.sentEmail() was called exactly once
        verify(emailService, times(1)).sentEmail(any(EmailDetail.class));
    }


    @Test
    public void register_DuplicatedEmail_throwsDuplicatedEntity() {
        // Set up test input with a duplicated email
        RegisterAccountRequest request = new RegisterAccountRequest();
        request.setRoleEnum("member");
        request.setPassword("password");
        request.setPhoneNumber("123456789");
        request.setEmail("duplicated_email@domain.com"); // Simulate a duplicated email
        request.setUsername("testUser");

        // Prepare the Account object
        Account newAccount = new Account();
        newAccount.setPhoneNumber(request.getPhoneNumber());
        newAccount.setEmail(request.getEmail());
        newAccount.setUsername(request.getUsername());

        // Mock the behavior of modelMapper and passwordEncoder
        when(modelMapper.map(request, Account.class)).thenReturn(newAccount);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");

        // Simulate a duplicated email exception from the repository
        when(accountRepository.save(any(Account.class)))
                .thenThrow(new RuntimeException(request.getEmail()));

        try {
            // Execute the register method
            authenticationService.register(request);
            Assert.fail("Expected DuplicatedEntity exception to be thrown");
        } catch (DuplicatedEntity e) {
            // Verify that the exception message matches what we expect
            Assert.assertEquals("Duplicated  email ", e.getMessage());
        }

        // Verify that accountRepository.save() was called but threw an exception
        verify(accountRepository, times(1)).save(newAccount);
        // Verify that emailService.sentEmail() was never called due to failure
        verify(emailService, times(0)).sentEmail(any(EmailDetail.class));
    }



    @Test
    public void register_DuplicatedPhoneNumber_ThrowsException() {
        // Set up test input with a duplicated phone number
        RegisterAccountRequest request = new RegisterAccountRequest();
        request.setRoleEnum("member");
        request.setPassword("password");
        request.setPhoneNumber("123456789"); // Simulate a duplicated phone number
        request.setEmail("test@domain.com");
        request.setUsername("testUser");

        // Prepare the Account object
        Account newAccount = new Account();
        newAccount.setPhoneNumber(request.getPhoneNumber());
        newAccount.setEmail(request.getEmail());
        newAccount.setUsername(request.getUsername());

        // Mock the behavior of modelMapper and passwordEncoder
        when(modelMapper.map(request, Account.class)).thenReturn(newAccount);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");

        // Simulate a duplicated phone number exception from the repository
        when(accountRepository.save(any(Account.class)))
                .thenThrow(new RuntimeException(request.getPhoneNumber())); // Simulate exception with phone number

        try {
            // Execute the register method
            authenticationService.register(request);
            Assert.fail("Expected DuplicatedEntity exception to be thrown due to duplicated phone number");
        } catch (DuplicatedEntity e) {
            // Verify that the exception message matches what we expect
            Assert.assertEquals("Duplicated phone", e.getMessage());
        }

        // Verify that accountRepository.save() was called but threw an exception
        verify(accountRepository, times(1)).save(newAccount);
        // Verify that emailService.sentEmail() was never called due to failure
        verify(emailService, times(0)).sentEmail(any(EmailDetail.class));
    }


    @Test
    public void testLoginSuccess() {
        // Arrange: Set up the login request
        LoginAccountRequest loginRequest = new LoginAccountRequest();
        loginRequest.setUsername("manager1");
        loginRequest.setPassword("manager1");

        // Create a mock Account object to simulate database retrieval
        Account mockAccount = new Account();
        mockAccount.setUsername("manager1");
        mockAccount.setPassword("$10$yecSolxPBCde2BGqT1kfKOljY3DOhIQZYdHYLOFK4s6jXWRd5WRJi");
        mockAccount.setStatus(AccountStatusEnum.ACTIVE);

        // Create an expected AccountResponse
        AccountResponse expectedResponse = new AccountResponse();
        expectedResponse.setUsername("manager1");
        expectedResponse.setToken("mocked-token");

        // Mock the behavior of accountRepository to return the mock account
        when(accountRepository.findByUsername("manager1")).thenReturn(mockAccount);

        // Mock the behavior of accountMapper to map Account to AccountResponse
        when(accountMapper.toAccountResponse(mockAccount)).thenReturn(expectedResponse);

        // Mock the behavior of authenticationManager and tokenService
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(mockAccount);
        when(tokenService.generateToken(mockAccount)).thenReturn("mocked-token");

        // Act: Call the login method
        AccountResponse actualResponse = authenticationService.login(loginRequest);

        // Assert: Validate that the expected and actual responses match
        assertEquals(actualResponse, expectedResponse);

        // Verify that accountRepository.findByUsername was called once
        verify(accountRepository, times(1)).findByUsername("manager1");
    }



}
