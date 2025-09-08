import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
abstract class Property {
    protected String propertyId;
    protected String address;
    protected String type;
    protected double rent;
    protected boolean isAvailable;

    public Property(String propertyId, String address, String type, double rent) {
        this.propertyId = propertyId;
        this.address = address;
        this.type = type;
        this.rent = rent;
        this.isAvailable = true;
    }

    
    public String getPropertyId() { return propertyId; }
    public String getAddress() { return address; }
    public String getType() { return type; }
    public double getRent() { return rent; }
    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { this.isAvailable = available; }
    public void setRent(double rent) { this.rent = rent; }

   
    public abstract void display();
    public abstract double maintenanceCharge();
    public double computeMonthlyCharge() {
        return rent + maintenanceCharge();
    }
}

class Apartment extends Property {
    private double hoaFee; 
    private int floor;

    public Apartment(String propertyId, String address, double rent, double hoaFee, int floor) {
        super(propertyId, address, "Apartment", rent);
        this.hoaFee = hoaFee;
        this.floor = floor;
    }

    public double getHoaFee() { return hoaFee; }
    public int getFloor() { return floor; }
    public void setHoaFee(double hoaFee) { this.hoaFee = hoaFee; }
    public void setFloor(int floor) { this.floor = floor; }

    @Override
    public double maintenanceCharge() {
        return hoaFee + Math.max(50, 0.02 * rent);
    }

    @Override
    public void display() {
        System.out.printf("[Apartment] %s | %s | Rent: %.2f | HOA: %.2f | Available: %b\n",
                propertyId, address, rent, hoaFee, isAvailable);
    }
}

class House extends Property {
    private double yardMaintenanceFee;
    private boolean hasGarage;

    public House(String propertyId, String address, double rent, double yardMaintenanceFee, boolean hasGarage) {
        super(propertyId, address, "House", rent);
        this.yardMaintenanceFee = yardMaintenanceFee;
        this.hasGarage = hasGarage;
    }

    public double getYardMaintenanceFee() { return yardMaintenanceFee; }
    public boolean hasGarage() { return hasGarage; }
    public void setYardMaintenanceFee(double fee) { this.yardMaintenanceFee = fee; }
    public void setHasGarage(boolean g) { this.hasGarage = g; }

    @Override
    public double maintenanceCharge() {
        // houses have higher yard/structure maintenance
        return yardMaintenanceFee + Math.max(100, 0.03 * rent);
    }

    @Override
    public void display() {
        System.out.printf("[House] %s | %s | Rent: %.2f | YardFee: %.2f | Garage: %b | Available: %b\n",
                propertyId, address, rent, yardMaintenanceFee, hasGarage, isAvailable);
    }
}
class Tenant {
    private String tenantId;
    private String name;
    private String contact;
    private double deposit;
    private List<String> activeLeases; // list of leaseIds

    public Tenant(String tenantId, String name, String contact, double deposit) {
        this.tenantId = tenantId;
        this.name = name;
        this.contact = contact;
        this.deposit = deposit;
        this.activeLeases = new ArrayList<>();
    }
    public double getDeposit() { return deposit; }
    public void addToDeposit(double amount) { this.deposit += amount; }
    public boolean deductFromDeposit(double amount) {
        if (amount <= deposit) {
            deposit -= amount;
            return true;
        }
        return false;
    }

    public String getTenantId() { return tenantId; }
    public String getName() { return name; }
    public String getContact() { return contact; }

    public List<String> getActiveLeases() { return Collections.unmodifiableList(activeLeases); }
    public void addLease(String leaseId) { activeLeases.add(leaseId); }
    public void removeLease(String leaseId) { activeLeases.remove(leaseId); }

    public void display() {
        System.out.printf("Tenant %s (%s) Contact: %s Deposit: %.2f ActiveLeases: %s\n",
                tenantId, name, contact, deposit, activeLeases);
    }
}

enum LeaseStatus { ACTIVE, TERMINATED, PENDING }
class Lease {
    private String leaseId;
    private String propertyId;
    private String tenantId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String rentCycle; // MONTHLY / QUARTERLY etc.
    private LeaseStatus status;
    private double outstandingBalance; // encapsulated

    public Lease(String leaseId, String propertyId, String tenantId,
                 LocalDate startDate, LocalDate endDate, String rentCycle) {
        this.leaseId = leaseId;
        this.propertyId = propertyId;
        this.tenantId = tenantId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.rentCycle = rentCycle;
        this.status = LeaseStatus.PENDING;
        this.outstandingBalance = 0.0;
    }

