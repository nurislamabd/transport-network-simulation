package simulation;
import java.util.Random;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.File;
import java.util.Scanner;
import datastructures.MyArrayList;

/**
 * Contains the main program and methods which write a random and 
 * user-defined configuration for the simulation to the config file.
 * The program takes user input to let the user decide if he wants 
 * to exit the program, run a simulation with random default 
 * configuration, or run a simulation with user-defined configuration. 
 * If he chooses to run a simulation with random default configuration, 
 * randomConfiguration() is called which creates configuration with
 * random values within default bounds. If he chooses to run a 
 * simulation with user-defined configuration, configure() is called 
 * with user-inputted values as params which creates configuration with
 * user-defined values. User has ability to define only sizes of axis of 
 * the map, number of trucks, warehouses and shipments. After that
 * the simulation is ran and the values are logged into log files. 
 * 
 * After that, the user has a choice to run the simulation again or 
 * exit the program. If he chooses to exit, the program says goodbye 
 * and exits.
 * 
 * 
 * @author Nuris Abdyldaev
 * @version 05/02/2026
 */
public class Main
{
    private static final int DEFAULT_MAP = 990;
    private static final int DEFAULT_WAREHOUSES = 5000;
    private static final int DEFAULT_SHIPMENTS = 10000;
    private static final int DEFAULT_TRUCKS = 5000;
    private static Random random = new Random();
    
    /**
     * By Default, creates a configuration for up to 50 Warehouses, 100 Shipments, 50 Trucks, with map size 
     * between 10 to 100 and writes it to config file.
     */
    public static void randomConfiguration(File file)
    {
        try
        {
            // initialize vars
            BufferedWriter buffer = new BufferedWriter(new FileWriter(file, false));
            boolean handle = true;
            int x = 10 + random.nextInt(DEFAULT_MAP + 1);                // DEFAULT 10-1000
            int y = 10 + random.nextInt(DEFAULT_MAP + 1);                // DEFAULT 10-1000
            int warehouses = 2 + random.nextInt(DEFAULT_WAREHOUSES - 1); // DEFAULT 2-5000
            int shipments = 1 + random.nextInt(DEFAULT_SHIPMENTS);       // DEFAULT 1-10000
            int trucks = 1 + random.nextInt(DEFAULT_TRUCKS);             // DEFAULT 1-5000
            MyArrayList<String> warehousesConfig = new MyArrayList<String>(warehouses);
            MyArrayList<String> shipmentsConfig = new MyArrayList<String>(shipments);
            MyArrayList<String> trucksConfig = new MyArrayList<String>(trucks);
            
            // trucks config without manifest
            for (int i = 0; i < trucks; i++) {
                int size = 2 + random.nextInt(4); // 2 - 5
                if (size >= 3) handle = false;
                trucksConfig.add("Truck," + random.nextDouble() * x + "," + random.nextDouble() * y + "," + size + ",");
            }
            
            // warehouses config without manifest
            for (int i = 0; i < warehouses; i++) {
                int docks = 1 + random.nextInt(3); // 1 - 3
                warehousesConfig.add("Warehouse," + random.nextDouble() * x + "," + random.nextDouble() * y + "," + docks);
            }
            
            // shipments config without manifest
            for (int i = 1; i <= shipments; i++) {
                int size = 1 + random.nextInt(3); // 1 - 3
                int source = 1 + random.nextInt(warehouses);
                int manifest = random.nextInt(trucks); // index
                int destination = 1 + random.nextInt(warehouses);
                while (destination == source) {
                    destination = 1 + random.nextInt(warehouses);
                }
                shipmentsConfig.add("Shipment," + source + "," + destination + "," + size + ",");
                
                // handling the situation when there are no trucks with size of 3 or more and there are shipments with size of 3
                if (handle && size > 2) {
                    int truckToBeChanged = random.nextInt(trucks);
                    int newSize = 3 + random.nextInt(3);
                    String[] prevConfig = trucksConfig.get(truckToBeChanged).split(",");
                    String newConfig = "Truck," + random.nextDouble() * x + "," + random.nextDouble() * y + "," + newSize + ",";
                    for (int j = 4; j < prevConfig.length; j++) newConfig += prevConfig[j] + ",";
                    trucksConfig.set(truckToBeChanged, newConfig);
                }
                
                // add shipment to random truck's manifest; first checks size issue case (shipment size = 3 and truck load size = 2 case)
                if (size == 3) {
                    String[] currTruck = trucksConfig.get(manifest).split(",");
                    while (Integer.parseInt(currTruck[3]) < 3) {
                        manifest = random.nextInt(trucks);
                        currTruck = trucksConfig.get(manifest).split(",");
                    }
                }
                trucksConfig.set(manifest, trucksConfig.get(manifest) + i + ",");
            }
            
            
            // map x & y
            buffer.append(x + "," + y);
            buffer.newLine();
            
            // numbers of trucks, warehouses and shipments
            buffer.append(warehouses + "," + shipments + "," + trucks);
            buffer.newLine();
            
            
            // warehouse objects
            for (int i = 0; i < warehouses; i++) {
                buffer.append(warehousesConfig.get(i));
                buffer.newLine();
            }
            
            // shipments objects
            for (int i = 0; i < shipments; i++) {
                buffer.append(shipmentsConfig.get(i));
                buffer.newLine();
            }
            
            // truck objects
            for (int i = 0; i < trucks; i++) {
                buffer.append(trucksConfig.get(i));
                buffer.newLine();
            }
            
            buffer.close();
        } catch (java.io.IOException ioe) {
            System.err.println("Failed to open/find the file config.txt");
        } 
    }
    
