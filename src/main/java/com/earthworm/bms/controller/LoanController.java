package com.earthworm.bms.controller;

import com.earthworm.bms.model.CustomerRecord;
import com.earthworm.bms.model.LoanRecord;
import com.earthworm.bms.model.datapojos.DepositDTO;
import com.earthworm.bms.service.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class LoanController {

   @Autowired
    private LoanService loanService;
    @PostMapping("/add-loan")
    public ResponseEntity<LoanRecord> applyLoan(@RequestBody LoanRecord body){
        System.out.println("body " + body.toString());
        loanService.createLoan(body);
        return ResponseEntity.created(null).build();
    }
    @GetMapping("/get-loans")
    public List<LoanRecord> getUserDetails(){
        String userName = SecurityContextHolder.getContext().getAuthentication().getName().toString();
        return loanService.getAllLoans(userName);
    }
}
