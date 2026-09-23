import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;
import java.sql.*;
import java.util.*;
import oracle.jdbc.driver.*;
import org.apache.ibatis.jdbc.ScriptRunner;

public class Student{
    static Connection con;
    static Statement stmt;
    static Scanner scanner = new Scanner(System.in);

    public static void main(String argv[])
    {
	connectToDatabase();

    if(con != null){
        loadScript();
        menuScreen();
    }
    closeAll();
    }

    public static void closeAll(){
        try {
            if(stmt != null) {
                stmt.close();
            }
            if(con != null){
                con.close();
            }
            if(scanner != null){
                scanner.close();
            }
        } catch (Exception e) {
            System.out.println("Error closing");
        }
    }

    public static void menuScreen(){
        boolean exitMenu = false;

        while(!exitMenu){
            System.out.println("***********************************************");
            System.out.println("**Enter Selection with the # of the option*****");
            System.out.println("1: View Table Contents");
            System.out.println("2: Search Bikes");
            System.out.println("3: Show Active Rentals");
            System.out.println("4: Rent a Bike");
            System.out.println("5: Return a Bike");
            System.out.println("6: Exit");
            System.out.print("Choice: ");

            String userOption = scanner.nextLine().trim();

            switch (userOption) {
                case "1":
                    displayTable();
                    break;
                case "2":
                    searchBikes();
                    break;
                case "3":
                    activeRentals();
                    break;
                case "4":
                    rentBike();
                    break;
                case "5":
                    returnBike();
                    break;
                case "6":
                    exitMenu = true;
                    System.out.println("Exiting");
                    break;
                default:
                    System.out.println("Please Pick a Valid Option 1-6");
            }
        }
    }

    public static String getRequiredInput(String option, String errorMessage){
        String userInput = "";

        while(userInput.isEmpty()){
            System.out.print(option);
            userInput = scanner.nextLine().trim();

            if(userInput.isEmpty()){
                System.out.println(errorMessage);
            }
        }
        return userInput;
    }

    public static void resetTransaction(){
        try {
            con.rollback();
            con.setAutoCommit(true);
        } catch (Exception e) {
            System.out.println("Error resetting transaction");
        }
    }


    public static void returnBike(){
        System.out.println("***********************************************");
        String bikeID = getRequiredInput("Enter a BikeID: ", "Please enter a BikeID");
        String studentID = getRequiredInput("Enter a StudentID: ", "Please enter a StudentID");

        try{
            con.setAutoCommit(false);

            String bikeQuery = "SELECT Status FROM Bikes WHERE BikeID = ?";
            String bikeStatus;

            try(PreparedStatement preparedStatement = con.prepareStatement(bikeQuery)){
                preparedStatement.setString(1, bikeID);
                try(ResultSet resultSet = preparedStatement.executeQuery()){
                    if(resultSet.next()){
                        bikeStatus = resultSet.getString("Status");
                    } else {
                        System.out.println("Bike not found.");
                        resetTransaction();
                        return;
                    }
                }
            }

            String activeCountQuery = "SELECT COUNT(*) AS activeCount FROM Rentals WHERE BikeID = ? AND EndTime IS NULL";
            int activeCount = 0;

            try(PreparedStatement preparedStatement = con.prepareStatement(activeCountQuery)){
                preparedStatement.setString(1, bikeID);
                try(ResultSet resultSet = preparedStatement.executeQuery()){
                    if(resultSet.next()){
                        activeCount = resultSet.getInt("activeCount");
                    }
                }
            }

            if(activeCount > 1){
                System.out.println("Error: inconsistent data. Multiple active rentals found by this BikeID.");
                resetTransaction();
                return;
            }

            if(!"Rented".equalsIgnoreCase(bikeStatus)){
                System.out.println("No active rental found.");
                resetTransaction();
                return;
            }

            String checkRentalsQuery = "SELECT COUNT(*) AS matchingRentals " + "FROM Rentals " + "WHERE BikeID = ? AND StudentID = ? AND EndTime IS NULL";
            int matchingRentals = 0;

            try(PreparedStatement preparedStatement = con.prepareStatement(checkRentalsQuery)){
                preparedStatement.setString(1, bikeID);
                preparedStatement.setString(2, studentID);

                try(ResultSet resultSet = preparedStatement.executeQuery()){
                    if(resultSet.next()){
                        matchingRentals = resultSet.getInt("matchingRentals");
                    }
                }
            }

            if(matchingRentals == 0){
                System.out.println("No active rental found.");
                resetTransaction();
                return;
            }

            String returnQuery = "UPDATE Rentals " + "SET EndTime = SYSTIMESTAMP " + "WHERE BikeID = ? AND StudentID = ? AND EndTime IS NULL";

            try(PreparedStatement preparedStatement = con.prepareStatement(returnQuery)){
                preparedStatement.setString(1, bikeID);
                preparedStatement.setString(2, studentID);
                preparedStatement.executeUpdate();
            }

            String updateBikeQuery = "UPDATE Bikes SET Status = 'Available' WHERE BikeID = ?";
            try(PreparedStatement preparedStatement = con.prepareStatement(updateBikeQuery)){
                preparedStatement.setString(1, bikeID);
                preparedStatement.executeUpdate();
            }

            con.commit();
            con.setAutoCommit(true);
            System.out.println("Bike returned successfully.");

        } catch (Exception e){
            resetTransaction();
            System.out.println("Error returning bike.");
            e.printStackTrace();
        }
    }

