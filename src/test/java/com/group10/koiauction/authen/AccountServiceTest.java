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

    @InjectMocks
    private AuthenticationService authenticationService;

    private RegisterAccountRequest registerAccountRequest;
    private Account account;
    private AccountResponse accountResponse;

    @BeforeMethod
    public void setup() {
        // Initialize mocks
        MockitoAnnotations.openMocks(this);

        // Sample RegisterAccountRequest setup
        registerAccountRequest = new RegisterAccountRequest();
        registerAccountRequest.setUsername("testUser");
        registerAccountRequest.setEmail("test@example.com");
        registerAccountRequest.setPhoneNumber("0829017282");
        registerAccountRequest.setPassword("password123");
        registerAccountRequest.setRoleEnum("MEMBER");

        // Mock Account entity
        account = new Account();
        account.setUsername(registerAccountRequest.getUsername());
        account.setEmail(registerAccountRequest.getEmail());

        // Mock AccountResponse
        accountResponse = new AccountResponse();
        accountResponse.setUsername("testUser");
    }

    @Test
    public void testRegister_successfulRegistration() {
        // Mock the mapping from RegisterAccountRequest to Account
        when(modelMapper.map(registerAccountRequest, Account.class)).thenReturn(account);
        // Mock password encoding
        when(passwordEncoder.encode(registerAccountRequest.getPassword())).thenReturn("encodedPassword");
        // Mock saving the account
        when(accountRepository.save(account)).thenReturn(account);
        // Mock mapping from Account to AccountResponse
        when(modelMapper.map(account, AccountResponse.class)).thenReturn(accountResponse);

        // Call the method under test
        AccountResponse response = authenticationService.register(registerAccountRequest);

        // Verify interactions
        verify(passwordEncoder).encode(registerAccountRequest.getPassword());
        verify(accountRepository).save(account);


        assertEquals(response.getUsername(), "testUser");
    }

    @Test(expectedExceptions = DuplicatedEntity.class)
    public void testRegister_duplicatedPhone() {
        // Mock the exception thrown when saving the account
        when(accountRepository.save(any(Account.class)))
                .thenThrow(new RuntimeException(registerAccountRequest.getPhoneNumber()));

        // Call the method under test and expect the exception
        authenticationService.register(registerAccountRequest);
    }

    @Test(expectedExceptions = DuplicatedEntity.class)
    public void testRegister_duplicatedEmail() {
        // Mock the exception thrown when saving the account
        when(accountRepository.save(any(Account.class)))
                .thenThrow(new RuntimeException(registerAccountRequest.getEmail()));

        // Call the method under test and expect the exception
        authenticationService.register(registerAccountRequest);
    }

    @Test(expectedExceptions = DuplicatedEntity.class)
    public void testRegister_duplicatedUsername() {
        // Mock the exception thrown when saving the account
        when(accountRepository.save(any(Account.class)))
                .thenThrow(new RuntimeException(registerAccountRequest.getUsername()));

        // Call the method under test and expect the exception
        authenticationService.register(registerAccountRequest);
    }



}
