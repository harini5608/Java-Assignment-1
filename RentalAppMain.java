package j1;
import java.util.ArrayList;
import java.util.Scanner;

public class RentalAppMain {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        RentalService service = new RentalService();

        // Sample data
        service.addProperty(new Property(1, "Apartment", 15000));
        service.addProperty(new Property(2, "Villa", 30000));
        service.addTenant(new Tenant(1, "Alice"));
        service.addTenant(new Tenant(2, "Bob"));
        service.addLease(new Lease(1, 1));
        service.addLease(new Lease(2, 2));

        System.out.println("Welcome to Rental App!");
        System.out.println("Commands: listProps | income | report | exit");

        while (true) {
            System.out.print("\nEnter command: ");
            String cmd = sc.nextLine().trim().toLowerCase();

            switch (cmd) {
                case "listprops":
                    service.listProperties();
                    break;
                case "income":
                    System.out.println("Total income: ₹" + service.calculateIncome());
                    break;
                case "report":
                    service.showReport();
                    break;
                case "exit":
                    System.out.println("Exiting...");
                    sc.close();
                    return;
                default:
                    System.out.println("Invalid command!");
            }
        }
    }
}

// =================== Supporting Classes ===================

class Property {
    int id;
    String name;
    double rent;

    Property(int id, String name, double rent) {
        this.id = id;
        this.name = name;
        this.rent = rent;
    }
}

class Tenant {
    int id;
    String name;

    Tenant(int id, String name) {
        this.id = id;
        this.name = name;
    }
}

class Lease {
    int propertyId;
    int tenantId;

    Lease(int propertyId, int tenantId) {
        this.propertyId = propertyId;
        this.tenantId = tenantId;
    }
}

class RentalService {
    ArrayList<Property> properties = new ArrayList<>();
    ArrayList<Tenant> tenants = new ArrayList<>();
    ArrayList<Lease> leases = new ArrayList<>();

    void addProperty(Property p) {
        properties.add(p);
    }

    void addTenant(Tenant t) {
        tenants.add(t);
    }

    void addLease(Lease l) {
        leases.add(l);
    }

    void listProperties() {
        if (properties.isEmpty()) {
            System.out.println("No properties found.");
            return;
        }
        for (Property p : properties) {
            System.out.println(p.id + ": " + p.name + " - ₹" + p.rent);
        }
    }

    double calculateIncome() {
        double total = 0;
        for (Lease l : leases) {
            for (Property p : properties) {
                if (p.id == l.propertyId) {
                    total += p.rent;
                }
            }
        }
        return total;
    }

    void showReport() {
        if (leases.isEmpty()) {
            System.out.println("No leases found.");
            return;
        }
        for (Lease l : leases) {
            String tenantName = "";
            String propertyName = "";
            double rent = 0;
            for (Tenant t : tenants) {
                if (t.id == l.tenantId) tenantName = t.name;
            }
            for (Property p : properties) {
                if (p.id == l.propertyId) {
                    propertyName = p.name;
                    rent = p.rent;
                }
            }
            System.out.println(tenantName + " rents " + propertyName + " for ₹" + rent);
        }
    }
}

   
  
