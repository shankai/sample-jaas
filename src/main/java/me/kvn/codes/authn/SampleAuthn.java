package me.kvn.codes.authn;

import javax.security.auth.login.*;

/**
 * Authentication
 */
public class SampleAuthn {

    /**
     * 尝试认证
     */
    public static void main(String[] args) {

        // 构造 LoginContext
        LoginContext lc = null;
        try {
            // LoginModule: Sample
            lc = new LoginContext("Sample", new SampleCallbackHandler());
        } catch (LoginException le) {
            System.err.println("Cannot create LoginContext. "
                    + le.getMessage());
            System.exit(-1);
        } catch (SecurityException se) {
            System.err.println("Cannot create LoginContext. "
                    + se.getMessage());
            System.exit(-1);
        }

        // 尝试 3 次
        int i;
        for (i = 0; i < 3; i++) {
            try {

                // attempt authentication
                lc.login();

                // if we return with no exception, authentication succeeded
                break;

            } catch (LoginException le) {

                System.err.println("Authentication failed:");
                System.err.println("  " + le.getMessage());
                try {
                    Thread.currentThread().sleep(3000);
                } catch (Exception e) {
                    // ignore
                }

            }
        }

        // did they fail three times?
        if (i == 3) {
            System.out.println("Sorry");
            System.exit(-1);
        }

        System.out.println("Authentication succeeded!");

    }
}


