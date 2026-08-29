package com.mdswaley.BookingApplication.Repository;

import com.mdswaley.BookingApplication.Entity.ResourceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResourceRepository extends JpaRepository<ResourceEntity, Long> {
}
