package com.bloodbank.service;
import com.bloodbank.dto.AuthUserResponse;
import com.bloodbank.dto.ChangePasswordRequest;
import com.bloodbank.dto.LoginRequest;
import com.bloodbank.dto.LoginResponse;
import com.bloodbank.dto.RegisterDonorRequest;
import com.bloodbank.dto.RegisterHospitalRequest;
import com.bloodbank.model.User;
import com.bloodbank.repository.UserRepository;

import com.bloodbank.model.BloodGroup;
import com.bloodbank.repository.BloodGroupRepository;
import com.bloodbank.model.enums.ApprovalStatus;
import com.bloodbank.model.enums.EventType;
import com.bloodbank.model.enums.Gender;
import com.bloodbank.model.enums.Role;
import com.bloodbank.event.DomainEvent;
import com.bloodbank.event.DomainEventPublisher;
import com.bloodbank.config.AppProperties;
import com.bloodbank.model.Donor;
import com.bloodbank.repository.DonorRepository;
import com.bloodbank.exception.AccountDisabledException;
import com.bloodbank.exception.ConflictException;
import com.bloodbank.exception.InvalidCredentialsException;
import com.bloodbank.exception.ResourceNotFoundException;
import com.bloodbank.model.Hospital;
import com.bloodbank.repository.HospitalRepository;
import com.bloodbank.security.JwtService;
import com.bloodbank.security.SecurityUtils;
import com.bloodbank.dto.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;

@Service
public class AuthService {

    private final UserRepository users;
    private final DonorRepository donors;
    private final HospitalRepository hospitals;
    private final BloodGroupRepository bloodGroups;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final DomainEventPublisher eventPublisher;
    private final AuditRecorder auditRecorder;
    private final AppProperties properties;

    public AuthService(UserRepository users,
                       DonorRepository donors,
                       HospitalRepository hospitals,
                       BloodGroupRepository bloodGroups,
                       PasswordEncoder encoder,
                       JwtService jwtService,
                       DomainEventPublisher eventPublisher,
                       AuditRecorder auditRecorder,
                       AppProperties properties) {
        this.users = users;
        this.donors = donors;
        this.hospitals = hospitals;
        this.bloodGroups = bloodGroups;
        this.encoder = encoder;
        this.jwtService = jwtService;
        this.eventPublisher = eventPublisher;
        this.auditRecorder = auditRecorder;
        this.properties = properties;
    }

    @Transactional
    public AuthUserResponse registerDonor(RegisterDonorRequest req) {
        String normalizedEmail = req.email().trim().toLowerCase();
        if (users.existsByEmailIgnoreCase(normalizedEmail)) {
            throw ConflictException.emailExists(normalizedEmail);
        }

        Long bgId = req.bloodGroupId();
        if (bgId == null && req.bloodGroup() != null) {
            bgId = bloodGroups.findByCodeIgnoreCase(req.bloodGroup().trim())
                    .map(BloodGroup::getId)
                    .orElse(1L);
        }
        if (bgId == null) bgId = 1L;

        User user = new User();
        user.setEmail(normalizedEmail);
        user.setPasswordHash(encoder.encode(req.password()));
        user.setFullName(req.fullName().trim());
        user.setPhone(req.phone() != null ? req.phone().trim() : null);
        user.setRole(Role.DONOR);
        user.setActive(true);
        user = users.save(user);

        Donor donor = new Donor();
        donor.setUserId(user.getId());
        donor.setUser(user);
        donor.setBloodGroupId(bgId);
        LocalDate dob = req.effectiveDob();
        donor.setDob(dob != null ? dob : LocalDate.of(2000, 1, 1));
        donor.setGender(req.gender() != null ? req.gender() : Gender.OTHER);
        donor.setWeightKg(req.weightKg() != null ? req.weightKg() : 60.0);
        donor.setPhone(req.phone() != null ? req.phone().trim() : "");
        donor.setCity(req.city() != null ? req.city().trim() : "City");
        donors.save(donor);

        return AuthUserResponse.from(user, null);
    }

