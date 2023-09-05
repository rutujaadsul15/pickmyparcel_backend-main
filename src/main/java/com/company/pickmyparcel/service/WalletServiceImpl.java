package com.company.pickmyparcel.service;

import com.company.pickmyparcel.model.Deal;
import com.company.pickmyparcel.model.User;
import com.company.pickmyparcel.model.Wallet;
import com.company.pickmyparcel.model.WalletHistory;
import com.company.pickmyparcel.repository.UserRepository;
import com.company.pickmyparcel.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@Service
public class WalletServiceImpl implements WalletService {
    private static final Logger LOGGER = Logger.getLogger(WalletServiceImpl.class.getName());

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public Wallet fetchWalletDetails(String userContactNumber) {
        try {
            Optional<Wallet> walletOptional = walletRepository.findById(userContactNumber);
            return walletOptional.orElse(null);
        } catch (Exception e) {
            LOGGER.info("Exception while fetching wallet for user : " + userContactNumber);
            e.printStackTrace();
            return null;
        }
    }

    public void creditMoneyIntoCarrierWallet(Deal deal) {
        String carrierContactNumber = deal.getCarrier().getCarrierContactNo();
        LOGGER.info("Wallet balance update started for user : " + carrierContactNumber);
        Double amountToBeCredited = deal.getDealTotalAsCarrier();
        LOGGER.info("Amount to be credited in user wallet is : " + amountToBeCredited);
        try {
            if (carrierContactNumber == null || carrierContactNumber.equals("")) {
                LOGGER.info("Carrier contact number is empty. Can't credit amount in carrier's wallet");
                throw new Exception("Carrier contact number is empty. Can't credit amount in carrier's wallet");
            }
            if (amountToBeCredited == null || amountToBeCredited == 0) {
                LOGGER.info("amountToBeCredited is empty. Can't credit amount in carrier's wallet");
                throw new Exception("amountToBeCredited is empty. Can't credit amount in carrier's wallet");
            }
            Optional<User> user = userRepository.findById(carrierContactNumber);
            if (user.isPresent()) {
                Wallet wallet = user.get().getWallet();
                Double walletBalance = wallet.getWalletBalance();
                LOGGER.info("Wallet balance before update : " + walletBalance);
                walletBalance = walletBalance + amountToBeCredited;
                wallet.setWalletBalance(walletBalance);
                LOGGER.info("Wallet balance after update : " + walletBalance);
                LOGGER.info("Amount is credited successfully to wallet ID : " + user.get().getWallet().getUserContactNumber());
                this.updateWalletHistory(wallet, user.get(), deal, amountToBeCredited);
                String msgContent = "You have successfully delivered a parcel with the deal ID: " + deal.getDealId() + ". We have credited Rs. " + amountToBeCredited + " to your pickmyparcel wallet. We appreciate your valuable contribution in delivering the parcel and earning with pickmyparcel. Thank you for choosing pickmyparcel.in jenesys";
                DealServiceImpl.sendSMS(deal.getCarrier().getCarrierContactNo(), msgContent, "1707168784751356198");
            }
        } catch (Exception e) {
            LOGGER.info("Something went wrong while updated wallet balance for user : " + carrierContactNumber);
            throw new RuntimeException(e);
        }
    }

    private void updateWalletHistory(Wallet wallet, User user, Deal deal, Double amountToBeCredited) {
        LOGGER.info("Updating wallet history for wallet ID : " + user.getWallet().getUserContactNumber());
        WalletHistory walletHistory = new WalletHistory();
        walletHistory.setDealId(deal.getDealId());
        walletHistory.setTransactionAmount(amountToBeCredited);
        List<WalletHistory> walletHistoryList = wallet.getWalletHistoryList();
        walletHistoryList.add(walletHistory);
        wallet.setWalletHistoryList(walletHistoryList);
        LOGGER.info("Successfully updated wallet history for wallet ID : " + user.getWallet().getUserContactNumber());
    }
}
