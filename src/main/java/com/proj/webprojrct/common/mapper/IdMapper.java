package com.proj.webprojrct.common.mapper;

import com.proj.webprojrct.user.entity.User;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

@Component
public class IdMapper {

    @Named("mapIdToUser")
    public User mapIdToUser(Long id) {
        if (id == null) {
            return null;
        }
        User user = new User();
        user.setId(id);
        return user;
    }

    @Named("mapUserToId")
    public Long mapUserToId(User user) {
        return user != null ? user.getId() : null;
    }

    // Id to Entity of other e
}