    @Transactional
    public AuthUserResponse registerHospital(RegisterHospitalRequest req) {
        String normalizedEmail = req.email().trim().toLowerCase();
        if (users.existsByEmailIgnoreCase(normalizedEmail)) {
            throw ConflictException.emailExists(normalizedEmail);
        }

        String license = req.licenseNo().trim();
        if (hospitals.existsByLicenseNoIgnoreCase(license)) {
            throw ConflictException.licenseExists(license);
        }

        User user = new User();
        user.setEmail(normalizedEmail);
        user.setPasswordHash(encoder.encode(req.password()));
        String hospitalName = req.effectiveName();
        user.setFullName(hospitalName);
        user.setPhone(req.phone().trim());
        user.setRole(Role.HOSPITAL);
        user.setActive(true);
        user = users.save(user);

        Hospital hospital = new Hospital();
        hospital.setUserId(user.getId());
        hospital.setUser(user);
        hospital.setName(hospitalName);
        hospital.setLicenseNo(license);
        hospital.setContactPerson(req.contactPerson() != null ? req.contactPerson().trim() : "");
        hospital.setPhone(req.phone().trim());
        hospital.setCity(req.city().trim());
        hospital.setAddress(req.address() != null ? req.address().trim() : "");
        hospital.setApprovalStatus(ApprovalStatus.PENDING);
        hospital = hospitals.save(hospital);

        eventPublisher.publish(new DomainEvent(
                EventType.HOSPITAL_REGISTERED,
                Map.of("hospitalId", hospital.getId(), "name", hospital.getName(), "userId", user.getId()),
                Instant.now()
        ));

        return AuthUserResponse.from(user, ApprovalStatus.PENDING);
    }

    public LoginResponse login(LoginRequest req) {
        String email = req.email().trim().toLowerCase();
        User user = users.findByEmailIgnoreCase(email).orElse(null);

        if (user == null || !encoder.matches(req.password(), user.getPasswordHash())) {
            auditRecorder.record("LOGIN_FAILED", "USER", null, "Failed login attempt for " + email);
            throw new InvalidCredentialsException("Invalid email or password");
        }

        if (!user.isActive()) {
            auditRecorder.record("LOGIN_BLOCKED", "USER", user.getId(), "Blocked login for disabled user " + email);
            throw new AccountDisabledException("Account is disabled");
        }

        ApprovalStatus hospitalStatus = null;
        if (user.getRole() == Role.HOSPITAL) {
            hospitalStatus = hospitals.findByUserId(user.getId())
                    .map(Hospital::getApprovalStatus)
                    .orElse(ApprovalStatus.PENDING);
        }

        long expirySeconds = properties.getJwt() != null && properties.getJwt().getExpirySeconds() > 0
                ? properties.getJwt().getExpirySeconds()
                : 3600L;

        String token = jwtService.generateToken(user.getId(), user.getRole().name());

        return new LoginResponse(
                token,
                "Bearer",
                expirySeconds,
                AuthUserResponse.from(user, hospitalStatus)
        );
    }

    @Transactional(readOnly = true)
    public AuthUserResponse me() {
        Long userId = SecurityUtils.currentUserId();
        User user = users.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        ApprovalStatus hospitalStatus = null;
        if (user.getRole() == Role.HOSPITAL) {
            hospitalStatus = hospitals.findByUserId(userId)
                    .map(Hospital::getApprovalStatus)
                    .orElse(null);
        }
        return AuthUserResponse.from(user, hospitalStatus);
    }

    @Transactional
    public void changePassword(ChangePasswordRequest req) {
        Long userId = SecurityUtils.currentUserId();
        User user = users.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!encoder.matches(req.currentPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Current password is incorrect");
        }

        user.setPasswordHash(encoder.encode(req.newPassword()));
        users.save(user);
    }
}
