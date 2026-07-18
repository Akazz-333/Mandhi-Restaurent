package com.mandhi.restaurant.repository;

import com.mandhi.restaurant.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByDateAndTime(String date, String time);
}