    public String getLeaseId() { return leaseId; }
    public String getPropertyId() { return propertyId; }
    public String getTenantId() { return tenantId; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public String getRentCycle() { return rentCycle; }
    public LeaseStatus getStatus() { return status; }
    public double getOutstandingBalance() { return outstandingBalance; }

    public boolean activate() {
        if (status == LeaseStatus.PENDING) {
            status = LeaseStatus.ACTIVE;
            return true;
        }
        return false;
    }

    public boolean terminate() {
        if (status == LeaseStatus.ACTIVE) {
            status = LeaseStatus.TERMINATED;
            return true;
        }
        return false;
    }

    public void addCharge(double amount) {
        outstandingBalance += amount;
    }

    public boolean makePayment(double amount) {
        if (amount <= 0) return false;
        outstandingBalance -= amount;
        if (outstandingBalance < 0) outstandingBalance = 0;
        return true;
    }

    public void display() {
        DateTimeFormatter f = DateTimeFormatter.ISO_DATE;
        System.out.printf("Lease %s | Property: %s | Tenant: %s | %s -> %s | Cycle: %s | Status: %s | O/S: %.2f\n",
                leaseId, propertyId, tenantId, startDate.format(f), endDate.format(f), rentCycle, status, outstandingBalance);
    }
}
class RentalService {
    private Map<String, Property> properties = new HashMap<>();
    private Map<String, Tenant> tenants = new HashMap<>();
    private Map<String, Lease> leases = new HashMap<>();

    private double totalIncomeThisMonth = 0.0; // tracking for demo

    public void addProperty(Property p) {
        properties.put(p.getPropertyId(), p);
    }

    
    public void addTenant(Tenant t) {
        tenants.put(t.getTenantId(), t);
    }
    public void listProperties() {
        System.out.println("--- Properties ---");
        for (Property p : properties.values()) {
            p.display();
        }
    }

    public boolean leaseProperty(String leaseId, String propertyId, String tenantId, LocalDate start, LocalDate end, String cycle) {
        Property prop = properties.get(propertyId);
        Tenant t = tenants.get(tenantId);
        if (prop == null || t == null) return false;
        if (!prop.isAvailable()) return false;
        Lease l = new Lease(leaseId, propertyId, tenantId, start, end, cycle);
        leases.put(leaseId, l);
        // activate lease
        l.activate();
        prop.setAvailable(false);
        t.addLease(leaseId);
        System.out.printf("Lease %s created and activated for property %s to tenant %s\n", leaseId, propertyId, tenantId);
        return true;
    }

    public boolean collectRent(String leaseId) {
        Lease l = leases.get(leaseId);
        if (l == null || l.getStatus() != LeaseStatus.ACTIVE) return false;
        Property p = properties.get(l.getPropertyId());
        double amount = p.getRent();
        l.addCharge(amount); // assume monthly charge applied first
        boolean ok = l.makePayment(amount);
        if (ok) {
            totalIncomeThisMonth += amount;
            System.out.printf("Collected full rent %.2f for lease %s\n", amount, leaseId);
        }
        return ok;
    }

    public boolean collectRent(String leaseId, double paidAmount) {
        Lease l = leases.get(leaseId);
        if (l == null || l.getStatus() != LeaseStatus.ACTIVE) return false;
        if (paidAmount <= 0) return false;
        l.addCharge(0); // assume charge already set elsewhere; here we just apply payment
        boolean ok = l.makePayment(paidAmount);
        if (ok) {
            totalIncomeThisMonth += paidAmount;
            System.out.printf("Collected partial payment %.2f for lease %s\n", paidAmount, leaseId);
        }
        return ok;
    }

    public boolean collectRent(String leaseId, int daysLate) {
        Lease l = leases.get(leaseId);
        if (l == null || l.getStatus() != LeaseStatus.ACTIVE) return false;
        Property p = properties.get(l.getPropertyId());
        double base = p.getRent();
        double lateFee = Math.min(0.25 * base, 50 + daysLate * 5); // example rule
        l.addCharge(base + lateFee);
        boolean ok = l.makePayment(base + lateFee);
        if (ok) {
            totalIncomeThisMonth += (base + lateFee);
            System.out.printf("Collected rent %.2f + late fee %.2f for lease %s\n", base, lateFee, leaseId);
        }
        return ok;
    }

    public boolean terminateLease(String leaseId) {
        Lease l = leases.get(leaseId);
        if (l == null) return false;
        if (l.getStatus() != LeaseStatus.ACTIVE) return false;
        boolean ok = l.terminate();
        if (ok) {
            Property p = properties.get(l.getPropertyId());
            Tenant t = tenants.get(l.getTenantId());
            if (p != null) p.setAvailable(true);
            if (t != null) t.removeLease(leaseId);
            System.out.printf("Lease %s terminated\n", leaseId);
        }
        return ok;
    }

   
    public void generateStatementForTenant(String tenantId) {
        Tenant t = tenants.get(tenantId);
        if (t == null) { System.out.println("Tenant not found"); return; }
        System.out.println("--- Statement for " + t.getName() + " ---");
        for (String lid : t.getActiveLeases()) {
            Lease l = leases.get(lid);
            if (l != null) l.display();
        }
    }