    public static void rentBike(){
        System.out.println("***********************************************");
        String bikeID = getRequiredInput("Enter a BikeID: ", "Please enter a BikeID, cannot be blank");
        String studentID = getRequiredInput("Enter a StudentID: ", "Please enter a StudentID, cannot be blank");

        try{
            con.setAutoCommit(false);

            String findBikeQuery = "SELECT Status FROM Bikes WHERE BikeID = ?";
            String bikeStatus;

            try(PreparedStatement preparedStatement = con.prepareStatement(findBikeQuery)){
                preparedStatement.setString(1, bikeID);
                try(ResultSet resultSet = preparedStatement.executeQuery()){
                    if(resultSet.next()){
                        bikeStatus = resultSet.getString("Status");
                    } else {
                        System.out.println("Bike not found.");
                        resetTransaction();
                        return;
                    }
                }
            }

            if(!"Available".equalsIgnoreCase(bikeStatus)){
                System.out.println("Bike is not available.");
                resetTransaction();
                return;
            }

            String activeRentals = "SELECT COUNT(*) AS currentlyActive FROM Rentals WHERE BikeID = ? AND EndTime IS NULL";
            int currentlyActive = 0;

            try(PreparedStatement preparedStatement = con.prepareStatement(activeRentals)){
                preparedStatement.setString(1, bikeID);
                try(ResultSet resultSet = preparedStatement.executeQuery()){
                    if (resultSet.next()){
                        currentlyActive = resultSet.getInt("currentlyActive");
                    }
                }
            }

            if(currentlyActive > 0){
                System.out.println("Bike is not available.");
                resetTransaction();
                return;
            }
            
            String insertRental = "INSERT INTO Rentals (BikeID, StudentID, StartTime, EndTime) VALUES (?, ?, SYSTIMESTAMP, NULL)";

            try(PreparedStatement preparedStatement = con.prepareStatement(insertRental)){
                preparedStatement.setString(1, bikeID);
                preparedStatement.setString(2, studentID);
                preparedStatement.executeUpdate();
            }

            String updateBike = "UPDATE Bikes SET Status = 'Rented' WHERE BikeID = ?";
            try(PreparedStatement preparedStatement = con.prepareStatement(updateBike)){
                preparedStatement.setString(1, bikeID);
                preparedStatement.executeUpdate();
            }

            con.commit();
            con.setAutoCommit(true);
            System.out.println("Bike was rented");

        } catch (Exception e){
            resetTransaction();
            System.out.println("Error ocurred renting bike.");
            e.printStackTrace();
        }
    }

