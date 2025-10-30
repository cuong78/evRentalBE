package com.group4.evRentalBE.initializer;

import com.group4.evRentalBE.domain.entity.*;
import com.group4.evRentalBE.domain.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
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
    private final WalletRepository walletRepository;

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
        initializeVehicles();
        initializeWallets();
    }
    private void initializeWallets() {
        // Get all customer users
        var customerUsers = userRepository.findAll().stream()
                .filter(user -> user.hasRole("CUSTOMER"))
                .toList();

        for (User customer : customerUsers) {
            // Kiểm tra xem customer đã có ví chưa
            if (!walletRepository.existsByUser(customer)) {
                Wallet wallet = Wallet.builder()
                        .user(customer)
                        .balance(0L) // Số dư ban đầu là 0
                        .build();
                walletRepository.save(wallet);
            }
        }
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
            new Permission("System Settings", "SYSTEM_SETTINGS", "Manage system configurations"),
            new Permission("Wallet View", "WALLET_VIEW", "View wallet balance & history"),
            new Permission("Wallet Topup", "WALLET_TOPUP", "Create topup bill and get VNPay URL"),
                new Permission("Wallet Adjust", "WALLET_ADJUST", "Adjust wallet balance manually")
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
                    .name("VinFast VF 3")
                    .depositAmount(5000000.0)
                    .rentalRate(590000.0)
                    .imageUrl("https://greenfuture.tech/_next/image?url=https%3A%2F%2Fupload-static.fgf.vn%2Fcar%2Fvf301.jpg&w=384&q=75")
                    .seats(4)
                    .range(210)
                    .rangeStandard("NEDC")
                    .trunkCapacity(285)
                    .category("Minicar")
                    .description("Dòng xe 4 chỗ nhỏ gọn, hiện đại thích hợp cho nhu cầu di chuyển nội thành hằng ngày")
                    .build(),
            VehicleType.builder()
                    .name("VinFast VF 6S")
                    .depositAmount(10000000.0)
                    .rentalRate(1100000.0)
                    .imageUrl("https://greenfuture.tech/_next/image?url=https%3A%2F%2Fupload-static.fgf.vn%2Fcar%2Fvf6plus001.png&w=384&q=75")
                    .seats(5)
                    .range(480)
                    .rangeStandard("NEDC")
                    .trunkCapacity(423)
                    .category("B-SUV")
                    .description("Thiết kế thời thượng, khoang ngồi rộng rãi và được trang bị đầy đủ các tính năng công nghệ cơ bản")
                    .build(),
            VehicleType.builder()
                    .name("VinFast VF 6 Plus")
                    .depositAmount(12000000.0)
                    .rentalRate(1250000.0)
                    .imageUrl("https://greenfuture.tech/_next/image?url=https%3A%2F%2Fupload-static.fgf.vn%2Fcar%2Fvf6s001.png&w=384&q=75")
                    .seats(5)
                    .range(460)
                    .rangeStandard("NEDC")
                    .trunkCapacity(423)
                    .category("B-SUV")
                    .description("Khoang ngồi rộng rãi, thiết kế sang trọng và được trang bị màn hình giải trí lớn")
                    .build(),
            VehicleType.builder()
                    .name("VinFast VF 7S")
                    .depositAmount(15000000.0)
                    .rentalRate(1700000.0)
                    .imageUrl("https://shop.vinfastauto.com/on/demandware.static/-/Sites-app_vinfast_vn-Library/default/dw4a2b0002/reserves/VF7/vf7-masterpiece-1.webp")
                    .seats(5)
                    .range(430)
                    .rangeStandard("NEDC")
                    .trunkCapacity(537)
                    .category("C-SUV")
                    .description("Thiết kế mạnh mẽ, thể thao, động cơ êm ái. Đáp ứng tốt cho nhu cầu đi xa, đi tỉnh của các gia đình, nhóm bạn")
                    .build(),
            VehicleType.builder()
                    .name("VinFast VF 7 Plus")
                    .depositAmount(15000000.0)
                    .rentalRate(1700000.0)
                    .imageUrl("https://greenfuture.tech/_next/image?url=https%3A%2F%2Fupload-static.fgf.vn%2Fcar%2Fvf7plus001.png&w=384&q=75")
                    .seats(5)
                    .range(496)
                    .rangeStandard("NEDC")
                    .trunkCapacity(537)
                    .category("C-SUV")
                    .description("Thiết kế tương tự dòng VF 7S nhưng được tích hợp thêm nhiều tính năng vượt trội như cửa sổ trời, hệ thống âm thanh sống động")
                    .build(),
            VehicleType.builder()
                    .name("VinFast VF 8 Eco")
                    .depositAmount(18000000.0)
                    .rentalRate(1700000.0)
                    .imageUrl("https://greenfuture.tech/_next/image?url=https%3A%2F%2Fupload-static.fgf.vn%2Fcar%2Fvf8eco01.jpg&w=384&q=75")
                    .seats(5)
                    .range(471)
                    .rangeStandard("WLTP")
                    .trunkCapacity(350)
                    .category("D-SUV")
                    .description("Tiêu biểu trong phân khúc xe 5 chỗ với khoang ngồi rộng rãi kết hợp với công nghệ hỗ trợ lái cơ bản")
                    .build(),
            VehicleType.builder()
                    .name("VinFast VF 8 Plus")
                    .depositAmount(20000000.0)
                    .rentalRate(1800000.0)
                    .imageUrl("https://greenfuture.tech/_next/image?url=https%3A%2F%2Fupload-static.fgf.vn%2Fcar%2Fvf8plus01.jpg&w=384&q=75")
                    .seats(5)
                    .range(471)
                    .rangeStandard("WLTP")
                    .trunkCapacity(350)
                    .category("D-SUV")
                    .description("Mẫu xe 5 chỗ hiện đại được tích hợp thêm nhiều tính năng cao cấp như ghế chỉnh điện, sưởi/làm mát ghế")
                    .build(),
            VehicleType.builder()
                    .name("VinFast VF 9 Eco")
                    .depositAmount(25000000.0)
                    .rentalRate(2400000.0)
                    .imageUrl("https://greenfuture.tech/_next/image?url=https%3A%2F%2Fupload-static.fgf.vn%2Fcar%2Fvf9-eco-09.jpg&w=384&q=75")
                    .seats(7)
                    .range(437)
                    .rangeStandard("WLTP")
                    .trunkCapacity(212)
                    .category("E-SUV")
                    .description("Mẫu xe SUV 7 chỗ cỡ lớn phù hợp cho gia đình đông thành viên. Dung lượng pin cao giúp di chuyển hành trình xa")
                    .build(),
            VehicleType.builder()
                    .name("VinFast VF 9 Plus")
                    .depositAmount(30000000.0)
                    .rentalRate(2600000.0)
                    .imageUrl("https://greenfuture.tech/_next/image?url=https%3A%2F%2Fupload-static.fgf.vn%2Fcar%2Fvf9-plus-10.jpg&w=384&q=75")
                    .seats(7)
                    .range(602)
                    .rangeStandard("WLTP")
                    .trunkCapacity(212)
                    .category("E-SUV")
                    .description("Dòng xe điện cao cấp nhất được trang bị đầy đủ những tính năng lái thông minh. Khoang ngồi rộng rãi, sang trọng")
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



    private void initializeVehicles() {
        var stations = rentalStationRepository.findAll();
        var vehicleTypes = vehicleTypeRepository.findAll();

        // Prefixes cho biển số theo tỉnh/thành phố
        String[] licensePrefixes = {"59", "51", "29", "43"}; // HCM, HCM, Hanoi, Da Nang
        String[] cities = {"TP.HCM", "TP.HCM", "Hà Nội", "Đà Nẵng"};
        
        int globalVehicleCounter = 1;

        // Create vehicles for each station and type combination
        for (int stationIndex = 0; stationIndex < stations.size(); stationIndex++) {
            RentalStation station = stations.get(stationIndex);
            String prefix = licensePrefixes[stationIndex];
            String city = cities[stationIndex];
            
            for (int typeIndex = 0; typeIndex < vehicleTypes.size(); typeIndex++) {
                VehicleType type = vehicleTypes.get(typeIndex);
                
                // Create 2-3 vehicles per type per station
                int vehicleCount = 2 + (typeIndex % 2); // 2-3 vehicles
                
                for (int i = 0; i < vehicleCount; i++) {
                    // Generate license plate for car: e.g., "59A-123.45"
                    String letter = String.valueOf((char)('A' + ((globalVehicleCounter + typeIndex) / 50) % 26));
                    int firstPart = 100 + (globalVehicleCounter % 900); // 100-999
                    int secondPart = 10 + (globalVehicleCounter % 90);  // 10-99
                    String licensePlate = String.format("%s%s-%d.%d", prefix, letter, firstPart, secondPart);
                    
                    // Tạo ghi chú tình trạng xe
                    String conditionNote = String.format(
                        "Xe %s tại %s - Tình trạng tốt, đã bảo trì định kỳ. " +
                        "Pin: %d%%. Quãng đường đã đi: %,d km.",
                        type.getName(),
                        city,
                        85 + (i * 5), // Pin từ 85-95%
                        (globalVehicleCounter * 1000) + (i * 500) // Km đã đi
                    );
                    
                    Vehicle vehicle = Vehicle.builder()
                            .type(type)
                            .station(station)
                            .licensePlate(licensePlate)
                            .status(i == 0 && typeIndex % 3 == 0 ? 
                                    Vehicle.VehicleStatus.MAINTENANCE : 
                                    Vehicle.VehicleStatus.AVAILABLE)
                            .conditionNotes(conditionNote)
                            .photos(type.getImageUrl()) // Sử dụng ảnh từ VehicleType
                            .build();
                    vehicleRepository.save(vehicle);
                    
                    globalVehicleCounter++;
                }
            }
        }
    }
}
