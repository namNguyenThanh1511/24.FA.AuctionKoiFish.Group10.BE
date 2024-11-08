package com.group10.koiauction.service;


import com.group10.koiauction.constant.MappingURL;
import com.group10.koiauction.entity.Account;
import com.group10.koiauction.entity.enums.AccountRoleEnum;
import com.group10.koiauction.entity.enums.AccountStatusEnum;
import com.group10.koiauction.mapper.AccountMapper;
import com.group10.koiauction.model.request.CreateBreederAccountRequest;
import com.group10.koiauction.model.request.CreateStaffAccountRequest;
import com.group10.koiauction.model.request.LoginAccountRequest;
import com.group10.koiauction.model.request.RegisterAccountRequest;
import com.group10.koiauction.exception.DuplicatedEntity;
import com.group10.koiauction.exception.EntityNotFoundException;
import com.group10.koiauction.model.request.RegisterMemberRequest;
import com.group10.koiauction.model.request.UpdateProfileRequestDTO;
import com.group10.koiauction.model.request.ResetPasswordRequestDTO;
import com.group10.koiauction.model.response.*;
import com.group10.koiauction.repository.AccountRepository;
import com.group10.koiauction.utilities.AccountUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class AuthenticationService implements UserDetailsService {
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    AccountMapper accountMapper;

    @Autowired
    TokenService tokenService;

    @Autowired
    EmailService emailService;

    @Autowired
    AccountUtils accountUtils;


    public AccountResponse register(RegisterAccountRequest registerAccountRequest) {
//        if (accountRepository.findAccountByPhoneNumber(registerAccountRequest.getPhoneNumber()).isPresent()) {
//            throw new DuplicatedEntity("Duplicated phone");
//        } else if(accountRepository.findAccountByUsername(registerAccountRequest.getUsername())== null) {
//            throw new DuplicatedEntity("Duplicated username");
//        } else if (accountRepository.findAccountByEmail(registerAccountRequest.getEmail())== null) {
//            throw new DuplicatedEntity("Duplicated email");
//        }
        Account newAccount = new Account();
        newAccount = modelMapper.map(registerAccountRequest, Account.class);
        String trimmedUsername = registerAccountRequest.getUsername().trim();
        String trimmedPassword = registerAccountRequest.getPassword().trim();
        try {
            newAccount.setUsername(trimmedUsername);
            newAccount.setRoleEnum(getRoleEnumX(registerAccountRequest.getRoleEnum()));
            newAccount.setPassword(passwordEncoder.encode(trimmedPassword));
            newAccount = accountRepository.save(newAccount);
            EmailDetail emailDetail = new EmailDetail();
            emailDetail.setAccount(newAccount);
            emailDetail.setSubject("Welcome to my web");
            Runnable runnable = () -> {
                emailDetail.setLink(MappingURL.BASE_URL_LOCAL);
                emailService.sentEmail(emailDetail);
            };
            new Thread(runnable).start();
            return modelMapper.map(newAccount, AccountResponse.class);
        } catch (Exception e) {
            if (e.getMessage().contains(registerAccountRequest.getPhoneNumber())) {
                throw new DuplicatedEntity("Duplicated phone");
            } else if (e.getMessage().contains(registerAccountRequest.getEmail())) {
                throw new DuplicatedEntity("Duplicated  email ");
            } else if (e.getMessage().contains(registerAccountRequest.getUsername())) {
                throw new DuplicatedEntity("username  exist");
            }
            throw e;
        }
    }
    public AccountResponse registerMember(RegisterMemberRequest registerAccountRequest) {
        Account newAccount = modelMapper.map(registerAccountRequest, Account.class);// Account.class : tự động new Account() rồi mapping
        String trimmedUsername = registerAccountRequest.getUsername().trim();
        String trimmedPassword = registerAccountRequest.getPassword().trim();
        try {
            newAccount.setUsername(trimmedUsername);
            newAccount.setRoleEnum(AccountRoleEnum.MEMBER);
            newAccount.setPassword(passwordEncoder.encode(trimmedPassword));
            accountRepository.save(newAccount);
            EmailDetail emailDetail = new EmailDetail();
            emailDetail.setAccount(newAccount);
            emailDetail.setSubject("Welcome to my web");
            Runnable runnable = () -> {
                emailDetail.setLink(MappingURL.BASE_URL_LOCAL);
                emailService.sentEmail(emailDetail);
            };
            new Thread(runnable).start();
            return modelMapper.map(newAccount, AccountResponse.class);
        } catch (Exception e) {
            if (e.getMessage().contains(registerAccountRequest.getPhoneNumber())) {
                throw new DuplicatedEntity("Duplicated phone");
            } else if (e.getMessage().contains(registerAccountRequest.getEmail())) {
                throw new DuplicatedEntity("Duplicated  email ");
            } else if (e.getMessage().contains(registerAccountRequest.getUsername())) {
                throw new DuplicatedEntity("username  exist");
            }
            throw e;
        }
    }

    public AccountResponse login(LoginAccountRequest loginAccountRequest) {

        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    loginAccountRequest.getUsername().trim(), loginAccountRequest.getPassword().trim()
                    // go to loadByUsername check username first
                    // so sanh password db vs request password
            ));
            Account account = (Account) authentication.getPrincipal();
            if (account.getStatus().equals(AccountStatusEnum.INACTIVE)) {
                throw new EntityNotFoundException("Account is inactive");
            }
            AccountResponse accountResponse = accountMapper.toAccountResponse(account);
            accountResponse.setToken(tokenService.generateToken(account));// set token response ve` cho front end
            return accountResponse;
        } catch (EntityNotFoundException e) {
            // Catch inactive account error and re-throw it as is
            throw e;

        } catch (Exception e) {
            throw new EntityNotFoundException("Username or password are incorrect");
        }

    }

    public AccountResponse loginGoogle(String token){
        try{
            AccountResponse accountResponse = new AccountResponse();
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
            String email = decodedToken.getEmail();
            Account account = accountRepository.findAccountByEmail(email);
            if(account == null){
                Account newAccount = new Account();
                newAccount.setEmail(email);
                newAccount.setFirstName(decodedToken.getName());
                newAccount.setLastName("");
                newAccount.setRoleEnum(AccountRoleEnum.MEMBER);
                newAccount.setStatus(AccountStatusEnum.ACTIVE);
                newAccount.setUsername(email);
                newAccount.setBalance(0);
                accountRepository.save(newAccount);
//                return accountMapper.toAccountResponse(newAccount);
            }else{
                accountResponse = accountMapper.toAccountResponse(account);
                accountResponse.setToken(tokenService.generateToken(account));
                return accountResponse;
            }

        }catch (FirebaseAuthException e){
            e.printStackTrace();
        }
        return null;
    }

    public String deleteDB(Long id) {
        Account account = accountRepository.findByUser_id(id);
        try {
            accountRepository.delete(account);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return "Delete success";
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    public Account deleteAccount(Long id) {
        Account target = getAccountById(id);
        target.setStatus(AccountStatusEnum.INACTIVE);
        target.setUpdatedDate(new Date());
        return accountRepository.save(target);
    }

    public AccountResponseForManageDTO unlockAccount(Long id) {
        Account target = getAccountById(id);
        target.setStatus(AccountStatusEnum.ACTIVE);
        target.setUpdatedDate(new Date());
        target = accountRepository.save(target);
        AccountResponseForManageDTO accountResponseForManageDTO = accountMapper.toAccountResponseForManageDTO(target);
        accountResponseForManageDTO.setCreatedAt(target.getCreatedDate());
        accountResponseForManageDTO.setUpdatedAt(target.getUpdatedDate());
        return  accountResponseForManageDTO ;
    }
    public Account updateAccount(Long id, RegisterAccountRequest account) {
        try {
            Account target = getAccountById(id);
            target.setUsername(account.getUsername());
            target.setPassword(account.getPassword());
            target.setEmail(account.getEmail());
            target.setFirstName(account.getFirstName());
            target.setLastName(account.getLastName());
            target.setPassword(passwordEncoder.encode(account.getPassword()));
            target.setAddress(account.getAddress());
            target.setRoleEnum(getRoleEnumX(account.getRoleEnum()));
            target.setUpdatedDate(new Date());
            return accountRepository.save(target);
        } catch (Exception e) {
            if (e.getMessage().contains(account.getPhoneNumber())) {
                throw new DuplicatedEntity("Duplicated phone");
            } else if (e.getMessage().contains(account.getEmail())) {
                throw new DuplicatedEntity("Duplicated  email ");
            }
            throw e;
        }

    }
    public AccountResponse getAccountProfile(){
        Account account = accountUtils.getCurrentAccount();
        AccountResponse accountResponse = modelMapper.map(account, AccountResponse.class);
        return accountResponse;
    }

    public AccountResponse updateAccountProfile(Long id, UpdateProfileRequestDTO updateProfileRequestDTO) {
        Account target = getAccountById(id);
        try {
            if (updateProfileRequestDTO.getUsername() != null && !updateProfileRequestDTO.getUsername().equals(target.getUsername()))
            {
                if (accountRepository.existsByUsername(updateProfileRequestDTO.getUsername())) {
                    throw new DuplicatedEntity("username is already been used ");
                }
                target.setUsername(updateProfileRequestDTO.getUsername());
            }
            if (updateProfileRequestDTO.getEmail() != null && !updateProfileRequestDTO.getEmail().equals(target.getEmail())) {
                if (accountRepository.existsByEmail(updateProfileRequestDTO.getEmail())) {
                    throw new DuplicatedEntity("Email already been used");
                }
                target.setEmail(updateProfileRequestDTO.getEmail());
            }
            if (updateProfileRequestDTO.getFirstName() != null) {
                target.setFirstName(updateProfileRequestDTO.getFirstName());
            }
            if (updateProfileRequestDTO.getLastName() != null) {
                target.setLastName(updateProfileRequestDTO.getLastName());
            }
            if (updateProfileRequestDTO.getPhoneNumber() != null && !updateProfileRequestDTO.getPhoneNumber().equals(target.getPhoneNumber())) {
                if (accountRepository.existsByPhoneNumber(updateProfileRequestDTO.getPhoneNumber())) {
                    throw new DuplicatedEntity("Phone already been used");
                }
                target.setPhoneNumber(updateProfileRequestDTO.getPhoneNumber());
            }
            accountRepository.save(target);
        } catch (DuplicatedEntity e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error updating account profile: " + e.getMessage());
        }
        return accountMapper.toAccountResponse(target);
    }

    public AccountResponse updateAccountProfileOfCurrentUser(UpdateProfileRequestDTO updateProfileRequestDTO) {
        Account target = accountUtils.getCurrentAccount();
        try {
            if (updateProfileRequestDTO.getUsername() != null && !updateProfileRequestDTO.getUsername().equals(target.getUsername()))
            {
                if (accountRepository.existsByUsername(updateProfileRequestDTO.getUsername())) {
                    throw new DuplicatedEntity("username is already been used ");
                }
                target.setUsername(updateProfileRequestDTO.getUsername());
            }
            if (updateProfileRequestDTO.getEmail() != null && !updateProfileRequestDTO.getEmail().equals(target.getEmail())) {
                if (accountRepository.existsByEmail(updateProfileRequestDTO.getEmail())) {
                    throw new DuplicatedEntity("Email already been used");
                }
                target.setEmail(updateProfileRequestDTO.getEmail());
            }
            if (updateProfileRequestDTO.getFirstName() != null) {
                target.setFirstName(updateProfileRequestDTO.getFirstName());
            }
            if (updateProfileRequestDTO.getLastName() != null) {
                target.setLastName(updateProfileRequestDTO.getLastName());
            }
            if (updateProfileRequestDTO.getPhoneNumber() != null && !updateProfileRequestDTO.getPhoneNumber().equals(target.getPhoneNumber())) {
                if (accountRepository.existsByPhoneNumber(updateProfileRequestDTO.getPhoneNumber())) {
                    throw new DuplicatedEntity("Phone already been used");
                }
                target.setPhoneNumber(updateProfileRequestDTO.getPhoneNumber());
            }
            target.setAddress(updateProfileRequestDTO.getAddress());
            accountRepository.save(target);
        } catch (DuplicatedEntity e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error updating account profile: " + e.getMessage());
        }
        return accountMapper.toAccountResponse(target);
    }

    public AccountResponse createBreederAccount(CreateBreederAccountRequest createBreederAccountRequest) {
        Account newAccount = modelMapper.map(createBreederAccountRequest, Account.class);
        try {
            // Automatically set the role to KOI_BREEDER for breeder account creation
            newAccount.setRoleEnum(AccountRoleEnum.KOI_BREEDER);

            // Set the default password to "123@abc"
            String defaultPassword = "123@abc";
            newAccount.setPassword(passwordEncoder.encode(defaultPassword));

            accountRepository.save(newAccount);
            EmailDetail emailDetail = new EmailDetail();
            emailDetail.setAccount(newAccount);
            emailDetail.setSubject("Welcome to my web");
            Runnable runnable = () -> {
                emailDetail.setLink(MappingURL.BASE_URL_LOCAL);
                emailService.sentEmailBreeder(emailDetail);
            };
            new Thread(runnable).start();

            return modelMapper.map(newAccount, AccountResponse.class);
        } catch (Exception e) {
            if (e.getMessage().contains(createBreederAccountRequest.getPhoneNumber())) {
                throw new DuplicatedEntity("Duplicated phone");
            } else if (e.getMessage().contains(createBreederAccountRequest.getEmail())) {
                throw new DuplicatedEntity("Duplicated email");
            } else if (e.getMessage().contains(createBreederAccountRequest.getUsername())) {
                throw new DuplicatedEntity("Username exists");
            }
            throw e;
        }
    }

    public AccountResponse createStaffAccount(CreateStaffAccountRequest createStaffAccountRequest) {
        Account newAccount = modelMapper.map(createStaffAccountRequest, Account.class);
        try {
            // Automatically set the role to STAFF for staff account creation
            newAccount.setRoleEnum(AccountRoleEnum.STAFF);

            // Set the default password to "123@abc"
            String defaultPassword = "123@abc";
            newAccount.setPassword(passwordEncoder.encode(defaultPassword));

            accountRepository.save(newAccount);

            EmailDetail emailDetail = new EmailDetail();
            emailDetail.setAccount(newAccount);
            emailDetail.setSubject("Welcome to my web");

            Runnable runnable = () -> {
                emailDetail.setLink(MappingURL.BASE_URL_LOCAL);
                emailService.sentEmailBreeder(emailDetail);
            };
            new Thread(runnable).start();

            return modelMapper.map(newAccount, AccountResponse.class);
        } catch (Exception e) {
            if (e.getMessage().contains(createStaffAccountRequest.getPhoneNumber())) {
                throw new DuplicatedEntity("Duplicated phone");
            } else if (e.getMessage().contains(createStaffAccountRequest.getEmail())) {
                throw new DuplicatedEntity("Duplicated email");
            } else if (e.getMessage().contains(createStaffAccountRequest.getUsername())) {
                throw new DuplicatedEntity("Username exists");
            }
            throw e;
        }
    }

    public void forgotPassword(String email) {
        Account account = accountRepository.findAccountByEmail(email);
        if(account == null) {
            throw new EntityNotFoundException("Account not found");
        }
        String token = tokenService.generateToken(account);
        EmailDetail emailDetail = new EmailDetail();
        emailDetail.setAccount(account);//set receiver
        emailDetail.setSubject("Reset password");
        emailDetail.setLink("http://www.koiauctionsystem.store/reset-password?token="+token);
        emailService.sentEmail(emailDetail);

    }

    public void resetPassword(ResetPasswordRequestDTO resetPasswordRequestDTO) {
        Account account = accountUtils.getCurrentAccount();
        account.setPassword(passwordEncoder.encode(resetPasswordRequestDTO.getPassword()));
        try{
            accountRepository.save(account);
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return accountRepository.findByUsername(username); // find by username config in Account class
    }


    public AccountResponse getAccountResponseById(Long id) {
        Account target = getAccountById(id);
        AccountResponse accountResponse = accountMapper.toAccountResponse(target);
        return accountResponse;
    }
    public Account getAccountById(Long id) {
        Account account = accountRepository.findByUser_id(id);
        if (account == null) {
            throw new EntityNotFoundException("Account with id " + id + " not found");
        }
        return account;
    }

    public List<Account> getAllBreederAccounts() {
    return accountRepository.findAccountsByRoleEnum(AccountRoleEnum.KOI_BREEDER);
    }

    public List<Account> getAllStaffAccounts() {
        return accountRepository.findAccountsByRoleEnum(AccountRoleEnum.STAFF);
    }

    public List<AuctionSessionResponseAccountDTO> getAllStaffAccountsWithShorterResponse() {
        List<Account> accountList = accountRepository.findAccountsByRoleEnum(AccountRoleEnum.STAFF);
        List<AuctionSessionResponseAccountDTO> accountDTOList = new ArrayList<>();
        for (Account account : accountList) {
            AuctionSessionResponseAccountDTO accountDTO = new AuctionSessionResponseAccountDTO();
            accountDTO.setId(account.getUser_id());
            accountDTO.setUsername(account.getUsername());
            accountDTO.setFullName(account.getFirstName() + " " + account.getLastName());
            accountDTOList.add(accountDTO);
        }

        return accountDTOList;
    }

    public List<Account> getAllMemberAccounts() {
        return accountRepository.findAccountsByRoleEnum(AccountRoleEnum.MEMBER);
    }

    public AccountResponseForManagePagination getAllBreederAccountsPagination(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Account> breederAccounts = accountRepository.findAccountsByRoleEnum(AccountRoleEnum.KOI_BREEDER, pageable);

        // Convert Account entities to AccountResponse
        List<AccountResponseForManageDTO> accountResponseList = new ArrayList<>();
        for (Account account : breederAccounts.getContent()) {
            AccountResponseForManageDTO accountResponse = accountMapper.toAccountResponseForManageDTO(account);
            accountResponse.setCreatedAt(account.getCreatedDate());
            accountResponse.setUpdatedAt(account.getUpdatedDate());
            accountResponseList.add(accountResponse);
        }

        // Use the constructor with arguments
        return new AccountResponseForManagePagination(
                accountResponseList,
                breederAccounts.getNumber(),
                breederAccounts.getTotalPages(),
                breederAccounts.getNumberOfElements(),
                breederAccounts.getTotalElements()
        );
    }


    public AccountResponseForManagePagination getAllStaffAccountsPagination(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Account> staffAccounts = accountRepository.findAccountsByRoleEnum(AccountRoleEnum.STAFF, pageable);

        // Convert Account entities to AccountResponse
        List<AccountResponseForManageDTO> accountResponseList = new ArrayList<>();
        for (Account account : staffAccounts.getContent()) {
            AccountResponseForManageDTO accountResponse = accountMapper.toAccountResponseForManageDTO(account);
            accountResponse.setCreatedAt(account.getCreatedDate());
            accountResponse.setUpdatedAt(account.getUpdatedDate());
            accountResponseList.add(accountResponse);
        }

        // Create AccountResponsePagination and set pagination details
        return new AccountResponseForManagePagination(
                accountResponseList,
                staffAccounts.getNumber(),
                staffAccounts.getTotalPages(),
                staffAccounts.getNumberOfElements(),
                staffAccounts.getTotalElements()
        );
    }


    public AccountResponseForManagePagination getAllMemberAccountsPagination(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Account> memberAccounts = accountRepository.findAccountsByRoleEnum(AccountRoleEnum.MEMBER, pageable);

        // Convert Account entities to AccountResponse
        List<AccountResponseForManageDTO> accountResponseList = new ArrayList<>();
        for (Account account : memberAccounts.getContent()) {
            AccountResponseForManageDTO accountResponse = accountMapper.toAccountResponseForManageDTO(account);
            accountResponse.setCreatedAt(account.getCreatedDate());
            accountResponse.setUpdatedAt(account.getUpdatedDate());
            accountResponseList.add(accountResponse);
        }

        // Create AccountResponsePagination and set pagination details
        return new AccountResponseForManagePagination(
                accountResponseList,
                memberAccounts.getNumber(),
                memberAccounts.getTotalPages(),
                memberAccounts.getNumberOfElements(),
                memberAccounts.getTotalElements()
        );
    }


    private AccountResponse mapToAccountResponse(Account account) {
        AccountResponse response = new AccountResponse();
        response.setUser_id(account.getUser_id());  // Corrected to user_id
        response.setUsername(account.getUsername());
        response.setFirstName(account.getFirstName());
        response.setLastName(account.getLastName());
        response.setEmail(account.getEmail());
        response.setPhoneNumber(account.getPhoneNumber());
        response.setAddress(account.getAddress());
        response.setStatus(account.getStatus());
        response.setRoleEnum(account.getRoleEnum());
        response.setBalance(account.getBalance());
        return response;
    }

    public AccountRoleEnum getRoleEnumX(String role) {
        String roleX = role.toLowerCase().replaceAll("\\s", "");
        return switch (roleX) {
            case "member" -> AccountRoleEnum.MEMBER;
            case "staff" -> AccountRoleEnum.STAFF;
            case "manager" -> AccountRoleEnum.MANAGER;
            case "koibreeder" -> AccountRoleEnum.KOI_BREEDER;
            default -> throw new EntityNotFoundException("Invalid role");
        };
    }


}
