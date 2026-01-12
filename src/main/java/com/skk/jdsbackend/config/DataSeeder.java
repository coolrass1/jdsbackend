package com.skk.jdsbackend.config;

import com.skk.jdsbackend.entity.*;
import com.skk.jdsbackend.repository.*;
import com.skk.jdsbackend.service.SequenceGeneratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.data.seed-enabled", havingValue = "true", matchIfMissing = false)
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final CaseRepository caseRepository;
    private final TaskRepository taskRepository;
    private final ActivityRepository activityRepository;
    private final DocumentSignatureRepository documentSignatureRepository;
    private final DocumentVersionRepository documentVersionRepository;
    private final DocumentTemplateRepository documentTemplateRepository;
    private final PasswordEncoder passwordEncoder;
    private final SequenceGeneratorService sequenceGeneratorService;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Starting data seeding...");

        // Clear existing data
        clearData();

        // Seed users
        List<User> users = seedUsers();

        // Seed clients
        List<Client> clients = seedClients(users);

        // Seed cases
        List<Case> cases = seedCases(users, clients);

        // Seed tasks
        seedTasks(cases, users);

        log.info("Data seeding completed successfully!");
        log.info("=".repeat(60));
        log.info("DEFAULT LOGIN CREDENTIALS:");
        log.info("  Username: admin | Password: password123");
        log.info("  Username: caseworker | Password: password123");
        log.info("=".repeat(60));
    }

    private void clearData() {
        log.info("Cleaning up database...");

        // Delete dependent entities first
        activityRepository.deleteAll();
        documentSignatureRepository.deleteAll();
        documentVersionRepository.deleteAll();
        documentTemplateRepository.deleteAll();
        taskRepository.deleteAll();

        // Notes and Documents are cascaded from Case
        caseRepository.deleteAll();

        // Delete Users first (Owner of many-to-many relationship) to clear join table
        userRepository.deleteAll();
        clientRepository.deleteAll();

        log.info("Database cleaned.");
    }

    private List<User> seedUsers() {
        log.info("Seeding users...");
        List<User> users = new ArrayList<>();

        // Create admin user
        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@jds.com");
        admin.setPassword(passwordEncoder.encode("password123"));
        Set<Role> adminRoles = new HashSet<>();
        adminRoles.add(Role.ADMIN);
        adminRoles.add(Role.CASE_WORKER);
        admin.setRoles(adminRoles);
        users.add(userRepository.save(admin));
        log.info("Created admin user - username: admin, password: password123");

        // Create case worker user
        User caseWorker = new User();
        caseWorker.setUsername("caseworker");
        caseWorker.setEmail("caseworker@jds.com");
        caseWorker.setPassword(passwordEncoder.encode("password123"));
        Set<Role> cwRoles = new HashSet<>();
        cwRoles.add(Role.CASE_WORKER);
        caseWorker.setRoles(cwRoles);
        users.add(userRepository.save(caseWorker));
        log.info("Created case worker user - username: caseworker, password: password123");

        // Create another case worker
        User caseWorker2 = new User();
        caseWorker2.setUsername("caseworker2");
        caseWorker2.setEmail("caseworker2@jds.com");
        caseWorker2.setPassword(passwordEncoder.encode("password123"));
        Set<Role> cwRoles2 = new HashSet<>();
        cwRoles2.add(Role.CASE_WORKER);
        caseWorker2.setRoles(cwRoles2);
        users.add(userRepository.save(caseWorker2));
        log.info("Created case worker 2 user - username: caseworker2, password: password123");

        log.info("Users seeded successfully!");
        return users;
    }

    private List<Client> seedClients(List<User> users) {
        log.info("Seeding clients...");
        List<Client> clients = new ArrayList<>();

        // Create sample clients
        Client client1 = new Client();
        client1.setFirstname("John");
        client1.setLastname("Smith");
        client1.setEmail("john.smith@example.com");
        client1.setNi_number("AB123456A");
        client1.setPhone("+44-20-7123-4567");
        client1.setAddress("123 High Street, London, UK");
        client1.setCompany("Tech Solutions Ltd");
        client1.setHasConflictOfInterest(false);
        // Assign users
        client1.addUser(users.get(0)); // Admin
        client1.addUser(users.get(1)); // Caseworker
        // Audit fields
        client1.setReferenceNumber(sequenceGeneratorService.generateNextClientReference());
        client1.setCreatedByUser(users.get(0)); // Admin
        client1.setLastModifiedByUser(users.get(0));
        clients.add(clientRepository.save(client1));

        Client client2 = new Client();
        client2.setFirstname("Sarah");
        client2.setLastname("Johnson");
        client2.setEmail("sarah.johnson@example.com");
        client2.setNi_number("CD789012B");
        client2.setPhone("+44-20-7234-5678");
        client2.setAddress("456 Park Lane, Manchester, UK");
        client2.setCompany("Marketing Pro");
        client2.setHasConflictOfInterest(false);
        // Assign users
        client2.addUser(users.get(1)); // Caseworker
        // Audit fields
        client2.setReferenceNumber(sequenceGeneratorService.generateNextClientReference());
        client2.setCreatedByUser(users.get(1)); // Caseworker
        client2.setLastModifiedByUser(users.get(1));
        clients.add(clientRepository.save(client2));

        Client client3 = new Client();
        client3.setFirstname("Michael");
        client3.setLastname("Brown");
        client3.setEmail("michael.brown@example.com");
        client3.setNi_number("EF345678C");
        client3.setPhone("+44-20-7345-6789");
        client3.setAddress("789 Queen Street, Birmingham, UK");
        client3.setCompany("Consulting Group");
        client3.setHasConflictOfInterest(true);
        client3.setConflictOfInterestComment("Previously worked with competing firm");
        // Assign users
        client3.addUser(users.get(2)); // Caseworker 2
        // Audit fields
        client3.setReferenceNumber(sequenceGeneratorService.generateNextClientReference());
        client3.setCreatedByUser(users.get(2)); // Caseworker
        client3.setLastModifiedByUser(users.get(2));
        clients.add(clientRepository.save(client3));

        // Save users to persist the relationship (as User is the owner)
        // We can just save all of them, or specific ones.
        // For simplicity and safety, save all since list is small.
        userRepository.saveAll(users);

        log.info("Clients seeded successfully!");
        return clients;
    }

    private List<Case> seedCases(List<User> users, List<Client> clients) {
        log.info("Seeding cases...");
        List<Case> cases = new ArrayList<>();

        // Get case workers only
        List<User> caseWorkers = users.stream()
                .filter(user -> user.getRoles().contains(Role.CASE_WORKER))
                .toList();

        // Case 1
        Case case1 = new Case();
        case1.setTitle("Contract Dispute Resolution");
        case1.setDescription("Client needs assistance with contract interpretation and potential breach of agreement.");
        case1.setStatus(CaseStatus.OPEN);
        case1.setPriority(CasePriority.HIGH);
        case1.setClient(clients.get(0));
        case1.setAssignedUser(caseWorkers.get(0));
        case1.setReferenceNumber(sequenceGeneratorService.generateNextCaseReference());
        case1.setCreatedByUser(caseWorkers.get(0));
        case1.setLastModifiedByUser(caseWorkers.get(0));
        cases.add(caseRepository.save(case1));

        // Case 2
        Case case2 = new Case();
        case2.setTitle("Employment Termination Review");
        case2.setDescription("Review of employment termination circumstances and potential wrongful dismissal claim.");
        case2.setStatus(CaseStatus.IN_PROGRESS);
        case2.setPriority(CasePriority.URGENT);
        case2.setClient(clients.get(1));
        case2.setAssignedUser(caseWorkers.get(0));
        case2.setReferenceNumber(sequenceGeneratorService.generateNextCaseReference());
        case2.setCreatedByUser(caseWorkers.get(0));
        case2.setLastModifiedByUser(caseWorkers.get(0));
        cases.add(caseRepository.save(case2));

        // Case 3
        Case case3 = new Case();
        case3.setTitle("Business Partnership Dissolution");
        case3.setDescription("Legal guidance for dissolving business partnership and asset distribution.");
        case3.setStatus(CaseStatus.PENDING);
        case3.setPriority(CasePriority.MEDIUM);
        case3.setClient(clients.get(2));
        case3.setAssignedUser(caseWorkers.size() > 1 ? caseWorkers.get(1) : caseWorkers.get(0));
        case3.setReferenceNumber(sequenceGeneratorService.generateNextCaseReference());
        case3.setCreatedByUser(caseWorkers.size() > 1 ? caseWorkers.get(1) : caseWorkers.get(0));
        case3.setLastModifiedByUser(caseWorkers.size() > 1 ? caseWorkers.get(1) : caseWorkers.get(0));
        cases.add(caseRepository.save(case3));

        // Case 4
        Case case4 = new Case();
        case4.setTitle("Intellectual Property Protection");
        case4.setDescription("Copyright and trademark registration for new business venture.");
        case4.setStatus(CaseStatus.OPEN);
        case4.setPriority(CasePriority.LOW);
        case4.setClient(clients.get(0));
        case4.setAssignedUser(caseWorkers.get(0));
        case4.setReferenceNumber(sequenceGeneratorService.generateNextCaseReference());
        case4.setCreatedByUser(caseWorkers.get(0));
        case4.setLastModifiedByUser(caseWorkers.get(0));
        cases.add(caseRepository.save(case4));

        log.info("Cases seeded successfully!");
        return cases;
    }

    private void seedTasks(List<Case> cases, List<User> users) {
        log.info("Seeding tasks...");

        // Get case workers only
        List<User> caseWorkers = users.stream()
                .filter(user -> user.getRoles().contains(Role.CASE_WORKER))
                .toList();

        // Tasks for Case 1
        Task task1 = new Task();
        task1.setTitle("Review Initial Documents");
        task1.setDescription("Review all preliminary documents provided by the client.");
        task1.setStatus(TaskStatus.TODO);
        task1.setPriority(TaskPriority.HIGH);
        task1.setDueDate(LocalDate.now().plusDays(3));
        task1.setCaseEntity(cases.get(0));
        task1.setAssignedUser(caseWorkers.get(0));
        taskRepository.save(task1);

        Task task2 = new Task();
        task2.setTitle("Draft Client Letter");
        task2.setDescription("Draft a formal letter to the client regarding the case status.");
        task2.setStatus(TaskStatus.IN_PROGRESS);
        task2.setPriority(TaskPriority.MEDIUM);
        task2.setDueDate(LocalDate.now().plusDays(5));
        task2.setCaseEntity(cases.get(0));
        task2.setAssignedUser(caseWorkers.get(0));
        taskRepository.save(task2);

        // Tasks for Case 2
        Task task3 = new Task();
        task3.setTitle("Research Case Law");
        task3.setDescription("Conduct research on relevant case law and precedents.");
        task3.setStatus(TaskStatus.COMPLETED);
        task3.setPriority(TaskPriority.HIGH);
        task3.setDueDate(LocalDate.now().minusDays(1));
        task3.setCaseEntity(cases.get(1));
        task3.setAssignedUser(caseWorkers.get(0));
        taskRepository.save(task3);

        Task task4 = new Task();
        task4.setTitle("Schedule Deposition");
        task4.setDescription("Coordinate with opposing counsel to schedule depositions.");
        task4.setStatus(TaskStatus.IN_PROGRESS);
        task4.setPriority(TaskPriority.URGENT);
        task4.setDueDate(LocalDate.now().plusDays(2));
        task4.setCaseEntity(cases.get(1));
        task4.setAssignedUser(caseWorkers.get(0));
        taskRepository.save(task4);

        // Tasks for Case 3
        Task task5 = new Task();
        task5.setTitle("Client Meeting");
        task5.setDescription("Meet with the client to discuss strategy and next steps.");
        task5.setStatus(TaskStatus.TODO);
        task5.setPriority(TaskPriority.MEDIUM);
        task5.setDueDate(LocalDate.now().plusDays(7));
        task5.setCaseEntity(cases.get(2));
        task5.setAssignedUser(caseWorkers.size() > 1 ? caseWorkers.get(1) : caseWorkers.get(0));
        taskRepository.save(task5);

        log.info("Tasks seeded successfully!");
    }
}
