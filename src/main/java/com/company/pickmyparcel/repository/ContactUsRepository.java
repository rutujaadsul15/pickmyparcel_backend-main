package com.company.pickmyparcel.repository;


import com.company.pickmyparcel.model.ContactUs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactUsRepository extends JpaRepository<ContactUs, String> {
}
