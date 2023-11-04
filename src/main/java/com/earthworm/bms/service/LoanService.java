package com.earthworm.bms.service;

import com.earthworm.bms.model.EducationLoan;
import com.earthworm.bms.model.LoanRecord;
import com.earthworm.bms.model.OtherLoanRecord;
import com.earthworm.bms.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class LoanService {

    @Autowired
    private LoanRepository<EducationLoan> eduLoanRepository;
    @Autowired
    private LoanRepository<OtherLoanRecord> otherLoanRepository;
    @Autowired
    private LoanRepository<LoanRecord> loanRepository;

    public List<LoanRecord> getAllLoans(String userId){
        List<LoanRecord> l = new ArrayList<LoanRecord>();
        l.addAll(loanRepository.findAllByUserId(userId));
       // l.addAll(otherLoanRepository.findAllByUserId(userId));
        return l;
    }
    public LoanRecord createLoan(LoanRecord loan){
        if(loan instanceof EducationLoan)
            eduLoanRepository.save((EducationLoan) loan);
        else
            otherLoanRepository.save((OtherLoanRecord) loan);
        return loan;
    }
}