    public void printMonthlyIncomeReport() {
        System.out.printf("Total income collected this month: %.2f\n", totalIncomeThisMonth);
    }

    public void printOccupancyReport() {
        long total = properties.size();
        long available = properties.values().stream().filter(Property::isAvailable).count();
        long occupied = total - available;
        System.out.printf("Occupancy: %d/%d occupied, %d available\n", occupied, total, available);
    }

    public double computeTotalMonthlyCharges() {
        double sum = 0;
        for (Property p : properties.values()) {
            sum += p.computeMonthlyCharge(); 
        }
        return sum;
    }

    // Helper to display tenants
    public void listTenants() {
        System.out.println("--- Tenants ---");
        for (Tenant t : tenants.values()) t.display();
    }

  
    public void listLeases() {
        System.out.println("--- Leases ---");
        for (Lease l : leases.values()) l.display();
    }
}


public class RentalAppMain {
    public static void main(String[] args) {
        RentalService service = new RentalService();

        
        Apartment a1 = new Apartment("A101", "12B MG Road, Chennai", 12000, 800, 3);
        Apartment a2 = new Apartment("A102", "15A Anna Salai, Chennai", 15000, 1000, 6);
        House h1 = new House("H201", "7 Green St, Coimbatore", 20000, 1200, true);
        House h2 = new House("H202", "9 Lake View, Madurai", 18000, 900, false);

        service.addProperty(a1);
        service.addProperty(a2);
        service.addProperty(h1);
        service.addProperty(h2);

      
        Tenant t1 = new Tenant("T001", "Ananya", "+91-98765-43210", 24000);
        Tenant t2 = new Tenant("T002", "Rohan", "+91-91234-56789", 18000);
        service.addTenant(t1);
        service.addTenant(t2);

      
        service.listProperties();
        service.listTenants();

     
        service.leaseProperty("L1001", "A101", "T001", LocalDate.of(2025,9,1), LocalDate.of(2026,8,31), "MONTHLY");
        service.leaseProperty("L1002", "H201", "T002", LocalDate.of(2025,9,10), LocalDate.of(2026,9,9), "MONTHLY");

       
        service.listLeases();

       
        service.collectRent("L1001");

        
        service.collectRent("L1002", 8000);

       
        service.collectRent("L1001", 5);

       
        service.generateStatementForTenant("T001");
        service.generateStatementForTenant("T002");

      
        service.printMonthlyIncomeReport();
        service.printOccupancyReport();

       
        double totalCharges = service.computeTotalMonthlyCharges();
        System.out.printf("Total expected monthly charges across all properties (rent + maintenance): %.2f\n", totalCharges);

       
        service.terminateLease("L1002");

     
        service.printOccupancyReport();

   
        service.listProperties();
        service.listLeases();

       
        interactiveConsole(service);
    }

    private static void interactiveConsole(RentalService service) {
        Scanner sc = new Scanner(System.in);
        System.out.println("--- Interactive Console (type 'exit' to quit) ---");
        while (true) {
            System.out.println("Options: listProps | listTenants | listLeases | collect <leaseId> | collectPartial <leaseId> <amt> | collectLate <leaseId> <days> | terminate <leaseId> | income | occupancy | exit");
            System.out.print("Enter: ");
            String line = sc.nextLine().trim();
            if (line.equalsIgnoreCase("exit")) break;
            if (line.isEmpty()) continue;
            String[] parts = line.split(" ");
            try {
                switch (parts[0]) {
                    case "listProps": service.listProperties(); break;
                    case "listTenants": service.listTenants(); break;
                    case "listLeases": service.listLeases(); break;
                    case "collect": if (parts.length>=2) service.collectRent(parts[1]); break;
                    case "collectPartial": if (parts.length>=3) service.collectRent(parts[1], Double.parseDouble(parts[2])); break;
                    case "collectLate": if (parts.length>=3) service.collectRent(parts[1], Integer.parseInt(parts[2])); break;
                    case "terminate": if (parts.length>=2) service.terminateLease(parts[1]); break;
                    case "income": service.printMonthlyIncomeReport(); break;
                    case "occupancy": service.printOccupancyReport(); break;
                    default: System.out.println("Unknown command");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
        sc.close();
    }
}
