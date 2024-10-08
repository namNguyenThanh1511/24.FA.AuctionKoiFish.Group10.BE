package com.group10.koiauction.service;


import com.group10.koiauction.entity.Account;
import com.group10.koiauction.entity.enums.AccountRoleEnum;
import com.group10.koiauction.entity.enums.AccountStatusEnum;
import com.group10.koiauction.mapper.AccountMapper;
import com.group10.koiauction.model.request.CreateBreederAccountRequest;
import com.group10.koiauction.model.request.LoginAccountRequest;
import com.group10.koiauction.model.request.RegisterAccountRequest;
import com.group10.koiauction.exception.DuplicatedEntity;
import com.group10.koiauction.exception.EntityNotFoundException;
import com.group10.koiauction.model.request.RegisterMemberRequest;
import com.group10.koiauction.model.request.UpdateProfileRequestDTO;
import com.group10.koiauction.model.request.ResetPasswordRequestDTO;
import com.group10.koiauction.model.response.AccountResponse;
import com.group10.koiauction.model.response.EmailDetail;
import com.group10.koiauction.repository.AccountRepository;
import com.group10.koiauction.utilities.AccountUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

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
        Account newAccount = modelMapper.map(registerAccountRequest, Account.class);// Account.class : tự động new Account() rồi mapping
        try {
            newAccount.setRoleEnum(getRoleEnumX(registerAccountRequest.getRoleEnum()));
            newAccount.setPassword(passwordEncoder.encode(registerAccountRequest.getPassword()));
            accountRepository.save(newAccount);

            EmailDetail emailDetail = new EmailDetail();
            emailDetail.setAccount(newAccount);
            emailDetail.setSubject("Welcome to my web");
            emailDetail.setLink("https://www.google.com/");

            emailService.sentEmail(emailDetail);

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
        try {
            newAccount.setRoleEnum(AccountRoleEnum.MEMBER);
            newAccount.setPassword(passwordEncoder.encode(registerAccountRequest.getPassword()));
            accountRepository.save(newAccount);

            EmailDetail emailDetail = new EmailDetail();
            emailDetail.setAccount(newAccount);
            emailDetail.setSubject("Welcome to my web");
            emailDetail.setLink("https://www.google.com/");

            emailService.sentEmail(emailDetail);

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
                    loginAccountRequest.getUsername(), loginAccountRequest.getPassword()
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
            emailDetail.setLink("https://www.google.com/");

            emailService.sentEmailBreeder(emailDetail);

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


    public void forgotPassword(String email) {
        Account account = accountRepository.findAccountByEmail(email);
        if(account == null) {
            throw new EntityNotFoundException("Account not found");
        }
        String token = tokenService.generateToken(account);
        EmailDetail emailDetail = new EmailDetail();
        emailDetail.setAccount(account);//set receiver
        emailDetail.setSubject("Reset password");
        emailDetail.setLink("https://www.google.com/?token="+token);
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
    public UserDetails loadUserByUsername(String phoneNumber) throws UsernameNotFoundException {
        Account account = accountRepository.findAccountByPhoneNumber(phoneNumber);
        if (account == null) {
            throw new EntityNotFoundException("User not found");
        }
        return account;
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
