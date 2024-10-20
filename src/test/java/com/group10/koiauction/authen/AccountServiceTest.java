package com.group10.koiauction.authen;

import static org.mockito.Mockito.*;

import com.group10.koiauction.entity.Account;
import com.group10.koiauction.entity.enums.AccountRoleEnum;
import com.group10.koiauction.entity.enums.AccountStatusEnum;
import com.group10.koiauction.exception.DuplicatedEntity;
import com.group10.koiauction.exception.EntityNotFoundException;
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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Optional;

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
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenService tokenService;

    @Mock
    private AccountMapper accountMapper;

    @InjectMocks
    private AuthenticationService authenticationService;

    private RegisterAccountRequest registerAccountRequest;
    private Account account;
    private AccountResponse accountResponse;

    private LoginAccountRequest loginAccountRequest;
    private Authentication authentication;

    @BeforeMethod
    public void setup() {
        MockitoAnnotations.openMocks(this);

        // Tạo RegisterAccountRequest mẫu
        registerAccountRequest = new RegisterAccountRequest();
        registerAccountRequest.setUsername("testUser");
        registerAccountRequest.setEmail("namntse170239@fpt.edu.vn");
        registerAccountRequest.setPhoneNumber("0829017284");
        registerAccountRequest.setPassword("password123");
        registerAccountRequest.setRoleEnum("MEMBER");

        // Tạo đối tượng Account mô phỏng
        account = new Account();
        account.setUsername(registerAccountRequest.getUsername());
        account.setEmail(registerAccountRequest.getEmail());
        account.setPhoneNumber(registerAccountRequest.getPhoneNumber());
        account.setRoleEnum(AccountRoleEnum.MEMBER);
        account.setStatus(AccountStatusEnum.ACTIVE);

        // Mô phỏng hành vi của ModelMapper để trả về đối tượng Account hợp lệ
        when(modelMapper.map(registerAccountRequest, Account.class)).thenReturn(account);

        // Tạo AccountResponse mô phỏng
        accountResponse = new AccountResponse();
        accountResponse.setUsername(account.getUsername());
        accountResponse.setEmail(account.getEmail());
        accountResponse.setPhoneNumber(account.getPhoneNumber());
        accountResponse.setRoleEnum(account.getRoleEnum());

        // Tạo LoginAccountRequest mẫu
        loginAccountRequest = new LoginAccountRequest();
        loginAccountRequest.setUsername("namnt");
        loginAccountRequest.setPassword("11111111");

        // Mô phỏng Authentication
        authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(account);
    }

    @Test
    public void testRegister_successfulRegistration() {
        // Mô phỏng ánh xạ từ RegisterAccountRequest sang Account
        when(modelMapper.map(registerAccountRequest, Account.class)).thenReturn(account);
        // Mô phỏng mã hóa mật khẩu
        when(passwordEncoder.encode(registerAccountRequest.getPassword())).thenReturn("encodedPassword");
        // Mô phỏng lưu tài khoản
        when(accountRepository.save(account)).thenReturn(account);
        // Mô phỏng ánh xạ từ Account sang AccountResponse
        when(modelMapper.map(account, AccountResponse.class)).thenReturn(accountResponse);

        // Gọi phương thức cần kiểm thử
        AccountResponse response = authenticationService.register(registerAccountRequest);

        // Kiểm tra tương tác
        verify(passwordEncoder).encode(registerAccountRequest.getPassword());
        verify(accountRepository).save(account);

        // Kiểm tra kết quả
        assertEquals(response.getUsername(), "testUser");
        assertEquals(response.getEmail(), "namntse170239@fpt.edu.vn");
        assertEquals(response.getPhoneNumber(), "0829017289");
        assertEquals(response.getRoleEnum(), AccountRoleEnum.MEMBER);
    }

    @Test(expectedExceptions = DuplicatedEntity.class)
    public void testRegister_duplicatedPhone() {
        // Giả lập rằng số điện thoại đã tồn tại trong cơ sở dữ liệu
        when(accountRepository.findAccountByPhoneNumber(registerAccountRequest.getPhoneNumber()))
                .thenReturn(Optional.of(account)); // Trả về Optional.of(account) để mô phỏng số điện thoại đã tồn tại

        // Gọi phương thức register và kiểm tra xem ngoại lệ có được ném ra không
        authenticationService.register(registerAccountRequest);
    }

    @Test
    public void testRegister_newPhone() {
        // Giả lập rằng số điện thoại chưa tồn tại trong cơ sở dữ liệu
        when(accountRepository.findAccountByPhoneNumber(registerAccountRequest.getPhoneNumber()))
                .thenReturn(Optional.empty());

        // Mô phỏng việc ánh xạ từ RegisterAccountRequest sang Account
        when(modelMapper.map(registerAccountRequest, Account.class)).thenReturn(account);

        // Mô phỏng mã hóa mật khẩu
        when(passwordEncoder.encode(registerAccountRequest.getPassword())).thenReturn("encodedPassword");

        // Mô phỏng việc lưu tài khoản và trả về đối tượng Account hợp lệ
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        // Mô phỏng việc ánh xạ từ Account sang AccountResponse
        when(modelMapper.map(account, AccountResponse.class)).thenReturn(accountResponse);

        // Gọi phương thức register và kiểm tra không ném ra ngoại lệ
        AccountResponse response = authenticationService.register(registerAccountRequest);

        // Kiểm tra xem tài khoản có được tạo thành công không
        assertNotNull(response);
        assertEquals(response.getPhoneNumber(), registerAccountRequest.getPhoneNumber());
    }

    @Test(expectedExceptions = DuplicatedEntity.class)
    public void testRegister_duplicatedEmail() {
        // Giả lập rằng email đã tồn tại trong cơ sở dữ liệu
        when(accountRepository.findAccountByEmail(registerAccountRequest.getEmail()))
                .thenReturn(account);

        // Gọi phương thức register và kiểm tra xem ngoại lệ có được ném ra không
        authenticationService.register(registerAccountRequest);
    }

    @Test(expectedExceptions = DuplicatedEntity.class)
    public void testRegister_duplicatedUsername() {
        // Giả lập rằng username đã tồn tại trong cơ sở dữ liệu
        when(accountRepository.findAccountByUsername(registerAccountRequest.getUsername()))
                .thenReturn(account);

        // Gọi phương thức register và kiểm tra xem ngoại lệ có được ném ra không
        authenticationService.register(registerAccountRequest);
    }

    // Thêm các hàm kiểm thử cho phương thức login

    @Test
    public void testLogin_successful() {
        // Mô phỏng xác thực thành công
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        // Mô phỏng chuyển đổi từ Account sang AccountResponse
        when(accountMapper.toAccountResponse(account)).thenReturn(accountResponse);
        // Mô phỏng tạo token
        when(tokenService.generateToken(account)).thenReturn("sampleToken");

        // Gọi phương thức login
        AccountResponse response = authenticationService.login(loginAccountRequest);

        // Kiểm tra kết quả
        assertNotNull(response);
        assertEquals(response.getUsername(), "testUser");
        assertEquals(response.getToken(), "sampleToken");

        // Xác minh tương tác
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenService).generateToken(account);
    }

    @Test(expectedExceptions = EntityNotFoundException.class)
    public void testLogin_inactiveAccount() {
        // Đặt trạng thái account là INACTIVE
        account.setStatus(AccountStatusEnum.INACTIVE);

        // Mô phỏng xác thực thành công
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        // Gọi phương thức login và kiểm tra xem ngoại lệ có được ném ra không
        authenticationService.login(loginAccountRequest);
    }

    @Test(expectedExceptions = EntityNotFoundException.class)
    public void testLogin_invalidCredentials() {
        // Mô phỏng xác thực thất bại (thông tin đăng nhập không chính xác)
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Gọi phương thức login và kiểm tra xem ngoại lệ có được ném ra không
        authenticationService.login(loginAccountRequest);
    }
}
