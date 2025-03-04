package com.fayupable.test.mapper.user;

import com.fayupable.test.dto.user.UserContactDto;
import com.fayupable.test.entity.user.UserContact;
import org.springframework.stereotype.Service;

@Service
public class UserContactMapper {

    public static UserContactDto fromUserContact(UserContact contact) {
        if (contact == null) {
            return null;
        }

        UserContactDto dto = new UserContactDto();
        dto.setPhoneNumber(contact.getPhoneNumber());
        return dto;
    }
}
