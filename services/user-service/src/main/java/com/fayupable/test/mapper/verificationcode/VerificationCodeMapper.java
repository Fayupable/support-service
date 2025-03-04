package com.fayupable.test.mapper.verificationcode;

import com.fayupable.test.dto.verificationcode.VerificationCodeDto;
import com.fayupable.test.entity.verification.VerificationCode;
import org.springframework.stereotype.Service;

@Service
public class VerificationCodeMapper {

    public VerificationCodeDto fromVerificationCode(VerificationCode verificationCode) {
        VerificationCodeDto verificationCodeDto = new VerificationCodeDto();
        verificationCodeDto.setUserId(verificationCode.getUser().getUserId());
        verificationCodeDto.setVerificationCode(verificationCode.getCode());
        return verificationCodeDto;
    }

}
