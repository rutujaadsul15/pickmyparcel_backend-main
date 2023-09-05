package com.company.pickmyparcel.controller;

import com.company.pickmyparcel.model.Wallet;
import com.company.pickmyparcel.service.WalletServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/wallet")
@CrossOrigin(origins = "*")
public class WalletController {

    @Autowired
    private WalletServiceImpl walletServiceImpl;


    @GetMapping("/fetchWalletDetails")
    public ResponseEntity<Wallet> fetchWalletDetails(@RequestParam("userContactNumber") String userContactNumber) {
        Wallet wallet = walletServiceImpl.fetchWalletDetails(userContactNumber);
        if (wallet!=null){
            return ResponseEntity.ok(wallet);
        }else{
            return ResponseEntity.internalServerError().body(wallet);
        }
    }
}
