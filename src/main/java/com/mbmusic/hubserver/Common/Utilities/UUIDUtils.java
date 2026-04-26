package com.mbmusic.hubserver.Common.Utilities;

import java.util.UUID;

public class UUIDUtils {
    
    /**
     * This method checks if a session id is valid. Invalid means that the session id
     * is either null or the nil UUID (all zeroes)
     * @param sessionUuid The UUID corresponding with the front end session id
     * @return True if the UUID is a valid session id. False otherwise.
     */
    public static boolean IsValidSessionId(UUID sessionUuid) {
        return sessionUuid != null && !sessionUuid.equals(UUID.fromString("00000000-0000-0000-0000-000000000000"));
    }

}
