package com.sgx.icms.tools;

import org.mindrot.jbcrypt.BCrypt;

/**
 * CLI helper used by {@code setup.sh}: prints a BCrypt hash for the given password
 * using the SAME library the app verifies with, so {@code __PWHASH__} in seed.sql is
 * guaranteed compatible with {@code PasswordService.matches}.
 *
 * <pre>java -cp &lt;classpath&gt; com.sgx.icms.tools.PasswordHashTool "Password@123"</pre>
 */
public final class PasswordHashTool {

    private PasswordHashTool() {
    }

    public static void main(String[] args) {
        if (args.length < 1 || args[0].isEmpty()) {
            System.err.println("Usage: PasswordHashTool <password>");
            System.exit(2);
            return;
        }
        // Print only the hash (no newline noise) so setup.sh can capture it cleanly.
        System.out.print(BCrypt.hashpw(args[0], BCrypt.gensalt(10)));
    }
}
