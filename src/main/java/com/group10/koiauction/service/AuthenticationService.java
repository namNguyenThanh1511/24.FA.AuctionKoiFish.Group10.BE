package com.group10.koiauction.service;


import com.group10.koiauction.entity.Account;
import com.group10.koiauction.entity.enums.AccountRoleEnum;
import com.group10.koiauction.entity.enums.AccountStatusEnum;
import com.group10.koiauction.mapper.AccountMapper;
import com.group10.koiauction.model.request.LoginAccountRequest;
import com.group10.koiauction.model.request.RegisterAccountRequest;
import com.group10.koiauction.exception.DuplicatedEntity;
import com.group10.koiauction.exception.EntityNotFoundException;
import com.group10.koiauction.model.request.UpdateProfileRequestDTO;
import com.group10.koiauction.model.response.AccountResponse;
import com.group10.koiauction.repository.AccountRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
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
    public AccountResponse register(RegisterAccountRequest registerAccountRequest) {
        Account newAccount = modelMapper.map(registerAccountRequest, Account.class);// Account.class : tự động new Account() rồi mapping
        try {
            newAccount.setRoleEnum(getRoleEnumX(registerAccountRequest.getRoleEnum()));
            newAccount.setPassword(passwordEncoder.encode(registerAccountRequest.getPassword()));
            accountRepository.save(newAccount);
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

        try{
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    loginAccountRequest.getUsername(), loginAccountRequest.getPassword()
                    // go to loadByUsername check username first
                    // so sanh password db vs request password
            ));
            Account account = (Account) authentication.getPrincipal();
            if(account.getStatus().equals(AccountStatusEnum.INACTIVE)){
                throw new EntityNotFoundException("Account is inactive");
            }
            AccountResponse accountResponse = accountMapper.toAccountResponse(account);
            accountResponse.setToken(tokenService.generateToken(account));// set token response ve` cho front end
            return accountResponse;
        }catch (EntityNotFoundException e) {
            // Catch inactive account error and re-throw it as is
            throw e;

        }catch (Exception e) {
            throw new EntityNotFoundException("Username or password are incorrect");
        }

    }

    public String deleteDB(Long id){
        Account account= accountRepository.findByUser_id(id);
        try{
            accountRepository.delete(account);
        }catch (Exception e){
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
        }catch (Exception e) {
            if (e.getMessage().contains(account.getPhoneNumber())) {
                throw new DuplicatedEntity("Duplicated phone");
            } else if (e.getMessage().contains(account.getEmail())) {
                throw new DuplicatedEntity("Duplicated  email ");
            }
            throw e;
        }

    }

    public AccountResponse updateAccountProfile(Long id,UpdateProfileRequestDTO updateProfileRequestDTO) {
        Account target = getAccountById(id);
        try {
        if (updateProfileRequestDTO.getEmail() != null && !updateProfileRequestDTO.getEmail().equals(target.getEmail())) {
            if(accountRepository.existsByEmail(updateProfileRequestDTO.getEmail())){
                throw new DuplicatedEntity("Email already been used");
            }
            target.setEmail(updateProfileRequestDTO.getEmail());
        }
        if(updateProfileRequestDTO.getPassword() != null){
            target.setPassword(passwordEncoder.encode(updateProfileRequestDTO.getPassword()));
        }
        if (updateProfileRequestDTO.getFirstName() != null) {
            target.setFirstName(updateProfileRequestDTO.getFirstName());
        }
        if (updateProfileRequestDTO.getLastName() != null) {
            target.setLastName(updateProfileRequestDTO.getLastName());
        }
        if (updateProfileRequestDTO.getPhoneNumber() != null && !updateProfileRequestDTO.getPhoneNumber().equals(target.getPhoneNumber())) {
            if(accountRepository.existsByPhoneNumber(updateProfileRequestDTO.getPhoneNumber())){
                throw new DuplicatedEntity("Email already been used");
            }
            target.setPhoneNumber(updateProfileRequestDTO.getPhoneNumber());
        }
            accountRepository.save(target);
        } catch (DuplicatedEntity e) {
           throw e;
        }
        catch (Exception e) {
            throw new RuntimeException("Error updating account profile: " + e.getMessage());
        }
        return accountMapper.toAccountResponse(target);
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
        String roleX = role.toLowerCase().replaceAll("\\s","");
        return switch (roleX) {
            case "member" -> AccountRoleEnum.MEMBER;
            case "staff" -> AccountRoleEnum.STAFF;
            case "manager" -> AccountRoleEnum.MANAGER;
            case "koibreeder" -> AccountRoleEnum.KOI_BREEDER;
            default -> throw new EntityNotFoundException("Invalid role");
        };
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return accountRepository.findByUsername(username); // find by username config in Account class

    }


}