    /**
     * Creates a configuration based on user's chosen values for the map size and number of simulation objects.
     * Doesn't allow illegal value (i.e. less than 2 warehouses - at least 1 source and 1 destination needed, negative values, etc.).
     * 
     * @param  file                configuration file, where the configuration will be written
     * @param  mapX                size of x-axis of the map (should be at least 1 and at max 1000)
     * @param  mapY                size of y-axis of the map (should be at least 1 and at max 1000)
     * @param  numberOfTrucks      number of trucks used for simulation (should be at least 1 and at max 5000)
     * @param  numberOfWarehouses  number of warehouses used for simulation (should be at least 2 and at max 5000)
     * @param  numberOfShipments   number of shipments used for simulation (should be at least 1 and at max 10000)
     * @throws IllegalArgumentException if the params are too big/small
     */
    public static void configure(File file, int mapX, int mapY, int numberOfTrucks, int numberOfWarehouses, int numberOfShipments)
    {
        // check the input vars
        if (mapX > 1000 || mapX < 1)                             throw new IllegalArgumentException("Map size (x-axis) is too big/small");
        if (mapY > 1000 || mapY < 1)                             throw new IllegalArgumentException("Map size (y-axis) is too big/small");
        if (numberOfTrucks > 5000 || numberOfTrucks < 1)         throw new IllegalArgumentException("Number of trucks is too big/small");
        if (numberOfWarehouses > 5000 || numberOfWarehouses < 2) throw new IllegalArgumentException("Number of warehouses is too big/small");
        if (numberOfShipments > 10000 || numberOfShipments < 1)  throw new IllegalArgumentException("Number of shipments is too big/small");
        
        try
        {
            // initialize vars
            BufferedWriter buffer = new BufferedWriter(new FileWriter(file, false));
            boolean handle = true;
            int x = mapX;               
            int y = mapY;            
            int warehouses = numberOfWarehouses;    
            int shipments = numberOfShipments;      
            int trucks = numberOfTrucks;            
            MyArrayList<String> warehousesConfig = new MyArrayList<String>(warehouses);
            MyArrayList<String> shipmentsConfig = new MyArrayList<String>(shipments);
            MyArrayList<String> trucksConfig = new MyArrayList<String>(trucks);
            
            // trucks config without manifest
            for (int i = 0; i < trucks; i++) {
                int size = 2 + random.nextInt(4); // 2 - 5
                if (size >= 3) handle = false;
                trucksConfig.add("Truck," + random.nextDouble() * x + "," + random.nextDouble() * y + "," + size + ",");
            }
            
            // warehouses config without manifest
            for (int i = 0; i < warehouses; i++) {
                int docks = 1 + random.nextInt(3); // 1 - 3
                warehousesConfig.add("Warehouse," + random.nextDouble() * x + "," + random.nextDouble() * y + "," + docks);
            }
            
            // shipments config without manifest
            for (int i = 1; i <= shipments; i++) {
                int size = 1 + random.nextInt(3); // 1 - 3
                int source = 1 + random.nextInt(warehouses);
                int manifest = random.nextInt(trucks); // index
                int destination = 1 + random.nextInt(warehouses);
                while (destination == source) {
                    destination = 1 + random.nextInt(warehouses);
                }
                shipmentsConfig.add("Shipment," + source + "," + destination + "," + size + ",");
                
                // handling the situation when there are no trucks with size of 3 or more and there are shipments with size of 3
                if (handle && size > 2) {
                    int truckToBeChanged = random.nextInt(trucks);
                    int newSize = 3 + random.nextInt(3);
                    String[] prevConfig = trucksConfig.get(truckToBeChanged).split(",");
                    String newConfig = "Truck," + random.nextDouble() * x + "," + random.nextDouble() * y + "," + newSize + ",";
                    for (int j = 4; j < prevConfig.length; j++) newConfig += prevConfig[j] + ",";
                    trucksConfig.set(truckToBeChanged, newConfig);
                }
                
                // add shipment to random truck's manifest; first checks size issue case (shipment size = 3 and truck load size = 2 case)
                if (size == 3) {
                    String[] currTruck = trucksConfig.get(manifest).split(",");
                    while (Integer.parseInt(currTruck[3]) < 3) {
                        manifest = random.nextInt(trucks);
                        currTruck = trucksConfig.get(manifest).split(",");
                    }
                }
                trucksConfig.set(manifest, trucksConfig.get(manifest) + i + ",");
            }
            
            // map x & y
            buffer.append(x + "," + y);
            buffer.newLine();
            
            // numbers of trucks, warehouses and shipments
            buffer.append(warehouses + "," + shipments + "," + trucks);
            buffer.newLine();
            
            // warehouse objects
            for (int i = 0; i < warehouses; i++) {
                buffer.append(warehousesConfig.get(i));
                buffer.newLine();
            }
            
            // shipments objects
            for (int i = 0; i < shipments; i++) {
                buffer.append(shipmentsConfig.get(i));
                buffer.newLine();
            }
            
            // truck objects
            for (int i = 0; i < trucks; i++) {
                buffer.append(trucksConfig.get(i));
                buffer.newLine();
            }
            
            buffer.close();
        } catch (java.io.IOException ioe) {
            System.err.println("Failed to open/find the file config.txt");
        } 
    }
    
