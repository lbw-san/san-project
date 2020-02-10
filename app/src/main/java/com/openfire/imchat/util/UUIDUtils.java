package com.openfire.imchat.util;

import java.util.UUID;

public class UUIDUtils {

    public static String generate() {
        UUID uuid = UUID.randomUUID();

        return uuid.toString().replaceAll("-", "");
    }


}
