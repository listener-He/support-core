/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
 * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
 * to You under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package cn.hehouhui.exception;

import cn.hehouhui.constant.ErrorCodeEnum;
import cn.hehouhui.util.EmptyUtil;
import lombok.Getter;

import java.io.Serial;

/**
 * 通用异常
 *
 * @author HEHH
 * @date 2024/12/02
 */
@Getter
public class CommonException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 8301576693940858217L;

    /**
     * 当前异常的错误码 -- GETTER -- 获取当前错误码
     */
    private final ErrorCodeEnum errCode;

    public CommonException(ErrorCodeEnum errCode) {
        super(toMsg(errCode, null));
        this.errCode = errCode;
    }

    public CommonException(ErrorCodeEnum errCode, String message) {
        super(toMsg(errCode, message));
        this.errCode = errCode;
    }

    public CommonException(ErrorCodeEnum errCode, String message, Throwable cause) {
        super(toMsg(errCode, message), cause);
        this.errCode = errCode;
    }

    public CommonException(ErrorCodeEnum errCode, Throwable cause) {
        super(toMsg(errCode, null), cause);
        this.errCode = errCode;
    }

    protected CommonException(ErrorCodeEnum errCode, String message, Throwable cause, boolean enableSuppression,
        boolean writableStackTrace) {
        super(toMsg(errCode, message), cause, enableSuppression, writableStackTrace);
        this.errCode = errCode;
    }

    private static String toMsg(ErrorCodeEnum errCode, String msg) {
        StringBuilder sb = new StringBuilder();
        if (EmptyUtil.isNotEmpty(msg)) {
            sb.append("msg: ").append(msg).append(", ");
        }

        if (errCode != null) {
            sb.append("errCode: ").append(errCode.getValue()).append(", errCodeDesc: ").append(errCode.getDesc());
        }
        return sb.toString();
    }
}