    public static void activeRentals(){
        while(true){
            String findBy;
            System.out.println();
            System.out.println("***********************************************");
            System.out.println("********What would you Like to Search by*******");
            System.out.println("**Enter Selection with the # of the option*****");
            System.out.println("1: Search by BikeID");
            System.out.println("2: Search by Type");
            System.out.print("Choice: ");

            findBy = scanner.nextLine().trim();

            if(!findBy.equals("1") && !findBy.equals("2")){
                System.out.println("Enter either 1 or 2 please");
                continue;
            }

            if(findBy.equals("1")){
                String bikeID = getRequiredInput("Enter BikeID: ", "BikeID cannot be blank");

                String query = "SELECT StudentID FROM Rentals WHERE BikeID = ? AND EndTime IS NULL";
                try(PreparedStatement preparedStatement = con.prepareStatement(query)){
                    preparedStatement.setString(1, bikeID);
                    try(ResultSet resultSet = preparedStatement.executeQuery()){
                        int counter = 0;
                        String studentID = "";

                        while(resultSet.next()){
                            counter++;
                            studentID = resultSet.getString("StudentID");
                        }

                        if(counter > 1){
                            System.out.println("Error inconsistent data. Multiple active rentals found for this BikeID");
                        } else if (counter == 0){
                            System.out.println("0 active rentals");
                        } else {
                            System.out.println("StudentID currently renting " + bikeID + ": " + studentID);
                        }
                    }

                } catch (Exception e){
                    System.out.println("Error showing active rentals");
                }
                return;
            }

            if(findBy.equals("2")){
                String bikeType = getRequiredInput("Enter Bike Type: ", "Type cannot be blank");

                String bikeQuery = "SELECT r.BikeID, r.StudentID " + "FROM Rentals r JOIN Bikes b ON r.BikeID = b.BikeID " + 
                "WHERE UPPER(b.Type) = UPPER(?) AND r.EndTime IS NULL " + "ORDER BY r.BikeID";

                try(PreparedStatement preparedStatement = con.prepareStatement(bikeQuery)){
                    preparedStatement.setString(1, bikeType);

                    try(ResultSet resultSet = preparedStatement.executeQuery()){
                        LinkedHashMap<String, String> activeRentals = new LinkedHashMap<>();

                        while(resultSet.next()){
                            String currentBikeID = resultSet.getString("BikeID");
                            String studentID = resultSet.getString("StudentID");

                            if(activeRentals.containsKey(currentBikeID)){
                                System.out.println("Error inconsistent data, Multiple active rentals for the same BikeID");
                                return;
                            }
                            activeRentals.put(currentBikeID, studentID);
                        }

                        if(activeRentals.isEmpty()){
                            System.out.println("0 active rentals.");
                        } else {
                            System.out.println("BikeID, StudentID");
                            System.out.println();

                            for(Map.Entry<String, String> entry : activeRentals.entrySet()){
                                System.out.println(entry.getKey() + ", " + entry.getValue());
                            }
                            System.out.println("Total active rentals: " + activeRentals.size());
                        }
                    }
                } catch (Exception e){
                    System.out.println("Error showing active rentals");
                }
                return;
            }
        }
    }

    public static void searchBikes(){
        while(true){
            String bikeID;
            String bikeType;
            String bikeStatus;
            String bikeLocation;
            String bikeRate;

            System.out.println();
            System.out.println("***********************************************");
            System.out.println("Leave blank to skip");
            System.out.print("BikeID: ");
            bikeID = scanner.nextLine().trim();

            System.out.print("Type: ");
            bikeType = scanner.nextLine().trim();
            System.out.print("Status: ");
            bikeStatus = scanner.nextLine().trim();
            System.out.print("Location: ");
            bikeLocation = scanner.nextLine().trim();
            System.out.print("HourlyRate: ");
            bikeRate = scanner.nextLine().trim();

            if(bikeID.isEmpty() && bikeType.isEmpty() && bikeStatus.isEmpty() && bikeLocation.isEmpty() && bikeRate.isEmpty()){
                System.out.println("Please fill out at least one filter option");
                continue;
            }

            if(!bikeStatus.isEmpty()){
                if(!bikeStatus.equalsIgnoreCase("Available") && !bikeStatus.equalsIgnoreCase("Rented")){
                    System.out.println("Invalid Status Enter: Available or Rented");
                    continue;
                }
            }

            StringBuilder bikeQuery = new StringBuilder("SELECT * FROM Bikes WHERE 1=1");
            ArrayList<Object> attributes = new ArrayList<>();

            if(!bikeID.isEmpty()){
                bikeQuery.append(" AND BikeID = ?");
                attributes.add(bikeID);
            }

            if(!bikeType.isEmpty()){
                bikeQuery.append(" AND Type LIKE ?");
                attributes.add("%" + bikeType + "%");
            }

            if(!bikeStatus.isEmpty()){
                bikeQuery.append(" AND Status = ?");
                attributes.add(bikeStatus);
            }

            if(!bikeLocation.isEmpty()){
                bikeQuery.append(" AND Location LIKE ?");
                attributes.add("%" + bikeLocation + "%");
            }

            if(!bikeRate.isEmpty()){
                try {
                    double hourlyRate = Double.parseDouble(bikeRate);
                    bikeQuery.append(" AND HourlyRate = ?");
                    attributes.add(hourlyRate);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid HourlyRate Enter a valid number");
                    continue;
                }
            }

            //
            try(PreparedStatement preparedStatement = con.prepareStatement(bikeQuery.toString())) {
                for(int i = 0; i < attributes.size(); i++){
                    preparedStatement.setObject(i + 1, attributes.get(i));
                }

                try(ResultSet resultSet = preparedStatement.executeQuery()) {
                    ResultSetMetaData data = resultSet.getMetaData();
                    int columns = data.getColumnCount();

                    for(int i = 1; i <= columns; i++){
                        System.out.print(data.getColumnName(i));
                        if(i < columns){
                            System.out.print(", ");
                        }
                    }
                    System.out.println();
                    System.out.println();
                    boolean found = false;

                    while(resultSet.next()){
                        found = true;
                        for(int i = 1; i <= columns; i++){
                            System.out.print(resultSet.getString(i));
                            if(i < columns){
                                System.out.print(", ");
                            }
                        }
                        System.out.println();
                    }
                    if(!found){
                        System.out.println("No matching bikes found");
                    }   
                }
            } catch (Exception e) {
                System.out.println("Error searching bikes");
            }
            return;
        }
    }

