package com.group4.evRentalBE.initializer;

import com.group4.evRentalBE.model.entity.*;
import com.group4.evRentalBE.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final DocumentRepository documentRepository;
    private final VehicleTypeRepository vehicleTypeRepository;
    private final VehicleRepository vehicleRepository;
    private final RentalStationRepository rentalStationRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() > 0) {
            return;
        }

        // Initialize data in correct order
        initializePermissions();
        initializeRoles();
        initializeVehicleTypes();
        initializeRentalStations();
        initializeUsers();
        initializeDocuments();
        initializeVehicles();
    }

    private void initializePermissions() {
        Permission[] permissions = {
            new Permission("User Management", "USER_MANAGE", "Manage users and accounts"),
            new Permission("Vehicle Management", "VEHICLE_MANAGE", "Manage vehicles and inventory"),
            new Permission("Station Management", "STATION_MANAGE", "Manage rental stations"),
            new Permission("Booking Management", "BOOKING_MANAGE", "Manage bookings and rentals"),
            new Permission("Payment Management", "PAYMENT_MANAGE", "Manage payments and transactions"),
            new Permission("Report Access", "REPORT_ACCESS", "Access reports and analytics"),
            new Permission("Customer Support", "CUSTOMER_SUPPORT", "Handle customer support"),
            new Permission("System Settings", "SYSTEM_SETTINGS", "Manage system configurations")
        };

        for (Permission permission : permissions) {
            if (!permissionRepository.existsByCode(permission.getCode())) {
                permissionRepository.save(permission);
            }
        }
    }

    private void initializeRoles() {
        // Admin role with all permissions
        Set<Permission> adminPermissions = new HashSet<>(permissionRepository.findAll());
        Role adminRole = Role.builder()
                .name("ADMIN")
                .description("System Administrator with full access")
                .permissions(adminPermissions)
                .build();
        if (!roleRepository.existsByName("ADMIN")) {
            roleRepository.save(adminRole);
        }

        // Staff role with limited permissions
        Set<Permission> staffPermissions = new HashSet<>();
        permissionRepository.findByCode("VEHICLE_MANAGE").ifPresent(staffPermissions::add);
        permissionRepository.findByCode("BOOKING_MANAGE").ifPresent(staffPermissions::add);
        permissionRepository.findByCode("CUSTOMER_SUPPORT").ifPresent(staffPermissions::add);
        
        Role staffRole = Role.builder()
                .name("STAFF")
                .description("Station staff with operational access")
                .permissions(staffPermissions)
                .build();
        if (!roleRepository.existsByName("STAFF")) {
            roleRepository.save(staffRole);
        }

        // Customer role with basic permissions
        Set<Permission> customerPermissions = new HashSet<>();
        Role customerRole = Role.builder()
                .name("CUSTOMER")
                .description("Regular customer user")
                .permissions(customerPermissions)
                .build();
        if (!roleRepository.existsByName("CUSTOMER")) {
            roleRepository.save(customerRole);
        }
    }

    private void initializeVehicleTypes() {
        VehicleType[] vehicleTypes = {
            VehicleType.builder()
                    .name("Electric Motorbike")
                    .depositAmount(2000000.0)
                    .rentalRate(150000.0)
                    .build(),
            VehicleType.builder()
                    .name("Electric Bicycle")
                    .depositAmount(500000.0)
                    .rentalRate(50000.0)
                    .build(),
            VehicleType.builder()
                    .name("Electric Scooter")
                    .depositAmount(1000000.0)
                    .rentalRate(80000.0)
                    .build(),
            VehicleType.builder()
                    .name("Electric Car")
                    .depositAmount(10000000.0)
                    .rentalRate(500000.0)
                    .build()
        };

        for (VehicleType vehicleType : vehicleTypes) {
            if (!vehicleTypeRepository.existsByName(vehicleType.getName())) {
                vehicleTypeRepository.save(vehicleType);
            }
        }
    }

    private void initializeRentalStations() {
        RentalStation[] stations = {
            RentalStation.builder()
                    .city("Ho Chi Minh City")
                    .address("123 Nguyen Van Linh, District 7, Ho Chi Minh City")
                    .build(),
            RentalStation.builder()
                    .city("Ho Chi Minh City")
                    .address("456 Le Van Viet, Thu Duc City, Ho Chi Minh City")
                    .build(),
            RentalStation.builder()
                    .city("Hanoi")
                    .address("789 Cau Giay, Cau Giay District, Hanoi")
                    .build(),
            RentalStation.builder()
                    .city("Da Nang")
                    .address("321 Bach Dang, Hai Chau District, Da Nang")
                    .build()
        };

        for (RentalStation station : stations) {
            rentalStationRepository.save(station);
        }
    }

    private void initializeUsers() {
        Role adminRole = roleRepository.findByName("ADMIN").orElseThrow();
        Role staffRole = roleRepository.findByName("STAFF").orElseThrow();
        Role customerRole = roleRepository.findByName("CUSTOMER").orElseThrow();

        // Create Admin User
        User adminUser = User.builder()
                .username("admin")
                .email("admin@evrental.com")
                .phone("0901234567")
                .password(passwordEncoder.encode("admin123"))
                .isVerify(true)
                .tokenVersion(0)
                .roles(Set.of(adminRole))
                .build();
        userRepository.save(adminUser);

        // Assign admin to first station
        RentalStation firstStation = rentalStationRepository.findAll().get(0);
        firstStation.setAdminUser(adminUser);
        rentalStationRepository.save(firstStation);

        // Create Staff Users
        String[] staffUsernames = {"staff1", "staff2", "staff3", "staff4"};
        String[] staffEmails = {"john@evrental.com", "jane@evrental.com", "mike@evrental.com", "sarah@evrental.com"};
        String[] staffPhones = {"0901234568", "0901234569", "0901234570", "0901234571"};
        
        var allStations = rentalStationRepository.findAll();
        for (int i = 0; i < staffUsernames.length; i++) {
            User staffUser = User.builder()
                    .username(staffUsernames[i])
                    .email(staffEmails[i])
                    .phone(staffPhones[i])
                    .password(passwordEncoder.encode("staff123"))
                    .isVerify(true)
                    .tokenVersion(0)
                    .roles(Set.of(staffRole))
                    .managedStation(allStations.get(i % allStations.size()))
                    .build();
            userRepository.save(staffUser);
        }

        // Create Customer Users
        String[] customerUsernames = {"customer1", "customer2", "customer3", "customer4", "customer5"};
        String[] customerEmails = {"alice@email.com", "bob@email.com", "carol@email.com", "david@email.com", "eva@email.com"};
        String[] customerPhones = {"0912345678", "0912345679", "0912345680", "0912345681", "0912345682"};

        for (int i = 0; i < customerUsernames.length; i++) {
            User customerUser = User.builder()
                    .username(customerUsernames[i])
                    .email(customerEmails[i])
                    .phone(customerPhones[i])
                    .password(passwordEncoder.encode("customer123"))
                    .isVerify(true)
                    .tokenVersion(0)
                    .roles(Set.of(customerRole))
                    .build();
            userRepository.save(customerUser);
        }
    }

    private void initializeDocuments() {
        // Get all customer users
        var customerUsers = userRepository.findAll().stream()
                .filter(user -> user.hasRole("CUSTOMER"))
                .toList();

        String[] cccdNumbers = {"123456789012", "234567890123", "345678901234", "456789012345", "567890123456"};
        String[] drivingLicenseNumbers = {"B1123456", "B2234567", "B3345678", "B4456789", "B5567890"};

        for (int i = 0; i < customerUsers.size() && i < cccdNumbers.length; i++) {
            User customerUser = customerUsers.get(i);
            
            // Create CCCD document
            Document cccdDoc = Document.builder()
                    .user(customerUser)
                    .documentType(Document.DocumentType.CCCD)
                    .documentNumber(cccdNumbers[i])
                    .frontPhoto("/photos/cccd_front_" + (i + 1) + ".jpg")
                    .backPhoto("/photos/cccd_back_" + (i + 1) + ".jpg")
                    .issueDate(LocalDateTime.now().minusYears(2))
                    .expiryDate(LocalDateTime.now().plusYears(8))
                    .issuedBy("Department of Public Security")
                    .status(Document.DocumentStatus.VERIFIED)
                    .verifiedAt(LocalDateTime.now().minusDays(30))
                    .isDefault(true)
                    .build();
            documentRepository.save(cccdDoc);

            // Create Driving License document
            Document drivingLicenseDoc = Document.builder()
                    .user(customerUser)
                    .documentType(Document.DocumentType.DRIVING_LICENSE)
                    .documentNumber(drivingLicenseNumbers[i])
                    .frontPhoto("/photos/driving_license_front_" + (i + 1) + ".jpg")
                    .backPhoto("/photos/driving_license_back_" + (i + 1) + ".jpg")
                    .issueDate(LocalDateTime.now().minusYears(1))
                    .expiryDate(LocalDateTime.now().plusYears(4))
                    .issuedBy("Department of Transport")
                    .status(Document.DocumentStatus.VERIFIED)
                    .verifiedAt(LocalDateTime.now().minusDays(15))
                    .isDefault(false)
                    .build();
            documentRepository.save(drivingLicenseDoc);
        }
    }

    private void initializeVehicles() {
        var stations = rentalStationRepository.findAll();
        var vehicleTypes = vehicleTypeRepository.findAll();

        // Create vehicles for each station and type combination
        for (int stationIndex = 0; stationIndex < stations.size(); stationIndex++) {
            RentalStation station = stations.get(stationIndex);
            
            for (int typeIndex = 0; typeIndex < vehicleTypes.size(); typeIndex++) {
                VehicleType type = vehicleTypes.get(typeIndex);
                
                // Create 3-5 vehicles per type per station
                int vehicleCount = 3 + (stationIndex + typeIndex) % 3; // 3-5 vehicles
                
                for (int i = 0; i < vehicleCount; i++) {
                    Vehicle vehicle = Vehicle.builder()
                            .type(type)
                            .station(station)
                            .status(i == 0 ? Vehicle.VehicleStatus.MAINTENANCE : Vehicle.VehicleStatus.AVAILABLE)
                            .conditionNotes("Good condition, regular maintenance completed")
                            .photos("/photos/vehicle_" + type.getName().toLowerCase().replace(" ", "_") + "_" + (i + 1) + ".jpg")
                            .build();
                    vehicleRepository.save(vehicle);
                }
            }
        }
    }
}
