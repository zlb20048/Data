/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chleon.datacollection.autolink;

import com.chleon.telematics.MyLog;

/**
 * {@hide}
 */
public class CommandException extends RuntimeException {
    private static final String TAG = CommandException.class.getSimpleName();

    private Error e;

    public enum Error {
        SERVER_NOT_AVAILABLE,
        INVALID_RESPONSE,
        RESPONSE_TIMEOUT,
        GENERIC_FAILURE
    }

    public CommandException(Error e) {
        super(e.toString());
        this.e = e;
    }

    public static CommandException
    fromErrno(int errno) {
        switch(errno) {
            case Constants.SUCCESS:                       return null;
            case Constants.INVALID_RESPONSE:
                return new CommandException(Error.INVALID_RESPONSE);
            case Constants.SERVER_NOT_AVAILABLE:
                return new CommandException(Error.SERVER_NOT_AVAILABLE);
            case Constants.GENERIC_FAILURE:
                return new CommandException(Error.GENERIC_FAILURE);
            case Constants.RESPONSE_TIMEOUT:
                return new CommandException(Error.RESPONSE_TIMEOUT);
            default:
                MyLog.e(TAG, "Unrecognized errno " + errno);
                return new CommandException(Error.INVALID_RESPONSE);
        }
    }

    public Error getCommandError() {
        return e;
    }



}
