package com.fayupable.test.repository;

import com.fayupable.test.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface INotificationRepository extends JpaRepository<Notification, UUID> {

}