    /**
     * Main program which takes user input to let the user decide if he wants 
     * to exit the program, run a simulation with random default 
     * configuration, or run a simulation with user-defined configuration. 
     * If he chooses to run a simulation with random default configuration, 
     * randomConfiguration() is called which creates configuration with
     * random values within default bounds. If he chooses to run a 
     * simulation with user-defined configuration, configure() is called 
     * with user-inputted values as params which creates configuration with
     * user-defined values. User has ability to define only sizes of axis of 
     * the map, number of trucks, warehouses and shipments. After that
     * the simulation is ran and the values are logged into log files. 
     * 
     * After that, the user has a choice to run the simulation again or 
     * exit the program. If he chooses to exit, the program says goodbye 
     * and exits.
     *
     * @param  args  is not used
     */
    public static void main(String[] args)
    {
        // scanner and variables initialization
        int menu;
        String line;
        long startTime;
        long endTime;
        double durationMs;
        Scanner sc = new Scanner(System.in);
        
        // welcome message and menu
        System.out.println("Welcome to CS150 Transport Simulation!");
        System.out.println("");
        System.out.println("Transport Simulator: Select Configuration Type");
        System.out.println("Please choose: 1 (default random configuration)  2 (your own configuration)  3 (exit)");

        // handle user input issues
        menu = -1;
        line = sc.nextLine();
        while (true) {
            try {
                menu = Integer.parseInt(line);
                
                // if user input for menu selection is not an int 1-3 then ask again
                while (menu > 3 || menu < 1) {
                    // error message & print menu again
                    System.out.println("Invalid selection. Please try again.");
                    System.out.println("");
                    System.out.println("Transport Simulator: Select Configuration Type");    
                    System.out.println("Please choose: 1 (default random configuration)  2 (your own configuration)  3 (exit)");
                
                    // user choice input      
                    line = sc.nextLine();
                    menu = Integer.parseInt(line);
                }
                
                break;
            } catch (NumberFormatException e) {
                // error message & print menu again
                System.out.println("Invalid format of the integer. Please try again.");
                System.out.println("");
                System.out.println("Transport Simulator: Select Configuration Type");
                System.out.println("Please choose: 1 (default random configuration)  2 (your own configuration)  3 (exit)");
                
                // user choice input      
                line = sc.nextLine();
            }
        }
        
        // while user is not choosing to exit, create simulations
        while (menu != 3) {
            // if menu == 1, create default random configuration
            if (menu == 1) {
                // random config simulation
                startTime = System.nanoTime();
                randomConfiguration(new File("config.txt"));
                Simulation simulation = new Simulation(new File("config.txt"));
                System.out.println("");
                System.out.println("Simulating...");
                simulation.simulate();
                endTime = System.nanoTime();
                durationMs = (endTime - startTime) / 1000000; 
                System.out.printf("Estimated Runtime: %.3f seconds\n", durationMs / 1000);
                System.out.println("Check log files for more info.");
                
                System.out.println("");
                
                // print menu again
                System.out.println("");
                System.out.println("Transport Simulator: Select Configuration Type");
                System.out.println("Please choose: 1 (default random configuration)  2 (your own configuration)  3 (exit)");
                
                // user choice input      
                line = sc.nextLine();
                
                while (true) {
                    try {
                        menu = Integer.parseInt(line);
                        
                        // if user input for menu selection is not an int 1-3 then ask again
                        while (menu > 3 || menu < 1) {
                            // error message & print menu again
                            System.out.println("Invalid selection. Please try again.");
                            System.out.println("");
                            System.out.println("Transport Simulator: Select Configuration Type");
                            System.out.println("Please choose: 1 (default random configuration)  2 (your own configuration)  3 (exit)");
                        
                            // user choice input      
                            line = sc.nextLine();
                            menu = Integer.parseInt(line);
                        }
                        
                        break;
                    } catch (NumberFormatException e) {
                        // error message & print menu again
                        System.out.println("Invalid format of the integer. Please try again.");
                        System.out.println("");
                        System.out.println("Transport Simulator: Select Configuration Type");
                        System.out.println("Please choose: 1 (default random configuration)  2 (your own configuration)  3 (exit)");
                        
                        // user choice input      
                        line = sc.nextLine();
                    }
                }
            } // if menu == 2, create user's configuration
            else if (menu == 2) {
                int mapX;               
                int mapY;                 
                int numberOfTrucks; 
                int numberOfWarehouses;    
                int numberOfShipments; 
                
                // mapX
                System.out.println("Please type in your value for x-axis of the map (should be between 1 and 1000): ");
                line = sc.nextLine();
                while (true) {
                    try {
                        mapX = Integer.parseInt(line);
                        
                        // if user input is invalid
                        while (mapX > 1000 || mapX < 1) {
                            // error message & print menu again
                            System.out.println("Invalid selection. Please try again.");
                            System.out.println("");
                            System.out.println("Please type in your value for x-axis of the map (should be between 1 and 1000): ");
                            line = sc.nextLine();
                            mapX = Integer.parseInt(line);
                        }
                        break;
                    } catch (NumberFormatException e) {
                        // error message & print menu again
                        System.out.println("Invalid format of the integer. Please try again.");
                        System.out.println("");
                        System.out.println("Please type in your value for x-axis of the map (should be between 1 and 1000): ");
                        line = sc.nextLine();
                    }
                }
                
                // mapY
                System.out.println("Please type in your value for y-axis of the map (should be between 1 and 1000): ");
                line = sc.nextLine();
                while (true) {
                    try {
                        mapY = Integer.parseInt(line);
                        
                        // if user input is invalid
                        while (mapY > 1000 || mapY < 1) {
                            // error message & print menu again
                            System.out.println("Invalid selection. Please try again.");
                            System.out.println("");
                            System.out.println("Please type in your value for y-axis of the map (should be between 1 and 1000): ");
                            line = sc.nextLine();
                            mapY = Integer.parseInt(line);
                        }
                        break;
                    } catch (NumberFormatException e) {
                        // error message & print menu again
                        System.out.println("Invalid format of the integer. Please try again.");
                        System.out.println("");
                        System.out.println("Please type in your value for y-axis of the map (should be between 1 and 1000): ");
                        line = sc.nextLine();
                    }
                }
                
                // numberOfTrucks
                System.out.println("Please type in your value for the number of trucks (should be between 1 and 5000): ");
                line = sc.nextLine();
                while (true) {
                    try {
                        numberOfTrucks = Integer.parseInt(line);
                        
                        // if user input is invalid
                        while (numberOfTrucks > 5000 || numberOfTrucks < 1) {
                            // error message & print menu again
                            System.out.println("Invalid selection. Please try again.");
                            System.out.println("");
                            System.out.println("Please type in your value for the number of trucks (should be between 1 and 5000): ");
                            line = sc.nextLine();
                            numberOfTrucks = Integer.parseInt(line);
                        }
                        break;
                    } catch (NumberFormatException e) {
                        // error message & print menu again
                        System.out.println("Invalid format of the integer. Please try again.");
                        System.out.println("");
                        System.out.println("Please type in your value for the number of trucks (should be between 1 and 5000): ");
                        line = sc.nextLine();
                    }
                }
                
                // numberOfWarehouses
                System.out.println("Please type in your value for the number of warehouses (should be between 2 and 5000): ");
                line = sc.nextLine();
                while (true) {
                    try {
                        numberOfWarehouses = Integer.parseInt(line);
                        
                        // if user input is invalid
                        while (numberOfWarehouses > 5000 || numberOfWarehouses < 2) {
                            // error message & print menu again
                            System.out.println("Invalid selection. Please try again.");
                            System.out.println("");
                            System.out.println("Please type in your value for the number of warehouses (should be between 2 and 5000): ");
                            line = sc.nextLine();
                            numberOfWarehouses = Integer.parseInt(line);
                        }
                        break;
                    } catch (NumberFormatException e) {
                        // error message & print menu again
                        System.out.println("Invalid format of the integer. Please try again.");
                        System.out.println("");
                        System.out.println("Please type in your value for the number of warehouses (should be between 2 and 5000): ");
                        line = sc.nextLine();
                    }
                }
                
                // numberOfShipments
                System.out.println("Please type in your value for the number of shipments (should be between 1 and 10000): ");
                line = sc.nextLine();
                while (true) {
                    try {
                        numberOfShipments = Integer.parseInt(line);
                        
                        // if user input is invalid
                        while (numberOfShipments > 10000 || numberOfShipments < 1) {
                            // error message & print menu again
                            System.out.println("Invalid selection. Please try again.");
                            System.out.println("");
                            System.out.println("Please type in your value for the number of shipments (should be between 1 and 10000): ");
                            line = sc.nextLine();
                            numberOfShipments = Integer.parseInt(line);
                        }
                        break;
                    } catch (NumberFormatException e) {
                        // error message & print menu again
                        System.out.println("Invalid format of the integer. Please try again.");
                        System.out.println("");
                        System.out.println("Please type in your value for the number of shipments (should be between 1 and 10000): ");
                        line = sc.nextLine();
                    }
                }
                
                startTime = System.nanoTime();
                configure(new File("config.txt"), mapX, mapY, numberOfTrucks, numberOfWarehouses, numberOfShipments);
                Simulation simulation = new Simulation(new File("config.txt"));
                System.out.println("");
                System.out.println("Simulating...");
                simulation.simulate();
                endTime = System.nanoTime();
                durationMs = (endTime - startTime) / 1000000; 
                System.out.printf("Estimated Runtime: %.3f seconds\n", durationMs / 1000);
                System.out.println("Check log files for more info.");
                System.out.println("");
                
                // print menu again
                System.out.println("");
                System.out.println("Transport Simulator: Select Configuration Type");
                System.out.println("Please choose: 1 (default random configuration)  2 (your own configuration)  3 (exit)");
                
                // user choice input      
                line = sc.nextLine();
                
                while (true) {
                    try {
                        menu = Integer.parseInt(line);
                        
                        // if user input for menu selection is not an int 1-3 then ask again
                        while (menu > 3 || menu < 1) {
                            // error message & print menu again
                            System.out.println("Invalid selection. Please try again.");
                            System.out.println("");
                            System.out.println("Transport Simulator: Select Configuration Type");
                            System.out.println("Please choose: 1 (default random configuration)  2 (your own configuration)  3 (exit)");
                        
                            // user choice input      
                            line = sc.nextLine();
                            menu = Integer.parseInt(line);
                        }
                        
                        break;
                    } catch (NumberFormatException e) {
                        // error message & print menu again
                        System.out.println("Invalid format of the integer. Please try again.");
                        System.out.println("");
                        System.out.println("Transport Simulator: Select Configuration Type");
                        System.out.println("Please choose: 1 (default random configuration)  2 (your own configuration)  3 (exit)");
                        
                        // user choice input      
                        line = sc.nextLine();
                    }
                }
            }
        }
        
        // Goodbye and exit program
        System.out.println("Goodbye!");
    }
}



