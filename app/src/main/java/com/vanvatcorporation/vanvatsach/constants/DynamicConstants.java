package com.vanvatcorporation.vanvatsach.constants;

import com.vanvatcorporation.vanvatsach.stuff.Account;

public class DynamicConstants {

    /**
     * If set to true, whatever field like username and password will be noted to UI that this app is not safe to input.
     */
    public static boolean IS_PROCEED_UNKNOWN_OTHER_INSTALLATION;


    public static Account account;
    public static Account.AccountInformation temporaryAccountInformation = new Account.AccountInformation();
}
