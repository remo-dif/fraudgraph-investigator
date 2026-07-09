package com.fraudgraph.investigator.config;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private final Neo4jClient neo4jClient;

    public DataSeeder(Neo4jClient neo4jClient) {
        this.neo4jClient = neo4jClient;
    }

    @Override
    public void run(String... args) {
        long customers = neo4jClient.query("MATCH (c:Customer) RETURN count(c)")
                .fetchAs(Long.class)
                .mappedBy((typeSystem, record) -> record.get(0).asLong())
                .one()
                .orElse(0L);
        if (customers > 0) {
            log.info("Neo4j seed data already exists; skipping seed loader");
            return;
        }
        log.info("Seeding Neo4j demo data");
        createConstraints();
        log.info("Seed constraints created");
        seedCustomers();
        log.info("Seed customers created");
        seedAccountsCardsDevicesIps();
        log.info("Seed accounts, cards, devices, and IP addresses created");
        seedMerchants();
        log.info("Seed merchants created");
        seedTransactions();
        log.info("Seed transactions created");
        seedSuspiciousTransferLoop();
        log.info("Seed circular transfer loop created");
        seedAlertsAndCase();
        log.info("Seed alerts and case created");
        seedHypergraphEvents();
        log.info("Seed hypergraph events created");
    }

    private void createConstraints() {
        List.of("Customer", "Account", "Card", "Device", "IPAddress", "Merchant", "Transaction", "Alert", "Case",
                        "PaymentEvent", "LoginEvent", "DeviceFingerprintEvent", "AlertEvent")
                .forEach(label -> neo4jClient.query("CREATE CONSTRAINT %s_id IF NOT EXISTS FOR (n:%s) REQUIRE n.id IS UNIQUE".formatted(label.toLowerCase(), label)).run());
    }

    private void seedCustomers() {
        List<String> names = List.of(
                "Avery Stone", "Maya Patel", "Noah Kim", "Sofia Reyes", "Ethan Brooks",
                "Lina Chen", "Owen Martin", "Iris Novak", "Caleb Ford", "Nadia Farah",
                "Leo Mancini", "Amara Cole", "Jonas Weber", "Priya Shah", "Miles Grant",
                "Elena Rossi", "Samir Haddad", "Clara Voss", "Theo Bennett", "Hana Mori"
        );
        IntStream.range(0, names.size()).forEach(i -> neo4jClient.query("""
                        CREATE (:Customer {
                          id: $id, name: $name, email: $email, segment: $segment,
                          country: $country, riskScore: $riskScore, previouslySuspicious: $previouslySuspicious
                        })
                        """)
                .bind("cust-%03d".formatted(i + 1)).to("id")
                .bind(names.get(i)).to("name")
                .bind(names.get(i).toLowerCase().replace(" ", ".") + "@examplebank.test").to("email")
                .bind(i % 5 == 0 ? "small-business" : "retail").to("segment")
                .bind(List.of("US", "GB", "IT", "DE", "NL").get(i % 5)).to("country")
                .bind(i == 2 || i == 15 ? 88 : 30 + (i % 7) * 6).to("riskScore")
                .bind(i == 2 || i == 15).to("previouslySuspicious")
                .run());
    }

    private void seedAccountsCardsDevicesIps() {
        IntStream.rangeClosed(1, 30).forEach(i -> {
            String accountId = "acct-%03d".formatted(i);
            String customerId = "cust-%03d".formatted(((i - 1) % 20) + 1);
            String deviceId = i <= 6 ? "dev-001" : "dev-%03d".formatted(((i - 1) % 15) + 1);
            String ipId = i <= 6 ? "ip-001" : "ip-%03d".formatted(((i - 1) % 12) + 1);
            neo4jClient.query("""
                            MERGE (account:Account {
                              id: $accountId, accountNumber: $accountNumber, product: $product,
                              status: $status, openedAt: $openedAt, riskScore: $riskScore
                            })
                            MERGE (card:Card {
                              id: $cardId, maskedPan: $maskedPan, network: $network,
                              status: $cardStatus, riskScore: $cardRisk
                            })
                            MERGE (device:Device {id: $deviceId})
                            SET device.fingerprint = coalesce(device.fingerprint, $fingerprint),
                                device.platform = coalesce(device.platform, $platform),
                                device.riskScore = CASE
                                    WHEN device.riskScore IS NULL OR device.riskScore < $deviceRisk THEN $deviceRisk
                                    ELSE device.riskScore
                                END,
                                device.previouslySuspicious = coalesce(device.previouslySuspicious, false) OR $deviceSuspicious
                            MERGE (ip:IPAddress {id: $ipId})
                            SET ip.ip = coalesce(ip.ip, $ip),
                                ip.country = coalesce(ip.country, $ipCountry),
                                ip.riskScore = CASE
                                    WHEN ip.riskScore IS NULL OR ip.riskScore < $ipRisk THEN $ipRisk
                                    ELSE ip.riskScore
                                END
                            WITH account, card, device, ip
                            MATCH (customer:Customer {id: $customerId})
                            MERGE (customer)-[:OWNS]->(account)
                            MERGE (account)-[:USED]->(card)
                            MERGE (account)-[:LOGGED_IN_FROM]->(device)
                            MERGE (device)-[:USED_IP]->(ip)
                            """)
                    .bind(accountId).to("accountId")
                    .bind("FG-%08d".formatted(42000000 + i)).to("accountNumber")
                    .bind(i % 4 == 0 ? "business-checking" : "consumer-checking").to("product")
                    .bind(i == 28 ? "RESTRICTED" : "ACTIVE").to("status")
                    .bind(OffsetDateTime.now().minusDays(900L - i * 9L).toString()).to("openedAt")
                    .bind(i <= 6 ? 82 : 35 + (i % 9) * 5).to("riskScore")
                    .bind("card-%03d".formatted(i)).to("cardId")
                    .bind("**** **** **** %04d".formatted(1000 + i)).to("maskedPan")
                    .bind(i % 2 == 0 ? "VISA" : "MASTERCARD").to("network")
                    .bind(i == 28 ? "BLOCKED" : "ACTIVE").to("cardStatus")
                    .bind(i <= 6 ? 76 : 20 + (i % 5) * 7).to("cardRisk")
                    .bind(deviceId).to("deviceId")
                    .bind("fp-" + deviceId + "-a7").to("fingerprint")
                    .bind(i % 3 == 0 ? "Android" : i % 3 == 1 ? "iOS" : "Windows").to("platform")
                    .bind(i <= 6 ? 91 : 25 + (i % 6) * 8).to("deviceRisk")
                    .bind(i <= 6).to("deviceSuspicious")
                    .bind(ipId).to("ipId")
                    .bind("198.51.100." + ((i % 12) + 20)).to("ip")
                    .bind(List.of("US", "GB", "IT", "DE").get(i % 4)).to("ipCountry")
                    .bind(i <= 6 ? 84 : 28 + (i % 4) * 9).to("ipRisk")
                    .bind(customerId).to("customerId")
                    .run();
        });
    }

    private void seedMerchants() {
        List<String> merchants = List.of(
                "Northstar Electronics", "Metro Fuel", "Aster Digital Goods", "QuickSend Remit",
                "Harbor Market", "Cobalt Crypto Exchange", "Silverline Travel", "Vega Luxury",
                "Orchid Marketplace", "Atlas Gift Cards"
        );
        IntStream.range(0, merchants.size()).forEach(i -> neo4jClient.query("""
                        CREATE (:Merchant {
                          id: $id, name: $name, category: $category, country: $country,
                          riskScore: $riskScore, previouslySuspicious: $previouslySuspicious
                        })
                        """)
                .bind("merch-%03d".formatted(i + 1)).to("id")
                .bind(merchants.get(i)).to("name")
                .bind(List.of("retail", "fuel", "digital-goods", "money-service", "marketplace").get(i % 5)).to("category")
                .bind(List.of("US", "GB", "NL", "CY", "AE").get(i % 5)).to("country")
                .bind(i == 5 || i == 9 ? 90 : 35 + (i % 4) * 9).to("riskScore")
                .bind(i == 5 || i == 9).to("previouslySuspicious")
                .run());
    }

    private void seedTransactions() {
        IntStream.rangeClosed(1, 112).forEach(i -> {
            boolean structured = i >= 91 && i <= 100;
            boolean merchantBurst = i >= 61 && i <= 82;
            String merchantId = merchantBurst ? "merch-010" : "merch-%03d".formatted(((i - 1) % 10) + 1);
            double amount = structured ? 9900 - (i % 7) * 73 : 18.75 + (i * 137 % 5400);
            String accountId = "acct-%03d".formatted(((i - 1) % 30) + 1);
            neo4jClient.query("""
                            CREATE (tx:Transaction {
                              id: $id, type: $type, amount: $amount, currency: 'USD',
                              channel: $channel, status: $status, occurredAt: $occurredAt,
                              riskScore: $riskScore
                            })
                            WITH tx
                            MATCH (account:Account {id: $accountId})
                            MATCH (merchant:Merchant {id: $merchantId})
                            MERGE (account)-[:INITIATED]->(tx)
                            MERGE (tx)-[:SENT_TO]->(merchant)
                            """)
                    .bind("txn-%04d".formatted(i)).to("id")
                    .bind(structured ? "WIRE" : i % 5 == 0 ? "TRANSFER" : "CARD_PURCHASE").to("type")
                    .bind(amount).to("amount")
                    .bind(i % 4 == 0 ? "mobile" : i % 4 == 1 ? "web" : "card-present").to("channel")
                    .bind(i % 17 == 0 ? "REVERSED" : "SETTLED").to("status")
                    .bind(OffsetDateTime.now().minusHours(220L - i).toString()).to("occurredAt")
                    .bind(structured || merchantBurst ? 79 : 25 + (i % 10) * 4).to("riskScore")
                    .bind(accountId).to("accountId")
                    .bind(merchantId).to("merchantId")
                    .run();
        });
    }

    private void seedSuspiciousTransferLoop() {
        List<Map<String, Object>> transfers = List.of(
                Map.of("id", "txn-loop-001", "from", "acct-003", "to", "acct-014", "amount", 7200.0),
                Map.of("id", "txn-loop-002", "from", "acct-014", "to", "acct-021", "amount", 6950.0),
                Map.of("id", "txn-loop-003", "from", "acct-021", "to", "acct-003", "amount", 6810.0)
        );
        transfers.forEach(tx -> neo4jClient.query("""
                        CREATE (transfer:Transaction {
                          id: $id, type: 'TRANSFER', amount: $amount, currency: 'USD',
                          channel: 'web', status: 'SETTLED', occurredAt: $occurredAt, riskScore: 94
                        })
                        WITH transfer
                        MATCH (source:Account {id: $from})
                        MATCH (target:Account {id: $to})
                        MERGE (source)-[:INITIATED]->(transfer)
                        MERGE (transfer)-[:TRANSFERRED_TO]->(target)
                        """)
                .bind(tx.get("id")).to("id")
                .bind(tx.get("amount")).to("amount")
                .bind(OffsetDateTime.now().minusHours(10).toString()).to("occurredAt")
                .bind(tx.get("from")).to("from")
                .bind(tx.get("to")).to("to")
                .run());
    }

    private void seedAlertsAndCase() {
        List<Map<String, Object>> alerts = List.of(
                Map.of("id", "alert-001", "title", "Shared device mule cluster", "tx", "txn-006", "severity", "HIGH", "score", 88),
                Map.of("id", "alert-002", "title", "Circular transfer loop", "tx", "txn-loop-001", "severity", "CRITICAL", "score", 96),
                Map.of("id", "alert-003", "title", "Merchant concentration spike", "tx", "txn-061", "severity", "MEDIUM", "score", 77),
                Map.of("id", "alert-004", "title", "Sub-threshold wire structuring", "tx", "txn-091", "severity", "HIGH", "score", 84),
                Map.of("id", "alert-005", "title", "Previously suspicious counterparty proximity", "tx", "txn-100", "severity", "HIGH", "score", 86)
        );
        alerts.forEach(alert -> neo4jClient.query("""
                        CREATE (a:Alert {
                          id: $id, title: $title, status: 'OPEN', severity: $severity,
                          riskScore: $score, description: $description, createdAt: $createdAt,
                          riskReasons: $riskReasons, recommendedActions: $recommendedActions
                        })
                        WITH a
                        MATCH (tx:Transaction {id: $tx})
                        MERGE (a)-[:FLAGS]->(tx)
                        """)
                .bind(alert.get("id")).to("id")
                .bind(alert.get("title")).to("title")
                .bind(alert.get("severity")).to("severity")
                .bind(alert.get("score")).to("score")
                .bind("Automated graph detection found a pattern requiring analyst review.").to("description")
                .bind(OffsetDateTime.now().minusHours(4).toString()).to("createdAt")
                .bind(List.of("Graph connectivity exceeded baseline", "Transaction behavior matched known typology")).to("riskReasons")
                .bind(List.of("Review graph neighborhood", "Verify customer intent", "Consider temporary controls")).to("recommendedActions")
                .bind(alert.get("tx")).to("tx")
                .run());

        neo4jClient.query("""
                        CREATE (c:Case {
                          id: 'case-seed-001', title: 'Coordinated mule and structuring review',
                          status: 'TRIAGED', riskScore: 91, createdAt: $createdAt,
                          recommendedActions: $actions
                        })
                        WITH c
                        MATCH (a:Alert)
                        WHERE a.id IN ['alert-001', 'alert-002', 'alert-004']
                        MERGE (c)-[:CONTAINS]->(a)
                        """)
                .bind(OffsetDateTime.now().minusHours(2).toString()).to("createdAt")
                .bind(List.of("Escalate to senior analyst", "Prepare SAR decision memo", "Preserve access logs")).to("actions")
                .run();
    }

    private void seedHypergraphEvents() {
        neo4jClient.query("""
                        MATCH (account:Account)-[:INITIATED]->(tx:Transaction)
                        OPTIONAL MATCH (customer:Customer)-[:OWNS]->(account)
                        OPTIONAL MATCH (account)-[:USED]->(card:Card)
                        OPTIONAL MATCH (account)-[:LOGGED_IN_FROM]->(device:Device)-[:USED_IP]->(ip:IPAddress)
                        OPTIONAL MATCH (tx)-[:SENT_TO]->(merchant:Merchant)
                        OPTIONAL MATCH (tx)-[:TRANSFERRED_TO]->(target:Account)
                        MERGE (event:PaymentEvent {id: 'payment-event-' + tx.id})
                        SET event.label = tx.type + ' ' + tx.currency + ' ' + toString(tx.amount),
                            event.riskScore = tx.riskScore,
                            event.timestamp = tx.occurredAt,
                            event.amount = tx.amount,
                            event.currency = tx.currency,
                            event.channel = tx.channel
                        MERGE (event)-[:DERIVED_FROM]->(tx)
                        WITH event, [account, customer, card, device, ip, merchant, target] AS participants
                        UNWIND participants AS participant
                        WITH event, participant
                        WHERE participant IS NOT NULL
                        MERGE (participant)-[:PARTICIPATED_IN]->(event)
                        """).run();

        neo4jClient.query("""
                        MATCH (account:Account)-[:LOGGED_IN_FROM]->(device:Device)-[:USED_IP]->(ip:IPAddress)
                        MERGE (event:LoginEvent {id: 'login-event-' + account.id + '-' + device.id})
                        SET event.label = 'Login from ' + device.platform,
                            event.riskScore = coalesce(device.riskScore, 0),
                            event.timestamp = toString(datetime())
                        WITH event, [account, device, ip] AS participants
                        UNWIND participants AS participant
                        MERGE (participant)-[:PARTICIPATED_IN]->(event)
                        """).run();

        neo4jClient.query("""
                        MATCH (alert:Alert)-[:FLAGS]->(tx:Transaction)<-[:DERIVED_FROM]-(payment:PaymentEvent)
                        MERGE (event:AlertEvent {id: 'alert-event-' + alert.id})
                        SET event.label = alert.title,
                            event.riskScore = alert.riskScore,
                            event.timestamp = alert.createdAt,
                            event.severity = alert.severity
                        MERGE (event)-[:DERIVED_FROM]->(tx)
                        WITH event, payment, alert
                        MATCH (participant)-[:PARTICIPATED_IN]->(payment)
                        MERGE (participant)-[:PARTICIPATED_IN]->(event)
                        MERGE (alert)-[:PARTICIPATED_IN]->(event)
                        """).run();
    }
}
