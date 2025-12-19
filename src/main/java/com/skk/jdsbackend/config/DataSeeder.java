package com.skk.jdsbackend.config;

import com.skk.jdsbackend.entity.*;
import com.skk.jdsbackend.repository.CaseRepository;
import com.skk.jdsbackend.repository.ClientRepository;
import com.skk.jdsbackend.repository.UserRepository;
import com.skk.jdsbackend.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

@Component
@ConditionalOnProperty(name = "app.data.seed-enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

        private final UserRepository userRepository;
        private final ClientRepository clientRepository;
        private final CaseRepository caseRepository;
        private final TaskRepository taskRepository;
        private final PasswordEncoder passwordEncoder;

        private final Random random = new Random();

        @Override
        public void run(String... args) {
                // Only seed if database is empty
                if (userRepository.count() > 0 && taskRepository.count() > 0) {
                        log.info("Database already contains data. Skipping seeding.");
                        return;
                }

                log.info("Starting database seeding...");

                List<User> users;
                // Seed users if not exists
                if (userRepository.count() == 0) {
                        users = seedUsers();
                        log.info("Created {} users", users.size());
                } else {
                        users = userRepository.findAll();
                        log.info("Found {} existing users", users.size());
                }

                List<Client> clients;
                // Seed clients if not exists
                if (clientRepository.count() == 0) {
                        clients = seedClients();
                        log.info("Created {} clients", clients.size());
                } else {
                        clients = clientRepository.findAll();
                        log.info("Found {} existing clients", clients.size());
                }

                List<Case> cases;
                // Seed cases if not exists
                if (caseRepository.count() == 0) {
                        cases = seedCases(users, clients);
                        log.info("Created {} cases", cases.size());
                } else {
                        cases = caseRepository.findAll();
                        log.info("Found {} existing cases", cases.size());
                }

                // Seed tasks
                if (taskRepository.count() == 0) {
                        List<Task> tasks = seedTasks(cases, users);
                        log.info("Created {} tasks", tasks.size());
                }

                log.info("Database seeding completed successfully!");
                logCredentials(users);
        }

        private List<User> seedUsers() {
                List<User> users = new ArrayList<>();

                // Admin user
                User admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@jds.com");
                admin.setPassword(passwordEncoder.encode("password123"));
                Set<Role> adminRoles = new HashSet<>();
                adminRoles.add(Role.ADMIN);
                admin.setRoles(adminRoles);
                users.add(userRepository.save(admin));

                // Supervisor user
                User supervisor = new User();
                supervisor.setUsername("supervisor");
                supervisor.setEmail("supervisor@jds.com");
                supervisor.setPassword(passwordEncoder.encode("password123"));
                Set<Role> supervisorRoles = new HashSet<>();
                supervisorRoles.add(Role.SUPERVISOR);
                supervisor.setRoles(supervisorRoles);
                users.add(userRepository.save(supervisor));

                // Case workers
                String[] workerNames = { "john_doe", "jane_smith", "mike_wilson" };
                String[] workerEmails = { "john.doe@jds.com", "jane.smith@jds.com", "mike.wilson@jds.com" };

                for (int i = 0; i < workerNames.length; i++) {
                        User worker = new User();
                        worker.setUsername(workerNames[i]);
                        worker.setEmail(workerEmails[i]);
                        worker.setPassword(passwordEncoder.encode("password123"));
                        Set<Role> workerRoles = new HashSet<>();
                        workerRoles.add(Role.CASE_WORKER);
                        worker.setRoles(workerRoles);
                        users.add(userRepository.save(worker));
                }

                return users;
        }

        private List<Client> seedClients() {
                List<Client> clients = new ArrayList<>();

                String[][] clientData = {
                                { "James", "Anderson", "james.anderson@email.com", "+1-555-0101",
                                                "123 Main St, New York, NY 10001",
                                                "Tech Solutions Inc" },
                                { "Sarah", "Martinez", "sarah.martinez@email.com", "+1-555-0102",
                                                "456 Oak Ave, Los Angeles, CA 90001",
                                                "Marketing Pro" },
                                { "Robert", "Johnson", "robert.johnson@email.com", "+1-555-0103",
                                                "789 Pine Rd, Chicago, IL 60601",
                                                "Johnson & Associates" },
                                { "Emily", "Williams", "emily.williams@email.com", "+1-555-0104",
                                                "321 Elm St, Houston, TX 77001",
                                                null },
                                { "Michael", "Brown", "michael.brown@email.com", "+1-555-0105",
                                                "654 Maple Dr, Phoenix, AZ 85001",
                                                "Brown Enterprises" },
                                { "Jessica", "Davis", "jessica.davis@email.com", "+1-555-0106",
                                                "987 Cedar Ln, Philadelphia, PA 19101",
                                                null },
                                { "David", "Miller", "david.miller@email.com", "+1-555-0107",
                                                "147 Birch Blvd, San Antonio, TX 78201",
                                                "Miller Corp" },
                                { "Jennifer", "Wilson", "jennifer.wilson@email.com", "+1-555-0108",
                                                "258 Spruce St, San Diego, CA 92101", "Wilson Legal" },
                                { "Christopher", "Moore", "christopher.moore@email.com", "+1-555-0109",
                                                "369 Walnut Ave, Dallas, TX 75201", null },
                                { "Amanda", "Taylor", "amanda.taylor@email.com", "+1-555-0110",
                                                "741 Ash Rd, San Jose, CA 95101",
                                                "Taylor Consulting" }
                };

                for (String[] data : clientData) {
                        Client client = new Client();
                        client.setFirstname(data[0]);
                        client.setLastname(data[1]);
                        client.setEmail(data[2]);
                        client.setPhone(data[3]);
                        client.setAddress(data[4]);
                        client.setCompany(data[5]);
                        clients.add(clientRepository.save(client));
                }

                return clients;
        }

        private List<Case> seedCases(List<User> users, List<Client> clients) {
                List<Case> cases = new ArrayList<>();

                String[][] caseData = {
                                { "Contract Dispute Resolution",
                                                "Client needs assistance with contract interpretation and potential breach of agreement.",
                                                "OPEN", "HIGH" },
                                { "Employment Termination Review",
                                                "Review of employment termination circumstances and potential wrongful dismissal claim.",
                                                "IN_PROGRESS", "URGENT" },
                                { "Property Lease Agreement",
                                                "Assistance with commercial property lease negotiation and terms review.",
                                                "OPEN", "MEDIUM" },
                                { "Business Partnership Dissolution",
                                                "Legal guidance for dissolving business partnership and asset distribution.",
                                                "IN_PROGRESS",
                                                "HIGH" },
                                { "Intellectual Property Protection",
                                                "Copyright and trademark registration for new business venture.",
                                                "PENDING", "MEDIUM" },
                                { "Debt Collection Matter",
                                                "Assistance with collecting outstanding business debts from multiple parties.",
                                                "OPEN", "LOW" },
                                { "Regulatory Compliance Review",
                                                "Review of business operations for regulatory compliance in new jurisdiction.",
                                                "RESOLVED",
                                                "MEDIUM" },
                                { "Merger and Acquisition Due Diligence",
                                                "Legal due diligence for potential company acquisition.",
                                                "IN_PROGRESS", "URGENT" },
                                { "Employment Contract Drafting",
                                                "Drafting employment contracts for new executive hires.", "OPEN",
                                                "MEDIUM" },
                                { "Vendor Agreement Negotiation",
                                                "Negotiation and review of vendor service agreements.", "PENDING",
                                                "LOW" },
                                { "Real Estate Transaction",
                                                "Legal support for commercial real estate purchase transaction.",
                                                "IN_PROGRESS", "HIGH" },
                                { "Non-Compete Agreement Review",
                                                "Review and advice on non-compete clause enforceability.", "CLOSED",
                                                "MEDIUM" },
                                { "Corporate Governance Advisory",
                                                "Advisory services for corporate governance best practices.", "OPEN",
                                                "LOW" },
                                { "Shareholder Dispute Mediation",
                                                "Mediation services for shareholder disagreement resolution.",
                                                "IN_PROGRESS", "URGENT" },
                                { "Data Privacy Compliance",
                                                "GDPR and data privacy compliance assessment and implementation.",
                                                "OPEN",
                                                "HIGH" },
                                { "Franchise Agreement Review", "Review of franchise agreement terms and conditions.",
                                                "PENDING",
                                                "MEDIUM" },
                                { "Insurance Claim Assistance",
                                                "Assistance with business insurance claim processing and negotiation.",
                                                "RESOLVED", "MEDIUM" },
                                { "Tax Planning Consultation",
                                                "Legal consultation for business tax planning strategies.", "OPEN",
                                                "LOW" },
                                { "Licensing Agreement Drafting",
                                                "Drafting software licensing agreements for product distribution.",
                                                "IN_PROGRESS", "MEDIUM" },
                                { "Litigation Support Services",
                                                "Document review and legal research for ongoing litigation.", "OPEN",
                                                "HIGH" }
                };

                // Get only case workers for assignment (skip admin and supervisor)
                List<User> caseWorkers = users.stream()
                                .filter(user -> user.getRoles().contains(Role.CASE_WORKER))
                                .toList();

                for (int i = 0; i < caseData.length; i++) {
                        String[] data = caseData[i];
                        Case caseEntity = new Case();
                        caseEntity.setTitle(data[0]);
                        caseEntity.setDescription(data[1]);
                        caseEntity.setStatus(CaseStatus.valueOf(data[2]));
                        caseEntity.setPriority(CasePriority.valueOf(data[3]));

                        // Assign to a random case worker
                        caseEntity.setAssignedUser(caseWorkers.get(random.nextInt(caseWorkers.size())));

                        // Assign to a random client
                        caseEntity.setClient(clients.get(random.nextInt(clients.size())));

                        cases.add(caseRepository.save(caseEntity));
                }

                return cases;
        }

        private List<Task> seedTasks(List<Case> cases, List<User> users) {
                List<Task> tasks = new ArrayList<>();

                String[][] taskTitles = {
                                { "Review Initial Documents",
                                                "Review all preliminary documents provided by the client." },
                                { "Draft Client Letter",
                                                "Draft a formal letter to the client regarding the case status." },
                                { "File Court Motions", "Prepare and file necessary motions with the court." },
                                { "Schedule Deposition", "Coordinate with opposing counsel to schedule depositions." },
                                { "Research Case Law", "Conduct research on relevant case law and precedents." },
                                { "Prepare Witness List", "Compile a list of potential witnesses for the trial." },
                                { "Review Settlement Offer",
                                                "Analyze the settlement offer received from the opposing party." },
                                { "Attend Hearing", "Represent the client at the scheduled court hearing." },
                                { "Update Case File", "Ensure all recent correspondence and documents are filed." },
                                { "Client Meeting", "Meet with the client to discuss strategy and next steps." }
                };

                // Get only case workers
                List<User> caseWorkers = users.stream()
                                .filter(user -> user.getRoles().contains(Role.CASE_WORKER))
                                .toList();

                for (Case caseEntity : cases) {
                        // Create 2-4 tasks for each case
                        int numTasks = random.nextInt(3) + 2;

                        for (int i = 0; i < numTasks; i++) {
                                String[] titleDesc = taskTitles[random.nextInt(taskTitles.length)];
                                Task task = new Task();
                                task.setTitle(titleDesc[0] + " - " + caseEntity.getTitle().substring(0,
                                                Math.min(10, caseEntity.getTitle().length())) + "...");
                                task.setDescription(titleDesc[1]);

                                // Random status and priority
                                TaskStatus[] statuses = TaskStatus.values();
                                task.setStatus(statuses[random.nextInt(statuses.length)]);

                                TaskPriority[] priorities = TaskPriority.values();
                                task.setPriority(priorities[random.nextInt(priorities.length)]);

                                // Due date +/- 10 days from now
                                task.setDueDate(java.time.LocalDate.now().plusDays(random.nextInt(21) - 10));

                                task.setCaseEntity(caseEntity);

                                // Assign to the case's user or random worker
                                if (random.nextBoolean() && caseEntity.getAssignedUser() != null) {
                                        task.setAssignedUser(caseEntity.getAssignedUser());
                                } else {
                                        task.setAssignedUser(caseWorkers.get(random.nextInt(caseWorkers.size())));
                                }

                                tasks.add(taskRepository.save(task));
                        }
                }

                return tasks;
        }

        private void logCredentials(List<User> users) {
                log.info("=".repeat(60));
                log.info("SEEDED USER CREDENTIALS (Password for all: password123)");
                log.info("=".repeat(60));
                for (User user : users) {
                        log.info("Username: {} | Email: {} | Roles: {}",
                                        user.getUsername(),
                                        user.getEmail(),
                                        user.getRoles());
                }
                log.info("=".repeat(60));
        }
}