    public static void displayTable(){
        String tableQuery;

        while(true){
            System.out.println("Which Table Would You Like To View");
            System.out.println("1: Bikes");
            System.out.println("2: Rentals");
            System.out.println("Enter Selection with the # of the option");

            String tablePicked = scanner.nextLine().trim();

            if(tablePicked.equals("1")){
                tableQuery = "SELECT * FROM Bikes";
                break;
            } else if (tablePicked.equals("2")){
                tableQuery = "SELECT * FROM Rentals";
                break;
            } else {
                System.out.println("Please Pick a Valid Option 1 or 2");
            }
        }
        
        try(ResultSet resultSet = stmt.executeQuery(tableQuery)){
            ResultSetMetaData data = resultSet.getMetaData();
            int columns = data.getColumnCount();

            for(int i = 1; i <= columns; i++){
                System.out.print(data.getColumnName(i));
                if(i < columns){
                    System.out.print(", ");
                }
            }
            System.out.println();
            System.out.println();
            boolean found = false;

            while(resultSet.next()){
                found = true;
                for(int i = 1; i <= columns; i++){
                    System.out.print(resultSet.getString(i));
                    if(i < columns){
                        System.out.print(", ");
                    }
                }
                System.out.println();
            }

            if(!found){
                System.out.println("No records found");
            }
        } catch (Exception e) {
            System.out.println("Could not load table.");
        }
    }

    public static void loadScript() {
        System.out.print("Enter File Path (leave blank to skip): ");
        String userFilePath = scanner.nextLine().trim();

        if (userFilePath.isEmpty()) {
            System.out.println("Skipping script load.");
            return;
        }

        try (Reader reader = new BufferedReader(new FileReader(userFilePath))) {

            org.apache.ibatis.jdbc.ScriptRunner scriptRunner =
                new org.apache.ibatis.jdbc.ScriptRunner(con);

            scriptRunner.runScript(reader);
            System.out.println("Script Ran Successfully");

        } catch (Exception e) {
            System.out.println("Failed to run Script");
            e.printStackTrace();
        }
    }

    public static void connectToDatabase()
    {
	String driverPrefixURL="jdbc:oracle:thin:@";
	String jdbc_url="artemis.vsnet.gmu.edu:1521/vse18c.vsnet.gmu.edu";
	
        String username="";
        String password="";

        
        System.out.print("Enter Username: ");
        username = scanner.nextLine().trim();
        System.out.print("Enter Password: ");
        password = scanner.nextLine().trim();
        
	
        try{
	    //Register Oracle driver
            DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
        } catch (Exception e) {
            System.out.println("Failed to load JDBC/ODBC driver.");
            return;
        }

       try{
            System.out.println(driverPrefixURL+jdbc_url);
            con=DriverManager.getConnection(driverPrefixURL+jdbc_url, username, password);
            DatabaseMetaData dbmd=con.getMetaData();
            stmt=con.createStatement();

            System.out.println("Connected.");

            if(dbmd==null){
                System.out.println("No database meta data");
            }
            else {
                System.out.println("Database Product Name: "+dbmd.getDatabaseProductName());
                System.out.println("Database Product Version: "+dbmd.getDatabaseProductVersion());
                System.out.println("Database Driver Name: "+dbmd.getDriverName());
                System.out.println("Database Driver Version: "+dbmd.getDriverVersion());
            }
        }catch( Exception e) {e.printStackTrace();}

    }// End of connectToDatabase()
}// End of class

