package com.group4.evRentalBE.repository;

import com.group4.evRentalBE.model.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer,Long>
{
    Optional<Customer> findByUser_UserId(Long userId);

}
