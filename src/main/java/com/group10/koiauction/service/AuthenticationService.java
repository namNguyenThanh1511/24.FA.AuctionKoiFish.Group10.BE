package com.group10.koiauction.service;


import com.group10.koiauction.entity.Account;
import com.group10.koiauction.entity.enums.AccountRoleEnum;
import com.group10.koiauction.model.request.LoginAccountRequest;
import com.group10.koiauction.model.request.RegisterAccountRequest;
import com.group10.koiauction.exception.DuplicatedEntity;
import com.group10.koiauction.exception.EntityNotFoundException;
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
            return modelMapper.map(account, AccountResponse.class);
        } catch (Exception e) {
            throw new EntityNotFoundException("Username or password are incorrect");
        }

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
